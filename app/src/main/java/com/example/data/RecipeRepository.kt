package com.example.data

import com.example.model.Recipe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RecipeRepository(private val recipeDao: RecipeDao) {

    val favoriteRecipes: Flow<List<Recipe>> = recipeDao.getAllFavorites().map { entities ->
        entities.map { it.toDomain() }
    }

    suspend fun toggleFavorite(recipe: Recipe): Boolean {
        val currentlyFavorite = recipeDao.isFavorite(recipe.id)
        if (currentlyFavorite) {
            recipeDao.deleteFavoriteById(recipe.id)
            return false
        } else {
            recipeDao.insertFavorite(RecipeEntity.fromDomain(recipe))
            return true
        }
    }

    suspend fun isFavorite(id: String): Boolean {
        return recipeDao.isFavorite(id)
    }

    suspend fun removeFavorite(id: String) {
        recipeDao.deleteFavoriteById(id)
    }
}
