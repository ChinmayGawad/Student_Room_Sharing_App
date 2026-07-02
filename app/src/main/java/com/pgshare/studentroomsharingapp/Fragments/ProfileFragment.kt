package com.pgshare.studentroomsharingapp.Fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.pgshare.studentroomsharingapp.Authentication.Login
import com.pgshare.studentroomsharingapp.R
import com.pgshare.studentroomsharingapp.databinding.FragmentProfileBinding
import com.pgshare.studentroomsharingapp.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels { ProfileViewModel.Factory() }

    private lateinit var auth: FirebaseAuth
    private var selectedImageUri: Uri? = null
    private var initialUsername: String = ""

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            binding.ivProfileAvatar.setImageURI(it)
            checkForChanges()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        if (auth.currentUser == null) {
            startActivity(Intent(requireContext(), Login::class.java))
            return
        }

        setupClickListeners()
        observeUiState()

        binding.etUsername.doAfterTextChanged {
            checkForChanges()
        }
    }

    private fun setupClickListeners() {
        binding.fabEditAvatar.setOnClickListener { openGallery() }
        binding.btnSaveChanges.setOnClickListener { saveProfileChanges() }
        binding.btnLogout.setOnClickListener { showLogoutConfirmationDialog() }
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

                            profile.profileImageUrl?.let { url ->
                                if (url.contains("http")) {
                                    Glide.with(requireContext()).load(url).circleCrop().into(binding.ivProfileAvatar)
                                }
                            }
                        }
                    }

                    if (state.isSaving) {
                        showLoading(true)
                        binding.btnSaveChanges.text = "Saving..."
                    }

                    if (state.saveSuccess) {
                        showLoading(false)
                        binding.btnSaveChanges.text = "Save Changes"
                        selectedImageUri = null
                        checkForChanges()
                        viewModel.clearSaveSuccess()
                    }

                    state.error?.let {
                        showLoading(false)
                        binding.btnSaveChanges.text = "Save Changes"
                        Toast.makeText(requireContext(), "Error: $it", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun checkForChanges() {
        val currentUsername = binding.etUsername.text.toString().trim()
        binding.btnSaveChanges.isEnabled = currentUsername != initialUsername || selectedImageUri != null
    }

    private fun saveProfileChanges() {
        val newUsername = binding.etUsername.text.toString().trim()
        if (newUsername.isEmpty()) {
            binding.tilUsername.error = "Please enter username"
            return
        }
        binding.tilUsername.error = null
        viewModel.saveProfile(newUsername, selectedImageUri, requireContext().contentResolver)
    }

    private fun openGallery() {
        galleryLauncher.launch("image/*")
    }

    private fun showLogoutConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Log Out")
            .setMessage("Are you sure you want to log out of your account?")
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .setPositiveButton("Log Out") { dialog, _ ->
                dialog.dismiss()
                performLogout()
            }
            .show()
    }

    private fun performLogout() {
        auth.signOut()
        startActivity(Intent(requireContext(), Login::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        requireActivity().finish()
    }

    private fun showLoading(show: Boolean) {
        _binding?.apply {
            progressBar.visibility = if (show) View.VISIBLE else View.GONE
            btnSaveChanges.isEnabled = !show
            fabEditAvatar.isEnabled = !show
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
