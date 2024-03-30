package com.pgshare.studentroomsharingapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class EditProfile extends AppCompatActivity {

    EditText  nameEditText, phoneEditText, genderEditText;
    Button saveButton;
    ProgressBar profileProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        nameEditText = findViewById(R.id.editTextText);
        phoneEditText = findViewById(R.id.editTextText2);
        genderEditText = findViewById(R.id.editTextText4);
        saveButton = findViewById(R.id.button);
        profileProgressBar = findViewById(R.id.ProfileProgressBar);

        saveButton.setOnClickListener(v -> {
            // Perform save operation here
            saveProfile();
        });
    }

    private void saveProfile() {
        // Placeholder method for saving profile
        // You can implement your saving logic here
        // For demonstration, just showing a toast message
        Toast.makeText(this, "Profile saved!", Toast.LENGTH_SHORT).show();
    }
}
