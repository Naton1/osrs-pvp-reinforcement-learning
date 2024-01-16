from typing import Any

import torch as th

from pvp_ml.scripted.script_plugin import ScriptPlugin
from pvp_ml.util.contract_loader import EnvironmentMeta


class ScriptPluginAdapter:
    def __init__(
        self, plugin: ScriptPlugin, environment_name: str, environment: EnvironmentMeta
    ):
        self._environment = environment
        self._environment_name = environment_name
        self._plugin = plugin

    def get_env_name(self) -> str:
        return self._environment_name

    def predict(self, observations: th.Tensor, action_masks: th.Tensor) -> th.Tensor:
        assert (
            len(observations.shape) == 3
        ), f"Observations shape invalid: {observations.shape}"  # batch, frame_stack, obs
        assert (
            len(action_masks.shape) == 2
        ), f"Action masks shape invalid: {action_masks.shape}"  # batch, masks
        assert observations.size(0) == action_masks.size(
            0
        ), f"Observations size {observations.size(0)} != action masks size {action_masks.size(0)}"

        results = []
        for obs, mask in zip(observations, action_masks):
            kwargs = self._convert_params(obs, mask)
            result = self._plugin.predict(**kwargs)
            results.append(self._convert_result(result, kwargs))

        return th.stack(results)

    def _convert_params(
        self, observations: th.Tensor, action_masks: th.Tensor
    ) -> dict[str, Any]:
        kwargs = {}

        # Convert obs to params based on ID, and support frame stacking
        for i, f in enumerate(observations):
            for j, o in enumerate(f):
                key = self._environment.observations[j].id
                if i > 0:
                    key = f"frame_{i}_{key}"
                kwargs[key] = o

        # Add action masks based on action ID
        action_head_sizes = [
            len(action_head.actions) for action_head in self._environment.actions
        ]
        flattened_masks = th.split(action_masks, action_head_sizes)
        for i, a in enumerate(flattened_masks):
            for j, m in enumerate(a):
                kwargs[self._environment.actions[i].actions[j].id] = j

        return kwargs

    def _convert_result(
        self, result: dict[str, str], input_kwargs: dict[str, Any]
    ) -> th.Tensor:
        actions = []
        for action in self._environment.actions:
            if action.id not in result:
                # No action provided, use no-op action
                actions.append(0)
                continue
            selected_action_id = result[action.id]
            # Ensure action is available via action masks, or is no-op action
            assert selected_action_id == action.actions[0].id or input_kwargs.get(
                selected_action_id
            ), f"Selected action for {action.id}: {selected_action_id} is not available"
            # Remove the key, so we can keep track of known actions
            del result[action.id]
            action_index = next(
                (
                    i
                    for i, action in enumerate(action.actions)
                    if action.id == selected_action_id
                ),
                None,
            )
            assert (
                action_index is not None
            ), f"Unknown action for {action.id}: {selected_action_id}"
            actions.append(action_index)
        # There should be no keys left, otherwise they are invalid
        assert not result, f"Unknown action keys: {result}"
        return th.tensor(actions)
