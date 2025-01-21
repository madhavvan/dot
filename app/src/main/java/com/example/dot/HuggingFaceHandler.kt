package com.example.Autply

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import okhttp3.MediaType.Companion.toMediaType

class HuggingFaceHandler {

    private val apiKey = BuildConfig.HUGGING_FACE_API_KEY
    private val client = OkHttpClient()

    fun generateReply(prompt: String, callback: (String) -> Unit) {
        Thread {
            try {
                val response = callHuggingFaceAPI(prompt)
                callback(response)
            } catch (e: Exception) {
                Log.e("HuggingFaceHandler", "Error generating reply", e)
                callback("Error: ${e.message}")
            }
        }.start()
    }

    private fun callHuggingFaceAPI(prompt: String): String {
        val apiUrl = "https://api-inference.huggingface.co/models/EleutherAI/gpt-neo-2.7B"
        val json = JSONObject().apply {
            put("inputs", prompt)
            put("parameters", JSONObject().apply {
                put("max_length", 50)  // Allows longer generated replies
                put("temperature", 0.7)  // Controls randomness (higher = more random)
                put("top_p", 0.9)  // Probability for nucleus sampling
            })
        }

        val body = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url(apiUrl)
            .post(body)
            .addHeader("Authorization", "Bearer $apiKey")
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string()
                Log.d("HuggingFaceHandler", "Status Code: ${response.code}, Body: $responseBody")

                if (response.isSuccessful && responseBody != null) {
                    val jsonResponse = JSONObject(responseBody)
                    val generatedText = jsonResponse.optJSONArray("generated_texts")?.optString(0)
                    generatedText ?: "Error: No text generated"
                } else {
                    "Error: ${response.code} - ${response.message}"
                }
            }
        } catch (e: IOException) {
            Log.e("HuggingFaceHandler", "Network Error", e)
            "Error: ${e.message}"
        }
    }
}
