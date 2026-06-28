package com.pgshare.studentroomsharingapp

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.chip.Chip
import com.google.firebase.database.FirebaseDatabase
import com.pgshare.studentroomsharingapp.Adapter.Room
import com.pgshare.studentroomsharingapp.Adapter.WizardPagerAdapter
import com.pgshare.studentroomsharingapp.Fragments.AddRoomStep1Fragment
import com.pgshare.studentroomsharingapp.databinding.ActivityAddRoomBinding
import com.pgshare.studentroomsharingapp.fragments.AddRoomStep3Fragment
import com.pgshare.studentroomsharingapp.interfaces.ValidatableFragment
import java.io.ByteArrayOutputStream

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

        // 2. Handle the "Next" Button with Validation
        binding.btnWizardNext.setOnClickListener {
            val currentStep = binding.viewpagerAddRoomSteps.currentItem

            val currentFragment = supportFragmentManager.findFragmentByTag("f$currentStep") as? ValidatableFragment
            val isValid = currentFragment?.isValid() ?: true

            if (isValid) {
                if (currentStep < wizardAdapter.itemCount - 1) {
                    binding.viewpagerAddRoomSteps.currentItem = currentStep + 1
                    updateWizardUI(binding.viewpagerAddRoomSteps.currentItem)
                } else {
                    // Final Step - Validated!
                    submitRoomData()
                }
            }
        }

        // 3. Handle the "Back" Button
        binding.btnWizardBack.setOnClickListener {
            val currentStep = binding.viewpagerAddRoomSteps.currentItem
            if (currentStep > 0) {
                binding.viewpagerAddRoomSteps.currentItem = currentStep - 1
                updateWizardUI(binding.viewpagerAddRoomSteps.currentItem)
            }
        }

        // 4. Handle the top-left Close 'X' Button
        binding.toolbarAddRoom.setNavigationOnClickListener {
            finish()
        }
    }

    /**
     * Updates the Progress Bar, Buttons, and UI based on the current step.
     */
    private fun updateWizardUI(currentStep: Int) {
        val totalSteps = wizardAdapter.itemCount
        val progressPercentage = ((currentStep + 1).toFloat() / totalSteps.toFloat() * 100).toInt()
        binding.progressAddRoom.setProgressCompat(progressPercentage, true)

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
    }

    /**
     * Gathers data from all 3 fragments and pushes to Firebase.
     */
    private fun submitRoomData() {
        Toast.makeText(this, "Compressing photos and publishing...", Toast.LENGTH_LONG).show()

        val step1 = supportFragmentManager.findFragmentByTag("f0") as? AddRoomStep1Fragment
        val step2 = supportFragmentManager.findFragmentByTag("f1") as? AddRoomStep2Fragment
        val step3 = supportFragmentManager.findFragmentByTag("f2") as? AddRoomStep3Fragment

        val title = step1?.binding?.etRoomTitle?.text.toString().trim()
        val location = step1?.binding?.etRoomLocation?.text.toString().trim()
        val rent = step3?.binding?.etMonthlyRent?.text.toString().trim()
        val imageUris = step2?.selectedImageUris ?: emptyList()
        val deposit = step3?.binding?.etSecurityDeposit?.text.toString().trim()

        val checkedChipId = step1?.binding?.chipGroupRoomType?.checkedChipId ?: View.NO_ID
        val roomType = if (checkedChipId != View.NO_ID && step1 != null) {
            step1.binding.root.findViewById<Chip>(checkedChipId).text.toString()
        } else {
            "Room"
        }

        Thread {
            val base64Images = ArrayList<String?>()
            for (uri in imageUris) {
                val base64String = compressAndEncodeImage(uri)
                if (base64String != null) {
                    base64Images.add(base64String)
                }
            }

            val databaseRef = FirebaseDatabase.getInstance().getReference("Rooms")
            val newRoomId = databaseRef.push().key ?: return@Thread

            val newRoom = Room(
                newRoomId,
                title,
                location,
                "Type: $roomType",
                rent,
                deposit,
                base64Images,
                0
            )

            databaseRef.child(newRoomId).setValue(newRoom).addOnCompleteListener { task ->
                runOnUiThread {
                    if (task.isSuccessful) {
                        Toast.makeText(this@Add_Room, "Room Published Successfully!", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this@Add_Room, "Failed to publish: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }.start()
    }

    /**
     * Shrinks the image and converts it to Base64 text.
     */
    private fun compressAndEncodeImage(uri: Uri): String? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)

            val ratio = Math.min(600.0 / originalBitmap.width, 600.0 / originalBitmap.height)
            val width = Math.round(ratio * originalBitmap.width).toInt()
            val height = Math.round(ratio * originalBitmap.height).toInt()
            val resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, width, height, true)

            val outputStream = ByteArrayOutputStream()
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 40, outputStream)
            val imageBytes = outputStream.toByteArray()

            Base64.encodeToString(imageBytes, Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}