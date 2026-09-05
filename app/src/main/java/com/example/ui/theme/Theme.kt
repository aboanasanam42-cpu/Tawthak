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
    primary = DentalTealLight,
    onPrimary = Color(0xFF003833),
    primaryContainer = DentalTealDark,
    onPrimaryContainer = DentalTealContainer,
    secondary = DentalCyanSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF0369A1),
    onSecondaryContainer = DentalCyanContainer,
    tertiary = Color(0xFFA5B4FC),
    background = DentalBgDark,
    surface = DentalSurfaceDark,
    surfaceVariant = DentalSurfaceVariantDark,
    onBackground = DentalTextPrimaryDark,
    onSurface = DentalTextPrimaryDark,
    onSurfaceVariant = DentalTextSecondaryDark
)

private val LightColorScheme = lightColorScheme(
    primary = DentalTealPrimary,
    onPrimary = Color.White,
    primaryContainer = DentalTealContainer,
    onPrimaryContainer = DentalOnTealContainer,
    secondary = DentalCyanSecondary,
    onSecondary = Color.White,
    secondaryContainer = DentalCyanContainer,
    onSecondaryContainer = DentalOnCyanContainer,
    tertiary = DentalTertiary,
    background = DentalBgLight,
    surface = DentalSurfaceLight,
    surfaceVariant = DentalSurfaceVariantLight,
    onBackground = DentalTextPrimaryLight,
    onSurface = DentalTextPrimaryLight,
    onSurfaceVariant = DentalTextSecondaryLight
)

@Composable
fun DentalVaultTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use intentional dental branding
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

// Keep alias for backward compatibility
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) = DentalVaultTheme(darkTheme, dynamicColor, content)
