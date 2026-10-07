package com.tastetribe.config;

import com.tastetribe.dao.CategoryDao;
import com.tastetribe.dao.CommentDao;
import com.tastetribe.dao.FavoriteDao;
import com.tastetribe.dao.FollowDao;
import com.tastetribe.dao.LikeDao;
import com.tastetribe.dao.RecipeDao;
import com.tastetribe.dao.ReviewDao;
import com.tastetribe.dao.UserDao;
import com.tastetribe.model.Category;
import com.tastetribe.model.Comment;
import com.tastetribe.model.Difficulty;
import com.tastetribe.model.Nutrition;
import com.tastetribe.model.Recipe;
import com.tastetribe.model.RecipeIngredient;
import com.tastetribe.model.RecipeStatus;
import com.tastetribe.model.Review;
import com.tastetribe.model.Role;
import com.tastetribe.model.User;
import com.tastetribe.util.PasswordHasher;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Idempotent demo-data seeder. Runs once at startup and only when the users table is
 * empty, so the platform looks alive on a first visit (chefs, recipes, ratings,
 * comments, follows) without ever duplicating rows on a restart.
 */
@Component
public class DataSeeder implements ApplicationRunner {

    private final UserDao userDao;
    private final RecipeDao recipeDao;
    private final CategoryDao categoryDao;
    private final ReviewDao reviewDao;
    private final CommentDao commentDao;
    private final LikeDao likeDao;
    private final FavoriteDao favoriteDao;
    private final FollowDao followDao;
    private final PasswordHasher hasher;

    public DataSeeder(UserDao userDao, RecipeDao recipeDao, CategoryDao categoryDao, ReviewDao reviewDao,
                      CommentDao commentDao, LikeDao likeDao, FavoriteDao favoriteDao, FollowDao followDao,
                      PasswordHasher hasher) {
        this.userDao = userDao;
        this.recipeDao = recipeDao;
        this.categoryDao = categoryDao;
        this.reviewDao = reviewDao;
        this.commentDao = commentDao;
        this.likeDao = likeDao;
        this.favoriteDao = favoriteDao;
        this.followDao = followDao;
        this.hasher = hasher;
    }

    @Override
    public void run(ApplicationArguments args) {
        seedCategories();
        if (userDao.countAll() > 0) {
            return; // already seeded
        }
        List<User> chefs = seedUsers();
        List<Recipe> recipes = seedRecipes(chefs);
        seedCommunity(chefs, recipes);
    }

    // ---------- categories ----------

    private static final List<String[]> CATEGORY_SEED = List.of(
            new String[]{"Breakfast", "Bright morning plates and slow weekend brunches",
                    "https://images.unsplash.com/photo-1506084868230-bb9d95c24759?crop=entropy&cs=srgb&fm=jpg&q=85&w=800"},
            new String[]{"Lunch", "Midday meals that travel well",
                    "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?crop=entropy&cs=srgb&fm=jpg&q=85&w=800"},
            new String[]{"Dinner", "The centrepiece of the evening table",
                    "https://images.unsplash.com/photo-1585937421612-70a008356fbe?crop=entropy&cs=srgb&fm=jpg&q=85&w=800"},
            new String[]{"Snacks", "Small bites and street-food favourites",
                    "https://images.unsplash.com/photo-1599974579688-8dbdd335c77f?crop=entropy&cs=srgb&fm=jpg&q=85&w=800"},
            new String[]{"Desserts", "Bakes, puddings and sweet finishes",
                    "https://images.unsplash.com/photo-1557925923-33b27f891f88?crop=entropy&cs=srgb&fm=jpg&q=85&w=800"},
            new String[]{"Beverages", "Coolers, brews and everything in a glass",
                    "https://images.unsplash.com/photo-1512223792601-592a9809eed4?crop=entropy&cs=srgb&fm=jpg&q=85&w=800"},
            new String[]{"Quick Meals", "On the table in 30 minutes or less",
                    "https://images.unsplash.com/photo-1571175534150-72cd2b5a6039?crop=entropy&cs=srgb&fm=jpg&q=85&w=800"},
            new String[]{"High Protein", "Protein-forward plates for training days",
                    "https://images.unsplash.com/photo-1490645935967-10de6ba17061?crop=entropy&cs=srgb&fm=jpg&q=85&w=800"});

