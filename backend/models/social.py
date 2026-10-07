"""Social models: reviews (rating + comment, one per user per recipe), comments, reports."""

from datetime import datetime
from typing import Optional

from pydantic import BaseModel, Field


class ReviewIn(BaseModel):
    rating: int = Field(ge=1, le=5)
    comment: Optional[str] = Field(default=None, max_length=2000)


class ReviewOut(BaseModel):
    id: str
    recipe_id: str
    user_id: str
    username: str
    name: str = ""
    avatar_url: Optional[str] = None
    rating: int
    comment: Optional[str] = None
    created_at: Optional[datetime] = None
    updated_at: Optional[datetime] = None


class CommentIn(BaseModel):
    text: str = Field(min_length=1, max_length=1000)


class CommentOut(BaseModel):
    id: str
    recipe_id: str
    user_id: str
    username: str
    name: str = ""
    avatar_url: Optional[str] = None
    text: str
    created_at: Optional[datetime] = None


class ReportIn(BaseModel):
    target_type: str = Field(pattern=r"^(recipe|comment)$")
    target_id: str
    reason: str = Field(min_length=3, max_length=500)


class ReportOut(BaseModel):
    id: str
    target_type: str
    target_id: str
    reason: str
    reporter_username: str
    status: str = "pending"
    created_at: Optional[datetime] = None
    target_title: Optional[str] = None
