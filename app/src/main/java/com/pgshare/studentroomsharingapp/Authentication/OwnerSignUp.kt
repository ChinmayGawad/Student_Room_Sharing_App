package com.pgshare.studentroomsharingapp.Authentication

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.pgshare.studentroomsharingapp.Adapter.Owner
import com.pgshare.studentroomsharingapp.R
import java.util.Objects

class OwnerSignUp : AppCompatActivity() {
    private var editTextOwnerEmail: EditText? = null
    private var editTextOwnerPassword: EditText? = null
    private var editTextOwnerConfirmPassword: EditText? = null

    private var firebaseAuth: FirebaseAuth? = null
    private var databaseReference: DatabaseReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_owner_sign_up)
        getSupportActionBar()!!.setBackgroundDrawable(ColorDrawable(getResources().getColor(R.color.C_color)))

        editTextOwnerEmail = findViewById<EditText>(R.id.editTextOwnerEmail)
        editTextOwnerPassword = findViewById<EditText>(R.id.editTextOwnerPassword)
        editTextOwnerConfirmPassword = findViewById<EditText>(R.id.editTextOwnerConfirmPassword)
        val buttonRegisterOwner = findViewById<Button>(R.id.buttonRegisterOwner)

        firebaseAuth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().getReference("Owners")


        buttonRegisterOwner.setOnClickListener(View.OnClickListener { v: View? -> registerOwner() })
    }

    private fun registerOwner() {
        val ownerEmail = editTextOwnerEmail!!.getText().toString().trim { it <= ' ' }
        val ownerPassword = editTextOwnerPassword!!.getText().toString().trim { it <= ' ' }
        val ownerConfirmPassword =
            editTextOwnerConfirmPassword!!.getText().toString().trim { it <= ' ' }

        if (TextUtils.isEmpty(ownerEmail) ||
            TextUtils.isEmpty(ownerPassword) || TextUtils.isEmpty(ownerConfirmPassword)
        ) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
            return
        }

        if (ownerPassword != ownerConfirmPassword) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        // Perform owner registration with Firebase Authentication
        firebaseAuth!!.createUserWithEmailAndPassword(ownerEmail, ownerPassword)
            .addOnCompleteListener(OnCompleteListener { task: Task<AuthResult?>? ->
                if (task!!.isSuccessful()) {
                    // Owner registration successful, save owner details to database
                    val ownerId =
                        Objects.requireNonNull<FirebaseUser?>(firebaseAuth!!.getCurrentUser())
                            .getUid()
                    val owner = Owner(ownerId, ownerEmail)
                    databaseReference!!.child(ownerId).setValue(owner)

                    Toast.makeText(this, "Owner registered successfully", Toast.LENGTH_SHORT).show()

                    // Navigate to owner dashboard or login screen
                    startActivity(Intent(this, RegisterOwnerDetails::class.java))
                    finish()
                } else {
                    // Owner registration failed, display error message
                    Toast.makeText(
                        this,
                        "Failed to register owner: " + Objects.requireNonNull<Exception?>(task.getException()).message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
}
