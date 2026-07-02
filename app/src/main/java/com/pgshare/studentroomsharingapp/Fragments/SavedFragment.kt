package com.pgshare.studentroomsharingapp.Fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.pgshare.studentroomsharingapp.Adapter.RoomListAdapter
import com.pgshare.studentroomsharingapp.RoomDetailsActivity
import com.pgshare.studentroomsharingapp.model.Room
import com.pgshare.studentroomsharingapp.databinding.FragmentSavedBinding
import com.pgshare.studentroomsharingapp.viewmodel.SavedViewModel
import kotlinx.coroutines.launch

class SavedFragment : Fragment() {

    private var _binding: FragmentSavedBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SavedViewModel by viewModels { SavedViewModel.Factory() }

    private lateinit var roomAdapter: RoomListAdapter
    private val savedRoomList = ArrayList<Room>()

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
        observeUiState()
    }

    private fun setupRecyclerView() {
        binding.rvSavedRooms.layoutManager = LinearLayoutManager(requireContext())

        roomAdapter = RoomListAdapter(savedRoomList, object : RoomListAdapter.OnRoomClickListener {
            override fun onRoomClick(room: Room) {
                val intent = Intent(requireContext(), RoomDetailsActivity::class.java).apply {
                    putExtra("Rooms", room)
                }
                startActivity(intent)
            }

            override fun onSaveClick(room: Room) {
                Toast.makeText(binding.root.context, "Not logged in!", Toast.LENGTH_SHORT).show()
            }
        })

        binding.rvSavedRooms.adapter = roomAdapter
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE

                    savedRoomList.clear()
                    savedRoomList.addAll(state.savedRooms)
                    roomAdapter.setOwnerNames(state.ownerNames)
                    roomAdapter.updateData(savedRoomList)

                    if (!state.isLoggedIn || (state.savedRooms.isEmpty() && !state.isLoading)) {
                        binding.rvSavedRooms.visibility = View.GONE
                        binding.layoutEmptyState.visibility = View.VISIBLE
                    } else {
                        binding.rvSavedRooms.visibility = View.VISIBLE
                        binding.layoutEmptyState.visibility = View.GONE
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
