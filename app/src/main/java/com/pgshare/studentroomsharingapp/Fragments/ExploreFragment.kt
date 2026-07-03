package com.pgshare.studentroomsharingapp.Fragments

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.pgshare.studentroomsharingapp.R
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import com.pgshare.studentroomsharingapp.Adapter.RoomListAdapter
import com.pgshare.studentroomsharingapp.RoomDetailsActivity
import com.pgshare.studentroomsharingapp.databinding.FragmentExploreBinding
import com.pgshare.studentroomsharingapp.model.Room
import com.pgshare.studentroomsharingapp.viewmodel.ExploreViewModel
import kotlinx.coroutines.launch

class ExploreFragment : Fragment() {

    private var _binding: FragmentExploreBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ExploreViewModel by viewModels { ExploreViewModel.Factory() }

    private lateinit var roomAdapter: RoomListAdapter
    private val displayRoomList = ArrayList<Room>()

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
        setupSearch()
        observeUiState()
    }

    private fun setupSearch() {
        binding.searchText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.search(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupRecyclerView() {
        binding.recyclerViewRooms.layoutManager = LinearLayoutManager(requireContext())

        roomAdapter = RoomListAdapter(displayRoomList, object : RoomListAdapter.OnRoomClickListener {
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
        binding.chipAny.isChecked = true
        binding.chipGroupFilters.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isEmpty()) {
                binding.chipAny.isChecked = true
                viewModel.clearFilter()
            } else {
                val selectedChipId = checkedIds.first()
                val chip = group.findViewById<Chip>(selectedChipId)
                val filterText = chip.text.toString()

                if (filterText == getString(R.string.categories_any)) {
                    viewModel.clearFilter()
                } else {
                    viewModel.applyFilter(filterText)
                }
            }
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.progressBarLoading.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                    binding.recyclerViewRooms.visibility = if (state.isLoading) View.GONE else View.VISIBLE

                    displayRoomList.clear()
                    displayRoomList.addAll(state.displayRooms)
                    roomAdapter.setOwnerNames(state.ownerNames)
                    roomAdapter.setSavedRoomKeys(state.savedRoomKeys)
                    roomAdapter.notifyDataSetChanged()

                    if (state.displayRooms.isEmpty() && !state.isLoading) {
                        binding.recyclerViewRooms.visibility = View.GONE
                        binding.tvEmptyState.visibility = View.VISIBLE
                    } else {
                        binding.recyclerViewRooms.visibility = View.VISIBLE
                        binding.tvEmptyState.visibility = View.GONE
                    }
                }
            }
        }
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
