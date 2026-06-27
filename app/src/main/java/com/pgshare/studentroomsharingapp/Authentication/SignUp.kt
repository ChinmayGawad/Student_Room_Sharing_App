package com.pgshare.studentroomsharingapp.Authentication

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.pgshare.studentroomsharingapp.R
import com.pgshare.studentroomsharingapp.databinding.ActivitySignUpBinding
import java.util.Objects

class SignUp : AppCompatActivity() {
    private var emailLayout: TextInputLayout? = null
    private var passwordLayout: TextInputLayout? = null
    private var confirmPasswordLayout: TextInputLayout? = null
    private var editTextEmail: EditText? = null
    private var passwordEditText: EditText? = null
    private var editTextConfirmPassword: EditText? = null
    private var buttonNext: Button? = null
    private var progressBar: ProgressBar? = null
    private var userRef: DatabaseReference? = null
    private var database: FirebaseDatabase? = null
    private var auth: FirebaseAuth? = null

    lateinit var binding : ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // Initialize Firebase
        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()


        // Button click listener
        binding.btnRegister.setOnClickListener { v: View? -> onRegisterBtnClick() }
    }

    private fun onRegisterBtnClick() {
        val email = binding.etEmailSignup.text.toString().trim()
        val password = binding.etPasswordSignup.text.toString().trim()
        val confirmPassword = binding.etConfirmPasswordSignup.text.toString().trim()

        // Input validation
        if (isValidInput(email, password, confirmPassword)) {
            // Show progress bar
            progressBar?.visibility = View.VISIBLE

            auth?.createUserWithEmailAndPassword(email, password)
                ?.addOnCompleteListener(this, OnCompleteListener { task: Task<AuthResult?>? ->
                    if (task!!.isSuccessful) {
                        // User registration success
                        val userId = auth?.currentUser?.uid


                        // Now, add user data to the Realtime Database
                        userRef = database?.getReference("Users")?.child(userId.toString())

                        // Replace "users" with the desired node name
                        userRef?.child("email")?.setValue(email)


                        // You can add more data if needed, such as name, etc.
                        // database.getReference("users").child(userId).child("name").setValue(userName);

                        // Hide progress bar
                        progressBar?.visibility = View.GONE
                        // Navigate to the next screen
                        val intent = Intent(this@SignUp, RegisterUserDetails::class.java)
                        intent.putExtra("email", email)
                        startActivity(intent)
                        finish()
                    } else {
                        // User registration failed
                        // Handle the failure, display an error message, etc.
                        // You can check task.getException().getMessage() for the error message.
                        val errorMessage = task.exception?.message ?: "Registration failed"
                        // You can show this error to the user, e.g., via Toast or Snackbar
                        // Hide progress bar
                        progressBar?.visibility = View.GONE
                    }
                })
        }
    }

    private fun isValidInput(email: String, password: String, confirmPassword: String): Boolean {
        var valid = true

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
