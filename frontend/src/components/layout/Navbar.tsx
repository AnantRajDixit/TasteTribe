import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { ChefHat, Search, Menu, PlusCircle, ShoppingBasket, LayoutDashboard, Shield, LogOut, User as UserIcon } from "lucide-react";
import { Button, buttonVariants } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Sheet, SheetContent, SheetTrigger, SheetTitle } from "@/components/ui/sheet";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { useMe, endSession } from "@/lib/session";
import { initials } from "@/lib/format";
import { toast } from "sonner";

const NAV_LINKS = [
  { to: "/recipes", label: "Explore", testid: "nav-explore-link" },
  { to: "/categories", label: "Categories", testid: "nav-categories-link" },
  { to: "/feed", label: "Community", testid: "nav-feed-link" },
];

export default function Navbar() {
  const { data: me } = useMe();
  const navigate = useNavigate();
  const [term, setTerm] = useState("");
  const [mobileOpen, setMobileOpen] = useState(false);

  const submitSearch = (event: React.FormEvent) => {
    event.preventDefault();
    navigate(`/recipes?q=${encodeURIComponent(term.trim())}`);
  };

  const signOut = async () => {
    await endSession();
    toast.success("Signed out. See you at the next meal.");
    navigate("/");
  };

  return (
    <header className="sticky top-0 z-40 w-full border-b border-border bg-background/90 backdrop-blur-xl">
      <div className="mx-auto flex max-w-7xl items-center gap-4 px-4 py-3 sm:px-6 lg:px-8">
        <Link to="/" className="flex shrink-0 items-center gap-2" data-testid="navbar-brand-logo">
          <span className="flex size-9 items-center justify-center rounded-xl bg-primary text-primary-foreground">
            <ChefHat className="size-5" />
          </span>
          <span className="font-heading text-xl font-bold tracking-tight">TasteTribe</span>
        </Link>

        <nav className="ml-4 hidden items-center gap-1 lg:flex">
          {NAV_LINKS.map((link) => (
            <Link
              key={link.to}
              to={link.to}
              data-testid={link.testid}
              className="rounded-lg px-3 py-2 text-sm font-medium text-muted-foreground transition-colors duration-150 hover:bg-muted hover:text-foreground"
            >
              {link.label}
            </Link>
          ))}
        </nav>

        <form onSubmit={submitSearch} className="ml-auto hidden max-w-xs flex-1 md:block">
          <div className="relative">
            <Search className="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
            <Input
              value={term}
              onChange={(event) => setTerm(event.target.value)}
              placeholder="Search recipes or ingredients"
              aria-label="Search recipes"
              data-testid="search-input-main"
              className="pl-9"
            />
          </div>
          <button type="submit" className="sr-only" data-testid="search-submit-btn">
            Search
          </button>
        </form>

        <div className="ml-auto flex items-center gap-2 md:ml-0">
          <Link
            to="/shopping-list"
            aria-label="Shopping list"
            data-testid="nav-shopping-list-link"
            className={buttonVariants({ variant: "ghost", size: "icon" }) + " hidden sm:inline-flex"}
          >
            <ShoppingBasket className="size-5" />
          </Link>

          {me ? (
            <>
              <Link
                to="/recipes/new"
                data-testid="nav-create-recipe-btn"
                className={buttonVariants({ size: "sm" }) + " hidden sm:inline-flex"}
              >
                <PlusCircle className="mr-1.5 size-4" />
                Add recipe
              </Link>
              <DropdownMenu>
                <DropdownMenuTrigger
                  data-testid="nav-user-menu-trigger"
                  aria-label="Account menu"
                  className="flex size-9 items-center justify-center overflow-hidden rounded-full border border-border bg-muted text-sm font-semibold transition-shadow duration-150 hover:shadow-md"
                >
                  {me.avatarUrl ? (
                    <img src={me.avatarUrl} alt={me.name} className="size-full object-cover" />
                  ) : (
                    initials(me.name)
                  )}
                </DropdownMenuTrigger>
                <DropdownMenuContent align="end" className="w-52">
                  <DropdownMenuItem
                    onClick={() => navigate(`/chefs/${me.username}`)}
                    data-testid="menu-profile-link"
                  >
                    <UserIcon className="mr-2 size-4" />
                    My profile
                  </DropdownMenuItem>
                  <DropdownMenuItem
                    onClick={() => navigate("/dashboard")}
                    data-testid="nav-dashboard-link"
                  >
                    <LayoutDashboard className="mr-2 size-4" />
                    Dashboard
                  </DropdownMenuItem>
                  <DropdownMenuItem
                    onClick={() => navigate("/shopping-list")}
                    data-testid="menu-shopping-link"
                  >
                    <ShoppingBasket className="mr-2 size-4" />
                    Shopping list
                  </DropdownMenuItem>
                  {me.role === "admin" && (
                    <DropdownMenuItem onClick={() => navigate("/admin")} data-testid="nav-admin-link">
                      <Shield className="mr-2 size-4" />
                      Admin panel
                    </DropdownMenuItem>
                  )}
                  <DropdownMenuSeparator />
                  <DropdownMenuItem onClick={signOut} variant="destructive" data-testid="menu-logout-btn">
                    <LogOut className="mr-2 size-4" />
                    Sign out
                  </DropdownMenuItem>
                </DropdownMenuContent>
              </DropdownMenu>
            </>
          ) : (
            <>
              <Link
                to="/login"
                data-testid="nav-auth-login-btn"
                className={buttonVariants({ variant: "ghost", size: "sm" })}
              >
                Log in
              </Link>
              <Link
                to="/register"
                data-testid="nav-auth-register-btn"
                className={buttonVariants({ size: "sm" })}
              >
                Join free
              </Link>
            </>
          )}

          <Sheet open={mobileOpen} onOpenChange={setMobileOpen}>
            <SheetTrigger
              aria-label="Open menu"
              data-testid="nav-mobile-menu-trigger"
              className={buttonVariants({ variant: "ghost", size: "icon" }) + " lg:hidden"}
            >
              <Menu className="size-5" />
            </SheetTrigger>
            <SheetContent side="right" className="w-72 p-6">
              <SheetTitle className="font-heading text-lg">Browse TasteTribe</SheetTitle>
              <nav className="mt-6 flex flex-col gap-1">
                {NAV_LINKS.concat([
                  { to: "/shopping-list", label: "Shopping list", testid: "nav-mobile-shopping" },
                  { to: "/dashboard", label: "Dashboard", testid: "nav-mobile-dashboard" },
                ]).map((link) => (
                  <Link
                    key={link.to}
                    to={link.to}
                    onClick={() => setMobileOpen(false)}
                    data-testid={link.testid}
                    className="rounded-lg px-3 py-2.5 text-sm font-medium transition-colors duration-150 hover:bg-muted"
                  >
                    {link.label}
                  </Link>
                ))}
              </nav>
              {me && (
                <Link
                  to="/recipes/new"
                  onClick={() => setMobileOpen(false)}
                  className={buttonVariants({ className: "mt-6 w-full" })}
                  data-testid="nav-mobile-create"
                >
                  Add a recipe
                </Link>
              )}
            </SheetContent>
          </Sheet>
        </div>
      </div>
    </header>
  );
}
