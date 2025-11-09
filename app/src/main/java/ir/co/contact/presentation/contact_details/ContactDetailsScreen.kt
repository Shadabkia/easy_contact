package ir.co.contact.presentation.contact_details

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.co.contact.domain.model.*
import ir.co.contact.presentation.contact_list.generateColorFromName
import ir.co.contact.presentation.theme.*

// Helper functions
fun getInitials(name: String): String {
    val parts = name.trim().split("\\s+".toRegex())
    return when {
        parts.size >= 2 -> "${parts[0].first().uppercaseChar()}${parts[1].first().uppercaseChar()}"
        parts.isNotEmpty() -> parts[0].take(2).uppercase()
        else -> "??"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactDetailScreen(
    contact: Contact,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDarkTheme = isSystemInDarkTheme()
    val gradientColors = if (isDarkTheme) {
        listOf(DeepNavy1, ElectricBlue)
    } else {
        listOf(DeepNavy1, ElectricBlue)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            ActionButtons(
                onEditClick = onEditClick,
                onDeleteClick = onDeleteClick
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(colors = gradientColors)
                )
                .padding(padding)
        ) {
            item { DetailHeader(contact = contact) }

            contact.phoneNumbers.takeIf { it.isNotEmpty() }?.let {
                item {
                    DetailSection(
                        title = "Phone",
                        items = it.map { phone ->
                            DetailItem(
                                icon = when (phone.type) {
                                    PhoneType.HOME -> Icons.Default.Phone
                                    PhoneType.MOBILE -> Icons.Default.Smartphone
                                    PhoneType.WORK -> Icons.Default.Business
                                    PhoneType.OTHER -> Icons.Default.Phone
                                },
                                text = phone.number,
                                label = phone.type.name.lowercase().replaceFirstChar { it.uppercase() }
                            )
                        }
                    )
                }
            }

            contact.emails.takeIf { it.isNotEmpty() }?.let {
                item {
                    DetailSection(
                        title = "Email",
                        items = it.map { email ->
                            DetailItem(
                                icon = Icons.Default.Email,
                                text = email.address,
                                label = email.type.name.lowercase().replaceFirstChar { it.uppercase() }
                            )
                        }
                    )
                }
            }

            contact.addresses.takeIf { it.isNotEmpty() }?.let {
                item {
                    DetailSection(
                        title = "Address",
                        items = it.map { addr ->
                            DetailItem(
                                icon = Icons.Default.Home,
                                text = addr.address,
                                label = addr.type.name.lowercase().replaceFirstChar { it.uppercase() }
                            )
                        }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun DetailHeader(contact: Contact) {
    val isDarkTheme = isSystemInDarkTheme()
    val subTextColor = if (isDarkTheme) TextSecondaryDark else TextSecondary

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(120.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(generateColorFromName(contact.name)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = getInitials(contact.name),
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = contact.name,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
            if (contact.isFavorite) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Favorite",
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun DetailSection(
    title: String,
    items: List<DetailItem>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        items.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = item.text,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    item.label?.let {
                        Text(
                            text = it,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

data class DetailItem(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val text: String,
    val label: String? = null
)

@Composable
fun ActionButtons(
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Button(
            onClick = onEditClick,
            modifier = Modifier.weight(2f),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Edit", fontWeight = FontWeight.Bold)
        }

        Button(
            onClick = onDeleteClick,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ErrorRed
            )
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Delete", fontWeight = FontWeight.Bold)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ContactDetailScreenLightPreview() {
    ContactTheme(darkTheme = false) {
        ContactDetailScreen(
            contact = createMockContact(),
            onBackClick = {},
            onEditClick = {},
            onDeleteClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ContactDetailScreenDarkPreview() {
    ContactTheme(darkTheme = true) {
        ContactDetailScreen(
            contact = createMockContact(),
            onBackClick = {},
            onEditClick = {},
            onDeleteClick = {}
        )
    }
}

private fun createMockContact() = Contact(
    id = "1",
    name = "Alice Johnson",
    phoneNumber = "+1 (555) 123-4567", // Primary phone
    isFavorite = true,
    phoneNumbers = listOf(
        PhoneNumber("+1 (555) 123-4567", PhoneType.HOME),
        PhoneNumber("+1 (555) 765-4321", PhoneType.MOBILE),
        PhoneNumber("+1 (555) 555-0000", PhoneType.OTHER)
    ),
    emails = listOf(
        Email("alicejohnson@email.com", EmailType.PERSONAL),
        Email("alice.johnson@techsolutions.com", EmailType.WORK)
    ),
    addresses = listOf(
        Address("123 Main St, New York, NY 10001", AddressType.HOME)
    )
)