package com.example.whatson

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.ContentAlpha
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.example.whatson.ui.theme.WhatsOnTheme

class PasswordEntryActivity : ComponentActivity() {
    private val correctPassword = "" // 비밀번호 설정(""사이에 입력하면 됨)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WhatsOnTheme { // 앱 테마 적용
                PasswordEntryScreen { password ->
                    if (password == correctPassword) {
                        // 비밀번호가 맞으면 ApprovePostsActivity로 이동
                        val intent = Intent(this, ApprovePostsActivity::class.java)
                        startActivity(intent)
                        finish() // 현재 Activity 종료
                    } else {
                        // 비밀번호가 틀리면 에러 메시지 표시
                        Toast.makeText(this, "비밀번호가 틀렸습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordEntryScreen(onPasswordEntered: (String) -> Unit) {
    val darkTheme = isSystemInDarkTheme()
    val statusBarColor = if (darkTheme) Color.Black else Color.White
    val view = LocalView.current
    val navController = rememberNavController()

    // 상태바 색상 설정
    SetPasswordEntryStatusBarColor(view, statusBarColor)

    var password by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("") }
            )
        },
        bottomBar = {
            BottomNavigationBar(navController = navController, onHomeClick = {})
        }

    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                TextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("비밀번호") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.textFieldColors(
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                        unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = ContentAlpha.disabled)
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { onPasswordEntered(password) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("확인", color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    }
}

@Composable
fun SetPasswordEntryStatusBarColor(view: View, color: Color) {
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
    }}
