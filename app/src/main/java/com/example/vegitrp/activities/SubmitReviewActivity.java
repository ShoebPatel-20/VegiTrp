package com.example.vegitrp.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import com.example.vegitrp.R;
import com.example.vegitrp.models.Review;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SubmitReviewActivity extends BaseActivity {

    private RatingBar ratingBar;
    private TextInputEditText etComment;
    private Button btnSubmitReview;

    private String orderId;
    private String driverId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit_review);

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle(R.string.review_toolbar_title);
                getSupportActionBar().setSubtitle(R.string.review_toolbar_subtitle);
            }
        }

        orderId = getIntent().getStringExtra("ORDER_ID");
        if (orderId == null) {
            orderId = getIntent().getStringExtra("orderId");
        }
        driverId = getIntent().getStringExtra("DRIVER_ID");
        if (driverId == null) {
            driverId = getIntent().getStringExtra("driverId");
        }

        ratingBar = findViewById(R.id.ratingBar);
        etComment = findViewById(R.id.etComment);
        btnSubmitReview = findViewById(R.id.btnSubmitReview);

        btnSubmitReview.setOnClickListener(v -> submitReview());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void submitReview() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "You must be logged in to leave a review.", Toast.LENGTH_SHORT).show();
            return;
        }

        float ratingValue = ratingBar.getRating();
        if (ratingValue == 0) {
            Toast.makeText(this, "Please select a rating.", Toast.LENGTH_SHORT).show();
            return;
        }

        String comment = etComment.getText().toString().trim();
        String reviewerId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference reviewsRef = FirebaseDatabase.getInstance().getReference("reviews");
        String reviewId = reviewsRef.push().getKey();

        if (reviewId != null) {
            Review review = new Review(reviewId, orderId, reviewerId, driverId, ratingValue, comment, System.currentTimeMillis());
            
            reviewsRef.child(reviewId).setValue(review)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Review submitted successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to submit review. Try again.", Toast.LENGTH_SHORT).show();
                });
        }
    }
}
