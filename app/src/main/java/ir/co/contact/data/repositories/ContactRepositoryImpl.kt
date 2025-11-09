package ir.co.contact.data.repositories

import android.content.ContentResolver
import android.content.Context
import android.provider.ContactsContract
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.co.contact.data.source.local.ContactObserver
import ir.co.contact.data.source.local.database.ContactDao
import ir.co.contact.data.source.local.database.toDomain
import ir.co.contact.data.source.local.database.toEntity
import ir.co.contact.domain.model.Contact
import ir.co.contact.domain.repositories.ContactRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ContactRepositoryImpl @Inject constructor(
    private val contactDao: ContactDao,
    @ApplicationContext private val context: Context
) : ContactRepository {
    
    private val contactObserver by lazy { ContactObserver(context) }

    override fun getContacts(): Flow<List<Contact>> {
        return contactDao.getContacts()
            .map { entities -> entities.map { it.toDomain() } }
    }

    override fun getContactById(contactId: String): Flow<Contact?> {
        return contactDao.getContactById(contactId)
            .map { entity -> entity?.toDomain() }
    }

    override suspend fun syncContactsFromPhone(contentResolver: ContentResolver): Result<Unit> {
        return try {
            val contacts = fetchContactsFromPhone(contentResolver)
            contactDao.replaceContacts(contacts.map { it.toEntity() })
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun hasCachedContacts(): Boolean {
        return contactDao.getContactsCount() > 0
    }

    override fun observeContactChanges(): Flow<Unit> {
        return contactObserver.observeContactChanges()
    }

    private fun fetchContactsFromPhone(contentResolver: ContentResolver): List<Contact> {
        val contacts = mutableListOf<Contact>()

        val projection = arrayOf(
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.DISPLAY_NAME,
            ContactsContract.Contacts.STARRED
        )

        contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            projection,
            null,
            null,
            "${ContactsContract.Contacts.DISPLAY_NAME} ASC"
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndex(ContactsContract.Contacts._ID)
            val nameColumn = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
            val starredColumn = cursor.getColumnIndex(ContactsContract.Contacts.STARRED)

            while (cursor.moveToNext()) {
                val id = cursor.getString(idColumn)
                val name = cursor.getString(nameColumn) ?: continue
                val isStarred = cursor.getInt(starredColumn) > 0

                // Get all phone numbers
                val phoneNumbers = fetchPhoneNumbers(contentResolver, id)
                val primaryPhone = phoneNumbers.firstOrNull()?.number ?: ""

                // Get all emails
                val emails = fetchEmails(contentResolver, id)

                // Get addresses
                val addresses = fetchAddresses(contentResolver, id)

                if (phoneNumbers.isNotEmpty()) {
                    contacts.add(
                        Contact(
                            id = id,
                            name = name,
                            phoneNumber = formatPhoneNumber(primaryPhone),
                            isFavorite = isStarred,
                            phoneNumbers = phoneNumbers,
                            emails = emails,
                            addresses = addresses
                        )
                    )
                }
            }
        }

        return contacts
    }

    private fun fetchPhoneNumbers(contentResolver: ContentResolver, contactId: String): List<ir.co.contact.domain.model.PhoneNumber> {
        val phoneNumbers = mutableListOf<ir.co.contact.domain.model.PhoneNumber>()

        contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.TYPE
            ),
            "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
            arrayOf(contactId),
            null
        )?.use { cursor ->
            val numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val typeIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE)

            while (cursor.moveToNext()) {
                val number = cursor.getString(numberIndex) ?: continue
                val type = when (cursor.getInt(typeIndex)) {
                    ContactsContract.CommonDataKinds.Phone.TYPE_HOME -> ir.co.contact.domain.model.PhoneType.HOME
                    ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE -> ir.co.contact.domain.model.PhoneType.MOBILE
                    ContactsContract.CommonDataKinds.Phone.TYPE_WORK -> ir.co.contact.domain.model.PhoneType.WORK
                    else -> ir.co.contact.domain.model.PhoneType.OTHER
                }
                phoneNumbers.add(ir.co.contact.domain.model.PhoneNumber(formatPhoneNumber(number), type))
            }
        }

        return phoneNumbers
    }

    private fun fetchEmails(contentResolver: ContentResolver, contactId: String): List<ir.co.contact.domain.model.Email> {
        val emails = mutableListOf<ir.co.contact.domain.model.Email>()

        contentResolver.query(
            ContactsContract.CommonDataKinds.Email.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.TYPE
            ),
            "${ContactsContract.CommonDataKinds.Email.CONTACT_ID} = ?",
            arrayOf(contactId),
            null
        )?.use { cursor ->
            val addressIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS)
            val typeIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE)

            while (cursor.moveToNext()) {
                val address = cursor.getString(addressIndex) ?: continue
                val type = when (cursor.getInt(typeIndex)) {
                    ContactsContract.CommonDataKinds.Email.TYPE_HOME -> ir.co.contact.domain.model.EmailType.PERSONAL
                    ContactsContract.CommonDataKinds.Email.TYPE_WORK -> ir.co.contact.domain.model.EmailType.WORK
                    else -> ir.co.contact.domain.model.EmailType.OTHER
                }
                emails.add(ir.co.contact.domain.model.Email(address, type))
            }
        }

        return emails
    }

    private fun fetchAddresses(contentResolver: ContentResolver, contactId: String): List<ir.co.contact.domain.model.Address> {
        val addresses = mutableListOf<ir.co.contact.domain.model.Address>()

        contentResolver.query(
            ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS,
                ContactsContract.CommonDataKinds.StructuredPostal.TYPE
            ),
            "${ContactsContract.CommonDataKinds.StructuredPostal.CONTACT_ID} = ?",
            arrayOf(contactId),
            null
        )?.use { cursor ->
            val addressIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS)
            val typeIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.TYPE)

            while (cursor.moveToNext()) {
                val address = cursor.getString(addressIndex) ?: continue
                val type = when (cursor.getInt(typeIndex)) {
                    ContactsContract.CommonDataKinds.StructuredPostal.TYPE_HOME -> ir.co.contact.domain.model.AddressType.HOME
                    ContactsContract.CommonDataKinds.StructuredPostal.TYPE_WORK -> ir.co.contact.domain.model.AddressType.WORK
                    else -> ir.co.contact.domain.model.AddressType.OTHER
                }
                addresses.add(ir.co.contact.domain.model.Address(address, type))
            }
        }

        return addresses
    }

    private fun formatPhoneNumber(phone: String): String {
        val original = phone.trim()

        if (original.startsWith("+")) {
            return original
        }

        val digits = original.filter { it.isDigit() }
        if (digits.length == 10) {
            return "(${digits.substring(0, 3)}) ${digits.substring(3, 6)}-${digits.substring(6)}"
        }

        return original
    }
}

