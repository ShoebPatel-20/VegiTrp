package com.example.vegitrp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vegitrp.R;
import com.example.vegitrp.models.Vehicle;

import java.util.List;

public class VehicleListAdapter extends RecyclerView.Adapter<VehicleListAdapter.VehicleViewHolder> {

    private final List<Vehicle> vehicleList;
    private final OnVehicleListener onVehicleListener;

    // Interface for click events
    public interface OnVehicleListener {
        void onEditClick(int position);
        void onDeleteClick(int position);
    }

    public VehicleListAdapter(List<Vehicle> vehicleList, OnVehicleListener onVehicleListener) {
        this.vehicleList = vehicleList;
        this.onVehicleListener = onVehicleListener;
    }

    @NonNull
    @Override
    public VehicleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_vehicle, parent, false);
        return new VehicleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VehicleViewHolder holder, int position) {
        Vehicle vehicle = vehicleList.get(position);
        holder.tvVehicleName.setText(vehicle.getVehicleName());
        holder.tvVehicleNumber.setText(vehicle.getVehicleNumber());

        holder.btnEdit.setOnClickListener(v -> {
            if (onVehicleListener != null) {
                onVehicleListener.onEditClick(position);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (onVehicleListener != null) {
                onVehicleListener.onDeleteClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return vehicleList.size();
    }

    static class VehicleViewHolder extends RecyclerView.ViewHolder {
        TextView tvVehicleName, tvVehicleNumber;
        ImageButton btnEdit, btnDelete;

        public VehicleViewHolder(@NonNull View itemView) {
            super(itemView);
            tvVehicleName = itemView.findViewById(R.id.tv_vehicle_name);
            tvVehicleNumber = itemView.findViewById(R.id.tv_vehicle_number);
            btnEdit = itemView.findViewById(R.id.btn_edit_vehicle);
            btnDelete = itemView.findViewById(R.id.btn_delete_vehicle);
        }
    }
}
