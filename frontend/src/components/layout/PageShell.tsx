import type { ReactNode } from "react";
import Navbar from "@/components/layout/Navbar";
import Footer from "@/components/layout/Footer";
import FloatingAIAssistant from "@/components/ai/FloatingAIAssistant";

/**
 * Page shell. The nav, footer and AI button render unconditionally so a failed
 * data fetch degrades to a partial page, never a blank one.
 */
export default function PageShell({ children }: { children: ReactNode }) {
  return (
    <div className="relative flex min-h-svh flex-col">
      <Navbar />
      <main className="relative z-10 flex-1">{children}</main>
      <Footer />
      <FloatingAIAssistant />
    </div>
  );
}
