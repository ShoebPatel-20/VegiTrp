package com.example.vegitrp.activities;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.vegitrp.R;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends BaseActivity {

    private EditText etEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        etEmail = findViewById(R.id.et_email);
        Button btnSendResetLink = findViewById(R.id.btn_send_reset_link);
        TextView tvBackToLogin = findViewById(R.id.tv_back_to_login);

        View layoutBrandHeader = findViewById(R.id.layout_brand_header);
        View cardForm = findViewById(R.id.card_forgot_password_form);

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

        if (tvBackToLogin != null) {
            tvBackToLogin.setAlpha(0f);
            tvBackToLogin.animate()
                    .alpha(1f)
                    .setStartDelay(300)
                    .setDuration(400)
                    .start();

            tvBackToLogin.setOnClickListener(v -> finish());
        }

        btnSendResetLink.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();

            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(ForgotPasswordActivity.this, "Password reset link sent to your email", Toast.LENGTH_LONG).show();
                            finish();
                        } else {
                            String errorMessage = "Failed to send reset link";
                            if (task.getException() != null) {
                                errorMessage += ": " + task.getException().getMessage();
                            }
                            Toast.makeText(ForgotPasswordActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}
