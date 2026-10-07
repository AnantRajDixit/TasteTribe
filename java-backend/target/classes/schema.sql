-- TasteTribe schema (MySQL dialect; also loads on H2 in MySQL compatibility mode).
-- Normalized relational design: recipe_ingredients is a proper child table of recipes
-- (ingredient name / quantity / unit / optionality), not one big text blob.

CREATE TABLE IF NOT EXISTS users (
  id            VARCHAR(36) PRIMARY KEY,
  name          VARCHAR(80)  NOT NULL,
  username      VARCHAR(30)  NOT NULL,
  email         VARCHAR(120) NOT NULL,
  password_hash VARCHAR(100) NOT NULL,
  avatar_url    VARCHAR(500),
  bio           VARCHAR(400) DEFAULT '',
  role          VARCHAR(10)  NOT NULL DEFAULT 'USER',
  created_at    DATETIME     NOT NULL,
  UNIQUE KEY uk_users_username (username),
  UNIQUE KEY uk_users_email (email)
);

CREATE TABLE IF NOT EXISTS sessions (
  token      VARCHAR(64) PRIMARY KEY,
  user_id    VARCHAR(36) NOT NULL,
  created_at DATETIME NOT NULL,
  expires_at DATETIME NOT NULL,
  KEY idx_sessions_user (user_id)
);

CREATE TABLE IF NOT EXISTS password_resets (
  token      VARCHAR(64) PRIMARY KEY,
  user_id    VARCHAR(36) NOT NULL,
  created_at DATETIME NOT NULL,
  expires_at DATETIME NOT NULL,
  KEY idx_pwresets_user (user_id)
);

CREATE TABLE IF NOT EXISTS categories (
  id          VARCHAR(36) PRIMARY KEY,
  name        VARCHAR(60) NOT NULL,
  slug        VARCHAR(60) NOT NULL,
  description VARCHAR(300) DEFAULT '',
  image_url   VARCHAR(500),
  created_at  DATETIME NOT NULL,
  UNIQUE KEY uk_categories_slug (slug)
);

CREATE TABLE IF NOT EXISTS recipes (
  id              VARCHAR(36) PRIMARY KEY,
  title           VARCHAR(140) NOT NULL,
  description     VARCHAR(2000) DEFAULT '',
  cover_image     VARCHAR(500),
  cuisine         VARCHAR(60) NOT NULL,
  category        VARCHAR(60) NOT NULL,
  difficulty      VARCHAR(10) NOT NULL,
  dietary         VARCHAR(30) NOT NULL DEFAULT 'Non-Vegetarian',
  prep_time       INT NOT NULL,
  cook_time       INT NOT NULL,
  total_time      INT NOT NULL,
  servings        INT NOT NULL DEFAULT 2,
  status          VARCHAR(10) NOT NULL DEFAULT 'PUBLISHED',
  source          VARCHAR(10) NOT NULL DEFAULT 'user',
  views           INT NOT NULL DEFAULT 0,
  likes_count     INT NOT NULL DEFAULT 0,
  favorites_count INT NOT NULL DEFAULT 0,
  avg_rating      DECIMAL(3,2),
  ratings_count   INT NOT NULL DEFAULT 0,
  tags_json       TEXT,
  instructions_json TEXT,
  nutrition_json  TEXT,
  author_id       VARCHAR(36) NOT NULL,
  author_name     VARCHAR(80) DEFAULT '',
  author_username VARCHAR(30) DEFAULT '',
  created_at      DATETIME NOT NULL,
  updated_at      DATETIME NOT NULL,
  KEY idx_recipes_status_created (status, created_at),
  KEY idx_recipes_author (author_id),
  KEY idx_recipes_category (category),
  KEY idx_recipes_cuisine (cuisine),
  KEY idx_recipes_rating (avg_rating)
);

