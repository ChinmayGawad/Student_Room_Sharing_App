package com.pgshare.studentroomsharingapp

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.pgshare.studentroomsharingapp.Adapter.PhotoPreviewAdapter
import com.pgshare.studentroomsharingapp.databinding.FragmentAddRoomStep2Binding
import com.pgshare.studentroomsharingapp.interfaces.ValidatableFragment

class AddRoomStep2Fragment : Fragment(), ValidatableFragment {

    // Public binding so AddRoomActivity can access it if needed
    var _binding: FragmentAddRoomStep2Binding? = null
    val binding get() = _binding!!

    // We store the URIs publicly so the host AddRoomActivity can grab them
    // to compress and Base64 encode them when the user hits "Publish" on Step 3
    val selectedImageUris = mutableListOf<Uri>()
    private lateinit var previewAdapter: PhotoPreviewAdapter

    // Modern Android Photo Picker Launcher (No legacy storage permissions required!)
    private val pickMultipleMedia = registerForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(6) // Limit to 6 photos to prevent massive payload sizes
    ) { uris ->
        if (uris.isNotEmpty()) {
            selectedImageUris.clear()
            selectedImageUris.addAll(uris)
            previewAdapter.notifyDataSetChanged()

            // Show the RecyclerView, hide the empty state placeholder text (if you have one)
            binding.recyclerSelectedPhotos.visibility = View.VISIBLE
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

        setupRecyclerView()

        // Ensure this ID matches the large "Tap to Upload" card in fragment_add_room_step2.xml
        binding.cardUploadPhotos.setOnClickListener {
            // Launch the picker filtering for images only
            pickMultipleMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }

    private fun setupRecyclerView() {
        previewAdapter = PhotoPreviewAdapter(selectedImageUris)

        // Ensure this ID matches the RecyclerView in fragment_add_room_step2.xml
        // We use a HORIZONTAL layout manager for a sleek side-scrolling preview
        binding.recyclerSelectedPhotos.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerSelectedPhotos.adapter = previewAdapter
    }

    // --- ValidatableFragment Interface Implementation ---

    override fun isValid(): Boolean {
        // Step 2 is only valid if they have selected at least 1 image
        return selectedImageUris.isNotEmpty()
    }

    fun getErrorMessage(): String {
        return "Please select at least one photo of the room."
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}