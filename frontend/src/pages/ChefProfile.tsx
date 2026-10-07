import { useMutation, useQuery } from "@tanstack/react-query";
import { Link, useParams } from "react-router-dom";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { apiDelete, apiGet, apiPost } from "@/lib/api";
import { queryClient } from "@/lib/queryClient";
import { useMe } from "@/lib/session";
import { errorMessage, initials } from "@/lib/format";
import PageShell from "@/components/layout/PageShell";
import RecipeGrid from "@/components/recipes/RecipeGrid";
import type { FollowResponse, Profile, Recipe } from "@/lib/types";

export default function ChefProfile() {
  const { username = "" } = useParams();
  const { data: me } = useMe();

  const profile = useQuery({
    queryKey: ["profile", username],
    queryFn: () => apiGet<Profile>(`/users/${username}`),
    retry: false,
  });

  const recipes = useQuery({
    queryKey: ["users", username, "recipes"],
    queryFn: () => apiGet<Recipe[]>(`/users/${username}/recipes`),
    retry: false,
  });

  const follow = useMutation({
    mutationFn: (next: boolean) =>
      next
        ? apiPost<FollowResponse>(`/users/${username}/follow`)
        : apiDelete<FollowResponse>(`/users/${username}/follow`),
    onSuccess: (data) => {
      toast.success(data.following ? `Following @${username}` : `Unfollowed @${username}`);
      void queryClient.invalidateQueries();
    },
    onError: (e) => toast.error(errorMessage(e)),
  });

  const chef = profile.data;
  const isSelf = me?.username === username;

  if (profile.isError) {
    return (
      <PageShell>
        <div className="mx-auto max-w-2xl px-4 py-24 text-center">
          <h1 className="font-heading text-3xl font-bold">Chef not found</h1>
          <p className="mt-3 text-muted-foreground">This profile doesn't exist.</p>
        </div>
      </PageShell>
    );
  }

  const stats = [
    { label: "Recipes", value: chef?.recipesCount ?? 0, testid: "profile-recipes-count" },
    { label: "Followers", value: chef?.followersCount ?? 0, testid: "profile-followers-count" },
    { label: "Following", value: chef?.followingCount ?? 0, testid: "profile-following-count" },
    {
      label: "Avg rating",
      value: chef?.avgRating ? chef.avgRating.toFixed(1) : "—",
      testid: "profile-avg-rating",
    },
  ];

  return (
    <PageShell>
      <div className="border-b border-border bg-card">
        <div className="mx-auto max-w-7xl px-4 py-12 sm:px-6 lg:px-8">
          <div className="flex flex-wrap items-start gap-6">
            <span className="flex size-24 items-center justify-center overflow-hidden rounded-2xl bg-muted font-heading text-2xl font-bold">
              {chef?.avatarUrl ? (
                <img src={chef.avatarUrl} alt={chef.name} className="size-full object-cover" />
              ) : (
                initials(chef?.name)
              )}
            </span>
            <div className="flex-1">
              <h1 className="font-heading text-3xl font-bold tracking-tight" data-testid="profile-name">
                {chef?.name ?? username}
              </h1>
              <p className="mt-1 font-mono text-sm text-muted-foreground">@{username}</p>
              {chef?.bio && <p className="mt-3 max-w-xl leading-relaxed text-muted-foreground">{chef.bio}</p>}
            </div>
            {me && !isSelf && (
              <Button
                variant={chef?.isFollowing ? "outline" : "default"}
                onClick={() => follow.mutate(!chef?.isFollowing)}
                disabled={follow.isPending}
                data-testid="profile-follow-btn"
              >
                {chef?.isFollowing ? "Following" : "Follow"}
              </Button>
            )}
          </div>

          <dl className="mt-8 grid grid-cols-2 gap-4 sm:grid-cols-4">
            {stats.map((stat) => (
              <div key={stat.label} className="rounded-xl bg-secondary p-4" data-testid={stat.testid}>
                <dt className="font-mono text-xs uppercase tracking-wider text-muted-foreground">
                  {stat.label}
                </dt>
                <dd className="mt-1.5 font-heading text-2xl font-bold">{stat.value}</dd>
              </div>
            ))}
          </dl>
        </div>
      </div>

      <div className="mx-auto max-w-7xl px-4 py-12 sm:px-6 lg:px-8">
        <h2 className="font-heading text-2xl font-semibold tracking-tight">
          Published recipes
        </h2>
        <div className="mt-6">
          <RecipeGrid
            recipes={recipes.data}
            isLoading={recipes.isLoading}
            isError={recipes.isError}
            emptyTitle={isSelf ? "You haven't published anything yet" : "No published recipes yet"}
            emptyHint={
              isSelf ? "Share your first dish with the community." : "Check back soon for new dishes."
            }
          />
        </div>
      </div>
    </PageShell>
  );
}
