package com.zara.ai.core.permissions

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
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
        if (Build.VERSION.SDK_INT < permission.minSdk || permission.manifestPermission.isEmpty()) {
            return true
        }
        return ContextCompat.checkSelfPermission(
            context,
            permission.manifestPermission
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun areAllGranted(permissions: List<ZaraPermission>): Boolean {
        return permissions.all { isPermissionGranted(it) }
    }
}
