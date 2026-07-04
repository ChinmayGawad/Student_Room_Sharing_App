package com.pgshare.studentroomsharingapp.Fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.pgshare.studentroomsharingapp.Add_Room
import com.pgshare.studentroomsharingapp.Authentication.Login
import com.pgshare.studentroomsharingapp.BuildConfig
import com.pgshare.studentroomsharingapp.R
import com.pgshare.studentroomsharingapp.databinding.FragmentSettingsBinding
import com.pgshare.studentroomsharingapp.repository.FirebaseRepository
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val auth = FirebaseAuth.getInstance()
    private val repository = FirebaseRepository()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        populateVersionInfo()
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        binding.rowDarkMode.setOnClickListener {
            binding.switchDarkMode.performClick()
        }

        binding.rowNotifications.setOnClickListener {
            Toast.makeText(context, "Notifications settings", Toast.LENGTH_SHORT).show()
        }

        binding.rowLanguage.setOnClickListener {
            Toast.makeText(context, "Language selection", Toast.LENGTH_SHORT).show()
        }

        binding.rowPersonalInfo.setOnClickListener {
            Toast.makeText(context, "Personal Information", Toast.LENGTH_SHORT).show()
        }

        binding.rowPrivacy.setOnClickListener {
            Toast.makeText(context, "Privacy settings", Toast.LENGTH_SHORT).show()
        }

        binding.rowSecurity.setOnClickListener {
            Toast.makeText(context, "Security settings", Toast.LENGTH_SHORT).show()
        }

        binding.rowListProperty.setOnClickListener { handleListProperty() }

        binding.rowHelpCenter.setOnClickListener {
            Toast.makeText(context, "Help Center", Toast.LENGTH_SHORT).show()
        }

        binding.rowSendFeedback.setOnClickListener {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "message/rfc822"
                putExtra(Intent.EXTRA_EMAIL, arrayOf("chinmaygawad365@gmail.com"))
                putExtra(Intent.EXTRA_SUBJECT, "Feedback - Student Room Sharing")
            }
            if (intent.resolveActivity(requireContext().packageManager) != null) {
                startActivity(intent)
            } else {
                Toast.makeText(context, "No email app found", Toast.LENGTH_SHORT).show()
            }
        }

        binding.rowRateApp.setOnClickListener {
            Toast.makeText(context, "Rate the App", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleListProperty() {
        val user = auth.currentUser
        if (user == null) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Login Required")
                .setMessage("You need to log in to list a property.")
                .setPositiveButton("Log In") { _, _ ->
                    startActivity(Intent(requireContext(), Login::class.java))
                }
                .setNegativeButton("Cancel", null)
                .show()
            return
        }

        lifecycleScope.launch {
            val role = repository.getUserRole(user.uid)
            if (role != "owner") {
                Toast.makeText(context, "Only property owners can list rooms", Toast.LENGTH_SHORT).show()
                return@launch
            }
            showAdminPasswordDialog(user.email ?: "")
        }
    }

    private fun showAdminPasswordDialog(email: String) {
        val input = EditText(requireContext()).apply {
            inputType = android.text.InputType.TYPE_CLASS_TEXT or
                    android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            hint = "Password"
            layoutParams = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(48, 16, 48, 16) }
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.admin_password_title)
            .setMessage(R.string.admin_password_message)
            .setView(input)
            .setPositiveButton(R.string.verify) { _, _ ->
                val password = input.text.toString().trim()
                if (password.isEmpty()) {
                    Toast.makeText(context, "Password is required", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                verifyPassword(email, password)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun verifyPassword(email: String, password: String) {
        val user = auth.currentUser ?: return
        val credential = EmailAuthProvider.getCredential(email, password)

        user.reauthenticate(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    startActivity(Intent(requireContext(), Add_Room::class.java))
                } else {
                    Toast.makeText(context, "Incorrect password", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun populateVersionInfo() {
        val versionName = BuildConfig.VERSION_NAME
        val versionCode = BuildConfig.VERSION_CODE
        binding.tvVersion.text = getString(R.string.version, versionName, versionCode.toString())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
