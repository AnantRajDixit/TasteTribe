import { useRef, useState } from "react";
import { useMutation } from "@tanstack/react-query";
import { Upload, Link2, X, ImagePlus } from "lucide-react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { apiUpload } from "@/lib/api";
import { errorMessage } from "@/lib/format";
import type { UploadResponse } from "@/lib/types";

/**
 * Cover photo picker: upload straight from the device (default) or paste a URL.
 * The upload posts multipart to /api/uploads/image and stores the returned URL.
 */
export default function CoverPhotoPicker({
  value,
  onChange,
}: {
  value: string;
  onChange: (next: string) => void;
}) {
  const inputRef = useRef<HTMLInputElement>(null);
  const [mode, setMode] = useState<"upload" | "url">("upload");

  const upload = useMutation({
    mutationFn: (file: File) => apiUpload<UploadResponse>("/uploads/image", file),
    onSuccess: (data) => {
      onChange(data.url);
      toast.success("Cover photo uploaded");
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
        <Label>Cover photo</Label>
        <div className="flex gap-1 rounded-lg bg-muted p-1">
          <button
            type="button"
            onClick={() => setMode("upload")}
            data-testid="cover-mode-upload-btn"
            className={`rounded-md px-2.5 py-1 text-xs font-medium transition-colors duration-150 ${
              mode === "upload" ? "bg-background shadow-sm" : "text-muted-foreground"
            }`}
          >
            <Upload className="mr-1 inline size-3" /> Upload
          </button>
          <button
            type="button"
            onClick={() => setMode("url")}
            data-testid="cover-mode-url-btn"
            className={`rounded-md px-2.5 py-1 text-xs font-medium transition-colors duration-150 ${
              mode === "url" ? "bg-background shadow-sm" : "text-muted-foreground"
            }`}
          >
            <Link2 className="mr-1 inline size-3" /> URL
          </button>
        </div>
      </div>

      {value ? (
        <div className="relative overflow-hidden rounded-xl border border-border">
          <img
            src={value}
            alt="Recipe cover preview"
            className="aspect-[16/9] w-full object-cover"
            data-testid="cover-photo-preview"
          />
          <button
            type="button"
            aria-label="Remove cover photo"
            data-testid="cover-photo-remove-btn"
            onClick={() => onChange("")}
            className="absolute right-2 top-2 flex size-8 items-center justify-center rounded-full bg-background/90 backdrop-blur transition-transform duration-150 hover:scale-105"
          >
            <X className="size-4" />
          </button>
        </div>
      ) : mode === "upload" ? (
        <div
          onDragOver={(event) => event.preventDefault()}
          onDrop={(event) => {
            event.preventDefault();
            pick(event.dataTransfer.files?.[0]);
          }}
          className="flex flex-col items-center justify-center rounded-xl border-2 border-dashed border-border bg-muted/40 px-6 py-10 text-center transition-colors duration-150 hover:border-primary/60"
        >
          <ImagePlus className="size-8 text-muted-foreground" />
          <p className="mt-3 text-sm font-medium">
            {upload.isPending ? "Uploading…" : "Drag a photo here, or choose a file"}
          </p>
          <p className="mt-1 text-xs text-muted-foreground">JPG, PNG, WEBP or GIF · up to 8 MB</p>
          <Button
            type="button"
            variant="outline"
            className="mt-4"
            disabled={upload.isPending}
            onClick={() => inputRef.current?.click()}
            data-testid="cover-photo-choose-btn"
          >
            <Upload className="mr-2 size-4" /> Choose photo
          </Button>
          <input
            ref={inputRef}
            type="file"
            accept="image/*"
            className="hidden"
            data-testid="cover-photo-file-input"
            onChange={(event) => pick(event.target.files?.[0])}
          />
        </div>
      ) : (
        <Input
          value={value}
          onChange={(event) => onChange(event.target.value)}
          placeholder="https://…"
          data-testid="recipe-cover-input"
        />
      )}
    </div>
  );
}
