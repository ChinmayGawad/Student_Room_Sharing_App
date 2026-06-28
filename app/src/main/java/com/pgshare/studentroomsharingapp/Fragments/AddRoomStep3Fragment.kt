package com.pgshare.studentroomsharingapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.pgshare.studentroomsharingapp.databinding.FragmentAddRoomStep3Binding
import com.pgshare.studentroomsharingapp.interfaces.ValidatableFragment // Add this import

class AddRoomStep3Fragment : Fragment(), ValidatableFragment { // Implement interface

    private var _binding: FragmentAddRoomStep3Binding? = null
    val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddRoomStep3Binding.inflate(inflater, container, false)
        return binding.root
    }

    // --- NEW VALIDATION LOGIC ---
    override fun isValid(): Boolean {
        var isStepValid = true

        val rentText = binding.etMonthlyRent.text.toString().trim()

        // Validate Monthly Rent
        if (rentText.isEmpty()) {
            binding.tilMonthlyRent.error = "Monthly rent is required"
            isStepValid = false
        } else {
            val rentValue = rentText.toDoubleOrNull()
            if (rentValue == null || rentValue <= 0) {
                binding.tilMonthlyRent.error = "Enter a valid amount"
                isStepValid = false
            } else {
                binding.tilMonthlyRent.error = null // Clear error
            }
        }

        // We treat Security Deposit as optional here, but clear any leftover errors just in case
        val depositText = binding.etSecurityDeposit.text.toString().trim()

        if (depositText.isEmpty()) {
            binding.tilSecurityDeposit.error = "Monthly rent is required"
            isStepValid = false
        } else {
            val depositValue = rentText.toDoubleOrNull()
            if (depositValue == null || depositValue <= 0) {
                binding.etSecurityDeposit.error = "Enter a valid amount"
                isStepValid = false
            } else {
                binding.etSecurityDeposit.error = null // Clear error
            }
        }

        // Amenities are optional, so we don't strictly validate the ChipGroup.

        return isStepValid
    }
    // ----------------------------

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}