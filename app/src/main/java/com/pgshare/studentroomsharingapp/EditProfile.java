package com.pgshare.studentroomsharingapp;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EditProfile extends AppCompatActivity {

    EditText nameEditText, phoneEditText, genderEditText;
    Button saveButton;
    ProgressBar profileProgressBar;

    // Firebase authentication instance
    FirebaseAuth mAuth;
    // Firebase Realtime Database instance
    FirebaseDatabase mDatabase;
    // Reference to the current user's data node in the database
    DatabaseReference mUserReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.C_color)));

        nameEditText = findViewById(R.id.editTextText);
        phoneEditText = findViewById(R.id.editTextText2);
        genderEditText = findViewById(R.id.editTextText4);
        saveButton = findViewById(R.id.button);
        profileProgressBar = findViewById(R.id.ProfileProgressBar);

        // Initialize Firebase instances
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            mUserReference = mDatabase.getReference().child("Users").child(currentUser.getUid());
        }

        saveButton.setOnClickListener(v -> {
            // Perform save operation here
            saveProfile();
        });
    }

    private void saveProfile() {
        // Retrieve updated profile information from EditText fields
        String newName = nameEditText.getText().toString();
        String newPhone = phoneEditText.getText().toString();
        String newGender = genderEditText.getText().toString();

        profileProgressBar.setVisibility(View.VISIBLE);
        // Update profile in the Firebase Realtime Database
        if (mUserReference != null) {
            mUserReference.child("name").setValue(newName);
            mUserReference.child("phone").setValue(newPhone);
            mUserReference.child("gender").setValue(newGender);
            Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show();

            nameEditText.getText().clear();
            phoneEditText.getText().clear();
            genderEditText.getText().clear();

            profileProgressBar.setVisibility(View.GONE);
            startActivity(new Intent(EditProfile.this, Profile_Page.class));
            finish();


        } else {
            Toast.makeText(this, "Failed to update profile.", Toast.LENGTH_SHORT).show();
        }
    }
}
