package com.pgshare.studentroomsharingapp.Fragments

import android.util.Base64
import java.io.ByteArrayOutputStream
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.pgshare.studentroomsharingapp.Authentication.Login
import com.pgshare.studentroomsharingapp.R
import com.pgshare.studentroomsharingapp.databinding.FragmentProfileBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlin.math.min
import kotlin.math.roundToInt

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase


    private var currentUserId: String = ""
    private var selectedImageUri: Uri? = null

    private var initialUsername: String = ""

    private fun compressAndEncodeImage(uri: Uri): String? {
        return try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)

            // Resize for profile (e.g., max 200x200) to keep DB payload small
            val ratio = min(200.0 / originalBitmap.width, 200.0 / originalBitmap.height)
            val width = (ratio * originalBitmap.width).roundToInt()
            val height = (ratio * originalBitmap.height).roundToInt()
            val resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, width, height, true)

            val outputStream = ByteArrayOutputStream()
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream) // 70 quality is good for profile pics
            val imageBytes = outputStream.toByteArray()

            Base64.encodeToString(imageBytes, Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            binding.ivProfileAvatar.setImageURI(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun showLogoutConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Log Out")
            .setMessage("Are you sure you want to log out of your account?")
            // The Negative button (Cancel)
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss() // Just close the box, do nothing
            }
            // The Positive button (Confirm)
            .setPositiveButton("Log Out") { dialog, _ ->
                dialog.dismiss()
                performLogout() // Trigger the actual logout
            }
            .show()
    }

    private fun performLogout() {
        auth.signOut()
        startActivity(Intent(requireContext(), Login::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        requireActivity().finish()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
//        storage = FirebaseStorage.getInstance()
        currentUserId = auth.currentUser?.uid ?: ""



        if (currentUserId.isEmpty()) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        loadUserData()
        setupClickListeners()

        // 3. Setup listeners in setupClickListeners()
        binding.etUsername.doAfterTextChanged {
            checkForChanges()
        }
    }

    // 5. The comparison logic
    private fun checkForChanges() {
        val currentUsername = binding.etUsername.text.toString().trim()
        val hasUsernameChanged = currentUsername != initialUsername
        val hasImageChanged = selectedImageUri != null

        // Enable button only if something is different
        binding.btnSaveChanges.isEnabled = hasUsernameChanged || hasImageChanged
    }



    private fun setupClickListeners() {
        binding.fabEditAvatar.setOnClickListener {
            openGallery()
        }

        binding.btnSaveChanges.setOnClickListener {
            saveProfileChanges()
        }

        binding.btnLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }
    }

    private fun loadUserData() {
        showLoading(true, "Loading profile...")

        val userRef = database.getReference("Users").child(currentUserId)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded || _binding == null) return

                if (snapshot.exists()) {
                    val username = snapshot.child("username").getValue(String::class.java)
                    val email = snapshot.child("email").getValue(String::class.java)
                    val profileImageUrl = snapshot.child("profileImageUrl").getValue(String::class.java)

                    binding.etUsername.setText(username)
                    if (username != null) {
                        initialUsername = username
                    }
                    binding.etEmail.setText(email ?: "")

                    if (!profileImageUrl.isNullOrEmpty()) {
                        if (profileImageUrl.contains("http")) {
                            // Legacy URL loading
                            Glide.with(requireContext()).load(profileImageUrl).circleCrop().into(binding.ivProfileAvatar)
                        } else {
                            // Base64 decoding
                            try {
                                val imageBytes = Base64.decode(profileImageUrl, Base64.DEFAULT)
                                val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                                binding.ivProfileAvatar.setImageBitmap(decodedImage)
                            } catch (e: Exception) {
                                binding.ivProfileAvatar.setImageResource(R.drawable.ic_person_placeholder)
                            }
                        }
                    }
                }

                showLoading(false)
            }

            override fun onCancelled(error: DatabaseError) {
                if (!isAdded || _binding == null) return
                showLoading(false)
                Toast.makeText(
                    requireContext(),
                    getString(R.string.error_loading_profile, error.message),
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun openGallery() {
        galleryLauncher.launch("image/*")
    }

/*    private fun uploadImageToStorage(imageUri: Uri, onSuccess: (String) -> Unit) {
        showLoading(true, "Uploading image...")

        val imageRef = storage.reference.child("Profile_Images/$currentUserId.jpg")
        imageRef.putFile(imageUri)
            .addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    showLoading(false)
                    onSuccess(downloadUrl.toString())
                }
            }
            .addOnFailureListener { exception ->
                if (!isAdded || _binding == null) return@addOnFailureListener
                showLoading(false)
                Toast.makeText(
                    requireContext(),
                    getString(R.string.error_uploading_image, exception.message),
                    Toast.LENGTH_SHORT
                ).show()
            }
    }*/



    private fun saveProfileChanges() {
        val newUsername = binding.etUsername.text.toString().trim()
        if (newUsername.isEmpty()) {
            binding.tilUsername.error = "Please enter username"
            return
        }
        binding.tilUsername.error = null

        showLoading(true, "Saving changes...")

        if (selectedImageUri != null) {
            // Run compression in a background thread
            Thread {
                val base64Image = compressAndEncodeImage(selectedImageUri!!)
                activity?.runOnUiThread {
                    updateUserData(newUsername, base64Image)
                }
            }.start()
        } else {
            updateUserData(newUsername, null)
        }
    }

    // --- Replaced Toast with Snackbar ---
    private fun updateUserData(username: String, imageUrl: String?) {
        val userRef = database.getReference("Users").child(currentUserId)
        val updates = mutableMapOf<String, Any>(
            "username" to username
        )

        if (imageUrl != null) {
            updates["profileImageUrl"] = imageUrl
        }

        userRef.updateChildren(updates)
            .addOnSuccessListener {
                initialUsername = username // Update base state to current
                selectedImageUri = null    // Clear temporary URI
                checkForChanges()          // Button will disable automatically
            }
            .addOnFailureListener { exception ->
                if (!isAdded || _binding == null) return@addOnFailureListener
                showLoading(false)
                Snackbar.make(binding.root, "Error: ${exception.message}", Snackbar.LENGTH_LONG).show()
            }
    }

    // --- Enhanced Loading State ---
    private fun showLoading(show: Boolean, message: String = "") {
        if (!isAdded || _binding == null) return
        // If you want to use the message, you could set it to a TextView or Log it
        Log.d("ProfileFragment", "Loading: $message")

        _binding?.apply {
            progressBar.visibility = if (show) View.VISIBLE else View.GONE
            btnSaveChanges.isEnabled = !show
            fabEditAvatar.isEnabled = !show
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}