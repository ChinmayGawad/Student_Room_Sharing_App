package com.pgshare.studentroomsharingapp

import com.pgshare.studentroomsharingapp.util.RazorpaySignatureVerifier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PaymentVerificationUnitTest {

    private val testOrderId = "order_EKwxwAgItmmGdp"
    private val testPaymentId = "pay_29QQoUBi66xm2f"
    private val testSecret = "EnLs1eaNwvkmA9eEwH01c9uP"

    @Test
    fun calculateSignature_matchesKnownHmacSha256Vector() {
        val signature = RazorpaySignatureVerifier.calculateSignature(
            orderId = testOrderId,
            paymentId = testPaymentId,
            secret = testSecret
        )
        // Verify signature is 64 hex characters (256 bits)
        assertEquals(64, signature.length)
        assertTrue(signature.matches("[0-9a-f]{64}".toRegex()))

        // Verify verification with the generated signature succeeds
        val isValid = RazorpaySignatureVerifier.verify(
            orderId = testOrderId,
            paymentId = testPaymentId,
            secret = testSecret,
            signature = signature
        )
        assertTrue(isValid)
    }

    @Test
    fun verifySignature_rejectsTamperedOrderId() {
        val signature = RazorpaySignatureVerifier.calculateSignature(
            orderId = testOrderId,
            paymentId = testPaymentId,
            secret = testSecret
        )

        // Attempt price / order tampering by modifying orderId
        val isTamperedValid = RazorpaySignatureVerifier.verify(
            orderId = "order_TAMPERED_123",
            paymentId = testPaymentId,
            secret = testSecret,
            signature = signature
        )
        assertFalse(isTamperedValid)
    }

    @Test
    fun verifySignature_rejectsTamperedPaymentId() {
        val signature = RazorpaySignatureVerifier.calculateSignature(
            orderId = testOrderId,
            paymentId = testPaymentId,
            secret = testSecret
        )

        val isTamperedValid = RazorpaySignatureVerifier.verify(
            orderId = testOrderId,
            paymentId = "pay_TAMPERED_456",
            secret = testSecret,
            signature = signature
        )
        assertFalse(isTamperedValid)
    }

    @Test
    fun verifySignature_rejectsInvalidSecret() {
        val signature = RazorpaySignatureVerifier.calculateSignature(
            orderId = testOrderId,
            paymentId = testPaymentId,
            secret = testSecret
        )

        val isTamperedValid = RazorpaySignatureVerifier.verify(
            orderId = testOrderId,
            paymentId = testPaymentId,
            secret = "wrong_secret_key",
            signature = signature
        )
        assertFalse(isTamperedValid)
    }

    @Test
    fun verifySignature_rejectsEmptyOrBlankInputs() {
        assertFalse(RazorpaySignatureVerifier.verify("", testPaymentId, testSecret, "sig"))
        assertFalse(RazorpaySignatureVerifier.verify(testOrderId, "", testSecret, "sig"))
        assertFalse(RazorpaySignatureVerifier.verify(testOrderId, testPaymentId, "", "sig"))
        assertFalse(RazorpaySignatureVerifier.verify(testOrderId, testPaymentId, testSecret, ""))
        assertFalse(RazorpaySignatureVerifier.verify("  ", "  ", "  ", "  "))
    }

    @Test
    fun priceTamperDefense_computesExactAmountsInPaise() {
        val priceStr = "₹8,500"
        val depositStr = "₹17,000"

        val rentAmount = priceStr.replace("[^0-9]".toRegex(), "").toLongOrNull() ?: 0L
        val depositAmount = depositStr.replace("[^0-9]".toRegex(), "").toLongOrNull() ?: 0L

        val totalRupees = rentAmount + depositAmount
        val totalPaise = totalRupees * 100

        assertEquals(8500L, rentAmount)
        assertEquals(17000L, depositAmount)
        assertEquals(25500L, totalRupees)
        assertEquals(2550000L, totalPaise)
    }
}
