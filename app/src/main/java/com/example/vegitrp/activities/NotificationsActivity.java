package com.example.vegitrp.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vegitrp.R;
import com.example.vegitrp.adapters.NotificationAdapter;
import com.example.vegitrp.models.NotificationModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NotificationsActivity extends BaseActivity {

    private RecyclerView rvNotifications;
    private NotificationAdapter notificationAdapter;
    private List<NotificationModel> notificationList;

    private DatabaseReference notificationsRef;

    private View layoutEmptyState;
    private android.widget.ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        Toolbar toolbar = findViewById(R.id.notificationsToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.notifications_toolbar_title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        rvNotifications = findViewById(R.id.rvNotifications);
        rvNotifications.setLayoutManager(new LinearLayoutManager(this));

        layoutEmptyState = findViewById(R.id.layout_empty_state);
        progressBar = findViewById(R.id.progress_bar);

        notificationList = new ArrayList<>();
        notificationAdapter = new NotificationAdapter(this, notificationList);
        rvNotifications.setAdapter(notificationAdapter);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            notificationsRef = FirebaseDatabase.getInstance().getReference("notifications").child(currentUserId);
            if (progressBar != null) progressBar.setVisibility(android.view.View.VISIBLE);
            loadNotifications();
        } else {
            Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadNotifications() {
        notificationsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (progressBar != null) progressBar.setVisibility(android.view.View.GONE);
                notificationList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    NotificationModel notification = data.getValue(NotificationModel.class);
                    if (notification != null) {
                        notificationList.add(notification);
                        // Mark as read once loaded
                        if (!notification.isRead()) {
                            data.getRef().child("read").setValue(true);
                        }
                    }
                }
                // Sort newest first
                Collections.sort(notificationList, (n1, n2) -> Long.compare(n2.getTimestamp(), n1.getTimestamp()));
                notificationAdapter.notifyDataSetChanged();

                if (notificationList.isEmpty()) {
                    if (layoutEmptyState != null) layoutEmptyState.setVisibility(android.view.View.VISIBLE);
                    rvNotifications.setVisibility(android.view.View.GONE);
                } else {
                    if (layoutEmptyState != null) layoutEmptyState.setVisibility(android.view.View.GONE);
                    rvNotifications.setVisibility(android.view.View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (progressBar != null) progressBar.setVisibility(android.view.View.GONE);
                Toast.makeText(NotificationsActivity.this, "Failed to load notifications", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
