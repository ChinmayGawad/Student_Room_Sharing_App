package com.pgshare.studentroomsharingapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.viewpager.widget.ViewPager
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.pgshare.studentroomsharingapp.Adapter.Room
import com.pgshare.studentroomsharingapp.Adapter.ViewPagerAdapter

class Add_Room : AppCompatActivity() {
    private var editTextRoomName: EditText? = null
    private var editTextLocation: EditText? = null
    private var editTextPrice: EditText? = null
    private var editTextDescription: EditText? = null
    private var BtnGallery: Button? = null
    private var SaveRoom: Button? = null
    private var imageContainer: ViewPager? = null
    private var ImageUri: Uri? = null
    private var imageList: ArrayList<Uri>? = null
    private var progressBar: ProgressBar? = null
    private var databaseReference: DatabaseReference? = null
    private var firebaseAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_room)
        getSupportActionBar()!!.setBackgroundDrawable(ColorDrawable(getResources().getColor(R.color.C_color)))

        editTextRoomName = findViewById<EditText>(R.id.editTextRoomName)
        editTextLocation = findViewById<EditText>(R.id.editTextLocation)
        editTextDescription = findViewById<EditText>(R.id.editTextDescription)
        editTextPrice = findViewById<EditText>(R.id.editTextPrice)
        BtnGallery = findViewById<Button>(R.id.BtnGallery)
        SaveRoom = findViewById<Button>(R.id.buttonSave)
        imageContainer = findViewById<ViewPager>(R.id.viewPager)

        //progressBar
        progressBar = findViewById<ProgressBar>(R.id.Add_roomProgressBar)

        //imageList
        imageList = ArrayList<Uri>()

        databaseReference = FirebaseDatabase.getInstance().getReference("Rooms")
        firebaseAuth = FirebaseAuth.getInstance()


        BtnGallery!!.setOnClickListener(View.OnClickListener { view: View? ->
            CheckPermission()
            pickImageFromGallery()
        })

        SaveRoom!!.setOnClickListener(View.OnClickListener { view: View? ->
            val currentUser = firebaseAuth!!.getCurrentUser()
            if (currentUser != null) {
                if (ValidateInput()) {
                    isCurrentUserOwner(currentUser)
                }
            } else {
                // Handle case where user is not logged in
                Toast.makeText(this, "Please log in to add a room", Toast.LENGTH_SHORT).show()
                val intent = Intent(this@Add_Room, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        })
    }

    private fun isCurrentUserOwner(user: FirebaseUser) {
        val usersRef = FirebaseDatabase.getInstance().getReference("Owners")
        usersRef.child(user.getUid()).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val userType =
                        dataSnapshot.child("userType").getValue<String?>(String::class.java)
                    if (userType != null && userType == "Owner") {
                        // User is an owner
                        saveToDatabase()
                    } else {
                        // User is not an owner
                        Toast.makeText(
                            this@Add_Room,
                            "Only owners can add rooms",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    // User data does not exist
                    Toast.makeText(this@Add_Room, "User data not found", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(databaseError: DatabaseError?) {
                // Handle error
                Toast.makeText(this@Add_Room, "Database error", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun ValidateInput(): Boolean {
        if (editTextRoomName!!.getText().toString().isEmpty()) {
            editTextRoomName!!.setError("Room name is required")
            return false
        }

        if (editTextLocation!!.getText().toString().isEmpty()) {
            editTextLocation!!.setError("Location is required")
            return false
        }

        if (editTextDescription!!.getText().toString().isEmpty()) {
            editTextDescription!!.setError("Description is required")
            return false
        }

        if (editTextPrice!!.getText().toString().isEmpty()) {
            editTextPrice!!.setError("Price is required")
            return false
        }


        if (imageList!!.isEmpty()) {
            Toast.makeText(this, "Please add at least one image", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun saveToDatabase() {
        val roomName = editTextRoomName!!.getText().toString().trim { it <= ' ' }
        val location = editTextLocation!!.getText().toString().trim { it <= ' ' }
        val description = editTextDescription!!.getText().toString()
            .trim { it <= ' ' }  // Add logic to get description if needed
        val price = editTextPrice!!.getText().toString().trim { it <= ' ' }


        // Generate a unique key for the room
        val roomId = databaseReference!!.push().getKey()


        if (roomId != null) {
            // Save room details to the database

            val room = Room(
                roomId,
                roomName,
                location,
                description,
                price,
                ArrayList<String?>(),
                0
            ) // Set imageUrl and imageResourceId as needed
            databaseReference!!.child(roomId).setValue(room)

            // Upload images to Firebase Storage and save their URLs in the database
            for (i in imageList!!.indices) {
                val imageUri = imageList!!.get(i)
                uploadImageToStorage(roomId, i, imageUri, room)
            }

            progressBar!!.setVisibility(View.GONE)
            // Clear input fields
            editTextRoomName!!.setText("")
            editTextLocation!!.setText("")
            editTextPrice!!.setText("")
            editTextDescription!!.setText("")
            // Clear the image list
            imageContainer!!.removeAllViews()
        } else {
            progressBar!!.setVisibility(View.GONE)
            // Handle failure to generate room ID
            Toast.makeText(this, "Failed to generate room ID", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadImageToStorage(roomId: String, i: Int, imageUri: Uri, room: Room) {
        val storageReference = FirebaseStorage.getInstance().getReference()
        val fileReference = storageReference.child(
            "RoomImages/" + roomId + "/" + i + "." + getFileExtension(imageUri)
        )
        fileReference.putFile(imageUri)
            .addOnSuccessListener(OnSuccessListener { taskSnapshot: UploadTask.TaskSnapshot? ->
                // Image uploaded successfully, get download URL
                fileReference.getDownloadUrl().addOnSuccessListener(OnSuccessListener { uri: Uri? ->
                    val imageUrl = uri.toString()
                    // Update the Room object with the image URL
                    if (room.getImageUrls() == null) {
                        room.setImageUrls(ArrayList<String?>())
                    }
                    room.getImageUrls().add(imageUrl)

                    // Save the updated Room object to the database
                    databaseReference!!.child(roomId).setValue(room)
                    Toast.makeText(this, "Room saved successfully!", Toast.LENGTH_SHORT).show()
                })
            })
            .addOnFailureListener(OnFailureListener { e: Exception? ->
                // Handle image upload failure
                Toast.makeText(
                    this@Add_Room,
                    "Failed to upload image: " + e!!.message,
                    Toast.LENGTH_SHORT
                ).show()
            })
    }


    private fun getFileExtension(imageUri: Uri): String? {
        val contentResolver = getContentResolver()
        val mimeTypeMap = MimeTypeMap.getSingleton()
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(imageUri))
    }

    private fun CheckPermission() {
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf<String>(Manifest.permission.READ_EXTERNAL_STORAGE),
                1
            )
        } else {
            pickImageFromGallery()
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent()
        intent.setType("image/*")
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        intent.setAction(Intent.ACTION_GET_CONTENT)
        startActivityForResult(intent, 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getClipData() != null) {
            val count = data.getClipData()!!.getItemCount()
            for (i in 0..<count) {
                ImageUri = data.getClipData()!!.getItemAt(i).getUri()
                imageList!!.add(ImageUri!!)

                SetAdapter()
            }
        }
    }

    private fun SetAdapter() {
        val viewPagerAdapter = ViewPagerAdapter(this, imageList)
        imageContainer!!.setAdapter(viewPagerAdapter)
    }
}
