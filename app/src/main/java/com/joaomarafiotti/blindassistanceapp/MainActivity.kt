package com.joaomarafiotti.blindassistanceapp

import android.net.Uri
import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.joaomarafiotti.blindassistanceapp.ui.theme.BlindAssistanceAppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.Locale

class MainActivity : ComponentActivity(), TextToSpeech.OnInitListener {

    private var textToSpeech: TextToSpeech? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        textToSpeech = TextToSpeech(this, this)
        enableEdgeToEdge()
        setContent {
            BlindAssistanceAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    BlindAssistanceHomeScreen(
                        modifier = Modifier.padding(innerPadding),
                        onSpeakResult = { result -> speakText(result) }
                    )
                }
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            textToSpeech?.language = Locale("pt", "BR")
        }
    }

    private fun speakText(text: String) {
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    override fun onDestroy() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        super.onDestroy()
    }
}

@Composable
fun BlindAssistanceHomeScreen(
    modifier: Modifier = Modifier,
    onSpeakResult: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedImageName by remember { mutableStateOf("Nenhuma imagem selecionada") }
    var detectionResult by remember { mutableStateOf("Nenhum resultado ainda.") }
    var isLoading by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            selectedImageName = uri.lastPathSegment ?: "Imagem selecionada"
            detectionResult = "Imagem pronta para envio ao backend."
        } else {
            selectedImageUri = null
            selectedImageName = "Nenhuma imagem selecionada"
            detectionResult = "Nenhum resultado ainda."
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Blind Assistance App",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Aplicativo cliente Android da iniciação científica para reconhecimento de objetos em ambiente educacional.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Escolher imagem")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                if (selectedImageUri == null) {
                    detectionResult = "Selecione uma imagem primeiro."
                    onSpeakResult(detectionResult)
                    return@Button
                }

                isLoading = true
                detectionResult = "Enviando imagem para o backend..."

                scope.launch {
                    val rawResult = sendImageToBackend(context, selectedImageUri!!)
                    val formattedResult = formatDetectionResult(rawResult)
                    detectionResult = formattedResult
                    isLoading = false
                    onSpeakResult(formattedResult)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = selectedImageUri != null && !isLoading,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        ) {
            Text(
                if (isLoading) "Enviando..." else "Enviar imagem"
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        SectionCard(title = "Imagem selecionada") {
            Text(
                text = selectedImageName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )

            if (selectedImageUri != null) {
                Spacer(modifier = Modifier.height(12.dp))

                Image(
                    painter = rememberAsyncImagePainter(selectedImageUri),
                    contentDescription = "Imagem selecionada",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(16.dp))
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        SectionCard(title = "Resultado") {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = detectionResult,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedImageUri != null && detectionResult != "Nenhum resultado ainda.") {
            Button(
                onClick = { onSpeakResult(detectionResult) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Ouvir resultado novamente")
            }
        }
    }
}

@Composable
fun SectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            content()
        }
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