-- Structured ingredients: one row per (recipe, ingredient).
CREATE TABLE IF NOT EXISTS recipe_ingredients (
  id         BIGINT AUTO_INCREMENT PRIMARY KEY,
  recipe_id  VARCHAR(36) NOT NULL,
  name       VARCHAR(120) NOT NULL,
  quantity   DECIMAL(10,3) NOT NULL,
  unit       VARCHAR(30) NOT NULL DEFAULT 'piece',
  is_optional TINYINT NOT NULL DEFAULT 0,
  position   INT NOT NULL DEFAULT 0,
  KEY idx_rin (recipe_id, position)
);

CREATE TABLE IF NOT EXISTS reviews (
  id         VARCHAR(36) PRIMARY KEY,
  recipe_id  VARCHAR(36) NOT NULL,
  user_id    VARCHAR(36) NOT NULL,
  rating     TINYINT NOT NULL,
  comment    VARCHAR(2000),
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  UNIQUE KEY uk_reviews_recipe_user (recipe_id, user_id),
  KEY idx_reviews_recipe (recipe_id)
);

CREATE TABLE IF NOT EXISTS comments (
  id         VARCHAR(36) PRIMARY KEY,
  recipe_id  VARCHAR(36) NOT NULL,
  user_id    VARCHAR(36) NOT NULL,
  text       VARCHAR(1000) NOT NULL,
  created_at DATETIME NOT NULL,
  KEY idx_comments_recipe (recipe_id, created_at)
);

CREATE TABLE IF NOT EXISTS likes (
  user_id    VARCHAR(36) NOT NULL,
  recipe_id  VARCHAR(36) NOT NULL,
  created_at DATETIME NOT NULL,
  PRIMARY KEY (user_id, recipe_id),
  KEY idx_likes_recipe (recipe_id)
);

CREATE TABLE IF NOT EXISTS favorites (
  user_id    VARCHAR(36) NOT NULL,
  recipe_id  VARCHAR(36) NOT NULL,
  created_at DATETIME NOT NULL,
  PRIMARY KEY (user_id, recipe_id),
  KEY idx_favorites_recipe (recipe_id)
);

CREATE TABLE IF NOT EXISTS follows (
  follower_id  VARCHAR(36) NOT NULL,
  following_id VARCHAR(36) NOT NULL,
  created_at   DATETIME NOT NULL,
  PRIMARY KEY (follower_id, following_id),
  KEY idx_follows_following (following_id)
);

CREATE TABLE IF NOT EXISTS recently_viewed (
  user_id   VARCHAR(36) NOT NULL,
  recipe_id VARCHAR(36) NOT NULL,
  viewed_at DATETIME NOT NULL,
  PRIMARY KEY (user_id, recipe_id),
  KEY idx_rv_user (user_id, viewed_at)
);

CREATE TABLE IF NOT EXISTS shopping_list_items (
  id           VARCHAR(36) PRIMARY KEY,
  user_id      VARCHAR(36) NOT NULL,
  name         VARCHAR(120) NOT NULL,
  quantity     DECIMAL(10,3) NOT NULL DEFAULT 1,
  unit         VARCHAR(30) NOT NULL DEFAULT 'piece',
  checked      TINYINT NOT NULL DEFAULT 0,
  recipe_title VARCHAR(140),
  position     INT NOT NULL DEFAULT 0,
  KEY idx_sli_user (user_id, position)
);

CREATE TABLE IF NOT EXISTS reports (
  id          VARCHAR(36) PRIMARY KEY,
  target_type VARCHAR(10) NOT NULL,
  target_id   VARCHAR(36) NOT NULL,
  reason      VARCHAR(500) NOT NULL,
  reporter_id VARCHAR(36) NOT NULL,
  status      VARCHAR(10) NOT NULL DEFAULT 'PENDING',
  created_at  DATETIME NOT NULL,
  KEY idx_reports_status (status, created_at)
);

CREATE TABLE IF NOT EXISTS ai_messages (
  id         VARCHAR(36) PRIMARY KEY,
  user_id    VARCHAR(36) NOT NULL,
  session_id VARCHAR(64) NOT NULL,
  role       VARCHAR(10) NOT NULL,
  content    TEXT NOT NULL,
  created_at DATETIME NOT NULL,
  KEY idx_ai_session (session_id, created_at)
);
