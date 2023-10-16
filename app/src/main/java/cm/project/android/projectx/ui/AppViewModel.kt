package cm.project.android.projectx.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cm.project.android.projectx.network.AppApi
import kotlinx.coroutines.launch
import java.io.IOException

sealed interface AppUiState {
    data class Map(val pointsOfInterest: List<String>) : AppUiState
    object Error : AppUiState
}

class AppViewModel : ViewModel() {
    var appUiState: AppUiState by mutableStateOf(AppUiState.Map(emptyList())) // Call API
        private set

    init {
        // getPointsOfInterest()
    }

    fun getPointsOfInterest() {
        viewModelScope.launch {
            appUiState = try {
                val pointsOfInterest = AppApi.retrofitService.getPOIs()
                AppUiState.Map(pointsOfInterest)
            } catch (e: IOException) {
                AppUiState.Error
            }
        }
    }
}
