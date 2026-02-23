"""Test fixtures for backend tests."""

import pytest
import pytest_asyncio
from unittest.mock import AsyncMock
from contextlib import asynccontextmanager
from datetime import datetime, timezone
from httpx import AsyncClient, ASGITransport
from fastapi import FastAPI

from prisma.models import Action as PrismaAction, ActionInput as PrismaActionInput, ActionOutput as PrismaActionOutput, Item as PrismaItem

from osrs_backend.api.app import create_app
from osrs_backend.db.database import get_db


NOW = datetime.now(timezone.utc)


def _make_item(id: int, source_id: int, name: str) -> PrismaItem:
    return PrismaItem.model_construct(
        id=id,
        source_id=source_id,
        name=name,
        is_members=False,
        low_alch_price=0,
        high_alch_price=0,
        ge_buy_limit=0,
        examine_text="",
        item_type="MATERIAL",
        track_price=True,
        created_at=NOW,
        updated_at=NOW,
    )


# Known test items
TEST_ITEMS = {
    "Nature rune": _make_item(1, 561, "Nature rune"),
    "Oak logs": _make_item(2, 1521, "Oak logs"),
    "Oak plank": _make_item(3, 8778, "Oak plank"),
}

TEST_ITEMS_BY_SOURCE_ID = {item.source_id: item for item in TEST_ITEMS.values()}


@asynccontextmanager
async def noop_lifespan(app: FastAPI):
    """No-op lifespan for testing (skips DB/Redis connections)."""
    yield


@pytest.fixture
def mock_db():
    """Create a mock Prisma client with pre-configured responses."""
    db = AsyncMock()

    # Mock item.find_first (name lookup)
    async def mock_find_first(where=None, **kwargs):
        if where and "name" in where:
            return TEST_ITEMS.get(where["name"])
        return None

    db.item.find_first = AsyncMock(side_effect=mock_find_first)

    # Mock item.find_unique (source_id lookup)
    async def mock_find_unique_item(where=None, **kwargs):
        if where and "source_id" in where:
            return TEST_ITEMS_BY_SOURCE_ID.get(where["source_id"])
        return None

    db.item.find_unique = AsyncMock(side_effect=mock_find_unique_item)

    # Mock action.create
    async def mock_action_create(data=None, include=None, **kwargs):
        return PrismaAction.model_construct(
            id=1,
            name=data.get("name", "Test Action"),
            action_type=data.get("action_type", "spell"),
            cost=data.get("cost", 0.0),
            cost_per_hour=0.0,
            actions_per_hour=data.get("actions_per_hour", 1),
            experience=data.get("experience", 0),
            experience_per_hour=0,
            requirements=[],
            inputs=[],
            outputs=[],
            created_at=NOW,
            updated_at=NOW,
        )

    db.action.create = AsyncMock(side_effect=mock_action_create)

    # Mock action.find_unique (for add_input/add_output action existence check)
    db.action.find_unique = AsyncMock(
        return_value=PrismaAction.model_construct(
            id=1,
            name="Test Action",
            action_type="spell",
            cost=0.0,
            cost_per_hour=0.0,
            actions_per_hour=1,
            experience=0,
            experience_per_hour=0,
            requirements=[],
            inputs=[],
            outputs=[],
            created_at=NOW,
            updated_at=NOW,
        )
    )

    # Mock actioninput.create
    async def mock_actioninput_create(data=None, include=None, **kwargs):
        source_id = data.get("item_source_id", 0)
        item = TEST_ITEMS_BY_SOURCE_ID.get(source_id)
        return PrismaActionInput.model_construct(
            id=1,
            action_id=data.get("action_id", 1),
            item_source_id=source_id,
            quantity=data.get("quantity", 1),
            created_at=NOW,
            action=None,
            item=item,
        )

    db.actioninput.create = AsyncMock(side_effect=mock_actioninput_create)

    # Mock actionoutput.create
    async def mock_actionoutput_create(data=None, include=None, **kwargs):
        source_id = data.get("item_source_id", 0)
        item = TEST_ITEMS_BY_SOURCE_ID.get(source_id)
        return PrismaActionOutput.model_construct(
            id=1,
            action_id=data.get("action_id", 1),
            item_source_id=source_id,
            quantity=data.get("quantity", 1),
            created_at=NOW,
            action=None,
            item=item,
        )

    db.actionoutput.create = AsyncMock(side_effect=mock_actionoutput_create)

    return db


@pytest_asyncio.fixture
async def test_client(mock_db):
    """Create an async test client with mocked database."""
    app = create_app(lifespan=noop_lifespan)

    async def override_get_db():
        return mock_db

    app.dependency_overrides[get_db] = override_get_db

    async with AsyncClient(
        transport=ASGITransport(app=app), base_url="http://test"
    ) as client:
        yield client
