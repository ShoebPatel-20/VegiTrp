package com.example.vegitrp.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import com.example.vegitrp.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class DashboardActivity extends BaseActivity {

    private static final String TAG = "DashboardActivity";

    private BottomNavigationView bottomNavView;
    private NavController navController;
    private AppBarConfiguration appBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_dashboard);

            Toolbar toolbar = findViewById(R.id.toolbar);
            if (toolbar != null) {
                setSupportActionBar(toolbar);
            }

            bottomNavView = findViewById(R.id.nav_view);
            if (bottomNavView == null) {
                Log.e("UI_ERROR", "View not found: nav_view in DashboardActivity");
                return;
            }

            try {
                SharedPreferences sharedPreferences = getEncryptedSharedPreferences();
                String role = sharedPreferences.getString("user_role", "");

                NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);

                if (navHostFragment == null) {
                    Log.e(TAG, "NavHostFragment not found. Cannot setup navigation.");
                    Toast.makeText(this, "Error setting up navigation.", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                navController = navHostFragment.getNavController();

                int navGraphId;
                switch (role) {
                    case "Farmer":
                        navGraphId = R.navigation.farmer_navigation;
                        break;
                    case "Driver":
                        navGraphId = R.navigation.driver_navigation;
                        break;
                    case "Buyer":
                        navGraphId = R.navigation.buyer_navigation;
                        break;
                    default:
                        navGraphId = R.navigation.mobile_navigation;
                        break;
                }

                int menuId;
                switch (role) {
                    case "Farmer":
                        menuId = R.menu.farmer_bottom_nav_menu;
                        break;
                    case "Driver":
                        menuId = R.menu.driver_bottom_nav_menu;
                        break;
                    case "Buyer":
                        menuId = R.menu.buyer_bottom_nav_menu;
                        break;
                    default:
                        menuId = 0;
                        break;
                }

                navController.setGraph(navGraphId);

                if (menuId != 0) {
                    bottomNavView.getMenu().clear();
                    bottomNavView.inflateMenu(menuId);
                    NavigationUI.setupWithNavController(bottomNavView, navController);
                }

                appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
                NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

            } catch (GeneralSecurityException | IOException e) {
                Log.e(TAG, "Failed to load user profile.", e);
                Toast.makeText(this, "Failed to load profile.", Toast.LENGTH_SHORT).show();
                finish();
            }
            Log.d("SCREEN_DEBUG", "Activity started: DashboardActivity");
        } catch (Exception e) {
            Log.e("CRASH_ERROR", "Activity crash in DashboardActivity: " + e.getMessage());
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp();
    }

    private SharedPreferences getEncryptedSharedPreferences() throws GeneralSecurityException, IOException {
        String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
        return EncryptedSharedPreferences.create(
                "user_prefs",
                masterKeyAlias,
                this,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        );
    }
}
