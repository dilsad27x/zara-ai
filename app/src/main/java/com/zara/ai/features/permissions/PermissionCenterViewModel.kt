package com.zara.ai.features.permissions

import android.content.Context
import androidx.lifecycle.ViewModel
import com.zara.ai.core.permissions.PermissionManager
import com.zara.ai.core.permissions.model.ZaraPermission
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class PermissionItemState(
    val permission: ZaraPermission,
    val isGranted: Boolean
)

@HiltViewModel
class PermissionCenterViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val permissionManager: PermissionManager
) : ViewModel() {

    private val _permissionStates = MutableStateFlow<List<PermissionItemState>>(emptyList())
    val permissionStates: StateFlow<List<PermissionItemState>> = _permissionStates.asStateFlow()

    init {
        refreshPermissions()
    }

    fun refreshPermissions() {
        val list = ZaraPermission.entries.map { perm ->
            PermissionItemState(
                permission = perm,
                isGranted = permissionManager.isPermissionGranted(perm)
            )
        }
        _permissionStates.value = list
    }

    fun openPermissionSettings(permission: ZaraPermission) {
        val intent = permission.createSettingsIntent(context)
        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            val fallback = android.content.Intent(
                android.provider.Settings.ACTION_SETTINGS
            ).addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(fallback)
        }
    }
}
