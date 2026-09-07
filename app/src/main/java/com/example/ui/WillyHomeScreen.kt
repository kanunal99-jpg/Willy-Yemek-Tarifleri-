package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.Recipe
import com.example.ui.components.CategorySelector
import com.example.ui.components.CookingModeDialog
import com.example.ui.components.FavoritesSheet
import com.example.ui.components.IngredientInputSection
import com.example.ui.components.OtaUpdateDialog
import com.example.ui.components.PromoBannerSection
import com.example.ui.components.RecipeCard
import com.example.ui.components.RecipeDetailDialog
import com.example.ui.theme.CulinaryPrimary
import com.example.ui.theme.CulinarySecondary
import com.example.ui.theme.GoldPromoBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WillyHomeScreen(
    viewModel: MainViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val favorites by viewModel.favoriteRecipes.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.copiedCouponMessage) {
        uiState.copiedCouponMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearCouponMessage()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(CulinaryPrimary, Color(0xFFFF8F00))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Restaurant,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Willy",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = CulinaryPrimary
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = "AI ŞEF",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 9.sp,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        ),
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Yapay Zeka Yemek Tarifi",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                },
                actions = {
                    // OTA Update Button
                    IconButton(
                        onClick = { viewModel.showOtaDialog(true) },
                        modifier = Modifier.testTag("ota_app_bar_btn")
                    ) {
                        BadgedBox(
                            badge = {
                                if (uiState.otaInfo.isUpdateAvailable) {
                                    Badge(containerColor = CulinaryPrimary) {
                                        Text("!", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.SystemUpdate,
                                contentDescription = "OTA Güncellemeleri",
                                tint = if (uiState.otaInfo.isUpdateAvailable) CulinaryPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Favorites Button
                    IconButton(
                        onClick = { viewModel.showFavorites(true) },
                        modifier = Modifier.testTag("favorites_app_bar_btn")
                    ) {
                        BadgedBox(
                            badge = {
                                if (favorites.isNotEmpty()) {
                                    Badge(containerColor = Color(0xFFE53935)) {
                                        Text("${favorites.size}")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "Favoriler",
                                tint = if (favorites.isNotEmpty()) Color(0xFFE53935) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .windowInsetsPadding(WindowInsets.navigationBars),
            contentPadding = PaddingValues(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // OTA In-App Update Notice Banner (if update is available)
            if (uiState.otaInfo.isUpdateAvailable) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .clickable { viewModel.showOtaDialog(true) }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = CulinaryPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Yeni Sürüm Hazır: v${uiState.otaInfo.latestVersion}",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )
                                Text(
                                    text = "Uygulama içerisinden tek dokunuşla güncelleyin.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    )
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = CulinaryPrimary
                            ) {
                                Text(
                                    text = "Güncelle",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Promotional Section (Coupons & campaigns)
            item {
                PromoBannerSection(
                    promos = viewModel.promos,
                    onCouponCopied = { code ->
                        viewModel.copyCouponCode(code)
                    }
                )
            }

            // Ingredient Input Section
            item {
                IngredientInputSection(
                    ingredients = uiState.ingredients,
                    onAddIngredient = { viewModel.addIngredient(it) },
                    onRemoveIngredient = { viewModel.removeIngredient(it) },
                    onClearAll = { viewModel.clearAllIngredients() }
                )
            }

            // Category / Dietary Preference Selector
            item {
                CategorySelector(
                    selectedCategory = uiState.selectedCategory,
                    onCategorySelected = { viewModel.selectCategory(it) }
                )
            }

            // Generate Button
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Button(
                        onClick = { viewModel.generateRecipes() },
                        enabled = !uiState.isGenerating && uiState.ingredients.isNotEmpty(),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CulinaryPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("generate_recipes_btn")
                    ) {
                        if (uiState.isGenerating) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.5.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Şef Willy 5 Tarifi Hazırlıyor...",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = GoldPromoBadge,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Yapay Zeka ile 5 Farklı Tarif Hazırla",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }
                    }

                    if (uiState.ingredients.isEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Lütfen tarif hazırlamak için en az 1 malzeme ekleyin.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }
            }

            // Results Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Yapay Zeka Gurme Menüsü",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "${uiState.generatedRecipes.size} farklı detaylı tarif hazırlandı",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = uiState.selectedCategory.title,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = CulinaryPrimary
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Recipe Cards
            itemsIndexed(uiState.generatedRecipes) { index, recipe ->
                val isFav = favorites.any { it.id == recipe.id || it.title == recipe.title }
                RecipeCard(
                    recipe = recipe,
                    index = index,
                    isFavorite = isFav,
                    onRecipeClick = { viewModel.selectRecipe(recipe) },
                    onToggleFavorite = { viewModel.toggleFavorite(recipe) },
                    onCookClick = { viewModel.openCookingMode(recipe) },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }

    // Modal Dialogs & Sheets
    uiState.selectedRecipe?.let { recipe ->
        if (!uiState.showCookingMode) {
            val isFav = favorites.any { it.id == recipe.id || it.title == recipe.title }
            RecipeDetailDialog(
                recipe = recipe,
                isFavorite = isFav,
                onDismiss = { viewModel.selectRecipe(null) },
                onToggleFavorite = { viewModel.toggleFavorite(recipe) },
                onStartCooking = {
                    viewModel.openCookingMode(recipe)
                }
            )
        }
    }

    if (uiState.showCookingMode && uiState.selectedRecipe != null) {
        CookingModeDialog(
            recipe = uiState.selectedRecipe!!,
            remainingSeconds = uiState.timerRemainingSeconds,
            totalSeconds = uiState.timerTotalSeconds,
            isTimerRunning = uiState.isTimerRunning,
            onToggleTimer = { viewModel.toggleCookingTimer() },
            onResetTimer = { viewModel.resetTimer() },
            onClose = { viewModel.closeCookingMode() }
        )
    }

    if (uiState.showOtaDialog) {
        OtaUpdateDialog(
            otaInfo = uiState.otaInfo,
            isChecking = uiState.isCheckingUpdate,
            isDownloading = uiState.isDownloadingUpdate,
            downloadProgress = uiState.downloadProgress,
            onCheckAgain = { viewModel.checkForOtaUpdate(manual = true) },
            onInstallUpdate = { viewModel.startOtaDownloadAndInstall(context) },
            onDismiss = { viewModel.showOtaDialog(false) }
        )
    }

    if (uiState.showFavorites) {
        FavoritesSheet(
            favorites = favorites,
            onSelectRecipe = {
                viewModel.showFavorites(false)
                viewModel.selectRecipe(it)
            },
            onRemoveFavorite = { viewModel.toggleFavorite(it) },
            onDismiss = { viewModel.showFavorites(false) }
        )
    }
}
