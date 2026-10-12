package com.example.vegitrp.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.vegitrp.R;
import com.example.vegitrp.models.Listing;
import com.example.vegitrp.models.User;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SystemReportsFragment extends Fragment {

    private TextView tvTotalUsers, tvTotalCompletedTrips, tvTotalSystemRevenue;
    private PieChart pieChart;
    private DatabaseReference mDatabase;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_system_reports, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        tvTotalUsers = view.findViewById(R.id.tv_total_users);
        tvTotalCompletedTrips = view.findViewById(R.id.tv_total_completed_trips);
        tvTotalSystemRevenue = view.findViewById(R.id.tv_total_system_revenue);
        pieChart = view.findViewById(R.id.pie_chart);

        loadSystemReports();
    }

    private void loadSystemReports() {
        // Load total users
        mDatabase.child("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tvTotalUsers.setText("Total Users: " + snapshot.getChildrenCount());
                
                Map<String, Integer> roleCounts = new HashMap<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null) {
                        String role = user.getRole();
                        roleCounts.put(role, roleCounts.getOrDefault(role, 0) + 1);
                    }
                }
                updatePieChart(roleCounts);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // Load total completed trips and revenue
        mDatabase.child("Listings").orderByChild("status").equalTo("Completed").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tvTotalCompletedTrips.setText("Total Completed Trips: " + snapshot.getChildrenCount());

                double totalRevenue = 0;
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Listing listing = dataSnapshot.getValue(Listing.class);
                    if (listing != null) {
                        totalRevenue += listing.getPrice();
                    }
                }
                tvTotalSystemRevenue.setText("Total System Revenue: " + totalRevenue);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void updatePieChart(Map<String, Integer> roleCounts) {
        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : roleCounts.entrySet()) {
            entries.add(new PieEntry(entry.getValue(), entry.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "User Roles");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(12f);

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
        pieChart.getDescription().setEnabled(false);
        pieChart.invalidate();
    }
}
