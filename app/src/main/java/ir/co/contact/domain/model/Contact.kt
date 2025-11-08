package ir.co.contact.domain.model

// 1. Data model
data class Contact(
    val id: String,
    val name: String,
    val phoneNumber: String,
    val isFavorite: Boolean = false
)
