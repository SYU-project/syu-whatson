package com.example.whatson.util


import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileReader
import java.io.FileWriter

data class NewsItem(
    val category: String,
    val title: String,
    val description: String
)

// ArticleItem class definition
data class ArticleItem(
    val title: String,
    val description: String,
    val imageUrl: List<String>
)

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
