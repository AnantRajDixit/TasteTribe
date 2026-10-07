import { useQuery } from "@tanstack/react-query";
import { useSearchParams } from "react-router-dom";
import { apiGet } from "@/lib/api";
import PageShell from "@/components/layout/PageShell";
import RecipeGrid from "@/components/recipes/RecipeGrid";
import { Tabs, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { useMe } from "@/lib/session";
import type { RecipePage } from "@/lib/types";

const TABS = [
  { value: "latest", label: "Latest" },
  { value: "trending", label: "Trending" },
  { value: "following", label: "People you follow" },
  { value: "recommended", label: "Recommended" },
];

export default function Feed() {
  const [params, setParams] = useSearchParams();
  const { data: me } = useMe();
  const tab = params.get("tab") ?? "latest";

  const query = useQuery({
    queryKey: ["feed", tab],
    queryFn: () => apiGet<RecipePage>(`/feed?tab=${tab}&page=1&limit=16`),
    retry: false,
  });

  const needsLogin = !me && (tab === "following" || tab === "recommended");

  return (
    <PageShell>
      <div className="mx-auto max-w-7xl px-4 py-10 sm:px-6 lg:px-8">
        <header>
          <p className="font-mono text-xs uppercase tracking-wider text-muted-foreground">
            The community table
          </p>
          <h1 className="mt-2 font-heading text-4xl font-bold tracking-tight">Your feed</h1>
        </header>

        <Tabs
          value={tab}
          onValueChange={(value) => setParams(new URLSearchParams({ tab: String(value) }))}
          className="mt-8"
        >
          <TabsList variant="line" data-testid="feed-tabs">
            {TABS.map((item) => (
              <TabsTrigger key={item.value} value={item.value} data-testid={`feed-tab-${item.value}`}>
                {item.label}
              </TabsTrigger>
            ))}
          </TabsList>
        </Tabs>

        <div className="mt-8">
          {needsLogin ? (
            <div
              className="rounded-2xl border border-dashed border-border bg-card p-12 text-center"
              data-testid="feed-login-prompt"
            >
              <h3 className="font-heading text-xl font-semibold">Log in to personalise this feed</h3>
              <p className="mx-auto mt-2 max-w-md text-sm text-muted-foreground">
                Follow cooks and save recipes, and we'll tailor what appears here.
              </p>
            </div>
          ) : (
            <RecipeGrid
              recipes={query.data?.items}
              isLoading={query.isLoading}
              isError={query.isError}
              emptyTitle={
                tab === "following" ? "You're not following anyone yet" : "Nothing here yet"
              }
              emptyHint={
                tab === "following"
                  ? "Visit a chef's profile and hit Follow to fill this feed."
                  : "Save or rate a few recipes and this will fill up fast."
              }
            />
          )}
        </div>
      </div>
    </PageShell>
  );
}
