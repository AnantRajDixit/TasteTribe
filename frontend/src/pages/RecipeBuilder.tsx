import { useState, useEffect } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { useMutation, useQuery } from "@tanstack/react-query";
import { Plus, Trash2, Sparkles } from "lucide-react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Checkbox } from "@/components/ui/checkbox";
import {
  Select, SelectContent, SelectItem, SelectTrigger, SelectValue,
} from "@/components/ui/select";
import { apiGet, apiPatch, apiPost } from "@/lib/api";
import { queryClient } from "@/lib/queryClient";
import { useMe } from "@/lib/session";
import { errorMessage } from "@/lib/format";
import PageShell from "@/components/layout/PageShell";
import {
  CUISINES, CATEGORY_NAMES, DIFFICULTIES, DIETS, UNITS,
} from "@/lib/types";
import type { Ingredient, Recipe, RecipeInput } from "@/lib/types";

const EMPTY: RecipeInput = {
  title: "",
  description: "",
  coverImage: "",
  cuisine: "Indian",
  category: "Dinner",
  difficulty: "Easy",
  dietary: "Vegetarian",
  prepTime: 15,
  cookTime: 25,
  servings: 2,
  ingredients: [{ name: "", quantity: 1, unit: "g", optional: false }],
  instructions: [""],
  nutrition: { calories: null, protein: null, carbs: null, fat: null },
  tags: [],
  status: "published",
};

