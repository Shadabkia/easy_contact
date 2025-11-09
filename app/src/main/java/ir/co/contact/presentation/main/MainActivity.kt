package ir.co.contact.presentation.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import dagger.hilt.android.AndroidEntryPoint
import ir.co.contact.presentation.contacts.contact_details.ContactDetailScreen
import ir.co.contact.presentation.contacts.contact_details.MissingContactState
import ir.co.contact.presentation.contacts.contact_list.ContactListViewModel
import ir.co.contact.presentation.contacts.contact_list.ContactScreenWithPermission
import ir.co.contact.presentation.main.navigation.NavigationRoutes
import ir.co.contact.presentation.theme.ContactTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ContactTheme {
                val snackBarHostState = remember { SnackbarHostState() }
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                val contactListViewModel = hiltViewModel<ContactListViewModel>()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = {
                        SnackbarHost(
                            modifier = Modifier
                                .fillMaxWidth(),
                            hostState = snackBarHostState
                        )
                    }
                ) { innerPadding ->
                    val paddingModifier = Modifier.padding(innerPadding)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding()
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = NavigationRoutes.ContactListScreen
                        ) {
                            composable<NavigationRoutes.ContactListScreen> {
                                ContactScreenWithPermission(
                                    onContactClick = { contactId ->
                                        navController.navigate(NavigationRoutes.ContactDetailScreen(contactId))
                                    },
                                    contactListViewModel
                                )
                            }
                            
                            composable<NavigationRoutes.ContactDetailScreen> { backStackEntry ->
                                val route = backStackEntry.toRoute<NavigationRoutes.ContactDetailScreen>()
                                val contactFlow = remember(route.contactId) {
                                    contactListViewModel.getContactById(route.contactId)
                                }
                                val contact by contactFlow.collectAsStateWithLifecycle(initialValue = null)

                                if (contact != null) {
                                    ContactDetailScreen(
                                        contact = contact!!,
                                        onBackClick = { navController.popBackStack() },
                                        snackBarHostState = snackBarHostState
                                    )
                                } else {
                                    MissingContactState(onBackClick = { navController.popBackStack() })
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}