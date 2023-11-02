package cm.project.android.projectx.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.material3.AlertDialog
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import cm.project.android.projectx.db.entities.Route
import cm.project.android.projectx.ui.AppViewModel

@Composable
fun ShowRoutes(
    modifier: Modifier = Modifier,
    vm: AppViewModel = viewModel(),
    onBack: () -> Unit
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState()),
    ) {
        SearchBar(
            search = { vm.filterRoutes(it) },
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth()
        )
        vm.allDRoutes.forEach {
            if (it.value.isEmpty()) return@forEach
            Column {
                Text(
                    text = "Routes by: ${it.value.first().createdBy}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(top = 20.dp, bottom = 20.dp)
                        .fillMaxWidth()
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
            Text(
                text = "Origin",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
            Text(route.origin)

            Text(
                text = "Destination",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier.padding(top = 20.dp)
            )
            Text(route.destination)

            Text(
                text = "Metrics",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier.padding(top = 20.dp)
            )
            Text(String.format("%.3f meters, %d seconds", route.totalDistance, route.totalDuration))
            Text(
                String.format(
                    "Average Speed: %.3f km/h",
                    3.6 * (route.totalDistance / route.totalDuration)
                )
            )
            Text("Number of Points: ${route.points.size}")
            Row(
                modifier = Modifier.padding(top = 20.dp)
            ) {
                if (route.createdBy == vm.user?.username) {
                    Button(
                        onClick = { vm.showDeleteRoutePrompt() },
                        colors = ButtonDefaults.textButtonColors(
                            containerColor = Color.Red,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .padding(end = 10.dp)
                    ) {
                        Text(text = "Delete")
                    }
                }
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
    //
    // Delete Route
    //
    if (vm.isDeleteRoutePrompt) {
        CustomAlertDialog(
            onDismissRequest = { vm.hideDeleteRoutePrompt() },
            onConfirmation = { vm.user?.let { vm.deleteRoute(it.id, route) } },
            dialogTitle = "Delete Route",
            dialogText = "Do you want to delete this route?",
            dismissText = "Cancel",
            confirmText = "Delete"
        )
    }
}