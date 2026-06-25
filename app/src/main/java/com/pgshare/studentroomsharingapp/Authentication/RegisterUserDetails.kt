package com.pgshare.studentroomsharingapp.Authentication

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.pgshare.studentroomsharingapp.R

class RegisterUserDetails : AppCompatActivity() {
    private var editTextName: EditText? = null
    private var editTextPhone: EditText? = null
    private var radioGroupGender: RadioGroup? = null
    private var radioButtonMale: RadioButton? = null
    private var radioButtonFemale: RadioButton? = null
    private var registerBtn: Button? = null
    private var progressBar: ProgressBar? = null

    private var database: FirebaseDatabase? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_user_details)
        getSupportActionBar()!!.setBackgroundDrawable(ColorDrawable(getResources().getColor(R.color.C_color)))

        // Initialize Firebase
        database = FirebaseDatabase.getInstance()

        // Find views
        editTextName = findViewById<EditText>(R.id.editTextName)
        editTextPhone = findViewById<EditText>(R.id.editTextPhone)
        registerBtn = findViewById<Button>(R.id.buttonRegister)
        radioGroupGender = findViewById<RadioGroup>(R.id.radioGroupGender)
        radioButtonMale = findViewById<RadioButton?>(R.id.radioButtonMale)
        radioButtonFemale = findViewById<RadioButton?>(R.id.radioButtonFemale)
        progressBar = findViewById<ProgressBar>(R.id.RegisterProgressBar)

        // Button click listener
        registerBtn!!.setOnClickListener(View.OnClickListener { v: View? -> onRegisterBtnClick() })
    }

    private fun onRegisterBtnClick() {
        val name = editTextName!!.getText().toString().trim { it <= ' ' }
        val phone = editTextPhone!!.getText().toString().trim { it <= ' ' }
        val gender = this.selectedGender

        // Input validation
        if (isValidInput(name, phone, gender)) {
            // Show progress bar
            progressBar!!.setVisibility(View.VISIBLE)

            // Obtain the current user's UID
            val userId = FirebaseAuth.getInstance().getCurrentUser()!!.getUid()

            // Create a new user node in the database
            val usersRef = database!!.getReference("Users").child(userId)

            // Store user details directly
            usersRef.child("name").setValue(name)
            usersRef.child("phone").setValue(phone)
            usersRef.child("gender").setValue(gender)

            // Add more details as needed

            // Hide progress bar
            progressBar!!.setVisibility(View.GONE)
            Toast.makeText(this@RegisterUserDetails, "Registration successful!", Toast.LENGTH_SHORT)
                .show()

            // Logic for starting the next activity or navigation
            val intent = Intent(this@RegisterUserDetails, Login::class.java)
            startActivity(intent)
            finish()
        }
    }

    private val selectedGender: String
        get() {
            val selectedId = radioGroupGender!!.getCheckedRadioButtonId()
            if (selectedId == R.id.radioButtonMale) {
                return "Male"
            } else if (selectedId == R.id.radioButtonFemale) {
                return "Female"
            } else {
                Toast.makeText(this, "Please select your gender.", Toast.LENGTH_SHORT).show()
                return ""
            }
        }

    private fun isValidInput(name: String?, phone: String?, gender: String): Boolean {
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Please enter your name.", Toast.LENGTH_SHORT).show()
            return false
        }

        if (TextUtils.isEmpty(phone)) {
            Toast.makeText(this, "Please enter your phone number.", Toast.LENGTH_SHORT).show()
            return false
        }

        if (gender.isEmpty()) {
            Toast.makeText(this, "Please select your gender.", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }
}
