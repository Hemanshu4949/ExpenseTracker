package com.example.expensetracker.Setting;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.expensetracker.MainActivity;
import com.example.expensetracker.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class setting extends Fragment {
    private TextView tvUserName;
    private TextView tvUserEmail;
    private TextView tvEditProfile;
    private ImageView ivProfileImage;
    private MaterialButton btnLogout;

    // --- Firebase ---
    private FirebaseAuth mAuth;
    private FirebaseFirestore db; // Firestore for user search


    // --- New Friend UI ---
    private View cardAddFriend; // The card to click for adding a friend
    private RecyclerView rvFriendRequests; // List to show incoming requests


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_setting, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // --- Find UI Components ---
        // Ensure these IDs match your fragment_profile.xml
        tvUserName = view.findViewById(R.id.tv_user_name);
        tvUserEmail = view.findViewById(R.id.tv_user_email);
        ivProfileImage = view.findViewById(R.id.iv_profile_image);
        btnLogout = view.findViewById(R.id.btn_logout);


        // Find new Friend components
        cardAddFriend = view.findViewById(R.id.card_add_friend);
        rvFriendRequests = view.findViewById(R.id.rv_friend_requests);


        // --- Initialize Firebase Auth ---
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();


        // --- Load User Data ---
        loadUserData();

        // --- Setup Listeners ---
        setupClickListeners();

        rvFriendRequests.setLayoutManager(new LinearLayoutManager(getContext()));

    }

    private void loadUserData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // Set Email
            String email = user.getEmail();
            tvUserEmail.setText(email != null ? email : "No Email");

            // Set Name
            String name = user.getDisplayName();
            if (name != null && !name.isEmpty()) {
                tvUserName.setText(name);
            } else {
                // Fallback: Use part of email before '@' if display name is not set
                // Or just show "User"
                if (email != null && email.contains("@")) {
                    String fallbackName = email.split("@")[0];
                    // Capitalize first letter
                    fallbackName = fallbackName.substring(0, 1).toUpperCase() + fallbackName.substring(1);
                    tvUserName.setText(fallbackName);
                } else {
                    tvUserName.setText("User");
                }
            }

            // Set Profile Image if available (using Glide or Picasso is recommended)
            // For now, we use the default icon from XML, or you can load from URL

            if (user.getPhotoUrl() != null) {
//                 Use a library like Glide to load the image
                 Glide.with(this).load(user.getPhotoUrl()).into(ivProfileImage);
            }
        }
    }

    private void setupClickListeners() {
        // Logout Button Logic
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();

            // Navigate back to Login Activity
            Intent intent = new Intent(getActivity(), MainActivity.class);
            // Clear back stack so user can't go back to profile after logging out
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            // Finish the current activity if it exists
            if (getActivity() != null) {
                getActivity().finish();
            }
        });

        // Edit Profile Click (Placeholder)
        View.OnClickListener editListener = v -> {
            Toast.makeText(getContext(), "Edit Profile clicked", Toast.LENGTH_SHORT).show();
            // Navigate to an Edit Profile Fragment or Dialog here if you implement it
        };

        ivProfileImage.setOnClickListener(editListener);
    }

    private void showAddFriendDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Add Friend");
        builder.setMessage("Enter your friend's email address to search:");

        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        builder.setView(input);

        builder.setPositiveButton("Search & Add", (dialog, which) -> {
            String friendEmail = input.getText().toString().trim();
            if (!TextUtils.isEmpty(friendEmail)) {
                searchAndAddFriend(friendEmail);
            } else {
                Toast.makeText(getContext(), "Please enter an email.", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    // --- Search Logic ---
    private void searchAndAddFriend(String email) {
        // Query the "users" collection in Firestore where "email" matches
        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot documents = task.getResult();
                        if (documents != null && !documents.isEmpty()) {
                            // User found!
                            String friendId = documents.getDocuments().get(0).getId();
                            String friendName = documents.getDocuments().get(0).getString("name");

                            // Send request logic would go here
                            Toast.makeText(getContext(), "Found user: " + friendName + ". Request sent!", Toast.LENGTH_LONG).show();

                        } else {
                            Toast.makeText(getContext(), "User not found with that email.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "Error searching: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}