-- H2 2.2.224;
;             
CREATE USER IF NOT EXISTS "SA" SALT '4b1a361ce8c7f4bc' HASH 'ef860b81a776863e788d3335025c56c54699e0904d1047e26462685613a0e003' ADMIN;         
CREATE CACHED TABLE "PUBLIC"."USERS"(
    "ID" CHARACTER VARYING(36) NOT NULL,
    "NAME" CHARACTER VARYING(80) NOT NULL,
    "USERNAME" CHARACTER VARYING(30) NOT NULL,
    "EMAIL" CHARACTER VARYING(120) NOT NULL,
    "PASSWORD_HASH" CHARACTER VARYING(100) NOT NULL,
    "AVATAR_URL" CHARACTER VARYING(500),
    "BIO" CHARACTER VARYING(400) DEFAULT '',
    "ROLE" CHARACTER VARYING(10) DEFAULT 'USER' NOT NULL,
    "CREATED_AT" TIMESTAMP NOT NULL
);   
ALTER TABLE "PUBLIC"."USERS" ADD CONSTRAINT "PUBLIC"."CONSTRAINT_4" PRIMARY KEY("ID");        
-- 6 +/- SELECT COUNT(*) FROM PUBLIC.USERS;   
INSERT INTO "PUBLIC"."USERS" VALUES
('043cb315-8f5a-4b61-9a0c-a0f35068fc80', 'Priya Raghavan', 'priya', 'priya@tastetribe.dev', '$2a$10$mwHnND70eyhWas/5I9kfp.QkZsxwF7UN.jbanwJM12DIUVFEQSqNq', 'https://images.unsplash.com/photo-1544005313-94ddf0286df2?crop=faces&cs=srgb&fm=jpg&q=85&w=400&h=400&fit=crop', 'Home cook from Chennai. Chasing the perfect dosa batter and the slowest biryani.', 'USER', TIMESTAMP '2026-10-12 16:08:15.479862'),
('a1940738-2ee5-4b84-9b3c-bc61d9f7bf93', 'Marco Bellini', 'marco', 'marco@tastetribe.dev', '$2a$10$ofhtmDoWaxdEctuwKg07q.dykyvmzmnUiERjk7gTqbHpYlvCFjrEi', 'https://images.unsplash.com/photo-1656338997878-279d71d48f6e?crop=faces&cs=srgb&fm=jpg&q=85&w=400&h=400&fit=crop', U&'Third-generation pasta maker. Flour, eggs, patience \2014 nothing else.', 'USER', TIMESTAMP '2026-10-12 16:08:15.611395'),
('7e4a9a64-0dca-4f28-8eef-09065d72ce47', 'Hana Kim', 'hana', 'hana@tastetribe.dev', '$2a$10$hUkdu99FiKgux2y9Wm/1Kubub.7ybL64Z7O36/e.FcBgXh85NdMna', 'https://images.unsplash.com/photo-1607569708758-0270aa4651bd?crop=faces&cs=srgb&fm=jpg&q=85&w=400&h=400&fit=crop', 'Seoul-born, Berlin-based. Fermentation, banchan and very good noodles.', 'USER', TIMESTAMP '2026-10-12 16:08:15.712934'),
('df49b286-b6db-4a84-bf6f-b1e34ea2bbf8', 'Diego Alvarez', 'diego', 'diego@tastetribe.dev', '$2a$10$bJGp9zi8uai38RgC5QzaAOynudr6cryVVwfHGZCvg7rw0CxywgCfS', 'https://images.unsplash.com/flagged/photo-1557581462-0bf3e5907811?crop=faces&cs=srgb&fm=jpg&q=85&w=400&h=400&fit=crop', 'Taquero at heart. If it can be grilled and wrapped in a tortilla, I''ve tried it.', 'USER', TIMESTAMP '2026-10-12 16:08:15.813255'),
('d8365c12-b600-42ce-ad5e-4be8b2d51a41', 'Amelia Fox', 'amelia', 'amelia@tastetribe.dev', '$2a$10$0TOtIkJyYp.KG.TLozMiWOh8rwoOzoCJZFoDzrtYMNFFOazleLa96', 'https://images.unsplash.com/photo-1604072366595-e75dc92d6bdc?crop=faces&cs=srgb&fm=jpg&q=85&w=400&h=400&fit=crop', 'Pastry-obsessed. Butter is a food group and I will defend that.', 'USER', TIMESTAMP '2026-10-12 16:08:15.900788'),
('cd703a29-e725-444c-b8c5-0b21e4d47f85', 'Platform Admin', 'admin', 'admin@tastetribe.dev', '$2a$10$hGQb59gt2ve3G2g.5gqjvOpk5i3sPIMWjuMRLbW5zpf0f/cGQnGC6', 'https://images.unsplash.com/photo-1597651711127-600d0c2e78b0?crop=faces&cs=srgb&fm=jpg&q=85&w=400&h=400&fit=crop', 'Keeping the TasteTribe kitchen tidy.', 'ADMIN', TIMESTAMP '2026-10-12 16:08:15.996308');             
CREATE CACHED TABLE "PUBLIC"."SESSIONS"(
    "TOKEN" CHARACTER VARYING(64) NOT NULL,
    "USER_ID" CHARACTER VARYING(36) NOT NULL,
    "CREATED_AT" TIMESTAMP NOT NULL,
    "EXPIRES_AT" TIMESTAMP NOT NULL
);           
ALTER TABLE "PUBLIC"."SESSIONS" ADD CONSTRAINT "PUBLIC"."CONSTRAINT_8" PRIMARY KEY("TOKEN");  
-- 0 +/- SELECT COUNT(*) FROM PUBLIC.SESSIONS;
CREATE INDEX "PUBLIC"."IDX_SESSIONS_USER" ON "PUBLIC"."SESSIONS"("USER_ID" NULLS FIRST);      
CREATE CACHED TABLE "PUBLIC"."PASSWORD_RESETS"(
    "TOKEN" CHARACTER VARYING(64) NOT NULL,
    "USER_ID" CHARACTER VARYING(36) NOT NULL,
    "CREATED_AT" TIMESTAMP NOT NULL,
    "EXPIRES_AT" TIMESTAMP NOT NULL
);    
ALTER TABLE "PUBLIC"."PASSWORD_RESETS" ADD CONSTRAINT "PUBLIC"."CONSTRAINT_84" PRIMARY KEY("TOKEN");          
-- 0 +/- SELECT COUNT(*) FROM PUBLIC.PASSWORD_RESETS;         
CREATE INDEX "PUBLIC"."IDX_PWRESETS_USER" ON "PUBLIC"."PASSWORD_RESETS"("USER_ID" NULLS FIRST);               
CREATE CACHED TABLE "PUBLIC"."CATEGORIES"(
    "ID" CHARACTER VARYING(36) NOT NULL,
    "NAME" CHARACTER VARYING(60) NOT NULL,
    "SLUG" CHARACTER VARYING(60) NOT NULL,
    "DESCRIPTION" CHARACTER VARYING(300) DEFAULT '',
    "IMAGE_URL" CHARACTER VARYING(500),
    "CREATED_AT" TIMESTAMP NOT NULL
);          
ALTER TABLE "PUBLIC"."CATEGORIES" ADD CONSTRAINT "PUBLIC"."CONSTRAINT_6" PRIMARY KEY("ID");   
-- 8 +/- SELECT COUNT(*) FROM PUBLIC.CATEGORIES;              
INSERT INTO "PUBLIC"."CATEGORIES" VALUES
('21150ec1-07fd-4c1f-96f6-07806f18e223', 'Breakfast', 'breakfast', 'Bright morning plates and slow weekend brunches', 'https://images.unsplash.com/photo-1506084868230-bb9d95c24759?crop=entropy&cs=srgb&fm=jpg&q=85&w=800', TIMESTAMP '2026-10-12 16:08:15.446858'),
('fa585431-1f48-4e40-8a3e-db78431833dd', 'Lunch', 'lunch', 'Midday meals that travel well', 'https://images.unsplash.com/photo-1512621776951-a57141f2eefd?crop=entropy&cs=srgb&fm=jpg&q=85&w=800', TIMESTAMP '2026-10-12 16:08:15.45987'),
('9ea8fa29-987f-4082-8b77-41d7bdf54ea0', 'Dinner', 'dinner', 'The centrepiece of the evening table', 'https://images.unsplash.com/photo-1585937421612-70a008356fbe?crop=entropy&cs=srgb&fm=jpg&q=85&w=800', TIMESTAMP '2026-10-12 16:08:15.46187'),
('f08edd30-ffc3-4afc-9825-1d9cfd6e8538', 'Snacks', 'snacks', 'Small bites and street-food favourites', 'https://images.unsplash.com/photo-1599974579688-8dbdd335c77f?crop=entropy&cs=srgb&fm=jpg&q=85&w=800', TIMESTAMP '2026-10-12 16:08:15.463864'),
('0c0457d4-3c6e-4638-a807-f3ca134063ac', 'Desserts', 'desserts', 'Bakes, puddings and sweet finishes', 'https://images.unsplash.com/photo-1557925923-33b27f891f88?crop=entropy&cs=srgb&fm=jpg&q=85&w=800', TIMESTAMP '2026-10-12 16:08:15.464857'),
('d50d102b-d47d-436f-a38e-5434d50dff02', 'Beverages', 'beverages', 'Coolers, brews and everything in a glass', 'https://images.unsplash.com/photo-1512223792601-592a9809eed4?crop=entropy&cs=srgb&fm=jpg&q=85&w=800', TIMESTAMP '2026-10-12 16:08:15.46789'),
('9571a8d8-7b09-4fa9-a6d1-7375b180b4c5', 'Quick Meals', 'quick-meals', 'On the table in 30 minutes or less', 'https://images.unsplash.com/photo-1571175534150-72cd2b5a6039?crop=entropy&cs=srgb&fm=jpg&q=85&w=800', TIMESTAMP '2026-10-12 16:08:15.470858'),
('d97bfbfb-6f6e-45a9-994e-7cd2cf1c40a2', 'High Protein', 'high-protein', 'Protein-forward plates for training days', 'https://images.unsplash.com/photo-1490645935967-10de6ba17061?crop=entropy&cs=srgb&fm=jpg&q=85&w=800', TIMESTAMP '2026-10-12 16:08:15.474872');              
CREATE CACHED TABLE "PUBLIC"."RECIPES"(
    "ID" CHARACTER VARYING(36) NOT NULL,
    "TITLE" CHARACTER VARYING(140) NOT NULL,
    "DESCRIPTION" CHARACTER VARYING(2000) DEFAULT '',
    "COVER_IMAGE" CHARACTER VARYING(500),
    "CUISINE" CHARACTER VARYING(60) NOT NULL,
    "CATEGORY" CHARACTER VARYING(60) NOT NULL,
    "DIFFICULTY" CHARACTER VARYING(10) NOT NULL,
    "DIETARY" CHARACTER VARYING(30) DEFAULT 'Non-Vegetarian' NOT NULL,
    "PREP_TIME" INTEGER NOT NULL,
    "COOK_TIME" INTEGER NOT NULL,
    "TOTAL_TIME" INTEGER NOT NULL,
    "SERVINGS" INTEGER DEFAULT 2 NOT NULL,
    "STATUS" CHARACTER VARYING(10) DEFAULT 'PUBLISHED' NOT NULL,
    "SOURCE" CHARACTER VARYING(10) DEFAULT 'user' NOT NULL,
    "VIEWS" INTEGER DEFAULT 0 NOT NULL,
    "LIKES_COUNT" INTEGER DEFAULT 0 NOT NULL,
    "FAVORITES_COUNT" INTEGER DEFAULT 0 NOT NULL,
    "AVG_RATING" DECIMAL(3, 2),
    "RATINGS_COUNT" INTEGER DEFAULT 0 NOT NULL,
    "TAGS_JSON" CHARACTER VARYING,
    "INSTRUCTIONS_JSON" CHARACTER VARYING,
    "NUTRITION_JSON" CHARACTER VARYING,
    "AUTHOR_ID" CHARACTER VARYING(36) NOT NULL,
    "AUTHOR_NAME" CHARACTER VARYING(80) DEFAULT '',
    "AUTHOR_USERNAME" CHARACTER VARYING(30) DEFAULT '',
    "CREATED_AT" TIMESTAMP NOT NULL,
    "UPDATED_AT" TIMESTAMP NOT NULL
);           
ALTER TABLE "PUBLIC"."RECIPES" ADD CONSTRAINT "PUBLIC"."CONSTRAINT_6B" PRIMARY KEY("ID");     
-- 12 +/- SELECT COUNT(*) FROM PUBLIC.RECIPES;
INSERT INTO "PUBLIC"."RECIPES" VALUES
('abb850b1-551c-4d21-9c8a-0b87b5e54ef5', 'Slow-Simmered Butter Chicken', 'The restaurant classic, built properly: charred marinated thighs folded into a glossy tomato-cashew gravy finished with cold butter.', 'https://images.unsplash.com/photo-1606471191009-63994c53433b?crop=entropy&cs=srgb&fm=jpg&q=85&w=1200', 'Indian', 'Dinner', 'Medium', 'Non-Vegetarian', 30, 45, 75, 4, 'PUBLISHED', 'user', 1840, 3, 2, 4.00, 2, '["curry","comfort food","restaurant style"]', U&'["Marinate the chicken with yoghurt, ginger garlic paste, chilli powder and salt for at least 30 minutes.","Sear the chicken in a hot pan until charred at the edges but not cooked through. Set aside.","Simmer tomatoes with cashews and a splash of water for 15 minutes until collapsing, then blend smooth and pass through a sieve.","Melt butter in the same pan, add the tomato-cashew pur\00e9e and cook until it darkens and the fat separates.","Return the chicken with any resting juices, cover and simmer 12 minutes until tender.","Stir in cream, garam masala and crushed kasuri methi. Rest 5 minutes before serving with naan or rice."]', '{"calories":640.0,"protein":42.0,"carbs":18.0,"fat":44.0}', '043cb315-8f5a-4b61-9a0c-a0f35068fc80', 'Priya Raghavan', 'priya', TIMESTAMP '2026-09-30 16:08:16.096437', TIMESTAMP '2026-09-30 16:08:16.096437'),
('ede1d89c-2897-4b10-bc76-5ebc3705f6ad', 'Hyderabadi Chicken Dum Biryani', 'Layered long-grain rice and spiced chicken sealed and steamed so every grain stays separate and perfumed.', 'https://images.unsplash.com/photo-1563379091339-03b21ab4a4f8?crop=entropy&cs=srgb&fm=jpg&q=85&w=1200', 'Indian', 'Dinner', 'Hard', 'Non-Vegetarian', 45, 60, 105, 6, 'PUBLISHED', 'user', 2410, 5, 4, 5.00, 2, '["biryani","festive","one pot"]', '["Marinate the chicken with yoghurt, fried onions, mint, biryani masala and salt for 2 hours.","Soak the rice 30 minutes, then parboil in heavily salted water with whole spices until 70% cooked. Drain.","Layer the marinated chicken in a heavy pot, top with the rice, remaining fried onions, saffron milk and ghee.","Seal the lid with dough or foil and cook on high for 5 minutes, then on the lowest heat for 35 minutes.","Rest sealed for 15 minutes. Open at the table and fold gently from the bottom up."]', '{"calories":780.0,"protein":38.0,"carbs":92.0,"fat":26.0}', '043cb315-8f5a-4b61-9a0c-a0f35068fc80', 'Priya Raghavan', 'priya', TIMESTAMP '2026-09-22 16:08:16.262321', TIMESTAMP '2026-09-22 16:08:16.262321'),
('2527f6c2-6e60-4e21-aab7-aff17643d9cd', 'Cacio e Pepe, Done Right', 'Four ingredients, zero cream, no broken sauce: a lesson in emulsifying pecorino with starchy pasta water.', 'https://images.unsplash.com/photo-1571175534150-72cd2b5a6039?crop=entropy&cs=srgb&fm=jpg&q=85&w=1200', 'Italian', 'Quick Meals', 'Medium', 'Vegetarian', 5, 15, 20, 2, 'PUBLISHED', 'user', 3120, 4, 3, 4.00, 2, '["pasta","4 ingredients","weeknight"]', '["Toast coarsely cracked peppercorns in a dry pan until fragrant, then add a ladle of water to make a pepper tea.","Cook the pasta in deliberately under-salted water so the cheese can season the dish.","Grate the pecorino very finely and mix with a little cool pasta water into a thick paste.","Drain the pasta two minutes early and finish it in the pepper pan with a splash of its water.","Off the heat, vigorously toss in the cheese paste until glossy. Serve immediately in warm bowls."]', '{"calories":520.0,"protein":22.0,"carbs":64.0,"fat":19.0}', 'a1940738-2ee5-4b84-9b3c-bc61d9f7bf93', 'Marco Bellini', 'marco', TIMESTAMP '2026-10-07 16:08:16.280334', TIMESTAMP '2026-10-07 16:08:16.280334'),
('bbb8dce6-d1f4-4794-bd72-38c0784949ce', 'Hand-Rolled Spinach Ricotta Ravioli', 'A Sunday project worth every minute: silky egg pasta around a bright, lemony ricotta filling.', 'https://images.unsplash.com/photo-1587740908075-9e245070dfaa?crop=entropy&cs=srgb&fm=jpg&q=85&w=1200', 'Italian', 'Dinner', 'Hard', 'Vegetarian', 60, 10, 70, 4, 'PUBLISHED', 'user', 1460, 2, 3, 5.00, 2, '["fresh pasta","weekend project","vegetarian"]', '["Make a well with the flour, add eggs and knead 10 minutes until smooth and elastic. Rest wrapped for 30 minutes.","Wilt the spinach, squeeze it bone dry and chop finely. Fold through ricotta, parmesan, lemon zest, nutmeg and salt.","Roll the dough to the second-thinnest pasta setting in long sheets.","Pipe walnut-sized mounds of filling, brush around them with water, lay a second sheet over and seal out all air before cutting.","Boil in generously salted water for 3 minutes.","Foam butter with sage until nut-brown and spoon over the drained ravioli."]', '{"calories":690.0,"protein":29.0,"carbs":71.0,"fat":31.0}', 'a1940738-2ee5-4b84-9b3c-bc61d9f7bf93', 'Marco Bellini', 'marco', TIMESTAMP '2026-10-03 16:08:16.290352', TIMESTAMP '2026-10-03 16:08:16.290352');            
INSERT INTO "PUBLIC"."RECIPES" VALUES
('a1779ab3-b77a-4713-b927-bb524d6fc832', 'Tonkotsu-Style Shoyu Ramen', 'A weeknight-honest ramen: deeply seasoned broth, jammy marinated eggs and noodles with real bite.', 'https://images.unsplash.com/photo-1753525808061-b42dfdd8aa41?crop=entropy&cs=srgb&fm=jpg&q=85&w=1200', 'Japanese', 'Dinner', 'Medium', 'Non-Vegetarian', 25, 50, 75, 2, 'PUBLISHED', 'user', 2680, 3, 2, 4.00, 2, '["ramen","noodles","broth"]', U&'["Simmer the pork belly in stock with garlic for 40 minutes until a chopstick slides through easily.","Mix soy sauce and mirin; marinate 6\00bd-minute boiled eggs in it for at least an hour.","Slice and briefly sear the pork to crisp the edges.","Season the hot broth with the remaining marinade until it tastes assertively savoury.","Cook the noodles to the low end of the package time and drain hard.","Build the bowl: broth, noodles, pork, halved egg, spring onion and nori."]', '{"calories":720.0,"protein":44.0,"carbs":68.0,"fat":29.0}', '7e4a9a64-0dca-4f28-8eef-09065d72ce47', 'Hana Kim', 'hana', TIMESTAMP '2026-10-05 16:08:16.308756', TIMESTAMP '2026-10-05 16:08:16.308756'),
('d97f5cb0-e8ec-4067-8e33-6ba654e042d5', 'Crunchy Kimchi Fried Rice', 'The best possible use of leftover rice and over-fermented kimchi, finished with a runny fried egg.', 'https://images.unsplash.com/photo-1591814468924-caf88d1232e1?crop=entropy&cs=srgb&fm=jpg&q=85&w=1200', 'Korean', 'Quick Meals', 'Easy', 'Vegetarian', 10, 12, 22, 2, 'PUBLISHED', 'user', 1990, 2, 2, 5.00, 2, '["15 minutes","leftovers","spicy"]', '["Chop the kimchi and fry it in a very hot oiled pan for 3 minutes until the edges caramelise.","Stir in gochujang and the kimchi brine and cook 1 minute more.","Add the cold rice, breaking up clumps, and press it into the pan to build a crust.","Toss through sesame oil and spring onions off the heat.","Fry the eggs with crisp edges and runny yolks and set one on each bowl."]', '{"calories":480.0,"protein":14.0,"carbs":72.0,"fat":15.0}', '7e4a9a64-0dca-4f28-8eef-09065d72ce47', 'Hana Kim', 'hana', TIMESTAMP '2026-10-09 16:08:16.321782', TIMESTAMP '2026-10-09 16:08:16.321782'),
('19a29845-f4af-4e5f-8978-51d5ce89fd13', 'Tacos al Pastor', 'Chilli-and-pineapple marinated pork, griddled hard and piled into warm corn tortillas.', 'https://images.unsplash.com/photo-1599974579688-8dbdd335c77f?crop=entropy&cs=srgb&fm=jpg&q=85&w=1200', 'Mexican', 'Dinner', 'Medium', 'Non-Vegetarian', 30, 25, 55, 4, 'PUBLISHED', 'user', 2240, 4, 3, 4.00, 2, '["tacos","street food","grill"]', '["Rehydrate the chillies in hot water, then blend with achiote, a quarter of the pineapple, garlic and vinegar.","Slice the pork thinly, coat in the marinade and refrigerate at least 4 hours.","Sear the pork in batches on a screaming-hot griddle so it chars rather than steams.","Char the remaining pineapple alongside and dice it.","Warm the tortillas directly over the flame until pliable and spotted.","Fill with pork, pineapple, finely diced onion and coriander. Serve with lime."]', '{"calories":560.0,"protein":36.0,"carbs":44.0,"fat":24.0}', 'df49b286-b6db-4a84-bf6f-b1e34ea2bbf8', 'Diego Alvarez', 'diego', TIMESTAMP '2026-10-01 16:08:16.333796', TIMESTAMP '2026-10-01 16:08:16.333796'),
('8d3646f5-7627-4dec-8458-8e2bcb1c6534', 'Charred Salsa Verde', 'Blistered tomatillos and serranos blitzed into a sharp, smoky salsa that improves everything it touches.', 'https://images.unsplash.com/photo-1552332386-f8dd00dc2f85?crop=entropy&cs=srgb&fm=jpg&q=85&w=1200', 'Mexican', 'Snacks', 'Easy', 'Vegan', 10, 10, 20, 6, 'PUBLISHED', 'user', 980, 3, 1, 5.00, 2, '["salsa","vegan","5 ingredients"]', U&'["Dry-roast tomatillos, chillies, onion and unpeeled garlic in a hot pan until blackened in patches.","Peel the garlic and tip everything into a blender with coriander and salt.","Pulse to a coarse pur\00e9e \2014 texture matters more than smoothness.","Finish with lime juice and adjust salt. Keeps 5 days refrigerated."]', '{"calories":45.0,"protein":2.0,"carbs":8.0,"fat":1.0}', 'df49b286-b6db-4a84-bf6f-b1e34ea2bbf8', 'Diego Alvarez', 'diego', TIMESTAMP '2026-10-10 16:08:16.347333', TIMESTAMP '2026-10-10 16:08:16.347333');  
INSERT INTO "PUBLIC"."RECIPES" VALUES
('a9539172-91d5-4b53-97e5-2c6b6057fa0b', 'Raspberry Cloud Layer Cake', 'A tender vanilla sponge with mascarpone cream and a mountain of fresh raspberries.', 'https://images.unsplash.com/photo-1557925923-33b27f891f88?crop=entropy&cs=srgb&fm=jpg&q=85&w=1200', 'Continental', 'Desserts', 'Medium', 'Vegetarian', 35, 30, 65, 8, 'PUBLISHED', 'user', 2890, 4, 3, 4.00, 2, '["cake","baking","celebration"]', U&'["Cream the soft butter and sugar for a full 5 minutes until pale and aerated.","Add the eggs one at a time with a spoon of flour between each to stop the batter splitting.","Fold in the remaining flour and baking powder, then divide between two lined tins.","Bake at 175\00b0C for 28-32 minutes until a skewer comes out clean. Cool completely.","Whip mascarpone, cream and vanilla to soft, scoopable peaks \2014 stop before it stiffens.","Layer the sponges with cream and raspberries, finishing with fruit on top."]', '{"calories":610.0,"protein":9.0,"carbs":62.0,"fat":37.0}', 'd8365c12-b600-42ce-ad5e-4be8b2d51a41', 'Amelia Fox', 'amelia', TIMESTAMP '2026-10-06 16:08:16.360338', TIMESTAMP '2026-10-06 16:08:16.360338'),
('8a300c40-80ef-4493-b2df-fbf8b9d00796', 'Brown Butter Blueberry Pancakes', 'Thick, fluffy pancakes with nutty brown butter in the batter and berries that burst as they cook.', 'https://images.unsplash.com/photo-1506084868230-bb9d95c24759?crop=entropy&cs=srgb&fm=jpg&q=85&w=1200', 'American', 'Breakfast', 'Easy', 'Vegetarian', 10, 15, 25, 4, 'PUBLISHED', 'user', 2150, 3, 1, 5.00, 2, '["breakfast","fluffy","family"]', U&'["Melt the butter and keep cooking it until the milk solids smell nutty and turn amber. Cool slightly.","Whisk the dry ingredients in one bowl, the buttermilk, eggs and brown butter in another.","Combine with as few strokes as possible \2014 a lumpy batter makes tall pancakes. Rest 10 minutes.","Ladle onto a medium pan, scatter blueberries over each pancake and cook until bubbles pop on top.","Flip once and cook 90 seconds more. Serve stacked with maple syrup."]', '{"calories":430.0,"protein":12.0,"carbs":58.0,"fat":16.0}', 'd8365c12-b600-42ce-ad5e-4be8b2d51a41', 'Amelia Fox', 'amelia', TIMESTAMP '2026-10-11 16:08:16.372344', TIMESTAMP '2026-10-11 16:08:16.372344'),
('59405b37-98b2-4e28-8f85-c6d2b14e1024', 'Sesame Soy Protein Bowl', 'A cold-prep bowl built for training days: marinated tofu, edamame, quinoa and a punchy sesame dressing.', 'https://images.unsplash.com/photo-1512621776951-a57141f2eefd?crop=entropy&cs=srgb&fm=jpg&q=85&w=1200', 'Korean', 'High Protein', 'Easy', 'Vegan', 20, 15, 35, 2, 'PUBLISHED', 'user', 1620, 5, 3, 4.00, 2, '["high protein","meal prep","vegan"]', U&'["Press the tofu 15 minutes, cube it and marinate in half the soy sauce.","Cook the quinoa in well-salted water, then spread on a tray to cool and stay fluffy.","Roast or air-fry the tofu at 200\00b0C for 15 minutes until the edges are firm.","Whisk tahini, rice vinegar, sesame oil and the remaining soy into a pourable dressing.","Assemble quinoa, edamame, ribboned cucumber and tofu, then spoon the dressing over."]', '{"calories":540.0,"protein":34.0,"carbs":48.0,"fat":22.0}', '7e4a9a64-0dca-4f28-8eef-09065d72ce47', 'Hana Kim', 'hana', TIMESTAMP '2026-10-08 16:08:16.383342', TIMESTAMP '2026-10-08 16:08:16.383342'),
('11bd7336-73d7-47ab-9b66-ae9611d40c2f', 'Masala Chai, Properly Brewed', 'Not a teabag in warm milk: whole spices bruised fresh and boiled hard with strong assam.', 'https://images.unsplash.com/photo-1631452180539-96aca7d48617?crop=entropy&cs=srgb&fm=jpg&q=85&w=1200', 'Indian', 'Beverages', 'Easy', 'Vegetarian', 5, 10, 15, 2, 'PUBLISHED', 'user', 1340, 2, 4, 5.00, 2, '["chai","10 minutes","comfort"]', U&'["Bruise the cardamom and ginger in a mortar \2014 never use ground spice here.","Boil the water with the spices for 3 minutes to build a concentrated infusion.","Add the tea leaves and boil 2 minutes until dark and tannic.","Pour in the milk and sugar and bring to a rolling boil, pulling the pan back each time it rises.","Strain from a height into cups to aerate it."]', '{"calories":120.0,"protein":4.0,"carbs":18.0,"fat":4.0}', '043cb315-8f5a-4b61-9a0c-a0f35068fc80', 'Priya Raghavan', 'priya', TIMESTAMP '2026-10-04 16:08:16.396345', TIMESTAMP '2026-10-04 16:08:16.396345'); 
CREATE INDEX "PUBLIC"."IDX_RECIPES_STATUS_CREATED" ON "PUBLIC"."RECIPES"("STATUS" NULLS FIRST, "CREATED_AT" NULLS FIRST);     
CREATE INDEX "PUBLIC"."IDX_RECIPES_AUTHOR" ON "PUBLIC"."RECIPES"("AUTHOR_ID" NULLS FIRST);    
CREATE INDEX "PUBLIC"."IDX_RECIPES_CATEGORY" ON "PUBLIC"."RECIPES"("CATEGORY" NULLS FIRST);   
CREATE INDEX "PUBLIC"."IDX_RECIPES_CUISINE" ON "PUBLIC"."RECIPES"("CUISINE" NULLS FIRST);     
CREATE INDEX "PUBLIC"."IDX_RECIPES_RATING" ON "PUBLIC"."RECIPES"("AVG_RATING" NULLS FIRST);   
CREATE CACHED TABLE "PUBLIC"."RECIPE_INGREDIENTS"(
    "ID" BIGINT GENERATED BY DEFAULT AS IDENTITY(START WITH 1 RESTART WITH 99) DEFAULT ON NULL NOT NULL,
    "RECIPE_ID" CHARACTER VARYING(36) NOT NULL,
    "NAME" CHARACTER VARYING(120) NOT NULL,
    "QUANTITY" DECIMAL(10, 3) NOT NULL,
    "UNIT" CHARACTER VARYING(30) DEFAULT 'piece' NOT NULL,
    "IS_OPTIONAL" TINYINT DEFAULT 0 NOT NULL,
    "POSITION" INTEGER DEFAULT 0 NOT NULL
); 
ALTER TABLE "PUBLIC"."RECIPE_INGREDIENTS" ADD CONSTRAINT "PUBLIC"."CONSTRAINT_1" PRIMARY KEY("ID");           
-- 98 +/- SELECT COUNT(*) FROM PUBLIC.RECIPE_INGREDIENTS;     
INSERT INTO "PUBLIC"."RECIPE_INGREDIENTS" VALUES
(1, 'abb850b1-551c-4d21-9c8a-0b87b5e54ef5', 'Chicken thighs', 800.000, 'g', 0, 0),
(2, 'abb850b1-551c-4d21-9c8a-0b87b5e54ef5', 'Greek yoghurt', 150.000, 'g', 0, 1),
(3, 'abb850b1-551c-4d21-9c8a-0b87b5e54ef5', 'Ginger garlic paste', 2.000, 'tbsp', 0, 2),
(4, 'abb850b1-551c-4d21-9c8a-0b87b5e54ef5', 'Kashmiri chilli powder', 2.000, 'tsp', 0, 3),
(5, 'abb850b1-551c-4d21-9c8a-0b87b5e54ef5', 'Tomatoes', 6.000, 'pieces', 0, 4),
(6, 'abb850b1-551c-4d21-9c8a-0b87b5e54ef5', 'Cashews', 60.000, 'g', 0, 5),
(7, 'abb850b1-551c-4d21-9c8a-0b87b5e54ef5', 'Butter', 80.000, 'g', 0, 6),
(8, 'abb850b1-551c-4d21-9c8a-0b87b5e54ef5', 'Cream', 100.000, 'ml', 0, 7),
(9, 'abb850b1-551c-4d21-9c8a-0b87b5e54ef5', 'Garam masala', 1.000, 'tsp', 0, 8),
(10, 'abb850b1-551c-4d21-9c8a-0b87b5e54ef5', 'Kasuri methi', 1.000, 'tbsp', 1, 9),
(11, 'ede1d89c-2897-4b10-bc76-5ebc3705f6ad', 'Basmati rice', 750.000, 'g', 0, 0),
(12, 'ede1d89c-2897-4b10-bc76-5ebc3705f6ad', 'Chicken', 1.000, 'kg', 0, 1),
(13, 'ede1d89c-2897-4b10-bc76-5ebc3705f6ad', 'Yoghurt', 250.000, 'g', 0, 2),
(14, 'ede1d89c-2897-4b10-bc76-5ebc3705f6ad', 'Fried onions', 200.000, 'g', 0, 3),
(15, 'ede1d89c-2897-4b10-bc76-5ebc3705f6ad', 'Mint leaves', 1.000, 'cup', 0, 4),
(16, 'ede1d89c-2897-4b10-bc76-5ebc3705f6ad', 'Biryani masala', 3.000, 'tbsp', 0, 5),
(17, 'ede1d89c-2897-4b10-bc76-5ebc3705f6ad', 'Saffron', 1.000, 'pinch', 0, 6),
(18, 'ede1d89c-2897-4b10-bc76-5ebc3705f6ad', 'Ghee', 100.000, 'g', 0, 7),
(19, 'ede1d89c-2897-4b10-bc76-5ebc3705f6ad', 'Green chillies', 6.000, 'pieces', 1, 8),
(20, '2527f6c2-6e60-4e21-aab7-aff17643d9cd', 'Tonnarelli or spaghetti', 200.000, 'g', 0, 0),
(21, '2527f6c2-6e60-4e21-aab7-aff17643d9cd', 'Pecorino Romano', 120.000, 'g', 0, 1),
(22, '2527f6c2-6e60-4e21-aab7-aff17643d9cd', 'Black peppercorns', 2.000, 'tsp', 0, 2),
(23, '2527f6c2-6e60-4e21-aab7-aff17643d9cd', 'Sea salt', 1.000, 'tbsp', 0, 3),
(24, 'bbb8dce6-d1f4-4794-bd72-38c0784949ce', '00 flour', 400.000, 'g', 0, 0),
(25, 'bbb8dce6-d1f4-4794-bd72-38c0784949ce', 'Eggs', 4.000, 'pieces', 0, 1),
(26, 'bbb8dce6-d1f4-4794-bd72-38c0784949ce', 'Ricotta', 400.000, 'g', 0, 2),
(27, 'bbb8dce6-d1f4-4794-bd72-38c0784949ce', 'Spinach', 300.000, 'g', 0, 3),
(28, 'bbb8dce6-d1f4-4794-bd72-38c0784949ce', 'Parmesan', 80.000, 'g', 0, 4),
(29, 'bbb8dce6-d1f4-4794-bd72-38c0784949ce', 'Lemon zest', 1.000, 'piece', 0, 5),
(30, 'bbb8dce6-d1f4-4794-bd72-38c0784949ce', 'Nutmeg', 1.000, 'pinch', 1, 6),
(31, 'bbb8dce6-d1f4-4794-bd72-38c0784949ce', 'Butter', 60.000, 'g', 0, 7),
(32, 'bbb8dce6-d1f4-4794-bd72-38c0784949ce', 'Sage leaves', 10.000, 'pieces', 1, 8),
(33, 'a1779ab3-b77a-4713-b927-bb524d6fc832', 'Ramen noodles', 2.000, 'portions', 0, 0),
(34, 'a1779ab3-b77a-4713-b927-bb524d6fc832', 'Pork belly', 300.000, 'g', 0, 1),
(35, 'a1779ab3-b77a-4713-b927-bb524d6fc832', 'Chicken stock', 1.000, 'l', 0, 2),
(36, 'a1779ab3-b77a-4713-b927-bb524d6fc832', 'Soy sauce', 80.000, 'ml', 0, 3),
(37, 'a1779ab3-b77a-4713-b927-bb524d6fc832', 'Mirin', 40.000, 'ml', 0, 4),
(38, 'a1779ab3-b77a-4713-b927-bb524d6fc832', 'Eggs', 2.000, 'pieces', 0, 5),
(39, 'a1779ab3-b77a-4713-b927-bb524d6fc832', 'Spring onions', 3.000, 'pieces', 0, 6),
(40, 'a1779ab3-b77a-4713-b927-bb524d6fc832', 'Nori sheets', 2.000, 'pieces', 1, 7),
(41, 'a1779ab3-b77a-4713-b927-bb524d6fc832', 'Garlic', 4.000, 'cloves', 0, 8),
(42, 'd97f5cb0-e8ec-4067-8e33-6ba654e042d5', 'Day-old cooked rice', 500.000, 'g', 0, 0),
(43, 'd97f5cb0-e8ec-4067-8e33-6ba654e042d5', 'Aged kimchi', 200.000, 'g', 0, 1),
(44, 'd97f5cb0-e8ec-4067-8e33-6ba654e042d5', 'Kimchi brine', 3.000, 'tbsp', 0, 2),
(45, 'd97f5cb0-e8ec-4067-8e33-6ba654e042d5', 'Gochujang', 1.000, 'tbsp', 0, 3),
(46, 'd97f5cb0-e8ec-4067-8e33-6ba654e042d5', 'Sesame oil', 2.000, 'tsp', 0, 4),
(47, 'd97f5cb0-e8ec-4067-8e33-6ba654e042d5', 'Eggs', 2.000, 'pieces', 0, 5),
(48, 'd97f5cb0-e8ec-4067-8e33-6ba654e042d5', 'Spring onions', 2.000, 'pieces', 0, 6),
(49, 'd97f5cb0-e8ec-4067-8e33-6ba654e042d5', 'Toasted sesame seeds', 1.000, 'tsp', 1, 7),
(50, '19a29845-f4af-4e5f-8978-51d5ce89fd13', 'Pork shoulder', 800.000, 'g', 0, 0);            
INSERT INTO "PUBLIC"."RECIPE_INGREDIENTS" VALUES
(51, '19a29845-f4af-4e5f-8978-51d5ce89fd13', 'Guajillo chillies', 4.000, 'pieces', 0, 1),
(52, '19a29845-f4af-4e5f-8978-51d5ce89fd13', 'Achiote paste', 2.000, 'tbsp', 0, 2),
(53, '19a29845-f4af-4e5f-8978-51d5ce89fd13', 'Pineapple', 300.000, 'g', 0, 3),
(54, '19a29845-f4af-4e5f-8978-51d5ce89fd13', 'White onion', 1.000, 'piece', 0, 4),
(55, '19a29845-f4af-4e5f-8978-51d5ce89fd13', 'Corn tortillas', 12.000, 'pieces', 0, 5),
(56, '19a29845-f4af-4e5f-8978-51d5ce89fd13', 'Coriander', 1.000, 'bunch', 0, 6),
(57, '19a29845-f4af-4e5f-8978-51d5ce89fd13', 'Limes', 2.000, 'pieces', 0, 7),
(58, '8d3646f5-7627-4dec-8458-8e2bcb1c6534', 'Tomatillos', 500.000, 'g', 0, 0),
(59, '8d3646f5-7627-4dec-8458-8e2bcb1c6534', 'Serrano chillies', 3.000, 'pieces', 0, 1),
(60, '8d3646f5-7627-4dec-8458-8e2bcb1c6534', 'White onion', 0.500, 'piece', 0, 2),
(61, '8d3646f5-7627-4dec-8458-8e2bcb1c6534', 'Garlic', 2.000, 'cloves', 0, 3),
(62, '8d3646f5-7627-4dec-8458-8e2bcb1c6534', 'Coriander', 1.000, 'handful', 0, 4),
(63, '8d3646f5-7627-4dec-8458-8e2bcb1c6534', 'Lime', 1.000, 'piece', 0, 5),
(64, '8d3646f5-7627-4dec-8458-8e2bcb1c6534', 'Salt', 1.000, 'tsp', 0, 6),
(65, 'a9539172-91d5-4b53-97e5-2c6b6057fa0b', 'Plain flour', 300.000, 'g', 0, 0),
(66, 'a9539172-91d5-4b53-97e5-2c6b6057fa0b', 'Caster sugar', 280.000, 'g', 0, 1),
(67, 'a9539172-91d5-4b53-97e5-2c6b6057fa0b', 'Butter', 220.000, 'g', 0, 2),
(68, 'a9539172-91d5-4b53-97e5-2c6b6057fa0b', 'Eggs', 4.000, 'pieces', 0, 3),
(69, 'a9539172-91d5-4b53-97e5-2c6b6057fa0b', 'Mascarpone', 400.000, 'g', 0, 4),
(70, 'a9539172-91d5-4b53-97e5-2c6b6057fa0b', 'Double cream', 300.000, 'ml', 0, 5),
(71, 'a9539172-91d5-4b53-97e5-2c6b6057fa0b', 'Raspberries', 400.000, 'g', 0, 6),
(72, 'a9539172-91d5-4b53-97e5-2c6b6057fa0b', 'Vanilla extract', 2.000, 'tsp', 0, 7),
(73, 'a9539172-91d5-4b53-97e5-2c6b6057fa0b', 'Baking powder', 2.000, 'tsp', 0, 8),
(74, '8a300c40-80ef-4493-b2df-fbf8b9d00796', 'Plain flour', 250.000, 'g', 0, 0),
(75, '8a300c40-80ef-4493-b2df-fbf8b9d00796', 'Buttermilk', 350.000, 'ml', 0, 1),
(76, '8a300c40-80ef-4493-b2df-fbf8b9d00796', 'Butter', 70.000, 'g', 0, 2),
(77, '8a300c40-80ef-4493-b2df-fbf8b9d00796', 'Eggs', 2.000, 'pieces', 0, 3),
(78, '8a300c40-80ef-4493-b2df-fbf8b9d00796', 'Caster sugar', 2.000, 'tbsp', 0, 4),
(79, '8a300c40-80ef-4493-b2df-fbf8b9d00796', 'Baking powder', 2.000, 'tsp', 0, 5),
(80, '8a300c40-80ef-4493-b2df-fbf8b9d00796', 'Bicarbonate of soda', 0.500, 'tsp', 0, 6),
(81, '8a300c40-80ef-4493-b2df-fbf8b9d00796', 'Blueberries', 200.000, 'g', 0, 7),
(82, '8a300c40-80ef-4493-b2df-fbf8b9d00796', 'Maple syrup', 4.000, 'tbsp', 1, 8),
(83, '59405b37-98b2-4e28-8f85-c6d2b14e1024', 'Firm tofu', 400.000, 'g', 0, 0),
(84, '59405b37-98b2-4e28-8f85-c6d2b14e1024', 'Quinoa', 180.000, 'g', 0, 1),
(85, '59405b37-98b2-4e28-8f85-c6d2b14e1024', 'Edamame', 200.000, 'g', 0, 2),
(86, '59405b37-98b2-4e28-8f85-c6d2b14e1024', 'Cucumber', 1.000, 'piece', 0, 3),
(87, '59405b37-98b2-4e28-8f85-c6d2b14e1024', 'Soy sauce', 4.000, 'tbsp', 0, 4),
(88, '59405b37-98b2-4e28-8f85-c6d2b14e1024', 'Tahini', 2.000, 'tbsp', 0, 5),
(89, '59405b37-98b2-4e28-8f85-c6d2b14e1024', 'Rice vinegar', 2.000, 'tbsp', 0, 6),
(90, '59405b37-98b2-4e28-8f85-c6d2b14e1024', 'Sesame oil', 2.000, 'tsp', 0, 7),
(91, '59405b37-98b2-4e28-8f85-c6d2b14e1024', 'Chilli flakes', 1.000, 'tsp', 1, 8),
(92, '11bd7336-73d7-47ab-9b66-ae9611d40c2f', 'Water', 300.000, 'ml', 0, 0),
(93, '11bd7336-73d7-47ab-9b66-ae9611d40c2f', 'Whole milk', 200.000, 'ml', 0, 1),
(94, '11bd7336-73d7-47ab-9b66-ae9611d40c2f', 'Assam tea leaves', 2.000, 'tsp', 0, 2),
(95, '11bd7336-73d7-47ab-9b66-ae9611d40c2f', 'Green cardamom', 4.000, 'pods', 0, 3),
(96, '11bd7336-73d7-47ab-9b66-ae9611d40c2f', 'Fresh ginger', 15.000, 'g', 0, 4),
(97, '11bd7336-73d7-47ab-9b66-ae9611d40c2f', 'Cloves', 2.000, 'pieces', 1, 5),
(98, '11bd7336-73d7-47ab-9b66-ae9611d40c2f', 'Sugar', 2.000, 'tsp', 0, 6); 
CREATE INDEX "PUBLIC"."IDX_RIN" ON "PUBLIC"."RECIPE_INGREDIENTS"("RECIPE_ID" NULLS FIRST, "POSITION" NULLS FIRST);            
CREATE CACHED TABLE "PUBLIC"."REVIEWS"(
    "ID" CHARACTER VARYING(36) NOT NULL,
    "RECIPE_ID" CHARACTER VARYING(36) NOT NULL,
    "USER_ID" CHARACTER VARYING(36) NOT NULL,
    "RATING" TINYINT NOT NULL,
    "COMMENT" CHARACTER VARYING(2000),
    "CREATED_AT" TIMESTAMP NOT NULL,
    "UPDATED_AT" TIMESTAMP NOT NULL
);      
ALTER TABLE "PUBLIC"."REVIEWS" ADD CONSTRAINT "PUBLIC"."CONSTRAINT_6C" PRIMARY KEY("ID");     
-- 24 +/- SELECT COUNT(*) FROM PUBLIC.REVIEWS;
INSERT INTO "PUBLIC"."REVIEWS" VALUES
('a6319a08-958e-447b-8829-27bd3de185cd', 'abb850b1-551c-4d21-9c8a-0b87b5e54ef5', 'a1940738-2ee5-4b84-9b3c-bc61d9f7bf93', 4, 'Made this twice in a week. The timings are spot on and nothing was wasted.', TIMESTAMP '2026-10-12 16:08:16.418341', TIMESTAMP '2026-10-11 16:08:16.418341'),
('4d8def30-4325-4425-985c-32323357abb1', 'abb850b1-551c-4d21-9c8a-0b87b5e54ef5', '7e4a9a64-0dca-4f28-8eef-09065d72ce47', 4, U&'Great base recipe \2014 I cut the chilli by half for the kids and it still sang.', TIMESTAMP '2026-10-12 16:08:16.421338', TIMESTAMP '2026-10-10 16:08:16.421338'),
('7879e1b2-3220-4e21-8cc9-f0a804a7a410', 'ede1d89c-2897-4b10-bc76-5ebc3705f6ad', 'a1940738-2ee5-4b84-9b3c-bc61d9f7bf93', 5, 'This is now my default dinner-party dish. Zero stress, big reaction.', TIMESTAMP '2026-10-12 16:08:16.461891', TIMESTAMP '2026-10-11 16:08:16.461891'),
('3e72a853-1664-4e72-9f5e-a5986130cc09', 'ede1d89c-2897-4b10-bc76-5ebc3705f6ad', '7e4a9a64-0dca-4f28-8eef-09065d72ce47', 5, 'Solid weeknight winner. Took me about 10 minutes longer than stated, worth it.', TIMESTAMP '2026-10-12 16:08:16.463882', TIMESTAMP '2026-10-10 16:08:16.463882'),
('82f9444e-558c-4bbe-9604-88bf57f174ea', '2527f6c2-6e60-4e21-aab7-aff17643d9cd', '043cb315-8f5a-4b61-9a0c-a0f35068fc80', 4, 'Made this twice in a week. The timings are spot on and nothing was wasted.', TIMESTAMP '2026-10-12 16:08:16.483889', TIMESTAMP '2026-10-11 16:08:16.483889'),
('3da52b1a-b73c-4718-8150-1a1e1d1c0f2d', '2527f6c2-6e60-4e21-aab7-aff17643d9cd', '7e4a9a64-0dca-4f28-8eef-09065d72ce47', 4, U&'Great base recipe \2014 I cut the chilli by half for the kids and it still sang.', TIMESTAMP '2026-10-12 16:08:16.4849', TIMESTAMP '2026-10-10 16:08:16.4849'),
('e504572c-35c9-4046-b09a-5cd9eff94d3c', 'bbb8dce6-d1f4-4794-bd72-38c0784949ce', '043cb315-8f5a-4b61-9a0c-a0f35068fc80', 5, 'This is now my default dinner-party dish. Zero stress, big reaction.', TIMESTAMP '2026-10-12 16:08:16.507889', TIMESTAMP '2026-10-11 16:08:16.507889'),
('9813639b-8ec8-4141-b1b3-d09efb372852', 'bbb8dce6-d1f4-4794-bd72-38c0784949ce', '7e4a9a64-0dca-4f28-8eef-09065d72ce47', 5, 'Solid weeknight winner. Took me about 10 minutes longer than stated, worth it.', TIMESTAMP '2026-10-12 16:08:16.509885', TIMESTAMP '2026-10-10 16:08:16.509885'),
('d7e6328a-a9ca-4aad-9744-fffdeb949490', 'a1779ab3-b77a-4713-b927-bb524d6fc832', '043cb315-8f5a-4b61-9a0c-a0f35068fc80', 4, 'Made this twice in a week. The timings are spot on and nothing was wasted.', TIMESTAMP '2026-10-12 16:08:16.52589', TIMESTAMP '2026-10-11 16:08:16.52589'),
('72a29f80-60c7-43e2-9d0d-1bfa8515b21f', 'a1779ab3-b77a-4713-b927-bb524d6fc832', 'a1940738-2ee5-4b84-9b3c-bc61d9f7bf93', 4, U&'Great base recipe \2014 I cut the chilli by half for the kids and it still sang.', TIMESTAMP '2026-10-12 16:08:16.527901', TIMESTAMP '2026-10-10 16:08:16.527901'),
('7b5bcd8d-e288-46d0-9cdd-930c98f837bd', 'd97f5cb0-e8ec-4067-8e33-6ba654e042d5', '043cb315-8f5a-4b61-9a0c-a0f35068fc80', 5, 'This is now my default dinner-party dish. Zero stress, big reaction.', TIMESTAMP '2026-10-12 16:08:16.544453', TIMESTAMP '2026-10-11 16:08:16.544453'),
('7422b7bd-f369-4f4f-9146-b94a7bb7e785', 'd97f5cb0-e8ec-4067-8e33-6ba654e042d5', 'a1940738-2ee5-4b84-9b3c-bc61d9f7bf93', 5, 'Solid weeknight winner. Took me about 10 minutes longer than stated, worth it.', TIMESTAMP '2026-10-12 16:08:16.545442', TIMESTAMP '2026-10-10 16:08:16.545442'),
('bfc749cb-8fca-4b1c-83f5-01001be07939', '19a29845-f4af-4e5f-8978-51d5ce89fd13', '043cb315-8f5a-4b61-9a0c-a0f35068fc80', 4, 'Made this twice in a week. The timings are spot on and nothing was wasted.', TIMESTAMP '2026-10-12 16:08:16.563448', TIMESTAMP '2026-10-11 16:08:16.563448'),
('f8953135-e17d-4e2d-adc8-2b8ce3758808', '19a29845-f4af-4e5f-8978-51d5ce89fd13', 'a1940738-2ee5-4b84-9b3c-bc61d9f7bf93', 4, U&'Great base recipe \2014 I cut the chilli by half for the kids and it still sang.', TIMESTAMP '2026-10-12 16:08:16.564446', TIMESTAMP '2026-10-10 16:08:16.564446'),
('cc221b9f-3f0a-4750-b748-a0d0caee1c1c', '8d3646f5-7627-4dec-8458-8e2bcb1c6534', '043cb315-8f5a-4b61-9a0c-a0f35068fc80', 5, 'This is now my default dinner-party dish. Zero stress, big reaction.', TIMESTAMP '2026-10-12 16:08:16.581446', TIMESTAMP '2026-10-11 16:08:16.581446');       
INSERT INTO "PUBLIC"."REVIEWS" VALUES
('fb24350e-4f52-4c1e-88c2-d0b944260529', '8d3646f5-7627-4dec-8458-8e2bcb1c6534', 'a1940738-2ee5-4b84-9b3c-bc61d9f7bf93', 5, 'Solid weeknight winner. Took me about 10 minutes longer than stated, worth it.', TIMESTAMP '2026-10-12 16:08:16.582454', TIMESTAMP '2026-10-10 16:08:16.582454'),
('9f5e2429-1102-4eb7-92eb-46f199aa3aef', 'a9539172-91d5-4b53-97e5-2c6b6057fa0b', '043cb315-8f5a-4b61-9a0c-a0f35068fc80', 4, 'Made this twice in a week. The timings are spot on and nothing was wasted.', TIMESTAMP '2026-10-12 16:08:16.599441', TIMESTAMP '2026-10-11 16:08:16.599441'),
('adea38be-ae1b-4ce6-b4bc-d3f7bed0ab92', 'a9539172-91d5-4b53-97e5-2c6b6057fa0b', 'a1940738-2ee5-4b84-9b3c-bc61d9f7bf93', 4, U&'Great base recipe \2014 I cut the chilli by half for the kids and it still sang.', TIMESTAMP '2026-10-12 16:08:16.60044', TIMESTAMP '2026-10-10 16:08:16.60044'),
('17f4af1b-6ebb-4ced-abcd-982d45a38c7e', '8a300c40-80ef-4493-b2df-fbf8b9d00796', '043cb315-8f5a-4b61-9a0c-a0f35068fc80', 5, 'This is now my default dinner-party dish. Zero stress, big reaction.', TIMESTAMP '2026-10-12 16:08:16.617448', TIMESTAMP '2026-10-11 16:08:16.617448'),
('6498983c-7de1-4c68-baec-570e1b54b46c', '8a300c40-80ef-4493-b2df-fbf8b9d00796', 'a1940738-2ee5-4b84-9b3c-bc61d9f7bf93', 5, 'Solid weeknight winner. Took me about 10 minutes longer than stated, worth it.', TIMESTAMP '2026-10-12 16:08:16.618442', TIMESTAMP '2026-10-10 16:08:16.618442'),
('6ee26711-f854-4ae8-9a8a-62297d8520a7', '59405b37-98b2-4e28-8f85-c6d2b14e1024', '043cb315-8f5a-4b61-9a0c-a0f35068fc80', 4, 'Made this twice in a week. The timings are spot on and nothing was wasted.', TIMESTAMP '2026-10-12 16:08:16.635506', TIMESTAMP '2026-10-11 16:08:16.635506'),
('ed602921-1429-442f-9d9c-dc53ea9fbd03', '59405b37-98b2-4e28-8f85-c6d2b14e1024', 'a1940738-2ee5-4b84-9b3c-bc61d9f7bf93', 4, U&'Great base recipe \2014 I cut the chilli by half for the kids and it still sang.', TIMESTAMP '2026-10-12 16:08:16.638518', TIMESTAMP '2026-10-10 16:08:16.638518'),
('ec86ff5f-6992-48fa-a286-2d7f4436cb70', '11bd7336-73d7-47ab-9b66-ae9611d40c2f', 'a1940738-2ee5-4b84-9b3c-bc61d9f7bf93', 5, 'This is now my default dinner-party dish. Zero stress, big reaction.', TIMESTAMP '2026-10-12 16:08:16.658039', TIMESTAMP '2026-10-11 16:08:16.658039'),
('592d9202-36a1-47ac-b628-dff8f96b9b66', '11bd7336-73d7-47ab-9b66-ae9611d40c2f', '7e4a9a64-0dca-4f28-8eef-09065d72ce47', 5, 'Solid weeknight winner. Took me about 10 minutes longer than stated, worth it.', TIMESTAMP '2026-10-12 16:08:16.659053', TIMESTAMP '2026-10-10 16:08:16.659053');               
CREATE INDEX "PUBLIC"."IDX_REVIEWS_RECIPE" ON "PUBLIC"."REVIEWS"("RECIPE_ID" NULLS FIRST);    
CREATE CACHED TABLE "PUBLIC"."COMMENTS"(
    "ID" CHARACTER VARYING(36) NOT NULL,
    "RECIPE_ID" CHARACTER VARYING(36) NOT NULL,
    "USER_ID" CHARACTER VARYING(36) NOT NULL,
    "TEXT" CHARACTER VARYING(1000) NOT NULL,
    "CREATED_AT" TIMESTAMP NOT NULL
);     
ALTER TABLE "PUBLIC"."COMMENTS" ADD CONSTRAINT "PUBLIC"."CONSTRAINT_A" PRIMARY KEY("ID");     
-- 19 +/- SELECT COUNT(*) FROM PUBLIC.COMMENTS;               
INSERT INTO "PUBLIC"."COMMENTS" VALUES
('0a9a44af-72a2-46f0-9981-d5e9afbe20be', 'abb850b1-551c-4d21-9c8a-0b87b5e54ef5', '7e4a9a64-0dca-4f28-8eef-09065d72ce47', 'The shopping list feature made this so easy to prep.', TIMESTAMP '2026-10-12 10:08:16.434349'),
('fbeeb732-fa2b-472d-a71b-3f3e065e9650', 'abb850b1-551c-4d21-9c8a-0b87b5e54ef5', 'df49b286-b6db-4a84-bf6f-b1e34ea2bbf8', 'Adding this to my weekend list immediately.', TIMESTAMP '2026-10-12 04:08:16.437355'),
('734c1fe4-b4f7-46a7-abfc-82069692fa93', 'ede1d89c-2897-4b10-bc76-5ebc3705f6ad', 'cd703a29-e725-444c-b8c5-0b21e4d47f85', 'Any suggestions for making this dairy-free?', TIMESTAMP '2026-10-12 10:08:16.467893'),
('c7c382c3-8429-4d3a-a6f8-28770dcb7f6f', '2527f6c2-6e60-4e21-aab7-aff17643d9cd', '7e4a9a64-0dca-4f28-8eef-09065d72ce47', 'Adding this to my weekend list immediately.', TIMESTAMP '2026-10-12 10:08:16.491886'),
('1d83a690-6e62-4083-b683-faba0b5d4529', '2527f6c2-6e60-4e21-aab7-aff17643d9cd', 'df49b286-b6db-4a84-bf6f-b1e34ea2bbf8', U&'Used the serving scaler for 8 people \2014 quantities were bang on.', TIMESTAMP '2026-10-12 04:08:16.493892'),
('9fdbbf05-d6ff-4500-aab2-9d9a050f527e', 'bbb8dce6-d1f4-4794-bd72-38c0784949ce', 'cd703a29-e725-444c-b8c5-0b21e4d47f85', 'Doubled it for a crowd and it scaled perfectly.', TIMESTAMP '2026-10-12 10:08:16.512897'),
('cba3030d-9556-447c-8f27-1b636f5a9a5e', 'bbb8dce6-d1f4-4794-bd72-38c0784949ce', '043cb315-8f5a-4b61-9a0c-a0f35068fc80', 'The shopping list feature made this so easy to prep.', TIMESTAMP '2026-10-12 04:08:16.513893'),
('a93c6c37-0d46-4ccb-81fe-5986f783bd42', 'a1779ab3-b77a-4713-b927-bb524d6fc832', 'df49b286-b6db-4a84-bf6f-b1e34ea2bbf8', 'Any suggestions for making this dairy-free?', TIMESTAMP '2026-10-12 04:08:16.531903'),
('cb885ccc-6876-4df0-9c73-d0de5e68f7d2', 'd97f5cb0-e8ec-4067-8e33-6ba654e042d5', 'cd703a29-e725-444c-b8c5-0b21e4d47f85', 'The shopping list feature made this so easy to prep.', TIMESTAMP '2026-10-12 10:08:16.550461'),
('2781b749-0595-4499-a31b-25b9bb35a1cd', 'd97f5cb0-e8ec-4067-8e33-6ba654e042d5', '043cb315-8f5a-4b61-9a0c-a0f35068fc80', 'Adding this to my weekend list immediately.', TIMESTAMP '2026-10-12 04:08:16.55144'),
('1d8a5b6f-7e71-44df-b16f-3f76f3390daf', '19a29845-f4af-4e5f-8978-51d5ce89fd13', '7e4a9a64-0dca-4f28-8eef-09065d72ce47', 'Any suggestions for making this dairy-free?', TIMESTAMP '2026-10-12 10:08:16.56845'),
('dd0dda0d-520a-4640-ad65-c43be2b74363', '8d3646f5-7627-4dec-8458-8e2bcb1c6534', 'cd703a29-e725-444c-b8c5-0b21e4d47f85', 'Adding this to my weekend list immediately.', TIMESTAMP '2026-10-12 10:08:16.587443'),
('e04a18e0-af75-4c2e-a55b-a258133c1a9e', '8d3646f5-7627-4dec-8458-8e2bcb1c6534', '043cb315-8f5a-4b61-9a0c-a0f35068fc80', U&'Used the serving scaler for 8 people \2014 quantities were bang on.', TIMESTAMP '2026-10-12 04:08:16.58944'),
('c70001f4-eb45-4fe3-9d35-e123686b0e99', 'a9539172-91d5-4b53-97e5-2c6b6057fa0b', '7e4a9a64-0dca-4f28-8eef-09065d72ce47', 'Doubled it for a crowd and it scaled perfectly.', TIMESTAMP '2026-10-12 10:08:16.604454'),
('62b64974-ede5-46f5-b53e-2e807bd3aec1', 'a9539172-91d5-4b53-97e5-2c6b6057fa0b', 'df49b286-b6db-4a84-bf6f-b1e34ea2bbf8', 'The shopping list feature made this so easy to prep.', TIMESTAMP '2026-10-12 04:08:16.606455'),
('fd564ea2-c2b0-4f26-bf5c-9dc7c011110a', '8a300c40-80ef-4493-b2df-fbf8b9d00796', 'cd703a29-e725-444c-b8c5-0b21e4d47f85', U&'Used the serving scaler for 8 people \2014 quantities were bang on.', TIMESTAMP '2026-10-12 10:08:16.624449'),
('061fbeba-ee70-45a9-82a7-5fa9b3aa77b4', '8a300c40-80ef-4493-b2df-fbf8b9d00796', '043cb315-8f5a-4b61-9a0c-a0f35068fc80', 'Any suggestions for making this dairy-free?', TIMESTAMP '2026-10-12 04:08:16.626451'),
('cd2e5256-3b51-47f9-be7d-39aef7884e5c', '59405b37-98b2-4e28-8f85-c6d2b14e1024', 'df49b286-b6db-4a84-bf6f-b1e34ea2bbf8', 'Adding this to my weekend list immediately.', TIMESTAMP '2026-10-12 04:08:16.644038'),
('343a1ed0-adce-4d42-a7a8-365b64275b1e', '11bd7336-73d7-47ab-9b66-ae9611d40c2f', 'cd703a29-e725-444c-b8c5-0b21e4d47f85', 'Any suggestions for making this dairy-free?', TIMESTAMP '2026-10-12 10:08:16.662041');           
CREATE INDEX "PUBLIC"."IDX_COMMENTS_RECIPE" ON "PUBLIC"."COMMENTS"("RECIPE_ID" NULLS FIRST, "CREATED_AT" NULLS FIRST);        
CREATE CACHED TABLE "PUBLIC"."LIKES"(
    "USER_ID" CHARACTER VARYING(36) NOT NULL,
    "RECIPE_ID" CHARACTER VARYING(36) NOT NULL,
    "CREATED_AT" TIMESTAMP NOT NULL
);
ALTER TABLE "PUBLIC"."LIKES" ADD CONSTRAINT "PUBLIC"."CONSTRAINT_45" PRIMARY KEY("USER_ID", "RECIPE_ID");     
-- 40 +/- SELECT COUNT(*) FROM PUBLIC.LIKES;  
INSERT INTO "PUBLIC"."LIKES" VALUES
('a1940738-2ee5-4b84-9b3c-bc61d9f7bf93', 'abb850b1-551c-4d21-9c8a-0b87b5e54ef5', TIMESTAMP '2026-10-12 16:08:16.443881'),
('df49b286-b6db-4a84-bf6f-b1e34ea2bbf8', 'abb850b1-551c-4d21-9c8a-0b87b5e54ef5', TIMESTAMP '2026-10-12 16:08:16.445882'),
('cd703a29-e725-444c-b8c5-0b21e4d47f85', 'abb850b1-551c-4d21-9c8a-0b87b5e54ef5', TIMESTAMP '2026-10-12 16:08:16.447884'),
('a1940738-2ee5-4b84-9b3c-bc61d9f7bf93', 'ede1d89c-2897-4b10-bc76-5ebc3705f6ad', TIMESTAMP '2026-10-12 16:08:16.46889'),
('7e4a9a64-0dca-4f28-8eef-09065d72ce47', 'ede1d89c-2897-4b10-bc76-5ebc3705f6ad', TIMESTAMP '2026-10-12 16:08:16.472892'),
('df49b286-b6db-4a84-bf6f-b1e34ea2bbf8', 'ede1d89c-2897-4b10-bc76-5ebc3705f6ad', TIMESTAMP '2026-10-12 16:08:16.475895'),
('d8365c12-b600-42ce-ad5e-4be8b2d51a41', 'ede1d89c-2897-4b10-bc76-5ebc3705f6ad', TIMESTAMP '2026-10-12 16:08:16.476903'),
('cd703a29-e725-444c-b8c5-0b21e4d47f85', 'ede1d89c-2897-4b10-bc76-5ebc3705f6ad', TIMESTAMP '2026-10-12 16:08:16.477883'),
('7e4a9a64-0dca-4f28-8eef-09065d72ce47', '2527f6c2-6e60-4e21-aab7-aff17643d9cd', TIMESTAMP '2026-10-12 16:08:16.494886'),
('df49b286-b6db-4a84-bf6f-b1e34ea2bbf8', '2527f6c2-6e60-4e21-aab7-aff17643d9cd', TIMESTAMP '2026-10-12 16:08:16.496889'),
('d8365c12-b600-42ce-ad5e-4be8b2d51a41', '2527f6c2-6e60-4e21-aab7-aff17643d9cd', TIMESTAMP '2026-10-12 16:08:16.498889'),
('cd703a29-e725-444c-b8c5-0b21e4d47f85', '2527f6c2-6e60-4e21-aab7-aff17643d9cd', TIMESTAMP '2026-10-12 16:08:16.499885'),
('043cb315-8f5a-4b61-9a0c-a0f35068fc80', 'bbb8dce6-d1f4-4794-bd72-38c0784949ce', TIMESTAMP '2026-10-12 16:08:16.51489'),
('df49b286-b6db-4a84-bf6f-b1e34ea2bbf8', 'bbb8dce6-d1f4-4794-bd72-38c0784949ce', TIMESTAMP '2026-10-12 16:08:16.518894'),
('043cb315-8f5a-4b61-9a0c-a0f35068fc80', 'a1779ab3-b77a-4713-b927-bb524d6fc832', TIMESTAMP '2026-10-12 16:08:16.532907'),
('a1940738-2ee5-4b84-9b3c-bc61d9f7bf93', 'a1779ab3-b77a-4713-b927-bb524d6fc832', TIMESTAMP '2026-10-12 16:08:16.533908'),
('cd703a29-e725-444c-b8c5-0b21e4d47f85', 'a1779ab3-b77a-4713-b927-bb524d6fc832', TIMESTAMP '2026-10-12 16:08:16.535894'),
('a1940738-2ee5-4b84-9b3c-bc61d9f7bf93', 'd97f5cb0-e8ec-4067-8e33-6ba654e042d5', TIMESTAMP '2026-10-12 16:08:16.552446'),
('d8365c12-b600-42ce-ad5e-4be8b2d51a41', 'd97f5cb0-e8ec-4067-8e33-6ba654e042d5', TIMESTAMP '2026-10-12 16:08:16.555447'),
('a1940738-2ee5-4b84-9b3c-bc61d9f7bf93', '19a29845-f4af-4e5f-8978-51d5ce89fd13', TIMESTAMP '2026-10-12 16:08:16.571448'),
('7e4a9a64-0dca-4f28-8eef-09065d72ce47', '19a29845-f4af-4e5f-8978-51d5ce89fd13', TIMESTAMP '2026-10-12 16:08:16.573441'),
('d8365c12-b600-42ce-ad5e-4be8b2d51a41', '19a29845-f4af-4e5f-8978-51d5ce89fd13', TIMESTAMP '2026-10-12 16:08:16.575451'),
('cd703a29-e725-444c-b8c5-0b21e4d47f85', '19a29845-f4af-4e5f-8978-51d5ce89fd13', TIMESTAMP '2026-10-12 16:08:16.575451'),
('043cb315-8f5a-4b61-9a0c-a0f35068fc80', '8d3646f5-7627-4dec-8458-8e2bcb1c6534', TIMESTAMP '2026-10-12 16:08:16.590438'),
('a1940738-2ee5-4b84-9b3c-bc61d9f7bf93', '8d3646f5-7627-4dec-8458-8e2bcb1c6534', TIMESTAMP '2026-10-12 16:08:16.592443'),
('cd703a29-e725-444c-b8c5-0b21e4d47f85', '8d3646f5-7627-4dec-8458-8e2bcb1c6534', TIMESTAMP '2026-10-12 16:08:16.593446'),
('043cb315-8f5a-4b61-9a0c-a0f35068fc80', 'a9539172-91d5-4b53-97e5-2c6b6057fa0b', TIMESTAMP '2026-10-12 16:08:16.607446'),
('7e4a9a64-0dca-4f28-8eef-09065d72ce47', 'a9539172-91d5-4b53-97e5-2c6b6057fa0b', TIMESTAMP '2026-10-12 16:08:16.608453'),
('df49b286-b6db-4a84-bf6f-b1e34ea2bbf8', 'a9539172-91d5-4b53-97e5-2c6b6057fa0b', TIMESTAMP '2026-10-12 16:08:16.610444'),
('cd703a29-e725-444c-b8c5-0b21e4d47f85', 'a9539172-91d5-4b53-97e5-2c6b6057fa0b', TIMESTAMP '2026-10-12 16:08:16.611441'),
('043cb315-8f5a-4b61-9a0c-a0f35068fc80', '8a300c40-80ef-4493-b2df-fbf8b9d00796', TIMESTAMP '2026-10-12 16:08:16.627481'),
('a1940738-2ee5-4b84-9b3c-bc61d9f7bf93', '8a300c40-80ef-4493-b2df-fbf8b9d00796', TIMESTAMP '2026-10-12 16:08:16.628507'),
('cd703a29-e725-444c-b8c5-0b21e4d47f85', '8a300c40-80ef-4493-b2df-fbf8b9d00796', TIMESTAMP '2026-10-12 16:08:16.629519'),
('043cb315-8f5a-4b61-9a0c-a0f35068fc80', '59405b37-98b2-4e28-8f85-c6d2b14e1024', TIMESTAMP '2026-10-12 16:08:16.645045');       
INSERT INTO "PUBLIC"."LIKES" VALUES
('a1940738-2ee5-4b84-9b3c-bc61d9f7bf93', '59405b37-98b2-4e28-8f85-c6d2b14e1024', TIMESTAMP '2026-10-12 16:08:16.645045'),
('df49b286-b6db-4a84-bf6f-b1e34ea2bbf8', '59405b37-98b2-4e28-8f85-c6d2b14e1024', TIMESTAMP '2026-10-12 16:08:16.648054'),
('d8365c12-b600-42ce-ad5e-4be8b2d51a41', '59405b37-98b2-4e28-8f85-c6d2b14e1024', TIMESTAMP '2026-10-12 16:08:16.649057'),
('cd703a29-e725-444c-b8c5-0b21e4d47f85', '59405b37-98b2-4e28-8f85-c6d2b14e1024', TIMESTAMP '2026-10-12 16:08:16.650042'),
('7e4a9a64-0dca-4f28-8eef-09065d72ce47', '11bd7336-73d7-47ab-9b66-ae9611d40c2f', TIMESTAMP '2026-10-12 16:08:16.665036'),
('cd703a29-e725-444c-b8c5-0b21e4d47f85', '11bd7336-73d7-47ab-9b66-ae9611d40c2f', TIMESTAMP '2026-10-12 16:08:16.668037');         
CREATE INDEX "PUBLIC"."IDX_LIKES_RECIPE" ON "PUBLIC"."LIKES"("RECIPE_ID" NULLS FIRST);        
CREATE CACHED TABLE "PUBLIC"."FAVORITES"(
    "USER_ID" CHARACTER VARYING(36) NOT NULL,
    "RECIPE_ID" CHARACTER VARYING(36) NOT NULL,
    "CREATED_AT" TIMESTAMP NOT NULL
);            
ALTER TABLE "PUBLIC"."FAVORITES" ADD CONSTRAINT "PUBLIC"."CONSTRAINT_3" PRIMARY KEY("USER_ID", "RECIPE_ID");  
-- 31 +/- SELECT COUNT(*) FROM PUBLIC.FAVORITES;              
INSERT INTO "PUBLIC"."FAVORITES" VALUES
('7e4a9a64-0dca-4f28-8eef-09065d72ce47', 'abb850b1-551c-4d21-9c8a-0b87b5e54ef5', TIMESTAMP '2026-10-12 16:08:16.444882'),
('df49b286-b6db-4a84-bf6f-b1e34ea2bbf8', 'abb850b1-551c-4d21-9c8a-0b87b5e54ef5', TIMESTAMP '2026-10-12 16:08:16.446884'),
('a1940738-2ee5-4b84-9b3c-bc61d9f7bf93', 'ede1d89c-2897-4b10-bc76-5ebc3705f6ad', TIMESTAMP '2026-10-12 16:08:16.470889'),
('7e4a9a64-0dca-4f28-8eef-09065d72ce47', 'ede1d89c-2897-4b10-bc76-5ebc3705f6ad', TIMESTAMP '2026-10-12 16:08:16.473894'),
('d8365c12-b600-42ce-ad5e-4be8b2d51a41', 'ede1d89c-2897-4b10-bc76-5ebc3705f6ad', TIMESTAMP '2026-10-12 16:08:16.476903'),
('cd703a29-e725-444c-b8c5-0b21e4d47f85', 'ede1d89c-2897-4b10-bc76-5ebc3705f6ad', TIMESTAMP '2026-10-12 16:08:16.478895'),
('7e4a9a64-0dca-4f28-8eef-09065d72ce47', '2527f6c2-6e60-4e21-aab7-aff17643d9cd', TIMESTAMP '2026-10-12 16:08:16.495895'),
('df49b286-b6db-4a84-bf6f-b1e34ea2bbf8', '2527f6c2-6e60-4e21-aab7-aff17643d9cd', TIMESTAMP '2026-10-12 16:08:16.497881'),
('cd703a29-e725-444c-b8c5-0b21e4d47f85', '2527f6c2-6e60-4e21-aab7-aff17643d9cd', TIMESTAMP '2026-10-12 16:08:16.500894'),
('043cb315-8f5a-4b61-9a0c-a0f35068fc80', 'bbb8dce6-d1f4-4794-bd72-38c0784949ce', TIMESTAMP '2026-10-12 16:08:16.516891'),
('7e4a9a64-0dca-4f28-8eef-09065d72ce47', 'bbb8dce6-d1f4-4794-bd72-38c0784949ce', TIMESTAMP '2026-10-12 16:08:16.517886'),
('d8365c12-b600-42ce-ad5e-4be8b2d51a41', 'bbb8dce6-d1f4-4794-bd72-38c0784949ce', TIMESTAMP '2026-10-12 16:08:16.519892'),
('a1940738-2ee5-4b84-9b3c-bc61d9f7bf93', 'a1779ab3-b77a-4713-b927-bb524d6fc832', TIMESTAMP '2026-10-12 16:08:16.534897'),
('cd703a29-e725-444c-b8c5-0b21e4d47f85', 'a1779ab3-b77a-4713-b927-bb524d6fc832', TIMESTAMP '2026-10-12 16:08:16.537905'),
('df49b286-b6db-4a84-bf6f-b1e34ea2bbf8', 'd97f5cb0-e8ec-4067-8e33-6ba654e042d5', TIMESTAMP '2026-10-12 16:08:16.554442'),
('d8365c12-b600-42ce-ad5e-4be8b2d51a41', 'd97f5cb0-e8ec-4067-8e33-6ba654e042d5', TIMESTAMP '2026-10-12 16:08:16.555447'),
('043cb315-8f5a-4b61-9a0c-a0f35068fc80', '19a29845-f4af-4e5f-8978-51d5ce89fd13', TIMESTAMP '2026-10-12 16:08:16.569442'),
('a1940738-2ee5-4b84-9b3c-bc61d9f7bf93', '19a29845-f4af-4e5f-8978-51d5ce89fd13', TIMESTAMP '2026-10-12 16:08:16.572447'),
('cd703a29-e725-444c-b8c5-0b21e4d47f85', '19a29845-f4af-4e5f-8978-51d5ce89fd13', TIMESTAMP '2026-10-12 16:08:16.576441'),
('043cb315-8f5a-4b61-9a0c-a0f35068fc80', '8d3646f5-7627-4dec-8458-8e2bcb1c6534', TIMESTAMP '2026-10-12 16:08:16.591446'),
('7e4a9a64-0dca-4f28-8eef-09065d72ce47', 'a9539172-91d5-4b53-97e5-2c6b6057fa0b', TIMESTAMP '2026-10-12 16:08:16.609444'),
('df49b286-b6db-4a84-bf6f-b1e34ea2bbf8', 'a9539172-91d5-4b53-97e5-2c6b6057fa0b', TIMESTAMP '2026-10-12 16:08:16.611441'),
('cd703a29-e725-444c-b8c5-0b21e4d47f85', 'a9539172-91d5-4b53-97e5-2c6b6057fa0b', TIMESTAMP '2026-10-12 16:08:16.612445'),
('7e4a9a64-0dca-4f28-8eef-09065d72ce47', '8a300c40-80ef-4493-b2df-fbf8b9d00796', TIMESTAMP '2026-10-12 16:08:16.628507'),
('a1940738-2ee5-4b84-9b3c-bc61d9f7bf93', '59405b37-98b2-4e28-8f85-c6d2b14e1024', TIMESTAMP '2026-10-12 16:08:16.646044'),
('d8365c12-b600-42ce-ad5e-4be8b2d51a41', '59405b37-98b2-4e28-8f85-c6d2b14e1024', TIMESTAMP '2026-10-12 16:08:16.649057'),
('cd703a29-e725-444c-b8c5-0b21e4d47f85', '59405b37-98b2-4e28-8f85-c6d2b14e1024', TIMESTAMP '2026-10-12 16:08:16.651045'),
('a1940738-2ee5-4b84-9b3c-bc61d9f7bf93', '11bd7336-73d7-47ab-9b66-ae9611d40c2f', TIMESTAMP '2026-10-12 16:08:16.664038'),
('7e4a9a64-0dca-4f28-8eef-09065d72ce47', '11bd7336-73d7-47ab-9b66-ae9611d40c2f', TIMESTAMP '2026-10-12 16:08:16.666039'),
('d8365c12-b600-42ce-ad5e-4be8b2d51a41', '11bd7336-73d7-47ab-9b66-ae9611d40c2f', TIMESTAMP '2026-10-12 16:08:16.667047'),
('cd703a29-e725-444c-b8c5-0b21e4d47f85', '11bd7336-73d7-47ab-9b66-ae9611d40c2f', TIMESTAMP '2026-10-12 16:08:16.668037');  
CREATE INDEX "PUBLIC"."IDX_FAVORITES_RECIPE" ON "PUBLIC"."FAVORITES"("RECIPE_ID" NULLS FIRST);
CREATE CACHED TABLE "PUBLIC"."FOLLOWS"(
    "FOLLOWER_ID" CHARACTER VARYING(36) NOT NULL,
    "FOLLOWING_ID" CHARACTER VARYING(36) NOT NULL,
    "CREATED_AT" TIMESTAMP NOT NULL
);       
ALTER TABLE "PUBLIC"."FOLLOWS" ADD CONSTRAINT "PUBLIC"."CONSTRAINT_2" PRIMARY KEY("FOLLOWER_ID", "FOLLOWING_ID");             
-- 12 +/- SELECT COUNT(*) FROM PUBLIC.FOLLOWS;
INSERT INTO "PUBLIC"."FOLLOWS" VALUES
('043cb315-8f5a-4b61-9a0c-a0f35068fc80', 'df49b286-b6db-4a84-bf6f-b1e34ea2bbf8', TIMESTAMP '2026-10-12 16:08:16.409336'),
('043cb315-8f5a-4b61-9a0c-a0f35068fc80', 'cd703a29-e725-444c-b8c5-0b21e4d47f85', TIMESTAMP '2026-10-12 16:08:16.410336'),
('a1940738-2ee5-4b84-9b3c-bc61d9f7bf93', '043cb315-8f5a-4b61-9a0c-a0f35068fc80', TIMESTAMP '2026-10-12 16:08:16.411337'),
('a1940738-2ee5-4b84-9b3c-bc61d9f7bf93', 'df49b286-b6db-4a84-bf6f-b1e34ea2bbf8', TIMESTAMP '2026-10-12 16:08:16.411337'),
('7e4a9a64-0dca-4f28-8eef-09065d72ce47', 'cd703a29-e725-444c-b8c5-0b21e4d47f85', TIMESTAMP '2026-10-12 16:08:16.412337'),
('df49b286-b6db-4a84-bf6f-b1e34ea2bbf8', '043cb315-8f5a-4b61-9a0c-a0f35068fc80', TIMESTAMP '2026-10-12 16:08:16.413338'),
('df49b286-b6db-4a84-bf6f-b1e34ea2bbf8', 'd8365c12-b600-42ce-ad5e-4be8b2d51a41', TIMESTAMP '2026-10-12 16:08:16.414342'),
('d8365c12-b600-42ce-ad5e-4be8b2d51a41', 'a1940738-2ee5-4b84-9b3c-bc61d9f7bf93', TIMESTAMP '2026-10-12 16:08:16.415346'),
('d8365c12-b600-42ce-ad5e-4be8b2d51a41', '7e4a9a64-0dca-4f28-8eef-09065d72ce47', TIMESTAMP '2026-10-12 16:08:16.415346'),
('d8365c12-b600-42ce-ad5e-4be8b2d51a41', 'cd703a29-e725-444c-b8c5-0b21e4d47f85', TIMESTAMP '2026-10-12 16:08:16.416334'),
('cd703a29-e725-444c-b8c5-0b21e4d47f85', 'a1940738-2ee5-4b84-9b3c-bc61d9f7bf93', TIMESTAMP '2026-10-12 16:08:16.417338'),
('cd703a29-e725-444c-b8c5-0b21e4d47f85', 'd8365c12-b600-42ce-ad5e-4be8b2d51a41', TIMESTAMP '2026-10-12 16:08:16.417338');     
CREATE INDEX "PUBLIC"."IDX_FOLLOWS_FOLLOWING" ON "PUBLIC"."FOLLOWS"("FOLLOWING_ID" NULLS FIRST);              
CREATE CACHED TABLE "PUBLIC"."RECENTLY_VIEWED"(
    "USER_ID" CHARACTER VARYING(36) NOT NULL,
    "RECIPE_ID" CHARACTER VARYING(36) NOT NULL,
    "VIEWED_AT" TIMESTAMP NOT NULL
);       
ALTER TABLE "PUBLIC"."RECENTLY_VIEWED" ADD CONSTRAINT "PUBLIC"."CONSTRAINT_7" PRIMARY KEY("USER_ID", "RECIPE_ID");            
-- 0 +/- SELECT COUNT(*) FROM PUBLIC.RECENTLY_VIEWED;         
CREATE INDEX "PUBLIC"."IDX_RV_USER" ON "PUBLIC"."RECENTLY_VIEWED"("USER_ID" NULLS FIRST, "VIEWED_AT" NULLS FIRST);            
CREATE CACHED TABLE "PUBLIC"."SHOPPING_LIST_ITEMS"(
    "ID" CHARACTER VARYING(36) NOT NULL,
    "USER_ID" CHARACTER VARYING(36) NOT NULL,
    "NAME" CHARACTER VARYING(120) NOT NULL,
    "QUANTITY" DECIMAL(10, 3) DEFAULT 1 NOT NULL,
    "UNIT" CHARACTER VARYING(30) DEFAULT 'piece' NOT NULL,
    "CHECKED" TINYINT DEFAULT 0 NOT NULL,
    "RECIPE_TITLE" CHARACTER VARYING(140),
    "POSITION" INTEGER DEFAULT 0 NOT NULL
);
ALTER TABLE "PUBLIC"."SHOPPING_LIST_ITEMS" ADD CONSTRAINT "PUBLIC"."CONSTRAINT_62" PRIMARY KEY("ID");         
-- 0 +/- SELECT COUNT(*) FROM PUBLIC.SHOPPING_LIST_ITEMS;     
CREATE INDEX "PUBLIC"."IDX_SLI_USER" ON "PUBLIC"."SHOPPING_LIST_ITEMS"("USER_ID" NULLS FIRST, "POSITION" NULLS FIRST);        
CREATE CACHED TABLE "PUBLIC"."REPORTS"(
    "ID" CHARACTER VARYING(36) NOT NULL,
    "TARGET_TYPE" CHARACTER VARYING(10) NOT NULL,
    "TARGET_ID" CHARACTER VARYING(36) NOT NULL,
    "REASON" CHARACTER VARYING(500) NOT NULL,
    "REPORTER_ID" CHARACTER VARYING(36) NOT NULL,
    "STATUS" CHARACTER VARYING(10) DEFAULT 'PENDING' NOT NULL,
    "CREATED_AT" TIMESTAMP NOT NULL
);              
ALTER TABLE "PUBLIC"."REPORTS" ADD CONSTRAINT "PUBLIC"."CONSTRAINT_6C0" PRIMARY KEY("ID");    
-- 0 +/- SELECT COUNT(*) FROM PUBLIC.REPORTS; 
CREATE INDEX "PUBLIC"."IDX_REPORTS_STATUS" ON "PUBLIC"."REPORTS"("STATUS" NULLS FIRST, "CREATED_AT" NULLS FIRST);             
CREATE CACHED TABLE "PUBLIC"."AI_MESSAGES"(
    "ID" CHARACTER VARYING(36) NOT NULL,
    "USER_ID" CHARACTER VARYING(36) NOT NULL,
    "SESSION_ID" CHARACTER VARYING(64) NOT NULL,
    "ROLE" CHARACTER VARYING(10) NOT NULL,
    "CONTENT" CHARACTER VARYING NOT NULL,
    "CREATED_AT" TIMESTAMP NOT NULL
);        
ALTER TABLE "PUBLIC"."AI_MESSAGES" ADD CONSTRAINT "PUBLIC"."CONSTRAINT_8F" PRIMARY KEY("ID"); 
-- 0 +/- SELECT COUNT(*) FROM PUBLIC.AI_MESSAGES;             
CREATE INDEX "PUBLIC"."IDX_AI_SESSION" ON "PUBLIC"."AI_MESSAGES"("SESSION_ID" NULLS FIRST, "CREATED_AT" NULLS FIRST);         
CREATE CACHED TABLE "PUBLIC"."MEAL_PLAN_ENTRIES"(
    "ID" CHARACTER VARYING(36) NOT NULL,
    "USER_ID" CHARACTER VARYING(36) NOT NULL,
    "RECIPE_ID" CHARACTER VARYING(36) NOT NULL,
    "PLAN_DATE" DATE NOT NULL,
    "SLOT" CHARACTER VARYING(12) NOT NULL,
    "SERVINGS" INTEGER DEFAULT 2 NOT NULL,
    "CREATED_AT" TIMESTAMP NOT NULL
);  
ALTER TABLE "PUBLIC"."MEAL_PLAN_ENTRIES" ADD CONSTRAINT "PUBLIC"."CONSTRAINT_9" PRIMARY KEY("ID");            
-- 0 +/- SELECT COUNT(*) FROM PUBLIC.MEAL_PLAN_ENTRIES;       
CREATE INDEX "PUBLIC"."IDX_MPE_USER_DATE" ON "PUBLIC"."MEAL_PLAN_ENTRIES"("USER_ID" NULLS FIRST, "PLAN_DATE" NULLS FIRST);    
ALTER TABLE "PUBLIC"."USERS" ADD CONSTRAINT "PUBLIC"."UK_USERS_USERNAME" UNIQUE("USERNAME");  
ALTER TABLE "PUBLIC"."USERS" ADD CONSTRAINT "PUBLIC"."UK_USERS_EMAIL" UNIQUE("EMAIL");        
ALTER TABLE "PUBLIC"."REVIEWS" ADD CONSTRAINT "PUBLIC"."UK_REVIEWS_RECIPE_USER" UNIQUE("RECIPE_ID", "USER_ID");               
ALTER TABLE "PUBLIC"."CATEGORIES" ADD CONSTRAINT "PUBLIC"."UK_CATEGORIES_SLUG" UNIQUE("SLUG");
