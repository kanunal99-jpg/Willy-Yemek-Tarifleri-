package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {
    @Query("SELECT * FROM favorite_recipes ORDER BY savedAtTimestamp DESC")
    fun getAllFavorites(): Flow<List<RecipeEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_recipes WHERE id = :id)")
    fun isFavoriteFlow(id: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_recipes WHERE id = :id)")
    suspend fun isFavorite(id: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(recipe: RecipeEntity)

    @Query("DELETE FROM favorite_recipes WHERE id = :id")
    suspend fun deleteFavoriteById(id: String)
}
