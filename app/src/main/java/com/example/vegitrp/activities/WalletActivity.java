package com.example.vegitrp.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vegitrp.R;
import com.example.vegitrp.adapters.TransactionAdapter;
import com.example.vegitrp.models.Transaction;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class WalletActivity extends BaseActivity {

    private TextView tvWalletBalance;
    private Button btnWithdraw;
    private RecyclerView rvTransactions;
    private View layoutEmptyTransactions;
    private TransactionAdapter transactionAdapter;
    private List<Transaction> transactionList;

    private DatabaseReference walletRef;
    private DatabaseReference transactionsRef;
    private String currentUserId;

    private double currentBalance = 0.0;
    private BarChart walletBarChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.wallet_toolbar_title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        tvWalletBalance = findViewById(R.id.tv_wallet_balance);
        btnWithdraw = findViewById(R.id.btn_withdraw);
        rvTransactions = findViewById(R.id.rv_transactions);
        layoutEmptyTransactions = findViewById(R.id.layout_empty_transactions);
        walletBarChart = findViewById(R.id.walletBarChart);

        rvTransactions.setLayoutManager(new LinearLayoutManager(this));
        transactionList = new ArrayList<>();
        transactionAdapter = new TransactionAdapter(this, transactionList);
        rvTransactions.setAdapter(transactionAdapter);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            walletRef = FirebaseDatabase.getInstance().getReference("wallets").child(currentUserId);
            transactionsRef = FirebaseDatabase.getInstance().getReference("transactions").child(currentUserId);

            loadWalletBalance();
            loadTransactions();
        } else {
            Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show();
            finish();
        }

        btnWithdraw.setOnClickListener(v -> processWithdrawal());
    }

    private void loadWalletBalance() {
        walletRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.child("balance").exists()) {
                    Double val = snapshot.child("balance").getValue(Double.class);
                    currentBalance = (val != null) ? val : 0.0;
                } else {
                    currentBalance = 0.0;
                }
                tvWalletBalance.setText(String.format(Locale.getDefault(), "₹%.2f", currentBalance));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(WalletActivity.this, "Failed to load balance", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadTransactions() {
        transactionsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                transactionList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Transaction transaction = dataSnapshot.getValue(Transaction.class);
                    if (transaction != null) {
                        transactionList.add(transaction);
                    }
                }
                Collections.sort(transactionList, (t1, t2) -> Long.compare(t2.getTimestamp(), t1.getTimestamp()));
                transactionAdapter.notifyDataSetChanged();
                
                if (layoutEmptyTransactions != null) {
                    layoutEmptyTransactions.setVisibility(transactionList.isEmpty() ? View.VISIBLE : View.GONE);
                }
                if (rvTransactions != null) {
                    rvTransactions.setVisibility(transactionList.isEmpty() ? View.GONE : View.VISIBLE);
                }
                updateChart();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void processWithdrawal() {
        if (currentBalance <= 0) {
            Toast.makeText(this, "Insufficient balance to withdraw", Toast.LENGTH_SHORT).show();
            return;
        }

        // Simulate withdrawal
        String transId = transactionsRef.push().getKey();
        if (transId != null) {
            Transaction transaction = new Transaction(transId, currentUserId, currentBalance, "DEBIT", "Bank Withdrawal", System.currentTimeMillis());
            transactionsRef.child(transId).setValue(transaction);

            // Reset balance
            walletRef.child("balance").setValue(0.0);
            Toast.makeText(this, "Withdrawal request submitted successfully!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void updateChart() {
        if (transactionList.isEmpty()) {
            walletBarChart.clear();
            return;
        }

        // Group by day for the last 7 days
        Map<String, Float> dailyEarnings = new HashMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd", Locale.getDefault());
        List<String> days = new ArrayList<>();

        // Initialize the last 7 days to 0
        long dayMillis = 24 * 60 * 60 * 1000L;
        long currentMillis = System.currentTimeMillis();
        for (int i = 6; i >= 0; i--) {
            String dayStr = sdf.format(currentMillis - (i * dayMillis));
            dailyEarnings.put(dayStr, 0f);
            days.add(dayStr);
        }

        for (Transaction t : transactionList) {
            if ("CREDIT".equals(t.getType())) {
                String day = sdf.format(t.getTimestamp());
                if (dailyEarnings.containsKey(day)) {
                    Float prev = dailyEarnings.get(day);
                    dailyEarnings.put(day, (prev != null ? prev : 0f) + (float) t.getAmount());
                }
            }
        }

        List<BarEntry> entries = new ArrayList<>();
        int index = 0;
        for (String day : days) {
            Float earnings = dailyEarnings.get(day);
            entries.add(new BarEntry(index++, earnings != null ? earnings : 0f));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Daily Earnings (₹)");
        dataSet.setColor(androidx.core.content.ContextCompat.getColor(this, R.color.brand_green_primary));
        dataSet.setValueTextColor(androidx.core.content.ContextCompat.getColor(this, R.color.brand_text_title));
        dataSet.setValueTextSize(10f);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.5f);

        walletBarChart.setData(data);
        walletBarChart.getDescription().setEnabled(false);
        if (walletBarChart.getLegend() != null) {
            walletBarChart.getLegend().setTextColor(androidx.core.content.ContextCompat.getColor(this, R.color.brand_text_muted));
        }

        XAxis xAxis = walletBarChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(days));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(androidx.core.content.ContextCompat.getColor(this, R.color.brand_text_muted));
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);

        walletBarChart.getAxisRight().setEnabled(false);
        walletBarChart.getAxisLeft().setAxisMinimum(0f);
        walletBarChart.getAxisLeft().setTextColor(androidx.core.content.ContextCompat.getColor(this, R.color.brand_text_muted));
        walletBarChart.getAxisLeft().setDrawGridLines(true);
        walletBarChart.getAxisLeft().setGridColor(androidx.core.content.ContextCompat.getColor(this, R.color.brand_card_stroke));
        walletBarChart.getAxisLeft().setDrawAxisLine(false);
        walletBarChart.animateY(800);
        walletBarChart.invalidate();
    }
}
