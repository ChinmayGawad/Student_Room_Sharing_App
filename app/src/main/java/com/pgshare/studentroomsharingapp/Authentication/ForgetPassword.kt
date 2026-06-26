package com.pgshare.studentroomsharingapp.Authentication

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.pgshare.studentroomsharingapp.R

class ForgetPassword : AppCompatActivity() {
    private var emailEditText: EditText? = null
    private var firebaseAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forget_password)
        getSupportActionBar()!!.setBackgroundDrawable(ColorDrawable(getResources().getColor(R.color.C_color)))

        emailEditText = findViewById<EditText>(R.id.resetPassword)
        val resetButton = findViewById<Button>(R.id.buttonReset)
        firebaseAuth = FirebaseAuth.getInstance()

        resetButton.setOnClickListener { v: View? ->
            val email = emailEditText!!.getText().toString().trim { it <= ' ' }
            if (TextUtils.isEmpty(email)) {
                emailEditText!!.setError("Email is required")
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailEditText!!.setError("Invalid email address")
                return@setOnClickListener
            }

            // Send password reset email
            firebaseAuth!!.sendPasswordResetEmail(email)
                .addOnCompleteListener(OnCompleteListener { task: Task<Void?>? ->
                    if (task!!.isSuccessful()) {
                        Toast.makeText(
                            this@ForgetPassword,
                            "Password reset email sent",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    } else {
                        Toast.makeText(
                            this@ForgetPassword,
                            "Failed to send password reset email",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
        }
    }
}
