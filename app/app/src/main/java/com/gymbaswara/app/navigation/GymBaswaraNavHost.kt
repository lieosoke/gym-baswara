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
import com.gymbaswara.app.MainViewModel
import com.gymbaswara.app.feature.home.HomeScreen
import com.gymbaswara.app.feature.exercises.ExerciseListScreen
import com.gymbaswara.app.feature.workout.ActiveWorkoutScreen
import com.gymbaswara.app.feature.workout.NewWorkoutScreen
import com.gymbaswara.app.feature.workout.WorkoutPreviewScreen
import com.gymbaswara.app.feature.splash.SplashScreen
import com.gymbaswara.app.feature.progress.ProgressScreen

@Composable
fun GymBaswaraNavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: String = "splash",
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val isActiveSession by mainViewModel.isActiveSession.collectAsState()

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
                onNavigateToHome = {
                    navController.navigate("home") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }
        composable("home") {
            HomeScreen(
                onNavigateToWorkout = { navController.navigate("workout_menu") },
                onNavigateToProgress = { navController.navigate("progress") }
            )
        }
        composable("workout_menu") {
            NewWorkoutScreen(
                onStartTemplate = { routineId ->
                    navController.navigate("workout_preview?routineId=${Uri.encode(routineId)}")
                }
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
