package cm.project.android.projectx.ui

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cm.project.android.projectx.network.AppApi
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import java.io.IOException

sealed interface AppUiState {
    data class Map(val center: GeoPoint) : AppUiState
    object Error : AppUiState
}

class AppViewModel : ViewModel() {
    var appUiState: AppUiState by mutableStateOf(AppUiState.Map(GeoPoint(40.64427, -8.64554))) // Call API
        private set

    private val API_KEY = "HlogdPHLwPTb9a-4u-7ep-WFPOaUOZVjYx7_--mEJfw"
    private val i = "countryCode:PRT"

    init {
        // getPointsOfInterest()
    }

    fun getLatLng(q: String) {
        viewModelScope.launch {
            appUiState = try {
                val data = AppApi.retrofitService.getLatLng(q, i, API_KEY)
                val lat = data["items"]?.get(0)?.position?.get("lat")
                val lng = data["items"]?.get(0)?.position?.get("lng")
                AppUiState.Map(GeoPoint(lat!!, lng!!))
            } catch (e: IOException) {
                AppUiState.Error
            }
        }
    }
}
