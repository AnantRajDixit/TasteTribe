import { Routes, Route } from "react-router-dom";
import { Toaster } from "@/components/ui/sonner";
import Home from "@/pages/Home";
import Explore from "@/pages/Explore";
import Categories from "@/pages/Categories";
import Feed from "@/pages/Feed";
import RecipeDetail from "@/pages/RecipeDetail";
import RecipeBuilder from "@/pages/RecipeBuilder";
import Dashboard from "@/pages/Dashboard";
import ShoppingListPage from "@/pages/ShoppingListPage";
import ChefProfile from "@/pages/ChefProfile";
import MealPlanner from "@/pages/MealPlanner";
import AdminPanel from "@/pages/AdminPanel";
import { LoginPage, RegisterPage } from "@/pages/Auth";
import NotFound from "@/pages/NotFound";

// One <Route> per page in src/pages; BrowserRouter already wraps this in main.tsx.
export default function App() {
  return (
    <>
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/recipes" element={<Explore />} />
        <Route path="/recipes/new" element={<RecipeBuilder />} />
        <Route path="/recipes/:id" element={<RecipeDetail />} />
        <Route path="/recipes/:id/edit" element={<RecipeBuilder />} />
        <Route path="/categories" element={<Categories />} />
        <Route path="/feed" element={<Feed />} />
        <Route path="/dashboard" element={<Dashboard />} />
        <Route path="/shopping-list" element={<ShoppingListPage />} />
        <Route path="/meal-planner" element={<MealPlanner />} />
        <Route path="/chefs/:username" element={<ChefProfile />} />
        <Route path="/admin" element={<AdminPanel />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="*" element={<NotFound />} />
      </Routes>
      <Toaster richColors />
    </>
  );
}
