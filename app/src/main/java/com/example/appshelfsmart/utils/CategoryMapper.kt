package com.example.appshelfsmart.utils

object CategoryMapper {
    private val categoryKeywords = mapOf(
        "Lácteos y derivados" to listOf(
            "milk", "dairy", "cheese", "yogurt", "yoghurt", "butter", "cream",
            "leche", "lácteo", "queso", "yogur", "mantequilla", "crema",
            "lactose", "lacteos", "dairies"
        ),
        "Carnes y pescados" to listOf(
            "meat", "beef", "pork", "chicken", "fish", "seafood", "poultry",
            "carne", "res", "cerdo", "pollo", "pescado", "mariscos", "aves",
            "turkey", "pavo", "salmon", "tuna", "atún"
        ),
        "Frutas" to listOf(
            "fruit", "apple", "banana", "orange", "grape", "strawberry",
            "fruta", "manzana", "plátano", "naranja", "uva", "fresa",
            "berry", "melon", "watermelon", "pineapple", "piña"
        ),
        "Verduras" to listOf(
            "vegetable", "lettuce", "tomato", "carrot", "onion", "potato",
            "verdura", "lechuga", "tomate", "zanahoria", "cebolla", "papa",
            "broccoli", "spinach", "espinaca", "brocoli"
        ),
        "Granos y cereales" to listOf(
            "grain", "cereal", "bread", "rice", "pasta", "wheat", "oat",
            "grano", "cereal", "pan", "arroz", "pasta", "trigo", "avena",
            "corn", "maíz", "flour", "harina"
        ),
        "Bebidas" to listOf(
            "beverage", "drink", "juice", "soda", "water", "tea", "coffee",
            "bebida", "jugo", "refresco", "agua", "té", "café",
            "beer", "wine", "cerveza", "vino", "cola"
        ),
        "Condimentos y especias" to listOf(
            "condiment", "spice", "salt", "pepper", "sauce", "seasoning",
            "condimento", "especia", "sal", "pimienta", "salsa", "sazonador",
            "ketchup", "mustard", "mostaza", "mayonnaise", "mayonesa"
        ),
        "Snacks y dulces" to listOf(
            "snack", "candy", "chocolate", "cookie", "chip", "sweet", "dessert",
            "snack", "dulce", "chocolate", "galleta", "papa", "postre",
            "cake", "pastel", "ice cream", "helado", "gum", "chicle"
        ),
        "Productos de limpieza" to listOf(
            "cleaning", "detergent", "soap", "bleach", "disinfectant",
            "limpieza", "detergente", "jabón", "cloro", "desinfectante",
            "cleaner", "limpiador"
        )
    )

    /**
     * Maps a category from OpenFoodFacts API to app-specific categories
     * @param apiCategory Category string from the API (can be comma-separated)
     * @param productName Product name to help with categorization
     * @return Mapped category or "Otros" if no match found
     */
    fun mapCategory(apiCategory: String?, productName: String?): String {
        val searchText = buildString {
            if (!apiCategory.isNullOrBlank()) append(apiCategory.lowercase())
            append(" ")
            if (!productName.isNullOrBlank()) append(productName.lowercase())
        }

        // Find the best matching category
        for ((category, keywords) in categoryKeywords) {
            if (keywords.any { keyword -> searchText.contains(keyword) }) {
                return category
            }
        }

        return "Otros"
    }
}
