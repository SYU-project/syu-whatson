package com.example.whatson

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.rememberScrollState
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
fun BottomNavigationBar(
    navController: NavHostController,
    onHomeClick: (() -> Unit)? = null // 기본값을 null로 설정한 onHomeClick 콜백
) {
    val context = LocalContext.current // 현재 컨텍스트를 가져옴
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
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            BottomNavigationItem(
                icon = { Icon(item.icon, contentDescription = item.title) },
                selected = currentRoute == item.route,
                onClick = {
                    if (item.route == BottomNavItem.Home.route) {
                        if (onHomeClick != null) {
                            onHomeClick() // Home 버튼 클릭 시 콜백이 존재하면 호출
                        } else {
                            // Home 버튼 클릭 시 기본 동작
                            val intent = Intent(context, MainActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                            context.startActivity(intent)
                        }
                    } else { // Home 이외의 버튼 클릭 시의 동작
                        val intent = when (item.route) {
                            BottomNavItem.Favorite.route -> Intent(context, LikeScreenActivity::class.java)
                            BottomNavItem.Post.route -> Intent(context, WritePostActivity::class.java)
                            BottomNavItem.Settings.route -> Intent(context, SettingsActivity::class.java)
                            else -> return@BottomNavigationItem
                        }
                        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                        context.startActivity(intent)
                    }
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