package ir.co.contact.presentation.contact_list

import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.provider.ContactsContract
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.co.contact.data.source.local.database.ContactDao
import ir.co.contact.data.source.local.database.toDomain
import ir.co.contact.data.source.local.database.toEntity
import ir.co.contact.domain.model.Contact
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// 3. ViewModel
@HiltViewModel
class ContactListViewModel @Inject constructor(
    private val contactDao: ContactDao
) : ViewModel() {
    private val _contacts = mutableStateOf<List<Contact>>(emptyList())
    val contacts: State<List<Contact>> = _contacts

    private val _hasPermission = mutableStateOf(false)
    val hasPermission: State<Boolean> = _hasPermission

    // Add loading state
    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    // StateFlow for reactive updates
    private val _contactsFlow = MutableStateFlow<List<Contact>>(emptyList())
    val contactsFlow: StateFlow<List<Contact>> = _contactsFlow.asStateFlow()

    init {
        observeLocalContacts()
    }

    fun checkPermission(context: Context) {
        _hasPermission.value = ContextCompat.checkSelfPermission(
            context, Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun loadContacts(context: Context, forceRefresh: Boolean = false) {
        if (!hasPermission.value) return

        viewModelScope.launch {
            _isLoading.value = true  // Show loading
            try {
                val hasCachedContacts = withContext(Dispatchers.IO) {
                    contactDao.getContactsCount() > 0
                }

                if (!hasCachedContacts || forceRefresh) {
                    // Use Dispatchers.IO for database operations to prevent ANR
                    val contactList = withContext(Dispatchers.IO) {
                        fetchContactsFromPhone(context.contentResolver)
                    }
                    withContext(Dispatchers.IO) {
                        contactDao.replaceContacts(contactList.map { it.toEntity() })
                    }
                }
                _isLoading.value = false
            } catch (e: Exception) {
                // Handle any errors during contact loading
                e.printStackTrace()
                _isLoading.value = false  // Hide loading on error
            }
        }
    }

    private fun observeLocalContacts() {
        viewModelScope.launch {
            contactDao.getContacts()
                .map { entities -> entities.map { it.toDomain() } }
                .collectLatest { contactList ->
                    _contacts.value = contactList
                    _contactsFlow.value = contactList
                    if (_isLoading.value && contactList.isNotEmpty()) {
                        _isLoading.value = false
                    }
                }
        }
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