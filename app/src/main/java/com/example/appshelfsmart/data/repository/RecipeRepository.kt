package com.example.appshelfsmart.data.repository

import com.example.appshelfsmart.data.Recipe
import com.example.appshelfsmart.data.api.TheMealDBApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RecipeRepository {
    
    private val api: TheMealDBApi = Retrofit.Builder()
        .baseUrl(TheMealDBApi.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(TheMealDBApi::class.java)
    
    /**
     * Translate common Spanish ingredients to English for API search
     */
    private fun translateIngredient(spanish: String): List<String> {
        val normalized = spanish.lowercase().trim()
        
        // Return multiple English variations to increase search results
        return when {
            // Carnes
            normalized.contains("pollo") || normalized.contains("chicken") -> listOf("chicken", "chicken breast")
            normalized.contains("carne") || normalized.contains("res") -> listOf("beef", "ground beef")
            normalized.contains("cerdo") || normalized.contains("pork") -> listOf("pork")
            normalized.contains("pescado") || normalized.contains("fish") -> listOf("fish", "salmon", "cod")
            normalized.contains("camarón") || normalized.contains("shrimp") -> listOf("shrimp", "prawns")
            
            // Lácteos
            normalized.contains("leche") || normalized.contains("milk") -> listOf("milk")
            normalized.contains("queso") || normalized.contains("cheese") -> listOf("cheese", "cheddar", "parmesan")
            normalized.contains("yogurt") -> listOf("yogurt")
            normalized.contains("mantequilla") || normalized.contains("butter") -> listOf("butter")
            normalized.contains("crema") || normalized.contains("cream") -> listOf("cream", "heavy cream")
            
            // Vegetales
            normalized.contains("tomate") || normalized.contains("tomato") -> listOf("tomato", "tomatoes")
            normalized.contains("cebolla") || normalized.contains("onion") -> listOf("onion", "onions")
            normalized.contains("ajo") || normalized.contains("garlic") -> listOf("garlic")
            normalized.contains("papa") || normalized.contains("patata") || normalized.contains("potato") -> listOf("potato", "potatoes")
            normalized.contains("zanahoria") || normalized.contains("carrot") -> listOf("carrot", "carrots")
            normalized.contains("lechuga") || normalized.contains("lettuce") -> listOf("lettuce")
            normalized.contains("espinaca") || normalized.contains("spinach") -> listOf("spinach")
            normalized.contains("brócoli") || normalized.contains("broccoli") -> listOf("broccoli")
            normalized.contains("pimiento") || normalized.contains("pepper") -> listOf("pepper", "bell pepper")
            normalized.contains("chile") -> listOf("chili", "pepper")
            
            // Granos y cereales
            normalized.contains("arroz") || normalized.contains("rice") -> listOf("rice")
            normalized.contains("pasta") || normalized.contains("spaghetti") -> listOf("pasta", "spaghetti")
            normalized.contains("pan") || normalized.contains("bread") -> listOf("bread")
            normalized.contains("harina") || normalized.contains("flour") -> listOf("flour")
            normalized.contains("avena") || normalized.contains("oats") -> listOf("oats")
            
            // Frutas
            normalized.contains("manzana") || normalized.contains("apple") -> listOf("apple")
            normalized.contains("plátano") || normalized.contains("banana") -> listOf("banana")
            normalized.contains("naranja") || normalized.contains("orange") -> listOf("orange")
            normalized.contains("limón") || normalized.contains("lemon") -> listOf("lemon")
            normalized.contains("fresa") || normalized.contains("strawberry") -> listOf("strawberry")
            
            // Otros
            normalized.contains("huevo") || normalized.contains("egg") -> listOf("egg", "eggs")
            normalized.contains("aceite") || normalized.contains("oil") -> listOf("oil", "olive oil")
            normalized.contains("sal") || normalized.contains("salt") -> listOf("salt")
            normalized.contains("azúcar") || normalized.contains("sugar") -> listOf("sugar")
            
            // Si no hay traducción, usar el original
            else -> listOf(normalized)
        }
    }
    
    /**
     * Search recipes based on available ingredients
     * Returns recipes grouped by missing ingredient count
     */
    suspend fun searchRecipes(availableIngredients: List<String>): Result<Map<Int, List<Recipe>>> {
        return try {
            val recipeMap = mutableMapOf<String, Recipe>()
            
            // Translate all ingredients to English
            val translatedIngredients = availableIngredients.flatMap { ingredient ->
                translateIngredient(ingredient)
            }.distinct()
            
            val availableSet = translatedIngredients.map { it.lowercase().trim() }.toSet()
            
            android.util.Log.d("RecipeRepository", "Searching with ingredients: $translatedIngredients")
            
            // Search recipes for each ingredient (increased to 10 for more results)
            translatedIngredients.take(10).forEach { ingredient ->
                try {
                    val response = api.searchByIngredient(ingredient)
                    android.util.Log.d("RecipeRepository", "Found ${response.meals?.size ?: 0} recipes for $ingredient")
                    
                    response.meals?.forEach { meal ->
                        if (!recipeMap.containsKey(meal.idMeal)) {
                            // Get full recipe details
                            val detailResponse = api.getRecipeDetails(meal.idMeal)
                            detailResponse.meals?.firstOrNull()?.let { detail ->
                                recipeMap[meal.idMeal] = detail.toRecipe(availableSet)
                            }
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("RecipeRepository", "Error searching for $ingredient", e)
                }
            }
            
            android.util.Log.d("RecipeRepository", "Total recipes found: ${recipeMap.size}")
            
            // Group recipes by missing ingredient count
            val grouped = recipeMap.values.groupBy { recipe ->
                recipe.ingredients.count { !it.available }
            }
            
            grouped.forEach { (missingCount, recipes) ->
                android.util.Log.d("RecipeRepository", "Recipes with $missingCount missing: ${recipes.size}")
            }
            
            Result.success(grouped)
        } catch (e: Exception) {
            android.util.Log.e("RecipeRepository", "Error searching recipes", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get complete recipes (all ingredients available)
     */
    fun getCompleteRecipes(recipeMap: Map<Int, List<Recipe>>): List<Recipe> {
        return recipeMap[0] ?: emptyList()
    }
    
    /**
     * Get almost complete recipes (missing 1-2 ingredients)
     */
    fun getAlmostCompleteRecipes(recipeMap: Map<Int, List<Recipe>>): List<Recipe> {
        val missing1 = recipeMap[1] ?: emptyList()
        val missing2 = recipeMap[2] ?: emptyList()
        return (missing1 + missing2).sortedBy { recipe ->
            recipe.ingredients.count { !it.available }
        }
    }
}