    private void seedCategories() {
        for (String[] row : CATEGORY_SEED) {
            String slug = row[0].toLowerCase().replaceAll("[^a-z0-9]+", "-");
            if (categoryDao.existsBySlug(slug)) {
                continue;
            }
            Category category = new Category();
            category.setName(row[0]);
            category.setSlug(slug);
            category.setDescription(row[1]);
            category.setImageUrl(row[2]);
            categoryDao.save(category);
        }
    }

    // ---------- users ----------

    private User chef(String name, String username, String email, String bio, String avatar, Role role) {
        User user = new User();
        user.setName(name);
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(hasher.hash(role == Role.ADMIN ? "admin1234" : "taste1234"));
        user.setBio(bio);
        user.setAvatarUrl(avatar);
        user.setRole(role);
        userDao.save(user);
        return user;
    }

    private List<User> seedUsers() {
        List<User> chefs = new ArrayList<>();
        chefs.add(chef("Priya Raghavan", "priya", "priya@tastetribe.dev",
                "Home cook from Chennai. Chasing the perfect dosa batter and the slowest biryani.",
                "https://images.unsplash.com/photo-1544005313-94ddf0286df2?crop=faces&cs=srgb&fm=jpg&q=85&w=400&h=400&fit=crop",
                Role.USER));
        chefs.add(chef("Marco Bellini", "marco", "marco@tastetribe.dev",
                "Third-generation pasta maker. Flour, eggs, patience — nothing else.",
                "https://images.unsplash.com/photo-1656338997878-279d71d48f6e?crop=faces&cs=srgb&fm=jpg&q=85&w=400&h=400&fit=crop",
                Role.USER));
        chefs.add(chef("Hana Kim", "hana", "hana@tastetribe.dev",
                "Seoul-born, Berlin-based. Fermentation, banchan and very good noodles.",
                "https://images.unsplash.com/photo-1607569708758-0270aa4651bd?crop=faces&cs=srgb&fm=jpg&q=85&w=400&h=400&fit=crop",
                Role.USER));
        chefs.add(chef("Diego Alvarez", "diego", "diego@tastetribe.dev",
                "Taquero at heart. If it can be grilled and wrapped in a tortilla, I've tried it.",
                "https://images.unsplash.com/flagged/photo-1557581462-0bf3e5907811?crop=faces&cs=srgb&fm=jpg&q=85&w=400&h=400&fit=crop",
                Role.USER));
        chefs.add(chef("Amelia Fox", "amelia", "amelia@tastetribe.dev",
                "Pastry-obsessed. Butter is a food group and I will defend that.",
                "https://images.unsplash.com/photo-1604072366595-e75dc92d6bdc?crop=faces&cs=srgb&fm=jpg&q=85&w=400&h=400&fit=crop",
                Role.USER));
        chefs.add(chef("Platform Admin", "admin", "admin@tastetribe.dev",
                "Keeping the TasteTribe kitchen tidy.",
                "https://images.unsplash.com/photo-1597651711127-600d0c2e78b0?crop=faces&cs=srgb&fm=jpg&q=85&w=400&h=400&fit=crop",
                Role.ADMIN));
        return chefs;
    }

    // ---------- recipes ----------

    private RecipeIngredient ing(String name, double quantity, String unit, boolean optional) {
        return new RecipeIngredient(name, quantity, unit, optional);
    }

