package com.example.whatson.util


import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.BufferedReader
import java.io.InputStreamReader

data class NewsItem(val title: String, val description: String)

fun saveFavorites(context: Context, favorites: List<NewsItem>) {
    val gson = Gson()
    val jsonString = gson.toJson(favorites)
    val file = File(context.cacheDir, "favorites.json")
    FileWriter(file).use {
        it.write(jsonString)
    }
}

fun loadFavorites(context: Context): List<NewsItem> {
    val file = File(context.cacheDir, "favorites.json")
    if (!file.exists()) return emptyList()

    val gson = Gson()
    val itemType = object : TypeToken<List<NewsItem>>() {}.type
    FileReader(file).use {
        return gson.fromJson(it, itemType)
    }
}
fun loadNewsFromAssets(context: Context): List<NewsItem> {
    val newsList = mutableListOf<NewsItem>()
    try {
        val inputStream = context.assets.open("news.txt")
        val reader = BufferedReader(InputStreamReader(inputStream))
        var line = reader.readLine()
        while (line != null) {
            val parts = line.split("|")
            if (parts.size == 2) {
                val title = parts[0]
                val description = parts[1]
                newsList.add(NewsItem(title, description))
            }
            line = reader.readLine()
        }
        reader.close()
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return newsList
}
