package com.pgshare.studentroomsharingapp;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.pgshare.studentroomsharingapp.ChatActivity;
import com.pgshare.studentroomsharingapp.Adapter.ImageAdapter;
import com.pgshare.studentroomsharingapp.Adapter.Room;

import java.util.List;

public class RoomDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_details);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.C_color)));

        // Find view references
        TextView roomNameTextView = findViewById(R.id.roomNameTextView);
        TextView locationTextView = findViewById(R.id.locationTextView);
        TextView descriptionTextView = findViewById(R.id.descriptionTextView);
        TextView priceTextView = findViewById(R.id.priceTextView);
        RecyclerView recyclerView = findViewById(R.id.imageRecyclerView);
        Button chatWithRoomMate = findViewById(R.id.ChatWithRoomMate);

        // Set layout manager for RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));



        ImageAdapter imageAdapter = new ImageAdapter();
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

        chatWithRoomMate.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra("roomId", room.getId()); // Pass room ID instead of entire Room object
            startActivity(intent);
        });

    }
}