package com.pgshare.studentroomsharingapp.Authentication

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

import com.pgshare.studentroomsharingapp.StudentDashboardActivity
import com.pgshare.studentroomsharingapp.databinding.ActivityLoginBinding

class Login : AppCompatActivity() {
    private var authLogin: FirebaseAuth? = null

    lateinit var binding : ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        binding.btnBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        binding.btnForgotPassword.setOnClickListener {
            val intent = Intent(this@Login, ForgotPassword::class.java)
            startActivity(intent)
        }

        binding.btnSignUp.setOnClickListener {
            val signUpIntent = Intent(this@Login, SignUp::class.java)
            startActivity(signUpIntent)
        }
        authLogin = FirebaseAuth.getInstance()
        binding.btnSignIn.setOnClickListener {
            val textEmail = binding.tilEmail.editText?.text.toString()
            val textPass = binding.tilPassword.editText?.text.toString()
            if (validateEmail() && validatePassword()) {
                authLogin?.signInWithEmailAndPassword(textEmail, textPass)
                    ?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@Login, StudentDashboardActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            if (task.exception?.message?.contains("There is no user record corresponding to this identifier") == true) {
                                Toast.makeText(this, "User does not exist", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this, "Login Failed", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
            }
        }
    }

    private fun validateEmail(): Boolean {
        val email = binding.tilEmail.editText?.text.toString()
        if (email.isEmpty()) {
            binding.tilEmail.error = "Field Can Not be Empty"
            return false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error ="Invalid Email Address"
            return false
        } else {
            binding.tilEmail.error = null
            return true
        }
    }

    private fun validatePassword(): Boolean {
        val password = binding.tilPassword.editText?.text.toString()

        if (password.isEmpty()) {
            binding.tilPassword.error = "Field can not be empty"
            return false
        } else {
            binding.tilPassword.error = null
            return true
        }
    }

    override fun onStart() {
        super.onStart()
        if (authLogin?.currentUser != null) {
            val intent = Intent(this@Login, StudentDashboardActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
