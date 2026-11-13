package com.zara.ai.core.permissions

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.zara.ai.core.permissions.model.ZaraPermission
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PermissionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun isPermissionGranted(permission: ZaraPermission): Boolean {
        return when (permission) {
            ZaraPermission.SYSTEM_ALERT_WINDOW -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Settings.canDrawOverlays(context)
                } else true
            }
            ZaraPermission.NOTIFICATION_LISTENER -> {
                val enabledPackages = NotificationManagerCompat.getEnabledListenerPackages(context)
                enabledPackages.contains(context.packageName)
            }
            ZaraPermission.ACCESSIBILITY_SERVICE -> {
                val enabledServices = Settings.Secure.getString(
                    context.contentResolver,
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
                ) ?: ""
                enabledServices.contains(context.packageName)
            }
            else -> {
                if (permission.manifestPermission.isEmpty()) return true
                ContextCompat.checkSelfPermission(
                    context,
                    permission.manifestPermission
                ) == PackageManager.PERMISSION_GRANTED
            }
        }
    }

    fun areAllGranted(permissions: List<ZaraPermission>): Boolean {
        return permissions.all { isPermissionGranted(it) }
    }
}
