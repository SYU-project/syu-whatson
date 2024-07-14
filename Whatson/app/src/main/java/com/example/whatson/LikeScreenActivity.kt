package com.example.whatson

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.example.whatson.ui.theme.WhatsOnTheme
import com.example.whatson.*
import com.example.whatson.util.loadArticleFavorites
import com.example.whatson.util.loadFavorites
import com.example.whatson.util.loadNewsFavorites

class LikeScreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WhatsOnTheme {
                FavoritesScreen()
            }
        }
    }}

@Composable
fun FavoritesScreen() {
    val navController = rememberNavController()
    val context = LocalContext.current

    // Load favorites from both NewsItem and ArticleItem
    val newsFavorites = remember { mutableStateOf(loadNewsFavorites(context)) }
    val articleFavorites = remember { mutableStateOf(loadArticleFavorites(context)) }

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
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