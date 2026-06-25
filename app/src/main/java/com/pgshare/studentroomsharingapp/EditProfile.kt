package com.pgshare.studentroomsharingapp

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class EditProfile : AppCompatActivity() {
    var nameEditText: EditText? = null
    var phoneEditText: EditText? = null
    var genderEditText: EditText? = null
    var saveButton: Button? = null
    var profileProgressBar: ProgressBar? = null

    // Firebase authentication instance
    var mAuth: FirebaseAuth? = null

    // Firebase Realtime Database instance
    var mDatabase: FirebaseDatabase? = null

    // Reference to the current user's data node in the database
    var mUserReference: DatabaseReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        getSupportActionBar()!!.setBackgroundDrawable(ColorDrawable(getResources().getColor(R.color.C_color)))

        nameEditText = findViewById<EditText>(R.id.editTextText)
        phoneEditText = findViewById<EditText>(R.id.editTextText2)
        genderEditText = findViewById<EditText>(R.id.editTextText4)
        saveButton = findViewById<Button>(R.id.button)
        profileProgressBar = findViewById<ProgressBar>(R.id.ProfileProgressBar)

        // Initialize Firebase instances
        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance()
        val currentUser = mAuth!!.getCurrentUser()
        if (currentUser != null) {
            mUserReference = mDatabase!!.getReference().child("Users").child(currentUser.getUid())
        }

        saveButton!!.setOnClickListener(View.OnClickListener { v: View? ->
            // Perform save operation here
            saveProfile()
        })
    }

    private fun saveProfile() {
        // Retrieve updated profile information from EditText fields
        val newName = nameEditText!!.getText().toString()
        val newPhone = phoneEditText!!.getText().toString()
        val newGender = genderEditText!!.getText().toString()

        profileProgressBar!!.setVisibility(View.VISIBLE)
        // Update profile in the Firebase Realtime Database
        if (mUserReference != null) {
            mUserReference!!.child("name").setValue(newName)
            mUserReference!!.child("phone").setValue(newPhone)
            mUserReference!!.child("gender").setValue(newGender)
            Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show()

            nameEditText!!.getText().clear()
            phoneEditText!!.getText().clear()
            genderEditText!!.getText().clear()

            profileProgressBar!!.setVisibility(View.GONE)
            startActivity(Intent(this@EditProfile, Profile_Page::class.java))
            finish()
        } else {
            Toast.makeText(this, "Failed to update profile.", Toast.LENGTH_SHORT).show()
        }
    }
}
