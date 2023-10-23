package cm.project.android.projectx.ui

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.net.Uri
import android.os.Looper
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cm.project.android.projectx.db.entities.POI
import cm.project.android.projectx.db.entities.Rating
import cm.project.android.projectx.db.repositories.POIRepository
import cm.project.android.projectx.network.AppApi
import cm.project.android.projectx.network.entities.GeocodeDto
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices.getFusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.utsman.osmandcompose.CameraProperty
import com.utsman.osmandcompose.CameraState
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import java.util.concurrent.TimeUnit

@SuppressLint("MissingPermission")
class AppViewModel(application: Application) : AndroidViewModel(application) {

    val fusedLocationProviderClient: FusedLocationProviderClient

    val locationRequest: LocationRequest

    val locationCallback: LocationCallback

    val poiRepository = POIRepository()

    val user = FirebaseAuth.getInstance().currentUser

    var poiList by mutableStateOf<List<POI>>(emptyList())
        private set

    var camera by mutableStateOf<CameraState?>(null)
        private set

    var location by mutableStateOf<GeoPoint?>(null)
        private set

    var showRoute by mutableStateOf(false)
        private set

    var showDetails by mutableStateOf(false)
        private set

    var selectedPOI by mutableStateOf<POI?>(null)
        private set

    init {

        //
        // LOCATION
        //

        // Main class for receiving location updates
        fusedLocationProviderClient = getFusedLocationProviderClient(application.applicationContext)
        // Requirements for the location updates
        locationRequest = LocationRequest
            .Builder(Priority.PRIORITY_HIGH_ACCURACY, TimeUnit.SECONDS.toMillis(1))
            .setMinUpdateIntervalMillis(TimeUnit.SECONDS.toMillis(1))
            .setMaxUpdateDelayMillis(TimeUnit.SECONDS.toMillis(5))
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

    fun setCamera(latitude: Double, longitude: Double, zoom: Double) {
        camera = CameraState(
            CameraProperty(
                geoPoint = GeoPoint(latitude, longitude),
                zoom = zoom
            )
        )
    }

    fun getPOIs() {
        viewModelScope.launch {
            poiList = poiRepository.getAllPOIs()
        }
    }

    fun gotoUserLocation() {
        viewModelScope.launch {
            if (location != null) {
                setCamera(location!!.latitude, location!!.longitude, 18.0)
            }
        }
    }

    fun gotoSearch(query: String) {
        viewModelScope.launch {
            if (query.isEmpty()) {
                return@launch
            }
            Log.e("AppViewModel", "Query: $query")
            var res: HashMap<String, List<GeocodeDto>>? = null
            val isMatch = Regex("^(-?\\d+(\\.\\d+)?),\\s*(-?\\d+(\\.\\d+)?)\$").matches(query)
            if (isMatch) {
                Log.e("AppViewModel", "Reverse geocoding")
                try {
                    res = AppApi.revGeocodeService.getRevGeocode(query)
                } catch (e: Exception) {
                    Log.e("AppViewModel", "Error: ${e.message}")
                }
            } else {
                Log.e("AppViewModel", "Geocoding")
                try {
                    res = AppApi.geocodeService.getGeocode(query)
                } catch (e: Exception) {
                    Log.e("AppViewModel", "Error: ${e.message}")
                }
            }
            // Consider only the first result
            if (res != null && res["items"]?.isNotEmpty() == true) {
                val item = res["items"]!![0]
                setCamera(item.position.lat, item.position.lng, 14.0)
            }
        }
    }

    fun gotoRoute(query: String, context: Context) {
        viewModelScope.launch {
            if (query.isEmpty()) {
                return@launch
            }
            Log.e("AppViewModel", "Query: $query")
            var res: HashMap<String, List<GeocodeDto>>? = null
            val isMatch = Regex("^(-?\\d+(\\.\\d+)?),\\s*(-?\\d+(\\.\\d+)?)\$").matches(query)
            if (isMatch) {
                Log.e("AppViewModel", "Reverse geocoding")
                res = AppApi.revGeocodeService.getRevGeocode(query)
            } else {
                Log.e("AppViewModel", "Geocoding")
                res = AppApi.geocodeService.getGeocode(query)
            }
            // Consider only the first result
            if (res["items"]?.isNotEmpty() == true) {
                val lat = res["items"]!![0].position.lat
                val lng = res["items"]!![0].position.lng
                val gmmIntentUri = Uri.parse("google.navigation:q=$lat,$lng&mode=b")
                val mapIntent =
                    android.content.Intent(android.content.Intent.ACTION_VIEW, gmmIntentUri)
                mapIntent.setPackage("com.google.android.apps.maps")
                startActivity(context, mapIntent, null)
            }
        }
    }

    fun addPOI(poi: POI, imageUri: Uri) {
        viewModelScope.launch {
            val savedPOI = poiRepository.savePOI(poi, imageUri)
            poiList = poiList.toMutableList().apply { add(savedPOI) }
        }
    }

    fun ratePOI(poi: POI, rating: Rating) {
        viewModelScope.launch {
            val ratings = poi.ratings.toMutableList()
            ratings.removeIf({ it.user == rating.user })
            ratings.add(rating)
            val ratedPOI = poi.copy(ratings = ratings)
            poiRepository.updatePOI(ratedPOI)

            poiList = poiList.indexOf(poi).let {
                poiList.toMutableList().apply { set(it, ratedPOI) }
            }
            if (poi.hashCode() == selectedPOI?.hashCode()) {
                selectedPOI = ratedPOI
            }
        }
    }

    fun showRoute() {
        viewModelScope.launch {
            showRoute = true
        }
    }

    fun hideRoute() {
        viewModelScope.launch {
            showRoute = false
        }
    }

    fun showDetails(poi: POI) {
        viewModelScope.launch {
            selectedPOI = poi
            showDetails = true
            setCamera(poi.latitude - 0.0005, poi.longitude, 18.0)
        }
    }

    fun hideDetails() {
        viewModelScope.launch {
            showDetails = false
            setCamera(selectedPOI!!.latitude, selectedPOI!!.longitude, 18.0)
        }
    }
}
