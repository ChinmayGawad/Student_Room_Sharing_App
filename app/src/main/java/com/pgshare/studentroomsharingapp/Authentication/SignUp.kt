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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        getSupportActionBar()!!.setBackgroundDrawable(ColorDrawable(getResources().getColor(R.color.C_color)))

        // Initialize Firebase
        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()


        // Find views
        emailLayout = findViewById<TextInputLayout>(R.id.EmailLayout)
        passwordLayout = findViewById<TextInputLayout>(R.id.PasswordLayout)
        confirmPasswordLayout = findViewById<TextInputLayout>(R.id.ConfirmPasswordLayout)

        editTextEmail = findViewById<EditText>(R.id.editTextEmail)
        passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        editTextConfirmPassword = findViewById<EditText>(R.id.editTextConfirmPassword)

        buttonNext = findViewById<Button>(R.id.buttonNext)
        progressBar = findViewById<ProgressBar>(R.id.SignUpProgressBar)

        // Button click listener
        buttonNext!!.setOnClickListener(View.OnClickListener { v: View? -> onRegisterBtnClick() })
    }

    private fun onRegisterBtnClick() {
        val email = editTextEmail!!.getText().toString().trim { it <= ' ' }
        val password = passwordEditText!!.getText().toString().trim { it <= ' ' }
        val confirmPassword = editTextConfirmPassword!!.getText().toString().trim { it <= ' ' }

        // Input validation
        if (isValidInput(email, password, confirmPassword)) {
            // Show progress bar
            progressBar!!.setVisibility(View.VISIBLE)

            auth!!.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, OnCompleteListener { task: Task<AuthResult?>? ->
                    if (task!!.isSuccessful()) {
                        // User registration success
                        val userId = auth!!.getCurrentUser()!!.getUid()


                        // Now, add user data to the Realtime Database
                        userRef = database!!.getReference("Users").child(userId)

                        // Replace "users" with the desired node name
                        userRef!!.child("email").setValue(email)


                        // You can add more data if needed, such as name, etc.
                        // database.getReference("users").child(userId).child("name").setValue(userName);

                        // Hide progress bar
                        progressBar!!.setVisibility(View.GONE)
                        // Navigate to the next screen
                        val intent = Intent(this@SignUp, RegisterUserDetails::class.java)
                        intent.putExtra("email", email)
                        startActivity(intent)
                        finish()
                    } else {
                        // User registration failed
                        // Handle the failure, display an error message, etc.
                        // You can check task.getException().getMessage() for the error message.
                        Objects.requireNonNull<Exception?>(task.getException()).message
                        // Hide progress bar
                        progressBar!!.setVisibility(View.GONE)
                    }
                })
        }
    }

    private fun isValidInput(email: String?, password: String, confirmPassword: String?): Boolean {
        var valid = true

        // Check if email is valid
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout!!.setError("Invalid email address")
            valid = false
        } else {
            emailLayout!!.setError(null)
        }

        // Check if password is empty or meets minimum length
        if (TextUtils.isEmpty(password) || password.length < 6) {
            passwordLayout!!.setError("Password must be at least 6 characters")
            valid = false
        } else {
            passwordLayout!!.setError(null)
        }

        // Check if passwords match
        if (password != confirmPassword) {
            confirmPasswordLayout!!.setError("Passwords do not match")
            valid = false
        } else {
            confirmPasswordLayout!!.setError(null)
        }

        return valid
    }
}
