package com.example.vegitrp.adapters;

import android.content.Context;
import android.graphics.Color;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vegitrp.R;
import com.example.vegitrp.models.NotificationModel;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private Context context;
    private List<NotificationModel> notificationList;

    public NotificationAdapter(Context context, List<NotificationModel> notificationList) {
        this.context = context;
        this.notificationList = notificationList;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        NotificationModel notification = notificationList.get(position);

        holder.tvNotificationTitle.setText(notification.getTitle());
        holder.tvNotificationMessage.setText(notification.getMessage());

        Calendar cal = Calendar.getInstance(Locale.getDefault());
        cal.setTimeInMillis(notification.getTimestamp());
        String time = DateFormat.format("dd MMM yyyy, hh:mm a", cal).toString();
        holder.tvNotificationTime.setText(time);

        if (!notification.isRead()) {
            holder.llNotificationBackground.setBackgroundColor(androidx.core.content.ContextCompat.getColor(context, R.color.brand_green_soft)); // Soft emerald for unread
        } else {
            holder.llNotificationBackground.setBackgroundColor(androidx.core.content.ContextCompat.getColor(context, R.color.brand_surface_card)); // Crisp white for read
        }
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        LinearLayout llNotificationBackground;
        TextView tvNotificationTitle, tvNotificationMessage, tvNotificationTime;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            llNotificationBackground = itemView.findViewById(R.id.llNotificationBackground);
            tvNotificationTitle = itemView.findViewById(R.id.tvNotificationTitle);
            tvNotificationMessage = itemView.findViewById(R.id.tvNotificationMessage);
            tvNotificationTime = itemView.findViewById(R.id.tvNotificationTime);
        }
    }
}
