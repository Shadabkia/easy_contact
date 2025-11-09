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

                // Get phone number
                contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
                    "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                    arrayOf(id),
                    null
                )?.use { phoneCursor ->
                    if (phoneCursor.moveToFirst()) {
                        val phoneIndex = phoneCursor.getColumnIndex(
                            ContactsContract.CommonDataKinds.Phone.NUMBER
                        )
                        val phoneNumber = phoneCursor.getString(phoneIndex) ?: ""

                        contacts.add(
                            Contact(
                                id = id,
                                name = name,
                                phoneNumber = formatPhoneNumber(phoneNumber),
                                isFavorite = isStarred
                            )
                        )
                    }
                }
            }
        }

        return contacts
    }

    private fun formatPhoneNumber(phone: String): String {
        val original = phone.trim()

        // Keep international numbers as-is (they start with +)
        if (original.startsWith("+")) {
            return original
        }

        // Format 10-digit numbers: (555) 123-4567
        val digits = original.filter { it.isDigit() }
        if (digits.length == 10) {
            return "(${digits.substring(0, 3)}) ${digits.substring(3, 6)}-${digits.substring(6)}"
        }

        // Return original for other formats
        return original
    }
}

