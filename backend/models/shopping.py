"""Shopping list models — one list document per user with structured items."""

from typing import List, Optional

from pydantic import BaseModel, Field


class ShoppingItem(BaseModel):
    id: str
    name: str
    quantity: float = 1
    unit: str = "piece"
    checked: bool = False
    recipe_title: Optional[str] = None


class AddItemIn(BaseModel):
    name: str = Field(min_length=1, max_length=120)
    quantity: float = Field(gt=0, default=1)
    unit: str = "piece"


class AddRecipeIn(BaseModel):
    recipe_id: str
    servings: Optional[int] = Field(default=None, ge=1, le=50)


class ShoppingListOut(BaseModel):
    items: List[ShoppingItem]
