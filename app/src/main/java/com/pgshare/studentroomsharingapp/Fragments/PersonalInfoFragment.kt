package com.pgshare.studentroomsharingapp.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.snackbar.Snackbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.pgshare.studentroomsharingapp.R
import com.pgshare.studentroomsharingapp.databinding.FragmentPersonalInfoBinding
import com.pgshare.studentroomsharingapp.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch

class PersonalInfoFragment : Fragment() {

    private var _binding: FragmentPersonalInfoBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by activityViewModels { ProfileViewModel.Factory() }

    private var initialUsername: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPersonalInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        observeUiState()

        binding.etUsername.doAfterTextChanged {
            checkForChanges()
        }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.btnSaveChanges.setOnClickListener { saveProfileChanges() }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    if (state.isLoading && state.profile == null) {
                        showLoading(true)
                    } else {
                        showLoading(false)
                        state.profile?.let { profile ->
                            binding.etUsername.setText(profile.username)
                            initialUsername = profile.username ?: ""
                            binding.etEmail.setText(profile.email ?: "")
                        }
                    }

                    if (state.isSaving) {
                        showLoading(true)
                        binding.btnSaveChanges.text = "Saving..."
                    }

                    if (state.saveSuccess) {
                        showLoading(false)
                        binding.btnSaveChanges.text = getString(R.string.save_changes)
                        initialUsername = binding.etUsername.text.toString().trim()
                        checkForChanges()
                        viewModel.clearSaveSuccess()
                        Snackbar.make(binding.root, "Profile updated", Snackbar.LENGTH_SHORT).show()
                    }

                    state.error?.let {
                        showLoading(false)
                        binding.btnSaveChanges.text = getString(R.string.save_changes)
                        Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT).show()
                        viewModel.clearError()
                    }
                }
            }
        }
    }

    private fun checkForChanges() {
        val currentUsername = binding.etUsername.text.toString().trim()
        binding.btnSaveChanges.isEnabled = currentUsername != initialUsername
    }

    private fun saveProfileChanges() {
        val newUsername = binding.etUsername.text.toString().trim()
        if (newUsername.isEmpty()) {
            binding.tilUsername.error = "Please enter username"
            return
        }
        binding.tilUsername.error = null
        viewModel.saveProfile(newUsername, null, requireContext().contentResolver)
    }

    private fun showLoading(show: Boolean) {
        _binding?.apply {
            progressBar.visibility = if (show) View.VISIBLE else View.GONE
            btnSaveChanges.isEnabled = !show
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
