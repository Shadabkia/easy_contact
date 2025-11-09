package ir.co.contact.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ir.co.contact.data.source.local.database.ContactDao
import ir.co.contact.data.source.local.database.ContactDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): ContactDatabase {
        return Room.databaseBuilder(
            context,
            ContactDatabase::class.java,
            ContactDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration() // Remove in production, use proper migrations
            .build()
    }


    @Provides
    fun provideContactDao(
        database: ContactDatabase
    ): ContactDao = database.contactDao()
}

