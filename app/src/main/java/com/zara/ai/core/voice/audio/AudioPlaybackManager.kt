package com.zara.ai.core.voice.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.zara.ai.core.common.logger.ZaraLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.LinkedBlockingQueue
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * High-performance low-latency PCM audio playback manager using Android AudioTrack.
 * Supports instant flushing and cancellation for seamless user interruption (barge-in).
 */
@Singleton
class AudioPlaybackManager @Inject constructor() {
    private val logger = ZaraLogger("AudioPlaybackManager")
    private val scope = CoroutineScope(Dispatchers.IO)

    private var audioTrack: AudioTrack? = null
    private var playbackJob: Job? = null
    private val audioQueue = LinkedBlockingQueue<ByteArray>()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _playbackAmplitude = MutableStateFlow(0.0f)
    val playbackAmplitude: StateFlow<Float> = _playbackAmplitude.asStateFlow()

    private var currentSampleRate = 24000 // Standard for Gemini Live 24kHz audio output

    /**
     * Initialize the streaming AudioTrack instance with the desired sample rate.
     */
    @Synchronized
    fun initialize(sampleRate: Int = 24000): Boolean {
        if (audioTrack != null && currentSampleRate == sampleRate) {
            return true
        }

        release()
        currentSampleRate = sampleRate

        val channelMask = AudioFormat.CHANNEL_OUT_MONO
        val encoding = AudioFormat.ENCODING_PCM_16BIT
        val minBufferSize = AudioTrack.getMinBufferSize(sampleRate, channelMask, encoding)

        if (minBufferSize <= 0) {
            logger.e("Invalid AudioTrack buffer size: $minBufferSize")
            return false
        }

        try {
            val attributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANT)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()

            val format = AudioFormat.Builder()
                .setSampleRate(sampleRate)
                .setChannelMask(channelMask)
                .setEncoding(encoding)
                .build()

            val track = AudioTrack(
                attributes,
                format,
                max(minBufferSize * 2, 4096),
                AudioTrack.MODE_STREAM,
                android.media.AudioManager.AUDIO_SESSION_ID_GENERATE
            )

            if (track.state != AudioTrack.STATE_INITIALIZED) {
                logger.e("Failed to initialize AudioTrack instance")
                track.release()
                return false
            }

            audioTrack = track
            track.play()
            logger.i("AudioTrack initialized at ${sampleRate}Hz")

            startPlaybackLoop()
            return true
        } catch (e: Exception) {
            logger.e("Exception initializing AudioTrack: ${e.message}")
            return false
        }
    }

    /**
     * Enqueue a PCM chunk for streaming playback.
     */
    fun enqueueAudio(chunk: ByteArray) {
        if (audioTrack == null) {
            initialize(currentSampleRate)
        }
        audioQueue.offer(chunk)
    }

    /**
     * Flush all buffered audio immediately and halt playback.
     * Essential for conversational barge-in when the user interrupts the assistant.
     */
    @Synchronized
    fun flushAndHalt() {
        audioQueue.clear()
        _isPlaying.value = false
        _playbackAmplitude.value = 0.0f

        try {
            audioTrack?.apply {
                if (playState == AudioTrack.PLAYSTATE_PLAYING) {
                    pause()
                    flush()
                    play() // Re-arm for immediate future frames
                }
            }
            logger.d("AudioTrack flushed and halted for user interruption")
        } catch (e: Exception) {
            logger.e("Error flushing AudioTrack: ${e.message}")
        }
    }

    /**
     * Adjust audio playback volume (0.0 to 1.0).
     */
    fun setVolume(volume: Float) {
        val clamped = max(0.0f, min(1.0f, volume))
        audioTrack?.setVolume(clamped)
    }

    /**
     * Release AudioTrack and clean up background processing.
     */
    @Synchronized
    fun release() {
        audioQueue.clear()
        playbackJob?.cancel()
        playbackJob = null
        _isPlaying.value = false
        _playbackAmplitude.value = 0.0f

        try {
            audioTrack?.apply {
                if (playState == AudioTrack.PLAYSTATE_PLAYING) {
                    stop()
                }
                release()
            }
        } catch (e: Exception) {
            logger.e("Error releasing AudioTrack: ${e.message}")
        } finally {
            audioTrack = null
        }
    }

    private fun startPlaybackLoop() {
        playbackJob?.cancel()
        playbackJob = scope.launch {
            while (true) {
                val chunk = audioQueue.poll(50, java.util.concurrent.TimeUnit.MILLISECONDS)
                if (chunk != null && chunk.isNotEmpty()) {
                    _isPlaying.value = true
                    computeAndEmitVisualAmplitude(chunk)

                    val track = audioTrack
                    if (track != null && track.state == AudioTrack.STATE_INITIALIZED) {
                        track.write(chunk, 0, chunk.size)
                    }
                } else if (audioQueue.isEmpty()) {
                    if (_isPlaying.value) {
                        _isPlaying.value = false
                        _playbackAmplitude.value = 0.0f
                    }
                }
            }
        }
    }

    private fun computeAndEmitVisualAmplitude(bytes: ByteArray) {
        try {
            val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer()
            val count = buffer.remaining()
            if (count == 0) return

            var sumSquare = 0.0
            for (i in 0 until count) {
                val s = buffer.get(i) / 32768.0f
                sumSquare += (s * s)
            }
            val rms = sqrt(sumSquare / count).toFloat()
            val db = 20.0f * log10(max(rms, 0.0001f))
            val visualAmp = max(0.0f, min(1.0f, (db + 45.0f) / 45.0f))
            _playbackAmplitude.value = visualAmp
        } catch (_: Exception) {
            // Ignore calculation anomalies during rapid flush
        }
    }
}
