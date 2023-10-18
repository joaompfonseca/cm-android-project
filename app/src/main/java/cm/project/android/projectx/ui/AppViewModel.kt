package cm.project.android.projectx.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cm.project.android.projectx.db.entities.POI
import cm.project.android.projectx.db.repositories.POIRepository
import kotlinx.coroutines.launch

class AppViewModel : ViewModel() {

    val poiRepository = POIRepository()
    var poiList by mutableStateOf(listOf<POI>())
        private set

    init {
        getPOIs()
    }

    fun getPOIs() {
        viewModelScope.launch {
            poiList = poiRepository.getAllPOIs()
        }
    }
}
