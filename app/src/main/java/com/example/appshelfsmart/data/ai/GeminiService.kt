package com.example.appshelfsmart.data.ai

import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiService(private val apiKey: String) {

    suspend fun simplifyNutritionalInfo(extractedText: String): String {
        return withContext(Dispatchers.IO) {
            // Sanitize API Key
            val cleanKey = apiKey.trim().replace("\"", "")
            
            if (cleanKey.isBlank()) {
                return@withContext "API Error: API Key is missing. Check local.properties."
            }

            // Try multiple variants based on user's available models
            val modelsToTry = listOf(
                "gemini-2.0-flash", // Confirmed available in user's list
                "gemini-flash-latest",
                "gemini-1.5-flash" 
            )
            
            var lastException: Exception? = null

            for (modelName in modelsToTry) {
                try {
                    android.util.Log.d("GeminiService", "Attempting with model: $modelName")
                    val generativeModel = GenerativeModel(
                        modelName = modelName,
                        apiKey = cleanKey
                    )

                    val prompt = """
                        Analiza la siguiente información nutricional extraída de una etiqueta y proporciona un resumen simple y claro en español.
                        Incluye solo los datos más importantes como calorías, proteínas, carbohidratos, grasas, azúcares y sodio.
                        Solo da un texto corto y preciso con palabras simples, que las entienda todo el mundo
                       
                        
                        Texto extraído:
                        $extractedText
                    """.trimIndent()

                    val response = generativeModel.generateContent(prompt)
                    
                    if (response.text != null) {
                        android.util.Log.d("GeminiService", "Success with model: $modelName")
                        return@withContext response.text!!
                    }
                } catch (e: Exception) {
                    lastException = e
                    android.util.Log.w("GeminiService", "Failed with model $modelName: ${e.message}")
                    // If permissions error, don't try other models
                    if (e.message?.contains("API key", ignoreCase = true) == true) break
                }
            }
            
            // Handle final error
            val e = lastException ?: Exception("Unknown error")
            val msg = e.message ?: e.toString()
            val maskedKey = if (cleanKey.length > 8) "${cleanKey.take(4)}...${cleanKey.takeLast(4)}" else "Invalid Key"

            return@withContext when {
                msg.contains("quota", ignoreCase = true) -> 
                    "QUOTA_ERROR: Cuota excedida. Revisa billing en Google Cloud."
                msg.contains("API key", ignoreCase = true) -> 
                    "API_KEY_ERROR: Clave inválida ($maskedKey). Verifica local.properties."
                msg.contains("404", ignoreCase = true) || msg.contains("NOT_FOUND", ignoreCase = true) -> 
                    "MODEL_ERROR: No se encontró ningún modelo Flash disponible. Asegúrate de que la API está habilitada en el proyecto correcto."
                else -> 
                    "ERROR TÉCNICO: $msg"
            }
        }
    }
}
