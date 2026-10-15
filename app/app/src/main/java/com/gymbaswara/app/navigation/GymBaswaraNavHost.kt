package com.gymbaswara.app.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.ui.platform.LocalContext
import com.gymbaswara.app.MainViewModel
import com.gymbaswara.app.core.sync.SyncScheduler
import com.gymbaswara.app.feature.auth.LoginScreen
import com.gymbaswara.app.feature.auth.RegisterScreen
import com.gymbaswara.app.feature.home.HomeScreen
import com.gymbaswara.app.feature.exercises.ExerciseListScreen
import com.gymbaswara.app.feature.workout.ActiveWorkoutScreen
import com.gymbaswara.app.feature.workout.NewWorkoutScreen
import com.gymbaswara.app.feature.workout.WorkoutPreviewScreen
import com.gymbaswara.app.feature.splash.SplashScreen
import com.gymbaswara.app.feature.progress.ProgressScreen
import com.gymbaswara.app.feature.routines.RoutineListScreen
import com.gymbaswara.app.feature.routines.RoutineEditScreen
import com.gymbaswara.app.feature.profile.ProfileScreen
import com.gymbaswara.app.feature.settings.SettingsScreen
import com.gymbaswara.app.feature.bodymetrics.BodyMetricsScreen
import com.gymbaswara.app.feature.onboarding.OnboardingScreen
import com.gymbaswara.app.feature.backup.BackupRestoreScreen

@Composable
fun GymBaswaraNavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: String = "splash",
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val isActiveSession by mainViewModel.isActiveSession.collectAsState()
    val session by mainViewModel.session.collectAsState()
    val onboardingDone by mainViewModel.onboardingDone.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(session.isLoggedIn) {
        if (session.isLoggedIn) SyncScheduler.enqueue(context)
    }

    LaunchedEffect(isActiveSession) {
        if (isActiveSession) {
            val currentRoute = navController.currentDestination?.route
            if (currentRoute != null && !currentRoute.startsWith("active_workout")) {
                navController.navigate("active_workout") {
                    popUpTo("home")
                }
            }
        }
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable("splash") {
            SplashScreen(
                isLoggedIn = session.isLoggedIn,
                onFinished = { loggedIn ->
                    val target = if (loggedIn) "home" else "login"
                    navController.navigate(target) {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }
        composable("login") {
            LoginScreen(
                onAuthenticated = {
                    val target = if (onboardingDone == false) "onboarding" else "home"
                    navController.navigate(target) {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate("register") },
                onContinueOffline = {
                    val target = if (onboardingDone == false) "onboarding" else "home"
                    navController.navigate(target) {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToRestore = { navController.navigate("backup_restore") }
            )
        }
        composable("register") {
            RegisterScreen(
                onAuthenticated = {
                    val target = if (onboardingDone == false) "onboarding" else "home"
                    navController.navigate(target) {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("onboarding") {
            OnboardingScreen(
                onFinished = {
                    navController.navigate("home") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            )
        }
        composable("home") {
            HomeScreen(
                onNavigateToWorkout = { navController.navigate("workout_menu") },
                onNavigateToProgress = { navController.navigate("progress") },
                onResumeWorkout = { navController.navigate("active_workout") },
                onNavigateToRoutines = { navController.navigate("routines") },
                onNavigateToProfile = { navController.navigate("profile") },
                onNavigateToSettings = { navController.navigate("settings") }
            )
        }
        composable("profile") {
            ProfileScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSettings = { navController.navigate("settings") },
                onNavigateToBodyMetrics = { navController.navigate("bodymetrics") },
                onNavigateToBackupRestore = { navController.navigate("backup_restore") },
                onLoggedOut = {
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }
        composable("settings") {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToBackupRestore = { navController.navigate("backup_restore") }
            )
        }
        composable("backup_restore") {
            BackupRestoreScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable("bodymetrics") {
            BodyMetricsScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable("routines") {
            RoutineListScreen(
                onNavigateBack = { navController.popBackStack() },
                onAddRoutine = { navController.navigate("routine_edit?routineId=new") },
                onEditRoutine = { routineId ->
                    navController.navigate("routine_edit?routineId=${Uri.encode(routineId)}")
                }
            )
        }
        composable(
            route = "routine_edit?routineId={routineId}",
            arguments = listOf(
                navArgument("routineId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = "new"
                }
            )
        ) {
            RoutineEditScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable("workout_menu") {
            NewWorkoutScreen(
                onStartTemplate = { routineId ->
                    navController.navigate("workout_preview?routineId=${Uri.encode(routineId)}")
                },
                onResumeWorkout = { navController.navigate("active_workout") }
            )
        }
        composable(
            route = "workout_preview?routineId={routineId}",
            arguments = listOf(
                navArgument("routineId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) {
            WorkoutPreviewScreen(
                onNavigateBack = { navController.popBackStack() },
                onStartWorkout = { navController.navigate("active_workout") }
            )
        }
        composable(
            route = "active_workout?routineId={routineId}",
            arguments = listOf(
                navArgument("routineId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) {
            ActiveWorkoutScreen(
                onFinishWorkout = { navController.popBackStack("home", inclusive = false) }
            )
        }
        composable("exercises") {
            ExerciseListScreen()
        }
        composable("progress") {
            ProgressScreen()
        }
    }
}
