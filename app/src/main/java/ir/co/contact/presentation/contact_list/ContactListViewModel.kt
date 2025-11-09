package ir.co.contact.presentation.contact_list

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.co.contact.domain.model.Contact
import ir.co.contact.domain.usecases.GetContactsUseCase
import ir.co.contact.domain.usecases.ObserveContactChangesUseCase
import ir.co.contact.domain.usecases.SyncContactsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ContactListViewModel @Inject constructor(
    private val getContactsUseCase: GetContactsUseCase,
    private val syncContactsUseCase: SyncContactsUseCase,
    private val observeContactChangesUseCase: ObserveContactChangesUseCase
) : ViewModel() {

    // UI State
    private val _contacts = mutableStateOf<List<Contact>>(emptyList())
    val contacts: State<List<Contact>> = _contacts

    private val _hasPermission = mutableStateOf(false)
    val hasPermission: State<Boolean> = _hasPermission

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    // SharedFlow for one-time events (like toast messages)
    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage: SharedFlow<String> = _toastMessage.asSharedFlow()

    private var contextRef: Context? = null
    private var isObservingChanges = false

    init {
        observeContacts()
    }

    /**
     * Check if READ_CONTACTS permission is granted
     */
    fun checkPermission(context: Context) {
        _hasPermission.value = ContextCompat.checkSelfPermission(
            context, Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Initial load: Shows loading and syncs contacts from phone.
     * Always syncs to ensure fresh data on first app open.
     */
    fun loadContacts(context: Context) {
        if (!hasPermission.value) return

        contextRef = context

        viewModelScope.launch {
            _isLoading.value = true
            try {
                _toastMessage.emit("Syncing contacts...")

                val result = withContext(Dispatchers.IO) {
                    syncContactsUseCase(context.contentResolver)
                }

                result.onSuccess {
                    _toastMessage.emit("Contacts synced successfully")
                    // Start observing contact changes after successful initial load
                    startObservingContactChanges()
                }.onFailure { error ->
                    error.printStackTrace()
                    _toastMessage.emit("Failed to sync contacts")
                }

                _isLoading.value = false
            } catch (e: Exception) {
                e.printStackTrace()
                _isLoading.value = false
                _toastMessage.emit("Error loading contacts")
            }
        }
    }

    /**
     * Sync contacts when app resumes from background.
     * Runs in background without loading indicator.
     * Ensures contacts are always fresh when user returns to app.
     */
    fun syncOnAppResume(context: Context) {
        if (!hasPermission.value) return

        contextRef = context

        viewModelScope.launch {
            try {
                // Sync silently in background
                val result = withContext(Dispatchers.IO) {
                    syncContactsUseCase(context.contentResolver)
                }

                result.onSuccess {
                    _toastMessage.emit("Contacts updated")
                }.onFailure { error ->
                    // Silent failure - log only, don't disturb user
                    error.printStackTrace()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Start observing device contact changes for live sync.
     * Only starts once to avoid duplicate observers.
     */
    private fun startObservingContactChanges() {
        if (!hasPermission.value || contextRef == null || isObservingChanges) return

        isObservingChanges = true
        
        viewModelScope.launch {
            observeContactChangesUseCase()
                .collectLatest {
                    // Contact change detected, sync automatically
                    _toastMessage.emit("Syncing contacts...")
                    
                    val result = withContext(Dispatchers.IO) {
                        contextRef?.let { ctx ->
                            syncContactsUseCase(ctx.contentResolver)
                        }
                    }

                    result?.onSuccess {
                        _toastMessage.emit("Contacts synced")
                    }?.onFailure {
                        _toastMessage.emit("Sync failed")
                    }
                }
        }
    }

    /**
     * Observe contacts from database using Flow.
     * Updates UI state whenever database changes.
     * This is the single source of truth for contact data.
     */
    private fun observeContacts() {
        viewModelScope.launch {
            getContactsUseCase()
                .collectLatest { contactList ->
                    _contacts.value = contactList
                    
                    // Auto-hide loading when contacts are loaded
                    if (_isLoading.value && contactList.isNotEmpty()) {
                        _isLoading.value = false
                    }
                }
        }
    }
}