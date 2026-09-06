package com.pgshare.studentroomsharingapp.Fragments

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.chip.Chip
import com.pgshare.studentroomsharingapp.R
import com.pgshare.studentroomsharingapp.databinding.FragmentAddRoomStep3Binding
import com.pgshare.studentroomsharingapp.viewmodel.AddRoomViewModel

class AddRoomStep3Fragment : Fragment(), ValidatableFragment {

    private var _binding: FragmentAddRoomStep3Binding? = null
    val binding get() = _binding!!

    private val viewModel: AddRoomViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddRoomStep3Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        restoreStateFromViewModel()

        binding.tilCustomAmenity.setEndIconOnClickListener {
            addCustomAmenity()
        }

        binding.etCustomAmenity.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                (event?.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER)
            ) {
                addCustomAmenity()
                true
            } else {
                false
            }
        }
    }

    private fun restoreStateFromViewModel() {
        if (viewModel.rent.isNotEmpty()) {
            binding.etMonthlyRent.setText(viewModel.rent)
        }
        if (viewModel.deposit.isNotEmpty()) {
            binding.etSecurityDeposit.setText(viewModel.deposit)
        }
        if (viewModel.selectedAmenities.isNotEmpty()) {
            val existingChips = mutableMapOf<String, Chip>()
            for (i in 0 until binding.chipGroupAmenities.childCount) {
                val chip = binding.chipGroupAmenities.getChildAt(i) as? Chip
                if (chip != null) {
                    existingChips[chip.text.toString().lowercase()] = chip
                }
            }
            for (amenity in viewModel.selectedAmenities) {
                val existing = existingChips[amenity.lowercase()]
                if (existing != null) {
                    existing.isChecked = true
                } else {
                    addCustomAmenityChip(amenity)
                }
            }
        }
    }

    private fun addCustomAmenityChip(text: String) {
        val chip = layoutInflater.inflate(
            R.layout.item_amenity_chip,
            binding.chipGroupAmenities,
            false
        ) as Chip

        chip.id = View.generateViewId()
        chip.text = text
        chip.isChecked = true
        chip.isCheckable = true
        chip.isCloseIconVisible = true
        chip.setOnCloseIconClickListener {
            binding.chipGroupAmenities.removeView(it)
        }

        binding.chipGroupAmenities.addView(chip)
    }

    private fun addCustomAmenity() {
        val text = binding.etCustomAmenity.text.toString().trim()
        if (text.isEmpty()) return

        addCustomAmenityChip(text)
        binding.etCustomAmenity.text?.clear()
    }

    fun getSelectedAmenities(): List<String> {
        val checkedIds = binding.chipGroupAmenities.checkedChipIds
        val amenities = mutableListOf<String>()
        for (id in checkedIds) {
            val chip = binding.root.findViewById<Chip>(id)
            chip?.let { amenities.add(it.text.toString()) }
        }
        return amenities
    }

    private fun saveStateToViewModel() {
        _binding?.let { b ->
            viewModel.setStep3Data(
                rent = b.etMonthlyRent.text.toString().trim(),
                deposit = b.etSecurityDeposit.text.toString().trim(),
                amenities = getSelectedAmenities()
            )
        }
    }

    override fun isValid(): Boolean {
        var isStepValid = true

        val rentText = binding.etMonthlyRent.text.toString().trim()

        if (rentText.isEmpty()) {
            binding.tilMonthlyRent.error = "Monthly rent is required"
            isStepValid = false
        } else {
            val rentValue = rentText.toDoubleOrNull()
            if (rentValue == null || rentValue <= 0) {
                binding.tilMonthlyRent.error = "Enter a valid amount"
                isStepValid = false
            } else {
                binding.tilMonthlyRent.error = null
            }
        }

        val depositText = binding.etSecurityDeposit.text.toString().trim()

        if (depositText.isEmpty()) {
            binding.tilSecurityDeposit.error = "Security deposit is required"
            isStepValid = false
        } else {
            val depositValue = depositText.toDoubleOrNull()
            if (depositValue == null || depositValue <= 0) {
                binding.tilSecurityDeposit.error = "Enter a valid amount"
                isStepValid = false
            } else {
                binding.tilSecurityDeposit.error = null
            }
        }

        if (isStepValid) {
            saveStateToViewModel()
        }

        return isStepValid
    }

    override fun onDestroyView() {
        saveStateToViewModel()
        super.onDestroyView()
        _binding = null
    }
}
