package com.example.vegitrp.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.vegitrp.R;
import com.example.vegitrp.models.Vehicle;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddVehicleActivity extends BaseActivity {

    private EditText etVehicleName, etVehicleNumber;
    private Button btnAddVehicle;

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_vehicle);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

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
        btnAddVehicle = findViewById(R.id.btn_add_vehicle);

        btnAddVehicle.setOnClickListener(v -> addVehicle());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void addVehicle() {
        String vehicleName = etVehicleName.getText().toString().trim();
        String vehicleNumber = etVehicleNumber.getText().toString().trim();

        if (vehicleName.isEmpty() || vehicleNumber.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // User is not logged in, handle appropriately
            Toast.makeText(this, "You must be logged in to add a vehicle", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        Vehicle vehicle = new Vehicle(vehicleName, vehicleNumber);

        mDatabase.child("Vehicles").child(userId).push().setValue(vehicle)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(AddVehicleActivity.this, "Vehicle added successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(AddVehicleActivity.this, "Failed to add vehicle", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
