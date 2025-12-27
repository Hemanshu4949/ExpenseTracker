package com.example.expensetracker.Summery;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensetracker.Adapter.CategoryAdapter;
import com.example.expensetracker.Model.CategoryItem;
import com.example.expensetracker.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;

public class CategorySelectionBottomSheet_summery extends BottomSheetDialogFragment {

    private CategorySelectionListener_summery mListener;

    public interface CategorySelectionListener_summery{
        void onCategorySelected_summery(String categoryName);
    }

    public void setCategorySelectionListener_summery(CategorySelectionListener_summery listener) {
        mListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_category_selection, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView rvCategories = view.findViewById(R.id.rvCategories);

        List<CategoryItem> categories = new ArrayList<>();
        // Add your categories here with their corresponding icons
        categories.add(new CategoryItem("Food", R.drawable.restaurant));
        categories.add(new CategoryItem("Transport", R.drawable.car));
        categories.add(new CategoryItem("Fun", R.drawable.laugh));
        categories.add(new CategoryItem("Shopping", R.drawable.shopping_bag_1));
        categories.add(new CategoryItem("Utilities", R.drawable.utilization));
        categories.add(new CategoryItem("All Category" , R.drawable.ic_paid));

        CategoryAdapter adapter = new CategoryAdapter(categories, item -> {
            if (mListener != null) {
                mListener.onCategorySelected_summery(item.getName());
            }
            dismiss(); // Close the bottom sheet after selection
        });

        rvCategories.setAdapter(adapter);
    }
}
