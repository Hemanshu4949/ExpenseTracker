package com.example.expensetracker.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.expensetracker.Model.Expense;
import com.example.expensetracker.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

import java.util.Locale;

/**
 * This adapter binds data from Firebase (using FirebaseRecyclerOptions)
 * to your RecyclerView in the ExpenseFragment.
 */
public class TransactionAdapter extends FirebaseRecyclerAdapter<Expense, TransactionAdapter.TransactionViewHolder> {

    private final Context context;


    public TransactionAdapter(@NonNull FirebaseRecyclerOptions<Expense> options, Context context) {
        super(options);
        // We need context to get color resources (e.g., R.color.income_green)
        this.context = context;
    }

    /**
     * This method is called by the adapter to bind the data from an Expense object
     * to the views in a single row.
     */
    @Override
    protected void onBindViewHolder(@NonNull TransactionViewHolder holder, int position, @NonNull Expense model) {
        // Pass the data to the ViewHolder's bind method
        holder.bind(model, context);
    }

    /**
     * This method is called by the adapter to create a new ViewHolder
     * (a new row) when needed.
     */
    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for a single transaction item
        // This must match the dark-mode layout: item_transaction.xml
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    /**
     * The ViewHolder class holds the references to the UI components for each row.
     * This improves performance by avoiding repeated findViewById calls.
     */
    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCategoryIcon;
        TextView tvCategoryName;
        TextView tvTransactionNote;
        TextView tvTransactionAmount;
        TextView tvDate;


        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            // Find all the views in the item_transaction.xml layout
            ivCategoryIcon = itemView.findViewById(R.id.iv_icon);
            tvCategoryName = itemView.findViewById(R.id.tv_title);
            tvTransactionNote = itemView.findViewById(R.id.tv_transaction_note);
            tvTransactionAmount = itemView.findViewById(R.id.tv_transaction_amount);
            tvDate = itemView.findViewById(R.id.tv_date);
        }

        /**
         * This method contains the core logic to set the text and colors
         * for each transaction based on its "type".
         */
        public void bind(Expense expense, Context context) {
            tvCategoryName.setText(expense.getCategory());
            tvTransactionNote.setText(expense.getNotes());
            tvDate.setText(expense.getDate());

            // --- THIS IS THE CORE LOGIC for +/ - and colors ---
            if ("credited".equalsIgnoreCase(expense.getType())) {
                // It's INCOME
                String amountText = String.format(Locale.US, "+₹%.2f", expense.getAmount());
                tvTransactionAmount.setText(amountText);
                // Set text color to green (e.g., #5AE7C8 from your design)
                tvTransactionAmount.setTextColor(ContextCompat.getColor(context, R.color.green));
            } else {
                // It's an EXPENSE ("spend" or any other value)
                String amountText = String.format(Locale.US, "-₹%.2f", expense.getAmount());
                tvTransactionAmount.setText(amountText);
                // Set text color to red (e.g., #FF5A5F from your design)
                tvTransactionAmount.setTextColor(ContextCompat.getColor(context, R.color.error));
            }

            // --- This logic sets the icon based on the category string ---
            // Make sure you have these drawables in your res/drawable folder
            if ("Food".equalsIgnoreCase(expense.getCategory())) {
                ivCategoryIcon.setImageResource(R.drawable.restaurant);
            } else if ("Transport".equalsIgnoreCase(expense.getCategory())) {
                ivCategoryIcon.setImageResource(R.drawable.car);
            } else if ("Fun".equalsIgnoreCase(expense.getCategory())) {
                ivCategoryIcon.setImageResource(R.drawable.laugh);
            } else if ("Shopping".equalsIgnoreCase(expense.getCategory())) {
                ivCategoryIcon.setImageResource(R.drawable.shopping_bag_1);
            } else if ("Utilities".equalsIgnoreCase(expense.getCategory())) {
                ivCategoryIcon.setImageResource(R.drawable.utilization);
            } else {
                // A default icon for any other category
                ivCategoryIcon.setImageResource(R.drawable.ic_paid);
            }
        }
    }
}

