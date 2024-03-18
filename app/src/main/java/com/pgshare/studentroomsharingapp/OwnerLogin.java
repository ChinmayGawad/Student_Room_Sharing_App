package com.pgshare.studentroomsharingapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class OwnerLogin extends AppCompatActivity {

    private EditText editTextOwnerEmail, editTextOwnerPassword;
    private TextView textViewCreateAccount, textViewForgetPassword;
    private Button buttonLoginOwner;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_login);

        // Initialize Firebase Authentication instance
        firebaseAuth = FirebaseAuth.getInstance();

        // Initialize views
        editTextOwnerEmail = findViewById(R.id.editTextOwnerEmail);
        editTextOwnerPassword = findViewById(R.id.editTextOwnerPassword);
        textViewCreateAccount = findViewById(R.id.textViewCreateAccount);
        textViewForgetPassword = findViewById(R.id.textViewForgetPassword);
        buttonLoginOwner = findViewById(R.id.buttonLoginOwner);

        // Set onClickListener for "Create Account" TextView
        textViewCreateAccount.setOnClickListener(v -> {
            // Handle click event for creating an account
            startActivity(new Intent(OwnerLogin.this, OwnerSignUp.class));
        });

        // Set onClickListener for "Forget Password" TextView
        textViewForgetPassword.setOnClickListener(v -> {
            // Handle click event for forget password
            startActivity(new Intent(OwnerLogin.this, ForgetPassword.class));
        });

        // Set onClickListener for login button
        buttonLoginOwner.setOnClickListener(v -> {
            // Perform login operation
            loginOwner();
        });
    }

    private void loginOwner() {
        String email = editTextOwnerEmail.getText().toString().trim();
        String password = editTextOwnerPassword.getText().toString().trim();

        // Validate email and password
        if (TextUtils.isEmpty(email)) {
            editTextOwnerEmail.setError("Email is required");
            editTextOwnerEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextOwnerEmail.setError("Enter a valid email");
            editTextOwnerEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            editTextOwnerPassword.setError("Password is required");
            editTextOwnerPassword.requestFocus();
            return;
        }

        // Authenticate user using Firebase Authentication
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        Toast.makeText(OwnerLogin.this, "Login successful", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(OwnerLogin.this, Add_Room.class));
                        finish();
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(OwnerLogin.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
