"""Action-related API endpoints."""

from fastapi import APIRouter, Depends, HTTPException, status
from typing import List
from prisma.models import Action, ActionRequirement, ActionOutput, ActionInput
from prisma.enums import Skill
from prisma import Prisma
from prisma.errors import UniqueViolationError
from logging import getLogger

from osrs_backend.db.database import get_db
from osrs_backend.models.action import (
    RequirementCreate,
    InputCreate,
    OutputCreate,
    create_action_with_requirements,
    get_action_with_requirements,
    _requirement_to_prisma_data,
)
from osrs_backend.models.action_requirement import (
    Action as ActionModel,
    SkillRequirement,
    ItemRequirement,
    QuestRequirement,
)

logger = getLogger(__name__)

router = APIRouter(
    prefix="/actions",
    tags=["actions"],
)


@router.get("/", response_model=List[Action], response_model_exclude_none=True)
async def get_actions(db: Prisma = Depends(get_db), limit: int = 100):
    """Get all actions with optional limit."""
    actions = await db.action.find_many(
        order={"id": "desc"},
        take=limit,
        include={
            "requirements": {"include": {"item": True}},
            "inputs": {"include": {"item": True}},
            "outputs": {"include": {"item": True}},
        },
    )
    return actions


@router.get("/{action_id}", response_model=Action, response_model_exclude_none=True)
async def get_action(action_id: int, db: Prisma = Depends(get_db)):
    """Get a specific action by ID with all requirements."""
    action = await db.action.find_unique(
        where={"id": action_id},
        include={
            "requirements": {"include": {"item": True}},
            "inputs": {"include": {"item": True}},
            "outputs": {"include": {"item": True}},
        },
    )
    if not action:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Action {action_id} not found",
        )
    return action


@router.get(
    "/name/{action_name}", response_model=Action, response_model_exclude_none=True
)
async def get_action_by_name(action_name: str, db: Prisma = Depends(get_db)):
    """Get a specific action by name with all requirements."""
    action = await get_action_with_requirements(db, action_name)
    if not action:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Action '{action_name}' not found",
        )
    return action


@router.post("/", response_model=Action)
async def create_action(action: ActionModel, db: Prisma = Depends(get_db)):
    """Create a new action with all its requirements."""
    try:
        created_action = await create_action_with_requirements(db, action)
    except UniqueViolationError as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=f"Action {action.name} already exists",
        )
    return created_action


@router.get(
    "/{action_id}/requirements",
    response_model=List[ActionRequirement],
    response_model_exclude_none=True,
)
async def get_action_requirements(action_id: int, db: Prisma = Depends(get_db)):
    """Get all requirements for a specific action."""
    action = await db.action.find_unique(
        where={"id": action_id},
        include={"requirements": {"include": {"item": True}}},
    )
    if not action:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Action {action_id} not found",
        )
    return action.requirements


@router.post("/{action_id}/requirements", response_model=ActionRequirement)
async def add_requirement(
    action_id: int, requirement: RequirementCreate, db: Prisma = Depends(get_db)
):
    """Add a new requirement to an action."""
    action = await db.action.find_unique(where={"id": action_id})
    if not action:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Action {action_id} not found",
        )

    if requirement.type == "skill":
        if not requirement.skill or requirement.level is None:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Skill requirement requires 'skill' and 'level' fields",
            )
        req_model = SkillRequirement(
            skill=Skill(requirement.skill), level=requirement.level
        )
    elif requirement.type == "item":
        if (
            requirement.item_source_id is None
            or requirement.item_name is None
            or requirement.quantity is None
        ):
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Item requirement requires 'item_source_id', 'item_name', and 'quantity' fields",
            )
        req_model = ItemRequirement(
            item_source_id=requirement.item_source_id,
            item_name=requirement.item_name,
            quantity=requirement.quantity,
        )
    elif requirement.type == "quest":
        if not requirement.quest_name:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Quest requirement requires 'quest_name' field",
            )
        req_model = QuestRequirement(
            quest_name=requirement.quest_name,
            completed=(
                requirement.completed if requirement.completed is not None else True
            ),
        )
    else:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=f"Invalid requirement type: {requirement.type}. Must be 'skill', 'item', or 'quest'",
        )

    existing_reqs = await db.actionrequirement.find_many(
        where={"action_id": action_id}, order={"order": "desc"}, take=1
    )
    next_order = existing_reqs[0].order + 1 if existing_reqs else requirement.order

    prisma_data = _requirement_to_prisma_data(req_model, next_order)
    prisma_data["action_id"] = action_id

    created_requirement = await db.actionrequirement.create(
        data=prisma_data, include={"item": True}
    )

    return created_requirement


@router.get(
    "/{action_id}/inputs",
    response_model=List[ActionInput],
    response_model_exclude_none=True,
)
async def get_action_inputs(action_id: int, db: Prisma = Depends(get_db)):
    """Get all inputs for a specific action."""
    action = await db.action.find_unique(
        where={"id": action_id},
        include={"inputs": {"include": {"item": True}}},
    )
    if not action:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Action {action_id} not found",
        )
    return action.inputs


@router.post("/{action_id}/inputs", response_model=ActionInput)
async def add_input(
    action_id: int, input_data: InputCreate, db: Prisma = Depends(get_db)
):
    """Add a new input to an action."""
    action = await db.action.find_unique(where={"id": action_id})
    if not action:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Action {action_id} not found",
        )

    item = await db.item.find_first(where={"name": input_data.item_name})
    if not item:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Item '{input_data.item_name}' not found",
        )

    created_input = await db.actioninput.create(
        data={
            "action_id": action_id,
            "item_source_id": item.source_id,
            "quantity": input_data.quantity,
        },
        include={"item": True},
    )

    return created_input


@router.post("/{action_id}/outputs", response_model=ActionOutput)
async def add_output(
    action_id: int, output: OutputCreate, db: Prisma = Depends(get_db)
):
    """Add a new output to an action."""
    action = await db.action.find_unique(where={"id": action_id})
    if not action:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Action {action_id} not found",
        )

    item = await db.item.find_first(where={"name": output.item_name})
    if not item:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Item '{output.item_name}' not found",
        )

    created_output = await db.actionoutput.create(
        data={
            "action_id": action_id,
            "item_source_id": item.source_id,
            "quantity": output.quantity,
        },
        include={"item": True},
    )

    return created_output
