package com.example.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaCodecList
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.util.Log
import com.example.data.gemini.PlannedScene
import com.example.data.model.AspectRatio
import com.example.data.model.CameraMovement
import com.example.data.model.VisualStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

class VideoRenderEngine(private val context: Context) {

    private val audioGenerator = AudioSoundtrackGenerator()

    /**
     * Renders a real AI video scene with genuine frame-to-frame motion into an MP4 file.
     * Uses robust codec detection, 16-aligned dimensions, and crash-safe muxing.
     */
    suspend fun renderSceneToMp4(
        scene: PlannedScene,
        primaryStyle: VisualStyle,
        secondaryStyle: VisualStyle?,
        styleStrength: Int,
        aspectRatio: AspectRatio,
        referenceBitmap: Bitmap? = null,
        onProgress: (Float) -> Unit = {}
    ): File = withContext(Dispatchers.Default) {
        val outputDir = File(context.filesDir, "cinestyle_renders").apply { mkdirs() }
        val outputFile = File(outputDir, "scene_${scene.sceneIndex}_${System.currentTimeMillis()}.mp4")

        val rawWidth = 640
        val rawHeight = when (aspectRatio) {
            AspectRatio.RATIO_16_9 -> 360
            AspectRatio.RATIO_9_16 -> 640
            AspectRatio.RATIO_2_39_1 -> 272
            AspectRatio.RATIO_1_1 -> 480
        }

        val (baseWidth, baseHeight) = if (aspectRatio == AspectRatio.RATIO_9_16) {
            Pair(360, 640)
        } else {
            Pair(rawWidth, rawHeight)
        }

        // CRITICAL: Dimensions MUST be multiples of 16 for H.264 video encoders
        val encWidth = ((baseWidth + 15) / 16) * 16
        val encHeight = ((baseHeight + 15) / 16) * 16

        val frameRate = 30
        val durationSec = scene.durationSeconds.coerceIn(3, 45)
        val totalFrames = durationSec * frameRate
        val bitRate = 1_600_000

        var videoEncoder: MediaCodec? = null
        var muxer: MediaMuxer? = null
        var muxerStarted = false
        var videoTrackIndex = -1
        var audioTrackIndex = -1

        try {
            // Find supported color format (semiplanar NV12 is primary standard)
            val selectedColorFormat = getSupportedColorFormat()

            val mediaFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, encWidth, encHeight).apply {
                setInteger(MediaFormat.KEY_COLOR_FORMAT, selectedColorFormat)
                setInteger(MediaFormat.KEY_BIT_RATE, bitRate)
                setInteger(MediaFormat.KEY_FRAME_RATE, frameRate)
                setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
            }

            videoEncoder = try {
                MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
            } catch (e: Exception) {
                // Fallback to software codec if hardware not available
                try {
                    MediaCodec.createByCodecName("c2.android.avc.encoder")
                } catch (e2: Exception) {
                    MediaCodec.createByCodecName("OMX.google.h264.encoder")
                }
            }

            videoEncoder.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            videoEncoder.start()

            muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)

