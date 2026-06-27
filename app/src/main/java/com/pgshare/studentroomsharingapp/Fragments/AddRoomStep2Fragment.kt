package com.pgshare.studentroomsharingapp.fragments

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.pgshare.studentroomsharingapp.databinding.FragmentAddRoomStep2Binding
import com.pgshare.studentroomsharingapp.interfaces.ValidatableFragment // Add this import

class AddRoomStep2Fragment : Fragment(), ValidatableFragment { // Implement interface

    private var _binding: FragmentAddRoomStep2Binding? = null
     val binding get() = _binding!!

    val selectedImageUris = mutableListOf<Uri>()

    private val pickMultipleMedia = registerForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(5)
    ) { uris ->
        if (uris.isNotEmpty()) {
            selectedImageUris.clear()
            selectedImageUris.addAll(uris)
            updateUiWithPhotos()
        } else {
            Toast.makeText(requireContext(), "No photos selected", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddRoomStep2Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.cardUploadPhotos.setOnClickListener {
            pickMultipleMedia.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }
    }

    private fun updateUiWithPhotos() {
        Toast.makeText(
            requireContext(),
            "Successfully selected ${selectedImageUris.size} photos!",
            Toast.LENGTH_SHORT
        ).show()
    }

    // --- NEW VALIDATION LOGIC ---
    override fun isValid(): Boolean {
        if (selectedImageUris.isEmpty()) {
            Toast.makeText(requireContext(), "Please select at least 1 photo", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
    // ----------------------------

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}