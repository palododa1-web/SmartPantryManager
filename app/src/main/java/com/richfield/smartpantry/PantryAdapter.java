package com.richfield.smartpantry;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/** Custom RecyclerView adapter that shows the pantry rows read from the SQLite database. */
public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.ViewHolder> {

    public interface Listener {
        void onEdit(PantryItem item);
        void onDelete(PantryItem item);
    }

    private final List<PantryItem> items = new ArrayList<>();
    private final Listener listener;

    public PantryAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setItems(List<PantryItem> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pantry, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        final PantryItem item = items.get(position);
        h.name.setText(item.getName());
        h.qty.setText(Units.formatQty(item.getQuantity()) + " " + item.getUnit());

        long days = item.getDaysUntilExpiry();
        if (days == Long.MAX_VALUE) {
            h.expiry.setText("No expiry date");
            h.expiry.setTextColor(Color.GRAY);
        } else if (days < 0) {
            h.expiry.setText("Expired on " + item.getExpiry());
            h.expiry.setTextColor(Color.parseColor("#C62828"));
        } else if (days <= 3) {
            h.expiry.setText("Expires in " + days + (days == 1 ? " day" : " days") + " (" + item.getExpiry() + ")");
            h.expiry.setTextColor(Color.parseColor("#E65100"));
        } else {
            h.expiry.setText("Expires: " + item.getExpiry());
            h.expiry.setTextColor(Color.DKGRAY);
        }

        h.itemView.setOnClickListener(v -> listener.onEdit(item));
        h.btnEdit.setOnClickListener(v -> listener.onEdit(item));
        h.btnDelete.setOnClickListener(v -> listener.onDelete(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView name, qty, expiry, btnEdit, btnDelete;

        ViewHolder(View v) {
            super(v);
            name = v.findViewById(R.id.textItemName);
            qty = v.findViewById(R.id.textItemQty);
            expiry = v.findViewById(R.id.textItemExpiry);
            btnEdit = v.findViewById(R.id.btnEdit);
            btnDelete = v.findViewById(R.id.btnDelete);
        }
    }
}
