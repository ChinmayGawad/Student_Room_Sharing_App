package com.pgshare.studentroomsharingapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class Profile_Page extends AppCompatActivity {

    TextView ProfileUserName, ProfileEmailId, ProfilePhoneNo, ProfileAadharNo, ProfileGender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_page);


        ProfileUserName = findViewById(R.id.ProfileUserName);
        ProfileEmailId = findViewById(R.id.ProfileEmailId);
        ProfilePhoneNo = findViewById(R.id.ProfilePhoneNo);
        ProfileAadharNo = findViewById(R.id.ProfileAadharNo);
        ProfileGender = findViewById(R.id.ProfileGender);


        showProfile();



    }

    private void showProfile() {

        Intent intent = getIntent();

        String Profile_email = intent.getStringExtra("email");
        String Profile_name = intent.getStringExtra("name");
        String Profile_phoneNo = intent.getStringExtra("phoneNo");
        String Profile_aadhar = intent.getStringExtra("aadhar");
        String Profile_gender = intent.getStringExtra("gender");


        ProfileUserName.setText(Profile_name);
        ProfileEmailId.setText(Profile_email);
        ProfilePhoneNo.setText(Profile_phoneNo);
        ProfileAadharNo.setText(Profile_aadhar);
        ProfileGender.setText(Profile_gender);


    }
}