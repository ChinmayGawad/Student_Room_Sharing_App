package com.pgshare.studentroomsharingapp;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
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
import com.pgshare.studentroomsharingapp.Adapter.Owner;
import com.pgshare.studentroomsharingapp.Adapter.UserHelper;

public class Profile_Page extends AppCompatActivity {

    private TextView ProfileUserName, ProfileEmailId, ProfilePhoneNo, ProfileGender;
    private ProgressBar Profile_ProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_page);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.C_color)));

        ProfileUserName = findViewById(R.id.ProfileUserName);
        ProfileEmailId = findViewById(R.id.ProfileEmailId);
        ProfilePhoneNo = findViewById(R.id.ProfilePhoneNo);
        ProfileGender = findViewById(R.id.ProfileGender);
        Profile_ProgressBar = findViewById(R.id.ProfileProgressBar);

        FirebaseAuth authProfile = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();

        if (firebaseUser == null) {
            Toast.makeText(this, "Something went wrong! User not found", Toast.LENGTH_SHORT).show();
        } else {
            Profile_ProgressBar.setVisibility(View.VISIBLE);
            // Display user's data
            showProfile(firebaseUser);
        }
    }

    private void showProfile(FirebaseUser firebaseUser) {
        String uid = firebaseUser.getUid();
        DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
        DatabaseReference referenceOwner = FirebaseDatabase.getInstance().getReference().child("Owners").child(uid);

        referenceProfile.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserHelper user = snapshot.getValue(UserHelper.class);
                Log.d("Profile_Page", "UserHelper object: " + user);
                if (user != null) {
                    // Extract user's data
                    String profileName = user.getName();
                    String profileEmail = user.getEmail();
                    String profilePhoneNo = user.getPhone(); // Corrected getter method name
                    String profileGender = user.getGender();

                    Log.d("Profile_Page", "Profile Phone Number: " + profilePhoneNo);

                    // Display user's data
                    ProfileUserName.setText(profileName);
                    ProfileEmailId.setText(profileEmail);
                    ProfilePhoneNo.setText(profilePhoneNo);
                    ProfileGender.setText(profileGender);
                    Profile_ProgressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Profile_Page.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                Profile_ProgressBar.setVisibility(View.GONE);
            }
        });

        referenceOwner.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Owner owner = snapshot.getValue(Owner.class);
                if (owner != null) {
                    // Extract owner's data
                    String ownerName = owner.getOwnerName();
                    String ownerPhone = owner.getOwnerPhone();
                    String ownerGender = owner.getGender();

                    // Display owner's data
                    ProfileUserName.setText(ownerName);
                    ProfileEmailId.setText("Owner Email"); // You can decide if you want to display owner's email
                    ProfilePhoneNo.setText(ownerPhone);
                    ProfileGender.setText(ownerGender);
                    Profile_ProgressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Profile_Page.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                Profile_ProgressBar.setVisibility(View.GONE);
            }
        });
    }



    public void onEditProfileClick(View view) {
        Intent intent = new Intent(Profile_Page.this, EditProfile.class);
        startActivity(intent);
    }
}
