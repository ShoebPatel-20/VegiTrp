package com.example.vegitrp.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.example.vegitrp.R;
import com.example.vegitrp.models.Listing;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddListingActivity extends BaseActivity {

    private EditText etProductName, etQuantity, etPrice;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_listing);

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        etProductName = findViewById(R.id.et_product_name);
        etQuantity = findViewById(R.id.et_quantity);
        etPrice = findViewById(R.id.et_price);
        Button btnSubmitListing = findViewById(R.id.btn_submit_listing);

        btnSubmitListing.setOnClickListener(v -> {
            String productName = etProductName.getText().toString().trim();
            String quantity = etQuantity.getText().toString().trim();
            String priceString = etPrice.getText().toString().trim();

            if (productName.isEmpty() || quantity.isEmpty() || priceString.isEmpty()) {
                Toast.makeText(this, R.string.fill_all_fields_toast, Toast.LENGTH_SHORT).show();
                return;
            }

            double price;
            try {
                price = Double.parseDouble(priceString);
            } catch (NumberFormatException e) {
                Toast.makeText(this, R.string.invalid_price_toast, Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                String userId = currentUser.getUid();
                Listing listing = new Listing(userId, productName, quantity, "Active", price);

                mDatabase.child("Listings").push().setValue(listing)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(AddListingActivity.this, "Listing submitted successfully", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                Toast.makeText(AddListingActivity.this, "Failed to submit listing", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }
}
