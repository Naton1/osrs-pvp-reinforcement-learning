"""Action model helpers for database operations."""

from typing import Optional
from pydantic import BaseModel, Field
from fastapi import HTTPException
from prisma import Prisma
from prisma.enums import RequirementType, Skill

from osrs_backend.models.action_requirement import (
    Action as ActionModel,
    SkillRequirement,
    ItemRequirement,
    QuestRequirement,
    ActionInput,
)


class ActionCreate(BaseModel):
    """Model for creating an Action in the database."""

    name: str = Field(..., description="Name of the action")
    action_type: str = Field(
        ..., description="Type of action (e.g., 'spell', 'activity', 'item_creation')"
    )


async def create_action_with_requirements(db: Prisma, action: ActionModel):
    """Create an Action and its requirements in the database."""
    create_data = {
        "name": action.name,
        "action_type": action.action_type,
        "requirements": {
            "create": [
                _requirement_to_prisma_data(req, idx)
                for idx, req in enumerate(action.requirements)
            ]
        },
    }

    if hasattr(action, "inputs") and action.inputs:
        resolved_inputs = []
        for inp in action.inputs:
            item = await db.item.find_first(where={"name": inp.item_name})
            if not item:
                raise HTTPException(
                    status_code=404, detail=f"Item '{inp.item_name}' not found"
                )
            resolved_inputs.append({
                "item_source_id": item.source_id,
                "quantity": inp.quantity,
            })
        create_data["inputs"] = {"create": resolved_inputs}

    if hasattr(action, "outputs") and action.outputs:
        resolved_outputs = []
        for out in action.outputs:
            item = await db.item.find_first(where={"name": out.item_name})
            if not item:
                raise HTTPException(
                    status_code=404, detail=f"Item '{out.item_name}' not found"
                )
            resolved_outputs.append({
                "item_source_id": item.source_id,
                "quantity": out.quantity,
            })
        create_data["outputs"] = {"create": resolved_outputs}

    create_data["cost"] = 0.0
    create_data["experience"] = (
        action.experience if action.experience is not None else 0
    )
    create_data["actions_per_hour"] = (
        action.actions_per_hour if action.actions_per_hour is not None else 1
    )
    create_data["experience_per_hour"] = (
        action.experience * action.actions_per_hour
        if action.experience is not None and action.actions_per_hour is not None
        else 0
    )
    created_action = await db.action.create(
        data=create_data,
        include={
            "requirements": {"include": {"item": True}},
            "inputs": {"include": {"item": True}},
        },
    )

    return created_action


class RequirementCreate(BaseModel):
    """Model for creating a single requirement."""

    type: str = Field(
        ..., description="Type of requirement: 'skill', 'item', or 'quest'"
    )
    skill: str | None = Field(None, description="Skill name (e.g., 'MAGIC')")
    level: int | None = Field(None, ge=1, le=99, description="Required skill level")
    item_source_id: int | None = Field(None, description="OSRS item ID")
    item_name: str | None = Field(None, description="Name of the item")
    quantity: int | None = Field(None, ge=1, description="Required quantity")
    quest_name: str | None = Field(None, description="Name of the quest")
    completed: bool | None = Field(
        True, description="Whether the quest must be completed"
    )
    order: int = Field(0, description="Order/position of requirement")


class InputCreate(BaseModel):
    """Model for creating an action input."""

    item_name: str = Field(..., description="Name of the OSRS item")
    quantity: int = Field(..., ge=1, description="Input quantity")


class OutputCreate(BaseModel):
    """Model for creating an action output."""

    item_name: str = Field(..., description="Name of the OSRS item")
    quantity: int = Field(..., ge=1, description="Output quantity")


def _requirement_to_prisma_data(requirement, order: int) -> dict:
    """Convert a Pydantic requirement to Prisma create data."""
    if isinstance(requirement, SkillRequirement):
        return {
            "requirement_type": RequirementType.SKILL,
            "skill": Skill(requirement.skill.value),
            "skill_level": requirement.level,
            "order": order,
        }
    elif isinstance(requirement, ItemRequirement):
        return {
            "requirement_type": RequirementType.ITEM,
            "item_source_id": requirement.item_source_id,
            "item_quantity": requirement.quantity,
            "order": order,
        }
    elif isinstance(requirement, QuestRequirement):
        return {
            "requirement_type": RequirementType.QUEST,
            "quest_name": requirement.quest_name,
            "quest_completed": requirement.completed,
            "order": order,
        }
    else:
        raise ValueError(f"Unknown requirement type: {type(requirement)}")


async def get_action_with_requirements(db: Prisma, action_name: str):
    """Get an Action with its requirements from the database."""
    return await db.action.find_unique(
        where={"name": action_name},
        include={
            "requirements": {"include": {"item": True}},
            "inputs": {"include": {"item": True}},
            "outputs": {"include": {"item": True}},
        },
    )


def action_to_pydantic(action_data) -> ActionModel:
    """Convert a Prisma Action record to a Pydantic Action model."""
    requirements = []

    for req in action_data.requirements:
        req_type = req.requirement_type

        if req_type == RequirementType.SKILL:
            if not req.skill:
                raise ValueError(f"Skill requirement {req.id} has no skill set")
            requirements.append(
                SkillRequirement(
                    skill=Skill(req.skill.value),
                    level=req.skill_level or 1,
                )
            )
        elif req_type == RequirementType.ITEM:
            item_name = req.item.name if req.item else f"Item {req.item_source_id}"
            requirements.append(
                ItemRequirement(
                    item_source_id=req.item_source_id,
                    item_name=item_name,
                    quantity=req.item_quantity,
                )
            )
        elif req_type == RequirementType.QUEST:
            requirements.append(
                QuestRequirement(
                    quest_name=req.quest_name,
                    completed=(
                        req.quest_completed if req.quest_completed is not None else True
                    ),
                )
            )

    inputs = []
    if hasattr(action_data, "inputs") and action_data.inputs:
        for inp in action_data.inputs:
            item_name = inp.item.name if hasattr(inp, "item") and inp.item else f"Item {inp.item_source_id}"
            inputs.append(
                ActionInput(
                    item_name=item_name,
                    quantity=inp.quantity,
                )
            )

    return ActionModel(
        name=action_data.name,
        action_type=action_data.action_type,
        actions_per_hour=(
            action_data.actions_per_hour
            if hasattr(action_data, "actions_per_hour")
            else 1
        ),
        experience=(
            action_data.experience if hasattr(action_data, "experience") else None
        ),
        requirements=requirements,
        inputs=inputs,
    )
