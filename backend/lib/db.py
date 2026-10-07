"""Shared Mongo handle — import `client`/`db` from here (server.py, routers, seed.py)."""

import logging
import os
from pathlib import Path

from datetime import datetime, timezone

from dotenv import load_dotenv
from motor.motor_asyncio import AsyncIOMotorClient
from pymongo import ASCENDING, DESCENDING, IndexModel

load_dotenv(Path(__file__).parent.parent / ".env")

mongo_url = os.environ["MONGO_URL"]
client = AsyncIOMotorClient(mongo_url)
db = client[os.environ["DB_NAME"]]

logger = logging.getLogger(__name__)

# One entry per collection: every field a route filters, sorts, or dedupes on. Applied by ensure_indexes() at startup.
INDEXES: dict[str, list[IndexModel]] = {
    "status_checks": [IndexModel([("timestamp", DESCENDING)], name="timestamp_desc")],
    "users": [
        IndexModel([("username", ASCENDING)], name="username_unique", unique=True),
        IndexModel([("email", ASCENDING)], name="email_unique", unique=True),
    ],
    "sessions": [
        IndexModel([("token", ASCENDING)], name="token_unique", unique=True),
        IndexModel([("user_id", ASCENDING)], name="user_id"),
    ],
    "password_resets": [
        IndexModel([("token", ASCENDING)], name="token_unique", unique=True),
        IndexModel([("expires_at", ASCENDING)], name="expires_at"),
    ],
    "recipes": [
        IndexModel([("id", ASCENDING)], name="id_unique", unique=True),
        IndexModel([("status", ASCENDING), ("created_at", DESCENDING)], name="status_created"),
        IndexModel([("author_id", ASCENDING)], name="author_id"),
        IndexModel([("category", ASCENDING)], name="category"),
        IndexModel([("cuisine", ASCENDING)], name="cuisine"),
        IndexModel([("avg_rating", DESCENDING)], name="avg_rating"),
        IndexModel([("likes_count", DESCENDING)], name="likes_count"),
    ],
    "reviews": [
        IndexModel([("recipe_id", ASCENDING), ("user_id", ASCENDING)], name="recipe_user", unique=True),
        IndexModel([("recipe_id", ASCENDING), ("created_at", DESCENDING)], name="recipe_created"),
    ],
    "comments": [
        IndexModel([("recipe_id", ASCENDING), ("created_at", DESCENDING)], name="recipe_created"),
    ],
    "likes": [
        IndexModel([("user_id", ASCENDING), ("recipe_id", ASCENDING)], name="user_recipe", unique=True),
        IndexModel([("recipe_id", ASCENDING)], name="recipe_id"),
    ],
    "favorites": [
        IndexModel([("user_id", ASCENDING), ("recipe_id", ASCENDING)], name="user_recipe", unique=True),
        IndexModel([("recipe_id", ASCENDING)], name="recipe_id"),
    ],
    "follows": [
        IndexModel([("follower_id", ASCENDING), ("following_id", ASCENDING)], name="pair", unique=True),
        IndexModel([("following_id", ASCENDING)], name="following_id"),
    ],
    "recently_viewed": [
        IndexModel([("user_id", ASCENDING), ("viewed_at", DESCENDING)], name="user_viewed"),
    ],
    "categories": [
        IndexModel([("slug", ASCENDING)], name="slug_unique", unique=True),
    ],
    "reports": [
        IndexModel([("status", ASCENDING), ("created_at", DESCENDING)], name="status_created"),
    ],
    "shopping_lists": [
        IndexModel([("user_id", ASCENDING)], name="user_unique", unique=True),
    ],
    "ai_messages": [
        IndexModel([("session_id", ASCENDING), ("created_at", ASCENDING)], name="session_created"),
    ],
}


async def ensure_indexes() -> None:
    for collection, models in INDEXES.items():
        for model in models:  # one at a time so a bad spec skips only itself
            try:
                await db[collection].create_indexes([model])
            except Exception as exc:  # never block boot on an index; the log line names what to fix
                logger.error("ensure_indexes(%s.%s): %s", collection, model.document["name"], exc)
