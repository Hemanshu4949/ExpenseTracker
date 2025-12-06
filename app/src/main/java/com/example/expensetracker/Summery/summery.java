package com.example.expensetracker.Summery;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.icu.text.NumberFormat;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.icu.util.TimeZone;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensetracker.Adapter.CategorySpendingAdapter;
import com.example.expensetracker.CategorySelectionBottomSheet;
import com.example.expensetracker.Model.CategorySpending;
import com.example.expensetracker.Model.Expense;
import com.example.expensetracker.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class summery extends Fragment implements CategorySelectionBottomSheet_summery.CategorySelectionListener_summery {

    private TextView tvTotalSpending , Compare_value;
    private TextView tvThisMonthSpending;
    private RecyclerView rvCategories;
    private MaterialButton btnDateRangeFilter , filter_category;
    private ImageView Compare_icon;
    private LineChart incomeChart;


    // --- Firebase & Adapter ---
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private ValueEventListener mExpenseListener;
    private CategorySpendingAdapter adapter;
    private final List<CategorySpending> categorySpendingList = new ArrayList<>();

    // --- Date Filtering ---
    private Long filterStartDate = null;
    private Long filterEndDate = null;
    private final Calendar startCalendar = Calendar.getInstance();
    private final Calendar endCalendar = Calendar.getInstance();

    // --- CategiryFiltering ---
    private String filterCategory = null;


    // Currency Formatter for Indian Rupee (₹)
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_summery, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvTotalSpending = view.findViewById(R.id.total_spending_amount);
        tvThisMonthSpending = view.findViewById(R.id.monthly_spending_amount);
        rvCategories = view.findViewById(R.id.recycler_view_categories);

        Compare_icon = view.findViewById(R.id.Compare_arrow);
        Compare_value = view.findViewById(R.id.Compare_value);


        // --- Initialize Firebase ---


        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {


            // Get a reference to the user's "expenses" node in the database


            mDatabase = FirebaseDatabase.getInstance().getReference().child("expenses").child(currentUser.getUid());
        }



        // --- Setup RecyclerView ---
        setupRecyclerView();
        btnDateRangeFilter = view.findViewById(R.id.filter_date_range);
        btnDateRangeFilter.setOnClickListener(v -> showDateRangePickerDialog());

        filter_category = view.findViewById(R.id.filter_category);
        filter_category.setOnClickListener(v -> setupCategorySelector());

        // --- Chart code ---
        incomeChart = view.findViewById(R.id.incomeChart);




    }


    private void setupCategorySelector() {
        filter_category.setOnClickListener(v -> {
            Log.d("clicked_container" , "Clicked Container");
            // Create an instance of our new bottom sheet.
            CategorySelectionBottomSheet_summery bottomSheet = new CategorySelectionBottomSheet_summery();
            // Set this fragment as the listener for the bottom sheet.
            bottomSheet.setCategorySelectionListener_summery((CategorySelectionBottomSheet_summery.CategorySelectionListener_summery) this);
            // Show the bottom sheet.
            bottomSheet.show(getParentFragmentManager(), "CategorySelectionBottomSheet");
        });
    }




    private void showDateRangePickerDialog() {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        // Inflate the custom layout
        View dialogView = inflater.inflate(R.layout.dialog_date_range_picker, null);

        // Get references to the EditText fields inside the dialog
        EditText etStartDate = dialogView.findViewById(R.id.etStartDate);
        EditText etEndDate = dialogView.findViewById(R.id.etEndDate);

        // --- Setup Start Date Picker ---
        DatePickerDialog.OnDateSetListener startDateSetListener = (view, year, month, dayOfMonth) -> {
            startCalendar.set(Calendar.YEAR, year);
            startCalendar.set(Calendar.MONTH, month);
            startCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            etStartDate.setText(formatDate(startCalendar)); // Update the EditText
        };
        // Show DatePickerDialog when etStartDate is clicked
        etStartDate.setOnClickListener(v -> new DatePickerDialog(getContext(), startDateSetListener,
                startCalendar.get(Calendar.YEAR), startCalendar.get(Calendar.MONTH),
                startCalendar.get(Calendar.DAY_OF_MONTH)).show());

        // --- Setup End Date Picker ---
        DatePickerDialog.OnDateSetListener endDateSetListener = (view, year, month, dayOfMonth) -> {
            endCalendar.set(Calendar.YEAR, year);
            endCalendar.set(Calendar.MONTH, month);
            endCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            etEndDate.setText(formatDate(endCalendar)); // Update the EditText
        };
        // Show DatePickerDialog when etEndDate is clicked
        etEndDate.setOnClickListener(v -> new DatePickerDialog(getContext(), endDateSetListener,
                endCalendar.get(Calendar.YEAR), endCalendar.get(Calendar.MONTH),
                endCalendar.get(Calendar.DAY_OF_MONTH)).show());

        // --- Build and Show the Alert Dialog ---
        new AlertDialog.Builder(getContext()) // Using a theme if you have one
                .setView(dialogView)
                .setPositiveButton("Apply", (dialog, which) -> {
                    // When "Apply" is clicked, save the timestamps
                    filterStartDate = startCalendar.getTimeInMillis();
                    filterEndDate = endCalendar.getTimeInMillis();
                    loadFirebaseData(); // Reload data with the new filters
                    dialog.dismiss();
                })
                .setNeutralButton("Clear Filter", (dialog, which) -> {
                    // When "Clear Filter" is clicked, reset the timestamps
                    filterStartDate = null;
                    filterEndDate = null;
                    loadFirebaseData(); // Reload data with default (current month)
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    /**
     * Helper method to format a Calendar object into "MM/dd/yyyy" string
     */
    private String formatDate(Calendar calendar) {
        String myFormat = "MM/dd/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        return sdf.format(calendar.getTime());
    }



    private void setupRecyclerView() {
        rvCategories.setLayoutManager(new LinearLayoutManager(getContext()));
        // Initialize the adapter with an empty list. It will be populated by the Firebase listener.
        adapter = new CategorySpendingAdapter(getContext(), categorySpendingList);
        rvCategories.setAdapter(adapter);
    }

    /**
     * Loads and processes all expense data from Firebase.
     */

    private void loadFirebaseData() {
        Log.d("cateoryselector" , "loadfirebasedata");
        if (mDatabase == null) {
            Toast.makeText(getContext(), "User not logged in.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mExpenseListener != null) {
            mDatabase.removeEventListener(mExpenseListener);
        }

        mExpenseListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Expense> allExpenses = new ArrayList<>();
                double allTimeSpending = 0;
                double filteredPeriodSpending = 0;
                // Comparison Logic Variables
                double currentMonthSpending = 0;
                double last3MonthsSpending = 0;

                HashMap<String, Double> categoryTotals = new HashMap<>();

                // Get current month and year for default (no filter)
                Calendar now = Calendar.getInstance();
                int currentYear = now.get(Calendar.YEAR);
                int currentMonth = now.get(Calendar.MONTH)+1;


                Calendar current = Calendar.getInstance(TimeZone.getTimeZone("UTC"));


                // 1. Start of Current Month (e.g., Nov 1, 00:00:00)
                Calendar startOfCurrentMonth = (Calendar) current.clone();
                startOfCurrentMonth.set(Calendar.DAY_OF_MONTH, 1);
                startOfCurrentMonth.set(Calendar.HOUR_OF_DAY, 0);
                long startOfCurrentMonthMillis = startOfCurrentMonth.getTimeInMillis();

                // We clone the current month start and subtract 3 months
                Calendar threeMonthsAgo = (Calendar) startOfCurrentMonth.clone();
                threeMonthsAgo.add(Calendar.MONTH, -3);
                long threeMonthsAgoMillis = threeMonthsAgo.getTimeInMillis();


                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Expense expense = dataSnapshot.getValue(Expense.class);

                    if (expense != null && "Spend".equalsIgnoreCase(expense.getType()))
                    {
                        // to feed the all expense list for the chart
                            allExpenses.add(expense);

                        // 1. calculating the current month spending
                        long txTimestamp = expense.getTimestamp();
                        Calendar txCalendar = Calendar.getInstance();
                        txCalendar.setTimeInMillis(txTimestamp);

                        Date date = new Date(txTimestamp);
                        Log.d("tranow", String.valueOf(date));
                        txCalendar.setTime(date);

                        int txYear = txCalendar.get(Calendar.YEAR);
                        int txMonth = txCalendar.get(Calendar.MONTH)+1;

                        // 2. Check if the transaction is in the correct date range



                        boolean isInFilterRange;

                        if (filterStartDate != null && filterEndDate != null) {
                            // --- Use the user-selected date range ---
                            isInFilterRange = (txTimestamp >= filterStartDate && txTimestamp <= filterEndDate);

                            Log.d("tranow", filterStartDate + " " + filterEndDate);
                        } else {

                            // --- Default: Use the current month ---

                            isInFilterRange = (txYear == currentYear && txMonth == currentMonth);
                            Log.d("tranow", txYear + " 3" + txMonth);
                            Log.d("tranow", String.valueOf(isInFilterRange));
                        }

                        if(txYear == currentYear && txMonth == currentMonth)
                        {
                            Log.d("tranow", currentYear + " 4" + currentMonth);
                            allTimeSpending += expense.getAmount();
                        }

                        // Comparison Logic (Independent of filters)
                        // Current Month
                        if (txTimestamp >= startOfCurrentMonthMillis) {
                            currentMonthSpending += expense.getAmount();
                        }
                        // Last 3 Months (excluding current month)
                        if(txTimestamp >= threeMonthsAgoMillis && txTimestamp < startOfCurrentMonthMillis) {
                            last3MonthsSpending += expense.getAmount();
                        }

                        // --- 3. THIS IS THE CODE for the category filter ---
                        boolean isInCategory;
                        if (filterCategory == null || (filterCategory.equals("All Category"))) {
                            isInCategory = true;
                        }
                        else {
                            isInCategory = filterCategory.equalsIgnoreCase(expense.getCategory());
                        }
                        Log.d("category_checker" , String.valueOf(isInCategory));
                        // 4. Add to totals ONLY if it passes BOTH filters
                        if (isInFilterRange && isInCategory) {
                            Log.d("booleancheck", isInCategory + " "+ isInCategory);
                            filteredPeriodSpending += expense.getAmount();
                            String category = expense.getCategory();
                            double currentTotal = categoryTotals.getOrDefault(category, 0.0);
                            categoryTotals.put(category, currentTotal + expense.getAmount());
                        }

                    }
                }
                setupChart(allExpenses);

                // --- Update the UI Text ---
                currencyFormat.setMaximumFractionDigits(2); // e.g., ₹2,450.75
                tvTotalSpending.setText(currencyFormat.format(allTimeSpending));
                tvThisMonthSpending.setText(currencyFormat.format(filteredPeriodSpending));

                Log.d("last3monthandcurrent"  ,  currentMonthSpending +"  "+ last3MonthsSpending);
                updateComparisonUI(currentMonthSpending, last3MonthsSpending);

                // --- Process and Display the Category List ---
                Log.d("SummaryFragment", "Data reloaded. Items in list1: " + categorySpendingList.size());
                categorySpendingList.clear();
                for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
                    String category = entry.getKey();
                    double amount = entry.getValue();
                    int progress = 0;
                    if (filteredPeriodSpending > 0) {
                        progress = (int) ((amount / filteredPeriodSpending) * 100);
                    }

                    categorySpendingList.add(new CategorySpending(
                            category,
                            amount,
                            progress,
                            getIconForCategory(category),
                            getColorForCategory(category)
                    ));
                }

                categorySpendingList.sort((o1, o2) -> Double.compare(o2.getAmount(), o1.getAmount()));
                // IMPORTANT: Notify adapter on UI thread
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> adapter.notifyDataSetChanged());
                }

                Log.d("SummaryFragment", "Data reloaded. Items in list: " + categorySpendingList.size());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load summary.", Toast.LENGTH_SHORT).show();
            }
        };
        mDatabase.addValueEventListener(mExpenseListener);
    }

    @Override
    public void onCategorySelected_summery(String categoryName) {
        Log.d("SummaryFragment", "onCategorySelected called with: " + categoryName);

        if ("All Categories".equalsIgnoreCase(categoryName)) {
            filterCategory = null;
            filter_category.setText("Category");
        } else {
            filterCategory = categoryName;
            filter_category.setText(categoryName);
        }

        loadFirebaseData();

    }

    private void updateComparisonUI(double currentMonth, double last3MonthsTotal) {
        // Check if views are found to avoid NPE
        if (Compare_value == null || Compare_icon == null) return;

        double averageLast3Months = last3MonthsTotal / 3.0;

        if (averageLast3Months == 0) {
            // No prior data or spending was 0
            if (currentMonth > 0) {
                // 100% increase conceptually
                Compare_value.setText("100%");
                Compare_value.setTextColor(ContextCompat.getColor(getContext(), R.color.green));
                Compare_icon.setImageResource(R.drawable.ic_arrow_upward); // Use up arrow
                Compare_icon.setColorFilter(ContextCompat.getColor(getContext(), R.color.green));
            } else {
                // Both 0
                Compare_value.setText("0%");
                Compare_value.setTextColor(ContextCompat.getColor(getContext(), R.color.red));
                Compare_icon.setImageResource(R.drawable.ic_arrow_upward); // Use up arrow
                Compare_icon.setColorFilter(ContextCompat.getColor(getContext(), R.color.red));
            }
            return;
        }

        double percentageChange = ((currentMonth - averageLast3Months) / averageLast3Months) * 100;

        Compare_value.setText(String.format(Locale.US, "%.1f%%", Math.abs(percentageChange)));

        if (percentageChange > 0) {
            // Spending Increased (Bad) -> Red
            Compare_value.setTextColor(ContextCompat.getColor(getContext(), R.color.red));
            Compare_icon.setImageResource(R.drawable.ic_arrow_upward);
            Compare_icon.setColorFilter(ContextCompat.getColor(getContext(), R.color.red));
        } else {
            // Spending Decreased (Good) -> Green
            Compare_value.setTextColor(ContextCompat.getColor(getContext(), R.color.green));
            Compare_icon.setImageResource(R.drawable.ic_downwards_arrow); // Using drop down as down arrow, or create ic_arrow_downward
            Compare_icon.setColorFilter(ContextCompat.getColor(getContext(), R.color.green));
        }
    }






