package com.richfield.smartpantry;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Local SQLite database (SQLiteOpenHelper). Stores the user's pantry (full CRUD) and the pre-loaded
 * recipe collection, which is seeded the first time the database is created.
 * Data lives in a file on the device so it survives closing and reopening the app.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "smart_pantry.db";
    private static final int DB_VERSION = 1;

    private static final String T_PANTRY = "pantry";
    private static final String T_RECIPES = "recipes";
    private static final String T_INGREDIENTS = "recipe_ingredients";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + T_PANTRY + " (id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL, quantity REAL NOT NULL, unit TEXT NOT NULL, expiry TEXT)");
        db.execSQL("CREATE TABLE " + T_RECIPES + " (id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL, steps TEXT NOT NULL)");
        db.execSQL("CREATE TABLE " + T_INGREDIENTS + " (id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "recipe_id INTEGER NOT NULL, name TEXT NOT NULL, quantity REAL NOT NULL, unit TEXT NOT NULL, "
                + "FOREIGN KEY(recipe_id) REFERENCES " + T_RECIPES + "(id))");
        seedRecipes(db); // pre-load recipes on first run
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + T_INGREDIENTS);
        db.execSQL("DROP TABLE IF EXISTS " + T_RECIPES);
        db.execSQL("DROP TABLE IF EXISTS " + T_PANTRY);
        onCreate(db);
    }

    // ------------------------------------------------------------------ PANTRY CRUD

    private ContentValues toValues(PantryItem item) {
        ContentValues cv = new ContentValues();
        cv.put("name", item.getName());
        cv.put("quantity", item.getQuantity());
        cv.put("unit", item.getUnit());
        cv.put("expiry", item.getExpiry());
        return cv;
    }

    /** CREATE */
    public long insertItem(PantryItem item) {
        return getWritableDatabase().insert(T_PANTRY, null, toValues(item));
    }

    /** UPDATE */
    public int updateItem(PantryItem item) {
        return getWritableDatabase().update(T_PANTRY, toValues(item), "id=?",
                new String[]{String.valueOf(item.getId())});
    }

    /** DELETE */
    public int deleteItem(long id) {
        return getWritableDatabase().delete(T_PANTRY, "id=?", new String[]{String.valueOf(id)});
    }

    /** READ (all, alphabetical) */
    public List<PantryItem> getAllItems() {
        List<PantryItem> list = new ArrayList<>();
        try (Cursor c = getReadableDatabase().query(T_PANTRY, null, null, null, null, null,
                "name COLLATE NOCASE ASC")) {
            while (c.moveToNext()) {
                list.add(readItem(c));
            }
        }
        return list;
    }

    /** READ (one) - used to pre-fill the edit screen. */
    public PantryItem getItem(long id) {
        try (Cursor c = getReadableDatabase().query(T_PANTRY, null, "id=?",
                new String[]{String.valueOf(id)}, null, null, null)) {
            return c.moveToFirst() ? readItem(c) : null;
        }
    }

    private PantryItem readItem(Cursor c) {
        return new PantryItem(
                c.getLong(c.getColumnIndexOrThrow("id")),
                c.getString(c.getColumnIndexOrThrow("name")),
                c.getDouble(c.getColumnIndexOrThrow("quantity")),
                c.getString(c.getColumnIndexOrThrow("unit")),
                c.getString(c.getColumnIndexOrThrow("expiry")));
    }

    // ------------------------------------------------------------------ RECIPES

    /** Loads every recipe together with its ingredients. */
    public List<Recipe> getAllRecipes() {
        Map<Long, Recipe> map = new LinkedHashMap<>();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.query(T_RECIPES, null, null, null, null, null, "name ASC")) {
            while (c.moveToNext()) {
                long id = c.getLong(c.getColumnIndexOrThrow("id"));
                map.put(id, new Recipe(id, c.getString(c.getColumnIndexOrThrow("name")),
                        c.getString(c.getColumnIndexOrThrow("steps"))));
            }
        }
        try (Cursor c = db.query(T_INGREDIENTS, null, null, null, null, null, "id ASC")) {
            while (c.moveToNext()) {
                Recipe r = map.get(c.getLong(c.getColumnIndexOrThrow("recipe_id")));
                if (r != null) {
                    r.getIngredients().add(new RecipeIngredient(
                            c.getString(c.getColumnIndexOrThrow("name")),
                            c.getDouble(c.getColumnIndexOrThrow("quantity")),
                            c.getString(c.getColumnIndexOrThrow("unit"))));
                }
            }
        }
        return new ArrayList<>(map.values());
    }

    public Recipe getRecipe(long id) {
        for (Recipe r : getAllRecipes()) {
            if (r.getId() == id) {
                return r;
            }
        }
        return null;
    }

    // ------------------------------------------------------------------ SEED DATA

    /** Inserts one recipe; each ingredient string is "name|quantity|unit". */
    private void addRecipe(SQLiteDatabase db, String name, String steps, String... ingredients) {
        ContentValues rv = new ContentValues();
        rv.put("name", name);
        rv.put("steps", steps);
        long recipeId = db.insert(T_RECIPES, null, rv);
        for (String line : ingredients) {
            String[] p = line.split("\\|");
            ContentValues iv = new ContentValues();
            iv.put("recipe_id", recipeId);
            iv.put("name", p[0]);
            iv.put("quantity", Double.parseDouble(p[1]));
            iv.put("unit", p[2]);
            db.insert(T_INGREDIENTS, null, iv);
        }
    }

    private void seedRecipes(SQLiteDatabase db) {
        addRecipe(db, "Scrambled Eggs on Toast",
                "1. Whisk the eggs with the salt.\n2. Melt the butter in a pan over medium heat.\n3. Stir the eggs gently until just set.\n4. Toast the bread and pile the eggs on top.",
                "egg|3|piece", "bread|2|piece", "butter|15|g", "salt|2|g");
        addRecipe(db, "Tomato Pasta",
                "1. Boil the pasta in salted water until tender.\n2. Fry the chopped onion and garlic in olive oil.\n3. Add the chopped tomatoes and simmer for 10 minutes.\n4. Toss the pasta through the sauce.",
                "pasta|200|g", "tomato|3|piece", "onion|1|piece", "garlic|2|piece", "olive oil|15|ml", "salt|2|g");
        addRecipe(db, "Cheese Omelette",
                "1. Beat the eggs with the salt.\n2. Melt the butter in a pan and pour in the eggs.\n3. Sprinkle the cheese on one half and fold over.\n4. Cook for one more minute and serve.",
                "egg|3|piece", "cheese|50|g", "butter|10|g", "salt|2|g");
        addRecipe(db, "Egg Fried Rice",
                "1. Heat the oil and fry the chopped onion.\n2. Push to the side and scramble the eggs.\n3. Add the cooked rice and soy sauce.\n4. Stir-fry for 3 minutes and serve hot.",
                "rice|200|g", "egg|2|piece", "onion|1|piece", "soy sauce|15|ml", "oil|15|ml");
        addRecipe(db, "Peanut Butter Banana Toast",
                "1. Toast the bread.\n2. Spread the peanut butter on both slices.\n3. Slice the banana and arrange on top.",
                "bread|2|piece", "peanut butter|30|g", "banana|1|piece");
        addRecipe(db, "Pancakes",
                "1. Mix the flour and sugar, then whisk in the milk and egg until smooth.\n2. Melt a little butter in a pan.\n3. Pour in small ladles of batter and cook until bubbles form.\n4. Flip and cook for one more minute.",
                "flour|150|g", "milk|200|ml", "egg|1|piece", "sugar|20|g", "butter|15|g");
        addRecipe(db, "Buttered Rice",
                "1. Cook the rice in lightly salted water.\n2. Drain well.\n3. Stir the butter through the hot rice and serve.",
                "rice|150|g", "butter|20|g", "salt|2|g");
        addRecipe(db, "Cheese Toastie",
                "1. Butter the outside of both slices of bread.\n2. Place the cheese between the slices.\n3. Fry on both sides until golden and melted.",
                "bread|2|piece", "cheese|40|g", "butter|10|g");
        addRecipe(db, "Simple Tomato Soup",
                "1. Fry the onion and garlic in olive oil.\n2. Add the chopped tomatoes and stock.\n3. Simmer for 20 minutes and blend until smooth.\n4. Season with salt.",
                "tomato|5|piece", "onion|1|piece", "garlic|2|piece", "vegetable stock|500|ml", "salt|2|g", "olive oil|15|ml");
        addRecipe(db, "Creamy Mashed Potato",
                "1. Peel and boil the potatoes until soft.\n2. Drain and mash.\n3. Beat in the butter, warm milk and salt.",
                "potato|4|piece", "butter|30|g", "milk|60|ml", "salt|2|g");
        addRecipe(db, "Chicken Stir-Fry",
                "1. Slice the chicken, pepper and onion.\n2. Fry the chicken in hot oil until cooked through.\n3. Add the vegetables and soy sauce.\n4. Stir-fry for 4 minutes.",
                "chicken breast|300|g", "bell pepper|1|piece", "onion|1|piece", "soy sauce|30|ml", "oil|15|ml");
        addRecipe(db, "Chicken and Rice",
                "1. Season the chicken with salt and brown it with the chopped onion.\n2. Add the rice and enough water to cover.\n3. Cover and simmer for 20 minutes until the rice is cooked.",
                "chicken breast|300|g", "rice|200|g", "onion|1|piece", "salt|2|g");
        addRecipe(db, "Beans on Toast",
                "1. Warm the baked beans in a pan.\n2. Toast and butter the bread.\n3. Spoon the beans over the toast.",
                "baked beans|200|g", "bread|2|piece", "butter|10|g");
        addRecipe(db, "Tuna Pasta",
                "1. Boil the pasta until tender.\n2. Fry the onion in olive oil.\n3. Add the drained tuna and warm through.\n4. Toss with the pasta.",
                "pasta|200|g", "tuna|1|piece", "onion|1|piece", "olive oil|15|ml");
        addRecipe(db, "French Toast",
                "1. Whisk the eggs, milk and sugar together.\n2. Dip each slice of bread into the mixture.\n3. Fry in butter until golden on both sides.",
                "bread|4|piece", "egg|2|piece", "milk|100|ml", "sugar|10|g", "butter|10|g");
        addRecipe(db, "Vegetable Omelette",
                "1. Dice the pepper and onion and fry until soft.\n2. Beat the eggs with salt and pour over the vegetables.\n3. Cook until set and fold over.",
                "egg|3|piece", "bell pepper|1|piece", "onion|1|piece", "salt|2|g");
        addRecipe(db, "Fried Potatoes",
                "1. Dice the potatoes and onion.\n2. Heat the oil in a large pan.\n3. Fry for 15 minutes, turning often, until crisp.\n4. Season with salt.",
                "potato|4|piece", "oil|30|ml", "salt|2|g", "onion|1|piece");
        addRecipe(db, "Banana Oatmeal",
                "1. Heat the oats and milk in a pot, stirring.\n2. Cook for 5 minutes until creamy.\n3. Top with sliced banana and honey.",
                "oats|80|g", "milk|250|ml", "banana|1|piece", "honey|15|ml");
        addRecipe(db, "Mushroom Rice",
                "1. Fry the sliced mushrooms, onion and garlic in butter.\n2. Add the rice and stir for 1 minute.\n3. Add water, cover and simmer until the rice is cooked.",
                "rice|200|g", "mushroom|150|g", "onion|1|piece", "garlic|2|piece", "butter|15|g");
        addRecipe(db, "Cheesy Pasta Bake",
                "1. Boil the pasta and drain.\n2. Melt the butter, stir in the flour, then whisk in the milk to make a sauce.\n3. Stir in most of the cheese and mix with the pasta.\n4. Top with cheese and bake at 200 C for 20 minutes.",
                "pasta|250|g", "cheese|100|g", "milk|200|ml", "flour|20|g", "butter|20|g");
        addRecipe(db, "Garlic Bread",
                "1. Mash the butter with the crushed garlic.\n2. Spread over the bread.\n3. Bake at 200 C for 10 minutes until golden.",
                "bread|4|piece", "garlic|3|piece", "butter|40|g");
    }
}
