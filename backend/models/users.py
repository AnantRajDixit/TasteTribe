"""User-related Pydantic v2 models — keep the TS mirrors in frontend/src/lib/types.ts in sync."""

from datetime import datetime
from typing import Optional

from pydantic import BaseModel, EmailStr, Field


class UserCreate(BaseModel):
    name: str = Field(min_length=2, max_length=80)
    username: str = Field(min_length=3, max_length=30, pattern=r"^[a-zA-Z0-9_]+$")
    email: EmailStr
    password: str = Field(min_length=8, max_length=128)
    avatar_url: Optional[str] = None


class UserLogin(BaseModel):
    username_or_email: str
    password: str


class UserOut(BaseModel):
    id: str
    name: str
    username: str
    email: Optional[EmailStr] = None
    avatar_url: Optional[str] = None
    bio: str = ""
    role: str = "user"
    created_at: Optional[datetime] = None


class UserUpdate(BaseModel):
    name: Optional[str] = Field(default=None, min_length=2, max_length=80)
    bio: Optional[str] = Field(default=None, max_length=400)
    avatar_url: Optional[str] = None


class PasswordChange(BaseModel):
    current_password: str
    new_password: str = Field(min_length=8, max_length=128)


class ForgotPasswordIn(BaseModel):
    email: EmailStr


class ResetPasswordIn(BaseModel):
    token: str
    new_password: str = Field(min_length=8, max_length=128)


class ProfileOut(UserOut):
    followers_count: int = 0
    following_count: int = 0
    recipes_count: int = 0
    avg_rating: Optional[float] = None
    is_following: bool = False


class FollowActionOut(BaseModel):
    following: bool
    followers_count: int
