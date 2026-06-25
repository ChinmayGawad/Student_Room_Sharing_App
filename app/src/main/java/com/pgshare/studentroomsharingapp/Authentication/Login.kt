package com.pgshare.studentroomsharingapp.Authentication

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.pgshare.studentroomsharingapp.Display_Room
import com.pgshare.studentroomsharingapp.R
import java.util.Objects

class Login : AppCompatActivity() {
    private var LoginMail: TextInputEditText? = null
    private var LoginPass: TextInputEditText? = null
    private var authLogin: FirebaseAuth? = null
    private var ForgetPass: TextView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        getSupportActionBar()!!.setBackgroundDrawable(ColorDrawable(getResources().getColor(R.color.C_color)))

        LoginMail = findViewById<TextInputEditText>(R.id.UsernameEt)
        LoginPass = findViewById<TextInputEditText>(R.id.PasswordEt)

        ForgetPass = findViewById<TextView>(R.id.forgetPassword)

        ForgetPass!!.setOnClickListener(View.OnClickListener { v: View? ->
            val intent = Intent(this@Login, ForgetPassword::class.java)
            startActivity(intent)
        })


        val create_acc = findViewById<TextView>(R.id.CreateAccount)

        create_acc.setOnClickListener(View.OnClickListener { v: View? ->
            val SignUpIntent = Intent(this@Login, SignUp::class.java)
            startActivity(SignUpIntent)
        })
        authLogin = FirebaseAuth.getInstance()
        //User Login Button
        val buttonLogin = findViewById<Button>(R.id.buttonLogin)
        buttonLogin.setOnClickListener(View.OnClickListener { v: View? ->
            val TextEmail = Objects.requireNonNull<Editable?>(LoginMail!!.getText()).toString()
            val TextPass = Objects.requireNonNull<Editable?>(LoginPass!!.getText()).toString()
            if (validateEmail() && validatePassword()) {
                authLogin!!.signInWithEmailAndPassword(TextEmail, TextPass)
                    .addOnCompleteListener(OnCompleteListener { task: Task<AuthResult?>? ->
                        if (task!!.isSuccessful()) {
                            Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@Login, Display_Room::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            // Check if the error is due to user not existing (invalid email)
                            if (task.getException() != null && task.getException()!!.message != null &&
                                task.getException()!!.message!!.contains("There is no user record corresponding to this identifier")
                            ) {
                                Toast.makeText(this, "User does not exist", Toast.LENGTH_SHORT)
                                    .show()
                            } else {
                                Toast.makeText(this, "Login Failed", Toast.LENGTH_SHORT).show()
                            }
                        }
                    })
            }
        })
    }

    private fun validateEmail(): Boolean {
        val `val` = Objects.requireNonNull<Editable?>(LoginMail!!.getText()).toString()
        if (`val`.isEmpty()) {
            LoginMail!!.setError("Field Can Not be Empty")
            return false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(`val`).matches()) {
            LoginMail!!.setError("Invalid Email Address")
            return false
        } else {
            LoginMail!!.setError(null)
            return true
        }
    }

    private fun validatePassword(): Boolean {
        val `val` = Objects.requireNonNull<Editable?>(LoginPass!!.getText()).toString()

        if (`val`.isEmpty()) {
            LoginPass!!.setError("Field can not be empty")
            return false
        } else {
            LoginPass!!.setError(null)
            return true
        }
    }

    //Check if user is Already logged in
    override fun onStart() {
        super.onStart()
        if (authLogin!!.getCurrentUser() != null) {
            val intent = Intent(this@Login, Display_Room::class.java)
            startActivity(intent)
            finish()
        }
    }
}
