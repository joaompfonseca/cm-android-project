@file:OptIn(ExperimentalMaterial3Api::class)

package cm.project.android.projectx.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import cm.project.android.projectx.R
import cm.project.android.projectx.ui.screens.AddPOIScreen
import cm.project.android.projectx.ui.screens.AddUserScreen
import cm.project.android.projectx.ui.screens.MapScreen
import cm.project.android.projectx.ui.screens.ShowRoutes
import cm.project.android.projectx.ui.screens.ShowUserDetails

enum class AppScreen(@StringRes val title: Int) {
    Map(title = R.string.map_screen),
    AddPOI(title = R.string.add_poi_screen),
    AddUser(title = R.string.add_user_screen),
    UserDetails(title = R.string.show_user_details_screen),
    ShowRoutes(title = R.string.show_routes_screen),
    Profile(title = R.string.profile_screen)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(
    modifier: Modifier = Modifier,
    vm: AppViewModel = viewModel()
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val currentScreen = AppScreen.valueOf(backStackEntry?.destination?.route ?: AppScreen.Map.name)

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            AppTopAppBar(
                scrollBehavior = scrollBehavior,
                currentScreen = currentScreen,
                canNavigateBack = navController.previousBackStackEntry != null,
                navigateUp = { navController.navigateUp() }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppScreen.Map.name,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(route = AppScreen.Map.name) {
                MapScreen(
                    vm = vm,
                    onAddPOI = { navController.navigate(AppScreen.AddPOI.name) },
                    onAddUser = { navController.navigate(AppScreen.AddUser.name) },
                    onUserDetails = { navController.navigate(AppScreen.UserDetails.name) },
                    onShowRoutes = { navController.navigate(AppScreen.ShowRoutes.name) }
                )
            }
            composable(route = AppScreen.AddPOI.name) {
                AddPOIScreen(
                    vm = vm,
                    onBack = { navController.popBackStack(AppScreen.Map.name, inclusive = false) },
                )
            }
            composable(route = AppScreen.AddUser.name) {
                AddUserScreen(
                    vm = vm,
                    onBack = { navController.popBackStack(AppScreen.Map.name, inclusive = false) },
                )
            }
            composable(route = AppScreen.UserDetails.name) {
                ShowUserDetails(
                    vm = vm,
                    onBack = { navController.popBackStack(AppScreen.Map.name, inclusive = false) },
                )
            }
            composable(route = AppScreen.ShowRoutes.name) {
                ShowRoutes(
                    vm = vm,
                    onBack = { navController.popBackStack(AppScreen.Map.name, inclusive = false) },
                )
            }
        }
    }
}

@Composable
fun AppTopAppBar(
    scrollBehavior: TopAppBarScrollBehavior,
    currentScreen: AppScreen,
    canNavigateBack: Boolean,
    navigateUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    CenterAlignedTopAppBar(
        title = { Text(stringResource(currentScreen.title)) },
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        scrollBehavior = scrollBehavior,
        modifier = modifier,
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back_button)
                    )
                }
            }
        }
    )
}