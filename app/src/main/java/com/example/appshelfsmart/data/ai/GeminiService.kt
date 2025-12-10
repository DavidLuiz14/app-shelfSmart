package com.example.appshelfsmart.data.ai

import android.graphics.Bitmap
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiService(private val apiKey: String) {
    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-pro",
        apiKey = apiKey
    )

    suspend fun simplifyNutritionalInfo(extractedText: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val prompt = """
                    Analiza la siguiente información nutricional extraída de una etiqueta y proporciona un resumen simple y claro en español.
                    Incluye solo los datos más importantes como calorías, proteínas, carbohidratos, grasas, azúcares y sodio.
                    Si la información está incompleta, menciona solo lo que esté disponible.
                    Formato: "Por porción: X calorías, Xg proteínas, Xg carbohidratos, Xg grasas, Xg azúcares, Xmg sodio"
                    
                    Texto extraído:
                    $extractedText
                """.trimIndent()

                val response = generativeModel.generateContent(prompt)
                
                // Log success
                android.util.Log.d("GeminiService", "API call successful (text-only)")
                
                response.text ?: "No se pudo obtener información nutricional"
            } catch (e: Exception) {
                e.printStackTrace()
                
                // Log detailed error
                android.util.Log.e("GeminiService", "Full error: ${e.javaClass.simpleName}: ${e.message}", e)
                
                val errorMsg = when {
                    e.message?.contains("quota", ignoreCase = true) == true -> 
                        "QUOTA_ERROR: ${e.message}"
                    e.message?.contains("API key", ignoreCase = true) == true -> 
                        "API_KEY_ERROR: ${e.message}"
                    e.message?.contains("404", ignoreCase = true) == true -> 
                        "MODEL_NOT_FOUND: ${e.message}"
                    else -> 
                        "UNKNOWN_ERROR: ${e.message}"
                }
                
                "Error processing nutritional information: $errorMsg"
            }
        }
    }
}
