package com.example.whatson

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings

// 각 바텀 네비게이션 항목을 정의하는 sealed class
sealed class BottomNavItem(var title: String, var icon: ImageVector, var route: String) {
    object Home : BottomNavItem("홈", Icons.Default.Home, "home")
    object Favorite : BottomNavItem("스크랩", Icons.Default.Favorite, "favorite")
    object Post : BottomNavItem("글 작성", Icons.Default.Create, "Post")
    object Settings : BottomNavItem("셋팅", Icons.Default.Settings, "settings")
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val context = LocalContext.current
    // 바텀 네비게이션 항목 리스트
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Favorite,
        BottomNavItem.Post,
        BottomNavItem.Settings
    )
    val colors = MaterialTheme.colorScheme

    BottomNavigation(
        backgroundColor = colors.onPrimary,
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
                    val intent = when (item.route) {
                        BottomNavItem.Home.route -> Intent(context, MainActivity::class.java)
                        BottomNavItem.Favorite.route -> Intent(context, LikeScreenActivity::class.java)
                        BottomNavItem.Post.route -> Intent(context, WritePostActivity::class.java)
                        BottomNavItem.Settings.route -> Intent(context, SettingsActivity::class.java)
                        else -> return@BottomNavigationItem
                    }
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)//새로고침 없이 화면전환
                    context.startActivity(intent)
                }
            )
        }
    }
}

// 각 Activity 예시



class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Text(text = "셋팅 화면")
        }
    }
}