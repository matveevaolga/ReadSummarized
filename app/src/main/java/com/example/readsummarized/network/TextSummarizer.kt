package com.example.readsummarized.network

import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class TextSummarizer {
    private val client = OkHttpClient()
    private val summarizeApiToken = "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"

    suspend fun summarizeText(text: String): String {
        return withContext(Dispatchers.IO) {
            val url = "https://api-inference.huggingface.co/models/facebook/bart-large-cnn"
            val json = JSONObject().apply {
                put("inputs", text)
                put("parameters", JSONObject().apply {
                    put("truncation", "only_first")
                })
            }

            val requestBody = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $summarizeApiToken")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val jsonObject = JsonParser.parseString(response.body!!.string()).asJsonArray[0].asJsonObject
                jsonObject["summary_text"].asString
            } else {
                "Request failed: ${response.code} ${response.message}"
            }
        }
    }
}