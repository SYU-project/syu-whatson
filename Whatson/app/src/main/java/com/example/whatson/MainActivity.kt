package com.example.whatson

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.whatson.ui.theme.WhatsOnTheme

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

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val newsList = remember { mutableStateListOf<NewsItem>() }

    // 이곳에 서버에서 뉴스 데이터를 불러오는 로직을 추가하세요.
    LaunchedEffect(Unit) {
        // 예시 데이터
        newsList.addAll(
            listOf(
                NewsItem("정부, 새로운 교육 정책 발표", "정부가 학생들의 학습 환경 개선과 교사 업무 부담 완화를 위해 더 많은 예산 투입과 연수 프로그램 확대를 발표했어요."),
                NewsItem("서울 시내 대규모 교통사고 발생", "서울 시내에서 대규모 교통사고 발생, 20명 이상 부상. 경찰과 소방당국이 구조 작업과 사고 원인 조사 중이에요."),
                NewsItem("한국은행, 기준금리 0.25% 인상 발표", "한국은행이 물가 상승과 경제 회복을 고려해 기준금리를 0.25% 인상했어요.")
            )
        )
    }

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            LazyColumn {
                items(newsList) { newsItem ->
                    NewsCard(newsItem)
                }
            }
        }
    }
}
