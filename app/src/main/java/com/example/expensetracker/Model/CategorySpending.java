package com.example.expensetracker.Model;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;

public class CategorySpending {
    private final String categoryName;
    private final double amount;
    private final int progress; // Should be 0-100
    @DrawableRes
    private final int iconResId;
    @ColorRes
    private final int iconBackgroundColorResId;

    public CategorySpending(String categoryName, double amount, int progress, int iconResId, int iconBackgroundColorResId) {
        this.categoryName = categoryName;
        this.amount = amount;
        this.progress = progress;
        this.iconResId = iconResId;
        this.iconBackgroundColorResId = iconBackgroundColorResId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public double getAmount() {
        return amount;
    }

    public int getProgress() {
        return progress;
    }

    public int getIconResId() {
        return iconResId;
    }

    public int getIconBackgroundColorResId() {
        return iconBackgroundColorResId;
    }
}
