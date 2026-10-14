import { useState, useEffect } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useMutation, useQuery } from "@tanstack/react-query";
import {
  ChevronLeft, ChevronRight, ShoppingBasket, Trash2, Plus, CalendarDays,
} from "lucide-react";
import { toast } from "sonner";
import { Button, buttonVariants } from "@/components/ui/button";
import {
  Dialog, DialogContent, DialogHeader, DialogTitle,
} from "@/components/ui/dialog";
import { apiDelete, apiGet, apiPatch, apiPost } from "@/lib/api";
import { queryClient } from "@/lib/queryClient";
import { useMe } from "@/lib/session";
import { errorMessage, formatMinutes } from "@/lib/format";
import PageShell from "@/components/layout/PageShell";
import { MEAL_SLOTS } from "@/lib/types";
import type { MealPlan, MealPlanEntry, MealSlot, Recipe, RecipePage } from "@/lib/types";

/** Monday of the week containing `date`, as YYYY-MM-DD. */
function mondayOf(date: Date): string {
  const copy = new Date(date);
  const day = (copy.getDay() + 6) % 7; // Monday = 0
  copy.setDate(copy.getDate() - day);
  return copy.toISOString().slice(0, 10);
}

function addDays(iso: string, days: number): string {
  const date = new Date(`${iso}T00:00:00`);
  date.setDate(date.getDate() + days);
  return date.toISOString().slice(0, 10);
}

const DAY_LABELS = ["Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"];

