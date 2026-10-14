"""Recipes: CRUD, search/filter/sort, serving scaler, likes, favorites, feed, categories."""

import math
import re
import uuid
from typing import List, Optional

from fastapi import APIRouter, Depends, HTTPException, Query
from pymongo import ASCENDING, DESCENDING

from lib.auth import get_current_user, require_user
from lib.db import db
from lib.dates import utcnow
from lib.recipe_view import my_sets, scaled_ingredient, to_recipe_out
from models.recipes import (
    Category,
    CategoryCreate,
    RecipeCreate,
    RecipeListOut,
    RecipeOut,
    ScaleOut,
)

router = APIRouter(tags=["recipes"])

SORTS = {
    "newest": [("created_at", DESCENDING)],
    "oldest": [("created_at", ASCENDING)],
    "rating": [("avg_rating", DESCENDING), ("ratings_count", DESCENDING)],
    "popular": [("likes_count", DESCENDING), ("views", DESCENDING)],
    "time": [("total_time", ASCENDING)],
}


def slugify(name: str) -> str:
    return re.sub(r"[^a-z0-9]+", "-", name.lower()).strip("-")


async def _paged(cond: dict, order: list, page: int, limit: int, user: Optional[dict]) -> RecipeListOut:
    total = await db.recipes.count_documents(cond)
    docs = (
        await db.recipes.find(cond)
        .sort(order)
        .skip((page - 1) * limit)
        .limit(limit)
        .to_list(limit)
    )
    likes, favs = await my_sets(user)
    items = [to_recipe_out(d, likes, favs) for d in docs]
    return RecipeListOut(items=items, total=total, page=page, pages=max(1, math.ceil(total / limit)))


async def _recipe_or_404(recipe_id: str) -> dict:
    doc = await db.recipes.find_one({"id": recipe_id})
    if not doc:
        raise HTTPException(status_code=404, detail="Recipe not found.")
    return doc


def _can_touch(doc: dict, user: Optional[dict]) -> bool:
    return bool(user and (user["id"] == doc["author_id"] or user.get("role") == "admin"))


# ---------- search & listing ----------

@router.get("/recipes", response_model=RecipeListOut)
async def list_recipes(
    q: Optional[str] = None,
    ingredient: Optional[str] = None,
    cuisine: Optional[str] = None,
    category: Optional[str] = None,
    difficulty: Optional[str] = None,
    dietary: Optional[str] = None,
    tag: Optional[str] = None,
    max_time: Optional[int] = None,
    sort: str = "newest",
    page: int = Query(1, ge=1),
    limit: int = Query(12, ge=1, le=48),
    user: Optional[dict] = Depends(get_current_user),
):
    cond: dict = {"status": "published"}
    if q:
        rx = {"$regex": re.escape(q), "$options": "i"}
        cond["$or"] = [{"title": rx}, {"description": rx}, {"tags": rx}]
    if ingredient:
        cond["ingredients.name"] = {"$regex": re.escape(ingredient), "$options": "i"}
    if cuisine:
        cond["cuisine"] = cuisine
    if category:
        cond["category"] = category
    if difficulty:
        cond["difficulty"] = difficulty
    if dietary:
        cond["dietary"] = dietary
    if tag:
        cond["tags"] = {"$regex": re.escape(tag), "$options": "i"}
    if max_time:
        cond["total_time"] = {"$lte": max_time}
    return await _paged(cond, SORTS.get(sort, SORTS["newest"]), page, limit, user)


@router.get("/recipes/{recipe_id}", response_model=RecipeOut)
async def get_recipe(recipe_id: str, user: Optional[dict] = Depends(get_current_user)):
    doc = await _recipe_or_404(recipe_id)
    if doc.get("status") == "draft" and not _can_touch(doc, user):
        raise HTTPException(status_code=404, detail="Recipe not found.")
    if doc.get("status") == "published":
        await db.recipes.update_one({"id": recipe_id}, {"$inc": {"views": 1}})
        doc["views"] = doc.get("views", 0) + 1
        if user:
            await db.recently_viewed.update_one(
                {"user_id": user["id"], "recipe_id": recipe_id},
                {"$set": {"viewed_at": utcnow()}},
                upsert=True,
            )
    likes, favs = await my_sets(user)
    return to_recipe_out(doc, likes, favs)


