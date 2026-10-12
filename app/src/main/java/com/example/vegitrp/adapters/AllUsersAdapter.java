package com.example.vegitrp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vegitrp.R;
import com.example.vegitrp.models.User;

import java.util.Objects;

public class AllUsersAdapter extends ListAdapter<User, AllUsersAdapter.UserViewHolder> {

    private final OnUserClickListener listener;

    public AllUsersAdapter(OnUserClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<User> DIFF_CALLBACK = new DiffUtil.ItemCallback<User>() {
        @Override
        public boolean areItemsTheSame(@NonNull User oldItem, @NonNull User newItem) {
            return oldItem.getKey().equals(newItem.getKey());
        }

        @Override
        public boolean areContentsTheSame(@NonNull User oldItem, @NonNull User newItem) {
            return Objects.equals(oldItem.getName(), newItem.getName())
                    && Objects.equals(oldItem.getRole(), newItem.getRole());
        }
    };

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = getItem(position);
        if (user != null) {
            holder.bind(user, listener);
        }
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName, tvUserRole;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tv_user_name);
            tvUserRole = itemView.findViewById(R.id.tv_user_role);
        }

        public void bind(final User user, final OnUserClickListener listener) {
            tvUserName.setText(user.getName());
            tvUserRole.setText(user.getRole());

            itemView.setOnClickListener(v -> listener.onUserClick(user));
        }
    }

    public interface OnUserClickListener {
        void onUserClick(User user);
    }
}
