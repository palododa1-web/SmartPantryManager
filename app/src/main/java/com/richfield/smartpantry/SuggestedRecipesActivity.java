package com.richfield.smartpantry;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Suggested Recipes screen. Runs RecipeMatcher (the strict-matching rule) every time it is shown,
 * so adding or removing a pantry item immediately changes the list.
 * The optional "Almost there" list is kept visually separate from the strict suggestions.
 */
public class SuggestedRecipesActivity extends AppCompatActivity implements RecipeAdapter.Listener {

    private DatabaseHelper db;
    private RecipeAdapter strictAdapter, almostAdapter;
    private TextView textSummary, textEmpty, textAlmostHeader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested_recipes);
        setTitle("Suggested Recipes");

        db = new DatabaseHelper(this);
        textSummary = findViewById(R.id.textSummary);
        textEmpty = findViewById(R.id.textEmpty);
        textAlmostHeader = findViewById(R.id.textAlmostHeader);

        RecyclerView strictList = findViewById(R.id.recyclerStrict);
        strictList.setLayoutManager(new LinearLayoutManager(this));
        strictAdapter = new RecipeAdapter(this);
        strictList.setAdapter(strictAdapter);

        RecyclerView almostList = findViewById(R.id.recyclerAlmost);
        almostList.setLayoutManager(new LinearLayoutManager(this));
        almostAdapter = new RecipeAdapter(this);
        almostList.setAdapter(almostAdapter);

        NavHelper.setup(this, (BottomNavigationView) findViewById(R.id.bottomNav), R.id.nav_recipes);
    }

    @Override
    protected void onResume() {
        super.onResume();
        List<PantryItem> pantry = db.getAllItems();
        List<Recipe> allRecipes = db.getAllRecipes();

        List<Recipe> strict = RecipeMatcher.findStrictMatches(pantry, allRecipes);
        strictAdapter.setRecipes(strict, null);
        textSummary.setText("You can cook " + strict.size() + " of " + allRecipes.size() + " recipes right now");

        // Feedback instead of a blank screen when nothing matches.
        textEmpty.setVisibility(strict.isEmpty() ? View.VISIBLE : View.GONE);

        // Optional stretch: recipes missing exactly one ingredient, in a clearly separate section.
        Map<Recipe, String> almost = RecipeMatcher.findAlmostThere(pantry, allRecipes);
        Map<Long, String> notes = new HashMap<>();
        for (Map.Entry<Recipe, String> e : almost.entrySet()) {
            notes.put(e.getKey().getId(), "Missing: " + e.getValue());
        }
        almostAdapter.setRecipes(new java.util.ArrayList<>(almost.keySet()), notes);
        textAlmostHeader.setVisibility(almost.isEmpty() ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onRecipeClicked(Recipe recipe) {
        Intent intent = new Intent(this, RecipeDetailActivity.class);
        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.getId());
        startActivity(intent);
    }
}
