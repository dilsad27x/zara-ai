package com.zara.ai.features.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DevicesOther
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Web
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.DevicesOther
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MicNone
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Web
import androidx.compose.ui.graphics.vector.ImageVector

sealed class ZaraDestination(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    data object Home : ZaraDestination(
        route = "home",
        title = "Home",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    )

    data object Chat : ZaraDestination(
        route = "chat",
        title = "Chat",
        selectedIcon = Icons.Filled.ChatBubble,
        unselectedIcon = Icons.Outlined.ChatBubbleOutline
    )

    data object Voice : ZaraDestination(
        route = "voice",
        title = "Voice",
        selectedIcon = Icons.Filled.Mic,
        unselectedIcon = Icons.Outlined.MicNone
    )

    data object Memory : ZaraDestination(
        route = "memory",
        title = "Memory",
        selectedIcon = Icons.Filled.Psychology,
        unselectedIcon = Icons.Outlined.Psychology
    )

    data object Dashboard : ZaraDestination(
        route = "dashboard",
        title = "Dashboard",
        selectedIcon = Icons.Filled.Dashboard,
        unselectedIcon = Icons.Outlined.Dashboard
    )

    data object Settings : ZaraDestination(
        route = "settings",
        title = "Settings",
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings
    )

    data object DeviceControl : ZaraDestination(
        route = "device_control",
        title = "Device Control",
        selectedIcon = Icons.Filled.DevicesOther,
        unselectedIcon = Icons.Outlined.DevicesOther
    )

    data object Social : ZaraDestination(
        route = "social",
        title = "Social",
        selectedIcon = Icons.Filled.Share,
        unselectedIcon = Icons.Outlined.Share
    )

    data object WebsiteForge : ZaraDestination(
        route = "website_forge",
        title = "Website Forge",
        selectedIcon = Icons.Filled.Web,
        unselectedIcon = Icons.Outlined.Web
    )

    companion object {
        val bottomBarItems = listOf(Home, Chat, Voice, Dashboard, Settings)
    }
}
