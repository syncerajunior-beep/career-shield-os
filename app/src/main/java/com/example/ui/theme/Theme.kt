package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val CareerShieldColorScheme = darkColorScheme(
    primary = ShieldCyan,
    onPrimary = ShieldObsidian,
    primaryContainer = ShieldSlateCard,
    onPrimaryContainer = ShieldCyan,
    secondary = ShieldAmberWarn,
    onSecondary = ShieldObsidian,
    secondaryContainer = ShieldSurfaceVariant,
    onSecondaryContainer = ShieldTextWhite,
    tertiary = ShieldEmeraldSuccess,
    onTertiary = ShieldObsidian,
    error = ShieldRedAlert,
    onError = ShieldTextWhite,
    background = ShieldObsidian,
    onBackground = ShieldTextWhite,
    surface = ShieldNavyBar,
    onSurface = ShieldTextWhite,
    surfaceVariant = ShieldDarkCard,
    onSurfaceVariant = ShieldTextLight,
    outline = ShieldBorderGray
)

@Composable
fun CareerShieldAndroidTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CareerShieldColorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    CareerShieldAndroidTheme(content = content)
}
