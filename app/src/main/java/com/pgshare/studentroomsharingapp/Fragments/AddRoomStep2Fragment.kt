package com.pgshare.studentroomsharingapp.Fragments

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.pgshare.studentroomsharingapp.Adapter.PhotoPreviewAdapter
import com.pgshare.studentroomsharingapp.databinding.FragmentAddRoomStep2Binding
import com.pgshare.studentroomsharingapp.viewmodel.AddRoomViewModel

class AddRoomStep2Fragment : Fragment(), ValidatableFragment {

    var _binding: FragmentAddRoomStep2Binding? = null
    val binding get() = _binding!!

    private val viewModel: AddRoomViewModel by activityViewModels()

    val selectedImageUris = mutableListOf<Uri>()
    private lateinit var previewAdapter: PhotoPreviewAdapter

    // Modern Android Photo Picker Launcher (No legacy storage permissions required!)
    private val pickMultipleMedia = registerForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(6)
    ) { uris ->
        if (uris.isNotEmpty()) {
            selectedImageUris.clear()
            selectedImageUris.addAll(uris)
            viewModel.setImageUris(uris)
            previewAdapter.notifyDataSetChanged()

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
        restorePhotosFromViewModel()

        binding.cardUploadPhotos.setOnClickListener {
            pickMultipleMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }

    private fun restorePhotosFromViewModel() {
        if (viewModel.selectedImageUris.isNotEmpty()) {
            selectedImageUris.clear()
            selectedImageUris.addAll(viewModel.selectedImageUris)
            previewAdapter.notifyDataSetChanged()
            binding.recyclerSelectedPhotos.visibility = View.VISIBLE
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