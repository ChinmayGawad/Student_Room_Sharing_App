package com.pgshare.studentroomsharingapp

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.pgshare.studentroomsharingapp.Authentication.Login
import com.pgshare.studentroomsharingapp.Authentication.OwnerLogin

class MainActivity : AppCompatActivity() {
    private var firebaseAuth: FirebaseAuth? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        getSupportActionBar()!!.setBackgroundDrawable(ColorDrawable(getResources().getColor(R.color.C_color)))
        // Initialize Firebase Authentication
        firebaseAuth = FirebaseAuth.getInstance()
        Log.d("temp_debug", "Testing log filter")
    }

    fun Owner(view: View?) {
        val intent = Intent(this@MainActivity, OwnerLogin::class.java)
        startActivity(intent)
    }

    fun Rent(view: View?) {
        val intent = Intent(this@MainActivity, Login::class.java)
        startActivity(intent)
    }

    // Inflate the menu resource file
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        getMenuInflater().inflate(R.menu.main_menu, menu)
        getSupportActionBar()!!.setBackgroundDrawable(ColorDrawable(getResources().getColor(R.color.C_color)))
        return true
    }

    // Handle menu item selection
    @SuppressLint("NonConstantResourceId")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.getItemId()
        if (id == R.id.profile) {
            if (firebaseAuth!!.getCurrentUser() != null) {
                openProfile()
            } else {
                Toast.makeText(this, "Please log in to view your profile", Toast.LENGTH_SHORT)
                    .show()
            }
            return true
        } else if (id == R.id.contact) {
            // Handle contact menu item click
            openContactUs()
            return true
        } else if (id == R.id.menu_About) {
            // Handle about menu item click
            openAbout()
            return true
        } else if (id == R.id.menu_logout) {
            // Handle logout menu item click
            logoutUser()
            return true
        } else {
            return super.onOptionsItemSelected(item)
        }
    }


    private fun openAbout() {
        /*Intent intent = new Intent(MainActivity.this, About.class);
        startActivity(intent);*/
        Toast.makeText(this, "Working on it", Toast.LENGTH_SHORT).show()
    }

    private fun openContactUs() {
        /* Intent intent = new Intent(MainActivity.this, ContactUs.class);
        startActivity(intent);*/
        Toast.makeText(this, "Working on it", Toast.LENGTH_SHORT).show()
    }

    /*   private void openContactUs() {
        Intent intent = new Intent(MainActivity.this, ContactUs.class);
        startActivity(intent);
    }*/
    private fun openProfile() {
        val intent = Intent(this@MainActivity, Profile_Page::class.java)
        Toast.makeText(this, "Opening Profile", Toast.LENGTH_SHORT).show()
        startActivity(intent)
    }

    // Method to log out the current user
    private fun logoutUser() {
        // Sign out the user
        firebaseAuth!!.signOut()

        // Redirect to the login activity
        Toast.makeText(this, "Logging out", Toast.LENGTH_SHORT).show()
        // Clear the back stack to prevent the user from navigating back to the main activity after logout
    }
}
