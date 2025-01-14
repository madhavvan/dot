package com.example.Autply

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import org.json.JSONArray
import java.io.IOException
import okhttp3.MediaType.Companion.toMediaType




class HuggingFaceHandler {

    private val apiKey = BuildConfig.HUGGING_FACE_API_KEY
    private val client = OkHttpClient()


    fun generateReply(prompt: String, callback: (String) -> Unit) {
        Thread {
            try {
                val response = callHuggingFaceAPI(prompt)  // Pass enhancedPrompt
                callback(response)
            } catch (e: Exception) {
                Log.e("HuggingFaceHandler", "Error generating reply", e)
                callback("Error: ${e.message}")
            }
        }.start()
    }


    private fun callHuggingFaceAPI(prompt: String): String {
        val url = "https://api-inference.huggingface.co/models/EleutherAI/gpt-neo-2.7B"
        val json = JSONObject().apply {
            put("inputs", prompt)
            put("parameters", JSONObject().apply {
                put("max_length", 15)  // Adjust for longer responses
                put("temperature", 0.5)  // Adjust randomness
                put("top_p", 0.5)
            })
        }

        val body = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder()
            .url(url)
            .post(body)
            .addHeader("Authorization", "Bearer $apiKey")
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string()
                Log.d("API Response", "Status Code: ${response.code}, Body: $responseBody")

                if (response.isSuccessful && responseBody != null) {
                    val responseArray = JSONArray(responseBody)
                    if (responseArray.length() > 0) {
                        responseArray.getJSONObject(0).getString("generated_text").trim()
                    } else {
                        "Error: No text generated"
                    }
                } else {
                    "Error: ${response.code} - ${response.message}"
                }
            }
        } catch (e: IOException) {
            Log.e("HuggingFaceHandler", "Network Error", e)
            "Exception: ${e.message}"
        }
    }
}  // <-- Ensure this closing brace is present for the class
