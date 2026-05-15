package com.startscope.coldstart.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val StartTypeColdColor = Color(0xFF4FC3F7)
private val WarmAmber = Color(0xFFFFB74D)
private val HotGreen = Color(0xFF81C784)
private val Background = Color(0xFF121212)
private val Surface = Color(0xFF1E1E1E)

private val Scheme = darkColorScheme(
    primary = StartTypeColdColor,
    secondary = WarmAmber,
    tertiary = HotGreen,
    background = Background,
    surface = Surface,
)

@Composable
fun StartScopeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = Scheme,
        content = content,
    )
}

fun startTypeColor(startType: Int): Color =
    when (startType) {
        android.app.ApplicationStartInfo.START_TYPE_COLD -> StartTypeColdColor
        android.app.ApplicationStartInfo.START_TYPE_WARM -> WarmAmber
        android.app.ApplicationStartInfo.START_TYPE_HOT -> HotGreen
        else -> Color.LightGray
    }
