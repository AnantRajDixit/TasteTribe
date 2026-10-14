import { useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { useMutation, useQuery } from "@tanstack/react-query";
import {
  Clock, Users, Heart, Bookmark, Share2, Minus, Plus, ShoppingBasket, Pencil,
  Trash2, Flame, ChefHat, Replace, CalendarDays,
} from "lucide-react";
import { toast } from "sonner";
import { Button, buttonVariants } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Checkbox } from "@/components/ui/checkbox";
import { Textarea } from "@/components/ui/textarea";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog";
import { apiDelete, apiGet, apiPost, apiPut } from "@/lib/api";
import { queryClient } from "@/lib/queryClient";
import { useMe } from "@/lib/session";
import { coverFor, errorMessage, formatMinutes, formatQuantity, initials, timeAgo } from "@/lib/format";
import PageShell from "@/components/layout/PageShell";
import CookMode from "@/components/recipes/CookMode";
import ReportDialog from "@/components/recipes/ReportDialog";
import { StarRating, StarPicker } from "@/components/recipes/StarRating";
import type {
  Comment, Recipe, Review, ScaleResponse, SubstituteResponse, ToggleResponse,
} from "@/lib/types";

export default function RecipeDetail() {
  const { id = "" } = useParams();
  const navigate = useNavigate();
  const { data: me } = useMe();
  const [servings, setServings] = useState<number | null>(null);
  const [checked, setChecked] = useState<Record<string, boolean>>({});
  const [rating, setRating] = useState(0);
  const [reviewText, setReviewText] = useState("");
  const [commentText, setCommentText] = useState("");
  const [subTarget, setSubTarget] = useState("");
  const [cookMode, setCookMode] = useState(false);

  const recipeQuery = useQuery({
    queryKey: ["recipe", id],
    queryFn: () => apiGet<Recipe>(`/recipes/${id}`),
    retry: false,
  });
  const recipe = recipeQuery.data;

  const effectiveServings = servings ?? recipe?.servings ?? 2;

  const scaleQuery = useQuery({
    queryKey: ["recipe", id, "scale", effectiveServings],
    queryFn: () => apiGet<ScaleResponse>(`/recipes/${id}/scale?servings=${effectiveServings}`),
    enabled: Boolean(recipe),
    retry: false,
  });

  const reviewsQuery = useQuery({
    queryKey: ["recipe", id, "reviews"],
    queryFn: () => apiGet<Review[]>(`/recipes/${id}/reviews`),
    retry: false,
  });

  const commentsQuery = useQuery({
    queryKey: ["recipe", id, "comments"],
    queryFn: () => apiGet<Comment[]>(`/recipes/${id}/comments`),
    retry: false,
  });

  const requireLogin = (): boolean => {
    if (!me) {
      toast.info("Log in to do that.");
      navigate("/login");
      return false;
    }
    return true;
  };

  const invalidate = () => void queryClient.invalidateQueries();

  const like = useMutation({
    mutationFn: () => apiPost<ToggleResponse>(`/recipes/${id}/like`),
    onSuccess: invalidate,
    onError: (e) => toast.error(errorMessage(e)),
  });

  const favorite = useMutation({
    mutationFn: () => apiPost<ToggleResponse>(`/recipes/${id}/favorite`),
    onSuccess: (data) => {
      toast.success(data.active ? "Saved to your collection" : "Removed from your collection");
      invalidate();
    },
    onError: (e) => toast.error(errorMessage(e)),
  });

  const addToList = useMutation({
    mutationFn: () => apiPost("/shopping-list/add-recipe", { recipeId: id, servings: effectiveServings }),
    onSuccess: () => {
      toast.success(`Ingredients for ${effectiveServings} servings added to your shopping list`);
      invalidate();
    },
    onError: (e) => toast.error(errorMessage(e)),
  });

  const submitReview = useMutation({
    mutationFn: () => apiPut<Review>(`/recipes/${id}/reviews`, { rating, comment: reviewText || null }),
    onSuccess: () => {
      toast.success("Thanks for rating this recipe!");
      setReviewText("");
      invalidate();
    },
    onError: (e) => toast.error(errorMessage(e)),
  });

  const submitComment = useMutation({
    mutationFn: () => apiPost<Comment>(`/recipes/${id}/comments`, { text: commentText }),
    onSuccess: () => {
      setCommentText("");
      invalidate();
    },
    onError: (e) => toast.error(errorMessage(e)),
  });

  const removeRecipe = useMutation({
    mutationFn: () => apiDelete(`/recipes/${id}`),
    onSuccess: () => {
      toast.success("Recipe deleted");
      void queryClient.invalidateQueries();
      navigate("/dashboard");
    },
    onError: (e) => toast.error(errorMessage(e)),
  });

  const substitute = useMutation({
    mutationFn: (ingredient: string) =>
      apiPost<SubstituteResponse>("/ai/substitute", { ingredient, recipeId: id }),
    onError: (e) => toast.error(errorMessage(e, "Could not fetch substitutions.")),
  });

  const addToPlan = useMutation({
    mutationFn: () =>
      apiPost("/meal-plan/entries", {
        recipeId: id,
        planDate: new Date().toISOString().slice(0, 10),
        slot: "dinner",
        servings: effectiveServings,
      }),
    onSuccess: () => {
      toast.success("Added to today's dinner in your meal planner");
      void queryClient.invalidateQueries({ queryKey: ["meal-plan"] });
    },
    onError: (e) => toast.error(errorMessage(e)),
  });

  const share = async () => {
    try {
      await navigator.clipboard.writeText(window.location.href);
      toast.success("Recipe link copied to your clipboard");
    } catch {
      toast.error("Could not copy the link.");
    }
  };

  const canEdit = Boolean(me && recipe && (me.id === recipe.authorId || me.role === "admin"));
  const myReview = reviewsQuery.data?.find((item) => item.userId === me?.id);

  if (recipeQuery.isError) {
    return (
      <PageShell>
        <div className="mx-auto max-w-2xl px-4 py-24 text-center">
          <h1 className="font-heading text-3xl font-bold">Recipe not found</h1>
          <p className="mt-3 text-muted-foreground">
            This recipe may have been removed, or the link is incorrect.
          </p>
          <Link to="/recipes" className={buttonVariants({ className: "mt-8" })}>
            Browse all recipes
          </Link>
        </div>
      </PageShell>
    );
  }

  return (
    <PageShell>
      {/* Hero */}
      <section className="relative">
        {recipe ? (
          <img src={coverFor(recipe)} alt={recipe.title} className="absolute inset-0 size-full object-cover" />
        ) : (
          <div className="absolute inset-0 bg-muted" />
        )}
        <div
          aria-hidden
          className="absolute inset-0"
          style={{
            background:
              "linear-gradient(180deg, rgba(28,25,23,0.75) 0%, rgba(28,25,23,0.55) 45%, rgba(28,25,23,0.94) 100%)",
          }}
        />
        <div className="relative mx-auto max-w-7xl px-4 py-20 sm:px-6 lg:px-8">
          {recipe ? (
            <div className="max-w-3xl">
              <div className="flex flex-wrap gap-2">
                <Badge className="bg-primary text-primary-foreground">{recipe.cuisine}</Badge>
                <Badge className="bg-stone-100 text-stone-900">{recipe.category}</Badge>
                <Badge className="bg-stone-100 text-stone-900">{recipe.difficulty}</Badge>
                <Badge className="bg-stone-100 text-stone-900">{recipe.dietary}</Badge>
              </div>
              <h1
                data-testid="recipe-detail-title"
                className="mt-5 font-heading text-4xl font-bold leading-tight tracking-tight text-stone-50 sm:text-5xl"
              >
                {recipe.title}
              </h1>
              <p className="mt-4 max-w-2xl text-lg leading-relaxed text-stone-200">
                {recipe.description}
              </p>

              <div className="mt-6 flex flex-wrap items-center gap-x-6 gap-y-3 text-sm text-stone-200">
                <span className="inline-flex items-center gap-2">
                  <Clock className="size-4" /> {formatMinutes(recipe.totalTime)} total
                </span>
                <span className="inline-flex items-center gap-2">
                  <Users className="size-4" /> Serves {recipe.servings}
                </span>
                <span className="inline-flex items-center gap-2">
                  <Flame className="size-4" /> {recipe.views} views
                </span>
                <StarRating value={recipe.avgRating} count={recipe.ratingsCount} testid="recipe-detail-rating" />
              </div>

              <div className="mt-8 flex flex-wrap items-center gap-3">
                <Button
                  onClick={() => setCookMode(true)}
                  data-testid="cook-mode-open-btn"
                >
                  <ChefHat className="mr-2 size-4" />
                  Start cooking
                </Button>
                <Button
                  variant="outline"
                  onClick={() => requireLogin() && favorite.mutate()}
                  data-testid="recipe-save-btn"
                  className="border-stone-300 bg-transparent text-stone-50 hover:bg-stone-50 hover:text-stone-900"
                >
                  <Bookmark className={`mr-2 size-4 ${recipe.favoritedByMe ? "fill-current" : ""}`} />
                  {recipe.favoritedByMe ? "Saved" : "Save recipe"}
                </Button>
                <Button
                  variant="outline"
                  onClick={() => requireLogin() && like.mutate()}
                  data-testid="recipe-like-btn"
                  className="border-stone-300 bg-transparent text-stone-50 hover:bg-stone-50 hover:text-stone-900"
                >
                  <Heart className={`mr-2 size-4 ${recipe.likedByMe ? "fill-current" : ""}`} />
                  {recipe.likesCount}
                </Button>
                <Button
                  variant="outline"
                  onClick={share}
                  data-testid="recipe-share-btn"
                  className="border-stone-300 bg-transparent text-stone-50 hover:bg-stone-50 hover:text-stone-900"
                >
                  <Share2 className="mr-2 size-4" /> Share
                </Button>
                {canEdit && (
                  <>
                    <Link
                      to={`/recipes/${id}/edit`}
                      data-testid="recipe-edit-btn"
                      className={
                        buttonVariants({ variant: "outline" }) +
                        " border-stone-300 bg-transparent text-stone-50 hover:bg-stone-50 hover:text-stone-900"
                      }
                    >
                      <Pencil className="mr-2 size-4" /> Edit
                    </Link>
                    <Button
                      variant="destructive"
                      onClick={() => removeRecipe.mutate()}
                      data-testid="recipe-delete-btn"
                    >
                      <Trash2 className="mr-2 size-4" /> Delete
                    </Button>
                  </>
                )}
              </div>

              <Link
                to={`/chefs/${recipe.authorUsername}`}
                data-testid="recipe-author-link"
                className="mt-8 inline-flex items-center gap-3 text-stone-200 transition-colors duration-150 hover:text-primary"
              >
                <span className="flex size-10 items-center justify-center rounded-full bg-stone-700 text-sm font-semibold text-stone-50">
                  {initials(recipe.authorName)}
                </span>
                <span>
                  <span className="block text-sm font-medium text-stone-50">{recipe.authorName}</span>
                  <span className="block text-xs text-stone-300">@{recipe.authorUsername}</span>
                </span>
              </Link>
            </div>          ) : (
            <div className="h-64 max-w-3xl animate-pulse rounded-2xl bg-stone-700/40" />
          )}
        </div>
      </section>

      <div className="mx-auto max-w-7xl px-4 py-14 sm:px-6 lg:px-8">
        <div className="grid items-start gap-10 lg:grid-cols-12">
          {/* Ingredients + scaler */}
          <aside className="space-y-6 lg:col-span-5 lg:sticky lg:top-24">
            <div className="rounded-2xl border border-border bg-card p-6">
              <h2 className="font-heading text-2xl font-semibold tracking-tight">Ingredients</h2>

              <div className="mt-5 flex items-center justify-between rounded-xl bg-secondary p-3">
                <span className="text-sm font-medium">Servings</span>
                <div className="flex items-center gap-2">
                  <Button
                    size="icon-sm"
                    variant="outline"
                    aria-label="Decrease servings"
                    data-testid="serving-scaler-decrement"
                    disabled={effectiveServings <= 1}
                    onClick={() => setServings(Math.max(1, effectiveServings - 1))}
                  >
                    <Minus className="size-4" />
                  </Button>
                  <span
                    className="w-10 text-center font-mono text-lg font-semibold"
                    data-testid="serving-scaler-display"
                    aria-live="polite"
                  >
                    {effectiveServings}
                  </span>
                  <Button
                    size="icon-sm"
                    variant="outline"
                    aria-label="Increase servings"
                    data-testid="serving-scaler-increment"
                    disabled={effectiveServings >= 50}
                    onClick={() => setServings(Math.min(50, effectiveServings + 1))}
                  >
                    <Plus className="size-4" />
                  </Button>
                </div>
              </div>

              <ul className="mt-5 space-y-1" data-testid="ingredient-list">
                {(scaleQuery.data?.ingredients ?? []).map((item, index) => (
                  <li
                    key={`${item.name}-${index}`}
                    className="flex items-start gap-3 rounded-lg px-2 py-2 transition-colors duration-150 hover:bg-muted"
                  >
                    <Checkbox
                      checked={Boolean(checked[item.name])}
                      onCheckedChange={(value) =>
                        setChecked((prev) => ({ ...prev, [item.name]: Boolean(value) }))
                      }
                      aria-label={`Mark ${item.name}`}
                      data-testid="ingredient-item-checkbox"
                      className="mt-0.5"
                    />
                    <span
                      className={`flex-1 text-sm leading-relaxed ${
                        checked[item.name] ? "text-muted-foreground line-through" : ""
                      }`}
                      data-testid="ingredient-quantity-label"
                    >
                      <strong className="font-semibold">
                        {formatQuantity(item.quantity)} {item.unit}
                      </strong>{" "}
                      {item.name}
                      {item.optional && (
                        <span className="ml-1.5 text-xs text-muted-foreground">(optional)</span>
                      )}
                    </span>
                    <Dialog>
                      <DialogTrigger
                        aria-label={`Find a substitute for ${item.name}`}
                        data-testid="ai-substitute-trigger-btn"
                        onClick={() => {
                          if (!requireLogin()) return;
                          setSubTarget(item.name);
                          substitute.mutate(item.name);
                        }}
                        className="rounded-md p-1 text-muted-foreground transition-colors duration-150 hover:bg-secondary hover:text-primary"
                      >
                        <Replace className="size-4" />
                      </DialogTrigger>
                      <DialogContent className="max-w-lg">
                        <DialogHeader>
                          <DialogTitle className="font-heading">
                            Substitutes for {subTarget || item.name}
                          </DialogTitle>
                        </DialogHeader>
                        <div className="max-h-[60vh] space-y-4 overflow-y-auto" data-testid="substitute-results">
                          {substitute.isPending && (
                            <p className="text-sm text-muted-foreground">Asking the sous-chef…</p>
                          )}
                          {substitute.data && (
                            <>
                              <p className="text-sm text-muted-foreground">{substitute.data.summary}</p>
                              {substitute.data.substitutes.map((option) => (
                                <div key={option.name} className="rounded-xl border border-border p-4">
                                  <p className="font-heading text-base font-semibold">
                                    {option.name}
                                    {option.ratio && (
                                      <span className="ml-2 font-mono text-xs font-normal text-muted-foreground">
                                        {option.ratio}
                                      </span>
                                    )}
                                  </p>
                                  <dl className="mt-2 space-y-1 text-sm">
                                    {option.quantity && <p><strong>Use:</strong> {option.quantity}</p>}
                                    {option.taste && <p><strong>Taste:</strong> {option.taste}</p>}
                                    {option.texture && <p><strong>Texture:</strong> {option.texture}</p>}
                                    {option.temperature && (
                                      <p><strong>Temperature:</strong> {option.temperature}</p>
                                    )}
                                  </dl>
                                </div>
                              ))}
                            </>
                          )}
                          {substitute.isError && (
                            <p className="text-sm text-destructive">
                              Could not load substitutions. Please try again.
                            </p>
                          )}
                        </div>
                      </DialogContent>
                    </Dialog>
                  </li>
                ))}
                {scaleQuery.isLoading &&
                  Array.from({ length: 6 }).map((_, index) => (
                    <li key={index} className="h-8 animate-pulse rounded bg-muted" />
                  ))}
              </ul>

              <Button
                className="mt-6 w-full"
                onClick={() => requireLogin() && addToList.mutate()}
                disabled={addToList.isPending}
                data-testid="add-all-to-shopping-list-btn"
              >
                <ShoppingBasket className="mr-2 size-4" />
                Add all to shopping list
              </Button>
              <Button
                variant="outline"
                className="mt-2 w-full"
                onClick={() => requireLogin() && addToPlan.mutate()}
                disabled={addToPlan.isPending}
                data-testid="add-to-meal-plan-btn"
              >
                <CalendarDays className="mr-2 size-4" />
                Add to meal planner
              </Button>
            </div>

            {recipe?.nutrition && (
              <div className="rounded-2xl border border-border bg-card p-6" data-testid="nutrition-panel">
                <h3 className="font-heading text-lg font-semibold">Nutrition per serving</h3>
                <dl className="mt-4 grid grid-cols-2 gap-3 text-sm">
                  {[
                    ["Calories", recipe.nutrition.calories, "kcal"],
                    ["Protein", recipe.nutrition.protein, "g"],
                    ["Carbs", recipe.nutrition.carbs, "g"],
                    ["Fat", recipe.nutrition.fat, "g"],
                  ].map(([label, value, unit]) =>
                    value ? (
                      <div key={String(label)} className="rounded-lg bg-secondary p-3">
                        <dt className="font-mono text-xs uppercase tracking-wider text-muted-foreground">
                          {label}
                        </dt>
                        <dd className="mt-1 font-heading text-lg font-semibold">
                          {Math.round(Number(value))} {unit}
                        </dd>
                      </div>
                    ) : null,
                  )}
                </dl>
              </div>
            )}
          </aside>

          {/* Method + community */}
          <div className="space-y-10 lg:col-span-7">
            <section>
              <h2 className="font-heading text-2xl font-semibold tracking-tight">Method</h2>
              <ol className="mt-6 space-y-6" data-testid="instruction-list">
                {(recipe?.instructions ?? []).map((step, index) => (
                  <li key={index} className="flex gap-4">
                    <span className="flex size-9 shrink-0 items-center justify-center rounded-full bg-primary font-mono text-sm font-semibold text-primary-foreground">
                      {index + 1}
                    </span>
                    <p className="pt-1.5 leading-relaxed">{step}</p>
                  </li>
                ))}
              </ol>
              {recipe && recipe.tags.length > 0 && (
                <div className="mt-8 flex flex-wrap gap-2">
                  {recipe.tags.map((tag) => (
                    <Link key={tag} to={`/recipes?tag=${encodeURIComponent(tag)}`}>
                      <Badge variant="secondary">#{tag}</Badge>
                    </Link>
                  ))}
                </div>
              )}
              {recipe && (
                <div className="mt-8 flex items-center justify-between border-t border-border pt-6">
                  <p className="text-sm text-muted-foreground">
                    Something wrong with this recipe?
                  </p>
                  <ReportDialog targetType="recipe" targetId={recipe.id} />
                </div>
              )}
            </section>

            {/* Reviews */}
            <section className="rounded-2xl border border-border bg-card p-6">
              <h2 className="font-heading text-2xl font-semibold tracking-tight">
                Ratings &amp; reviews
              </h2>

              <div className="mt-5 rounded-xl bg-secondary p-5">
                <p className="text-sm font-medium">
                  {myReview ? "Update your review" : "Rate this recipe"}
                </p>
                <div className="mt-3">
                  <StarPicker value={rating || myReview?.rating || 0} onChange={setRating} />
                </div>
                <Textarea
                  value={reviewText}
                  onChange={(event) => setReviewText(event.target.value)}
                  placeholder={myReview?.comment ?? "How did it turn out? Any tweaks you'd make?"}
                  className="mt-4"
                  rows={3}
                  data-testid="review-comment-textarea"
                />
                <Button
                  className="mt-3"
                  disabled={submitReview.isPending || (rating || myReview?.rating || 0) === 0}
                  onClick={() => requireLogin() && submitReview.mutate()}
                  data-testid="review-submit-btn"
                >
                  {myReview ? "Update review" : "Post review"}
                </Button>
              </div>

              <ul className="mt-6 space-y-5" data-testid="review-list">
                {(reviewsQuery.data ?? []).map((review) => (
                  <li key={review.id} className="border-t border-border pt-5" data-testid="review-item">
                    <div className="flex items-center gap-3">
                      <span className="flex size-9 items-center justify-center overflow-hidden rounded-full bg-muted text-xs font-semibold">
                        {review.avatarUrl ? (
                          <img src={review.avatarUrl} alt={review.name} className="size-full object-cover" />
                        ) : (
                          initials(review.name || review.username)
                        )}
                      </span>
                      <div className="flex-1">
                        <p className="text-sm font-semibold">{review.name || review.username}</p>
                        <p className="text-xs text-muted-foreground">{timeAgo(review.createdAt)}</p>
                      </div>
                      <StarRating value={review.rating} testid="review-item-rating" />
                    </div>
                    {review.comment && (
                      <p className="mt-3 leading-relaxed text-muted-foreground">{review.comment}</p>
                    )}
                  </li>
                ))}
                {reviewsQuery.data?.length === 0 && (
                  <li className="pt-4 text-sm text-muted-foreground">
                    No reviews yet — be the first to cook it and report back.
                  </li>
                )}
              </ul>
            </section>

            {/* Comments */}
            <section className="rounded-2xl border border-border bg-card p-6">
              <h2 className="font-heading text-2xl font-semibold tracking-tight">Discussion</h2>
              <form
                onSubmit={(event) => {
                  event.preventDefault();
                  if (requireLogin()) submitComment.mutate();
                }}
                className="mt-5 flex flex-col gap-3 sm:flex-row"
              >
                <Textarea
                  value={commentText}
                  onChange={(event) => setCommentText(event.target.value)}
                  placeholder="Ask a question or share a tip…"
                  rows={2}
                  data-testid="comment-input"
                />
                <Button
                  type="submit"
                  disabled={!commentText.trim() || submitComment.isPending}
                  data-testid="comment-submit-btn"
                >
                  Post
                </Button>
              </form>

              <ul className="mt-6 space-y-4" data-testid="comment-list">
                {(commentsQuery.data ?? []).map((comment) => (
                  <li key={comment.id} className="flex gap-3" data-testid="comment-item">
                    <span className="flex size-9 shrink-0 items-center justify-center overflow-hidden rounded-full bg-muted text-xs font-semibold">
                      {comment.avatarUrl ? (
                        <img src={comment.avatarUrl} alt={comment.name} className="size-full object-cover" />
                      ) : (
                        initials(comment.name || comment.username)
                      )}
                    </span>
                    <div className="flex-1 rounded-xl bg-secondary px-4 py-3">
                      <p className="text-sm font-semibold">
                        {comment.name || comment.username}
                        <span className="ml-2 text-xs font-normal text-muted-foreground">
                          {timeAgo(comment.createdAt)}
                        </span>
                      </p>
                      <p className="mt-1 text-sm leading-relaxed">{comment.text}</p>
                      <div className="mt-2 flex justify-end">
                        <ReportDialog
                          targetType="comment"
                          targetId={comment.id}
                          label="Report"
                          compact
                        />
                      </div>
                    </div>
                  </li>
                ))}
                {commentsQuery.data?.length === 0 && (
                  <li className="text-sm text-muted-foreground">No comments yet.</li>
                )}
              </ul>
            </section>
          </div>
        </div>
      </div>

      {/* Full-screen cooking view */}
      {cookMode && recipe && (
        <CookMode
          recipe={recipe}
          ingredients={scaleQuery.data?.ingredients ?? []}
          servings={effectiveServings}
          onClose={() => setCookMode(false)}
        />
      )}
    </PageShell>
  );
}
