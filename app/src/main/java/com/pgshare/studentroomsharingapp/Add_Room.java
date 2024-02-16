package com.pgshare.studentroomsharingapp;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class Add_Room extends AppCompatActivity {

    private EditText editTextRoomName, editTextLocation, editTextDescription, editTextPrice;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private Uri imageUri;

    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_room);

        editTextRoomName = findViewById(R.id.editTextRoomName);
        editTextLocation = findViewById(R.id.editTextLocation);
        editTextDescription = findViewById(R.id.editTextDescription);
        editTextPrice = findViewById(R.id.editTextPrice);
        Button buttonSelectImage = findViewById(R.id.buttonSelectImages);
        Button buttonSave = findViewById(R.id.buttonSave);

        databaseReference = FirebaseDatabase.getInstance().getReference("Rooms");
        storageReference = FirebaseStorage.getInstance().getReference("RoomImages");

        buttonSelectImage.setOnClickListener(v -> openFileChooser());
        buttonSave.setOnClickListener(v -> saveRoom());
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            imageUri = data.getData();
        }
    }

    private void saveRoom() {
        String roomName = editTextRoomName.getText().toString().trim();
        String location = editTextLocation.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();
        String price = editTextPrice.getText().toString().trim();

        if (roomName.isEmpty()) {
            editTextRoomName.setError("Room name required");
            editTextRoomName.requestFocus();
            return;
        } else if (location.isEmpty()) {
            editTextLocation.setError("Room name required");
            editTextLocation.requestFocus();
            return;
        }else if (description.isEmpty()) {
            editTextDescription.setError("Room name required");
            editTextDescription.requestFocus();
            return;
        } else if (price.isEmpty()) {
            editTextPrice.setError("Room name required");
            editTextPrice.requestFocus();
            return;
        }else if (imageUri == null) {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
            return;
        }else {

        StorageReference fileReference = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(imageUri));
        fileReference.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Get the download URL and save room details with image URL
                    fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        String id = databaseReference.push().getKey();
                        Room room = new Room(id, roomName, location, description, price, imageUrl);
                        databaseReference.child(id).setValue(room)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(Add_Room.this, "Room saved", Toast.LENGTH_SHORT).show();
                                        // Clear EditText fields
                                        editTextRoomName.setText("");
                                        editTextLocation.setText("");
                                        editTextDescription.setText("");
                                        editTextPrice.setText("");
                                    } else {
                                        Toast.makeText(Add_Room.this, "Failed to save room", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(Add_Room.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
        }
    }

    // Method to get the file extension of the image
    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }
}
