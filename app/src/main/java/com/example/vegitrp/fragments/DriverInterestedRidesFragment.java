package com.example.vegitrp.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vegitrp.R;
import com.example.vegitrp.adapters.OrderListAdapter;
import com.example.vegitrp.models.Order;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DriverInterestedRidesFragment extends Fragment {

    private static final String TAG = "DriverInterestedRidesFragment";
    private RecyclerView rvInterestedRides;
    private OrderListAdapter adapter;
    private ProgressBar progressBar;
    private View layoutEmptyState;
    private TextView tvEmptyState;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private ValueEventListener valueEventListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_driver_interested_rides, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("AllOrders");
        mAuth = FirebaseAuth.getInstance();

        rvInterestedRides = view.findViewById(R.id.rv_interested_rides);
        progressBar = view.findViewById(R.id.progress_bar);
        layoutEmptyState = view.findViewById(R.id.layout_empty_state);
        tvEmptyState = view.findViewById(R.id.tv_empty_state);

        rvInterestedRides.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new OrderListAdapter();
        rvInterestedRides.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchRides();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mDatabase != null && valueEventListener != null) {
            mDatabase.removeEventListener(valueEventListener);
        }
    }

    private void fetchRides() {
        progressBar.setVisibility(View.VISIBLE);
        if (rvInterestedRides != null) rvInterestedRides.setVisibility(View.GONE);
        if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.GONE);
        if (tvEmptyState != null) tvEmptyState.setVisibility(View.GONE);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            progressBar.setVisibility(View.GONE);
            if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.VISIBLE);
            if (tvEmptyState != null) tvEmptyState.setVisibility(View.VISIBLE);
            return;
        }

        String userId = currentUser.getUid();

        valueEventListener = mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Order> orderList = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Order order = dataSnapshot.getValue(Order.class);
                    if (order != null) {
                        boolean isInterestedStatus = "INTERESTED".equalsIgnoreCase(order.getStatus())
                                || "PENDING".equalsIgnoreCase(order.getStatus())
                                || "Active".equalsIgnoreCase(order.getStatus());
                        boolean isDriverMatched = userId.equals(order.getAssignedDriverId())
                                || (order.getInterestedFarmers() != null && order.getInterestedFarmers().contains(userId));
                        if (isInterestedStatus && isDriverMatched) {
                            order.setKey(dataSnapshot.getKey());
                            orderList.add(order);
                        }
                    }
                }
                Collections.reverse(orderList);
                adapter.submitList(orderList);

                progressBar.setVisibility(View.GONE);
                Log.d(TAG, "Loaded " + orderList.size() + " interested rides for Driver " + userId);

                if (orderList.isEmpty()) {
                    if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.VISIBLE);
                    if (tvEmptyState != null) tvEmptyState.setVisibility(View.VISIBLE);
                    if (rvInterestedRides != null) rvInterestedRides.setVisibility(View.GONE);
                } else {
                    if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.GONE);
                    if (tvEmptyState != null) tvEmptyState.setVisibility(View.GONE);
                    if (rvInterestedRides != null) rvInterestedRides.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load interested rides: " + error.getMessage());
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Error loading rides: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
