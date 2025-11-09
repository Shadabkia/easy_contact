package ir.co.contact.data.source.local

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.provider.ContactsContract
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Observes changes to the device's contact database and emits events when contacts are modified.
 * This enables live synchronization of contacts between the phone and the app.
 */
class ContactObserver(private val context: Context) {

    /**
     * Returns a Flow that emits whenever contacts change on the device.
     * The flow emits Unit values as a signal that contacts have changed.
     */
    fun observeContactChanges(): Flow<Unit> = callbackFlow {
        val contentObserver = object : ContentObserver(null) {
            override fun onChange(selfChange: Boolean) {
                super.onChange(selfChange)
                // Emit signal that contacts have changed
                trySend(Unit)
            }

            override fun onChange(selfChange: Boolean, uri: Uri?) {
                super.onChange(selfChange, uri)
                // Emit signal that contacts have changed
                trySend(Unit)
            }
        }

        // Register observer for all contact changes
        context.contentResolver.registerContentObserver(
            ContactsContract.Contacts.CONTENT_URI,
            true,
            contentObserver
        )

        // Clean up when flow is cancelled
        awaitClose {
            context.contentResolver.unregisterContentObserver(contentObserver)
        }
    }
}

