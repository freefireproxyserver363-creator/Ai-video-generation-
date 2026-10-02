package com.example.engine

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.util.Log
import com.example.data.model.VisualStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.sin

/**
 * Procedural Audio Synthesizer that generates cinematic ambient score,
 * atmospheric foley, and musical chords tailored to each visual style.
 */
class AudioSoundtrackGenerator {

    companion object {
        const val SAMPLE_RATE = 44100
        const val CHANNEL_COUNT = 2
        const val BIT_RATE = 128000
    }

    /**
     * Synthesizes PCM 16-bit audio data for a given visual style and duration.
     */
    fun generateSoundtrackPcm(
        style: VisualStyle,
        durationSeconds: Int,
        sceneIndex: Int
    ): ByteArray {
        val totalSamples = SAMPLE_RATE * durationSeconds
        val pcmData = ByteArray(totalSamples * CHANNEL_COUNT * 2) // 16-bit stereo = 4 bytes per sample
        val buffer = ByteBuffer.wrap(pcmData).order(ByteOrder.LITTLE_ENDIAN)

        // Musical base frequencies depending on style
        val (baseFreq1, baseFreq2, baseFreq3) = when (style) {
            VisualStyle.HORROR, VisualStyle.DARK_FANTASY -> Triple(55.0, 58.27, 82.41) // Low ominous minor 2nd drone (A1, Bb1, E2)
            VisualStyle.SCI_FI -> Triple(65.41, 130.81, 196.00) // Synth C2, C3, G3
            VisualStyle.ANIME, VisualStyle.COMIC_MANGA -> Triple(110.0, 164.81, 220.0) // Dramatic A2, E3, A3
            VisualStyle.FANTASY -> Triple(130.81, 196.0, 261.63) // Enchanted C major chord
            VisualStyle.WATERCOLOR, VisualStyle.DRAWING -> Triple(146.83, 220.0, 293.66) // Soft lyrical D major
            VisualStyle.PIXEL_ART -> Triple(220.0, 277.18, 329.63) // 8-bit chiptune arpeggio
            VisualStyle.CLAY_STOP_MOTION, VisualStyle.CARTOON -> Triple(130.81, 164.81, 196.0) // Playful C-E-G
            else -> Triple(73.42, 110.0, 146.83) // Cinematic D minor
        }

        var phase1 = 0.0
        var phase2 = 0.0
        var phase3 = 0.0
        var lfoPhase = 0.0

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / SAMPLE_RATE

            // Low frequency oscillator for atmospheric swell
            val lfo = 0.7 + 0.3 * sin(2.0 * PI * 0.25 * t)

            // Tension rise across scene duration
            val tensionMod = 1.0 + (sceneIndex * 0.05) + 0.1 * (t / durationSeconds)

            // Primary harmonic drones
            val wave1 = sin(phase1)
            val wave2 = sin(phase2) * 0.7
            val wave3 = sin(phase3) * 0.5

            // Atmospheric whisper / tape saturation noise for realism & horror
            val noise = if (style == VisualStyle.HORROR || style == VisualStyle.DARK_FANTASY) {
                ((Math.random() - 0.5) * 0.08) * (1.0 + sin(2.0 * PI * 1.5 * t))
            } else if (style == VisualStyle.DRAWING || style == VisualStyle.CLAY_STOP_MOTION) {
                ((Math.random() - 0.5) * 0.04) // Vinyl/paper texture crackle
            } else {
                ((Math.random() - 0.5) * 0.015)
            }

            // Pulse or heartbeat in horror/action
            val heartbeat = if (style == VisualStyle.HORROR && (t % 1.2) < 0.15) {
                sin(2.0 * PI * 40.0 * (t % 1.2)) * 0.4
            } else 0.0

            val rawSample = ((wave1 + wave2 + wave3) * 0.3 * lfo + noise + heartbeat) * tensionMod

            // Clamp and convert to 16-bit signed integer
            val sampleVal = (rawSample.coerceIn(-1.0, 1.0) * 28000.0).toInt().toShort()

            // Stereo separation (left & right slight phase difference)
            val rightSampleVal = (rawSample * 0.95).coerceIn(-1.0, 1.0).let {
                (it * 28000.0).toInt().toShort()
            }

            buffer.putShort(sampleVal)
            buffer.putShort(rightSampleVal)

            phase1 += 2.0 * PI * baseFreq1 / SAMPLE_RATE
            phase2 += 2.0 * PI * baseFreq2 / SAMPLE_RATE
            phase3 += 2.0 * PI * baseFreq3 / SAMPLE_RATE
        }

        return pcmData
    }

    /**
     * Encodes PCM audio into AAC format and writes to the MediaMuxer audio track.
     */
    suspend fun encodeAndMuxAudio(
        pcmData: ByteArray,
        muxer: MediaMuxer,
        audioTrackIndex: Int
    ) = withContext(Dispatchers.IO) {
        try {
            val audioFormat = MediaFormat.createAudioFormat(
                MediaFormat.MIMETYPE_AUDIO_AAC,
                SAMPLE_RATE,
                CHANNEL_COUNT
            ).apply {
                setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
                setInteger(MediaFormat.KEY_BIT_RATE, BIT_RATE)
                setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 16384)
            }

            val encoder = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC)
            encoder.configure(audioFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            encoder.start()

            val bufferInfo = MediaCodec.BufferInfo()
            var pcmOffset = 0
            val pcmLength = pcmData.size
            var presentationTimeUs = 0L
            val bytesPerSample = CHANNEL_COUNT * 2
            var isEos = false

            while (!isEos) {
                // Feed input
                val inputIndex = encoder.dequeueInputBuffer(10000)
                if (inputIndex >= 0) {
                    val inputBuffer = encoder.getInputBuffer(inputIndex)
                    if (inputBuffer != null) {
                        inputBuffer.clear()
                        val bytesToRead = minOf(inputBuffer.remaining(), pcmLength - pcmOffset)
                        if (bytesToRead > 0) {
                            inputBuffer.put(pcmData, pcmOffset, bytesToRead)
                            pcmOffset += bytesToRead
                            val sampleCount = bytesToRead / bytesPerSample
                            presentationTimeUs += (sampleCount * 1_000_000L) / SAMPLE_RATE
                            encoder.queueInputBuffer(inputIndex, 0, bytesToRead, presentationTimeUs, 0)
                        } else {
                            encoder.queueInputBuffer(
                                inputIndex,
                                0,
                                0,
                                presentationTimeUs,
                                MediaCodec.BUFFER_FLAG_END_OF_STREAM
                            )
                        }
                    }
                }

                // Drain output
                var outputIndex = encoder.dequeueOutputBuffer(bufferInfo, 10000)
                while (outputIndex >= 0) {
                    val outputBuffer = encoder.getOutputBuffer(outputIndex)
                    if (outputBuffer != null && (bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) == 0) {
                        if (bufferInfo.size > 0) {
                            muxer.writeSampleData(audioTrackIndex, outputBuffer, bufferInfo)
                        }
                    }
                    if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        isEos = true
                    }
                    encoder.releaseOutputBuffer(outputIndex, false)
                    if (isEos) break
                    outputIndex = encoder.dequeueOutputBuffer(bufferInfo, 1000)
                }
            }

            encoder.stop()
            encoder.release()
        } catch (e: Exception) {
            Log.e("AudioGenerator", "Error encoding audio track: ${e.message}", e)
        }
    }
}
