"""Shared recipe presentation helpers: quantity formatting, scaling, liked/favorited markers."""

import math
from datetime import datetime, timezone
from typing import Optional

from lib.db import db
from lib.dates import aware
from models.recipes import Ingredient, RecipeOut, ScaledIngredient


def fmt_quantity(q: float) -> str:
    """Humanise a quantity: nice fractions under 10, plain decimals above."""
    q = round(float(q), 2)
    whole = int(math.floor(q + 1e-9))
    frac = q - whole
    table = [(0.25, "1/4"), (1 / 3, "1/3"), (0.5, "1/2"), (2 / 3, "2/3"), (0.75, "3/4")]
    best = min(table, key=lambda t: abs(t[0] - frac))
    if abs(best[0] - frac) <= 0.03:
        if whole and best[1]:
            return f"{whole} {best[1]}"
        return str(whole) if not best[1] else best[1]
    if abs(q - round(q)) < 0.01:
        return str(int(round(q)))
    return f"{q:g}"


def scaled_ingredient(ing: dict, factor: float) -> ScaledIngredient:
    quantity = round(float(ing["quantity"]) * factor, 3)
    unit = ing.get("unit", "piece")
    return ScaledIngredient(
        name=ing["name"],
        quantity=quantity,
        unit=unit,
        optional=bool(ing.get("optional", False)),
        display=f"{fmt_quantity(quantity)} {unit} {ing['name']}".strip(),
    )


async def my_sets(user: Optional[dict]) -> tuple[set, set]:
    """Ids of recipes the current user liked / favorited — empty sets when logged out."""
    if not user:
        return set(), set()
    likes = {d["recipe_id"] for d in await db.likes.find({"user_id": user["id"]}).to_list(3000)}
    favs = {d["recipe_id"] for d in await db.favorites.find({"user_id": user["id"]}).to_list(3000)}
    return likes, favs


def to_recipe_out(doc: dict, liked_ids: Optional[set] = None, fav_ids: Optional[set] = None) -> RecipeOut:
    rid = doc["id"]
    created = doc.get("created_at")
    updated = doc.get("updated_at")
    nutrition = doc.get("nutrition")
    return RecipeOut(
        id=rid,
        title=doc["title"],
        description=doc.get("description", ""),
        cover_image=doc.get("cover_image"),
        cuisine=doc["cuisine"],
        category=doc["category"],
        difficulty=doc["difficulty"],
        dietary=doc.get("dietary", "Non-Vegetarian"),
        prep_time=doc["prep_time"],
        cook_time=doc["cook_time"],
        total_time=doc.get("total_time", doc["prep_time"] + doc["cook_time"]),
        servings=doc["servings"],
        ingredients=[Ingredient(**i) for i in doc.get("ingredients", [])],
        instructions=doc.get("instructions", []),
        nutrition=nutrition if nutrition is None else dict(nutrition),
        tags=doc.get("tags", []),
        author_id=doc["author_id"],
        author_name=doc.get("author_name", ""),
        author_username=doc.get("author_username", ""),
        status=doc.get("status", "published"),
        source=doc.get("source", "user"),
        views=doc.get("views", 0),
        likes_count=doc.get("likes_count", 0),
        favorites_count=doc.get("favorites_count", 0),
        avg_rating=doc.get("avg_rating"),
        ratings_count=doc.get("ratings_count", 0),
        liked_by_me=bool(liked_ids and rid in liked_ids),
        favorited_by_me=bool(fav_ids and rid in fav_ids),
        created_at=aware(created) if isinstance(created, datetime) else None,
        updated_at=aware(updated) if isinstance(updated, datetime) else None,
    )


def ensure_utc(value: Optional[datetime]) -> Optional[datetime]:
    if isinstance(value, datetime):
        return value.replace(tzinfo=timezone.utc) if value.tzinfo is None else value
    return value
