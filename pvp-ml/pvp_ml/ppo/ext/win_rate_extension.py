import logging
import random
from typing import Any, cast

import numpy as np
import torch as th
import torch.nn as nn
from torch.utils.data import DataLoader, TensorDataset
from torch.utils.tensorboard import SummaryWriter

from pvp_ml.ppo.buffer import Buffer
from pvp_ml.ppo.ppo import Meta, ModelExtension
from pvp_ml.util.mlp_helper import MlpConfig, create_mlp, default_mlp_config

logger = logging.getLogger(__name__)

WON_LABEL = 1
LOST_LABEL = 0

TERMINAL_STATE_LABELS = {
    "WON": WON_LABEL,
    "LOST": LOST_LABEL,
}

EXTENSION_LABELS = {
    "WIN": WON_LABEL,
    "LOSE": LOST_LABEL,
}


class WinRateClassifier(nn.Module):
    def __init__(self, input_size: int, mlp_config: MlpConfig):
        super(WinRateClassifier, self).__init__()
        output_dim = len(TERMINAL_STATE_LABELS)
        mlp, mlp_out = create_mlp(mlp_config, input_size)
        self.net = nn.Sequential(
            mlp,
            nn.Linear(mlp_out, output_dim),
        )

    def forward(self, x: th.Tensor) -> th.Tensor:
        return cast(th.Tensor, self.net(x))


