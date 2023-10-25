package cm.project.android.projectx.ui.screens

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ExitToApp
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import cm.project.android.projectx.MainActivity
import cm.project.android.projectx.R
import cm.project.android.projectx.db.entities.POI
import cm.project.android.projectx.db.entities.Rating
import cm.project.android.projectx.ui.AppViewModel
import coil.compose.AsyncImage
import com.firebase.ui.auth.AuthUI
import com.utsman.osmandcompose.MapProperties
import com.utsman.osmandcompose.Marker
import com.utsman.osmandcompose.MarkerState
import com.utsman.osmandcompose.OpenStreetMap
import com.utsman.osmandcompose.Polyline
import com.utsman.osmandcompose.PolylineCap
import com.utsman.osmandcompose.ZoomButtonVisibility
import kotlinx.coroutines.delay
import okhttp3.internal.wait
import org.osmdroid.tileprovider.tilesource.ITileSource
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.GeoPoint


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    vm: AppViewModel = viewModel(),
    onAddPOI: () -> Unit,
    onAddUser: () -> Unit,
    showUser: () -> Unit,
    onShowRoutes: () -> Unit
) {

    if (vm.userl == null) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.surfaceVariant,
                trackColor = MaterialTheme.colorScheme.secondary,
                modifier = Modifier
                    .width(64.dp)
                    .align(Alignment.Center)
            )
        }
        return
    }

    val context = LocalContext.current

    val cyclOSM: ITileSource = XYTileSource(
        "CyclOSM", 1, 18, 256, ".png", arrayOf(
            "https://a.tile-cyclosm.openstreetmap.fr/cyclosm/",
            "https://b.tile-cyclosm.openstreetmap.fr/cyclosm/",
            "https://c.tile-cyclosm.openstreetmap.fr/cyclosm/"
        )
    )

    var mapProperties by remember {
        mutableStateOf(
            MapProperties(
                tileSources = cyclOSM,
                zoomButtonVisibility = ZoomButtonVisibility.NEVER
            )
        )
    }

    SideEffect {
        mapProperties = mapProperties
            .copy(isTilesScaledToDpi = true)
    }

    Box(modifier = modifier.fillMaxSize()) {
        OpenStreetMap(
            modifier = Modifier.fillMaxSize(),
            cameraState = vm.camera,
            properties = mapProperties,
            onMapClick = {
                println("on click  -> $it")
            },
            onMapLongClick = {
                println("on long click -> ${it.latitude}, ${it.longitude}")

            },
            onFirstLoadListener = {

            }
        ) {
            vm.poiList.forEach { poi ->
                Marker(
                    state = MarkerState(
                        geoPoint = GeoPoint(poi.latitude, poi.longitude)
                    ),
                    title = poi.name,
                    icon = context.getDrawable(
                        when (poi.type) {
                            "bicycle-parking" -> R.drawable.m_bicycle_parking
                            "bicycle-shop" -> R.drawable.m_bicycle_shop
                            "drinking-water" -> R.drawable.m_drinking_water
                            "toilets" -> R.drawable.m_toilets
                            "bench" -> R.drawable.m_bench
                            else -> R.drawable.m_default
                        }
                    ),
                    onClick = { _ ->
                        vm.showDetails(poi)
                        return@Marker true
                    }
                )
            }
            if (vm.location != null) {
                Marker(
                    icon = context.getDrawable(R.drawable.user_location),
                    state = MarkerState(geoPoint = vm.location!!),
                    onClick = { _ ->
                        vm.gotoUserLocation()
                        return@Marker true
                    }
                )
            }
            //
            // Current tracked route
            //
            Polyline(
                geoPoints = vm.routePoints.map { GeoPoint(it.latitude, it.longitude) },
                color = Color.Green,
                cap = PolylineCap.ROUND,
                width = 15.0f
            )
            //
            // Selected guide route
            //
            if (vm.isDisplayRoute) {
                Polyline(
                    geoPoints = vm.displayRoute!!.points.map {
                        GeoPoint(
                            it.latitude,
                            it.longitude
                        )
                    },
                    color = Color.Red,
                    cap = PolylineCap.ROUND,
                    width = 15.0f
                )
            }
        }
        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.Top,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(20.dp)
                .fillMaxWidth()
        ) {
            Column {
                //
                // Profile Page
                //
                FloatingActionButton(
                    onClick = {
                        if (vm.userl != null) {
                            showUser()
                        } else {
                            onAddUser()
                        }
                    },
                    contentColor = Color.DarkGray,
                    containerColor = Color.Gray,
                    modifier = Modifier
                        .padding(bottom = 20.dp, end = 20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Person,
                        contentDescription = "Profile",
                        modifier = Modifier.size(30.dp)
                    )
                }
                //
                // Routes Page
                //
                FloatingActionButton(
                    onClick = {
                        if (vm.userl != null) {
                            onShowRoutes()
                        } else {
                            onAddUser()
                        }
                    },
                    contentColor = Color.DarkGray,
                    containerColor = Color.Gray,
                    modifier = Modifier
                        .padding(bottom = 20.dp, end = 20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Send,
                        contentDescription = "Routes",
                        modifier = Modifier
                            .size(30.dp)
                            .rotate(-45f)
                            .padding(start = 5.dp)
                    )
                }
                //
                // Logout
                //
                FloatingActionButton(
                    onClick = { logout(context) },
                    contentColor = Color.DarkGray,
                    containerColor = Color.LightGray,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ExitToApp,
                            contentDescription = "Logout",
                            modifier = Modifier.size(30.dp)
                        )
                        Text("Logout")
                    }
                }
            }
            //
            // Search Bar
            //
            SearchBar(
                search = { vm.gotoSearch(it) },
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                //
                // Clear Display Route
                //
                if (vm.isDisplayRoute) {
                    FloatingActionButton(
                        onClick = { vm.clearDisplayRoute() },
                        containerColor = Color.Red
                    ) {
                        Column (
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                    .padding(10.dp)
                        ){
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Clear Displayed Route"
                            )
                            Text("Clear Displayed Route")
                        }
                    }
                }
            }
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                //
                // Add POI
                //
                ExtendedFloatingActionButton(
                    onClick = {
                        if (vm.userl != null) {
                            if (vm.location != null) {
                                onAddPOI()
                            } else {
                                Toast.makeText(
                                    context,
                                    "Please enable location",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            }
                        } else {
                            onAddUser()
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "Add POI"
                        )
                    },
                    text = { Text("POI") }
                )
                //
                // Track / Un-track User
                //
                if (!vm.isTrackingLocation) {
                    LargeFloatingActionButton(
                        onClick = { vm.gotoUserLocation(); vm.startTrackingUserLocation() },
                        shape = CircleShape,
                        containerColor = Color.Green
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Track me!",
                                tint = Color.DarkGray,
                                modifier = Modifier.size(30.dp)
                            )
                            Text(
                                text = "Track me!",
                                color = Color.DarkGray
                            )
                        }
                    }
                } else {
                    LargeFloatingActionButton(
                        onClick = { vm.stopTrackingUserLocation() },
                        shape = CircleShape,
                        containerColor = Color.Red
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Stop",
                                modifier = Modifier.size(30.dp)
                            )
                            Text("Stop")
                        }
                    }
                }
                //
                // Create Route
                //
                ExtendedFloatingActionButton(
                    onClick = { vm.showRoute() },
                    icon = {
                        Icon(
                            imageVector = Icons.Rounded.Send,
                            contentDescription = "Go to location",
                            modifier = Modifier
                                .rotate(-45f)
                                .padding(bottom = 5.dp)
                        )
                    },
                    text = { Text("GO") }
                )
            }
        }
        //
        // Create a Route
        //
        if (vm.showRoute) {
            Dialog(onDismissRequest = { vm.hideRoute() }) {
                OriginDestination(
                    onDismissRequest = { vm.hideRoute() },
                    routing = { vm.gotoRoute(it, context) }
                )
            }
        }
        //
        // POI Details
        //
        if (vm.showDetails) {
            ModalBottomSheet(
                onDismissRequest = { vm.hideDetails() },
                sheetState = rememberModalBottomSheetState()
            ) {
                POIDetails(
                    poi = vm.selectedPOI!!,
                    uid = vm.user!!.uid,
                    ratePOI = { value ->
                        vm.ratePOI(vm.selectedPOI!!, Rating(vm.user!!.uid, value))
                    }
                )
            }
        }
    }
}

