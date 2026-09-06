package com.pgshare.studentroomsharingapp.util

import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object RazorpaySignatureVerifier {

    /**
     * Computes the HMAC-SHA256 signature for Razorpay payment verification
     * using the order ID and payment ID joined by a pipe delimiter.
     */
    fun calculateSignature(orderId: String, paymentId: String, secret: String): String {
        val payload = "$orderId|$paymentId"
        val mac = Mac.getInstance("HmacSHA256")
        val secretKeySpec = SecretKeySpec(secret.toByteArray(Charsets.UTF_8), "HmacSHA256")
        mac.init(secretKeySpec)
        val hash = mac.doFinal(payload.toByteArray(Charsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }

    /**
     * Validates that the provided payment signature matches the cryptographic HMAC-SHA256
     * hash of orderId and paymentId with constant-time equality check to prevent timing attacks.
     */
    fun verify(orderId: String, paymentId: String, secret: String, signature: String): Boolean {
        if (orderId.isBlank() || paymentId.isBlank() || secret.isBlank() || signature.isBlank()) {
            return false
        }
        val calculated = calculateSignature(orderId, paymentId, secret)
        return MessageDigest.isEqual(
            calculated.lowercase().toByteArray(Charsets.UTF_8),
            signature.lowercase().toByteArray(Charsets.UTF_8)
        )
    }
}
