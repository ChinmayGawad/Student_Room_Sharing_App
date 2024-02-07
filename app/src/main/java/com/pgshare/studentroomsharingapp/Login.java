package com.pgshare.studentroomsharingapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Login extends AppCompatActivity {


    private TextInputEditText EtMail, EtPass;
    private TextView Create_acc;
    DatabaseReference databaseReference;
    FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();
        // Initialize Firebase Database references
        databaseReference = FirebaseDatabase.getInstance().getReference("users");


        EtMail = findViewById(R.id.UsernameEt);
        EtPass = findViewById(R.id.PassEt);

        Create_acc = findViewById(R.id.ViewCreateAccount);
        Create_acc.setOnClickListener(view -> {
            Intent i = new Intent(Login.this, SignUp.class);
            startActivity(i);
        });


    }


    private Boolean validateEmail() {
        String val = EtMail.getEditableText().toString();
        if (val.isEmpty()) {
            EtMail.setError("Field can not be empty");
            return false;
        }  else {
            EtMail.setError(null);
            return true;
        }
    }

    private Boolean validatePassword() {
        String val = EtPass.getEditableText().toString();

        if (val.isEmpty()) {
            EtPass.setError("Field can not be empty");
            return false;
        }
        else {
            EtPass.setError(null);
            return true;
        }
    }

    public void UserLogin(View view) {
        if (!validateEmail() && !validatePassword()) {
            Toast.makeText(this, "Please enter valid email and password", Toast.LENGTH_SHORT).show();
            return;
        } else {
            isUser();
        }
    }

    private void isUser() {
        String email = EtMail.getEditableText().toString().trim();
        String password = EtPass.getEditableText().toString().trim();

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, retrieve additional user data
                        new Handler().postDelayed(() -> {  // Add a brief delay
                            FirebaseUser user = auth.getCurrentUser();
                            assert user != null; // Should now be valid
                            String userId = user.getUid();
                            DatabaseReference userRef = databaseReference.child("users").child(userId);
                            userRef.addValueEventListener(new ValueEventListener() {

                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    Log.d("TAG", "Snapshot value: " + snapshot.getValue());
                                    UserHelper user = snapshot.getValue(UserHelper.class);

                                    if (user != null) {
                                        // Redirect to ProfilePage with user data
                                        Intent intent = new Intent(getApplicationContext(), Profile_Page.class);


                                        // Pass user data to ProfilePage
                                        startActivity(intent);
                                        finish();
                                    }
                                    else {
                                        // Handle user not found
                                        Toast.makeText(Login.this, "User not found", Toast.LENGTH_SHORT).show();
                                    }

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    // Handle database error
                                    Toast.makeText(Login.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();

                                }
                            });
                        }, 500);


                    } else {
                        // Handle specific errors
                        Exception exception = task.getException();
                        if (exception instanceof FirebaseAuthInvalidCredentialsException) {
                            Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Sign in failed. Please try again.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


}