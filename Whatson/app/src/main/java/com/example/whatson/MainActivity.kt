package com.example.whatson

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.example.whatson.ui.theme.WhatsOnTheme
import com.example.whatson.util.ArticleItem
import com.example.whatson.util.NewsItem
import com.example.whatson.util.loadNewsFromAssets
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WhatsOnTheme {
                MainScreen()
            }
        }
    }
}

suspend fun fetchArticlesFromUrl(): List<ArticleItem> {
    val urlString = "https://firebasestorage.googleapis.com/v0/b/whatson-93370.appspot.com/o/article%2Farticle.json?alt=media&token=70e0c119-e396-4a3d-998f-a1db85e77c21"
    return withContext(Dispatchers.IO) {
        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"

        if (connection.responseCode == HttpURLConnection.HTTP_OK) {
            val jsonString = connection.inputStream.bufferedReader().use { it.readText() }
            val gson = Gson()
            val mapType = object : TypeToken<Map<String, List<Map<String, Any>>>>() {}.type
            val articlesMap: Map<String, List<Map<String, Any>>> = gson.fromJson(jsonString, mapType)

            val articleItems = mutableListOf<ArticleItem>()
            for (category in articlesMap.keys) {
                val articles = articlesMap[category] ?: continue
                for (article in articles) {
                    val title = article["Title"] as? String ?: ""
                    val description = article["Content"] as? String ?: ""
                    val imageUrl = (article["imageurl"] as? List<String>) ?: listOf()
                    articleItems.add(ArticleItem(title, description, imageUrl))
                }
            }
            Log.d("ArticleData", articleItems.toString())
            articleItems
        } else {
            emptyList()
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val context = LocalContext.current
    var articleList by remember { mutableStateOf(listOf<ArticleItem>()) }
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }

    LaunchedEffect(Unit) {
        val articles = fetchArticlesFromUrl()
        articleList = articles
    }

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        val scrollState = rememberLazyListState()
        val topBarVisible by remember {
            derivedStateOf {
                scrollState.firstVisibleItemScrollOffset == 0
            }
        }

        Box(modifier = Modifier.padding(innerPadding)) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (topBarVisible){
                    SearchBar(searchQuery) { searchQuery = it }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                TabRowExample()
                Spacer(modifier = Modifier.height(8.dp))

                val filteredList = articleList.filter { article ->
                    article.title.contains(searchQuery.text, ignoreCase = true) ||
                            article.description.contains(searchQuery.text, ignoreCase = true)
                }

                LazyColumn(
                    state = scrollState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredList) { article ->
                        ArticleCard(article)
                    }
                }
            }
        }
    }
}

@Composable
fun SearchBar(query: TextFieldValue, onQueryChange: (TextFieldValue) -> Unit) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        label = { Text("Search") },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun TabRowExample() {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("전체", "경제", "IT", "사회", "문화", "글로벌")

    TabRow(
        selectedTabIndex = selectedTabIndex,
        modifier = Modifier.fillMaxWidth()
    ) {
        tabs.forEachIndexed { index, tab ->
            Tab(
                selected = selectedTabIndex == index,
                onClick = { selectedTabIndex = index },
                text = { Text(tab) }
            )
        }
    }
}
