package com.example.whatson

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp


@Composable
fun TopBar(searchQuery: TextFieldValue, onSearchQueryChange: (TextFieldValue) -> Unit) {
    var isSearchMode by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            if (!isSearchMode) {
                val painter: Painter = painterResource(id = R.drawable.logo) //로고 이미지 가안
                Image(
                    painter = painter,
                    contentDescription = "Logo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.height(48.dp) // 원하는 높이로 조절
                )
            } else {
                BasicTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Transparent), // 배경 : 투명(흰색)
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            if (searchQuery.text.isEmpty()) {
                                Text(
                                    text = "검색",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            }
        },
        actions = {
            IconButton(onClick = {
                isSearchMode = !isSearchMode
                if (!isSearchMode) onSearchQueryChange(TextFieldValue(""))
            }) {
                Icon(
                    /*imageVector = if (!isSearchMode) Icons.Default.Search else Icons.AutoMirrored.Filled.ArrowBack,*/
                    imageVector = Icons.Default.Search , // 검색 모드에 따른 아이콘 변경이 필요한가?
                    contentDescription = if (isSearchMode) "Back" else "검색"
                )
            }
        },
        backgroundColor = MaterialTheme.colorScheme.background
    )
}
