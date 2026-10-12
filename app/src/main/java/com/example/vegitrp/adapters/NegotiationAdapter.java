package com.example.vegitrp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vegitrp.R;
import com.example.vegitrp.models.Negotiation;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NegotiationAdapter extends RecyclerView.Adapter<NegotiationAdapter.NegotiationViewHolder> {

    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    private final List<Negotiation> negotiationList;
    private final String currentUserId;
    private final android.util.SparseBooleanArray expandedItems = new android.util.SparseBooleanArray();

    public NegotiationAdapter(List<Negotiation> negotiationList) {
        this.negotiationList = negotiationList;
        this.currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @Override
    public int getItemViewType(int position) {
        if (negotiationList.get(position).getSenderId().equals(currentUserId)) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public NegotiationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_negotiation_sent, parent, false);
            return new NegotiationViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_negotiation_received, parent, false);
            return new NegotiationViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull NegotiationViewHolder holder, int position) {
        Negotiation negotiation = negotiationList.get(position);
        boolean isExpanded = expandedItems.get(position, false);
        holder.bind(negotiation, isExpanded);

        holder.bubbleLayout.setOnClickListener(v -> {
            boolean expanded = !expandedItems.get(position, false);
            expandedItems.put(position, expanded);
            notifyItemChanged(position);
        });
    }

    @Override
    public int getItemCount() {
        return negotiationList.size();
    }

    static class NegotiationViewHolder extends RecyclerView.ViewHolder {
        TextView tvSenderName, tvMessage, tvTimestamp;
        android.widget.ImageView toggleArrow;
        android.widget.LinearLayout detailsContainer, bubbleLayout;

        public NegotiationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSenderName = itemView.findViewById(R.id.tv_sender_name);
            tvMessage = itemView.findViewById(R.id.tv_message);
            tvTimestamp = itemView.findViewById(R.id.tv_timestamp);
            toggleArrow = itemView.findViewById(R.id.toggle_arrow);
            detailsContainer = itemView.findViewById(R.id.details_container);
            bubbleLayout = itemView.findViewById(R.id.bubble_layout);
        }

        public void bind(Negotiation negotiation, boolean isExpanded) {
            tvSenderName.setText(negotiation.getSenderName());

            String message = "";
            if (negotiation.getPrice() > 0) {
                message += "Price: " + negotiation.getPrice();
            }
            if (negotiation.getQuantity() != null && !negotiation.getQuantity().isEmpty()) {
                if (!message.isEmpty()) {
                    message += ", ";
                }
                message += "Quantity: " + negotiation.getQuantity();
            }
            tvMessage.setText(message);

            if (negotiation.getTimestamp() instanceof Long) {
                SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                tvTimestamp.setText(sdf.format(new Date((Long) negotiation.getTimestamp())));
            }
            
            detailsContainer.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
            toggleArrow.setImageResource(isExpanded ? android.R.drawable.arrow_up_float : android.R.drawable.arrow_down_float);
        }
    }
}