    private Recipe recipe(User author, String title, String description, String cover, String cuisine,
                          String category, Difficulty difficulty, String dietary, int prep, int cook,
                          int servings, List<RecipeIngredient> ingredients, List<String> steps,
                          Nutrition nutrition, List<String> tags, int views, int daysAgo) {
        Recipe recipe = new Recipe();
        recipe.setTitle(title);
        recipe.setDescription(description);
        recipe.setCoverImage(cover);
        recipe.setCuisine(cuisine);
        recipe.setCategory(category);
        recipe.setDifficulty(difficulty);
        recipe.setDietary(dietary);
        recipe.setPrepTime(prep);
        recipe.setCookTime(cook);
        recipe.setTotalTime(prep + cook);
        recipe.setServings(servings);
        recipe.setIngredients(ingredients);
        recipe.setInstructions(steps);
        recipe.setNutrition(nutrition);
        recipe.setTags(tags);
        recipe.setStatus(RecipeStatus.PUBLISHED);
        recipe.setSource("user");
        recipe.setViews(views);
        recipe.setAuthorId(author.getId());
        recipe.setAuthorName(author.getName());
        recipe.setAuthorUsername(author.getUsername());
        recipe.setCreatedAt(LocalDateTime.now().minusDays(daysAgo));
        recipe.setUpdatedAt(LocalDateTime.now().minusDays(daysAgo));
        recipeDao.save(recipe);
        return recipe;
    }

