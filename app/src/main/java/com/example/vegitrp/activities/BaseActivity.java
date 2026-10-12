package com.example.vegitrp.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import com.example.vegitrp.R;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class BaseActivity extends AppCompatActivity {

    private static final String TAG = "BaseActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        applyRoleBasedTheme();
        super.onCreate(savedInstanceState);
        
        // Use a standard background to avoid compilation errors
        getWindow().setBackgroundDrawableResource(android.R.color.white);
    }

    private void applyThemeMode() {
        SharedPreferences settingsPrefs = getSharedPreferences("SettingsPrefs", Context.MODE_PRIVATE);
        int themeMode = settingsPrefs.getInt("THEME_MODE", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(themeMode);
    }

    private void applyRoleBasedTheme() {
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
                setTheme(R.style.Theme_VegiTrp_Driver);
            } else if ("Farmer".equals(role)) {
                setTheme(R.style.Theme_VegiTrp_Farmer);
            } else if ("Buyer".equals(role)) {
                setTheme(R.style.Theme_VegiTrp_Buyer);
            } else {
                setTheme(R.style.Theme_VegiTrp);
            }

        } catch (GeneralSecurityException | IOException e) {
            Log.e(TAG, "Failed to load theme preferences.", e);
            setTheme(R.style.Theme_VegiTrp); // fallback
        }
        applyThemeMode();
    }
}
