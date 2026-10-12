package com.example.vegitrp.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.vegitrp.R;
import com.example.vegitrp.models.Vehicle;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EditVehicleActivity extends BaseActivity {

    private EditText etVehicleName, etVehicleNumber;
    private String vehicleKey;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_vehicle);

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

        etVehicleName = findViewById(R.id.et_vehicle_name);
        etVehicleNumber = findViewById(R.id.et_vehicle_number);
        Button btnUpdate = findViewById(R.id.btn_update_vehicle);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            vehicleKey = extras.getString("vehicle_key");
            etVehicleName.setText(extras.getString("vehicle_name"));
            etVehicleNumber.setText(extras.getString("vehicle_number"));
        }

        btnUpdate.setOnClickListener(v -> {
            String vehicleName = etVehicleName.getText().toString().trim();
            String vehicleNumber = etVehicleNumber.getText().toString().trim();

            if (vehicleName.isEmpty() || vehicleNumber.isEmpty()) {
                Toast.makeText(this, "All fields must be filled", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null && vehicleKey != null) {
                String userId = currentUser.getUid();
                Vehicle vehicle = new Vehicle(vehicleName, vehicleNumber);

                mDatabase.child("Vehicles").child(userId).child(vehicleKey).setValue(vehicle)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(EditVehicleActivity.this, "Vehicle updated successfully", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                Toast.makeText(EditVehicleActivity.this, "Failed to update vehicle", Toast.LENGTH_SHORT).show();
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
