package com.richfield.smartpantry;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.switchmaterial.SwitchMaterial;

/** Settings screen. Preferences are saved with SharedPreferences so they also survive app restarts. */
public class SettingsActivity extends AppCompatActivity {

    private static final String PREFS = "pantry_prefs";
    private static final String KEY_ALERTS = "alerts_enabled";
    private static final String KEY_UNIT = "default_unit_index";

    public static boolean alertsEnabled(Context ctx) {
        return ctx.getSharedPreferences(PREFS, MODE_PRIVATE).getBoolean(KEY_ALERTS, true);
    }

    public static int defaultUnitIndex(Context ctx) {
        return ctx.getSharedPreferences(PREFS, MODE_PRIVATE).getInt(KEY_UNIT, 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setTitle("Settings");

        final SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);

        SwitchMaterial switchAlerts = findViewById(R.id.switchAlerts);
        switchAlerts.setChecked(alertsEnabled(this));
        switchAlerts.setOnCheckedChangeListener((button, checked) ->
                prefs.edit().putBoolean(KEY_ALERTS, checked).apply());

        Spinner spinner = findViewById(R.id.spinnerDefaultUnit);
        spinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, Units.ALL));
        spinner.setSelection(defaultUnitIndex(this));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                prefs.edit().putInt(KEY_UNIT, position).apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        NavHelper.setup(this, (BottomNavigationView) findViewById(R.id.bottomNav), R.id.nav_settings);
    }
}
