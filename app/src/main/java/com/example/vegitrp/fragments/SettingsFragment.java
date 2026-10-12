package com.example.vegitrp.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;


import com.example.vegitrp.R;
import com.example.vegitrp.activities.BuyerProfileActivity;
import com.example.vegitrp.activities.DriverProfileActivity;
import com.example.vegitrp.activities.EditProfileActivity;
import com.example.vegitrp.activities.FarmerProfileActivity;
import com.example.vegitrp.activities.LoginActivity;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class SettingsFragment extends Fragment {

    private static final String TAG = "SettingsFragment";
    private SharedPreferences sharedPreferences;

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        
        sharedPreferences = requireContext().getSharedPreferences("SettingsPrefs", Context.MODE_PRIVATE);

        bindAccountSection(view);
        bindPreferencesSection(view);
        bindSecuritySection(view);
        bindSupportSection(view);
        bindAboutSection(view);
        bindLogoutAction(view);

        return view;
    }

    private void bindAccountSection(View view) {
        View btnMyProfile = view.findViewById(R.id.btnMyProfile);
        if (btnMyProfile != null) btnMyProfile.setOnClickListener(v -> routeToProfile());

        View btnEditProfile = view.findViewById(R.id.btnEditProfile);
        if (btnEditProfile != null) btnEditProfile.setOnClickListener(v -> {
            startActivity(new Intent(requireActivity(), EditProfileActivity.class));
        });

        View btnChangePasswordAcc = view.findViewById(R.id.btnChangePasswordAcc);
        if (btnChangePasswordAcc != null) btnChangePasswordAcc.setOnClickListener(v -> showToast("Change Password logic connecting..."));
    }

    private void bindPreferencesSection(View view) {
        View btnTheme = view.findViewById(R.id.btnTheme);
        TextView tvThemeSelection = view.findViewById(R.id.tvThemeSelection);
        setupThemeLogic(btnTheme, tvThemeSelection);

        View btnAppearance = view.findViewById(R.id.btnAppearance);
        if (btnAppearance != null) btnAppearance.setOnClickListener(v -> showToast("Appearance customizer opening..."));

        SwitchCompat switchNotifications = view.findViewById(R.id.switchNotifications);
        if (switchNotifications != null) {
            boolean isNotificationsEnabled = sharedPreferences.getBoolean("NOTIFICATIONS_ENABLED", true);
            switchNotifications.setChecked(isNotificationsEnabled);
            switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
                sharedPreferences.edit().putBoolean("NOTIFICATIONS_ENABLED", isChecked).apply();
                showToast(isChecked ? "Notifications Enabled" : "Notifications Disabled");
            });
        }

        View btnLanguage = view.findViewById(R.id.btnLanguage);
        if (btnLanguage != null) btnLanguage.setOnClickListener(v -> showToast("Language Selection screen opening..."));
    }

    private void bindSecuritySection(View view) {
        View btnPrivacySecurity = view.findViewById(R.id.btnPrivacySecurity);
        if (btnPrivacySecurity != null) btnPrivacySecurity.setOnClickListener(v -> showToast("Privacy Hub opening..."));

        View btnAppLock = view.findViewById(R.id.btnAppLock);
        if (btnAppLock != null) btnAppLock.setOnClickListener(v -> showToast("App Lock setup launching..."));
    }

    private void bindSupportSection(View view) {
        View btnHelpSupport = view.findViewById(R.id.btnHelpSupport);
        if (btnHelpSupport != null) btnHelpSupport.setOnClickListener(v -> showToast("Help & Support center opening..."));

        View btnContactUs = view.findViewById(R.id.btnContactUs);
        if (btnContactUs != null) {
            btnContactUs.setOnClickListener(v -> {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(Uri.parse("mailto:support@vegitrp.com"));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Customer Support Inquiry");
                startActivity(Intent.createChooser(emailIntent, "Send Email Using: "));
            });
        }
    }

    private void bindAboutSection(View view) {
        // Empty as we removed About App, Terms, Privacy Policy, App Version from layout
    }

    private void bindLogoutAction(View view) {
        View btnLogout = view.findViewById(R.id.btnLogout);
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> performLogout());
        }
    }

    private void setupThemeLogic(View btnTheme, TextView tvThemeSelection) {
        if (btnTheme == null || tvThemeSelection == null) return;
        
        int currentMode = sharedPreferences.getInt("THEME_MODE", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        updateThemeText(tvThemeSelection, currentMode);

        btnTheme.setOnClickListener(v -> {
            String[] themeOptions = {"Light", "Dark", "System Default"};
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Select Theme");
            builder.setItems(themeOptions, (dialog, which) -> {
                int modeToApply;
                if (which == 0) {
                    modeToApply = AppCompatDelegate.MODE_NIGHT_NO;
                } else if (which == 1) {
                    modeToApply = AppCompatDelegate.MODE_NIGHT_YES;
                } else {
                    modeToApply = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                }
                
                sharedPreferences.edit().putInt("THEME_MODE", modeToApply).apply();
                updateThemeText(tvThemeSelection, modeToApply);
                AppCompatDelegate.setDefaultNightMode(modeToApply);
            });
            builder.show();
        });
    }

    private void updateThemeText(TextView tvThemeSelection, int mode) {
        if (mode == AppCompatDelegate.MODE_NIGHT_NO) {
            tvThemeSelection.setText("Light");
        } else if (mode == AppCompatDelegate.MODE_NIGHT_YES) {
            tvThemeSelection.setText("Dark");
        } else {
            tvThemeSelection.setText("System");
        }
    }

    private void performLogout() {
        try {
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            EncryptedSharedPreferences.create(
                    "user_prefs",
                    masterKeyAlias,
                    requireContext(),
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            ).edit().clear().commit();

            com.google.firebase.auth.FirebaseAuth.getInstance().signOut();

            Intent intent = new Intent(requireActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
            
        } catch (GeneralSecurityException | IOException e) {
            Log.e(TAG, "Failed to clear user data on logout.", e);
            showToast("Failed to logout securely.");
        }
    }

    private void routeToProfile() {
        try {
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            SharedPreferences encryptedPrefs = EncryptedSharedPreferences.create(
                    "user_prefs",
                    masterKeyAlias,
                    requireContext(),
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );

            String userRole = encryptedPrefs.getString("user_role", "");
            Intent intent;
            if ("Farmer".equals(userRole)) {
                intent = new Intent(requireActivity(), FarmerProfileActivity.class);
            } else if ("Driver".equals(userRole)) {
                intent = new Intent(requireActivity(), DriverProfileActivity.class);
            } else {
                intent = new Intent(requireActivity(), BuyerProfileActivity.class);
            }
            startActivity(intent);
        } catch (GeneralSecurityException | IOException e) {
            Log.e(TAG, "Failed to determine role for profile routing.", e);
            showToast("Error accessing Profile");
        }
    }

    private void showToast(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
}
