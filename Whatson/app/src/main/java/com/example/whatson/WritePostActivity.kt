package com.example.whatson

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Build
import android.view.View
import android.view.WindowInsetsController
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.example.whatson.ui.theme.WhatsOnTheme
import com.example.whatson.util.Post
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material.TopAppBar
import androidx.compose.material3.MaterialTheme

@OptIn(ExperimentalMaterial3Api::class)
class WritePostActivity : ComponentActivity() {
    // 액티비티가 생성될 때 호출되는 메서드
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // 다크 모드를 비활성화하고 밝은 테마를 강제 적용
            WhatsOnTheme(darkTheme = false) {
                // 상태바 색상을 흰색으로 설정
                val statusBarColor = Color(0xFFFFFFFF)
                window.statusBarColor = statusBarColor.toArgb()

                // 안드로이드 버전에 따라 상태바의 외관을 설정
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    window.insetsController?.setSystemBarsAppearance(
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                    )
                } else {
                    // 구 버전에서는 데프리케이션된 메서드를 사용하여 상태바 외관 설정
                    @Suppress("DEPRECATION")
                    window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                }
                // 실제 화면을 호출
                WritePostScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WritePostScreen() {
    // 다크 모드가 활성화된 상태인지 확인
    val darkTheme = isSystemInDarkTheme()
    // 다크 모드에 따른 상태바 색상 설정
    val statusBarColor = if (darkTheme) Color.Black else Color.White
    val context = LocalContext.current

    // 상태바 색상을 변경하는 함수 호출
    SetStatusBarColor(statusBarColor)

    // 네비게이션 컨트롤러와 코루틴 스코프 생성
    val navController = rememberNavController()
    val coroutineScope = rememberCoroutineScope()

    // 클릭 횟수를 기억하는 상태 변수
    var clickCount by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            // 상단 바 설정
            TopAppBar(
                title = {
                    // 로고 이미지 설정
                    val painter: Painter = painterResource(id = R.drawable.zipup_magazine)
                    Image(
                        painter = painter,
                        contentDescription = "Logo",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.height(48.dp)
                    )
                },
                actions = {
                    IconButton(
                        // 아이콘 클릭 시 동작 설정 (5번 클릭 시 승인 모드로 이동)
                        onClick = {
                            clickCount++
                            if (clickCount == 5) {
                                val intent = Intent(context, PasswordEntryActivity::class.java)
                                context.startActivity(intent)
                                clickCount = 0
                            }
                        },
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .background(Color.Transparent)
                    ) {
                        // 투명 아이콘 설정
                        Icon(
                            painter = painterResource(id = R.drawable.transparent_icon),
                            contentDescription = "Transparent Icon"
                        )
                    }
                },
                // 상단 바 배경색과 그림자 설정
                backgroundColor = MaterialTheme.colorScheme.background,
                elevation = 4.dp
            )
        },
        // 하단 바 설정
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        // 입력 양식을 호출
        Box(modifier = Modifier.padding(innerPadding)) {
            WritePostForm(coroutineScope)
        }
    }
}

