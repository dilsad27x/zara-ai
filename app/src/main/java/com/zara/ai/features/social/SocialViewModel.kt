package com.zara.ai.features.social

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zara.ai.core.common.logger.ZaraLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SocialChannelIntegration(
    val id: String,
    val platformName: String,
    val accountHandle: String?,
    val isConnected: Boolean,
    val statusDescription: String,
    val autoReplyEnabled: Boolean
)

data class PendingDraft(
    val id: String,
    val platform: String,
    val recipient: String,
    val draftContent: String,
    val createdAt: Long = System.currentTimeMillis()
)

data class ApprovalRequest(
    val id: String,
    val actionTitle: String,
    val description: String,
    val riskLevel: String = "Medium"
)

data class SocialUiState(
    val channels: List<SocialChannelIntegration> = emptyList(),
    val pendingDrafts: List<PendingDraft> = emptyList(),
    val approvalRequests: List<ApprovalRequest> = emptyList(),
    val emergencyStopActive: Boolean = false
)

@HiltViewModel
class SocialViewModel @Inject constructor(
    private val logger: ZaraLogger
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SocialUiState(
            channels = listOf(
                SocialChannelIntegration(
                    id = "telegram",
                    platformName = "Telegram Bot API",
                    accountHandle = "@ZaraAgent_Bot",
                    isConnected = true,
                    statusDescription = "Polling webhook listener active",
                    autoReplyEnabled = true
                ),
                SocialChannelIntegration(
                    id = "whatsapp",
                    platformName = "WhatsApp Business / API",
                    accountHandle = null,
                    isConnected = false,
                    statusDescription = "Token configuration required",
                    autoReplyEnabled = false
                ),
                SocialChannelIntegration(
                    id = "instagram",
                    platformName = "Instagram Graph API",
                    accountHandle = null,
                    isConnected = false,
                    statusDescription = "Meta developer credentials required",
                    autoReplyEnabled = false
                ),
                SocialChannelIntegration(
                    id = "messenger",
                    platformName = "Facebook Messenger",
                    accountHandle = null,
                    isConnected = false,
                    statusDescription = "Meta webhooks required",
                    autoReplyEnabled = false
                )
            ),
            pendingDrafts = listOf(
                PendingDraft(
                    id = "draft_1",
                    platform = "Telegram",
                    recipient = "@alex_developer",
                    draftContent = "Hi Alex, ZARA has completed the automated website build. Let me know when you are ready to review."
                )
            ),
            approvalRequests = listOf(
                ApprovalRequest(
                    id = "req_1",
                    actionTitle = "Broadcast Task Completion Notice",
                    description = "Publish status update to connected Telegram channels.",
                    riskLevel = "Low"
                )
            )
        )
    )
    val uiState: StateFlow<SocialUiState> = _uiState.asStateFlow()

    fun toggleAutoReply(channelId: String) {
        val updated = _uiState.value.channels.map { ch ->
            if (ch.id == channelId) ch.copy(autoReplyEnabled = !ch.autoReplyEnabled) else ch
        }
        _uiState.value = _uiState.value.copy(channels = updated)
        logger.i("SocialViewModel", "Toggled auto-reply for channel: $channelId")
    }

    fun approveDraft(draftId: String) {
        val remaining = _uiState.value.pendingDrafts.filterNot { it.id == draftId }
        _uiState.value = _uiState.value.copy(pendingDrafts = remaining)
        logger.i("SocialViewModel", "Approved and dispatched draft: $draftId")
    }

    fun dismissDraft(draftId: String) {
        val remaining = _uiState.value.pendingDrafts.filterNot { it.id == draftId }
        _uiState.value = _uiState.value.copy(pendingDrafts = remaining)
        logger.i("SocialViewModel", "Dismissed draft: $draftId")
    }

    fun approveRequest(requestId: String) {
        val remaining = _uiState.value.approvalRequests.filterNot { it.id == requestId }
        _uiState.value = _uiState.value.copy(approvalRequests = remaining)
        logger.i("SocialViewModel", "Authorized autonomous approval request: $requestId")
    }

    fun triggerEmergencyStop() {
        val current = _uiState.value.emergencyStopActive
        val updatedChannels = _uiState.value.channels.map { it.copy(autoReplyEnabled = false) }
        _uiState.value = _uiState.value.copy(
            emergencyStopActive = !current,
            channels = updatedChannels
        )
        logger.w("SocialViewModel", "EMERGENCY STOP TOGGLED: ${!current}")
    }
}
