import { useRef, useState } from "react";
import { useMutation } from "@tanstack/react-query";
import { Upload, Link2, X, UserRound } from "lucide-react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { apiUpload } from "@/lib/api";
import { errorMessage } from "@/lib/format";
import type { UploadResponse } from "@/lib/types";

/**
 * Profile picture picker: upload straight from the device library (default)
 * or paste a URL. Uploads post multipart to /api/uploads/image.
 */
export default function AvatarPicker({
  value,
  onChange,
  label = "Profile picture",
}: {
  value: string;
  onChange: (next: string) => void;
  label?: string;
}) {
  const inputRef = useRef<HTMLInputElement>(null);
  const [mode, setMode] = useState<"upload" | "url">("upload");

  const upload = useMutation({
    mutationFn: (file: File) => apiUpload<UploadResponse>("/uploads/image", file),
    onSuccess: (data) => {
      onChange(data.url);
      toast.success("Profile picture uploaded");
    },
    onError: (error) => toast.error(errorMessage(error, "Could not upload that image.")),
  });

  const pick = (file?: File | null) => {
    if (!file) return;
    if (!file.type.startsWith("image/")) {
      toast.error("Please choose an image file.");
      return;
    }
    if (file.size > 8 * 1024 * 1024) {
      toast.error("That image is larger than 8 MB.");
      return;
    }
    upload.mutate(file);
  };

  return (
    <div className="space-y-3">
      <div className="flex items-center justify-between">
        <Label>{label}</Label>
        <div className="flex gap-1 rounded-lg bg-muted p-1">
          <button
            type="button"
            onClick={() => setMode("upload")}
            data-testid="avatar-mode-upload-btn"
            className={`rounded-md px-2.5 py-1 text-xs font-medium transition-colors duration-150 ${
              mode === "upload" ? "bg-background shadow-sm" : "text-muted-foreground"
            }`}
          >
            Upload
          </button>
          <button
            type="button"
            onClick={() => setMode("url")}
            data-testid="avatar-mode-url-btn"
            className={`rounded-md px-2.5 py-1 text-xs font-medium transition-colors duration-150 ${
              mode === "url" ? "bg-background shadow-sm" : "text-muted-foreground"
            }`}
          >
            URL
          </button>
        </div>
      </div>

      <div className="flex items-center gap-4">
        <div
          className="relative size-20 shrink-0 overflow-hidden rounded-full border border-border bg-muted"
          data-testid="avatar-preview"
        >
          {value ? (
            <img src={value} alt="Profile preview" className="size-full object-cover" />
          ) : (
            <span className="flex size-full items-center justify-center text-muted-foreground">
              <UserRound className="size-7" />
            </span>
          )}
          {value && (
            <button
              type="button"
              onClick={() => onChange("")}
              aria-label="Remove profile picture"
              data-testid="avatar-remove-btn"
              className="absolute right-0 top-0 rounded-bl-lg bg-background/90 p-1 text-foreground transition-opacity duration-150 hover:opacity-80"
            >
              <X className="size-3" />
            </button>
          )}
        </div>

        <div className="min-w-0 flex-1">
          {mode === "upload" ? (
            <>
              <input
                ref={inputRef}
                type="file"
                accept="image/*"
                className="hidden"
                data-testid="avatar-file-input"
                onChange={(event) => {
                  pick(event.target.files?.[0]);
                  event.target.value = "";
                }}
              />
              <Button
                type="button"
                variant="outline"
                onClick={() => inputRef.current?.click()}
                disabled={upload.isPending}
                data-testid="avatar-upload-btn"
              >
                <Upload className="mr-2 size-4" />
                {upload.isPending ? "Uploading…" : "Choose from device"}
              </Button>
              <p className="mt-2 text-xs text-muted-foreground">
                JPG, PNG, WEBP or GIF — up to 8 MB.
              </p>
            </>
          ) : (
            <div className="relative">
              <Link2 className="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
              <Input
                value={value}
                onChange={(event) => onChange(event.target.value)}
                placeholder="https://…"
                className="pl-9"
                data-testid="avatar-url-input"
              />
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
