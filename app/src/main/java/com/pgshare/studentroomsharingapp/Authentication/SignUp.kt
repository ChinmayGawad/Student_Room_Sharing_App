package com.pgshare.studentroomsharingapp.Authentication

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.pgshare.studentroomsharingapp.databinding.ActivitySignUpBinding

class SignUp : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Handle the Sign Up Button
        binding.btnRegister.setOnClickListener {
            onRegisterBtnClick()
        }

        // Handle the "Log In" text at the bottom
        binding.tvLoginLink.setOnClickListener {
            // Close this activity and return to the Login screen
            finish()
        }
    }

    private fun onRegisterBtnClick() {
        // 1. Grab ALL the fields from the redesigned XML
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmailSignup.text.toString().trim()
        val password = binding.etPasswordSignup.text.toString().trim()
        val confirmPassword = binding.etConfirmPasswordSignup.text.toString().trim()

        // 2. Validate everything including the name
        if (isValidInput(name, email, password, confirmPassword)) {

            // Show a simple loading toast (or keep your progress bar if you add one to XML)
            Toast.makeText(this, "Creating account...", Toast.LENGTH_SHORT).show()

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {

                        val userId = auth.currentUser?.uid ?: return@addOnCompleteListener

                        // NOTE: Ensure this matches the casing in your InboxAdapter!
                        // I set it to lowercase "users" as that is standard for Firebase.
                        val userRef = database.getReference("Users").child(userId)

                        // 3. Save ALL data to the database simultaneously using a HashMap
                        val userData = hashMapOf(
                            "email" to email,
                            "username" to name
                        )

                        userRef.setValue(userData).addOnCompleteListener { dbTask ->
                            if (dbTask.isSuccessful) {
                                Toast.makeText(this, "Account Created Successfully!", Toast.LENGTH_SHORT).show()

                                val intent = Intent(this@SignUp, Login::class.java)
                                intent.putExtra("email", email)
                                startActivity(intent)
                                finish()
                            }
                        }
                    } else {
                        val errorMessage = task.exception?.message ?: "Registration failed"
                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                    }
                }
        }
    }

    private fun isValidInput(name: String, email: String, password: String, confirmPassword: String): Boolean {
        var valid = true

        // Check if Name is empty
        if (TextUtils.isEmpty(name)) {
            binding.etName.error = "Full Name is required"
            valid = false
        } else {
            binding.etName.error = null
        }

        // Check if email is valid
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmailSignup.error = "Invalid email address"
            valid = false
        } else {
            binding.etEmailSignup.error = null
        }

        // Check if password is empty or meets minimum length
        if (TextUtils.isEmpty(password) || password.length < 6) {
            binding.etPasswordSignup.error = "Password must be at least 6 characters"
            valid = false
        } else {
            binding.etPasswordSignup.error = null
        }

        // Check if passwords match
        if (password != confirmPassword) {
            binding.etConfirmPasswordSignup.error = "Passwords do not match"
            valid = false
        } else {
            binding.etConfirmPasswordSignup.error = null
        }

        return valid
    }
}