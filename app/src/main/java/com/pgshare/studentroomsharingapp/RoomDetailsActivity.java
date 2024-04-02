package com.pgshare.studentroomsharingapp;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.pgshare.studentroomsharingapp.Adapter.ImageAdapter;
import com.pgshare.studentroomsharingapp.Adapter.Room;

import java.util.List;

public class RoomDetailsActivity extends AppCompatActivity {
    private Button bookRoomButton;
    private boolean isRoomBooked;
    protected Room room;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_details);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.C_color)));

        // Initialize views
        TextView roomNameTextView = findViewById(R.id.roomNameTextView);
        TextView locationTextView = findViewById(R.id.locationTextView);
        TextView descriptionTextView = findViewById(R.id.descriptionTextView);
        TextView priceTextView = findViewById(R.id.priceTextView);
        RecyclerView recyclerView = findViewById(R.id.imageRecyclerView);
        Button chatWithRoomMate = findViewById(R.id.ChatWithRoomMate);
        bookRoomButton = findViewById(R.id.bookRoomButton);

        // Set layout manager for RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        // Get the Room object from the intent
        room = getIntent().getParcelableExtra("Rooms");

        // Check if room object is not null
        if (room != null) {
            // Set room details
            roomNameTextView.setText(room.getRoomName());
            locationTextView.setText(room.getLocation());
            descriptionTextView.setText(room.getDescription());
            priceTextView.setText(room.getFormatPrice());

            // Load images into RecyclerView
            List<String> imageUrls = room.getImageUrls();
            if (imageUrls != null && !imageUrls.isEmpty()) {
                ImageAdapter imageAdapter = new ImageAdapter();
                recyclerView.setAdapter(imageAdapter);
                imageAdapter.setImageUrls(imageUrls);
            }

            // Retrieve booking status of the room from the database
            // Check if the room is booked
            isRoomBooked = room.isRoomBooked(); // Example: Retrieve booked status from Room object
            if (isRoomBooked) {
                // If room is booked, disable the book button and display a message
                bookRoomButton.setText("Room Booked");
                bookRoomButton.setEnabled(false);
            }
        } else {
            // Handle case where room object is null
            Toast.makeText(this, "Failed to load room details", Toast.LENGTH_SHORT).show();
        }

        // Set onClickListener for chat button
        chatWithRoomMate.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra("roomId", room.getId());
            startActivity(intent);
        });
    }

    // Method to handle booking of the room
    public void bookRoom(View view) {
        // Toggle the booking status of the room
        isRoomBooked = !isRoomBooked;

        // Update UI to reflect the booking status
        if (isRoomBooked) {
            // If room is booked, disable the book button and display a message
            bookRoomButton.setText("Room Booked");
            bookRoomButton.setEnabled(false);
        } else {
            // If room is available, enable the book button
            bookRoomButton.setText("Book Room");
            bookRoomButton.setEnabled(true);
        }

        // Update the booked status in the Room object
        room.setRoomBooked(isRoomBooked); // Assuming you have a Room object named 'room'

        // You can also update the booked status in the database here
        // For example, if using Firebase Realtime Database:
        DatabaseReference roomRef = FirebaseDatabase.getInstance().getReference("Rooms").child(room.getId());
        roomRef.child("booked").setValue(isRoomBooked);
    }

    // Method to make the room available again
    public void makeRoomAvailableAgain() {
        // Assuming you have the room ID available
        String roomId = room.getId(); // Get the room ID from the Room object

        // Update the booked status in the database to false
        DatabaseReference roomRef = FirebaseDatabase.getInstance().getReference("Rooms").child(roomId);
        roomRef.child("booked").setValue(false); // Update the 'booked' field to false
    }
}