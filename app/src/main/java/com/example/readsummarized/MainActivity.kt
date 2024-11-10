package com.example.readsummarized

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.readsummarized.network.TextSummarizer
import com.example.readsummarized.ui.theme.ReadSummarizedTheme
import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import java.io.File

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

                    LaunchedEffect(Unit) {
                        summary = textSummarizer.summarizeText(inputText)
                    }
                }
            }
        }

// Initialize the launcher in onCreate or during initialization
        openFileLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val file = result.data?.data?.toFile(this)
                val text = file?.readText().orEmpty()
                Log.d("hihi", text)
                // Handle the URI as needed
            }
        }
        // Запрашиваем разрешения и извлекаем файлы, если разрешение предоставлено
        openFile()
    }

    fun Uri.toFile(context: Context): File? {
        val inputStream = context.contentResolver.openInputStream(this)
        val tempFile = File.createTempFile("temp", ".pdf")
        return try {
            tempFile.outputStream().use { fileOut ->
                inputStream?.copyTo(fileOut)
            }
            tempFile.deleteOnExit()
            inputStream?.close()
            tempFile
        } catch (e: Exception) {
            null
        }
    }

    private lateinit var openFileLauncher: ActivityResultLauncher<Intent>

    // Function to open the file
    fun openFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
            // Optionally, specify a URI for the file that should appear in the
            // system file picker when it loads.
            // putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri)
        }
        openFileLauncher.launch(intent)
    }
    @Preview(showBackground = true)
    @Composable
    fun DefaultPreview() {
        ReadSummarizedTheme {
            Text(text = "Пример резюме")
        }
    }
}