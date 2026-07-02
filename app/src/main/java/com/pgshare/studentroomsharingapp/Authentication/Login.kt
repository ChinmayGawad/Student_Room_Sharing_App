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

        binding.ivBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        binding.forgetPassword.setOnClickListener {
            val intent = Intent(this@Login, ForgotPassword::class.java)
            startActivity(intent)
        }

        binding.CreateAccount.setOnClickListener {
            val signUpIntent = Intent(this@Login, SignUp::class.java)
            startActivity(signUpIntent)
        }
        authLogin = FirebaseAuth.getInstance()
        binding.buttonLogin.setOnClickListener {
            val textEmail = binding.UsernameEt.text.toString()
            val textPass = binding.PasswordEt.text.toString()
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
        val `val` = binding.UsernameEt.text.toString()
        if (`val`.isEmpty()) {
            binding.UsernameEt.error = "Field Can Not be Empty"
            return false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(`val`).matches()) {
            binding.UsernameEt.error ="Invalid Email Address"
            return false
        } else {
            binding.UsernameEt.error = null
            return true
        }
    }

    private fun validatePassword(): Boolean {
        val `val` = binding.PasswordEt.text.toString()

        if (`val`.isEmpty()) {
            binding.PasswordEt.error = "Field can not be empty"
            return false
        } else {
            binding.PasswordEt.error = null
            return true
        }
    }

    //Check if user is Already logged in
    override fun onStart() {
        super.onStart()
        if (authLogin?.currentUser != null) {
            val intent = Intent(this@Login, StudentDashboardActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
