package com.example.vegitrp.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

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

public abstract class BaseOrderListFragment extends Fragment {

    private static final String TAG = "BaseOrderListFragment";
    protected RecyclerView recyclerView;
    protected OrderListAdapter adapter;
    protected DatabaseReference mDatabase;
    protected FirebaseAuth mAuth;
    protected ProgressBar progressBar;
    protected LinearLayout layoutEmptyState;
    protected ValueEventListener valueEventListener;
    protected DatabaseReference ordersRef;

    protected String currentUserId;
    protected String currentUserRole = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_generic_orders, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        
        recyclerView = view.findViewById(R.id.recycler_view);
        progressBar = view.findViewById(R.id.progress_bar);
        layoutEmptyState = view.findViewById(R.id.layout_empty_state);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new OrderListAdapter();
        recyclerView.setAdapter(adapter);

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            currentUserId = user.getUid();
        }

        try {
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            SharedPreferences sharedPreferences = EncryptedSharedPreferences.create(
                    "user_prefs", masterKeyAlias, requireContext(),
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
            currentUserRole = sharedPreferences.getString("user_role", "");
        } catch (Exception e) {
            Log.e(TAG, "Error reading user role", e);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (currentUserId != null) {
            loadOrders();
        }
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
        recyclerView.setVisibility(View.GONE);
        layoutEmptyState.setVisibility(View.GONE);

        ordersRef = mDatabase.child("AllOrders");
        valueEventListener = ordersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Order> orderList = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Order order = dataSnapshot.getValue(Order.class);
                    if (order != null) {
                        order.setKey(dataSnapshot.getKey());
                        if (shouldIncludeOrder(order)) {
                            orderList.add(order);
                        }
                    }
                }
                
                adapter.submitList(orderList);
                progressBar.setVisibility(View.GONE);
                
                if (orderList.isEmpty()) {
                    layoutEmptyState.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    layoutEmptyState.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "Failed to load orders.", error.toException());
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    /**
     * Determines whether an order should be shown in this specific tab based on role and status.
     */
    protected abstract boolean shouldIncludeOrder(Order order);
}
