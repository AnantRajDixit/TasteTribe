import { useState, useEffect, useRef, useCallback } from "react";
import { X, ChevronLeft, ChevronRight, Play, Pause, RotateCcw, Timer, Check } from "lucide-react";
import { Button } from "@/components/ui/button";
import { formatQuantity } from "@/lib/format";
import type { Recipe, ScaledIngredient } from "@/lib/types";

/** Pull the first "N minute(s)" / "N min" mention out of a step, in seconds. */
function detectTimer(step: string): number | null {
  const match = step.match(/(\d+(?:\.\d+)?)\s*(?:-\s*\d+\s*)?(minute|minutes|min|mins|hour|hours|hr|hrs|second|seconds|sec|secs)\b/i);
  if (!match) return null;
  const amount = Number(match[1]);
  const unit = match[2].toLowerCase();
  if (unit.startsWith("hour") || unit.startsWith("hr")) return Math.round(amount * 3600);
  if (unit.startsWith("sec")) return Math.round(amount);
  return Math.round(amount * 60);
}

function clock(seconds: number): string {
  const safe = Math.max(0, seconds);
  const minutes = Math.floor(safe / 60);
  const rest = safe % 60;
  return `${String(minutes).padStart(2, "0")}:${String(rest).padStart(2, "0")}`;
}

/**
 * Full-screen, phone-friendly cooking view: one step at a time in large type, the
 * scaled ingredient list for reference, and a countdown timer seeded from any
 * duration mentioned in the current step.
 */
