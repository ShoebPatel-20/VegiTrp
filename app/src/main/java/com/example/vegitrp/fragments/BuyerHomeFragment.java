package com.example.vegitrp.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import androidx.navigation.Navigation;

import com.example.vegitrp.R;
import com.example.vegitrp.activities.DashboardActivity;
import com.example.vegitrp.activities.WalletActivity;
import com.example.vegitrp.models.Order;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class BuyerHomeFragment extends Fragment {

    private static final String TAG = "BuyerHomeFragment";
    private TextView tvGreetingName;
    private TextView tvTotalPurchases, tvPendingDelivery, tvReceivedOrders, tvTotalSpent;
    
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_buyer_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvGreetingName = view.findViewById(R.id.tv_greeting_name);
        tvTotalPurchases = view.findViewById(R.id.tv_total_purchases);
        tvPendingDelivery = view.findViewById(R.id.tv_pending_delivery);
        tvReceivedOrders = view.findViewById(R.id.tv_received_orders);
        tvTotalSpent = view.findViewById(R.id.tv_total_spent);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        
        if (mAuth.getCurrentUser() != null) {
            currentUserId = mAuth.getCurrentUser().getUid();
        }

        try {
            SharedPreferences prefs = getEncryptedSharedPreferences(requireContext());
            String name = prefs.getString("user_name", "Buyer");
            String firstName = name.contains(" ") ? name.split(" ")[0] : name;
            tvGreetingName.setText("Hello, " + firstName);
        } catch (Exception e) {
            Log.e(TAG, "Error reading prefs", e);
        }

        if (currentUserId != null) {
            mDatabase.child("Users").child(currentUserId).child("profileImage").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String url = snapshot.getValue(String.class);
                    if (url != null && !url.isEmpty() && isAdded()) {
                        com.bumptech.glide.Glide.with(requireContext()).load(url).into((android.widget.ImageView) view.findViewById(R.id.iv_header_avatar));
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }

        // Subtle entry transition for stats grid and quick actions
        View statsGrid = view.findViewById(R.id.grid_stats);
        View quickActions = view.findViewById(R.id.layout_quick_actions);
        if (statsGrid != null) {
            statsGrid.setAlpha(0f);
            statsGrid.setTranslationY(20f);
            statsGrid.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(450)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        }
        if (quickActions != null) {
            quickActions.setAlpha(0f);
            quickActions.setTranslationY(20f);
            quickActions.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setStartDelay(100)
                    .setDuration(450)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        }

        setupClickListeners(view);
        loadDashboardStats();
    }

    private void setupClickListeners(View view) {
        View.OnClickListener purchasesClickListener = v -> {
            Navigation.findNavController(v).navigate(R.id.navigation_buyer_dashboard);
        };
        view.findViewById(R.id.cv_total_purchases).setOnClickListener(purchasesClickListener);
        view.findViewById(R.id.cv_pending_delivery).setOnClickListener(purchasesClickListener);
        view.findViewById(R.id.cv_received_orders).setOnClickListener(purchasesClickListener);

        View.OnClickListener walletClickListener = v -> {
            startActivity(new android.content.Intent(requireContext(), WalletActivity.class));
        };
        view.findViewById(R.id.cv_total_spent).setOnClickListener(walletClickListener);
        view.findViewById(R.id.btn_view_wallet).setOnClickListener(walletClickListener);
        view.findViewById(R.id.iv_header_wallet).setOnClickListener(walletClickListener);

        view.findViewById(R.id.iv_header_avatar).setOnClickListener(v -> {
            startActivity(new android.content.Intent(requireContext(), com.example.vegitrp.activities.EditProfileActivity.class));
        });

        view.findViewById(R.id.btn_create_order).setOnClickListener(v -> {
            // Usually buyers just navigate to marketplace tab
            Navigation.findNavController(v).navigate(R.id.navigation_buyer_dashboard);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null && ((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getActivity() != null && ((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().show();
        }
    }

    private void loadDashboardStats() {
        if (currentUserId == null) return;
        mDatabase.child("AllOrders").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int total = 0;
                int pending = 0;
                int received = 0;
                double spent = 0.0;
                
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Order order = dataSnapshot.getValue(Order.class);
                    if (order != null && currentUserId.equals(order.getBuyerId())) {
                        total++;
                        String status = order.getStatus();
                        if ("COMPLETED".equalsIgnoreCase(status)) {
                            received++;
                            spent += order.getPrice();
                        } else {
                            pending++;
                        }
                    }
                }
                
                if (isAdded()) {
                    tvTotalPurchases.setText(String.valueOf(total));
                    tvPendingDelivery.setText(String.valueOf(pending));
                    tvReceivedOrders.setText(String.valueOf(received));
                    tvTotalSpent.setText("₹" + String.format("%.0f", spent));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database error: " + error.getMessage());
            }
        });
    }

    private SharedPreferences getEncryptedSharedPreferences(Context context) throws GeneralSecurityException, IOException {
        String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
        return EncryptedSharedPreferences.create(
                "user_prefs",
                masterKeyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        );
    }
}
