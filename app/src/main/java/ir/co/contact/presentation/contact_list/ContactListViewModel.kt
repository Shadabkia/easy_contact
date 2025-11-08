package ir.co.contact.presentation.contact_list

import android.Manifest
import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.provider.ContactsContract
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ir.co.contact.domain.model.Contact
import kotlinx.coroutines.launch

// 3. ViewModel
class ContactListViewModel(application: Application) : AndroidViewModel(application) {
    private val _contacts = mutableStateOf<List<Contact>>(emptyList())
    val contacts: State<List<Contact>> = _contacts

    private val _hasPermission = mutableStateOf(false)
    val hasPermission: State<Boolean> = _hasPermission

    // Add loading state
    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    fun checkPermission(context: Context) {
        _hasPermission.value = ContextCompat.checkSelfPermission(
            context, Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun loadContacts(context: Context) {
        if (!hasPermission.value) return

        viewModelScope.launch {
            _isLoading.value = true  // Show loading
            try {
                val contactList = fetchContactsFromPhone(context.contentResolver)
                _contacts.value = contactList
            } finally {
                _isLoading.value = false  // Hide loading
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