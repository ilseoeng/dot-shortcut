package app.dotshortcut.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Accent = Color(0xFFFF7A2B)
val ScreenBg = Color(0xFFF7F5F3)
val CardBg = Color(0xFFFFFFFF)
val TextPrimary = Color(0xFF111820)
val TextSecondary = Color(0xFF666A70)
val ChipWarnBg = Color(0xFFF6E8D9)
val ChipWarnFg = Color(0xFF9A5A14)
val ChipOkBg = Color(0xFFE3F0E4)
val ChipOkFg = Color(0xFF2F6B3C)
val OutlineSoft = Color(0xFFE9E6E3)
val PeachTile = Color(0xFFFFEFE3)
val NeutralTile = Color(0xFFF3F1EF)

private val LightColors = lightColorScheme(
    primary = Accent,
    onPrimary = Color.White,
    background = ScreenBg,
    onBackground = TextPrimary,
    surface = CardBg,
    onSurface = TextPrimary,
    surfaceVariant = NeutralTile,
    onSurfaceVariant = TextSecondary,
    outline = OutlineSoft,
    secondary = TextSecondary,
)

@Composable
fun DotShortcutTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = LightColors, content = content)
}
