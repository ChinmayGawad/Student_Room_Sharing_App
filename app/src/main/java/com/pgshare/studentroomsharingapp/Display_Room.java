package com.pgshare.studentroomsharingapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pgshare.studentroomsharingapp.Adapter.Room;
import com.pgshare.studentroomsharingapp.Adapter.RoomListAdapter;

import java.util.ArrayList;
import java.util.List;

public class Display_Room extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    List<Room> roomData = new ArrayList<>();
    private DatabaseReference roomRef;
    private RoomListAdapter adapter;
    private ProgressBar DisplayProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_room);

        // Set up SearchView
        SearchView searchView = findViewById(R.id.RoomSearchView);
        searchView.clearFocus();
        searchView.setOnQueryTextListener(this);

        // Set up RecyclerView
        RecyclerView roomList = findViewById(R.id.roomList);
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
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("Display_Room", "Data changed");
                roomData.clear();

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
                    Toast.makeText(Display_Room.this, "No rooms found", Toast.LENGTH_LONG).show();
                    DisplayProgressBar.setVisibility(ProgressBar.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
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
        filterList(newText);
        return true;
    }

    private void filterList(String searchText) {
        List<Room> filteredList = new ArrayList<>();
        for (Room room : roomData) {
            if (room.getDescription().toLowerCase().contains(searchText.toLowerCase()) ||
                    room.getLocation().toLowerCase().contains(searchText.toLowerCase())) {
                filteredList.add(room);
            }
        }
        if (filteredList.isEmpty()) {
            // Handle case when no results
            Log.d("Display_Room", "No results found for search query: " + searchText);
            Toast.makeText(this, "No rooms found", Toast.LENGTH_SHORT).show();
        } else {
            adapter.setFilteredList(filteredList);
        }
    }

}
