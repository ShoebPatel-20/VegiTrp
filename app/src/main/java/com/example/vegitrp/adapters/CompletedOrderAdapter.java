package com.example.vegitrp.adapters;

import android.content.Context;
import android.content.Intent;
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
import com.example.vegitrp.activities.OrderDetailsActivity;
import com.example.vegitrp.models.Order;

public class CompletedOrderAdapter extends ListAdapter<Order, CompletedOrderAdapter.CompletedOrderViewHolder> {

    private final OnCompletedOrderListener listener;

    public interface OnCompletedOrderListener {
        void onAcceptClick(int position);
        void onRejectClick(int position);
    }

    public CompletedOrderAdapter(OnCompletedOrderListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Order> DIFF_CALLBACK = new DiffUtil.ItemCallback<Order>() {
        @Override
        public boolean areItemsTheSame(@NonNull Order oldItem, @NonNull Order newItem) {
            if (oldItem.getKey() == null || newItem.getKey() == null) return false;
            return oldItem.getKey().equals(newItem.getKey());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Order oldItem, @NonNull Order newItem) {
            if (oldItem.getStatus() == null || newItem.getStatus() == null) return false;
            return oldItem.getStatus().equals(newItem.getStatus());
        }
    };

    @NonNull
    @Override
    public CompletedOrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_completed_order, parent, false);
        return new CompletedOrderViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull CompletedOrderViewHolder holder, int position) {
        Order order = getItem(position);
        if (order != null) {
            holder.bind(order);
        }
    }

    class CompletedOrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvProductName, tvQuantity, tvPrice, tvOrderStatus;
        Button btnAccept, btnReject;
        OnCompletedOrderListener listener;

        public CompletedOrderViewHolder(@NonNull View itemView, OnCompletedOrderListener listener) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvQuantity = itemView.findViewById(R.id.tv_quantity);
            tvPrice = itemView.findViewById(R.id.tv_price);
            tvOrderStatus = itemView.findViewById(R.id.tv_order_status);
            btnAccept = itemView.findViewById(R.id.btn_accept);
            btnReject = itemView.findViewById(R.id.btn_reject);
            this.listener = listener;

            if (btnAccept != null) {
                btnAccept.setOnClickListener(v -> {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION && listener != null) {
                        listener.onAcceptClick(pos);
                    }
                });
            }
            if (btnReject != null) {
                btnReject.setOnClickListener(v -> {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION && listener != null) {
                        listener.onRejectClick(pos);
                    }
                });
            }
            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    Order order = getItem(pos);
                    if (order != null && order.getKey() != null) {
                        Intent intent = new Intent(v.getContext(), OrderDetailsActivity.class);
                        intent.putExtra("orderKey", order.getKey());
                        v.getContext().startActivity(intent);
                    }
                }
            });
        }

        public void bind(final Order order) {
            if (tvProductName != null && order.getProduct() != null) {
                tvProductName.setText(order.getProduct());
            }
            if (tvQuantity != null && order.getQuantity() != null) {
                tvQuantity.setText(order.getQuantity());
            }
            if (tvPrice != null) {
                tvPrice.setText("₹" + order.getPrice() + " / KG");
            }
            if (tvOrderStatus != null && order.getStatus() != null) {
                tvOrderStatus.setText(order.getStatus());
            }
        }
    }
}
