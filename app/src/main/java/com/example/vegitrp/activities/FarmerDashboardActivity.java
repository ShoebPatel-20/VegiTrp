package com.example.vegitrp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vegitrp.R;
import com.example.vegitrp.adapters.ListingAdapter;
import com.example.vegitrp.models.Listing;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FarmerDashboardActivity extends BaseActivity {

    private static final String TAG = "FarmerDashboardActivity";
    private ListingAdapter listingAdapter;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;
    private RecyclerView rvListings;
    private ValueEventListener valueEventListener;
    private Query listingsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farmer_dashboard);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("All Listings");
        }

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        progressBar = findViewById(R.id.progress_bar);
        rvListings = findViewById(R.id.rv_listings);
        FloatingActionButton fabAddListing = findViewById(R.id.fab_add_listing);

        listingAdapter = new ListingAdapter(null); // No listener needed for farmer

        rvListings.setLayoutManager(new LinearLayoutManager(this));
        rvListings.setAdapter(listingAdapter);

        fabAddListing.setOnClickListener(v -> startActivity(new Intent(this, AddListingActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadListings();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (listingsRef != null && valueEventListener != null) {
            listingsRef.removeEventListener(valueEventListener);
        }
    }

    private void loadListings() {
        progressBar.setVisibility(View.VISIBLE);
        rvListings.setVisibility(View.GONE);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            listingsRef = mDatabase.child("Listings").orderByChild("farmerId").equalTo(userId);
            valueEventListener = listingsRef.addValueEventListener(new ValueEventListener() {
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
                    progressBar.setVisibility(View.GONE);
                    rvListings.setVisibility(View.VISIBLE);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.w(TAG, "Failed to load listings.", error.toException());
                    progressBar.setVisibility(View.GONE);
                }
            });
        }
    }
}
