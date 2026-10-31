package com.arifa.assistant.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.arifa.assistant.ui.components.FloatingBottomNav
import com.arifa.assistant.ui.screens.chat.ChatScreen
import com.arifa.assistant.ui.screens.debug.DebugScreen
import com.arifa.assistant.ui.screens.history.HistoryScreen
import com.arifa.assistant.ui.screens.home.HomeScreen
import com.arifa.assistant.ui.screens.onboarding.OnboardingScreen
import com.arifa.assistant.ui.screens.orb.OrbStyleScreen
import com.arifa.assistant.ui.screens.permissions.PermissionsScreen
import com.arifa.assistant.ui.screens.personality.PersonalityScreen
import com.arifa.assistant.ui.screens.profile.ProfileScreen
import com.arifa.assistant.ui.screens.settings.SettingsScreen
import com.arifa.assistant.ui.screens.splash.SplashScreen
import com.arifa.assistant.ui.screens.voice.VoiceScreen
import com.arifa.assistant.ui.screens.websiteforge.WebsiteForgeScreen
import com.arifa.assistant.ui.screens.websiteforge.WebsiteViewerScreen

@Composable
fun AppNavigation(
    navController: NavHostController
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: NavRoutes.SPLASH

    val showBottomBar = currentRoute in listOf(
        NavRoutes.HOME,
        NavRoutes.CHAT,
        NavRoutes.HISTORY,
        NavRoutes.SETTINGS
    )

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = NavRoutes.SPLASH
        ) {
            composable(NavRoutes.SPLASH) {
                SplashScreen(
                    onNavigateNext = { isOnboardingDone ->
                        val target = if (isOnboardingDone) NavRoutes.HOME else NavRoutes.ONBOARDING
                        navController.navigate(target) {
                            popUpTo(NavRoutes.SPLASH) { inclusive = true }
                        }
                    }
                )
            }

            composable(NavRoutes.ONBOARDING) {
                OnboardingScreen(
                    onFinishOnboarding = {
                        navController.navigate(NavRoutes.HOME) {
                            popUpTo(NavRoutes.ONBOARDING) { inclusive = true }
                        }
                    }
                )
            }

            composable(NavRoutes.HOME) {
                HomeScreen(
                    onNavigate = { route -> navController.navigate(route) },
                    onQuickPrompt = { prompt ->
                        if (prompt.contains("website", ignoreCase = true)) {
                            navController.navigate(NavRoutes.WEBSITE_FORGE)
                        } else {
                            navController.navigate(NavRoutes.CHAT)
                        }
                    }
                )
            }

            composable(NavRoutes.VOICE) {
                VoiceScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToSettings = { navController.navigate(NavRoutes.SETTINGS) }
                )
            }

            composable(NavRoutes.CHAT) {
                ChatScreen(
                    onNavigateToVoice = { navController.navigate(NavRoutes.VOICE) },
                    onNavigateToForge = { prompt -> navController.navigate(NavRoutes.WEBSITE_FORGE) }
                )
            }

            composable(NavRoutes.HISTORY) {
                HistoryScreen(
                    onSelectSession = { navController.navigate(NavRoutes.CHAT) }
                )
            }

            composable(NavRoutes.SETTINGS) {
                SettingsScreen(
                    onNavigate = { route -> navController.navigate(route) }
                )
            }

            composable(NavRoutes.PERSONALITY) {
                PersonalityScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(NavRoutes.ORB_STYLE) {
                OrbStyleScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(NavRoutes.PERMISSIONS) {
                PermissionsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(NavRoutes.PROFILE) {
                ProfileScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(NavRoutes.DEBUG) {
                DebugScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(NavRoutes.WEBSITE_FORGE) {
                WebsiteForgeScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onOpenViewer = { projId -> navController.navigate(NavRoutes.buildViewerRoute(projId)) }
                )
            }

            composable(
                route = NavRoutes.WEBSITE_VIEWER,
                arguments = listOf(navArgument("projectId") { type = NavType.StringType })
            ) { backStack ->
                val projectId = backStack.arguments?.getString("projectId") ?: ""
                WebsiteViewerScreen(
                    projectId = projectId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }

        // Floating Bottom Navigation Bar
        AnimatedVisibility(
            visible = showBottomBar,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            FloatingBottomNav(
                currentRoute = currentRoute,
                onNavigate = { targetRoute ->
                    if (targetRoute != currentRoute) {
                        navController.navigate(targetRoute) {
                            popUpTo(NavRoutes.HOME) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}
