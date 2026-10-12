package com.example.vegitrp.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vegitrp.R;
import com.example.vegitrp.adapters.ListingAdapter;
import com.example.vegitrp.models.Listing;
import com.example.vegitrp.models.Order;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MarketplaceFragment extends Fragment implements ListingAdapter.OnListingActionListener, PaymentResultListener {

    private static final String TAG = "MarketplaceFragment";
    private ListingAdapter listingAdapter;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private String currentOrderId;
    private ProgressBar progressBar;
    private RecyclerView rvCompletedTrips;
    private View layoutEmptyState;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_marketplace, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        rvCompletedTrips = view.findViewById(R.id.rv_completed_trips);
        progressBar = view.findViewById(R.id.progress_bar);
        layoutEmptyState = view.findViewById(R.id.layout_empty_state);

        listingAdapter = new ListingAdapter(this);

        rvCompletedTrips.setLayoutManager(new LinearLayoutManager(getContext()));
        rvCompletedTrips.setAdapter(listingAdapter);

        loadCompletedTrips();
    }

    private void loadCompletedTrips() {
        progressBar.setVisibility(View.VISIBLE);
        rvCompletedTrips.setVisibility(View.GONE);
        if (layoutEmptyState != null) {
            layoutEmptyState.setVisibility(View.GONE);
        }

        mDatabase.child("Listings").orderByChild("status").equalTo("Completed").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Listing> tripList = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Listing trip = dataSnapshot.getValue(Listing.class);
                    if (trip != null) {
                        trip.setKey(dataSnapshot.getKey());
                        tripList.add(trip);
                    }
                }
                listingAdapter.submitList(tripList);
                progressBar.setVisibility(View.GONE);
                if (tripList.isEmpty()) {
                    rvCompletedTrips.setVisibility(View.GONE);
                    if (layoutEmptyState != null) {
                        layoutEmptyState.setVisibility(View.VISIBLE);
                    }
                } else {
                    rvCompletedTrips.setVisibility(View.VISIBLE);
                    if (layoutEmptyState != null) {
                        layoutEmptyState.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "Failed to load completed trips.", error.toException());
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Failed to load completed trips.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onActionClick(int position) {
        Listing trip = listingAdapter.getCurrentList().get(position);
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String buyerId = currentUser.getUid();
            double price = trip.getPrice();

            DatabaseReference newOrderRef = mDatabase.child("AllOrders").push();
            currentOrderId = newOrderRef.getKey();
            Order order = new Order(buyerId, trip.getProduct(), trip.getQuantity(), "Placed", price, "000000");

            newOrderRef.setValue(order)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            startPayment(price);
                        } else {
                            Toast.makeText(getContext(), "Failed to place order.", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void startPayment(double amount) {
        final Activity activity = getActivity();
        final Checkout checkout = new Checkout();
        checkout.setKeyID("YOUR_KEY_ID"); // Replace with your key

        try {
            JSONObject options = new JSONObject();
            options.put("name", "VegiTrp");
            options.put("description", "Vegetable Purchase");
            options.put("currency", "INR");
            options.put("amount", amount * 100); // Amount in paise

            checkout.open(activity, options);
        } catch (Exception e) {
            Toast.makeText(activity, "Error in payment: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    public void onPaymentSuccess(String razorpayPaymentID) {
        if (currentOrderId != null) {
            mDatabase.child("AllOrders").child(currentOrderId).child("status").setValue("Paid");
            Toast.makeText(getContext(), "Payment Successful: " + razorpayPaymentID, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPaymentError(int code, String response) {
        Toast.makeText(getContext(), "Payment failed: " + code + " " + response, Toast.LENGTH_SHORT).show();
    }
}
