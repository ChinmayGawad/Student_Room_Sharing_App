package com.pgshare.studentroomsharingapp

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.pgshare.studentroomsharingapp.Adapter.WizardPagerAdapter
import com.pgshare.studentroomsharingapp.databinding.ActivityAddRoomBinding

class Add_Room : AppCompatActivity() {

    private lateinit var binding: ActivityAddRoomBinding
    private lateinit var wizardAdapter: WizardPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Hide default action bar
        supportActionBar?.hide()

        binding = ActivityAddRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Setup the ViewPager
        wizardAdapter = WizardPagerAdapter(this)
        binding.viewpagerAddRoomSteps.adapter = wizardAdapter

        // CRITICAL UX FIX: Disable swiping so the user MUST use the Next/Back buttons
        binding.viewpagerAddRoomSteps.isUserInputEnabled = false

        // Initialize the UI for Step 1
        updateWizardUI(0)

        // 2. Handle the "Next" Button
        binding.btnWizardNext.setOnClickListener {
            val currentStep = binding.viewpagerAddRoomSteps.currentItem

            // TODO: Here is where we will add Validation later
            // (e.g., if currentStep == 0, check if Title is empty)

            if (currentStep < wizardAdapter.itemCount - 1) {
                // Move to the next step
                binding.viewpagerAddRoomSteps.currentItem = currentStep + 1
                updateWizardUI(binding.viewpagerAddRoomSteps.currentItem)
            } else {
                // If we are on the last step, submit to Firebase
                Toast.makeText(this, "Publishing Room to Firebase...", Toast.LENGTH_SHORT).show()
                // submitRoomData()
                // finish()
            }
        }

        // 3. Handle the "Back" Button
        binding.btnWizardBack.setOnClickListener {
            val currentStep = binding.viewpagerAddRoomSteps.currentItem
            if (currentStep > 0) {
                // Move to the previous step
                binding.viewpagerAddRoomSteps.currentItem = currentStep - 1
                updateWizardUI(binding.viewpagerAddRoomSteps.currentItem)
            }
        }

        // 4. Handle the top-left Close 'X' Button
        binding.toolbarAddRoom.setNavigationOnClickListener {
            // Optional: Show an "Are you sure you want to discard?" dialog here
            finish()
        }
    }

    /**
     * Updates the Progress Bar, Buttons, and UI based on the current step.
     */
    private fun updateWizardUI(currentStep: Int) {
        val totalSteps = wizardAdapter.itemCount

        // Update Progress Bar (e.g., Step 1 of 2 = 50%)
        val progressPercentage = ((currentStep + 1).toFloat() / totalSteps.toFloat() * 100).toInt()
        binding.progressAddRoom.setProgressCompat(progressPercentage, true)

        // Handle "Back" button visibility
        if (currentStep == 0) {
            binding.btnWizardBack.visibility = View.INVISIBLE // Hide on first step
        } else {
            binding.btnWizardBack.visibility = View.VISIBLE
        }

        // Handle "Next" vs "Publish" text
        if (currentStep == totalSteps - 1) {
            binding.btnWizardNext.text = "Publish"
        } else {
            binding.btnWizardNext.text = "Next"
        }
    }


}