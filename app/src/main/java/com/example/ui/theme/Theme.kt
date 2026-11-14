package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = BrandOrange,
    onPrimary = PureWhite,
    primaryContainer = BrandOrangeSoft,
    onPrimaryContainer = BrandOrangeDark,
    secondary = BrandPurple,
    onSecondary = PureWhite,
    secondaryContainer = BrandPurpleSoft,
    onSecondaryContainer = BrandPurple,
    tertiary = BrandBlue,
    onTertiary = PureWhite,
    tertiaryContainer = BrandBlueSoft,
    onTertiaryContainer = BrandBlueDeep,
    background = SoftGreyBg,
    onBackground = SlateInk,
    surface = PureWhite,
    onSurface = SlateInk,
    surfaceVariant = SoftGreySurface,
    onSurfaceVariant = SlateSecondary,
    outline = OutlineGrey,
    error = BrandRose,
    onError = PureWhite,
    errorContainer = BrandRoseSoft,
    onErrorContainer = BrandRose
)

private val DarkColorScheme = darkColorScheme(
    primary = BrandOrange,
    onPrimary = PureWhite,
    primaryContainer = Color(0xFF431A09),
    onPrimaryContainer = BrandOrangeSoft,
    secondary = Color(0xFFA78BFA),
    onSecondary = Color(0xFF1E103C),
    secondaryContainer = Color(0xFF2E1065),
    onSecondaryContainer = BrandPurpleSoft,
    tertiary = Color(0xFF60A5FA),
    onTertiary = Color(0xFF0B254E),
    tertiaryContainer = Color(0xFF172554),
    onTertiaryContainer = BrandBlueSoft,
    background = DarkBg,
    onBackground = DarkInk,
    surface = DarkSurface,
    onSurface = DarkInk,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkSecondaryInk,
    outline = DarkOutline,
    error = Color(0xFFFB7185),
    onError = Color(0xFF4C0519)
)

private val TargetSsbShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun TargetSsbTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = TargetSsbTypography,
        shapes = TargetSsbShapes,
        content = content
    )
}
