package com.example.data.model

enum class GenerationMode(
    val id: String,
    val title: String,
    val subtitle: String,
    val defaultDurationSec: Int,
    val recommendedShots: Int
) {
    QUICK(
        id = "quick",
        title = "Quick Video",
        subtitle = "Short single-shot test for prompts and style tuning (5-10s)",
        defaultDurationSec = 8,
        recommendedShots = 1
    ),
    CINEMATIC(
        id = "cinematic",
        title = "Cinematic Video",
        subtitle = "Multi-shot assembled sequence with dynamic camera angles (30-60s)",
        defaultDurationSec = 30,
        recommendedShots = 4
    ),
    STORY_MOVIE(
        id = "story_movie",
        title = "Story / Movie Mode",
        subtitle = "Deep long-form AI movie (1-5 mins) with scene bible & continuity",
        defaultDurationSec = 180,
        recommendedShots = 8
    )
}

enum class AspectRatio(
    val id: String,
    val label: String,
    val ratioFloat: Float,
    val widthPx: Int,
    val heightPx: Int
) {
    RATIO_16_9("16:9", "16:9 Cinema", 16f / 9f, 1280, 720),
    RATIO_9_16("9:16", "9:16 Shorts/Reels", 9f / 16f, 720, 1280),
    RATIO_2_39_1("2.39:1", "2.39:1 Anamorphic", 2.39f, 1280, 536),
    RATIO_1_1("1:1", "1:1 Square", 1.0f, 720, 720)
}

enum class InputMode {
    TEXT_PROMPT,
    IMAGE_REFERENCE,
    STORY_SCRIPT
}

enum class CameraMovement(val label: String, val description: String) {
    DOLLY_IN("Dolly In", "Smooth cinematic forward push toward subject"),
    DOLLY_ZOOM("Dolly Zoom (Vertigo)", "Simultaneous zoom and track back for psychological dread"),
    ORBIT_360("Orbit", "Smooth arc rotation around focal character"),
    CRANE_DOWN("Crane Down", "High angle descending to reveal environmental scale"),
    PAN_TRACKING("Tracking Pan", "Horizontal camera motion following character movement"),
    HANDHELD_SHAKY("Handheld", "Realistic subtle camera tremor for tension and urgency"),
    STATIC_FIXED("Static Master", "Locked camera with deep depth of field")
}

data class CharacterBibleEntry(
    val name: String,
    val role: String,
    val visualAppearance: String,
    val costume: String,
    val personalityTrait: String,
    val consistencyTokens: String
)

data class EnvironmentBibleEntry(
    val locationName: String,
    val timeOfDay: String,
    val atmosphericConditions: String,
    val lightingSetup: String,
    val colorPaletteKeywords: String,
    val spatialProps: String
)

data class ContinuityCheckResult(
    val passed: Boolean,
    val scorePercent: Int,
    val issuesFound: List<String>,
    val recommendations: List<String>
)

data class PipelineStage(
    val stepNumber: Int,
    val name: String,
    val description: String,
    val isCompleted: Boolean = false,
    val isCurrent: Boolean = false,
    val details: String = ""
)
