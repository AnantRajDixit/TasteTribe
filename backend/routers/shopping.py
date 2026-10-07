"""Persistent per-user shopping list with recipe merge and quantity scaling."""

import uuid
from typing import List

from fastapi import APIRouter, Depends, HTTPException
from pydantic import BaseModel

from lib.auth import require_user
from lib.db import db
from lib.recipe_view import scaled_ingredient
from models.shopping import AddItemIn, AddRecipeIn, ShoppingItem, ShoppingListOut

router = APIRouter(prefix="/shopping-list", tags=["shopping"])


class ItemToggle(BaseModel):
    checked: bool


async def _load(user_id: str) -> list[dict]:
    doc = await db.shopping_lists.find_one({"user_id": user_id})
    return list(doc.get("items", [])) if doc else []


async def _save(user_id: str, items: list[dict]) -> ShoppingListOut:
    await db.shopping_lists.update_one(
        {"user_id": user_id}, {"$set": {"items": items}}, upsert=True
    )
    return ShoppingListOut(items=[ShoppingItem(**i) for i in items])


@router.get("", response_model=ShoppingListOut)
async def get_list(user: dict = Depends(require_user)):
    return ShoppingListOut(items=[ShoppingItem(**i) for i in await _load(user["id"])])


@router.post("/items", response_model=ShoppingListOut)
async def add_item(body: AddItemIn, user: dict = Depends(require_user)):
    items = await _load(user["id"])
    items.append(
        {
            "id": str(uuid.uuid4()),
            "name": body.name.strip(),
            "quantity": body.quantity,
            "unit": body.unit,
            "checked": False,
            "recipe_title": None,
        }
    )
    return await _save(user["id"], items)


@router.post("/add-recipe", response_model=ShoppingListOut)
async def add_recipe(body: AddRecipeIn, user: dict = Depends(require_user)):
    """Merge a recipe's scaled ingredients into the list, combining duplicates."""
    recipe = await db.recipes.find_one({"id": body.recipe_id})
    if not recipe:
        raise HTTPException(status_code=404, detail="Recipe not found.")
    servings = body.servings or recipe["servings"]
    factor = servings / max(1, recipe["servings"])
    items = await _load(user["id"])
    for ing in recipe.get("ingredients", []):
        if ing.get("optional"):
            continue
        scaled = scaled_ingredient(ing, factor)
        match = next(
            (m for m in items if m["name"].lower() == scaled.name.lower() and m["unit"] == scaled.unit),
            None,
        )
        if match:
            match["quantity"] = round(match["quantity"] + scaled.quantity, 3)
        else:
            items.append(
                {
                    "id": str(uuid.uuid4()),
                    "name": scaled.name,
                    "quantity": scaled.quantity,
                    "unit": scaled.unit,
                    "checked": False,
                    "recipe_title": recipe["title"],
                }
            )
    return await _save(user["id"], items)


@router.patch("/items/{item_id}", response_model=ShoppingListOut)
async def toggle_item(item_id: str, body: ItemToggle, user: dict = Depends(require_user)):
    items = await _load(user["id"])
    for item in items:
        if item["id"] == item_id:
            item["checked"] = body.checked
            break
    return await _save(user["id"], items)


@router.delete("/items/{item_id}", response_model=ShoppingListOut)
async def remove_item(item_id: str, user: dict = Depends(require_user)):
    items = [i for i in await _load(user["id"]) if i["id"] != item_id]
    return await _save(user["id"], items)


@router.post("/clear-checked", response_model=ShoppingListOut)
async def clear_checked(user: dict = Depends(require_user)):
    items = [i for i in await _load(user["id"]) if not i["checked"]]
    return await _save(user["id"], items)


@router.delete("", response_model=ShoppingListOut)
async def clear_all(user: dict = Depends(require_user)):
    return await _save(user["id"], [])
