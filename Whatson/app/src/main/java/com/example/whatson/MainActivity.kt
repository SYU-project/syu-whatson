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
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.livedata.observeAsState

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

@Composable
fun MainScreen(viewModel: MainViewModel = viewModel()) {
    val navController = rememberNavController()

    val newsList by viewModel.newsList.observeAsState(emptyList())
    val articleList by viewModel.articleList.observeAsState(emptyList())
    var mixedList by remember { mutableStateOf(viewModel.mixedList) }
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    var selectedTabIndex by remember { mutableStateOf(0) }
    var swipeRefreshState = rememberSwipeRefreshState(isRefreshing = false)

    // 각 탭의 스크롤 상태를 저장하는 맵
    val scrollStates = remember { mutableMapOf<Int, LazyListState>() }
    var currentScrollState by remember { mutableStateOf(LazyListState()) }

    // mixedList 변경 시 UI 업데이트
    LaunchedEffect(newsList, articleList) {
        mixedList = (newsList + articleList).toMutableList().also { it.shuffle() }
    }

    fun filterListByTab(index: Int) {
        val filteredList = when (index) {
            0 -> viewModel.mixedList
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
                scrollState.firstVisibleItemScrollOffset == 0 // 스크롤을 화면 최상단으로 올렸을 때 search 창이 뜸(수정 예정)
            }
        }
        Box(modifier = Modifier.padding(innerPadding)) {
            Column {
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
                    currentScrollState = scrollStates.getOrElse(selectedTabIndex) { LazyListState() }
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
                        viewModel.refreshArticles()
                        viewModel.refreshNews()
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

                // RefreshState를 업데이트하는 코드 추가
                LaunchedEffect(viewModel.mixedList) {
                    swipeRefreshState.isRefreshing = false
                    mixedList = viewModel.mixedList
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
