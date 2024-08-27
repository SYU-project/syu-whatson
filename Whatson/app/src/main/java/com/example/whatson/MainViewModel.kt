package com.example.whatson


import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.whatson.util.ArticleItem
import com.example.whatson.util.NewsItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

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

    val jsonString = withContext(Dispatchers.IO) {
        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "GET"

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                null
            }
        } finally {
            connection.disconnect()
        }
    }

    return jsonString?.let {
        withContext(Dispatchers.Default) {
            val gson = Gson()
            val mapType = object : TypeToken<Map<String, List<Map<String, Any>>>>() {}.type
            val articlesMap: Map<String, List<Map<String, Any>>> = gson.fromJson(it, mapType)
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
        }
    } ?: emptyList()
}

suspend fun fetchNewsFromUrl(): List<NewsItem> {
    val urlString = "http://210.109.52.162:5000/summaries"

    val jsonString = withContext(Dispatchers.IO) {
        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "GET"

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                null
            }
        } finally {
            connection.disconnect()
        }
    }

    return jsonString?.let {
        withContext(Dispatchers.Default) {
            val gson = Gson()
            val listType = object : TypeToken<List<Map<String, String>>>() {}.type
            val newsList: List<Map<String, String>> = gson.fromJson(it, listType)

            val newsItems = newsList.map { article ->
                val category = article["category"] ?: ""
                val title = article["title"] ?: ""
                val description = article["summary"] ?: ""
                NewsItem(category, title, description)
            }
            Log.d("NewsData", newsItems.toString())
            newsItems
        }
    } ?: emptyList()
}