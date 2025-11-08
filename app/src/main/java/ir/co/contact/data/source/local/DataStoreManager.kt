package ir.co.contact.data.source.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import ir.co.contact.data.source.local.DataStoreConstants.ACCESS_TOKEN
import ir.co.contact.data.source.local.DataStoreConstants.DATASTORE_NAME
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

// At the top level of your kotlin file:
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = DATASTORE_NAME
)

class DataStoreManager @Inject constructor(@ApplicationContext val context: Context) {

    fun <T> getData(key: Preferences.Key<T>): Flow<T?> = context.dataStore.data.map { preferences ->
        preferences[key]
    }

    suspend fun <T> updateData(key: Preferences.Key<T>, value: T) =
        context.dataStore.edit { preferences ->
            preferences[key] = value
        }

    suspend fun clearDataStore() =
        context.dataStore.edit {
            it.clear()
        }

    suspend fun clearUnusedData() {
        val keys = arrayListOf(ACCESS_TOKEN)
        for (key in keys) {
            context.dataStore.edit {
                it.remove(key)
            }
        }
    }
}

object DataStoreConstants {
    const val DATASTORE_NAME = "default_datastore"
    val ACCESS_TOKEN = stringPreferencesKey(name = "access_token")
}