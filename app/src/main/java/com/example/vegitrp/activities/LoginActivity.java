package com.example.vegitrp.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import com.example.vegitrp.R;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class LoginActivity extends BaseActivity {

    private static final String TAG = "LoginActivity";
    private EditText etEmail, etPassword;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        Button btnLogin = findViewById(R.id.btn_login);
        TextView tvRegister = findViewById(R.id.tv_register);
        TextView tvForgotPassword = findViewById(R.id.tv_forgot_password);

        View layoutBrandHeader = findViewById(R.id.layout_brand_header);
        View cardForm = findViewById(R.id.card_login_form);

        // Subtle entry transitions
        if (layoutBrandHeader != null) {
            layoutBrandHeader.setAlpha(0f);
            layoutBrandHeader.setTranslationY(-15f);
            layoutBrandHeader.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(500)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        }

        if (cardForm != null) {
            cardForm.setAlpha(0f);
            cardForm.setTranslationY(25f);
            cardForm.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setStartDelay(150)
                    .setDuration(500)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        }

        if (tvRegister != null) {
            tvRegister.setAlpha(0f);
            tvRegister.animate()
                    .alpha(1f)
                    .setStartDelay(300)
                    .setDuration(400)
                    .start();
        }

        btnLogin.setOnClickListener(v -> loginUser());

        tvRegister.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));

        tvForgotPassword.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class)));
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, R.string.fill_all_fields_toast, Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, (Task<AuthResult> task) -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            String userId = user.getUid();
                            mDatabase.child("Users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        String name = snapshot.child("name").getValue(String.class);
                                        String role = snapshot.child("role").getValue(String.class);

                                        if (role == null) {
                                            Toast.makeText(LoginActivity.this, R.string.user_role_not_found_toast, Toast.LENGTH_SHORT).show();
                                            return;
                                        }

                                        try {
                                            saveUserData(name, email, role);

                                            Intent intent;
                                            switch (role) {
                                                case "Farmer":
                                                case "Driver":
                                                case "Buyer":
                                                    intent = new Intent(LoginActivity.this, DashboardActivity.class);
                                                    break;
                                                default:
                                                    intent = new Intent(LoginActivity.this, LoginActivity.class); // Or some default activity
                                                    break;
                                            }
                                            startActivity(intent);
                                            finish();

                                        } catch (GeneralSecurityException | IOException e) {
                                            Log.e(TAG, getString(R.string.failed_to_save_user_data_toast), e);
                                            Toast.makeText(LoginActivity.this, R.string.failed_to_save_user_data_toast, Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Toast.makeText(LoginActivity.this, R.string.user_data_not_found_toast, Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.e(TAG, getString(R.string.database_error_toast), error.toException());
                                    Toast.makeText(LoginActivity.this, R.string.database_error_toast, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } else {
                        Log.w(TAG, getString(R.string.auth_failed_toast), task.getException());
                        Toast.makeText(LoginActivity.this, R.string.auth_failed_toast, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserData(String name, String email, String role) throws GeneralSecurityException, IOException {
        String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);

        SharedPreferences sharedPreferences = EncryptedSharedPreferences.create(
                "user_prefs",
                masterKeyAlias,
                this,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        );

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("user_name", name);
        editor.putString("user_email", email);
        editor.putString("user_role", role);
        editor.commit();
    }
}
