package ir.co.contact.presentation.contacts.contact_list

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.co.contact.data.source.local.DataStoreConstants
import ir.co.contact.data.source.local.DataStoreManager
import ir.co.contact.domain.model.Contact
import ir.co.contact.domain.usecases.GetContactByIdUseCase
import ir.co.contact.domain.usecases.GetContactsUseCase
import ir.co.contact.domain.usecases.ObserveContactChangesUseCase
import ir.co.contact.domain.usecases.SyncContactsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ContactListViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val getContactsUseCase: GetContactsUseCase,
    private val getContactByIdUseCase: GetContactByIdUseCase,
    private val syncContactsUseCase: SyncContactsUseCase,
    private val observeContactChangesUseCase: ObserveContactChangesUseCase,
    private val dataStoreManager: DataStoreManager
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

    private var isObservingChanges = false
    private var hasLoadedInitially = false
    private var isSyncInProgress = false

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

    fun loadContacts(forceRefresh: Boolean = false) {
        if (!hasPermission.value) return

        // Prevent multiple simultaneous syncs
        if (isSyncInProgress) return
        
        if (!forceRefresh && hasLoadedInitially) {
            // Already synced in this session; keep existing list to avoid redundant loading
            return
        }

        viewModelScope.launch {
            // Check if this is the first sync ever by looking at DataStore timestamp
            val lastSyncTimestamp = dataStoreManager.getData(DataStoreConstants.LAST_CONTACT_SYNC_TIMESTAMP).first() ?: 0L
            
            if (lastSyncTimestamp == 0L) {
                // First sync ever - show loading indicator
                performInitialSync()
            } else {
                // Already synced before - do silent background sync
                performSilentSync()
            }
            
            hasLoadedInitially = true
            startObservingContactChanges()
        }
    }
    
    /**
     * Performs the first-time sync with loading indicator
     */
    private suspend fun performInitialSync() {
        isSyncInProgress = true
        _isLoading.value = true
        
        try {
            _toastMessage.emit("Syncing contacts...")

            val result = withContext(Dispatchers.IO) {
                syncContactsUseCase(appContext.contentResolver)
            }

            result.onSuccess {
                _toastMessage.emit("Contacts synced successfully")
            }.onFailure { error ->
                error.printStackTrace()
                _toastMessage.emit("Failed to sync contacts")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _toastMessage.emit("Error loading contacts")
        } finally {
            _isLoading.value = false
            isSyncInProgress = false
        }
    }
    
    /**
     * Performs silent background sync (for subsequent app launches)
     */
    private suspend fun performSilentSync() {
        isSyncInProgress = true
        
        try {
            val result = withContext(Dispatchers.IO) {
                syncContactsUseCase(appContext.contentResolver)
            }

            result.onSuccess {
                // Silent success - no toast needed for quick incremental sync
            }.onFailure { error ->
                error.printStackTrace()
                // Only show error if sync fails
                _toastMessage.emit("Failed to sync contacts")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isSyncInProgress = false
        }
    }

    /**
     * Sync contacts when app resumes from background.
     * Runs in background without loading indicator.
     * Uses incremental sync to only fetch changed/deleted contacts for efficiency.
     */
    fun syncOnAppResume() {
        if (!hasPermission.value) return
        
        // Prevent sync if one is already in progress
        if (isSyncInProgress) return

        isSyncInProgress = true
        viewModelScope.launch {
            try {
                // Sync silently in background
                val result = withContext(Dispatchers.IO) {
                    syncContactsUseCase(appContext.contentResolver)
                }

                result.onSuccess {
                    _toastMessage.emit("Contacts updated")
                }.onFailure { error ->
                    // Silent failure - log only, don't disturb user
                    error.printStackTrace()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isSyncInProgress = false
            }
        }
    }

    /**
     * Start observing device contact changes for live sync.
     * Only starts once to avoid duplicate observers.
     */
    private fun startObservingContactChanges() {
        if (!hasPermission.value || isObservingChanges) return

        isObservingChanges = true
        
        viewModelScope.launch {
            observeContactChangesUseCase()
                .collectLatest {
                    // Contact change detected, sync automatically
                    _toastMessage.emit("Syncing contacts...")
                    
                    val result = withContext(Dispatchers.IO) {
                        syncContactsUseCase(appContext.contentResolver)
                    }

                    result.onSuccess {
                        _toastMessage.emit("Contacts synced")
                    }.onFailure {
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

    fun getContactById(contactId: String): Flow<Contact?> {
        return getContactByIdUseCase(contactId)
    }
}