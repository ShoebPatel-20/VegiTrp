package com.example.vegitrp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.vegitrp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class BuyerProfileActivity extends BaseActivity {

    private TextView tvProfileBuyerName, tvProfileBuyerLocation, tvAverageRating;
    private ImageView bannerImage, ivEditProfile;
    private CircleImageView avatarImage;
    private String buyerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buyer_profile);

        buyerId = getIntent().getStringExtra("BUYER_ID");
        if (buyerId == null || buyerId.isEmpty()) {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                buyerId = currentUser.getUid();
            }
        }

        tvProfileBuyerName = findViewById(R.id.tvProfileBuyerName);
        tvProfileBuyerLocation = findViewById(R.id.tvProfileBuyerLocation);
        tvAverageRating = findViewById(R.id.tvAverageRating);

        bannerImage = findViewById(R.id.ivProfileBanner);
        avatarImage = findViewById(R.id.ivBuyerProfile);
        ivEditProfile = findViewById(R.id.ivEditProfile);

        Toolbar toolbar = findViewById(R.id.toolbarProfile);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        if (ivEditProfile != null) {
            ivEditProfile.setOnClickListener(v -> {
                startActivity(new Intent(BuyerProfileActivity.this, EditProfileActivity.class));
            });
        }

        loadBuyerDetails();
    }

    private void loadBuyerDetails() {
        if (buyerId == null) return;

        FirebaseDatabase.getInstance().getReference("users").child(buyerId)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String name = snapshot.child("name").getValue(String.class);
                        if (tvProfileBuyerName != null) {
                            tvProfileBuyerName.setText(name != null ? name : "Unknown Buyer");
                        }

                        String location = snapshot.child("location").getValue(String.class);
                        if (tvProfileBuyerLocation != null) {
                            if (location != null && !location.isEmpty()) {
                                tvProfileBuyerLocation.setText(location);
                            } else {
                                tvProfileBuyerLocation.setText("Region: Market Hub 1");
                            }
                        }

                        if (tvAverageRating != null) {
                            tvAverageRating.setText("5.0");
                        }

                        String avatarUrl = snapshot.child("avatarUrl").getValue(String.class);
                        if (avatarUrl == null || avatarUrl.isEmpty()) {
                            avatarUrl = snapshot.child("profileImage").getValue(String.class);
                        }
                        if (avatarUrl != null && !avatarUrl.isEmpty() && avatarImage != null && !isDestroyed()) {
                            Glide.with(BuyerProfileActivity.this).load(avatarUrl).into(avatarImage);
                        }

                        String bannerUrl = snapshot.child("bannerUrl").getValue(String.class);
                        if (bannerUrl != null && !bannerUrl.isEmpty() && bannerImage != null && !isDestroyed()) {
                            Glide.with(BuyerProfileActivity.this).load(bannerUrl).into(bannerImage);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) { }
            });
    }
}
