package com.example.vegitrp.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.vegitrp.R;
import com.example.vegitrp.models.UserDocument;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class KYCActivity extends BaseActivity {

    private ImageView ivDocumentPreview;
    private Button btnSelectImage, btnUploadDocument;
    private View cardKycStatus;
    private android.widget.TextView tvKycStatusBadge, tvKycStatusDesc;
    private Uri imageUri;
    private StorageReference storageReference;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kyc);

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Identity Verification");
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }

        ivDocumentPreview = findViewById(R.id.ivDocumentPreview);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        btnUploadDocument = findViewById(R.id.btnUploadDocument);
        cardKycStatus = findViewById(R.id.cardKycStatus);
        tvKycStatusBadge = findViewById(R.id.tvKycStatusBadge);
        tvKycStatusDesc = findViewById(R.id.tvKycStatusDesc);

        storageReference = FirebaseStorage.getInstance().getReference("kyc_documents");
        databaseReference = FirebaseDatabase.getInstance().getReference("kyc");

        // Check if an existing KYC document exists for the user
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            databaseReference.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        UserDocument existingDoc = snapshot.getValue(UserDocument.class);
                        if (existingDoc != null) {
                            if (cardKycStatus != null) {
                                cardKycStatus.setVisibility(View.VISIBLE);
                            }
                            String status = existingDoc.getStatus();
                            if (status == null) status = "Pending";

                            if ("Approved".equalsIgnoreCase(status)) {
                                if (tvKycStatusBadge != null) {
                                    tvKycStatusBadge.setText("VERIFIED");
                                    tvKycStatusBadge.setTextColor(androidx.core.content.ContextCompat.getColor(KYCActivity.this, R.color.brand_green_primary));
                                    tvKycStatusBadge.setBackgroundTintList(android.content.res.ColorStateList.valueOf(androidx.core.content.ContextCompat.getColor(KYCActivity.this, R.color.brand_green_light)));
                                }
                                if (tvKycStatusDesc != null) {
                                    tvKycStatusDesc.setText("Your identity document has been verified successfully.");
                                }
                                if (btnUploadDocument != null) {
                                    btnUploadDocument.setText("Update Document");
                                }
                            } else if ("Rejected".equalsIgnoreCase(status)) {
                                if (tvKycStatusBadge != null) {
                                    tvKycStatusBadge.setText("ACTION REQUIRED");
                                    tvKycStatusBadge.setTextColor(androidx.core.content.ContextCompat.getColor(KYCActivity.this, R.color.brand_accent_orange));
                                    tvKycStatusBadge.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#FFF3E0")));
                                }
                                if (tvKycStatusDesc != null) {
                                    tvKycStatusDesc.setText("Your document verification was rejected. Please upload a clear valid government ID.");
                                }
                                if (btnUploadDocument != null) {
                                    btnUploadDocument.setText("Re-upload Document");
                                }
                            } else {
                                if (tvKycStatusBadge != null) {
                                    tvKycStatusBadge.setText("UNDER REVIEW");
                                    tvKycStatusBadge.setTextColor(androidx.core.content.ContextCompat.getColor(KYCActivity.this, R.color.brand_green_primary));
                                    tvKycStatusBadge.setBackgroundTintList(android.content.res.ColorStateList.valueOf(androidx.core.content.ContextCompat.getColor(KYCActivity.this, R.color.brand_green_light)));
                                }
                                if (tvKycStatusDesc != null) {
                                    tvKycStatusDesc.setText("Your document has been submitted and is currently pending review.");
                                }
                                if (btnUploadDocument != null) {
                                    btnUploadDocument.setText("Update Document");
                                }
                            }

                            if (existingDoc.getDocumentUrl() != null && !existingDoc.getDocumentUrl().isEmpty()) {
                                if (ivDocumentPreview != null && !isFinishing() && !isDestroyed()) {
                                    ivDocumentPreview.setImageTintList(null);
                                    ivDocumentPreview.setScaleType(ImageView.ScaleType.CENTER_CROP);
                                    Glide.with(KYCActivity.this)
                                            .load(existingDoc.getDocumentUrl())
                                            .into(ivDocumentPreview);
                                }
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }

        ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        imageUri = result.getData().getData();
                        ivDocumentPreview.setImageTintList(null);
                        ivDocumentPreview.setImageURI(imageUri);
                        ivDocumentPreview.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    }
                });

        btnSelectImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            activityResultLauncher.launch(intent);
        });

        btnUploadDocument.setOnClickListener(v -> uploadDocumentToFirebase());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void uploadDocumentToFirebase() {
        if (imageUri != null) {
            btnUploadDocument.setEnabled(false);
            btnUploadDocument.setText("Uploading...");

            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                Toast.makeText(this, "You must be logged in to verify ID.", Toast.LENGTH_SHORT).show();
                btnUploadDocument.setEnabled(true);
                return;
            }

            String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            String docId = databaseReference.push().getKey();
            StorageReference fileReference = storageReference.child(currentUserId + "_" + System.currentTimeMillis() + ".jpg");

            fileReference.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        
                        if (docId != null) {
                            UserDocument document = new UserDocument(docId, currentUserId, "ID Card", imageUrl, "Pending", System.currentTimeMillis());
                            databaseReference.child(currentUserId).setValue(document)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(KYCActivity.this, "Document Uploaded Successfully!", Toast.LENGTH_LONG).show();
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(KYCActivity.this, "Failed to save data.", Toast.LENGTH_SHORT).show();
                                        btnUploadDocument.setEnabled(true);
                                        btnUploadDocument.setText("Submit for Verification");
                                    });
                        }
                    }))
                    .addOnFailureListener(e -> {
                        Toast.makeText(KYCActivity.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        btnUploadDocument.setEnabled(true);
                        btnUploadDocument.setText("Submit for Verification");
                    });
        } else {
            Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT).show();
        }
    }
}
