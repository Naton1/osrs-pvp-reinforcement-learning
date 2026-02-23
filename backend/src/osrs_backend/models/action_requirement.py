"""Action and requirement Pydantic models."""

from typing import Literal, Union, Annotated, Optional
from pydantic import BaseModel, Field, Discriminator
from prisma.enums import Skill

class SkillRequirement(BaseModel):
    """Requirement for a skill level (e.g., 88 Magic)."""

    type: Literal["skill"] = "skill"
    skill: Skill = Field(..., description="The skill required")
    level: int = Field(..., ge=1, le=99, description="Required skill level")


class ItemRequirement(BaseModel):
    """Requirement for an item (e.g., Nature rune x5)."""

    type: Literal["item"] = "item"
    item_source_id: int = Field(..., description="OSRS item ID")
    item_name: str = Field(..., description="Name of the item")
    quantity: int = Field(..., ge=1, description="Required quantity")


class QuestRequirement(BaseModel):
    """Requirement for quest completion."""

    type: Literal["quest"] = "quest"
    quest_name: str = Field(..., description="Name of the quest")
    completed: bool = Field(True, description="Whether the quest must be completed")


# Discriminated union type for all requirements
ActionRequirement = Annotated[
    Union[SkillRequirement, ItemRequirement, QuestRequirement],
    Discriminator("type"),
]


class ActionInput(BaseModel):
    """Input for an action (items consumed when taking the action)."""

    item_name: str = Field(..., description="Name of the OSRS item")
    quantity: int = Field(..., ge=1, description="Quantity consumed")


class ActionOutput(ActionInput):
    """Model for creating an action output."""

    pass


class Action(BaseModel):
    """An OSRS action with its requirements (e.g., Plank Make spell)."""

    name: str = Field(..., description="Name of the action")
    action_type: str = Field(
        ..., description="Type of action (e.g., 'spell', 'activity', 'item_creation')"
    )
    actions_per_hour: Optional[int] = Field(
        default=1,
        ge=1,
        description="Number of actions performed per hour",
    )
    experience: Optional[int] = Field(
        default=None, ge=0, description="Experience gained from the action"
    )
    requirements: list[ActionRequirement] = Field(
        default_factory=list, description="List of all requirements for this action"
    )
    inputs: list["ActionInput"] = Field(
        default_factory=list,
        description="List of items consumed when taking this action",
    )
    outputs: list["ActionOutput"] = Field(
        default_factory=list,
        description="List of items produced when taking this action",
    )

    def add_skill_requirement(self, skill: Skill, level: int) -> None:
        """Helper method to add a skill requirement."""
        self.requirements.append(SkillRequirement(skill=skill, level=level))

    def add_item_requirement(
        self, item_source_id: int, item_name: str, quantity: int
    ) -> None:
        """Helper method to add an item requirement."""
        self.requirements.append(
            ItemRequirement(
                item_source_id=item_source_id, item_name=item_name, quantity=quantity
            )
        )

    def add_quest_requirement(self, quest_name: str, completed: bool = True) -> None:
        """Helper method to add a quest requirement."""
        self.requirements.append(
            QuestRequirement(quest_name=quest_name, completed=completed)
        )

    def add_input(self, item_name: str, quantity: int) -> None:
        """Helper method to add an action input (consumed item)."""
        self.inputs.append(
            ActionInput(
                item_name=item_name, quantity=quantity
            )
        )
