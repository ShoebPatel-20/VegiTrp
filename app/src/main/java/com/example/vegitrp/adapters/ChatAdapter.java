package com.example.vegitrp.adapters;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vegitrp.R;
import com.example.vegitrp.models.ChatMessage;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private final Context context;
    private final List<ChatMessage> chatList;
    private final String currentUserId;

    public ChatAdapter(Context context, List<ChatMessage> chatList, String currentUserId) {
        this.context = context;
        this.chatList = chatList;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat_message, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMessage chatMessage = chatList.get(position);

        holder.tvChatMessageText.setText(chatMessage.getMessage());

        Calendar cal = Calendar.getInstance(Locale.getDefault());
        cal.setTimeInMillis(chatMessage.getTimestamp());
        String time = DateFormat.format("hh:mm a", cal).toString();
        holder.tvChatMessageTime.setText(time);

        // Align sent vs received messages
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 8, 0, 8);

        int paddingStartEnd = (int) (14 * context.getResources().getDisplayMetrics().density);
        int paddingTopBottom = (int) (10 * context.getResources().getDisplayMetrics().density);

        if (currentUserId != null && currentUserId.equals(chatMessage.getSenderId())) {
            // Sent message
            params.gravity = Gravity.END;
            holder.chatBubble.setBackgroundResource(R.drawable.bg_chat_bubble_sent);
            holder.tvChatMessageText.setTextColor(ContextCompat.getColor(context, R.color.brand_text_title));
            holder.tvChatMessageTime.setTextColor(ContextCompat.getColor(context, R.color.brand_green_primary));
        } else {
            // Received message
            params.gravity = Gravity.START;
            holder.chatBubble.setBackgroundResource(R.drawable.bg_chat_bubble_received);
            holder.tvChatMessageText.setTextColor(ContextCompat.getColor(context, R.color.brand_text_title));
            holder.tvChatMessageTime.setTextColor(ContextCompat.getColor(context, R.color.brand_text_muted));
        }
        
        holder.chatBubble.setPadding(paddingStartEnd, paddingTopBottom, paddingStartEnd, paddingTopBottom);
        holder.chatBubble.setLayoutParams(params);
    }

    @Override
    public int getItemCount() {
        return chatList != null ? chatList.size() : 0;
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        LinearLayout chatBubble;
        TextView tvChatMessageText, tvChatMessageTime;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            chatBubble = itemView.findViewById(R.id.chatBubble);
            tvChatMessageText = itemView.findViewById(R.id.tvChatMessageText);
            tvChatMessageTime = itemView.findViewById(R.id.tvChatMessageTime);
        }
    }
}
