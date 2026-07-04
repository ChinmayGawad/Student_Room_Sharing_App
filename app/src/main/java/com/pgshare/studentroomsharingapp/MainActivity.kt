package com.pgshare.studentroomsharingapp

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.pgshare.studentroomsharingapp.Authentication.Login
import com.pgshare.studentroomsharingapp.repository.FirebaseRepository
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private var firebaseAuth: FirebaseAuth? = null
    private val repository = FirebaseRepository()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        firebaseAuth = FirebaseAuth.getInstance()

        findViewById<android.widget.TextView>(R.id.tvContinueAsGuest)?.setOnClickListener {
            startActivity(Intent(this, StudentDashboardActivity::class.java))
        }
    }

    fun Rent(view: View?) {
        val intent = Intent(this@MainActivity, StudentDashboardActivity::class.java)
        startActivity(intent)
    }

    fun Owner(view: View?) {
        val user = firebaseAuth?.currentUser
        if (user != null) {
            lifecycleScope.launch {
                val role = repository.getUserRole(user.uid)
                if (role == "owner") {
                    val intent = Intent(this@MainActivity, Add_Room::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(this@MainActivity, "Only property owners can list rooms", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            MaterialAlertDialogBuilder(this)
                .setTitle("Login Required")
                .setMessage("You need to log in to list a room. Would you like to log in?")
                .setPositiveButton("Log In") { _, _ ->
                    startActivity(Intent(this@MainActivity, Login::class.java))
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    // Inflate the menu resource file
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        supportActionBar?.setBackgroundDrawable(ColorDrawable(resources.getColor(R.color.colorPrimary)))
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_profile) {
            if (firebaseAuth?.currentUser != null) {
                openProfile()
            } else {
                Toast.makeText(this, "Please log in to view your profile", Toast.LENGTH_SHORT)
                    .show()
            }
            return true
        } else if (id == R.id.action_contact_us) {
            openContactUs()
            return true
        } else if (id == R.id.action_about) {
            openAbout()
            return true
        } else if (id == R.id.action_logout) {
            logoutUser()
            return true
        } else {
            return super.onOptionsItemSelected(item)
        }
    }


    private fun openAbout() {
        val  intent = Intent(this@MainActivity, Login::class.java)
        startActivity(intent)
        Toast.makeText(this, "Working on it", Toast.LENGTH_SHORT).show()
    }

    private fun openContactUs() {
      /*  val intent = Intent(this@MainActivity, ChatActivity::class.java);
        startActivity(intent);*/
        Toast.makeText(this, "Working on it", Toast.LENGTH_SHORT).show()

    }

    /*   private void openContactUs() {
        Intent intent = new Intent(MainActivity.this, ContactUs.class);
        startActivity(intent);
    }*/
    private fun openProfile() {
        val intent = Intent(this@MainActivity, StudentDashboardActivity::class.java)
        Toast.makeText(this, "Opening Profile", Toast.LENGTH_SHORT).show()
        startActivity(intent)
    }

    private fun logoutUser() {
        firebaseAuth?.signOut()
        startActivity(Intent(this, Login::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }
}
