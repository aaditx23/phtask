package com.aaditx23.phtask.presentation.screens.CourseList.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aaditx23.phtask.presentation.screens.CourseList.state.SyncStatus

@Composable
fun SyncStatusIcon(
    syncStatus: SyncStatus,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (syncStatus) {
        is SyncStatus.Idle -> {
            IconButton(onClick = onRetryClick) {
                Icon(
                    imageVector = Icons.Filled.CloudSync,
                    contentDescription = "Refresh courses",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }

        is SyncStatus.Syncing -> {
            Box(modifier = modifier.padding(12.dp)) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        is SyncStatus.Success -> {
            IconButton(onClick = onRetryClick) {
                Icon(
                    imageVector = Icons.Filled.CloudDone,
                    contentDescription = "Sync successful",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        is SyncStatus.DeviceOffline -> {
            IconButton(onClick = onRetryClick) {
                Icon(
                    imageVector = Icons.Default.CloudOff,
                    contentDescription = "Device offline - Tap to retry",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }

        is SyncStatus.NetworkError -> {
            IconButton(onClick = onRetryClick) {
                Icon(
                    imageVector = Icons.Default.ErrorOutline,
                    contentDescription = "Network error - Tap to retry",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

