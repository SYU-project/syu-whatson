package com.example.whatson

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsetsController
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
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
                val darkTheme = isSystemInDarkTheme()
                val statusBarColor = if (darkTheme) Color.Black else Color(0xFFFFFFFF) // 원하는 색상으로 변경
                window.statusBarColor = statusBarColor.toArgb()

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    window.insetsController?.setSystemBarsAppearance(
                        if (darkTheme) {
                            0
                        } else {
                            WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                        },
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                    )
                } else {
                    @Suppress("DEPRECATION")
                    window.decorView.systemUiVisibility = if (darkTheme) 0 else View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                }
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
                    val writer = article["writer"] as? String ?:""// 글쓴이 추출
                    val date = article ["date"] as? String ?:"날짜미정" //날짜 추출
                    articleItems.add(ArticleItem(title, description, imageUrl,writer,date)) // ArticleItem 객체 생성 및 리스트에 추가
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
    var mixedList by remember { mutableStateOf(listOf<Any>()) }
    var initialMixedList by remember { mutableStateOf(listOf<Any>()) }
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    var selectedTabIndex by remember { mutableStateOf(0) }
    var swipeRefreshState = rememberSwipeRefreshState(isRefreshing = false)

    // 각 탭의 스크롤 상태를 저장하는 맵
    val scrollStates = remember { mutableMapOf<Int, LazyListState>() }
    var currentScrollState by remember { mutableStateOf(LazyListState()) }

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
        mixedList = combinedList
        initialMixedList = combinedList

        // 초기 스크롤 상태 저장
        scrollStates[0] = currentScrollState
    }
    fun filterListByTab(index: Int) {
        val filteredList = when (index) {
            0 -> initialMixedList
            1 -> newsList.filter { it.category == "economy" }
            2 -> newsList.filter { it.category == "IT" }
            3 -> newsList.filter { it.category == "society" }
            4 -> newsList.filter { it.category == "culture" }
            5 -> newsList.filter { it.category == "global" }
            else -> newsList + articleList
        }
        mixedList = filteredList // 필터링된 리스트를 섞지 않고 mixedList에 할당
    }

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        val scrollState = rememberLazyListState()
        val topBarVisible by remember {
            derivedStateOf {
                scrollState.firstVisibleItemScrollOffset == 0//스크롤을 화면 최상단으로 올렸을때 search창이 뜸(수정 예정)
            }
        }
        Box(modifier = Modifier.padding(innerPadding)) {
            Column() {
                if (topBarVisible) {
                    SearchBar(searchQuery) { searchQuery = it }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                TabRowExample(selectedTabIndex) { index ->
                    // 현재 탭의 스크롤 상태 저장
                    scrollStates[selectedTabIndex] = currentScrollState

                    selectedTabIndex = index
                    filterListByTab(index)

                    // 새 탭의 스크롤 상태 로드 (없으면 최상단으로)
                    currentScrollState =
                        scrollStates.getOrElse(selectedTabIndex) { LazyListState() }
                    if (scrollStates[selectedTabIndex] == null) {
                        scrollStates[selectedTabIndex] = LazyListState()
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                val trimmedQuery = searchQuery.text.trim() // 검색어의 앞뒤 공백을 제거
                val filteredList = mixedList.filter { item ->
                    when (item) {
                        is NewsItem -> item.title.contains(
                            trimmedQuery,
                            ignoreCase = true
                        ) || // 항목이 NewsItem 타입이면, 제목이 공백이 제거된 검색어를 포함하는지 확인 (대소문자 구분 없음)
                                item.description.contains(trimmedQuery, ignoreCase = true)

                        is ArticleItem -> item.title.contains(
                            trimmedQuery,
                            ignoreCase = true
                        ) || // 항목이 ArticleItem 타입이면, 제목이 공백이 제거된 검색어를 포함하는지 확인 (대소문자 구분 없음)
                                item.description.contains(trimmedQuery, ignoreCase = true)

                        else -> false // 다른 타입이면, 필터링된 리스트에 포함시키지 않음
                    }
                }
                SwipeRefresh(
                    state = swipeRefreshState,
                    onRefresh = {
                        val combinedList = (newsList + articleList).toMutableList()
                        combinedList.shuffle()
                        mixedList = combinedList
                        swipeRefreshState.isRefreshing = false
                    }
                ) {
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
}

@Composable
fun TabRowExample(selectedTabIndex: Int, onTabSelected: (Int) -> Unit) {
    val tabs = listOf("전체", "경제", "IT", "사회", "문화", "글로벌")

    TabRow(
        selectedTabIndex = selectedTabIndex,
        modifier = Modifier.fillMaxWidth()
    ) {
        tabs.forEachIndexed { index, tab ->
            Tab(
                selected = selectedTabIndex == index,
                onClick = {
                    onTabSelected(index) // 탭이 선택될 때 필터링 함수 호출
                },
                text = { Text(tab, style = MaterialTheme.typography.titleMedium) } // 글자 크기 조정
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
            .padding(8.dp) // Add padding around the box if needed
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
                .padding(16.dp), // Adjust padding inside the text field if needed
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
