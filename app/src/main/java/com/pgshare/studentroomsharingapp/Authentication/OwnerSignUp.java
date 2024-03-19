package com.pgshare.studentroomsharingapp.Authentication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.pgshare.studentroomsharingapp.Adapter.Owner;
import com.pgshare.studentroomsharingapp.R;

public class OwnerSignUp extends AppCompatActivity {

    private EditText editTextOwnerEmail, editTextOwnerPassword, editTextOwnerConfirmPassword;
    private Button buttonRegisterOwner;
    ProgressBar OwnerSignUpProgressBar;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_sign_up);

        editTextOwnerEmail = findViewById(R.id.editTextOwnerEmail);
        editTextOwnerPassword = findViewById(R.id.editTextOwnerPassword);
        editTextOwnerConfirmPassword = findViewById(R.id.editTextOwnerConfirmPassword);
        buttonRegisterOwner = findViewById(R.id.buttonRegisterOwner);
        OwnerSignUpProgressBar = findViewById(R.id.OwnerSignUpProgressBar);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Owners");


        buttonRegisterOwner.setOnClickListener(v -> registerOwner());
    }

    private void registerOwner() {
        String ownerEmail = editTextOwnerEmail.getText().toString().trim();
        String ownerPassword = editTextOwnerPassword.getText().toString().trim();
        String ownerConfirmPassword = editTextOwnerConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(ownerEmail) || TextUtils.isEmpty(ownerPassword) || TextUtils.isEmpty(ownerConfirmPassword)) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }
        if (ownerPassword.length() < 6) {
            editTextOwnerPassword.setError("Minimum length of password should be 6");
            editTextOwnerPassword.requestFocus();
            return;
        }

        if (!ownerPassword.equals(ownerConfirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }
        OwnerSignUpProgressBar.setVisibility(ProgressBar.VISIBLE);
        // Perform owner registration with Firebase Authentication
        firebaseAuth.createUserWithEmailAndPassword(ownerEmail, ownerPassword).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                // Owner registration successful, save owner details to database
                String ownerId = firebaseAuth.getCurrentUser().getUid();
                Owner owner = new Owner(ownerId, ownerEmail);
                databaseReference.child(ownerId).setValue(owner);
                sendVerificationEmail();

                Toast.makeText(this, "Owner registered successfully", Toast.LENGTH_SHORT).show();

                OwnerSignUpProgressBar.setVisibility(ProgressBar.GONE);

            } else {
                // Owner registration failed, display error message\
                OwnerSignUpProgressBar.setVisibility(ProgressBar.GONE);
                Toast.makeText(this, "Failed to register owner: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendVerificationEmail() {

        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            user.sendEmailVerification().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Email verification sent successfully
                    Toast.makeText(this, "Verification email sent, please check your email", Toast.LENGTH_SHORT).show();
                    OwnerSignUpProgressBar.setVisibility(ProgressBar.GONE);
                    // Proceed to registration details activity or login
                    startActivity(new Intent(this, RegisterOwnerDetails.class));
                    finish();
                } else {
                    // Email verification sending failed
                    OwnerSignUpProgressBar.setVisibility(ProgressBar.GONE);
                    Toast.makeText(this, "Failed to send verification email: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
