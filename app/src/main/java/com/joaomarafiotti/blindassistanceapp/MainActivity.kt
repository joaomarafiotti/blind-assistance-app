package com.joaomarafiotti.blindassistanceapp

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.joaomarafiotti.blindassistanceapp.ui.theme.BlindAssistanceAppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.regex.Pattern

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BlindAssistanceAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    BlindAssistanceHomeScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun BlindAssistanceHomeScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedImageName by remember { mutableStateOf("Nenhuma imagem selecionada") }
    var detectionResult by remember { mutableStateOf("Nenhum resultado ainda.") }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            selectedImageName = uri.toString()
            detectionResult = "Imagem pronta para envio ao backend."
        } else {
            selectedImageUri = null
            selectedImageName = "Nenhuma imagem selecionada"
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Blind Assistance App",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Aplicativo cliente Android da iniciação científica para reconhecimento de objetos em ambiente educacional.",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Escolher imagem")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                if (selectedImageUri == null) {
                    detectionResult = "Selecione uma imagem primeiro."
                    return@Button
                }

                detectionResult = "Enviando imagem para o backend..."

                scope.launch {
                    val rawResult = sendImageToBackend(context, selectedImageUri!!)
                    detectionResult = formatDetectionResult(rawResult)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Enviar imagem")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Imagem selecionada:",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = selectedImageName,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Resultado:",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = detectionResult,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

suspend fun sendImageToBackend(context: android.content.Context, imageUri: Uri): String {
    return withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
                ?: return@withContext "Erro: não foi possível abrir a imagem."

            val imageBytes = inputStream.readBytes()
            inputStream.close()

            val requestBody = imageBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())

            val multipartBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", "selected_image.jpg", requestBody)
                .build()

            val request = Request.Builder()
                .url("http://10.0.2.2:8000/detect")
                .post(multipartBody)
                .build()

            val client = OkHttpClient()
            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                response.body?.string() ?: "Resposta vazia do backend."
            } else {
                "Erro do backend: ${response.code}"
            }
        } catch (e: Exception) {
            "Erro ao enviar imagem: ${e.message}"
        }
    }
}

fun formatDetectionResult(rawResponse: String): String {
    val regex = Regex("\"class_name\":\"(.*?)\"")
    val matches = regex.findAll(rawResponse).map { it.groupValues[1] }.toList()

    return if (matches.isEmpty()) {
        "Nenhum objeto detectado."
    } else {
        "Objetos detectados: " + matches.distinct().joinToString(", ")
    }
}
