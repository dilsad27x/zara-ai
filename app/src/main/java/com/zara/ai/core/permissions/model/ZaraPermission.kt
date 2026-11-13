package com.zara.ai.core.permissions.model

import android.Manifest
import android.os.Build

enum class ZaraPermission(
    val manifestPermission: String,
    val title: String,
    val rationale: String,
    val minSdk: Int = Build.VERSION_CODES.BASE
) {
    RECORD_AUDIO(
        manifestPermission = Manifest.permission.RECORD_AUDIO,
        title = "Voice Input & Audio Processing",
        rationale = "Allows ZARA AI to hear your voice commands, conduct real-time voice conversations, and interpret audio input."
    ),
    POST_NOTIFICATIONS(
        manifestPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.POST_NOTIFICATIONS
        } else {
            ""
        },
        title = "Agent Task Notifications",
        rationale = "Enables ZARA AI to notify you when autonomous background agent tasks, web builds, or long-running routines complete.",
        minSdk = Build.VERSION_CODES.TIRAMISU
    )
}