export default function RecipeBuilder() {
  const { id } = useParams();
  const isEdit = Boolean(id);
  const navigate = useNavigate();
  const { data: me, isLoading: meLoading } = useMe();
  const [form, setForm] = useState<RecipeInput>(EMPTY);
  const [tagText, setTagText] = useState("");
  const [pantry, setPantry] = useState("");

  const existing = useQuery({
    queryKey: ["recipe", id],
    queryFn: () => apiGet<Recipe>(`/recipes/${id}`),
    enabled: isEdit,
    retry: false,
  });

  useEffect(() => {
    if (existing.data) {
      const r = existing.data;
      setForm({
        title: r.title,
        description: r.description,
        coverImage: r.coverImage ?? "",
        cuisine: r.cuisine,
        category: r.category,
        difficulty: r.difficulty,
        dietary: r.dietary,
        prepTime: r.prepTime,
        cookTime: r.cookTime,
        servings: r.servings,
        ingredients: r.ingredients.length ? r.ingredients : EMPTY.ingredients,
        instructions: r.instructions.length ? r.instructions : [""],
        nutrition: r.nutrition ?? EMPTY.nutrition,
        tags: r.tags,
        status: r.status,
      });
      setTagText(r.tags.join(", "));
    }
  }, [existing.data]);

  useEffect(() => {
    if (!meLoading && !me) {
      toast.info("Log in to publish a recipe.");
      navigate("/login");
    }
  }, [me, meLoading, navigate]);

  const payload = (status: "draft" | "published"): RecipeInput => ({
    ...form,
    status,
    coverImage: form.coverImage?.trim() || null,
    tags: tagText.split(",").map((t) => t.trim()).filter(Boolean),
    ingredients: form.ingredients.filter((i) => i.name.trim()),
    instructions: form.instructions.filter((s) => s.trim()),
  });

  const save = useMutation({
    mutationFn: (status: "draft" | "published") =>
      isEdit
        ? apiPatch<Recipe>(`/recipes/${id}`, payload(status))
        : apiPost<Recipe>("/recipes", payload(status)),
    onSuccess: (recipe) => {
      toast.success(isEdit ? "Recipe updated" : `Recipe ${recipe.status === "draft" ? "saved as draft" : "published"}`);
      void queryClient.invalidateQueries();
      navigate(`/recipes/${recipe.id}`);
    },
    onError: (e) => toast.error(errorMessage(e, "Could not save the recipe.")),
  });

  const generate = useMutation({
    mutationFn: () =>
      apiPost<RecipeInput>("/ai/generate-recipe", {
        ingredients: pantry.split(",").map((s) => s.trim()).filter(Boolean),
        servings: form.servings,
        difficulty: form.difficulty,
      }),
    onSuccess: (generated) => {
      setForm({ ...generated, coverImage: form.coverImage, status: "published" });
      setTagText((generated.tags ?? []).join(", "));
      toast.success("Draft generated — review and tweak it before publishing.");
    },
    onError: (e) => toast.error(errorMessage(e, "The AI could not generate a recipe.")),
  });

  const setIngredient = (index: number, patch: Partial<Ingredient>) => {
    setForm((prev) => ({
      ...prev,
      ingredients: prev.ingredients.map((item, i) => (i === index ? { ...item, ...patch } : item)),
    }));
  };

  const valid = form.title.trim().length >= 3
    && form.ingredients.some((i) => i.name.trim())
    && form.instructions.some((s) => s.trim());

  return (
    <PageShell>
      <div className="mx-auto max-w-4xl px-4 py-10 sm:px-6 lg:px-8">
        <header>
          <p className="font-mono text-xs uppercase tracking-wider text-muted-foreground">
            Recipe studio
          </p>
          <h1 className="mt-2 font-heading text-4xl font-bold tracking-tight">
            {isEdit ? "Edit recipe" : "Publish a recipe"}
          </h1>
        </header>

        {/* AI pantry generator */}
        {!isEdit && (
          <div className="mt-8 rounded-2xl border border-border bg-secondary/50 p-5">
            <div className="flex items-center gap-2">
              <Sparkles className="size-4 text-primary" />
              <h2 className="font-heading text-lg font-semibold">Start from your pantry</h2>
            </div>
            <p className="mt-1.5 text-sm text-muted-foreground">
              List what you have and the AI sous-chef will draft a full recipe you can edit.
            </p>
            <div className="mt-4 flex flex-col gap-2 sm:flex-row">
              <Input
                value={pantry}
                onChange={(event) => setPantry(event.target.value)}
                placeholder="chicken, rice, onion, garlic"
                data-testid="ai-pantry-input"
              />
              <Button
                onClick={() => generate.mutate()}
                disabled={!pantry.trim() || generate.isPending}
                data-testid="ai-pantry-generate-btn"
              >
                {generate.isPending ? "Generating…" : "Generate draft"}
              </Button>
            </div>
          </div>
        )}

        <form
          onSubmit={(event) => {
            event.preventDefault();
            save.mutate("published");
          }}
          className="mt-8 space-y-8"
        >
          {/* Basics */}
          <section className="rounded-2xl border border-border bg-card p-6">
            <h2 className="font-heading text-xl font-semibold">The basics</h2>
            <div className="mt-5 space-y-4">
              <div className="space-y-2">
                <Label htmlFor="title">Recipe name</Label>
                <Input
                  id="title"
                  value={form.title}
                  onChange={(e) => setForm({ ...form, title: e.target.value })}
                  placeholder="Slow-simmered butter chicken"
                  required
                  data-testid="recipe-title-input"
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="description">Description</Label>
                <Textarea
                  id="description"
                  value={form.description}
                  onChange={(e) => setForm({ ...form, description: e.target.value })}
                  placeholder="What makes this dish worth cooking?"
                  rows={3}
                  data-testid="recipe-description-input"
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="cover">Cover image URL</Label>
                <Input
                  id="cover"
                  value={form.coverImage ?? ""}
                  onChange={(e) => setForm({ ...form, coverImage: e.target.value })}
                  placeholder="https://…"
                  data-testid="recipe-cover-input"
                />
              </div>

              <div className="grid gap-4 sm:grid-cols-2">
                {([
                  ["Cuisine", "cuisine", CUISINES, "recipe-cuisine-select"],
                  ["Course", "category", CATEGORY_NAMES, "recipe-category-select"],
                  ["Difficulty", "difficulty", DIFFICULTIES, "recipe-difficulty-select"],
                  ["Dietary", "dietary", DIETS, "recipe-dietary-select"],
                ] as const).map(([label, key, options, testid]) => (
                  <div key={key} className="space-y-2">
                    <Label>{label}</Label>
                    <Select
                      value={String(form[key])}
                      onValueChange={(value: string) => setForm({ ...form, [key]: value })}
                    >
                      <SelectTrigger data-testid={testid}>
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        {options.map((option) => (
                          <SelectItem key={option} value={option}>
                            {option}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>
                ))}
              </div>

              <div className="grid gap-4 sm:grid-cols-3">
                {([
                  ["Prep time (min)", "prepTime", "recipe-prep-input"],
                  ["Cook time (min)", "cookTime", "recipe-cook-input"],
                  ["Servings", "servings", "recipe-servings-input"],
                ] as const).map(([label, key, testid]) => (
                  <div key={key} className="space-y-2">
                    <Label htmlFor={key}>{label}</Label>
                    <Input
                      id={key}
                      type="number"
                      min={key === "servings" ? 1 : 0}
                      value={form[key]}
                      onChange={(e) => setForm({ ...form, [key]: Number(e.target.value) })}
                      data-testid={testid}
                    />
                  </div>
                ))}
              </div>
              <p className="text-sm text-muted-foreground">
                Total time: <strong>{form.prepTime + form.cookTime} minutes</strong>
              </p>
            </div>
          </section>

          {/* Ingredients */}
          <section className="rounded-2xl border border-border bg-card p-6">
            <h2 className="font-heading text-xl font-semibold">Ingredients</h2>
            <p className="mt-1.5 text-sm text-muted-foreground">
              Give each one a quantity and unit — that's what powers scaling and shopping lists.
            </p>
            <div className="mt-5 space-y-3" data-testid="ingredient-rows">
              {form.ingredients.map((ingredient, index) => (
                <div key={index} className="flex flex-wrap items-end gap-2 sm:flex-nowrap">
                  <div className="min-w-[8rem] flex-1 space-y-1.5">
                    <Label className="text-xs">Ingredient</Label>
                    <Input
                      value={ingredient.name}
                      onChange={(e) => setIngredient(index, { name: e.target.value })}
                      placeholder="Tomato"
                      data-testid={`ingredient-name-input-${index}`}
                    />
                  </div>
                  <div className="w-20 space-y-1.5">
                    <Label className="text-xs">Qty</Label>
                    <Input
                      type="number"
                      min={0.01}
                      step="any"
                      value={ingredient.quantity}
                      onChange={(e) => setIngredient(index, { quantity: Number(e.target.value) })}
                      data-testid={`ingredient-qty-input-${index}`}
                    />
                  </div>
                  <div className="w-28 space-y-1.5">
                    <Label className="text-xs">Unit</Label>
                    <Select
                      value={ingredient.unit}
                      onValueChange={(value: string) => setIngredient(index, { unit: value })}
                    >
                      <SelectTrigger data-testid={`ingredient-unit-select-${index}`}>
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        {UNITS.map((unit) => (
                          <SelectItem key={unit} value={unit}>
                            {unit}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>
                  <label className="flex items-center gap-2 pb-2.5 text-xs">
                    <Checkbox
                      checked={ingredient.optional}
                      onCheckedChange={(value) => setIngredient(index, { optional: Boolean(value) })}
                      data-testid={`ingredient-optional-checkbox-${index}`}
                    />
                    Optional
                  </label>
                  <Button
                    type="button"
                    variant="ghost"
                    size="icon"
                    aria-label="Remove ingredient"
                    data-testid={`ingredient-remove-btn-${index}`}
                    onClick={() =>
                      setForm((prev) => ({
                        ...prev,
                        ingredients: prev.ingredients.filter((_, i) => i !== index),
                      }))
                    }
                  >
                    <Trash2 className="size-4" />
                  </Button>
                </div>
              ))}
            </div>
            <Button
              type="button"
              variant="outline"
              className="mt-4"
              data-testid="add-ingredient-btn"
              onClick={() =>
                setForm((prev) => ({
                  ...prev,
                  ingredients: [...prev.ingredients, { name: "", quantity: 1, unit: "g", optional: false }],
                }))
              }
            >
              <Plus className="mr-2 size-4" /> Add ingredient
            </Button>
          </section>

          {/* Steps */}
          <section className="rounded-2xl border border-border bg-card p-6">
            <h2 className="font-heading text-xl font-semibold">Method</h2>
            <div className="mt-5 space-y-3" data-testid="instruction-rows">
              {form.instructions.map((step, index) => (
                <div key={index} className="flex items-start gap-3">
                  <span className="mt-2.5 flex size-8 shrink-0 items-center justify-center rounded-full bg-secondary font-mono text-sm font-semibold">
                    {index + 1}
                  </span>
                  <Textarea
                    value={step}
                    onChange={(e) =>
                      setForm((prev) => ({
                        ...prev,
                        instructions: prev.instructions.map((s, i) => (i === index ? e.target.value : s)),
                      }))
                    }
                    placeholder="Describe this step…"
                    rows={2}
                    data-testid={`instruction-input-${index}`}
                  />
                  <Button
                    type="button"
                    variant="ghost"
                    size="icon"
                    aria-label="Remove step"
                    className="mt-1"
                    data-testid={`instruction-remove-btn-${index}`}
                    onClick={() =>
                      setForm((prev) => ({
                        ...prev,
                        instructions: prev.instructions.filter((_, i) => i !== index),
                      }))
                    }
                  >
                    <Trash2 className="size-4" />
                  </Button>
                </div>
              ))}
            </div>
            <Button
              type="button"
              variant="outline"
              className="mt-4"
              data-testid="add-instruction-btn"
              onClick={() => setForm((prev) => ({ ...prev, instructions: [...prev.instructions, ""] }))}
            >
              <Plus className="mr-2 size-4" /> Add step
            </Button>
          </section>

          {/* Extras */}
          <section className="rounded-2xl border border-border bg-card p-6">
            <h2 className="font-heading text-xl font-semibold">Nutrition &amp; tags</h2>
            <div className="mt-5 grid gap-4 sm:grid-cols-4">
              {([
                ["Calories", "calories"],
                ["Protein (g)", "protein"],
                ["Carbs (g)", "carbs"],
                ["Fat (g)", "fat"],
              ] as const).map(([label, key]) => (
                <div key={key} className="space-y-2">
                  <Label htmlFor={key}>{label}</Label>
                  <Input
                    id={key}
                    type="number"
                    min={0}
                    value={form.nutrition?.[key] ?? ""}
                    onChange={(e) =>
                      setForm({
                        ...form,
                        nutrition: {
                          ...(form.nutrition ?? {}),
                          [key]: e.target.value === "" ? null : Number(e.target.value),
                        },
                      })
                    }
                    data-testid={`nutrition-${key}-input`}
                  />
                </div>
              ))}
            </div>
            <div className="mt-4 space-y-2">
              <Label htmlFor="tags">Tags (comma separated)</Label>
              <Input
                id="tags"
                value={tagText}
                onChange={(e) => setTagText(e.target.value)}
                placeholder="weeknight, one-pan, spicy"
                data-testid="recipe-tags-input"
              />
            </div>
          </section>

          <div className="flex flex-wrap gap-3">
            <Button type="submit" disabled={!valid || save.isPending} data-testid="recipe-publish-btn">
              {save.isPending ? "Saving…" : isEdit ? "Save changes" : "Publish recipe"}
            </Button>
            <Button
              type="button"
              variant="outline"
              disabled={!valid || save.isPending}
              onClick={() => save.mutate("draft")}
              data-testid="recipe-draft-btn"
            >
              Save as draft
            </Button>
          </div>
        </form>
      </div>
    </PageShell>
  );
}
