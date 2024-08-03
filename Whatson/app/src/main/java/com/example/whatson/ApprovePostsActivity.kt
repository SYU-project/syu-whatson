package com.example.whatson

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsetsController
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.TabRowDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.whatson.ui.theme.WhatsOnTheme
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

data class ArticleItem(
    val title: String,
    val description: String,
    val imageUrl: List<String>,
    val writer: String,
    val date: String
)

class ApprovePostsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WhatsOnTheme {
                ApprovePostsScreen(context = this)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApprovePostsScreen(context: Context) {
    var postList by remember { mutableStateOf(listOf<ArticleItem>()) } // 게시물 목록
    var selectedPosts by remember { mutableStateOf(setOf<ArticleItem>()) } // 선택된 게시물 목록
    var isLoading by remember { mutableStateOf(true) } // 로딩 상태
    var showConfirmationDialog by remember { mutableStateOf(false) } // 확인 대화상자 표시 여부
    val coroutineScope = rememberCoroutineScope()
    val isButtonEnabled = selectedPosts.isNotEmpty() // 승인 버튼 활성화 상태

    val view = LocalView.current
    // 상태바 색상 설정
    val SetApprovePostsStatusBarColor = if (isLoading) Color.Gray else MaterialTheme.colorScheme.background
    SetApprovePostsStatusBarColor(view, SetApprovePostsStatusBarColor)

    val navController = rememberNavController()

    LaunchedEffect(Unit) {
        try {
            val posts = fetchArticlesFromApproveUrl()
            postList = posts
        } catch (e: Exception) {
            Log.e("ApprovePostsScreen", "Error loading posts: ${e.message}")
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Approve Posts") }
            )
        },
        bottomBar = {
            BottomNavigationBar(navController = navController, onHomeClick = {})
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { if (isButtonEnabled) showConfirmationDialog = true },
                containerColor = if (isButtonEnabled) MaterialTheme.colorScheme.primary else Color.DarkGray,
                contentColor = if (isButtonEnabled) MaterialTheme.colorScheme.onPrimary else Color.LightGray
            ) {
                Icon(Icons.Filled.Check, contentDescription = "Approve")
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 64.dp) // 승인 버튼 높이 패딩
                ) {
                    items(postList) { post ->
                        PostItem(
                            post = post,
                            isSelected = post in selectedPosts,
                            onSelectionChanged = { isSelected ->
                                selectedPosts = if (isSelected) {
                                    selectedPosts + post
                                } else {
                                    selectedPosts - post
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }

    // 확인 대화상자
    if (showConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmationDialog = false },
            title = { Text("승인 확인") },
            text = { Text("선택된 기사들을 승인합니다.") },
            confirmButton = {
                TextButton(onClick = {
                    coroutineScope.launch {
                        try {
                            saveApprovedPostsToFirebaseStorage(selectedPosts)

                            updateUncertificatedPosts(selectedPosts, context)

                            postList = postList.filterNot { it in selectedPosts }
                            selectedPosts = emptySet()
                        } catch (e: Exception) {
                            Log.e("ApprovePostsScreen", "Error during approval: ${e.message}")
                        } finally {
                            showConfirmationDialog = false
                        }
                    }
                }) {
                    Text("예")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmationDialog = false }) {
                    Text("아니요")
                }
            }
        )
    }
}

// 개별 게시물 항목 표시
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PostItem(post: ArticleItem, isSelected: Boolean, onSelectionChanged: (Boolean) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .background(Color.White)
                .padding(16.dp)
        ) {
            if (post.imageUrl.isNotEmpty()) {
                val pagerState = rememberPagerState(
                    pageCount = { post.imageUrl.size }
                )

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) { page ->
                    AsyncImage(
                        model = post.imageUrl[page],
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(id = R.drawable.daehae),
                        error = painterResource(id = R.drawable.daehae)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = post.title,
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = post.description,
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = post.date,
                    style = MaterialTheme.typography.bodySmall
                )

                Text(
                    text = post.writer,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 체크박스를 카드의 오른쪽 끝에 위치시키기 위한 Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.weight(1f))

                Checkbox(
                    checked = isSelected,
                    onCheckedChange = onSelectionChanged
                )
            }
            // 구분선
            Divider(
                color = Color.Gray.copy(alpha = 0.6f),
                thickness = 1.dp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

// 상태바 색상
@Composable
fun SetApprovePostsStatusBarColor(view: View, color: Color) {
    val activity = LocalContext.current as? Activity
    activity?.window?.let {
        it.statusBarColor = color.toArgb()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            it.insetsController?.setSystemBarsAppearance(
                if (color == Color.Black) 0 else WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
        } else {
            @Suppress("DEPRECATION")
            it.decorView.systemUiVisibility = if (color == Color.Black) 0 else View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    }
}

// 승인 대기 게시물 목록
suspend fun fetchArticlesFromApproveUrl(): List<ArticleItem> {
    val urlString = "https://firebasestorage.googleapis.com/v0/b/whatson-93370.appspot.com/o/posts%2Funcertificatedpost.json?alt=media&token=3c1fa9fc-1454-4260-80f4-74cc6998dfa6"

    return withContext(Dispatchers.IO) {
        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"

        if (connection.responseCode == HttpURLConnection.HTTP_OK) {
            val jsonString = connection.inputStream.bufferedReader().use { it.readText() }
            Log.d("JSONResponse", "Response: $jsonString")

            val gson = Gson()
            val listType = object : TypeToken<List<Map<String, Any>>>() {}.type
            val posts: List<Map<String, Any>> = gson.fromJson(jsonString, listType)

            val articleItems = posts.map { post ->
                val title = (post["Title"] as? String)?.takeIf { it.isNotBlank() } ?: "No Title"
                val description = (post["Content"] as? String)?.takeIf { it.isNotBlank() } ?: "No Content"
                val imageUrlString = (post["imageurl"] as? String)?.trim('[')?.trim(']') ?: ""
                val imageUrl = if (imageUrlString.isNotBlank()) {
                    imageUrlString.split(',').map { it.trim().removePrefix("\"").removeSuffix("\"") }
                } else {
                    emptyList()
                }
                val writer = (post["writer"] as? String)?.takeIf { it.isNotBlank() } ?: "Unknown"
                val date = (post["date"] as? String)?.takeIf { it.isNotBlank() } ?: "Unknown"

                ArticleItem(title, description, imageUrl, writer, date)
            }

            Log.d("ArticleData", articleItems.toString())

            articleItems
        } else {
            Log.e("NetworkError", "Response code: ${connection.responseCode}")
            emptyList()
        }
    }
}

// 승인된 게시물을 Firebase Storage에 저장
suspend fun saveApprovedPostsToFirebaseStorage(posts: Set<ArticleItem>) {
    val fileName = "approvepost.txt"
    val gson = GsonBuilder().disableHtmlEscaping().create()

    val storageRef = FirebaseStorage.getInstance().reference
    val fileRef = storageRef.child(fileName)

    val existingJson = fetchJsonFromFirebaseStorage(fileRef)

    val existingPosts = try {
        val listType = object : TypeToken<List<Map<String, Any>>>() {}.type
        gson.fromJson<List<Map<String, Any>>>(existingJson, listType)
    } catch (e: Exception) {
        emptyList<Map<String, Any>>()
    }

    val newPosts = posts.map { post ->
        mapOf(
            "Title" to post.title,
            "Content" to post.description,
            "imageurl" to post.imageUrl,
            "writer" to post.writer,
            "date" to post.date
        )
    }

    val allPosts = existingPosts + newPosts // 기존게시물 + 새 게시물

    val json = gson.toJson(allPosts)
    val stream = ByteArrayInputStream(json.toByteArray(Charsets.UTF_8))

    fileRef.putStream(stream).await()
}

// Firebase Storage에서 JSON 데이터 가져오기
suspend fun fetchJsonFromFirebaseStorage(fileRef: StorageReference): String {
    return withContext(Dispatchers.IO) {
        try {
            val url = fileRef.downloadUrl.await()
            val connection = URL(url.toString()).openConnection() as HttpURLConnection
            connection.inputStream.bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            ""
        }
    }
}

// 승인된 게시물 제외한 나머지 게시물 목록을 업데이트
suspend fun updateUncertificatedPosts(posts: Set<ArticleItem>, context: Context) {
    val storageRef = FirebaseStorage.getInstance().reference
    val fileRef = storageRef.child("posts/uncertificatedpost.json")

    val existingJson = fetchJsonFromFirebaseStorage(fileRef)
    val gson = Gson()
    val listType = object : TypeToken<List<Map<String, Any>>>() {}.type

    val existingPosts: List<Map<String, Any>> = gson.fromJson(existingJson, listType)
    // 나머지 게시물 목록 생성
    val updatedPosts = existingPosts.filterNot { post ->
        posts.any {
            it.title == post["Title"] &&
                    it.description == post["Content"] &&
                    it.writer == post["writer"] &&
                    it.date == post["date"]
        }
    }

    val updatedJson = gson.toJson(updatedPosts)
    val inputStream: InputStream = ByteArrayInputStream(updatedJson.toByteArray())

    withContext(Dispatchers.IO) {
        fileRef.putStream(inputStream).await()
    }
}
