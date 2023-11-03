package cm.project.android.projectx.ui.screens

import android.net.Uri
import android.widget.Toast
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
import androidx.lifecycle.viewmodel.compose.viewModel
import cm.project.android.projectx.R
import cm.project.android.projectx.db.entities.POI
import cm.project.android.projectx.db.entities.User
import cm.project.android.projectx.ui.AppViewModel
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddUserScreen(
    modifier: Modifier = Modifier,
    vm: AppViewModel = viewModel(),
    onBack: () -> Unit
) {

    var name by rememberSaveable { mutableStateOf("") }

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
        modifier = modifier
            .padding(16.dp, 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        Text(
            text = stringResource(R.string.add_user_screen),
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.displaySmall
        )

        //
        // NAME
        //

        Spacer(modifier = Modifier.padding(8.dp))

        Text(
            text = stringResource(R.string.user_name),
            style = MaterialTheme.typography.bodyLarge
        )

        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = name,
            onValueChange = { name = it },
            placeholder = { Text(stringResource(R.string.user_name_placeholder)) },
        )


        //
        // IMAGE
        //

        Spacer(modifier = Modifier.padding(4.dp))

        Text(
            text = stringResource(R.string.picture),
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
                    Text(stringResource(R.string.picture_select))
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
                    Text(stringResource(R.string.picture_photo))
                }
            }
            if (hasImage && imageUri != null) {
                AsyncImage(
                    model = imageUri,
                    contentDescription = stringResource(R.string.picture_selected),
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
                if (name == "" || imageUri == null) {
                    Toast.makeText(
                        context,
                        R.string.form_fill_everything,
                        Toast.LENGTH_SHORT
                    ).show()
                    return@ExtendedFloatingActionButton
                }
                val u = FirebaseAuth.getInstance().currentUser
                if (u == null) {
                    Toast.makeText(
                        context,
                        R.string.form_user_not_logged_in,
                        Toast.LENGTH_SHORT
                    ).show()
                    return@ExtendedFloatingActionButton
                }
                vm.addUser(
                    User(
                        id = u.uid,
                        displayName = u.displayName ?: name,
                        username = name,
                        pictureUrl = "",
                        totalXP = 0,
                        addedPOIs = 0,
                        receivedRatings = 0,
                        givenRatings = 0
                    ),
                    imageUri!!
                )
                onBack()
            },
            icon = {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = stringResource(R.string.form_submit)
                )
            },
            text = { Text(stringResource(R.string.form_submit)) },
            containerColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.End)
        )
    }
}