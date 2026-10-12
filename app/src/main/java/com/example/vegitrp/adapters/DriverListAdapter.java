package com.example.vegitrp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vegitrp.R;
import com.example.vegitrp.models.Driver;

import java.util.List;

public class DriverListAdapter extends RecyclerView.Adapter<DriverListAdapter.DriverViewHolder> {

    private final List<Driver> driverList;
    private final OnDriverListener onDriverListener;

    public interface OnDriverListener {
        void onEditClick(int position);
        void onDeleteClick(int position);
    }

    public DriverListAdapter(List<Driver> driverList, OnDriverListener onDriverListener) {
        this.driverList = driverList;
        this.onDriverListener = onDriverListener;
    }

    @NonNull
    @Override
    public DriverViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_driver, parent, false);
        return new DriverViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DriverViewHolder holder, int position) {
        Driver driver = driverList.get(position);
        holder.tvDriverName.setText(driver.getName());
        holder.tvDriverPhone.setText(driver.getPhone());

        holder.btnEdit.setOnClickListener(v -> {
            if (onDriverListener != null) {
                onDriverListener.onEditClick(position);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (onDriverListener != null) {
                onDriverListener.onDeleteClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return driverList.size();
    }

    static class DriverViewHolder extends RecyclerView.ViewHolder {
        TextView tvDriverName, tvDriverPhone;
        ImageButton btnEdit, btnDelete;

        public DriverViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDriverName = itemView.findViewById(R.id.tv_driver_name);
            tvDriverPhone = itemView.findViewById(R.id.tv_driver_phone);
            btnEdit = itemView.findViewById(R.id.btn_edit_driver);
            btnDelete = itemView.findViewById(R.id.btn_delete_driver);
        }
    }
}
