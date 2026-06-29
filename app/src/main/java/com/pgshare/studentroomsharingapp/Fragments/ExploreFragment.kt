package com.pgshare.studentroomsharingapp.Fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.pgshare.studentroomsharingapp.Adapter.Room
import com.pgshare.studentroomsharingapp.Adapter.RoomListAdapter
import com.pgshare.studentroomsharingapp.RoomDetailsActivity
import com.pgshare.studentroomsharingapp.databinding.FragmentExploreBinding

class ExploreFragment : Fragment() {

    private var _binding: FragmentExploreBinding? = null
    private val binding get() = _binding!!

    private lateinit var roomAdapter: RoomListAdapter

    // Master list holds everything from Firebase
    private val allRoomsList = ArrayList<Room>()
    // Display list holds what the user actually sees based on filters
    private val displayRoomList = ArrayList<Room>()

    private val databaseReference = FirebaseDatabase.getInstance().getReference("Rooms")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExploreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupFilters()
        fetchRoomListings()
    }

    private fun setupRecyclerView() {
        binding.recyclerViewRooms.layoutManager = LinearLayoutManager(requireContext())

        // Pass the display list to the adapter, not the master list
        roomAdapter = RoomListAdapter(displayRoomList,object : RoomListAdapter.OnRoomClickListener {
            override fun onRoomClick(room: Room) {
                navigateToDetails(room)
            }

            override fun onSaveClick(room: Room) {
                if (FirebaseAuth.getInstance().currentUser == null) {
                    Toast.makeText(requireContext(), "Please login to save rooms", Toast.LENGTH_SHORT).show()
                }
            }
        })
        binding.recyclerViewRooms.adapter = roomAdapter
    }

    private fun setupFilters() {
        // Listen for when the user taps on any filter chip
        binding.chipGroupFilters.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isEmpty()) {
                // No chip selected, show all rooms
                updateDisplayList(allRoomsList)
            } else {
                // Single line chip group returns one ID in the list
                val selectedChipId = checkedIds.first()
                val selectedChip = group.findViewById<Chip>(selectedChipId)
                val filterCriteria = selectedChip.text.toString()

                applyFilter(filterCriteria)
            }
        }
    }

    private fun applyFilter(criteria: String) {
        val filteredList = ArrayList<Room>()

        for (room in allRoomsList) {
            when (criteria) {
                "Under ₹8,000" -> {
                    // Assuming your Room object has a price property stored as a String or Int
                    // Adjust this logic to match your exact variable name (e.g., room.rent)
                    val price = room.price?.toString()?.replace("[^0-9]".toRegex(), "")?.toIntOrNull() ?: 0
                    if (price in 1..8000) {
                        filteredList.add(room)
                    }
                }
                "Private Room" -> {
                    // Based on your setup, room type is stored in the description field
                    if (room.description?.contains("Private Room", ignoreCase = true) == true) {
                        filteredList.add(room)
                    }
                }
                "AC" -> {
                    // Check if AC is in amenities or description
                    if (room.description?.contains("AC", ignoreCase = true) == true) {
                        filteredList.add(room)
                    }
                }
                "Student Friendly" -> {
                    if (room.description?.contains("Student", ignoreCase = true) == true) {
                        filteredList.add(room)
                    }
                }
            }
        }

        updateDisplayList(filteredList)
    }

    private fun updateDisplayList(newList: List<Room>) {
        displayRoomList.clear()
        displayRoomList.addAll(newList)
        roomAdapter.notifyDataSetChanged()

        // Toggle empty state if the filter resulted in 0 matches
        if (displayRoomList.isEmpty()) {
            binding.recyclerViewRooms.visibility = View.GONE
            binding.tvEmptyState.visibility = View.VISIBLE
            binding.tvEmptyState.text = "No rooms match your filter."
        } else {
            binding.recyclerViewRooms.visibility = View.VISIBLE
            binding.tvEmptyState.visibility = View.GONE
        }
    }

    private fun fetchRoomListings() {
        binding.progressBarLoading.visibility = View.VISIBLE
        binding.recyclerViewRooms.visibility = View.GONE
        binding.tvEmptyState.visibility = View.GONE

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                // 1. SAFETY CHECK (Perfectly executed)
                if (!isAdded || _binding == null) {
                    return
                }

                allRoomsList.clear()
                for (dataSnapshot in snapshot.children) {
                    val room = dataSnapshot.getValue(Room::class.java)
                    room?.let { allRoomsList.add(it) }
                }

                // 2. HIDE the loading spinner now that data has arrived
                binding.progressBarLoading.visibility = View.GONE

                // 3. CHECK if a filter is currently active
                val checkedIds = binding.chipGroupFilters.checkedChipIds
                if (checkedIds.isEmpty()) {
                    // No filter active, show everything!
                    updateDisplayList(allRoomsList)
                } else {
                    // Re-apply the active filter to the newly downloaded data
                    val selectedChip = binding.chipGroupFilters.findViewById<Chip>(checkedIds.first())
                    applyFilter(selectedChip.text.toString())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Safely hide the progress bar and show the error
                if (isAdded && _binding != null) {
                    binding.progressBarLoading.visibility = View.GONE
                    Toast.makeText(requireContext(), "Failed to load: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun navigateToDetails(room: Room) {
        val intent = Intent(requireContext(), RoomDetailsActivity::class.java).apply {
            putExtra("Rooms", room)
        }
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}