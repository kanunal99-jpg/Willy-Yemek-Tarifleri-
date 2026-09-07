package com.example.ai

import com.example.model.Recipe
import com.example.model.RecipeCategory
import com.example.network.GeminiApiService
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

object AiRecipeEngine {

    suspend fun generate5Recipes(
        ingredients: List<String>,
        category: RecipeCategory
    ): List<Recipe> {
        val cleanIngredients = ingredients.map { it.trim() }.filter { it.isNotBlank() }

        // Try AI via Gemini API first
        val aiResult = tryGenerateWithGemini(cleanIngredients, category)
        if (aiResult != null && aiResult.size >= 5) {
            return aiResult.take(5)
        }

        // Seamless fallback: Intelligent Culinary Engine tailored to ingredients & category
        return generateSynthesizedGourmetRecipes(cleanIngredients, category)
    }

    private suspend fun tryGenerateWithGemini(
        ingredients: List<String>,
        category: RecipeCategory
    ): List<Recipe>? {
        val ingredientListStr = ingredients.joinToString(", ")
        val categoryContext = when (category) {
            RecipeCategory.DIET -> "DİYET & FİT (Düşük kalorili, taze, hafif ve yağsız/az yağlı, kilo verme dostu)"
            RecipeCategory.GLUTEN_FREE -> "GLUTENSİZ (Kesinlikle buğday, un, gluten içermeyen, çölyak dostu)"
            RecipeCategory.VEGETARIAN -> "VEJETARYEN / VEGAN (Et ve balık içermeyen, bitkisel protein ve sebze ağırlıklı)"
            RecipeCategory.QUICK -> "PRATİK & HIZLI (Maksimum 15-25 dakikada pratikçe hazırlanan)"
            RecipeCategory.HIGH_PROTEIN -> "YÜKSEK PROTEİN (Kas yapıcı, sporcu beslenmesine uygun, bol proteinli)"
            RecipeCategory.TRADITIONAL -> "GELENEKSEL EV YEMEĞİ (Türk ve Akdeniz mutfağı usulü leziz tencere/fırın yemeği)"
            RecipeCategory.HEALTHY_DESSERT -> "FİT TATLI & SAĞLIKLI ATIŞTIRMALIK (Rafine şekersiz, doğal tatlandırılmış)"
            RecipeCategory.ALL -> "GURME ŞEF SEÇİMİ (Lezzetli, dengeli ve yaratıcı)"
        }

        val prompt = """
            Sen uzman gurme şef "Willy"sin. Kullanıcının elindeki malzemeler: [$ingredientListStr].
            Seçilen Kategori: $categoryContext.
            
            GÖREVİN: Kullanıcının malzemelerini ve seçtiği kategoriyi temel alarak TAM OLARAK 5 FARKLI, birbiriyle aynı olmayan, son derece detaylı ve lezzetli yemek tarifi hazırla.
            
            Lütfen SADECE aşağıdaki JSON formatında geçerli bir JSON ARRAY döndür, markdown ya da başka metin ekleme:
            [
              {
                "title": "Yemek Adı",
                "subtitle": "Kısa ve iştah açıcı açıklama",
                "category": "${category.title}",
                "prepTimeMinutes": 15,
                "cookTimeMinutes": 25,
                "difficulty": "Kolay",
                "calories": 380,
                "proteinGrams": 28,
                "carbsGrams": 32,
                "fatGrams": 14,
                "servings": 2,
                "matchedIngredients": ["kullanıcının malzemelerinden kullanılanlar"],
                "pantryIngredients": ["tuz", "zeytinyağı", "karabiber vb."],
                "instructions": [
                  "1. Adım açıklaması...",
                  "2. Adım açıklaması...",
                  "3. Adım açıklaması..."
                ],
                "chefTip": "Şef Willy'nin bu yemeği harika yapacak gizli püf noktası",
                "dietaryTags": ["Glutensiz", "Diyet", "Yüksek Lif"]
              }
            ]
        """.trimIndent()

        val response = GeminiApiService.generateContent(prompt)
        val text = response.getOrNull() ?: return null

        return parseRecipesFromJson(text, category, ingredients)
    }

