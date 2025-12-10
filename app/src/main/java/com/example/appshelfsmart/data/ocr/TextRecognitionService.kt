package com.example.appshelfsmart.data.ocr

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await

class TextRecognitionService {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    /**
     * Extract text from multiple images using ML Kit Text Recognition v2
     * Works offline and locally on device
     */
    suspend fun extractTextFromImages(images: List<Bitmap>): Result<String> {
        return try {
            val allText = StringBuilder()
            
            images.forEach { bitmap ->
                val inputImage = InputImage.fromBitmap(bitmap, 0)
                val result = recognizer.process(inputImage).await()
                
                // Append recognized text
                if (result.text.isNotBlank()) {
                    allText.append(result.text)
                    allText.append("\n\n")
                }
            }
            
            val extractedText = allText.toString().trim()
            
            if (extractedText.isBlank()) {
                Result.failure(Exception("No se pudo extraer texto de las imágenes. Asegúrate de que la etiqueta sea legible."))
            } else {
                android.util.Log.d("TextRecognitionService", "Extracted text: $extractedText")
                Result.success(extractedText)
            }
        } catch (e: Exception) {
            android.util.Log.e("TextRecognitionService", "Error extracting text", e)
            Result.failure(Exception("Error al procesar las imágenes: ${e.message}"))
        }
    }
    
    /**
     * Clean up resources
     */
    fun close() {
        recognizer.close()
    }
}
