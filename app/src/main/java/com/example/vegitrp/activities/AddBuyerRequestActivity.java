package com.example.vegitrp.activities;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.example.vegitrp.R;
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
import java.util.Locale;
import java.util.concurrent.Executors;

public class AddBuyerRequestActivity extends BaseActivity {

    private static final String TAG = "AddBuyerRequest";

    private Spinner spinnerVegetableName, spinnerUnit;
    private EditText etQuantity, etPrice, etPickupAddress, etDeliveryAddress, etPinCode;
    private Button btnSubmitRequest;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_buyer_request);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        spinnerVegetableName = findViewById(R.id.spinner_vegetable_name);
        spinnerUnit = findViewById(R.id.spinner_unit);
        etQuantity = findViewById(R.id.et_quantity);
        etPrice = findViewById(R.id.et_price);
        etPickupAddress = findViewById(R.id.et_pickup_address);
        etDeliveryAddress = findViewById(R.id.et_delivery_address);
        etPinCode = findViewById(R.id.et_pin_code);
        btnSubmitRequest = findViewById(R.id.btn_submit_request);

        loadProducts();
        loadUnits();

        btnSubmitRequest.setOnClickListener(v -> submitOrderRequest());
    }

    private void submitOrderRequest() {
        if (spinnerVegetableName.getSelectedItemPosition() == 0) {
            Toast.makeText(this, R.string.select_product_prompt, Toast.LENGTH_SHORT).show();
            return;
        }

        String productName = spinnerVegetableName.getSelectedItem().toString();
        String quantity = etQuantity.getText().toString().trim();
        String unit = spinnerUnit.getSelectedItem().toString();
        String priceString = etPrice.getText().toString().trim();
        String pickupAddress = etPickupAddress != null ? etPickupAddress.getText().toString().trim() : "";
        String deliveryAddress = etDeliveryAddress != null ? etDeliveryAddress.getText().toString().trim() : "";
        String pinCode = etPinCode.getText().toString().trim();

        if (productName.isEmpty() || quantity.isEmpty() || unit.isEmpty() || priceString.isEmpty()
                || pickupAddress.isEmpty() || deliveryAddress.isEmpty() || pinCode.isEmpty()) {
            Toast.makeText(this, R.string.fill_all_fields_toast, Toast.LENGTH_SHORT).show();
            return;
        }

        if (pinCode.length() != 6) {
            Toast.makeText(this, R.string.invalid_pin_code_toast, Toast.LENGTH_SHORT).show();
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
        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSubmitRequest.setEnabled(false);
        Toast.makeText(this, "Resolving location coordinates...", Toast.LENGTH_SHORT).show();

        String userId = currentUser.getUid();

        Executors.newSingleThreadExecutor().execute(() -> {
            double pickupLat = 0.0;
            double pickupLng = 0.0;
            double deliveryLat = 0.0;
            double deliveryLng = 0.0;

            boolean geocoderAvailable = Geocoder.isPresent();
            if (geocoderAvailable) {
                Geocoder geocoder = new Geocoder(AddBuyerRequestActivity.this, Locale.getDefault());
                try {
                    String pickupQuery = pickupAddress.toLowerCase().contains("india") ? pickupAddress : pickupAddress + ", India";
                    List<Address> pickupAddrs = geocoder.getFromLocationName(pickupQuery, 1);
                    if (pickupAddrs != null && !pickupAddrs.isEmpty()) {
                        pickupLat = pickupAddrs.get(0).getLatitude();
                        pickupLng = pickupAddrs.get(0).getLongitude();
                    }

                    String deliveryQuery = deliveryAddress.toLowerCase().contains("india") ? deliveryAddress : deliveryAddress + ", India";
                    List<Address> deliveryAddrs = geocoder.getFromLocationName(deliveryQuery, 1);
                    if (deliveryAddrs != null && !deliveryAddrs.isEmpty()) {
                        deliveryLat = deliveryAddrs.get(0).getLatitude();
                        deliveryLng = deliveryAddrs.get(0).getLongitude();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Geocoding error during order creation: " + e.getMessage());
                }
            }

            final double finalPickupLat = pickupLat;
            final double finalPickupLng = pickupLng;
            final double finalDeliveryLat = deliveryLat;
            final double finalDeliveryLng = deliveryLng;

            runOnUiThread(() -> {
                DatabaseReference orderRef = FirebaseDatabase.getInstance().getReference("AllOrders");
                String orderId = orderRef.push().getKey();

                if (orderId == null) {
                    btnSubmitRequest.setEnabled(true);
                    Toast.makeText(AddBuyerRequestActivity.this, "Order ID generation failed", Toast.LENGTH_SHORT).show();
                    return;
                }

                java.util.Map<String, Object> orderMap = new java.util.HashMap<>();
                orderMap.put("orderId", orderId);
                orderMap.put("buyerId", userId);
                orderMap.put("product", productName);
                orderMap.put("quantity", quantity);
                orderMap.put("unit", unit);
                orderMap.put("price", price);
                orderMap.put("pickupAddress", pickupAddress);
                orderMap.put("deliveryAddress", deliveryAddress);
                if (finalPickupLat != 0.0 && finalPickupLng != 0.0) {
                    orderMap.put("pickupLatitude", finalPickupLat);
                    orderMap.put("pickupLongitude", finalPickupLng);
                }
                if (finalDeliveryLat != 0.0 && finalDeliveryLng != 0.0) {
                    orderMap.put("deliveryLatitude", finalDeliveryLat);
                    orderMap.put("deliveryLongitude", finalDeliveryLng);
                }
                orderMap.put("pinCode", pinCode);
                orderMap.put("status", "PENDING");
                orderMap.put("timestamp", System.currentTimeMillis());

                orderRef.child(orderId).setValue(orderMap)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(AddBuyerRequestActivity.this, "Order Created", Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            btnSubmitRequest.setEnabled(true);
                            Toast.makeText(AddBuyerRequestActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            Log.e("ORDER_SAVE_ERROR", e.getMessage());
                        });
            });
        });
    }

    private void loadProducts() {
        mDatabase.child("Products").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> productNames = new ArrayList<>();
                productNames.add(getString(R.string.select_product_prompt));

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String productName = dataSnapshot.getValue(String.class);
                    if (productName != null) {
                        productNames.add(productName);
                    }
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(AddBuyerRequestActivity.this,
                        android.R.layout.simple_spinner_item, productNames) {
                    @Override
                    public boolean isEnabled(int position) {
                        return position != 0;
                    }

                    @Override
                    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                        View view = super.getDropDownView(position, convertView, parent);
                        TextView tv = (TextView) view;
                        if (position == 0) {
                            tv.setTextColor(ContextCompat.getColor(getContext(), R.color.brand_text_muted));
                        } else {
                            tv.setTextColor(ContextCompat.getColor(getContext(), R.color.brand_text_title));
                        }
                        return view;
                    }
                };
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerVegetableName.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AddBuyerRequestActivity.this, "Failed to load products.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUnits() {
        mDatabase.child("Units").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> unitNames = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String unitName = dataSnapshot.getValue(String.class);
                    if (unitName != null) {
                        unitNames.add(unitName);
                    }
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(AddBuyerRequestActivity.this,
                        android.R.layout.simple_spinner_item, unitNames);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerUnit.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AddBuyerRequestActivity.this, "Failed to load units.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
