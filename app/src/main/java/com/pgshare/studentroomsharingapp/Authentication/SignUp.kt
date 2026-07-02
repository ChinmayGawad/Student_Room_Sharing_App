package com.pgshare.studentroomsharingapp.Authentication

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.pgshare.studentroomsharingapp.databinding.ActivitySignUpBinding
import com.pgshare.studentroomsharingapp.viewmodel.SignUpEvent
import com.pgshare.studentroomsharingapp.viewmodel.SignUpViewModel
import kotlinx.coroutines.launch

class SignUp : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var viewModel: SignUpViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = SignUpViewModel()

        binding.btnRegister.setOnClickListener { onRegisterBtnClick() }

        binding.tvLoginLink.setOnClickListener { finish() }

        observeEvents()
    }

    private fun observeEvents() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { event ->
                    when (event) {
                        is SignUpEvent.Success -> {
                            Toast.makeText(this@SignUp, "Account Created Successfully!", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@SignUp, Login::class.java)
                            intent.putExtra("email", binding.etEmailSignup.text.toString().trim())
                            startActivity(intent)
                            finish()
                        }
                        is SignUpEvent.Error -> {
                            Toast.makeText(this@SignUp, event.message, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    private fun onRegisterBtnClick() {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmailSignup.text.toString().trim()
        val password = binding.etPasswordSignup.text.toString().trim()
        val confirmPassword = binding.etConfirmPasswordSignup.text.toString().trim()

        val role = when (binding.cgRole.checkedChipId) {
            binding.chipStudent.id -> "student"
            binding.chipOwner.id -> "owner"
            else -> ""
        }

        if (isValidInput(name, email, password, confirmPassword, role)) {
            Toast.makeText(this, "Creating account...", Toast.LENGTH_SHORT).show()
            viewModel.signUp(name, email, password, role)
        }
    }

    private fun isValidInput(name: String, email: String, password: String, confirmPassword: String, role: String): Boolean {
        var valid = true

        if (TextUtils.isEmpty(name)) {
            binding.etName.error = "Full Name is required"
            valid = false
        } else {
            binding.etName.error = null
        }

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmailSignup.error = "Invalid email address"
            valid = false
        } else {
            binding.etEmailSignup.error = null
        }

        if (TextUtils.isEmpty(password) || password.length < 6) {
            binding.etPasswordSignup.error = "Password must be at least 6 characters"
            valid = false
        } else {
            binding.etPasswordSignup.error = null
        }

        if (password != confirmPassword) {
            binding.etConfirmPasswordSignup.error = "Passwords do not match"
            valid = false
        } else {
            binding.etConfirmPasswordSignup.error = null
        }

        if (role.isEmpty()) {
            Toast.makeText(this, "Please select if you are a Student or an Owner", Toast.LENGTH_SHORT).show()
            valid = false
        }

        return valid
    }
}