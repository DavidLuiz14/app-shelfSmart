package com.example.appshelfsmart.utils

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await
import java.io.IOException

object OCRUtils {

    suspend fun extractTextFromImages(context: Context, uris: List<Uri>): String {
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val stringBuilder = StringBuilder()

        for (uri in uris) {
            try {
                val image = InputImage.fromFilePath(context, uri)
                val result = recognizer.process(image).await()
                stringBuilder.append(result.text).append("\n\n")
            } catch (e: IOException) {
                e.printStackTrace()
                stringBuilder.append("[Error processing image: ${e.message}]\n")
            } catch (e: Exception) {
                e.printStackTrace()
                stringBuilder.append("[Error recognizing text: ${e.message}]\n")
            }
        }
        return stringBuilder.toString()
    }
}
