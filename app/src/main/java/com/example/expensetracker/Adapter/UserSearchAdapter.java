package com.example.expensetracker.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensetracker.Model.User;
import com.example.expensetracker.R;
import com.google.android.material.button.MaterialButton;
import java.util.List;

public class UserSearchAdapter extends RecyclerView.Adapter<UserSearchAdapter.ViewHolder> {

    private final List<User> userList;
    private final OnUserActionListener listener;
    private final boolean isSelectionMode; // true for Split selection (Select), false for Add Friend (Add)

    public interface OnUserActionListener {
        void onAction(User user);
    }

    // Constructor with mode flag
    public UserSearchAdapter(List<User> userList, boolean isSelectionMode, OnUserActionListener listener) {
        this.userList = userList;
        this.isSelectionMode = isSelectionMode;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_user_search, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = userList.get(position);
        holder.tvName.setText(user.getName());
        holder.tvEmail.setText(user.getEmail());

        // Custom button text based on mode
        if (isSelectionMode) {
            holder.btnAction.setText("Select");
        } else {
            holder.btnAction.setText("Add");
        }

        // Click on the whole item OR the button triggers the action
        View.OnClickListener clickListener = v -> listener.onAction(user);

        // Optional: If in selection mode, clicking the whole row feels more natural
        if (isSelectionMode) {
            holder.itemView.setOnClickListener(clickListener);
        }

        holder.btnAction.setOnClickListener(clickListener);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail;
        MaterialButton btnAction;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvUserName);
            tvEmail = itemView.findViewById(R.id.tvUserEmail);
            // Ensure this ID (btnAdd) matches your list_item_user_search.xml layout
            btnAction = itemView.findViewById(R.id.btnAdd);
        }
    }
}