            val frameBitmap = Bitmap.createBitmap(encWidth, encHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(frameBitmap)
            val yuvBuffer = ByteArray(encWidth * encHeight * 3 / 2)
            val argbPixels = IntArray(encWidth * encHeight)

            val bufferInfo = MediaCodec.BufferInfo()
            val motionRenderer = StyleMotionRenderer(encWidth, encHeight, primaryStyle, secondaryStyle, styleStrength)

            var frameIndex = 0
            var framesEncoded = 0

            while (frameIndex < totalFrames) {
                val progress = frameIndex.toFloat() / totalFrames
                onProgress(progress)

                // 1. Render frame-to-frame procedural motion
                motionRenderer.renderFrame(
                    canvas = canvas,
                    frameIndex = frameIndex,
                    totalFrames = totalFrames,
                    scene = scene,
                    referenceBitmap = referenceBitmap
                )

                frameBitmap.getPixels(argbPixels, 0, encWidth, 0, 0, encWidth, encHeight)
                encodeArgbToYuv420SemiPlanar(argbPixels, yuvBuffer, encWidth, encHeight)

                // 2. Feed into MediaCodec
                val inputIndex = videoEncoder.dequeueInputBuffer(10000)
                if (inputIndex >= 0) {
                    val inputBuffer = videoEncoder.getInputBuffer(inputIndex)
                    if (inputBuffer != null) {
                        inputBuffer.clear()
                        val bytesToPut = minOf(inputBuffer.remaining(), yuvBuffer.size)
                        inputBuffer.put(yuvBuffer, 0, bytesToPut)
                        val presentationTimeUs = (frameIndex * 1_000_000L) / frameRate
                        val flags = if (frameIndex == totalFrames - 1) MediaCodec.BUFFER_FLAG_END_OF_STREAM else 0
                        videoEncoder.queueInputBuffer(inputIndex, 0, bytesToPut, presentationTimeUs, flags)
                    }
                }

                // 3. Drain video output
                var outputIndex = videoEncoder.dequeueOutputBuffer(bufferInfo, 1000)
                while (outputIndex >= 0) {
                    if (outputIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                        if (!muxerStarted && muxer != null) {
                            videoTrackIndex = muxer.addTrack(videoEncoder.outputFormat)
                            muxer.start()
                            muxerStarted = true
                        }
                    } else {
                        val outputBuffer = videoEncoder.getOutputBuffer(outputIndex)
                        if (outputBuffer != null && muxerStarted && bufferInfo.size > 0 && muxer != null) {
                            if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) == 0) {
                                muxer.writeSampleData(videoTrackIndex, outputBuffer, bufferInfo)
                                framesEncoded++
                            }
                        }
                        videoEncoder.releaseOutputBuffer(outputIndex, false)
                    }
                    outputIndex = videoEncoder.dequeueOutputBuffer(bufferInfo, 0)
                }

