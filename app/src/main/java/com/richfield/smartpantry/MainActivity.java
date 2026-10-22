package com.richfield.smartpantry;

import android.content.Intent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

/**
 * Pantry List screen (launcher). READ: shows every ingredient from the database.
 * Also starts the Add/Edit screen (CREATE / UPDATE) and handles DELETE with a confirmation dialog.
 */
public class MainActivity extends AppCompatActivity implements PantryAdapter.Listener {

    private DatabaseHelper db;
    private PantryAdapter adapter;
    private TextView textEmpty, textBanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("My Pantry");

        db = new DatabaseHelper(this);
        textEmpty = findViewById(R.id.textEmpty);
        textBanner = findViewById(R.id.textBanner);

        RecyclerView recycler = findViewById(R.id.recyclerPantry);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PantryAdapter(this);
        recycler.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fabAdd);
        fab.setOnClickListener(v -> startActivity(new Intent(this, AddEditIngredientActivity.class)));

        BottomNavigationView nav = findViewById(R.id.bottomNav);
        NavHelper.setup(this, nav, R.id.nav_pantry);
    }

    /** onResume runs every time the screen comes back, so the list is always up to date. */
    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    private void refresh() {
        List<PantryItem> items = db.getAllItems();
        adapter.setItems(items);
        textEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);

        // Expiring-soon alert, controlled from the Settings screen.
        int soon = 0;
        for (PantryItem item : items) {
            if (item.getDaysUntilExpiry() <= 3) {
                soon++;
            }
        }
        if (SettingsActivity.alertsEnabled(this) && soon > 0) {
            textBanner.setText("Heads up: " + soon + (soon == 1 ? " item is" : " items are")
                    + " expired or expiring within 3 days.");
            textBanner.setVisibility(View.VISIBLE);
        } else {
            textBanner.setVisibility(View.GONE);
        }
    }

    @Override
    public void onEdit(PantryItem item) {
        Intent intent = new Intent(this, AddEditIngredientActivity.class);
        intent.putExtra(AddEditIngredientActivity.EXTRA_ITEM_ID, item.getId()); // pass data with the Intent
        startActivity(intent);
    }

    @Override
    public void onDelete(final PantryItem item) {
        new AlertDialog.Builder(this)
                .setTitle("Delete ingredient")
                .setMessage("Remove " + item.getName() + " from your pantry?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    db.deleteItem(item.getId());
                    Toast.makeText(this, item.getName() + " deleted", Toast.LENGTH_SHORT).show();
                    refresh();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
