package com.example.whatson

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
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
    // JSON 파일 URL
    val urlString = "https://firebasestorage.googleapis.com/v0/b/whatson-93370.appspot.com/o/article%2Farticle.json?alt=media&token=70e0c119-e396-4a3d-998f-a1db85e77c21"

    return withContext(Dispatchers.IO) {
        val url = URL(urlString) // URL 객체 생성
        val connection = url.openConnection() as HttpURLConnection // HTTP 연결 열기
        connection.requestMethod = "GET" // GET 요청 설정

        if (connection.responseCode == HttpURLConnection.HTTP_OK) {
            // 응답이 성공적인 경우
            val jsonString = connection.inputStream.bufferedReader().use { it.readText() } // 응답을 문자열로 읽기
            val gson = Gson() // Gson 객체 생성
            val mapType = object : TypeToken<Map<String, List<Map<String, Any>>>>() {}.type // JSON 타입 정의
            val articlesMap: Map<String, List<Map<String, Any>>> = gson.fromJson(jsonString, mapType) // JSON 파싱

            val articleItems = mutableListOf<ArticleItem>()
            for (category in articlesMap.keys) {
                val articles = articlesMap[category] ?: continue
                for (article in articles) {
                    val title = article["Title"] as? String ?: "" // 제목 추출
                    val description = article["Content"] as? String ?: "" // 내용 추출
                    val imageUrl = (article["imageurl"] as? List<String>) ?: listOf() // 이미지 URL 리스트 추출
                    articleItems.add(ArticleItem(title, description, imageUrl)) // ArticleItem 객체 생성 및 리스트에 추가
                }
            }
            // JSON 파싱 결과를 로그에 출력
            Log.d("ArticleData", articleItems.toString())

            articleItems // 결과 반환
        } else {
            emptyList() // 응답이 실패한 경우 빈 리스트 반환
        }
    }
}
//아직 서버 안열려서 안해놓음
suspend fun fetchNewsFromUrl(): List<NewsItem> {
    val urlString = "http://210.109.52.162:5000/summaries"

    return withContext(Dispatchers.IO) {
        val url = URL(urlString) // URL 객체 생성
        val connection = url.openConnection() as HttpURLConnection // HTTP 연결 열기
        connection.requestMethod = "GET" // GET 요청 설정

        if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                // 응답이 성공적인 경우
                val jsonString = connection.inputStream.bufferedReader().use { it.readText() } // 응답을 문자열로 읽기
                val gson = Gson() // Gson 객체 생성
                val listType = object : TypeToken<List<Map<String, String>>>() {}.type // JSON 타입 정의
                val newsList: List<Map<String, String>> = gson.fromJson(jsonString, listType) // JSON 파싱

                // JSON 데이터를 NewsItem 객체로 변환
                val newsItems = newsList.map { article ->
                    val category = article["category"] ?: "" // 카테고리 추출
                    val title = article["title"] ?: "" // 제목 추출
                    val description = article["summary"] ?: "" // 요약 추출
                    NewsItem(category, title, description) // NewsItem 객체 생성

                }
            // 파싱된 결과를 로그에 출력
                Log.d("NewsData", newsItems.toString())

                newsItems// 결과 반환
        } else {
            emptyList() // 응답이 실패한 경우 빈 리스트 반환
        }
    }
}
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    var newsList by remember { mutableStateOf(listOf<NewsItem>()) }
    var articleList by remember { mutableStateOf(listOf<ArticleItem>()) }
    var mixedList by remember { mutableStateOf(listOf<Any>())}
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }


    LaunchedEffect(Unit) {
        // assets에서 뉴스 데이터 불러오기

       /* val loadedNews = fetchNewsFromUrl()
        newsList = loadedNews*/

        // Firebase에서 기사 데이터 가져오기
        val articles = fetchArticlesFromUrl()
        articleList = articles

        // 리스트 합치고 섞기
        val combinedList = (articleList).toMutableList()
        combinedList.shuffle()
        mixedList = articleList
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
                val filteredList = mixedList.filter { item ->
                    when (item) {
                        is NewsItem -> item.title.contains(searchQuery.text, ignoreCase = true) ||
                                item.description.contains(searchQuery.text, ignoreCase = true)
                        is ArticleItem -> item.title.contains(searchQuery.text, ignoreCase = true) ||
                                item.description.contains(searchQuery.text, ignoreCase = true)
                        else -> false
                    }
                }
                LazyColumn(
                    state = scrollState,
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
}}
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
@Composable
fun SearchBar(query: TextFieldValue, onQueryChange: (TextFieldValue) -> Unit) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        label = { Text("Search") },
        modifier = Modifier.fillMaxWidth()
    )
}

