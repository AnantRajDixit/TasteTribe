"""Ratings & written reviews — one review per user per recipe, updatable; averages recomputed server-side."""

import uuid
from datetime import datetime
from typing import List, Optional

from fastapi import APIRouter, Depends, HTTPException
from pymongo import DESCENDING

from lib.auth import require_user
from lib.db import db
from lib.dates import aware, utcnow
from models.social import ReviewIn, ReviewOut

router = APIRouter(tags=["reviews"])


async def _recipe_or_404(recipe_id: str) -> dict:
    doc = await db.recipes.find_one({"id": recipe_id})
    if not doc:
        raise HTTPException(status_code=404, detail="Recipe not found.")
    return doc


async def recompute_rating(recipe_id: str) -> None:
    rows = await db.reviews.aggregate(
        [
            {"$match": {"recipe_id": recipe_id}},
            {"$group": {"_id": None, "avg": {"$avg": "$rating"}, "n": {"$sum": 1}}},
        ]
    ).to_list(1)
    if rows:
        await db.recipes.update_one(
            {"id": recipe_id},
            {"$set": {"avg_rating": round(rows[0]["avg"], 2), "ratings_count": rows[0]["n"]}},
        )
    else:
        await db.recipes.update_one({"id": recipe_id}, {"$set": {"avg_rating": None, "ratings_count": 0}})


def _review_out(row: dict, user_doc: dict) -> ReviewOut:
    return ReviewOut(
        id=row["id"],
        recipe_id=row["recipe_id"],
        user_id=row["user_id"],
        username=user_doc.get("username", "[deleted]"),
        name=user_doc.get("name", ""),
        avatar_url=user_doc.get("avatar_url"),
        rating=row["rating"],
        comment=row.get("comment"),
        created_at=aware(row["created_at"]) if isinstance(row.get("created_at"), datetime) else None,
        updated_at=aware(row["updated_at"]) if isinstance(row.get("updated_at"), datetime) else None,
    )


@router.get("/recipes/{recipe_id}/reviews", response_model=List[ReviewOut])
async def list_reviews(recipe_id: str):
    rows = await db.reviews.find({"recipe_id": recipe_id}).sort("created_at", DESCENDING).to_list(300)
    user_ids = list({r["user_id"] for r in rows})
    users = {u["id"]: u for u in await db.users.find({"id": {"$in": user_ids}}).to_list(500)}
    return [_review_out(r, users.get(r["user_id"], {})) for r in rows]


@router.put("/recipes/{recipe_id}/reviews", response_model=ReviewOut)
async def upsert_review(recipe_id: str, body: ReviewIn, user: dict = Depends(require_user)):
    await _recipe_or_404(recipe_id)
    now = utcnow()
    existing = await db.reviews.find_one({"recipe_id": recipe_id, "user_id": user["id"]})
    if existing:
        await db.reviews.update_one(
            {"_id": existing["_id"]},
            {"$set": {"rating": body.rating, "comment": body.comment, "updated_at": now}},
        )
        row = await db.reviews.find_one({"_id": existing["_id"]})
    else:
        row = {
            "id": str(uuid.uuid4()),
            "recipe_id": recipe_id,
            "user_id": user["id"],
            "rating": body.rating,
            "comment": body.comment,
            "created_at": now,
            "updated_at": now,
        }
        await db.reviews.insert_one(row)
    await recompute_rating(recipe_id)
    return _review_out(row, user)


@router.delete("/recipes/{recipe_id}/reviews")
async def delete_my_review(recipe_id: str, user: dict = Depends(require_user)):
    result = await db.reviews.delete_one({"recipe_id": recipe_id, "user_id": user["id"]})
    await recompute_rating(recipe_id)
    return {"ok": True, "deleted": result.deleted_count}
