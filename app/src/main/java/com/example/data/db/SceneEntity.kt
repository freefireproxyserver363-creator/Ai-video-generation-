package com.example.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "scenes",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("projectId")]
)
data class SceneEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val projectId: Long,
    val sceneIndex: Int,
    val title: String,
    val narrativeScript: String,
    val visualPrompt: String,
    val cameraMovement: String,
    val lightingSetup: String,
    val actionDescription: String,
    val durationSeconds: Int,
    val videoClipPath: String?,
    val continuityNotes: String?,
    val status: String // PENDING, GENERATING, READY, FAILED
)
