import { useSearchParams } from "react-router-dom";
import { useQuery } from "@tanstack/react-query";
import { SlidersHorizontal, X } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Badge } from "@/components/ui/badge";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { apiGet } from "@/lib/api";
import PageShell from "@/components/layout/PageShell";
import RecipeGrid from "@/components/recipes/RecipeGrid";
import { CUISINES, CATEGORY_NAMES, DIFFICULTIES, DIETS } from "@/lib/types";
import type { RecipePage } from "@/lib/types";

const SORTS: Array<{ value: string; label: string }> = [
  { value: "newest", label: "Newest" },
  { value: "rating", label: "Highest rated" },
  { value: "popular", label: "Most popular" },
  { value: "time", label: "Quickest" },
];

const ANY = "any";

export default function Explore() {
  const [params, setParams] = useSearchParams();

  const read = (key: string) => params.get(key) ?? "";
  const page = Math.max(1, Number(params.get("page") ?? 1));

  const update = (key: string, value: string) => {
    const next = new URLSearchParams(params);
    if (!value || value === ANY) next.delete(key);
    else next.set(key, value);
    next.delete("page");
    setParams(next);
  };

  const query = useQuery({
    queryKey: ["recipes", params.toString()],
    queryFn: () => {
      const search = new URLSearchParams(params);
      search.set("page", String(page));
      search.set("limit", "12");
      return apiGet<RecipePage>(`/recipes?${search.toString()}`);
    },
    retry: false,
  });

  const activeFilters = ["q", "ingredient", "cuisine", "category", "difficulty", "dietary", "maxTime"]
    .map((key) => ({ key, value: read(key) }))
    .filter((entry) => entry.value);

  return (
    <PageShell>
      <div className="mx-auto max-w-7xl px-4 py-10 sm:px-6 lg:px-8">
        <header>
          <p className="font-mono text-xs uppercase tracking-wider text-muted-foreground">
            Recipe discovery
          </p>
          <h1 className="mt-2 font-heading text-4xl font-bold tracking-tight">Explore recipes</h1>
          <p className="mt-2 max-w-xl text-muted-foreground">
            Filter by cuisine, course, diet, difficulty or the ingredient you need to use up.
          </p>
        </header>

        <div className="mt-10 grid gap-8 lg:grid-cols-4">
          {/* Filters */}
          <aside className="lg:col-span-1">
            <div className="rounded-2xl border border-border bg-card p-5">
              <div className="flex items-center gap-2">
                <SlidersHorizontal className="size-4" />
                <h2 className="font-heading text-lg font-semibold">Filters</h2>
              </div>

              <div className="mt-5 space-y-5">
                <div className="space-y-2">
                  <Label htmlFor="filter-q">Search</Label>
                  <Input
                    id="filter-q"
                    defaultValue={read("q")}
                    onBlur={(event) => update("q", event.target.value)}
                    onKeyDown={(event) => {
                      if (event.key === "Enter") update("q", (event.target as HTMLInputElement).value);
                    }}
                    placeholder="Recipe name or tag"
                    data-testid="filter-search-input"
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="filter-ingredient">Ingredient</Label>
                  <Input
                    id="filter-ingredient"
                    defaultValue={read("ingredient")}
                    onBlur={(event) => update("ingredient", event.target.value)}
                    onKeyDown={(event) => {
                      if (event.key === "Enter")
                        update("ingredient", (event.target as HTMLInputElement).value);
                    }}
                    placeholder="e.g. paneer"
                    data-testid="filter-ingredient-input"
                  />
                </div>

                <div className="space-y-2">
                  <Label>Cuisine</Label>
                  <Select
                    value={read("cuisine") || ANY}
                    onValueChange={(value: string) => update("cuisine", value)}
                  >
                    <SelectTrigger data-testid="filter-cuisine-select">
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value={ANY}>Any cuisine</SelectItem>
                      {CUISINES.map((item) => (
                        <SelectItem key={item} value={item}>
                          {item}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>

                <div className="space-y-2">
                  <Label>Course</Label>
                  <Select
                    value={read("category") || ANY}
                    onValueChange={(value: string) => update("category", value)}
                  >
                    <SelectTrigger data-testid="filter-category-select">
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value={ANY}>Any course</SelectItem>
                      {CATEGORY_NAMES.map((item) => (
                        <SelectItem key={item} value={item}>
                          {item}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>

                <div className="space-y-2">
                  <Label>Difficulty</Label>
                  <Select
                    value={read("difficulty") || ANY}
                    onValueChange={(value: string) => update("difficulty", value)}
                  >
                    <SelectTrigger data-testid="filter-difficulty-select">
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value={ANY}>Any difficulty</SelectItem>
                      {DIFFICULTIES.map((item) => (
                        <SelectItem key={item} value={item}>
                          {item}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>

                <div className="space-y-2">
                  <Label>Dietary</Label>
                  <Select
                    value={read("dietary") || ANY}
                    onValueChange={(value: string) => update("dietary", value)}
                  >
                    <SelectTrigger data-testid="filter-dietary-select">
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value={ANY}>Any diet</SelectItem>
                      {DIETS.map((item) => (
                        <SelectItem key={item} value={item}>
                          {item}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>

                <div className="space-y-2">
                  <Label htmlFor="filter-time">Max total time (minutes)</Label>
                  <Input
                    id="filter-time"
                    type="number"
                    min={5}
                    defaultValue={read("maxTime")}
                    onBlur={(event) => update("maxTime", event.target.value)}
                    placeholder="e.g. 30"
                    data-testid="filter-time-input"
                  />
                </div>

                <Button
                  variant="outline"
                  className="w-full"
                  onClick={() => setParams(new URLSearchParams())}
                  data-testid="filter-clear-btn"
                >
                  Clear all filters
                </Button>
              </div>
            </div>
          </aside>

          {/* Results */}
          <div className="lg:col-span-3">
            <div className="flex flex-wrap items-center justify-between gap-4">
              <p className="text-sm text-muted-foreground" data-testid="results-count">
                {query.data ? `${query.data.total} recipes found` : "Searching the kitchen…"}
              </p>
              <div className="flex items-center gap-2">
                <Label htmlFor="sort" className="text-sm">
                  Sort
                </Label>
                <Select
                  value={read("sort") || "newest"}
                  onValueChange={(value: string) => update("sort", value)}
                >
                  <SelectTrigger className="w-44" data-testid="sort-by-select">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    {SORTS.map((item) => (
                      <SelectItem key={item.value} value={item.value}>
                        {item.label}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
            </div>

            {activeFilters.length > 0 && (
              <div className="mt-4 flex flex-wrap gap-2" data-testid="active-filter-pills">
                {activeFilters.map((entry) => (
                  <Badge
                    key={entry.key}
                    variant="secondary"
                    className="gap-1.5 py-1 pl-3 pr-1.5"
                  >
                    {entry.value}
                    <button
                      type="button"
                      aria-label={`Remove ${entry.key} filter`}
                      onClick={() => update(entry.key, "")}
                      className="rounded-full p-0.5 transition-colors duration-150 hover:bg-background"
                    >
                      <X className="size-3" />
                    </button>
                  </Badge>
                ))}
              </div>
            )}

            <div className="mt-6">
              <RecipeGrid
                recipes={query.data?.items}
                isLoading={query.isLoading}
                isError={query.isError}
              />
            </div>

            {query.data && query.data.pages > 1 && (
              <div className="mt-10 flex items-center justify-center gap-3">
                <Button
                  variant="outline"
                  disabled={page <= 1}
                  onClick={() => {
                    const next = new URLSearchParams(params);
                    next.set("page", String(page - 1));
                    setParams(next);
                  }}
                  data-testid="pagination-prev-btn"
                >
                  Previous
                </Button>
                <span className="text-sm text-muted-foreground" data-testid="pagination-status">
                  Page {query.data.page} of {query.data.pages}
                </span>
                <Button
                  variant="outline"
                  disabled={page >= query.data.pages}
                  onClick={() => {
                    const next = new URLSearchParams(params);
                    next.set("page", String(page + 1));
                    setParams(next);
                  }}
                  data-testid="pagination-next-btn"
                >
                  Next
                </Button>
              </div>
            )}
          </div>
        </div>
      </div>
    </PageShell>
  );
}
