package com.pgshare.studentroomsharingapp;// Import statements...

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Display_Room extends AppCompatActivity {

    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference roomRef;
    private RecyclerView roomList;
    private RoomListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_room); // Replace with your actual layout

        roomList = findViewById(R.id.roomList); // Replace with your RecyclerView ID
        roomList.setLayoutManager(new LinearLayoutManager(this)); // Set layout manager

        // Create the adapter with an empty list initially
        adapter = new RoomListAdapter(new ArrayList<>(), Display_Room.this, room -> {
            // Handle room click event
            Intent intent = new Intent(Display_Room.this, RoomDetailsActivity.class);
            // Pass the room object to the RoomDetailsActivity
            intent.putExtra("Rooms", room);
            startActivity(intent);
        });
        roomList.setAdapter(adapter); // Set the adapter to the RecyclerView

        // Fetch room data from Firebase
        roomRef = database.getReference("Rooms"); // Replace "rooms" with your actual data path
        fetchRoomDataFromFirebase();
    }

    private void fetchRoomDataFromFirebase() {
        Log.d("Display_Room", "Fetching room data from Firebase");
        roomRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Log.d("Display_Room", "Data changed");
                List<Room> roomData = new ArrayList<>();
                if (snapshot.exists()) {
                    for (DataSnapshot roomSnapshot : snapshot.getChildren()) {
                        Room room = roomSnapshot.getValue(Room.class);
                        if (room != null) {
                            roomData.add(room);
                        }
                    }
                    // Update adapter with retrieved data
                    adapter.updateData(roomData);
                } else {
                    // Handle case when there is no data
                    Toast.makeText(Display_Room.this, "No rooms found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("Display_Room", "Error fetching rooms: " + error.getMessage(), error.toException());
                // Handle database read errors
                Toast.makeText(Display_Room.this, "Error fetching rooms: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.w("Display_Room", "Error fetching rooms: ", error.toException());
            }
        });
    }
}
