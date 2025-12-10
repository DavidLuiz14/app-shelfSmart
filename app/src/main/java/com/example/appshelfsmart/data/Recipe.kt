package com.example.appshelfsmart.data

data class Recipe(
    val id: String,
    val name: String,
    val category: String,
    val area: String,
    val instructions: String,
    val thumbnail: String,
    val ingredients: List<RecipeIngredient>,
    val youtubeUrl: String? = null
)

data class RecipeIngredient(
    val name: String,
    val measure: String,
    val available: Boolean = false
)

// API Response models
data class MealListResponse(
    val meals: List<MealSummary>?
)

data class MealSummary(
    val idMeal: String,
    val strMeal: String,
    val strMealThumb: String
)

data class MealDetailResponse(
    val meals: List<MealDetail>?
)

data class MealDetail(
    val idMeal: String,
    val strMeal: String,
    val strCategory: String?,
    val strArea: String?,
    val strInstructions: String?,
    val strMealThumb: String?,
    val strYoutube: String?,
    val strIngredient1: String?,
    val strIngredient2: String?,
    val strIngredient3: String?,
    val strIngredient4: String?,
    val strIngredient5: String?,
    val strIngredient6: String?,
    val strIngredient7: String?,
    val strIngredient8: String?,
    val strIngredient9: String?,
    val strIngredient10: String?,
    val strIngredient11: String?,
    val strIngredient12: String?,
    val strIngredient13: String?,
    val strIngredient14: String?,
    val strIngredient15: String?,
    val strIngredient16: String?,
    val strIngredient17: String?,
    val strIngredient18: String?,
    val strIngredient19: String?,
    val strIngredient20: String?,
    val strMeasure1: String?,
    val strMeasure2: String?,
    val strMeasure3: String?,
    val strMeasure4: String?,
    val strMeasure5: String?,
    val strMeasure6: String?,
    val strMeasure7: String?,
    val strMeasure8: String?,
    val strMeasure9: String?,
    val strMeasure10: String?,
    val strMeasure11: String?,
    val strMeasure12: String?,
    val strMeasure13: String?,
    val strMeasure14: String?,
    val strMeasure15: String?,
    val strMeasure16: String?,
    val strMeasure17: String?,
    val strMeasure18: String?,
    val strMeasure19: String?,
    val strMeasure20: String?
) {
    fun toRecipe(availableIngredients: Set<String>): Recipe {
        val ingredients = mutableListOf<RecipeIngredient>()
        
        // Extract all non-null ingredients and measures
        listOf(
            strIngredient1 to strMeasure1, strIngredient2 to strMeasure2,
            strIngredient3 to strMeasure3, strIngredient4 to strMeasure4,
            strIngredient5 to strMeasure5, strIngredient6 to strMeasure6,
            strIngredient7 to strMeasure7, strIngredient8 to strMeasure8,
            strIngredient9 to strMeasure9, strIngredient10 to strMeasure10,
            strIngredient11 to strMeasure11, strIngredient12 to strMeasure12,
            strIngredient13 to strMeasure13, strIngredient14 to strMeasure14,
            strIngredient15 to strMeasure15, strIngredient16 to strMeasure16,
            strIngredient17 to strMeasure17, strIngredient18 to strMeasure18,
            strIngredient19 to strMeasure19, strIngredient20 to strMeasure20
        ).forEach { (ingredient, measure) ->
            if (!ingredient.isNullOrBlank() && ingredient.trim().isNotEmpty()) {
                val available = availableIngredients.any { 
                    it.contains(ingredient.trim(), ignoreCase = true) ||
                    ingredient.trim().contains(it, ignoreCase = true)
                }
                ingredients.add(
                    RecipeIngredient(
                        name = ingredient.trim(),
                        measure = measure?.trim() ?: "",
                        available = available
                    )
                )
            }
        }
        
        return Recipe(
            id = idMeal,
            name = strMeal,
            category = strCategory ?: "",
            area = strArea ?: "",
            instructions = strInstructions ?: "",
            thumbnail = strMealThumb ?: "",
            ingredients = ingredients,
            youtubeUrl = strYoutube
        )
    }
}
