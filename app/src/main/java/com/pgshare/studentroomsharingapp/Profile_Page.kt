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
        supportActionBar?.setBackgroundDrawable(ColorDrawable(resources.getColor(R.color.C_color)))

        ProfileUserName = findViewById<TextView>(R.id.ProfileUserName)
        ProfileEmailId = findViewById<TextView>(R.id.ProfileEmailId)
        ProfilePhoneNo = findViewById<TextView>(R.id.ProfilePhoneNo)
        ProfileGender = findViewById<TextView>(R.id.ProfileGender)
        Profile_ProgressBar = findViewById<ProgressBar>(R.id.ProfileProgressBar)

        val authProfile = FirebaseAuth.getInstance()
        val firebaseUser = authProfile.currentUser

        if (firebaseUser == null) {
            Toast.makeText(this, "Something went wrong! User not found", Toast.LENGTH_SHORT).show()
        } else {
            Profile_ProgressBar!!.visibility = View.VISIBLE
            // Display user's data
            showProfile(firebaseUser)
        }
    }

    private fun showProfile(firebaseUser: FirebaseUser) {
        val uid = firebaseUser.uid
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
                    val profileName = user.name
                    val profileEmail = user.email
                    val profilePhoneNo = user.phone // Corrected getter method name
                    val profileGender = user.gender

                    Log.d("Profile_Page", "Profile Phone Number: " + profilePhoneNo)

                    // Display user's data
                    ProfileUserName!!.text = profileName
                    ProfileEmailId!!.text = profileEmail
                    ProfilePhoneNo!!.text = profilePhoneNo
                    ProfileGender!!.text = profileGender
                    Profile_ProgressBar!!.visibility = View.GONE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Profile_Page, "Something went wrong", Toast.LENGTH_SHORT).show()
                Profile_ProgressBar!!.visibility = View.GONE
            }
        })

        referenceOwner.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val owner = snapshot.getValue<Owner?>(Owner::class.java)
                if (owner != null) {
                    // Extract owner's data
                    val ownerName = owner.ownerName
                    val ownerPhone = owner.ownerPhone
                    val ownerGender = owner.gender

                    // Display owner's data
                    ProfileUserName!!.text = ownerName
                    ProfileEmailId!!.text = "Owner Email" // You can decide if you want to display owner's email
                    ProfilePhoneNo!!.text = ownerPhone
                    ProfileGender!!.text = ownerGender
                    Profile_ProgressBar!!.visibility = View.GONE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Profile_Page, "Something went wrong", Toast.LENGTH_SHORT).show()
                Profile_ProgressBar!!.visibility = View.GONE
            }
        })
    }


    fun onEditProfileClick(view: View?) {
        val intent = Intent(this@Profile_Page, EditProfile::class.java)
        startActivity(intent)
    }
}
