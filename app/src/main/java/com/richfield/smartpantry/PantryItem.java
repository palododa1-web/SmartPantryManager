package com.richfield.smartpantry;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/** Model class for one row of the pantry table. */
public class PantryItem {

    public static final String DATE_FORMAT = "yyyy-MM-dd";

    private long id;
    private String name;
    private double quantity;
    private String unit;
    private String expiry; // yyyy-MM-dd or null

    public PantryItem(long id, String name, double quantity, String unit, String expiry) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.expiry = expiry;
    }

    public long getId() { return id; }
    public String getName() { return name; }
    public double getQuantity() { return quantity; }
    public String getUnit() { return unit; }
    public String getExpiry() { return expiry; }

    /** Days from today until expiry (negative if already expired). Long.MAX_VALUE when there is no date. */
    public long getDaysUntilExpiry() {
        if (expiry == null || expiry.isEmpty()) {
            return Long.MAX_VALUE;
        }
        try {
            Date expiryDate = new SimpleDateFormat(DATE_FORMAT, Locale.US).parse(expiry);
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);
            long diff = expiryDate.getTime() - today.getTimeInMillis();
            return Math.round(diff / (24.0 * 60 * 60 * 1000));
        } catch (ParseException e) {
            return Long.MAX_VALUE;
        }
    }
}