//    private void loadFirebaseData() {
//        if (mDatabase == null) {
//            Toast.makeText(getContext(), "User not logged in.", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        // Remove any existing listener to avoid attaching it multiple times
//        if (mExpenseListener != null) {
//            mDatabase.removeEventListener(mExpenseListener);
//        }
//
//        mExpenseListener = new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                double allTimeSpending = 0;
//                double thisMonthSpending = 0;
//
//                // Use a HashMap to sum up spending by category for the current month
//                HashMap<String, Double> categoryTotals = new HashMap<>();
//
//                // Get current month and year to filter transactions
//                Calendar now = Calendar.getInstance();
//                int currentYear = now.get(Calendar.YEAR);
//                int currentMonth = now.get(Calendar.MONTH);
//
//                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
//                    Expense expense = dataSnapshot.getValue(Expense.class);
//
//                    // Process only "spend" type transactions
//                    if (expense != null && "spend".equalsIgnoreCase(expense.getType())) {
//
//                        // 1. Calculate All-Time Spending
//                        allTimeSpending += expense.getAmount();
//
//                        // 2. Check if this transaction is from the current month
//                        Calendar txCalendar = Calendar.getInstance();
//                        txCalendar.setTimeInMillis(expense.getTimestamp());
//                        int txYear = txCalendar.get(Calendar.YEAR);
//                        int txMonth = txCalendar.get(Calendar.MONTH);
//
//                        if (txYear == currentYear && txMonth == currentMonth) {
//                            // 3. Calculate This Month's Total Spending
//                            thisMonthSpending += expense.getAmount();
//
//                            // 4. Aggregate This Month's Spending by Category
//                            String category = expense.getCategory();
//                            double currentTotal = categoryTotals.getOrDefault(category, 0.0);
//                            categoryTotals.put(category, currentTotal + expense.getAmount());
//                        }
//                    }
//                }
//
//                // --- Update the UI Text ---
//                currencyFormat.setMaximumFractionDigits(2); // e.g., ₹2,450.75
//                tvTotalSpending.setText(currencyFormat.format(allTimeSpending));
//                tvThisMonthSpending.setText(currencyFormat.format(thisMonthSpending));
//
//                // --- Process and Display the Category List ---
//                categorySpendingList.clear();
//                for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
//                    String category = entry.getKey();
//                    double amount = entry.getValue();
//                    int progress = 0;
//                    // Calculate the progress percentage for the progress bar
//                    if (thisMonthSpending > 0) {
//                        progress = (int) ((amount / thisMonthSpending) * 100);
//                    }
//
//                    // Add the processed data to the list for the adapter
//                    categorySpendingList.add(new CategorySpending(
//                            category,
//                            amount,
//                            progress,
//                            getIconForCategory(category), // Get R.drawable.ic_...
//                            getColorForCategory(category)  // Get R.color...
//                    ));
//                }
//
//                // Sort the list so the highest spending category is at the top
//                Collections.sort(categorySpendingList, (o1, o2) -> Double.compare(o2.getAmount(), o1.getAmount()));
//
//                // Tell the adapter that the data has changed and it needs to update the list
//                adapter.notifyDataSetChanged();
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Toast.makeText(getContext(), "Failed to load summary.", Toast.LENGTH_SHORT).show();
//            }
//        };
//        // Attach the listener to the database reference
//        mDatabase.addValueEventListener(mExpenseListener);
//    }

    /**
     * Helper method to map a category name (String) to a drawable resource ID.
     */


    // --- method to find the data to feed inside the chart---
    private List<Entry> getMonthlyIncomeEntries(List<Expense> expenses) {
        // 1. Initialize an array to hold totals for 12 months (0=Jan, 11=Dec)
        float[] monthlyTotals = new float[13];
        System.out.println("total no of input " + expenses.size());


        // Use a Calendar instance to extract month/year from timestamps/dates
        Calendar cal = Calendar.getInstance();

        // 2. Iterate through all expenses
        for (Expense expense : expenses) {
            // Filter for Income only
            if (expense != null ) {

                // Resolve the correct timestamp (using your DateHelper logic if needed)
                long timestamp = expense.getTimestamp();

                // If you have the issue where timestamp is wrong but date string is right:
                // timestamp = DateHelper.parseDateToMillis(expense.getDate(), expense.getTimestamp());

                cal.setTimeInMillis(timestamp);

                int expenseYear = cal.get(Calendar.YEAR);
                int expenseMonth = cal.get(Calendar.MONTH)+1; // 0-indexed


                Calendar cale = Calendar.getInstance();
                int currentyear = cale.get(Calendar.YEAR);

                Log.d("chart_entry_value", expenseYear +" " + expenseMonth + " " + currentyear);

                // 3. Check if it belongs to the target year
                if (expenseYear == currentyear) {
                    // Add the amount to the corresponding month bucket
                    monthlyTotals[expenseMonth-1] += (float) expense.getAmount();
                }
            }
        }

        // 4. Convert the array into MPAndroidChart Entry objects
        List<Entry> chartEntries = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            // i is the x-value (month index), monthlyTotals[i] is the y-value (amount)
            chartEntries.add(new Entry(i, monthlyTotals[i]));
        }

        return chartEntries;
    }


    // code ot setup the chart

    private void setupChart(List<Expense> expense) {
        // 1. Create Data Entries (X: Month Index, Y: Income Amount)
        // In a real app, you would loop through your Firebase data here to calculate these totals.
        List<Entry> entries = getMonthlyIncomeEntries(expense);

        // 2. Create a DataSet
        LineDataSet dataSet = new LineDataSet(entries, "Income");

        // Styling the Line
        dataSet.setColor(Color.parseColor("#5AE7C8")); // Green/Teal color
        dataSet.setLineWidth(3f);
        dataSet.setCircleColor(Color.parseColor("#5AE7C8"));
        dataSet.setCircleRadius(5f);
        dataSet.setDrawCircleHole(false);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(10f);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER); // Makes the line smooth/curved

        // Enable Gradient Fill (Optional but looks nice)
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.parseColor("#5AE7C8"));
        dataSet.setFillAlpha(50); // Transparency

        // 3. Create LineData
        LineData lineData = new LineData(dataSet);

        // 4. Set Data to Chart
        incomeChart.setData(lineData);

        // 5. Customize Chart Appearance
        configureChartAppearance();

        // Refresh chart
        incomeChart.invalidate();
    }

    private void configureChartAppearance() {
        incomeChart.getDescription().setEnabled(false); // Hide description label
        incomeChart.setTouchEnabled(true);
        incomeChart.setDragEnabled(true);
        incomeChart.setScaleEnabled(false); // Disable zooming for simplicity
        incomeChart.getLegend().setTextColor(Color.WHITE);

        // X-Axis Customization (Months)
        XAxis xAxis = incomeChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f); // Show every month

        // Custom Labels for X-Axis
        final List<String> months = Arrays.asList("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec");
        xAxis.setValueFormatter(new IndexAxisValueFormatter(months));

        // Y-Axis Customization (Left)
        YAxis leftAxis = incomeChart.getAxisLeft();
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(Color.parseColor("#33FFFFFF")); // Faint grid lines

        // Disable Right Y-Axis
        incomeChart.getAxisRight().setEnabled(false);
    }


    @DrawableRes
    private int getIconForCategory(String category) {
        if ("Food".equalsIgnoreCase(category)) {
            return R.drawable.restaurant;
        } else if ("Transport".equalsIgnoreCase(category)) {
            return R.drawable.car;
        } else if ("Fun".equalsIgnoreCase(category)) {
            return R.drawable.laugh;
        } else if ("Shopping".equalsIgnoreCase(category)) {
            return R.drawable.shopping_bag_1;
        } else if ("Utilities".equalsIgnoreCase(category)) {
            return R.drawable.utilization;
        } else {
            return R.drawable.ic_paid; // A default icon
        }
    }

    /**
     * Helper method to map a category name (String) to a color resource ID for the icon background.
     */
    @ColorRes
    private int getColorForCategory(String category) {
        if ("Food".equalsIgnoreCase(category)) {
            return R.color.summary_category_food; // e.g., Orange
        } else if ("Transport".equalsIgnoreCase(category)) {
            return R.color.summary_accent_blue; // e.g., Blue
        } else if ("Fun".equalsIgnoreCase(category)) {
            return R.color.colorCard; // e.g., Purple
        } else if ("Shopping".equalsIgnoreCase(category)) {
            return R.color.summary_accent_green; // e.g., Green
        } else {
            return R.color.summary_text_grey; // A default color
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // Start loading data when the fragment becomes visible
        loadFirebaseData();
    }

    @Override
    public void onStop() {
        super.onStop();
        // Remove the listener when the fragment is not visible to save resources
        if (mExpenseListener != null && mDatabase != null) {
            mDatabase.removeEventListener(mExpenseListener);
        }
    }
}