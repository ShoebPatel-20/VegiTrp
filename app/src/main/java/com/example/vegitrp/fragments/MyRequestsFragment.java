package com.example.vegitrp.fragments;

import android.content.Intent;
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
import com.example.vegitrp.activities.AddListingActivity;
import com.example.vegitrp.adapters.ListingAdapter;
import com.example.vegitrp.models.Listing;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MyRequestsFragment extends Fragment implements ListingAdapter.OnListingActionListener {

    private static final String TAG = "MyRequestsFragment";
    private ListingAdapter listingAdapter;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;
    private RecyclerView rvTransportRequests;
    private View layoutEmptyState;
    private ValueEventListener valueEventListener;
    private DatabaseReference transportRequestsRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_requests, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        rvTransportRequests = view.findViewById(R.id.rv_transport_requests);
        progressBar = view.findViewById(R.id.progress_bar);
        layoutEmptyState = view.findViewById(R.id.layout_empty_state);
        FloatingActionButton fabAddRequest = view.findViewById(R.id.fab_add_request);

        listingAdapter = new ListingAdapter(this);

        rvTransportRequests.setLayoutManager(new LinearLayoutManager(getContext()));
        rvTransportRequests.setAdapter(listingAdapter);

        fabAddRequest.setOnClickListener(v -> startActivity(new Intent(getActivity(), AddListingActivity.class)));
    }

    @Override
    public void onResume() {
        super.onResume();
        loadTransportRequests();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (transportRequestsRef != null && valueEventListener != null) {
            transportRequestsRef.removeEventListener(valueEventListener);
        }
    }

    private void loadTransportRequests() {
        progressBar.setVisibility(View.VISIBLE);
        rvTransportRequests.setVisibility(View.GONE);
        if (layoutEmptyState != null) {
            layoutEmptyState.setVisibility(View.GONE);
        }

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            transportRequestsRef = mDatabase.child("Listings");
            valueEventListener = transportRequestsRef.orderByChild("farmerId").equalTo(userId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    List<Listing> transportRequestList = new ArrayList<>();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Listing request = dataSnapshot.getValue(Listing.class);
                        if (request != null) {
                            request.setKey(dataSnapshot.getKey());
                            transportRequestList.add(request);
                        }
                    }
                    listingAdapter.submitList(transportRequestList);
                    progressBar.setVisibility(View.GONE);
                    if (transportRequestList.isEmpty()) {
                        rvTransportRequests.setVisibility(View.GONE);
                        if (layoutEmptyState != null) {
                            layoutEmptyState.setVisibility(View.VISIBLE);
                        }
                    } else {
                        rvTransportRequests.setVisibility(View.VISIBLE);
                        if (layoutEmptyState != null) {
                            layoutEmptyState.setVisibility(View.GONE);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.w(TAG, "Failed to load transport requests.", error.toException());
                    progressBar.setVisibility(View.GONE);
                }
            });
        }
    }

    @Override
    public void onActionClick(int position) {
        // Not used by farmers
    }
}
