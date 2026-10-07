"""User profiles, following, and per-user recipe listings."""

from typing import List, Optional

from fastapi import APIRouter, Depends, HTTPException, Query
from pydantic import BaseModel
from pymongo import ASCENDING, DESCENDING

from lib.auth import get_current_user, require_user
from lib.db import db
from lib.dates import aware, utcnow
from lib.recipe_view import my_sets, to_recipe_out
from models.recipes import RecipeOut
from models.users import FollowActionOut, ProfileOut

router = APIRouter(prefix="/users", tags=["users"])


class PublicUser(BaseModel):
    id: str
    name: str
    username: str
    avatar_url: Optional[str] = None
    bio: str = ""


def _pub(doc: dict) -> PublicUser:
    return PublicUser(
        id=doc["id"],
        name=doc.get("name", ""),
        username=doc.get("username", ""),
        avatar_url=doc.get("avatar_url"),
        bio=doc.get("bio", ""),
    )


async def _get_user_or_404(username: str) -> dict:
    doc = await db.users.find_one({"username": username.lower()})
    if not doc:
        raise HTTPException(status_code=404, detail="Chef not found.")
    return doc


@router.get("/{username}", response_model=ProfileOut)
async def get_profile(username: str, user: Optional[dict] = Depends(get_current_user)):
    doc = await _get_user_or_404(username)
    followers = await db.follows.count_documents({"following_id": doc["id"]})
    following = await db.follows.count_documents({"follower_id": doc["id"]})
    recipes_n = await db.recipes.count_documents({"author_id": doc["id"], "status": "published"})
    avg_rows = await db.recipes.aggregate(
        [
            {"$match": {"author_id": doc["id"], "status": "published", "avg_rating": {"$ne": None}}},
            {"$group": {"_id": None, "avg": {"$avg": "$avg_rating"}}},
        ]
    ).to_list(1)
    is_self = bool(user and user["id"] == doc["id"])
    is_admin = bool(user and user.get("role") == "admin")
    return ProfileOut(
        id=doc["id"],
        name=doc.get("name", ""),
        username=doc.get("username", ""),
        email=doc.get("email") if (is_self or is_admin) else None,
        avatar_url=doc.get("avatar_url"),
        bio=doc.get("bio", ""),
        role=doc.get("role", "user"),
        created_at=aware(doc.get("created_at")) if doc.get("created_at") else None,
        followers_count=followers,
        following_count=following,
        recipes_count=recipes_n,
        avg_rating=round(avg_rows[0]["avg"], 2) if avg_rows else None,
        is_following=bool(user and await db.follows.find_one({"follower_id": user["id"], "following_id": doc["id"]})),
    )


@router.post("/{username}/follow", response_model=FollowActionOut)
async def follow_user(username: str, user: dict = Depends(require_user)):
    target = await _get_user_or_404(username)
    if target["id"] == user["id"]:
        raise HTTPException(status_code=400, detail="You can't follow yourself.")
    await db.follows.update_one(
        {"follower_id": user["id"], "following_id": target["id"]},
        {"$set": {"created_at": utcnow()}},
        upsert=True,
    )
    count = await db.follows.count_documents({"following_id": target["id"]})
    return FollowActionOut(following=True, followers_count=count)


@router.delete("/{username}/follow", response_model=FollowActionOut)
async def unfollow_user(username: str, user: dict = Depends(require_user)):
    target = await _get_user_or_404(username)
    await db.follows.delete_many({"follower_id": user["id"], "following_id": target["id"]})
    count = await db.follows.count_documents({"following_id": target["id"]})
    return FollowActionOut(following=False, followers_count=count)


@router.get("/{username}/followers", response_model=List[PublicUser])
async def list_followers(username: str):
    doc = await _get_user_or_404(username)
    rows = await db.follows.find({"following_id": doc["id"]}).sort("created_at", DESCENDING).to_list(500)
    ids = [r["follower_id"] for r in rows]
    users = await db.users.find({"id": {"$in": ids}}).to_list(500)
    by_id = {u["id"]: u for u in users}
    return [_pub(by_id[i]) for i in ids if i in by_id]


@router.get("/{username}/following", response_model=List[PublicUser])
async def list_following(username: str):
    doc = await _get_user_or_404(username)
    rows = await db.follows.find({"follower_id": doc["id"]}).sort("created_at", DESCENDING).to_list(500)
    ids = [r["following_id"] for r in rows]
    users = await db.users.find({"id": {"$in": ids}}).to_list(500)
    by_id = {u["id"]: u for u in users}
    return [_pub(by_id[i]) for i in ids if i in by_id]


@router.get("/{username}/recipes", response_model=List[RecipeOut])
async def user_recipes(
    username: str,
    status: Optional[str] = Query(None, pattern=r"^(draft|published|all)$"),
    user: Optional[dict] = Depends(get_current_user),
):
    doc = await _get_user_or_404(username)
    is_self = bool(user and (user["id"] == doc["id"] or user.get("role") == "admin"))
    cond: dict = {"author_id": doc["id"]}
    if not is_self:
        cond["status"] = "published"
    elif status and status != "all":
        cond["status"] = status
    docs = await db.recipes.find(cond).sort("created_at", DESCENDING).to_list(200)
    likes, favs = await my_sets(user)
    return [to_recipe_out(d, likes, favs) for d in docs]
