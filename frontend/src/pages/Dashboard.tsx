import { useEffect } from "react";
import { Link, useNavigate, useSearchParams } from "react-router-dom";
import { useQuery } from "@tanstack/react-query";
import { PlusCircle } from "lucide-react";
import { apiGet } from "@/lib/api";
import { buttonVariants } from "@/components/ui/button";
import { Tabs, TabsList, TabsTrigger } from "@/components/ui/tabs";
import PageShell from "@/components/layout/PageShell";
import RecipeGrid from "@/components/recipes/RecipeGrid";
import { useMe } from "@/lib/session";
import type { Profile, Recipe, RecipePage } from "@/lib/types";

const TABS = [
  { value: "mine", label: "My recipes" },
  { value: "saved", label: "Saved" },
  { value: "drafts", label: "Drafts" },
];

export default function Dashboard() {
  const { data: me, isLoading } = useMe();
  const navigate = useNavigate();
  const [params, setParams] = useSearchParams();
  const tab = params.get("tab") ?? "mine";

  useEffect(() => {
    if (!isLoading && !me) navigate("/login?next=/dashboard");
  }, [me, isLoading, navigate]);

  const profile = useQuery({
    queryKey: ["profile", me?.username],
    queryFn: () => apiGet<Profile>(`/users/${me?.username}`),
    enabled: Boolean(me),
    retry: false,
  });

  const mine = useQuery({
    queryKey: ["users", me?.username, "recipes", "all"],
    queryFn: () => apiGet<Recipe[]>(`/users/${me?.username}/recipes?status=all`),
    enabled: Boolean(me),
    retry: false,
  });

  const saved = useQuery({
    queryKey: ["dashboard", "saved"],
    queryFn: () => apiGet<RecipePage>("/recipes?limit=48"),
    enabled: Boolean(me) && tab === "saved",
    retry: false,
  });

  const published = (mine.data ?? []).filter((r) => r.status === "published");
  const drafts = (mine.data ?? []).filter((r) => r.status === "draft");
  const savedRecipes = (saved.data?.items ?? []).filter((r) => r.favoritedByMe);

  const stats = [
    { label: "Recipes published", value: published.length },
    { label: "Drafts", value: drafts.length },
    { label: "Followers", value: profile.data?.followersCount ?? 0 },
    {
      label: "Avg rating",
      value: profile.data?.avgRating ? profile.data.avgRating.toFixed(1) : "—",
    },
  ];

  return (
    <PageShell>
      <div className="mx-auto max-w-7xl px-4 py-10 sm:px-6 lg:px-8">
        <header className="flex flex-wrap items-end justify-between gap-4">
          <div>
            <p className="font-mono text-xs uppercase tracking-wider text-muted-foreground">
              Your kitchen
            </p>
            <h1 className="mt-2 font-heading text-4xl font-bold tracking-tight">
              {me ? `Welcome back, ${me.name.split(" ")[0]}` : "Dashboard"}
            </h1>
          </div>
          <Link to="/recipes/new" className={buttonVariants()} data-testid="dashboard-create-btn">
            <PlusCircle className="mr-2 size-4" /> Add a recipe
          </Link>
        </header>

        <div className="mt-8 grid grid-cols-2 gap-4 lg:grid-cols-4">
          {stats.map((stat) => (
            <div
              key={stat.label}
              className="rounded-2xl border border-border bg-card p-5"
              data-testid="dashboard-stat-card"
            >
              <p className="font-mono text-xs uppercase tracking-wider text-muted-foreground">
                {stat.label}
              </p>
              <p className="mt-2 font-heading text-3xl font-bold">{stat.value}</p>
            </div>
          ))}
        </div>

        <Tabs
          value={tab}
          onValueChange={(value) => setParams(new URLSearchParams({ tab: String(value) }))}
          className="mt-10"
        >
          <TabsList variant="line" data-testid="dashboard-tabs">
            {TABS.map((item) => (
              <TabsTrigger key={item.value} value={item.value} data-testid={`dashboard-tab-${item.value}`}>
                {item.label}
              </TabsTrigger>
            ))}
          </TabsList>
        </Tabs>

        <div className="mt-8">
          {tab === "mine" && (
            <RecipeGrid
              recipes={published}
              isLoading={mine.isLoading}
              isError={mine.isError}
              emptyTitle="You haven't published anything yet"
              emptyHint="Share your first recipe — the community is waiting."
            />
          )}
          {tab === "drafts" && (
            <RecipeGrid
              recipes={drafts}
              isLoading={mine.isLoading}
              isError={mine.isError}
              emptyTitle="No drafts"
              emptyHint="Recipes you save as drafts stay private until you publish them."
            />
          )}
          {tab === "saved" && (
            <RecipeGrid
              recipes={savedRecipes}
              isLoading={saved.isLoading}
              isError={saved.isError}
              emptyTitle="Nothing saved yet"
              emptyHint="Tap the bookmark on any recipe to keep it here."
            />
          )}
        </div>
      </div>
    </PageShell>
  );
}
