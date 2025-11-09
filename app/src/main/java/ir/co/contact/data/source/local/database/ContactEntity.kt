package ir.co.contact.data.source.local.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import ir.co.contact.domain.model.Contact

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey val id: String,
    val name: String,
    val phoneNumber: String,
    val isFavorite: Boolean
)

fun ContactEntity.toDomain(): Contact = Contact(
    id = id,
    name = name,
    phoneNumber = phoneNumber,
    isFavorite = isFavorite
)

fun Contact.toEntity(): ContactEntity = ContactEntity(
    id = id,
    name = name,
    phoneNumber = phoneNumber,
    isFavorite = isFavorite
)

