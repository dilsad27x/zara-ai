package com.zara.ai.features.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zara.ai.core.designsystem.theme.ZaraBackgroundPrimary
import com.zara.ai.core.designsystem.theme.ZaraBorder
import com.zara.ai.core.designsystem.theme.ZaraElectricBlue
import com.zara.ai.core.designsystem.theme.ZaraPrimaryAccent
import com.zara.ai.core.designsystem.theme.ZaraSurface
import com.zara.ai.core.designsystem.theme.ZaraTextSecondary

@Composable
fun ZaraBottomBar(
    currentRoute: String?,
    onNavigateToDestination: (ZaraDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    if (currentRoute == ZaraDestination.Splash.route) return

    NavigationBar(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
            .border(width = 1.dp, color = ZaraBorder.copy(alpha = 0.6f)),
        containerColor = ZaraSurface,
        tonalElevation = 8.dp
    ) {
        ZaraDestination.bottomBarItems.forEach { destination ->
            val isSelected = currentRoute == destination.route
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigateToDestination(destination) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                        contentDescription = destination.title
                    )
                },
                label = {
                    Text(
                        text = destination.title,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = ZaraPrimaryAccent,
                    selectedTextColor = ZaraPrimaryAccent,
                    indicatorColor = ZaraPrimaryAccent.copy(alpha = 0.18f),
                    unselectedIconColor = ZaraTextSecondary,
                    unselectedTextColor = ZaraTextSecondary
                )
            )
        }
    }
}