@Composable
fun SetStatusBarColor(color: Color) {
    // 현재 액티비티의 윈도우를 가져옴
    val window = (LocalContext.current as? Activity)?.window
    window?.let {
        // 상태바 색상을 설정
        it.statusBarColor = color.toArgb()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // 안드로이드 R 이상에서 상태바 외관 설정
            it.insetsController?.setSystemBarsAppearance(
                if (isSystemInDarkTheme()) {
                    0
                } else {
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                },
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
        } else {
            // 구 버전에서는 데프리케이션된 메서드를 사용
            @Suppress("DEPRECATION")
            it.decorView.systemUiVisibility = if (isSystemInDarkTheme()) 0 else View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    }
}

@Composable
fun WritePostForm(coroutineScope: CoroutineScope) {
    val context = LocalContext.current

    // 입력 필드 상태 변수
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var writer by remember { mutableStateOf("") }
    val imageUris = remember { mutableStateListOf<Uri?>() }

    var selectedImageIndex by remember { mutableStateOf(-1) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showCompletionDialog by remember { mutableStateOf(false) }

    // 이미지 선택기 런처
    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            imageUris.add(it)
        }
    }

    // 이미지 삭제 확인 다이얼로그 표시 함수
    fun showConfirmDeleteDialog(index: Int) {
        selectedImageIndex = index
        showDeleteDialog = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 작성자 입력 필드
        OutlinedTextField(
            value = writer,
            onValueChange = { writer = it },
            label = { Text("작성자") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        // 기사 제목 입력 필드
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("기사 제목") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        // 기사 내용 입력 필드
        OutlinedTextField(
            value = content,
            onValueChange = { content = it },
            label = { Text("기사 내용") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .height(200.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {}
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 이미지 선택 및 삭제를 위한 LazyRow
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            item {
                Spacer(modifier = Modifier.width(1.dp))
            }
            items(4) { index ->
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .clickable {
                            if (index < imageUris.size) {
                                showConfirmDeleteDialog(index)
                            } else {
                                imagePickerLauncher.launch("image/*")
                            }
                        }
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        // 이미지 박스의 테두리 모서리 설정 (ㄱ 모양)
                        val borderWidth = 2.dp.toPx()
                        val cornerSize = 20.dp.toPx()

                        drawLine(
                            color = Color.Gray,
                            start = Offset(0f, 0f),
                            end = Offset(cornerSize, 0f),
                            strokeWidth = borderWidth
                        )
                        drawLine(
                            color = Color.Gray,
                            start = Offset(0f, 0f),
                            end = Offset(0f, cornerSize),
                            strokeWidth = borderWidth
                        )

                        // ㄴ 모양 테두리 설정
                        drawLine(
                            color = Color.Gray,
                            start = Offset(0f, size.height),
                            end = Offset(0f, size.height - cornerSize),
                            strokeWidth = borderWidth
                        )
                        drawLine(
                            color = Color.Gray,
                            start = Offset(0f, size.height),
                            end = Offset(cornerSize, size.height),
                            strokeWidth = borderWidth
                        )

                        // 오른쪽 모서리 테두리 설정
                        drawLine(
                            color = Color.Gray,
                            start = Offset(size.width, 0f),
                            end = Offset(size.width - cornerSize, 0f),
                            strokeWidth = borderWidth
                        )
                        drawLine(
                            color = Color.Gray,
                            start = Offset(size.width, 0f),
                            end = Offset(size.width, cornerSize),
                            strokeWidth = borderWidth
                        )

                        drawLine(
                            color = Color.Gray,
                            start = Offset(size.width, size.height),
                            end = Offset(size.width - cornerSize, size.height),
                            strokeWidth = borderWidth
                        )
                        drawLine(
                            color = Color.Gray,
                            start = Offset(size.width, size.height),
                            end = Offset(size.width, size.height - cornerSize),
                            strokeWidth = borderWidth
                        )
                    }

                    // 이미지 박스 내부 설정
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White) // 배경색 흰색
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (index < imageUris.size && imageUris[index] != null) {
                            // 이미지가 있을 경우 이미지 로드
                            Image(
                                painter = rememberAsyncImagePainter(imageUris[index]),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            // 이미지가 없을 경우 "+" 텍스트 표시
                            Text(
                                "+",
                                color = Color.Gray,
                                fontSize = 40.sp,
                                fontWeight = FontWeight.ExtraLight,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))
            }
        }

        Spacer(modifier = Modifier.height(32.dp)) // 이미지와 버튼 사이에 간격 추가

        // 업로드 버튼
        Button(
            onClick = {
                coroutineScope.launch {
                    val imageUrls = mutableListOf<String>()

                    // 선택된 이미지를 Firebase Storage에 업로드하고 URL을 저장
                    for (uri in imageUris) {
                        uri?.let {
                            val url = uploadImageToFirebaseStorage(context, it)
                            if (url.isNotEmpty()) {
                                imageUrls.add(url)
                            }
                        }
                    }

                    // 입력된 정보로 포스트 객체 생성
                    val post = Post(
                        title = title,
                        content = content,
                        imageUrls = imageUrls,
                        writer = writer,
                        date = getCurrentDate()
                    )

                    // 생성된 포스트를 Firebase Storage에 업로드
                    uploadPostToFirebaseStorage(context, post)
                    showCompletionDialog = true // 작성 완료 후 다이얼로그 표시
                }
            },
            modifier = Modifier.align(Alignment.End),
            enabled = title.isNotEmpty() && content.isNotEmpty() && writer.isNotEmpty()
        ) {
            Text(text = "업로드 하기")
        }

        // 작성 완료 다이얼로그 표시
        if (showCompletionDialog) {
            AlertDialog(
                onDismissRequest = { showCompletionDialog = false },
                title = { Text("작성 완료") },
                text = { Text("관리자의 승인 후 업로드까지 최대 24시간이 소요될 수 있습니다.") },
                confirmButton = {
                    TextButton(onClick = { showCompletionDialog = false }) {
                        Text("확인")
                    }
                }
            )
        }
    }

    // 이미지 삭제 다이얼로그 표시
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("이미지 삭제") },
            text = { Text("이미지를 지우시겠습니까?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        imageUris.removeAt(selectedImageIndex)
                        showDeleteDialog = false
                    }
                ) {
                    Text("예")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                    }
                ) {
                    Text("아니오")
                }
            }
        )
    }
}

