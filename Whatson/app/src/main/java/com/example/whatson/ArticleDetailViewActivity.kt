package com.example.whatson

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.TabRowDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.whatson.ui.theme.WhatsOnTheme
import com.example.whatson.util.ArticleItem

class ArticleDetailViewActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WhatsOnTheme {
                val darkTheme = isSystemInDarkTheme()
                val statusBarColor = if (darkTheme) Color.Black else Color(0xFFFFFFFF)
                window.statusBarColor = statusBarColor.toArgb()

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
                    windowInsetsController.isAppearanceLightStatusBars = !darkTheme
                } else {
                    @Suppress("DEPRECATION")
                    window.decorView.systemUiVisibility = if (darkTheme) 0 else View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                }
                val articleItem = intent.getSerializableExtra("articleItem") as? ArticleItem
                DetailScreen(articleItem)

            }
        }
    }
}
@OptIn(ExperimentalFoundationApi::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun DetailScreen(articleItem: ArticleItem?) {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .background(Color.White),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "미정",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                textAlign = TextAlign.Center
            )

            if (articleItem != null) {
                Text(
                    text = articleItem.title,
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Text(
                    text = "By " + articleItem.writer,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
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
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = articleItem.date,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    textAlign = TextAlign.End
                )
                Text(
                    text = articleItem.description,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                TabRowDefaults.Divider(
                    color = Color.Gray.copy(alpha = 0.6f),
                    thickness = 1.dp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "더 볼만한 기사",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(vertical = 8.dp),
                    textAlign = TextAlign.Start
                )
                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {/*
                        rowItems.forEach { item ->
                            ArticleCard(item, Modifier.weight(1f).padding(8.dp))
                        }
                        if (rowItems.size < 2) {
                            Spacer(modifier = Modifier.weight(1f))*/
                        }
                    }
                }
            }
        }
    }



