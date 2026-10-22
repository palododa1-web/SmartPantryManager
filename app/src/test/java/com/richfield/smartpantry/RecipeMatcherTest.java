package com.richfield.smartpantry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

/** Unit tests proving the strict-matching rule (run with: Right-click > Run in Android Studio). */
public class RecipeMatcherTest {

    private Recipe fiveIngredientRecipe() {
        Recipe r = new Recipe(1, "Test Pasta", "steps");
        r.getIngredients().add(new RecipeIngredient("pasta", 200, "g"));
        r.getIngredients().add(new RecipeIngredient("tomato", 3, "piece"));
        r.getIngredients().add(new RecipeIngredient("onion", 1, "piece"));
        r.getIngredients().add(new RecipeIngredient("garlic", 2, "piece"));
        r.getIngredients().add(new RecipeIngredient("olive oil", 15, "ml"));
        return r;
    }

    @Test
    public void normalizeHandlesPlurals() {
        assertEquals("tomato", RecipeMatcher.normalize("Tomatoes"));
        assertEquals("egg", RecipeMatcher.normalize(" Eggs "));
        assertEquals("bell pepper", RecipeMatcher.normalize("bell peppers"));
    }

    @Test
    public void fourOfFiveIngredientsIsNotSuggested() {
        List<PantryItem> pantry = Arrays.asList(
                new PantryItem(1, "Pasta", 500, "g", null),
                new PantryItem(2, "Tomatoes", 4, "piece", null),
                new PantryItem(3, "Onions", 2, "piece", null),
                new PantryItem(4, "Garlic", 3, "piece", null)); // olive oil missing
        List<Recipe> recipes = new ArrayList<>(Arrays.asList(fiveIngredientRecipe()));
        assertTrue(RecipeMatcher.findStrictMatches(pantry, recipes).isEmpty());
        assertEquals(1, RecipeMatcher.findAlmostThere(pantry, recipes).size());
    }

    @Test
    public void allIngredientsPresentIsSuggestedWithUnitConversion() {
        List<PantryItem> pantry = Arrays.asList(
                new PantryItem(1, "pasta", 0.5, "kg", null),         // 500 g
                new PantryItem(2, "Tomato", 3, "piece", null),
                new PantryItem(3, "onion", 1, "piece", null),
                new PantryItem(4, "garlic", 2, "piece", null),
                new PantryItem(5, "Olive Oil", 1, "tbsp", null));    // 15 ml
        List<Recipe> recipes = Arrays.asList(fiveIngredientRecipe());
        assertEquals(1, RecipeMatcher.findStrictMatches(pantry, recipes).size());
    }

    @Test
    public void notEnoughQuantityIsNotSuggested() {
        List<PantryItem> pantry = Arrays.asList(
                new PantryItem(1, "pasta", 100, "g", null),           // needs 200 g
                new PantryItem(2, "tomato", 3, "piece", null),
                new PantryItem(3, "onion", 1, "piece", null),
                new PantryItem(4, "garlic", 2, "piece", null),
                new PantryItem(5, "olive oil", 15, "ml", null));
        assertTrue(RecipeMatcher.findStrictMatches(pantry, Arrays.asList(fiveIngredientRecipe())).isEmpty());
    }

    @Test
    public void duplicateRowsAreAddedTogether() {
        List<PantryItem> pantry = Arrays.asList(
                new PantryItem(1, "pasta", 100, "g", null),
                new PantryItem(2, "Pasta", 100, "g", null),           // 100 + 100 = 200 g
                new PantryItem(3, "tomato", 3, "piece", null),
                new PantryItem(4, "onion", 1, "piece", null),
                new PantryItem(5, "garlic", 2, "piece", null),
                new PantryItem(6, "olive oil", 15, "ml", null));
        assertEquals(1, RecipeMatcher.findStrictMatches(pantry, Arrays.asList(fiveIngredientRecipe())).size());
    }
}
