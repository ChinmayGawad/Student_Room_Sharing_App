package com.pgshare.studentroomsharingapp

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.pgshare.studentroomsharingapp.Adapter.Owner
import com.pgshare.studentroomsharingapp.Adapter.UserHelper

class Profile_Page : AppCompatActivity() {
    private var ProfileUserName: TextView? = null
    private var ProfileEmailId: TextView? = null
    private var ProfilePhoneNo: TextView? = null
    private var ProfileGender: TextView? = null
    private var Profile_ProgressBar: ProgressBar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_page)
        getSupportActionBar()!!.setBackgroundDrawable(ColorDrawable(getResources().getColor(R.color.C_color)))

        ProfileUserName = findViewById<TextView>(R.id.ProfileUserName)
        ProfileEmailId = findViewById<TextView>(R.id.ProfileEmailId)
        ProfilePhoneNo = findViewById<TextView>(R.id.ProfilePhoneNo)
        ProfileGender = findViewById<TextView>(R.id.ProfileGender)
        Profile_ProgressBar = findViewById<ProgressBar>(R.id.ProfileProgressBar)

        val authProfile = FirebaseAuth.getInstance()
        val firebaseUser = authProfile.getCurrentUser()

        if (firebaseUser == null) {
            Toast.makeText(this, "Something went wrong! User not found", Toast.LENGTH_SHORT).show()
        } else {
            Profile_ProgressBar!!.setVisibility(View.VISIBLE)
            // Display user's data
            showProfile(firebaseUser)
        }
    }

    private fun showProfile(firebaseUser: FirebaseUser) {
        val uid = firebaseUser.getUid()
        val referenceProfile =
            FirebaseDatabase.getInstance().getReference().child("Users").child(uid)
        val referenceOwner =
            FirebaseDatabase.getInstance().getReference().child("Owners").child(uid)

        referenceProfile.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue<UserHelper?>(UserHelper::class.java)
                Log.d("Profile_Page", "UserHelper object: " + user)
                if (user != null) {
                    // Extract user's data
                    val profileName = user.getName()
                    val profileEmail = user.getEmail()
                    val profilePhoneNo = user.getPhone() // Corrected getter method name
                    val profileGender = user.getGender()

                    Log.d("Profile_Page", "Profile Phone Number: " + profilePhoneNo)

                    // Display user's data
                    ProfileUserName!!.setText(profileName)
                    ProfileEmailId!!.setText(profileEmail)
                    ProfilePhoneNo!!.setText(profilePhoneNo)
                    ProfileGender!!.setText(profileGender)
                    Profile_ProgressBar!!.setVisibility(View.GONE)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Profile_Page, "Something went wrong", Toast.LENGTH_SHORT).show()
                Profile_ProgressBar!!.setVisibility(View.GONE)
            }
        })

        referenceOwner.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val owner = snapshot.getValue<Owner?>(Owner::class.java)
                if (owner != null) {
                    // Extract owner's data
                    val ownerName = owner.getOwnerName()
                    val ownerPhone = owner.getOwnerPhone()
                    val ownerGender = owner.getGender()

                    // Display owner's data
                    ProfileUserName!!.setText(ownerName)
                    ProfileEmailId!!.setText("Owner Email") // You can decide if you want to display owner's email
                    ProfilePhoneNo!!.setText(ownerPhone)
                    ProfileGender!!.setText(ownerGender)
                    Profile_ProgressBar!!.setVisibility(View.GONE)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Profile_Page, "Something went wrong", Toast.LENGTH_SHORT).show()
                Profile_ProgressBar!!.setVisibility(View.GONE)
            }
        })
    }


    fun onEditProfileClick(view: View?) {
        val intent = Intent(this@Profile_Page, EditProfile::class.java)
        startActivity(intent)
    }
}
