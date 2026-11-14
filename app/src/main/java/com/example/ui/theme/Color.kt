package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Brand & Accent Colors
val BrandOrange = Color(0xFFFF6B2C)
val BrandOrangeDark = Color(0xFFE54D10)
val BrandOrangeSoft = Color(0xFFFFEFE8)

val BrandPurple = Color(0xFF7C3AED)
val BrandPurpleSoft = Color(0xFFF3EEFF)

val BrandBlue = Color(0xFF2563EB)
val BrandBlueDeep = Color(0xFF1A237E)
val BrandBlueSoft = Color(0xFFEBF2FF)

val BrandGreen = Color(0xFF059669)
val BrandGreenSoft = Color(0xFFE8FBF3)

val BrandAmber = Color(0xFFD97706)
val BrandAmberSoft = Color(0xFFFEF3C7)

val BrandRose = Color(0xFFE11D48)
val BrandRoseSoft = Color(0xFFFFE4EA)

// Neutral Light Surfaces
val PureWhite = Color(0xFFFFFFFF)
val SoftGreyBg = Color(0xFFF4F6FA)
val SoftGreySurface = Color(0xFFEAEEF6)
val OutlineGrey = Color(0xFFD8E0EC)
val SlateInk = Color(0xFF0F172A)
val SlateSecondary = Color(0xFF475569)
val SlateMuted = Color(0xFF64748B)

// Dark Mode Surfaces
val DarkBg = Color(0xFF0B1120)
val DarkSurface = Color(0xFF131C31)
val DarkSurfaceVariant = Color(0xFF1E293B)
val DarkOutline = Color(0xFF334155)
val DarkInk = Color(0xFFF8FAFC)
val DarkSecondaryInk = Color(0xFFCBD5E1)

// Signature Gradient Brushes
object TargetGradients {
    val OrangePurple = Brush.linearGradient(
        colors = listOf(BrandOrange, BrandPurple)
    )
    val BluePurple = Brush.linearGradient(
        colors = listOf(BrandBlueDeep, BrandPurple)
    )
    val BlueEmerald = Brush.linearGradient(
        colors = listOf(BrandBlue, BrandGreen)
    )
    val OrangeAmber = Brush.linearGradient(
        colors = listOf(BrandOrange, BrandAmber)
    )
    val HeroOverlay = Brush.horizontalGradient(
        colors = listOf(
            Color(0xEE0F172A),
            Color(0xCC1E1B4B),
            Color(0x557C3AED)
        )
    )
}