// 현재 날짜를 반환하는 함수
fun getCurrentDate(): String {
    val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
    return dateFormat.format(Date())
}

// Firebase Storage에 이미지를 업로드하고 다운로드 URL을 반환하는 함수
suspend fun uploadImageToFirebaseStorage(context: Context, imageUri: Uri): String {
    val storageReference = Firebase.storage.reference
    val imageRef = storageReference.child("images/${UUID.randomUUID()}")

    return try {
        // 파일 업로드
        imageRef.putFile(imageUri).await()
        // 다운로드 URL 반환
        val downloadUrl = imageRef.downloadUrl.await()
        downloadUrl.toString()
    } catch (e: Exception) {
        Toast.makeText(context, "이미지 업로드 실패: ${e.message}", Toast.LENGTH_SHORT).show()
        ""
    }
}

// Firebase Storage에 포스트 데이터를 업로드하는 함수
suspend fun uploadPostToFirebaseStorage(context: Context, post: Post) {
    val storageReference = Firebase.storage.reference
    val postRef = storageReference.child("posts/uncertificatedpost.json")

    try {
        // 기존 JSON 파일 다운로드
        val existingJsonString = try {
            postRef.getBytes(Long.MAX_VALUE).await().toString(StandardCharsets.UTF_8)
        } catch (e: Exception) {
            "[]" // 파일이 없을 경우 빈 배열 반환
        }

        // 새로운 포스트 데이터를 JSON 객체로 변환
        val jsonArray = JSONArray(existingJsonString)
        val newPostJson = JSONObject().apply {
            put("Title", post.title)
            put("Content", post.content)
            put("imageurl", post.imageUrls)
            put("writer", post.writer)
            put("date", post.date)
        }
        jsonArray.put(newPostJson)

        // 업데이트된 JSON 파일 업로드
        val updatedJsonString = jsonArray.toString()
        val inputStream = ByteArrayInputStream(updatedJsonString.toByteArray(StandardCharsets.UTF_8))
        postRef.putStream(inputStream).await() // JSON 파일 업로드
    } catch (e: Exception) {
        Toast.makeText(context, "포스트 업로드 실패: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

@Preview
@Composable
fun PreviewWritePostScreen() {
    WhatsOnTheme {
        WritePostScreen()
    }
}