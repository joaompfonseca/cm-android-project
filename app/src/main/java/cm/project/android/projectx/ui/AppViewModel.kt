package cm.project.android.projectx.ui

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.om.OverlayManager
import android.net.Uri
import android.os.Looper
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cm.project.android.projectx.db.entities.POI
import cm.project.android.projectx.db.repositories.POIRepository
import cm.project.android.projectx.network.AppApi
import cm.project.android.projectx.network.entities.GeocodeDto
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices.getFusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.utsman.osmandcompose.CameraProperty
import com.utsman.osmandcompose.CameraState
import com.utsman.osmandcompose.MarkerState
import com.utsman.osmandcompose.OverlayManagerState
import com.utsman.osmandcompose.rememberOverlayManagerState
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import java.util.concurrent.TimeUnit

@SuppressLint("MissingPermission")
class AppViewModel(application: Application) : AndroidViewModel(application) {

    val fusedLocationProviderClient: FusedLocationProviderClient

    val locationRequest: LocationRequest

    val locationCallback: LocationCallback

    val poiRepository = POIRepository()

    var poiList by mutableStateOf(listOf<POI>())
        private set

    var camera by mutableStateOf(
        CameraState(
            CameraProperty(
                geoPoint = GeoPoint(40.64427, -8.64554),
                zoom = 14.0
            )
        )
    )
        private set

    var location by mutableStateOf<GeoPoint?>(null)
        private set

    var route by mutableStateOf(false)

    init {

        //
        // LOCATION
        //

        // Main class for receiving location updates
        fusedLocationProviderClient = getFusedLocationProviderClient(application.applicationContext)
        // Requirements for the location updates
        locationRequest = LocationRequest
            .Builder(Priority.PRIORITY_HIGH_ACCURACY, TimeUnit.SECONDS.toMillis(10))
            .setMinUpdateIntervalMillis(TimeUnit.SECONDS.toMillis(5))
            .setMaxUpdateDelayMillis(TimeUnit.MINUTES.toMillis(2))
            .build()
        // Called when FusedLocationProviderClient has a new Location
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(results: LocationResult) {
                val it = results.lastLocation ?: return
                location = GeoPoint(it.latitude, it.longitude)
            }
        }
        // Initialize location
        fusedLocationProviderClient.lastLocation.addOnSuccessListener {
            if (it != null) {
                location = GeoPoint(it.latitude, it.longitude)
                gotoUserLocation() // Center map on user location
            }
        }
        // Update location if there are changes
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )

        //
        // POIs
        //

        getPOIs()
    }

    fun getPOIs() {
        viewModelScope.launch {
            poiList = poiRepository.getAllPOIs()
        }
    }

    fun gotoUserLocation() {
        viewModelScope.launch {
            if (location != null) {
                camera = CameraState(
                    CameraProperty(
                        geoPoint = location as GeoPoint,
                        zoom = 18.0
                    )
                )
            }
        }
    }

    fun getSearch(query: String) {
        viewModelScope.launch {
            Log.e("AppViewModel", "Query: $query")
            var res: HashMap<String, List<GeocodeDto>>? = null
            val isMatch = Regex("^(-?\\d+(\\.\\d+)?),\\s*(-?\\d+(\\.\\d+)?)\$").matches(query)
            if (isMatch) {
                Log.e("AppViewModel", "Reverse geocoding")
                res = AppApi.revGeocodeService.getRevGeocode(query)
            } else {
                Log.e("AppViewModel", "Geocoding")
                res =  AppApi.geocodeService.getGeocode(query)
            }
            // Consider only the first result
            if (res["items"]?.isNotEmpty() == true) {
                val item = res["items"]!![0]
                camera = CameraState(
                    CameraProperty(
                        geoPoint = GeoPoint(item.position.lat, item.position.lng),
                        zoom = 14.0
                    )
                )
            }
        }
    }

    fun getRoute(query: String, context: Context){
        viewModelScope.launch {
            Log.e("AppViewModel", "Query: $query")
            var res: HashMap<String, List<GeocodeDto>>? = null
            val isMatch = Regex("^(-?\\d+(\\.\\d+)?),\\s*(-?\\d+(\\.\\d+)?)\$").matches(query)
            if (isMatch) {
                Log.e("AppViewModel", "Reverse geocoding")
                res = AppApi.revGeocodeService.getRevGeocode(query)
            } else {
                Log.e("AppViewModel", "Geocoding")
                res =  AppApi.geocodeService.getGeocode(query)
            }
            // Consider only the first result
            if (res["items"]?.isNotEmpty() == true) {
                val lat = res["items"]!![0].position.lat
                val lng = res["items"]!![0].position.lng
                val gmmIntentUri = Uri.parse("google.navigation:q=$lat,$lng&mode=b")
                val mapIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, gmmIntentUri)
                mapIntent.setPackage("com.google.android.apps.maps")
                startActivity(context, mapIntent, null)
            }
        }
    }

    fun addPOI(poi: POI) {
        viewModelScope.launch {
            poiRepository.savePOI(poi)
        }
    }
}
