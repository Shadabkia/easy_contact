package ir.co.contact.domain.model

// Contact data model
data class Contact(
    val id: String,
    val name: String,
    val phoneNumber: String, // Primary phone number
    val isFavorite: Boolean = false,
    val phoneNumbers: List<PhoneNumber> = emptyList(),
    val emails: List<Email> = emptyList(),
    val addresses: List<Address> = emptyList()
)

// Phone number with type
data class PhoneNumber(
    val number: String,
    val type: PhoneType
)

enum class PhoneType { HOME, MOBILE, WORK, OTHER }

// Email with type
data class Email(
    val address: String,
    val type: EmailType
)

enum class EmailType { PERSONAL, WORK, OTHER }

// Address with type  
data class Address(
    val address: String,
    val type: AddressType
)

enum class AddressType { HOME, WORK, OTHER }
