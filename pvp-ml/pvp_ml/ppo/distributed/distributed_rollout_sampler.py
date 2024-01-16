import logging
import uuid
import zipfile
from typing import Any

from ray.actor import ActorHandle

from pvp_ml.callback.callback import Callback
from pvp_ml.env.async_io_vec_env import AsyncIoVecEnv
from pvp_ml.ppo.buffer import Buffer, merge_buffers
from pvp_ml.ppo.ppo import PPO, Meta
from pvp_ml.ppo.rollout_sampler import RolloutSampler
from pvp_ml.util import ray_helper
from pvp_ml.util.compression_helper import create_zip, decompress_and_unpickle
from pvp_ml.util.files import get_experiment_dir

logger = logging.getLogger(__name__)


class DistributedRolloutSampler(RolloutSampler):
    def __init__(
        self,
        experiment_name: str,
        preset: str,
        num_tasks: int = 0,
        cpus_per_rollout: int = 4,
        include_additional_experiments: set[str] = set(),
    ):
        assert (
            preset
        ), "Distributed rollout preset must be provided for distributed rollouts"
        self._experiment_name = experiment_name
        self._preset = preset
        self._num_tasks = num_tasks
        self._cpus_per_rollout = cpus_per_rollout
        self._sync_experiments = {experiment_name, *include_additional_experiments}
        self._rollout_id = str(uuid.uuid4())

    def _sample_rollout(
        self,
        env: AsyncIoVecEnv,
        ppo: PPO,
        steps: int,
        callback: Callback,
        eps_greedy: float = 0.0,
        gae_lambda: float = 0.95,
        gamma: float = 0.99,
    ) -> Buffer:
        logger.info(f"Running distributed rollout {self._rollout_id}")

        import ray

        from pvp_ml.ppo.distributed.distributed_rollout import RolloutActor

        ray_helper.init()

        resources = ray.cluster_resources()
        num_cpus = resources["CPU"]
        num_available_tasks = (
            int(num_cpus // self._cpus_per_rollout)
            if self._cpus_per_rollout != 0
            else 0
        )

        num_tasks = self._num_tasks

        if num_tasks <= 0:
            num_tasks = num_available_tasks

        if num_tasks > num_available_tasks:
            # Request exactly what is needed, so the autoscaler doesn't think more work is coming and over-allocate
            ray.autoscaler.sdk.request_resources(
                num_cpus=self._cpus_per_rollout * num_tasks
            )

        logger.info(
            f"Running {num_tasks} distributed rollouts,"
            f" which requires {self._cpus_per_rollout * num_tasks} CPUs"
            f" (currently have {num_cpus} CPUs)"
        )

        actors: dict[ActorHandle, int] = {
            RolloutActor.options(
                num_cpus=self._cpus_per_rollout,
                name=f"{self._experiment_name}-{i}-{self._rollout_id}",
            ).remote(f"{self._experiment_name}-{i}-{self._rollout_id}", self._preset): i
            for i in range(num_tasks)
        }

        logger.info("Syncing experiments across actors")
        for experiment in self._sync_experiments:
            self._sync_experiment(experiment, actors)
        logger.info(
            f"Finished syncing experiments {self._sync_experiments} to {len(actors)} actors"
        )

        logger.info(f"Collecting {len(actors)} rollouts")
        jobs = [actor.collect_rollout.remote() for actor in actors.keys()]

        buffers: list[Buffer] = []
        metas: list[Meta] = []
        while jobs:
            done_ids, jobs = ray.wait(jobs)
            logger.info(
                f"{len(done_ids)} more rollouts finished, {len(jobs)} rollouts remaining, {len(buffers)} already finished"
            )
            for done_id in done_ids:
                try:
                    buffer_bytes, meta_bytes = ray.get(done_id)
                except ray.exceptions.RayTaskError:
                    logger.exception("Job threw exception when collecting rollout")
                    continue
                buffer = decompress_and_unpickle(buffer_bytes)
                buffers.append(buffer)
                meta = decompress_and_unpickle(meta_bytes)
                metas.append(meta)
                logger.info(f"Collected rollout - {len(buffers)} collected total")

        if not buffers:
            raise ValueError("No job completed successfully")

        callback.on_distributed_rollout_collection(metas)

        logger.info(f"Collected {len(buffers)} rollouts")

        return merge_buffers(buffers)

    def _sync_experiment(self, experiment_name: str, actors: dict[Any, int]) -> None:
        import ray

        logger.info(f"Zipping experiment {experiment_name}...")
        experiment_bytes_ref = ray.put(
            create_zip(
                get_experiment_dir(experiment_name), compression=zipfile.ZIP_STORED
            )
        )
        logger.info(f"Stored experiment zip for {experiment_name}")

        logger.info(f"Syncing experiments {experiment_name}")
        sync_job_to_actor = {
            actor.sync_experiment.remote(
                experiment_bytes_ref, f"{experiment_name}-{i}-{self._rollout_id}"
            ): actor
            for actor, i in actors.items()
        }
        logger.info(f"Sent experiment sync jobs for {experiment_name}...")

        del experiment_bytes_ref

        while sync_job_to_actor:
            done_ids, _ = ray.wait(list(sync_job_to_actor.keys()))
            for done_id in done_ids:
                actor = sync_job_to_actor[done_id]
                del sync_job_to_actor[done_id]
                try:
                    ray.get(done_id)
                except Exception:
                    logger.exception(
                        f"Sync failed for actor and experiment {experiment_name}, skipping {actor}"
                    )
                    del actors[actor]
        logger.info(f"Experiments synced for {experiment_name}: {len(actors)}")
