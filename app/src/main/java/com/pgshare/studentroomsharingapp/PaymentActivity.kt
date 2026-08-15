package com.pgshare.studentroomsharingapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.pgshare.studentroomsharingapp.databinding.ActivityPaymentBinding
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import org.json.JSONObject

class PaymentActivity : AppCompatActivity(), PaymentResultListener {

    private lateinit var binding: ActivityPaymentBinding
    private var roomId: String? = null
    private var roomName: String? = null
    private var rentAmount: Long = 0
    private var depositAmount: Long = 0
    private var totalAmountPaise: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbarPayment.setNavigationOnClickListener { finish() }

        Checkout.preload(applicationContext)

        extractIntentData()
        setupUI()

        binding.btnPayNow.setOnClickListener {
            startRazorpayPayment()
        }
    }

    private fun extractIntentData() {
        roomId = intent.getStringExtra("ROOM_ID")
        roomName = intent.getStringExtra("ROOM_NAME") ?: "Student Room"
        val priceStr = intent.getStringExtra("ROOM_PRICE") ?: "0"
        val depositStr = intent.getStringExtra("ROOM_DEPOSIT") ?: "0"

        rentAmount = priceStr.replace("[^0-9]".toRegex(), "").toLongOrNull() ?: 0
        depositAmount = depositStr.replace("[^0-9]".toRegex(), "").toLongOrNull() ?: 0
        
        val totalRupees = rentAmount + depositAmount
        totalAmountPaise = if (totalRupees > 0) totalRupees * 100 else 10000 // Fallback minimum 100 INR in paise if empty
    }

    private fun setupUI() {
        binding.tvRoomName.text = roomName
        binding.tvRentAmount.text = "₹$rentAmount"
        binding.tvDepositAmount.text = "₹$depositAmount"
        val totalRupees = rentAmount + depositAmount
        binding.tvTotalAmount.text = "₹$totalRupees"
    }

    private fun startRazorpayPayment() {
        val checkout = Checkout()
        // Test key ID for Razorpay SDK initialization
        checkout.setKeyID("rzp_test_YourRazorpayKey")

        val currentUser = FirebaseAuth.getInstance().currentUser
        val userEmail = currentUser?.email ?: "student@example.com"

        try {
            val options = JSONObject().apply {
                put("name", "Student Room Sharing")
                put("description", "Room Booking & Deposit Payment for $roomName")
                put("currency", "INR")
                put("amount", totalAmountPaise)
                
                val prefill = JSONObject().apply {
                    put("email", userEmail)
                    put("contact", "9876543210")
                }
                put("prefill", prefill)

                val theme = JSONObject().apply {
                    put("color", "#6750A4")
                }
                put("theme", theme)
            }

            checkout.open(this, options)
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to start Razorpay payment: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onPaymentSuccess(razorpayPaymentID: String?) {
        Toast.makeText(this, "Payment Successful! Txn ID: $razorpayPaymentID", Toast.LENGTH_LONG).show()
        
        markRoomAsBooked(razorpayPaymentID)
    }

    override fun onPaymentError(code: Int, response: String?) {
        Toast.makeText(this, "Payment Failed/Cancelled ($code): $response", Toast.LENGTH_LONG).show()
    }

    private fun markRoomAsBooked(paymentId: String?) {
        val targetRoomId = roomId ?: return finish()
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        val updates = hashMapOf<String, Any>(
            "roomBooked" to true,
            "bookedBy" to currentUserId,
            "paymentTxnId" to (paymentId ?: "")
        )

        FirebaseDatabase.getInstance().getReference("Rooms")
            .child(targetRoomId)
            .updateChildren(updates)
            .addOnSuccessListener {
                Toast.makeText(this, "Room Reserved Successfully!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                finish()
            }
    }
}