class WinRateExtension(ModelExtension):
    def __init__(
        self,
        input_size: int,
        max_sequence_length: int = 1,
        epochs_per_learn: int = 1,
        max_steps_per_episode: int | None = 5,
        balance_outcomes: bool = False,
        batch_size: int = 64,
        classifier_state: dict[str, Any] = {},
        test_size: float = 0.1,
        optimizer_state: dict[str, Any] | None = {},
        mlp_config: MlpConfig = default_mlp_config([64, 64]),
    ):
        super().__init__()
        self._input_size = input_size
        self._max_sequence_length = max_sequence_length
        self._epochs_per_learn = epochs_per_learn
        self._batch_size = batch_size
        self._mlp_config = mlp_config
        self._classifier = WinRateClassifier(
            input_size=input_size * max_sequence_length, mlp_config=mlp_config
        )
        self._classifier.eval()
        self._test_size = test_size
        self._max_steps_per_episode = max_steps_per_episode
        self._balance_outcomes = balance_outcomes
        if classifier_state:
            self._classifier.load_state_dict(classifier_state)
        self._optimizer: th.optim.Adam | None
        if optimizer_state is not None:
            self._optimizer = th.optim.Adam(self._classifier.parameters())
            if optimizer_state:
                self._optimizer.load_state_dict(optimizer_state)
        else:
            self._optimizer = None

    def state_dict(self) -> dict[str, Any]:
        return {
            "mlp_config": self._mlp_config,
            "input_size": self._input_size,
            "max_sequence_length": self._max_sequence_length,
            "epochs_per_learn": self._epochs_per_learn,
            "max_steps_per_episode": self._max_steps_per_episode,
            "balance_outcomes": self._balance_outcomes,
            "batch_size": self._batch_size,
            "test_size": self._test_size,
            "classifier_state": self._classifier.state_dict(),
            "optimizer_state": self._optimizer.state_dict()
            if self._optimizer is not None
            else None,
        }

    def run_extension(self, obs: th.Tensor) -> dict[str, list[float]]:
        # Limit input size to expected amount
        obs = obs[..., : self._max_sequence_length, : self._input_size]
        # Flatten frame stacking (last 2 dims)
        obs = obs.reshape(*obs.shape[:-2], -1)
        logits = self._classifier(obs)
        probs = th.softmax(logits, dim=-1)
        return {
            state: probs[..., value].tolist()
            for state, value in EXTENSION_LABELS.items()
        }

    def to(self, device: str) -> None:
        self._classifier.to(device)
        if self._optimizer is not None:
            # This moves the optimizer to the device of the parameters
            self._optimizer.load_state_dict(self._optimizer.state_dict())

    def eval(self) -> None:
        self._optimizer = None

    @classmethod
    def optimize_for_inference(cls, state_dict: dict[str, Any]) -> None:
        state_dict.pop("optimizer_state", None)

    def learn(
        self, buffer: Buffer, meta: Meta, summary_writer: SummaryWriter | None
    ) -> None:
        assert self._optimizer is not None, "Cannot train without an optimizer"
        criterion = nn.CrossEntropyLoss()

        observations, labels = self._build_training_data(buffer, meta)
        data_set_size = len(labels)
        logger.info(f"Generated {data_set_size} data points to train on")
        if summary_writer is not None:
            summary_writer.add_scalar(
                "train/ext/win_rate_data_length", data_set_size, meta.trained_steps
            )
        if data_set_size == 0:
            # No episodes completed
            return

        # Select in order (not random) so test data doesn't contain data from same episode that was trained on
        test_size = int(self._test_size * data_set_size)
        if test_size == 0 or data_set_size - test_size == 0:
            # Not enough data for split
            return
        training_data = TensorDataset(observations[:test_size], labels[:test_size])
        testing_data = TensorDataset(observations[test_size:], labels[test_size:])

        train_loader = DataLoader(
            training_data, shuffle=True, batch_size=self._batch_size
        )
        test_loader = DataLoader(testing_data, batch_size=self._batch_size)

        for epoch in range(self._epochs_per_learn):
            # Train
            self._classifier.train()
            train_loss = 0.0
            for i, (inputs, labels) in enumerate(train_loader):
                self._optimizer.zero_grad()
                outputs = self._classifier(inputs)
                loss = criterion(outputs, labels)
                loss.backward()
                self._optimizer.step()
                train_loss += loss.item()
            train_loss /= len(train_loader)
            logger.info(
                f"[{epoch}] Match outcome classification train loss: {train_loss}"
            )

            # Eval
            self._classifier.eval()
            test_loss = 0.0
            correct = 0
            total = 0
            with th.no_grad():
                for inputs, labels in test_loader:
                    outputs = self._classifier(inputs)
                    loss = criterion(outputs, labels)
                    test_loss += loss.item()
                    _, predicted = th.max(outputs, dim=1)
                    total += labels.size(0)
                    correct += int((predicted == labels).sum().item())
            test_loss /= len(test_loader)
            accuracy = correct / total
            logger.info(
                f"[{epoch}] Match outcome classification test loss: {test_loss}"
            )
            logger.info(
                f"[{epoch}] Match outcome classification test accuracy: {accuracy}"
            )

            # Track tensorboard stats on last epoch
            if epoch == self._epochs_per_learn - 1 and summary_writer is not None:
                summary_writer.add_scalar(
                    "train/ext/win_rate_train_loss",
                    train_loss,
                    meta.trained_steps,
                )
                summary_writer.add_scalar(
                    "train/ext/win_rate_test_loss", test_loss, meta.trained_steps
                )
                summary_writer.add_scalar(
                    "train/ext/win_rate_test_accuracy", accuracy, meta.trained_steps
                )

    def _build_training_data(
        self, buffer: Buffer, meta: Meta
    ) -> tuple[th.Tensor, th.Tensor]:
        # Extract observations and pair them with the eventual outcome of the fight
        # Collect outcomes
        episode_outcomes: dict[str, str] = {}
        for steps in buffer.infos:
            for env_step in steps:
                if "terminal_state" in env_step:
                    episode_outcomes[env_step["episode_id"]] = env_step[
                        "terminal_state"
                    ]
        # Balance outcomes so that every # of outcomes is equal
        if self._balance_outcomes:
            episode_id_by_outcomes: dict[str, list[str]] = {}
            for episode_id, outcome in episode_outcomes.items():
                if outcome not in episode_id_by_outcomes:
                    episode_id_by_outcomes[outcome] = []
                episode_id_by_outcomes[outcome].append(episode_id)
            min_outcomes = min(len(ids) for ids in episode_id_by_outcomes.values())
            episode_outcomes = {}
            for outcome, episode_ids in episode_id_by_outcomes.items():
                for episode_id in np.random.choice(
                    episode_ids, size=min_outcomes, replace=False
                ):
                    episode_outcomes[episode_id] = outcome
        # Map episode IDs to list of corresponding observation indices
        episode_indices_map: dict[str, list[int]] = {}
        for i, steps in enumerate(buffer.infos):
            for j, env_step in enumerate(steps):
                episode_id = env_step["episode_id"]
                if episode_id not in episode_indices_map:
                    episode_indices_map[episode_id] = []
                episode_indices_map[episode_id].append(i * buffer.n_envs + j)
        # Randomize outcome order
        tmp_list = list(episode_outcomes.items())
        random.shuffle(tmp_list)
        episode_outcomes = dict(tmp_list)
        # Sample data, limiting by max inputs per episode
        observations = buffer.observations.reshape(
            buffer.buffer_size * buffer.n_envs, *buffer.observation_space.shape
        )
        sampled_observations = []
        sampled_labels = []
        for episode_id, outcome in episode_outcomes.items():
            label = TERMINAL_STATE_LABELS[outcome]
            episode_obs_indices = episode_indices_map[episode_id]
            sample_size = (
                min(self._max_steps_per_episode, len(episode_obs_indices))
                if self._max_steps_per_episode
                else len(episode_obs_indices)
            )
            sampled_indices = np.random.choice(
                episode_obs_indices, size=sample_size, replace=False
            )
            for idx in sampled_indices:
                sampled_observations.append(observations[idx])
                sampled_labels.append(label)
        # Convert data into tensors
        observation_tensor = th.as_tensor(
            np.array(sampled_observations),
            dtype=th.float32,
            device=meta.running_observation_stats.mean.device,
        )
        if meta.normalized_observations:
            observation_tensor = meta.running_observation_stats.normalize(
                observation_tensor, clip=True
            )
        observation_tensor = observation_tensor[
            ..., : self._input_size
        ]  # Remove global obs, if critic sees full game
        observation_tensor = observation_tensor.reshape(
            -1,
            self._max_sequence_length * self._input_size,
        )  # Flatten all episodes, and frame stacking
        labels_tensor = th.tensor(
            sampled_labels,
            dtype=th.long,
            device=meta.running_observation_stats.mean.device,
        )
        return observation_tensor, labels_tensor
