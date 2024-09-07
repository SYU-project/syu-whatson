package com.example.whatson

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.TabRowDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.whatson.ui.theme.WhatsOnTheme
import com.google.accompanist.pager.ExperimentalPagerApi

class SettingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // 다크 모드를 강제 비활성화하고 밝은 테마로 고정
            WhatsOnTheme(darkTheme = false) {
                val statusBarColor = Color(0xFFFFFFFF) // 배경색을 흰색으로 고정
                window.statusBarColor = statusBarColor.toArgb()

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    window.insetsController?.setSystemBarsAppearance(
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                    )
                } else {
                    @Suppress("DEPRECATION")
                    window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                }
                // 원하는 화면을 호출
                SettingScreen()
            }
        }
    }
}

    @OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(viewModel: MainViewModel = viewModel()) {
    val navController = rememberNavController()
    var showSupportDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }

    if (showSupportDialog) {
        SupportDialog(onDismiss = { showSupportDialog = false })
    }
    if (showTermsDialog) {
        TermsDialog(onDismiss = { showTermsDialog = false })
    }

    if (showPrivacyDialog) {
        PrivacyPolicyDialog(onDismiss = { showPrivacyDialog = false })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "설정",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                },

                modifier = Modifier.padding(8.dp)
            )},
        bottomBar = { BottomNavigationBar(navController) }){

        innerPadding ->

        Column(modifier = Modifier.padding(innerPadding)) {
        // 도움말 및 지원 섹션
        SettingsCategory(title = "도움말 및 지원") {
            TabRowDefaults.Divider(
                color = Color.Black.copy(alpha = 0.6f),
                thickness = 1.dp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            SettingsItem(
                title = "고객 지원",
                onClick = { showSupportDialog = true            })

        }

        // 법적 정보 섹션
        SettingsCategory(title = "법적 정보") {
            TabRowDefaults.Divider(
                color = Color.Black.copy(alpha = 0.6f),
                thickness = 1.dp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            SettingsItem(title = "이용약관", onClick = {
                showTermsDialog = true            })
            SettingsItem(title = "개인정보 처리방침", onClick = {
                showPrivacyDialog = true
            })

        }
    }
    }
}

@Composable
fun SettingsCategory(
    title: String,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        content()
    }
}

@Composable
fun SupportDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            Text(text = "고객 지원")
        },
        text = {
            Text(
                text = """
                    zipup에 대해 궁금한 점이 있거나 도움이 필요하신가요?
                    
                    저희 고객 지원 팀이 항상 도와드릴 준비가 되어 있습니다.
                    아래의 이메일 또는 전화번호를 통해 언제든지 연락해 주세요.
                    
                    - 이메일: starnote320389@gmail.com
                    - 전화번호: 010-4017-9089(담당 김상훈팀장)
                    
                    여러분의 소중한 의견은 zipup을 더 나은 서비스로 만드는 데 큰 도움이 됩니다.
                    감사합니다!
                """.trimIndent()
            )
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("확인")
            }
        }
    )
}
@Composable
fun TermsDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(text = "이용약관") },
        text = {
            Text(
                text = """
                    zipup을 이용해 주셔서 감사합니다.
                    
                    본 이용약관은 zipup 서비스 이용과 관련된 규정을 명시합니다. 
                    사용자는 본 약관에 동의함으로써, zipup에서 제공하는 모든 콘텐츠와 서비스에 대한 이용 규칙을 준수해야 합니다.
                    
                    - 사용자는 zipup의 콘텐츠를 개인적인 용도로만 사용해야 하며, 상업적 목적으로 재배포하거나 이용할 수 없습니다.
                    - zipup은 서비스 제공을 위해 사용자의 정보를 수집할 수 있으며, 이 정보는 개인정보 처리방침에 따라 관리됩니다.
                    - 기타 자세한 내용은 zipup 웹사이트에서 확인하실 수 있습니다.
                    
                    이 약관은 수시로 업데이트될 수 있으므로 정기적으로 확인해 주시기 바랍니다.
                """.trimIndent()
            )
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("확인")
            }
        }
    )
}

@Composable
fun PrivacyPolicyDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(text = "개인정보 처리방침") },
        text = {
            Text(
                text = """
                    zipup은 사용자의 개인정보를 중요하게 생각하며, 이를 보호하기 위해 최선을 다하고 있습니다.
                    
                    - zipup은 서비스 제공 및 개선을 위해 최소한의 개인정보를 수집하며, 이 정보는 법적 요구사항을 준수하여 안전하게 관리됩니다.
                    - zipup은 제3자와 개인정보를 공유하지 않으며, 사용자의 동의 없이 외부에 공개하지 않습니다.
                    - zipup은 사용자가 zipup에 작성 및 게시한 모든 콘텐츠에 대해 zipup 매거진은 복제권, 배포권, 공중송신권 등 해당 저작물에 대한 저작재산권을 보유하게 됩니다. 사용자는 콘텐츠 작성 시, 본인의 게시물이 어플리케이션 내에서 공적으로 공개될 수 있음을 인지하고 이에 동의한 것으로 간주합니다.
                      
                   
                    
                    zipup의 개인정보 처리방침은 수시로 변경될 수 있으므로, 정기적으로 확인해 주시기 바랍니다.
                """.trimIndent()
            )
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("확인")
            }
        }
    )
}

@Composable
fun SettingsItem(
    title: String,

    onClick: () -> Unit
) {
    Text(
        text = title,
        style = MaterialTheme.typography.headlineMedium,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp)
    )
}











