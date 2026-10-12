package com.example.vegitrp.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

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
import java.util.List;

public class FarmerInterestedFragment extends Fragment {

    private static final String TAG = "FarmerInterestedFragment";
    private RecyclerView rvInterestedOrders;
    private OrderListAdapter orderListAdapter;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;
    private View layoutEmptyState;
    private ValueEventListener valueEventListener;
    private DatabaseReference ordersRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_farmer_interested, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        rvInterestedOrders = view.findViewById(R.id.rv_interested_orders);
        progressBar = view.findViewById(R.id.progress_bar);
        layoutEmptyState = view.findViewById(R.id.layout_empty_state);
        rvInterestedOrders.setLayoutManager(new LinearLayoutManager(getContext()));

        orderListAdapter = new OrderListAdapter();
        rvInterestedOrders.setAdapter(orderListAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadOrders();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (ordersRef != null && valueEventListener != null) {
            ordersRef.removeEventListener(valueEventListener);
        }
    }

    private void loadOrders() {
        progressBar.setVisibility(View.VISIBLE);
        rvInterestedOrders.setVisibility(View.GONE);
        if (layoutEmptyState != null) {
            layoutEmptyState.setVisibility(View.GONE);
        }

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            progressBar.setVisibility(View.GONE);
            if (layoutEmptyState != null) {
                layoutEmptyState.setVisibility(View.VISIBLE);
            }
            Log.w(TAG, "No logged in user — cannot load interested orders");
            return;
        }

        String userId = currentUser.getUid();
        ordersRef = mDatabase.child("AllOrders");
        valueEventListener = ordersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Order> orderList = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Order order = dataSnapshot.getValue(Order.class);
                    if (order != null && "Active".equalsIgnoreCase(order.getStatus()) && order.getInterestedFarmers() != null && order.getInterestedFarmers().contains(userId)) {
                        order.setKey(dataSnapshot.getKey());
                        orderList.add(order);
                    }
                }
                orderListAdapter.submitList(orderList);
                progressBar.setVisibility(View.GONE);

                if (orderList.isEmpty()) {
                    if (layoutEmptyState != null) {
                        layoutEmptyState.setVisibility(View.VISIBLE);
                    }
                    rvInterestedOrders.setVisibility(View.GONE);
                } else {
                    if (layoutEmptyState != null) {
                        layoutEmptyState.setVisibility(View.GONE);
                    }
                    rvInterestedOrders.setVisibility(View.VISIBLE);
                }
                Log.d(TAG, "Loaded " + orderList.size() + " interested orders for farmer " + userId);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "Failed to load orders.", error.toException());
                progressBar.setVisibility(View.GONE);
            }
        });
    }
}
