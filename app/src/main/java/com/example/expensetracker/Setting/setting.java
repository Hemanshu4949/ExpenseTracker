package com.example.expensetracker.Setting;

import static android.view.View.VISIBLE;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
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
import com.example.expensetracker.Adapter.FriendRequestAdapter;
import com.example.expensetracker.Adapter.UserSearchAdapter;
import com.example.expensetracker.MainActivity;
import com.example.expensetracker.Model.FriendRequest;
import com.example.expensetracker.Model.User;
import com.example.expensetracker.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class setting extends Fragment {
    private TextView tvUserName;
    private TextView tvUserEmail;
    private TextView tvEditProfile;
    private ImageView ivProfileImage;
    private MaterialButton btnLogout;
    private TextView tvNoRequests;

    // --- FLAG Variable ---

    private Boolean friend_aciton = false;
    FriendRequestAdapter adapter ;
    private FriendRequest action_user;


    // --- Firebase ---
    private FirebaseAuth mAuth;
    private FirebaseFirestore db; // Firestore for user search


    // --- New Friend UI ---
    private ImageView cardAddFriend; // The card to click for adding a friend
    private RecyclerView rvFriendRequests;
    private List<FriendRequest> requests = new ArrayList<>();// List to show incoming requests


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
        tvNoRequests = view.findViewById(R.id.Norequest);


        // Find new Friend components
        cardAddFriend = view.findViewById(R.id.add_friend);
        rvFriendRequests = view.findViewById(R.id.rv_friend_requests);


        // --- Initialize Firebase Auth ---
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();


        // --- Load User Data ---
        loadUserData();

        // --- Setup Listeners ---
        setupClickListeners();

        // --- loading friend request ---
        loadFriendRequests();

        rvFriendRequests.setLayoutManager(new LinearLayoutManager(getContext()));
        Log.d("loadfriendrequests" , "phase- remove1");

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
//        cardAddFriend.setOnClickListener(v -> showAddFriendDialog());
        // Open Custom Search Dialog when "Add Friend" card is clicked
        if (cardAddFriend != null) {
            cardAddFriend.setOnClickListener(v -> showCustomSearchDialog());
        }
    }


    private void showCustomSearchDialog() {
        // 1. Inflate the custom layout for the dialog
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.dialog_search_friend, null);

        EditText etSearch = dialogView.findViewById(R.id.etSearchFriend);
        RecyclerView rvResults = dialogView.findViewById(R.id.rvSearchResults);
        TextView tvNoResults = dialogView.findViewById(R.id.tvNoResults);

        // 2. Setup RecyclerView inside the dialog
        rvResults.setLayoutManager(new LinearLayoutManager(getContext()));
        List<User> searchResults = new ArrayList<>();

        // Create the adapter with a click listener for the "Add" button on each user item
        UserSearchAdapter searchAdapter = new UserSearchAdapter(searchResults,false ,user -> {
            // Logic when "Add" is clicked for a specific user
            sendFriendRequest(user);
        });
        rvResults.setAdapter(searchAdapter);

        // 3. Create and Show the Dialog
        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .setCancelable(true)
                .create();

        // Make background transparent if needed to show rounded corners of custom layout
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        // 4. Add Search Logic (TextWatcher) to filter users as you type
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                // Start searching only after 3 characters to avoid too many queries
                if (query.length() > 2) {
                    Log.d("input_search_friend", query.toString());
                    performSearch(query, searchResults, searchAdapter, rvResults, tvNoResults);
                } else {
                    // Clear results if query is too short
                    searchResults.clear();
                    searchAdapter.notifyDataSetChanged();
                    rvResults.setVisibility(View.GONE);
                    tvNoResults.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        dialog.show();
    }

    private void performSearch(String emailQuery, List<User> results, UserSearchAdapter adapter, RecyclerView rv, TextView tvNoResults) {
        // Search logic: Find users where email starts with the query
        // Using Firestore's range filter pattern for prefix search
        db.collection("users")
                .whereGreaterThanOrEqualTo("email", emailQuery)
                .whereLessThan("email", emailQuery + "\uf8ff")
                .limit(5) // Limit to 5 results
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    results.clear();
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                            // Don't include the current user in search results
                            if (mAuth.getUid() != null && !doc.getId().equals(mAuth.getUid())) {
                                String uid = doc.getId();
                                String name = doc.getString("name");
                                String email = doc.getString("email");
                                // Add user to list if name/email are valid
                                if (name != null && email != null) {
                                    results.add(new User(uid, name, email));
                                }
                            }
                        }

                        // Update visibility based on results
                        if (results.isEmpty()) {
                            rv.setVisibility(View.GONE);
                            tvNoResults.setVisibility(VISIBLE);

                        } else {
                            rv.setVisibility(VISIBLE);
                            tvNoResults.setVisibility(View.GONE);
                        }
                    } else {
                        rv.setVisibility(View.GONE);
                        tvNoResults.setVisibility(VISIBLE);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Search failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void sendFriendRequest(User friend) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        String currentUserId = currentUser.getUid();
        String friendId = friend.getUid();

        // 1. Create a request object/map
        Map<String, Object> request = new HashMap<>();
        request.put("senderId", currentUserId);
        request.put("senderName", currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "Unknown");
        request.put("senderEmail", currentUser.getEmail());
        request.put("status", "pending");
        request.put("timestamp", FieldValue.serverTimestamp());

        // 2. Add to the friend's "friend_requests" subcollection
        db.collection("users")
                .document(friendId)
                .collection("friend_requests")
                .document(currentUserId) // Use sender's ID as doc ID to prevent duplicates
                .set(request)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Friend request sent to " + friend.getName(), Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to send request: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadFriendRequests() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;
        Log.d("loadfriendrequests" , "phase2");

        // Reference to the user's friend_requests subcollection
        // Path: users/{userId}/friend_requests
        db.collection("users")
                .document(currentUser.getUid())
                .collection("friend_requests")
                .whereEqualTo("status", "pending") // Only get pending requests
                .orderBy("timestamp", Query.Direction.DESCENDING) // Show newest first
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.d("friend_request " , Objects.requireNonNull(e.getMessage()));
                        return;
                    }
                    Log.d("loadfriendrequests" , "phase3");
                    tvNoRequests.setVisibility(View.GONE);


                    if (snapshots != null) {
                        for (DocumentSnapshot doc : snapshots) {
                            // Convert the document to a FriendRequest object
                            FriendRequest request = doc.toObject(FriendRequest.class);
                            if (request != null) {
                                action_user = request;
                                requests.add(request);
                            }
                        }
                    }

                    // Update the adapter
                    adapter = new FriendRequestAdapter(requests, new FriendRequestAdapter.OnRequestActionListener() {
                        @Override
                        public void onAccept(FriendRequest request) {
                            acceptFriendRequest(request);
                            friend_aciton = true;
                            remove_action();

                        }

                        @Override
                        public void onDecline(FriendRequest request) {
                            declineFriendRequest(request);
                            friend_aciton = true;
                            remove_action();
                        }
                    });
                    rvFriendRequests.setAdapter(adapter);
                    Log.d("listsize" , String.valueOf(requests.size()));

                    // Optional: Show/Hide a "No Requests" message based on list size

                    if (!requests.isEmpty()) {
                        Log.d("loadfriendrequests" , "phase4");
                        rvFriendRequests.setVisibility(VISIBLE);
                        tvNoRequests.setVisibility(View.GONE);

                    } else {
                        Log.d("loadfriendrequests" , "phase5");
                        rvFriendRequests.setVisibility(View.GONE);
                        tvNoRequests.setVisibility(VISIBLE);
                    }
                });
    }

    private void acceptFriendRequest(FriendRequest request) {
        String currentUserId = mAuth.getCurrentUser().getUid();
        String friendId = request.getSenderId();

        // 1. Add friend to CURRENT USER'S friends list
        Map<String, Object> friendData = new HashMap<>();
        friendData.put("uid", friendId);
        friendData.put("name", request.getSenderName());
        friendData.put("email", request.getSenderEmail());


        db.collection("users").document(currentUserId)
                .collection("friends").document(friendId).set(friendData);

        // 2. Add current user to FRIEND'S friends list (reciprocal)
        // We need current user's details for this.
        String myName = tvUserName.getText().toString(); // Getting from UI for simplicity
        String myEmail = tvUserEmail.getText().toString();

        Map<String, Object> myData = new HashMap<>();
        myData.put("uid", currentUserId);
        myData.put("name", myName);
        myData.put("email", myEmail);

        db.collection("users").document(friendId)
                .collection("friends").document(currentUserId).set(myData);

        // 3. Remove the request document
        db.collection("users").document(currentUserId)
                .collection("friend_requests").document(friendId).delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Friend Added!", Toast.LENGTH_SHORT).show();


                });
        loadFriendRequests();
    }

    private void declineFriendRequest(FriendRequest request) {
        String currentUserId = mAuth.getCurrentUser().getUid();
        // Simply delete the request document
        db.collection("users").document(currentUserId)
                .collection("friend_requests").document(request.getSenderId()).delete()
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Request Declined", Toast.LENGTH_SHORT).show());
    }


