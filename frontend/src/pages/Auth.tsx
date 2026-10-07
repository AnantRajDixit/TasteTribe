import { useState } from "react";
import { Link, useNavigate, useSearchParams } from "react-router-dom";
import { useMutation } from "@tanstack/react-query";
import { toast } from "sonner";
import { ChefHat } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import AvatarPicker from "@/components/profile/AvatarPicker";
import { apiPost } from "@/lib/api";
import { beginSession } from "@/lib/session";
import { errorMessage } from "@/lib/format";
import type { User } from "@/lib/types";

const SIDE_IMAGE =
  "https://images.unsplash.com/photo-1577219491135-ce391730fb2c?crop=entropy&cs=srgb&fm=jpg&q=85&w=1200";

/** Shared split-screen auth layout for login and register. */
function AuthShell({
  title,
  subtitle,
  children,
  footer,
}: {
  title: string;
  subtitle: string;
  children: React.ReactNode;
  footer: React.ReactNode;
}) {
  return (
    <div className="grid min-h-svh lg:grid-cols-2">
      <div className="relative hidden lg:block">
        <img src={SIDE_IMAGE} alt="" aria-hidden className="absolute inset-0 size-full object-cover" />
        <div aria-hidden className="absolute inset-0 bg-stone-900/70" />
        <div className="relative flex h-full flex-col justify-between p-12">
          <Link to="/" className="flex items-center gap-2 text-stone-50">
            <span className="flex size-9 items-center justify-center rounded-xl bg-primary">
              <ChefHat className="size-5 text-primary-foreground" />
            </span>
            <span className="font-heading text-xl font-bold">TasteTribe</span>
          </Link>
          <blockquote className="max-w-sm">
            <p className="font-heading text-2xl leading-snug text-stone-50">
              "The best recipes come with someone's fingerprints on them."
            </p>
            <footer className="mt-4 text-sm text-stone-300">
              Join cooks sharing what actually works in their kitchen.
            </footer>
          </blockquote>
        </div>
      </div>

      <div className="flex items-center justify-center px-4 py-12 sm:px-8">
        <div className="w-full max-w-sm">
          <Link to="/" className="mb-8 flex items-center gap-2 lg:hidden">
            <span className="flex size-9 items-center justify-center rounded-xl bg-primary text-primary-foreground">
              <ChefHat className="size-5" />
            </span>
            <span className="font-heading text-xl font-bold">TasteTribe</span>
          </Link>
          <h1 className="font-heading text-3xl font-bold tracking-tight">{title}</h1>
          <p className="mt-2 text-sm text-muted-foreground">{subtitle}</p>
          <div className="mt-8">{children}</div>
          <div className="mt-6 text-center text-sm text-muted-foreground">{footer}</div>
        </div>
      </div>
    </div>
  );
}

