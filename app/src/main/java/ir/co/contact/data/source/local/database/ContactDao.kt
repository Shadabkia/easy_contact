package ir.co.contact.data.source.local.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {

    @Query("SELECT * FROM contacts ORDER BY name COLLATE NOCASE ASC")
    fun getContacts(): Flow<List<ContactEntity>>

    @Query("SELECT COUNT(*) FROM contacts")
    suspend fun getContactsCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContacts(contacts: List<ContactEntity>)

    @Query("DELETE FROM contacts")
    suspend fun clearContacts()

    @Transaction
    suspend fun replaceContacts(contacts: List<ContactEntity>) {
        clearContacts()
        if (contacts.isNotEmpty()) {
            insertContacts(contacts)
        }
    }
}