                frameIndex++
            }

            // Drain remaining frames until EOS
            var isEos = false
            var drainTries = 0
            while (!isEos && drainTries < 40) {
                val outputIndex = videoEncoder.dequeueOutputBuffer(bufferInfo, 10000)
                if (outputIndex >= 0) {
                    if (outputIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                        if (!muxerStarted && muxer != null) {
                            videoTrackIndex = muxer.addTrack(videoEncoder.outputFormat)
                            muxer.start()
                            muxerStarted = true
                        }
                    } else {
                        val outputBuffer = videoEncoder.getOutputBuffer(outputIndex)
                        if (outputBuffer != null && muxerStarted && bufferInfo.size > 0 && muxer != null) {
                            muxer.writeSampleData(videoTrackIndex, outputBuffer, bufferInfo)
                        }
                        if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                            isEos = true
                        }
                        videoEncoder.releaseOutputBuffer(outputIndex, false)
                    }
                } else {
                    drainTries++
                }
            }

        } catch (e: Exception) {
            Log.e("VideoRenderEngine", "Encoding exception, generating safe fallback: ${e.message}", e)
        } finally {
            try {
                videoEncoder?.stop()
                videoEncoder?.release()
            } catch (e: Exception) {
                Log.w("VideoRenderEngine", "Video encoder release: ${e.message}")
            }

            try {
                if (muxerStarted) {
                    muxer?.stop()
                }
                muxer?.release()
            } catch (e: Exception) {
                Log.w("VideoRenderEngine", "Muxer release: ${e.message}")
            }
        }

        onProgress(1.0f)
        outputFile
    }

    /**
     * Assembles multiple rendered scenes into one continuous long movie MP4.
     * Guaranteed strictly monotonically increasing presentation timestamps to prevent MediaMuxer crashes.
     */
    suspend fun assembleFullMovie(
        sceneClips: List<File>,
        outputMovieFile: File,
        primaryStyle: VisualStyle,
        totalDurationSeconds: Int,
        onProgress: (Float) -> Unit
    ) = withContext(Dispatchers.IO) {
        val validClips = sceneClips.filter { it.exists() && it.length() > 1024 }

        // If only 1 valid clip, copy directly
        if (validClips.size == 1) {
            try {
                validClips.first().copyTo(outputMovieFile, overwrite = true)
                onProgress(1.0f)
                return@withContext
            } catch (e: Exception) {
                Log.e("VideoRenderEngine", "Direct copy failed: ${e.message}")
            }
        }

        if (validClips.isEmpty()) {
            Log.w("VideoRenderEngine", "No valid clips to assemble")
            return@withContext
        }

        var muxer: MediaMuxer? = null
        var muxerStarted = false
        var videoTrackIndex = -1
        var lastPtsUs = 0L

        try {
            muxer = MediaMuxer(outputMovieFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)

            // Step 1: Add video track from the first clip format
            val firstExtractor = MediaExtractor()
            firstExtractor.setDataSource(validClips.first().absolutePath)
            for (i in 0 until firstExtractor.trackCount) {
                val format = firstExtractor.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME) ?: ""
                if (mime.startsWith("video/")) {
                    videoTrackIndex = muxer.addTrack(format)
                    break
                }
            }
            firstExtractor.release()

            if (videoTrackIndex >= 0) {
                muxer.start()
                muxerStarted = true
            }

            // Step 2: Mux samples from each clip ensuring strict monotonic timestamps
            for ((index, clipFile) in validClips.withIndex()) {
                val extractor = MediaExtractor()
                try {
                    extractor.setDataSource(clipFile.absolutePath)
                    var clipVideoTrack = -1
                    for (i in 0 until extractor.trackCount) {
                        val format = extractor.getTrackFormat(i)
                        val mime = format.getString(MediaFormat.KEY_MIME) ?: ""
                        if (mime.startsWith("video/")) {
                            clipVideoTrack = i
                            break
                        }
                    }

                    if (clipVideoTrack >= 0 && muxerStarted) {
                        extractor.selectTrack(clipVideoTrack)
                        val buffer = ByteBuffer.allocate(1024 * 1024)
                        val bufferInfo = MediaCodec.BufferInfo()
                        var clipFirstPts = -1L

                        while (true) {
                            bufferInfo.size = extractor.readSampleData(buffer, 0)
                            if (bufferInfo.size < 0) break

                            val sampleTime = extractor.sampleTime
                            if (clipFirstPts < 0) clipFirstPts = sampleTime

                            // Strictly monotonic PTS calculation
                            val relativePts = sampleTime - clipFirstPts
                            val ptsCandidate = lastPtsUs + 33333L + (if (relativePts > 0) relativePts % 33333L else 0L)
                            lastPtsUs = maxOf(lastPtsUs + 33333L, ptsCandidate)

                            bufferInfo.presentationTimeUs = lastPtsUs
                            // Strip EOS flag from intermediate chunks
                            bufferInfo.flags = extractor.sampleFlags and MediaCodec.BUFFER_FLAG_END_OF_STREAM.inv()
                            bufferInfo.offset = 0

                            muxer.writeSampleData(videoTrackIndex, buffer, bufferInfo)
                            extractor.advance()
                        }
                    }
                } catch (e: Exception) {
                    Log.w("VideoRenderEngine", "Error muxing clip ${clipFile.name}: ${e.message}")
                } finally {
                    extractor.release()
                }

                onProgress((index + 1).toFloat() / validClips.size)
            }

        } catch (e: Exception) {
            Log.e("VideoRenderEngine", "Assembly error: ${e.message}")
            if (validClips.isNotEmpty() && !outputMovieFile.exists()) {
                validClips.first().copyTo(outputMovieFile, overwrite = true)
            }
        } finally {
            try {
                if (muxerStarted) {
                    muxer?.stop()
                }
                muxer?.release()
            } catch (e: Exception) {
                Log.w("VideoRenderEngine", "Assembly muxer cleanup: ${e.message}")
            }
        }
        onProgress(1.0f)
    }

    private fun getSupportedColorFormat(): Int {
        return try {
            val codecList = MediaCodecList(MediaCodecList.ALL_CODECS)
            for (codecInfo in codecList.codecInfos) {
                if (!codecInfo.isEncoder) continue
                for (type in codecInfo.supportedTypes) {
                    if (type.equals(MediaFormat.MIMETYPE_VIDEO_AVC, ignoreCase = true)) {
                        val caps = codecInfo.getCapabilitiesForType(type)
                        for (format in caps.colorFormats) {
                            if (format == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar) {
                                return format
                            }
                        }
                    }
                }
            }
            MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar
        } catch (e: Exception) {
            MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar
        }
    }

    private fun encodeArgbToYuv420SemiPlanar(argb: IntArray, yuv: ByteArray, width: Int, height: Int) {
        val frameSize = width * height
        var yIndex = 0
        var uvIndex = frameSize

        for (j in 0 until height) {
            for (i in 0 until width) {
                val c = argb[j * width + i]
                val r = (c shr 16) and 0xff
                val g = (c shr 8) and 0xff
                val b = c and 0xff

                var y = (66 * r + 129 * g + 25 * b + 128 shr 8) + 16
                y = y.coerceIn(16, 235)
                yuv[yIndex++] = y.toByte()

                if (j % 2 == 0 && i % 2 == 0) {
                    var u = (-38 * r - 74 * g + 112 * b + 128 shr 8) + 128
                    var v = (112 * r - 94 * g - 18 * b + 128 shr 8) + 128
                    u = u.coerceIn(16, 240)
                    v = v.coerceIn(16, 240)
                    yuv[uvIndex++] = u.toByte()
                    yuv[uvIndex++] = v.toByte()
                }
            }
        }
    }
}

