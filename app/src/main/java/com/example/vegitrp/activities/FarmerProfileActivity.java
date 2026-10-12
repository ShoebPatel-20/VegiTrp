package com.example.vegitrp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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

public class FarmerProfileActivity extends BaseActivity {

    private TextView tvProfileFarmerName, tvProfileFarmerEmail, tvProfileFarmLocation;
    private TextView tvAverageRating, tvActiveListed, tvItemsSold;
    private ImageView bannerImage, ivEditProfile;
    private CircleImageView avatarImage;
    private String farmerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farmer_profile);

        farmerId = getIntent().getStringExtra("FARMER_ID");
        if (farmerId == null || farmerId.isEmpty()) {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                farmerId = currentUser.getUid();
            }
        }

        tvProfileFarmerName = findViewById(R.id.tvProfileFarmerName);
        tvProfileFarmerEmail = findViewById(R.id.tvProfileFarmerEmail);
        tvProfileFarmLocation = findViewById(R.id.tvProfileFarmLocation);
        
        tvAverageRating = findViewById(R.id.tvAverageRating);
        tvActiveListed = findViewById(R.id.tvActiveListed);
        tvItemsSold = findViewById(R.id.tvItemsSold);
        
        bannerImage = findViewById(R.id.ivProfileBanner);
        avatarImage = findViewById(R.id.ivFarmerProfile);
        ivEditProfile = findViewById(R.id.ivEditProfile);

        Toolbar toolbar = findViewById(R.id.toolbarProfile);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        if (ivEditProfile != null) {
            ivEditProfile.setOnClickListener(v -> {
                startActivity(new Intent(FarmerProfileActivity.this, EditProfileActivity.class));
            });
        }

        loadFarmerDetails();
    }

    private void loadFarmerDetails() {
        if (farmerId == null) return;

        FirebaseDatabase.getInstance().getReference("Users").child(farmerId)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String name = snapshot.child("name").getValue(String.class);
                        String email = snapshot.child("email").getValue(String.class);
                        String location = snapshot.child("location").getValue(String.class);

                        if (tvProfileFarmerName != null) {
                            tvProfileFarmerName.setText(name != null ? name : "Unknown Farmer");
                        }

                        if (tvProfileFarmerEmail != null) {
                            if (email != null && !email.isEmpty()) {
                                tvProfileFarmerEmail.setText(email);
                                tvProfileFarmerEmail.setVisibility(View.VISIBLE);
                            } else {
                                tvProfileFarmerEmail.setVisibility(View.GONE);
                            }
                        }

                        if (tvProfileFarmLocation != null) {
                            if (location != null && !location.isEmpty()) {
                                tvProfileFarmLocation.setText(location);
                                tvProfileFarmLocation.setVisibility(View.VISIBLE);
                            } else {
                                tvProfileFarmLocation.setVisibility(View.GONE);
                            }
                        }
                        
                        if (tvAverageRating != null) tvAverageRating.setText("4.9");
                        if (tvActiveListed != null) tvActiveListed.setText("15");
                        if (tvItemsSold != null) tvItemsSold.setText("850+");
                        
                        String avatarUrl = snapshot.child("avatarUrl").getValue(String.class);
                        if (avatarUrl == null || avatarUrl.isEmpty()) {
                            avatarUrl = snapshot.child("profileImage").getValue(String.class);
                        }
                        if (avatarUrl != null && !avatarUrl.isEmpty() && avatarImage != null && !isDestroyed()) {
                            Glide.with(FarmerProfileActivity.this).load(avatarUrl).into(avatarImage);
                        }

                        String bannerUrl = snapshot.child("bannerUrl").getValue(String.class);
                        if (bannerUrl != null && !bannerUrl.isEmpty() && bannerImage != null && !isDestroyed()) {
                            Glide.with(FarmerProfileActivity.this).load(bannerUrl).into(bannerImage);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) { }
            });
    }
}
