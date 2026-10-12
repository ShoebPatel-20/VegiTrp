package com.example.vegitrp.activities;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.vegitrp.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class InvoiceActivity extends BaseActivity {

    private static final String TAG = "INVOICE_DEBUG";
    private static final int PERMISSION_REQUEST_CODE = 100;

    private TextView tvInvoiceId, tvDate, tvTime, tvOrderId, tvBuyerName, tvSellerName;
    private TextView tvProduct, tvQuantity, tvUnitPrice, tvNegotiatedPrice, tvTotalAmount, tvTags;
    private Button btnDownloadPdf, btnShare;

    private String orderId;
    private View invoiceCard;
    private boolean isInvoiceGenerated = false;
    private File pdfFile = null;
    private Uri pdfUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invoice);

        orderId = getIntent().getStringExtra("orderId");
        if (orderId == null) {
            String invId = getIntent().getStringExtra("invoiceId");
            if (invId != null && invId.startsWith("INV_")) {
                orderId = invId.replace("INV_", "");
            } else {
                Toast.makeText(this, "Order ID missing", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        }

        setupToolbar();
        bindViews();
        
        new Handler(Looper.getMainLooper()).postDelayed(this::loadInvoice, 300);

        btnDownloadPdf.setOnClickListener(v -> checkPermissionAndDownload());
        btnShare.setOnClickListener(v -> shareInvoice());
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle(R.string.invoice_toolbar_title);
                getSupportActionBar().setSubtitle(R.string.invoice_toolbar_subtitle);
            }
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

    private void bindViews() {
        invoiceCard = findViewById(R.id.invoice_card);
        tvInvoiceId = findViewById(R.id.tvInvoiceId);
        tvDate = findViewById(R.id.tvDate);
        tvTime = findViewById(R.id.tvTime);
        tvOrderId = findViewById(R.id.tvOrderId);
        tvBuyerName = findViewById(R.id.tvBuyerName);
        tvSellerName = findViewById(R.id.tvSellerName);
        tvProduct = findViewById(R.id.tvProduct);
        tvQuantity = findViewById(R.id.tvQuantity);
        tvUnitPrice = findViewById(R.id.tvUnitPrice);
        tvNegotiatedPrice = findViewById(R.id.tvNegotiatedPrice);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        tvTags = findViewById(R.id.tvTags);

        btnDownloadPdf = findViewById(R.id.btnDownloadPdf);
        btnShare = findViewById(R.id.btnShare);
        
        btnDownloadPdf.setEnabled(false);
        btnShare.setEnabled(false);
    }

    private void loadInvoice() {
        Log.d(TAG, "Loading Invoice for Order ID: " + orderId);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Invoices");
        String invoiceKey = "INV_" + orderId;

        ref.child(invoiceKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    displayInvoiceData(snapshot);
                } else if (!isInvoiceGenerated) {
                    isInvoiceGenerated = true;
                    generateFallbackInvoice();
                } else {
                    Toast.makeText(InvoiceActivity.this, "Invoice not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(InvoiceActivity.this, "Database error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayInvoiceData(DataSnapshot snapshot) {
        tvInvoiceId.setText(getStringValue(snapshot, "invoiceId", "N/A"));
        tvOrderId.setText(getStringValue(snapshot, "orderId", "N/A"));
        tvBuyerName.setText(getStringValue(snapshot, "buyerName", "N/A"));
        tvSellerName.setText(getStringValue(snapshot, "sellerName", "N/A"));
        tvProduct.setText(getStringValue(snapshot, "productName", "N/A"));
        tvQuantity.setText(getStringValue(snapshot, "quantity", "0"));
        tvUnitPrice.setText("₹" + getStringValue(snapshot, "pricePerUnit", "0"));
        tvNegotiatedPrice.setText("₹" + getStringValue(snapshot, "finalPrice", "0"));
        tvTotalAmount.setText("₹" + getStringValue(snapshot, "totalAmount", "0"));
        
        String status = getStringValue(snapshot, "status", "N/A");
        String payment = getStringValue(snapshot, "payment", "N/A");
        tvTags.setText("Status: " + status + " | Payment: " + payment);

        Long timeObj = snapshot.child("timestamp").getValue(Long.class);
        long timeMillis = timeObj != null ? timeObj : System.currentTimeMillis();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        tvDate.setText(dateFormat.format(new Date(timeMillis)));
        tvTime.setText(timeFormat.format(new Date(timeMillis)));

        btnDownloadPdf.setEnabled(true);
        btnShare.setEnabled(true);
    }

    private String getStringValue(DataSnapshot snapshot, String key, String defaultValue) {
        Object val = snapshot.child(key).getValue();
        return val != null ? String.valueOf(val) : defaultValue;
    }

    private void generateFallbackInvoice() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        ref.child("AllOrders").child(orderId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String prod = snapshot.child("product").getValue(String.class);
                    Double price = snapshot.child("price").getValue(Double.class);
                    if (price == null) price = 0.0;

                    String qtyRaw = snapshot.child("quantity").getValue(String.class);
                    double qty = 1.0;
                    if (qtyRaw != null) {
                        try { qty = Double.parseDouble(qtyRaw.replaceAll("[^0-9.]", "")); } catch (Exception ignored) {}
                    }

                    String bId = snapshot.child("buyerId").getValue(String.class);
                    String sId = snapshot.child("assignedFarmerId").getValue(String.class);

                    final double fPrice = price;
                    final double fQty = qty;
                    final String fProd = prod;

                    ref.child("Users").child(bId != null ? bId : "none").child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot bSnap) {
                            String bName = bSnap.exists() ? bSnap.getValue(String.class) : "Unknown Buyer";
                            ref.child("Users").child(sId != null ? sId : "none").child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot sSnap) {
                                    String sName = sSnap.exists() ? sSnap.getValue(String.class) : "Assigned Farmer";
                                    saveInvoiceToFirebase(bName, sName, fProd, fQty, fPrice);
                                }
                                @Override public void onCancelled(@NonNull DatabaseError e) {}
                            });
                        }
                        @Override public void onCancelled(@NonNull DatabaseError e) {}
                    });
                } else {
                    Toast.makeText(InvoiceActivity.this, "Order details not found", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }

    private void saveInvoiceToFirebase(String buyer, String seller, String prod, double qty, double price) {
        String invoiceId = "INV_" + orderId;
        HashMap<String, Object> map = new HashMap<>();
        map.put("invoiceId", invoiceId);
        map.put("orderId", orderId);
        map.put("buyerName", buyer);
        map.put("sellerName", seller);
        map.put("productName", prod != null ? prod : "N/A");
        map.put("quantity", String.valueOf(qty));
        map.put("pricePerUnit", String.valueOf(price));
        map.put("finalPrice", String.valueOf(price));
        map.put("totalAmount", String.valueOf(qty * price));
        map.put("status", "ACCEPTED");
        map.put("payment", "PENDING");
        map.put("timestamp", System.currentTimeMillis());

        FirebaseDatabase.getInstance().getReference("Invoices").child(invoiceId).setValue(map).addOnSuccessListener(aVoid -> loadInvoice());
    }

    private void checkPermissionAndDownload() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
                return;
            }
        }
        generatePdf();
    }

    private void generatePdf() {
        try {
            String currentInvoiceId = tvInvoiceId.getText().toString();
            String fileName = "VegiTrp_Invoice_" + currentInvoiceId + ".pdf";
            OutputStream outputStream;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                values.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
                values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
                pdfUri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                outputStream = getContentResolver().openOutputStream(pdfUri);
                pdfFile = null;
            } else {
                File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                if (!downloadsDir.exists()) downloadsDir.mkdirs();
                pdfFile = new File(downloadsDir, fileName);
                outputStream = new FileOutputStream(pdfFile);
                pdfUri = null;
            }

            com.itextpdf.text.Document document = new com.itextpdf.text.Document();
            com.itextpdf.text.pdf.PdfWriter.getInstance(document, outputStream);
            document.open();

            com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 24, com.itextpdf.text.Font.BOLD);
            com.itextpdf.text.Paragraph title = new com.itextpdf.text.Paragraph("INVOICE\n\n", titleFont);
            title.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            document.add(title);

            document.add(new com.itextpdf.text.Paragraph("Invoice ID: " + currentInvoiceId));
            document.add(new com.itextpdf.text.Paragraph("Date: " + tvDate.getText().toString() + " " + tvTime.getText().toString()));
            document.add(new com.itextpdf.text.Paragraph("Order ID: " + tvOrderId.getText().toString() + "\n\n"));
            document.add(new com.itextpdf.text.Paragraph("Billed To: " + tvBuyerName.getText().toString()));
            document.add(new com.itextpdf.text.Paragraph("Sold By: " + tvSellerName.getText().toString() + "\n\n"));

            com.itextpdf.text.pdf.PdfPTable table = new com.itextpdf.text.pdf.PdfPTable(4);
            table.setWidthPercentage(100);
            table.addCell("Product"); table.addCell("Quantity"); table.addCell("Unit Price"); table.addCell("Total");
            table.addCell(tvProduct.getText().toString());
            table.addCell(tvQuantity.getText().toString());
            table.addCell(tvUnitPrice.getText().toString());
            table.addCell(tvTotalAmount.getText().toString());

            document.add(table);
            document.add(new com.itextpdf.text.Paragraph("\nTotal Amount: " + tvTotalAmount.getText().toString()));
            document.close();
            outputStream.close();

            Toast.makeText(this, "Saved to Downloads: " + fileName, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e(TAG, "PDF Error", e);
            Toast.makeText(this, "Failed to save PDF", Toast.LENGTH_SHORT).show();
        }
    }

    private void shareInvoice() {
        if (pdfUri == null && (pdfFile == null || !pdfFile.exists())) {
            Toast.makeText(this, "Please download the PDF first", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Uri uriToShare = pdfUri != null ? pdfUri : FileProvider.getUriForFile(this, getPackageName() + ".provider", pdfFile);
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("application/pdf");
            intent.putExtra(Intent.EXTRA_STREAM, uriToShare);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Share Invoice"));
        } catch (Exception e) {
            Toast.makeText(this, "Error sharing file", Toast.LENGTH_SHORT).show();
        }
    }
}
