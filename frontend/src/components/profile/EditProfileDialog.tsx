import { useState } from "react";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { Pencil } from "lucide-react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import AvatarPicker from "@/components/profile/AvatarPicker";
import { apiPatch } from "@/lib/api";
import { errorMessage } from "@/lib/format";
import type { User } from "@/lib/types";
import { ME_KEY } from "@/lib/session";

/** Edit name / bio / profile picture — PATCH /api/auth/profile. */
export default function EditProfileDialog({ me }: { me: User }) {
  const [open, setOpen] = useState(false);
  const [name, setName] = useState(me.name);
  const [bio, setBio] = useState(me.bio ?? "");
  const [avatarUrl, setAvatarUrl] = useState(me.avatarUrl ?? "");
  const queryClient = useQueryClient();

  const save = useMutation({
    mutationFn: () =>
      apiPatch<User>("/auth/profile", {
        name: name.trim(),
        bio: bio.trim(),
        avatarUrl: avatarUrl.trim() || null,
      }),
    onSuccess: () => {
      toast.success("Profile updated");
      queryClient.invalidateQueries({ queryKey: ME_KEY });
      queryClient.invalidateQueries({ queryKey: ["profile"] });
      setOpen(false);
    },
    onError: (error) => toast.error(errorMessage(error, "Could not update your profile.")),
  });

  return (
    <Dialog
      open={open}
      onOpenChange={(next) => {
        setOpen(next);
        if (next) {
          setName(me.name);
          setBio(me.bio ?? "");
          setAvatarUrl(me.avatarUrl ?? "");
        }
      }}
    >
      <Button variant="outline" onClick={() => setOpen(true)} data-testid="edit-profile-open-btn">
        <Pencil className="mr-2 size-4" /> Edit profile
      </Button>
      <DialogContent data-testid="edit-profile-dialog">
        <DialogHeader>
          <DialogTitle>Edit profile</DialogTitle>
          <DialogDescription>
            Upload a photo from your device library and update your details.
          </DialogDescription>
        </DialogHeader>

        <div className="space-y-5">
          <AvatarPicker value={avatarUrl} onChange={setAvatarUrl} />
          <div className="space-y-2">
            <Label htmlFor="profile-name">Name</Label>
            <Input
              id="profile-name"
              value={name}
              onChange={(event) => setName(event.target.value)}
              data-testid="edit-profile-name-input"
            />
          </div>
          <div className="space-y-2">
            <Label htmlFor="profile-bio">Bio</Label>
            <Textarea
              id="profile-bio"
              value={bio}
              rows={3}
              placeholder="Tell the community what you cook."
              onChange={(event) => setBio(event.target.value)}
              data-testid="edit-profile-bio-input"
            />
          </div>
        </div>

        <DialogFooter>
          <Button
            onClick={() => save.mutate()}
            disabled={save.isPending || name.trim().length < 2}
            data-testid="edit-profile-save-btn"
          >
            {save.isPending ? "Saving…" : "Save changes"}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
