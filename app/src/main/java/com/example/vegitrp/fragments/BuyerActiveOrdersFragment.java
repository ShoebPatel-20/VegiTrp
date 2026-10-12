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

public class BuyerActiveOrdersFragment extends Fragment {

    private static final String TAG = "BuyerActiveOrdersFragment";
    private RecyclerView rvActiveOrders;
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
        return inflater.inflate(R.layout.fragment_buyer_active_orders, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        rvActiveOrders = view.findViewById(R.id.rv_active_orders);
        progressBar = view.findViewById(R.id.progress_bar);
        layoutEmptyState = view.findViewById(R.id.layout_empty_state);
        rvActiveOrders.setLayoutManager(new LinearLayoutManager(getContext()));

        orderListAdapter = new OrderListAdapter();
        rvActiveOrders.setAdapter(orderListAdapter);
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
        rvActiveOrders.setVisibility(View.GONE);
        if (layoutEmptyState != null) {
            layoutEmptyState.setVisibility(View.GONE);
        }

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            progressBar.setVisibility(View.GONE);
            if (layoutEmptyState != null) {
                layoutEmptyState.setVisibility(View.VISIBLE);
            }
            Log.w(TAG, "No logged in user — cannot load orders");
            return;
        }
        String userId = currentUser.getUid();
        ordersRef = mDatabase.child("AllOrders");
        // Note: Firebase doesn't support nested array queries, so we fetch all and filter client-side
        valueEventListener = ordersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Order> orderList = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Order order = dataSnapshot.getValue(Order.class);
                    if (order != null && userId.equals(order.getBuyerId()) && !"Completed".equalsIgnoreCase(order.getStatus())) {
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
                    rvActiveOrders.setVisibility(View.GONE);
                } else {
                    if (layoutEmptyState != null) {
                        layoutEmptyState.setVisibility(View.GONE);
                    }
                    rvActiveOrders.setVisibility(View.VISIBLE);
                }
                Log.d(TAG, "Loaded " + orderList.size() + " active orders for buyer " + userId);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "Failed to load orders.", error.toException());
                progressBar.setVisibility(View.GONE);
            }
        });
    }
}
