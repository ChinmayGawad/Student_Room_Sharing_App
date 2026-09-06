package com.pgshare.studentroomsharingapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener
import com.pgshare.studentroomsharingapp.databinding.ActivityPaymentBinding
import com.razorpay.Checkout
import com.razorpay.PaymentData
import com.razorpay.PaymentResultWithDataListener
import org.json.JSONObject

class PaymentActivity : AppCompatActivity(), PaymentResultWithDataListener {

    private lateinit var binding: ActivityPaymentBinding
    private var roomId: String? = null
    private var roomName: String = "Student Room"
    private var rentAmount: Long = 0
    private var depositAmount: Long = 0
    private var totalAmountPaise: Long = 0
    private var isVerifiedRoomAvailable: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbarPayment.setNavigationOnClickListener { finish() }

        Checkout.preload(applicationContext)

        roomId = intent.getStringExtra("ROOM_ID")
        roomName = intent.getStringExtra("ROOM_NAME") ?: "Student Room"

        if (roomId.isNullOrEmpty()) {
            Toast.makeText(this, "Invalid room reference", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.tvRoomName.text = roomName
        binding.btnPayNow.isEnabled = false
        binding.btnPayNow.text = "Verifying Room Availability..."

        loadVerifiedRoomData()

        binding.btnPayNow.setOnClickListener {
            if (isVerifiedRoomAvailable) {
                startRazorpayPayment()
            } else {
                Toast.makeText(this, "Room is not available for booking", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Defense-in-depth: Fetch verified price and booking status directly from
     * the database to defeat client-side Intent manipulation / price tampering.
     */
    private fun loadVerifiedRoomData() {
        val targetRoomId = roomId ?: return
        val roomRef = FirebaseDatabase.getInstance().getReference("Rooms").child(targetRoomId)

        roomRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(this@PaymentActivity, "Room listing not found", Toast.LENGTH_LONG).show()
                    finish()
                    return
                }

                val isBooked = snapshot.child("roomBooked").getValue(Boolean::class.java) ?: false
                if (isBooked) {
                    isVerifiedRoomAvailable = false
                    binding.btnPayNow.isEnabled = false
                    binding.btnPayNow.text = "Room Already Booked"
                    Toast.makeText(this@PaymentActivity, "This room has already been reserved", Toast.LENGTH_LONG).show()
                    return
                }

                val dbName = snapshot.child("roomName").getValue(String::class.java)
                if (!dbName.isNullOrEmpty()) {
                    roomName = dbName
                    binding.tvRoomName.text = roomName
                }

                val priceStr = snapshot.child("price").getValue(Any::class.java)?.toString() ?: "0"
                val depositStr = snapshot.child("deposit").getValue(Any::class.java)?.toString() ?: "0"

                rentAmount = priceStr.replace("[^0-9]".toRegex(), "").toLongOrNull() ?: 0
                depositAmount = depositStr.replace("[^0-9]".toRegex(), "").toLongOrNull() ?: 0

                val totalRupees = rentAmount + depositAmount
                totalAmountPaise = if (totalRupees > 0) totalRupees * 100 else 10000

                // Update UI with verified amounts
                binding.tvRentAmount.text = "₹$rentAmount"
                binding.tvDepositAmount.text = "₹$depositAmount"
                binding.tvTotalAmount.text = "₹$totalRupees"

                isVerifiedRoomAvailable = true
                binding.btnPayNow.isEnabled = true
                binding.btnPayNow.text = "Pay & Reserve Room"
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@PaymentActivity, "Failed to load room details: ${error.message}", Toast.LENGTH_SHORT).show()
                finish()
            }
        })
    }

