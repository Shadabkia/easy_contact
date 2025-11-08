package ir.co.contact.domain.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ErrorResponse(
    @Json(name = "message")
    val message: String,
//    @Json(name = "timeStamp")
//    val errors: String?
)