package cm.project.android.projectx.ui.screens

import android.content.Context
import android.net.Uri
import coil.compose.AsyncImage
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.core.content.FileProvider.getUriForFile
import androidx.lifecycle.viewmodel.compose.viewModel
import cm.project.android.projectx.R
import cm.project.android.projectx.db.entities.POI
import cm.project.android.projectx.ui.AppViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPOIScreen(
    modifier: Modifier = Modifier,
    vm: AppViewModel = viewModel(),
    onBack: () -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }

    var description by rememberSaveable { mutableStateOf("") }

    val typeList = arrayOf("parking", "fountain", "bathroom", "bench")
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    var type by rememberSaveable { mutableStateOf(typeList[0]) }

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

    val context = LocalContext.current

    Column(
        modifier = Modifier
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
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) },
                modifier = Modifier.menuAnchor()
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

        Column(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button(
                onClick = {
                    imagePicker.launch("image/*")
                },
            ) {
                Text(
                    text = "Select Image"
                )
            }
            Button(
                modifier = Modifier.padding(top = 16.dp),
                onClick = {
                    val uri = ComposeFileProvider.getImageUri(context)
                    imageUri = uri
                    cameraLauncher.launch(uri)
                },
            ) {
                Text(
                    text = "Take photo"
                )
            }
            if (hasImage && imageUri != null) {
                AsyncImage(
                    model = imageUri,
                    modifier = Modifier
                        .size(100.dp),
                    contentDescription = "Selected image",
                )
            }
        }

        //
        // SUBMIT
        //

        Spacer(modifier = Modifier.padding(4.dp))

        Button(onClick = {
            vm.addPOI(
                POI(
                    name = name,
                    description = description,
                    type = type,
                    pictureUrl = imageUri.toString(), // TODO: upload image to Firebase Storage
                    latitude = vm.location!!.latitude,
                    longitude = vm.location!!.longitude,
                    createdBy = "user", // TODO: change to user id
                    ratings = mutableListOf()
                )
            )
            onBack()
        }) {
            Text(text = "Submit")
        }

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