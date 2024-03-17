package com.pgshare.studentroomsharingapp;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.viewpager.widget.ViewPager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class Add_Room extends AppCompatActivity {

    private EditText editTextRoomName, editTextLocation, editTextPrice, editTextDescription;
    private Button BtnGallery, SaveRoom;
    private ViewPager imageContainer;
    private Uri ImageUri;
    private ArrayList<Uri> imageList;

    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_room);

        editTextRoomName = findViewById(R.id.editTextRoomName);
        editTextLocation = findViewById(R.id.editTextLocation);
        editTextDescription = findViewById(R.id.editTextDescription);
        editTextPrice = findViewById(R.id.editTextPrice);
        BtnGallery = findViewById(R.id.BtnGallery);
        SaveRoom = findViewById(R.id.buttonSave);
        imageContainer = findViewById(R.id.viewPager);


        //imageList
        imageList = new ArrayList<>();

        databaseReference = FirebaseDatabase.getInstance().getReference("Rooms");
        firebaseAuth = FirebaseAuth.getInstance();


        BtnGallery.setOnClickListener(view -> {
            CheckPermission();
            pickImageFromGallery();
        });

        SaveRoom.setOnClickListener(view -> {
            // Check if the user is logged in
            FirebaseUser currentUser = firebaseAuth.getCurrentUser();
            if (currentUser != null) {
                saveToDatabase();
            } else {
                // Handle case where user is not logged in
                Toast.makeText(this, "Please log in to save the room", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Add_Room.this, Login.class);
                startActivity(intent);
            }

        });






    }

    private void saveToDatabase() {
        String roomName = editTextRoomName.getText().toString().trim();
        String location = editTextLocation.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim(); // Add logic to get description if needed
        String price = editTextPrice.getText().toString().trim();

        if (TextUtils.isEmpty(roomName) || TextUtils.isEmpty(location) || TextUtils.isEmpty(price) || imageList.isEmpty()) {
            // Handle case where any required field is empty or no images are selected
            Toast.makeText(this, "Please fill in all fields and select at least one image", Toast.LENGTH_SHORT).show();
            return;
        }

        // Generate a unique key for the room
        String roomId = databaseReference.push().getKey();

        if (roomId != null) {
            // Save room details to the database
            Room room = new Room(roomId, roomName, location, description, price, new ArrayList<>(), 0); // Set imageUrl and imageResourceId as needed
            databaseReference.child(roomId).setValue(room);

            // Upload images to Firebase Storage and save their URLs in the database
            for (int i = 0; i < imageList.size(); i++) {
                Uri imageUri = imageList.get(i);
                StorageReference storageReference = FirebaseStorage.getInstance().getReference();
                StorageReference fileReference = storageReference.child("RoomImages/" + roomId + "/" + i + "." + getFileExtension(imageUri));
                fileReference.putFile(imageUri)
                        .addOnSuccessListener(taskSnapshot -> {
                            // Image uploaded successfully, get download URL
                            fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                                String imageUrl = uri.toString();

                                // Update the Room object with the image URL
                                if (room.getImageUrls() == null) {
                                    room.setImageUrls(new ArrayList<>());
                                }
                                room.getImageUrls().add(imageUrl);


                                // Save the updated Room object to the database
                                databaseReference.child(roomId).setValue(room);
                            });
                        })
                        .addOnFailureListener(e -> {
                            // Handle image upload failure
                            Toast.makeText(Add_Room.this, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }

            // Clear input fields
            editTextRoomName.setText("");
            editTextLocation.setText("");
            editTextPrice.setText("");
            editTextDescription.setText("");
            // Clear the image list
            imageList.clear();
        }
    }


    private String getFileExtension(Uri imageUri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(imageUri));
    }

    private void CheckPermission() {
        if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        } else {
            pickImageFromGallery();
        }

    }

    private void pickImageFromGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getClipData() != null) {
            int count = data.getClipData().getItemCount();
            for (int i = 0; i < count; i++) {
               ImageUri = data.getClipData().getItemAt(i).getUri();
               imageList.add(ImageUri);

           SetAdapter();
            }
        }
    }

    private void SetAdapter() {
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(this, imageList);
        imageContainer.setAdapter(viewPagerAdapter);

    }
}
