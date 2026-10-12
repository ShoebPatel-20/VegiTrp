package com.example.vegitrp.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import com.example.vegitrp.R;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;

public class RegisterActivity extends BaseActivity {

    private static final String TAG = "RegisterActivity";
    private EditText etName, etEmail, etMobile, etPassword, etConfirmPassword;
    private Spinner spinnerUserRole;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);
        etMobile = findViewById(R.id.et_mobile);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        spinnerUserRole = findViewById(R.id.spinner_user_role);
        Button btnRegister = findViewById(R.id.btn_register);
        TextView tvLogin = findViewById(R.id.tv_login);

        View layoutBrandHeader = findViewById(R.id.layout_brand_header);
        View cardForm = findViewById(R.id.card_register_form);

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

        if (tvLogin != null) {
            tvLogin.setAlpha(0f);
            tvLogin.animate()
                    .alpha(1f)
                    .setStartDelay(300)
                    .setDuration(400)
                    .start();
        }

        // Setup Adapter for Spinner
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, getResources().getTextArray(R.array.user_roles)) {
            @Override
            public boolean isEnabled(int position) {
                // Disable the first item from Spinner
                // First item will be use for hint
                return position != 0;
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if (position == 0) {
                    // Set the hint text color gray
                    tv.setTextColor(ContextCompat.getColor(getContext(), R.color.gray_400));
                } else {
                    tv.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
                }
                return view;
            }
        };

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUserRole.setAdapter(adapter);

        btnRegister.setOnClickListener(v -> registerUser());

        tvLogin.setOnClickListener(v -> startActivity(new Intent(RegisterActivity.this, LoginActivity.class)));
    }

    private void registerUser() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String mobile = etMobile.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (spinnerUserRole.getSelectedItemPosition() == 0) {
            Toast.makeText(this, R.string.select_role_prompt, Toast.LENGTH_SHORT).show();
            return;
        }
        String role = spinnerUserRole.getSelectedItem().toString();

        if (name.isEmpty() || email.isEmpty() || mobile.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || role.isEmpty()) {
            Toast.makeText(this, R.string.fill_all_fields_toast, Toast.LENGTH_SHORT).show();
            return;
        }

        if (mobile.length() != 10) {
            Toast.makeText(this, R.string.invalid_mobile_toast, Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password should be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, R.string.passwords_do_not_match, Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, (Task<AuthResult> task) -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            String userId = user.getUid();
                            HashMap<String, String> userData = new HashMap<>();
                            userData.put("name", name);
                            userData.put("email", email);
                            userData.put("mobile", mobile);
                            userData.put("role", role);

                            mDatabase.child("Users").child(userId).setValue(userData)
                                    .addOnCompleteListener(dbTask -> {
                                        if (dbTask.isSuccessful()) {
                                             try {
                                                 saveUserData(name, email, role, mobile);
                                                 Toast.makeText(RegisterActivity.this, R.string.registration_successful_toast, Toast.LENGTH_SHORT).show();
                                                 Intent intent = new Intent(RegisterActivity.this, DashboardActivity.class);
                                                 intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                 startActivity(intent);
                                                 finish();
                                             } catch (GeneralSecurityException | IOException e) {
                                                 Log.e(TAG, getString(R.string.failed_to_save_user_data_toast), e);
                                                 Toast.makeText(RegisterActivity.this, R.string.failed_to_save_user_data_toast, Toast.LENGTH_SHORT).show();
                                             }
                                        } else {
                                             Log.w(TAG, getString(R.string.database_error_toast), dbTask.getException());
                                             Toast.makeText(RegisterActivity.this, R.string.database_error_toast, Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    } else {
                        Log.w(TAG, getString(R.string.auth_failed_toast), task.getException());
                        Toast.makeText(RegisterActivity.this, R.string.auth_failed_toast, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserData(String name, String email, String role, String mobile) throws GeneralSecurityException, IOException {
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
        editor.putString("user_mobile", mobile);
        editor.commit();
    }
}
