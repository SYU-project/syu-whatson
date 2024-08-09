package com.example.whatson

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.TabRowDefaults.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.whatson.util.ArticleItem
import com.example.whatson.util.NewsItem
import com.example.whatson.util.loadArticleFavorites
import com.example.whatson.util.loadNewsFavorites
import com.example.whatson.util.saveArticleFavorites
import com.example.whatson.util.saveNewsFavorites
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

suspend fun sendViewsToServer(context: Context, title: String): Int? {
    val urlString = "http://210.109.52.162:5000/submit"

    return withContext(Dispatchers.IO) {
        try {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json")

            // 클라이언트 ID 가져오기 또는 생성
            val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            var clientId = sharedPreferences.getString("client_id", null)
            if (clientId == null) {
                clientId = UUID.randomUUID().toString()
                sharedPreferences.edit().putString("client_id", clientId).apply()
            }

            // 서버로 보낼 JSON 객체 생성
            val jsonObject = JsonObject().apply {
                addProperty("client_id", clientId)
                add("views", JsonArray().apply {
                    add(1) // 조회수 1 증가
                })
                addProperty("title", title) // 뉴스 또는 기사 제목
            }

            val jsonString = Gson().toJson(jsonObject)
            connection.outputStream.use { outputStream ->
                outputStream.write(jsonString.toByteArray())
                outputStream.flush()
            }

            // 서버 응답 코드 확인 및 총 조회수 파싱
            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val responseJson = Gson().fromJson(response, JsonObject::class.java)
                responseJson.get("current_views")?.asInt
            } else {
                Log.e("NetworkError", "Server returned: ${connection.responseCode}")
                null
            }
        } catch (e: Exception) {
            Log.e("NetworkError", "Exception: ${e.message}")
            null
        }
    }
}

@Composable
fun NewsCard(newsItem: NewsItem) {
    var isFavorite by remember { mutableStateOf(false) }
    var totalViews by remember { mutableStateOf(0) }
    val context = LocalContext.current

    // Load favorites only once when the composable is first launched
    val favorites = remember { mutableStateOf(loadNewsFavorites(context)) }

    // Update the isFavorite state based on the loaded favorites
    LaunchedEffect(favorites.value) {
        isFavorite = favorites.value.any { it.title == newsItem.title && it.description == newsItem.description }
    }

    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clickable {
                // 뉴스 카드 클릭 시 조회수 전송
                GlobalScope.launch {
                    val views = sendViewsToServer(context, newsItem.title)
                    if (views != null) {
                        totalViews = views
                        Log.d("NewsCard", "조회수가 서버로 성공적으로 전송되었습니다. 총 조회수: $totalViews")
                    } else {
                        Log.e("NewsCard", "조회수를 서버로 전송하는 데 실패했습니다.")
                    }
                }
            },
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .background(Color.LightGray)
                .padding(16.dp)
        ) {
            Text(
                text = newsItem.title,
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = newsItem.description,
                style = MaterialTheme.typography.bodyLarge
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "조회수: $totalViews",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Favorite",
                    modifier = Modifier
                        .clickable {
                            val updatedFavorites = favorites.value.toMutableList()
                            if (isFavorite) {
                                updatedFavorites.removeAll { it.title == newsItem.title && it.description == newsItem.description }
                            } else {
                                updatedFavorites.add(newsItem)
                            }
                            favorites.value = updatedFavorites
                            saveNewsFavorites(context, updatedFavorites)
                            isFavorite = !isFavorite
                        },
                    tint = if (isFavorite) Color.Red else Color.Gray
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ArticleCard(articleItem: ArticleItem) {
    var isFavorite by remember { mutableStateOf(false) }
    var totalViews by remember { mutableStateOf(0) }
    val context = LocalContext.current

    // Load favorites only once when the composable is first launched
    val favorites = remember { mutableStateOf(loadArticleFavorites(context)) }

    // Update the isFavorite state based on the loaded favorites
    LaunchedEffect(favorites.value) {
        isFavorite = favorites.value.any { it.title == articleItem.title && it.description == articleItem.description && it.imageUrl == articleItem.imageUrl }
    }
    var expanded by remember { mutableStateOf(false) }

    AnimatedVisibility(visible = true) {
        Card(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
                .clickable {
                    GlobalScope.launch {
                        val views = sendViewsToServer(context, articleItem.title)
                        if (views != null) {
                            totalViews = views
                            Log.d("ArticleCard", "조회수가 서버로 성공적으로 전송되었습니다. 총 조회수: $totalViews")
                        } else {
                            Log.e("ArticleCard", "조회수를 서버로 전송하는 데 실패했습니다.")
                        }
                    }

                    val intent = Intent(context, ArticleDetailViewActivity::class.java).apply {
                        putExtra("articleItem", articleItem)
                    }
                    context.startActivity(intent)
                }
                .animateContentSize(animationSpec = tween(durationMillis = 300)),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                // 이미지가 있는 경우에만 Pager 표시
                if (articleItem.imageUrl.isNotEmpty()) {
                    val pagerState = rememberPagerState(
                        pageCount = { articleItem.imageUrl.size }
                    )

                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                    ) { page ->
                        AsyncImage(
                            model = articleItem.imageUrl[page],
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            placeholder = painterResource(id = R.drawable.daehae),
                            error = painterResource(id = R.drawable.daehae)
                        )
                    }
                }
                Text(
                    text = articleItem.title,
                    style = MaterialTheme.typography.headlineMedium
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "조회수: $totalViews",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Favorite",
                        modifier = Modifier
                            .clickable {
                                val updatedFavorites = favorites.value.toMutableList()
                                if (isFavorite) {
                                    updatedFavorites.removeAll { it.title == articleItem.title && it.description == articleItem.description && it.imageUrl == articleItem.imageUrl }
                                } else {
                                    updatedFavorites.add(articleItem)
                                }
                                favorites.value = updatedFavorites
                                saveArticleFavorites(context, updatedFavorites)
                                isFavorite = !isFavorite
                            },
                        tint = if (isFavorite) Color.Red else Color.Gray
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Divider(
                    color = Color.Gray.copy(alpha = 0.6f),
                    thickness = 1.dp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }
    }
}
