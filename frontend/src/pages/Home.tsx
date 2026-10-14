import { Link } from "react-router-dom";
import { useQuery } from "@tanstack/react-query";
import { ArrowRight, Sparkles, ChefHat, Utensils } from "lucide-react";
import { apiGet } from "@/lib/api";
import { buttonVariants } from "@/components/ui/button";
import PageShell from "@/components/layout/PageShell";
import RecipeGrid from "@/components/recipes/RecipeGrid";
import type { Category, RecipePage } from "@/lib/types";

const HERO_IMAGE =
  "https://images.unsplash.com/photo-1516100882582-96c3a05fe590?crop=entropy&cs=srgb&fm=jpg&q=85&w=1600";

export default function Home() {
  const trending = useQuery({
    queryKey: ["feed", "trending", 1, 8],
    queryFn: () => apiGet<RecipePage>("/feed?tab=trending&page=1&limit=8"),
    retry: false,
  });

  const latest = useQuery({
    queryKey: ["feed", "latest", 1, 4],
    queryFn: () => apiGet<RecipePage>("/feed?tab=latest&page=1&limit=4"),
    retry: false,
  });

  const categories = useQuery({
    queryKey: ["categories"],
    queryFn: () => apiGet<Category[]>("/categories"),
    retry: false,
  });

  return (
    <PageShell>
      {/* Hero */}
      <section className="relative overflow-hidden">
        <img src={HERO_IMAGE} alt="" aria-hidden className="absolute inset-0 size-full object-cover" />
        <div
          aria-hidden
          className="absolute inset-0"
          style={{
            background:
              "linear-gradient(180deg, rgba(28,25,23,0.85) 0%, rgba(28,25,23,0.60) 50%, rgba(28,25,23,0.92) 100%)",
          }}
        />
        <div className="relative mx-auto max-w-7xl px-4 py-24 sm:px-6 lg:px-8 lg:py-32">
          <div className="max-w-2xl">
            <p className="font-mono text-xs uppercase tracking-wider text-orange-300">
              A community kitchen
            </p>
            <h1 className="mt-4 font-heading text-4xl font-bold leading-[1.1] tracking-tight text-stone-50 sm:text-5xl lg:text-6xl">
              Real recipes from cooks who actually make them
            </h1>
            <p className="mt-6 max-w-xl text-lg leading-relaxed text-stone-200">
              Browse, rate and save dishes from home cooks worldwide. Scale any recipe to
              your table, build a shopping list in one tap, and ask the AI sous-chef what
              to do with what's already in your fridge.
            </p>
            <div className="mt-9 flex flex-wrap gap-3">
              <Link
                to="/recipes"
                data-testid="hero-explore-btn"
                className={buttonVariants({ size: "lg" })}
              >
                Explore recipes
                <ArrowRight className="ml-2 size-4" />
              </Link>
              <Link
                to="/register"
                data-testid="hero-join-btn"
                className={
                  buttonVariants({ variant: "outline", size: "lg" }) +
                  " border-stone-300 bg-transparent text-stone-50 hover:bg-stone-50 hover:text-stone-900"
                }
              >
                Share your cooking
              </Link>
            </div>
          </div>
        </div>
      </section>

      {/* Value strip */}
      <section className="border-b border-border bg-card">
        <div className="mx-auto grid max-w-7xl gap-8 px-4 py-12 sm:px-6 md:grid-cols-3 lg:px-8">
          {[
            {
              icon: Utensils,
              title: "Structured recipes",
              body: "Every ingredient has a real quantity and unit, so scaling and shopping lists just work.",
            },
            {
              icon: Sparkles,
              title: "AI sous-chef",
              body: "Generate a recipe from your pantry, or get substitutions with taste and texture notes.",
            },
            {
              icon: ChefHat,
              title: "Honest ratings",
              body: "One review per cook per recipe — averages you can actually trust.",
            },
          ].map((item) => (
            <div key={item.title} className="flex gap-4">
              <span className="flex size-11 shrink-0 items-center justify-center rounded-xl bg-secondary text-accent-foreground">
                <item.icon className="size-5" />
              </span>
              <div>
                <h3 className="font-heading text-lg font-semibold">{item.title}</h3>
                <p className="mt-1.5 text-sm leading-relaxed text-muted-foreground">{item.body}</p>
              </div>
            </div>
          ))}
        </div>
      </section>

      {/* Trending */}
      <section className="mx-auto max-w-7xl px-4 py-16 sm:px-6 lg:px-8">
        <div className="flex flex-wrap items-end justify-between gap-4">
          <div>
            <p className="font-mono text-xs uppercase tracking-wider text-muted-foreground">
              Most cooked this week
            </p>
            <h2 className="mt-2 font-heading text-3xl font-semibold tracking-tight">Trending now</h2>
          </div>
          <Link
            to="/recipes?sort=popular"
            data-testid="trending-see-all-link"
            className="inline-flex items-center gap-1.5 text-sm font-medium text-primary transition-colors duration-150 hover:text-accent-foreground"
          >
            See all popular
            <ArrowRight className="size-4" />
          </Link>
        </div>
        <div className="mt-8">
          <RecipeGrid
            recipes={trending.data?.items}
            isLoading={trending.isLoading}
            isError={trending.isError}
            emptyTitle="No trending recipes yet"
            emptyHint="Be the first to publish a dish and start the conversation."
          />
        </div>
      </section>

      {/* Categories */}
      <section className="border-y border-border bg-secondary/40">
        <div className="mx-auto max-w-7xl px-4 py-16 sm:px-6 lg:px-8">
          <h2 className="font-heading text-3xl font-semibold tracking-tight">Browse by course</h2>
          <p className="mt-2 max-w-xl text-muted-foreground">
            From a ten-minute breakfast to a weekend biryani project.
          </p>
          <div className="mt-8 grid grid-cols-2 gap-4 sm:grid-cols-3 lg:grid-cols-4">
            {(categories.data ?? []).slice(0, 8).map((category) => (
              <Link
                key={category.id}
                to={`/recipes?category=${encodeURIComponent(category.name)}`}
                data-testid="category-tile"
                className="group relative overflow-hidden rounded-2xl border border-border"
              >
                <img
                  src={category.imageUrl ?? HERO_IMAGE}
                  alt={category.name}
                  loading="lazy"
                  className="aspect-[4/3] size-full object-cover transition-transform duration-500 group-hover:scale-105"
                />
                <div aria-hidden className="absolute inset-0 bg-gradient-to-t from-stone-900/85 to-transparent" />
                <div className="absolute inset-x-0 bottom-0 p-4">
                  <p className="font-heading text-base font-semibold text-stone-50">{category.name}</p>
                  <p className="text-xs text-stone-300">{category.recipesCount} recipes</p>
                </div>
              </Link>
            ))}
            {categories.isLoading &&
              Array.from({ length: 8 }).map((_, index) => (
                <div key={index} className="aspect-[4/3] animate-pulse rounded-2xl bg-muted" />
              ))}
          </div>
        </div>
      </section>

      {/* Freshly added */}
      <section className="mx-auto max-w-7xl px-4 py-16 sm:px-6 lg:px-8">
        <h2 className="font-heading text-3xl font-semibold tracking-tight">Freshly added</h2>
        <p className="mt-2 text-muted-foreground">The newest plates from the community.</p>
        <div className="mt-8">
          <RecipeGrid
            recipes={latest.data?.items}
            isLoading={latest.isLoading}
            isError={latest.isError}
          />
        </div>
      </section>
    </PageShell>
  );
}
