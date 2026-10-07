import { Link } from "react-router-dom";
import { ChefHat } from "lucide-react";

export default function Footer() {
  return (
    <footer className="mt-24 border-t border-border bg-card">
      <div className="mx-auto grid max-w-7xl gap-10 px-4 py-14 sm:px-6 md:grid-cols-4 lg:px-8">
        <div className="md:col-span-2">
          <div className="flex items-center gap-2">
            <span className="flex size-9 items-center justify-center rounded-xl bg-primary text-primary-foreground">
              <ChefHat className="size-5" />
            </span>
            <span className="font-heading text-xl font-bold">TasteTribe</span>
          </div>
          <p className="mt-4 max-w-sm text-sm leading-relaxed text-muted-foreground">
            A community kitchen where home cooks publish real recipes, rate each other's
            work and get AI help when the pantry looks empty.
          </p>
        </div>
        <div>
          <h3 className="font-mono text-xs uppercase tracking-wider text-muted-foreground">Cook</h3>
          <ul className="mt-4 space-y-2 text-sm">
            <li><Link to="/recipes" className="transition-colors duration-150 hover:text-primary">Explore recipes</Link></li>
            <li><Link to="/categories" className="transition-colors duration-150 hover:text-primary">Categories</Link></li>
            <li><Link to="/feed" className="transition-colors duration-150 hover:text-primary">Community feed</Link></li>
            <li><Link to="/shopping-list" className="transition-colors duration-150 hover:text-primary">Shopping list</Link></li>
          </ul>
        </div>
        <div>
          <h3 className="font-mono text-xs uppercase tracking-wider text-muted-foreground">Account</h3>
          <ul className="mt-4 space-y-2 text-sm">
            <li><Link to="/dashboard" className="transition-colors duration-150 hover:text-primary">My dashboard</Link></li>
            <li><Link to="/recipes/new" className="transition-colors duration-150 hover:text-primary">Publish a recipe</Link></li>
            <li><Link to="/login" className="transition-colors duration-150 hover:text-primary">Log in</Link></li>
            <li><Link to="/register" className="transition-colors duration-150 hover:text-primary">Create account</Link></li>
          </ul>
        </div>
      </div>
      <div className="border-t border-border px-4 py-6 text-center text-xs text-muted-foreground">
        Built with Java, Spring Boot, JDBC and MySQL · TasteTribe {new Date().getFullYear()}
      </div>
    </footer>
  );
}
