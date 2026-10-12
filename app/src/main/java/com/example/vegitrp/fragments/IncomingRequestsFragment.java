package com.example.vegitrp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vegitrp.R;
import com.example.vegitrp.activities.MapActivity;
import com.example.vegitrp.adapters.ListingAdapter;
import com.example.vegitrp.models.Driver;
import com.example.vegitrp.models.Listing;
import com.example.vegitrp.models.Vehicle;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IncomingRequestsFragment extends Fragment implements ListingAdapter.OnListingActionListener {

    private static final String TAG = "IncomingRequests";
    private RecyclerView rvIncomingRequests;
    private ListingAdapter listingAdapter;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;
    private View layoutEmptyState;
    private ValueEventListener valueEventListener;
    private DatabaseReference requestsRef;
    private final List<String> driverNames = new ArrayList<>();
    private final List<String> driverUids = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_incoming_requests, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        rvIncomingRequests = view.findViewById(R.id.rv_incoming_requests);
        progressBar = view.findViewById(R.id.progress_bar);
        layoutEmptyState = view.findViewById(R.id.layout_empty_state);

        listingAdapter = new ListingAdapter(this);

        rvIncomingRequests.setLayoutManager(new LinearLayoutManager(getContext()));
        rvIncomingRequests.setAdapter(listingAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadIncomingRequests();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (requestsRef != null && valueEventListener != null) {
            requestsRef.removeEventListener(valueEventListener);
        }
    }

    private void loadIncomingRequests() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        if (rvIncomingRequests != null) {
            rvIncomingRequests.setVisibility(View.GONE);
        }
        if (layoutEmptyState != null) {
            layoutEmptyState.setVisibility(View.GONE);
        }

        requestsRef = mDatabase.child("AllOrders");
        valueEventListener = requestsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Listing> listingList = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Listing listing = dataSnapshot.getValue(Listing.class);
                    if (listing != null) {
                        listing.setKey(dataSnapshot.getKey());
                        listingList.add(listing);
                    }
                }
                listingAdapter.submitList(listingList);
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                if (listingList.isEmpty()) {
                    if (layoutEmptyState != null) {
                        layoutEmptyState.setVisibility(View.VISIBLE);
                    }
                    if (rvIncomingRequests != null) {
                        rvIncomingRequests.setVisibility(View.GONE);
                    }
                } else {
                    if (layoutEmptyState != null) {
                        layoutEmptyState.setVisibility(View.GONE);
                    }
                    if (rvIncomingRequests != null) {
                        rvIncomingRequests.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load requests.", error.toException());
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public void onActionClick(int position) {
        if (position < 0 || position >= listingAdapter.getCurrentList().size()) return;
        Listing listing = listingAdapter.getCurrentList().get(position);
        if (listing == null || listing.getKey() == null) return;

        if ("Pending".equals(listing.getStatus())) {
            mDatabase.child("AllOrders").child(listing.getKey()).child("status").setValue("Accepted")
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if (getContext() != null) {
                                Toast.makeText(getContext(), "Request accepted", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            if (getContext() != null) {
                                Toast.makeText(getContext(), "Failed to accept request", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else if ("Accepted".equals(listing.getStatus())) {
            showAssignDialog(listing);
        } else {
            Intent intent = new Intent(getActivity(), MapActivity.class);
            intent.putExtra("tripId", listing.getKey());
            startActivity(intent);
        }
    }

    private void showAssignDialog(Listing listing) {
        if (getContext() == null || getActivity() == null) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_assign, null);
        builder.setView(dialogView);

        Spinner spinnerVehicle = dialogView.findViewById(R.id.spinner_vehicle);
        Spinner spinnerDriver = dialogView.findViewById(R.id.spinner_driver);

        loadVehicles(spinnerVehicle);
        loadDrivers(spinnerDriver);

        builder.setPositiveButton("Confirm", (dialog, which) -> {
            int selectedDriverIndex = spinnerDriver.getSelectedItemPosition();
            if (selectedDriverIndex == 0) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), R.string.select_driver_prompt, Toast.LENGTH_SHORT).show();
                }
                return;
            }
            if (selectedDriverIndex - 1 < driverUids.size()) {
                String selectedDriverUid = driverUids.get(selectedDriverIndex - 1);
                String selectedVehicle = spinnerVehicle.getSelectedItem().toString();

                Map<String, Object> updates = new HashMap<>();
                updates.put("status", "Scheduled");
                updates.put("assignedVehicle", selectedVehicle);
                updates.put("assignedDriver", selectedDriverUid);

                mDatabase.child("AllOrders").child(listing.getKey()).updateChildren(updates)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                if (getContext() != null) {
                                    Toast.makeText(getContext(), "Assignment successful", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Log.e("Assignment Error", "Failed to assign", task.getException());
                                if (getContext() != null) {
                                    Toast.makeText(getContext(), "Assignment failed.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void loadVehicles(Spinner spinner) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && getContext() != null) {
            String userId = currentUser.getUid();
            mDatabase.child("Vehicles").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (!isAdded() || getContext() == null) return;
                    List<String> vehicleNames = new ArrayList<>();
                    vehicleNames.add(getString(R.string.select_vehicle_prompt));
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Vehicle vehicle = dataSnapshot.getValue(Vehicle.class);
                        if (vehicle != null) {
                            vehicleNames.add(vehicle.getVehicleName());
                        }
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, vehicleNames);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinner.setAdapter(adapter);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("LoadVehicles", "Failed to load vehicles.", error.toException());
                }
            });
        }
    }

    private void loadDrivers(Spinner spinner) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null || getContext() == null) return;
        String userId = currentUser.getUid();

        mDatabase.child("Drivers").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded() || getContext() == null) return;
                driverNames.clear();
                driverUids.clear();
                driverNames.add(getString(R.string.select_driver_prompt));

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Driver driver = dataSnapshot.getValue(Driver.class);
                    if (driver != null) {
                        driverNames.add(driver.getName());
                        driverUids.add(dataSnapshot.getKey());
                    }
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, driverNames);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("LoadDrivers", "Failed to load drivers.", error.toException());
            }
        });
    }
}
