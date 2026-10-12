package com.example.vegitrp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vegitrp.R;
import com.example.vegitrp.models.Listing;

public class TripAdapter extends ListAdapter<Listing, TripAdapter.TripViewHolder> {

    private final OnTripListener onTripListener;

    public interface OnTripListener {
        void onUpdateStatusClick(int position);
    }

    public TripAdapter(OnTripListener onTripListener) {
        super(DIFF_CALLBACK);
        this.onTripListener = onTripListener;
    }

    private static final DiffUtil.ItemCallback<Listing> DIFF_CALLBACK = new DiffUtil.ItemCallback<Listing>() {
        @Override
        public boolean areItemsTheSame(@NonNull Listing oldItem, @NonNull Listing newItem) {
            return oldItem.getKey().equals(newItem.getKey());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Listing oldItem, @NonNull Listing newItem) {
            return oldItem.getStatus().equals(newItem.getStatus());
        }
    };

    @NonNull
    @Override
    public TripViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_trip, parent, false);
        return new TripViewHolder(view, onTripListener);
    }

    @Override
    public void onBindViewHolder(@NonNull TripViewHolder holder, int position) {
        Listing trip = getItem(position);
        holder.tvProductName.setText(trip.getProduct());
        holder.tvQuantity.setText(trip.getQuantity());
        holder.tvStatus.setText(trip.getStatus());

        switch (trip.getStatus()) {
            case "Scheduled":
                holder.btnUpdateStatus.setText("Start Trip");
                holder.btnUpdateStatus.setEnabled(true);
                break;
            case "In-Transit":
                holder.btnUpdateStatus.setText("Complete Trip");
                holder.btnUpdateStatus.setEnabled(true);
                break;
            default:
                holder.btnUpdateStatus.setText(trip.getStatus());
                holder.btnUpdateStatus.setEnabled(false);
                break;
        }
    }

    static class TripViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tvProductName, tvQuantity, tvStatus;
        Button btnUpdateStatus;
        OnTripListener onTripListener;

        public TripViewHolder(@NonNull View itemView, OnTripListener onTripListener) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvQuantity = itemView.findViewById(R.id.tv_quantity);
            tvStatus = itemView.findViewById(R.id.tv_status);
            btnUpdateStatus = itemView.findViewById(R.id.btn_update_status);
            this.onTripListener = onTripListener;

            btnUpdateStatus.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onTripListener.onUpdateStatusClick(getAdapterPosition());
        }
    }
}
