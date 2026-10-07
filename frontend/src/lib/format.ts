import { ApiError } from "@/lib/api";

/** Pull a human-readable message out of an ApiError / unknown throwable. */
export function errorMessage(error: unknown, fallback = "Something went wrong."): string {
  if (error instanceof ApiError) {
    const body = error.body as { detail?: unknown } | null;
    const detail = body?.detail;
    if (typeof detail === "string") return detail;
    if (Array.isArray(detail) && detail.length > 0) {
      const first = detail[0];
      if (typeof first === "string") return first;
      if (first && typeof first === "object" && "msg" in first) {
        return String((first as { msg: unknown }).msg);
      }
    }
    if (error.status === 401) return "Please log in to continue.";
    if (error.status === 403) return "You do not have permission to do that.";
    if (error.status === 404) return "We could not find that.";
  }
  if (error instanceof Error && error.message) return error.message;
  return fallback;
}

/** "1 h 25 min" style duration from a minute count. */
export function formatMinutes(total: number): string {
  if (!total || total <= 0) return "—";
  const hours = Math.floor(total / 60);
  const minutes = total % 60;
  if (hours === 0) return `${minutes} min`;
  if (minutes === 0) return `${hours} h`;
  return `${hours} h ${minutes} min`;
}

/** Humanise a quantity with common fractions (mirrors the backend formatter). */
export function formatQuantity(value: number): string {
  const rounded = Math.round(value * 100) / 100;
  const whole = Math.floor(rounded + 1e-9);
  const frac = rounded - whole;
  const table: Array<[number, string]> = [
    [0.25, "1/4"],
    [1 / 3, "1/3"],
    [0.5, "1/2"],
    [2 / 3, "2/3"],
    [0.75, "3/4"],
  ];
  let best = table[0];
  for (const entry of table) {
    if (Math.abs(entry[0] - frac) < Math.abs(best[0] - frac)) best = entry;
  }
  if (Math.abs(best[0] - frac) <= 0.03) {
    return whole > 0 ? `${whole} ${best[1]}` : best[1];
  }
  if (Math.abs(rounded - Math.round(rounded)) < 0.01) return String(Math.round(rounded));
  return String(rounded);
}

/** Relative time like "3 days ago". */
export function timeAgo(iso?: string | null): string {
  if (!iso) return "";
  const then = new Date(iso).getTime();
  if (Number.isNaN(then)) return "";
  const seconds = Math.max(1, Math.floor((Date.now() - then) / 1000));
  const units: Array<[number, string]> = [
    [60, "second"],
    [60, "minute"],
    [24, "hour"],
    [30, "day"],
    [12, "month"],
    [Number.POSITIVE_INFINITY, "year"],
  ];
  let value = seconds;
  let label = "second";
  for (const [step, name] of units) {
    if (value < step) {
      label = name;
      break;
    }
    value = Math.floor(value / step);
    label = name;
  }
  const rounded = Math.max(1, Math.floor(value));
  return `${rounded} ${label}${rounded === 1 ? "" : "s"} ago`;
}

/** Deterministic warm placeholder when a recipe has no cover image. */
export function coverFor(recipe: { coverImage?: string | null; title: string }): string {
  if (recipe.coverImage) return recipe.coverImage;
  const fallbacks = [
    "https://images.unsplash.com/photo-1516100882582-96c3a05fe590?crop=entropy&cs=srgb&fm=jpg&q=85&w=1200",
    "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?crop=entropy&cs=srgb&fm=jpg&q=85&w=1200",
    "https://images.unsplash.com/photo-1556029096-6696c16e115d?crop=entropy&cs=srgb&fm=jpg&q=85&w=1200",
    "https://images.unsplash.com/photo-1560179524-382d0d0f27f9?crop=entropy&cs=srgb&fm=jpg&q=85&w=1200",
  ];
  let hash = 0;
  for (let i = 0; i < recipe.title.length; i += 1) {
    hash = (hash * 31 + recipe.title.charCodeAt(i)) % 9973;
  }
  return fallbacks[hash % fallbacks.length];
}

/** Initials for avatar fallbacks. */
export function initials(name?: string | null): string {
  if (!name) return "?";
  return name
    .trim()
    .split(/\s+/)
    .slice(0, 2)
    .map((part) => part[0]?.toUpperCase() ?? "")
    .join("");
}
