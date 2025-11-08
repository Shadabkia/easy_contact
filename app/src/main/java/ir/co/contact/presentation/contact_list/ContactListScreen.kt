package ir.co.contact.presentation.contact_list

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
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



// Main screen composable
@Composable
fun ContactListScreen(
    contacts: List<Contact>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF7C4DFF),
                        Color(0xFFFF4081)
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
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "${contacts.size} contacts",
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }

            // Contact list with rounded top corners
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(Color.White)
                    .padding(top = 16.dp)
            ) {
                items(
                    items = contacts,
                    key = { it.id }
                ) { contact ->
                    ContactItem(contact = contact)
                }
            }
        }
    }
}

// Permission wrapper
@Composable
fun ContactScreenWithPermission(
    viewModel: ContactListViewModel = viewModel()
) {
    val context = LocalContext.current
    val contacts by viewModel.contacts
    val hasPermission by viewModel.hasPermission
    val isLoading by viewModel.isLoading

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            viewModel.checkPermission(context)
            if (granted) {
                viewModel.loadContacts(context)
            }
        }
    )

    LaunchedEffect(Unit) {
        viewModel.checkPermission(context)
        if (hasPermission) {
            viewModel.loadContacts(context)
        } else {
            permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }
    }

    when {
        !hasPermission -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Contacts permission required",
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
        isLoading -> ScreenLoading(Modifier)
        else -> ContactListScreen(contacts = contacts)
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

    ContactListScreen(contacts = mockContacts)
}