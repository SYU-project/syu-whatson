package com.example.whatson.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.example.whatson.R

// Color Palettes
private val DarkColorPalette = darkColorScheme(
    primary = Color(0xFF01439C),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF319CCD),
    onPrimaryContainer = Color.Black,
    secondary = Color(0xFF58B0D5),
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFFBCE3EC),
    onSecondaryContainer = Color.Black,
    background = Color(0xFF121212),
    onBackground = Color.White,
    surface = Color(0xFF1E1E1E),
    onSurface = Color.White
)

private val LightColorPalette = lightColorScheme(
    primary = Color(0xFF000000),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF319CCD),
    onPrimaryContainer = Color.Black,
    secondary = Color(0xFF58B0D5),
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFFBCE3EC),
    onSecondaryContainer = Color.Black,
    background = Color(0xFFFFFFFF),
    onBackground = Color.Black,
    surface = Color(0xFFFFFFFF),
    onSurface = Color.Black
)

val AppleSDGothicNeo = FontFamily(
    Font(R.font.applesdgothicneol, FontWeight.Normal)
)
val AppleSDGothicNeo1 = FontFamily(
    Font(R.font.applesdgothicneoh, FontWeight.Normal)
)

// Typography
private val AppTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = AppleSDGothicNeo,
        fontWeight = FontWeight.Normal,
        fontSize = 57.sp
    ),
    displayMedium = TextStyle(
        fontFamily = AppleSDGothicNeo,
        fontWeight = FontWeight.Normal,
        fontSize = 45.sp
    ),
    displaySmall = TextStyle(
        fontFamily = AppleSDGothicNeo,
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = AppleSDGothicNeo,
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = AppleSDGothicNeo1,
        fontWeight = FontWeight.Medium,
        fontSize = 21.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = AppleSDGothicNeo,
        fontWeight = FontWeight.Medium,
        fontSize = 24.sp
    ),
    titleLarge = TextStyle(
        fontFamily = AppleSDGothicNeo1,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp
    ),
    titleMedium = TextStyle(
        fontFamily = AppleSDGothicNeo1,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp
    ),
    titleSmall = TextStyle(
        fontFamily = AppleSDGothicNeo1 ,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = AppleSDGothicNeo,
        fontWeight = FontWeight.Normal,
        fontSize = 19.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = AppleSDGothicNeo,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp
    ),
    bodySmall = TextStyle(
        fontFamily = AppleSDGothicNeo,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp
    ),
    labelLarge = TextStyle(
        fontFamily = AppleSDGothicNeo,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp
    ),
    labelMedium = TextStyle(
        fontFamily = AppleSDGothicNeo,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp
    ),
    labelSmall = TextStyle(
        fontFamily = AppleSDGothicNeo,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp
    )
)

// Shapes
private val AppShapes = Shapes(
    small = RoundedCornerShape(20.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(20.dp)
)

@Composable
fun WhatsOnTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colorScheme = colors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
