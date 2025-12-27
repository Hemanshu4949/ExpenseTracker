package com.example.expensetracker.expense;

import android.app.DatePickerDialog;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensetracker.Adapter.ChipGroupHelper;
import com.example.expensetracker.Adapter.UserSearchAdapter;
import com.example.expensetracker.CategorySelectionBottomSheet;
import com.example.expensetracker.Model.Expense;
import com.example.expensetracker.Model.User;
import com.example.expensetracker.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class add_expense extends Fragment implements CategorySelectionBottomSheet.CategorySelectionListener {

    private EditText etAmount, etDate, etNotes;
    private LinearLayout categoryContainer;
    private TextView tvCategory, tvtype;
    private Button btnCash, btnCreditCard, btnDebitCard, btnOnline;
    private Button selectedPaymentMethod;
    ImageView datepick_icon, type_icon;
    private SwitchMaterial typeswitch;
    SwitchMaterial switchSplit;
    LinearLayout splitDetailsContainer;
    private RecyclerView rvSplitSearchResults;
    private EditText etSearchFriend;



    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private DatabaseReference mDatabase;


    // --- for splitwise feature ---
    private User selectedFriendForSplit = null;
    private List<User> selectedFriendsForSplit = new ArrayList<>();
    private ChipGroup chipGroupSelectedFriend;


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
        switchSplit = view.findViewById(R.id.switchSplit);
        splitDetailsContainer = view.findViewById(R.id.splitDetailsContainer);
        chipGroupSelectedFriend = view.findViewById(R.id.chipGroupSelectedFriend);
        rvSplitSearchResults = view.findViewById(R.id.rvSplitUsers);
        etSearchFriend = view.findViewById(R.id.etSearchUsers);


        type_icon = view.findViewById(R.id.type_icon);
        btnCash = view.findViewById(R.id.btnCash);
        btnCreditCard = view.findViewById(R.id.btnCreditCard);
        btnDebitCard = view.findViewById(R.id.btnDebitCard);
        btnOnline = view.findViewById(R.id.btnOnline);

        // --- initialize mauth ---
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();



        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {

            // Get the size of the IME (keyboard) insets
            // Note: WindowInsetsCompat.Type.ime() is the standard way to get keyboard height
            int imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom;

            // Apply this height as padding to the bottom of the view
            // This pushes the view hierarchy up by the exact height of the keyboard
            v.setPadding(0, 0, 0, imeHeight-200);

            // Return the insets to allow them to continue being dispatched to other views
            return insets;
        });


        // Setup Listeners
        setupCategorySelector();
        setuptypeswitcher();
        setupDatePicker();
        setupPaymentMethodSelection();
        setupAddExpenseButton(view);
        setupCloseButton(view);
        setupSplitWiseFeature();

    }


    // --- methods for splitwise ---

    private void setupSplitWiseFeature() {
        // Toggle Visibility
        switchSplit.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                splitDetailsContainer.setVisibility(View.VISIBLE);

            } else {
                splitDetailsContainer.setVisibility(View.GONE);
                // Clear selection if disabled
                selectedFriendsForSplit.clear();
                refreshSelectedChips();
            }
        });

        // Search Setup
        rvSplitSearchResults.setLayoutManager(new LinearLayoutManager(getContext()));
        List<User> searchResults = new ArrayList<>();

        // Adapter logic: When "Add" is clicked in the list
        UserSearchAdapter searchAdapter = new UserSearchAdapter(searchResults, true, user -> {
            addFriendToSplit(user);
        });
        rvSplitSearchResults.setAdapter(searchAdapter);

        etSearchFriend.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (query.length() > 2) {
                    performSearch(query, searchResults, searchAdapter);
                } else {
                    searchResults.clear();
                    searchAdapter.notifyDataSetChanged();
                    rvSplitSearchResults.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void addFriendToSplit(User user) {
        // Prevent adding the same user twice
        for (User selected : selectedFriendsForSplit) {
            if (selected.getUid().equals(user.getUid())) {
                Toast.makeText(getContext(), "User already added", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        selectedFriendsForSplit.add(user);
        refreshSelectedChips();

        // Clear search to allow adding another
        etSearchFriend.setText("");
        rvSplitSearchResults.setVisibility(View.GONE);
    }

    private void refreshSelectedChips() {
        // Use the ChipGroupHelper to redraw chips
        ChipGroupHelper.refreshChips(getContext(), chipGroupSelectedFriend, selectedFriendsForSplit, userToRemove -> {
            selectedFriendsForSplit.remove(userToRemove);
            refreshSelectedChips(); // Recursive update to remove the chip view
        });
    }

    private void performSearch(String emailQuery, List<User> results, UserSearchAdapter adapter) {
        String currentUid = mAuth.getCurrentUser().getUid().toString();
        if (currentUid == null) return;

        firestore.collection("users")
                .document(currentUid)
                .collection("friends")
                .whereGreaterThanOrEqualTo("email", emailQuery)
                .whereLessThan("email", emailQuery + "\uf8ff")
                .limit(5) // Limit to 5 results
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    results.clear();
                    if (!queryDocumentSnapshots.isEmpty()) {

                        for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                            String docId = doc.getId();

                            // 1. Don't show current user
                            if (currentUid != null && docId.equals(currentUid)) continue;

                            // 2. Don't show users already selected
                            boolean alreadySelected = false;
                            for (User u : selectedFriendsForSplit) {
                                if (u.getUid().equals(docId)) {
                                    alreadySelected = true;
                                    break;
                                }
                            }
                            if (alreadySelected) continue;

                            String name = doc.getString("name");
                            String email = doc.getString("email");
                            results.add(new User(docId, name, email));
                        }
                    }

                    if (!results.isEmpty()) {
                        rvSplitSearchResults.setVisibility(View.VISIBLE);
                    } else {
                        rvSplitSearchResults.setVisibility(View.GONE);
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    // --- mthods for other functions ---
    private void setupCloseButton(View view) {
        view.findViewById(R.id.btnClose).setOnClickListener(v -> {
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().popBackStack();
            }
        });
    }


    // --- NEW METHOD: Save Split Expense ---
    private void saveSplitExpense() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;
        String currentUserId = currentUser.getUid();
        String currentUserName = currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "Friend";

        double totalAmount = Double.parseDouble(etAmount.getText().toString().trim());
        String category = tvCategory.getText().toString();
        String date = etDate.getText().toString();
        String notes = etNotes.getText().toString().trim();
        String paymentMethod = selectedPaymentMethod != null ? selectedPaymentMethod.getText().toString() : "Cash";
        long timestamp = myCalendar.getTimeInMillis();
        String expenseType = tvtype.getText().toString();

        // 1. Calculate Split Amount
        // Total people = Me + Selected Friends
        int totalPeople = selectedFriendsForSplit.size() + 1;
        double splitAmount = totalAmount / totalPeople;
        // Round to 2 decimal places to avoid long floating points
        splitAmount = Math.round(splitAmount * 100.0) / 100.0;

        // 2. Save My Share (Current User)
        String myExpenseId = mDatabase.child("expenses").child(currentUserId).push().getKey();
        if (myExpenseId != null) {
            Expense myExpense = new Expense(
                    myExpenseId,
                    splitAmount, // Only saving my share
                    category,
                    date,
                    notes + " (Split with " + selectedFriendsForSplit.size() + " others)",
                    expenseType,
                    paymentMethod,
                    timestamp

            );
            mDatabase.child("expenses").child(currentUserId).child(myExpenseId).setValue(myExpense);
        }

        // 3. Save Friends' Shares (Iterate through list)
        for (User friend : selectedFriendsForSplit) {
            String friendUid = friend.getUid();
            String friendExpenseId = mDatabase.child("expenses").child(friendUid).push().getKey();

            if (friendExpenseId != null) {
                // Note for friend to know who added it
                String friendNote = "Split with " + currentUserName + ": " + notes;

                // Create expense entry for friend
                // Marked as "Unpaid" payment method to signify it's a debt/split initially
                Expense friendExpense = new Expense(
                        friendExpenseId,
                        splitAmount,
                        category,
                        date,
                        friendNote,
                        expenseType, // Or you can use "Split" if you want to track it differently
                        paymentMethod,
                        timestamp // Assuming split expenses are usually spending
                );

                mDatabase.child("expenses").child(friendUid).child(friendExpenseId).setValue(friendExpense);
            }
        }

        Toast.makeText(getContext(), "Split expense added for everyone!", Toast.LENGTH_SHORT).show();
        if (getParentFragmentManager() != null) {
            getParentFragmentManager().popBackStack();
        }
    }


    private void setuptypeswitcher() {
        tvtype.setText("Spend");
        typeswitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    tvtype.setText("Credited");
                    type_icon.setImageResource(R.drawable.expenses);
                } else {
                    tvtype.setText("Spend");
                    type_icon.setImageResource(R.drawable.spending);
                }
            }
        });
    }

    private void setupCategorySelector() {
        categoryContainer.setOnClickListener(v -> {
            Log.d("clicked_container", "Clicked Container");
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

        if (switchSplit.isChecked() && !selectedFriendsForSplit.isEmpty()) {
            saveSplitExpense();
        } else {
            saveSingleUserExpense();
        }



    }

//}
// --- Existing Single User Save Logic ---
private void saveSingleUserExpense() throws ParseException {
    String amountStr = etAmount.getText().toString().trim();
    String category = tvCategory.getText().toString();
    String date = etDate.getText().toString();
    String notes = etNotes.getText().toString().trim();
    String paymentMethod = selectedPaymentMethod != null ? selectedPaymentMethod.getText().toString() : "Cash";
    double amount = Double.parseDouble(amountStr);
    String type = tvtype.getText().toString();

//     2. Get the current user's ID

    FirebaseUser currentUser = mAuth.getCurrentUser();
    Log.d("user_ref", currentUser.getUid().toString());


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

    Expense expense = new Expense(expenseId, amount, category, date, notes, type, paymentMethod, timestamp);

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