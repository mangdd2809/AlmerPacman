package com.example.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

/**
 * Procedural low-latency 8-bit sound effects synthesizer using AudioTrack.
 * Generates authentic retro arcade sounds dynamically with zero external asset dependencies.
 */
class SoundManager(private val context: Context) {

    var soundEnabled: Boolean = true
    var hapticsEnabled: Boolean = true

    private val sampleRate = 22050
    private val scope = CoroutineScope(Dispatchers.Default)

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private var chompToggle = false

    fun playChomp() {
        if (!soundEnabled) return
        chompToggle = !chompToggle
        val freq = if (chompToggle) 420.0 else 540.0
        playSound(durationMs = 50, startFreq = freq, endFreq = freq, waveType = WaveType.SQUARE, volume = 0.4f)
    }

    fun playPowerPellet() {
        if (!soundEnabled) return
        vibrate(40)
        playSound(durationMs = 250, startFreq = 300.0, endFreq = 750.0, waveType = WaveType.SINE, volume = 0.7f)
    }

    fun playEatGhost() {
        if (!soundEnabled) return
        vibrate(80)
        scope.launch {
            playSoundSync(durationMs = 90, startFreq = 587.0, endFreq = 587.0, waveType = WaveType.SQUARE, volume = 0.8f)
            playSoundSync(durationMs = 140, startFreq = 880.0, endFreq = 980.0, waveType = WaveType.SQUARE, volume = 0.9f)
        }
    }

    fun playFruit() {
        if (!soundEnabled) return
        vibrate(60)
        scope.launch {
            playSoundSync(durationMs = 80, startFreq = 523.0, endFreq = 659.0, waveType = WaveType.SINE, volume = 0.7f)
            playSoundSync(durationMs = 120, startFreq = 784.0, endFreq = 1046.0, waveType = WaveType.SINE, volume = 0.8f)
        }
    }

    fun playDeath() {
        if (!soundEnabled) return
        vibrate(200)
        playSound(durationMs = 600, startFreq = 750.0, endFreq = 90.0, waveType = WaveType.SAWTOOTH, volume = 0.8f)
    }

    fun playLevelWin() {
        if (!soundEnabled) return
        vibrate(100)
        scope.launch {
            val notes = doubleArrayOf(440.0, 554.0, 659.0, 880.0)
            for (note in notes) {
                playSoundSync(durationMs = 110, startFreq = note, endFreq = note, waveType = WaveType.SINE, volume = 0.8f)
            }
        }
    }

    fun playButtonClick() {
        if (!soundEnabled) return
        vibrate(20)
        playSound(durationMs = 30, startFreq = 600.0, endFreq = 800.0, waveType = WaveType.SINE, volume = 0.5f)
    }

    fun playIntroJingle() {
        if (!soundEnabled) return
        vibrate(60)
        scope.launch {
            val notes = doubleArrayOf(493.88, 987.77, 739.99, 622.25, 987.77, 739.99, 622.25, 523.25)
            for (note in notes) {
                playSoundSync(durationMs = 120, startFreq = note, endFreq = note, waveType = WaveType.SINE, volume = 0.75f)
            }
        }
    }

    private enum class WaveType { SINE, SQUARE, SAWTOOTH }

    private fun playSound(
        durationMs: Int,
        startFreq: Double,
        endFreq: Double,
        waveType: WaveType,
        volume: Float
    ) {
        scope.launch {
            playSoundSync(durationMs, startFreq, endFreq, waveType, volume)
        }
    }

    private fun playSoundSync(
        durationMs: Int,
        startFreq: Double,
        endFreq: Double,
        waveType: WaveType,
        volume: Float
    ) {
        try {
            val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
            if (numSamples <= 0) return
            val buffer = ShortArray(numSamples)

            for (i in 0 until numSamples) {
                val progress = i.toDouble() / numSamples
                val currentFreq = startFreq + (endFreq - startFreq) * progress
                val time = i.toDouble() / sampleRate
                val phase = (time * currentFreq) % 1.0

                // Envelope: quick attack, steady decay
                val envelope = when {
                    progress < 0.1 -> progress / 0.1
                    else -> 1.0 - (progress - 0.1) / 0.9
                }

                val sampleVal: Double = when (waveType) {
                    WaveType.SINE -> sin(2.0 * Math.PI * phase)
                    WaveType.SQUARE -> if (phase < 0.5) 1.0 else -1.0
                    WaveType.SAWTOOTH -> 2.0 * (phase - 0.5)
                }

                val clamped = (sampleVal * envelope * volume * Short.MAX_VALUE).toInt()
                buffer[i] = clamped.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }

            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val audioFormat = AudioFormat.Builder()
                .setSampleRate(sampleRate)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build()

            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(audioAttributes)
                .setAudioFormat(audioFormat)
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(buffer, 0, buffer.size)
            audioTrack.play()
            Thread.sleep(durationMs.toLong() + 20)
            audioTrack.stop()
            audioTrack.release()
        } catch (_: Exception) {
            // Ignore audio interruptions gracefully
        }
    }

    private fun vibrate(durationMs: Long) {
        if (!hapticsEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
        } catch (_: Exception) {
            // Haptics permission or hardware missing safely ignored
        }
    }
}