@router.get("/recipes/{recipe_id}/scale", response_model=ScaleOut)
async def scale_recipe(recipe_id: str, servings: int = Query(ge=1, le=50)):
    """Backend business logic: rescale every ingredient quantity for a new serving count."""
    doc = await _recipe_or_404(recipe_id)
    base = max(1, doc["servings"])
    factor = servings / base
    return ScaleOut(
        recipe_id=recipe_id,
        base_servings=base,
        servings=servings,
        ingredients=[scaled_ingredient(i, factor) for i in doc.get("ingredients", [])],
    )


# ---------- CRUD ----------

@router.post("/recipes", response_model=RecipeOut, status_code=201)
async def create_recipe(body: RecipeCreate, user: dict = Depends(require_user)):
    doc = body.model_dump()
    doc.update(
        id=str(uuid.uuid4()),
        author_id=user["id"],
        author_name=user.get("name", ""),
        author_username=user.get("username", ""),
        total_time=body.prep_time + body.cook_time,
        views=0,
        likes_count=0,
        favorites_count=0,
        avg_rating=None,
        ratings_count=0,
        created_at=utcnow(),
        updated_at=utcnow(),
    )
    await db.recipes.insert_one(doc)
    return to_recipe_out(doc)


@router.patch("/recipes/{recipe_id}", response_model=RecipeOut)
async def update_recipe(recipe_id: str, body: RecipeCreate, user: dict = Depends(require_user)):
    doc = await _recipe_or_404(recipe_id)
    if not _can_touch(doc, user):
        raise HTTPException(status_code=403, detail="Only the recipe owner can edit this recipe.")
    updates = body.model_dump()
    updates["total_time"] = body.prep_time + body.cook_time
    updates["updated_at"] = utcnow()
    await db.recipes.update_one({"id": recipe_id}, {"$set": updates})
    doc = await _recipe_or_404(recipe_id)
    likes, favs = await my_sets(user)
    return to_recipe_out(doc, likes, favs)


@router.delete("/recipes/{recipe_id}")
async def delete_recipe(recipe_id: str, user: dict = Depends(require_user)):
    doc = await _recipe_or_404(recipe_id)
    if not _can_touch(doc, user):
        raise HTTPException(status_code=403, detail="Only the recipe owner or an admin can delete this recipe.")
    await db.recipes.delete_one({"id": recipe_id})
    await db.reviews.delete_many({"recipe_id": recipe_id})
    await db.comments.delete_many({"recipe_id": recipe_id})
    await db.likes.delete_many({"recipe_id": recipe_id})
    await db.favorites.delete_many({"recipe_id": recipe_id})
    await db.recently_viewed.delete_many({"recipe_id": recipe_id})
    return {"ok": True}


# ---------- likes & favorites (toggles) ----------

async def _toggle(collection: str, count_field: str, recipe_id: str, user: dict):
    await _recipe_or_404(recipe_id)
    existing = await db[collection].find_one({"user_id": user["id"], "recipe_id": recipe_id})
    if existing:
        await db[collection].delete_one({"_id": existing["_id"]})
        active = False
    else:
        await db[collection].insert_one(
            {"user_id": user["id"], "recipe_id": recipe_id, "created_at": utcnow()}
        )
        active = True
    count = await db[collection].count_documents({"recipe_id": recipe_id})
    await db.recipes.update_one({"id": recipe_id}, {"$set": {count_field: count}})
    return {"active": active, "count": count}


@router.post("/recipes/{recipe_id}/like")
async def toggle_like(recipe_id: str, user: dict = Depends(require_user)):
    return await _toggle("likes", "likes_count", recipe_id, user)


@router.post("/recipes/{recipe_id}/favorite")
async def toggle_favorite(recipe_id: str, user: dict = Depends(require_user)):
    return await _toggle("favorites", "favorites_count", recipe_id, user)


# ---------- personalized feed ----------

