package com.pgshare.studentroomsharingapp;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Profile_Page extends AppCompatActivity {

    private TextView ProfileUserName, ProfileEmailId, ProfilePhoneNo, ProfileGender;
    private String Profile_email, Profile_name, Profile_phoneNo, Profile_gender;
    private ProgressBar Profile_ProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_page);


        ProfileUserName = findViewById(R.id.ProfileUserName);
        ProfileEmailId = findViewById(R.id.ProfileEmailId);
        ProfilePhoneNo = findViewById(R.id.ProfilePhoneNo);
        ProfileGender = findViewById(R.id.ProfileGender);

        Profile_ProgressBar = findViewById(R.id.ProfileProgressBar);

        FirebaseAuth authProfile = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();

        if (firebaseUser == null) {
            Toast.makeText(this, "Something went wrong! User not found", Toast.LENGTH_SHORT).show();
        }
        else {
            Profile_ProgressBar.setVisibility(View.VISIBLE);
            //Display User's Data
            showProfile(firebaseUser);
        }

    }

    private void showProfile(FirebaseUser firebaseUser) {
        String uid = firebaseUser.getUid();

        //Extract User's Data
        DatabaseReference refrenceProfile = FirebaseDatabase.getInstance().getReference("Users");
        refrenceProfile.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserHelper FetchUser = snapshot.getValue(UserHelper.class);
                //Display User's Data
                if (FetchUser != null) {
                    //Extract User's Data
                    Profile_email = FetchUser.getEmail();
                    Profile_name = FetchUser.getName();
                    Profile_phoneNo = FetchUser.getPhoneNo();
                    Profile_gender = FetchUser.getGender();

                    //Display User's Data
                    ProfileUserName.setText(Profile_name);
                    ProfileEmailId.setText(Profile_email);
                    ProfilePhoneNo.setText(Profile_phoneNo);
                    ProfileGender.setText(Profile_gender);
                }
                Profile_ProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Profile_Page.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                Profile_ProgressBar.setVisibility(View.GONE);

            }
        });
    }
}