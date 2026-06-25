package com.pgshare.studentroomsharingapp.Authentication

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.pgshare.studentroomsharingapp.Add_Room
import com.pgshare.studentroomsharingapp.R

class OwnerLogin : AppCompatActivity() {
    private var editTextOwnerEmail: EditText? = null
    private var editTextOwnerPassword: EditText? = null
    private var OwnerLoginprogressBar: ProgressBar? = null

    private var firebaseAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_owner_login)
        getSupportActionBar()!!.setBackgroundDrawable(ColorDrawable(getResources().getColor(R.color.C_color)))

        // Initialize Firebase Authentication instance
        firebaseAuth = FirebaseAuth.getInstance()

        // Initialize views
        editTextOwnerEmail = findViewById<EditText>(R.id.editTextOwnerEmail)
        editTextOwnerPassword = findViewById<EditText>(R.id.editTextOwnerPassword)
        val textViewCreateAccount = findViewById<TextView>(R.id.textViewCreateAccount)
        val textViewForgetPassword = findViewById<TextView>(R.id.textViewForgetPassword)
        val buttonLoginOwner = findViewById<Button>(R.id.buttonLoginOwner)

        OwnerLoginprogressBar = findViewById<ProgressBar>(R.id.OwnerLoginProgressBar)

        // Set onClickListener for "Create Account" TextView
        textViewCreateAccount.setOnClickListener(View.OnClickListener { v: View? ->
            // Handle click event for creating an account
            startActivity(Intent(this@OwnerLogin, OwnerSignUp::class.java))
        })

        // Set onClickListener for "Forget Password" TextView
        textViewForgetPassword.setOnClickListener(View.OnClickListener { v: View? ->
            // Handle click event for forget password
            startActivity(Intent(this@OwnerLogin, ForgetPassword::class.java))
        })

        // Set onClickListener for login button
        buttonLoginOwner.setOnClickListener(View.OnClickListener { v: View? ->
            // Perform login operation
            loginOwner()
        })
    }

    private fun loginOwner() {
        val email = editTextOwnerEmail!!.getText().toString().trim { it <= ' ' }
        val password = editTextOwnerPassword!!.getText().toString().trim { it <= ' ' }

        // Validate email and password
        if (TextUtils.isEmpty(email)) {
            editTextOwnerEmail!!.setError("Email is required")
            editTextOwnerEmail!!.requestFocus()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextOwnerEmail!!.setError("Enter a valid email")
            editTextOwnerEmail!!.requestFocus()
            return
        }

        if (TextUtils.isEmpty(password)) {
            editTextOwnerPassword!!.setError("Password is required")
            editTextOwnerPassword!!.requestFocus()
            return
        }

        OwnerLoginprogressBar!!.setVisibility(ProgressBar.VISIBLE)
        // Authenticate user using Firebase Authentication
        firebaseAuth!!.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, OnCompleteListener { task: Task<AuthResult?>? ->
                if (task!!.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    val user = firebaseAuth!!.getCurrentUser()

                    // Retrieve user type from the database
                    val ownerRef =
                        FirebaseDatabase.getInstance().getReference("Owners").child(user!!.getUid())
                    ownerRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            Log.d("OwnerLogin", "onDataChange triggered")
                            if (snapshot.exists()) {
                                val userType =
                                    snapshot.child("userType").getValue<String?>(String::class.java)
                                Log.d("OwnerLogin", "Retrieved user type: " + userType)
                                if ("Owner" == userType) {
                                    Log.d("OwnerLogin", "User is an owner")
                                    // User is an owner, proceed to owner-specific functionality
                                    startActivity(Intent(this@OwnerLogin, Add_Room::class.java))
                                    finish()
                                } else {
                                    Log.d("OwnerLogin", "User is not an owner")
                                    // User is not authorized as an owner
                                    Toast.makeText(
                                        this@OwnerLogin,
                                        "You are not authorized as an owner",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } else {
                                Log.d("OwnerLogin", "Snapshot does not exist")
                                // User data does not exist in the database
                                Toast.makeText(
                                    this@OwnerLogin,
                                    "Owner data does not exist",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            OwnerLoginprogressBar!!.setVisibility(ProgressBar.GONE)
                        }


                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(
                                this@OwnerLogin,
                                "Database error: " + error.getMessage(),
                                Toast.LENGTH_SHORT
                            ).show()
                            OwnerLoginprogressBar!!.setVisibility(ProgressBar.GONE)
                        }
                    })
                } else {
                    // If sign in fails, display a message to the user.
                    OwnerLoginprogressBar!!.setVisibility(ProgressBar.GONE)
                    Toast.makeText(this@OwnerLogin, "Authentication failed", Toast.LENGTH_SHORT)
                        .show()
                }
            })
    }

    override fun onStart() {
        super.onStart()
        if (firebaseAuth!!.getCurrentUser() != null) {
            val intent = Intent(this@OwnerLogin, Add_Room::class.java)
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, "You Can Login Now", Toast.LENGTH_SHORT).show()
        }
    }
}
