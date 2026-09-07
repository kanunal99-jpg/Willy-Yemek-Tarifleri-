package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.example.model.Recipe

@Entity(tableName = "favorite_recipes")
data class RecipeEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val subtitle: String,
    val category: String,
    val prepTimeMinutes: Int,
    val cookTimeMinutes: Int,
    val difficulty: String,
    val calories: Int,
    val proteinGrams: Int,
    val carbsGrams: Int,
    val fatGrams: Int,
    val servings: Int,
    val matchedIngredientsCsv: String,
    val pantryIngredientsCsv: String,
    val instructionsCsv: String,
    val chefTip: String,
    val dietaryTagsCsv: String,
    val savedAtTimestamp: Long = System.currentTimeMillis()
) {
    fun toDomain(): Recipe {
        return Recipe(
            id = id,
            title = title,
            subtitle = subtitle,
            category = category,
            prepTimeMinutes = prepTimeMinutes,
            cookTimeMinutes = cookTimeMinutes,
            difficulty = difficulty,
            calories = calories,
            proteinGrams = proteinGrams,
            carbsGrams = carbsGrams,
            fatGrams = fatGrams,
            servings = servings,
            matchedIngredients = matchedIngredientsCsv.split(";;;").filter { it.isNotBlank() },
            pantryIngredients = pantryIngredientsCsv.split(";;;").filter { it.isNotBlank() },
            instructions = instructionsCsv.split(";;;").filter { it.isNotBlank() },
            chefTip = chefTip,
            dietaryTags = dietaryTagsCsv.split(";;;").filter { it.isNotBlank() },
            isFavorite = true
        )
    }

    companion object {
        fun fromDomain(recipe: Recipe): RecipeEntity {
            return RecipeEntity(
                id = recipe.id,
                title = recipe.title,
                subtitle = recipe.subtitle,
                category = recipe.category,
                prepTimeMinutes = recipe.prepTimeMinutes,
                cookTimeMinutes = recipe.cookTimeMinutes,
                difficulty = recipe.difficulty,
                calories = recipe.calories,
                proteinGrams = recipe.proteinGrams,
                carbsGrams = recipe.carbsGrams,
                fatGrams = recipe.fatGrams,
                servings = recipe.servings,
                matchedIngredientsCsv = recipe.matchedIngredients.joinToString(";;;"),
                pantryIngredientsCsv = recipe.pantryIngredients.joinToString(";;;"),
                instructionsCsv = recipe.instructions.joinToString(";;;"),
                chefTip = recipe.chefTip,
                dietaryTagsCsv = recipe.dietaryTags.joinToString(";;;")
            )
        }
    }
}
