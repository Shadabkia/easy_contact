package ir.co.contact.presentation.main.navigation

import kotlinx.serialization.Serializable


sealed interface NavigationRoutes {
    @Serializable
    data object ContactListScreen : NavigationRoutes

    @Serializable
    data class ContactDetailScreen(val contactId: String) : NavigationRoutes

}