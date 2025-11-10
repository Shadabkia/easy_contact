package ir.co.contact.data.repositories

import android.content.ContentResolver
import android.content.Context
import android.provider.ContactsContract
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.co.contact.data.source.local.ContactObserver
import ir.co.contact.data.source.local.DataStoreConstants
import ir.co.contact.data.source.local.DataStoreManager
import ir.co.contact.data.source.local.database.ContactDao
import ir.co.contact.data.source.local.database.toDomain
import ir.co.contact.data.source.local.database.toEntity
import ir.co.contact.domain.model.Contact
import ir.co.contact.domain.repositories.ContactRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ContactRepositoryImpl @Inject constructor(
    private val contactDao: ContactDao,
    private val dataStoreManager: DataStoreManager,
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
            val lastSyncTimestamp = dataStoreManager.getData(DataStoreConstants.LAST_CONTACT_SYNC_TIMESTAMP).first() ?: 0L
            val currentTimestamp = System.currentTimeMillis()
            
            if (lastSyncTimestamp == 0L) {
                // First sync - fetch all contacts
                val contacts = fetchContactsFromPhone(contentResolver, null)
                contactDao.replaceContacts(contacts.map { it.toEntity() })
            } else {
                // Incremental sync - fetch only changed contacts
                val changedContacts = fetchContactsFromPhone(contentResolver, lastSyncTimestamp)
                
                if (changedContacts.isNotEmpty()) {
                    // Upsert changed contacts
                    contactDao.upsertContacts(changedContacts.map { it.toEntity() })
                }
                
                // Detect deleted contacts by comparing IDs
                val currentPhoneContactIds = fetchAllContactIds(contentResolver)
                val cachedContactIds = contactDao.getAllContactIds()
                val deletedContactIds = cachedContactIds.filterNot { it in currentPhoneContactIds }
                
                if (deletedContactIds.isNotEmpty()) {
                    contactDao.deleteContactsByIds(deletedContactIds)
                }
            }
            
            // Update last sync timestamp
            dataStoreManager.updateData(DataStoreConstants.LAST_CONTACT_SYNC_TIMESTAMP, currentTimestamp)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun forceFullSyncFromPhone(contentResolver: ContentResolver): Result<Unit> {
        return try {
            // Force a full sync by resetting timestamp and fetching all contacts
            val contacts = fetchContactsFromPhone(contentResolver, null)
            contactDao.replaceContacts(contacts.map { it.toEntity() })
            
            // Update last sync timestamp
            val currentTimestamp = System.currentTimeMillis()
            dataStoreManager.updateData(DataStoreConstants.LAST_CONTACT_SYNC_TIMESTAMP, currentTimestamp)
            
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

    /**
     * Fetches contacts from phone. If lastSyncTimestamp is provided, only fetches contacts
     * modified after that timestamp for incremental sync.
     */
    private fun fetchContactsFromPhone(contentResolver: ContentResolver, lastSyncTimestamp: Long?): List<Contact> {
        val contacts = mutableListOf<Contact>()

        val projection = arrayOf(
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.DISPLAY_NAME,
            ContactsContract.Contacts.STARRED,
            ContactsContract.Contacts.CONTACT_LAST_UPDATED_TIMESTAMP
        )

        // Build selection clause for incremental sync
        val selection = if (lastSyncTimestamp != null) {
            "${ContactsContract.Contacts.CONTACT_LAST_UPDATED_TIMESTAMP} > ?"
        } else {
            null
        }
        
        val selectionArgs = if (lastSyncTimestamp != null) {
            arrayOf(lastSyncTimestamp.toString())
        } else {
            null
        }

        contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
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

    /**
     * Fetches only the IDs of all contacts from phone.
     * Used for detecting deleted contacts during incremental sync.
     */
    private fun fetchAllContactIds(contentResolver: ContentResolver): Set<String> {
        val contactIds = mutableSetOf<String>()
        
        contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            arrayOf(ContactsContract.Contacts._ID),
            null,
            null,
            null
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndex(ContactsContract.Contacts._ID)
            
            while (cursor.moveToNext()) {
                val id = cursor.getString(idColumn)
                if (id != null) {
                    contactIds.add(id)
                }
            }
        }
        
        return contactIds
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

