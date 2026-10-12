package com.example.vegitrp.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.vegitrp.R;
import com.example.vegitrp.models.Driver;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EditDriverActivity extends BaseActivity {

    private EditText etDriverName, etDriverPhone;
    private String driverKey;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_driver);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowTitleEnabled(false);
            }
        }

        etDriverName = findViewById(R.id.et_driver_name);
        etDriverPhone = findViewById(R.id.et_driver_phone);
        Button btnUpdate = findViewById(R.id.btn_update_driver);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            driverKey = extras.getString("driver_key");
            etDriverName.setText(extras.getString("driver_name"));
            etDriverPhone.setText(extras.getString("driver_phone"));
        }

        btnUpdate.setOnClickListener(v -> {
            String name = etDriverName.getText().toString().trim();
            String phone = etDriverPhone.getText().toString().trim();

            if (name.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "All fields must be filled", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null && driverKey != null) {
                String userId = currentUser.getUid();
                Driver driver = new Driver(name, phone);

                mDatabase.child("Drivers").child(userId).child(driverKey).setValue(driver)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(EditDriverActivity.this, "Driver updated successfully", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                Toast.makeText(EditDriverActivity.this, "Failed to update driver", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
