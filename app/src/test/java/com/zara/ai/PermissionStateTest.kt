package com.zara.ai

import com.zara.ai.core.permissions.model.PermissionCategory
import com.zara.ai.core.permissions.model.ZaraPermission
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PermissionStateTest {

    @Test
    fun `all permissions have non-empty title and description`() {
        ZaraPermission.entries.forEach { perm ->
            assertTrue(perm.title.isNotBlank())
            assertTrue(perm.description.isNotBlank())
            assertNotNull(perm.category)
        }
    }

    @Test
    fun `special access permissions are tagged appropriately`() {
        val overlay = ZaraPermission.SYSTEM_ALERT_WINDOW
        assertTrue(overlay.isSpecialAccess)
        assertEquals(PermissionCategory.SYSTEM_OVERLAY, overlay.category)

        val accessibility = ZaraPermission.ACCESSIBILITY_SERVICE
        assertTrue(accessibility.isSpecialAccess)

        val audio = ZaraPermission.RECORD_AUDIO
        assertEquals(PermissionCategory.CORE, audio.category)
    }
}
