package com.example.vegitrp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.vegitrp.R;
import com.google.firebase.auth.FirebaseAuth;

public class SettingsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Button btnLogout = findViewById(R.id.btn_logout);
        Button btnWallet = findViewById(R.id.btn_wallet);
        Button btnKyc = findViewById(R.id.btn_kyc);
        Button btnNotifications = findViewById(R.id.btn_notifications);
        Button btnEditProfile = findViewById(R.id.btn_edit_profile);

        btnEditProfile.setOnClickListener(v -> {
            startActivity(new Intent(SettingsActivity.this, EditProfileActivity.class));
        });

        btnNotifications.setOnClickListener(v -> {
            startActivity(new Intent(SettingsActivity.this, NotificationsActivity.class));
        });

        btnWallet.setOnClickListener(v -> {
            startActivity(new Intent(SettingsActivity.this, WalletActivity.class));
        });

        btnKyc.setOnClickListener(v -> {
            startActivity(new Intent(SettingsActivity.this, KYCActivity.class));
        });

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }
}
