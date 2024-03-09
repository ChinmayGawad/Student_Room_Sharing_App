package com.pgshare.studentroomsharingapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterUserDetails extends AppCompatActivity {

    private EditText editTextName, editTextPhone, editTextAdharCard;
    private RadioGroup radioGroupGender;
    private RadioButton radioButtonMale, radioButtonFemale;
    private Button registerBtn;
    private ProgressBar progressBar;

    private FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user_details);

        // Initialize Firebase
        database = FirebaseDatabase.getInstance();

        // Find views
        editTextName = findViewById(R.id.editTextName);
        editTextPhone = findViewById(R.id.editTextPhone);
        editTextAdharCard = findViewById(R.id.editTextAadharCard);
        registerBtn = findViewById(R.id.buttonRegister);
        radioGroupGender = findViewById(R.id.radioGroupGender);
        radioButtonMale = findViewById(R.id.radioButtonMale);
        radioButtonFemale = findViewById(R.id.radioButtonFemale);
        progressBar = findViewById(R.id.RegisterProgressBar);

        // Button click listener
        registerBtn.setOnClickListener(v -> onRegisterBtnClick());
    }

    private void onRegisterBtnClick() {
        String name = editTextName.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();
        String gender = getSelectedGender();
        String adharCard = editTextAdharCard.getText().toString().trim();

        // Input validation
        if (isValidInput(name, phone, gender, adharCard)) {
            // Show progress bar
            progressBar.setVisibility(View.VISIBLE);

            // Obtain the current user's UID
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            // Create a new user node in the database
            DatabaseReference usersRef = database.getReference("Users").child(userId);

            // Store user details directly
            usersRef.child("name").setValue(name);
            usersRef.child("phone").setValue(phone);
            usersRef.child("gender").setValue(gender);
            usersRef.child("adharCard").setValue(adharCard);

            // Add more details as needed

            // Hide progress bar
            progressBar.setVisibility(View.GONE);
            Toast.makeText(RegisterUserDetails.this, "Registration successful!", Toast.LENGTH_SHORT).show();

            // Logic for starting the next activity or navigation
            Intent intent = new Intent(RegisterUserDetails.this, Login.class);
            startActivity(intent);
            finish();
        }
    }

    private String getSelectedGender() {
        int selectedId = radioGroupGender.getCheckedRadioButtonId();
        if (selectedId == R.id.radioButtonMale) {
            return "Male";
        } else if (selectedId == R.id.radioButtonFemale) {
            return "Female";
        } else {
            Toast.makeText(this, "Please select your gender.", Toast.LENGTH_SHORT).show();
            return "";
        }
    }

    private boolean isValidInput(String name, String phone, String gender, String adharCard) {
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Please enter your name.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (TextUtils.isEmpty(phone)) {
            Toast.makeText(this, "Please enter your phone number.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (gender.isEmpty()) {
            Toast.makeText(this, "Please select your gender.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (TextUtils.isEmpty(adharCard)) {
            Toast.makeText(this, "Please enter your aadhar card number.", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }
}
