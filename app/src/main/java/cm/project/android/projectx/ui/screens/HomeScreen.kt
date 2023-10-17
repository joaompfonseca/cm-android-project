package cm.project.android.projectx.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cm.project.android.projectx.ui.AppUiState

@Composable
fun HomeScreen(
    appUiState: AppUiState, modifier: Modifier = Modifier
) {
    when (appUiState) {
        is AppUiState.Map -> MapScreen(center = appUiState.center,modifier = modifier.fillMaxSize())
        else -> {}
    }
}