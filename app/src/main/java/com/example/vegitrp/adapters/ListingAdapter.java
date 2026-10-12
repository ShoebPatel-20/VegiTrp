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

public class ListingAdapter extends ListAdapter<Listing, ListingAdapter.ListingViewHolder> {

    private final OnListingActionListener listener;

    public interface OnListingActionListener {
        void onActionClick(int position);
    }

    public ListingAdapter(OnListingActionListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
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
    public ListingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_listing, parent, false);
        return new ListingViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull ListingViewHolder holder, int position) {
        Listing listing = getItem(position);
        if (listing != null) {
            holder.bind(listing);
        }
    }

    static class ListingViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tvProductName, tvQuantity, tvStatus;
        Button btnAction;
        OnListingActionListener listener;

        public ListingViewHolder(@NonNull View itemView, OnListingActionListener listener) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvQuantity = itemView.findViewById(R.id.tv_quantity);
            tvStatus = itemView.findViewById(R.id.tv_status);
            btnAction = itemView.findViewById(R.id.btn_action);
            this.listener = listener;
            btnAction.setOnClickListener(this);
        }

        public void bind(final Listing listing) {
            tvProductName.setText(listing.getProduct());
            tvQuantity.setText(listing.getQuantity());
            tvStatus.setText(listing.getStatus());

            if (listener != null) {
                if ("Active".equals(listing.getStatus())) {
                    btnAction.setText("Show Interest");
                } else {
                    btnAction.setVisibility(View.GONE);
                }
            } else {
                btnAction.setVisibility(View.GONE);
            }
        }

        @Override
        public void onClick(View v) {
            if (listener != null) {
                listener.onActionClick(getAdapterPosition());
            }
        }
    }
}
