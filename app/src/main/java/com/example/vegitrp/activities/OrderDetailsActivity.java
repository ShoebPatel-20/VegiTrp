package com.example.vegitrp.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vegitrp.R;
import com.example.vegitrp.adapters.NegotiationAdapter;
import com.example.vegitrp.models.Negotiation;
import com.example.vegitrp.models.Order;
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
import java.util.UUID;
import com.example.vegitrp.models.Invoice;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.LinearLayout;

public class OrderDetailsActivity extends BaseActivity {

    private TextView tvProductName, tvQuantity, tvPrice, tvPinCode;
    private Button btnNegotiate, btnAccept, btnReject, btnInterest, btnDriverAccept, btnViewRoute, btnViewInvoice;
    private Button btnBuyerNegotiate, btnBuyerAccept;
    private LinearLayout llFarmerButtons, llDriverButtons, llBuyerButtons;
    private RecyclerView rvNegotiations;
    private NegotiationAdapter negotiationAdapter;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private String orderKey;
    private Order currentOrder;
    private List<Negotiation> negotiationList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_order_details);

            Toolbar toolbar = findViewById(R.id.toolbar);
            if (toolbar != null) {
                setSupportActionBar(toolbar);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle("Order Details");
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                }
            }

            orderKey = getIntent().getStringExtra("orderKey");
            mDatabase = FirebaseDatabase.getInstance().getReference();
            mAuth = FirebaseAuth.getInstance();

            tvProductName = findViewById(R.id.tv_product_name);
            tvQuantity = findViewById(R.id.tv_quantity);
            tvPrice = findViewById(R.id.tv_price);
            tvPinCode = findViewById(R.id.tv_pin_code);
            btnNegotiate = findViewById(R.id.btn_negotiate);
            btnAccept = findViewById(R.id.btn_accept);
            btnReject = findViewById(R.id.btn_reject);
            btnInterest = findViewById(R.id.btn_interest);
            btnDriverAccept = findViewById(R.id.btn_driver_accept);
            btnViewRoute = findViewById(R.id.btn_view_route);
            btnViewInvoice = findViewById(R.id.btn_view_invoice);
            rvNegotiations = findViewById(R.id.rv_negotiations);
            llFarmerButtons = findViewById(R.id.ll_farmer_buttons);
            llDriverButtons = findViewById(R.id.ll_driver_buttons);
            llBuyerButtons = findViewById(R.id.ll_buyer_buttons);
            btnBuyerNegotiate = findViewById(R.id.btnNegotiate);
            btnBuyerAccept = findViewById(R.id.btnAccept);

            if (tvProductName == null || btnNegotiate == null || btnBuyerNegotiate == null || btnBuyerAccept == null) {
                Log.e("UI_ERROR", "View not found in OrderDetailsActivity");
                return;
            }

            // Disable buttons until data is loaded
            btnNegotiate.setEnabled(false);
            if (btnAccept != null) btnAccept.setEnabled(false);
            if (btnReject != null) btnReject.setEnabled(false);
            if (btnInterest != null) btnInterest.setEnabled(false);
            if (btnDriverAccept != null) btnDriverAccept.setEnabled(false);

            negotiationAdapter = new NegotiationAdapter(negotiationList);
            rvNegotiations.setLayoutManager(new LinearLayoutManager(this));
            rvNegotiations.setAdapter(negotiationAdapter);

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                loadOrderDetails();
                loadNegotiations();
            }, 300);

            btnNegotiate.setOnClickListener(v -> showNegotiateDialog());

            btnBuyerNegotiate.setOnClickListener(v -> {
                if (orderKey == null) {
                    Toast.makeText(this, "Order ID missing", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(OrderDetailsActivity.this, ChatActivity.class);
                intent.putExtra("orderId", orderKey);
                startActivity(intent);
            });

            btnBuyerAccept.setOnClickListener(v -> {
                if (orderKey == null) {
                    Toast.makeText(this, "Order ID missing", Toast.LENGTH_SHORT).show();
                    return;
                }
                DatabaseReference ref = FirebaseDatabase.getInstance()
                        .getReference("AllOrders")
                        .child(orderKey);

                java.util.Map<String, Object> update = new java.util.HashMap<>();
                update.put("status", "ACCEPTED");

                ref.updateChildren(update)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Order Accepted", Toast.LENGTH_SHORT).show();
                            btnBuyerAccept.setEnabled(false);
                            btnBuyerAccept.setText("ACCEPTED");
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            });

            if (btnAccept != null) btnAccept.setOnClickListener(v -> acceptOrder());
            if (btnDriverAccept != null) btnDriverAccept.setOnClickListener(v -> acceptOrder());
            if (btnReject != null) btnReject.setOnClickListener(v -> rejectOrder());
            if (btnInterest != null) btnInterest.setOnClickListener(v -> expressInterest());
            
            btnViewInvoice.setOnClickListener(v -> {
                Intent intent = new Intent(OrderDetailsActivity.this, InvoiceActivity.class);
                intent.putExtra("orderId", orderKey);
                startActivity(intent);
            });
            btnViewRoute.setOnClickListener(v -> {
                android.content.Intent mapIntent = new android.content.Intent(OrderDetailsActivity.this, MapActivity.class);

                if (currentOrder != null) {
                    if (currentOrder.getPickupLatitude() != 0.0 && currentOrder.getPickupLongitude() != 0.0) {
                        mapIntent.putExtra("ORIGIN_LAT", currentOrder.getPickupLatitude());
                        mapIntent.putExtra("ORIGIN_LNG", currentOrder.getPickupLongitude());
                    }
                    if (currentOrder.getDeliveryLatitude() != 0.0 && currentOrder.getDeliveryLongitude() != 0.0) {
                        mapIntent.putExtra("DEST_LAT", currentOrder.getDeliveryLatitude());
                        mapIntent.putExtra("DEST_LNG", currentOrder.getDeliveryLongitude());
                    }

                    if (currentOrder.getPickupAddress() != null && !currentOrder.getPickupAddress().isEmpty()) {
                        mapIntent.putExtra("PICKUP_LOCATION", currentOrder.getPickupAddress());
                    }
                    if (currentOrder.getDeliveryAddress() != null && !currentOrder.getDeliveryAddress().isEmpty()) {
                        mapIntent.putExtra("DROPOFF_LOCATION", currentOrder.getDeliveryAddress());
                    } else if (currentOrder.getPinCode() != null && !currentOrder.getPinCode().isEmpty()) {
                        mapIntent.putExtra("DROPOFF_LOCATION", "PIN: " + currentOrder.getPinCode());
                    }
                    if (currentOrder.getPinCode() != null && !currentOrder.getPinCode().isEmpty()) {
                        mapIntent.putExtra("pinCode", currentOrder.getPinCode());
                    }
                }
                mapIntent.putExtra("orderId", orderKey);
                startActivity(mapIntent);
            });

            Log.d("SCREEN_DEBUG", "Activity started: OrderDetailsActivity");
        } catch (Exception e) {
            Log.e("CRASH_ERROR", "Activity crash: " + e.getMessage());
        }
    }


    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadOrderDetails() {
        mDatabase.child("AllOrders").child(orderKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentOrder = snapshot.getValue(Order.class);
                if (currentOrder != null) {
                    tvProductName.setText(currentOrder.getProduct());
                    tvQuantity.setText(currentOrder.getQuantity());
                    tvPrice.setText(String.valueOf(currentOrder.getPrice()));
                    tvPinCode.setText(currentOrder.getPinCode());
                    
                    if ("ACCEPTED".equalsIgnoreCase(currentOrder.getStatus()) || "Accepted".equalsIgnoreCase(currentOrder.getStatus()) || "Completed".equalsIgnoreCase(currentOrder.getStatus())) {
                        if (llFarmerButtons != null) llFarmerButtons.setVisibility(View.GONE);
                        if (llDriverButtons != null) llDriverButtons.setVisibility(View.GONE);
                        if (llBuyerButtons != null) llBuyerButtons.setVisibility(View.GONE);
                        btnNegotiate.setVisibility(View.GONE);
                        btnViewInvoice.setVisibility(View.VISIBLE);

                        if (btnBuyerAccept != null) {
                            btnBuyerAccept.setEnabled(false);
                            btnBuyerAccept.setText("ACCEPTED");
                        }
                    } else {
                        btnNegotiate.setEnabled(true);
                        if (btnAccept != null) btnAccept.setEnabled(true);
                        if (btnReject != null) btnReject.setEnabled(true);
                        if (btnInterest != null) btnInterest.setEnabled(true);
                        if (btnDriverAccept != null) btnDriverAccept.setEnabled(true);
                        if (btnBuyerAccept != null) btnBuyerAccept.setEnabled(true);
                        btnViewInvoice.setVisibility(View.GONE);
                        applyRoleBasedButtons();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(OrderDetailsActivity.this, "Failed to load order details.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadNegotiations() {
        mDatabase.child("AllOrders").child(orderKey).child("negotiations").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                negotiationList.clear();
                for (DataSnapshot negotiationSnapshot : snapshot.getChildren()) {
                    Negotiation negotiation = negotiationSnapshot.getValue(Negotiation.class);
                    if (negotiation != null) {
                        negotiationList.add(negotiation);
                    }
                }
                Collections.reverse(negotiationList); // Show latest on top
                negotiationAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(OrderDetailsActivity.this, "Failed to load negotiations.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showNegotiateDialog() {
        if (currentOrder == null) return; // Prevent action if data not loaded
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_negotiate, null);
        builder.setView(dialogView);

        final EditText etNewPrice = dialogView.findViewById(R.id.et_new_price);
        final EditText etNewQuantity = dialogView.findViewById(R.id.et_new_quantity);

        builder.setPositiveButton("Submit", (dialog, which) -> {
            String newPriceStr = etNewPrice.getText().toString();
            String newQuantity = etNewQuantity.getText().toString();

            if (TextUtils.isEmpty(newPriceStr) && TextUtils.isEmpty(newQuantity)) {
                Toast.makeText(this, "Please enter a new price or quantity.", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                String senderId = currentUser.getUid();
                String senderName = currentUser.getDisplayName();
                double price = TextUtils.isEmpty(newPriceStr) ? 0 : Double.parseDouble(newPriceStr);

                Negotiation negotiation = new Negotiation(price, newQuantity, senderId, senderName);
                mDatabase.child("AllOrders").child(orderKey).child("negotiations").push().setValue(negotiation);

                addInterestedFarmer(senderId);
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.create().show();
    }

    private void acceptOrder() {
        if (currentOrder == null) return; // Prevent action if data not loaded
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String farmerId = currentUser.getUid();
            String farmerName = currentUser.getDisplayName(); // Note: Assumes DisplayName is set

            // Formalize Acceptance
            mDatabase.child("AllOrders").child(orderKey).child("status").setValue("Accepted");
            mDatabase.child("AllOrders").child(orderKey).child("assignedFarmerId").setValue(farmerId);
            
            // Generate Unified Invoice Snapshot
            generateInvoice(farmerName);
        }
    }

    private void generateInvoice(String sellerName) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Invoices");
        String invoiceId = "INV_" + orderKey;
        
        ref.child(invoiceId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    double negPrice = 0;
                    if (!negotiationList.isEmpty()) {
                        negPrice = negotiationList.get(negotiationList.size() - 1).getPrice();
                    }

                    double unitPrice = currentOrder.getPrice();
                    double finalPrice = negPrice > 0 ? negPrice : unitPrice;
                    
                    double parsedQuantity = 1.0;
                    try { parsedQuantity = Double.parseDouble(currentOrder.getQuantity()); } catch (Exception ignored) {}
                    double totalAmount = parsedQuantity * finalPrice;

                    java.util.HashMap<String, Object> invoice = new java.util.HashMap<>();
                    invoice.put("invoiceId", invoiceId);
                    invoice.put("orderId", orderKey);
                    invoice.put("buyerName", "Buyer_" + currentOrder.getBuyerId().substring(0, 5));
                    invoice.put("sellerName", sellerName != null ? sellerName : "Assigned Farmer");
                    invoice.put("driverName", "Pending Assignment");
                    invoice.put("productName", currentOrder.getProduct());
                    invoice.put("quantity", currentOrder.getQuantity());
                    invoice.put("unit", "KG");
                    invoice.put("pricePerUnit", String.valueOf(unitPrice));
                    invoice.put("finalPrice", String.valueOf(finalPrice));
                    invoice.put("totalAmount", String.valueOf(totalAmount));
                    invoice.put("status", "ACCEPTED");
                    invoice.put("payment", "PENDING");
                    invoice.put("timestamp", System.currentTimeMillis());

                    ref.child(invoiceId).setValue(invoice)
                            .addOnSuccessListener(aVoid -> Toast.makeText(OrderDetailsActivity.this, "Order Accepted & Invoice Generated!", Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void rejectOrder() {
        if (currentOrder == null) return;
        Toast.makeText(this, "Order Rejected", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void expressInterest() {
        if (currentOrder == null) return;
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String driverId = currentUser.getUid();
            // Store driver interest in DB or navigate
            Toast.makeText(this, "Interest Expressed", Toast.LENGTH_SHORT).show();
        }
    }

    private void applyRoleBasedButtons() {
        try {
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            SharedPreferences sharedPreferences = EncryptedSharedPreferences.create(
                    "user_prefs",
                    masterKeyAlias,
                    this,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );

            String role = sharedPreferences.getString("user_role", "");

            if ("Driver".equals(role)) {
                if (llFarmerButtons != null) llFarmerButtons.setVisibility(View.GONE);
                if (llDriverButtons != null) llDriverButtons.setVisibility(View.VISIBLE);
                btnNegotiate.setVisibility(View.GONE); // Driver does not negotiate
            } else if ("Farmer".equals(role)) {
                if (llDriverButtons != null) llDriverButtons.setVisibility(View.GONE);
                if (llBuyerButtons != null) llBuyerButtons.setVisibility(View.GONE);
                if (llFarmerButtons != null) llFarmerButtons.setVisibility(View.VISIBLE);
                btnNegotiate.setVisibility(View.VISIBLE);
            } else if ("Buyer".equals(role)) {
                if (llFarmerButtons != null) llFarmerButtons.setVisibility(View.GONE);
                if (llDriverButtons != null) llDriverButtons.setVisibility(View.GONE);
                if (llBuyerButtons != null) llBuyerButtons.setVisibility(View.VISIBLE);
                btnNegotiate.setVisibility(View.GONE);
            } else {
                if (llFarmerButtons != null) llFarmerButtons.setVisibility(View.GONE);
                if (llDriverButtons != null) llDriverButtons.setVisibility(View.GONE);
                if (llBuyerButtons != null) llBuyerButtons.setVisibility(View.GONE);
                btnNegotiate.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            Log.e("ROLE_ERROR", "Failed to load role", e);
        }
    }

    private void addInterestedFarmer(String farmerId) {
        if (currentOrder == null) return; // Prevent action if data not loaded
        if (currentOrder.getInterestedFarmers() == null) {
            currentOrder.setInterestedFarmers(new ArrayList<>());
        }
        if (!currentOrder.getInterestedFarmers().contains(farmerId)) {
            currentOrder.getInterestedFarmers().add(farmerId);
            mDatabase.child("AllOrders").child(orderKey).child("interestedFarmers").setValue(currentOrder.getInterestedFarmers());
        }
    }
}
