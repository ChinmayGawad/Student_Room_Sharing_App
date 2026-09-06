package com.pgshare.studentroomsharingapp.Fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.snackbar.Snackbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.pgshare.studentroomsharingapp.AddRoomActivity
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

        val prefs = requireContext().getSharedPreferences("app_settings", android.content.Context.MODE_PRIVATE)
        val isDarkModeSaved = prefs.getBoolean("key_dark_mode", false)
        val currentNightMode = resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
        val isSystemDark = currentNightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES
        binding.switchDarkMode.isChecked = if (prefs.contains("key_dark_mode")) isDarkModeSaved else isSystemDark

        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("key_dark_mode", isChecked).apply()
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        binding.rowDarkMode.setOnClickListener {
            binding.switchDarkMode.toggle()
        }

        binding.rowNotifications.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Push Notifications")
                .setMessage("Push notifications are enabled. You will receive realtime alerts for new room listings in your area and incoming messages from roommates or property owners.")
                .setPositiveButton("OK", null)
                .show()
        }

        binding.rowLanguage.setOnClickListener {
            val languages = arrayOf("English (Default)", "Hindi (हिन्दी)", "Marathi (मराठी)")
            val currentLangIndex = prefs.getInt("key_language_index", 0)
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Select App Language")
                .setSingleChoiceItems(languages, currentLangIndex) { dialog, which ->
                    prefs.edit().putInt("key_language_index", which).apply()
                    Snackbar.make(binding.root, "Language preference set to: ${languages[which]}", Snackbar.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        binding.rowPersonalInfo.setOnClickListener {
            val user = auth.currentUser
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Personal Information")
                .setMessage("Name: ${user?.displayName ?: "Student User"}\nEmail: ${user?.email ?: "Not logged in"}\nAccount ID: ${user?.uid ?: "N/A"}")
                .setPositiveButton("Close", null)
                .show()
        }

        binding.rowPrivacy.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Privacy Policy")
                .setMessage("Student Room Sharing App is committed to protecting your privacy.\n\n• We do not sell or share personal data.\n• Payment processing is protected by Razorpay 256-bit encryption.\n• Chats are end-to-end access controlled via Firebase Security Rules.")
                .setPositiveButton("Understood", null)
                .show()
        }

        binding.rowSecurity.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Security Settings")
                .setMessage("Your account is secured with Firebase Authentication and zero-trust HMAC signature validation for all financial transactions.")
                .setPositiveButton("OK", null)
                .show()
        }

        binding.rowListProperty.setOnClickListener { handleListProperty() }

        binding.rowHelpCenter.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Help Center & Support")
                .setMessage("Need assistance or have questions?\n\n• Email Support: chinmaygawad365@gmail.com\n• Booking Issues: Contact our helpdesk 24/7\n• Deposit Protection: Deposits are reserved securely until move-in confirmation.")
                .setPositiveButton("Contact Support") { _, _ ->
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "message/rfc822"
                        putExtra(Intent.EXTRA_EMAIL, arrayOf("chinmaygawad365@gmail.com"))
                        putExtra(Intent.EXTRA_SUBJECT, "Support Request - Student Room Sharing")
                    }
                    if (intent.resolveActivity(requireContext().packageManager) != null) {
                        startActivity(intent)
                    }
                }
                .setNegativeButton("Close", null)
                .show()
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
                Snackbar.make(binding.root, R.string.no_email_app, Snackbar.LENGTH_SHORT).show()
            }
        }

        binding.rowRateApp.setOnClickListener {
            Snackbar.make(binding.root, "Thank you for supporting Student Room Sharing!", Snackbar.LENGTH_SHORT).show()
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
                Snackbar.make(binding.root, "Only property owners can list rooms", Snackbar.LENGTH_SHORT).show()
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
                    Snackbar.make(binding.root, "Password is required", Snackbar.LENGTH_SHORT).show()
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
                    startActivity(Intent(requireContext(), AddRoomActivity::class.java))
                } else {
                    Snackbar.make(binding.root, "Incorrect password", Snackbar.LENGTH_SHORT).show()
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
