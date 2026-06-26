package com.pgshare.studentroomsharingapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.pgshare.studentroomsharingapp.databinding.FragmentAddRoomStep3Binding

class AddRoomStep3Fragment : Fragment() {

    private var _binding: FragmentAddRoomStep3Binding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddRoomStep3Binding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Collects all data from this step.
     */
    fun getStepData(): Map<String, Any> {
        val rent = binding.etMonthlyRent.text.toString()
        val deposit = binding.etSecurityDeposit.text.toString()
        val selectedAmenities = mutableListOf<String>()

        if (binding.chipWifi.isChecked) selectedAmenities.add("High-Speed WiFi")
        if (binding.chipAc.isChecked) selectedAmenities.add("Air Conditioning")
        if (binding.chipAttachedBath.isChecked) selectedAmenities.add("Attached Bath")
        if (binding.chipFurnished.isChecked) selectedAmenities.add("Fully Furnished")
        if (binding.chipWashingMachine.isChecked) selectedAmenities.add("Washing Machine")
        if (binding.chipBalcony.isChecked) selectedAmenities.add("Balcony")

        return mapOf(
            "monthlyRent" to rent,
            "securityDeposit" to deposit,
            "amenities" to selectedAmenities
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}