from torch.utils.tensorboard import SummaryWriter

from pvp_ml.callback.callback import Callback
from pvp_ml.callback.callback_list import CallbackList
from pvp_ml.env.async_io_vec_env import AsyncIoVecEnv
from pvp_ml.ppo.ppo import PPO
from pvp_ml.ppo.rollout_sampler import RolloutSampler
from pvp_ml.util.schedule import ConstantSchedule, Schedule


class Trainer:
    def train(
        self,
        ppo: PPO,
        env: AsyncIoVecEnv,
        rollout_sampler: RolloutSampler,
        n_rollouts: int = 10,
        n_steps: int = 4096,
        batch_size: Schedule[int] = ConstantSchedule(64),
        grad_accum: Schedule[int] = ConstantSchedule(1),
        eps_greedy: Schedule[float] = ConstantSchedule(0.0),
        num_updates: Schedule[int] = ConstantSchedule(5),
        gae_lambda: Schedule[float] = ConstantSchedule(0.95),
        gamma: Schedule[float] = ConstantSchedule(0.99),
        learning_rate: Schedule[float] = ConstantSchedule(0.0003),
        clip_coef: Schedule[float] = ConstantSchedule(0.2),
        value_coef: Schedule[float] = ConstantSchedule(0.5),
        entropy_coef: Schedule[float] = ConstantSchedule(0.0),
        max_grad_norm: Schedule[float] = ConstantSchedule(0.5),
        novelty_reward_scale: Schedule[float] = ConstantSchedule(0.0),
        normalize_advantages: bool = True,
        normalize_rewards: bool = False,
        callbacks: list[Callback] = [],
        summary_writer: SummaryWriter | None = None,
    ) -> None:
        callback = CallbackList(callbacks)
        callback.initialize(summary_writer, ppo)

        callback.on_training_start()

        for i in range(0, n_rollouts):
            callback.on_rollout_start()
            buffer = rollout_sampler.collect(
                env,
                ppo,
                n_steps,
                callback,
                eps_greedy=eps_greedy.value(ppo.meta.trained_rollouts),
                gae_lambda=gae_lambda.value(ppo.meta.trained_rollouts),
                gamma=gamma.value(ppo.meta.trained_rollouts),
                normalize_rewards=normalize_rewards,
                summary_writer=summary_writer,
                novelty_reward_scale=novelty_reward_scale.value(
                    ppo.meta.trained_rollouts
                ),
            )
            callback.on_rollout_end(buffer)
            ppo.learn(
                buffer,
                summary_writer=summary_writer,
                num_updates=num_updates.value(ppo.meta.trained_rollouts),
                grad_accum=grad_accum.value(ppo.meta.trained_rollouts),
                batch_size=batch_size.value(ppo.meta.trained_rollouts),
                learning_rate=learning_rate.value(ppo.meta.trained_rollouts),
                clip_coef=clip_coef.value(ppo.meta.trained_rollouts),
                vf_coef=value_coef.value(ppo.meta.trained_rollouts),
                entropy_coef=entropy_coef.value(ppo.meta.trained_rollouts),
                max_grad_norm=max_grad_norm.value(ppo.meta.trained_rollouts),
                normalize_advantages=normalize_advantages,
            )
            callback.on_learn_end()

        callback.on_training_end()
