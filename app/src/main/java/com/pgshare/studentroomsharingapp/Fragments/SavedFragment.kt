package com.pgshare.studentroomsharingapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.pgshare.studentroomsharingapp.Adapter.Room
import com.pgshare.studentroomsharingapp.Adapter.RoomListAdapter
import com.pgshare.studentroomsharingapp.databinding.FragmentSavedBinding

class SavedFragment : Fragment() {

    private var _binding: FragmentSavedBinding? = null
    private val binding get() = _binding!!

    private lateinit var roomAdapter: RoomListAdapter
    private val savedRoomList = ArrayList<Room>()

    // Grab the authenticated user's ID
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    // Point the reference to the users node
    private val databaseReference = FirebaseDatabase.getInstance().getReference("Users")
    private var favoritesListener: ValueEventListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSavedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        fetchSavedRooms()
    }

    private fun setupRecyclerView() {
        binding.rvSavedRooms.layoutManager = LinearLayoutManager(requireContext())

        // We use your exact adapter and interface here
        roomAdapter = RoomListAdapter(savedRoomList, object : RoomListAdapter.OnRoomClickListener {
            override fun onRoomClick(room: Room) {
                // Navigate to details just like the Explore Feed
                val intent = Intent(requireContext(), RoomDetailsActivity::class.java).apply {
                    putExtra("Rooms", room)
                }
                startActivity(intent)
            }

            override fun onSaveClick(room: Room) {
                if (currentUserId == null) {
                    Toast.makeText(binding.root.context, "Not logged in!", Toast.LENGTH_SHORT).show()
                }
            }
        })

        binding.rvSavedRooms.adapter = roomAdapter
    }

    private fun fetchSavedRooms() {
        if (currentUserId == null) {
            // Failsafe: User isn't logged in, show the empty state immediately
            toggleEmptyState(true)
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.layoutEmptyState.visibility = View.GONE
        binding.rvSavedRooms.visibility = View.GONE

        // Listen exclusively to this specific user's "favorites" node
        favoritesListener = databaseReference.child(currentUserId).child("favorites")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (_binding == null) return
                    savedRoomList.clear()

                    if (snapshot.exists()) {
                        for (roomSnapshot in snapshot.children) {
                            try {
                                val room = roomSnapshot.getValue(Room::class.java)
                                if (room != null) {
                                    savedRoomList.add(room)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                Toast.makeText(requireContext(), "Error converting room: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    }

                    // Push the fresh data to your adapter using your custom updateData function
                    roomAdapter.updateData(savedRoomList)

                    // Toggle UI states based on if the list is empty
                    toggleEmptyState(savedRoomList.isEmpty())
                }

                override fun onCancelled(error: DatabaseError) {
                    _binding?.progressBar?.visibility = View.GONE
                    context?.let {
                        Toast.makeText(it, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            })
    }

    private fun toggleEmptyState(isEmpty: Boolean) {
        _binding?.apply {
            progressBar.visibility = View.GONE
            if (isEmpty) {
                rvSavedRooms.visibility = View.GONE
                layoutEmptyState.visibility = View.VISIBLE
            } else {
                rvSavedRooms.visibility = View.VISIBLE
                layoutEmptyState.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Remove the listener to prevent memory leaks and crashes
        favoritesListener?.let {
            currentUserId?.let { uid ->
                databaseReference.child(uid).child("favorites").removeEventListener(it)
            }
        }
        _binding = null
    }
}