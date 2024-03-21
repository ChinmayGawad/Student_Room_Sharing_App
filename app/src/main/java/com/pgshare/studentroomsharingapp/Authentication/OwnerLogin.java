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

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.pgshare.studentroomsharingapp.Add_Room;
import com.pgshare.studentroomsharingapp.Display_Room;
import com.pgshare.studentroomsharingapp.R;

public class OwnerLogin extends AppCompatActivity {

    private EditText editTextOwnerEmail, editTextOwnerPassword;
    private TextView textViewCreateAccount, textViewForgetPassword;
    private Button buttonLoginOwner;
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
        textViewCreateAccount = findViewById(R.id.textViewCreateAccount);
        textViewForgetPassword = findViewById(R.id.textViewForgetPassword);
        buttonLoginOwner = findViewById(R.id.buttonLoginOwner);

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
        String userType = getIntent().getStringExtra("userType");
        Log.d("UserType", "UserType: " + userType);

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

        if (password.length() < 6) {
            editTextOwnerPassword.setError("Minimum length of password should be 6");
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
                        /*Intent intent = new Intent(OwnerLogin.this, Add_Room.class);
                        intent.putExtra("userType", "Owner");
                        startActivity(intent);
                        finish();*/
                        if ("Owner".equals(userType)) {
                            // User is logging in as an owner, navigate to Add_Room activity
                            Intent intent = new Intent(OwnerLogin.this, Add_Room.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(this, "Please Login With  Owner Credentials", Toast.LENGTH_SHORT).show();
                            OwnerLoginprogressBar.setVisibility(ProgressBar.GONE);
                        }

                    } else {
                        // If sign in fails, display a message to the user.
                        OwnerLoginprogressBar.setVisibility(ProgressBar.GONE);
                        Toast.makeText(OwnerLogin.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (firebaseAuth.getCurrentUser() != null) {
            Intent intent = new Intent(OwnerLogin.this, Display_Room.class);
            startActivity(intent);
            finish();
        }
        else {
            Toast.makeText(this, "You Can Login Now", Toast.LENGTH_SHORT).show();
        }
    }
}
