package com.example.whatson

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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


@Composable
fun NewsCard(newsItem: NewsItem) {
    var isFavorite by remember { mutableStateOf(false) }
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
                        saveNewsFavorites(context, updatedFavorites)
                        isFavorite = !isFavorite
                    },
                tint = if (isFavorite) Color.Red else Color.Gray
            )
        }
    }
}

@Composable
fun ArticleCard(articleItem: ArticleItem) {
    var isFavorite by remember { mutableStateOf(false) }
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
                .clickable { expanded = !expanded }
                .animateContentSize(animationSpec = tween(durationMillis = 300)),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier
                    .background(Color.LightGray)
                    .padding(16.dp)
            ) {
                Text(
                    text = articleItem.title,
                    style = MaterialTheme.typography.headlineMedium
                )

                // 이미지가 있는 경우에만 LazyRow 표시
                if (articleItem.imageUrl.isNotEmpty()) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) {
                        items(articleItem.imageUrl) { imageUrl ->
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .padding(end = 8.dp),
                                contentScale = ContentScale.Crop,
                                placeholder = painterResource(id = R.drawable.daehae),
                                error = painterResource(id = R.drawable.daehae)
                            )
                        }
                    }
                }

                Text(
                    text = articleItem.description,
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
        }
    }
}