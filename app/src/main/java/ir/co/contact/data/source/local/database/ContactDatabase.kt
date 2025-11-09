package ir.co.contact.data.source.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [ContactEntity::class],
    version = 2,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class ContactDatabase : RoomDatabase() {

    abstract fun contactDao(): ContactDao

    companion object {
        const val DATABASE_NAME = "contacts.db"
    }
}