export default function CookMode({
  recipe,
  ingredients,
  servings,
  onClose,
}: {
  recipe: Recipe;
  ingredients: ScaledIngredient[];
  servings: number;
  onClose: () => void;
}) {
  const steps = recipe.instructions.length > 0 ? recipe.instructions : ["No steps provided."];
  const [index, setIndex] = useState(0);
  const [remaining, setRemaining] = useState<number | null>(null);
  const [running, setRunning] = useState(false);
  const [done, setDone] = useState<Record<number, boolean>>({});
  const tick = useRef<number | null>(null);

  const suggested = detectTimer(steps[index]);

  // Reset the timer whenever the step changes.
  useEffect(() => {
    setRemaining(detectTimer(steps[index]));
    setRunning(false);
  }, [index, steps]);

  // Countdown loop.
  useEffect(() => {
    if (!running || remaining === null) return;
    tick.current = window.setInterval(() => {
      setRemaining((prev) => {
        if (prev === null) return prev;
        if (prev <= 1) {
          setRunning(false);
          return 0;
        }
        return prev - 1;
      });
    }, 1000);
    return () => {
      if (tick.current) window.clearInterval(tick.current);
    };
  }, [running, remaining === null]);

  // Esc closes, arrows navigate.
  const go = useCallback(
    (next: number) => setIndex((prev) => Math.min(steps.length - 1, Math.max(0, next))),
    [steps.length],
  );

  useEffect(() => {
    const onKey = (event: KeyboardEvent) => {
      if (event.key === "Escape") onClose();
      if (event.key === "ArrowRight") setIndex((p) => Math.min(steps.length - 1, p + 1));
      if (event.key === "ArrowLeft") setIndex((p) => Math.max(0, p - 1));
    };
    window.addEventListener("keydown", onKey);
    document.body.style.overflow = "hidden";
    return () => {
      window.removeEventListener("keydown", onKey);
      document.body.style.overflow = "";
    };
  }, [onClose, steps.length]);

  const finished = remaining === 0;

  return (
    <div
      className="fixed inset-0 z-[60] flex flex-col bg-stone-950 text-stone-50"
      data-testid="cook-mode-overlay"
      role="dialog"
      aria-modal="true"
      aria-label={`Cook mode: ${recipe.title}`}
    >
      {/* Header */}
      <header className="flex items-center gap-3 border-b border-stone-800 px-4 py-3 sm:px-6">
        <div className="min-w-0 flex-1">
          <p className="truncate font-heading text-base font-semibold sm:text-lg">{recipe.title}</p>
          <p className="font-mono text-xs uppercase tracking-wider text-stone-400">
            Serves {servings} · Step {index + 1} of {steps.length}
          </p>
        </div>
        <Button
          variant="ghost"
          size="icon"
          aria-label="Exit cook mode"
          data-testid="cook-mode-close-btn"
          onClick={onClose}
          className="text-stone-50 hover:bg-stone-800 hover:text-stone-50"
        >
          <X className="size-5" />
        </Button>
      </header>

      {/* Progress */}
      <div className="h-1 w-full bg-stone-800" aria-hidden>
        <div
          className="h-full bg-primary transition-[width] duration-300"
          style={{ width: `${((index + 1) / steps.length) * 100}%` }}
        />
      </div>

      <div className="flex flex-1 flex-col overflow-y-auto lg:flex-row">
        {/* Current step */}
        <div className="flex flex-1 flex-col justify-center px-5 py-8 sm:px-10 lg:px-16">
          <span className="flex size-12 items-center justify-center rounded-full bg-primary font-mono text-lg font-bold text-primary-foreground">
            {index + 1}
          </span>
          <p
            className="mt-6 max-w-3xl font-heading text-2xl leading-snug sm:text-3xl lg:text-4xl"
            data-testid="cook-mode-step-text"
          >
            {steps[index]}
          </p>

          {/* Timer */}
          {(suggested !== null || remaining !== null) && (
            <div
              className="mt-10 flex flex-wrap items-center gap-4 rounded-2xl border border-stone-800 bg-stone-900 p-5"
              data-testid="cook-mode-timer"
            >
              <Timer className={`size-5 ${finished ? "text-emerald-400" : "text-primary"}`} />
              <span
                className={`font-mono text-4xl font-bold tabular-nums ${
                  finished ? "text-emerald-400" : ""
                }`}
                aria-live="polite"
                data-testid="cook-mode-timer-display"
              >
                {clock(remaining ?? suggested ?? 0)}
              </span>
              {finished ? (
                <span className="font-medium text-emerald-400">Time's up!</span>
              ) : (
                <>
                  <Button
                    onClick={() => setRunning((prev) => !prev)}
                    data-testid="cook-mode-timer-toggle-btn"
                  >
                    {running ? (
                      <>
                        <Pause className="mr-2 size-4" /> Pause
                      </>
                    ) : (
                      <>
                        <Play className="mr-2 size-4" /> Start
                      </>
                    )}
                  </Button>
                  <Button
                    variant="outline"
                    onClick={() => {
                      setRunning(false);
                      setRemaining(suggested);
                    }}
                    data-testid="cook-mode-timer-reset-btn"
                    className="border-stone-600 bg-transparent text-stone-50 hover:bg-stone-800 hover:text-stone-50"
                  >
                    <RotateCcw className="mr-2 size-4" /> Reset
                  </Button>
                </>
              )}
            </div>
          )}

          <label className="mt-8 inline-flex w-fit cursor-pointer items-center gap-2.5 text-sm text-stone-300">
            <input
              type="checkbox"
              checked={Boolean(done[index])}
              onChange={(event) => setDone((prev) => ({ ...prev, [index]: event.target.checked }))}
              className="size-4 accent-orange-500"
              data-testid="cook-mode-step-done-checkbox"
            />
            Mark this step done
          </label>
        </div>

        {/* Ingredient reference */}
        <aside className="border-t border-stone-800 px-5 py-6 sm:px-10 lg:w-80 lg:border-l lg:border-t-0 lg:px-6">
          <h2 className="font-mono text-xs uppercase tracking-wider text-stone-400">
            Ingredients for {servings}
          </h2>
          <ul className="mt-4 space-y-2.5" data-testid="cook-mode-ingredients">
            {ingredients.map((item, i) => (
              <li key={`${item.name}-${i}`} className="text-sm leading-relaxed text-stone-200">
                <strong className="font-semibold text-stone-50">
                  {formatQuantity(item.quantity)} {item.unit}
                </strong>{" "}
                {item.name}
              </li>
            ))}
          </ul>
        </aside>
      </div>

      {/* Footer nav */}
      <footer className="flex items-center gap-3 border-t border-stone-800 px-4 py-4 sm:px-6">
        <Button
          variant="outline"
          disabled={index === 0}
          onClick={() => go(index - 1)}
          data-testid="cook-mode-prev-btn"
          className="border-stone-600 bg-transparent text-stone-50 hover:bg-stone-800 hover:text-stone-50"
        >
          <ChevronLeft className="mr-1 size-4" /> Back
        </Button>
        {index < steps.length - 1 ? (
          <Button className="flex-1 sm:flex-none" onClick={() => go(index + 1)} data-testid="cook-mode-next-btn">
            Next step <ChevronRight className="ml-1 size-4" />
          </Button>
        ) : (
          <Button className="flex-1 sm:flex-none" onClick={onClose} data-testid="cook-mode-finish-btn">
            <Check className="mr-1.5 size-4" /> Finish cooking
          </Button>
        )}
      </footer>
    </div>
  );
}
