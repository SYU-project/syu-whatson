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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
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

@OptIn(ExperimentalMaterial3Api::class)
class WritePostActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WhatsOnTheme {
                WritePostScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WritePostScreen() {
    val darkTheme = isSystemInDarkTheme()
    val statusBarColor = if (darkTheme) Color.Black else Color.White
    val context = LocalContext.current

    SetStatusBarColor(statusBarColor)

    val navController = rememberNavController()
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Our Magazine") },
                actions = {
                    IconButton(
                        onClick = {
                            val intent = Intent(context, PasswordEntryActivity::class.java)
                            context.startActivity(intent)
                        },
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Settings"
                        )
                    }
                }
            )
        },
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            WritePostForm(coroutineScope)
        }
    }
}

@Composable
fun SetStatusBarColor(color: Color) {
    val window = (LocalContext.current as? Activity)?.window
    window?.let {
        it.statusBarColor = color.toArgb()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            it.insetsController?.setSystemBarsAppearance(
                if (isSystemInDarkTheme()) {
                    0
                } else {
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                },
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
        } else {
            @Suppress("DEPRECATION")
            it.decorView.systemUiVisibility = if (isSystemInDarkTheme()) 0 else View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    }
}

@Composable
fun WritePostForm(coroutineScope: CoroutineScope) {
    val context = LocalContext.current

    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var writer by remember { mutableStateOf("") }
    val imageUris = remember { mutableStateListOf<Uri?>() }

    var selectedImageIndex by remember { mutableStateOf(-1) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showCompletionDialog by remember { mutableStateOf(false) }

    // 이미지 선택기
    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            imageUris.add(it)
        }
    }

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
        OutlinedTextField(
            value = writer,
            onValueChange = { writer = it },
            label = { Text("작성자") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("기사 제목") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

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
                onDone = {
                    // Handle action when "Done" button is pressed on keyboard
                }
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            items(4) { index ->
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .background(Color.Gray, RoundedCornerShape(8.dp))
                        .clickable {
                            if (index < imageUris.size) {
                                showConfirmDeleteDialog(index)
                            } else {
                                imagePickerLauncher.launch("image/*")
                            }
                        }
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (index < imageUris.size && imageUris[index] != null) {
                        Image(
                            painter = rememberAsyncImagePainter(imageUris[index]),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text(
                            "+",
                            color = Color.White,
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Light,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                coroutineScope.launch {
                    val imageUrls = mutableListOf<String>()

                    for (uri in imageUris) {
                        uri?.let {
                            val url = uploadImageToFirebaseStorage(context, it)
                            if (url.isNotEmpty()) {
                                imageUrls.add(url)
                            }
                        }
                    }

                    val post = Post(
                        title = title,
                        content = content,
                        imageUrls = imageUrls,
                        writer = writer,
                        date = getCurrentDate()
                    )

                    uploadPostToFirebaseStorage(context, post)
                    showCompletionDialog = true // 작성 완료 후 다이얼로그 표시
                }
            },
            modifier = Modifier.align(Alignment.End),
            enabled = title.isNotEmpty() && content.isNotEmpty() && writer.isNotEmpty()
        ) {
            Text(text = "업로드 하기")
        }

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

fun getCurrentDate(): String {
    val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
    return dateFormat.format(Date())
}

suspend fun uploadImageToFirebaseStorage(context: Context, imageUri: Uri): String {
    val storageReference = Firebase.storage.reference
    val imageRef = storageReference.child("images/${UUID.randomUUID()}")

    return try {
        imageRef.putFile(imageUri).await()
        val downloadUrl = imageRef.downloadUrl.await()
        downloadUrl.toString()
    } catch (e: Exception) {
        Toast.makeText(context, "이미지 업로드 실패: ${e.message}", Toast.LENGTH_SHORT).show()
        ""
    }
}

suspend fun uploadPostToFirebaseStorage(context: Context, post: Post) {
    val storageReference = Firebase.storage.reference
    val postRef = storageReference.child("posts/uncertificatedpost.json")

    try {
        // 기존 JSON 파일 다운로드
        val existingJsonString = try {
            postRef.getBytes(Long.MAX_VALUE).await().toString(StandardCharsets.UTF_8)
        } catch (e: Exception) {
            // 파일이 존재하지 않거나 다운로드 실패 시 빈 JSON 배열 반환
            "[]"
        }

        val jsonArray = JSONArray(existingJsonString)
        val newPostJson = JSONObject().apply {
            put("Title", post.title)
            put("Content", post.content)
            put("imageurl", post.imageUrls)
            put("writer", post.writer)
            put("date", post.date)
        }
        jsonArray.put(newPostJson)

        //업데이트된 JSON 파일 업로드
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
