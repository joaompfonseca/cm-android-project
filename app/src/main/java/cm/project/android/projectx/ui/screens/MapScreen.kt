package cm.project.android.projectx.ui.screens

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
    onUserDetails: () -> Unit,
    onShowRoutes: () -> Unit
) {

    if (!vm.isReady) {
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
                        if (vm.user == null) {
                            onAddUser()
                        } else {
                            vm.showDetails(poi)
                        }
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
                        if (vm.user == null) {
                            onAddUser()
                        } else {
                            onUserDetails()
                        }
                    },
                    contentColor = Color.DarkGray,
                    containerColor = Color.Gray,
                    modifier = Modifier
                        .padding(bottom = 20.dp, end = 20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Person,
                        contentDescription = stringResource(R.string.profile),
                        modifier = Modifier.size(30.dp)
                    )
                }
                //
                // Logout
                //
                FloatingActionButton(
                    onClick = { logout(context) },
                    contentColor = Color.DarkGray,
                    containerColor = Color.LightGray,
                    modifier = Modifier
                        .padding(bottom = 20.dp, end = 20.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ExitToApp,
                            contentDescription = stringResource(R.string.logout),
                            modifier = Modifier.size(30.dp)
                        )
                        Text(stringResource(R.string.logout))
                    }
                }
                //
                // Routes Page
                //
                FloatingActionButton(
                    onClick = {
                        onShowRoutes()
                    },
                    contentColor = Color.DarkGray,
                    containerColor = Color.Yellow,
                    modifier = Modifier
                        .padding(bottom = 20.dp, end = 20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Send,
                        contentDescription = stringResource(R.string.routes),
                        modifier = Modifier
                            .size(30.dp)
                            .rotate(-45f)
                            .padding(start = 5.dp)
                    )
                }
                //
                // Find Me
                //
                FloatingActionButton(
                    onClick = { vm.gotoUserLocation() },
                    contentColor = Color.LightGray,
                    containerColor = Color.Blue,
                    shape = CircleShape,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.LocationOn,
                        contentDescription = stringResource(R.string.find_me),
                        modifier = Modifier
                            .size(30.dp)
                    )
                }
            }
            Column {
                //
                // Search Bar
                //
                SearchBar(
                    search = { vm.gotoSearch(it) },
                    modifier = Modifier
                        .padding(bottom = 20.dp)
                        .fillMaxWidth()
                )
                //
                // Battery Level
                //
                Card(
                    modifier = Modifier
                        .height(55.dp)
                        .fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(start = 10.dp, end = 10.dp)
                            .fillMaxSize()
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_bicycle),
                            contentDescription = stringResource(R.string.battery_level),
                            modifier = Modifier
                                .size(50.dp)
                                .padding(end = 10.dp)
                        )
                        Text(
                            text = "${vm.batteryLevel.toInt()}%",
                            modifier = Modifier
                                .padding(end = 10.dp)
                        )
                        LinearProgressIndicator(
                            progress = vm.batteryLevel / 100f,
                            color = if (vm.batteryLevel > 20) Color.Green else Color.Red,
                            trackColor = Color.Gray,
                            modifier = Modifier
                                .fillMaxWidth(1f)
                                .fillMaxHeight(0.5f)
                        )
                    }
                }
            }
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
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .padding(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = stringResource(R.string.clear_displayed_route)
                            )
                            Text(stringResource(R.string.clear_displayed_route))
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
                        if (vm.location != null) {
                            if (vm.user == null) {
                                onAddUser()
                            } else {
                                onAddPOI()
                            }
                        } else {
                            Toast.makeText(
                                context,
                                R.string.permission_location,
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = stringResource(R.string.add_poi_title)
                        )
                    },
                    text = { Text(stringResource(R.string.poi)) }
                )
                //
                // Track / Un-track User
                //
                if (!vm.isTrackingLocation) {
                    LargeFloatingActionButton(
                        onClick = {
                            if (vm.user == null) {
                                onAddUser()
                            } else {
                                vm.gotoUserLocation()
                                vm.startTrackingUserLocation()
                            }
                        },
                        shape = CircleShape,
                        containerColor = Color.Green
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = stringResource(R.string.track_me),
                                tint = Color.DarkGray,
                                modifier = Modifier.size(30.dp)
                            )
                            Text(
                                text = stringResource(R.string.track_me),
                                color = Color.DarkGray
                            )
                        }
                    }
                } else {
                    LargeFloatingActionButton(
                        onClick = {
                            vm.stopTrackingUserLocation()
                            if (vm.routePoints.isNotEmpty()) {
                                vm.showSavePrompt()
                            }
                        },
                        shape = CircleShape,
                        containerColor = Color.Red
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = stringResource(R.string.stop_tracking),
                                modifier = Modifier.size(30.dp)
                            )
                            Text(stringResource(R.string.stop_tracking))
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
                            contentDescription = stringResource(R.string.go_to_location),
                            modifier = Modifier
                                .rotate(-45f)
                                .padding(bottom = 5.dp)
                        )
                    },
                    text = { Text(stringResource(R.string.go)) }
                )
            }
        }
        //
        // Save or Discard Recorded Route
        //
        if (vm.isSaveRoutePrompt) {
            CustomAlertDialog(
                onDismissRequest = { vm.clearRoutePoints() },
                onConfirmation = { vm.saveRoutePoints() },
                dialogTitle = stringResource(R.string.save_route),
                dialogText = stringResource(R.string.save_route_prompt),
                dismissText = stringResource(R.string.discard),
                confirmText = stringResource(R.string.save)
            )
        }
        //
        // Delete POI
        //
        if (vm.isDeletePOIPrompt) {
            CustomAlertDialog(
                onDismissRequest = { vm.hideDeletePOIPrompt() },
                onConfirmation = { vm.selectedPOI?.let { vm.deletePOI(it) } },
                dialogTitle = stringResource(R.string.delete_poi),
                dialogText = stringResource(R.string.delete_poi_prompt),
                dismissText = stringResource(R.string.cancel),
                confirmText = stringResource(R.string.delete)
            )
        }
        //
        // Create a Route with Origin and Destination
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
                    vm = vm,
                    ratePOI = { value ->
                        vm.ratePOI(vm.selectedPOI!!, Rating(vm.user!!.id, value))
                    },
                    updateUser = { id, type, xp ->
                        vm.updateUser(id, type, xp)
                    },
                    showDeletePrompt = { vm.showDeletePOIPrompt() }
                )
            }
        }
    }
}

