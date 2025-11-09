package ir.co.contact.data.source.local.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import ir.co.contact.domain.model.Address
import ir.co.contact.domain.model.AddressType
import ir.co.contact.domain.model.Contact
import ir.co.contact.domain.model.Email
import ir.co.contact.domain.model.EmailType
import ir.co.contact.domain.model.PhoneNumber
import ir.co.contact.domain.model.PhoneType

@Entity(tableName = "contacts")
@TypeConverters(Converters::class)
data class ContactEntity(
    @PrimaryKey val id: String,
    val name: String,
    val phoneNumber: String, // Primary phone
    val isFavorite: Boolean,
    val phoneNumbers: List<PhoneNumber> = emptyList(),
    val emails: List<Email> = emptyList(),
    val addresses: List<Address> = emptyList()
)

// Type converters for Room
class Converters {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    
    private val phoneNumberListType = Types.newParameterizedType(List::class.java, PhoneNumber::class.java)
    private val phoneNumberListAdapter: JsonAdapter<List<PhoneNumber>> = moshi.adapter(phoneNumberListType)
    
    private val emailListType = Types.newParameterizedType(List::class.java, Email::class.java)
    private val emailListAdapter: JsonAdapter<List<Email>> = moshi.adapter(emailListType)
    
    private val addressListType = Types.newParameterizedType(List::class.java, Address::class.java)
    private val addressListAdapter: JsonAdapter<List<Address>> = moshi.adapter(addressListType)
    
    @TypeConverter
    fun fromPhoneNumberList(value: List<PhoneNumber>?): String? {
        return value?.let { phoneNumberListAdapter.toJson(it) }
    }
    
    @TypeConverter
    fun toPhoneNumberList(value: String?): List<PhoneNumber> {
        return value?.let { phoneNumberListAdapter.fromJson(it) } ?: emptyList()
    }
    
    @TypeConverter
    fun fromEmailList(value: List<Email>?): String? {
        return value?.let { emailListAdapter.toJson(it) }
    }
    
    @TypeConverter
    fun toEmailList(value: String?): List<Email> {
        return value?.let { emailListAdapter.fromJson(it) } ?: emptyList()
    }
    
    @TypeConverter
    fun fromAddressList(value: List<Address>?): String? {
        return value?.let { addressListAdapter.toJson(it) }
    }
    
    @TypeConverter
    fun toAddressList(value: String?): List<Address> {
        return value?.let { addressListAdapter.fromJson(it) } ?: emptyList()
    }
}

fun ContactEntity.toDomain(): Contact = Contact(
    id = id,
    name = name,
    phoneNumber = phoneNumber,
    isFavorite = isFavorite,
    phoneNumbers = phoneNumbers,
    emails = emails,
    addresses = addresses
)

fun Contact.toEntity(): ContactEntity = ContactEntity(
    id = id,
    name = name,
    phoneNumber = phoneNumber,
    isFavorite = isFavorite,
    phoneNumbers = phoneNumbers,
    emails = emails,
    addresses = addresses
)

