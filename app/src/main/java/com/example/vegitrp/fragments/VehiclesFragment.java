package com.example.vegitrp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vegitrp.R;
import com.example.vegitrp.activities.AddVehicleActivity;
import com.example.vegitrp.activities.EditVehicleActivity;
import com.example.vegitrp.adapters.VehicleListAdapter;
import com.example.vegitrp.models.Vehicle;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class VehiclesFragment extends Fragment implements VehicleListAdapter.OnVehicleListener {

    private static final String TAG = "VehiclesFragment";
    private RecyclerView rvVehicles;
    private VehicleListAdapter vehicleListAdapter;
    private List<Vehicle> vehicleList;
    private FloatingActionButton fabAddVehicle;
    private ProgressBar progressBar;
    private View layoutEmptyState;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private ValueEventListener valueEventListener;
    private DatabaseReference vehiclesRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_vehicles, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        rvVehicles = view.findViewById(R.id.rv_vehicles);
        fabAddVehicle = view.findViewById(R.id.fab_add_vehicle);
        progressBar = view.findViewById(R.id.progress_bar);
        layoutEmptyState = view.findViewById(R.id.layout_empty_state);

        vehicleList = new ArrayList<>();
        vehicleListAdapter = new VehicleListAdapter(vehicleList, this);

        rvVehicles.setLayoutManager(new LinearLayoutManager(getContext()));
        rvVehicles.setAdapter(vehicleListAdapter);

        fabAddVehicle.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), AddVehicleActivity.class));
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadVehicles();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (vehiclesRef != null && valueEventListener != null) {
            vehiclesRef.removeEventListener(valueEventListener);
        }
    }

    private void loadVehicles() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            if (progressBar != null) progressBar.setVisibility(View.GONE);
            if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.VISIBLE);
            if (rvVehicles != null) rvVehicles.setVisibility(View.GONE);
            return;
        }

        String userId = currentUser.getUid();
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        vehiclesRef = mDatabase.child("Vehicles").child(userId);

        valueEventListener = vehiclesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                vehicleList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Vehicle vehicle = dataSnapshot.getValue(Vehicle.class);
                    if (vehicle != null) {
                        vehicle.setKey(dataSnapshot.getKey());
                        vehicleList.add(vehicle);
                    }
                }
                vehicleListAdapter.notifyDataSetChanged();
                if (layoutEmptyState != null) {
                    layoutEmptyState.setVisibility(vehicleList.isEmpty() ? View.VISIBLE : View.GONE);
                }
                if (rvVehicles != null) {
                    rvVehicles.setVisibility(vehicleList.isEmpty() ? View.GONE : View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error loading vehicles: " + error.getMessage());
                if (progressBar != null) progressBar.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onEditClick(int position) {
        if (position >= 0 && position < vehicleList.size()) {
            Vehicle vehicle = vehicleList.get(position);
            Intent intent = new Intent(getActivity(), EditVehicleActivity.class);
            intent.putExtra("vehicle_key", vehicle.getKey());
            intent.putExtra("vehicle_name", vehicle.getVehicleName());
            intent.putExtra("vehicle_number", vehicle.getVehicleNumber());
            startActivity(intent);
        }
    }

    @Override
    public void onDeleteClick(int position) {
        if (position < 0 || position >= vehicleList.size()) return;
        if (getContext() == null) return;

        new MaterialAlertDialogBuilder(getContext())
                .setTitle("Delete Vehicle")
                .setMessage("Are you sure you want to delete this vehicle?")
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    FirebaseUser currentUser = mAuth.getCurrentUser();
                    if (currentUser != null && position < vehicleList.size()) {
                        String userId = currentUser.getUid();
                        Vehicle vehicle = vehicleList.get(position);
                        if (vehicle != null && vehicle.getKey() != null) {
                            mDatabase.child("Vehicles").child(userId).child(vehicle.getKey()).removeValue()
                                    .addOnSuccessListener(aVoid -> {
                                        if (getContext() != null) {
                                            Toast.makeText(getContext(), "Vehicle deleted successfully", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        if (getContext() != null) {
                                            Toast.makeText(getContext(), "Failed to delete vehicle", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .show();
    }
}
