package com.example.clockstudio.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.clockstudio.ClockStudioApplication
import com.example.clockstudio.R
import com.example.clockstudio.domain.model.WallpaperConfiguration
import com.example.clockstudio.presentation.customize.WallpaperCustomizeScreen
import com.example.clockstudio.presentation.details.WallpaperDetailScreen
import com.example.clockstudio.presentation.home.HomeScreen
import com.example.clockstudio.presentation.home.HomeViewModel
import com.example.clockstudio.presentation.language.LanguageScreen
import com.example.clockstudio.presentation.privacy.AboutScreen
import com.example.clockstudio.presentation.privacy.PrivacyScreen
import com.example.clockstudio.presentation.settings.SettingsScreen
import com.example.clockstudio.presentation.splash.SplashScreen
import com.example.clockstudio.util.launchLiveWallpaperPreview
import kotlinx.coroutines.launch

private object Route {
    const val SPLASH = "splash"
    const val HOME = "home"
    const val WALLPAPER = "wallpaper/{id}"
    const val CUSTOMIZE = "customize/{id}"
    const val LANGUAGE = "language"
    const val SETTINGS = "settings"
    const val PRIVACY = "privacy"
    const val ABOUT = "about"
}

@Composable
fun ClockStudioNavHost() {
    val navController = rememberNavController()
    val context = androidx.compose.ui.platform.LocalContext.current
    val application = context.applicationContext as ClockStudioApplication
    val preferences = application.preferencesRepository
    val repository = application.wallpaperRepository
    val homeViewModel: HomeViewModel = viewModel(
        factory = HomeViewModel.factory(repository, preferences),
    )

    NavHost(navController = navController, startDestination = Route.SPLASH) {
        composable(Route.SPLASH) {
            SplashScreen(preferences) {
                navController.navigate(Route.HOME) {
                    popUpTo(Route.SPLASH) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
        composable(Route.HOME) {
            HomeScreen(
                viewModel = homeViewModel,
                onOpenWallpaper = { item -> navController.navigate("wallpaper/${item.id}") },
                onOpenLanguage = { navController.navigate(Route.LANGUAGE) },
                onOpenSettings = { navController.navigate(Route.SETTINGS) },
                onOpenPrivacy = { navController.navigate(Route.PRIVACY) },
            )
        }
        composable(
            route = Route.WALLPAPER,
            arguments = listOf(navArgument("id") { type = NavType.StringType }),
        ) { entry ->
            val id = entry.arguments?.getString("id").orEmpty()
            val item = repository.getById(id)
            val scope = rememberCoroutineScope()
            if (item == null) {
                MissingWallpaper(onBack = { navController.popBackStack() })
            } else {
                WallpaperDetailScreen(
                    item = item,
                    preferencesRepository = preferences,
                    onBack = { navController.popBackStack() },
                    onCustomize = { configuration ->
                        scope.launch {
                            preferences.saveConfiguration(configuration)
                            navController.navigate("customize/${item.id}")
                        }
                    },
                    onSetWallpaper = { configuration ->
                        scope.launch {
                            preferences.saveConfiguration(configuration)
                            launchLiveWallpaperPreview(context)
                        }
                    },
                )
            }
        }
        composable(
            route = Route.CUSTOMIZE,
            arguments = listOf(navArgument("id") { type = NavType.StringType }),
        ) { entry ->
            val id = entry.arguments?.getString("id").orEmpty()
            val item = repository.getById(id)
            val savedConfiguration by preferences.configurationFlow.collectAsStateWithLifecycle(
                initialValue = WallpaperConfiguration(),
            )
            val scope = rememberCoroutineScope()
            if (item == null) {
                MissingWallpaper(onBack = { navController.popBackStack() })
            } else {
                val initial = if (savedConfiguration.wallpaperId == item.id) {
                    savedConfiguration
                } else {
                    WallpaperConfiguration.forWallpaper(item)
                }
                WallpaperCustomizeScreen(
                    item = item,
                    initialConfiguration = initial,
                    preferencesRepository = preferences,
                    onBack = { navController.popBackStack() },
                    onSetWallpaper = { configuration ->
                        scope.launch {
                            preferences.saveConfiguration(configuration)
                            launchLiveWallpaperPreview(context)
                        }
                    },
                )
            }
        }
        composable(Route.LANGUAGE) {
            LanguageScreen(preferencesRepository = preferences, onBack = { navController.popBackStack() })
        }
        composable(Route.SETTINGS) {
            SettingsScreen(
                preferencesRepository = preferences,
                onBack = { navController.popBackStack() },
                onLanguage = { navController.navigate(Route.LANGUAGE) },
                onPrivacy = { navController.navigate(Route.PRIVACY) },
                onAbout = { navController.navigate(Route.ABOUT) },
            )
        }
        composable(Route.PRIVACY) {
            PrivacyScreen(onBack = { navController.popBackStack() })
        }
        composable(Route.ABOUT) {
            AboutScreen(onBack = { navController.popBackStack() })
        }
    }
}

@Composable
private fun MissingWallpaper(onBack: () -> Unit) {
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center) {
        Text(stringResource(R.string.wallpaper_not_found), color = Color.White)
        androidx.compose.material3.TextButton(onClick = onBack) {
            Text(stringResource(R.string.back))
        }
    }
}
