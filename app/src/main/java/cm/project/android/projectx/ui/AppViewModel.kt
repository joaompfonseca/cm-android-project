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
import androidx.lifecycle.viewmodel.compose.viewModel
import cm.project.android.projectx.db.entities.POI
import cm.project.android.projectx.db.entities.Point
import cm.project.android.projectx.db.entities.Rating
import cm.project.android.projectx.db.entities.Route
import cm.project.android.projectx.db.entities.User
import cm.project.android.projectx.db.repositories.POIRepository
import cm.project.android.projectx.db.repositories.RouteRepository
import cm.project.android.projectx.db.repositories.UserRepository
import cm.project.android.projectx.network.AppApi
import cm.project.android.projectx.network.entities.GeocodeDto
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices.getFusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.firebase.auth.FirebaseAuth
import com.utsman.osmandcompose.CameraProperty
import com.utsman.osmandcompose.CameraState
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import java.time.Instant
import java.util.concurrent.TimeUnit
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@SuppressLint("MissingPermission", "MutableCollectionMutableState")
class AppViewModel(application: Application) : AndroidViewModel(application) {

    val AVEIRO = GeoPoint(40.6405, -8.6538)

    val fusedLocationProviderClient: FusedLocationProviderClient

    val locationRequest: LocationRequest

    val locationCallback: LocationCallback

    val poiRepository = POIRepository()

    val userRepository = UserRepository()

    val routeRepository = RouteRepository()

    val user = FirebaseAuth.getInstance().currentUser

    var userl by mutableStateOf<User?>(null)
        private set

    var poiList by mutableStateOf<List<POI>>(emptyList())
        private set

    var camera by mutableStateOf(CameraState(CameraProperty(AVEIRO, 14.0)))
        private set

    var location by mutableStateOf<GeoPoint?>(null)
        private set

    var routes by mutableStateOf<List<Route>>(emptyList())
        private set

    var allRoutes by mutableStateOf<HashMap<String, List<Route>>>(HashMap())
        private set

    var isTrackingLocation by mutableStateOf(false)
        private set

    var routePoints by mutableStateOf<List<Point>>(emptyList())
        private set

    var isDisplayRoute by mutableStateOf(false)
        private set

    var displayRoute by mutableStateOf<Route?>(null)
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
            .Builder(Priority.PRIORITY_HIGH_ACCURACY, TimeUnit.SECONDS.toMillis(0))
            .build()
        // Called when FusedLocationProviderClient has a new Location
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(results: LocationResult) {
                val it = results.lastLocation ?: return
                location = GeoPoint(it.latitude, it.longitude)
                if (isTrackingLocation) {
                    addRoutePoint(it.latitude, it.longitude, Instant.now().toEpochMilli())
                    gotoUserLocation() // Center map on user location
                }
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
        user?.let { getUser(it.uid) }
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

    fun getUser(id: String) {
        viewModelScope.launch {
            userl = userRepository.getUser(id)
        }
    }

    fun gotoUserLocation() {
        viewModelScope.launch {
            if (location != null) {
                setCamera(location!!.latitude, location!!.longitude, 18.0)
            }
        }
    }

    fun startTrackingUserLocation() {
        viewModelScope.launch {
            isTrackingLocation = true
            routePoints = emptyList()
        }
    }

    fun stopTrackingUserLocation() {
        viewModelScope.launch {
            isTrackingLocation = false
            if (userl != null && routePoints.isNotEmpty()) {
                val originCoordinates =
                    "${routePoints.first().latitude},${routePoints.first().longitude}"
                val destinationCoordinates =
                    "${routePoints.last().latitude},${routePoints.last().longitude}"

                val origin =
                    AppApi.revGeocodeService.getRevGeocode(originCoordinates)["items"]?.get(0)?.title
                        ?: "Unknown origin"
                val destination =
                    AppApi.revGeocodeService.getRevGeocode(destinationCoordinates)["items"]?.get(0)?.title
                        ?: "Unknown destination"

                // Get average speed based on coordinates and timestamp of a list of points
                fun totalDistance(points: List<Point>): Double {
                    fun degreesToRadians(degrees: Double): Double {
                        return degrees * PI / 180
                    }

                    fun distanceInMetersBetweenEarthCoordinates(
                        pointA: Point,
                        pointB: Point
                    ): Double {
                        val earthRadiusMeters = 6371 * 1000;

                        val dLat = degreesToRadians(pointB.latitude - pointA.latitude);
                        val dLon = degreesToRadians(pointB.longitude - pointA.longitude);

                        val latA = degreesToRadians(pointA.latitude);
                        val latB = degreesToRadians(pointB.latitude);

                        val a = sin(dLat / 2) * sin(dLat / 2) +
                                sin(dLon / 2) * sin(dLon / 2) * cos(latA) * cos(latB);
                        val c = 2 * atan2(sqrt(a), sqrt(1 - a));
                        return earthRadiusMeters * c;
                    }

                    var total = 0.0
                    for (i in 0 until points.size - 1) {
                        total += distanceInMetersBetweenEarthCoordinates(points[i], points[i + 1])
                    }
                    return total
                }

                val totalDistance = totalDistance(routePoints)
                val totalDuration =
                    (routePoints.last().timestamp - routePoints.first().timestamp) / 1000

                val route = Route(
                    origin = origin,
                    destination = destination,
                    totalDistance = totalDistance,
                    totalDuration = totalDuration,
                    routePoints.toMutableList(),
                    userl!!.username
                )
                addRoute(user!!.uid, route)
            }
            routePoints = emptyList()
        }
    }

    fun addRoutePoint(latitude: Double, longitude: Double, timestamp: Long) {
        viewModelScope.launch {
            routePoints = routePoints.toMutableList().apply {
                add(Point(latitude, longitude, timestamp))
            }
        }
    }

    suspend fun getRoutes(uid: String) {
        routes = routeRepository.getAllRoutes(uid)
    }

    suspend fun getAllRoutes() {
        allRoutes = routeRepository.getAllRoutes()
    }

    fun addRoute(uid: String, route: Route) {
        viewModelScope.launch {
            getRoutes(uid)
            routes = routes.toMutableList().apply { add(route) }
            routeRepository.saveRoutes(uid, routes)
        }
    }

    fun displayRoute(route: Route) {
        viewModelScope.launch {
            displayRoute = route
            isDisplayRoute = true
            setCamera(route.points.first().latitude, route.points.first().longitude, 16.0)
        }
    }

    fun clearDisplayRoute() {
        viewModelScope.launch {
            displayRoute = null
            isDisplayRoute = false
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

    fun addUser(user: User, imageUri: Uri) {
        viewModelScope.launch {
            userl = userRepository.saveUser(user, imageUri)
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
