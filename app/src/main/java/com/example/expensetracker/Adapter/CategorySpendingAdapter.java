package com.example.expensetracker.Adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensetracker.Model.CategorySpending;
import com.example.expensetracker.R;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CategorySpendingAdapter extends RecyclerView.Adapter<CategorySpendingAdapter.ViewHolder> {

    private final List<CategorySpending> categorySpendings;
    private final Context context;
    // Currency Formatter for Indian Rupee (₹), matching your SummaryFragment
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

    public CategorySpendingAdapter(Context context, List<CategorySpending> categorySpendings) {
        this.context = context;
        this.categorySpendings = categorySpendings;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for a single row (list_item_category_spending.xml)
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_category_spending, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Get the data for the current row
        CategorySpending item = categorySpendings.get(position);

        // Format the currency amount (e.g., "₹780")
        currencyFormat.setMaximumFractionDigits(0); // For whole numbers in the list
        holder.tvCategoryAmount.setText(currencyFormat.format(item.getAmount()));

        // Set the data from the object to the UI components
        holder.tvCategoryName.setText(item.getCategoryName());
        holder.progressCategory.setProgress(item.getProgress());
        holder.ivCategoryIcon.setImageResource(item.getIconResId());

        // Set the background color of the icon's circle
        int backgroundColor = ContextCompat.getColor(context, item.getIconBackgroundColorResId());
        holder.iconBackground.setBackgroundTintList(ColorStateList.valueOf(backgroundColor));
    }

    @Override
    public int getItemCount() {
        // Return the total number of categories
        return categorySpendings.size();
    }

    /**
     * ViewHolder class to hold references to the views in each row
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        final FrameLayout iconBackground;
        final ImageView ivCategoryIcon;
        final TextView tvCategoryName;
        final TextView tvCategoryAmount;
        final ProgressBar progressCategory;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Find all the views in the row layout
            iconBackground = itemView.findViewById(R.id.icon_background);
            ivCategoryIcon = itemView.findViewById(R.id.iv_category_icon);
            tvCategoryName = itemView.findViewById(R.id.tv_category_name);
            tvCategoryAmount = itemView.findViewById(R.id.tv_category_amount);
            progressCategory = itemView.findViewById(R.id.progress_category);
        }
    }
}