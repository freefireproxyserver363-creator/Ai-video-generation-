package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.ProjectEntity
import com.example.data.db.SceneEntity
import com.example.data.model.AspectRatio
import com.example.data.model.GenerationMode
import com.example.data.model.InputMode
import com.example.data.model.VisualStyle
import com.example.data.repository.GenerationState
import com.example.data.repository.VideoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StudioViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = VideoRepository(application)

    val generationState: StateFlow<GenerationState> = repository.generationState

    val allProjects: StateFlow<List<ProjectEntity>> = repository.allProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Creation State
    private val _prompt = MutableStateFlow("Create a 3-minute horror story about a boy who enters an abandoned school at midnight.")
    val prompt: StateFlow<String> = _prompt.asStateFlow()

    private val _selectedMode = MutableStateFlow(GenerationMode.STORY_MOVIE)
    val selectedMode: StateFlow<GenerationMode> = _selectedMode.asStateFlow()

    private val _inputMode = MutableStateFlow(InputMode.TEXT_PROMPT)
    val inputMode: StateFlow<InputMode> = _inputMode.asStateFlow()

    private val _primaryStyle = MutableStateFlow(VisualStyle.ANIME)
    val primaryStyle: StateFlow<VisualStyle> = _primaryStyle.asStateFlow()

    private val _secondaryStyle = MutableStateFlow<VisualStyle?>(VisualStyle.HORROR)
    val secondaryStyle: StateFlow<VisualStyle?> = _secondaryStyle.asStateFlow()

    private val _customStyleText = MutableStateFlow("")
    val customStyleText: StateFlow<String> = _customStyleText.asStateFlow()

    private val _styleStrength = MutableStateFlow(75) // 0, 25, 50, 75, 100
    val styleStrength: StateFlow<Int> = _styleStrength.asStateFlow()

    private val _aspectRatio = MutableStateFlow(AspectRatio.RATIO_16_9)
    val aspectRatio: StateFlow<AspectRatio> = _aspectRatio.asStateFlow()

    private val _durationMinutes = MutableStateFlow(3.0f)
    val durationMinutes: StateFlow<Float> = _durationMinutes.asStateFlow()

    private val _referenceBitmap = MutableStateFlow<Bitmap?>(null)
    val referenceBitmap: StateFlow<Bitmap?> = _referenceBitmap.asStateFlow()

    // Active project for playback
    private val _activeProject = MutableStateFlow<ProjectEntity?>(null)
    val activeProject: StateFlow<ProjectEntity?> = _activeProject.asStateFlow()

    private val _activeScenes = MutableStateFlow<List<SceneEntity>>(emptyList())
    val activeScenes: StateFlow<List<SceneEntity>> = _activeScenes.asStateFlow()

    fun setPrompt(text: String) {
        _prompt.value = text
    }

    fun setMode(mode: GenerationMode) {
        _selectedMode.value = mode
        when (mode) {
            GenerationMode.QUICK -> _durationMinutes.value = 0.15f // ~10s
            GenerationMode.CINEMATIC -> _durationMinutes.value = 0.5f // 30s
            GenerationMode.STORY_MOVIE -> _durationMinutes.value = 3.0f // 3 mins
        }
    }

    fun setInputMode(mode: InputMode) {
        _inputMode.value = mode
    }

    fun setPrimaryStyle(style: VisualStyle) {
        _primaryStyle.value = style
    }

    fun setSecondaryStyle(style: VisualStyle?) {
        _secondaryStyle.value = style
    }

    fun setCustomStyleText(text: String) {
        _customStyleText.value = text
    }

    fun setStyleStrength(strength: Int) {
        _styleStrength.value = strength
    }

    fun setAspectRatio(ratio: AspectRatio) {
        _aspectRatio.value = ratio
    }

    fun setDurationMinutes(minutes: Float) {
        _durationMinutes.value = minutes
    }

    fun setReferenceBitmap(bitmap: Bitmap?) {
        _referenceBitmap.value = bitmap
    }

    fun applyPresetCombination(primary: VisualStyle, secondary: VisualStyle?) {
        _primaryStyle.value = primary
        _secondaryStyle.value = secondary
    }

    fun startGeneration(onStarted: () -> Unit) {
        onStarted()
        viewModelScope.launch {
            try {
                val projectId = repository.generateAiVideo(
                    prompt = _prompt.value,
                    inputMode = _inputMode.value,
                    primaryStyle = _primaryStyle.value,
                    secondaryStyle = _secondaryStyle.value,
                    customStyleText = if (_primaryStyle.value == VisualStyle.CUSTOM_STYLE) _customStyleText.value else null,
                    styleStrength = _styleStrength.value,
                    mode = _selectedMode.value,
                    aspectRatio = _aspectRatio.value,
                    durationMinutes = _durationMinutes.value,
                    referenceBitmap = _referenceBitmap.value
                )
                loadProjectForPlayback(projectId)
            } catch (e: Exception) {
                // Handled in repository state
            }
        }
    }

    fun loadProjectForPlayback(projectId: Long) {
        viewModelScope.launch {
            repository.getProject(projectId).collect { proj ->
                _activeProject.value = proj
            }
        }
        viewModelScope.launch {
            repository.getScenes(projectId).collect { scenes ->
                _activeScenes.value = scenes
            }
        }
    }

    fun deleteProject(projectId: Long) {
        viewModelScope.launch {
            repository.deleteProject(projectId)
            if (_activeProject.value?.id == projectId) {
                _activeProject.value = null
                _activeScenes.value = emptyList()
            }
        }
    }

    fun resetGenerationState() {
        repository.resetState()
    }
}
