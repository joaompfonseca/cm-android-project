package cm.project.android.projectx.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.pm.PackageManager
import android.os.Looper
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cm.project.android.projectx.MainActivity
import cm.project.android.projectx.db.entities.POI
import cm.project.android.projectx.db.repositories.POIRepository
import cm.project.android.projectx.network.AppApi
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationServices.getFusedLocationProviderClient
import com.google.android.gms.location.Priority
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

    var center by mutableStateOf(GeoPoint(40.64427, -8.64554)) // Aveiro
        private set

    var location by mutableStateOf<GeoPoint?>(null)
        private set


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
