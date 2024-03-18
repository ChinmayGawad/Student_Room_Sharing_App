package com.pgshare.studentroomsharingapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.pgshare.studentroomsharingapp.Adapter.Owner;

public class OwnerSignUp extends AppCompatActivity {

    private EditText editTextOwnerName, editTextOwnerEmail, editTextOwnerPhone, editTextOwnerPassword, editTextOwnerConfirmPassword;
    private ImageView imageViewNOCDocument;
    private Button buttonRegisterOwner;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_sign_up);

        editTextOwnerName = findViewById(R.id.editTextOwnerName);
        editTextOwnerEmail = findViewById(R.id.editTextOwnerEmail);
        editTextOwnerPhone = findViewById(R.id.editTextOwnerPhone);
        editTextOwnerPassword = findViewById(R.id.editTextOwnerPassword);
        editTextOwnerConfirmPassword = findViewById(R.id.editTextOwnerConfirmPassword);
        imageViewNOCDocument = findViewById(R.id.NOCDocument);
        buttonRegisterOwner = findViewById(R.id.buttonRegisterOwner);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Owners");

        imageViewNOCDocument.setOnClickListener(v -> {
            // Add logic to select NOC document
        });

        buttonRegisterOwner.setOnClickListener(v -> registerOwner());
    }

    private void registerOwner() {
        String ownerName = editTextOwnerName.getText().toString().trim();
        String ownerEmail = editTextOwnerEmail.getText().toString().trim();
        String ownerPhone = editTextOwnerPhone.getText().toString().trim();
        String ownerPassword = editTextOwnerPassword.getText().toString().trim();
        String ownerConfirmPassword = editTextOwnerConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(ownerName) || TextUtils.isEmpty(ownerEmail) || TextUtils.isEmpty(ownerPhone) ||
                TextUtils.isEmpty(ownerPassword) || TextUtils.isEmpty(ownerConfirmPassword)) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!ownerPassword.equals(ownerConfirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        // Perform owner registration with Firebase Authentication
        firebaseAuth.createUserWithEmailAndPassword(ownerEmail, ownerPassword)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Owner registration successful, save owner details to database
                        String ownerId = firebaseAuth.getCurrentUser().getUid();
                        Owner owner = new Owner(ownerId, ownerName, ownerEmail, ownerPhone);
                        databaseReference.child(ownerId).setValue(owner);

                        Toast.makeText(this, "Owner registered successfully", Toast.LENGTH_SHORT).show();

                        // Navigate to owner dashboard or login screen
                        startActivity(new Intent(this, OwnerLogin.class));
                        finish();
                    } else {
                        // Owner registration failed, display error message
                        Toast.makeText(this, "Failed to register owner: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
