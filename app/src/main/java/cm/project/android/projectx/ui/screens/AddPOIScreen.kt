package cm.project.android.projectx.ui.screens

import android.content.Context
import android.net.Uri
import android.widget.Toast
import coil.compose.AsyncImage
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import cm.project.android.projectx.R
import cm.project.android.projectx.db.entities.POI
import cm.project.android.projectx.ui.AppViewModel
import org.osmdroid.util.GeoPoint
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPOIScreen(
    vm: AppViewModel = viewModel(),
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var name by rememberSaveable { mutableStateOf("") }

    var description by rememberSaveable { mutableStateOf("") }

    val typeList = arrayOf("bicycle-parking", "bicycle-shop", "drinking-water", "toilets", "bench")
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    var type by rememberSaveable { mutableStateOf("") }

    var hasImage by rememberSaveable { mutableStateOf(false) }
    var imageUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            hasImage = uri != null
            imageUri = uri
        }
    )
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            hasImage = success
        }
    )

    val location = GeoPoint(vm.location!!.latitude, vm.location!!.longitude)

    val context = LocalContext.current

    Column(
        modifier = modifier
            .padding(16.dp, 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        Text(
            text = stringResource(R.string.add_poi_title),
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.displaySmall
        )

        //
        // NAME
        //

        Spacer(modifier = Modifier.padding(8.dp))

        Text(
            text = stringResource(R.string.poi_name),
            style = MaterialTheme.typography.bodyLarge
        )

        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = name,
            onValueChange = { name = it },
            placeholder = { Text(text = "e.g. City Park Water Fountain") },
        )

        //
        // DESCRIPTION
        //

        Spacer(modifier = Modifier.padding(4.dp))

        Text(
            text = stringResource(R.string.poi_description),
            style = MaterialTheme.typography.bodyLarge
        )

        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = description,
            onValueChange = { description = it },
            placeholder = { Text(text = "e.g. Drinkable water fountain with foot activation") },
        )

        //
        // TYPE
        //

        Spacer(modifier = Modifier.padding(4.dp))

        Text(
            text = stringResource(R.string.poi_type),
            style = MaterialTheme.typography.bodyLarge
        )

        ExposedDropdownMenuBox(
            expanded = isExpanded,
            onExpandedChange = {
                isExpanded = !isExpanded
            }
        ) {
            TextField(
                value = type,
                placeholder = { Text(text = "e.g. Fountain") },
                onValueChange = { },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = isExpanded,
                onDismissRequest = { isExpanded = false }
            ) {
                typeList.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(text = item) },
                        onClick = {
                            type = item
                            isExpanded = false
                        }
                    )
                }
            }
        }

        //
        // IMAGE
        //

        Spacer(modifier = Modifier.padding(4.dp))

        Text(
            text = stringResource(R.string.poi_picture),
            style = MaterialTheme.typography.bodyLarge
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
            ) {
                Button(
                    onClick = {
                        imagePicker.launch("image/*")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Select Image"
                    )
                }
                Button(
                    onClick = {
                        val uri = ComposeFileProvider.getImageUri(context)
                        imageUri = uri
                        cameraLauncher.launch(uri)
                    },
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth(),
                ) {
                    Text(
                        text = "Take photo"
                    )
                }
            }
            if (hasImage && imageUri != null) {
                AsyncImage(
                    model = imageUri,
                    contentDescription = "Selected Image",
                    modifier = Modifier
                        .width(200.dp)
                        .padding(8.dp)
                )
            }
        }

        //
        // SUBMIT
        //

        Spacer(modifier = Modifier.padding(4.dp))

        ExtendedFloatingActionButton(
            onClick = {
                if (name == "" || description == "" || type == "" || imageUri == null) {
                    Toast.makeText(
                        context,
                        "Fill everything before submitting!",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@ExtendedFloatingActionButton
                }
                val u = vm.user
                if (u == null) {
                    Toast.makeText(
                        context,
                        "User is not logged in!",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@ExtendedFloatingActionButton
                }
                vm.addPOI(
                    POI(
                        name = name,
                        description = description,
                        type = type,
                        pictureUrl = "",
                        latitude = location.latitude,
                        longitude = location.longitude,
                        createdBy = u.username,
                        ratings = mutableListOf()
                    ),
                    imageUri!!
                )
                vm.updateUser(u.id, "added", 50)
                onBack()
            },
            icon = {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = "Submit"
                )
            },
            text = { Text("Submit") },
            containerColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.End)
        )

    }
}

class ComposeFileProvider : FileProvider(
    R.xml.filepaths
) {
    companion object {
        fun getImageUri(context: Context): Uri {
            val directory = File(context.cacheDir, "images")
            directory.mkdirs()
            val file = File.createTempFile(
                "selected_image_",
                ".jpg",
                directory,
            )
            val authority = context.packageName + ".fileprovider"
            return getUriForFile(
                context,
                authority,
                file,
            )
        }
    }
}