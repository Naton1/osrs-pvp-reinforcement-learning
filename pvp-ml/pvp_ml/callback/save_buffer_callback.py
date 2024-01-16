from pvp_ml.callback.callback import Callback
from pvp_ml.ppo.buffer import Buffer
from pvp_ml.util.compression_helper import save_compressed_pickle_to_file
from pvp_ml.util.files import get_experiment_dir


class SaveBufferCallback(Callback):
    def __init__(
        self, experiment_name: str, output_file_name: str = "last_rollout_buffer.zip"
    ):
        super(SaveBufferCallback, self).__init__()
        self._experiment_name = experiment_name
        self._output_file_name = output_file_name

    def on_rollout_sampling_end(self, buffer: Buffer) -> None:
        super().on_rollout_sampling_end(buffer)
        # Note that this saves the raw buffer, not processed yet
        save_path = (
            f"{get_experiment_dir(self._experiment_name)}/{self._output_file_name}"
        )
        save_compressed_pickle_to_file(buffer, save_path)
