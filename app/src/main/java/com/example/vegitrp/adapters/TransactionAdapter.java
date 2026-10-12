package com.example.vegitrp.adapters;

import android.content.Context;
import android.graphics.Color;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vegitrp.R;
import com.example.vegitrp.models.Transaction;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private Context context;
    private List<Transaction> transactionList;

    public TransactionAdapter(Context context, List<Transaction> transactionList) {
        this.context = context;
        this.transactionList = transactionList;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactionList.get(position);

        holder.tvTransactionDesc.setText(transaction.getDescription());

        // Format Date
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        cal.setTimeInMillis(transaction.getTimestamp());
        String date = DateFormat.format("dd MMM yyyy, HH:mm", cal).toString();
        holder.tvTransactionDate.setText(date);

        if ("CREDIT".equals(transaction.getType())) {
            holder.tvTransactionAmount.setText("+₹" + String.format(Locale.getDefault(), "%.2f", transaction.getAmount()));
            holder.tvTransactionAmount.setTextColor(androidx.core.content.ContextCompat.getColor(context, R.color.brand_green_primary));
            if (holder.ivTransactionIcon != null) {
                holder.ivTransactionIcon.setImageResource(R.drawable.ic_trans_credit);
                if (holder.ivTransactionIcon.getParent() instanceof View) {
                    ((View) holder.ivTransactionIcon.getParent()).setBackgroundTintList(
                            android.content.res.ColorStateList.valueOf(androidx.core.content.ContextCompat.getColor(context, R.color.brand_green_soft)));
                }
            }
        } else {
            holder.tvTransactionAmount.setText("-₹" + String.format(Locale.getDefault(), "%.2f", transaction.getAmount()));
            holder.tvTransactionAmount.setTextColor(Color.parseColor("#DC2626")); // Coral Red
            if (holder.ivTransactionIcon != null) {
                holder.ivTransactionIcon.setImageResource(R.drawable.ic_trans_debit);
                if (holder.ivTransactionIcon.getParent() instanceof View) {
                    ((View) holder.ivTransactionIcon.getParent()).setBackgroundTintList(
                            android.content.res.ColorStateList.valueOf(Color.parseColor("#FEF2F2")));
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView tvTransactionDesc, tvTransactionDate, tvTransactionAmount;
        ImageView ivTransactionIcon;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTransactionDesc = itemView.findViewById(R.id.tv_transaction_desc);
            tvTransactionDate = itemView.findViewById(R.id.tv_transaction_date);
            tvTransactionAmount = itemView.findViewById(R.id.tv_transaction_amount);
            ivTransactionIcon = itemView.findViewById(R.id.iv_transaction_icon);
        }
    }
}
