package cm.project.android.projectx.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cm.project.android.projectx.db.entities.POI
import cm.project.android.projectx.db.repositories.POIRepository
import cm.project.android.projectx.network.AppApi
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint

class AppViewModel : ViewModel() {

    val poiRepository = POIRepository()
    var poiList by mutableStateOf(listOf<POI>())
        private set

    var center by mutableStateOf(GeoPoint(40.64427, -8.64554)) // Aveiro
        private set

    init {
        getPOIs()
    }

    fun getPOIs() {
        viewModelScope.launch {
            poiList = poiRepository.getAllPOIs()
        }
    }

    fun getSearchGeocode(query: String) {
        viewModelScope.launch {
            val res = AppApi.geocodeService.getGeocode(query)
            // Consider only the first result
            if (res["items"]?.isNotEmpty() == true) {
                val item = res["items"]!![0]
                center = GeoPoint(item.position.lat, item.position.lng)
            }
        }
    }
}
