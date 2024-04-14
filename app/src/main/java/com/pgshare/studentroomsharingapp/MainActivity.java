package com.pgshare.studentroomsharingapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.pgshare.studentroomsharingapp.Adapter.Owner;
import com.pgshare.studentroomsharingapp.Authentication.Login;
import com.pgshare.studentroomsharingapp.Authentication.OwnerLogin;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.C_color)));
        // Initialize Firebase Authentication
        firebaseAuth = FirebaseAuth.getInstance();
        Log.d("temp_debug", "Testing log filter");
    }

    public void Owner(View view) {
        Intent intent = new Intent(MainActivity.this, OwnerLogin.class);
        startActivity(intent);
    }

    public void Rent(View view) {
        Intent intent = new Intent(MainActivity.this, Login.class);
        startActivity(intent);
    }

    // Inflate the menu resource file
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.C_color)));
        return true;
    }

    // Handle menu item selection
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.profile) {
            if (firebaseAuth.getCurrentUser() != null) {
                openProfile();
            } else {
                Toast.makeText(this, "Please log in to view your profile", Toast.LENGTH_SHORT).show();
            }
            return true;
        } else if (id == R.id.contact) {
            // Handle contact menu item click
            openContactUs();
            return true;
        } else if (id == R.id.menu_About) {
            // Handle about menu item click
            openAbout();
            return true;
        } else if (id == R.id.menu_logout) {
            // Handle logout menu item click
            logoutUser();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }


    private void openAbout() {
        /*Intent intent = new Intent(MainActivity.this, About.class);
        startActivity(intent);*/
        Toast.makeText(this, "Working on it", Toast.LENGTH_SHORT).show();
    }

    private void openContactUs() {
       /* Intent intent = new Intent(MainActivity.this, ContactUs.class);
        startActivity(intent);*/
        Toast.makeText(this, "Working on it", Toast.LENGTH_SHORT).show();
    }

 /*   private void openContactUs() {
        Intent intent = new Intent(MainActivity.this, ContactUs.class);
        startActivity(intent);
    }*/

    private void openProfile() {
        Intent intent = new Intent(MainActivity.this, Profile_Page.class);
        Toast.makeText(this, "Opening Profile", Toast.LENGTH_SHORT).show();
        startActivity(intent);
    }

    // Method to log out the current user
    private void logoutUser() {
        // Sign out the user
        firebaseAuth.signOut();

        // Redirect to the login activity
        Toast.makeText(this, "Logging out", Toast.LENGTH_SHORT).show();
        // Clear the back stack to prevent the user from navigating back to the main activity after logout
    }
}
