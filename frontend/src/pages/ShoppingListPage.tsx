import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { useMutation, useQuery } from "@tanstack/react-query";
import { Trash2, Plus, CheckCheck } from "lucide-react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Checkbox } from "@/components/ui/checkbox";
import { apiDelete, apiGet, apiPatch, apiPost } from "@/lib/api";
import { queryClient } from "@/lib/queryClient";
import { useMe } from "@/lib/session";
import { errorMessage, formatQuantity } from "@/lib/format";
import PageShell from "@/components/layout/PageShell";
import type { ShoppingList } from "@/lib/types";

export default function ShoppingListPage() {
  const { data: me, isLoading } = useMe();
  const navigate = useNavigate();
  const [name, setName] = useState("");

  useEffect(() => {
    if (!isLoading && !me) navigate("/login?next=/shopping-list");
  }, [me, isLoading, navigate]);

  const list = useQuery({
    queryKey: ["shopping-list"],
    queryFn: () => apiGet<ShoppingList>("/shopping-list"),
    enabled: Boolean(me),
    retry: false,
  });

  const refresh = () => void queryClient.invalidateQueries({ queryKey: ["shopping-list"] });

  const addItem = useMutation({
    mutationFn: () => apiPost<ShoppingList>("/shopping-list/items", { name, quantity: 1, unit: "piece" }),
    onSuccess: () => {
      setName("");
      refresh();
    },
    onError: (e) => toast.error(errorMessage(e)),
  });

  const toggle = useMutation({
    mutationFn: (vars: { id: string; checked: boolean }) =>
      apiPatch<ShoppingList>(`/shopping-list/items/${vars.id}`, { checked: vars.checked }),
    onSuccess: refresh,
    onError: (e) => toast.error(errorMessage(e)),
  });

  const remove = useMutation({
    mutationFn: (itemId: string) => apiDelete<ShoppingList>(`/shopping-list/items/${itemId}`),
    onSuccess: refresh,
    onError: (e) => toast.error(errorMessage(e)),
  });

  const clearChecked = useMutation({
    mutationFn: () => apiPost<ShoppingList>("/shopping-list/clear-checked"),
    onSuccess: () => {
      toast.success("Checked items cleared");
      refresh();
    },
    onError: (e) => toast.error(errorMessage(e)),
  });

  const items = list.data?.items ?? [];
  const remaining = items.filter((item) => !item.checked).length;

  return (
    <PageShell>
      <div className="mx-auto max-w-3xl px-4 py-10 sm:px-6 lg:px-8">
        <header>
          <p className="font-mono text-xs uppercase tracking-wider text-muted-foreground">
            Grocery run
          </p>
          <h1 className="mt-2 font-heading text-4xl font-bold tracking-tight">Shopping list</h1>
          <p className="mt-2 text-muted-foreground" data-testid="shopping-list-summary">
            {items.length === 0
              ? "Add ingredients from any recipe, or type them in below."
              : `${remaining} of ${items.length} items still to buy.`}
          </p>
        </header>

        <form
          onSubmit={(event) => {
            event.preventDefault();
            if (name.trim()) addItem.mutate();
          }}
          className="mt-8 flex gap-2"
        >
          <Input
            value={name}
            onChange={(event) => setName(event.target.value)}
            placeholder="Add a custom item…"
            aria-label="Add shopping list item"
            data-testid="shopping-list-add-input"
          />
          <Button type="submit" disabled={!name.trim() || addItem.isPending} data-testid="shopping-list-add-btn">
            <Plus className="mr-1.5 size-4" /> Add
          </Button>
        </form>

        <div className="mt-6 overflow-hidden rounded-2xl border border-border bg-card">
          {items.length === 0 ? (
            <p className="p-10 text-center text-sm text-muted-foreground" data-testid="shopping-list-empty">
              Your list is empty. Open a recipe and tap "Add all to shopping list".
            </p>
          ) : (
            <ul className="divide-y divide-border" data-testid="shopping-list-items">
              {items.map((item) => (
                <li key={item.id} className="flex items-center gap-3 px-5 py-3.5" data-testid="shopping-list-row">
                  <Checkbox
                    checked={item.checked}
                    onCheckedChange={(value) => toggle.mutate({ id: item.id, checked: Boolean(value) })}
                    aria-label={`Mark ${item.name} as bought`}
                    data-testid="shopping-list-item-checkbox"
                  />
                  <span className="flex-1">
                    <span
                      className={`block text-sm ${item.checked ? "text-muted-foreground line-through" : ""}`}
                    >
                      <strong className="font-semibold">
                        {formatQuantity(item.quantity)} {item.unit}
                      </strong>{" "}
                      {item.name}
                    </span>
                    {item.recipeTitle && (
                      <span className="block text-xs text-muted-foreground">for {item.recipeTitle}</span>
                    )}
                  </span>
                  <Button
                    variant="ghost"
                    size="icon-sm"
                    aria-label={`Remove ${item.name}`}
                    data-testid="shopping-list-remove-btn"
                    onClick={() => remove.mutate(item.id)}
                  >
                    <Trash2 className="size-4" />
                  </Button>
                </li>
              ))}
            </ul>
          )}
        </div>

        {items.some((item) => item.checked) && (
          <Button
            variant="outline"
            className="mt-5"
            onClick={() => clearChecked.mutate()}
            data-testid="shopping-list-clear-checked-btn"
          >
            <CheckCheck className="mr-2 size-4" /> Clear checked items
          </Button>
        )}
      </div>
    </PageShell>
  );
}
