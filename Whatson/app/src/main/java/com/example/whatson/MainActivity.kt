package com.example.whatson

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.rememberScrollState
import com.example.whatson.ui.theme.WhatsOnTheme
import com.example.whatson.util.ArticleItem
import com.example.whatson.util.NewsItem
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

suspend fun fetchNewsFromUrl(): List<NewsItem> {
    val urlString = "http://210.109.52.162:5000/summaries"

    return withContext(Dispatchers.IO) {
        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"

        if (connection.responseCode == HttpURLConnection.HTTP_OK) {
            val jsonString = connection.inputStream.bufferedReader().use { it.readText() }
            val gson = Gson()
            val listType = object : TypeToken<List<Map<String, String>>>() {}.type
            val newsList: List<Map<String, String>> = gson.fromJson(jsonString, listType)

            val newsItems = newsList.map { article ->
                val category = article["category"] ?: ""
                val title = article["title"] ?: ""
                val description = article["summary"] ?: ""
                NewsItem(category, title, description)
            }
            Log.d("NewsData", newsItems.toString())
            newsItems
        } else {
            emptyList()
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    var newsList by remember { mutableStateOf(listOf<NewsItem>()) }
    var articleList by remember { mutableStateOf(listOf<ArticleItem>()) }
    var mixedList by remember { mutableStateOf(listOf<Any>()) }
    var initialMixedList by remember { mutableStateOf(listOf<Any>()) }
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    var selectedTabIndex by remember { mutableStateOf(0) }

    // 각 탭의 스크롤 상태를 저장하는 맵
    val scrollStates = remember { mutableMapOf<Int, LazyListState>() }
    var currentScrollState by remember { mutableStateOf(LazyListState()) }

    LaunchedEffect(Unit) {
        val loadedNews = fetchNewsFromUrl()
        newsList = loadedNews

        val articles = fetchArticlesFromUrl()
        articleList = articles

        val combinedList = (newsList + articleList).toMutableList()
        combinedList.shuffle()
        mixedList = combinedList
        initialMixedList = combinedList
    }

    fun filterListByTabs(index: Int) {
        val filteredList = when (index) {
            0 -> initialMixedList
            1 -> newsList.filter { it.category == "economy" }
            2 -> newsList.filter { it.category == "IT" }
            3 -> newsList.filter { it.category == "society" }
            4 -> newsList.filter { it.category == "culture" }
            5 -> newsList.filter { it.category == "global" }
            else -> newsList + articleList
        }
        mixedList = filteredList
    }

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        val scrollState = rememberLazyListState()
        val topBarVisible by remember {
            derivedStateOf {
                scrollState.firstVisibleItemIndex == 0 && scrollState.firstVisibleItemScrollOffset == 0
            }
        }

        Box(modifier = Modifier.padding(innerPadding)) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (topBarVisible) {
                    SearchBar(searchQuery) { searchQuery = it }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                ButtonTabRowExample(selectedTabIndex) { index ->
                    // 현재 탭의 스크롤 상태 저장
                    scrollStates[selectedTabIndex] = scrollState

                    selectedTabIndex = index
                    filterListByTabs(index)

                    // 새 탭의 스크롤 상태 로드 (없으면 최상단으로)
                    currentScrollState = scrollStates[selectedTabIndex] ?: LazyListState()
                }

                Spacer(modifier = Modifier.height(8.dp))

                val trimmedQuery = searchQuery.text.trim()
                val filteredList = mixedList.filter { item ->
                    when (item) {
                        is NewsItem -> item.title.contains(trimmedQuery, ignoreCase = true) ||
                                item.description.contains(trimmedQuery, ignoreCase = true)
                        is ArticleItem -> item.title.contains(trimmedQuery, ignoreCase = true) ||
                                item.description.contains(trimmedQuery, ignoreCase = true)
                        else -> false
                    }
                }
                LazyColumn(
                    state = currentScrollState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredList) { item ->
                        when (item) {
                            is NewsItem -> NewsCard(item)
                            is ArticleItem -> ArticleCard(item)
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun ButtonTabRowExample(selectedTabIndex: Int, onTabSelected: (Int) -> Unit) {
    val tabs = listOf("전체", "경제", "IT", "사회", "문화", "글로벌")
    val scrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.Start
    ) {
        tabs.forEachIndexed { index, tab ->
            Button(
                onClick = { onTabSelected(index) },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = if (selectedTabIndex == index) Color.LightGray else Color.White,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .padding(4.dp)
                    .width(60.dp)
                    .height(30.dp)
            ) {
                Text(tab, fontSize = 10.5.sp)
            }
        }
    }
}

@Composable
fun SearchBar(query: TextFieldValue, onQueryChange: (TextFieldValue) -> Unit) {
    val background: Painter = painterResource(id = R.drawable.component_9)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Image(
            painter = background,
            contentDescription = null,
            modifier = Modifier.fillMaxWidth()
        )
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    if (query.text.isEmpty()) {
                        Text(
                            text = "검색",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
}
