package com.example.vegitrp.adapters;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vegitrp.R;
import com.example.vegitrp.models.Review;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private Context context;
    private List<Review> reviewList;

    public ReviewAdapter(Context context, List<Review> reviewList) {
        this.context = context;
        this.reviewList = reviewList;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviewList.get(position);

        holder.rbReviewItem.setRating(review.getRatingValue());
        holder.tvReviewText.setText(review.getComment());

        // Format Date
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        cal.setTimeInMillis(review.getTimestamp());
        String date = DateFormat.format("dd MMM yyyy", cal).toString();
        holder.tvReviewDate.setText(date);

        // Fetch User Name
        FirebaseDatabase.getInstance().getReference("users").child(review.getReviewerId())
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String name = snapshot.child("name").getValue(String.class);
                        holder.tvReviewerName.setText(name != null ? name : "Unknown User");
                    } else {
                        holder.tvReviewerName.setText("Unknown User");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    holder.tvReviewerName.setText("Unknown User");
                }
            });
    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    public static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView tvReviewerName, tvReviewDate, tvReviewText;
        RatingBar rbReviewItem;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            tvReviewerName = itemView.findViewById(R.id.tvReviewerName);
            tvReviewDate = itemView.findViewById(R.id.tvReviewDate);
            tvReviewText = itemView.findViewById(R.id.tvReviewText);
            rbReviewItem = itemView.findViewById(R.id.rbReviewItem);
        }
    }
}
