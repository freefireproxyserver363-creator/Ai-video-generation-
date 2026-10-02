package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val prompt: String,
    val generationMode: String, // QUICK, CINEMATIC, STORY_MOVIE
    val inputMode: String, // TEXT_PROMPT, IMAGE_REFERENCE, STORY_SCRIPT
    val primaryStyleId: String,
    val secondaryStyleId: String?,
    val customStylePrompt: String?,
    val styleStrength: Int, // 0, 25, 50, 75, 100
    val durationSeconds: Int,
    val aspectRatio: String, // 16:9, 9:16, etc.
    val status: String, // DRAFT, PLANNING, GENERATING, COMPLETED, FAILED
    val videoFilePath: String?,
    val coverImagePath: String?,
    val referenceImagePath: String?,
    val characterBibleJson: String?,
    val environmentBibleJson: String?,
    val narrationEnabled: Boolean = true,
    val cinematicAudioScore: String = "Orchestral Dramatic",
    val totalScenes: Int = 1,
    val completedScenes: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
