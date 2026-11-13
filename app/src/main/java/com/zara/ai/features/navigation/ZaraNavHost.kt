package com.zara.ai.features.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.zara.ai.core.common.error.ErrorHandler
import com.zara.ai.features.chat.ChatScreen
import com.zara.ai.features.dashboard.DashboardScreen
import com.zara.ai.features.devicecontrol.DeviceControlScreen
import com.zara.ai.features.home.HomeScreen
import com.zara.ai.features.memory.MemoryScreen
import com.zara.ai.features.settings.SettingsScreen
import com.zara.ai.features.social.SocialScreen
import com.zara.ai.features.voice.VoiceScreen
import com.zara.ai.features.websiteforge.WebsiteForgeScreen

@Composable
fun ZaraNavHost(
    errorHandler: ErrorHandler,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        errorHandler.errorEvents.collect { event ->
            snackbarHostState.showSnackbar(
                message = event.userFriendlyMessage,
                actionLabel = event.actionLabel
            )
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            ZaraBottomBar(
                currentRoute = currentRoute,
                onNavigateToDestination = { destination ->
                    navController.navigate(destination.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = ZaraDestination.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(ZaraDestination.Home.route) {
                HomeScreen(
                    onNavigateToDestination = { destination ->
                        navController.navigate(destination.route)
                    }
                )
            }
            composable(ZaraDestination.Chat.route) {
                ChatScreen(
                    onNavigateToVoice = {
                        navController.navigate(ZaraDestination.Voice.route)
                    }
                )
            }
            composable(ZaraDestination.Voice.route) {
                VoiceScreen()
            }
            composable(ZaraDestination.Memory.route) {
                MemoryScreen()
            }
            composable(ZaraDestination.Dashboard.route) {
                DashboardScreen()
            }
            composable(ZaraDestination.Settings.route) {
                SettingsScreen()
            }
            composable(ZaraDestination.DeviceControl.route) {
                DeviceControlScreen()
            }
            composable(ZaraDestination.Social.route) {
                SocialScreen()
            }
            composable(ZaraDestination.WebsiteForge.route) {
                WebsiteForgeScreen()
            }
        }
    }
}
