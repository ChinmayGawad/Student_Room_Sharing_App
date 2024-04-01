package com.pgshare.studentroomsharingapp;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.viewpager.widget.ViewPager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.pgshare.studentroomsharingapp.Adapter.Room;
import com.pgshare.studentroomsharingapp.Adapter.ViewPagerAdapter;

import java.util.ArrayList;

public class Add_Room extends AppCompatActivity {

    private EditText editTextRoomName, editTextLocation, editTextPrice, editTextDescription;
    private Button BtnGallery, SaveRoom;
    private ViewPager imageContainer;
    private Uri ImageUri;
    private ArrayList<Uri> imageList;
    private ProgressBar progressBar;
    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_room);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.C_color)));

        editTextRoomName = findViewById(R.id.editTextRoomName);
        editTextLocation = findViewById(R.id.editTextLocation);
        editTextDescription = findViewById(R.id.editTextDescription);
        editTextPrice = findViewById(R.id.editTextPrice);
        BtnGallery = findViewById(R.id.BtnGallery);
        SaveRoom = findViewById(R.id.buttonSave);
        imageContainer = findViewById(R.id.viewPager);

        //progressBar
        progressBar = findViewById(R.id.Add_roomProgressBar);

        //imageList
        imageList = new ArrayList<>();

        databaseReference = FirebaseDatabase.getInstance().getReference("Rooms");
        firebaseAuth = FirebaseAuth.getInstance();


        BtnGallery.setOnClickListener(view -> {
            CheckPermission();
            pickImageFromGallery();
        });

        SaveRoom.setOnClickListener(view -> {
            FirebaseUser currentUser = firebaseAuth.getCurrentUser();
            if (currentUser != null ) {
                if (ValidateInput()) {
                    isCurrentUserOwner(currentUser);
                }
            } else {
                // Handle case where user is not logged in
                Toast.makeText(this, "Please log in to add a room", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Add_Room.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });



    }

    private void isCurrentUserOwner(FirebaseUser user) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Owners");
        usersRef.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String userType = dataSnapshot.child("userType").getValue(String.class);
                    if (userType != null && userType.equals("Owner")) {
                        // User is an owner
                        saveToDatabase();
                    } else {
                        // User is not an owner
                        Toast.makeText(Add_Room.this, "Only owners can add rooms", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // User data does not exist
                    Toast.makeText(Add_Room.this, "User data not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
                Toast.makeText(Add_Room.this, "Database error", Toast.LENGTH_SHORT).show();
            }
        });
    }



    private boolean ValidateInput() {

        if (editTextRoomName.getText().toString().isEmpty()) {
            editTextRoomName.setError("Room name is required");
            return false;
        }

        if (editTextLocation.getText().toString().isEmpty()) {
            editTextLocation.setError("Location is required");
            return false;
        }

        if (editTextDescription.getText().toString().isEmpty()) {
            editTextDescription.setError("Description is required");
            return false;
        }

        if (editTextPrice.getText().toString().isEmpty()) {
            editTextPrice.setError("Price is required");
            return false;
        }



        if (imageList.isEmpty()) {
            Toast.makeText(this, "Please add at least one image", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void saveToDatabase() {
        String roomName = editTextRoomName.getText().toString().trim();
        String location = editTextLocation.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim(); // Add logic to get description if needed
        String price = editTextPrice.getText().toString().trim();




        // Generate a unique key for the room
        String roomId = databaseReference.push().getKey();


        if (roomId != null) {

            // Save room details to the database

            Room room = new Room(roomId, roomName, location, description, price, new ArrayList<>(), 0); // Set imageUrl and imageResourceId as needed
            databaseReference.child(roomId).setValue(room);

            // Upload images to Firebase Storage and save their URLs in the database
            for (int i = 0; i < imageList.size(); i++) {
                Uri imageUri = imageList.get(i);
                uploadImageToStorage(roomId, i, imageUri, room);
            }

            progressBar.setVisibility(View.GONE);
            // Clear input fields
            editTextRoomName.setText("");
            editTextLocation.setText("");
            editTextPrice.setText("");
            editTextDescription.setText("");
            // Clear the image list
            imageContainer.removeAllViews();
        }
        else {
            progressBar.setVisibility(View.GONE);
            // Handle failure to generate room ID
            Toast.makeText(this, "Failed to generate room ID", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadImageToStorage(String roomId, int i, Uri imageUri, Room room) {
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

                        Toast.makeText(this, "Room saved successfully!", Toast.LENGTH_SHORT).show();
                    });
                })
                .addOnFailureListener(e -> {
                    // Handle image upload failure
                    Toast.makeText(Add_Room.this, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
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
