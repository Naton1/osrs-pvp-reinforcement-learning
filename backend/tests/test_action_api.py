"""Tests for action API endpoints with item name resolution."""

import pytest


@pytest.mark.asyncio
async def test_create_action_with_item_name_inputs(test_client, mock_db):
    """POST /actions/ with inputs using item_name resolves to correct item_source_id."""
    response = await test_client.post(
        "/actions/",
        json={
            "name": "Test Spell",
            "action_type": "spell",
            "inputs": [{"item_name": "Nature rune", "quantity": 1}],
        },
    )
    assert response.status_code == 200

    # Verify item.find_first was called with the item name
    mock_db.item.find_first.assert_any_call(where={"name": "Nature rune"})

    # Verify action.create was called with resolved item_source_id
    create_call = mock_db.action.create.call_args
    create_data = create_call.kwargs.get("data") or create_call[1].get("data") or create_call[0][0] if create_call[0] else None
    if create_data is None:
        create_data = create_call.kwargs["data"]

    input_creates = create_data["inputs"]["create"]
    assert any(inp["item_source_id"] == 561 for inp in input_creates)


@pytest.mark.asyncio
async def test_create_action_with_item_name_outputs(test_client, mock_db):
    """POST /actions/ with outputs using item_name resolves to correct item_source_id."""
    response = await test_client.post(
        "/actions/",
        json={
            "name": "Test Craft",
            "action_type": "activity",
            "outputs": [{"item_name": "Oak plank", "quantity": 1}],
        },
    )
    assert response.status_code == 200

    mock_db.item.find_first.assert_any_call(where={"name": "Oak plank"})

    create_call = mock_db.action.create.call_args
    create_data = create_call.kwargs.get("data") or create_call[1].get("data")
    if create_data is None and create_call[0]:
        create_data = create_call[0][0]
    if create_data is None:
        create_data = create_call.kwargs["data"]

    output_creates = create_data["outputs"]["create"]
    assert any(out["item_source_id"] == 8778 for out in output_creates)


@pytest.mark.asyncio
async def test_create_action_unknown_item_name_returns_404(test_client, mock_db):
    """POST /actions/ with a nonexistent item name returns 404."""
    response = await test_client.post(
        "/actions/",
        json={
            "name": "Bad Action",
            "action_type": "spell",
            "inputs": [{"item_name": "Nonexistent item", "quantity": 1}],
        },
    )
    assert response.status_code == 404


@pytest.mark.asyncio
async def test_create_action_cost_defaults_to_zero(test_client, mock_db):
    """POST /actions/ without cost field stores cost as 0.0."""
    response = await test_client.post(
        "/actions/",
        json={
            "name": "Free Action",
            "action_type": "activity",
        },
    )
    assert response.status_code == 200

    create_call = mock_db.action.create.call_args
    create_data = create_call.kwargs.get("data") or create_call[1].get("data")
    if create_data is None and create_call[0]:
        create_data = create_call[0][0]
    if create_data is None:
        create_data = create_call.kwargs["data"]

    assert create_data["cost"] == 0.0


@pytest.mark.asyncio
async def test_add_input_by_item_name(test_client, mock_db):
    """POST /actions/{id}/inputs with item_name resolves correctly."""
    response = await test_client.post(
        "/actions/1/inputs",
        json={"item_name": "Oak logs", "quantity": 5},
    )
    assert response.status_code == 200

    # Verify item name was looked up
    mock_db.item.find_first.assert_any_call(where={"name": "Oak logs"})

    # Verify actioninput.create was called with resolved source_id
    create_call = mock_db.actioninput.create.call_args
    create_data = create_call.kwargs.get("data") or create_call[1].get("data")
    if create_data is None and create_call[0]:
        create_data = create_call[0][0]
    if create_data is None:
        create_data = create_call.kwargs["data"]

    assert create_data["item_source_id"] == 1521
    assert create_data["quantity"] == 5


@pytest.mark.asyncio
async def test_add_output_by_item_name(test_client, mock_db):
    """POST /actions/{id}/outputs with item_name resolves correctly."""
    response = await test_client.post(
        "/actions/1/outputs",
        json={"item_name": "Oak plank", "quantity": 5},
    )
    assert response.status_code == 200

    mock_db.item.find_first.assert_any_call(where={"name": "Oak plank"})

    create_call = mock_db.actionoutput.create.call_args
    create_data = create_call.kwargs.get("data") or create_call[1].get("data")
    if create_data is None and create_call[0]:
        create_data = create_call[0][0]
    if create_data is None:
        create_data = create_call.kwargs["data"]

    assert create_data["item_source_id"] == 8778
    assert create_data["quantity"] == 5


@pytest.mark.asyncio
async def test_add_input_unknown_item_returns_404(test_client, mock_db):
    """POST /actions/{id}/inputs with unknown name returns 404."""
    response = await test_client.post(
        "/actions/1/inputs",
        json={"item_name": "Fake item", "quantity": 1},
    )
    assert response.status_code == 404
