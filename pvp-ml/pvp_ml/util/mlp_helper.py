from typing import TypedDict

import torch.nn as nn


class MlpLayer(TypedDict, total=False):
    size: int
    activation: str
    dropout: float
    repeat: int


class MlpConfig(TypedDict, total=False):
    layers: list[MlpLayer]


def init_weights(module: nn.Module, gain: float = 1.0, bias: float = 0.0) -> None:
    if isinstance(module, nn.Linear):
        nn.init.orthogonal_(module.weight, gain=gain)
        nn.init.constant_(module.bias, bias)


def default_mlp_config(
    sizes: list[int], activation: str = "relu", activate_last_layer: bool = True
) -> MlpConfig:
    return MlpConfig(
        layers=[
            MlpLayer(
                size=size,
                **(
                    {"activation": activation}
                    if i < len(sizes) - 1 or activate_last_layer
                    else {}
                ),
            )
            for i, size in enumerate(sizes)
        ]
    )


def create_mlp(config: MlpConfig, required_input_size: int) -> tuple[nn.Module, int]:
    """
    Parses schema like

    config:
        layers:
            - size: 64
              activation: relu
            - size: 32
              activation: relu

    into a multi-layer perceptron.
    The size corresponds to the output layer size, the input layer size is taken from the previous layer.

    :param config: the mlp config
    :param required_input_size: the first layer input size
    :return: a multi-layer perceptron as defined in the config, and the final layer output size
    """

    layers = config.get("layers", [])
    modules: list[nn.Module] = []

    last_output = required_input_size

    activations = {
        "relu": nn.ReLU,
        "tanh": nn.Tanh,
        "gelu": nn.GELU,
    }

    for layer in layers:
        for _ in range(0, layer.get("repeat", 1)):
            if "dropout" in layer:
                assert "size" not in layer, "Dropout layer can't have size"
                modules.append(nn.Dropout(layer["dropout"]))
                continue
            else:
                # Default case - linear layer
                linear = nn.Linear(last_output, layer["size"])
                modules.append(linear)
                last_output = layer["size"]

            if "activation" in layer:
                if layer["activation"] not in activations:
                    raise ValueError(layer["activation"])
                activation = activations[layer["activation"]]()
                modules.append(activation)

    return nn.Sequential(*modules), last_output
