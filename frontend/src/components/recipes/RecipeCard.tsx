import { Link, useNavigate } from "react-router-dom";
import { useMutation } from "@tanstack/react-query";
import { Bookmark, Clock, Heart, Users } from "lucide-react";
import { toast } from "sonner";
import { Badge } from "@/components/ui/badge";
import { apiPost } from "@/lib/api";
import { queryClient } from "@/lib/queryClient";
import { useMe } from "@/lib/session";
import { coverFor, errorMessage, formatMinutes } from "@/lib/format";
import { StarRating } from "@/components/recipes/StarRating";
import type { Recipe, ToggleResponse } from "@/lib/types";

const DIFFICULTY_STYLES: Record<string, string> = {
  Easy: "bg-emerald-100 text-emerald-800",
  Medium: "bg-amber-100 text-amber-900",
  Hard: "bg-rose-100 text-rose-900",
};

export default function RecipeCard({ recipe }: { recipe: Recipe }) {
  const { data: me } = useMe();
  const navigate = useNavigate();

  const favorite = useMutation({
    mutationFn: () => apiPost<ToggleResponse>(`/recipes/${recipe.id}/favorite`),
    onSuccess: (data) => {
      toast.success(data.active ? "Saved to your collection" : "Removed from your collection");
      void queryClient.invalidateQueries();
    },
    onError: (error) => toast.error(errorMessage(error)),
  });

  const like = useMutation({
    mutationFn: () => apiPost<ToggleResponse>(`/recipes/${recipe.id}/like`),
    onSuccess: () => void queryClient.invalidateQueries(),
    onError: (error) => toast.error(errorMessage(error)),
  });

  const guard = (action: () => void) => {
    if (!me) {
      toast.info("Log in to save and like recipes.");
      navigate("/login");
      return;
    }
    action();
  };

  return (
    <article
      data-testid="recipe-card-item"
      className="group relative flex flex-col overflow-hidden rounded-2xl border border-border bg-card transition-[transform,box-shadow] duration-300 hover:-translate-y-[3px] hover:shadow-[0_12px_24px_rgba(28,25,23,0.08)]"
    >
      <Link to={`/recipes/${recipe.id}`} className="relative block aspect-[4/3] overflow-hidden">
        <img
          src={coverFor(recipe)}
          alt={recipe.title}
          loading="lazy"
          className="size-full object-cover transition-transform duration-500 group-hover:scale-105"
        />
        <span className="absolute left-3 top-3 flex gap-1.5">
          <Badge className={DIFFICULTY_STYLES[recipe.difficulty] ?? "bg-stone-100 text-stone-800"}>
            {recipe.difficulty}
          </Badge>
          {recipe.status === "draft" && (
            <Badge className="bg-stone-900 text-stone-50" data-testid="recipe-card-draft-badge">
              Draft
            </Badge>
          )}
        </span>
      </Link>

      <button
        type="button"
        aria-label={recipe.favoritedByMe ? "Remove from saved" : "Save recipe"}
        data-testid="recipe-card-bookmark-btn"
        onClick={() => guard(() => favorite.mutate())}
        className="absolute right-3 top-3 flex size-9 items-center justify-center rounded-full bg-background/90 backdrop-blur transition-[background-color,transform] duration-150 hover:scale-105 hover:bg-background"
      >
        <Bookmark
          className={`size-4 ${recipe.favoritedByMe ? "fill-primary text-primary" : "text-foreground"}`}
        />
      </button>

      <div className="flex flex-1 flex-col p-5">
        <div className="flex items-center gap-2 font-mono text-xs uppercase tracking-wider text-muted-foreground">
          <span>{recipe.cuisine}</span>
          <span aria-hidden>·</span>
          <span>{recipe.category}</span>
        </div>

        <h3 className="mt-2 font-heading text-lg font-semibold leading-snug tracking-tight">
          <Link
            to={`/recipes/${recipe.id}`}
            data-testid="recipe-card-title"
            className="transition-colors duration-150 hover:text-primary"
          >
            {recipe.title}
          </Link>
        </h3>

        <p className="clamp-2 mt-2 text-sm leading-relaxed text-muted-foreground">
          {recipe.description || "A recipe from the TasteTribe community."}
        </p>

        <div className="mt-4 flex flex-wrap items-center gap-x-4 gap-y-2 text-sm text-muted-foreground">
          <span className="inline-flex items-center gap-1.5">
            <Clock className="size-4" />
            {formatMinutes(recipe.totalTime)}
          </span>
          <span className="inline-flex items-center gap-1.5">
            <Users className="size-4" />
            Serves {recipe.servings}
          </span>
        </div>

        <div className="mt-4 flex items-center justify-between border-t border-border pt-4">
          <StarRating value={recipe.avgRating} count={recipe.ratingsCount} />
          <button
            type="button"
            aria-label={recipe.likedByMe ? "Unlike recipe" : "Like recipe"}
            data-testid="recipe-card-like-btn"
            onClick={() => guard(() => like.mutate())}
            className="inline-flex items-center gap-1.5 text-sm text-muted-foreground transition-colors duration-150 hover:text-primary"
          >
            <Heart className={`size-4 ${recipe.likedByMe ? "fill-primary text-primary" : ""}`} />
            {recipe.likesCount}
          </button>
        </div>

        <Link
          to={`/chefs/${recipe.authorUsername}`}
          data-testid="recipe-card-author-link"
          className="mt-3 text-xs text-muted-foreground transition-colors duration-150 hover:text-primary"
        >
          by {recipe.authorName || recipe.authorUsername}
        </Link>
      </div>
    </article>
  );
}