export default function MealPlanner() {
  const { data: me, isLoading } = useMe();
  const navigate = useNavigate();
  const [weekOf, setWeekOf] = useState(() => mondayOf(new Date()));
  const [dragging, setDragging] = useState<string | null>(null);
  const [hoverCell, setHoverCell] = useState<string | null>(null);
  const [picker, setPicker] = useState<{ date: string; slot: MealSlot } | null>(null);

  useEffect(() => {
    if (!isLoading && !me) navigate("/login?next=/meal-planner");
  }, [me, isLoading, navigate]);

  const plan = useQuery({
    queryKey: ["meal-plan", weekOf],
    queryFn: () => apiGet<MealPlan>(`/meal-plan?weekOf=${weekOf}`),
    enabled: Boolean(me),
    retry: false,
  });

  // Saved recipes are the pool users drag from.
  const saved = useQuery({
    queryKey: ["meal-plan", "saved-pool"],
    queryFn: () => apiGet<RecipePage>("/recipes?limit=48"),
    enabled: Boolean(me),
    retry: false,
  });

  const refresh = () => {
    void queryClient.invalidateQueries({ queryKey: ["meal-plan"] });
    void queryClient.invalidateQueries({ queryKey: ["shopping-list"] });
  };

  const addMeal = useMutation({
    mutationFn: (vars: { recipeId: string; planDate: string; slot: MealSlot }) =>
      apiPost<MealPlan>("/meal-plan/entries", { ...vars, servings: null }),
    onSuccess: () => {
      toast.success("Added to your week");
      setPicker(null);
      refresh();
    },
    onError: (e) => toast.error(errorMessage(e)),
  });

  const moveMeal = useMutation({
    mutationFn: (vars: { id: string; planDate: string; slot: MealSlot }) =>
      apiPatch<MealPlan>(`/meal-plan/entries/${vars.id}`, {
        planDate: vars.planDate,
        slot: vars.slot,
      }),
    onSuccess: refresh,
    onError: (e) => toast.error(errorMessage(e)),
  });

  const removeMeal = useMutation({
    mutationFn: (id: string) => apiDelete<MealPlan>(`/meal-plan/entries/${id}`),
    onSuccess: refresh,
    onError: (e) => toast.error(errorMessage(e)),
  });

  const toShoppingList = useMutation({
    mutationFn: () => apiPost(`/meal-plan/shopping-list?weekOf=${weekOf}`),
    onSuccess: () => {
      toast.success("Your week's ingredients are on the shopping list");
      refresh();
    },
    onError: (e) => toast.error(errorMessage(e, "Could not build the shopping list.")),
  });

  const entries = plan.data?.entries ?? [];
  const savedRecipes = (saved.data?.items ?? []).filter((r) => r.favoritedByMe);
  const days = Array.from({ length: 7 }, (_, index) => addDays(weekOf, index));

  const cellEntries = (date: string, slot: MealSlot): MealPlanEntry[] =>
    entries.filter((entry) => entry.planDate === date && entry.slot === slot);

  const onDrop = (date: string, slot: MealSlot) => {
    setHoverCell(null);
    if (!dragging) return;
    if (dragging.startsWith("entry:")) {
      moveMeal.mutate({ id: dragging.slice(6), planDate: date, slot });
    } else if (dragging.startsWith("recipe:")) {
      addMeal.mutate({ recipeId: dragging.slice(7), planDate: date, slot });
    }
    setDragging(null);
  };

  const weekLabel = `${new Date(`${weekOf}T00:00:00`).toLocaleDateString(undefined, {
    month: "short",
    day: "numeric",
  })} – ${new Date(`${addDays(weekOf, 6)}T00:00:00`).toLocaleDateString(undefined, {
    month: "short",
    day: "numeric",
  })}`;

  return (
    <PageShell>
      <div className="mx-auto max-w-7xl px-4 py-10 sm:px-6 lg:px-8">
        <header className="flex flex-wrap items-end justify-between gap-4">
          <div>
            <p className="font-mono text-xs uppercase tracking-wider text-muted-foreground">
              Plan the week
            </p>
            <h1 className="mt-2 font-heading text-4xl font-bold tracking-tight">Meal planner</h1>
            <p className="mt-2 max-w-xl text-muted-foreground">
              Drag saved recipes onto any day, then turn the whole week into one shopping list.
            </p>
          </div>
          <Button
            onClick={() => toShoppingList.mutate()}
            disabled={entries.length === 0 || toShoppingList.isPending}
            data-testid="meal-plan-to-shopping-btn"
          >
            <ShoppingBasket className="mr-2 size-4" />
            Build shopping list
          </Button>
        </header>

        {/* Week switcher */}
        <div className="mt-8 flex items-center gap-3">
          <Button
            variant="outline"
            size="icon"
            aria-label="Previous week"
            data-testid="meal-plan-prev-week-btn"
            onClick={() => setWeekOf(addDays(weekOf, -7))}
          >
            <ChevronLeft className="size-4" />
          </Button>
          <span className="inline-flex items-center gap-2 font-heading text-lg font-semibold" data-testid="meal-plan-week-label">
            <CalendarDays className="size-4 text-muted-foreground" />
            {weekLabel}
          </span>
          <Button
            variant="outline"
            size="icon"
            aria-label="Next week"
            data-testid="meal-plan-next-week-btn"
            onClick={() => setWeekOf(addDays(weekOf, 7))}
          >
            <ChevronRight className="size-4" />
          </Button>
          <Button
            variant="ghost"
            size="sm"
            onClick={() => setWeekOf(mondayOf(new Date()))}
            data-testid="meal-plan-today-btn"
          >
            This week
          </Button>
        </div>

        <div className="mt-8 grid gap-8 xl:grid-cols-4">
          {/* Saved recipe pool */}
          <aside className="xl:col-span-1">
            <div className="rounded-2xl border border-border bg-card p-5">
              <h2 className="font-heading text-lg font-semibold">Your saved recipes</h2>
              <p className="mt-1 text-xs text-muted-foreground">
                Drag one onto the grid, or use the + on any slot.
              </p>
              <ul className="mt-4 max-h-[28rem] space-y-2 overflow-y-auto" data-testid="meal-plan-recipe-pool">
                {savedRecipes.map((recipe) => (
                  <li
                    key={recipe.id}
                    draggable
                    onDragStart={() => setDragging(`recipe:${recipe.id}`)}
                    onDragEnd={() => setDragging(null)}
                    data-testid="meal-plan-pool-item"
                    className="flex cursor-grab items-center gap-3 rounded-xl border border-border p-2 transition-colors duration-150 hover:border-primary active:cursor-grabbing"
                  >
                    <img
                      src={recipe.coverImage ?? ""}
                      alt=""
                      className="size-11 shrink-0 rounded-lg object-cover"
                    />
                    <span className="min-w-0 flex-1">
                      <span className="clamp-2 text-sm font-medium leading-snug">{recipe.title}</span>
                      <span className="text-xs text-muted-foreground">
                        {formatMinutes(recipe.totalTime)}
                      </span>
                    </span>
                  </li>
                ))}
                {savedRecipes.length === 0 && (
                  <li className="rounded-xl border border-dashed border-border p-5 text-center text-xs text-muted-foreground">
                    No saved recipes yet. Bookmark a few from{" "}
                    <Link to="/recipes" className="text-primary hover:underline">
                      Explore
                    </Link>
                    .
                  </li>
                )}
              </ul>
            </div>
          </aside>

          {/* Week grid */}
          <div className="overflow-x-auto xl:col-span-3">
            <div className="min-w-[46rem]">
              <div className="grid grid-cols-8 gap-2">
                <div />
                {days.map((date, index) => (
                  <div key={date} className="pb-2 text-center">
                    <p className="font-mono text-xs uppercase tracking-wider text-muted-foreground">
                      {DAY_LABELS[index]}
                    </p>
                    <p className="font-heading text-base font-semibold">
                      {new Date(`${date}T00:00:00`).getDate()}
                    </p>
                  </div>
                ))}
              </div>

              {MEAL_SLOTS.map((slot) => (
                <div key={slot.value} className="mt-2 grid grid-cols-8 gap-2">
                  <div className="flex items-center">
                    <p className="font-mono text-xs uppercase tracking-wider text-muted-foreground">
                      {slot.label}
                    </p>
                  </div>
                  {days.map((date) => {
                    const cellKey = `${date}|${slot.value}`;
                    const items = cellEntries(date, slot.value);
                    return (
                      <div
                        key={cellKey}
                        onDragOver={(event) => {
                          event.preventDefault();
                          setHoverCell(cellKey);
                        }}
                        onDragLeave={() => setHoverCell(null)}
                        onDrop={() => onDrop(date, slot.value)}
                        data-testid="meal-plan-cell"
                        className={`group min-h-24 rounded-xl border p-1.5 transition-colors duration-150 ${
                          hoverCell === cellKey
                            ? "border-primary bg-primary/5"
                            : "border-border bg-card"
                        }`}
                      >
                        {items.map((entry) => (
                          <div
                            key={entry.id}
                            draggable
                            onDragStart={() => setDragging(`entry:${entry.id}`)}
                            onDragEnd={() => setDragging(null)}
                            data-testid="meal-plan-entry"
                            className="group/card relative mb-1.5 cursor-grab overflow-hidden rounded-lg border border-border bg-background active:cursor-grabbing"
                          >
                            <Link to={`/recipes/${entry.recipeId}`}>
                              <img
                                src={entry.recipeImage ?? ""}
                                alt=""
                                className="h-12 w-full object-cover"
                              />
                              <p className="clamp-2 px-1.5 py-1 text-[11px] font-medium leading-tight">
                                {entry.recipeTitle}
                              </p>
                            </Link>
                            <button
                              type="button"
                              aria-label={`Remove ${entry.recipeTitle} from ${slot.label}`}
                              data-testid="meal-plan-entry-remove-btn"
                              onClick={() => removeMeal.mutate(entry.id)}
                              className="absolute right-1 top-1 flex size-5 items-center justify-center rounded-full bg-background/90 opacity-0 transition-opacity duration-150 group-hover/card:opacity-100"
                            >
                              <Trash2 className="size-3" />
                            </button>
                          </div>
                        ))}
                        <button
                          type="button"
                          aria-label={`Add a recipe to ${slot.label} on ${date}`}
                          data-testid="meal-plan-add-btn"
                          onClick={() => setPicker({ date, slot: slot.value })}
                          className="flex w-full items-center justify-center rounded-lg py-1.5 text-muted-foreground opacity-0 transition-opacity duration-150 hover:bg-muted group-hover:opacity-100"
                        >
                          <Plus className="size-4" />
                        </button>
                      </div>
                    );
                  })}
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>

      {/* Slot picker dialog (tap-friendly alternative to dragging) */}
      <Dialog open={Boolean(picker)} onOpenChange={(open) => !open && setPicker(null)}>
        <DialogContent className="max-w-md">
          <DialogHeader>
            <DialogTitle className="font-heading">Add a recipe</DialogTitle>
          </DialogHeader>
          <ul className="max-h-[60vh] space-y-2 overflow-y-auto" data-testid="meal-plan-picker-list">
            {savedRecipes.map((recipe: Recipe) => (
              <li key={recipe.id}>
                <button
                  type="button"
                  data-testid="meal-plan-picker-item"
                  onClick={() =>
                    picker &&
                    addMeal.mutate({
                      recipeId: recipe.id,
                      planDate: picker.date,
                      slot: picker.slot,
                    })
                  }
                  className="flex w-full items-center gap-3 rounded-xl border border-border p-2 text-left transition-colors duration-150 hover:border-primary"
                >
                  <img src={recipe.coverImage ?? ""} alt="" className="size-11 rounded-lg object-cover" />
                  <span className="min-w-0 flex-1">
                    <span className="block truncate text-sm font-medium">{recipe.title}</span>
                    <span className="text-xs text-muted-foreground">
                      {formatMinutes(recipe.totalTime)} · serves {recipe.servings}
                    </span>
                  </span>
                </button>
              </li>
            ))}
            {savedRecipes.length === 0 && (
              <li className="p-6 text-center text-sm text-muted-foreground">
                Save some recipes first — bookmark them from Explore.
              </li>
            )}
          </ul>
        </DialogContent>
      </Dialog>
    </PageShell>
  );
}
