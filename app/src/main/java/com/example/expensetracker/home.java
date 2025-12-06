package com.example.expensetracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.expensetracker.Authentication.account;
import com.example.expensetracker.Authentication.login;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

public class home extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        // --- Setup for Bottom Navigation ---
        // Find the NavHostFragment which will host our different screen fragments.
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            // Get the NavController from the NavHostFragment.
            NavController navController = navHostFragment.getNavController();

            // Find the BottomNavigationView in our layout.
            BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

            // Link the NavController with the BottomNavigationView.
            // This line automatically handles switching fragments when a bottom tab is clicked.
            NavigationUI.setupWithNavController(bottomNav, navController);
        }
    }
}