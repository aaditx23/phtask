package com.aaditx23.phtask.presentation.screens.CourseDetails.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight

@Composable
fun PriceDisplay(
    priceUsd: Double,
    isPremium: Boolean,
    modifier: Modifier = Modifier
) {
    Text(
        text = if (isPremium) "$$priceUsd" else "Free",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = if (isPremium)
            MaterialTheme.colorScheme.primary
        else
            MaterialTheme.colorScheme.secondary,
        modifier = modifier
    )
}

