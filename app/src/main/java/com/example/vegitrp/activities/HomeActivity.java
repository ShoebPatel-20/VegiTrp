package com.example.vegitrp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;

import com.example.vegitrp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomeActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            // User is already logged in, go to DashboardActivity
            Intent intent = new Intent(HomeActivity.this, DashboardActivity.class);
            startActivity(intent);
            finish();
        } else {
            // User is not logged in, show the splash / home screen
            setContentView(R.layout.activity_home);

            View cardHero = findViewById(R.id.card_hero);
            View tvAppName = findViewById(R.id.tv_app_name);
            View tvWelcome = findViewById(R.id.tv_welcome);
            View tvSubtitle = findViewById(R.id.tv_subtitle);
            View layoutChips = findViewById(R.id.layout_feature_chips);
            Button btnGetStarted = findViewById(R.id.btn_get_started);

            // Subtle smooth entry animation for light 3D depth feel
            if (cardHero != null) {
                cardHero.setAlpha(0f);
                cardHero.setScaleX(0.88f);
                cardHero.setScaleY(0.88f);
                cardHero.animate()
                        .alpha(1f)
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(600)
                        .setInterpolator(new DecelerateInterpolator())
                        .start();
            }

            if (tvAppName != null) {
                tvAppName.setAlpha(0f);
                tvAppName.setTranslationY(20f);
                tvAppName.animate()
                        .alpha(1f)
                        .translationY(0f)
                        .setStartDelay(150)
                        .setDuration(500)
                        .setInterpolator(new DecelerateInterpolator())
                        .start();
            }

            if (tvWelcome != null) {
                tvWelcome.setAlpha(0f);
                tvWelcome.setTranslationY(20f);
                tvWelcome.animate()
                        .alpha(1f)
                        .translationY(0f)
                        .setStartDelay(250)
                        .setDuration(500)
                        .setInterpolator(new DecelerateInterpolator())
                        .start();
            }

            if (tvSubtitle != null) {
                tvSubtitle.setAlpha(0f);
                tvSubtitle.setTranslationY(20f);
                tvSubtitle.animate()
                        .alpha(1f)
                        .translationY(0f)
                        .setStartDelay(350)
                        .setDuration(500)
                        .setInterpolator(new DecelerateInterpolator())
                        .start();
            }

            if (layoutChips != null) {
                layoutChips.setAlpha(0f);
                layoutChips.setTranslationY(20f);
                layoutChips.animate()
                        .alpha(1f)
                        .translationY(0f)
                        .setStartDelay(450)
                        .setDuration(500)
                        .setInterpolator(new DecelerateInterpolator())
                        .start();
            }

            if (btnGetStarted != null) {
                btnGetStarted.setAlpha(0f);
                btnGetStarted.setTranslationY(30f);
                btnGetStarted.animate()
                        .alpha(1f)
                        .translationY(0f)
                        .setStartDelay(550)
                        .setDuration(500)
                        .setInterpolator(new DecelerateInterpolator())
                        .start();

                btnGetStarted.setOnClickListener(v -> {
                    Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                });
            }
        }
    }
}
