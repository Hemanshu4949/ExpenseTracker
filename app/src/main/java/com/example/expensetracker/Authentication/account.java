package com.example.expensetracker.Authentication;

import static org.chromium.net.httpflags.HttpFlagsLoader.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;

import com.example.expensetracker.MainActivity;
import com.example.expensetracker.R;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore; // Import Firestore
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class account extends AppCompatActivity {

    private TextInputEditText etName, etEmail, etPassword, etConfirmPassword;
    ImageButton back_button;
    MaterialButton sign_in_google;
    private FirebaseAuth mAuth;
    CredentialManager credentialManager;
    private FirebaseFirestore db; // Declare Firestore instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        back_button = findViewById(R.id.btnBack);
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        MaterialButton createAccountButton = findViewById(R.id.btnCreateAccount);
        sign_in_google = findViewById(R.id.signin_with_google_register);
        credentialManager = CredentialManager.create(this);

        sign_in_google.setOnClickListener(v -> signInWithGoogle());

        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // The validation method now initiates the creation process
                validate_and_create();
            }
        });
    }

    private void validate_and_create() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            etName.setError("Name is required");
            etName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email");
            etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Password should be at least 6 characters long");
            etPassword.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            etConfirmPassword.setError("Confirm Password is required");
            etConfirmPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return;
        }

        // Proceed with Firebase Registration
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(account.this, task -> {
                    if (task.isSuccessful()) {
                        // Registration success
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Now save the additional details (Name) to Firestore
                            saveUserToFirestore(user.getUid(), name, email);
                        }
                    } else {
                        // Registration failed
                        Toast.makeText(account.this, "Authentication failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                        updateUI(null);
                    }
                });
    }

    /**
     * Saves user details to Firestore 'users' collection.
     */
    private void saveUserToFirestore(String userId, String name, String email) {
        // Create a Map to store user data
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("name", name);
        userMap.put("email", email);
        // You can add more fields here like "createdAt", "phone", etc.

        // Add a new document with a generated ID (or use userId as document ID)
        db.collection("users")
                .document(userId) // Using the Auth UID as the document ID is best practice
                .set(userMap)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(account.this, "Account created successfully.", Toast.LENGTH_SHORT).show();
                    FirebaseUser user = mAuth.getCurrentUser();
                    updateUI(user);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(account.this, "Failed to save user details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    // Even if Firestore fails, the Auth account was created, so we might still want to proceed
                    // or handle it as a critical error. For now, let's proceed.
                    FirebaseUser user = mAuth.getCurrentUser();
                    updateUI(user);
                });
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            Intent intent = new Intent(account.this, MainActivity.class);
            // These flags clear the activity stack, so the user can't press "back"
            // to return to the registration or login screens.
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }

    private void signInWithGoogle() {
        // Get the Web Client ID from your google-services.json file
        String serverClientId = getString(R.string.default_web_client_id);

        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(serverClientId)
                .build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        credentialManager.getCredentialAsync(
                this,
                request,
                null, // CancellationSignal
                getMainExecutor(),
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse result) {
                        GoogleIdTokenCredential credential = GoogleIdTokenCredential.createFrom(result.getCredential().getData());
                        String idToken = credential.getIdToken();
                        firebaseAuthWithGoogle(idToken);
                    }

                    @Override
                    public void onError(@NonNull GetCredentialException e) {
                        Log.e(TAG, "GetCredentialException " + e);
                        Toast.makeText(account.this, "Google Sign In Failed", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Firebase sign-in success
                        Log.d(TAG, "signInWithCredential:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        // --- NEW LOGIC: Save Google User Data to Firestore ---
                        if (user != null) {
                            saveGoogleUserToFirestore(user);
                        } else {
                            updateUI(null);
                        }                    } else {
                        // Firebase sign-in failed
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        Toast.makeText(this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
                        updateUI(null);
                    }
                });
    }
    private void saveGoogleUserToFirestore(FirebaseUser user) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> userData = new HashMap<>();
        userData.put("email", user.getEmail());

        // Google Sign-In usually provides a display name
        if (user.getDisplayName() != null) {
            userData.put("name", user.getDisplayName());
        }

        db.collection("users").document(user.getUid())
                .set(userData, SetOptions.merge()) // CRITICAL: Merge prevents overwriting existing fields
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User data saved to Firestore.");
                    updateUI(user);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error writing user document", e);
                    // Even if Firestore fails, the auth succeeded, so let the user in
                    updateUI(user);
                });
    }



}