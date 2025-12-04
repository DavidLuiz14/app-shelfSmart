package com.example.appshelfsmart.data.ai

import android.graphics.Bitmap
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiService(private val apiKey: String) {
    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.0-flash",
        apiKey = apiKey
    )

    suspend fun simplifyNutritionalInfo(images: List<Bitmap>): String {
        return withContext(Dispatchers.IO) {
            try {
                val prompt = """
                    Analyze these images of a nutrition label. They might be different parts of the same label (e.g. left and right side of a cylindrical bottle).
                    Combine the information to extract the key nutritional information and explain it in simple, easy-to-understand terms for a general audience. 
                    Focus on what is good or bad about it (e.g., high sugar, good source of protein). 
                    Keep it brief (max 3 sentences).
                """.trimIndent()

                val inputContent = content {
                    images.forEach { image(it) }
                    text(prompt)
                }

                val response = generativeModel.generateContent(inputContent)
                response.text ?: "Could not simplify information."
            } catch (e: Exception) {
                e.printStackTrace()
                "Error processing nutritional information: ${e.message}"
            }
        }
    }
}
