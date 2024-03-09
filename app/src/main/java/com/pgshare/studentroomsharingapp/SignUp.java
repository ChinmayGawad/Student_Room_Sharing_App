package com.pgshare.studentroomsharingapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class SignUp extends AppCompatActivity {

    private static final String TAG = "SignUpActivity";

    private TextInputLayout emailLayout, passwordLayout, confirmPasswordLayout;
    private EditText editTextEmail, passwordEditText, editTextConfirmPassword;
    private Button buttonNext;
    private ProgressBar progressBar;
    private DatabaseReference userRef;
    private FirebaseDatabase database;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Initialize Firebase
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();


        // Find views
        emailLayout = findViewById(R.id.EmailLayout);
        passwordLayout = findViewById(R.id.PasswordLayout);
        confirmPasswordLayout = findViewById(R.id.ConfirmPasswordLayout);

        editTextEmail = findViewById(R.id.editTextEmail);
        passwordEditText = findViewById(R.id.passwordEditText);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);

        buttonNext = findViewById(R.id.buttonNext);
        progressBar = findViewById(R.id.SignUpProgressBar);

        // Button click listener
        buttonNext.setOnClickListener(v -> onRegisterBtnClick());
    }

    private void onRegisterBtnClick() {
        String email = editTextEmail.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();

        // Input validation
        if (isValidInput(email, password, confirmPassword)) {
            // Show progress bar
            progressBar.setVisibility(View.VISIBLE);

            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // User registration success
                            String userId = auth.getCurrentUser().getUid();


                            // Now, add user data to the Realtime Database
                            userRef = database.getReference("Users").child(userId);

                            // Replace "users" with the desired node name
                            userRef.child("email").setValue(email);


                            // You can add more data if needed, such as name, etc.
                            // database.getReference("users").child(userId).child("name").setValue(userName);

                            // Hide progress bar
                            progressBar.setVisibility(View.GONE);
                            // Navigate to the next screen
                            Intent intent = new Intent(SignUp.this, RegisterUserDetails.class);
                            startActivity(intent);

                        } else {
                            // User registration failed
                            // Handle the failure, display an error message, etc.
                            // You can check task.getException().getMessage() for the error message.
                            Objects.requireNonNull(task.getException()).getMessage();
                            // Hide progress bar
                            progressBar.setVisibility(View.GONE);
                        }
                    });

        }
    }

    private boolean isValidInput(String email, String password, String confirmPassword) {
        boolean valid = true;

        // Check if email is valid
        if (TextUtils.isEmpty(email) || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError("Invalid email address");
            valid = false;
        } else {
            emailLayout.setError(null);
        }

        // Check if password is empty or meets minimum length
        if (TextUtils.isEmpty(password) || password.length() < 6) {
            passwordLayout.setError("Password must be at least 6 characters");
            valid = false;
        } else {
            passwordLayout.setError(null);
        }

        // Check if passwords match
        if (!password.equals(confirmPassword)) {
            confirmPasswordLayout.setError("Passwords do not match");
            valid = false;
        } else {
            confirmPasswordLayout.setError(null);
        }

        return valid;
    }
}