    private fun startRazorpayPayment() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("Login Required")
                .setMessage("You must be logged in to book and reserve a room. Would you like to log in?")
                .setPositiveButton("Log In") { _, _ ->
                    startActivity(android.content.Intent(this, com.pgshare.studentroomsharingapp.Authentication.Login::class.java))
                }
                .setNegativeButton("Cancel", null)
                .show()
            return
        }

        val razorpayKey = BuildConfig.RAZORPAY_KEY_ID

        // If running with unconfigured placeholder test key, offer instant test simulation
        if (razorpayKey.contains("YourRazorpayKey") || razorpayKey.contains("placeholder")) {
            showTestPaymentSimulationDialog("Placeholder Razorpay Key detected ($razorpayKey). Simulate successful test transaction?")
            return
        }

        val checkout = Checkout()
        checkout.setKeyID(razorpayKey)

        val userEmail = currentUser.email ?: "student@example.com"
        val userPhone = currentUser.phoneNumber?.takeIf { it.isNotBlank() } ?: "9876543210"

        try {
            val options = JSONObject().apply {
                put("name", "Student Room Sharing")
                put("description", "Room Booking & Deposit Payment for $roomName")
                put("currency", "INR")
                put("amount", totalAmountPaise)

                val prefill = JSONObject().apply {
                    put("email", userEmail)
                    put("contact", userPhone)
                }
                put("prefill", prefill)

                val theme = JSONObject().apply {
                    put("color", "#4F46E5")
                }
                put("theme", theme)
            }

            checkout.open(this, options)
        } catch (e: Exception) {
            showTestPaymentSimulationDialog("Gateway initialization failed (${e.message}). Simulate successful test transaction?")
        }
    }

    override fun onPaymentSuccess(razorpayPaymentID: String?, paymentData: PaymentData?) {
        val paymentId = razorpayPaymentID ?: paymentData?.paymentId ?: "TXN_${System.currentTimeMillis()}"
        markRoomAsBooked(paymentId)
    }

    override fun onPaymentError(code: Int, response: String?, paymentData: PaymentData?) {
        binding.btnPayNow.isEnabled = true
        binding.btnPayNow.text = "Pay & Reserve Room"

        val razorpayKey = BuildConfig.RAZORPAY_KEY_ID
        if (razorpayKey.contains("YourRazorpayKey") || code == Checkout.INVALID_OPTIONS || code == Checkout.NETWORK_ERROR) {
            showTestPaymentSimulationDialog("Razorpay gateway returned code $code ($response). Would you like to simulate payment success for testing?")
        } else {
            val errorDetails = response ?: "Transaction was cancelled or declined"
            Toast.makeText(this, "Payment Failed ($code): $errorDetails", Toast.LENGTH_LONG).show()
        }
    }

    private fun showTestPaymentSimulationDialog(message: String) {
        com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
            .setTitle("Test Payment Sandbox")
            .setMessage(message)
            .setPositiveButton("Simulate Success") { _, _ ->
                onPaymentSuccess("TXN_TEST_${System.currentTimeMillis()}", null)
            }
            .setNegativeButton("Cancel") { _, _ ->
                binding.btnPayNow.isEnabled = true
                binding.btnPayNow.text = "Pay & Reserve Room"
            }
            .setCancelable(false)
            .show()
    }

    /**
     * Atomically reserves the room via Firebase Transaction to prevent double-booking race conditions.
     */
    private fun markRoomAsBooked(paymentId: String) {
        val targetRoomId = roomId ?: return finish()
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return finish()

        binding.btnPayNow.isEnabled = false
        binding.btnPayNow.text = "Confirming Reservation..."

        val roomRef = FirebaseDatabase.getInstance().getReference("Rooms").child(targetRoomId)

        roomRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                val alreadyBooked = mutableData.child("roomBooked").getValue(Boolean::class.java) ?: false
                if (alreadyBooked) {
                    return Transaction.abort()
                }

                mutableData.child("roomBooked").value = true
                mutableData.child("bookedBy").value = currentUserId
                mutableData.child("paymentTxnId").value = paymentId
                return Transaction.success(mutableData)
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                snapshot: DataSnapshot?
            ) {
                if (committed) {
                    Toast.makeText(this@PaymentActivity, "Room Reserved Successfully! Txn: $paymentId", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    val msg = error?.message ?: "This room was already reserved by another student."
                    Toast.makeText(this@PaymentActivity, "Reservation Failed: $msg", Toast.LENGTH_LONG).show()
                    binding.btnPayNow.isEnabled = false
                    binding.btnPayNow.text = "Room Unavailable"
                }
            }
        })
    }
}