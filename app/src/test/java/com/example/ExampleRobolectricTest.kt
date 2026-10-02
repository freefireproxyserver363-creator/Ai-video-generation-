package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.AspectRatio
import com.example.data.model.GenerationMode
import com.example.data.model.VisualStyle
import com.example.data.repository.PipelineStageGroup
import com.example.engine.AudioSoundtrackGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("CineStyle AI", appName)
    }

    @Test
    fun `visual style count and features are populated`() {
        assertEquals(15, VisualStyle.entries.size)
        assertNotNull(VisualStyle.fromId("anime"))
        assertNotNull(VisualStyle.fromId("horror"))
        assertNotNull(VisualStyle.fromId("realistic"))
        assertNotNull(VisualStyle.fromId("drawing"))
        assertNotNull(VisualStyle.fromId("watercolor"))
        assertNotNull(VisualStyle.fromId("comic_manga"))
        assertNotNull(VisualStyle.fromId("three_d_animation"))
        assertNotNull(VisualStyle.fromId("fantasy"))
        assertNotNull(VisualStyle.fromId("sci_fi"))
        assertNotNull(VisualStyle.fromId("clay_stop_motion"))
        assertNotNull(VisualStyle.fromId("pixel_art"))
        assertNotNull(VisualStyle.fromId("dark_fantasy"))
        assertNotNull(VisualStyle.fromId("custom_style"))

        assertTrue(VisualStyle.popularCombinations.isNotEmpty())
    }

    @Test
    fun `pipeline stage groups are complete and sequenced`() {
        assertEquals(4, PipelineStageGroup.entries.size)
        assertEquals(PipelineStageGroup.PLANNING, PipelineStageGroup.entries[0])
        assertEquals("Planning", PipelineStageGroup.PLANNING.title)
        assertEquals(PipelineStageGroup.GENERATING_FRAMES, PipelineStageGroup.entries[1])
        assertEquals("Generating Frames", PipelineStageGroup.GENERATING_FRAMES.title)
        assertEquals(PipelineStageGroup.ASSEMBLING_AUDIO, PipelineStageGroup.entries[2])
        assertEquals("Assembling Audio", PipelineStageGroup.ASSEMBLING_AUDIO.title)
        assertEquals(PipelineStageGroup.FINALIZING, PipelineStageGroup.entries[3])
        assertEquals("Finalizing Video", PipelineStageGroup.FINALIZING.title)
    }

    @Test
    fun `soundtrack generator produces audio pcm data`() {
        val generator = AudioSoundtrackGenerator()
        val pcm = generator.generateSoundtrackPcm(
            style = VisualStyle.HORROR,
            durationSeconds = 1,
            sceneIndex = 1
        )
        // 44100 samples * 2 channels * 2 bytes = 176400 bytes
        assertEquals(176400, pcm.size)
    }

    @Test
    fun `aspect ratios have valid dimensions and ratios`() {
        for (ar in AspectRatio.entries) {
            assertTrue(ar.ratioFloat > 0f)
            assertTrue(ar.widthPx > 0)
            assertTrue(ar.heightPx > 0)
        }
    }
}
