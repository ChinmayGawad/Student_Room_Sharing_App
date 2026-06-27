package com.pgshare.studentroomsharingapp.Fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.*
import com.pgshare.studentroomsharingapp.Adapter.Room
import com.pgshare.studentroomsharingapp.Adapter.RoomListAdapter
import com.pgshare.studentroomsharingapp.RoomDetailsActivity
import com.pgshare.studentroomsharingapp.databinding.FragmentExploreBinding

class ExploreFragment : Fragment() {

    private var _binding: FragmentExploreBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private lateinit var databaseRef: DatabaseReference
    private lateinit var roomList: ArrayList<Room>
    private lateinit var roomAdapter: RoomListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExploreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Initialize RecyclerView with a vertical layout manager
        binding.recyclerViewRooms.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewRooms.setHasFixedSize(true)

        roomList = arrayListOf()

        // 2. Initialize Adapter with a trailing lambda to handle item clicks
        roomAdapter = RoomListAdapter(roomList, object : RoomListAdapter.OnRoomClickListener {
            override fun onRoomClick(selectedRoom: Room) {
                val intent = Intent(requireContext(), RoomDetailsActivity::class.java)
                intent.putExtra("Rooms", selectedRoom)
                startActivity(intent)
            }

            override fun onSaveClick(room: Room) {
                TODO("Not yet implemented")
            }
        })

        binding.recyclerViewRooms.adapter = roomAdapter

        // 3. Fetch data
        fetchRoomsFromFirebase()
    }

    private fun fetchRoomsFromFirebase() {
        // Pointing to the "Rooms" node where Add_Room.kt pushes data
        databaseRef = FirebaseDatabase.getInstance().getReference("Rooms")

        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                roomList.clear() // Clear list to prevent duplicates on data refresh

                if (snapshot.exists()) {
                    for (roomSnapshot in snapshot.children) {
                        val room = roomSnapshot.getValue(Room::class.java)
                        if (room != null) {
                            roomList.add(room)
                        }
                    }
                    // Notify the adapter that the Firebase data has been fully loaded
                    roomAdapter.notifyDataSetChanged()
                } else {
                    Log.d("ExploreFragment", "No rooms found in database.")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Safely handle database read failures
                Toast.makeText(
                    requireContext(),
                    "Failed to load rooms: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e("ExploreFragment", "Database error: ${error.message}")
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Prevent memory leaks by nullifying the binding when the view is destroyed
        _binding = null
    }
}