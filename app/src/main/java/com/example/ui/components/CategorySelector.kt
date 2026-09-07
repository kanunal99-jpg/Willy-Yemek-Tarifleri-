package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.SoupKitchen
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RecipeCategory
import com.example.ui.theme.CulinaryPrimary
import com.example.ui.theme.CulinarySecondary

@Composable
fun CategorySelector(
    selectedCategory: RecipeCategory,
    onCategorySelected: (RecipeCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Beslenme & Kategori Tercihi",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "Seçilen kategoriye özel 5 farklı tarif hazırlanır",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
            ) {
                Text(
                    text = selectedCategory.title,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(RecipeCategory.entries) { category ->
                val isSelected = category == selectedCategory
                CategoryCardItem(
                    category = category,
                    isSelected = isSelected,
                    onClick = { onCategorySelected(category) }
                )
            }
        }
    }
}

@Composable
fun CategoryCardItem(
    category: RecipeCategory,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val icon = getCategoryIcon(category)

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                CulinaryPrimary
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 1.dp
        ),
        modifier = Modifier
            .clickable { onClick() }
            .testTag("cat_card_${category.id}")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) Color.White.copy(alpha = 0.25f)
                        else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = category.title,
                    tint = if (isSelected) Color.White else CulinaryPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = category.title,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                )
                Text(
                    text = category.subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        color = if (isSelected) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    maxLines = 1
                )
            }
        }
    }
}

fun getCategoryIcon(category: RecipeCategory): ImageVector {
    return when (category) {
        RecipeCategory.ALL -> Icons.Default.Restaurant
        RecipeCategory.DIET -> Icons.Default.Spa
        RecipeCategory.GLUTEN_FREE -> Icons.Default.Grass
        RecipeCategory.VEGETARIAN -> Icons.Default.Eco
        RecipeCategory.QUICK -> Icons.Default.Bolt
        RecipeCategory.HIGH_PROTEIN -> Icons.Default.FitnessCenter
        RecipeCategory.TRADITIONAL -> Icons.Default.SoupKitchen
        RecipeCategory.HEALTHY_DESSERT -> Icons.Default.Cake
    }
}
