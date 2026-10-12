package com.example.vegitrp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.vegitrp.R;
import com.example.vegitrp.adapters.ReviewAdapter;
import com.example.vegitrp.models.Review;
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
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class DriverProfileActivity extends BaseActivity {

    private TextView tvProfileDriverName, tvProfileVehicle, tvAverageRating;
    private RecyclerView rvReviews;
    private ImageView bannerImage, ivEditProfile;
    private CircleImageView avatarImage;

    private ReviewAdapter reviewAdapter;
    private List<Review> reviewList;

    private String driverId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_profile);

        driverId = getIntent().getStringExtra("DRIVER_ID");
        if (driverId == null || driverId.isEmpty()) {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                driverId = currentUser.getUid();
            }
        }

        tvProfileDriverName = findViewById(R.id.tvProfileDriverName);
        tvProfileVehicle = findViewById(R.id.tvProfileVehicle);
        tvAverageRating = findViewById(R.id.tvAverageRating);
        
        bannerImage = findViewById(R.id.ivProfileBanner);
        avatarImage = findViewById(R.id.ivDriverProfile);
        ivEditProfile = findViewById(R.id.ivEditProfile);

        Toolbar toolbar = findViewById(R.id.toolbarProfile);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        if (ivEditProfile != null) {
            ivEditProfile.setOnClickListener(v -> {
                startActivity(new Intent(DriverProfileActivity.this, EditProfileActivity.class));
            });
        }
        
        rvReviews = findViewById(R.id.rvReviews);
        if (rvReviews != null) {
            rvReviews.setLayoutManager(new LinearLayoutManager(this));
            reviewList = new ArrayList<>();
            reviewAdapter = new ReviewAdapter(this, reviewList);
            rvReviews.setAdapter(reviewAdapter);
        }

        loadDriverDetails();
        loadReviews();
    }

    private void loadDriverDetails() {
        if (driverId == null) return;

        FirebaseDatabase.getInstance().getReference("users").child(driverId)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String name = snapshot.child("name").getValue(String.class);
                        if (tvProfileDriverName != null) {
                            tvProfileDriverName.setText(name != null ? name : "Unknown Driver");
                        }
                        
                        String avatarUrl = snapshot.child("avatarUrl").getValue(String.class);
                        if (avatarUrl == null || avatarUrl.isEmpty()) {
                            avatarUrl = snapshot.child("profileImage").getValue(String.class);
                        }
                        if (avatarUrl != null && !avatarUrl.isEmpty() && avatarImage != null && !isDestroyed()) {
                            Glide.with(DriverProfileActivity.this).load(avatarUrl).into(avatarImage);
                        }

                        String bannerUrl = snapshot.child("bannerUrl").getValue(String.class);
                        if (bannerUrl != null && !bannerUrl.isEmpty() && bannerImage != null && !isDestroyed()) {
                            Glide.with(DriverProfileActivity.this).load(bannerUrl).into(bannerImage);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) { }
            });
    }

    private void loadReviews() {
        if (driverId == null) return;

        DatabaseReference reviewsRef = FirebaseDatabase.getInstance().getReference("reviews");
        reviewsRef.orderByChild("driverId").equalTo(driverId)
            .addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (reviewList == null) return;
                    reviewList.clear();
                    float totalRating = 0;
                    int count = 0;

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Review review = dataSnapshot.getValue(Review.class);
                        if (review != null) {
                            reviewList.add(review);
                            totalRating += review.getRatingValue();
                            count++;
                        }
                    }

                    // Sort newest first
                    Collections.sort(reviewList, (r1, r2) -> Long.compare(r2.getTimestamp(), r1.getTimestamp()));
                    if (reviewAdapter != null) {
                        reviewAdapter.notifyDataSetChanged();
                    }

                    // Update UI stats
                    if (tvAverageRating != null) {
                        if (count > 0) {
                            float avg = totalRating / count;
                            tvAverageRating.setText(String.format(Locale.getDefault(), "%.1f", avg));
                        } else {
                            tvAverageRating.setText("0.0");
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(DriverProfileActivity.this, "Failed to load reviews.", Toast.LENGTH_SHORT).show();
                }
            });
    }
}
