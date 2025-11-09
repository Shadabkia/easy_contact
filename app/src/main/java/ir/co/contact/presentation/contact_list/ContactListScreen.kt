package ir.co.contact.presentation.contact_list

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import ir.co.contact.domain.model.Contact
import ir.co.contact.presentation.widget.ScreenLoading
import kotlin.math.absoluteValue


fun getInitials(name: String): String {
    val parts = name.trim().split("\\s+".toRegex())
    return when {
        parts.size >= 2 -> "${parts[0].first().uppercaseChar()}${parts[1].first().uppercaseChar()}"
        parts.isNotEmpty() -> parts[0].take(2).uppercase()
        else -> "??"
    }
}

fun generateColorFromName(name: String): Color {
    val colors = listOf(
        Color(0xFFE57373), Color(0xFFF06292), Color(0xFFBA68C8),
        Color(0xFF9575CD), Color(0xFF7986CB), Color(0xFF64B5F6),
        Color(0xFF4FC3F7), Color(0xFF4DD0E1), Color(0xFF4DB6AC),
        Color(0xFF81C784), Color(0xFFAED581), Color(0xFFFFD54F),
        Color(0xFFFFB74D), Color(0xFF90A4AE)
    )
    return colors[name.hashCode().absoluteValue % colors.size]
}

// Permission dialog composable
@Composable
fun ContactPermissionDialog(
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit,
    showDialog: Boolean
) {
    if (showDialog) {
        Dialog(onDismissRequest = { /* Cannot dismiss - permission is required */ }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Icon
                    Icon(
                        imageVector = Icons.Default.ContactPhone,
                        contentDescription = "Contact Permission",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Title
                    Text(
                        text = "Contact Access Required",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Message
                    Text(
                        text = "EasyContact needs access to your contacts to display and manage them. Without this permission, the app cannot function.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Buttons
                    Button(
                        onClick = onRequestPermission,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = "Grant Permission",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedButton(
                        onClick = onOpenSettings,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Open Settings",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

// Main screen composable
@Composable
fun ContactListScreen(
    contacts: List<Contact>,
    onContactClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
    val surfaceColor = MaterialTheme.colorScheme.surface
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        backgroundColor,
                        secondaryColor
                    )
                )
            )
    ) {
        Column {
            // Header section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp, bottom = 24.dp, start = 16.dp, end = 16.dp)
            ) {
                Text(
                    text = "Contacts",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = onPrimaryColor
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "${contacts.size} contacts",
                    fontSize = 16.sp,
                    color = onPrimaryColor.copy(alpha = 0.8f)
                )
            }

            // Contact list with rounded top corners
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(surfaceColor)
                    .padding(top = 16.dp)
            ) {
                items(
                    items = contacts,
                    key = { it.id }
                ) { contact ->
                    ContactItem(
                        contact = contact,
                        onClick = { onContactClick(contact.id) }
                    )
                }
            }
        }
    }
}

// Permission wrapper
@Composable
fun ContactScreenWithPermission(
    onContactClick: (String) -> Unit,
    viewModel: ContactListViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val contacts by viewModel.contacts
    val hasPermission by viewModel.hasPermission
    val isLoading by viewModel.isLoading
    val lifecycleOwner = LocalLifecycleOwner.current
    
    var showPermissionDialog by remember { mutableStateOf(false) }
    var permissionDeniedCount by remember { mutableStateOf(0) }
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
                ContactListScreen(
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

    ContactListScreen(
        contacts = mockContacts,
        onContactClick = {}
    )
}