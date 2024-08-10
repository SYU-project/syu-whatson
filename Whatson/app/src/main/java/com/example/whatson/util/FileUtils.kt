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

