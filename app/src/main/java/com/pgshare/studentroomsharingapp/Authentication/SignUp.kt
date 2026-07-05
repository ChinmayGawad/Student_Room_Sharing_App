package com.pgshare.studentroomsharingapp.Authentication

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import androidx.core.util.PatternsCompat
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.pgshare.studentroomsharingapp.R
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

        binding.btnBack.setOnClickListener { finish() }
        binding.btnLoginLink.setOnClickListener { finish() }
        binding.btnCreateAccount.setOnClickListener { onRegisterBtnClick() }

        observeEvents()
    }

    private fun observeEvents() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { event ->
                    setLoading(false)
                    when (event) {
                        is SignUpEvent.Success -> {
                            val intent = Intent(this@SignUp, Login::class.java)
                            intent.putExtra("email", binding.tilEmail.editText?.text.toString().trim())
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
        val name = binding.tilName.editText?.text.toString().trim()
        val email = binding.tilEmail.editText?.text.toString().trim()
        val password = binding.tilPassword.editText?.text.toString().trim()
        val confirmPassword = binding.tilConfirmPassword.editText?.text.toString().trim()

        val role = when (binding.chipGroupRole.checkedChipId) {
            binding.chipStudent.id -> "student"
            binding.chipProfessional.id -> "professional"
            binding.chipOwner.id -> "owner"
            else -> ""
        }

        if (isValidInput(name, email, password, confirmPassword, role)) {
            setLoading(true)
            viewModel.signUp(name, email, password, role)
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.btnCreateAccount.isEnabled = !isLoading
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnCreateAccount.text = if (isLoading) "" else getString(R.string.create_account)
    }

    private fun isValidInput(name: String, email: String, password: String, confirmPassword: String, role: String): Boolean {
        var valid = true

        if (TextUtils.isEmpty(name)) {
            binding.tilName.error = "Full Name is required"
            valid = false
        } else {
            binding.tilName.error = null
        }

        if (TextUtils.isEmpty(email) || !PatternsCompat.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Invalid email address"
            valid = false
        } else {
            binding.tilEmail.error = null
        }

        if (TextUtils.isEmpty(password) || password.length < 6) {
            binding.tilPassword.error = "Password must be at least 6 characters"
            valid = false
        } else {
            binding.tilPassword.error = null
        }

        if (password != confirmPassword) {
            binding.tilConfirmPassword.error = "Passwords do not match"
            valid = false
        } else {
            binding.tilConfirmPassword.error = null
        }

        if (!binding.cbTerms.isChecked) {
            Toast.makeText(this, "Please agree to the Terms & Conditions", Toast.LENGTH_SHORT).show()
            valid = false
        }

        if (role.isEmpty()) {
            Toast.makeText(this, "Please select if you are a Student or an Owner", Toast.LENGTH_SHORT).show()
            valid = false
        }

        return valid
    }
}
