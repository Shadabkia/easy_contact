package ir.co.contact.domain.repositories

import ir.co.contact.domain.model.Contact
import kotlinx.coroutines.flow.Flow

interface ContactRepository {
    fun getContacts(): Flow<List<Contact>>
    suspend fun syncContactsFromPhone(contentResolver: android.content.ContentResolver): Result<Unit>
    suspend fun hasCachedContacts(): Boolean
    fun observeContactChanges(): Flow<Unit>
}

