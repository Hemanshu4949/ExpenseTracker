package com.example.expensetracker.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensetracker.Model.FriendRequest;
import com.example.expensetracker.R;

import java.util.List;

public class FriendRequestAdapter extends RecyclerView.Adapter<FriendRequestAdapter.ViewHolder> {

    private final List<FriendRequest> requestList;
    private final OnRequestActionListener listener;

    public interface OnRequestActionListener {
        void onAccept(FriendRequest request);
        void onDecline(FriendRequest request);
    }

    public FriendRequestAdapter(List<FriendRequest> requestList, OnRequestActionListener listener) {
        this.requestList = requestList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Ensure you have created list_item_friend_request.xml as shown in previous responses
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_friend_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FriendRequest request = requestList.get(position);

        // Display email or name
        String displayText = request.getSenderName() + " (" + request.getSenderEmail() + ")";
        holder.tvRequesterEmail.setText(displayText);

        holder.btnAccept.setOnClickListener(v -> listener.onAccept(request));
        holder.btnDecline.setOnClickListener(v -> listener.onDecline(request));

    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRequesterEmail;
        Button btnAccept;
        Button btnDecline;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRequesterEmail = itemView.findViewById(R.id.tv_requester_email);
            btnAccept = itemView.findViewById(R.id.btn_accept);
            btnDecline = itemView.findViewById(R.id.btn_decline);
        }
    }
}