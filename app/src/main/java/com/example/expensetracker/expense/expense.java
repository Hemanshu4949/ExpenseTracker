package com.example.expensetracker.expense;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensetracker.Adapter.TransactionAdapter;
import com.example.expensetracker.Model.Expense;
import com.example.expensetracker.R;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;

public class expense extends Fragment {

    // --- UI Components ---
    private TextView tvGreeting;
    private TextView tvTotalAmount;
    private TextView tvIncome;
    private TextView tvExpense;
    private MaterialButton btnAddExpense;
    private RecyclerView rvTransactions;

    // --- Firebase ---
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private FirebaseUser currentUser;
    private ValueEventListener balanceListener;
    private TransactionAdapter adapter; // The adapter for the RecyclerView
// To listen for data changes
    // Add your RecyclerView adapter here (e.g., FirebaseRecyclerAdapter)
    // private TransactionAdapter adapter;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_expense, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // --- Find all component references ---
        tvGreeting = view.findViewById(R.id.tv_greeting);
        tvTotalAmount = view.findViewById(R.id.tv_total_amount);
        tvIncome = view.findViewById(R.id.tv_income);
        tvExpense = view.findViewById(R.id.tv_expense);
        btnAddExpense = view.findViewById(R.id.btn_add_expense);
        rvTransactions = view.findViewById(R.id.rv_transactions);

        // --- Initialize Firebase ---
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        // Make sure to check if currentUser is null, though the HomeActivity gatekeeper should prevent this.
        if (currentUser != null) {
            mDatabase = FirebaseDatabase.getInstance().getReference().child("expenses").child(currentUser.getUid());
        }

        // --- Setup Logic ---
        setupRecyclerView();
        loadGreetingAndProfile();
        setupClickListeners();

    }
    private void setupClickListeners() {
        // Set listener for the "+" button
        btnAddExpense.setOnClickListener(v -> {
            try {
                // Navigate to the AddExpenseFragment
                // This ID must be defined in your res/navigation/nav_graph.xml
                Navigation.findNavController(v).navigate(R.id.action_expenseFragment_to_add_expense_Fragment);
            } catch (Exception e) {
                // This can happen if the action ID is wrong in nav_graph.xml
                Toast.makeText(getContext(), "Navigation to Add Expense failed.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadGreetingAndProfile() {
        if (currentUser != null) {
            String name = currentUser.getDisplayName();
            if (name != null && !name.isEmpty()) {
                // Show a personalized greeting
                tvGreeting.setText("Welcome back, " + name.split(" ")[0] + " 👋");
            } else {
                tvGreeting.setText("Welcome back 👋");
            }
            // You would load a profile image here using Glide or Picasso
//             Glide.with(this).load(currentUser.getPhotoUrl()).into(ivProfile);
        }
    }

    private void setupRecyclerView() {
        rvTransactions.setLayoutManager(new LinearLayoutManager(getContext()));
        // Call the method that loads all data from Firebase
        loadFirebaseData();
    }

    private void loadFirebaseData() {
        if (mDatabase == null) {
            // This would mean currentUser was null, which shouldn't happen
            return;
        }

        // --- 1. Load Totals (Income, Expense, Balance) ---
        // We add a persistent listener to the user's expenses node
        balanceListener = mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                double totalIncome = 0;
                double totalExpense = 0;

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Expense expense = dataSnapshot.getValue(Expense.class);
                    if (expense != null) {
                        // --- Use the 'type' field for calculation ---
                        if ("credited".equalsIgnoreCase(expense.getType())) {
                            totalIncome += expense.getAmount();
                        } else if ("spend".equalsIgnoreCase(expense.getType())) {
                            totalExpense += expense.getAmount();
                        }
                    }
                }
                double totalBalance = totalIncome - totalExpense;

                // Update the UI in the top card
                tvTotalAmount.setText(String.format(Locale.US, "₹%.2f", totalBalance));
                tvIncome.setText(String.format(Locale.US, "₹%.2f", totalIncome));
                tvExpense.setText(String.format(Locale.US, "₹%.2f", totalExpense));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load balance.", Toast.LENGTH_SHORT).show();
            }
        });

        // --- 2. Load Recent Transactions List ---
        // Create a query to get the most recent transactions
        // (This assumes "timestamp" is a negative value to sort descending)
        // If "timestamp" is a positive value, you would use .limitToLast(50)
        Query query = mDatabase.orderByChild("timestamp");

        FirebaseRecyclerOptions<Expense> options =
                new FirebaseRecyclerOptions.Builder<Expense>()
                        .setQuery(query, Expense.class)
                        .build();

        // Create the adapter
        adapter = new TransactionAdapter(options, getContext());
        rvTransactions.setAdapter(adapter);
    }


    @Override
    public void onStart() {
        super.onStart();
        // Start listening for changes in the adapter when the fragment is visible
        if (adapter != null) {
            adapter.startListening();
        }
        // Re-attach the balance listener if it was removed in onStop
        if (balanceListener == null && mDatabase != null) {
            loadFirebaseData();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        // Stop listening when the fragment is not visible to save resources
        if (adapter != null) {
            adapter.stopListening();
        }
        // Remove the balance listener
        if (balanceListener != null && mDatabase != null) {
            mDatabase.removeEventListener(balanceListener);
            balanceListener = null; // Set to null so it can be re-added in onStart
        }
    }

}