package com.example.whatson

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.example.whatson.ui.theme.WhatsOnTheme
import com.example.whatson.util.ArticleItem
import com.example.whatson.util.NewsItem
import com.example.whatson.util.loadNewsFromAssets
import com.google.gson.Gson
import com.google.gson.JsonArray
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

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val context = LocalContext.current
    var newsList by remember { mutableStateOf(listOf<NewsItem>()) }
    var articleList by remember { mutableStateOf(listOf<ArticleItem>()) }
    var mixedList by remember { mutableStateOf(listOf<Any>())}

    LaunchedEffect(Unit) {
        // assets에서 뉴스 데이터 불러오기
        val loadedNews = loadNewsFromAssets(context)
        newsList = loadedNews

        // Firebase에서 기사 데이터 가져오기
        val articles = fetchArticlesFromUrl()
        articleList = articles

        // 리스트 합치고 섞기
        val combinedList = (newsList + articleList).toMutableList()
        combinedList.shuffle()
        mixedList = combinedList
    }

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            LazyColumn {
                    items(mixedList) { item ->
                        when (item) {
                            is NewsItem -> NewsCard(item)
                            is ArticleItem -> ArticleCard(item)
                        }
                }
            }
        }
    }
}

