package com.example.expensetracker.Model;

public class CategoryItem {
    private final String name;
    private final int iconResId;

    public CategoryItem(String name, int iconResId) {
        this.name = name;
        this.iconResId = iconResId;
    }

    public String getName() {
        return name;
    }

    public int getIconResId() {
        return iconResId;
    }
}
