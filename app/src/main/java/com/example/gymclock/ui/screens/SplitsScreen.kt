package com.example.gymclock.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.gymclock.data.PredefinedSplitEntity
import com.example.gymclock.ui.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplitsScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showApplyDialog by remember { mutableStateOf<PredefinedSplitEntity?>(null) }
    var showSnackbar by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = { Text("Workout Splits") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            }
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    "Popular Workout Splits",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Choose a proven split from The Fitness Wiki",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
            }

            items(uiState.predefinedSplits) { split ->
                SplitCard(
                    split = split,
                    onApply = { showApplyDialog = split }
                )
            }
        }
    }

    // Show snackbar message if exists
    if (showSnackbar != null) {
        LaunchedEffect(showSnackbar) {
            snackbarHostState.showSnackbar(showSnackbar!!)
            showSnackbar = null
        }
    }

    // Apply Split Dialog
    showApplyDialog?.let { split ->
        AlertDialog(
            onDismissRequest = { showApplyDialog = null },
            title = { Text("Apply ${split.name}?") },
            text = {
                Column {
                    Text("This will replace your current weekly schedule with:")
                    Spacer(Modifier.height(8.dp))
                    Text("• ${split.description}")
                    Text("• ${split.daysPerWeek} days per week")
                    Text("• ${split.difficulty} difficulty")
                    Text("• Focus: ${split.category}")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.applySplit(split.id)
                        showApplyDialog = null
                        showSnackbar = "Split applied!"
                    }
                ) {
                    Text("Apply Split")
                }
            },
            dismissButton = {
                TextButton(onClick = { showApplyDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SplitCard(
    split: PredefinedSplitEntity,
    onApply: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        split.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        split.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                DifficultyBadge(difficulty = split.difficulty)
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                InfoChip(
                    icon = Icons.Default.Schedule,
                    text = "${split.daysPerWeek} days/week"
                )
                InfoChip(
                    icon = Icons.Default.Category,
                    text = split.category
                )
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(onClick = onApply) {
                    Icon(Icons.Default.Download, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Apply Split")
                }
            }
        }
    }
}

@Composable
fun DifficultyBadge(difficulty: String) {
    val color = when (difficulty) {
        "Beginner" -> MaterialTheme.colorScheme.primary
        "Intermediate" -> MaterialTheme.colorScheme.tertiary
        "Advanced" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.secondary
    }

    Surface(
        color = color.copy(alpha = 0.1f),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            difficulty,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun InfoChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
