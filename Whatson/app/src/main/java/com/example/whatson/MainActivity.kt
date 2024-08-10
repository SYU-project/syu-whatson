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
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.navigation.compose.rememberNavController
import com.example.whatson.ui.theme.WhatsOnTheme
import com.example.whatson.util.ArticleItem
import com.example.whatson.util.NewsItem
import com.example.whatson.util.TabRowExample
import com.example.whatson.util.fetchArticlesFromUrl
import com.example.whatson.util.fetchNewsFromUrl
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
                val pagerState = rememberPagerState()
                HorizontalPager(
                    state = pagerState,
                    count = 2,
                    modifier = Modifier.fillMaxSize()
                ){
                    when (it) {
                        0 -> MainScreen()
                        1 -> NewsScreen()
                    }

            }
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
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

    val scrollStates = remember { mutableMapOf<Int, LazyListState>() }
    var currentScrollState by remember { mutableStateOf(LazyListState()) }

    var isTopBarVisible by remember { mutableStateOf(true) }
    var previousScrollPosition by remember { mutableStateOf(0) }



    LaunchedEffect(Unit) {
       /* val news = fetchNewsFromUrl()
        newsList = news*/

        val articles = fetchArticlesFromUrl()
        articleList = articles

        val combinedList = (articleList).toMutableList()
        combinedList.shuffle()
        mixedList = combinedList
        initialMixedList = combinedList

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
        mixedList = filteredList
    }

    Scaffold(
        topBar = {
            AnimatedVisibility(visible = isTopBarVisible) {
                TopBar(searchQuery) { searchQuery = it }
            }
        },
        bottomBar = {
            BottomNavigationBar(navController = navController, onHomeClick = {
                currentScrollState = LazyListState(0, 0) // Reset scroll state to top
                scrollStates[selectedTabIndex] = currentScrollState // Update the scroll state
            })
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            Column {
                AnimatedVisibility(visible = isTopBarVisible) {
                    TabRowExample(selectedTabIndex) { index ->
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
                            val combinedList = (newsList + articleList).toMutableList()
                            combinedList.shuffle()
                            mixedList = combinedList
                            swipeRefreshState.isRefreshing = false
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
    @Composable
    fun NewsScreen() {
        val navController = rememberNavController()
        var newsList by remember { mutableStateOf(listOf<NewsItem>()) }
        var mixedList by remember { mutableStateOf(listOf<Any>()) }
        var initialMixedList by remember { mutableStateOf(listOf<Any>()) }
        var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
        var selectedTabIndex by remember { mutableStateOf(0) }
        var swipeRefreshState = rememberSwipeRefreshState(isRefreshing = false)

        val scrollStates = remember { mutableMapOf<Int, LazyListState>() }
        var currentScrollState by remember { mutableStateOf(LazyListState()) }

        var isTopBarVisible by remember { mutableStateOf(true) }
        var previousScrollPosition by remember { mutableStateOf(0) }


        LaunchedEffect(Unit) {
           /* val news = fetchNewsFromUrl()
            newsList = news*/

            val combinedList = (newsList).toMutableList()
            combinedList.shuffle()
            mixedList = combinedList
            initialMixedList = combinedList

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
                else -> newsList
            }
            mixedList = filteredList
        }

        Scaffold(
            topBar = {
                AnimatedVisibility(visible = isTopBarVisible) {
                    newsTopBar(searchQuery) { searchQuery = it }
                }
            },
            bottomBar = {
                BottomNavigationBar(navController = navController, onHomeClick = {
                    currentScrollState = LazyListState(0, 0) // Reset scroll state to top
                    scrollStates[selectedTabIndex] = currentScrollState // Update the scroll state
                })
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                Column {
                    AnimatedVisibility(visible = isTopBarVisible) {
                        TabRowExample(selectedTabIndex) { index ->
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
                            val combinedList = (newsList).toMutableList()
                            combinedList.shuffle()
                            mixedList = combinedList
                            swipeRefreshState.isRefreshing = false
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
    }}


