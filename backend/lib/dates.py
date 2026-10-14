"""Server-side date helpers. The pod clock is UTC — anchor "today" here, never in the browser."""

import os
from datetime import datetime
from zoneinfo import ZoneInfo


def today_iso(tz: str | None = None) -> str:
    """Today's date as YYYY-MM-DD in `tz` (default: APP_TZ env, else UTC)."""
    zone = tz or os.environ.get("APP_TZ", "UTC")
    return datetime.now(ZoneInfo(zone)).strftime("%Y-%m-%d")


def utcnow() -> datetime:
    """Aware UTC now — store aware, normalise naive reads back with `aware()`."""
    return datetime.now(timezone.utc)


def aware(dt: datetime) -> datetime:
    """Motor hands naive datetimes back from BSON — re-attach UTC before use/serialise."""
    return dt.replace(tzinfo=timezone.utc) if dt.tzinfo is None else dt
