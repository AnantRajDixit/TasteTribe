import { useQuery } from "@tanstack/react-query";
import { Link } from "react-router-dom";
import { apiGet } from "@/lib/api";
import PageShell from "@/components/layout/PageShell";
import type { Category } from "@/lib/types";

export default function Categories() {
  const query = useQuery({
    queryKey: ["categories"],
    queryFn: () => apiGet<Category[]>("/categories"),
    retry: false,
  });

  return (
    <PageShell>
      <div className="mx-auto max-w-7xl px-4 py-10 sm:px-6 lg:px-8">
        <header>
          <p className="font-mono text-xs uppercase tracking-wider text-muted-foreground">
            Every course covered
          </p>
          <h1 className="mt-2 font-heading text-4xl font-bold tracking-tight">Categories</h1>
          <p className="mt-2 max-w-xl text-muted-foreground">
            Pick a course and dive into what the community is cooking.
          </p>
        </header>

        <div className="mt-10 grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
          {query.isLoading &&
            Array.from({ length: 6 }).map((_, index) => (
              <div key={index} className="h-64 animate-pulse rounded-2xl bg-muted" />
            ))}

          {(query.data ?? []).map((category) => (
            <Link
              key={category.id}
              to={`/recipes?category=${encodeURIComponent(category.name)}`}
              data-testid="category-card"
              className="group relative overflow-hidden rounded-2xl border border-border transition-shadow duration-300 hover:shadow-[0_12px_24px_rgba(28,25,23,0.08)]"
            >
              <img
                src={
                  category.imageUrl ??
                  "https://images.unsplash.com/photo-1516100882582-96c3a05fe590?crop=entropy&cs=srgb&fm=jpg&q=85&w=900"
                }
                alt={category.name}
                loading="lazy"
                className="aspect-[3/2] size-full object-cover transition-transform duration-500 group-hover:scale-105"
              />
              <div aria-hidden className="absolute inset-0 bg-gradient-to-t from-stone-900/90 via-stone-900/30 to-transparent" />
              <div className="absolute inset-x-0 bottom-0 p-5">
                <h2 className="font-heading text-xl font-semibold text-stone-50">{category.name}</h2>
                <p className="clamp-2 mt-1 text-sm text-stone-300">{category.description}</p>
                <p className="mt-2 font-mono text-xs uppercase tracking-wider text-orange-300">
                  {category.recipesCount} recipes
                </p>
              </div>
            </Link>
          ))}

          {query.isError && (
            <div className="col-span-full rounded-2xl border border-dashed border-border p-12 text-center">
              <h3 className="font-heading text-xl font-semibold">Categories are unavailable</h3>
              <p className="mt-2 text-sm text-muted-foreground">Please refresh in a moment.</p>
            </div>
          )}
        </div>
      </div>
    </PageShell>
  );
}
