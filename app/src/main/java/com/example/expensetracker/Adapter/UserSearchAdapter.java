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
    private final OnAddClickListener listener;
    private final OnUserSelectedListener selected;

    public interface OnUserSelectedListener {
        void onUserSelected(User user);
    }

    public interface OnAddClickListener {
        void onAddClick(User user);
    }

    public UserSearchAdapter(List<User> userList, OnAddClickListener listener) {
        this.userList = userList;
        this.listener = listener;

    }
    public UserSearchAdapter(List<User> userList, OnAddClickListener listener, OnUserSelectedListener selected) {
        this.userList = userList;
        this.listener = listener;
        this.selected = selected;
    }
    public UserSearchAdapter(List<User> userList, OnUserSelectedListener selected) {
        this.userList = userList;
        this.selected = selected;
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

        holder.btnAdd.setOnClickListener(v -> listener.onAddClick(user));
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail;
        MaterialButton btnAdd;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvUserName);
            tvEmail = itemView.findViewById(R.id.tvUserEmail);
            btnAdd = itemView.findViewById(R.id.btnAdd);
        }
    }
}