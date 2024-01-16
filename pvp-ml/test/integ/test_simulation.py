from pvp_ml.env.remote_env_connector import RemoteEnvConnector
from pvp_ml.env.simulation import Simulation


def test_simulation_launches() -> None:
    with Simulation(game_port=20000, remote_env_port=21000) as simulation:
        assert simulation.process is not None
        assert simulation.is_running()
        assert not simulation.is_loaded()
        simulation.wait_until_loaded()
        assert simulation.is_loaded()
    assert simulation.process is None
    assert not simulation.is_running()
    assert not simulation.is_loaded()


async def test_communicate_simulation() -> None:
    with Simulation(game_port=20001, remote_env_port=21001) as simulation:
        simulation.wait_until_loaded()
        async with RemoteEnvConnector(
            env_id="test", port=simulation.remote_env_port
        ) as remote_env_connector:
            debug_response = await remote_env_connector.send(action="debug")
            assert debug_response is not None
