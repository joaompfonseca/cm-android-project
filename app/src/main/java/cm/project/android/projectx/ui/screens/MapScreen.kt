package cm.project.android.projectx.ui.screens

import android.app.Application
import android.util.Log
import coil.compose.AsyncImage
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cm.project.android.projectx.R
import cm.project.android.projectx.ui.AppViewModel
import com.firebase.ui.auth.AuthUI
import com.utsman.osmandcompose.MapProperties
import com.utsman.osmandcompose.Marker
import com.utsman.osmandcompose.MarkerState
import com.utsman.osmandcompose.OpenStreetMap
import com.utsman.osmandcompose.ZoomButtonVisibility
import org.osmdroid.tileprovider.tilesource.ITileSource
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.GeoPoint


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    vm: AppViewModel = viewModel(),
    onAddPOI: () -> Unit
) {

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
            Log.i("POI", vm.poiList.size.toString())
            vm.poiList.forEach { poi ->
                Marker(
                    state = MarkerState(
                        geoPoint = GeoPoint(poi.latitude, poi.longitude)
                    ),
                    title = poi.name
                ) {
                    Column(
                        modifier = Modifier
                            .size(100.dp)
                            .background(color = Color.Gray, shape = RoundedCornerShape(12.dp))
                    ) {
                        Text(text = it.title)
                        AsyncImage(model = poi.pictureUrl, contentDescription = "POI Image")
                    }
                }
            }
            Marker(
                icon = LocalContext.current.getDrawable(R.drawable.user_location),
                state = MarkerState(
                    geoPoint = vm.location ?: GeoPoint(0.0, 0.0)
                )
            )
        }
        if (!vm.route) {
            SearchBar(
                search = { vm.getSearch(it) },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 20.dp)
                    .width(200.dp)
                    .size(50.dp)
            )
        }
        Button(
            onClick = { vm.route = !vm.route },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = 20.dp)
        ) {
            Text(text = "Route")
        }
        if (vm.route) {
            OriginDestination(
                routing = { vm.getRoute(it, context) }
            )
        }
        Button(
            onClick = { vm.gotoUserLocation() },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp)
        ) {
            Text(text = "Find me!")
        }
        Button(
            onClick = {
                AuthUI.getInstance()
                    .signOut(context)
                    .addOnCompleteListener {

                    }
            },
        ) {
            Text(text = "Log Out")
        }
        if (vm.location != null) {
            Button(
                onClick = {
                    onAddPOI()
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 20.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add POI")
                Text(text = "Add POI")
            }
        }
    }
}

@Composable
fun OriginDestination(
    routing: (String) -> Unit
){
    var dest by remember { mutableStateOf("") }
    Column(modifier = Modifier
        .padding(20.dp)
        .background(color = Color.White, shape = RoundedCornerShape(12.dp))) {
        Row(modifier = Modifier) {
            Text(text = " Origin:         ", modifier = Modifier.padding(top = 20.dp))
            TextField(value = "Current Location",
                onValueChange = { },
                readOnly = true,
                modifier = Modifier.width(200.dp))
        }
        Row {
            Text(text = " Destination:", modifier = Modifier.padding(top = 20.dp))
            TextField(
                value = dest,
                onValueChange = { dest = it },
                label = { Text("Destination") },
                keyboardActions = KeyboardActions(
                    onSearch = {
                        routing(dest)
                    }
                ),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search
                ),
                modifier = Modifier.width(200.dp)
                )
        }
    }
}

@Composable
fun SearchBar(
    search: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var query by remember { mutableStateOf("") }
    TextField(
        value = query,
        onValueChange = { query = it },
        label = { Text("Search") },
        keyboardActions = KeyboardActions(
            onSearch = {
                search(query)
            }
        ),
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Search
        ),
        modifier = modifier
    )
}