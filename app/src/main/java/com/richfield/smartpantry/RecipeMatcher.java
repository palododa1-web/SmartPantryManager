package com.richfield.smartpantry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * THE CORE LOGIC OF THE APP: the strict-matching rule.
 *
 * A recipe is "suggested" only if EVERY ingredient it needs is in the pantry in at least the required quantity.
 * One missing or insufficient ingredient means the recipe is NOT suggested.
 *
 * Robustness: names are normalised (case, spaces, simple plurals) and quantities converted to a common
 * base unit, so "Tomatoes" matches "tomato" and 1 kg satisfies 500 g. Several pantry rows of the same
 * ingredient are added together.
 */
public final class RecipeMatcher {

    private RecipeMatcher() { }

    /** Lower-cases, removes punctuation and converts simple plurals to singular ("Tomatoes" -> "tomato"). */
    public static String normalize(String raw) {
        if (raw == null) {
            return "";
        }
        String cleaned = raw.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z ]", "").replaceAll("\\s+", " ");
        String[] words = cleaned.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (word.isEmpty()) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(singular(word));
        }
        return sb.toString();
    }

    private static String singular(String w) {
        int n = w.length();
        if (n <= 3) return w;
        if (w.endsWith("ies")) return w.substring(0, n - 3) + "y";          // berries -> berry
        if (w.endsWith("oes")) return w.substring(0, n - 2);                // tomatoes -> tomato
        if (w.endsWith("ches") || w.endsWith("shes") || w.endsWith("sses") || w.endsWith("xes")) {
            return w.substring(0, n - 2);                                   // peaches -> peach
        }
        if (w.endsWith("ss") || w.endsWith("us")) return w;                 // hummus, watercress stay
        if (w.endsWith("s")) return w.substring(0, n - 1);                  // eggs -> egg
        return w;
    }

    private static String key(String name, String unit) {
        return normalize(name) + "|" + Units.category(unit);
    }

    /** Totals the pantry per ingredient (in base units). */
    public static Map<String, Double> buildStock(List<PantryItem> pantry) {
        Map<String, Double> stock = new HashMap<>();
        for (PantryItem item : pantry) {
            String k = key(item.getName(), item.getUnit());
            double amount = Units.toBase(item.getQuantity(), item.getUnit());
            Double existing = stock.get(k);
            stock.put(k, (existing == null ? 0 : existing) + amount);
        }
        return stock;
    }

    /** Names of the ingredients the pantry cannot cover (missing completely or not enough). */
    public static List<String> missingIngredients(Recipe recipe, Map<String, Double> stock) {
        List<String> missing = new ArrayList<>();
        for (RecipeIngredient ri : recipe.getIngredients()) {
            Double have = stock.get(key(ri.getName(), ri.getUnit()));
            double need = Units.toBase(ri.getQuantity(), ri.getUnit());
            if (have == null || have + 1e-9 < need) {
                missing.add(ri.getName());
            }
        }
        return missing;
    }

    /** STRICT list: only recipes with zero missing ingredients. */
    public static List<Recipe> findStrictMatches(List<PantryItem> pantry, List<Recipe> recipes) {
        Map<String, Double> stock = buildStock(pantry);
        List<Recipe> result = new ArrayList<>();
        for (Recipe r : recipes) {
            if (!r.getIngredients().isEmpty() && missingIngredients(r, stock).isEmpty()) {
                result.add(r);
            }
        }
        return result;
    }

    /** Optional bonus list, kept completely separate: recipes missing exactly ONE ingredient. */
    public static Map<Recipe, String> findAlmostThere(List<PantryItem> pantry, List<Recipe> recipes) {
        Map<String, Double> stock = buildStock(pantry);
        Map<Recipe, String> result = new LinkedHashMap<>();
        for (Recipe r : recipes) {
            List<String> missing = missingIngredients(r, stock);
            if (missing.size() == 1) {
                result.put(r, missing.get(0));
            }
        }
        return result;
    }
}
