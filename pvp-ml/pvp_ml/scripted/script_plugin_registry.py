from pvp_ml.scripted.plugins.baseline_plugin import BaselinePlugin
from pvp_ml.scripted.plugins.no_op_plugin import NoOpPlugin
from pvp_ml.scripted.plugins.stay_alive_in_combat_plugin import StayAliveInCombatPlugin
from pvp_ml.scripted.script_plugin import ScriptPlugin
from pvp_ml.scripted.script_plugin_adapter import ScriptPluginAdapter
from pvp_ml.util.contract_loader import load_environment_contract

_registry: dict[str, ScriptPluginAdapter] = {}


def is_scripted_plugin(plugin_name: str) -> bool:
    return plugin_name in _registry


def get_scripted_plugin(plugin_name: str) -> ScriptPluginAdapter:
    assert is_scripted_plugin(plugin_name), f"Unknown plugin: {plugin_name}"
    return _registry[plugin_name]


def register(env_type: str, plugin_name: str, plugin_type: type[ScriptPlugin]) -> None:
    assert plugin_name not in _registry, f"Plugin name already in use: {plugin_name}"
    _registry[plugin_name] = ScriptPluginAdapter(
        plugin_type(), env_type, load_environment_contract(env_type)
    )


def _register() -> None:
    # Register defaults
    register("NhEnv", "baseline", BaselinePlugin)
    register("NhEnv", "noop", NoOpPlugin)
    register("NhEnv", "stayalive", StayAliveInCombatPlugin)


_register()