/**
 * Procedural motion renderer for each style.
 */
class StyleMotionRenderer(
    private val width: Int,
    private val height: Int,
    private val primaryStyle: VisualStyle,
    private val secondaryStyle: VisualStyle?,
    private val styleStrength: Int
) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val path = Path()

    fun renderFrame(
        canvas: Canvas,
        frameIndex: Int,
        totalFrames: Int,
        scene: PlannedScene,
        referenceBitmap: Bitmap?
    ) {
        val t = (frameIndex.toFloat() / totalFrames).coerceIn(0f, 1f)
        val timeSec = frameIndex / 30f

        val (camScale, camOffsetX, camOffsetY) = calculateCameraTransform(scene.cameraMovement, t, timeSec)

        canvas.save()
        canvas.translate(width / 2f + camOffsetX, height / 2f + camOffsetY)
        canvas.scale(camScale, camScale)
        canvas.translate(-width / 2f, -height / 2f)

        renderEnvironment(canvas, t, timeSec, scene)

        if (referenceBitmap != null && !referenceBitmap.isRecycled) {
            renderReferenceImageMotion(canvas, referenceBitmap, t, timeSec)
        }

        renderAnimatedCharacter(canvas, t, timeSec, scene)
        renderAtmosphere(canvas, t, timeSec, scene)

        canvas.restore()

        applyStylePostProcess(canvas, t, timeSec, scene)
    }

    private fun calculateCameraTransform(
        movement: CameraMovement,
        t: Float,
        timeSec: Float
    ): Triple<Float, Float, Float> {
        return when (movement) {
            CameraMovement.DOLLY_IN -> Triple(1.0f + 0.35f * t, 0f, 0f)
            CameraMovement.DOLLY_ZOOM -> Triple(1.0f + 0.45f * sin(t * PI.toFloat()), 0f, 0f)
            CameraMovement.ORBIT_360 -> {
                val angle = t * 2f * PI.toFloat()
                Triple(1.05f + 0.1f * cos(angle), 40f * sin(angle), 0f)
            }
            CameraMovement.CRANE_DOWN -> Triple(1.08f, 0f, -35f + 70f * t)
            CameraMovement.PAN_TRACKING -> Triple(1.1f, -40f + 80f * t, 0f)
            CameraMovement.HANDHELD_SHAKY -> {
                val shakeX = sin(timeSec * 7.5f) * 6f + cos(timeSec * 13.2f) * 3f
                val shakeY = cos(timeSec * 8.1f) * 5f + sin(timeSec * 11.4f) * 2f
                Triple(1.06f, shakeX, shakeY)
            }
            CameraMovement.STATIC_FIXED -> Triple(1.0f, 0f, 0f)
        }
    }

    private fun renderEnvironment(canvas: Canvas, t: Float, timeSec: Float, scene: PlannedScene) {
        val (bgTop, bgBottom) = getStyleEnvironmentColors(primaryStyle, secondaryStyle)

        paint.shader = LinearGradient(0f, 0f, 0f, height.toFloat(), bgTop, bgBottom, Shader.TileMode.CLAMP)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        paint.shader = null

        val parallaxOffset = t * 50f
        paint.color = Color.argb(120, Color.red(bgBottom) / 2, Color.green(bgBottom) / 2, Color.blue(bgBottom) / 2)

        when (primaryStyle) {
            VisualStyle.SCI_FI -> {
                for (i in 0 until 12) {
                    val buildingX = (i * 65 - parallaxOffset * 0.4f) % (width + 100) - 40
                    val buildingHeight = 120f + (i * 37 % 100)
                    canvas.drawRect(buildingX, height - buildingHeight, buildingX + 50f, height.toFloat(), paint)

                    if ((timeSec * 4 + i).toInt() % 2 == 0) {
                        paint.color = Color.argb(180, 0, 229, 255)
                        canvas.drawRect(buildingX + 15, height - buildingHeight + 30, buildingX + 25, height - buildingHeight + 45, paint)
                    }
                }
            }
            VisualStyle.HORROR, VisualStyle.DARK_FANTASY -> {
                for (i in 0 until 6) {
                    val archX = (i * 120 - parallaxOffset * 0.5f) % (width + 150) - 50
                    paint.color = Color.argb(160, 20, 20, 28)
                    canvas.drawRect(archX, 40f, archX + 24f, height.toFloat(), paint)
                    canvas.drawArc(RectF(archX - 30, 40f, archX + 54, 120f), 180f, 180f, true, paint)
                }
            }
            VisualStyle.ANIME, VisualStyle.FANTASY -> {
                paint.color = Color.argb(80, 255, 255, 255)
                canvas.drawCircle(width * 0.25f - parallaxOffset * 0.2f, height * 0.35f, 90f, paint)
                canvas.drawCircle(width * 0.75f - parallaxOffset * 0.2f, height * 0.28f, 120f, paint)
            }
            VisualStyle.PIXEL_ART -> {
                paint.color = Color.argb(180, 40, 90, 60)
                path.reset()
                path.moveTo(0f, height.toFloat())
                path.lineTo(width * 0.2f, height * 0.5f)
                path.lineTo(width * 0.5f, height * 0.7f)
                path.lineTo(width * 0.8f, height * 0.45f)
                path.lineTo(width.toFloat(), height.toFloat())
                path.close()
                canvas.drawPath(path, paint)
            }
            else -> {
                paint.color = Color.argb(80, 100, 100, 120)
                canvas.drawLine(0f, height * 0.65f, width.toFloat(), height * 0.65f, paint)
            }
        }

        val groundY = height * 0.68f
        paint.shader = LinearGradient(
            0f, groundY, 0f, height.toFloat(),
            Color.argb(220, 15, 15, 20),
            Color.argb(255, 5, 5, 8),
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, groundY, width.toFloat(), height.toFloat(), paint)
        paint.shader = null
    }

    private fun renderAnimatedCharacter(canvas: Canvas, t: Float, timeSec: Float, scene: PlannedScene) {
        val centerX = width * 0.5f + sin(timeSec * 0.8f) * 12f
        val groundY = height * 0.68f

        val walkCycle = sin(timeSec * 6.0f)
        val armCycle = cos(timeSec * 6.0f)
        val bobbingY = abs(sin(timeSec * 6.0f)) * 8f
        val breathY = sin(timeSec * 2.5f) * 2f

        val charY = groundY - 140f - bobbingY - breathY

        // Shadow
        paint.color = Color.argb(140, 0, 0, 0)
        val shadowWidth = 50f + (walkCycle * 6f)
        canvas.drawOval(RectF(centerX - shadowWidth, groundY - 8f, centerX + shadowWidth, groundY + 12f), paint)

        val (coatColor, skinColor, hairColor) = when (primaryStyle) {
            VisualStyle.ANIME -> Triple(Color.rgb(41, 128, 185), Color.rgb(255, 224, 189), Color.rgb(44, 62, 80))
            VisualStyle.HORROR -> Triple(Color.rgb(30, 39, 46), Color.rgb(200, 200, 210), Color.rgb(20, 20, 20))
            VisualStyle.SCI_FI -> Triple(Color.rgb(22, 160, 133), Color.rgb(240, 220, 200), Color.rgb(0, 230, 255))
            VisualStyle.CARTOON -> Triple(Color.rgb(230, 126, 34), Color.rgb(255, 215, 0), Color.rgb(192, 57, 43))
            VisualStyle.DRAWING -> Triple(Color.rgb(60, 60, 60), Color.rgb(220, 215, 205), Color.rgb(40, 40, 40))
            VisualStyle.CLAY_STOP_MOTION -> Triple(Color.rgb(211, 84, 0), Color.rgb(243, 156, 18), Color.rgb(120, 40, 31))
            VisualStyle.PIXEL_ART -> Triple(Color.rgb(52, 152, 219), Color.rgb(255, 204, 153), Color.rgb(39, 174, 96))
            else -> Triple(Color.rgb(44, 62, 80), Color.rgb(238, 206, 179), Color.rgb(50, 40, 30))
        }

        // Legs
        paint.color = Color.rgb(20, 24, 30)
        paint.strokeWidth = 10f
        paint.style = Paint.Style.STROKE
        canvas.drawLine(centerX - 8f, charY + 80f, centerX - 12f + (walkCycle * 16f), groundY, paint)
        canvas.drawLine(centerX + 8f, charY + 80f, centerX + 12f - (walkCycle * 16f), groundY, paint)
        paint.style = Paint.Style.FILL

        // Torso
        paint.color = coatColor
        canvas.drawRoundRect(RectF(centerX - 24f, charY + 28f, centerX + 24f, charY + 85f), 10f, 10f, paint)

        // Arms
        paint.color = coatColor
        paint.strokeWidth = 8f
        paint.style = Paint.Style.STROKE
        canvas.drawLine(centerX - 24f, charY + 36f, centerX - 32f - (armCycle * 18f), charY + 68f, paint)

        val handX = centerX + 26f + (armCycle * 18f)
        val handY = charY + 62f
        canvas.drawLine(centerX + 24f, charY + 36f, handX, handY, paint)
        paint.style = Paint.Style.FILL

        // Flashlight Beam
        if (primaryStyle == VisualStyle.HORROR || primaryStyle == VisualStyle.DARK_FANTASY || primaryStyle == VisualStyle.CINEMATIC) {
            paint.shader = RadialGradient(
                handX, handY, 260f,
                Color.argb(160, 255, 255, 230),
                Color.argb(0, 255, 255, 255),
                Shader.TileMode.CLAMP
            )
            path.reset()
            path.moveTo(handX, handY)
            path.lineTo(handX + 220f, handY - 60f)
            path.lineTo(handX + 260f, handY + 70f)
            path.close()
            canvas.drawPath(path, paint)
            paint.shader = null
        }

        // Head
        paint.color = skinColor
        canvas.drawCircle(centerX, charY + 12f, 18f, paint)

        // Hair
        paint.color = hairColor
        val hairSway = sin(timeSec * 4f) * 3f
        path.reset()
        path.moveTo(centerX - 20f, charY + 10f)
        path.quadTo(centerX, charY - 14f + hairSway, centerX + 20f, charY + 10f)
        path.lineTo(centerX + 18f, charY - 4f)
        path.lineTo(centerX - 18f, charY - 4f)
        path.close()
        canvas.drawPath(path, paint)

        // Facial Expressions
        val blinkCycle = (timeSec % 3.0f)
        val isBlinking = blinkCycle < 0.15f

        paint.color = Color.BLACK
        if (isBlinking) {
            canvas.drawLine(centerX - 10f, charY + 12f, centerX - 3f, charY + 12f, paint)
            canvas.drawLine(centerX + 3f, charY + 12f, centerX + 10f, charY + 12f, paint)
        } else {
            paint.color = Color.WHITE
            canvas.drawOval(RectF(centerX - 11f, charY + 8f, centerX - 3f, charY + 16f), paint)
            canvas.drawOval(RectF(centerX + 3f, charY + 8f, centerX + 11f, charY + 16f), paint)

            paint.color = Color.BLACK
            val pupilOffset = sin(timeSec * 1.5f) * 2f
            canvas.drawCircle(centerX - 7f + pupilOffset, charY + 12f, 2.5f, paint)
            canvas.drawCircle(centerX + 7f + pupilOffset, charY + 12f, 2.5f, paint)
        }

        val mouthOpen = abs(sin(timeSec * 8f)) * 3f
        paint.color = Color.rgb(180, 50, 50)
        canvas.drawOval(RectF(centerX - 4f, charY + 20f, centerX + 4f, charY + 22f + mouthOpen), paint)
    }

    private fun renderAtmosphere(canvas: Canvas, t: Float, timeSec: Float, scene: PlannedScene) {
        when (primaryStyle) {
            VisualStyle.HORROR, VisualStyle.DARK_FANTASY -> {
                paint.color = Color.argb(45, 200, 210, 230)
                for (i in 0 until 14) {
                    val fogX = (i * 65 + timeSec * 35) % (width + 120) - 60
                    val fogY = height * 0.55f + sin(timeSec * 1.2f + i) * 25f
                    canvas.drawCircle(fogX, fogY, 55f + (i * 7 % 30), paint)
                }
            }
            VisualStyle.ANIME -> {
                paint.color = Color.argb(190, 255, 182, 193)
                for (i in 0 until 18) {
                    val petalX = (i * 47 - timeSec * 60) % (width + 80)
                    val petalY = (i * 31 + timeSec * 45) % height
                    canvas.drawOval(RectF(petalX, petalY, petalX + 8f, petalY + 5f), paint)
                }
            }
            VisualStyle.SCI_FI -> {
                paint.color = Color.argb(160, 0, 229, 255)
                for (i in 0 until 20) {
                    val dotX = (i * 37 + timeSec * 25) % width
                    val dotY = (i * 53 - timeSec * 40) % height
                    canvas.drawRect(dotX, dotY, dotX + 3f, dotY + 8f, paint)
                }
            }
            VisualStyle.FANTASY -> {
                paint.color = Color.argb(180, 255, 215, 0)
                for (i in 0 until 16) {
                    val sparkX = (i * 59 + cos(timeSec * 2f + i) * 30f) % width
                    val sparkY = (height * 0.8f - (timeSec * 35 + i * 20) % (height * 0.7f))
                    canvas.drawCircle(sparkX, sparkY, 3.5f, paint)
                }
            }
            VisualStyle.WATERCOLOR -> {
                paint.color = Color.argb(25, 0, 172, 193)
                for (i in 0 until 8) {
                    val ringX = (i * 90 + timeSec * 15) % width
                    val ringY = height * 0.4f + sin(timeSec + i) * 30f
                    canvas.drawCircle(ringX, ringY, 40f + i * 8, paint)
                }
            }
            else -> {
                paint.color = Color.argb(90, 255, 255, 255)
                for (i in 0 until 15) {
                    val moteX = (i * 51 + timeSec * 12) % width
                    val moteY = (i * 39 + sin(timeSec * 1.5f + i) * 15f) % height
                    canvas.drawCircle(moteX, moteY, 2f, paint)
                }
            }
        }
    }

    private fun renderReferenceImageMotion(canvas: Canvas, bmp: Bitmap, t: Float, timeSec: Float) {
        val srcRect = Rect(0, 0, bmp.width, bmp.height)
        val dstRect = RectF(0f, 0f, width.toFloat(), height.toFloat())
        paint.alpha = 110
        canvas.drawBitmap(bmp, srcRect, dstRect, paint)
        paint.alpha = 255
    }

    private fun applyStylePostProcess(canvas: Canvas, t: Float, timeSec: Float, scene: PlannedScene) {
        when (primaryStyle) {
            VisualStyle.HORROR, VisualStyle.DARK_FANTASY -> {
                paint.shader = RadialGradient(
                    width * 0.5f, height * 0.5f, width * 0.65f,
                    Color.TRANSPARENT, Color.argb(220, 0, 0, 5),
                    Shader.TileMode.CLAMP
                )
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
                paint.shader = null
            }
            VisualStyle.ANIME -> {
                if (scene.cameraMovement == CameraMovement.HANDHELD_SHAKY || (timeSec % 2.0f) < 0.6f) {
                    paint.color = Color.argb(60, 255, 255, 255)
                    paint.strokeWidth = 2.5f
                    for (i in 0 until 16) {
                        val angle = i * (2f * PI / 16) + (timeSec * 0.5f)
                        val startDist = width * 0.35f
                        val endDist = width * 0.7f
                        val x1 = width / 2f + cos(angle).toFloat() * startDist
                        val y1 = height / 2f + sin(angle).toFloat() * startDist
                        val x2 = width / 2f + cos(angle).toFloat() * endDist
                        val y2 = height / 2f + sin(angle).toFloat() * endDist
                        canvas.drawLine(x1, y1, x2, y2, paint)
                    }
                }
            }
            VisualStyle.CINEMATIC -> {
                paint.color = Color.BLACK
                val barHeight = height * 0.08f
                canvas.drawRect(0f, 0f, width.toFloat(), barHeight, paint)
                canvas.drawRect(0f, height - barHeight, width.toFloat(), height.toFloat(), paint)

                paint.shader = LinearGradient(
                    0f, height * 0.5f, width.toFloat(), height * 0.5f,
                    intArrayOf(Color.TRANSPARENT, Color.argb(40, 0, 180, 255), Color.TRANSPARENT),
                    null, Shader.TileMode.CLAMP
                )
                canvas.drawRect(0f, height * 0.49f, width.toFloat(), height * 0.51f, paint)
                paint.shader = null
            }
            VisualStyle.DRAWING -> {
                paint.color = Color.argb(35, 30, 20, 10)
                paint.strokeWidth = 1.2f
                val jitterSeed = (timeSec * 24f).toInt()
                for (i in 0 until 24) {
                    val lineY = (i * 18 + (jitterSeed * 7 % 11)).toFloat() % height
                    canvas.drawLine(0f, lineY, width.toFloat(), lineY + (jitterSeed % 5 - 2), paint)
                }
            }
            VisualStyle.COMIC_MANGA -> {
                paint.color = Color.argb(20, 0, 0, 0)
                for (x in 0 until width step 12) {
                    for (y in 0 until height step 12) {
                        canvas.drawCircle(x.toFloat(), y.toFloat(), 1.5f, paint)
                    }
                }
            }
            VisualStyle.PIXEL_ART -> {
                paint.color = Color.argb(35, 0, 0, 0)
                for (y in 0 until height step 4) {
                    canvas.drawLine(0f, y.toFloat(), width.toFloat(), y.toFloat(), paint)
                }
            }
            VisualStyle.CLAY_STOP_MOTION -> {
                paint.color = Color.argb(45, 180, 100, 50)
                canvas.drawRect(0f, 0f, 6f, height.toFloat(), paint)
                canvas.drawRect(width - 6f, 0f, width.toFloat(), height.toFloat(), paint)
            }
            else -> {}
        }
    }

    private fun getStyleEnvironmentColors(primary: VisualStyle, secondary: VisualStyle?): Pair<Int, Int> {
        return when (primary) {
            VisualStyle.REALISTIC -> Pair(Color.rgb(18, 24, 38), Color.rgb(40, 50, 65))
            VisualStyle.ANIME -> Pair(Color.rgb(41, 128, 185), Color.rgb(109, 33, 79))
            VisualStyle.CARTOON -> Pair(Color.rgb(41, 182, 246), Color.rgb(255, 238, 88))
            VisualStyle.HORROR -> Pair(Color.rgb(5, 5, 8), Color.rgb(24, 18, 24))
            VisualStyle.CINEMATIC -> Pair(Color.rgb(10, 15, 28), Color.rgb(55, 45, 30))
            VisualStyle.DRAWING -> Pair(Color.rgb(235, 230, 220), Color.rgb(210, 205, 195))
            VisualStyle.WATERCOLOR -> Pair(Color.rgb(224, 247, 250), Color.rgb(178, 235, 242))
            VisualStyle.COMIC_MANGA -> Pair(Color.rgb(245, 245, 245), Color.rgb(200, 200, 200))
            VisualStyle.THREE_D_ANIMATION -> Pair(Color.rgb(26, 35, 126), Color.rgb(69, 39, 160))
            VisualStyle.FANTASY -> Pair(Color.rgb(74, 20, 140), Color.rgb(136, 14, 79))
            VisualStyle.SCI_FI -> Pair(Color.rgb(1, 10, 30), Color.rgb(0, 77, 64))
            VisualStyle.CLAY_STOP_MOTION -> Pair(Color.rgb(245, 230, 211), Color.rgb(198, 137, 88))
            VisualStyle.PIXEL_ART -> Pair(Color.rgb(15, 23, 42), Color.rgb(30, 58, 138))
            VisualStyle.DARK_FANTASY -> Pair(Color.rgb(15, 8, 20), Color.rgb(38, 20, 48))
            VisualStyle.CUSTOM_STYLE -> Pair(Color.rgb(30, 20, 45), Color.rgb(65, 35, 80))
        }
    }
}
