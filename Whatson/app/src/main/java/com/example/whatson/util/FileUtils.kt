package com.example.whatson.util


import android.content.Context
import android.os.Parcelable
import android.util.Log
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.Serializable
import java.net.HttpURLConnection
import java.net.URL

data class NewsItem(
    val category: String,
    val title: String,
    val description: String
)

// ArticleItem class definition
data class ArticleItem(
    val title: String,
    val description: String,
    val imageUrl: List<String>,
    val writer:String,
    val date:String
): Serializable


// Function to save favorites
fun saveNewsFavorites(context: Context, favorites: List<NewsItem>) {
    val gson = Gson()
    val jsonString = gson.toJson(favorites)
    val file = File(context.cacheDir, "news_favorites.json")
    FileWriter(file).use {
        it.write(jsonString)
    }
}

fun saveArticleFavorites(context: Context, favorites: List<ArticleItem>) {
    val gson = Gson()
    val jsonString = gson.toJson(favorites)
    val file = File(context.cacheDir, "article_favorites.json")
    FileWriter(file).use {
        it.write(jsonString)
    }
}

// Function to load favorites
fun loadNewsFavorites(context: Context): List<NewsItem> {
    val file = File(context.cacheDir, "news_favorites.json")
    if (!file.exists()) return emptyList()

    val gson = Gson()
    val itemType = object : TypeToken<List<NewsItem>>() {}.type
    FileReader(file).use {
        return gson.fromJson(it, itemType)
    }
}

fun loadArticleFavorites(context: Context): List<ArticleItem> {
    val file = File(context.cacheDir, "article_favorites.json")
    if (!file.exists()) return emptyList()

    val gson = Gson()
    val itemType = object : TypeToken<List<ArticleItem>>() {}.type
    FileReader(file).use {
        return gson.fromJson(it, itemType)
    }
}




@Composable
fun NewsTab(selectedTabIndex: Int, onTabSelected:  (Int) -> Unit) {
    val tabs = listOf("전체", "경제", "IT", "사회", "문화", "글로벌")

    TabRow(
        selectedTabIndex = selectedTabIndex,
        modifier = Modifier.fillMaxWidth()
    ) {
        tabs.forEachIndexed { index, tab ->
            Tab(
                selected = selectedTabIndex == index,
                onClick = { onTabSelected(index) }, // 일반 콜백 함수로 호출
                text = { Text(tab, style = MaterialTheme.typography.titleMedium) }
            )
        }
    }
}

