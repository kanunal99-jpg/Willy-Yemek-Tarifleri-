package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CulinaryPrimary
import com.example.ui.theme.CulinarySecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IngredientInputSection(
    ingredients: List<String>,
    onAddIngredient: (String) -> Unit,
    onRemoveIngredient: (String) -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    var textInput by remember { mutableStateOf("") }

    val quickPantrySuggestions = listOf(
        "Domates", "Tavuk", "Yumurta", "Biber", "Soğan",
        "Patates", "Peynir", "Mantar", "Ispanak", "Yoğurt",
        "Kıyma", "Sarımsak", "Makarna", "Pirinç", "Mercimek"
    )

    fun submitCurrentText() {
        if (textInput.isNotBlank()) {
            onAddIngredient(textInput)
            textInput = ""
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                MaterialTheme.colorScheme.primaryContainer,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Kitchen,
                            contentDescription = null,
                            tint = CulinaryPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Elinizdeki Malzemeler",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "${ingredients.size} malzeme eklendi",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }

                if (ingredients.isNotEmpty()) {
                    TextButton(
                        onClick = onClearAll,
                        modifier = Modifier.testTag("clear_ingredients_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Hepsini Sil",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Temizle",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Ingredient input field + Add button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    placeholder = { Text("Örn: Tavuk, Domates, Kabak...") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { submitCurrentText() }),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CulinaryPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("ingredient_text_field"),
                    trailingIcon = {
                        if (textInput.isNotBlank()) {
                            IconButton(onClick = { textInput = "" }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Sil")
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = { submitCurrentText() },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CulinaryPrimary
                    ),
                    modifier = Modifier
                        .height(54.dp)
                        .testTag("add_ingredient_button")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Ekle")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ekle", fontWeight = FontWeight.Bold)
                }
            }

            // Current Ingredient Chips
            AnimatedVisibility(visible = ingredients.isNotEmpty()) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Text(
                        text = "Tarif Hazırlanacak Malzemeler:",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ingredients.forEach { item ->
                            InputChip(
                                selected = true,
                                onClick = { onRemoveIngredient(item) },
                                label = { Text(item, fontWeight = FontWeight.Medium) },
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Kaldır",
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = InputChipDefaults.inputChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                modifier = Modifier.testTag("chip_$item")
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick add suggestions
            Text(
                text = "Hızlı Ekle (Sık Kullanılanlar):",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                quickPantrySuggestions.forEach { suggestion ->
                    val isAlreadyAdded = ingredients.any { it.equals(suggestion, ignoreCase = true) }
                    SuggestionChip(
                        onClick = {
                            if (isAlreadyAdded) onRemoveIngredient(suggestion) else onAddIngredient(suggestion)
                        },
                        label = {
                            Text(
                                text = if (isAlreadyAdded) "✓ $suggestion" else "+ $suggestion",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isAlreadyAdded) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = if (isAlreadyAdded) {
                                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            }
                        )
                    )
                }
            }
        }
    }
}