@router.get("/feed", response_model=RecipeListOut)
async def feed(
    tab: str = Query("latest", pattern=r"^(latest|following|trending|recommended)$"),
    page: int = Query(1, ge=1),
    limit: int = Query(12, ge=1, le=48),
    user: Optional[dict] = Depends(get_current_user),
):
    cond: dict = {"status": "published"}
    if tab == "following":
        if not user:
            raise HTTPException(status_code=401, detail="Log in to see recipes from chefs you follow.")
        ids = [f["following_id"] for f in await db.follows.find({"follower_id": user["id"]}).to_list(3000)]
        cond["author_id"] = {"$in": ids}
        return await _paged(cond, SORTS["newest"], page, limit, user)

    if tab == "recommended":
        if not user:
            raise HTTPException(status_code=401, detail="Log in to get personalised recommendations.")
        interacted = {d["recipe_id"] for d in await db.favorites.find({"user_id": user["id"]}).to_list(500)}
        interacted |= {d["recipe_id"] for d in await db.likes.find({"user_id": user["id"]}).to_list(500)}
        interacted |= {d["recipe_id"] for d in await db.reviews.find({"user_id": user["id"]}).to_list(500)}
        seeds = await db.recipes.find({"id": {"$in": list(interacted)}}).to_list(200) if interacted else []
        cats = sorted({r["category"] for r in seeds})
        cuisines = sorted({r["cuisine"] for r in seeds})
        if cats:
            cond["$or"] = [{"category": {"$in": cats}}, {"cuisine": {"$in": cuisines}}]
        cond["author_id"] = {"$ne": user["id"]}
        cond["id"] = {"$nin": list(interacted)}
        order = SORTS["rating"] if seeds else SORTS["newest"]
        return await _paged(cond, order, page, limit, user)

    if tab == "trending":
        docs = await db.recipes.find({"status": "published"}).to_list(400)

        def score(d: dict) -> float:
            return (
                d.get("likes_count", 0) * 2
                + d.get("favorites_count", 0) * 2
                + d.get("ratings_count", 0) * 3
                + d.get("views", 0) / 10.0
                + (d.get("avg_rating") or 0)
            )

        docs.sort(key=score, reverse=True)
        total = len(docs)
        start = (page - 1) * limit
        likes, favs = await my_sets(user)
        return RecipeListOut(
            items=[to_recipe_out(d, likes, favs) for d in docs[start : start + limit]],
            total=total,
            page=page,
            pages=max(1, math.ceil(total / limit)),
        )

    return await _paged(cond, SORTS["newest"], page, limit, user)


# ---------- categories ----------

@router.get("/categories", response_model=List[Category])
async def list_categories():
    cats = await db.categories.find().sort("name", ASCENDING).to_list(200)
    counts = {
        r["_id"]: r["n"]
        async for r in db.recipes.aggregate(
            [{"$match": {"status": "published"}}, {"$group": {"_id": "$category", "n": {"$sum": 1}}}]
        )
    }
    return [
        Category(
            id=c["id"],
            name=c["name"],
            slug=c["slug"],
            description=c.get("description", ""),
            image_url=c.get("image_url"),
            recipes_count=counts.get(c["name"], 0),
        )
        for c in cats
    ]


@router.post("/categories", response_model=Category, status_code=201)
async def create_category(body: CategoryCreate, user: dict = Depends(require_user)):
    from lib.auth import require_admin

    await require_admin(user)
    slug = slugify(body.name)
    if await db.categories.find_one({"slug": slug}):
        raise HTTPException(status_code=409, detail="A category with that name already exists.")
    doc = {
        "id": str(uuid.uuid4()),
        "name": body.name.strip(),
        "slug": slug,
        "description": body.description,
        "image_url": body.image_url,
        "created_at": utcnow(),
    }
    await db.categories.insert_one(doc)
    return Category(**doc)


@router.patch("/categories/{cat_id}", response_model=Category)
async def update_category(cat_id: str, body: CategoryCreate, user: dict = Depends(require_user)):
    from lib.auth import require_admin

    await require_admin(user)
    doc = await db.categories.find_one({"id": cat_id})
    if not doc:
        raise HTTPException(status_code=404, detail="Category not found.")
    updates = {"name": body.name.strip(), "description": body.description, "image_url": body.image_url}
    await db.categories.update_one({"id": cat_id}, {"$set": updates})
    doc = await db.categories.find_one({"id": cat_id})
    return Category(**doc)


@router.delete("/categories/{cat_id}")
async def delete_category(cat_id: str, user: dict = Depends(require_user)):
    from lib.auth import require_admin

    await require_admin(user)
    result = await db.categories.delete_one({"id": cat_id})
    if result.deleted_count == 0:
        raise HTTPException(status_code=404, detail="Category not found.")
    return {"ok": True}
