import { useState } from "react";
import { useMutation } from "@tanstack/react-query";
import { useNavigate } from "react-router-dom";
import { Flag } from "lucide-react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Textarea } from "@/components/ui/textarea";
import {
  Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger,
} from "@/components/ui/dialog";
import { apiPost } from "@/lib/api";
import { useMe } from "@/lib/session";
import { errorMessage } from "@/lib/format";

const REASONS = [
  "Spam or advertising",
  "Offensive or abusive language",
  "Stolen or plagiarised content",
  "Unsafe cooking advice",
  "Something else",
];

/**
 * Report control for a recipe or a comment. Posts to /api/reports, which fills the
 * admin moderation queue at /admin?tab=reports.
 */
export default function ReportDialog({
  targetType,
  targetId,
  label,
  compact = false,
}: {
  targetType: "recipe" | "comment";
  targetId: string;
  label?: string;
  compact?: boolean;
}) {
  const { data: me } = useMe();
  const navigate = useNavigate();
  const [open, setOpen] = useState(false);
  const [reason, setReason] = useState(REASONS[0]);
  const [detail, setDetail] = useState("");

  const report = useMutation({
    mutationFn: () =>
      apiPost("/reports", {
        targetType,
        targetId,
        reason: detail.trim() ? `${reason} — ${detail.trim()}` : reason,
      }),
    onSuccess: () => {
      toast.success("Thanks — our moderators will take a look.");
      setOpen(false);
      setDetail("");
    },
    onError: (error) => toast.error(errorMessage(error, "Could not submit that report.")),
  });

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger
        aria-label={`Report this ${targetType}`}
        data-testid={`report-${targetType}-trigger-btn`}
        onClick={(event) => {
          if (!me) {
            event.preventDefault();
            toast.info("Log in to report content.");
            navigate("/login");
          }
        }}
        className={
          compact
            ? "inline-flex items-center gap-1 rounded-md p-1 text-xs text-muted-foreground transition-colors duration-150 hover:text-destructive"
            : "inline-flex items-center gap-1.5 rounded-lg border border-border px-3 py-2 text-sm text-muted-foreground transition-colors duration-150 hover:border-destructive hover:text-destructive"
        }
      >
        <Flag className={compact ? "size-3.5" : "size-4"} />
        {label ?? (compact ? "" : "Report")}
      </DialogTrigger>
      <DialogContent className="max-w-md">
        <DialogHeader>
          <DialogTitle className="font-heading">Report this {targetType}</DialogTitle>
        </DialogHeader>
        <div className="space-y-4">
          <fieldset className="space-y-2">
            <legend className="text-sm font-medium">What's wrong with it?</legend>
            {REASONS.map((item) => (
              <label key={item} className="flex cursor-pointer items-center gap-2.5 text-sm">
                <input
                  type="radio"
                  name="report-reason"
                  value={item}
                  checked={reason === item}
                  onChange={() => setReason(item)}
                  className="size-4 accent-orange-500"
                  data-testid="report-reason-radio"
                />
                {item}
              </label>
            ))}
          </fieldset>
          <Textarea
            value={detail}
            onChange={(event) => setDetail(event.target.value)}
            placeholder="Add any details that would help a moderator (optional)"
            rows={3}
            data-testid="report-detail-textarea"
          />
          <Button
            className="w-full"
            disabled={report.isPending}
            onClick={() => report.mutate()}
            data-testid="report-submit-btn"
          >
            {report.isPending ? "Sending…" : "Submit report"}
          </Button>
        </div>
      </DialogContent>
    </Dialog>
  );
}
