package com.pgshare.studentroomsharingapp.Authentication;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.pgshare.studentroomsharingapp.Adapter.Owner;
import com.pgshare.studentroomsharingapp.R;

// Import statements omitted for brevity

public class RegisterOwnerDetails extends AppCompatActivity {

    private EditText editTextOwnerName, editTextOwnerPhone;
    private ImageView imageOwnerNOC;
    private RadioGroup radioGroupGender;
    private Button buttonChooseImage, buttonFinish;
    private static final int PICK_IMAGE_REQUEST = 1;
    ProgressBar RegisterOwnerDetailsProgressBar;

    // Firebase
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;

    // Image
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regsiter_owner_details);

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();
        // Initialize Firebase Database
        databaseReference = FirebaseDatabase.getInstance().getReference("Owners");
        // Initialize Firebase Storage
        storageReference = FirebaseStorage.getInstance().getReference("OwnerImages");

        editTextOwnerName = findViewById(R.id.editTextOwnerName);
        editTextOwnerPhone = findViewById(R.id.editTextOwnerPhone);
        imageOwnerNOC = findViewById(R.id.ImageOwnerNOC);
        radioGroupGender = findViewById(R.id.radioGroupGender);
        buttonChooseImage = findViewById(R.id.buttonChooseImage);
        buttonFinish = findViewById(R.id.buttonFinish);

        RegisterOwnerDetailsProgressBar = findViewById(R.id.OwnerRegisterProgressBar);

        buttonChooseImage.setOnClickListener(view -> openFileChooser());
        buttonFinish.setOnClickListener(view -> registerOwner());
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

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            imageOwnerNOC.setImageURI(imageUri);
        }else{
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void registerOwner() {
        String ownerName = editTextOwnerName.getText().toString().trim();
        String ownerPhone = editTextOwnerPhone.getText().toString().trim();
        String gender;
        int selectedId = radioGroupGender.getCheckedRadioButtonId();
        if (selectedId != -1) {
            RadioButton selectedRadioButton = findViewById(selectedId);
            gender = selectedRadioButton.getText().toString();
        } else {
            // Handle case where no radio button is selected
            Toast.makeText(this, "Please select your gender", Toast.LENGTH_SHORT).show();
            return;
        }

        String userType = "Owner";

        // Perform validation
        if (TextUtils.isEmpty(ownerName) || TextUtils.isEmpty(ownerPhone) || TextUtils.isEmpty(gender) || imageUri == null) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get the current user's ID
        String ownerId = firebaseAuth.getCurrentUser().getUid();
        RegisterOwnerDetailsProgressBar.setVisibility(View.VISIBLE);

        // Upload image to Firebase Storage
        StorageReference fileReference = storageReference.child(System.currentTimeMillis()
                + "." + getFileExtension(imageUri));
        fileReference.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {

                    fileReference.getDownloadUrl().addOnSuccessListener(uri -> {

                        // Get image URL
                        String imageUrl = uri.toString();
                        // Create Owner object with image URL
                        Owner owner = new Owner(ownerId, ownerName, ownerPhone, gender, imageUrl, userType);
                        // Save owner details to the database
                        databaseReference.child(ownerId).setValue(owner)
                                .addOnSuccessListener(aVoid -> {
                                    Intent intent = new Intent(RegisterOwnerDetails.this, OwnerLogin.class);
                                    intent.putExtra("userType", userType);
                                    Toast.makeText(RegisterOwnerDetails.this, "Owner registered successfully", Toast.LENGTH_SHORT).show();
                                    // You can add further actions here, such as redirecting to another activity
                                    RegisterOwnerDetailsProgressBar.setVisibility(View.GONE);
                                    startActivity(intent);

                                })
                                .addOnFailureListener(e -> {
                                    RegisterOwnerDetailsProgressBar.setVisibility(View.GONE);
                                    Toast.makeText(RegisterOwnerDetails.this, "Failed to register owner: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    });
                })
                .addOnFailureListener(e -> {
                    RegisterOwnerDetailsProgressBar.setVisibility(View.GONE);
                    Toast.makeText(RegisterOwnerDetails.this, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }
}
