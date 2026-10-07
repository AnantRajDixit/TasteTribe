"""Recipe + category models — structured ingredients, never one big text blob."""

from datetime import datetime
from typing import List, Literal, Optional

from pydantic import BaseModel, Field


class Ingredient(BaseModel):
    name: str = Field(min_length=1, max_length=120)
    quantity: float = Field(gt=0, default=1)
    unit: str = "piece"
    optional: bool = False


class Nutrition(BaseModel):
    calories: Optional[float] = None
    protein: Optional[float] = None
    carbs: Optional[float] = None
    fat: Optional[float] = None


class RecipeCreate(BaseModel):
    title: str = Field(min_length=3, max_length=140)
    description: str = Field(default="", max_length=2000)
    cover_image: Optional[str] = None
    cuisine: str = Field(min_length=1, max_length=60)
    category: str = Field(min_length=1, max_length=60)
    difficulty: Literal["Easy", "Medium", "Hard"]
    dietary: str = "Non-Vegetarian"
    prep_time: int = Field(ge=0, le=1440)
    cook_time: int = Field(ge=0, le=1440)
    servings: int = Field(ge=1, le=50, default=2)
    ingredients: List[Ingredient] = Field(min_length=1)
    instructions: List[str] = Field(min_length=1)
    nutrition: Optional[Nutrition] = None
    tags: List[str] = []
    status: Literal["draft", "published"] = "published"
    source: str = "user"


class RecipeOut(BaseModel):
    id: str
    title: str
    description: str = ""
    cover_image: Optional[str] = None
    cuisine: str
    category: str
    difficulty: str
    dietary: str = "Non-Vegetarian"
    prep_time: int
    cook_time: int
    total_time: int
    servings: int
    ingredients: List[Ingredient]
    instructions: List[str] = []
    nutrition: Optional[Nutrition] = None
    tags: List[str] = []
    author_id: str
    author_name: str = ""
    author_username: str = ""
    status: str = "published"
    source: str = "user"
    views: int = 0
    likes_count: int = 0
    favorites_count: int = 0
    avg_rating: Optional[float] = None
    ratings_count: int = 0
    liked_by_me: bool = False
    favorited_by_me: bool = False
    created_at: Optional[datetime] = None
    updated_at: Optional[datetime] = None


class RecipeListOut(BaseModel):
    items: List[RecipeOut]
    total: int
    page: int
    pages: int


class ScaledIngredient(BaseModel):
    name: str
    quantity: float
    unit: str
    optional: bool = False
    display: str


class ScaleOut(BaseModel):
    recipe_id: str
    base_servings: int
    servings: int
    ingredients: List[ScaledIngredient]


class Category(BaseModel):
    id: str
    name: str
    slug: str
    description: str = ""
    image_url: Optional[str] = None
    recipes_count: int = 0


class CategoryCreate(BaseModel):
    name: str = Field(min_length=2, max_length=60)
    description: str = Field(default="", max_length=300)
    image_url: Optional[str] = None
