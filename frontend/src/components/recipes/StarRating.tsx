import { Star } from "lucide-react";

/** Read-only star rating display. */
export function StarRating({
  value,
  count,
  size = "sm",
  testid,
}: {
  value?: number | null;
  count?: number;
  size?: "sm" | "md";
  testid?: string;
}) {
  const rating = value ?? 0;
  const dimension = size === "md" ? "size-5" : "size-4";
  return (
    <span className="inline-flex items-center gap-1.5" data-testid={testid ?? "recipe-card-rating"}>
      <span className="inline-flex">
        {[1, 2, 3, 4, 5].map((index) => (
          <Star
            key={index}
            className={`${dimension} ${
              index <= Math.round(rating)
                ? "fill-primary text-primary"
                : "fill-transparent text-muted-foreground/40"
            }`}
          />
        ))}
      </span>
      {value ? (
        <span className="text-sm font-medium">
          {value.toFixed(1)}
          {count !== undefined && count > 0 && (
            <span className="font-normal text-muted-foreground"> ({count})</span>
          )}
        </span>
      ) : (
        <span className="text-sm text-muted-foreground">No ratings yet</span>
      )}
    </span>
  );
}

/** Interactive 1-5 star picker. */
export function StarPicker({
  value,
  onChange,
}: {
  value: number;
  onChange: (next: number) => void;
}) {
  return (
    <div className="flex items-center gap-1" data-testid="review-rating-star-group">
      {[1, 2, 3, 4, 5].map((index) => (
        <button
          key={index}
          type="button"
          aria-label={`Rate ${index} star${index === 1 ? "" : "s"}`}
          data-testid={`review-star-${index}`}
          onClick={() => onChange(index)}
          className="rounded p-0.5 transition-transform duration-150 hover:scale-110 focus-visible:ring-2 focus-visible:ring-primary"
        >
          <Star
            className={`size-7 ${
              index <= value ? "fill-primary text-primary" : "fill-transparent text-muted-foreground/50"
            }`}
          />
        </button>
      ))}
    </div>
  );
}
