package com.richfield.smartpantry;

import android.app.Activity;
import android.content.Intent;

import com.google.android.material.bottomnavigation.BottomNavigationView;

/** Wires the shared bottom navigation bar. Navigation between screens uses explicit Intents. */
public final class NavHelper {

    private NavHelper() { }

    public static void setup(final Activity current, BottomNavigationView nav, int selectedId) {
        nav.setSelectedItemId(selectedId); // set BEFORE the listener so it does not fire
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == selectedId) {
                return true;
            }
            Intent intent;
            if (id == R.id.nav_pantry) {
                intent = new Intent(current, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            } else if (id == R.id.nav_recipes) {
                intent = new Intent(current, SuggestedRecipesActivity.class);
            } else {
                intent = new Intent(current, SettingsActivity.class);
            }
            current.startActivity(intent);
            current.overridePendingTransition(0, 0);
            if (!(current instanceof MainActivity)) {
                current.finish(); // keep the back stack small: Pantry is always the root
            }
            return true;
        });
    }
}
