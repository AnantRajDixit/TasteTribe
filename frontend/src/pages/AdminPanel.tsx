import { useEffect } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { useMutation, useQuery } from "@tanstack/react-query";
import { toast } from "sonner";
import { Trash2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Tabs, TabsList, TabsTrigger } from "@/components/ui/tabs";
import {
  Table, TableBody, TableCell, TableHead, TableHeader, TableRow,
} from "@/components/ui/table";
import { apiDelete, apiGet, apiPost } from "@/lib/api";
import { queryClient } from "@/lib/queryClient";
import { useMe } from "@/lib/session";
import { errorMessage, timeAgo } from "@/lib/format";
import PageShell from "@/components/layout/PageShell";
import type {
  AdminRecipeRow, AdminReportRow, AdminStats, AdminUserRow, Comment,
} from "@/lib/types";

const TABS = [
  { value: "overview", label: "Overview" },
  { value: "users", label: "Users" },
  { value: "recipes", label: "Recipes" },
  { value: "comments", label: "Comments" },
  { value: "reports", label: "Reports" },
];

export default function AdminPanel() {
  const { data: me, isLoading } = useMe();
  const navigate = useNavigate();
  const [params, setParams] = useSearchParams();
  const tab = params.get("tab") ?? "overview";

  useEffect(() => {
    if (!isLoading && (!me || me.role !== "admin")) {
      toast.error("Admin access required.");
      navigate("/");
    }
  }, [me, isLoading, navigate]);

  const isAdmin = me?.role === "admin";
  const refresh = () => void queryClient.invalidateQueries();

  const stats = useQuery({
    queryKey: ["admin", "stats"],
    queryFn: () => apiGet<AdminStats>("/admin/stats"),
    enabled: isAdmin,
    retry: false,
  });

  const users = useQuery({
    queryKey: ["admin", "users"],
    queryFn: () => apiGet<AdminUserRow[]>("/admin/users"),
    enabled: isAdmin && tab === "users",
    retry: false,
  });

  const recipes = useQuery({
    queryKey: ["admin", "recipes"],
    queryFn: () => apiGet<AdminRecipeRow[]>("/admin/recipes"),
    enabled: isAdmin && tab === "recipes",
    retry: false,
  });

  const comments = useQuery({
    queryKey: ["admin", "comments"],
    queryFn: () => apiGet<Comment[]>("/admin/comments"),
    enabled: isAdmin && tab === "comments",
    retry: false,
  });

  const reports = useQuery({
    queryKey: ["admin", "reports"],
    queryFn: () => apiGet<AdminReportRow[]>("/admin/reports"),
    enabled: isAdmin && tab === "reports",
    retry: false,
  });

  const deleteUser = useMutation({
    mutationFn: (id: string) => apiDelete(`/admin/users/${id}`),
    onSuccess: () => {
      toast.success("User removed");
      refresh();
    },
    onError: (e) => toast.error(errorMessage(e)),
  });

  const deleteRecipe = useMutation({
    mutationFn: (id: string) => apiDelete(`/recipes/${id}`),
    onSuccess: () => {
      toast.success("Recipe removed");
      refresh();
    },
    onError: (e) => toast.error(errorMessage(e)),
  });

  const deleteComment = useMutation({
    mutationFn: (id: string) => apiDelete(`/comments/${id}`),
    onSuccess: () => {
      toast.success("Comment removed");
      refresh();
    },
    onError: (e) => toast.error(errorMessage(e)),
  });

  const dismissReport = useMutation({
    mutationFn: (id: string) => apiPost(`/admin/reports/${id}/dismiss`),
    onSuccess: () => {
      toast.success("Report dismissed");
      refresh();
    },
    onError: (e) => toast.error(errorMessage(e)),
  });

  const statCards = [
    { label: "Users", value: stats.data?.users ?? 0 },
    { label: "Recipes", value: stats.data?.recipes ?? 0 },
    { label: "Published", value: stats.data?.publishedRecipes ?? 0 },
    { label: "Reviews", value: stats.data?.reviews ?? 0 },
    { label: "Comments", value: stats.data?.comments ?? 0 },
    { label: "Open reports", value: stats.data?.pendingReports ?? 0 },
  ];

  return (
    <PageShell>
      <div className="mx-auto max-w-7xl px-4 py-10 sm:px-6 lg:px-8">
        <header>
          <p className="font-mono text-xs uppercase tracking-wider text-muted-foreground">
            Moderation
          </p>
          <h1 className="mt-2 font-heading text-4xl font-bold tracking-tight">Admin panel</h1>
        </header>

        <Tabs
          value={tab}
          onValueChange={(value) => setParams(new URLSearchParams({ tab: String(value) }))}
          className="mt-8"
        >
          <TabsList variant="line" data-testid="admin-tabs">
            {TABS.map((item) => (
              <TabsTrigger key={item.value} value={item.value} data-testid={`admin-tab-${item.value}`}>
                {item.label}
              </TabsTrigger>
            ))}
          </TabsList>
        </Tabs>

        <div className="mt-8">
          {tab === "overview" && (
            <div className="grid grid-cols-2 gap-4 lg:grid-cols-3" data-testid="admin-stat-cards">
              {statCards.map((card) => (
                <div key={card.label} className="rounded-2xl border border-border bg-card p-5">
                  <p className="font-mono text-xs uppercase tracking-wider text-muted-foreground">
                    {card.label}
                  </p>
                  <p className="mt-2 font-heading text-3xl font-bold">{card.value}</p>
                </div>
              ))}
            </div>
          )}

          {tab === "users" && (
            <div className="rounded-2xl border border-border bg-card">
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Name</TableHead>
                    <TableHead>Username</TableHead>
                    <TableHead>Email</TableHead>
                    <TableHead>Role</TableHead>
                    <TableHead>Recipes</TableHead>
                    <TableHead />
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {(users.data ?? []).map((row) => (
                    <TableRow key={row.id} data-testid="admin-user-row">
                      <TableCell className="font-medium">{row.name}</TableCell>
                      <TableCell className="font-mono text-xs">@{row.username}</TableCell>
                      <TableCell className="text-xs">{row.email}</TableCell>
                      <TableCell>
                        <Badge variant={row.role === "admin" ? "default" : "secondary"}>{row.role}</Badge>
                      </TableCell>
                      <TableCell>{row.recipesCount}</TableCell>
                      <TableCell className="text-right">
                        {row.id !== me?.id && (
                          <Button
                            variant="ghost"
                            size="icon-sm"
                            aria-label={`Delete ${row.username}`}
                            data-testid="admin-delete-user-btn"
                            onClick={() => deleteUser.mutate(row.id)}
                          >
                            <Trash2 className="size-4" />
                          </Button>
                        )}
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </div>
          )}

          {tab === "recipes" && (
            <div className="rounded-2xl border border-border bg-card">
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Title</TableHead>
                    <TableHead>Author</TableHead>
                    <TableHead>Status</TableHead>
                    <TableHead>Views</TableHead>
                    <TableHead>Rating</TableHead>
                    <TableHead />
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {(recipes.data ?? []).map((row) => (
                    <TableRow key={row.id} data-testid="admin-recipe-row">
                      <TableCell className="font-medium">{row.title}</TableCell>
                      <TableCell className="font-mono text-xs">@{row.authorUsername}</TableCell>
                      <TableCell>
                        <Badge variant="secondary">{row.status}</Badge>
                      </TableCell>
                      <TableCell>{row.views}</TableCell>
                      <TableCell>{row.avgRating?.toFixed(1) ?? "—"}</TableCell>
                      <TableCell className="text-right">
                        <Button
                          variant="ghost"
                          size="icon-sm"
                          aria-label={`Delete ${row.title}`}
                          data-testid="admin-delete-recipe-btn"
                          onClick={() => deleteRecipe.mutate(row.id)}
                        >
                          <Trash2 className="size-4" />
                        </Button>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </div>
          )}

          {tab === "comments" && (
            <ul className="space-y-3" data-testid="admin-comment-list">
              {(comments.data ?? []).map((comment) => (
                <li
                  key={comment.id}
                  className="flex items-start gap-4 rounded-2xl border border-border bg-card p-4"
                  data-testid="admin-comment-row"
                >
                  <div className="flex-1">
                    <p className="text-sm font-semibold">
                      @{comment.username}
                      <span className="ml-2 text-xs font-normal text-muted-foreground">
                        {timeAgo(comment.createdAt)}
                      </span>
                    </p>
                    <p className="mt-1 text-sm">{comment.text}</p>
                  </div>
                  <Button
                    variant="ghost"
                    size="icon-sm"
                    aria-label="Delete comment"
                    data-testid="admin-delete-comment-btn"
                    onClick={() => deleteComment.mutate(comment.id)}
                  >
                    <Trash2 className="size-4" />
                  </Button>
                </li>
              ))}
              {comments.data?.length === 0 && (
                <li className="rounded-2xl border border-dashed border-border p-10 text-center text-sm text-muted-foreground">
                  No comments to moderate.
                </li>
              )}
            </ul>
          )}

          {tab === "reports" && (
            <ul className="space-y-3" data-testid="admin-report-list">
              {(reports.data ?? []).map((report) => (
                <li
                  key={report.id}
                  className="flex items-start gap-4 rounded-2xl border border-border bg-card p-4"
                  data-testid="admin-report-row"
                >
                  <div className="flex-1">
                    <p className="text-sm font-semibold">
                      {report.targetType}: {report.targetTitle ?? report.targetId}
                    </p>
                    <p className="mt-1 text-sm text-muted-foreground">{report.reason}</p>
                    <p className="mt-1 text-xs text-muted-foreground">
                      reported by @{report.reporterUsername} · {timeAgo(report.createdAt)}
                    </p>
                  </div>
                  <Button
                    variant="outline"
                    size="sm"
                    data-testid="admin-dismiss-report-btn"
                    onClick={() => dismissReport.mutate(report.id)}
                  >
                    Dismiss
                  </Button>
                </li>
              ))}
              {reports.data?.length === 0 && (
                <li className="rounded-2xl border border-dashed border-border p-10 text-center text-sm text-muted-foreground">
                  No open reports. The kitchen is clean.
                </li>
              )}
            </ul>
          )}
        </div>
      </div>
    </PageShell>
  );
}
