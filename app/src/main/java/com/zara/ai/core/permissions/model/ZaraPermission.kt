package com.zara.ai.core.permissions.model

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings

enum class PermissionCategory {
    CORE, ADVANCED, SYSTEM_OVERLAY
}

enum class ZaraPermission(
    val title: String,
    val description: String,
    val category: PermissionCategory,
    val isSpecialAccess: Boolean = false,
    val manifestPermission: String = ""
) {
    RECORD_AUDIO(
        title = "Voice Input & Acoustics",
        description = "Required for real-time natural language speech, voice wake-word triggers, and acoustic telemetry.",
        category = PermissionCategory.CORE,
        manifestPermission = Manifest.permission.RECORD_AUDIO
    ),
    POST_NOTIFICATIONS(
        title = "Autonomous Notifications",
        description = "Enables ZARA to deliver critical status alerts when long-running agent tasks or website builds finish in the background.",
        category = PermissionCategory.CORE,
        manifestPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.POST_NOTIFICATIONS
        } else ""
    ),
    SYSTEM_ALERT_WINDOW(
        title = "Floating Assistant Overlay",
        description = "Allows the ZARA Mini Orb and floating conversation window to render over other apps for instant global access.",
        category = PermissionCategory.SYSTEM_OVERLAY,
        isSpecialAccess = true
    ),
    ACCESSIBILITY_SERVICE(
        title = "Android Accessibility Service",
        description = "Empowers autonomous UI navigation, automated taps, and screen control routines on your explicit instruction.",
        category = PermissionCategory.ADVANCED,
        isSpecialAccess = true
    ),
    NOTIFICATION_LISTENER(
        title = "Notification Interceptor Access",
        description = "Enables ZARA to read, prioritize, summarize, and auto-reply to incoming messages and platform alerts.",
        category = PermissionCategory.ADVANCED,
        isSpecialAccess = true
    );

    fun createSettingsIntent(context: Context): Intent {
        return when (this) {
            SYSTEM_ALERT_WINDOW -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:${context.packageName}")
                    )
                } else {
                    Intent(Settings.ACTION_SETTINGS)
                }
            }
            NOTIFICATION_LISTENER -> {
                Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
            }
            ACCESSIBILITY_SERVICE -> {
                Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            }
            else -> {
                Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:${context.packageName}")
                )
            }
        }
    }
}
