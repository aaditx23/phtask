package com.aaditx23.phtask

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.aaditx23.phtask.presentation.screens.CourseDetails.CourseDetailScreen
import com.aaditx23.phtask.presentation.screens.CourseList.CourseListScreen

@Composable
fun App() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "course_list"
    ) {
        composable(route = "course_list") {
            CourseListScreen(
                onCourseClick = { course ->
                    navController.navigate("course_detail/${course.courseId}")
                }
            )
        }

        composable(
            route = "course_detail/{courseId}",
            arguments = listOf(
                navArgument("courseId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val courseId = backStackEntry.arguments?.getString("courseId") ?: return@composable
            CourseDetailScreen(
                courseId = courseId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

