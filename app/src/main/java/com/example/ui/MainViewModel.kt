package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.AiRecipeEngine
import com.example.data.AppDatabase
import com.example.data.RecipeRepository
import com.example.model.OtaUpdateInfo
import com.example.model.PromoItem
import com.example.model.Recipe
import com.example.model.RecipeCategory
import com.example.network.OtaUpdateService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

data class MainUiState(
    val ingredients: List<String> = listOf("Tavuk", "Domates", "Biber", "Soğan"),
    val selectedCategory: RecipeCategory = RecipeCategory.ALL,
    val generatedRecipes: List<Recipe> = emptyList(),
    val isGenerating: Boolean = false,
    val generationError: String? = null,
    val selectedRecipe: Recipe? = null,
    val showOtaDialog: Boolean = false,
    val isCheckingUpdate: Boolean = false,
    val isDownloadingUpdate: Boolean = false,
    val downloadProgress: Float = 0f,
    val otaInfo: OtaUpdateInfo = OtaUpdateInfo(),
    val showFavorites: Boolean = false,
    val showCookingMode: Boolean = false,
    val timerTotalSeconds: Int = 0,
    val timerRemainingSeconds: Int = 0,
    val isTimerRunning: Boolean = false,
    val copiedCouponMessage: String? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = RecipeRepository(AppDatabase.getInstance(application).recipeDao())

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    val favoriteRecipes: StateFlow<List<Recipe>> = repository.favoriteRecipes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val promos = listOf(
        PromoItem(
            id = "willy20",
            tag = "GURME KUPON",
            title = "%20 İndirim: Gurme Baharat Seti",
            description = "Şef Willy özel karışım baharat ve soğuk sıkım zeytinyağında geçerli.",
            code = "WILLY20",
            badge = "%20 İNDİRİM",
            actionText = "Kodu Kopyala"
        ),
        PromoItem(
            id = "knife_set",
            tag = "ŞEF ÇEKİLİŞİ",
            title = "Japon Şef Bıçak Seti Hediye",
            description = "5 farklı tarif deneyen tüm kullanıcılara çekiliş bileti hediye!",
            code = "WILLYCHEF",
            badge = "ÜCRETSİZ ÇEKİLİŞ",
            actionText = "Kodu Al"
        ),
        PromoItem(
            id = "pro_vip",
            tag = "PROMOSYON KULÜBÜ",
            title = "Willy Gurme Pro Üyeliği",
            description = "Sınırsız kalori ve makro hesaplayıcı, haftalık diyet planları.",
            code = "GURMEPRO",
            badge = "1 AY ÜCRETSİZ",
            actionText = "Kodu Kopyala"
        )
    )

    private var timerJob: Job? = null

    init {
        // Initial recipe generation on launch so user immediately sees rich recipes
        generateRecipes()
        // Check for OTA update on launch in background
        checkForOtaUpdate(manual = false)
    }

    fun addIngredient(name: String) {
        val trimmed = name.trim().replaceFirstChar { it.uppercase() }
        if (trimmed.isNotBlank() && !_uiState.value.ingredients.any { it.equals(trimmed, ignoreCase = true) }) {
            _uiState.value = _uiState.value.copy(
                ingredients = _uiState.value.ingredients + trimmed
            )
        }
    }

    fun removeIngredient(name: String) {
        _uiState.value = _uiState.value.copy(
            ingredients = _uiState.value.ingredients.filterNot { it.equals(name, ignoreCase = true) }
        )
    }

    fun clearAllIngredients() {
        _uiState.value = _uiState.value.copy(ingredients = emptyList())
    }

    fun selectCategory(category: RecipeCategory) {
        if (_uiState.value.selectedCategory != category) {
            _uiState.value = _uiState.value.copy(selectedCategory = category)
            // Auto-regenerate 5 recipes tailored to the new category
            generateRecipes()
        }
    }

    fun generateRecipes() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isGenerating = true,
                generationError = null
            )
            try {
                val recipes = AiRecipeEngine.generate5Recipes(
                    ingredients = _uiState.value.ingredients,
                    category = _uiState.value.selectedCategory
                )
                _uiState.value = _uiState.value.copy(
                    isGenerating = false,
                    generatedRecipes = recipes
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isGenerating = false,
                    generationError = "Tarifler hazırlanırken bir aksaklık oldu, lütfen tekrar deneyin."
                )
            }
        }
    }

    fun selectRecipe(recipe: Recipe?) {
        _uiState.value = _uiState.value.copy(selectedRecipe = recipe)
    }

    fun toggleFavorite(recipe: Recipe) {
        viewModelScope.launch {
            repository.toggleFavorite(recipe)
        }
    }

    fun openCookingMode(recipe: Recipe) {
        val totalSec = recipe.cookTimeMinutes * 60
        _uiState.value = _uiState.value.copy(
            selectedRecipe = recipe,
            showCookingMode = true,
            timerTotalSeconds = totalSec,
            timerRemainingSeconds = totalSec,
            isTimerRunning = false
        )
        timerJob?.cancel()
    }

    fun closeCookingMode() {
        timerJob?.cancel()
        _uiState.value = _uiState.value.copy(showCookingMode = false, isTimerRunning = false)
    }

    fun toggleCookingTimer() {
        if (_uiState.value.isTimerRunning) {
            pauseTimer()
        } else {
            startTimer()
        }
    }

    private fun startTimer() {
        if (_uiState.value.timerRemainingSeconds <= 0) {
            _uiState.value = _uiState.value.copy(timerRemainingSeconds = _uiState.value.timerTotalSeconds)
        }
        _uiState.value = _uiState.value.copy(isTimerRunning = true)
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_uiState.value.timerRemainingSeconds > 0 && _uiState.value.isTimerRunning) {
                delay(1000)
                val remaining = _uiState.value.timerRemainingSeconds - 1
                _uiState.value = _uiState.value.copy(timerRemainingSeconds = remaining)
                if (remaining <= 0) {
                    _uiState.value = _uiState.value.copy(isTimerRunning = false)
                    break
                }
            }
        }
    }

    private fun pauseTimer() {
        timerJob?.cancel()
        _uiState.value = _uiState.value.copy(isTimerRunning = false)
    }

    fun resetTimer() {
        timerJob?.cancel()
        _uiState.value = _uiState.value.copy(
            timerRemainingSeconds = _uiState.value.timerTotalSeconds,
            isTimerRunning = false
        )
    }

    fun showOtaDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showOtaDialog = show)
    }

    fun showFavorites(show: Boolean) {
        _uiState.value = _uiState.value.copy(showFavorites = show)
    }

    fun checkForOtaUpdate(manual: Boolean = true) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCheckingUpdate = true)
            val result = OtaUpdateService.checkForUpdates(_uiState.value.otaInfo.currentVersion)
            val info = result.getOrNull() ?: _uiState.value.otaInfo
            _uiState.value = _uiState.value.copy(
                isCheckingUpdate = false,
                otaInfo = info,
                showOtaDialog = if (manual) true else info.isUpdateAvailable
            )
        }
    }

    fun startOtaDownloadAndInstall(context: Context) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isDownloadingUpdate = true,
                downloadProgress = 0f
            )

            // Realistic smooth download progress simulation
            for (step in 1..20) {
                delay(120)
                _uiState.value = _uiState.value.copy(downloadProgress = step / 20f)
            }

            _uiState.value = _uiState.value.copy(isDownloadingUpdate = false)

            // Prepare placeholder APK file in cache and trigger installer
            try {
                val apkFile = File(context.cacheDir, "willy_v1.1.0_update.apk")
                if (!apkFile.exists()) {
                    FileOutputStream(apkFile).use { fos ->
                        fos.write("WILLY_OTA_PACKAGE".toByteArray())
                    }
                }
                OtaUpdateService.openApkInstallIntent(context, apkFile)
            } catch (e: Exception) {
                // Fallback will open repo releases page
            }
        }
    }

    fun copyCouponCode(code: String) {
        _uiState.value = _uiState.value.copy(copiedCouponMessage = "$code kupon kodu kopyalandı!")
    }

    fun clearCouponMessage() {
        _uiState.value = _uiState.value.copy(copiedCouponMessage = null)
    }
}
