package ir.co.contact.presentation.contacts.contact_list

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import ir.co.contact.domain.model.Contact
import ir.co.contact.presentation.widget.ScreenLoading

@Composable
fun ContactScreenWithPermission(
    onContactClick: (String) -> Unit,
    viewModel: ContactListViewModel
) {
    val context = LocalContext.current
    val contacts by viewModel.contacts
    val hasPermission by viewModel.hasPermission
    val isLoading by viewModel.isLoading
    val lifecycleOwner = LocalLifecycleOwner.current

    var showPermissionDialog by remember { mutableStateOf(false) }
    var permissionDeniedCount by remember { mutableIntStateOf(0) }
    var isFirstResume by remember { mutableStateOf(true) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            viewModel.checkPermission(context)
            if (granted) {
                showPermissionDialog = false
                viewModel.loadContacts()
            } else {
                permissionDeniedCount++
                showPermissionDialog = true
            }
        }
    )

    // Observe toast messages
    LaunchedEffect(Unit) {
        viewModel.toastMessage.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    // Observe lifecycle events for app resume
    LaunchedEffect(lifecycleOwner, hasPermission) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    if (!isFirstResume && hasPermission) {
                        // App resumed - sync contacts in background
                        viewModel.syncOnAppResume()
                    }
                    isFirstResume = false
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        // Cleanup observer when effect is cancelled
        kotlinx.coroutines.coroutineScope {
            try {
                kotlinx.coroutines.awaitCancellation()
            } finally {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.checkPermission(context)
        if (hasPermission) {
            viewModel.loadContacts()
        } else {
            showPermissionDialog = true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            isLoading -> {
                ScreenLoading(Modifier)
            }
            hasPermission -> {
                ContactList(
                    contacts = contacts,
                    onContactClick = onContactClick
                )
            }
            else -> {
                // Show an empty state while permission dialog is shown
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.secondary
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContactPhone,
                            contentDescription = "Contacts",
                            modifier = Modifier.size(100.dp),
                            tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Welcome to EasyContact",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Manage your contacts easily",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Show permission dialog when needed
        ContactPermissionDialog(
            showDialog = showPermissionDialog && !hasPermission,
            onRequestPermission = {
                permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
            },
            onOpenSettings = {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            }
        )
    }
}

// Preview with mock data
@Preview(showBackground = true)
@Composable
fun ContactListScreenPreview() {
    val mockContacts = listOf(
        Contact("1", "Hr kia", "+98 930 262 7437"),
        Contact("2", "Hr kia", "+98 930 262 7437"),
        Contact("3", "Hr kia", "+98 930 262 7437"),
        Contact("4", "Hr kia", "+98 930 262 7437"),
        Contact("5", "Hr kia", "+98 930 262 7437"),
        Contact("6", "Hr kiaHr kia", "+98 930 262 7437"),
        Contact("7", "Hr kia", "+98 930 262 7437", isFavorite = true),
        Contact("8", "Hr kia", "+98 930 262 7437")
    )

    ContactList(
        contacts = mockContacts,
        onContactClick = {}
    )
}