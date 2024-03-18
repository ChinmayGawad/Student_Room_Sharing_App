package com.pgshare.studentroomsharingapp;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pgshare.studentroomsharingapp.Adapter.ImageAdapter;

import java.util.List;

public class RoomDetailsActivity extends AppCompatActivity {

    private TextView roomNameTextView;
    private TextView locationTextView;
    private TextView descriptionTextView;
    private TextView priceTextView;
    private RecyclerView recyclerView;
    private ImageAdapter imageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_details);

        // Find view references
        roomNameTextView = findViewById(R.id.roomNameTextView);
        locationTextView = findViewById(R.id.locationTextView);
        descriptionTextView = findViewById(R.id.descriptionTextView);
        priceTextView = findViewById(R.id.priceTextView);
        recyclerView = findViewById(R.id.imageRecyclerView);

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        imageAdapter = new ImageAdapter();
        recyclerView.setAdapter(imageAdapter);

        // Get the Room object from the intent
        Room room = getIntent().getParcelableExtra("Rooms");

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
                imageAdapter.setImageUrls(imageUrls);
            }
        } else {
            // Handle case where room object is null
            Toast.makeText(this, "Failed to load room details", Toast.LENGTH_SHORT).show();
        }
    }
}
