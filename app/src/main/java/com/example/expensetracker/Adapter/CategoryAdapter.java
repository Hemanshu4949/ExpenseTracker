package com.example.expensetracker.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.expensetracker.Model.CategoryItem;
import com.example.expensetracker.R;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private final List<CategoryItem> categoryItems;
    private final OnCategoryClickListener listener;

    public interface OnCategoryClickListener {
        void onCategoryClick(CategoryItem item);
    }

    public CategoryAdapter(List<CategoryItem> categoryItems, OnCategoryClickListener listener) {
        this.categoryItems = categoryItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        CategoryItem item = categoryItems.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return categoryItems.size();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivCategoryIcon;
        private final TextView tvCategoryName;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCategoryIcon = itemView.findViewById(R.id.ivCategoryIcon);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
        }

        public void bind(final CategoryItem item, final OnCategoryClickListener listener) {
            ivCategoryIcon.setImageResource(item.getIconResId());
            tvCategoryName.setText(item.getName());
            itemView.setOnClickListener(v -> listener.onCategoryClick(item));
        }
    }
}
