package com.pgshare.studentroomsharingapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.SearchView;
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

public class Display_Room extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference roomRef;
    private RecyclerView roomList;
    private RoomListAdapter adapter;
    private SearchView searchView;
    private ProgressBar DisplayProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_room);

        // Set up SearchView
        searchView = findViewById(R.id.RoomSearchView);
        searchView.clearFocus();
        searchView.setOnQueryTextListener(this);

        // Set up RecyclerView
        roomList = findViewById(R.id.roomList);
        roomList.setLayoutManager(new LinearLayoutManager(this));

        // Set up ProgressBar
        DisplayProgressBar = findViewById(R.id.DisplayRoomProgressBar);

        // Create the adapter with an empty list initially
        adapter = new RoomListAdapter(new ArrayList<>(), Display_Room.this, room -> {
            Intent intent = new Intent(Display_Room.this, RoomDetailsActivity.class);
            intent.putExtra("Rooms", room);
            startActivity(intent);
        });
        roomList.setAdapter(adapter);

        // Fetch room data from Firebase
        roomRef = database.getReference("Rooms");
        fetchRoomDataFromFirebase();
    }

    private void fetchRoomDataFromFirebase() {
        DisplayProgressBar.setVisibility(ProgressBar.VISIBLE);
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

                    // Hide progress bar
                    DisplayProgressBar.setVisibility(ProgressBar.GONE);
                    Log.d("Display_Room", "Room data retrieved successfully: " + roomData);
                } else {
                    // Handle case when there is no data
                    Toast.makeText(Display_Room.this, "No rooms found", Toast.LENGTH_SHORT).show();
                    DisplayProgressBar.setVisibility(ProgressBar.GONE);
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

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        filter(newText);
        return true;
    }

    private void filter(String searchText) {
        List<Room> filteredList = new ArrayList<>();
        for (Room room : adapter.roomData) {
            if (TextUtils.isEmpty(searchText) || roomContainsQuery(room, searchText)) {
                filteredList.add(room);
            }
        }
        adapter.updateData(filteredList);
    }

    private boolean roomContainsQuery(Room room, String query) {
        return room.getRoomName().toLowerCase().contains(query.toLowerCase());
    }
}
