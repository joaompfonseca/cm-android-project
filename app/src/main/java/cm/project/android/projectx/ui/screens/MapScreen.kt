package cm.project.android.projectx.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cm.project.android.projectx.ui.AppViewModel
import com.utsman.osmandcompose.CameraProperty
import com.utsman.osmandcompose.CameraState
import com.utsman.osmandcompose.MapProperties
import com.utsman.osmandcompose.Marker
import com.utsman.osmandcompose.MarkerState
import com.utsman.osmandcompose.OpenStreetMap
import com.utsman.osmandcompose.ZoomButtonVisibility
import com.utsman.osmandcompose.rememberOverlayManagerState
import org.osmdroid.tileprovider.tilesource.ITileSource
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.GeoPoint


@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    vm: AppViewModel = viewModel()
) {
    val cameraState = CameraState(
        CameraProperty(
            geoPoint = vm.center,
            zoom = 13.0
        )
    )

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
            modifier = modifier.fillMaxSize(),
            cameraState = cameraState,
            overlayManagerState = rememberOverlayManagerState(),
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
                    title = poi.name
                ) {
                    Column(
                        modifier = Modifier
                            .size(100.dp)
                            .background(color = Color.Gray, shape = RoundedCornerShape(12.dp))
                    ) {
                        Text(text = it.title)
                    }
                }
            }
        }
        SearchBar(
            search = { vm.getSearchGeocode(it) },
            Modifier
                .align(Alignment.TopCenter)
                .padding(top = 20.dp)
                .width(200.dp)
                .size(50.dp))
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