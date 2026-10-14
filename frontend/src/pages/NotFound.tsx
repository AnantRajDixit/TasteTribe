import { Link } from "react-router-dom";
import { buttonVariants } from "@/components/ui/button";
import PageShell from "@/components/layout/PageShell";

export default function NotFound() {
  return (
    <PageShell>
      <div className="mx-auto max-w-xl px-4 py-28 text-center">
        <p className="font-mono text-xs uppercase tracking-wider text-muted-foreground">404</p>
        <h1 className="mt-3 font-heading text-4xl font-bold tracking-tight">
          This page left the kitchen
        </h1>
        <p className="mt-4 text-muted-foreground">
          The link may be old, or the recipe was taken off the menu.
        </p>
        <Link to="/" className={buttonVariants({ className: "mt-8" })} data-testid="notfound-home-link">
          Back to the front page
        </Link>
      </div>
    </PageShell>
  );
}