    private List<Recipe> seedRecipes(List<User> chefs) {
        User priya = chefs.get(0);
        User marco = chefs.get(1);
        User hana = chefs.get(2);
        User diego = chefs.get(3);
        User amelia = chefs.get(4);
        List<Recipe> all = new ArrayList<>();

        all.add(recipe(priya, "Slow-Simmered Butter Chicken",
                "The restaurant classic, built properly: charred marinated thighs folded into a glossy tomato-cashew gravy finished with cold butter.",
                "https://images.unsplash.com/photo-1606471191009-63994c53433b?crop=entropy&cs=srgb&fm=jpg&q=85&w=1200",
                "Indian", "Dinner", Difficulty.MEDIUM, "Non-Vegetarian", 30, 45, 4,
                List.of(ing("Chicken thighs", 800, "g", false), ing("Greek yoghurt", 150, "g", false),
                        ing("Ginger garlic paste", 2, "tbsp", false), ing("Kashmiri chilli powder", 2, "tsp", false),
                        ing("Tomatoes", 6, "pieces", false), ing("Cashews", 60, "g", false),
                        ing("Butter", 80, "g", false), ing("Cream", 100, "ml", false),
                        ing("Garam masala", 1, "tsp", false), ing("Kasuri methi", 1, "tbsp", true)),
                List.of("Marinate the chicken with yoghurt, ginger garlic paste, chilli powder and salt for at least 30 minutes.",
                        "Sear the chicken in a hot pan until charred at the edges but not cooked through. Set aside.",
                        "Simmer tomatoes with cashews and a splash of water for 15 minutes until collapsing, then blend smooth and pass through a sieve.",
                        "Melt butter in the same pan, add the tomato-cashew purée and cook until it darkens and the fat separates.",
                        "Return the chicken with any resting juices, cover and simmer 12 minutes until tender.",
                        "Stir in cream, garam masala and crushed kasuri methi. Rest 5 minutes before serving with naan or rice."),
                new Nutrition(640.0, 42.0, 18.0, 44.0),
                List.of("curry", "comfort food", "restaurant style"), 1840, 12));

        all.add(recipe(priya, "Hyderabadi Chicken Dum Biryani",
                "Layered long-grain rice and spiced chicken sealed and steamed so every grain stays separate and perfumed.",
                "https://images.unsplash.com/photo-1563379091339-03b21ab4a4f8?crop=entropy&cs=srgb&fm=jpg&q=85&w=1200",
                "Indian", "Dinner", Difficulty.HARD, "Non-Vegetarian", 45, 60, 6,
                List.of(ing("Basmati rice", 750, "g", false), ing("Chicken", 1, "kg", false),
                        ing("Yoghurt", 250, "g", false), ing("Fried onions", 200, "g", false),
                        ing("Mint leaves", 1, "cup", false), ing("Biryani masala", 3, "tbsp", false),
                        ing("Saffron", 1, "pinch", false), ing("Ghee", 100, "g", false),
                        ing("Green chillies", 6, "pieces", true)),
                List.of("Marinate the chicken with yoghurt, fried onions, mint, biryani masala and salt for 2 hours.",
                        "Soak the rice 30 minutes, then parboil in heavily salted water with whole spices until 70% cooked. Drain.",
                        "Layer the marinated chicken in a heavy pot, top with the rice, remaining fried onions, saffron milk and ghee.",
                        "Seal the lid with dough or foil and cook on high for 5 minutes, then on the lowest heat for 35 minutes.",
                        "Rest sealed for 15 minutes. Open at the table and fold gently from the bottom up."),
                new Nutrition(780.0, 38.0, 92.0, 26.0),
                List.of("biryani", "festive", "one pot"), 2410, 20));

        all.add(recipe(marco, "Cacio e Pepe, Done Right",
                "Four ingredients, zero cream, no broken sauce: a lesson in emulsifying pecorino with starchy pasta water.",
                "https://images.unsplash.com/photo-1571175534150-72cd2b5a6039?crop=entropy&cs=srgb&fm=jpg&q=85&w=1200",
                "Italian", "Quick Meals", Difficulty.MEDIUM, "Vegetarian", 5, 15, 2,
                List.of(ing("Tonnarelli or spaghetti", 200, "g", false),
                        ing("Pecorino Romano", 120, "g", false),
                        ing("Black peppercorns", 2, "tsp", false), ing("Sea salt", 1, "tbsp", false)),
                List.of("Toast coarsely cracked peppercorns in a dry pan until fragrant, then add a ladle of water to make a pepper tea.",
                        "Cook the pasta in deliberately under-salted water so the cheese can season the dish.",
                        "Grate the pecorino very finely and mix with a little cool pasta water into a thick paste.",
                        "Drain the pasta two minutes early and finish it in the pepper pan with a splash of its water.",
                        "Off the heat, vigorously toss in the cheese paste until glossy. Serve immediately in warm bowls."),
                new Nutrition(520.0, 22.0, 64.0, 19.0),
                List.of("pasta", "4 ingredients", "weeknight"), 3120, 5));

        all.add(recipe(marco, "Hand-Rolled Spinach Ricotta Ravioli",
                "A Sunday project worth every minute: silky egg pasta around a bright, lemony ricotta filling.",
                "https://images.unsplash.com/photo-1587740908075-9e245070dfaa?crop=entropy&cs=srgb&fm=jpg&q=85&w=1200",
                "Italian", "Dinner", Difficulty.HARD, "Vegetarian", 60, 10, 4,
                List.of(ing("00 flour", 400, "g", false), ing("Eggs", 4, "pieces", false),
                        ing("Ricotta", 400, "g", false), ing("Spinach", 300, "g", false),
                        ing("Parmesan", 80, "g", false), ing("Lemon zest", 1, "piece", false),
                        ing("Nutmeg", 1, "pinch", true), ing("Butter", 60, "g", false),
                        ing("Sage leaves", 10, "pieces", true)),
                List.of("Make a well with the flour, add eggs and knead 10 minutes until smooth and elastic. Rest wrapped for 30 minutes.",
                        "Wilt the spinach, squeeze it bone dry and chop finely. Fold through ricotta, parmesan, lemon zest, nutmeg and salt.",
                        "Roll the dough to the second-thinnest pasta setting in long sheets.",
                        "Pipe walnut-sized mounds of filling, brush around them with water, lay a second sheet over and seal out all air before cutting.",
                        "Boil in generously salted water for 3 minutes.",
                        "Foam butter with sage until nut-brown and spoon over the drained ravioli."),
                new Nutrition(690.0, 29.0, 71.0, 31.0),
                List.of("fresh pasta", "weekend project", "vegetarian"), 1460, 9));

        all.add(recipe(hana, "Tonkotsu-Style Shoyu Ramen",
                "A weeknight-honest ramen: deeply seasoned broth, jammy marinated eggs and noodles with real bite.",
                "https://images.unsplash.com/photo-1753525808061-b42dfdd8aa41?crop=entropy&cs=srgb&fm=jpg&q=85&w=1200",
                "Japanese", "Dinner", Difficulty.MEDIUM, "Non-Vegetarian", 25, 50, 2,
                List.of(ing("Ramen noodles", 2, "portions", false), ing("Pork belly", 300, "g", false),
                        ing("Chicken stock", 1, "l", false), ing("Soy sauce", 80, "ml", false),
                        ing("Mirin", 40, "ml", false), ing("Eggs", 2, "pieces", false),
                        ing("Spring onions", 3, "pieces", false), ing("Nori sheets", 2, "pieces", true),
                        ing("Garlic", 4, "cloves", false)),
                List.of("Simmer the pork belly in stock with garlic for 40 minutes until a chopstick slides through easily.",
                        "Mix soy sauce and mirin; marinate 6½-minute boiled eggs in it for at least an hour.",
                        "Slice and briefly sear the pork to crisp the edges.",
                        "Season the hot broth with the remaining marinade until it tastes assertively savoury.",
                        "Cook the noodles to the low end of the package time and drain hard.",
                        "Build the bowl: broth, noodles, pork, halved egg, spring onion and nori."),
                new Nutrition(720.0, 44.0, 68.0, 29.0),
                List.of("ramen", "noodles", "broth"), 2680, 7));

        all.add(recipe(hana, "Crunchy Kimchi Fried Rice",
                "The best possible use of leftover rice and over-fermented kimchi, finished with a runny fried egg.",
                "https://images.unsplash.com/photo-1591814468924-caf88d1232e1?crop=entropy&cs=srgb&fm=jpg&q=85&w=1200",
                "Korean", "Quick Meals", Difficulty.EASY, "Vegetarian", 10, 12, 2,
                List.of(ing("Day-old cooked rice", 500, "g", false), ing("Aged kimchi", 200, "g", false),
                        ing("Kimchi brine", 3, "tbsp", false), ing("Gochujang", 1, "tbsp", false),
                        ing("Sesame oil", 2, "tsp", false), ing("Eggs", 2, "pieces", false),
                        ing("Spring onions", 2, "pieces", false), ing("Toasted sesame seeds", 1, "tsp", true)),
                List.of("Chop the kimchi and fry it in a very hot oiled pan for 3 minutes until the edges caramelise.",
                        "Stir in gochujang and the kimchi brine and cook 1 minute more.",
                        "Add the cold rice, breaking up clumps, and press it into the pan to build a crust.",
                        "Toss through sesame oil and spring onions off the heat.",
                        "Fry the eggs with crisp edges and runny yolks and set one on each bowl."),
                new Nutrition(480.0, 14.0, 72.0, 15.0),
                List.of("15 minutes", "leftovers", "spicy"), 1990, 3));

        all.add(recipe(diego, "Tacos al Pastor",
                "Chilli-and-pineapple marinated pork, griddled hard and piled into warm corn tortillas.",
                "https://images.unsplash.com/photo-1599974579688-8dbdd335c77f?crop=entropy&cs=srgb&fm=jpg&q=85&w=1200",
                "Mexican", "Dinner", Difficulty.MEDIUM, "Non-Vegetarian", 30, 25, 4,
                List.of(ing("Pork shoulder", 800, "g", false), ing("Guajillo chillies", 4, "pieces", false),
                        ing("Achiote paste", 2, "tbsp", false), ing("Pineapple", 300, "g", false),
                        ing("White onion", 1, "piece", false), ing("Corn tortillas", 12, "pieces", false),
                        ing("Coriander", 1, "bunch", false), ing("Limes", 2, "pieces", false)),
                List.of("Rehydrate the chillies in hot water, then blend with achiote, a quarter of the pineapple, garlic and vinegar.",
                        "Slice the pork thinly, coat in the marinade and refrigerate at least 4 hours.",
                        "Sear the pork in batches on a screaming-hot griddle so it chars rather than steams.",
                        "Char the remaining pineapple alongside and dice it.",
                        "Warm the tortillas directly over the flame until pliable and spotted.",
                        "Fill with pork, pineapple, finely diced onion and coriander. Serve with lime."),
                new Nutrition(560.0, 36.0, 44.0, 24.0),
                List.of("tacos", "street food", "grill"), 2240, 11));

        all.add(recipe(diego, "Charred Salsa Verde",
                "Blistered tomatillos and serranos blitzed into a sharp, smoky salsa that improves everything it touches.",
                "https://images.unsplash.com/photo-1552332386-f8dd00dc2f85?crop=entropy&cs=srgb&fm=jpg&q=85&w=1200",
                "Mexican", "Snacks", Difficulty.EASY, "Vegan", 10, 10, 6,
                List.of(ing("Tomatillos", 500, "g", false), ing("Serrano chillies", 3, "pieces", false),
                        ing("White onion", 0.5, "piece", false), ing("Garlic", 2, "cloves", false),
                        ing("Coriander", 1, "handful", false), ing("Lime", 1, "piece", false),
                        ing("Salt", 1, "tsp", false)),
                List.of("Dry-roast tomatillos, chillies, onion and unpeeled garlic in a hot pan until blackened in patches.",
                        "Peel the garlic and tip everything into a blender with coriander and salt.",
                        "Pulse to a coarse purée — texture matters more than smoothness.",
                        "Finish with lime juice and adjust salt. Keeps 5 days refrigerated."),
                new Nutrition(45.0, 2.0, 8.0, 1.0),
                List.of("salsa", "vegan", "5 ingredients"), 980, 2));

        all.add(recipe(amelia, "Raspberry Cloud Layer Cake",
                "A tender vanilla sponge with mascarpone cream and a mountain of fresh raspberries.",
                "https://images.unsplash.com/photo-1557925923-33b27f891f88?crop=entropy&cs=srgb&fm=jpg&q=85&w=1200",
                "Continental", "Desserts", Difficulty.MEDIUM, "Vegetarian", 35, 30, 8,
                List.of(ing("Plain flour", 300, "g", false), ing("Caster sugar", 280, "g", false),
                        ing("Butter", 220, "g", false), ing("Eggs", 4, "pieces", false),
                        ing("Mascarpone", 400, "g", false), ing("Double cream", 300, "ml", false),
                        ing("Raspberries", 400, "g", false), ing("Vanilla extract", 2, "tsp", false),
                        ing("Baking powder", 2, "tsp", false)),
                List.of("Cream the soft butter and sugar for a full 5 minutes until pale and aerated.",
                        "Add the eggs one at a time with a spoon of flour between each to stop the batter splitting.",
                        "Fold in the remaining flour and baking powder, then divide between two lined tins.",
                        "Bake at 175°C for 28-32 minutes until a skewer comes out clean. Cool completely.",
                        "Whip mascarpone, cream and vanilla to soft, scoopable peaks — stop before it stiffens.",
                        "Layer the sponges with cream and raspberries, finishing with fruit on top."),
                new Nutrition(610.0, 9.0, 62.0, 37.0),
                List.of("cake", "baking", "celebration"), 2890, 6));

        all.add(recipe(amelia, "Brown Butter Blueberry Pancakes",
                "Thick, fluffy pancakes with nutty brown butter in the batter and berries that burst as they cook.",
                "https://images.unsplash.com/photo-1506084868230-bb9d95c24759?crop=entropy&cs=srgb&fm=jpg&q=85&w=1200",
                "American", "Breakfast", Difficulty.EASY, "Vegetarian", 10, 15, 4,
                List.of(ing("Plain flour", 250, "g", false), ing("Buttermilk", 350, "ml", false),
                        ing("Butter", 70, "g", false), ing("Eggs", 2, "pieces", false),
                        ing("Caster sugar", 2, "tbsp", false), ing("Baking powder", 2, "tsp", false),
                        ing("Bicarbonate of soda", 0.5, "tsp", false), ing("Blueberries", 200, "g", false),
                        ing("Maple syrup", 4, "tbsp", true)),
                List.of("Melt the butter and keep cooking it until the milk solids smell nutty and turn amber. Cool slightly.",
                        "Whisk the dry ingredients in one bowl, the buttermilk, eggs and brown butter in another.",
                        "Combine with as few strokes as possible — a lumpy batter makes tall pancakes. Rest 10 minutes.",
                        "Ladle onto a medium pan, scatter blueberries over each pancake and cook until bubbles pop on top.",
                        "Flip once and cook 90 seconds more. Serve stacked with maple syrup."),
                new Nutrition(430.0, 12.0, 58.0, 16.0),
                List.of("breakfast", "fluffy", "family"), 2150, 1));

        all.add(recipe(hana, "Sesame Soy Protein Bowl",
                "A cold-prep bowl built for training days: marinated tofu, edamame, quinoa and a punchy sesame dressing.",
                "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?crop=entropy&cs=srgb&fm=jpg&q=85&w=1200",
                "Korean", "High Protein", Difficulty.EASY, "Vegan", 20, 15, 2,
                List.of(ing("Firm tofu", 400, "g", false), ing("Quinoa", 180, "g", false),
                        ing("Edamame", 200, "g", false), ing("Cucumber", 1, "piece", false),
                        ing("Soy sauce", 4, "tbsp", false), ing("Tahini", 2, "tbsp", false),
                        ing("Rice vinegar", 2, "tbsp", false), ing("Sesame oil", 2, "tsp", false),
                        ing("Chilli flakes", 1, "tsp", true)),
                List.of("Press the tofu 15 minutes, cube it and marinate in half the soy sauce.",
                        "Cook the quinoa in well-salted water, then spread on a tray to cool and stay fluffy.",
                        "Roast or air-fry the tofu at 200°C for 15 minutes until the edges are firm.",
                        "Whisk tahini, rice vinegar, sesame oil and the remaining soy into a pourable dressing.",
                        "Assemble quinoa, edamame, ribboned cucumber and tofu, then spoon the dressing over."),
                new Nutrition(540.0, 34.0, 48.0, 22.0),
                List.of("high protein", "meal prep", "vegan"), 1620, 4));

        all.add(recipe(priya, "Masala Chai, Properly Brewed",
                "Not a teabag in warm milk: whole spices bruised fresh and boiled hard with strong assam.",
                "https://images.unsplash.com/photo-1631452180539-96aca7d48617?crop=entropy&cs=srgb&fm=jpg&q=85&w=1200",
                "Indian", "Beverages", Difficulty.EASY, "Vegetarian", 5, 10, 2,
                List.of(ing("Water", 300, "ml", false), ing("Whole milk", 200, "ml", false),
                        ing("Assam tea leaves", 2, "tsp", false), ing("Green cardamom", 4, "pods", false),
                        ing("Fresh ginger", 15, "g", false), ing("Cloves", 2, "pieces", true),
                        ing("Sugar", 2, "tsp", false)),
                List.of("Bruise the cardamom and ginger in a mortar — never use ground spice here.",
                        "Boil the water with the spices for 3 minutes to build a concentrated infusion.",
                        "Add the tea leaves and boil 2 minutes until dark and tannic.",
                        "Pour in the milk and sugar and bring to a rolling boil, pulling the pan back each time it rises.",
                        "Strain from a height into cups to aerate it."),
                new Nutrition(120.0, 4.0, 18.0, 4.0),
                List.of("chai", "10 minutes", "comfort"), 1340, 8));

        return all;
    }

