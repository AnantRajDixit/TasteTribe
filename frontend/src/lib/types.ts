// Hand-written TypeScript mirrors of the Java DTOs in
// java-backend/src/main/java/com/tastetribe/dto/*.java — nothing infers across the
// HTTP boundary, so these two sides are kept in sync by hand in the same edit.

export interface User {
  id: string;
  name: string;
  username: string;
  email?: string | null;
  avatarUrl?: string | null;
  bio: string;
  role: "user" | "admin";
  createdAt?: string | null;
}

export interface Profile extends User {
  followersCount: number;
  followingCount: number;
  recipesCount: number;
  avgRating?: number | null;
  isFollowing: boolean;
}

export interface FollowResponse {
  following: boolean;
  followersCount: number;
}

export interface Ingredient {
  name: string;
  quantity: number;
  unit: string;
  optional: boolean;
}

export interface Nutrition {
  calories?: number | null;
  protein?: number | null;
  carbs?: number | null;
  fat?: number | null;
}

export interface Recipe {
  id: string;
  title: string;
  description: string;
  coverImage?: string | null;
  cuisine: string;
  category: string;
  difficulty: string;
  dietary: string;
  prepTime: number;
  cookTime: number;
  totalTime: number;
  servings: number;
  ingredients: Ingredient[];
  instructions: string[];
  nutrition?: Nutrition | null;
  tags: string[];
  authorId: string;
  authorName: string;
  authorUsername: string;
  status: "draft" | "published";
  source: string;
  views: number;
  likesCount: number;
  favoritesCount: number;
  avgRating?: number | null;
  ratingsCount: number;
  likedByMe: boolean;
  favoritedByMe: boolean;
  createdAt?: string | null;
  updatedAt?: string | null;
}

export interface RecipePage {
  items: Recipe[];
  total: number;
  page: number;
  pages: number;
}

/** Request body for create/update — mirrors RecipeDtos.RecipeRequest. */
export interface RecipeInput {
  title: string;
  description: string;
  coverImage?: string | null;
  cuisine: string;
  category: string;
  difficulty: string;
  dietary: string;
  prepTime: number;
  cookTime: number;
  servings: number;
  ingredients: Ingredient[];
  instructions: string[];
  nutrition?: Nutrition | null;
  tags: string[];
  status: "draft" | "published";
}

export interface ScaledIngredient {
  name: string;
  quantity: number;
  unit: string;
  optional: boolean;
  display: string;
}

export interface ScaleResponse {
  recipeId: string;
  baseServings: number;
  servings: number;
  ingredients: ScaledIngredient[];
}

export interface Category {
  id: string;
  name: string;
  slug: string;
  description: string;
  imageUrl?: string | null;
  recipesCount: number;
}

export interface Review {
  id: string;
  recipeId: string;
  userId: string;
  username: string;
  name: string;
  avatarUrl?: string | null;
  rating: number;
  comment?: string | null;
  createdAt?: string | null;
  updatedAt?: string | null;
}

export interface Comment {
  id: string;
  recipeId: string;
  userId: string;
  username: string;
  name: string;
  avatarUrl?: string | null;
  text: string;
  createdAt?: string | null;
}

export interface ToggleResponse {
  active: boolean;
  count: number;
}

export interface ShoppingItem {
  id: string;
  name: string;
  quantity: number;
  unit: string;
  checked: boolean;
  recipeTitle?: string | null;
}

export interface ShoppingList {
  items: ShoppingItem[];
}

export interface SubstituteOption {
  name: string;
  ratio: string;
  taste: string;
  texture: string;
  temperature: string;
  quantity: string;
}

export interface SubstituteResponse {
  ingredient: string;
  summary: string;
  substitutes: SubstituteOption[];
}

export interface ChatReply {
  reply: string;
}

export interface AdminUserRow {
  id: string;
  name: string;
  username: string;
  email: string;
  role: string;
  recipesCount: number;
  createdAt?: string | null;
}

export interface AdminRecipeRow {
  id: string;
  title: string;
  authorUsername: string;
  status: string;
  views: number;
  avgRating?: number | null;
}

export interface AdminReportRow {
  id: string;
  targetType: string;
  targetId: string;
  reason: string;
  reporterUsername: string;
  status: string;
  targetTitle?: string | null;
  createdAt?: string | null;
}

export interface AdminStats {
  users: number;
  recipes: number;
  publishedRecipes: number;
  reviews: number;
  comments: number;
  pendingReports: number;
  recentUsers: AdminUserRow[];
  topRecipes: AdminRecipeRow[];
}

/** Mirrors MealPlanDtos.MealPlanEntryResponse. */
export interface MealPlanEntry {
  id: string;
  recipeId: string;
  recipeTitle: string;
  recipeImage?: string | null;
  recipeTotalTime: number;
  planDate: string;
  slot: MealSlot;
  servings: number;
}

export interface MealPlan {
  weekStart: string;
  entries: MealPlanEntry[];
}

export type MealSlot = "breakfast" | "lunch" | "dinner";

export const MEAL_SLOTS: Array<{ value: MealSlot; label: string }> = [
  { value: "breakfast", label: "Breakfast" },
  { value: "lunch", label: "Lunch" },
  { value: "dinner", label: "Dinner" },
];

/** Mirrors the {"url": "..."} body of POST /api/uploads/image. */
export interface UploadResponse {
  url: string;
}

export const CUISINES = [
  "Indian",
  "Italian",
  "Chinese",
  "Mexican",
  "Korean",
  "Japanese",
  "Thai",
  "American",
  "Continental",
] as const;

export const CATEGORY_NAMES = [
  "Breakfast",
  "Lunch",
  "Dinner",
  "Snacks",
  "Desserts",
  "Beverages",
  "Quick Meals",
  "High Protein",
] as const;

export const DIFFICULTIES = ["Easy", "Medium", "Hard"] as const;

export const DIETS = ["Vegetarian", "Non-Vegetarian", "Vegan"] as const;

export const UNITS = [
  "g",
  "kg",
  "ml",
  "l",
  "tsp",
  "tbsp",
  "cup",
  "pieces",
  "piece",
  "cloves",
  "pinch",
  "handful",
  "portions",
  "pods",
  "bunch",
] as const;