@Composable
fun OriginDestination(
    onDismissRequest: () -> Unit,
    routing: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var dest by remember { mutableStateOf("") }
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondary
        ),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth()
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Where to go?",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
                FloatingActionButton(
                    onClick = onDismissRequest,
                    contentColor = Color.DarkGray,
                    containerColor = Color.Gray,
                    modifier = Modifier
                        .size(30.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close"
                    )
                }
            }
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .padding(top = 20.dp)
                    .fillMaxWidth()
            ) {
                Text("Origin: ")
                Text(
                    text = "Current Location",
                    fontWeight = FontWeight.Bold
                )
            }
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .padding(top = 20.dp)
                    .fillMaxWidth()
            ) {
                TextField(
                    value = dest,
                    onValueChange = { dest = it },
                    placeholder = { Text("Destination") },
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            routing(dest)
                        }
                    ),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Search
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                )
                FloatingActionButton(
                    onClick = { routing(dest) }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Send,
                        contentDescription = "Go"
                    )
                }
            }
        }
    }
}

@Composable
fun SearchBar(
    search: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var query by remember { mutableStateOf("") }
    Row(
        modifier = modifier
    ) {
        TextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Search") },
            keyboardActions = KeyboardActions(
                onSearch = {
                    search(query)
                }
            ),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Search
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .weight(1f)
        )
        FloatingActionButton(
            onClick = { search(query) },
            contentColor = Color.DarkGray,
            containerColor = Color.Gray,
        ) {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = "Search",
                modifier = Modifier.size(30.dp)
            )
        }
    }
}

