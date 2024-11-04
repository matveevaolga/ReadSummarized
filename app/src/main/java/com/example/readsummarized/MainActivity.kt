package com.example.readsummarized

import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
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
import com.example.readsummarized.network.TextSummarizer
import com.example.readsummarized.ui.theme.ReadSummarizedTheme


class MainActivity : ComponentActivity() {
    private val textSummarizer = TextSummarizer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fetchPdfFiles()

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
    }

    //Method for getting all pdf files from device for all android versions
    protected fun pdfFiles(): ArrayList<String> {
        val pdfList = ArrayList<String>()

        val projection = arrayOf(
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.DATE_ADDED,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.MIME_TYPE,
        )

        val sortOrder = MediaStore.Files.FileColumns.DATE_ADDED + " DESC"

        val selection = MediaStore.Files.FileColumns.MIME_TYPE + " = ?"

        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension("pdf")
        val selectionArgs = arrayOf(mimeType)

        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Files.getContentUri("external")
        }


        contentResolver.query(collection, projection, selection, selectionArgs, sortOrder)
            .use { cursor ->
                checkNotNull(cursor)
                if (cursor.moveToFirst()) {
                    val columnData = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA)
                    val columnName =
                        cursor.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME)
                    do {
                        pdfList.add((cursor.getString(columnData)))
                        Log.d("PDF", "getPdf: " + cursor.getString(columnData))
                        //you can get your pdf files
                    } while (cursor.moveToNext())
                }
            }
        return pdfList
    }

    private fun fetchPdfFiles() {
        val pdfFiles = pdfFiles()
        Log.d("hihi", pdfFiles.toArray().contentToString())
        for (path in pdfFiles) {
            Log.d("PDF File Path", path)
        }
    }


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ReadSummarizedTheme {
        Text(text = "Пример резюме")
    }
}
}