    // ---------- community: ratings, comments, likes, follows ----------

    private static final List<String> REVIEW_TEXTS = List.of(
            "Made this twice in a week. The timings are spot on and nothing was wasted.",
            "Very easy to follow and tasted amazing. The resting step genuinely matters.",
            "Great base recipe — I cut the chilli by half for the kids and it still sang.",
            "This is now my default dinner-party dish. Zero stress, big reaction.",
            "The technique notes saved me. First time my sauce didn't split.",
            "Solid weeknight winner. Took me about 10 minutes longer than stated, worth it.");

    private static final List<String> COMMENT_TEXTS = List.of(
            "Any suggestions for making this dairy-free?",
            "Doubled it for a crowd and it scaled perfectly.",
            "The shopping list feature made this so easy to prep.",
            "Adding this to my weekend list immediately.",
            "Used the serving scaler for 8 people — quantities were bang on.");

    private void seedCommunity(List<User> chefs, List<Recipe> recipes) {
        // Follows: everyone follows the two most prolific chefs, plus a few cross-follows.
        for (User user : chefs) {
            for (User target : chefs) {
                if (!user.getId().equals(target.getId()) && Math.random() < 0.55) {
                    followDao.follow(user.getId(), target.getId());
                }
            }
        }

        int seed = 0;
        for (Recipe recipe : recipes) {
            // 2-4 reviews per recipe from chefs who are not the author.
            int reviewCount = 2 + (seed % 3);
            int added = 0;
            for (User reviewer : chefs) {
                if (added >= reviewCount || reviewer.getId().equals(recipe.getAuthorId())) {
                    continue;
                }
                Review review = new Review();
                review.setRecipeId(recipe.getId());
                review.setUserId(reviewer.getId());
                review.setRating(4 + ((seed + added) % 2)); // 4s and 5s
                review.setComment(REVIEW_TEXTS.get((seed + added) % REVIEW_TEXTS.size()));
                review.setUpdatedAt(LocalDateTime.now().minusDays(added + 1L));
                reviewDao.save(review);
                added++;
                seed++;
            }
            // Recompute the stored average from the rows we just inserted.
            ReviewDao.RatingSummary summary = reviewDao.summariseForRecipe(recipe.getId());
            recipeDao.updateRating(recipe.getId(),
                    summary.avg() == null ? null : Math.round(summary.avg() * 100.0) / 100.0,
                    (int) summary.count());

            // A couple of comments.
            for (int i = 0; i < 2; i++) {
                User commenter = chefs.get((seed + i) % chefs.size());
                if (commenter.getId().equals(recipe.getAuthorId())) {
                    continue;
                }
                Comment comment = new Comment();
                comment.setRecipeId(recipe.getId());
                comment.setUserId(commenter.getId());
                comment.setText(COMMENT_TEXTS.get((seed + i) % COMMENT_TEXTS.size()));
                comment.setCreatedAt(LocalDateTime.now().minusHours(6L * (i + 1)));
                commentDao.save(comment);
            }

            // Likes + favorites, then sync the denormalised counters.
            for (User fan : chefs) {
                if (fan.getId().equals(recipe.getAuthorId())) {
                    continue;
                }
                if (Math.random() < 0.6) {
                    likeDao.set(fan.getId(), recipe.getId(), true);
                }
                if (Math.random() < 0.4) {
                    favoriteDao.set(fan.getId(), recipe.getId(), true);
                }
            }
            recipeDao.updateCounter(recipe.getId(), "likes_count",
                    (int) likeDao.countByRecipe(recipe.getId()));
            recipeDao.updateCounter(recipe.getId(), "favorites_count",
                    (int) favoriteDao.countByRecipe(recipe.getId()));
            seed++;
        }
    }
}