@Composable
fun POIDetails(
    poi: POI,
    uid: String,
    ratePOI: (Boolean) -> Unit,
) {
    Column(
        modifier = Modifier
            .padding(20.dp)
            .fillMaxSize()
    ) {
        Text(
            text = poi.name,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = poi.type,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Row(
            modifier = Modifier
                .padding(top = 20.dp)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 20.dp)
            ) {
                Text(
                    text = poi.description
                )
                Text(
                    text = "Added by: ${poi.createdBy}",
                    modifier = Modifier
                        .padding(top = 20.dp)
                )
            }
            Column {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .width(175.dp)
                        .height(200.dp)
                ) {
                    if (poi.pictureUrl == "") {
                        Text(
                            text = "No image available",
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxSize()
                        )
                    } else {
                        AsyncImage(
                            model = poi.pictureUrl,
                            contentDescription = "POI Image",
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                        )
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                ) {
                    Button(
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Green.copy(alpha = 0.5f),
                            disabledContainerColor = Color.Green.copy(alpha = 0.2f)
                        ),
                        enabled = poi.ratings.none { it.user == uid && it.value },
                        onClick = {
                            ratePOI(true)
                        }
                    ) {
                        Text(text = "\uD83D\uDC4D")
                        Text(text = "${poi.ratings.filter { it.value }.size}")
                    }
                    Button(
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red.copy(alpha = 0.5f),
                            disabledContainerColor = Color.Red.copy(alpha = 0.2f)
                        ),
                        enabled = poi.ratings.none { it.user == uid && !it.value },
                        onClick = {
                            ratePOI(false)
                        }
                    ) {
                        Text(text = "\uD83D\uDC4E")
                        Text(text = "${poi.ratings.filter { !it.value }.size}")
                    }
                }
            }
        }
    }
}

private fun logout(context: Context) {
    AuthUI.getInstance()
        .signOut(context)
        .addOnCompleteListener {
            Log.i("Authentication", "User logged out")
            restartApp(context)
        }
}

private fun restartApp(context: Context) {
    val intent = Intent(context, MainActivity::class.java)
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(context, intent, null)
}
