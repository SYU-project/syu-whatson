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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val _newsList = MutableLiveData<List<NewsItem>>()
    val newsList: LiveData<List<NewsItem>> get() = _newsList

    private val _articleList = MutableLiveData<List<ArticleItem>>()
    val articleList: LiveData<List<ArticleItem>> get() = _articleList

    var mixedList by mutableStateOf(listOf<Any>())
        private set

    init {
        loadArticles()
        loadNews()
    }

    private fun loadArticles() {
        viewModelScope.launch {
            val articles = fetchArticlesFromUrl()
            _articleList.value = articles
            mixedList = (articles).toMutableList().also { it.shuffle() }
        }
    }

    private fun loadNews() {
        viewModelScope.launch {
            val news = fetchNewsFromUrl()
            _newsList.value = news
            mixedList = (news + (_articleList.value ?: emptyList())).toMutableList().also { it.shuffle() }
        }
    }

    fun refreshArticles() {
        viewModelScope.launch {
            val articles = fetchArticlesFromUrl()
            _articleList.value = articles
            refreshMixedList()
        }
    }

    fun refreshNews() {
        viewModelScope.launch {
            val news = fetchNewsFromUrl()
            _newsList.value = news
            refreshMixedList()
        }
    }

    fun refreshMixedList() {
        mixedList = (_newsList.value.orEmpty() + _articleList.value.orEmpty()).toMutableList().also { it.shuffle() }
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
                    val writer = article["writer"] as? String ?: ""
                    val date = article["date"] as? String ?: "날짜미정"
                    articleItems.add(ArticleItem(title, description, imageUrl, writer, date))
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