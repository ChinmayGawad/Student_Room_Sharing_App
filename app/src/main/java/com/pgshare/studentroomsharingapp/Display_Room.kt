package com.pgshare.studentroomsharingapp

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.widget.ProgressBar
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.pgshare.studentroomsharingapp.Adapter.Room
import com.pgshare.studentroomsharingapp.Adapter.RoomListAdapter
import java.util.Locale

class Display_Room : AppCompatActivity(), SearchView.OnQueryTextListener {
    private val database = FirebaseDatabase.getInstance()
    var roomData: MutableList<Room> = ArrayList<Room>()
    private var roomRef: DatabaseReference? = null
    private var adapter: RoomListAdapter? = null
    private var DisplayProgressBar: ProgressBar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display_room)
        getSupportActionBar()!!.setBackgroundDrawable(ColorDrawable(getResources().getColor(R.color.C_color)))

        // Set up SearchView
        val searchView = findViewById<SearchView>(R.id.RoomSearchView)
        searchView.clearFocus()
        searchView.setOnQueryTextListener(this)

        // Set up RecyclerView
        val roomList = findViewById<RecyclerView>(R.id.roomList)
        roomList.setLayoutManager(LinearLayoutManager(this))

        // Set up ProgressBar
        DisplayProgressBar = findViewById<ProgressBar>(R.id.DisplayRoomProgressBar)


        // Create the adapter with an empty list initially
        adapter = RoomListAdapter(
            ArrayList<Room?>(),
            this@Display_Room,
            RoomListAdapter.OnItemClickListener { room: Room? ->
                val intent = Intent(this@Display_Room, RoomDetailsActivity::class.java)
                intent.putExtra("Rooms", room)
                startActivity(intent)
            })
        roomList.setAdapter(adapter)

        // Fetch room data from Firebase
        roomRef = database.getReference("Rooms")
        fetchRoomDataFromFirebase()
    }

    private fun fetchRoomDataFromFirebase() {
        DisplayProgressBar!!.setVisibility(ProgressBar.VISIBLE)
        Log.d("Display_Room", "Fetching room data from Firebase")
        roomRef!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("Display_Room", "Data changed")
                roomData.clear()

                if (snapshot.exists()) {
                    for (roomSnapshot in snapshot.getChildren()) {
                        val room = roomSnapshot.getValue<Room?>(Room::class.java)
                        if (room != null) {
                            roomData.add(room)
                        }
                    }

                    // Update adapter with retrieved data
                    adapter!!.updateData(roomData)

                    // Hide progress bar
                    DisplayProgressBar!!.setVisibility(ProgressBar.GONE)
                    Log.d("Display_Room", "Room data retrieved successfully: " + roomData)
                } else {
                    // Handle case when there is no data
                    Toast.makeText(this@Display_Room, "No rooms found", Toast.LENGTH_LONG).show()
                    DisplayProgressBar!!.setVisibility(ProgressBar.GONE)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(
                    "Display_Room",
                    "Error fetching rooms: " + error.getMessage(),
                    error.toException()
                )
                // Handle database read errors
                Toast.makeText(
                    this@Display_Room,
                    "Error fetching rooms: " + error.getMessage(),
                    Toast.LENGTH_SHORT
                ).show()
                Log.w("Display_Room", "Error fetching rooms: ", error.toException())
            }
        })
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return false
    }

    override fun onQueryTextChange(newText: String): Boolean {
        filterList(newText)
        return true
    }

    private fun filterList(searchText: String) {
        val filteredList: MutableList<Room?> = ArrayList<Room?>()
        for (room in roomData) {
            if (room.description.lowercase(Locale.getDefault()).contains(
                    searchText.lowercase(
                        Locale.getDefault()
                    )
                ) ||
                room.location.lowercase(Locale.getDefault())
                    .contains(searchText.lowercase(Locale.getDefault())) || room.roomName
                    .lowercase(
                        Locale.getDefault()
                    ).contains(searchText.lowercase(Locale.getDefault()))
            ) {
                filteredList.add(room)
            }
        }
        if (filteredList.isEmpty()) {
            // Handle case when no results
            Log.d("Display_Room", "No results found for search query: " + searchText)
            Toast.makeText(this, "No rooms found", Toast.LENGTH_SHORT).show()
        } else {
            adapter!!.setFilteredList(filteredList)
        }
    }
}