    private fun parseRecipesFromJson(
        rawText: String,
        category: RecipeCategory,
        userIngredients: List<String>
    ): List<Recipe>? {
        return try {
            var cleanJson = rawText.trim()
            if (cleanJson.startsWith("```json")) {
                cleanJson = cleanJson.removePrefix("```json").trim()
            }
            if (cleanJson.startsWith("```")) {
                cleanJson = cleanJson.removePrefix("```").trim()
            }
            if (cleanJson.endsWith("```")) {
                cleanJson = cleanJson.removeSuffix("```").trim()
            }

            val startIndex = cleanJson.indexOf('[')
            val endIndex = cleanJson.lastIndexOf(']')
            if (startIndex == -1 || endIndex == -1 || endIndex <= startIndex) return null

            val jsonArray = JSONArray(cleanJson.substring(startIndex, endIndex + 1))
            val list = mutableListOf<Recipe>()

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val matched = mutableListOf<String>()
                val matchedArray = obj.optJSONArray("matchedIngredients")
                if (matchedArray != null) {
                    for (j in 0 until matchedArray.length()) {
                        matched.add(matchedArray.getString(j))
                    }
                }
                if (matched.isEmpty()) matched.addAll(userIngredients)

                val pantry = mutableListOf<String>()
                val pantryArray = obj.optJSONArray("pantryIngredients")
                if (pantryArray != null) {
                    for (j in 0 until pantryArray.length()) {
                        pantry.add(pantryArray.getString(j))
                    }
                }
                if (pantry.isEmpty()) pantry.addAll(listOf("Zeytinyağı", "Tuz", "Karabiber"))

                val instructions = mutableListOf<String>()
                val instructionsArray = obj.optJSONArray("instructions")
                if (instructionsArray != null) {
                    for (j in 0 until instructionsArray.length()) {
                        instructions.add(instructionsArray.getString(j))
                    }
                }
                if (instructions.isEmpty()) {
                    instructions.addAll(listOf(
                        "Malzemeleri yıkayıp uygun boyutlarda doğrayın.",
                        "Tavada veya tencerede kısık ateşte aromaları harmanlayarak pişirin.",
                        "Sıcak servis yapın, afiyet olsun!"
                    ))
                }

                val dietary = mutableListOf<String>()
                val dietaryArray = obj.optJSONArray("dietaryTags")
                if (dietaryArray != null) {
                    for (j in 0 until dietaryArray.length()) {
                        dietary.add(dietaryArray.getString(j))
                    }
                }
                if (dietary.isEmpty()) dietary.add(category.title)

                list.add(
                    Recipe(
                        id = UUID.randomUUID().toString(),
                        title = obj.optString("title", "Şef Willy Özel Tabağı"),
                        subtitle = obj.optString("subtitle", "Özel hazırlanmış lezzet dengesi"),
                        category = obj.optString("category", category.title),
                        prepTimeMinutes = obj.optInt("prepTimeMinutes", 15),
                        cookTimeMinutes = obj.optInt("cookTimeMinutes", 20),
                        difficulty = obj.optString("difficulty", "Orta"),
                        calories = obj.optInt("calories", 350),
                        proteinGrams = obj.optInt("proteinGrams", 20),
                        carbsGrams = obj.optInt("carbsGrams", 30),
                        fatGrams = obj.optInt("fatGrams", 12),
                        servings = obj.optInt("servings", 2),
                        matchedIngredients = matched,
                        pantryIngredients = pantry,
                        instructions = instructions,
                        chefTip = obj.optString("chefTip", "Şef Willy'nin tavsiyesi: Pişirirken baharatları kavurma aşamasında ekleyin."),
                        dietaryTags = dietary
                    )
                )
            }
            if (list.isNotEmpty()) list else null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Intelligent Gourmet Fallback Engine: Dynamically crafts 5 realistic, detailed Turkish & Mediterranean
     * recipes matching user ingredients and requested category (Diyet, Glutensiz, vb.).
     */
    fun generateSynthesizedGourmetRecipes(
        ingredients: List<String>,
        category: RecipeCategory
    ): List<Recipe> {
        val userItems = if (ingredients.isEmpty()) listOf("Domates", "Yumurta", "Biber") else ingredients
        val mainIng = userItems.firstOrNull()?.replaceFirstChar { it.uppercase() } ?: "Sebze"
        val secondIng = userItems.getOrNull(1)?.replaceFirstChar { it.uppercase() } ?: "Zeytinyağı"
        val thirdIng = userItems.getOrNull(2)?.replaceFirstChar { it.uppercase() } ?: "Baharat"

        val categoryName = category.title

        return when (category) {
            RecipeCategory.GLUTEN_FREE -> generateGlutenFreeRecipes(userItems, mainIng, secondIng, thirdIng)
            RecipeCategory.DIET -> generateDietRecipes(userItems, mainIng, secondIng, thirdIng)
            RecipeCategory.HIGH_PROTEIN -> generateHighProteinRecipes(userItems, mainIng, secondIng, thirdIng)
            RecipeCategory.VEGETARIAN -> generateVegetarianRecipes(userItems, mainIng, secondIng, thirdIng)
            RecipeCategory.QUICK -> generateQuickRecipes(userItems, mainIng, secondIng, thirdIng)
            RecipeCategory.TRADITIONAL -> generateTraditionalRecipes(userItems, mainIng, secondIng, thirdIng)
            RecipeCategory.HEALTHY_DESSERT -> generateDessertRecipes(userItems, mainIng, secondIng, thirdIng)
            RecipeCategory.ALL -> generateGourmetMasterRecipes(userItems, mainIng, secondIng, thirdIng)
        }
    }

    private fun generateGlutenFreeRecipes(
        items: List<String>,
        i1: String,
        i2: String,
        i3: String
    ): List<Recipe> = listOf(
        Recipe(
            id = UUID.randomUUID().toString(),
            title = "Glutensiz Fırın $i1 Güveci",
            subtitle = "Karamelize sebzeler ve saf zeytinyağıyla fırınlanmış çölyak dostu lezzet",
            category = "Glutensiz",
            prepTimeMinutes = 12,
            cookTimeMinutes = 28,
            difficulty = "Kolay",
            calories = 310,
            proteinGrams = 18,
            carbsGrams = 22,
            fatGrams = 14,
            servings = 2,
            matchedIngredients = items.take(4),
            pantryIngredients = listOf("Soğuk sıkım zeytinyağı", "Kaya tuzu", "Taze kekik", "Sarımsak"),
            instructions = listOf(
                "Tüm sebzeleri yıkayıp glutensiz mutfak gereçlerinde küp küp doğrayın.",
                "$i1 ve $i2 malzemelerini zeytinyağı, sarımsak ve kekikle geniş bir kapta harmanlayın.",
                "Fırın güvecine malzemeleri yayın, üzerine yağlı kağıt örterek 190°C fırında 25 dakika pişirin.",
                "Son 5 dakikada kağıdı alıp üzerini hafifçe kızartın ve sıcak servis yapın."
            ),
            chefTip = "Şef Willy Püf Noktası: Çölyak hassasiyetiniz varsa kullandığınız baharatların 'Glutensiz' sertifikalı olduğundan emin olun.",
            dietaryTags = listOf("Glutensiz", "Çölyak Dostu", "Zengin Lif", "Düşük Glisemik")
        ),
        Recipe(
            id = UUID.randomUUID().toString(),
            title = "Tavada Çıtır $i1 & $i2 Sote",
            subtitle = "Yüksek ateşte mühürlenmiş taptaze glutensiz tava ziyafeti",
            category = "Glutensiz",
            prepTimeMinutes = 10,
            cookTimeMinutes = 15,
            difficulty = "Çok Kolay",
            calories = 280,
            proteinGrams = 16,
            carbsGrams = 18,
            fatGrams = 12,
            servings = 2,
            matchedIngredients = items,
            pantryIngredients = listOf("Zeytinyağı", "Pul biber", "Karabiber", "Kimyon"),
            instructions = listOf(
                "Tavanızı orta ateşte ısıtın ve bir yemek kaşığı zeytinyağını ekleyin.",
                "Önce $i1 dilimlerini ekleyip 4-5 dakika hafifçe soteleyin.",
                "Ardından $i2 ve $i3 malzemelerini ekleyip baharatlarla 6-8 dakika çevirin.",
                "Sebzeler diriliğini korurken ateşten alın ve taze yeşilliklerle servis edin."
            ),
            chefTip = "Şef Willy Püf Noktası: Sebzeleri tavaya atmadan önce iyice kurulayın; bu sayede buğulanmak yerine lezzetle kızarırlar.",
            dietaryTags = listOf("Glutensiz", "Hızlı Hazırlık", "Antioksidan")
        ),
        Recipe(
            id = UUID.randomUUID().toString(),
            title = "Mısır Unlu $i1 Mücveri (Fırında)",
            subtitle = "Buğday unu içermeyen, organik mısır unuyla kıtırlaşan hafif fırın mücveri",
            category = "Glutensiz",
            prepTimeMinutes = 15,
            cookTimeMinutes = 25,
            difficulty = "Orta",
            calories = 340,
            proteinGrams = 14,
            carbsGrams = 36,
            fatGrams = 15,
            servings = 3,
            matchedIngredients = items.take(3),
            pantryIngredients = listOf("Glutensiz mısır unu", "Yumurta", "Dereotu", "Kabartma tozu"),
            instructions = listOf(
                "$i1 ve diğer malzemeleri rendeleyip suyunu temiz bir tülbentle iyice sıkın.",
                "Karıştırma kabında yumurta, 2 kaşık mısır unu ve baharatları çırpın.",
                "Tüm malzemeyi harmanlayıp yağlı kağıt serili tepsiye kaşıkla porsiyonlayın.",
                "180°C fırında üzeri altın sarısı olana dek 25 dakika pişirin."
            ),
            chefTip = "Şef Willy Püf Noktası: Suyunu ne kadar iyi sıkarsanız mücverleriniz o kadar kıtır ve lezzetli olur.",
            dietaryTags = listOf("Glutensiz", "Fırında Hafif", "Vejetaryen Alternatif")
        ),
        Recipe(
            id = UUID.randomUUID().toString(),
            title = "Taze Baharatlı $i1 Çorbası",
            subtitle = "Kıvamını patates ve sebzenin kendi nişastasından alan ipeksi çorba",
            category = "Glutensiz",
            prepTimeMinutes = 10,
            cookTimeMinutes = 20,
            difficulty = "Kolay",
            calories = 190,
            proteinGrams = 8,
            carbsGrams = 24,
            fatGrams = 6,
            servings = 4,
            matchedIngredients = items,
            pantryIngredients = listOf("Sebze veya tavuk suyu", "Zeytinyağı", "Limon", "Nane"),
            instructions = listOf(
                "Tencerede zeytinyağında $i1 ve $i2 malzemelerini 3 dakika kavurun.",
                "Üzerine 4 su bardağı sıcak su veya et suyu ilave edip kaynamaya bırakın.",
                "Sebzeler yumuşayınca el blenderı ile pürüzsüz hale getirin.",
                "Üzerine zeytinyağında ısıtılmış nane ve taze limon suyu gezdirin."
            ),
            chefTip = "Şef Willy Püf Noktası: Çorba bağlayıcı un kullanmadan, sebzeleri blenderdan geçirerek tamamen doğal ve glutensiz kıvam alır.",
            dietaryTags = listOf("Glutensiz", "Düşük Kalori", "Mide Dostu", "Detoks")
        ),
        Recipe(
            id = UUID.randomUUID().toString(),
            title = "Ilık $i1 & Kinoa Gurme Salatası",
            subtitle = "Bitkisel protein, taze otlar ve zeytinyağlı limon sosuyla doyumsuz kase",
            category = "Glutensiz",
            prepTimeMinutes = 15,
            cookTimeMinutes = 10,
            difficulty = "Kolay",
            calories = 320,
            proteinGrams = 12,
            carbsGrams = 38,
            fatGrams = 13,
            servings = 2,
            matchedIngredients = items,
            pantryIngredients = listOf("Kinoa veya karabuğday", "Limon", "Zeytinyağı", "Nar ekşisi"),
            instructions = listOf(
                "Kinoayı haşlayıp süzün ve ılıklaşmaya bırakın.",
                "$i1 ve $i2 malzemelerini hafifçe tavada 3 dakika soteleyip sıcaklığını koruyun.",
                "Geniş salata kasesinde ılık kinoa ile sote sebzeleri birleştirin.",
                "Zeytinyağı, limon suyu ve taze nane ile soslayarak hemen servis yapın."
            ),
            chefTip = "Şef Willy Püf Noktası: Kinoa glutensiz tam tahıl grubundadır ve vücuda 9 temel amino asidin tamamını sağlar.",
            dietaryTags = listOf("Glutensiz", "Süper Gıda", "Vegan", "Tok Tutan")
        )
    )

    private fun generateDietRecipes(
        items: List<String>,
        i1: String,
        i2: String,
        i3: String
    ): List<Recipe> = listOf(
        Recipe(
            id = UUID.randomUUID().toString(),
            title = "Fit Buharda $i1 & Sebze Tabağı",
            subtitle = "Besin değerini kaybetmeden pişirilen, kalorisi kontrol altında hafif öğün",
            category = "Diyet & Fit",
            prepTimeMinutes = 10,
            cookTimeMinutes = 15,
            difficulty = "Çok Kolay",
            calories = 210,
            proteinGrams = 14,
            carbsGrams = 18,
            fatGrams = 7,
            servings = 2,
            matchedIngredients = items,
            pantryIngredients = listOf("1 tatlı kaşığı zeytinyağı", "Limon suyu", "Elma sirkesi", "Kekik"),
            instructions = listOf(
                "$i1 ve $i2 malzemelerini eşit lokmalık dilimler halinde kesin.",
                "Buhar sepetinde sebzeleri 12-14 dakika diri kalacak şekilde pişirin.",
                "Sos kabında 1 tatlı kaşığı zeytinyağı, bol limon ve kekiği emülsifiye edin.",
                "Sıcak sebzelerin üzerine sosu dökün, metabolizmayı canlandıran öğününüz hazır."
            ),
            chefTip = "Şef Willy Püf Noktası: Buharda pişirme C vitamini ve mineralleri korur. Doyuruculuğu artırmak için yanında 2 kaşık probiyotik yoğurt tüketin.",
            dietaryTags = listOf("Düşük Kalori", "Kilo Verme", "Yağsız/Az Yağlı", "Metabolizma Dostu")
        ),
        Recipe(
            id = UUID.randomUUID().toString(),
            title = "Detoks $i1 & Yoğurtlu Diyet Meze",
            subtitle = "Sindirim sistemini rahatlatan, hafif ve ferahlatıcı yüksek lifli fit kase",
            category = "Diyet & Fit",
            prepTimeMinutes = 12,
            cookTimeMinutes = 5,
            difficulty = "Çok Kolay",
            calories = 165,
            proteinGrams = 12,
            carbsGrams = 14,
            fatGrams = 4,
            servings = 2,
            matchedIngredients = items.take(3),
            pantryIngredients = listOf("Süzme yoğurt (az yağlı)", "Ceviz içi (2 adet)", "Kuru nane", "Sarımsak"),
            instructions = listOf(
                "$i1 malzemesini rendenin kalın tarafıyla rendeleyin ve tavada susuz 3 dakika soteleyin.",
                "Yoğurdu ezilmiş sarımsak ve nane ile krema kıvamına gelene kadar çırpın.",
                "Ilıyan sebzeleri yoğurtla harmanlayın.",
                "Üzerine kırılmış 2 adet ceviz ekleyerek servis yapın."
            ),
            chefTip = "Şef Willy Püf Noktası: Yoğurttaki protein ve cevizdeki sağlıklı yağlar tokluk süresini en az 4 saate uzatır.",
            dietaryTags = listOf("Detoks", "Düşük Kalori", "Yüksek Kalsiyum", "Diyet")
        ),
        Recipe(
            id = UUID.randomUUID().toString(),
            title = "Fırında Baharatlı $i1 Cipsleri",
            subtitle = "Yağda kızartma yerine fırında kurutularak hazırlanan sağlıklı çıtırlar",
            category = "Diyet & Fit",
            prepTimeMinutes = 8,
            cookTimeMinutes = 20,
            difficulty = "Kolay",
            calories = 140,
            proteinGrams = 5,
            carbsGrams = 22,
            fatGrams = 3,
            servings = 2,
            matchedIngredients = items.take(2),
            pantryIngredients = listOf("Fısfıs zeytinyağı", "Kırmızı toz biber", "Kimyon", "Deniz tuzu"),
            instructions = listOf(
                "$i1 malzemesini ince cips halkaları şeklinde dilimleyin.",
                "Kağıt havlu üzerinde 5 dakika bekleterek fazla suyunu alın.",
                "Fırın tepsisine tek sıra dizip baharatlayın.",
                "175°C fırında çıtırlaşana dek 18-20 dakika pişirin."
            ),
            chefTip = "Şef Willy Püf Noktası: Gece gelen atıştırmalık krizlerinde sıfır suçluluk hissiyle tüketebilirsiniz.",
            dietaryTags = listOf("Atıştırmalık", "Çıtır", "Düşük Yağ", "Glutensiz")
        ),
        Recipe(
            id = UUID.randomUUID().toString(),
            title = "Yağ Yakıcı $i1 & Yeşil Mercimek Yemeği",
            subtitle = "Yüksek lif ve bitkisel proteinle kan şekerini dengeleyen tok tutan tencere yemeği",
            category = "Diyet & Fit",
            prepTimeMinutes = 15,
            cookTimeMinutes = 25,
            difficulty = "Orta",
            calories = 290,
            proteinGrams = 18,
            carbsGrams = 38,
            fatGrams = 5,
            servings = 3,
            matchedIngredients = items,
            pantryIngredients = listOf("Haşlanmış yeşil mercimek", "Soğan", "Zeytinyağı", "Kimyon"),
            instructions = listOf(
                "Tencerede 1 tatlı kaşığı zeytinyağında doğranmış soğan ve $i1 malzemesini soteleyin.",
                "Haşlanmış yeşil mercimeği ve $i2 malzemesini ekleyin.",
                "Üzerine 2 su bardağı sıcak su ve kimyon ekleyip kısık ateşte 20 dakika pişirin.",
                "Ilık veya sıcak servis yapın."
            ),
            chefTip = "Şef Willy Püf Noktası: Kimyon mercimeğin gaz yapmasını önler ve sindirimi hızlandırır.",
            dietaryTags = listOf("Yüksek Lif", "Düşük Glisemik", "Tok Tutan", "Diyet")
        ),
        Recipe(
            id = UUID.randomUUID().toString(),
            title = "Fit $i1 & Yumurta Beyazı Omleti",
            subtitle = "Metabolizmayı hızlandıran, yağsız ve kas koruyucu kahvaltılık / akşam yemeği",
            category = "Diyet & Fit",
            prepTimeMinutes = 6,
            cookTimeMinutes = 8,
            difficulty = "Çok Kolay",
            calories = 195,
            proteinGrams = 22,
            carbsGrams = 8,
            fatGrams = 6,
            servings = 1,
            matchedIngredients = items,
            pantryIngredients = listOf("Yumurta", "Taze maydanoz", "Karabiber", "Lor peyniri"),
            instructions = listOf(
                "Tavayı yağsız veya bir damla zeytinyağı ile peçeteyle silerek ısıtın.",
                "İnce doğranmış $i1 sebzesini 3 dakika yumuşatın.",
                "Çırpılmış 2 yumurta akı ve 1 tam yumurtayı sebzelerin üzerine dökün.",
                "Üzerine 1 kaşık lor peyniri ve maydanoz serpip kapağı kapalı 4 dakika pişirin."
            ),
            chefTip = "Şef Willy Püf Noktası: Lor peyniri yüksek kazein proteini ve çok düşük yağ oranı ile diyet yapanlar için en değerli peynirdir.",
            dietaryTags = listOf("Yüksek Protein", "Düşük Kalori", "Kahvaltılık", "Fit")
        )
    )

    private fun generateHighProteinRecipes(
        items: List<String>,
        i1: String,
        i2: String,
        i3: String
    ): List<Recipe> = listOf(
        Recipe(
            id = UUID.randomUUID().toString(),
            title = "Protein Bombası $i1 Sote",
            subtitle = "Sporcular için özel formüle edilmiş, 38g proteinli kas onarıcı tabak",
            category = "Yüksek Protein",
            prepTimeMinutes = 12,
            cookTimeMinutes = 18,
            difficulty = "Kolay",
            calories = 440,
            proteinGrams = 38,
            carbsGrams = 20,
            fatGrams = 16,
            servings = 2,
            matchedIngredients = items,
            pantryIngredients = listOf("Tavuk göğsü veya tofu", "Zeytinyağı", "Sarımsak", "Kırmızı biber"),
            instructions = listOf(
                "Proteini lokmalık kuşbaşı doğrayın ve döküm tavada yüksek ateşte mühürleyin.",
                "$i1 ve $i2 sebzelerini ekleyip ateşi kısmadan 6 dakika soteleyin.",
                "Sarımsak ve baharatları ekleyip aromalarını verdirin.",
                "Sıcak servis yapın, antrenman sonrası öğün için mükemmeldir."
            ),
            chefTip = "Şef Willy Püf Noktası: Yüksek ateşte mühürlemek etin suyunun ve protein değerinin içinde hapsolmasını sağlar.",
            dietaryTags = listOf("38g Protein", "Kas Gelişimi", "Sporcu Besini", "Fit")
        ),
        Recipe(
            id = UUID.randomUUID().toString(),
            title = "Kıymalı / Mantarlı $i1 Güveç",
            subtitle = "Zengin amino asit profili ve doyurucu lezzetiyle güçlü bir ana yemek",
            category = "Yüksek Protein",
            prepTimeMinutes = 15,
            cookTimeMinutes = 25,
            difficulty = "Orta",
            calories = 480,
            proteinGrams = 34,
            carbsGrams = 24,
            fatGrams = 22,
            servings = 2,
            matchedIngredients = items,
            pantryIngredients = listOf("Kıyma veya mantar", "Soğan", "Domates salçası", "Kekik"),
            instructions = listOf(
                "Soğanları ve kıymayı tencerede suyunu çekene kadar kavurun.",
                "$i1 ve $i2 doğranmış sebzelerini ekleyin.",
                "Salçalı ılık su ile güveç kabına aktarın.",
                "200°C fırında 20 dakika suyunu çektirerek fırınlayın."
            ),
            chefTip = "Şef Willy Püf Noktası: Yanına ekleyeceğiniz 1 bardak ayran protein emilimini ve sindirimini destekler.",
            dietaryTags = listOf("Yüksek Protein", "Doyurucu", "Demir Deposu")
        ),
        Recipe(
            id = UUID.randomUUID().toString(),
            title = "Lor Peynirli $i1 Şef Rulosu",
            subtitle = "Yüksek biyoyararlanıma sahip lor proteini ile fırınlanmış gurme lezzet",
            category = "Yüksek Protein",
            prepTimeMinutes = 15,
            cookTimeMinutes = 20,
            difficulty = "Orta",
            calories = 360,
            proteinGrams = 30,
            carbsGrams = 16,
            fatGrams = 14,
            servings = 2,
            matchedIngredients = items.take(3),
            pantryIngredients = listOf("Taze lor peyniri (200g)", "Yumurta (1 adet)", "Karabiber", "Kırmızı biber"),
            instructions = listOf(
                "$i1 sebzesini uzun ince şeritler halinde kesin ve 3 dakika tavada yumuşatın.",
                "Lor peynirini yumurta ve baharatlarla pürüzsüz kıvama getirin.",
                "Sebze dilimlerinin içine lor harcını sarıp rulo yapın.",
                "Fırın kabına dizip 180°C'de 18 dakika fırınlayın."
            ),
            chefTip = "Şef Willy Püf Noktası: Lor peynirinde bulunan whey fraksiyonu vücut tarafından hızla emilerek kasları besler.",
            dietaryTags = listOf("30g Protein", "Vejetaryen Protein", "Düşük Karbonhidrat")
        ),
        Recipe(
            id = UUID.randomUUID().toString(),
            title = "Nohutlu & $i1 Güç Tabağı",
            subtitle = "Bitkisel protein, lif ve antioksidan zengini Akdeniz güç kasesi",
            category = "Yüksek Protein",
            prepTimeMinutes = 10,
            cookTimeMinutes = 15,
            difficulty = "Kolay",
            calories = 410,
            proteinGrams = 24,
            carbsGrams = 48,
            fatGrams = 12,
            servings = 2,
            matchedIngredients = items,
            pantryIngredients = listOf("Haşlanmış nohut", "Tahin", "Limon", "Zeytinyağı"),
            instructions = listOf(
                "Tavada nohutları ve $i1 malzemesini zeytinyağında çıtırlaşana kadar 8 dakika kavurun.",
                "Ayrı bir kasede tahin, limon suyu ve biraz ılık suyu çırparak krema sos yapın.",
                "Çıtır nohutlu karışımı servis tabağına alın, tahin sosunu üzerine gezdirin.",
                "Taze maydanoz ve pul biberle süsleyin."
            ),
            chefTip = "Şef Willy Püf Noktası: Nohut ve tahin birleştiğinde mükemmel bir tam protein zinciri oluşturur.",
            dietaryTags = listOf("Bitkisel Protein", "Vegan", "Yüksek Lif", "Süper Gıda")
        ),
        Recipe(
            id = UUID.randomUUID().toString(),
            title = "Willy Şef Menemeni (Çift Yumurtalı & Peynirli)",
            subtitle = "Geleneksel menemenin bol proteinli ve gurme baharatlı versiyonu",
            category = "Yüksek Protein",
            prepTimeMinutes = 8,
            cookTimeMinutes = 10,
            difficulty = "Çok Kolay",
            calories = 380,
            proteinGrams = 26,
            carbsGrams = 12,
            fatGrams = 24,
            servings = 1,
            matchedIngredients = items,
            pantryIngredients = listOf("Yumurta (3 adet)", "Tereyağı", "Beyaz peynir", "Karabiber"),
            instructions = listOf(
                "Tereyağını tavada eritin, $i1 ve $i2 malzemelerini suyunu bırakıp çekene kadar soteleyin.",
                "3 adet yumurtayı tavaya kırın, sarılarını hafifçe dağıtarak kısık ateşte pişirin.",
                "Ocaktan almaya yakın ufalanmış peynir serpiştirin.",
                "Kapağını 1 dakika kapatıp dinlendirdikten sonra servis edin."
            ),
            chefTip = "Şef Willy Püf Noktası: Yumurtaları çok fazla kurutmayın; sulu ve ipeksi kıvam proteinlerin besin değerini korur.",
            dietaryTags = listOf("26g Protein", "Kahvaltı & Akşam", "Keto Uyumlu")
        )
    )

    private fun generateVegetarianRecipes(
        items: List<String>,
        i1: String,
        i2: String,
        i3: String
    ): List<Recipe> = listOf(
        Recipe(
            id = UUID.randomUUID().toString(),
            title = "Zeytinyağlı $i1 & Sebze Türlüsü",
            subtitle = "Ege usulü soğuk sıkım zeytinyağıyla ağır ateşte pişen sebze şöleni",
            category = "Vejetaryen / Vegan",
            prepTimeMinutes = 15,
            cookTimeMinutes = 30,
            difficulty = "Kolay",
            calories = 240,
            proteinGrams = 8,
            carbsGrams = 28,
            fatGrams = 11,
            servings = 3,
            matchedIngredients = items,
            pantryIngredients = listOf("Zeytinyağı", "Domates", "Sarımsak", "Kaya tuzu"),
            instructions = listOf(
                "Geniş tabanlı tencereye zeytinyağını dökün ve doğranmış sarımsakları ekleyin.",
                "$i1, $i2 ve $i3 sebzelerini kat kat tencereye dizin.",
                "Rendelenmiş domates ve tuz ekleyip hiç su koymadan kapağını kapatın.",
                "Kısık ateşte sebzeler kendi suyunda lokum gibi olana kadar 30 dakika pişirin."
            ),
            chefTip = "Şef Willy Püf Noktası: Zeytinyağlı yemekler piştikten sonra kapağı açılmadan oda sıcaklığına gelene dek dinlendirilmelidir.",
            dietaryTags = listOf("Vegan", "Bitkisel Güç", "Zeytinyağlı", "Antioksidan")
        ),
        Recipe(
            id = UUID.randomUUID().toString(),
            title = "Fırınlanmış Baharatlı $i1 & Tahin Sos",
            subtitle = "Karamelize sebzelerin kremamsı susam ezmesiyle buluştuğu gurme tabak",
            category = "Vejetaryen / Vegan",
            prepTimeMinutes = 12,
            cookTimeMinutes = 22,
            difficulty = "Kolay",
            calories = 310,
            proteinGrams = 11,
            carbsGrams = 26,
            fatGrams = 18,
            servings = 2,
            matchedIngredients = items,
            pantryIngredients = listOf("Tahin", "Limon", "Zeytinyağı", "Kimyon"),
            instructions = listOf(
                "$i1 sebzelerini iri parçalara bölüp zeytinyağı ve kimyonla ovun.",
                "200°C fırında kenarları hafifçe karamelize olana dek 20 dakika kızartın.",
                "Tahin, limon ve sarımsak sosunu hazırlayın.",
                "Fırından çıkan sıcak sebzelerin üzerine gezdirip taze nar taneleri veya otlarla süsleyin."
            ),
            chefTip = "Şef Willy Püf Noktası: Tahinin içindeki doymamış yağ asitleri sebzelerdeki A ve E vitaminlerinin emilimini 3 kat artırır.",
            dietaryTags = listOf("Vegan", "Glutensiz", "Gurme Sunum")
        ),
        Recipe(
            id = UUID.randomUUID().toString(),
            title = "Taze Otlu $i1 Mücveri",
            subtitle = "Fırında pişen, dereotu ve taze nane kokulu hafif mücver",
            category = "Vejetaryen / Vegan",
            prepTimeMinutes = 15,
            cookTimeMinutes = 25,
            difficulty = "Orta",
            calories = 270,
            proteinGrams = 12,
            carbsGrams = 28,
            fatGrams = 12,
            servings = 3,
            matchedIngredients = items.take(3),
            pantryIngredients = listOf("Yumurta", "Taze soğan", "Dereotu", "Karabiber"),
            instructions = listOf(
                "$i1 sebzesini rendeleyin ve suyunu sıkın.",
                "İnce kıyılmış taze otlar, yumurta ve baharatlarla yoğurun.",
                "Yağlı kağıt serili fırın tepsisine döküp 185°C'de 25 dakika pişirin.",
                "Ilık servis edin."
            ),
            chefTip = "Şef Willy Püf Noktası: Harca bir tutam kuru nane eklemek sebze kokusunu zenginleştirir.",
            dietaryTags = listOf("Vejetaryen", "Fırında", "Taze Otlu")
        ),
        Recipe(
            id = UUID.randomUUID().toString(),
            title = "Kremalı $i1 & Mantar Sote",
            subtitle = "Aromatik mantarlar ve taze sebzelerin ipeksi buluşması",
            category = "Vejetaryen / Vegan",
            prepTimeMinutes = 10,
            cookTimeMinutes = 15,
            difficulty = "Kolay",
            calories = 320,
            proteinGrams = 10,
            carbsGrams = 18,
            fatGrams = 21,
            servings = 2,
            matchedIngredients = items,
            pantryIngredients = listOf("Zeytinyağı veya bitkisel krema", "Sarımsak", "Kekik", "Karabiber"),
            instructions = listOf(
                "Tavada sarımsakları zeytinyağında 1 dakika çevirin.",
                "$i1 ve $i2 doğranmış parçalarını ekleyip yüksek ateşte 8 dakika soteleyin.",
                "Taze kekik ve kremayı (veya yulaf kremasını) ekleyip 3 dakika çektirin.",
                "Sıcak servis yapın."
            ),
            chefTip = "Şef Willy Püf Noktası: Yüksek ateş sebzelerin sulanmasını engelleyerek tatlarını yoğunlaştırır.",
            dietaryTags = listOf("Vejetaryen", "Hızlı", "Zengin Aroma")
        ),
        Recipe(
            id = UUID.randomUUID().toString(),
            title = "Renkli $i1 & Tahıl Güveci",
            subtitle = "Lif ve mineral deposu, kış ve yaz sofralarının taze sebze güveci",
            category = "Vejetaryen / Vegan",
            prepTimeMinutes = 15,
            cookTimeMinutes = 35,
            difficulty = "Orta",
            calories = 290,
            proteinGrams = 11,
            carbsGrams = 42,
            fatGrams = 8,
            servings = 4,
            matchedIngredients = items,
            pantryIngredients = listOf("Zeytinyağı", "Defne yaprağı", "Domates rendesi", "Tuz"),
            instructions = listOf(
                "Güveç tenceresine sırasıyla sert sebzelerden yumuşak sebzelere dizin.",
                "Aralara sarımsak ve defne yaprağı yerleştirin.",
                "Üzerine zeytinyağı ve domates sosunu döküp kapağını kapatın.",
                "190°C fırında 35 dakika pişirin."
            ),
            chefTip = "Şef Willy Püf Noktası: Defne yaprağı güveçteki sebzelere orman tazeliğinde harika bir buğu kazandırır.",
            dietaryTags = listOf("Vegan", "Güveç", "Lif Deposu")
        )
    )

    private fun generateQuickRecipes(
        items: List<String>,
        i1: String,
        i2: String,
        i3: String
    ): List<Recipe> = listOf(
        Recipe(
            id = UUID.randomUUID().toString(),
            title = "15 Dakikada Çıtır $i1 Tava",
            subtitle = "Zamanınız kısıtlıyken lezzetten ödün vermeyen süper hızlı tava yemeği",
            category = "Pratik & Hızlı",
            prepTimeMinutes = 5,
            cookTimeMinutes = 10,
            difficulty = "Çok Kolay",
            calories = 310,
            proteinGrams = 14,
            carbsGrams = 20,
            fatGrams = 16,
            servings = 2,
            matchedIngredients = items,
            pantryIngredients = listOf("Zeytinyağı", "Kırmızı toz biber", "Sarımsak", "Tuz"),
            instructions = listOf(
                "Sebzeleri olabildiğince ince jülyen doğrayın (hızlı pişmesi için).",
                "Tavayı maksimum ateşte ısıtıp yağı dökün.",
                "Malzemeleri tavaya atıp hiç durmadan 8 dakika sallayarak soteleyin.",
                "Baharatları ekleyip hemen ocaktan alın."
            ),
            chefTip = "Şef Willy Püf Noktası: Malzemeleri ince jülyen kesmek pişme süresini yarı yarıya azaltır.",
            dietaryTags = listOf("15 Dakika", "Pratik", "Öğrenci & Çalışan Dostu")
        ),
        Recipe(
            id = UUID.randomUUID().toString(),
            title = "Hızlı Fincan Omleti ($i1 & Peynir)",
            subtitle = "Mikrodalgada veya tavada 6 dakikada kabararak pişen acil kurtarıcı",
            category = "Pratik & Hızlı",
            prepTimeMinutes = 3,
            cookTimeMinutes = 5,
            difficulty = "Çok Kolay",
            calories = 240,
            proteinGrams = 18,
            carbsGrams = 6,
            fatGrams = 15,
            servings = 1,
            matchedIngredients = items.take(2),
            pantryIngredients = listOf("Yumurta (2 adet)", "Kaşar veya beyaz peynir", "Tuz"),
            instructions = listOf(
                "Geniş fincanı veya küçük tavayı yağlayın.",
                "İnce doğranmış $i1 ve peyniri fincana koyun.",
                "2 yumurtayı tuzla çırpıp üzerine dökün.",
                "Tavada kısık ateşte 5 dakikada pişirin veya mikrodalgada 2 dakikada hazır!"
            ),
            chefTip = "Şef Willy Püf Noktası: Fincan omleti sabah koşuşturmasında bulaşık çıkarmadan gününüzü kurtarır.",
            dietaryTags = listOf("6 Dakika", "Sıfır Bulaşık", "Kahvaltı")
        ),
        Recipe(
            id = UUID.randomUUID().toString(),
            title = "Pratik $i1 & Lavaş Dürümü",
            subtitle = "Tavada hafifçe ısıtılmış sebzelerle hazırlanan sıcak sokak lezzeti",
            category = "Pratik & Hızlı",
            prepTimeMinutes = 5,
            cookTimeMinutes = 7,
            difficulty = "Çok Kolay",
            calories = 360,
            proteinGrams = 12,
            carbsGrams = 42,
            fatGrams = 14,
            servings = 1,
            matchedIngredients = items,
            pantryIngredients = listOf("Tam buğday lavaş", "Zeytinyağı", "Yoğurt sos", "Pul biber"),
            instructions = listOf(
                "Tavada $i1 malzemesini 4 dakika yüksek ateşte soteleyin.",
                "Lavaşı tavanın üstüne kapatıp 1 dakika buharıyla ısıtın.",
                "Lavaşın içine sote sebzeleri ve yoğurt sosunu yayın.",
                "Sıkıca sarıp dörde bölün ve afiyetle yiyin."
            ),
            chefTip = "Şef Willy Püf Noktası: Lavaşın tavadaki sebzelerin buharıyla ısınması ekmeği pamuk gibi yumuşatır.",
            dietaryTags = listOf("12 Dakika", "Dürüm", "Doyurucu")
        ),
        Recipe(
            id = UUID.randomUUID().toString(),
            title = "Tek Tavada $i1 & Makarna",
            subtitle = "Makarnayı ayrı haşlamadan, sosuyla aynı tavada 15 dakikada pişen mucize",
            category = "Pratik & Hızlı",
            prepTimeMinutes = 5,
            cookTimeMinutes = 12,
            difficulty = "Kolay",
            calories = 420,
            proteinGrams = 15,
            carbsGrams = 62,
            fatGrams = 12,
            servings = 2,
            matchedIngredients = items,
            pantryIngredients = listOf("Makarna", "Sıcak su", "Zeytinyağı", "Fesleğen"),
            instructions = listOf(
                "Geniş tavaya doğranmış $i1, $i2 ve makarnayı yan yana koyun.",
                "Üzerini geçecek kadar sıcak su ve 2 kaşık zeytinyağı ekleyin.",
                "Orta ateşte suyunu çekene kadar 10-12 dakika karıştırarak kaynatın.",
                "Kendi nişastasıyla kıvam alan kremamsı makarnayı servis edin."
            ),
            chefTip = "Şef Willy Püf Noktası: Tek tavada makarna pişirildiğinde nişasta suya dökülmez, sosun içine bağlanır ve restoran lezzeti verir.",
            dietaryTags = listOf("Tek Tava", "15 Dakika", "Konfor Yemeği")
        ),
        Recipe(
            id = UUID.randomUUID().toString(),
            title = "Çırpılmış $i1 Şef Tostu",
            subtitle = "Sıradan tostu unutturan, sebzeli ve bol peynirli gurme lezzet",
            category = "Pratik & Hızlı",
            prepTimeMinutes = 4,
            cookTimeMinutes = 6,
            difficulty = "Çok Kolay",
            calories = 340,
            proteinGrams = 16,
            carbsGrams = 32,
            fatGrams = 16,
            servings = 1,
            matchedIngredients = items.take(3),
            pantryIngredients = listOf("Ekmek", "Kaşar peyniri", "Tereyağı", "Kekik"),
            instructions = listOf(
                "$i1 sebzesini tavada 2 dakika yumuşatın.",
                "İki dilim ekmeğin arasına peyniri ve sıcak sebzeleri yerleştirin.",
                "Tost makinesinde veya tavada iki tarafı altın sarısı olana dek bastırın.",
                "Üçgen kesip sıcak servis yapın."
            ),
            chefTip = "Şef Willy Püf Noktası: Ekmeğin dışına çok az tereyağı sürmek profesyonel çıtırlık sağlar.",
            dietaryTags = listOf("10 Dakika", "Çıtır", "Gurme Tost")
        )
    )

    private fun generateTraditionalRecipes(
        items: List<String>,
        i1: String,
        i2: String,
        i3: String
    ): List<Recipe> = listOf(
        Recipe(
            id = UUID.randomUUID().toString(),
            title = "Geleneksel Tencere $i1 Yemeği",
            subtitle = "Salçalı, soğanlı ve mis gibi kokan anne usulü klasik Türk tencere yemeği",
            category = "Geleneksel Ev Yemeği",
            prepTimeMinutes = 15,
            cookTimeMinutes = 35,
            difficulty = "Kolay",
            calories = 320,
            proteinGrams = 12,
            carbsGrams = 32,
            fatGrams = 14,
            servings = 4,
            matchedIngredients = items,
            pantryIngredients = listOf("Kuru soğan", "Biber salçası", "Zeytinyağı", "Kaya tuzu"),
            instructions = listOf(
                "Tencerede yemeklik doğranmış soğanı zeytinyağında pembeleşene kadar kavurun.",
                "1 tatlı kaşığı biber ve domates salçasını ekleyip kokusu çıkana dek 2 dakika çevirin.",
                "$i1 ve diğer sebzeleri ekleyip tencerenin kapağı kapalı 5 dakika terletin.",
                "Sebzelerin hizasına gelecek kadar sıcak su ekleyip kısık ateşte 30 dakika pişirin."
            ),
            chefTip = "Şef Willy Püf Noktası: Salçayı iyice kavurmak ekşiliğini alır ve yemeğin rengini parlak yakut kırmızısı yapar.",
            dietaryTags = listOf("Ev Yemeği", "Anne Usulü", "Geleneksel", "Tencere")
        ),
        Recipe(
            id = UUID.randomUUID().toString(),
            title = "Fırında $i1 Dizme Kebabı",
            subtitle = "Tepsiye özenle dizilip odun fırını lezzetinde pişirilen geleneksel fırın yemeği",
            category = "Geleneksel Ev Yemeği",
            prepTimeMinutes = 20,
            cookTimeMinutes = 35,
            difficulty = "Orta",
            calories = 410,
            proteinGrams = 22,
            carbsGrams = 28,
            fatGrams = 22,
            servings = 3,
            matchedIngredients = items,
            pantryIngredients = listOf("Kıyma veya kuşbaşı", "Sarımsak", "Domates sosu", "Kekik"),
            instructions = listOf(
                "$i1 ve diğer sebzeleri yuvarlak dilimler halinde kesin.",
                "Tepsiye sırasıyla bir sebze, bir kıyma/et harcı olacak şekilde dizin.",
                "Sarımsaklı, salçalı ve kekikli sıcak sosu üzerine gezdirin.",
                "200°C fırında üzeri kızarana dek 35 dakika fırınlayın."
            ),
            chefTip = "Şef Willy Püf Noktası: Fırınlamanın ilk 20 dakikasında üzerini ıslatılmış yağlı kağıtla kapatırsanız sebzeler kurumaz.",
            dietaryTags = listOf("Fırın Yemeği", "Ziyafet", "Geleneksel")
        ),
        Recipe(
            id = UUID.randomUUID().toString(),
            title = "Köz Kokulu $i1 & Biber Mezesi",
            subtitle = "Geleneksel ocak üstü köz tadında, zeytinyağlı ve sirkeli nefis meze",
            category = "Geleneksel Ev Yemeği",
            prepTimeMinutes = 15,
            cookTimeMinutes = 15,
            difficulty = "Kolay",
            calories = 190,
            proteinGrams = 6,
            carbsGrams = 18,
            fatGrams = 11,
            servings = 4,
            matchedIngredients = items.take(3),
            pantryIngredients = listOf("Zeytinyağı", "Üzüm sirkesi", "Sarımsak", "Tuz"),
            instructions = listOf(
                "$i1 ve biberleri fırının ızgara ayarında veya kuru tavada kabukları kararana kadar közleyin.",
                "Sıcakken bir poşete koyup ağzını bağlayın (buharıyla kabukları kolay soyulur).",
                "Kabuklarını soyup ince ince kıyın.",
                "Dövülmüş sarımsak, zeytinyağı ve az sirke ile harmanlayıp dinlendirin."
            ),
            chefTip = "Şef Willy Püf Noktası: Közlenen sebzeleri poşette 5 dakika bekletmek kabukların saniyeler içinde soyulmasını sağlar.",
            dietaryTags = listOf("Közleme", "Meze", "Geleneksel", "Hafif")
        ),
        Recipe(
            id = UUID.randomUUID().toString(),
            title = "Anadolu Usulü $i1 Çorbası",
            subtitle = "Kemik suyu aroması ve tereyağlı nane yakmasıyla şifa deposu geleneksel çorba",
            category = "Geleneksel Ev Yemeği",
            prepTimeMinutes = 10,
            cookTimeMinutes = 20,
            difficulty = "Kolay",
            calories = 220,
            proteinGrams = 9,
            carbsGrams = 22,
            fatGrams = 10,
            servings = 4,
            matchedIngredients = items,
            pantryIngredients = listOf("Tereyağı", "Nane", "Pul biber", "Kemik suyu veya su"),
            instructions = listOf(
                "Tencerede tereyağında $i1 ve $i2 malzemelerini soteleyin.",
                "Sıcak kemik suyunu ilave edip sebzeler yumuşayana kadar kaynatın.",
                "Kepçeyle veya blenderla ezip pürüzsüzleştirin.",
                "Küçük bir cezvede tereyağında nane ve pul biberi köpürtüp çorbanın üzerine dökün."
            ),
            chefTip = "Şef Willy Püf Noktası: Çorbanın üzerine dökülen tereyağlı nane yakması Anadolu mutfağının baş tacıdır.",
            dietaryTags = listOf("Şifa Deposu", "Geleneksel Çorba", "Bağışıklık")
        ),
        Recipe(
            id = UUID.randomUUID().toString(),
            title = "Köy Usulü $i1 Kavurması",
            subtitle = "Kendi suyunda ağır ağır karamelize olan ve tandır kokan köy kavurması",
            category = "Geleneksel Ev Yemeği",
            prepTimeMinutes = 10,
            cookTimeMinutes = 20,
            difficulty = "Çok Kolay",
            calories = 280,
            proteinGrams = 8,
            carbsGrams = 24,
            fatGrams = 16,
            servings = 2,
            matchedIngredients = items,
            pantryIngredients = listOf("Tereyağı / Zeytinyağı", "Kuru soğan", "Pul biber", "Kaya tuzu"),
            instructions = listOf(
                "Döküm veya bakır tavada yağı kızdırın ve soğanları yarım ay doğrayıp ekleyin.",
                "$i1 ve diğer malzemeleri ekleyip orta ateşte kapağı kapalı olarak pişmeye bırakın.",
                "Sebzeler yumuşayınca kapağı açıp tavanın dibi hafifçe tutana dek kızartın.",
                "Taze ekmekle sıcak sıcak servis yapın."
            ),
            chefTip = "Şef Willy Püf Noktası: Sebzeleri kavururken tavanın tabanındaki lezzetli kahverengi tortuları tahta kaşıkla kazıyarak yemeğe yedirin.",
            dietaryTags = listOf("Köy Usulü", "Kavurma", "Doğal Lezzet")
        )
    )

    private fun generateDessertRecipes(
        items: List<String>,
        i1: String,
        i2: String,
        i3: String
    ): List<Recipe> = listOf(
        Recipe(
            id = UUID.randomUUID().toString(),
            title = "Fırında Karamelize $i1 Tatlısı",
            subtitle = "Rafine şeker yerine bal ve tarçınla fırınlanmış hafif meyve/sebze tatlısı",
            category = "Fit Tatlı & Atıştırmalık",
            prepTimeMinutes = 8,
            cookTimeMinutes = 20,
            difficulty = "Çok Kolay",
            calories = 180,
            proteinGrams = 4,
            carbsGrams = 36,
            fatGrams = 3,
            servings = 2,
            matchedIngredients = items.take(2),
            pantryIngredients = listOf("Doğal bal veya pekmez", "Tarçın", "Ceviz", "Limon"),
            instructions = listOf(
                "$i1 malzemesini dilimleyip fırın kabına dizin.",
                "Üzerine tarçın ve birkaç damla limon gezdirin.",
                "180°C fırında yumuşayıp karamel kokusu yayılana dek 20 dakika pişirin.",
                "Fırından çıkınca üzerine 1 tatlı kaşığı bal ve dövülmüş ceviz ekleyin."
            ),
            chefTip = "Şef Willy Püf Noktası: Balı fırına sokmadan, fırından çıktıktan sonra ılıkken eklemek balın tüm şifasını korur.",
            dietaryTags = listOf("Rafine Şekersiz", "Doğal Tatlı", "Fit", "Hafif")
        ),
        Recipe(
            id = UUID.randomUUID().toString(),
            title = "Fit $i1 & Yulaf Topları",
            subtitle = "Pişirme gerektirmeyen, kahve yanı 5 dakikalık enerji lokmaları",
            category = "Fit Tatlı & Atıştırmalık",
            prepTimeMinutes = 8,
            cookTimeMinutes = 0,
            difficulty = "Çok Kolay",
            calories = 145,
            proteinGrams = 5,
            carbsGrams = 22,
            fatGrams = 5,
            servings = 4,
            matchedIngredients = items.take(2),
            pantryIngredients = listOf("Yulaf ezmesi", "Tahin veya fıstık ezmesi", "Kakao", "Hindistan cevizi"),
            instructions = listOf(
                "Malzemeleri rondoda veya çatalla püre haline getirin.",
                "Yulaf ve tahin ile karıştırıp ele yapışmayan kıvam elde edin.",
                "Ceviz büyüklüğünde toplar yuvarlayın.",
                "Hindistan cevizine veya kakaoya bulayıp buzdolabında 15 dakika soğutun."
            ),
            chefTip = "Şef Willy Püf Noktası: Buzdolabında hava almayan bir kapta 1 hafta tazeliğini korur.",
            dietaryTags = listOf("Pişirme Gerektirmez", "Enerji Deposu", "Kahve Yanı")
        ),
        Recipe(
            id = UUID.randomUUID().toString(),
            title = "Ilık $i1 & Yoğurt Parfesi",
            subtitle = "Kıtır cevizler ve tarçınla taçlanan proteinli tatlı kupları",
            category = "Fit Tatlı & Atıştırmalık",
            prepTimeMinutes = 6,
            cookTimeMinutes = 4,
            difficulty = "Çok Kolay",
            calories = 190,
            proteinGrams = 12,
            carbsGrams = 24,
            fatGrams = 5,
            servings = 2,
            matchedIngredients = items.take(2),
            pantryIngredients = listOf("Süzme yoğurt", "Bal", "Ceviz", "Tarçın"),
            instructions = listOf(
                "$i1 dilimlerini tavada tarçınla 3 dakika hafifçe yumuşatın.",
                "Kupların tabanına süzme yoğurt koyun.",
                "Üzerine ılık tarçınlı sebze/meyveyi ekleyin.",
                "Bal ve cevizle süsleyerek servis yapın."
            ),
            chefTip = "Şef Willy Püf Noktası: Ilık harç ile soğuk süzme yoğurdun kontrastı ağızda harika bir krema hissi yaratır.",
            dietaryTags = listOf("Tatlı Krizi Çözümü", "Yüksek Kalsiyum", "Fit")
        ),
        Recipe(
            id = UUID.randomUUID().toString(),
            title = "Fit $i1 Kekçikleri (Unsuz)",
            subtitle = "Muz ve yumurta bazlı, mikrodalgada veya fırında 10 dakikalık muffin",
            category = "Fit Tatlı & Atıştırmalık",
            prepTimeMinutes = 5,
            cookTimeMinutes = 12,
            difficulty = "Kolay",
            calories = 160,
            proteinGrams = 6,
            carbsGrams = 20,
            fatGrams = 6,
            servings = 2,
            matchedIngredients = items.take(2),
            pantryIngredients = listOf("Yumurta (1 adet)", "Kakao", "Tarçın", "Kabartma tozu"),
            instructions = listOf(
                "$i1 malzemesini çatalla iyice ezin.",
                "Yumurta, kakao ve bir çimdik kabartma tozuyla çırpın.",
                "Muffin kaplarına veya fincanlara paylaştırın.",
                "180°C fırında 12 dakika pişirin."
            ),
            chefTip = "Şef Willy Püf Noktası: İçine ekleyeceğiniz bir parça bitter çikolata içi akışkan bir lezzet bombasına dönüştürür.",
            dietaryTags = listOf("Unsuz", "Şekersiz", "Pratik Muffin")
        ),
        Recipe(
            id = UUID.randomUUID().toString(),
            title = "Çıtır $i1 Cipsleri (Tarçınlı)",
            subtitle = "Fırında kurutularak çıtırlaşan doğal tatlı cipsler",
            category = "Fit Tatlı & Atıştırmalık",
            prepTimeMinutes = 5,
            cookTimeMinutes = 20,
            difficulty = "Çok Kolay",
            calories = 110,
            proteinGrams = 2,
            carbsGrams = 26,
            fatGrams = 1,
            servings = 2,
            matchedIngredients = items.take(2),
            pantryIngredients = listOf("Toz tarçın", "Bir damla limon"),
            instructions = listOf(
                "$i1 malzemesini mandolin veya keskin bıçakla incecik dilimleyin.",
                "Yağlı kağıt serili tepsiye aralıklı dizin ve tarçın serpin.",
                "160°C fırında 20 dakika kuruyana kadar pişirin.",
                "Soğudukça çıtırlaşan cipslerinizi keyifle tüketin."
            ),
            chefTip = "Şef Willy Püf Noktası: Fırın kapağını hafif aralık bırakmak buharın çıkmasını sağlayarak çıtırlığı artırır.",
            dietaryTags = listOf("Düşük Kalori", "Doğal Çıtır", "Glutensiz")
        )
    )

    private fun generateGourmetMasterRecipes(
        items: List<String>,
        i1: String,
        i2: String,
        i3: String
    ): List<Recipe> = listOf(
        Recipe(
            id = UUID.randomUUID().toString(),
            title = "Şef Willy Özel Fırın $i1 Yatağında Lezzet",
            subtitle = "Karamelize sebzeler ve gurme baharat harmanı ile restoran kalitesinde tabak",
            category = "Tüm Lezzetler",
            prepTimeMinutes = 15,
            cookTimeMinutes = 25,
            difficulty = "Orta",
            calories = 380,
            proteinGrams = 24,
            carbsGrams = 28,
            fatGrams = 18,
            servings = 2,
            matchedIngredients = items,
            pantryIngredients = listOf("Sarımsaklı zeytinyağı", "Taze biberiye", "Deniz tuzu", "Karabiber"),
            instructions = listOf(
                "Fırını 200°C'ye ısıtın.",
                "$i1, $i2 ve $i3 malzemelerini aromatik sarımsaklı zeytinyağı ve biberiye ile harmanlayın.",
                "Tepsiye dengeli şekilde yayın ve 25 dakika altın rengi alana dek fırınlayın.",
                "Taze çekilmiş karabiber serpiştirerek sıcak servis yapın."
            ),
            chefTip = "Şef Willy Püf Noktası: Taze biberiye dalını elinizle hafifçe ezerek yağa yatırın; aroması tüm yemeğe nüfuz eder.",
            dietaryTags = listOf("Şef İmzası", "Gurme", "Dengeli Beslenme")
        ),
        Recipe(
            id = UUID.randomUUID().toString(),
            title = "Tavada Mühürlenmiş $i1 & $i2 Şöleni",
            subtitle = "Yüksek ısıda karamelize edilmiş, sulu ve yoğun lezzetli tava yemeği",
            category = "Tüm Lezzetler",
            prepTimeMinutes = 10,
            cookTimeMinutes = 14,
            difficulty = "Kolay",
            calories = 340,
            proteinGrams = 20,
            carbsGrams = 22,
            fatGrams = 17,
            servings = 2,
            matchedIngredients = items,
            pantryIngredients = listOf("Tereyağı & zeytinyağı", "Taze kekik", "Pul biber"),
            instructions = listOf(
                "Döküm tavayı dumanı tütene kadar ısıtın.",
                "Malzemeleri tavaya ekleyip ilk 2 dakika hiç karıştırmadan mühürleyin.",
                "Ardından bir parça tereyağı ve kekik ekleyip tıkırdatarak soteleyin.",
                "Sıcak servis yapın."
            ),
            chefTip = "Şef Willy Püf Noktası: Malzemeleri tavaya atar atmaz karıştırmayın; mühür tabakasının oluşması için ilk 2 dakika temas etmelidir.",
            dietaryTags = listOf("Mühürleme", "Yoğun Lezzet", "15 Dakika")
        ),
        Recipe(
            id = UUID.randomUUID().toString(),
            title = "İpeksi $i1 Kremalı Gurme Çorbası",
            subtitle = "Zengin kadife dokusu ve tereyağlı çıtır baharatlarıyla başlangıç ziyafeti",
            category = "Tüm Lezzetler",
            prepTimeMinutes = 10,
            cookTimeMinutes = 18,
            difficulty = "Kolay",
            calories = 230,
            proteinGrams = 9,
            carbsGrams = 26,
            fatGrams = 10,
            servings = 4,
            matchedIngredients = items,
            pantryIngredients = listOf("Zeytinyağı", "Süt veya krema", "Muskat rendesi", "Kaya tuzu"),
            instructions = listOf(
                "Tencerede zeytinyağında sebzeleri 4 dakika soteleyin.",
                "Üzerine sıcak su veya et suyu döküp 15 dakika pişirin.",
                "Blender ile pürüzsüzleştirip çok az süt/krema ve bir çimdik muskat ekleyin.",
                "Kısık ateşte 2 dakika tıkırdatıp sıcak servis edin."
            ),
            chefTip = "Şef Willy Püf Noktası: Muskat rendesi kök sebze çorbalarına olağanüstü bir derinlik kazandırır.",
            dietaryTags = listOf("Kadife Kıvam", "Gurme Çorba", "Şef Tavsiyesi")
        ),
        Recipe(
            id = UUID.randomUUID().toString(),
            title = "Willy Usulü Kat Kat $i1 Fırın Grateni",
            subtitle = "Fırında üzeri altın gibi kızarmış, eriyen peynirli lezzet katmanları",
            category = "Tüm Lezzetler",
            prepTimeMinutes = 15,
            cookTimeMinutes = 25,
            difficulty = "Orta",
            calories = 420,
            proteinGrams = 22,
            carbsGrams = 34,
            fatGrams = 22,
            servings = 3,
            matchedIngredients = items,
            pantryIngredients = listOf("Kaşar peyniri", "Yumurta", "Zeytinyağı", "Karabiber"),
            instructions = listOf(
                "$i1 ve $i2 malzemelerini ince ince dilimleyin.",
                "Fırın kabına sebzeleri kat kat döşeyin.",
                "Çırpılmış 1 yumurta ve baharat sosunu aralara dökün.",
                "Üzerini bol kaşar peyniriyle kaplayıp 190°C'de 25 dakika nar gibi olana kadar pişirin."
            ),
            chefTip = "Şef Willy Püf Noktası: Graten fırından çıkınca 5 dakika dinlendirilirse dilimler dağılmadan kalıp gibi çıkar.",
            dietaryTags = listOf("Graten", "Peynirli", "Ziyafet")
        ),
        Recipe(
            id = UUID.randomUUID().toString(),
            title = "Akdeniz Rüzgarı: Ilık $i1 & Taze Otlu Salata",
            subtitle = "Kavrulmuş tohumlar, zeytinyağlı limon emülsiyonu ve taze lezzetler",
            category = "Tüm Lezzetler",
            prepTimeMinutes = 10,
            cookTimeMinutes = 8,
            difficulty = "Çok Kolay",
            calories = 270,
            proteinGrams = 10,
            carbsGrams = 24,
            fatGrams = 15,
            servings = 2,
            matchedIngredients = items,
            pantryIngredients = listOf("Sızma zeytinyağı", "Limon", "Ceviz veya susam", "Taze nane"),
            instructions = listOf(
                "$i1 sebzesini tavada çok hafif 4 dakika soteleyin (ılık kalması için).",
                "Geniş kasede taze yeşilliklerle ılık sebzeyi birleştirin.",
                "Zeytinyağı, limon ve az hardal ile çırpılmış sosu üzerine gezdirin.",
                "Kavrulmuş tohumlar ve ceviz serpip servis yapın."
            ),
            chefTip = "Şef Willy Püf Noktası: Ilık sebzelerin yeşilliklerin üzerine konması otların aromasını havaya salar.",
            dietaryTags = listOf("Akdeniz", "Taze", "Antioksidan")
        )
    )
}
