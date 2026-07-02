package com.pgshare.studentroomsharingapp.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.pgshare.studentroomsharingapp.databinding.FragmentAddRoomStep1Binding
import com.pgshare.studentroomsharingapp.Fragments.ValidatableFragment

class AddRoomStep1Fragment : Fragment(), ValidatableFragment {

    private var _binding: FragmentAddRoomStep1Binding? = null
    val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddRoomStep1Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun isValid(): Boolean {
        var isStepValid = true

        // 1. Validate Title
        val title = binding.etRoomTitle.text.toString().trim() // Check your exact EditText ID
        if (title.isEmpty()) {
            binding.tilRoomTitle.error = "Room title is required"
            isStepValid = false
        } else {
            binding.tilRoomTitle.error = null // Clear error
        }

        // 2. Validate Location
        val location = binding.etRoomLocation.text.toString().trim()
        if (location.isEmpty()) {
            binding.tilRoomLocation.error = "Location is required"
            isStepValid = false
        } else {
            binding.tilRoomLocation.error = null
        }

        // 3. Validate Room Type (ChipGroup)
        if (binding.chipGroupRoomType.checkedChipId == View.NO_ID) {
            Toast.makeText(requireContext(), "Please select a room type", Toast.LENGTH_SHORT).show()
            isStepValid = false
        }

        return isStepValid
    }
}