package com.zara.ai.core.voice.audio

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import com.zara.ai.core.common.logger.ZaraLogger
import com.zara.ai.core.permissions.PermissionManager
import com.zara.ai.core.permissions.model.ZaraPermission
import com.zara.ai.core.voice.model.AudioFrame
import com.zara.ai.core.voice.model.VoiceConfiguration
import com.zara.ai.core.voice.vad.VoiceActivityDetector
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max

/**
 * Manages low-latency real-time microphone capture via Android AudioRecord.
 * Emits raw 16-bit mono PCM audio frames and real-time audio amplitude metrics.
 */
@Singleton
class AudioCaptureManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val permissionManager: PermissionManager
) {
    private val logger = ZaraLogger("AudioCaptureManager")
    private val scope = CoroutineScope(Dispatchers.IO)

    private var audioRecord: AudioRecord? = null
    private var captureJob: Job? = null

    private val _isCapturing = MutableStateFlow(false)
    val isCapturing: StateFlow<Boolean> = _isCapturing.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    private val _currentAmplitude = MutableStateFlow(0.0f)
    val currentAmplitude: StateFlow<Float> = _currentAmplitude.asStateFlow()

    private val _audioFrames = MutableSharedFlow<AudioFrame>(extraBufferCapacity = 64)
    val audioFrames: SharedFlow<AudioFrame> = _audioFrames.asSharedFlow()

    /**
     * Start recording audio from the microphone.
     */
    @SuppressLint("MissingPermission")
    fun startCapture(
        config: VoiceConfiguration = VoiceConfiguration(),
        vad: VoiceActivityDetector? = null
    ): Boolean {
        if (_isCapturing.value) {
            logger.d("Audio capture already running")
            return true
        }

        if (!permissionManager.isPermissionGranted(ZaraPermission.RECORD_AUDIO)) {
            logger.e("RECORD_AUDIO permission not granted")
            return false
        }

        val sampleRate = config.sampleRate
        val channelConfig = AudioFormat.CHANNEL_IN_MONO
        val audioFormat = AudioFormat.ENCODING_PCM_16BIT

        val minBufSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
        if (minBufSize == AudioRecord.ERROR || minBufSize == AudioRecord.ERROR_BAD_VALUE) {
            logger.e("Invalid AudioRecord parameters: minBufSize=$minBufSize")
            return false
        }

        val bufferSize = max(minBufSize * 2, config.bufferSize)

        try {
            val record = AudioRecord(
                MediaRecorder.AudioSource.VOICE_RECOGNITION,
                sampleRate,
                channelConfig,
                audioFormat,
                bufferSize
            )

            if (record.state != AudioRecord.STATE_INITIALIZED) {
                logger.w("VOICE_RECOGNITION audio source failed, falling back to MIC")
                record.release()
                audioRecord = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    sampleRate,
                    channelConfig,
                    audioFormat,
                    bufferSize
                )
            } else {
                audioRecord = record
            }

            val finalRecord = audioRecord ?: return false
            if (finalRecord.state != AudioRecord.STATE_INITIALIZED) {
                logger.e("Failed to initialize AudioRecord instance")
                finalRecord.release()
                audioRecord = null
                return false
            }

            finalRecord.startRecording()
            _isCapturing.value = true
            logger.i("AudioRecord capture started (sampleRate=$sampleRate, bufSize=$bufferSize)")

            val readChunkSize = config.bufferSize
            val buffer = ByteArray(readChunkSize)

            captureJob?.cancel()
            captureJob = scope.launch {
                while (isActive && _isCapturing.value) {
                    val bytesRead = finalRecord.read(buffer, 0, buffer.size)
                    if (bytesRead > 0) {
                        if (_isMuted.value) {
                            _currentAmplitude.value = 0.0f
                            continue
                        }

                        val frameBytes = buffer.copyOf(bytesRead)
                        val frame = vad?.processFrame(frameBytes, sampleRate) ?: AudioFrame(
                            data = frameBytes,
                            timestampMs = System.currentTimeMillis(),
                            sampleRate = sampleRate,
                            channels = 1,
                            rmsEnergy = 0.0f,
                            isSpeech = false
                        )

                        val visualAmp = vad?.calculateVisualAmplitude(frame.rmsEnergy) ?: 0.0f
                        _currentAmplitude.value = visualAmp
                        _audioFrames.tryEmit(frame)
                    } else if (bytesRead < 0) {
                        logger.w("AudioRecord.read error code: $bytesRead")
                        break
                    }
                }
            }
            return true
        } catch (e: Exception) {
            logger.e("Exception during AudioRecord startup: ${e.message}")
            stopCapture()
            return false
        }
    }

    /**
     * Stop microphone recording and release resources.
     */
    fun stopCapture() {
        if (!_isCapturing.value && audioRecord == null) return

        logger.d("Stopping audio capture")
        _isCapturing.value = false
        _currentAmplitude.value = 0.0f

        captureJob?.cancel()
        captureJob = null

        try {
            audioRecord?.apply {
                if (recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                    stop()
                }
                release()
            }
        } catch (e: Exception) {
            logger.e("Error releasing AudioRecord: ${e.message}")
        } finally {
            audioRecord = null
        }
    }

    /**
     * Mute or unmute voice capture.
     */
    fun setMute(muted: Boolean) {
        _isMuted.value = muted
        if (muted) {
            _currentAmplitude.value = 0.0f
        }
    }
}
