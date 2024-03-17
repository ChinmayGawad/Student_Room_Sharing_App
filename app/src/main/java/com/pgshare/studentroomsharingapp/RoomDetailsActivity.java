package com.pgshare.studentroomsharingapp;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class RoomDetailsActivity extends AppCompatActivity {

    private ImageView roomImageView;
    private TextView roomNameTextView;
    private TextView locationTextView;
    private TextView descriptionTextView;
    private TextView priceTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_details);

        // Find view references
        roomImageView = findViewById(R.id.roomImageView);
        roomNameTextView = findViewById(R.id.roomNameTextView);
        locationTextView = findViewById(R.id.locationTextView);
        descriptionTextView = findViewById(R.id.descriptionTextView);
        priceTextView = findViewById(R.id.priceTextView);

        // Get the Room object from the intent
        Room room = (Room) getIntent().getExtras().get("Rooms"); // Replace "room" with the actual key used

        // Check if room object is not null (handle null case if needed)
        if (room != null) {
            // Set room details
            roomNameTextView.setText(room.getRoomName());
            locationTextView.setText(room.getLocation()); // Assuming Room class has getLocation() method
            descriptionTextView.setText(room.getDescription());
            priceTextView.setText(room.getPrice()); // Assuming Room class has getPriceFormatted() method

            // Download and display image using Glide
            Glide.with(this)
                    .load(room.getImageUrl())
                    .placeholder(R.drawable.imageplaceholder)
                    .error(R.drawable.imageplaceholder)
                    .into(roomImageView);
        } else {
            // Handle case where room object is null (e.g., show error message)
            Log.d("RoomDetailsActivity", "Room object is null");
            Toast.makeText(this, "Failed to load room details", Toast.LENGTH_SHORT).show();
        }
    }
}
