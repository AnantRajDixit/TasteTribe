import RecipeCard from "@/components/recipes/RecipeCard";
import type { Recipe } from "@/lib/types";

/**
 * Recipe grid with graceful empty/error states. Never gates the whole page on a
 * fetch — the caller renders its shell regardless.
 */
export default function RecipeGrid({
  recipes,
  isLoading,
  isError,
  emptyTitle = "No recipes here yet",
  emptyHint = "Try widening your filters, or be the first to publish something here.",
}: {
  recipes?: Recipe[];
  isLoading?: boolean;
  isError?: boolean;
  emptyTitle?: string;
  emptyHint?: string;
}) {
  if (isLoading) {
    return (
      <div
        className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4"
        data-testid="recipe-grid-loading"
      >
        {Array.from({ length: 8 }).map((_, index) => (
          <div key={index} className="overflow-hidden rounded-2xl border border-border bg-card">
            <div className="aspect-[4/3] animate-pulse bg-muted" />
            <div className="space-y-3 p-5">
              <div className="h-3 w-24 animate-pulse rounded bg-muted" />
              <div className="h-5 w-3/4 animate-pulse rounded bg-muted" />
              <div className="h-3 w-full animate-pulse rounded bg-muted" />
            </div>
          </div>
        ))}
      </div>
    );
  }

  if (isError) {
    return (
      <div
        className="rounded-2xl border border-dashed border-border bg-card p-12 text-center"
        data-testid="recipe-grid-error"
      >
        <h3 className="font-heading text-xl font-semibold">We couldn't load recipes</h3>
        <p className="mx-auto mt-2 max-w-md text-sm text-muted-foreground">
          The kitchen is briefly offline. Please refresh in a moment.
        </p>
      </div>
    );
  }

  if (!recipes || recipes.length === 0) {
    return (
      <div
        className="rounded-2xl border border-dashed border-border bg-card p-12 text-center"
        data-testid="recipe-grid-empty"
      >
        <h3 className="font-heading text-xl font-semibold">{emptyTitle}</h3>
        <p className="mx-auto mt-2 max-w-md text-sm text-muted-foreground">{emptyHint}</p>
      </div>
    );
  }

  return (
    <div
      className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4"
      data-testid="recipe-grid"
    >
      {recipes.map((recipe, index) => (
        <div
          key={recipe.id}
          className="animate-rise"
          style={{ animationDelay: `${Math.min(index, 8) * 0.05}s` }}
        >
          <RecipeCard recipe={recipe} />
        </div>
      ))}
    </div>
  );
}
