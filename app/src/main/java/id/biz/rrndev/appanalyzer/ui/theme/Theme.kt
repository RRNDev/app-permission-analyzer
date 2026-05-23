package id.biz.rrndev.appanalyzer.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFFFF7418),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE0CC),
    onPrimaryContainer = Color(0xFF4A1C00),
    secondary = Color(0xFFC97B2F),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF5D5A0),
    onSecondaryContainer = Color(0xFF3E1E00),
    tertiary = Color(0xFF6B7F4A),
    onTertiary = Color.White,
    background = Color(0xFFFAFAF9),
    onBackground = Color(0xFF1A1210),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1A1210),
    surfaceVariant = Color(0xFFF5EDE6),
    onSurfaceVariant = Color(0xFF5C4033),
    outline = Color(0xFFD4C5BB),
    error = Color(0xFFE03120),
    onError = Color.White,
    errorContainer = Color(0xFFFFDED8),
    onErrorContainer = Color(0xFF5C1308)
)

@Composable
fun AppAnalyzerTheme(
    useDynamicColor: Boolean,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}