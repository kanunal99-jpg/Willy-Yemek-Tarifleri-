package com.example.model

data class Recipe(
    val id: String,
    val title: String,
    val subtitle: String,
    val category: String,
    val prepTimeMinutes: Int,
    val cookTimeMinutes: Int,
    val difficulty: String, // Kolay, Orta, Usta Şef
    val calories: Int,
    val proteinGrams: Int,
    val carbsGrams: Int,
    val fatGrams: Int,
    val servings: Int,
    val matchedIngredients: List<String>,
    val pantryIngredients: List<String>,
    val instructions: List<String>,
    val chefTip: String,
    val dietaryTags: List<String>,
    val isFavorite: Boolean = false
)

enum class RecipeCategory(
    val id: String,
    val title: String,
    val subtitle: String,
    val iconName: String
) {
    ALL("hepsi", "Tüm Lezzetler", "Serbest gurme tarifler", "Restaurant"),
    DIET("diyet", "Diyet & Fit", "Düşük kalori, taze & dengeli", "Spa"),
    GLUTEN_FREE("glutensiz", "Glutensiz", "Çölyak & hassasiyet dostu", "Grass"),
    VEGETARIAN("vejetaryen", "Vejetaryen / Vegan", "Taze sebzeler ve bitkisel güç", "Eco"),
    QUICK("pratik", "Pratik & Hızlı", "15-20 dakikada hazır tarifler", "Bolt"),
    HIGH_PROTEIN("protein", "Yüksek Protein", "Sporcu ve kas dostu beslenme", "FitnessCenter"),
    TRADITIONAL("geleneksel", "Geleneksel Ev Yemeği", "Anadolu ve Osmanlı klasik mutfağı", "SoupKitchen"),
    HEALTHY_DESSERT("tatli", "Fit Tatlı & Atıştırmalık", "Rafine şekersiz sağlıklı tatlar", "Cake");

    companion object {
        fun fromId(id: String): RecipeCategory {
            return entries.firstOrNull { it.id == id } ?: ALL
        }
    }
}

data class PromoItem(
    val id: String,
    val tag: String,
    val title: String,
    val description: String,
    val code: String,
    val badge: String,
    val actionText: String
)

data class OtaUpdateInfo(
    val currentVersion: String = "1.0.0",
    val latestVersion: String = "1.1.0",
    val releaseTitle: String = "Willy v1.1.0 Gurme Güncellemesi",
    val releaseDate: String = "Bugün",
    val releaseNotes: List<String> = listOf(
        "Yeni Yapay Zeka Gurme Motoru 2.0 entegrasyonu",
        "Keto ve Akdeniz diyeti kategorileri zenginleştirildi",
        "Akıllı pişirme zamanlayıcısı ve adım takibi iyileştirildi",
        "Uygulama içi hızlı OTA güncelleme motoru eklendi",
        "Performans ve arayüz hızlandırmaları"
    ),
    val apkDownloadUrl: String = "https://github.com/kanunal99-jpg/Willy-Yemek-Tarifi-/releases/download/v1.1.0/willy-v1.1.0.apk",
    val fileSizeMb: Double = 18.4,
    val isUpdateAvailable: Boolean = true
)
