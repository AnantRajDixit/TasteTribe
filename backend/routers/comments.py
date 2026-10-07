"""Community comments on recipes."""

import uuid
from datetime import datetime
from typing import List

from fastapi import APIRouter, Depends, HTTPException
from pymongo import ASCENDING

from lib.auth import get_current_user, require_user
from lib.db import db
from lib.dates import aware, utcnow
from models.social import CommentIn, CommentOut

router = APIRouter(tags=["comments"])


def _comment_out(row: dict, user_doc: dict) -> CommentOut:
    return CommentOut(
        id=row["id"],
        recipe_id=row["recipe_id"],
        user_id=row["user_id"],
        username=user_doc.get("username", "[deleted]"),
        name=user_doc.get("name", ""),
        avatar_url=user_doc.get("avatar_url"),
        text=row["text"],
        created_at=aware(row["created_at"]) if isinstance(row.get("created_at"), datetime) else None,
    )


async def _recipe_exists(recipe_id: str) -> None:
    if not await db.recipes.find_one({"id": recipe_id}):
        raise HTTPException(status_code=404, detail="Recipe not found.")


@router.get("/recipes/{recipe_id}/comments", response_model=List[CommentOut])
async def list_comments(recipe_id: str):
    rows = await db.comments.find({"recipe_id": recipe_id}).sort("created_at", ASCENDING).to_list(300)
    user_ids = list({r["user_id"] for r in rows})
    users = {u["id"]: u for u in await db.users.find({"id": {"$in": user_ids}}).to_list(500)}
    return [_comment_out(r, users.get(r["user_id"], {})) for r in rows]


@router.post("/recipes/{recipe_id}/comments", response_model=CommentOut, status_code=201)
async def post_comment(recipe_id: str, body: CommentIn, user: dict = Depends(require_user)):
    await _recipe_exists(recipe_id)
    row = {
        "id": str(uuid.uuid4()),
        "recipe_id": recipe_id,
        "user_id": user["id"],
        "text": body.text.strip(),
        "created_at": utcnow(),
    }
    await db.comments.insert_one(row)
    return _comment_out(row, user)


@router.delete("/comments/{comment_id}")
async def delete_comment(comment_id: str, user: dict = Depends(require_user)):
    row = await db.comments.find_one({"id": comment_id})
    if not row:
        raise HTTPException(status_code=404, detail="Comment not found.")
    if row["user_id"] != user["id"] and user.get("role") != "admin":
        raise HTTPException(status_code=403, detail="Not allowed.")
    await db.comments.delete_one({"id": comment_id})
    return {"ok": True}
