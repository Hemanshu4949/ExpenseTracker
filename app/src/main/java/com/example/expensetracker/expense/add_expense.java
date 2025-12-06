package com.example.expensetracker.expense;

import android.app.DatePickerDialog;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.fragment.app.Fragment;

import com.example.expensetracker.CategorySelectionBottomSheet;
import com.example.expensetracker.Model.Expense;
import com.example.expensetracker.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class add_expense extends Fragment implements CategorySelectionBottomSheet.CategorySelectionListener {

    private EditText etAmount, etDate, etNotes ;
    private LinearLayout categoryContainer ;
    private TextView tvCategory , tvtype;
    private Button btnCash, btnCreditCard, btnDebitCard, btnOnline;
    private Button selectedPaymentMethod;
    ImageView datepick_icon , type_icon;
    private SwitchMaterial typeswitch ;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;


    private final Calendar myCalendar = Calendar.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        return inflater.inflate(R.layout.fragment_add_expense, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize UI components
        etAmount = view.findViewById(R.id.etAmount);
        etDate = view.findViewById(R.id.etDate);
        datepick_icon = view.findViewById(R.id.date_pick_icon);
        etNotes = view.findViewById(R.id.etNotes);
        tvtype = view.findViewById(R.id.tvtype);
        categoryContainer = view.findViewById(R.id.categoryContainer);
        typeswitch = view.findViewById(R.id.typeswitch);
        tvCategory = view.findViewById(R.id.tvCategory); // You need to add this ID to your TextView inside categoryContainer
        SwitchMaterial switchSplit = view.findViewById(R.id.switchSplit);
        LinearLayout splitDetailsContainer = view.findViewById(R.id.splitDetailsContainer);

        type_icon  = view.findViewById(R.id.type_icon);
        btnCash = view.findViewById(R.id.btnCash);
        btnCreditCard = view.findViewById(R.id.btnCreditCard);
        btnDebitCard = view.findViewById(R.id.btnDebitCard);
        btnOnline = view.findViewById(R.id.btnOnline);


        switchSplit.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                splitDetailsContainer.setVisibility(View.VISIBLE);
                // Optional: Load user list here if not already loaded
            } else {
                splitDetailsContainer.setVisibility(View.GONE);
            }
        });


        // Setup Listeners
        setupCategorySelector();
        setuptypeswitcher();
        setupDatePicker();
        setupPaymentMethodSelection();
        setupAddExpenseButton(view);
        setupCloseButton(view);
    }

    private void setupCloseButton(View view) {
        view.findViewById(R.id.btnClose).setOnClickListener(v -> {
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().popBackStack();
            }
        });
    }



    private void setuptypeswitcher()
    {
        tvtype.setText("Spend");
        typeswitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                {
                    tvtype.setText("Credited");
                    type_icon.setImageResource(R.drawable.expenses);
                }
                else
                {
                    tvtype.setText("Spend");
                    type_icon.setImageResource(R.drawable.spending);
                }
            }
        });
    }

    private void setupCategorySelector() {
            categoryContainer.setOnClickListener(v -> {
                Log.d("clicked_container" , "Clicked Container");
                // Create an instance of our new bottom sheet.
                CategorySelectionBottomSheet bottomSheet = new CategorySelectionBottomSheet();
                // Set this fragment as the listener for the bottom sheet.
                bottomSheet.setCategorySelectionListener(this);
                // Show the bottom sheet.
                bottomSheet.show(getParentFragmentManager(), "CategorySelectionBottomSheet");
        });
    }


    private void setupDatePicker() {
        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, dayOfMonth) -> {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, month);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateDateLabel();
        };

        datepick_icon.setOnClickListener(v -> new DatePickerDialog(getContext(), dateSetListener,
                myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)).show());
    }

    private void updateDateLabel() {
        String myFormat = "dd/MM/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        etDate.setText(sdf.format(myCalendar.getTime()));
    }

    private void setupPaymentMethodSelection() {
        View.OnClickListener paymentMethodListener = v -> {
            // Unselect the previously selected button
            if (selectedPaymentMethod != null) {
                selectedPaymentMethod.setSelected(false);
            }
            // Select the new button
            v.setSelected(true);
            selectedPaymentMethod = (Button) v;
        };

        btnCash.setOnClickListener(paymentMethodListener);
        btnCreditCard.setOnClickListener(paymentMethodListener);
        btnDebitCard.setOnClickListener(paymentMethodListener);
        btnOnline.setOnClickListener(paymentMethodListener);

        // Set a default selection
        btnCash.performClick();
    }

    private void setupAddExpenseButton(View view) {
        MaterialButton btnAddExpense = view.findViewById(R.id.btnAddExpense);
        btnAddExpense.setOnClickListener(v -> {
            try {
                saveExpenseToDatabase();
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void onCategorySelected(String categoryName) {
        tvCategory.setText(categoryName);

    }


    //Adding data to database

    private void saveExpenseToDatabase() throws ParseException {
        // 1. Get all data from fields and validate
        String amountStr = etAmount.getText().toString().trim();
        String category = tvCategory.getText().toString();
        String date = etDate.getText().toString();
        String notes = etNotes.getText().toString().trim();
        String type = tvtype.getText().toString().trim();
        String paymentMethod = selectedPaymentMethod != null ? selectedPaymentMethod.getText().toString() : "None";

        if (TextUtils.isEmpty(amountStr) || Double.parseDouble(amountStr) <= 0) {
            Toast.makeText(getContext(), "Please enter a valid amount.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (category.equals("Category")) {
            Toast.makeText(getContext(), "Please select a category.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(date)) {
            Toast.makeText(getContext(), "Please select a date.", Toast.LENGTH_SHORT).show();
            return;
        }
        double amount = Double.parseDouble(amountStr);


        // 2. Get the current user's ID
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        Log.d("user_ref" , currentUser.getUid().toString());


        if (currentUser == null) {
            Toast.makeText(getContext(), "Error: User not logged in.", Toast.LENGTH_SHORT).show();
            return;
        }
        String userId = currentUser.getUid();


        // 3. Create a unique ID for the new expense
        String expenseId = mDatabase.child("expenses").child(userId).push().getKey();

        // 4. Create an Expense object with all the data

        // The date you want to convert
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.US);

        // Parse the string into a Date object
        Date date_object = sdf.parse(date);

        // Create a Timestamp object from the Date object
        Timestamp timestamps = new Timestamp(Instant.ofEpochMilli(date_object.getTime()));

        long timestamp = timestamps.toDate().getTime();

        Expense expense = new Expense(expenseId, amount, category, date, notes,type, paymentMethod, timestamp);

        // 5. Save the object to Firebase Realtime Database
        if (expenseId != null) {
            mDatabase.child("expenses").child(userId).child(expenseId).setValue(expense)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Expense added successfully!", Toast.LENGTH_SHORT).show();
                        // Return to the previous screen
                        if (getParentFragmentManager() != null) {
                            getParentFragmentManager().popBackStack();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Failed to add expense. Please try again.", Toast.LENGTH_SHORT).show();
                        Log.e("FirebaseDB", "Failed to write expense to database.", e);
                    });
        }
    }
}