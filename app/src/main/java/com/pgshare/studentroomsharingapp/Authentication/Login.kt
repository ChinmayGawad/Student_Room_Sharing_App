package com.pgshare.studentroomsharingapp.Authentication

import android.content.Intent
import android.os.Bundle
import androidx.core.util.PatternsCompat
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

import com.pgshare.studentroomsharingapp.R
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


        binding.btnSkip.setOnClickListener {
            val intent = Intent(this@Login, StudentDashboardActivity::class.java)
            startActivity(intent)
            finish()
        }

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
                setLoading(true)
                authLogin?.signInWithEmailAndPassword(textEmail, textPass)
                    ?.addOnCompleteListener { task ->
                        setLoading(false)
                        if (task.isSuccessful) {
                            val intent = Intent(this@Login, StudentDashboardActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            val message = when {
                                task.exception?.message?.contains("There is no user record") == true ->
                                    "User does not exist"
                                task.exception?.message?.contains("password is invalid") == true ->
                                    "Incorrect password"
                                task.exception?.message?.contains("too many requests") == true ->
                                    "Too many attempts. Try again later."
                                else -> "Login Failed"
                            }
                            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
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
        } else if (!PatternsCompat.EMAIL_ADDRESS.matcher(email).matches()) {
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
        } else if (password.length < 6) {
            binding.tilPassword.error = "Password must be at least 6 characters"
            return false
        } else {
            binding.tilPassword.error = null
            return true
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.btnSignIn.isEnabled = !isLoading
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnSignIn.text = if (isLoading) "" else getString(R.string.sign_in)
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
