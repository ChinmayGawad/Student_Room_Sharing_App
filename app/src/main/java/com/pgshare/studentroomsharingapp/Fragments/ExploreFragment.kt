package com.pgshare.studentroomsharingapp.Fragments

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import com.pgshare.studentroomsharingapp.R
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.chip.Chip
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
        setupSwipeRefresh()
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
        binding.recyclerViewRooms.setHasFixedSize(true)

        roomAdapter = RoomListAdapter(object : RoomListAdapter.OnRoomClickListener {
            override fun onRoomClick(room: Room) {
                navigateToDetails(room)
            }

            override fun onSaveClick(room: Room) {
                viewModel.toggleFavorite(room)
            }
        })
        binding.recyclerViewRooms.adapter = roomAdapter
    }

    private fun setupFilters() {
        binding.btnOpenFilter.setOnClickListener {
            val dialog = FilterBottomSheetDialog(
                initialOptions = viewModel.uiState.value.filterOptions
            ) { selectedOptions ->
                viewModel.applyFilterOptions(selectedOptions)
            }
            dialog.show(parentFragmentManager, FilterBottomSheetDialog.TAG)
        }

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
                    if (state.isLoading) {
                        binding.shimmerLoading.root.visibility = View.VISIBLE
                        binding.recyclerViewRooms.visibility = View.GONE
                        binding.tvEmptyState.visibility = View.GONE
                        startShimmerAnimation()
                    } else {
                        binding.shimmerLoading.root.visibility = View.GONE
                        stopShimmerAnimation()
                        roomAdapter.setOwnerNames(state.ownerNames)
                        roomAdapter.setSavedRoomKeys(state.savedRoomKeys)
                        roomAdapter.submitList(state.displayRooms)

                        if (state.displayRooms.isEmpty()) {
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
    }

    private var shimmerAnimation: AlphaAnimation? = null

    private fun startShimmerAnimation() {
        if (shimmerAnimation == null) {
            shimmerAnimation = AlphaAnimation(0.4f, 1.0f).apply {
                duration = 800
                repeatMode = Animation.REVERSE
                repeatCount = Animation.INFINITE
            }
        }
        binding.shimmerLoading.root.startAnimation(shimmerAnimation)
    }

    private fun stopShimmerAnimation() {
        binding.shimmerLoading.root.clearAnimation()
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refresh()
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.swipeRefresh.isRefreshing = state.isRefreshing
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
