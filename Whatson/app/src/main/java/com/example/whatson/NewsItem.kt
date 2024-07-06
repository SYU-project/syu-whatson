package com.example.whatson

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.whatson.util.NewsItem
import com.example.whatson.util.loadFavorites
import com.example.whatson.util.saveFavorites


@Composable
fun NewsCard(newsItem: NewsItem) {
    var isFavorite by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Load favorites only once when the composable is first launched
    val favorites = remember { mutableStateOf(loadFavorites(context)) }

    // Update the isFavorite state based on the loaded favorites
    LaunchedEffect(favorites.value) {
        isFavorite = favorites.value.any { it.title == newsItem.title && it.description == newsItem.description }
    }

    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
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
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = "Favorite",
                modifier = Modifier
                    .align(Alignment.End)
                    .clickable {
                        val updatedFavorites = favorites.value.toMutableList()
                        if (isFavorite) {
                            updatedFavorites.removeAll { it.title == newsItem.title && it.description == newsItem.description }
                        } else {
                            updatedFavorites.add(newsItem)
                        }
                        favorites.value = updatedFavorites
                        saveFavorites(context, updatedFavorites)
                        isFavorite = !isFavorite
                    },
                tint = if (isFavorite) Color.Red else Color.Gray
            )
        }
    }
}