export function LoginPage() {
  const navigate = useNavigate();
  const [params] = useSearchParams();
  const [form, setForm] = useState({ usernameOrEmail: "", password: "" });

  const login = useMutation({
    mutationFn: () => apiPost<User>("/auth/login", form),
    onSuccess: async (user) => {
      await beginSession();
      toast.success(`Welcome back, ${user.name.split(" ")[0]}!`);
      navigate(params.get("next") ?? "/");
    },
    onError: (error) => toast.error(errorMessage(error, "Could not sign you in.")),
  });

  return (
    <AuthShell
      title="Welcome back"
      subtitle="Log in to save recipes, rate dishes and talk to the AI sous-chef."
      footer={
        <>
          New here?{" "}
          <Link to="/register" className="font-medium text-primary hover:underline" data-testid="login-to-register-link">
            Create an account
          </Link>
        </>
      }
    >
      <form
        onSubmit={(event) => {
          event.preventDefault();
          login.mutate();
        }}
        className="space-y-4"
      >
        <div className="space-y-2">
          <Label htmlFor="identifier">Username or email</Label>
          <Input
            id="identifier"
            value={form.usernameOrEmail}
            onChange={(event) => setForm({ ...form, usernameOrEmail: event.target.value })}
            placeholder="priya"
            autoComplete="username"
            required
            data-testid="login-identifier-input"
          />
        </div>
        <div className="space-y-2">
          <Label htmlFor="password">Password</Label>
          <Input
            id="password"
            type="password"
            value={form.password}
            onChange={(event) => setForm({ ...form, password: event.target.value })}
            placeholder="••••••••"
            autoComplete="current-password"
            required
            data-testid="login-password-input"
          />
        </div>
        <Button type="submit" className="w-full" disabled={login.isPending} data-testid="login-submit-btn">
          {login.isPending ? "Signing in…" : "Log in"}
        </Button>
      </form>

      <p className="mt-6 rounded-xl border border-border bg-secondary/50 p-3 text-xs leading-relaxed text-muted-foreground">
        <strong className="font-semibold text-foreground">Demo accounts:</strong> log in as
        <code className="mx-1 font-mono">priya</code>/<code className="font-mono">taste1234</code>
        (cook) or <code className="mx-1 font-mono">admin</code>/<code className="font-mono">admin1234</code> (admin).
      </p>
    </AuthShell>
  );
}

export function RegisterPage() {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    name: "",
    username: "",
    email: "",
    password: "",
    avatarUrl: "",
  });

  const register = useMutation({
    mutationFn: () =>
      apiPost<User>("/auth/register", {
        ...form,
        avatarUrl: form.avatarUrl.trim() || null,
      }),
    onSuccess: async (user) => {
      await beginSession();
      toast.success(`Welcome to TasteTribe, ${user.name.split(" ")[0]}!`);
      navigate("/dashboard");
    },
    onError: (error) => toast.error(errorMessage(error, "Could not create your account.")),
  });

  return (
    <AuthShell
      title="Join the tribe"
      subtitle="Publish your recipes, follow cooks you trust and build your collection."
      footer={
        <>
          Already cooking with us?{" "}
          <Link to="/login" className="font-medium text-primary hover:underline" data-testid="register-to-login-link">
            Log in
          </Link>
        </>
      }
    >
      <form
        onSubmit={(event) => {
          event.preventDefault();
          register.mutate();
        }}
        className="space-y-4"
      >
        <div className="space-y-2">
          <Label htmlFor="name">Full name</Label>
          <Input
            id="name"
            value={form.name}
            onChange={(event) => setForm({ ...form, name: event.target.value })}
            placeholder="Priya Raghavan"
            required
            data-testid="register-name-input"
          />
        </div>
        <div className="space-y-2">
          <Label htmlFor="username">Username</Label>
          <Input
            id="username"
            value={form.username}
            onChange={(event) => setForm({ ...form, username: event.target.value })}
            placeholder="priyacooks"
            required
            data-testid="register-username-input"
          />
        </div>
        <div className="space-y-2">
          <Label htmlFor="email">Email</Label>
          <Input
            id="email"
            type="email"
            value={form.email}
            onChange={(event) => setForm({ ...form, email: event.target.value })}
            placeholder="you@example.com"
            required
            data-testid="register-email-input"
          />
        </div>
        <div className="space-y-2">
          <Label htmlFor="newPassword">Password</Label>
          <Input
            id="newPassword"
            type="password"
            value={form.password}
            onChange={(event) => setForm({ ...form, password: event.target.value })}
            placeholder="At least 8 characters"
            autoComplete="new-password"
            required
            data-testid="register-password-input"
          />
        </div>
        <AvatarPicker
          value={form.avatarUrl}
          onChange={(next) => setForm({ ...form, avatarUrl: next })}
          label="Profile picture (optional)"
        />
        <Button type="submit" className="w-full" disabled={register.isPending} data-testid="register-submit-btn">
          {register.isPending ? "Creating account…" : "Create account"}
        </Button>
      </form>
    </AuthShell>
  );
}
