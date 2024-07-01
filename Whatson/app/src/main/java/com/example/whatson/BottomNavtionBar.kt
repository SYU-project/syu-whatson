package com.example.whatson

import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings


// 각 바텀 네비게이션 항목을 정의하는 sealed class
sealed class BottomNavItem(var title: String, var icon: ImageVector, var route: String) {
    object Home : BottomNavItem("Home", Icons.Default.Home, "home")
    object Profile : BottomNavItem("Profile", Icons.Default.Person, "profile")
    object Settings : BottomNavItem("Settings", Icons.Default.Settings, "settings")
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    // 바텀 네비게이션 항목 리스트
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Profile,
        BottomNavItem.Settings
    )
    val colors = MaterialTheme.colorScheme

    BottomNavigation(
        backgroundColor = colors.primary,
        contentColor = colors.onPrimary
    ) {
        // 현재 네비게이션 상태를 관찰
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        // 각 항목을 BottomNavigationItem으로 생성
        items.forEach { item ->
            BottomNavigationItem(
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title) },
                selected = currentRoute == item.route,
                onClick = {
                    // 항목 클릭 시 네비게이션 동작 정의
                    navController.navigate(item.route) {
                        navController.graph.startDestinationRoute?.let { route ->
                            popUpTo(route) {
                                saveState = true
                            }
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

@Composable
fun NavigationGraph(navController: NavHostController) {
    // NavHost를 사용하여 네비게이션 그래프를 정의
    NavHost(navController, startDestination = BottomNavItem.Home.route) {
        // 각 경로와 화면 컴포저블을 연결
        composable(BottomNavItem.Home.route) {
            HomeScreen()
        }
        composable(BottomNavItem.Profile.route) {
            LikeScreen()
        }
        composable(BottomNavItem.Settings.route) {
            SettingsScreen()
        }
    }
}

// HomeScreen 컴포저블
@Composable
fun HomeScreen() {
    Text(text = "홈")
}

// ProfileScreen 컴포저블
@Composable
fun LikeScreen() {
    Text(text = "Like리스트")
}

// SettingsScreen 컴포저블
@Composable
fun SettingsScreen() {
  Text(text = "셋팅")

}

