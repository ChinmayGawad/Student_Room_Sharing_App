package com.pgshare.studentroomsharingapp.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.chip.Chip
import com.pgshare.studentroomsharingapp.databinding.FragmentAddRoomStep1Binding
import com.pgshare.studentroomsharingapp.viewmodel.AddRoomViewModel

class AddRoomStep1Fragment : Fragment(), ValidatableFragment {

    private var _binding: FragmentAddRoomStep1Binding? = null
    val binding get() = _binding!!

    private val viewModel: AddRoomViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddRoomStep1Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        restoreStateFromViewModel()
    }

    private fun restoreStateFromViewModel() {
        if (viewModel.title.isNotEmpty()) {
            binding.etRoomTitle.setText(viewModel.title)
        }
        if (viewModel.location.isNotEmpty()) {
            binding.etRoomLocation.setText(viewModel.location)
        }
        if (viewModel.description.isNotEmpty()) {
            binding.etDescription.setText(viewModel.description)
        }

        val savedType = viewModel.roomType
        for (i in 0 until binding.chipGroupRoomType.childCount) {
            val chip = binding.chipGroupRoomType.getChildAt(i) as? Chip
            if (chip != null && chip.text.toString().equals(savedType, ignoreCase = true)) {
                chip.isChecked = true
                break
            }
        }
    }

    fun getSelectedRoomType(): String {
        val checkedId = binding.chipGroupRoomType.checkedChipId
        if (checkedId == View.NO_ID) return "Room"
        val chip = binding.root.findViewById<Chip>(checkedId)
        return chip?.text?.toString() ?: "Room"
    }

    override fun onDestroyView() {
        saveStateToViewModel()
        super.onDestroyView()
        _binding = null
    }

    private fun saveStateToViewModel() {
        _binding?.let { b ->
            viewModel.setStep1Data(
                title = b.etRoomTitle.text.toString().trim(),
                location = b.etRoomLocation.text.toString().trim(),
                roomType = getSelectedRoomType(),
                description = b.etDescription.text.toString().trim()
            )
        }
    }

    override fun isValid(): Boolean {
        var isStepValid = true

        val title = binding.etRoomTitle.text.toString().trim()
        if (title.isEmpty()) {
            binding.tilRoomTitle.error = "Room title is required"
            isStepValid = false
        } else {
            binding.tilRoomTitle.error = null
        }

        val location = binding.etRoomLocation.text.toString().trim()
        if (location.isEmpty()) {
            binding.tilRoomLocation.error = "Location is required"
            isStepValid = false
        } else {
            binding.tilRoomLocation.error = null
        }

        if (binding.chipGroupRoomType.checkedChipId == View.NO_ID) {
            Toast.makeText(requireContext(), "Please select a room type", Toast.LENGTH_SHORT).show()
            isStepValid = false
        }

        if (isStepValid) {
            saveStateToViewModel()
        }

        return isStepValid
    }
}