@Composable
fun CustomAlertDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    dialogTitle: String,
    dialogText: String,
    dismissText: String,
    confirmText: String
) {
    AlertDialog(
        title = {
            Text(text = dialogTitle)
        },
        text = {
            Text(text = dialogText)
        },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmation()
                }
            ) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                }
            ) {
                Text(dismissText)
            }
        }
    )
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
                    text = stringResource(R.string.where_to_go),
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
                        contentDescription = stringResource(R.string.close)
                    )
                }
            }
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .padding(top = 20.dp)
                    .fillMaxWidth()
            ) {
                Text("${stringResource(R.string.origin)}: ")
                Text(
                    text = stringResource(R.string.current_location),
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
                    placeholder = { Text(stringResource(R.string.destination)) },
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
                        contentDescription = stringResource(R.string.go)
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
            placeholder = { Text(stringResource(R.string.search)) },
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
                contentDescription = stringResource(R.string.search),
                modifier = Modifier.size(30.dp)
            )
        }
    }
}

@Composable
fun POIDetails(
    poi: POI,
    vm: AppViewModel,
    ratePOI: (Boolean) -> Unit,
    updateUser: (String, String, Int) -> Unit,
    showDeletePrompt: () -> Unit,
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
                    text = stringResource(R.string.added_by, poi.createdBy),
                    modifier = Modifier
                        .padding(top = 20.dp)
                )
                if (poi.createdBy == vm.user?.username) {
                    Button(
                        onClick = { showDeletePrompt() },
                        colors = ButtonDefaults.textButtonColors(
                            containerColor = Color.Red,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .padding(top = 20.dp)
                    ) {
                        Text(stringResource(R.string.delete))
                    }
                }
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
                            text = stringResource(R.string.no_image_available),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxSize()
                        )
                    } else {
                        AsyncImage(
                            model = poi.pictureUrl,
                            contentDescription = stringResource(R.string.poi_image),
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
                        enabled = poi.ratings.none { it.user == vm.user!!.id && it.value },
                        onClick = {
                            ratePOI(true)
                            // check if user already rated this POI
                            if (poi.ratings.none { it.user == vm.user!!.id }) {
                                updateUser(poi.createdBy, "received", 10)
                                updateUser(vm.user!!.id, "given", 10)
                            }
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
                        enabled = poi.ratings.none { it.user == vm.user!!.id && !it.value },
                        onClick = {
                            ratePOI(false)
                            if (poi.ratings.none { it.user == vm.user!!.id }) {
                                updateUser(poi.createdBy, "received", 10)
                                updateUser(vm.user!!.id, "given", 10)
                            }
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
