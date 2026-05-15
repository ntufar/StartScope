package com.startscope.coldstart.ui

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.startscope.coldstart.AppContainer
import com.startscope.coldstart.R
import com.startscope.coldstart.ui.detail.AppDetailScreen
import com.startscope.coldstart.ui.detail.AppDetailViewModel
import com.startscope.coldstart.ui.detail.StartDetailScreen
import com.startscope.coldstart.ui.detail.StartDetailViewModel
import com.startscope.coldstart.ui.onboarding.OnboardingScreen
import com.startscope.coldstart.ui.settings.SettingsScreen
import com.startscope.coldstart.ui.settings.SettingsViewModel
import com.startscope.coldstart.ui.timeline.TimelineScreen
import com.startscope.coldstart.ui.timeline.TimelineViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private object Routes {
    const val TIMELINE = "timeline"
    const val SETTINGS = "settings"
    const val APP = "app/{packageName}"
    const val START = "start/{startId}"

    fun app(pkg: String): String = "app/${Uri.encode(pkg)}"

    fun start(id: Long): String = "start/$id"
}

@Composable
fun AppRoot(container: AppContainer) {
    val prefs = container.userPreferences
    val scope = rememberCoroutineScope()
    var gate by remember { mutableStateOf<Boolean?>(null) }
    LaunchedEffect(Unit) {
        gate = prefs.onboardingComplete.first()
    }
    when (gate) {
        null -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        false -> {
            OnboardingScreen(
                onFinished = {
                    scope.launch {
                        prefs.setOnboardingComplete(true)
                        gate = true
                    }
                },
            )
        }
        true -> {
            MainShell(container)
        }
    }
}

@Composable
private fun MainShell(container: AppContainer) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val backStack by navController.currentBackStackEntryAsState()
    val route = backStack?.destination?.route
    val showBottomBar = route == Routes.TIMELINE || route == Routes.SETTINGS

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                NavigationBarItem(
                    selected = route == Routes.TIMELINE,
                    onClick = {
                        navController.navigate(Routes.TIMELINE) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = {
                        Icon(
                            Icons.Filled.DateRange,
                            contentDescription = stringResource(R.string.tab_timeline),
                        )
                    },
                    label = { Text(stringResource(R.string.tab_timeline)) },
                )
                NavigationBarItem(
                    selected = route == Routes.SETTINGS,
                    onClick = {
                        navController.navigate(Routes.SETTINGS) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = {
                        Icon(
                            Icons.Filled.Settings,
                            contentDescription = stringResource(R.string.tab_settings),
                        )
                    },
                    label = { Text(stringResource(R.string.tab_settings)) },
                )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.TIMELINE,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Routes.TIMELINE) {
                val vm: TimelineViewModel = viewModel(
                    factory = ViewModelFactories.timeline(context, container.repository),
                )
                val sections by vm.sections.collectAsStateWithLifecycle()
                val packages by vm.packages.collectAsStateWithLifecycle()
                val filter by vm.filterPackage.collectAsStateWithLifecycle()
                TimelineScreen(
                    sections = sections,
                    packages = packages,
                    selectedPackage = filter,
                    onPackageSelected = vm::setFilterPackage,
                    onStartClick = { id -> navController.navigate(Routes.start(id)) },
                    onAppStatsClick = { pkg -> navController.navigate(Routes.app(pkg)) },
                )
            }
            composable(Routes.SETTINGS) {
                val vm: SettingsViewModel = viewModel(
                    factory = ViewModelFactories.settings(container.userPreferences),
                )
                val retention by vm.retentionDays.collectAsStateWithLifecycle()
                val collect by vm.collectionEnabled.collectAsStateWithLifecycle()
                SettingsScreen(
                    retentionDays = retention,
                    collectionEnabled = collect,
                    onRetentionChange = vm::setRetentionDays,
                    onCollectionChange = vm::setCollectionEnabled,
                    onBack = null,
                )
            }
            composable(
                route = Routes.APP,
                arguments = listOf(
                    navArgument("packageName") { type = NavType.StringType },
                ),
            ) { entry ->
                val raw = entry.arguments?.getString("packageName").orEmpty()
                val pkg = Uri.decode(raw)
                val vm: AppDetailViewModel = viewModel(
                    key = "app_$pkg",
                    factory = ViewModelFactories.appDetail(container.repository, pkg),
                )
                val state by vm.state.collectAsStateWithLifecycle()
                AppDetailScreen(
                    state = state,
                    packageName = pkg,
                    onBack = { navController.popBackStack() },
                )
            }
            composable(
                route = Routes.START,
                arguments = listOf(
                    navArgument("startId") { type = NavType.LongType },
                ),
            ) { entry ->
                val id = entry.arguments?.getLong("startId") ?: return@composable
                val vm: StartDetailViewModel = viewModel(
                    key = "start_$id",
                    factory = ViewModelFactories.startDetail(container.repository, id),
                )
                val start by vm.start.collectAsStateWithLifecycle()
                StartDetailScreen(
                    start = start,
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}
