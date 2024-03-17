package com.pgshare.studentroomsharingapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class VerificationOTP extends AppCompatActivity {

    Button sendOTP;
    EditText otp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification_otp);

        sendOTP = findViewById(R.id.SendOTP);
        otp = findViewById(R.id.OTPEditText);
         String phone = getIntent().getStringExtra("phone");

         sendVerificationPhone(phone);

    }

    private void sendVerificationPhone(String phone)   {


    }


}