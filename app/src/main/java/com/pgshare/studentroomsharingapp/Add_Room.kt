package com.pgshare.studentroomsharingapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.chip.Chip
import com.pgshare.studentroomsharingapp.Adapter.WizardPagerAdapter
import com.pgshare.studentroomsharingapp.Fragments.AddRoomStep1Fragment
import com.pgshare.studentroomsharingapp.Fragments.AddRoomStep2Fragment
import com.pgshare.studentroomsharingapp.Fragments.AddRoomStep3Fragment
import com.pgshare.studentroomsharingapp.Fragments.ValidatableFragment
import com.google.firebase.auth.FirebaseAuth
import com.pgshare.studentroomsharingapp.Authentication.Login
import com.pgshare.studentroomsharingapp.databinding.ActivityAddRoomBinding
import com.pgshare.studentroomsharingapp.viewmodel.AddRoomEvent
import com.pgshare.studentroomsharingapp.viewmodel.AddRoomViewModel
import kotlinx.coroutines.launch

class Add_Room : AppCompatActivity() {

    private lateinit var binding: ActivityAddRoomBinding
    private lateinit var wizardAdapter: WizardPagerAdapter
    private lateinit var viewModel: AddRoomViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        binding = ActivityAddRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (FirebaseAuth.getInstance().currentUser == null) {
            startActivity(Intent(this, Login::class.java))
            finish()
            return
        }

        viewModel = AddRoomViewModel()

        wizardAdapter = WizardPagerAdapter(this)
        binding.viewpagerAddRoomSteps.adapter = wizardAdapter
        binding.viewpagerAddRoomSteps.isUserInputEnabled = false
        updateWizardUI(0)

        binding.btnWizardNext.setOnClickListener {
            val currentStep = binding.viewpagerAddRoomSteps.currentItem
            val currentFragment = supportFragmentManager.findFragmentByTag("f$currentStep") as? ValidatableFragment
            val isValid = currentFragment?.isValid() ?: true

            if (isValid) {
                if (currentStep < wizardAdapter.itemCount - 1) {
                    binding.viewpagerAddRoomSteps.currentItem = currentStep + 1
                    updateWizardUI(binding.viewpagerAddRoomSteps.currentItem)
                } else {
                    submitRoomData()
                }
            }
        }

        binding.btnWizardBack.setOnClickListener {
            val currentStep = binding.viewpagerAddRoomSteps.currentItem
            if (currentStep > 0) {
                binding.viewpagerAddRoomSteps.currentItem = currentStep - 1
                updateWizardUI(binding.viewpagerAddRoomSteps.currentItem)
            }
        }

        binding.toolbarAddRoom.setNavigationOnClickListener { finish() }

        observeEvents()
    }

    private fun observeEvents() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { event ->
                    when (event) {
                        is AddRoomEvent.Success -> {
                            Toast.makeText(this@Add_Room, "Room Published Successfully!", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        is AddRoomEvent.Error -> {
                            Toast.makeText(this@Add_Room, "Failed to publish: ${event.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    private fun updateWizardUI(currentStep: Int) {
        val totalSteps = wizardAdapter.itemCount
        if (currentStep == 0) {
            binding.btnWizardBack.visibility = View.INVISIBLE
        } else {
            binding.btnWizardBack.visibility = View.VISIBLE
        }

        if (currentStep == totalSteps - 1) {
            binding.btnWizardNext.text = "Publish"
        } else {
            binding.btnWizardNext.text = "Next"
        }

        binding.tvStepLabel.text = "Step ${currentStep + 1}/$totalSteps"

        val dots = listOf(binding.dotStep1, binding.dotStep2, binding.dotStep3)
        val lines = listOf(binding.lineStep1, binding.lineStep2)

        for (i in dots.indices) {
            dots[i].setImageResource(
                if (i <= currentStep) R.drawable.circle_primary_filled
                else R.drawable.circle_outline_hollow
            )
        }
        for (i in lines.indices) {
            lines[i].setImageResource(
                if (i < currentStep) R.drawable.line_primary
                else R.drawable.line_outline
            )
        }
    }

    private fun submitRoomData() {
        Toast.makeText(this, "Compressing photos and publishing...", Toast.LENGTH_LONG).show()

        val step1 = supportFragmentManager.findFragmentByTag("f0") as? AddRoomStep1Fragment
        val step2 = supportFragmentManager.findFragmentByTag("f1") as? AddRoomStep2Fragment
        val step3 = supportFragmentManager.findFragmentByTag("f2") as? AddRoomStep3Fragment

        val title = step1?.binding?.etRoomTitle?.text.toString().trim()
        val location = step1?.binding?.etRoomLocation?.text.toString().trim()
        val description = step1?.binding?.etDescription?.text.toString().trim()
        val rent = step3?.binding?.etMonthlyRent?.text.toString().trim()
        val imageUris = step2?.selectedImageUris ?: emptyList()
        val deposit = step3?.binding?.etSecurityDeposit?.text.toString().trim()

        val checkedChipId = step1?.binding?.chipGroupRoomType?.checkedChipId ?: View.NO_ID
        val roomType = if (checkedChipId != View.NO_ID && step1 != null) {
            step1.binding.root.findViewById<Chip>(checkedChipId).text.toString()
        } else {
            "Room"
        }

        val amenities = step3?.getSelectedAmenities() ?: emptyList()

        viewModel.publishRoom(title, location, roomType, rent, deposit, imageUris, contentResolver, amenities, description)
    }
}
