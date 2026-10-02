package com.example.data.model

import androidx.compose.ui.graphics.Color

enum class VisualStyle(
    val id: String,
    val displayName: String,
    val subtitle: String,
    val description: String,
    val keyFeatures: List<String>,
    val primaryColor: Long,
    val secondaryColor: Long,
    val iconName: String
) {
    REALISTIC(
        id = "realistic",
        displayName = "Realistic AI Video",
        subtitle = "Photorealistic & Natural",
        description = "Photorealistic human rendering, natural facial expressions, real physics, cinematic lighting, and authentic camera movement.",
        keyFeatures = listOf("Photorealistic", "Natural expressions", "Real physics", "Cinematic lighting", "Natural camera movement"),
        primaryColor = 0xFF4A6572,
        secondaryColor = 0xFF344955,
        iconName = "camera"
    ),
    ANIME(
        id = "anime",
        displayName = "Anime",
        subtitle = "Japanese Animation Aesthetic",
        description = "2D anime characters, anime backgrounds, dynamic high-octane action, dramatic facial expressions, and cel-shaded lighting.",
        keyFeatures = listOf("2D anime characters", "Dynamic action", "Expressive eyes", "Cel shading", "Anime cinematic lighting"),
        primaryColor = 0xFFFF4081,
        secondaryColor = 0xFF7C4DFF,
        iconName = "auto_awesome"
    ),
    CARTOON(
        id = "cartoon",
        displayName = "Cartoon",
        subtitle = "Stylized 2D & 3D Toon",
        description = "Stylized animated characters, expressive squash-and-stretch animation, colorful vibrant environments, and whimsical camera work.",
        keyFeatures = listOf("Expressive animation", "Vibrant colors", "Stylized characters", "Playful motion", "Cartoon camera"),
        primaryColor = 0xFFFF9100,
        secondaryColor = 0xFFFFD700,
        iconName = "palette"
    ),
    HORROR(
        id = "horror",
        displayName = "Horror",
        subtitle = "Dark & Psychological",
        description = "Dark atmospheric dread, psychological & supernatural suspense, rolling fog, stark shadows, and chilling cinematography.",
        keyFeatures = listOf("Dark horror atmosphere", "Suspenseful camera", "Volumetric fog", "Deep shadows", "Atmospheric lighting"),
        primaryColor = 0xFFB71C1C,
        secondaryColor = 0xFF212121,
        iconName = "nightlight"
    ),
    CINEMATIC(
        id = "cinematic",
        displayName = "Cinematic",
        subtitle = "Hollywood Panavision Film",
        description = "Film-quality composition, shallow depth of field, anamorphic lens flares, dynamic tracking dolly shots, and filmic color grading.",
        keyFeatures = listOf("Hollywood composition", "Depth of field", "Tracking dolly shots", "Dramatic lighting", "Filmic color grade"),
        primaryColor = 0xFFD4AF37,
        secondaryColor = 0xFF1A1A24,
        iconName = "movie"
    ),
    DRAWING(
        id = "drawing",
        displayName = "Drawing / Hand-Drawn",
        subtitle = "Traditional Animated Sketch",
        description = "Frame-to-frame hand-drawn pencil & ink animation where characters move, cameras pan, backgrounds shift, and scenes transition fluidly.",
        keyFeatures = listOf("Hand-drawn animation", "Frame-to-frame movement", "Pencil & ink texture", "Animated characters", "Moving backgrounds"),
        primaryColor = 0xFF8D6E63,
        secondaryColor = 0xFF4E342E,
        iconName = "edit"
    ),
    WATERCOLOR(
        id = "watercolor",
        displayName = "Watercolor",
        subtitle = "Animated Fluid Pigment",
        description = "Hand-painted aesthetic with animated watercolor bleeds, soft paper textures, and gentle organic brush movement.",
        keyFeatures = listOf("Watercolor pigment bleed", "Soft brush textures", "Hand-painted feel", "Organic motion", "Vibrant washes"),
        primaryColor = 0xFF00ACC1,
        secondaryColor = 0xFF80DEEA,
        iconName = "brush"
    ),
    COMIC_MANGA(
        id = "comic_manga",
        displayName = "Comic / Manga",
        subtitle = "Graphic Novel Paneling",
        description = "Manga ink outlines, dynamic halftone screen-tones, dramatic paneling, action lines, and stylized graphic character poses.",
        keyFeatures = listOf("Manga screentones", "Bold ink lines", "Dynamic action panels", "Graphic styling", "Dramatic framing"),
        primaryColor = 0xFF37474F,
        secondaryColor = 0xFFECEFF1,
        iconName = "menu_book"
    ),
    THREE_D_ANIMATION(
        id = "three_d_animation",
        displayName = "3D Animation",
        subtitle = "Studio Feature Animation",
        description = "High-fidelity 3D characters and environments, ray-traced lighting, cinematic character rigging, and animated-film appearance.",
        keyFeatures = listOf("High-poly 3D models", "Realistic shaders", "Cinematic arcs", "Dynamic lighting", "Film quality 3D"),
        primaryColor = 0xFF2979FF,
        secondaryColor = 0xFF651FFF,
        iconName = "view_in_ar"
    ),
    FANTASY(
        id = "fantasy",
        displayName = "Fantasy",
        subtitle = "Magical & Mythic",
        description = "Magical kingdoms, mythical beasts, glowing ethereal spells, ancient citadels, and majestic fantasy lighting.",
        keyFeatures = listOf("Magical particle effects", "Mythic environments", "Ancient kingdoms", "Glowing runes", "Ethereal lighting"),
        primaryColor = 0xFF9C27B0,
        secondaryColor = 0xFFE040FB,
        iconName = "flare"
    ),
    SCI_FI(
        id = "sci_fi",
        displayName = "Sci-Fi",
        subtitle = "Futuristic & Cyberpunk",
        description = "Futuristic megacities, cybernetic robots, starships, neon holograms, and sleek high-tech cinematography.",
        keyFeatures = listOf("Futuristic megacities", "Hologram HUDs", "Cyberpunk neon", "Advanced robotics", "Sci-fi lighting"),
        primaryColor = 0xFF00E5FF,
        secondaryColor = 0xFF1DE9B6,
        iconName = "rocket_launch"
    ),
    CLAY_STOP_MOTION(
        id = "clay_stop_motion",
        displayName = "Clay / Stop Motion",
        subtitle = "Tactile Handcrafted 12fps",
        description = "Authentic clay textures with fingerprint details, stop-motion frame cadence, physical miniature lighting, and tactile charm.",
        keyFeatures = listOf("Clay character textures", "Stop-motion cadence", "Miniature depth of field", "Handmade feel", "Tactile physics"),
        primaryColor = 0xFFD84315,
        secondaryColor = 0xFFBF360C,
        iconName = "interests"
    ),
    PIXEL_ART(
        id = "pixel_art",
        displayName = "Pixel Art",
        subtitle = "Retro 16-Bit Motion",
        description = "Animated pixel-art sprites, parallax scrolling backgrounds, nostalgic color palettes, and retro-game cinematography.",
        keyFeatures = listOf("Pixel-perfect sprites", "Parallax backgrounds", "Animated movement", "Retro aesthetic", "Chiptune pacing"),
        primaryColor = 0xFF00E676,
        secondaryColor = 0xFF1B5E20,
        iconName = "grid_view"
    ),
    DARK_FANTASY(
        id = "dark_fantasy",
        displayName = "Dark Fantasy",
        subtitle = "Grim & Eldritch Ruins",
        description = "Grimdark ruined castles, eldritch monsters, ash particles, cursed sorcery, and dramatic moody chiaroscuro lighting.",
        keyFeatures = listOf("Dark fantasy ruins", "Cursed magic", "Eldritch creatures", "Ash embers", "Chiaroscuro lighting"),
        primaryColor = 0xFF4A148C,
        secondaryColor = 0xFF12005E,
        iconName = "shield"
    ),
    CUSTOM_STYLE(
        id = "custom_style",
        displayName = "Custom Style",
        subtitle = "User-Defined Aesthetics",
        description = "Enter your own aesthetic prompt. AI translates the custom description into comprehensive video generation instructions.",
        keyFeatures = listOf("Custom prompt translation", "Hybrid styles", "Unique artistic direction", "Prompt tuning"),
        primaryColor = 0xFFE91E63,
        secondaryColor = 0xFFFF6090,
        iconName = "tune"
    );

    companion object {
        fun fromId(id: String): VisualStyle =
            entries.find { it.id.equals(id, ignoreCase = true) } ?: REALISTIC

        val popularCombinations = listOf(
            Pair(ANIME, HORROR) to "Anime + Horror (Midnight Academy)",
            Pair(ANIME, CINEMATIC) to "Anime + Cinematic (Makoto Shinkai Film)",
            Pair(CARTOON, HORROR) to "Cartoon + Horror (Eerie Animated Mystery)",
            Pair(DRAWING, HORROR) to "Drawing + Horror (Haunted Sketchbook)",
            Pair(COMIC_MANGA, CINEMATIC) to "Manga + Cinematic (Noir Graphic Novel)",
            Pair(THREE_D_ANIMATION, FANTASY) to "3D + Fantasy (Epic Animated Quest)",
            Pair(REALISTIC, HORROR) to "Realistic + Horror (Found Footage Dread)",
            Pair(ANIME, DARK_FANTASY) to "Anime + Dark Fantasy (Berserk / Soulslike)",
            Pair(WATERCOLOR, FANTASY) to "Watercolor + Fantasy (Enchanted Forest Tale)",
            Pair(SCI_FI, CINEMATIC) to "Sci-Fi + Cinematic (Blade Runner Megacity)"
        )
    }
}
