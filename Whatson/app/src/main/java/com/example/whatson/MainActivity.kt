package com.example.whatson

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.rememberNavController
import com.example.whatson.ui.theme.WhatsOnTheme
import com.example.whatson.util.ArticleItem
import com.example.whatson.util.NewsItem
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.max

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
    val urlString = "https://firebasestorage.googleapis.com/v0/b/whatson-93370.appspot.com/o/article%2Farticle.json?alt=media&token=14c28589-fd32-46d8-977a-029260d20ace"

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

            newsItems // 결과 반환
        } else {
            emptyList() // 응답이 실패한 경우 빈 리스트 반환
        }
    }
}

suspend fun sendDataToServer(): List<String> {
    val urlString = "http://210.109.52.162:5000/hi"
    val data = listOf("h1", "h2", "h3")
    return withContext(Dispatchers.IO) {
        try {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json; utf-8")
            connection.setRequestProperty("Accept", "application/json")

            val jsonInputString = Gson().toJson(data)
            connection.outputStream.use { outputStream ->
                outputStream.write(jsonInputString.toByteArray(Charsets.UTF_8))
            }

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val jsonString = connection.inputStream.bufferedReader().use { it.readText() }
                val gson = Gson()
                val listType = object : TypeToken<List<String>>() {}.type
                gson.fromJson(jsonString, listType)
            } else {
                Log.e("sendDataToServer", "Server responded with: ${connection.responseCode}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("sendDataToServer", "Failed to send data to server", e)
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
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    var serverResponse by remember { mutableStateOf(listOf<String>()) } // 서버 통신 테스트용
    var selectedTabIndex by remember { mutableStateOf(0) }
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = false)
    val coroutineScope = rememberCoroutineScope() // 임시 버튼 추가용

    LaunchedEffect(Unit) {
        // assets에서 뉴스 데이터 불러오기
        val loadedNews = fetchNewsFromUrl()
        newsList = loadedNews

        // Firebase에서 기사 데이터 가져오기
        val articles = fetchArticlesFromUrl()
        articleList = articles

        // 리스트 합치고 섞기
        val combinedList = (newsList + articleList).toMutableList()
        combinedList.shuffle()
        mixedList = combinedList
    }

    fun filterListByTab(index: Int) {
        val filteredList = when (index) {
            1 -> newsList.filter { it.category == "economy" } + articleList
            2 -> newsList.filter { it.category == "IT" } + articleList
            3 -> newsList.filter { it.category == "society" } + articleList
            4 -> newsList.filter { it.category == "culture" } + articleList
            5 -> newsList.filter { it.category == "global" } + articleList
            else -> newsList + articleList
        }
        val adjustedList = filteredList.flatMap { item ->
            val viewCount = when (item) {
                is NewsItem -> item.viewCount
                is ArticleItem -> item.viewCount
                else -> 0
            }
            // 뷰 카운트가 높을수록 해당 항목이 적게 나타나도록 조정
            if (viewCount == 5) emptyList() else List(max(1, 5 - viewCount)) { item }
        }.shuffled()
        mixedList = adjustedList
    }

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        val scrollState = rememberLazyListState()
        val topBarVisible by remember {
            derivedStateOf {
                scrollState.firstVisibleItemScrollOffset == 0 // 스크롤을 화면 최상단으로 올렸을때 search창이 뜸(수정 예정)
            }
        }
        Box(modifier = Modifier.padding(innerPadding)) {
            SwipeRefresh(
                state = swipeRefreshState,
                onRefresh = {
                    filterListByTab(selectedTabIndex)
                    swipeRefreshState.isRefreshing = false
                }) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (topBarVisible) {
                        SearchBar(searchQuery) { searchQuery = it }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    Button(onClick = {
                        coroutineScope.launch {
                            val response = sendDataToServer()
                            Log.d("MainScreen", "Server response: $response")
                            serverResponse = response
                        }
                    }) {
                        Text(serverResponse.getOrElse(0) { "Send Data to Server" })
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    TabRowExample { index ->
                        selectedTabIndex = index
                        filterListByTab(index)
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
                        state = scrollState,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredList) { item ->
                            when (item) {
                                is NewsItem -> {
                                    NewsCard(item)
                                    item.viewCount += 1
                                }
                                is ArticleItem -> {
                                    ArticleCard(item)
                                    item.viewCount += 1
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TabRowExample(onTabSelected: (Int) -> Unit) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("전체", "경제", "IT", "사회", "문화", "글로벌")
    TabRow(
        selectedTabIndex = selectedTabIndex,
        modifier = Modifier.fillMaxWidth()
    ) {
        tabs.forEachIndexed { index, tab ->
            Tab(
                selected = selectedTabIndex == index,
                onClick = {
                    selectedTabIndex = index
                    onTabSelected(index)
                },
                text = { Text(tab, fontSize = 10.5.sp) }
            )
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
                    modifier = Modifier.fillMaxWidth()
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
