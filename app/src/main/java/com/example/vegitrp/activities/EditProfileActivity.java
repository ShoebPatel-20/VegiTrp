package com.example.vegitrp.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.vegitrp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileActivity extends BaseActivity {

    private CircleImageView ivEditAvatar;
    private ProgressBar pbAvatarUpload;
    
    private TextView tvNameValue;
    private TextView tvMobileValue;
    private TextView tvEmailValue;

    private String currentUserId;
    private DatabaseReference userRef;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUserId);
        storageReference = FirebaseStorage.getInstance().getReference("profile_images");

        setupToolbar();
        bindViews();
        setupClickListeners();
        loadUserData();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowTitleEnabled(false);
            }
            toolbar.setNavigationOnClickListener(v -> finish());
        }
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
        ivEditAvatar = findViewById(R.id.ivEditAvatar);
        pbAvatarUpload = findViewById(R.id.pbAvatarUpload);
        
        tvNameValue = findViewById(R.id.tvNameValue);
        tvMobileValue = findViewById(R.id.tvMobileValue);
        tvEmailValue = findViewById(R.id.tvEmailValue);
    }

    private void setupClickListeners() {
        ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri selectedUri = result.getData().getData();
                        if (selectedUri != null) {
                            if (ivEditAvatar != null) {
                                ivEditAvatar.setImageURI(selectedUri);
                            }
                            uploadSingleImage(selectedUri);
                        }
                    }
                });

        View avatarSection = findViewById(R.id.rlAvatarSection);
        if (avatarSection != null) {
            avatarSection.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                imagePickerLauncher.launch(intent);
            });
        }

        View rowName = findViewById(R.id.rowName);
        if (rowName != null) {
            rowName.setOnClickListener(v -> {
                String currentName = tvNameValue != null ? tvNameValue.getText().toString() : "";
                showEditDialog(getString(R.string.name_hint), "name", currentName, InputType.TYPE_CLASS_TEXT);
            });
        }

        View rowMobile = findViewById(R.id.rowMobile);
        if (rowMobile != null) {
            rowMobile.setOnClickListener(v -> {
                String currentMobile = tvMobileValue != null ? tvMobileValue.getText().toString() : "";
                showEditDialog(getString(R.string.mobile_hint), "mobile", currentMobile, InputType.TYPE_CLASS_PHONE);
            });
        }

        View rowEmail = findViewById(R.id.rowEmail);
        if (rowEmail != null) {
            rowEmail.setOnClickListener(v -> {
                String currentEmail = tvEmailValue != null ? tvEmailValue.getText().toString() : "";
                showEditDialog(getString(R.string.email_hint), "email", currentEmail, InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
            });
        }
    }

    private void loadUserData() {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String mobile = snapshot.child("mobile").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    
                    String avatarUrl = snapshot.child("profileImage").getValue(String.class);
                    if (avatarUrl == null || avatarUrl.isEmpty()) {
                        avatarUrl = snapshot.child("avatarUrl").getValue(String.class);
                    }

                    if (tvNameValue != null) {
                        tvNameValue.setText(name != null ? name : "");
                    }
                    if (tvMobileValue != null) {
                        tvMobileValue.setText(mobile != null ? mobile : "");
                    }
                    if (tvEmailValue != null) {
                        tvEmailValue.setText(email != null ? email : "");
                    }

                    if (avatarUrl != null && !avatarUrl.isEmpty() && ivEditAvatar != null && !isDestroyed()) {
                        Glide.with(EditProfileActivity.this).load(avatarUrl).into(ivEditAvatar);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EditProfileActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEditDialog(String title, String dbKey, String currentValue, int inputType) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.edit_profile_header_title) + " - " + title);

        final EditText input = new EditText(this);
        input.setInputType(inputType);
        if (currentValue != null && !currentValue.equals("Loading...")) {
            input.setText(currentValue);
            input.setSelection(input.getText().length());
        }

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        int paddingPx = (int) (20 * getResources().getDisplayMetrics().density);
        container.setPadding(paddingPx, (int) (10 * getResources().getDisplayMetrics().density), paddingPx, 0);
        
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        input.setLayoutParams(lp);
        container.addView(input);
        builder.setView(container);

        builder.setPositiveButton(R.string.edit_profile_dialog_save, (dialog, which) -> {
            String newValue = input.getText().toString().trim();
            if (!newValue.isEmpty()) {
                userRef.child(dbKey).setValue(newValue)
                        .addOnSuccessListener(aVoid -> Toast.makeText(EditProfileActivity.this, title + " updated", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(EditProfileActivity.this, "Failed to update " + title, Toast.LENGTH_SHORT).show());
            }
        });
        builder.setNegativeButton(R.string.edit_profile_dialog_cancel, (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void uploadSingleImage(Uri fileUri) {
        if (pbAvatarUpload != null) pbAvatarUpload.setVisibility(View.VISIBLE);
        if (ivEditAvatar != null) ivEditAvatar.setAlpha(0.5f);
        
        String fileName = currentUserId + "_avatar.jpg";
        StorageReference fileRef = storageReference.child(fileName);
        
        fileRef.putFile(fileUri).addOnSuccessListener(taskSnapshot -> {
            fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                String downloadUrl = uri.toString();
                userRef.child("profileImage").setValue(downloadUrl)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(EditProfileActivity.this, "Profile Image Updated", Toast.LENGTH_SHORT).show();
                            if (pbAvatarUpload != null) pbAvatarUpload.setVisibility(View.GONE);
                            if (ivEditAvatar != null) ivEditAvatar.setAlpha(1.0f);
                        });
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to upload: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            if (pbAvatarUpload != null) pbAvatarUpload.setVisibility(View.GONE);
            if (ivEditAvatar != null) ivEditAvatar.setAlpha(1.0f);
        });
    }
}