private void remove_action()
{
    if (action_user != null && friend_aciton) {
        Log.d("loadfriendrequests" , "phase- remove2");
        int position = requests.indexOf(action_user);
        if (position != -1) {
            requests.remove(position);
            adapter.notifyItemRemoved(position);
            friend_aciton = false;
        }
        if(requests.size() == 0 || requests == null )
        {
            tvNoRequests.setVisibility(View.VISIBLE);
        }
        else
        {
            tvNoRequests.setVisibility(View.GONE);
        }
    }
}

//    private void showAddFriendDialog() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
//        builder.setView(R.layout.dialog_search_friend);
//        builder.setTitle("Add Friend");
//        builder.setMessage("Enter your friend's email address to search:");
//
//        final EditText input = new EditText(getContext());
//        input.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
//        builder.setView(input);
//
//        builder.setPositiveButton("Search & Add", (dialog, which) -> {
//            String friendEmail = input.getText().toString().trim();
//            if (!TextUtils.isEmpty(friendEmail)) {
//                searchAndAddFriend(friendEmail);
//            } else {
//                Toast.makeText(getContext(), "Please enter an email.", Toast.LENGTH_SHORT).show();
//            }
//        });
//        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
//
//        builder.show();
//    }
//
//    // --- Search Logic ---
//    private void searchAndAddFriend(String email) {
//        // Query the "users" collection in Firestore where "email" matches
//        db.collection("users")
//                .whereEqualTo("email", email)
//                .get()
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        QuerySnapshot documents = task.getResult();
//                        if (documents != null && !documents.isEmpty()) {
//                            // User found!
//                            String friendId = documents.getDocuments().get(0).getId();
//                            String friendName = documents.getDocuments().get(0).getString("name");
//
//                            // Send request logic would go here
//                            Toast.makeText(getContext(), "Found user: " + friendName + ". Request sent!", Toast.LENGTH_LONG).show();
//
//                        } else {
//                            Toast.makeText(getContext(), "User not found with that email.", Toast.LENGTH_SHORT).show();
//                        }
//                    } else {
//                        Toast.makeText(getContext(), "Error searching: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
//                    }
//                });
//    }
}