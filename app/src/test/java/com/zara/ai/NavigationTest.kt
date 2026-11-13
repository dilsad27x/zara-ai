package com.zara.ai

import com.zara.ai.features.navigation.ZaraDestination
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NavigationTest {

    @Test
    fun `destinations contain all 10 core screens`() {
        val routes = listOf(
            ZaraDestination.Splash.route,
            ZaraDestination.Home.route,
            ZaraDestination.Chat.route,
            ZaraDestination.Voice.route,
            ZaraDestination.Tasks.route,
            ZaraDestination.Memory.route,
            ZaraDestination.Permissions.route,
            ZaraDestination.Settings.route,
            ZaraDestination.DeviceControl.route,
            ZaraDestination.Social.route,
            ZaraDestination.WebsiteForge.route
        )
        assertEquals(11, routes.distinct().size)
    }

    @Test
    fun `bottom bar items exclude splash and contain primary tabs`() {
        val items = ZaraDestination.bottomBarItems
        assertTrue(items.contains(ZaraDestination.Home))
        assertTrue(items.contains(ZaraDestination.Chat))
        assertTrue(items.contains(ZaraDestination.Voice))
        assertTrue(items.contains(ZaraDestination.Tasks))
        assertTrue(items.contains(ZaraDestination.Settings))
        assertTrue(!items.contains(ZaraDestination.Splash))
    }
}
