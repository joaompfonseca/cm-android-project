package cm.project.android.projectx.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import cm.project.android.projectx.R
import cm.project.android.projectx.db.entities.Route
import cm.project.android.projectx.ui.AppViewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowRoutes(
    vm: AppViewModel = viewModel(),
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    runBlocking {
        launch {
            vm.getAllRoutes()
        }
    }

    Column {
        vm.allRoutes.forEach {
            Column {
                Text(
                    text = "Routes by: ${it.value.first().createdBy}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                it.value.forEach {
                    RouteItem(
                        route = it,
                        vm = vm,
                        onBack = onBack,
                        modifier = Modifier
                            .padding(top = 10.dp, start = 20.dp, end = 20.dp)
                            .border(
                                border = BorderStroke(1.dp, color = Color.Black),
                                shape = RoundedCornerShape(20.dp),
                            )
                            .fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun RouteItem(
    route: Route,
    vm: AppViewModel = viewModel(),
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            Text("Origin: ${route.origin}")
            Text("Destination: ${route.destination}")
            Text(String.format("Total Distance (m): %.3f", route.totalDistance))
            Text(String.format("Total Duration (s): %d", route.totalDuration))
            Text(String.format("Average Speed (m/s): %.3f", route.totalDistance / route.totalDuration))
            Text("Number of Points: ${route.points.size}")
            Button(
                onClick = {
                    vm.displayRoute(route)
                    onBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text("Display Route")
            }
        }
    }
}