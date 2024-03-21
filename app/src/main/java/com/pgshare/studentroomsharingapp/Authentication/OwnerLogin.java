package com.pgshare.studentroomsharingapp.Authentication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pgshare.studentroomsharingapp.Add_Room;
import com.pgshare.studentroomsharingapp.R;

public class OwnerLogin extends AppCompatActivity {

    private EditText editTextOwnerEmail, editTextOwnerPassword;
    private ProgressBar OwnerLoginprogressBar;

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
        TextView textViewCreateAccount = findViewById(R.id.textViewCreateAccount);
        TextView textViewForgetPassword = findViewById(R.id.textViewForgetPassword);
        Button buttonLoginOwner = findViewById(R.id.buttonLoginOwner);

        OwnerLoginprogressBar = findViewById(R.id.OwnerLoginProgressBar);

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

        OwnerLoginprogressBar.setVisibility(ProgressBar.VISIBLE);
        // Authenticate user using Firebase Authentication
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        FirebaseUser user = firebaseAuth.getCurrentUser();

                        // Retrieve user type from the database
                        DatabaseReference ownerRef   = FirebaseDatabase.getInstance().getReference("Owners").child(user.getUid());
                        ownerRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    String userType = snapshot.child("userType").getValue(String.class);
                                    Log.d("UserType", userType);
                                    if ("Owner".equals(userType)) {
                                        // User is an owner, proceed to owner-specific functionality
                                        startActivity(new Intent(OwnerLogin.this, Add_Room.class));
                                        finish();
                                    } else {
                                        // User is not authorized as an owner
                                        Toast.makeText(OwnerLogin.this, "You are not authorized as an owner", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    // User data does not exist in the database
                                    Toast.makeText(OwnerLogin.this, "User data not found", Toast.LENGTH_SHORT).show();
                                }
                                OwnerLoginprogressBar.setVisibility(ProgressBar.GONE);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(OwnerLogin.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                OwnerLoginprogressBar.setVisibility(ProgressBar.GONE);
                            }

                        });

                    } else {
                        // If sign in fails, display a message to the user.
                        OwnerLoginprogressBar.setVisibility(ProgressBar.GONE);
                        Toast.makeText(OwnerLogin.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }



}
