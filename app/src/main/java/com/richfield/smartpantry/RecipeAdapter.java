package com.richfield.smartpantry;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** RecyclerView adapter for recipe cards. An optional note (e.g. "Missing: egg") can be shown per recipe. */
public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.ViewHolder> {

    public interface Listener {
        void onRecipeClicked(Recipe recipe);
    }

    private final List<Recipe> recipes = new ArrayList<>();
    private final Map<Long, String> notes = new HashMap<>();
    private final Listener listener;

    public RecipeAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setRecipes(List<Recipe> newRecipes, Map<Long, String> newNotes) {
        recipes.clear();
        recipes.addAll(newRecipes);
        notes.clear();
        if (newNotes != null) {
            notes.putAll(newNotes);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recipe, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        final Recipe recipe = recipes.get(position);
        h.name.setText(recipe.getName());
        h.count.setText(recipe.getIngredients().size() + " ingredients");
        String note = notes.get(recipe.getId());
        if (note != null) {
            h.note.setText(note);
            h.note.setVisibility(View.VISIBLE);
        } else {
            h.note.setVisibility(View.GONE);
        }
        h.itemView.setOnClickListener(v -> listener.onRecipeClicked(recipe));
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView name, count, note;

        ViewHolder(View v) {
            super(v);
            name = v.findViewById(R.id.textRecipeName);
            count = v.findViewById(R.id.textRecipeCount);
            note = v.findViewById(R.id.textRecipeNote);
        }
    }
}
