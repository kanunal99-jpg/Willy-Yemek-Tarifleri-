package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = CulinaryPrimaryDark,
    onPrimary = Color(0xFF5A1A00),
    primaryContainer = CulinaryPrimaryVariant,
    onPrimaryContainer = CulinaryPrimaryLight,
    secondary = CulinarySecondaryDark,
    onSecondary = Color(0xFF00390E),
    secondaryContainer = Color(0xFF1B5E20),
    onSecondaryContainer = CulinarySecondaryLight,
    tertiary = CulinaryTertiaryDark,
    onTertiary = Color(0xFF452B00),
    background = WarmBackgroundDark,
    onBackground = OnWarmSurfaceDark,
    surface = WarmSurfaceDark,
    onSurface = OnWarmSurfaceDark,
    surfaceVariant = WarmSurfaceVariantDark,
    onSurfaceVariant = Color(0xFFD7CCC8),
    outline = OutlineWarmDark
)

private val LightColorScheme = lightColorScheme(
    primary = CulinaryPrimary,
    onPrimary = Color.White,
    primaryContainer = CulinaryPrimaryLight,
    onPrimaryContainer = Color(0xFF3E0A00),
    secondary = CulinarySecondary,
    onSecondary = Color.White,
    secondaryContainer = CulinarySecondaryLight,
    onSecondaryContainer = Color(0xFF002206),
    tertiary = CulinaryTertiary,
    onTertiary = Color.White,
    background = WarmBackgroundLight,
    onBackground = OnWarmSurfaceLight,
    surface = WarmSurfaceLight,
    onSurface = OnWarmSurfaceLight,
    surfaceVariant = WarmSurfaceVariantLight,
    onSurfaceVariant = Color(0xFF5D4037),
    outline = OutlineWarmLight
)

@Composable
fun WillyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Preserve our distinctive warm culinary palette by default
    content: @Composable () -> Unit,
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

// Keep alias for compatibility
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    WillyTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}
