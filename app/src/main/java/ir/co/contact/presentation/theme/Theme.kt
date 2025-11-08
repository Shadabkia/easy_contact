package ir.co.contact.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.TileMode

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    secondary = Secondary,
    background = AppBackground,
    surface = CardBackground,
    onSurface = TextPrimary,
    error = ErrorRed,
    onBackground = TextPrimary
)

private val DarkColorScheme = darkColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    secondary = Secondary,
    background = DeepNavy1,
    surface = DeepNavy2,
    onSurface = OnPrimary,
    error = ErrorRed,
    onBackground = OnPrimary
)

@Composable
fun ContactTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors =  LightColorScheme

    MaterialTheme(
        colorScheme = colors,
        typography = ContactTypography,
        shapes = Shapes(
            small = RoundedCornerShape(10.dp),
            medium = RoundedCornerShape(14.dp),
            large = RoundedCornerShape(20.dp)
        ),
        content = content
    )
}

/** radial hero gradient (tweak center/radius to match your layout) */
val HeroGradientBrush = Brush.radialGradient(
    colors = listOf(ElectricBlue, DeepNavy2, DeepNavy1),
    center = Offset(220f, 350f),
    radius = 700f,
    tileMode = TileMode.Clamp
)
