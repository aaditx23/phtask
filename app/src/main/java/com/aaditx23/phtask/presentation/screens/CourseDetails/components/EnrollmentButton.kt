package com.aaditx23.phtask.presentation.screens.CourseDetails.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun EnrollmentButton(
    isEnrolled: Boolean,
    isLoading: Boolean,
    onEnrollClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isEnrolled) {
        OutlinedButton(
            onClick = onEnrollClick,
            enabled = !isLoading,
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Unenroll From Course")
            }
        }
    } else {
        FilledTonalButton(
            onClick = onEnrollClick,
            enabled = !isLoading,
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)

        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Enroll Now")
            }
        }
    }
}

