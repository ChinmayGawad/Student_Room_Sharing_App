package com.pgshare.studentroomsharingapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class Login extends AppCompatActivity {

    private TextInputEditText LoginMail, LoginPass;
    private FirebaseAuth authLogin;
    private TextView ForgetPass;


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        LoginMail = findViewById(R.id.UsernameEt);
        LoginPass = findViewById(R.id.PasswordEt);

        ForgetPass = findViewById(R.id.forgetPassword);

        ForgetPass.setOnClickListener(v -> {
            Intent intent = new Intent(Login.this, ForgetPassword.class);
            startActivity(intent);
        });


        TextView create_acc = findViewById(R.id.CreateAccount);

        create_acc.setOnClickListener(v -> {
            Intent SignUpIntent = new Intent(Login.this, SignUp.class);
            startActivity(SignUpIntent);
        });
        authLogin = FirebaseAuth.getInstance();
        //User Login Button
        Button buttonLogin = findViewById(R.id.buttonLogin);
        buttonLogin.setOnClickListener(v -> {
           String TextEmail = Objects.requireNonNull(LoginMail.getText()).toString();
           String TextPass = Objects.requireNonNull(LoginPass.getText()).toString();

           if (validateEmail() && validatePassword()) {
               authLogin.signInWithEmailAndPassword(TextEmail, TextPass).addOnCompleteListener(task -> {
                   if (task.isSuccessful()) {
                       Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();
                       Intent intent = new Intent(Login.this, Profile_Page.class);
                       startActivity(intent);
                       finish();
                   }
                   else {
                       Toast.makeText(this, "Login Failed", Toast.LENGTH_SHORT).show();
                   }
               });
            }
        });

    }

    private Boolean validateEmail() {
        String val = Objects.requireNonNull(LoginMail.getText()).toString();
        if (val.isEmpty()) {
            LoginMail.setError("Field Can Not be Empty");
            return false;
        }  else if (!Patterns.EMAIL_ADDRESS.matcher(val).matches()) {
            LoginMail.setError("Invalid Email Address");
            return false;
        }else {
            LoginMail.setError(null);
            return true;
        }
    }

    private Boolean validatePassword() {
        String val = Objects.requireNonNull(LoginPass.getText()).toString();

        if (val.isEmpty()) {
            LoginPass.setError("Field can not be empty");
            return false;
        }
        else {
            LoginPass.setError(null);
            return true;
        }
    }
    //Check if user is Already logged in
  /* @Override
    protected void onStart() {
        super.onStart();
        if (authLogin.getCurrentUser() != null) {
            Intent intent = new Intent(Login.this, Profile_Page.class);
            startActivity(intent);
            finish();
        }
        else {
            Toast.makeText(this, "You Can Login Now", Toast.LENGTH_SHORT).show();
        }
    }*/
}