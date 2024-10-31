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

    private val client = OkHttpClient()
    private val apiToken = "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" // Замените на ваш токен Hugging Face

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ReadSummarizedTheme {
                // State для хранения резюме
                var summary by remember { mutableStateOf("Loading...") }

                // Пример текста для резюмирования
                val textToSummarize = "I live in a house near the mountains. I have two brothers and one sister, and I was born last. My father teaches mathematics, and my mother is a nurse at a big hospital. My brothers are very smart and work hard in school. My sister is a nervous girl, but she is very kind. My grandmother also lives with us. She came from Italy when I was two years old. She has grown old, but she is still very strong. She cooks the best food!"

                // Выполняем запрос в корутине
                LaunchedEffect(Unit) {
                    summary = summarizeText(textToSummarize)
                }

                // Создаем пользовательский интерфейс
                Scaffold { paddingValues ->
                    Text(
                        text = summary,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    )
                }
            }
        }
    }

    private suspend fun summarizeText(text: String): String {
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
                .addHeader("Authorization", "Bearer $apiToken")
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