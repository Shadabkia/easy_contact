package ir.co.contact.domain.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AuthErrorResponse(
    @Json(name = "success")
    val success: Boolean,
    @Json(name = "message")
    val message: String
)