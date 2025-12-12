package com.example.expensetracker.Adapter;

import android.content.Context;
import android.view.View;

import com.example.expensetracker.Model.User;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.List;

public class ChipGroupHelper {
    public interface OnChipRemoveListener {
        void onRemove(User user);
    }

    /**
     * Re-populates the ChipGroup with the list of selected users.
     */
    public static void refreshChips(Context context, ChipGroup chipGroup, List<User> selectedUsers, OnChipRemoveListener removeListener) {
        chipGroup.removeAllViews();

        for (User user : selectedUsers) {
            Chip chip = new Chip(context);
            chip.setText(user.getName());
            chip.setCloseIconVisible(true);
            chip.setCheckable(false);
            chip.setClickable(false);

            // Handle remove click
            chip.setOnCloseIconClickListener(v -> {
                if (removeListener != null) {
                    removeListener.onRemove(user);
                }
            });

            chipGroup.addView(chip);
        }

        // Show/Hide group based on content
        chipGroup.setVisibility(selectedUsers.isEmpty() ? View.GONE : View.VISIBLE);
    }
}
