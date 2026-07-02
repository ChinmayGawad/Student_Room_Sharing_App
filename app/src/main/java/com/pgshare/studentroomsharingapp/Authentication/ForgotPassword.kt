package com.pgshare.studentroomsharingapp.Authentication

import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.pgshare.studentroomsharingapp.R

class ForgotPassword : AppCompatActivity() {
    private var emailEditText: EditText? = null
    private var firebaseAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_forgot_password)


        emailEditText = findViewById(R.id.resetPassword)
        val resetButton = findViewById<Button>(R.id.buttonReset)
        firebaseAuth = FirebaseAuth.getInstance()

        resetButton.setOnClickListener {
            val email = emailEditText?.text.toString().trim { it <= ' ' }
            if (TextUtils.isEmpty(email)) {
                emailEditText?.error = "Email is required"
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailEditText?.error = "Invalid email address"
                return@setOnClickListener
            }

            firebaseAuth?.sendPasswordResetEmail(email)
                ?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            this@ForgotPassword,
                            "Password reset email sent",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    } else {
                        Toast.makeText(
                            this@ForgotPassword,
                            "Failed to send password reset email",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }
}


