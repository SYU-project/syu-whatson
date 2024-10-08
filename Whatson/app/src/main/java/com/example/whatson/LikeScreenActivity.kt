package com.example.whatson

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.example.whatson.ui.theme.WhatsOnTheme
import com.example.whatson.util.loadArticleFavorites
import com.example.whatson.util.loadNewsFavorites

class LikeScreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // 다크 모드를 강제 비활성화하고 밝은 테마로 고정
            WhatsOnTheme(darkTheme = false) {
                val statusBarColor = Color(0xFFFFFFFF) // 배경색을 흰색으로 고정
                window.statusBarColor = statusBarColor.toArgb()

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    window.insetsController?.setSystemBarsAppearance(
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                    )
                } else {
                    @Suppress("DEPRECATION")
                    window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                }
                // 원하는 화면을 호출
                FavoritesScreen()
            }
        }
    }}
@Composable
fun FavoritesScreen() {
    val navController = rememberNavController()
    val context = LocalContext.current

    // Load favorites from both NewsItem and ArticleItem
    val newsFavorites = rememberSaveable { mutableStateOf(loadNewsFavorites(context)) }
    val articleFavorites = rememberSaveable { mutableStateOf(loadArticleFavorites(context)) }


    LaunchedEffect(Unit) {
        newsFavorites.value = loadNewsFavorites(context)
        articleFavorites.value = loadArticleFavorites(context)
    }
    Scaffold(
        topBar = { favorite_Bar() },
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }

    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            LazyColumn {
                // Display news favorites
                items(newsFavorites.value) { newsItem ->
                    NewsCard(newsItem)
                }
                // Display article favorites
                items(articleFavorites.value) { articleItem ->
                    ArticleCard(articleItem)
                }
            }
        }
    }
}