package ch.zhaw.pa_fs25.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Gray100,
    onPrimary = Black,
    secondary = Gray90,
    onSecondary = Black,
    background = Black,
    onBackground = White,
    surface = Gray30,
    onSurface = White,
    error = Color.Red,
    onError = White
)

private val LightColorScheme = lightColorScheme(
    primary = Gray30,
    onPrimary = White,
    secondary = Gray50,
    onSecondary = White,
    background = White,
    onBackground = Black,
    surface = Gray80,
    onSurface = Black,
    error = Color.Red,
    onError = White
)

@Composable
fun PAFS25Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
