package com.pgshare.studentroomsharingapp.Authentication

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.pgshare.studentroomsharingapp.Adapter.Owner
import com.pgshare.studentroomsharingapp.R

// Import statements omitted for brevity
class RegisterOwnerDetails : AppCompatActivity() {
    var RegisterOwnerDetailsProgressBar: ProgressBar? = null
    private var editTextOwnerName: EditText? = null
    private var editTextOwnerPhone: EditText? = null
    private var imageOwnerNOC: ImageView? = null
    private var radioGroupGender: RadioGroup? = null
    private var buttonChooseImage: Button? = null
    private var buttonFinish: Button? = null

    // Firebase
    private var firebaseAuth: FirebaseAuth? = null
    private var databaseReference: DatabaseReference? = null
    private var storageReference: StorageReference? = null

    // Image
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_regsiter_owner_details)
        getSupportActionBar()!!.setBackgroundDrawable(ColorDrawable(getResources().getColor(R.color.C_color)))

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance()
        // Initialize Firebase Database
        databaseReference = FirebaseDatabase.getInstance().getReference("Owners")
        // Initialize Firebase Storage
        storageReference = FirebaseStorage.getInstance().getReference("OwnerImages")

        editTextOwnerName = findViewById<EditText>(R.id.editTextOwnerName)
        editTextOwnerPhone = findViewById<EditText>(R.id.editTextOwnerPhone)
        imageOwnerNOC = findViewById<ImageView>(R.id.ImageOwnerNOC)
        radioGroupGender = findViewById<RadioGroup>(R.id.radioGroupGender)
        buttonChooseImage = findViewById<Button>(R.id.buttonChooseImage)
        buttonFinish = findViewById<Button>(R.id.buttonFinish)

        RegisterOwnerDetailsProgressBar = findViewById<ProgressBar>(R.id.OwnerRegisterProgressBar)

        buttonChooseImage!!.setOnClickListener(View.OnClickListener { view: View? -> openFileChooser() })
        buttonFinish!!.setOnClickListener(View.OnClickListener { view: View? -> registerOwner() })
    }

    private fun openFileChooser() {
        val intent = Intent()
        intent.setType("image/*")
        intent.setAction(Intent.ACTION_GET_CONTENT)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData()
            imageOwnerNOC!!.setImageURI(imageUri)
        } else {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show()
        }
    }

    private fun registerOwner() {
        val ownerName = editTextOwnerName!!.getText().toString().trim { it <= ' ' }
        val ownerPhone = editTextOwnerPhone!!.getText().toString().trim { it <= ' ' }
        val gender: String?
        val selectedId = radioGroupGender!!.getCheckedRadioButtonId()
        if (selectedId != -1) {
            val selectedRadioButton = findViewById<RadioButton>(selectedId)
            gender = selectedRadioButton.getText().toString()
        } else {
            // Handle case where no radio button is selected
            Toast.makeText(this, "Please select your gender", Toast.LENGTH_SHORT).show()
            return
        }

        val userType = "Owner"

        // Perform validation
        if (TextUtils.isEmpty(ownerName) || TextUtils.isEmpty(ownerPhone) || TextUtils.isEmpty(
                gender
            ) || imageUri == null
        ) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
            return
        }

        // Get the current user's ID
        val ownerId = firebaseAuth!!.getCurrentUser()!!.getUid()
        RegisterOwnerDetailsProgressBar!!.setVisibility(View.VISIBLE)

        // Upload image to Firebase Storage
        val fileReference = storageReference!!.child(
            (System.currentTimeMillis()
                .toString() + "." + getFileExtension(imageUri!!))
        )
        fileReference.putFile(imageUri!!)
            .addOnSuccessListener(OnSuccessListener { taskSnapshot: UploadTask.TaskSnapshot? ->
                fileReference.getDownloadUrl().addOnSuccessListener(OnSuccessListener { uri: Uri? ->

                    // Get image URL
                    val imageUrl = uri.toString()
                    // Create Owner object with image URL
                    val owner = Owner(ownerId, ownerName, ownerPhone, gender, imageUrl, userType)
                    // Save owner details to the database
                    databaseReference!!.child(ownerId).setValue(owner)
                        .addOnSuccessListener(OnSuccessListener { aVoid: Void? ->
                            val intent = Intent(this@RegisterOwnerDetails, OwnerLogin::class.java)
                            intent.putExtra("userType", userType)
                            Toast.makeText(
                                this@RegisterOwnerDetails,
                                "Owner registered successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                            // You can add further actions here, such as redirecting to another activity
                            RegisterOwnerDetailsProgressBar!!.setVisibility(View.GONE)
                            startActivity(intent)
                        })
                        .addOnFailureListener(OnFailureListener { e: Exception? ->
                            RegisterOwnerDetailsProgressBar!!.setVisibility(View.GONE)
                            Toast.makeText(
                                this@RegisterOwnerDetails,
                                "Failed to register owner: " + e!!.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        })
                })
            })
            .addOnFailureListener(OnFailureListener { e: Exception? ->
                RegisterOwnerDetailsProgressBar!!.setVisibility(View.GONE)
                Toast.makeText(
                    this@RegisterOwnerDetails,
                    "Failed to upload image: " + e!!.message,
                    Toast.LENGTH_SHORT
                ).show()
            })
    }

    private fun getFileExtension(uri: Uri): String? {
        val contentResolver = getContentResolver()
        val mimeTypeMap = MimeTypeMap.getSingleton()
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri))
    }

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }
}
