package com.pgshare.studentroomsharingapp.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.pgshare.studentroomsharingapp.Adapter.InboxAdapter
import com.pgshare.studentroomsharingapp.databinding.FragmentInboxBinding
import com.pgshare.studentroomsharingapp.viewmodel.InboxViewModel
import kotlinx.coroutines.launch

class InboxFragment : Fragment() {

    private var _binding: FragmentInboxBinding? = null
    private val binding get() = _binding!!

    private val viewModel: InboxViewModel by viewModels { InboxViewModel.Factory() }

    private lateinit var inboxAdapter: InboxAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInboxBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeUiState()
        observeUserProfiles()
    }

    private fun setupRecyclerView() {
        binding.recyclerViewInbox.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewInbox.setHasFixedSize(true)
        inboxAdapter = InboxAdapter()
        binding.recyclerViewInbox.adapter = inboxAdapter
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.progressBarInbox.visibility = if (state.isLoading) View.VISIBLE else View.GONE

                    inboxAdapter.submitList(state.inboxList)

                    if (!state.isLoggedIn || (state.inboxList.isEmpty() && !state.isLoading)) {
                        binding.recyclerViewInbox.visibility = View.GONE
                        binding.layoutEmptyState.visibility = View.VISIBLE
                    } else {
                        binding.recyclerViewInbox.visibility = View.VISIBLE
                        binding.layoutEmptyState.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun observeUserProfiles() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.userProfiles.collect { profiles ->
                    inboxAdapter.setUserProfiles(profiles)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
