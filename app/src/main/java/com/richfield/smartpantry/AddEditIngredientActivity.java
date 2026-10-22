package com.richfield.smartpantry;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;
import java.util.Locale;

/**
 * Add/Edit Ingredient screen. If the Intent carries EXTRA_ITEM_ID the form is pre-filled and saving
 * UPDATES that row; otherwise saving CREATES a new row. All input is validated before saving.
 */
public class AddEditIngredientActivity extends AppCompatActivity {

    public static final String EXTRA_ITEM_ID = "item_id";

    private DatabaseHelper db;
    private long itemId = -1;

    private TextInputLayout layoutName, layoutQty;
    private TextInputEditText editName, editQty, editExpiry;
    private Spinner spinnerUnit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_ingredient);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        db = new DatabaseHelper(this);
        layoutName = findViewById(R.id.layoutName);
        layoutQty = findViewById(R.id.layoutQty);
        editName = findViewById(R.id.editName);
        editQty = findViewById(R.id.editQty);
        editExpiry = findViewById(R.id.editExpiry);
        spinnerUnit = findViewById(R.id.spinnerUnit);
        MaterialButton btnSave = findViewById(R.id.btnSave);

        spinnerUnit.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, Units.ALL));
        spinnerUnit.setSelection(SettingsActivity.defaultUnitIndex(this));

        // Date picker for the optional expiry date.
        editExpiry.setOnClickListener(v -> showDatePicker());
        editExpiry.setOnLongClickListener(v -> {
            editExpiry.setText("");
            return true;
        });

        itemId = getIntent().getLongExtra(EXTRA_ITEM_ID, -1);
        if (itemId != -1) {
            setTitle("Edit Ingredient");
            PantryItem item = db.getItem(itemId);
            if (item != null) {
                editName.setText(item.getName());
                editQty.setText(Units.formatQty(item.getQuantity()));
                editExpiry.setText(item.getExpiry());
                for (int i = 0; i < Units.ALL.length; i++) {
                    if (Units.ALL[i].equals(item.getUnit())) {
                        spinnerUnit.setSelection(i);
                    }
                }
            }
        } else {
            setTitle("Add Ingredient");
        }

        btnSave.setOnClickListener(v -> save());
    }

    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) ->
                editExpiry.setText(String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day)),
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    /** Input validation, then INSERT or UPDATE. */
    private void save() {
        layoutName.setError(null);
        layoutQty.setError(null);

        String name = editName.getText() == null ? "" : editName.getText().toString().trim();
        String qtyText = editQty.getText() == null ? "" : editQty.getText().toString().trim();
        String expiry = editExpiry.getText() == null ? "" : editExpiry.getText().toString().trim();

        boolean valid = true;
        if (name.isEmpty()) {
            layoutName.setError("Please enter an ingredient name");
            valid = false;
        } else if (!name.matches(".*[A-Za-z].*")) {
            layoutName.setError("The name must contain letters");
            valid = false;
        }

        double quantity = 0;
        if (qtyText.isEmpty()) {
            layoutQty.setError("Please enter a quantity");
            valid = false;
        } else {
            try {
                quantity = Double.parseDouble(qtyText);
                if (quantity <= 0) {
                    layoutQty.setError("Quantity must be greater than 0");
                    valid = false;
                }
            } catch (NumberFormatException e) {
                layoutQty.setError("Enter a valid number");
                valid = false;
            }
        }
        if (!valid) {
            return;
        }

        String unit = Units.ALL[spinnerUnit.getSelectedItemPosition()];
        PantryItem item = new PantryItem(itemId, name, quantity, unit, expiry.isEmpty() ? null : expiry);

        if (itemId == -1) {
            db.insertItem(item);
            Toast.makeText(this, "Ingredient added", Toast.LENGTH_SHORT).show();
        } else {
            db.updateItem(item);
            Toast.makeText(this, "Ingredient updated", Toast.LENGTH_SHORT).show();
        }
        finish(); // returns to the pantry list, whose onResume refreshes the data
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
