package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.PipelineProgressScreen
import com.example.ui.screens.PlayerTheaterScreen
import com.example.ui.screens.StudioCreateScreen
import com.example.ui.screens.StyleLabScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.StudioBackground
import com.example.ui.theme.StudioGold
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioTextSecondary
import com.example.ui.viewmodel.StudioViewModel

enum class StudioTab(val label: String) {
    STUDIO("Studio"),
    PIPELINE("Pipeline"),
    THEATER("Theater"),
    LIBRARY("Films"),
    STYLE_LAB("Style Lab")
}

class MainActivity : ComponentActivity() {

    private val viewModel: StudioViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppScreen(viewModel: StudioViewModel) {
    var currentTab by remember { mutableStateOf(StudioTab.STUDIO) }

    // Proper back button handling
    BackHandler(enabled = currentTab != StudioTab.STUDIO) {
        currentTab = StudioTab.STUDIO
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(StudioBackground),
        containerColor = StudioBackground,
        bottomBar = {
            NavigationBar(
                containerColor = StudioSurface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = currentTab == StudioTab.STUDIO,
                    onClick = { currentTab = StudioTab.STUDIO },
                    icon = { Icon(Icons.Default.Videocam, contentDescription = "Studio") },
                    label = { Text("Studio", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = StudioGold,
                        indicatorColor = StudioGold,
                        unselectedIconColor = StudioTextSecondary,
                        unselectedTextColor = StudioTextSecondary
                    ),
                    modifier = Modifier.testTag("nav_studio")
                )

                NavigationBarItem(
                    selected = currentTab == StudioTab.PIPELINE,
                    onClick = { currentTab = StudioTab.PIPELINE },
                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "Pipeline") },
                    label = { Text("Pipeline", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = StudioGold,
                        indicatorColor = StudioGold,
                        unselectedIconColor = StudioTextSecondary,
                        unselectedTextColor = StudioTextSecondary
                    ),
                    modifier = Modifier.testTag("nav_pipeline")
                )

                NavigationBarItem(
                    selected = currentTab == StudioTab.THEATER,
                    onClick = { currentTab = StudioTab.THEATER },
                    icon = { Icon(Icons.Default.Movie, contentDescription = "Theater") },
                    label = { Text("Theater", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = StudioGold,
                        indicatorColor = StudioGold,
                        unselectedIconColor = StudioTextSecondary,
                        unselectedTextColor = StudioTextSecondary
                    ),
                    modifier = Modifier.testTag("nav_theater")
                )

                NavigationBarItem(
                    selected = currentTab == StudioTab.LIBRARY,
                    onClick = { currentTab = StudioTab.LIBRARY },
                    icon = { Icon(Icons.Default.VideoLibrary, contentDescription = "Films") },
                    label = { Text("Films", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = StudioGold,
                        indicatorColor = StudioGold,
                        unselectedIconColor = StudioTextSecondary,
                        unselectedTextColor = StudioTextSecondary
                    ),
                    modifier = Modifier.testTag("nav_films")
                )

                NavigationBarItem(
                    selected = currentTab == StudioTab.STYLE_LAB,
                    onClick = { currentTab = StudioTab.STYLE_LAB },
                    icon = { Icon(Icons.Default.Palette, contentDescription = "Style Lab") },
                    label = { Text("Style Lab", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = StudioGold,
                        indicatorColor = StudioGold,
                        unselectedIconColor = StudioTextSecondary,
                        unselectedTextColor = StudioTextSecondary
                    ),
                    modifier = Modifier.testTag("nav_style_lab")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                StudioTab.STUDIO -> StudioCreateScreen(
                    viewModel = viewModel,
                    onNavigateToPipeline = { currentTab = StudioTab.PIPELINE },
                    onNavigateToTheater = { currentTab = StudioTab.THEATER }
                )
                StudioTab.PIPELINE -> PipelineProgressScreen(
                    viewModel = viewModel,
                    onNavigateBack = { currentTab = StudioTab.STUDIO },
                    onNavigateToPlayer = { currentTab = StudioTab.THEATER }
                )
                StudioTab.THEATER -> PlayerTheaterScreen(
                    viewModel = viewModel,
                    onNavigateBack = { currentTab = StudioTab.STUDIO }
                )
                StudioTab.LIBRARY -> LibraryScreen(
                    viewModel = viewModel,
                    onPlayProject = { projectId ->
                        viewModel.loadProjectForPlayback(projectId)
                        currentTab = StudioTab.THEATER
                    },
                    onNavigateToCreate = { currentTab = StudioTab.STUDIO }
                )
                StudioTab.STYLE_LAB -> StyleLabScreen(
                    viewModel = viewModel,
                    onNavigateToCreate = { currentTab = StudioTab.STUDIO }
                )
            }
        }
    }
}
