package com.example.myapplication.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// --- PALETA DE COLORES OSCURA ---
private val DarkColorScheme = darkColorScheme(
    primary = BluePrimaryDark,
    onPrimary = OnBluePrimaryDark,
    primaryContainer = BluePrimaryContainerDark,
    onPrimaryContainer = OnBluePrimaryContainerDark,
    secondary = TealSecondaryDark,
    onSecondary = OnTealSecondaryDark,
    secondaryContainer = TealSecondaryContainerDark,
    onSecondaryContainer = OnTealSecondaryContainerDark,
    tertiary = GreenTertiaryDark,
    onTertiary = OnGreenTertiaryDark,
    tertiaryContainer = GreenTertiaryContainerDark,
    onTertiaryContainer = OnGreenTertiaryContainerDark,
    error = RedErrorDark,
    onError = OnRedErrorDark,
    errorContainer = RedErrorContainerDark,
    onErrorContainer = OnRedErrorContainerDark
)

// --- PALETA DE COLORES CLARA ---
private val LightColorScheme = lightColorScheme(
    primary = BluePrimaryLight,
    onPrimary = OnBluePrimaryLight,
    primaryContainer = BluePrimaryContainerLight,
    onPrimaryContainer = OnBluePrimaryContainerLight,
    secondary = TealSecondaryLight,
    onSecondary = OnTealSecondaryLight,
    secondaryContainer = TealSecondaryContainerLight,
    onSecondaryContainer = OnTealSecondaryContainerLight,
    tertiary = GreenTertiaryLight,
    onTertiary = OnGreenTertiaryLight,
    tertiaryContainer = GreenTertiaryContainerLight,
    onTertiaryContainer = OnGreenTertiaryContainerLight,
    // --- INICIO DE LA CORRECCIÓN ---
    error = RedErrorLight,
    onError = OnRedErrorLight,
    // --- FIN DE LA CORRECCIÓN ---
    errorContainer = RedErrorContainerLight,
    onErrorContainer = OnRedErrorContainerLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Desactivado para usar nuestro tema
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography, // Usamos la tipografía que ya teníamos
        content = content
    )
}