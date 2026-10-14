import { useQuery, useQueryClient } from "@tanstack/react-query";
import { apiGet, apiPost } from "@/lib/api";
import type { User } from "@/lib/types";
import { queryClient } from "@/lib/queryClient";

/**
 * Session helpers. The backend sets an httpOnly cookie, so there is no token to
 * store here — `GET /api/auth/me` answers "who am I" and returns null when anonymous.
 */

export const ME_KEY = ["auth", "me"] as const;

export const fetchMe = () => apiGet<User | null>("/auth/me");

/** Current user, or null. Never throws for anonymous visitors. */
export function useMe() {
  return useQuery({
    queryKey: ME_KEY,
    queryFn: fetchMe,
    retry: false,
    staleTime: 30_000,
  });
}

/** Call after a successful login/signup so every query refetches as the new user. */
export async function beginSession() {
  await queryClient.invalidateQueries();
}

/**
 * Every sign-out control must route through here: clearing only the server session
 * would leak the previous account's cached data to the next login in this browser.
 */
export async function endSession() {
  try {
    await apiPost("/auth/logout");
  } finally {
    queryClient.clear();
    await queryClient.invalidateQueries({ queryKey: ME_KEY });
  }
}

export { useQueryClient };
