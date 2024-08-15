package com.example.whatson

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.whatson.ui.theme.WhatsOnTheme
import com.example.whatson.util.ArticleItem
import com.example.whatson.util.NewsItem
import com.example.whatson.util.NewsTab
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalPagerApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WhatsOnTheme {
                val darkTheme = isSystemInDarkTheme()
                val statusBarColor = if (darkTheme) Color.Black else Color(0xFFFFFFFF) // 원하는 색상으로 변경
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
                val pagerState = rememberPagerState()
                HorizontalPager(
                    state = pagerState,
                    count = 2,
                    modifier = Modifier.fillMaxSize()
                ){
                    when (it) {
                        0 -> MainScreen()
                        1 -> NewsScreen()
                    }            }
            }
        }
    }

    @Composable
    fun MainScreen(viewModel: MainViewModel = viewModel()) {
        val navController = rememberNavController()
        val articleList by viewModel.articleList.observeAsState(emptyList())
        var mixedList by remember { mutableStateOf(viewModel.mixedList) }
        var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
        var selectedTabIndex by remember { mutableStateOf(0) }
        var swipeRefreshState = rememberSwipeRefreshState(isRefreshing = false)

        // 각 탭의 스크롤 상태를 저장하는 맵
        val scrollStates = remember { mutableMapOf<Int, LazyListState>() }
        var currentScrollState = rememberLazyListState()
        var isTopBarVisible by remember { mutableStateOf(true) }
        val trimmedQuery = searchQuery.text.trim() // 검색어의 앞뒤 공백을 제거

        // mixedList 변경 시 UI 업데이트
        LaunchedEffect(articleList) {
            mixedList = (articleList).toMutableList().also { it.shuffle() }
        }



        Scaffold(
            topBar = {
                AnimatedVisibility(visible = isTopBarVisible) {
                    TopBar(searchQuery) { searchQuery = it }
                }},
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
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // 새 탭의 스크롤 상태 로드 (없으면 최상단으로)
                    currentScrollState = scrollStates.getOrElse(selectedTabIndex) { LazyListState() }
                    if (scrollStates[selectedTabIndex] == null) {
                        scrollStates[selectedTabIndex] = LazyListState()
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))


                val filteredList = mixedList.filter { item ->
                    when (item) {
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
fun NewsScreen(viewModel: MainViewModel = viewModel()) {
    val navController = rememberNavController()
    val newsList by viewModel.newsList.observeAsState(emptyList())
    var mixedList by remember { mutableStateOf(viewModel.mixedList) }
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    var selectedTabIndex by remember { mutableStateOf(0) }
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = false)
    var isTopBarVisible by remember { mutableStateOf(true) }
    var previousScrollPosition by remember { mutableStateOf(0) }
    var currentScrollState = rememberLazyListState()
    val scrollStates = remember { mutableMapOf<Int, LazyListState>() }


    LaunchedEffect(Unit) {


        val combinedList = (newsList).toMutableList()
        combinedList.shuffle()
        scrollStates[0] = currentScrollState
    }

    fun filterListByTab(index: Int) {
        val filteredList = when (index) {
            0 -> mixedList
            1 -> newsList.filter { it.category == "economy" }
            2 -> newsList.filter { it.category == "IT" }
            3 -> newsList.filter { it.category == "society" }
            4 -> newsList.filter { it.category == "culture" }
            5 -> newsList.filter { it.category == "global" }
            else -> newsList
        }
        mixedList = filteredList
    }

    Scaffold(
        topBar = {
            AnimatedVisibility(visible = isTopBarVisible) {
                newsTopBar(searchQuery) { searchQuery = it }
            }},
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            Column {
                AnimatedVisibility(visible = isTopBarVisible) {
                    NewsTab(selectedTabIndex) { index ->
                        scrollStates[selectedTabIndex] = currentScrollState
                        selectedTabIndex = index
                        filterListByTab(index)
                        currentScrollState = scrollStates.getOrElse(selectedTabIndex) { LazyListState() }
                        if (scrollStates[selectedTabIndex] == null) {
                            scrollStates[selectedTabIndex] = LazyListState()
                        }
                    }
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

                SwipeRefresh(
                    state = swipeRefreshState,
                    onRefresh = {
                        viewModel.refreshNews()
                    }
                ) {
                    LazyColumn(
                        state = currentScrollState,
                        modifier = Modifier
                            .fillMaxSize()
                            .nestedScroll(remember {
                                object : NestedScrollConnection {
                                    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                                        val currentScrollPosition = currentScrollState.firstVisibleItemScrollOffset
                                        isTopBarVisible = previousScrollPosition >= currentScrollPosition
                                        previousScrollPosition = currentScrollPosition
                                        return super.onPreScroll(available, source)
                                    }
                                }
                            })
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

