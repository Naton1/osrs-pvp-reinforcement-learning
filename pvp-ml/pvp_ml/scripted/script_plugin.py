import abc
from typing import Any


class ScriptPlugin(abc.ABC):
    @abc.abstractmethod
    def predict(self, **kwargs: Any) -> dict[str, str]:
        """
        All environment observations/action masks will be passed in as kwargs,
        based on the observation/action ID in the environment contract. Past frames, if configured in the environment,
        will be available with the format `frame_x_obs` where x is the past frame index, and obs is the observation ID.

        The result must be a dictionary of actions, where the key is the action head ID,
        and the value is the action ID for that action head.
        """
        pass
