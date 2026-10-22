package com.richfield.smartpantry;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/** Recipe Detail screen: full ingredient list and method for the recipe id received through the Intent. */
public class RecipeDetailActivity extends AppCompatActivity {

    public static final String EXTRA_RECIPE_ID = "recipe_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        long id = getIntent().getLongExtra(EXTRA_RECIPE_ID, -1);
        Recipe recipe = new DatabaseHelper(this).getRecipe(id);
        if (recipe == null) {
            finish(); // nothing to show
            return;
        }

        setTitle("Recipe");
        ((TextView) findViewById(R.id.textDetailName)).setText(recipe.getName());

        StringBuilder sb = new StringBuilder();
        for (RecipeIngredient ri : recipe.getIngredients()) {
            sb.append("\u2022 ").append(Units.formatQty(ri.getQuantity())).append(' ')
                    .append(ri.getUnit()).append(' ').append(ri.getName()).append('\n');
        }
        ((TextView) findViewById(R.id.textDetailIngredients)).setText(sb.toString().trim());
        ((TextView) findViewById(R.id.textDetailSteps)).setText(recipe.getSteps());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
