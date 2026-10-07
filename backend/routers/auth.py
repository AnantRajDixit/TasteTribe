"""Auth: register / login / logout / me / profile / password flows. Sessions are httpOnly cookies."""

import secrets
import uuid
from datetime import timedelta
from typing import Optional

from fastapi import APIRouter, Depends, HTTPException, Request, Response

from lib.auth import (
    COOKIE_NAME,
    create_session,
    destroy_session,
    get_current_user,
    hash_password,
    require_user,
    verify_password,
)
from lib.db import db
from lib.dates import aware, utcnow
from models.users import (
    ForgotPasswordIn,
    PasswordChange,
    ResetPasswordIn,
    UserCreate,
    UserLogin,
    UserOut,
    UserUpdate,
)

router = APIRouter(prefix="/auth", tags=["auth"])


@router.post("/register", response_model=UserOut, status_code=201)
async def register(body: UserCreate, response: Response):
    username = body.username.lower()
    email = body.email.lower()
    if await db.users.find_one({"username": username}):
        raise HTTPException(status_code=409, detail="That username is already taken.")
    if await db.users.find_one({"email": email}):
        raise HTTPException(status_code=409, detail="An account with that email already exists.")
    doc = {
        "id": str(uuid.uuid4()),
        "name": body.name.strip(),
        "username": username,
        "email": email,
        "password_hash": hash_password(body.password),
        "avatar_url": body.avatar_url,
        "bio": "",
        "role": "user",
        "created_at": utcnow(),
    }
    await db.users.insert_one(doc)
    await create_session(response, doc["id"])
    return UserOut(**doc)


@router.post("/login", response_model=UserOut)
async def login(body: UserLogin, response: Response):
    ident = body.username_or_email.strip().lower()
    doc = await db.users.find_one({"$or": [{"username": ident}, {"email": ident}]})
    if not doc or not verify_password(body.password, doc.get("password_hash", "")):
        raise HTTPException(status_code=401, detail="Invalid username or password.")
    await create_session(response, doc["id"])
    doc["created_at"] = aware(doc.get("created_at"))
    return UserOut(**doc)


@router.post("/logout")
async def logout(request: Request, response: Response):
    await destroy_session(request, response)
    return {"ok": True}


@router.get("/me", response_model=Optional[UserOut])
async def me(user: Optional[dict] = Depends(get_current_user)):
    if not user:
        return None
    user["created_at"] = aware(user.get("created_at"))
    return UserOut(**user)


@router.patch("/profile", response_model=UserOut)
async def update_profile(body: UserUpdate, user: dict = Depends(require_user)):
    updates = body.model_dump(exclude_unset=True)
    if "name" in updates and updates["name"] is not None:
        updates["name"] = updates["name"].strip()
    await db.users.update_one({"id": user["id"]}, {"$set": updates})
    doc = await db.users.find_one({"id": user["id"]})
    doc["created_at"] = aware(doc.get("created_at"))
    return UserOut(**doc)


@router.post("/change-password")
async def change_password(body: PasswordChange, request: Request, user: dict = Depends(require_user)):
    if not verify_password(body.current_password, user.get("password_hash", "")):
        raise HTTPException(status_code=400, detail="Your current password is incorrect.")
    await db.users.update_one({"id": user["id"]}, {"$set": {"password_hash": hash_password(body.new_password)}})
    token = request.cookies.get(COOKIE_NAME)
    await db.sessions.delete_many({"user_id": user["id"], "token": {"$ne": token}})
    return {"ok": True}


@router.post("/forgot-password")
async def forgot_password(body: ForgotPasswordIn):
    doc = await db.users.find_one({"email": body.email.lower()})
    if not doc:
        # Never reveal whether an email is registered.
        return {"ok": True, "reset_token": None}
    token = secrets.token_hex(16)
    await db.password_resets.insert_one(
        {"token": token, "user_id": doc["id"], "created_at": utcnow(), "expires_at": utcnow() + timedelta(hours=1)}
    )
    # Demo environment: no mail provider is attached, so the token is returned
    # directly instead of being emailed. In production this would go out by email.
    return {"ok": True, "reset_token": token, "message": "Use this token on the reset page (valid 1 hour)."}


@router.post("/reset-password")
async def reset_password(body: ResetPasswordIn):
    row = await db.password_resets.find_one({"token": body.token})
    if not row:
        raise HTTPException(status_code=400, detail="This reset link is invalid.")
    if aware(row["expires_at"]) < utcnow():
        raise HTTPException(status_code=400, detail="This reset link has expired.")
    await db.users.update_one({"id": row["user_id"]}, {"$set": {"password_hash": hash_password(body.new_password)}})
    await db.password_resets.delete_one({"token": body.token})
    await db.sessions.delete_many({"user_id": row["user_id"]})
    return {"ok": True}
