package com.pgshare.studentroomsharingapp.Fragments

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import com.google.android.material.chip.Chip
import com.pgshare.studentroomsharingapp.R
import com.pgshare.studentroomsharingapp.databinding.FragmentAddRoomStep3Binding

class AddRoomStep3Fragment : Fragment(), ValidatableFragment {

    private var _binding: FragmentAddRoomStep3Binding? = null
    val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddRoomStep3Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

    private fun addCustomAmenity() {
        val text = binding.etCustomAmenity.text.toString().trim()
        if (text.isEmpty()) return

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

        return isStepValid
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
