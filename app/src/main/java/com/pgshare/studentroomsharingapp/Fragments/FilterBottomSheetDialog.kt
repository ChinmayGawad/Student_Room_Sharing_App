package com.pgshare.studentroomsharingapp.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.google.android.material.slider.RangeSlider
import com.pgshare.studentroomsharingapp.R
import com.pgshare.studentroomsharingapp.databinding.BottomSheetFilterBinding
import com.pgshare.studentroomsharingapp.viewmodel.FilterOptions

class FilterBottomSheetDialog(
    private val initialOptions: FilterOptions = FilterOptions(),
    private val onFilterApplied: (FilterOptions) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetFilterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupPriceSlider()
        setupRoomTypeChips()
        setupAmenityChips()
        setupAvailability()

        binding.btnCloseFilter.setOnClickListener {
            dismiss()
        }

        binding.btnReset.setOnClickListener {
            val defaultOptions = FilterOptions()
            onFilterApplied(defaultOptions)
            dismiss()
        }

        binding.btnApply.setOnClickListener {
            val appliedOptions = collectFilterOptions()
            onFilterApplied(appliedOptions)
            dismiss()
        }
    }

    private fun setupPriceSlider() {
        val minPrice = initialOptions.minPrice.coerceIn(2000, 30000).toFloat()
        val maxPrice = initialOptions.maxPrice.coerceIn(2000, 30000).toFloat()
        binding.priceRangeSlider.values = listOf(minPrice, maxPrice)

        binding.tvPriceMin.text = "₹${minPrice.toInt()}"
        binding.tvPriceMax.text = if (maxPrice.toInt() >= 30000) "₹30,000+" else "₹${maxPrice.toInt()}"

        binding.priceRangeSlider.addOnChangeListener(RangeSlider.OnChangeListener { slider, _, _ ->
            val values = slider.values
            val min = values[0].toInt()
            val max = values[1].toInt()
            binding.tvPriceMin.text = "₹$min"
            binding.tvPriceMax.text = if (max >= 30000) "₹30,000+" else "₹$max"
        })
    }

    private fun setupRoomTypeChips() {
        val selectedType = initialOptions.roomType
        for (i in 0 until binding.chipGroupRoomType.childCount) {
            val chip = binding.chipGroupRoomType.getChildAt(i) as? Chip
            if (chip != null && chip.text.toString().equals(selectedType, ignoreCase = true)) {
                chip.isChecked = true
                break
            }
        }
    }

    private fun setupAmenityChips() {
        val selectedAmenities = initialOptions.amenities
        for (i in 0 until binding.chipGroupAmenities.childCount) {
            val chip = binding.chipGroupAmenities.getChildAt(i) as? Chip
            if (chip != null && selectedAmenities.contains(chip.text.toString())) {
                chip.isChecked = true
            }
        }
    }

    private fun setupAvailability() {
        binding.checkOnlyAvailable.isChecked = initialOptions.onlyAvailable
    }

    private fun collectFilterOptions(): FilterOptions {
        val values = binding.priceRangeSlider.values
        val minPrice = values[0].toInt()
        val maxPrice = values[1].toInt()

        val checkedTypeId = binding.chipGroupRoomType.checkedChipId
        val roomType = if (checkedTypeId != View.NO_ID) {
            binding.chipGroupRoomType.findViewById<Chip>(checkedTypeId)?.text?.toString() ?: "Any"
        } else {
            "Any"
        }

        val checkedAmenityIds = binding.chipGroupAmenities.checkedChipIds
        val amenities = checkedAmenityIds.mapNotNull { id ->
            binding.chipGroupAmenities.findViewById<Chip>(id)?.text?.toString()
        }.toSet()

        val onlyAvailable = binding.checkOnlyAvailable.isChecked

        return FilterOptions(
            minPrice = minPrice,
            maxPrice = maxPrice,
            roomType = roomType,
            amenities = amenities,
            onlyAvailable = onlyAvailable
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "FilterBottomSheetDialog"
    }
}
