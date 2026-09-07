package com.example.ui.components

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Recipe
import com.example.ui.theme.CulinaryPrimary
import com.example.ui.theme.CulinarySecondary
import com.example.ui.theme.CulinaryTertiary

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RecipeDetailDialog(
    recipe: Recipe,
    isFavorite: Boolean,
    onDismiss: () -> Unit,
    onToggleFavorite: () -> Unit,
    onStartCooking: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val checkedSteps = remember { mutableStateMapOf<Int, Boolean>() }

    fun shareRecipe() {
        val shareText = buildString {
            append("🍽️ ${recipe.title} (Şef Willy Yemek Tarifi)\n\n")
            append("⏱️ Hazırlık: ${recipe.prepTimeMinutes} dk | Pişirme: ${recipe.cookTimeMinutes} dk\n")
            append("🔥 Kalori: ${recipe.calories} kcal | Protein: ${recipe.proteinGrams}g\n\n")
            append("🛒 Malzemeler:\n")
            recipe.matchedIngredients.forEach { append(" • $it\n") }
            recipe.pantryIngredients.forEach { append(" • $it (Kiler)\n") }
            append("\n👨‍🍳 Hazırlanışı:\n")
            recipe.instructions.forEachIndexed { i, step -> append("${i + 1}. $step\n") }
            append("\n💡 Şef Willy Püf Noktası: ${recipe.chefTip}\n")
            append("\nWilly Yemek Tarifi Uygulaması ile hazırlandı!")
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, recipe.title)
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        context.startActivity(Intent.createChooser(intent, "Tarifi Paylaş"))
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("recipe_detail_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Header: Category, Share, Favorite, Close
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = CulinaryPrimary
                ) {
                    Text(
                        text = recipe.category,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { shareRecipe() }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Paylaş",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = onToggleFavorite) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favori",
                            tint = if (isFavorite) Color(0xFFE53935) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Kapat"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Title & Subtitle
            Text(
                text = recipe.title,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.ExtraBold
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = recipe.subtitle,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Nutritional & Time Metrics Card
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MetricItem(
                        icon = Icons.Default.AccessTime,
                        label = "Süre",
                        value = "${recipe.prepTimeMinutes + recipe.cookTimeMinutes} dk"
                    )
                    MetricItem(
                        icon = Icons.Default.LocalFireDepartment,
                        label = "Kalori",
                        value = "${recipe.calories} kcal",
                        tint = Color(0xFFF4511E)
                    )
                    MetricItem(
                        icon = Icons.Default.Restaurant,
                        label = "Protein",
                        value = "${recipe.proteinGrams}g",
                        tint = CulinarySecondary
                    )
                    MetricItem(
                        icon = Icons.Default.People,
                        label = "Porsiyon",
                        value = "${recipe.servings} Kişilik"
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Ingredients List
            Text(
                text = "Gerekli Malzemeler",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Elinizdeki Malzemelerden Kullanılanlar:",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = CulinaryPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    recipe.matchedIngredients.forEach { item ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = CulinarySecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = item,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                            )
                        }
                    }

                    if (recipe.pantryIngredients.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Kiler / Temel İhtiyaçlar:",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        recipe.pantryIngredients.forEach { item ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 2.dp)
                            ) {
                                Text(
                                    text = "•",
                                    style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.outline)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = item,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Step-by-Step Instructions with Checkboxes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Adım Adım Hazırlanışı",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "Tamamlanan: ${checkedSteps.count { it.value }}/${recipe.instructions.size}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = CulinarySecondary,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            recipe.instructions.forEachIndexed { index, step ->
                val isChecked = checkedSteps[index] == true
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isChecked) {
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable {
                            checkedSteps[index] = !isChecked
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = if (isChecked) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = if (isChecked) CulinarySecondary else MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Adım ${index + 1}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isChecked) CulinarySecondary else CulinaryPrimary
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = step,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    lineHeight = 20.sp
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Şef Willy'nin Püf Noktası Callout
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFF8E1) // Warm golden culinary note
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(Color(0xFFFFB300), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = Color(0xFF3E2723),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Şef Willy'nin Püf Noktası",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF3E2723)
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = recipe.chefTip,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color(0xFF4E342E),
                                lineHeight = 20.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Start Cooking Button
            Button(
                onClick = onStartCooking,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CulinaryPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("start_cooking_btn")
            ) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Mutfak Sayacını Başlat & Pişir (${recipe.cookTimeMinutes} dk)",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun MetricItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    tint: Color = MaterialTheme.colorScheme.onSurface
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold
            )
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}
