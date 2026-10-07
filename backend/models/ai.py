"""AI assistant models: chat, recipe generation, ingredient substitution."""

from typing import List, Optional

from pydantic import BaseModel, Field


class ChatIn(BaseModel):
    message: str = Field(min_length=1, max_length=2000)
    session_id: str = Field(min_length=1, max_length=64)
    recipe_id: Optional[str] = None


class ChatDelta(BaseModel):
    delta: Optional[str] = None
    done: bool = False


class GenerateRecipeIn(BaseModel):
    ingredients: List[str] = Field(min_length=1)
    max_time: Optional[int] = None
    difficulty: Optional[str] = None
    servings: Optional[int] = None
    meal_type: Optional[str] = None


class SubstituteIn(BaseModel):
    ingredient: str = Field(min_length=2, max_length=120)
    recipe_id: Optional[str] = None


class SubstituteOption(BaseModel):
    name: str
    ratio: str = ""
    taste: str = ""
    texture: str = ""
    temperature: str = ""
    quantity: str = ""


class SubstituteOut(BaseModel):
    ingredient: str
    summary: str = ""
    substitutes: List[SubstituteOption] = []
