package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NavigateBefore
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Recipe
import com.example.ui.theme.CulinaryPrimary
import com.example.ui.theme.CulinarySecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CookingModeDialog(
    recipe: Recipe,
    remainingSeconds: Int,
    totalSeconds: Int,
    isTimerRunning: Boolean,
    onToggleTimer: () -> Unit,
    onResetTimer: () -> Unit,
    onClose: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var currentStepIndex by remember { mutableIntStateOf(0) }

    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val formattedTime = String.format("%02d:%02d", minutes, seconds)

    val progress = if (totalSeconds > 0) {
        1f - (remainingSeconds.toFloat() / totalSeconds.toFloat())
    } else 0f

    ModalBottomSheet(
        onDismissRequest = onClose,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("cooking_mode_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(CulinaryPrimary.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = CulinaryPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Willy Mutfak Sayacı",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                IconButton(onClick = onClose) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Kapat")
                }
            }

            Text(
                text = recipe.title,
                style = MaterialTheme.typography.titleSmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Circular Timer
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(190.dp)
            ) {
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.size(180.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    strokeWidth = 10.dp
                )

                CircularProgressIndicator(
                    progress = { progress.coerceIn(0f, 1f) },
                    modifier = Modifier.size(180.dp),
                    color = if (remainingSeconds == 0) CulinarySecondary else CulinaryPrimary,
                    strokeWidth = 10.dp
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (remainingSeconds == 0) {
                        Icon(
                            imageVector = Icons.Default.Celebration,
                            contentDescription = null,
                            tint = CulinarySecondary,
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = "Pişti!",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = CulinarySecondary
                            )
                        )
                    } else {
                        Text(
                            text = formattedTime,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 2.sp
                            )
                        )
                        Text(
                            text = if (isTimerRunning) "Geri Sayım Aktif" else "Duraklatıldı",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Timer Controls
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onResetTimer,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Sıfırla")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Sıfırla")
                }

                Button(
                    onClick = onToggleTimer,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isTimerRunning) Color(0xFFE53935) else CulinaryPrimary
                    ),
                    modifier = Modifier.testTag("timer_toggle_btn")
                ) {
                    Icon(
                        imageVector = if (isTimerRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isTimerRunning) "Duraklat" else "Başlat",
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Step Carousel / Viewer
            if (recipe.instructions.isNotEmpty()) {
                val safeIndex = currentStepIndex.coerceIn(0, recipe.instructions.size - 1)
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = CulinaryPrimary
                            ) {
                                Text(
                                    text = "Adım ${safeIndex + 1} / ${recipe.instructions.size}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            Row {
                                IconButton(
                                    onClick = { if (currentStepIndex > 0) currentStepIndex-- },
                                    enabled = currentStepIndex > 0
                                ) {
                                    Icon(Icons.Default.NavigateBefore, contentDescription = "Önceki Adım")
                                }
                                IconButton(
                                    onClick = { if (currentStepIndex < recipe.instructions.size - 1) currentStepIndex++ },
                                    enabled = currentStepIndex < recipe.instructions.size - 1
                                ) {
                                    Icon(Icons.Default.NavigateNext, contentDescription = "Sonraki Adım")
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = recipe.instructions[safeIndex],
                            style = MaterialTheme.typography.bodyLarge.copy(
                                lineHeight = 22.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
