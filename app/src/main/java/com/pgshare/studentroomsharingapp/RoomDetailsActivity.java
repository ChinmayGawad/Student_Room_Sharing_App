package com.pgshare.studentroomsharingapp;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;

public class RoomDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_details);

        // Get the Room object from the Intent
        Room room = getIntent().getParcelableExtra("room");

        // Set up the views
        TextView roomNameTextView = findViewById(R.id.roomNameTextView);
        TextView locationTextView = findViewById(R.id.locationTextView);
        TextView descriptionTextView = findViewById(R.id.descriptionTextView);
        TextView priceTextView = findViewById(R.id.priceTextView);
        ImageView roomImageView = findViewById(R.id.roomImageView);

        // Display Room details
        roomNameTextView.setText(room.getRoomName());
        locationTextView.setText(room.getLocation());
        descriptionTextView.setText(room.getDescription());
        priceTextView.setText(room.getPrice());

        // Load image using Glide library (make sure to add the dependency)
        if (room.getImageUrl() != null && !room.getImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(room.getImageUrl())
                    .into(roomImageView);
        }
    }
}
