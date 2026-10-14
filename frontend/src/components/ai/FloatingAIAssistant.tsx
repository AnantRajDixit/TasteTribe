import { useState, useRef, useEffect } from "react";
import { useMutation } from "@tanstack/react-query";
import { useNavigate } from "react-router-dom";
import { ChefHat, Send, X, Sparkles, Replace } from "lucide-react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { apiPost } from "@/lib/api";
import { useMe } from "@/lib/session";
import { errorMessage } from "@/lib/format";
import type { ChatReply } from "@/lib/types";

interface Bubble {
  role: "user" | "assistant";
  content: string;
}

const QUICK_PROMPTS = [
  "I have potatoes, onions and eggs. What can I cook?",
  "Suggest a high-protein vegetarian dinner",
  "Can I replace butter with oil?",
];

/** Stable per-tab conversation id so history persists across navigation. */
function sessionId(): string {
  const key = "tt_ai_session";
  let value = sessionStorage.getItem(key);
  if (!value) {
    value = `sess_${Math.random().toString(36).slice(2)}${Date.now().toString(36)}`;
    sessionStorage.setItem(key, value);
  }
  return value;
}

export default function FloatingAIAssistant() {
  const { data: me } = useMe();
  const navigate = useNavigate();
  const [open, setOpen] = useState(false);
  const [draft, setDraft] = useState("");
  const [bubbles, setBubbles] = useState<Bubble[]>([
    {
      role: "assistant",
      content:
        "Hi! I'm your TasteTribe sous-chef. Tell me what's in your kitchen, ask for a substitution, or get help scaling a recipe.",
    },
  ]);
  const scrollRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    scrollRef.current?.scrollTo({ top: scrollRef.current.scrollHeight, behavior: "smooth" });
  }, [bubbles, open]);

  const chat = useMutation({
    mutationFn: (message: string) =>
      apiPost<ChatReply>("/ai/chat", { message, sessionId: sessionId() }),
    onSuccess: (data) =>
      setBubbles((prev) => [...prev, { role: "assistant", content: data.reply }]),
    onError: (error) => {
      toast.error(errorMessage(error, "The assistant is unavailable right now."));
      setBubbles((prev) => [
        ...prev,
        { role: "assistant", content: "Sorry — I couldn't answer that just now. Please try again." },
      ]);
    },
  });

  const send = (message: string) => {
    const text = message.trim();
    if (!text || chat.isPending) return;
    if (!me) {
      toast.info("Log in to chat with the AI sous-chef.");
      setOpen(false);
      navigate("/login");
      return;
    }
    setBubbles((prev) => [...prev, { role: "user", content: text }]);
    setDraft("");
    chat.mutate(text);
  };

  return (
    <>
      {!open && (
        <button
          type="button"
          onClick={() => setOpen(true)}
          aria-label="Open AI cooking assistant"
          data-testid="floating-ai-assistant-btn"
          className="animate-pulse-ring fixed bottom-6 right-6 z-50 flex size-14 items-center justify-center rounded-full bg-primary text-primary-foreground shadow-lg transition-transform duration-200 hover:scale-105"
        >
          <ChefHat className="size-6" />
        </button>
      )}

      {open && (
        <div
          data-testid="ai-assistant-panel"
          className="animate-slide-up-panel fixed bottom-6 right-4 z-50 flex h-[32rem] w-[calc(100vw-2rem)] max-w-sm flex-col overflow-hidden rounded-2xl border border-border bg-card shadow-2xl sm:right-6"
        >
          <header className="flex items-center gap-3 bg-stone-900 px-4 py-3 text-stone-50">
            <span className="flex size-8 items-center justify-center rounded-lg bg-primary">
              <ChefHat className="size-4 text-primary-foreground" />
            </span>
            <div className="flex-1">
              <p className="text-sm font-semibold">AI Sous-Chef</p>
              <p className="text-xs text-stone-300">Cooking help, substitutions, ideas</p>
            </div>
            <button
              type="button"
              onClick={() => setOpen(false)}
              aria-label="Close AI assistant"
              data-testid="ai-close-btn"
              className="rounded-lg p-1.5 transition-colors duration-150 hover:bg-stone-700"
            >
              <X className="size-4" />
            </button>
          </header>

          <div ref={scrollRef} className="flex-1 space-y-3 overflow-y-auto p-4" data-testid="ai-chat-log">
            {bubbles.map((bubble, index) => (
              <div
                key={index}
                className={bubble.role === "user" ? "flex justify-end" : "flex justify-start"}
              >
                <p
                  data-testid={`ai-bubble-${bubble.role}`}
                  className={`max-w-[85%] whitespace-pre-wrap rounded-2xl px-3.5 py-2.5 text-sm leading-relaxed ${
                    bubble.role === "user"
                      ? "bg-primary text-primary-foreground"
                      : "bg-muted text-foreground"
                  }`}
                >
                  {bubble.content}
                </p>
              </div>
            ))}
            {chat.isPending && (
              <div className="flex justify-start" data-testid="ai-typing-indicator">
                <p className="rounded-2xl bg-muted px-3.5 py-2.5 text-sm text-muted-foreground">
                  Thinking through the pantry…
                </p>
              </div>
            )}
          </div>

          {bubbles.length <= 1 && (
            <div className="flex flex-wrap gap-2 border-t border-border px-4 py-3">
              {QUICK_PROMPTS.map((prompt, index) => (
                <button
                  key={prompt}
                  type="button"
                  onClick={() => send(prompt)}
                  data-testid={`ai-quick-prompt-${index}`}
                  className="rounded-full border border-border px-3 py-1.5 text-xs transition-colors duration-150 hover:border-primary hover:text-primary"
                >
                  {prompt}
                </button>
              ))}
            </div>
          )}

          <form
            onSubmit={(event) => {
              event.preventDefault();
              send(draft);
            }}
            className="flex items-center gap-2 border-t border-border p-3"
          >
            <Input
              value={draft}
              onChange={(event) => setDraft(event.target.value)}
              placeholder="Ask about any dish…"
              aria-label="Message the AI assistant"
              data-testid="ai-chat-input"
            />
            <Button
              type="submit"
              size="icon"
              disabled={chat.isPending || !draft.trim()}
              aria-label="Send message"
              data-testid="ai-chat-send-btn"
            >
              <Send className="size-4" />
            </Button>
          </form>
        </div>
      )}
    </>
  );
}
