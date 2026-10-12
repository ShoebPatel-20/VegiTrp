package com.example.vegitrp.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vegitrp.R;
import com.example.vegitrp.activities.OrderDetailsActivity;
import com.example.vegitrp.models.Order;

public class OrderListAdapter extends ListAdapter<Order, OrderListAdapter.OrderViewHolder> {

    public OrderListAdapter() {
        super(DIFF_CALLBACK);
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
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = getItem(position);
        if (order != null) {
            holder.bind(order);
        }
    }

    class OrderViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tvProductName, tvQuantity, tvPrice, tvPinCode, tvStatusBadge;
        Context context;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            context = itemView.getContext();
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvQuantity = itemView.findViewById(R.id.tv_quantity);
            tvPrice = itemView.findViewById(R.id.tv_price);
            tvStatusBadge = itemView.findViewById(R.id.tv_status_badge);
            tvPinCode = itemView.findViewById(R.id.tv_pin_code);
            itemView.setOnClickListener(this);
        }

        public void bind(final Order order) {
            tvProductName.setText(order.getProduct());
            tvQuantity.setText(order.getQuantity());
            tvPrice.setText("₹" + order.getPrice() + " / KG");
            tvPinCode.setText(order.getPinCode());

            String status = order.getStatus();
            tvStatusBadge.setText(status);

            if ("Active".equalsIgnoreCase(status)) {
                tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_active);
                tvStatusBadge.setTextColor(Color.parseColor("#2E7D32"));
            } else if ("Negotiating".equalsIgnoreCase(status) || "Interested".equalsIgnoreCase(status)) {
                tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_negotiating);
                tvStatusBadge.setTextColor(Color.parseColor("#E65100"));
            } else {
                tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_completed);
                tvStatusBadge.setTextColor(Color.parseColor("#424242"));
            }
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                Order order = getItem(position);
                Intent intent = new Intent(context, OrderDetailsActivity.class);
                intent.putExtra("orderKey", order.getKey());
                context.startActivity(intent);
            }
        }
    }
}
