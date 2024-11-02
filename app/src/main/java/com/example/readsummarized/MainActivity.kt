package com.example.readsummarized

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import com.example.readsummarized.ui.theme.ReadSummarizedTheme
import okhttp3.RequestBody.Companion.toRequestBody
import com.google.gson.JsonParser

class MainActivity : ComponentActivity() {

    private val textSummarizer = TextSummarizer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ReadSummarizedTheme {
                var summary by remember { mutableStateOf("Здесь будет резюме.") }
                var inputText by remember { mutableStateOf("Введите текст для резюмирования.") }

                Scaffold { paddingValues ->
                    Text(
                        text = summary,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    )

                    // Simulate a summarization process
                    LaunchedEffect(Unit) {
                        summary = textSummarizer.summarizeText(inputText)
                    }
                }
            }
        }
    }

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
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ReadSummarizedTheme {
        Text(text = "Пример резюме")
    }
}

