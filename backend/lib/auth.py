"""Password hashing + cookie sessions. Sessions are httpOnly cookies; tokens live in Mongo."""

import secrets
from datetime import timedelta

from fastapi import Depends, HTTPException, Request, Response
from passlib.context import CryptContext
from datetime import datetime, timezone

from lib.db import db
from lib.dates import utcnow

pwd_ctx = CryptContext(schemes=["bcrypt"], deprecated="auto")

COOKIE_NAME = "tt_session"
SESSION_DAYS = 7


def hash_password(plain: str) -> str:
    return pwd_ctx.hash(plain)


def verify_password(plain: str, hashed: str) -> bool:
    try:
        return pwd_ctx.verify(plain, hashed)
    except Exception:
        return False


async def create_session(response: Response, user_id: str) -> None:
    """Create a session row + set the httpOnly cookie (rides same-origin fetches)."""
    token = secrets.token_hex(32)
    now = utcnow()
    await db.sessions.insert_one(
        {
            "token": token,
            "user_id": user_id,
            "created_at": now,
            "expires_at": now + timedelta(days=SESSION_DAYS),
        }
    )
    response.set_cookie(
        COOKIE_NAME,
        token,
        httponly=True,
        samesite="lax",
        max_age=SESSION_DAYS * 86400,
        path="/",
    )


async def destroy_session(request: Request, response: Response) -> None:
    token = request.cookies.get(COOKIE_NAME)
    if token:
        await db.sessions.delete_one({"token": token})
    response.delete_cookie(COOKIE_NAME, path="/")


async def get_current_user(request: Request) -> dict | None:
    """Resolve the session cookie to a user document, or None."""
    token = request.cookies.get(COOKIE_NAME)
    if not token:
        return None
    session = await db.sessions.find_one({"token": token})
    if not session:
        return None
    expires_at = session["expires_at"]
    if expires_at.tzinfo is None:
        expires_at = expires_at.replace(tzinfo=timezone.utc)
    if expires_at < utcnow():
        await db.sessions.delete_one({"_id": session["_id"]})
        return None
    return await db.users.find_one({"id": session["user_id"]})


async def require_user(request: Request) -> dict:
    user = await get_current_user(request)
    if not user:
        raise HTTPException(status_code=401, detail="You need to be logged in for that.")
    return user


async def require_admin(user: dict = Depends(require_user)) -> dict:
    """Dependency chain: require_user first, then the role check."""
    if user.get("role") != "admin":
        raise HTTPException(status_code=403, detail="Admin access required.")
    return user
