package com.joaomarafiotti.blindassistanceapp

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.MediaStore
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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BlindAssistanceHomeScreen(
    modifier: Modifier = Modifier,
    onSpeakResult: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val yolo26nFloat32Detector = remember {
        YoloTfliteDetector(
            context = context.applicationContext,
            modelConfig = TfliteModelConfigs.YOLO26N_FLOAT32
        )
    }

    val yolo26nFloat16Detector = remember {
        YoloTfliteDetector(
            context = context.applicationContext,
            modelConfig = TfliteModelConfigs.YOLO26N_FLOAT16
        )
    }

    val yolov8nFloat32Detector = remember {
        YoloTfliteDetector(
            context = context.applicationContext,
            modelConfig = TfliteModelConfigs.YOLOV8N_FLOAT32
        )
    }

    var selectedLocalModelConfig by remember {
        mutableStateOf(TfliteModelConfigs.YOLO26N_FLOAT32)
    }

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedImageName by remember { mutableStateOf("Nenhuma imagem selecionada") }
    var detectionResult by remember { mutableStateOf("Nenhum resultado ainda.") }
    var spokenResult by remember { mutableStateOf("Nenhum resultado ainda.") }
    var detectedObjects by remember { mutableStateOf(listOf<String>()) }
    var isLoading by remember { mutableStateOf(false) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }
    var isLiveCameraMode by remember { mutableStateOf(false) }

    fun detectorFor(modelConfig: TfliteModelConfig): YoloTfliteDetector {
        return when (modelConfig.assetName) {
            TfliteModelConfigs.YOLO26N_FLOAT16.assetName -> yolo26nFloat16Detector
            TfliteModelConfigs.YOLOV8N_FLOAT32.assetName -> yolov8nFloat32Detector
            else -> yolo26nFloat32Detector
        }
    }

    fun resetImage(uri: Uri, label: String) {
        selectedImageUri = uri
        selectedImageName = label
        detectionResult = "Imagem pronta para análise."
        spokenResult = "Imagem pronta para análise."
        detectedObjects = emptyList()
    }

    fun clearImage() {
        selectedImageUri = null
        selectedImageName = "Nenhuma imagem selecionada"
        detectionResult = "Nenhum resultado ainda."
        spokenResult = "Nenhum resultado ainda."
        detectedObjects = emptyList()
    }

    fun updateResultAndSpeak(
        visualMessage: String,
        spokenMessage: String,
        objects: List<String> = emptyList()
    ) {
        detectionResult = visualMessage
        spokenResult = spokenMessage
        detectedObjects = objects
        onSpeakResult(spokenMessage)
    }

    fun analyzeImageWithBackend(uri: Uri) {
        isLoading = true
        detectionResult = "Enviando imagem para o backend..."
        spokenResult = "Enviando imagem para o backend."
        detectedObjects = emptyList()

        scope.launch {
            val rawResult = sendImageToBackend(context, uri)
            val formattedResult = formatBackendDetectionResult(rawResult)
            val ttsResult = formatBackendTtsResult(rawResult)
            val objects = extractDetectedObjects(rawResult)

            detectedObjects = objects
            detectionResult = formattedResult
            spokenResult = ttsResult
            isLoading = false

            onSpeakResult(ttsResult)
        }
    }

    fun analyzeImageOnDevice(
        uri: Uri,
        modelConfig: TfliteModelConfig = selectedLocalModelConfig
    ) {
        val detector = detectorFor(modelConfig)

        isLoading = true
        detectionResult = "Executando inferência local no dispositivo com ${modelConfig.displayName}..."
        spokenResult = "Analisando imagem."
        detectedObjects = emptyList()

        scope.launch {
            val localResult = withContext(Dispatchers.Default) {
                detector.runOnImageUri(uri)
            }

            val formattedResult = formatLocalDetectionResult(localResult)
            val ttsResult = formatLocalTtsResult(localResult)
            val feedbackType = getLocalFeedbackType(localResult)
            val objects = localResult.detections
                .map { translateClassName(it.className) }
                .distinct()

            triggerHapticFeedback(
                context = context.applicationContext,
                feedbackType = feedbackType
            )

            detectedObjects = objects
            detectionResult = formattedResult
            spokenResult = ttsResult
            isLoading = false

            onSpeakResult(ttsResult)
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            resetImage(
                uri = uri,
                label = uri.lastPathSegment ?: "Imagem selecionada"
            )
        } else {
            clearImage()
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val uri = pendingCameraUri

        if (success && uri != null) {
            resetImage(
                uri = uri,
                label = "Foto capturada pela câmera"
            )

            analyzeImageOnDevice(
                uri = uri,
                modelConfig = selectedLocalModelConfig
            )
        } else {
            updateResultAndSpeak(
                visualMessage = "Captura cancelada ou não concluída.",
                spokenMessage = "Captura cancelada."
            )
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
            fontWeight = FontWeight.Bold,
            modifier = Modifier.semantics {
                heading()
                contentDescription = "Blind Assistance App."
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Reconhecimento de objetos com resposta por voz e vibração.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
            modifier = Modifier.semantics {
                contentDescription =
                    "Aplicativo de reconhecimento de objetos com resposta por voz e vibração."
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        SectionCard(title = "Fluxo principal") {
            Text(
                text = "Use este modo para capturar uma foto e receber o resultado por voz, texto e vibração.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                modifier = Modifier.semantics {
                    contentDescription =
                        "Fluxo principal. Capture uma foto e receba o resultado por voz, texto e vibração."
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Modelo local atual: ${selectedLocalModelConfig.displayName}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.semantics {
                    contentDescription =
                        "Modelo local atual: ${selectedLocalModelConfig.displayName}."
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    val uri = createImageUri(context)

                    if (uri != null) {
                        pendingCameraUri = uri
                        cameraLauncher.launch(uri)
                    } else {
                        updateResultAndSpeak(
                            visualMessage = "Erro ao preparar captura da foto.",
                            spokenMessage = "Erro ao preparar a câmera."
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        contentDescription =
                            "Tirar foto e analisar objeto no dispositivo com o modelo ${selectedLocalModelConfig.displayName}."
                    },
                enabled = !isLoading,
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(if (isLoading) "Analisando..." else "Tirar foto e ouvir resultado")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                isLiveCameraMode = true
                onSpeakResult("Modo de detecção contínua iniciado.")
            },
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription =
                        "Iniciar modo de detecção contínua com câmera aberta."
                },
            enabled = !isLoading,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("Iniciar detecção contínua")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLiveCameraMode) {
            SectionCard(title = "Detecção contínua") {
                Text(
                    text = "Preview da câmera ativo. Nesta etapa, o app ainda não executa inferência contínua; o objetivo é validar a abertura da câmera dentro do aplicativo.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    modifier = Modifier.semantics {
                        contentDescription =
                            "Preview da câmera ativo. Esta etapa valida apenas a abertura da câmera dentro do aplicativo."
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                CameraPreviewScreen(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(420.dp),
                    onLiveDetectionMessage = { message ->
                        onSpeakResult(message)
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        isLiveCameraMode = false
                        onSpeakResult("Modo de detecção contínua encerrado.")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics {
                            contentDescription =
                                "Parar modo de detecção contínua."
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text("Parar detecção contínua")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        SectionCard(title = "Resultado") {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        liveRegion = LiveRegionMode.Polite
                        contentDescription = "Resultado da análise: $spokenResult"
                    },
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = detectionResult,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (detectedObjects.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            detectedObjects.forEach { obj ->
                                AssistChip(
                                    onClick = { },
                                    label = { Text(obj) },
                                    colors = AssistChipDefaults.assistChipColors(),
                                    modifier = Modifier.semantics {
                                        contentDescription = "Objeto detectado: $obj."
                                    }
                                )
                            }
                        }
                    }
                }
            }

            if (selectedImageUri != null && detectionResult != "Nenhum resultado ainda.") {
                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { onSpeakResult(spokenResult) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics {
                            contentDescription = "Ouvir novamente o resultado da análise."
                        },
                    enabled = !isLoading,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Ouvir resultado novamente")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        SectionCard(title = "Imagem atual") {
            Text(
                text = selectedImageName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                modifier = Modifier.semantics {
                    contentDescription = "Imagem atual: $selectedImageName."
                }
            )

            if (selectedImageUri != null) {
                Spacer(modifier = Modifier.height(12.dp))

                Image(
                    painter = rememberAsyncImagePainter(selectedImageUri),
                    contentDescription = "Imagem selecionada ou capturada para análise de objetos.",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(16.dp))
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        SectionCard(title = "Modo de teste e comparação") {
            Text(
                text = "Ferramentas para avaliar imagens selecionadas, comparar modelos on-device e testar o backend.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                modifier = Modifier.semantics {
                    contentDescription =
                        "Modo de teste e comparação. Ferramentas para avaliar imagens selecionadas, comparar modelos on-device e testar o backend."
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Modelo local selecionado: ${selectedLocalModelConfig.displayName}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.semantics {
                    contentDescription =
                        "Modelo local selecionado: ${selectedLocalModelConfig.displayName}."
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    selectedLocalModelConfig = TfliteModelConfigs.YOLO26N_FLOAT32
                    updateResultAndSpeak(
                        visualMessage = "Modelo local selecionado: ${TfliteModelConfigs.YOLO26N_FLOAT32.displayName}.",
                        spokenMessage = "Modelo YOLO26n selecionado."
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        contentDescription =
                            "Selecionar modelo YOLO26n Float32 para inferência local."
                    },
                enabled = !isLoading,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedLocalModelConfig.assetName == TfliteModelConfigs.YOLO26N_FLOAT32.assetName) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.secondary
                    }
                )
            ) {
                Text("Usar YOLO26n Float32")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    selectedLocalModelConfig = TfliteModelConfigs.YOLO26N_FLOAT16
                    updateResultAndSpeak(
                        visualMessage = "Modelo local selecionado: ${TfliteModelConfigs.YOLO26N_FLOAT16.displayName}.",
                        spokenMessage = "Modelo YOLO26n Float16 selecionado."
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        contentDescription =
                            "Selecionar modelo YOLO26n Float16 para inferência local."
                    },
                enabled = !isLoading,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedLocalModelConfig.assetName == TfliteModelConfigs.YOLO26N_FLOAT16.assetName) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.secondary
                    }
                )
            ) {
                Text("Usar YOLO26n Float16")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    selectedLocalModelConfig = TfliteModelConfigs.YOLOV8N_FLOAT32
                    updateResultAndSpeak(
                        visualMessage = "Modelo local selecionado: ${TfliteModelConfigs.YOLOV8N_FLOAT32.displayName}.",
                        spokenMessage = "Modelo YOLOv8n selecionado."
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        contentDescription =
                            "Selecionar modelo YOLOv8n Float32 para inferência local."
                    },
                enabled = !isLoading,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedLocalModelConfig.assetName == TfliteModelConfigs.YOLOV8N_FLOAT32.assetName) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.secondary
                    }
                )
            ) {
                Text("Usar YOLOv8n Float32")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        contentDescription =
                            "Selecionar imagem da galeria para teste de reconhecimento de objetos."
                    },
                enabled = !isLoading,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Text("Selecionar imagem para teste")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    val uri = selectedImageUri

                    if (uri == null) {
                        updateResultAndSpeak(
                            visualMessage = "Selecione ou capture uma imagem primeiro.",
                            spokenMessage = "Selecione ou capture uma imagem primeiro."
                        )
                        return@Button
                    }

                    analyzeImageOnDevice(
                        uri = uri,
                        modelConfig = selectedLocalModelConfig
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        contentDescription =
                            "Analisar imagem selecionada usando o modelo local ${selectedLocalModelConfig.displayName}."
                    },
                enabled = selectedImageUri != null && !isLoading,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text(
                    if (isLoading) {
                        "Analisando..."
                    } else {
                        "Analisar com ${selectedLocalModelConfig.displayName}"
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    val uri = selectedImageUri

                    if (uri == null) {
                        updateResultAndSpeak(
                            visualMessage = "Selecione uma imagem primeiro.",
                            spokenMessage = "Selecione uma imagem primeiro."
                        )
                        return@Button
                    }

                    analyzeImageWithBackend(uri)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        contentDescription =
                            "Analisar imagem selecionada usando o backend. Modo de teste e comparação."
                    },
                enabled = selectedImageUri != null && !isLoading,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text(if (isLoading) "Analisando..." else "Analisar via backend")
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
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.semantics {
                    heading()
                    contentDescription = title
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            content()
        }
    }
}

fun createImageUri(context: android.content.Context): Uri? {
    val contentValues = ContentValues().apply {
        put(
            MediaStore.Images.Media.DISPLAY_NAME,
            "blind_assistance_${System.currentTimeMillis()}.jpg"
        )
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
    }

    return context.contentResolver.insert(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        contentValues
    )
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

fun translateClassName(className: String): String {
    return when (className.lowercase()) {
        "person" -> "pessoa"
        "handbag" -> "bolsa"
        "backpack" -> "mochila"
        "chair" -> "cadeira"
        "book" -> "livro"
        "laptop" -> "notebook"
        "clock" -> "relógio"
        "table" -> "mesa"
        "pen" -> "caneta"
        "scissor", "scissors" -> "tesoura"
        "trash-can", "trash can" -> "lixeira"
        "whiteboard" -> "quadro branco"
        "bookshelf" -> "estante"
        "remote-control", "remote control", "remote" -> "controle remoto"
        "ruler" -> "régua"
        "eraser" -> "borracha"
        "sharpener" -> "apontador"
        "fan" -> "ventilador"
        "bag" -> "bolsa"
        "pants" -> "calça"
        "shoes" -> "sapato"
        "hat" -> "chapéu"
        "wall-magazine" -> "mural"
        else -> className
    }
}

fun extractDetectedObjects(rawResponse: String): List<String> {
    if (rawResponse.startsWith("Erro")) {
        return emptyList()
    }

    val regex = Regex("\"class_name\":\"(.*?)\"")
    return regex.findAll(rawResponse)
        .map { it.groupValues[1] }
        .map { translateClassName(it) }
        .distinct()
        .toList()
}

fun formatBackendDetectionResult(rawResponse: String): String {
    if (rawResponse.startsWith("Erro")) {
        return rawResponse
    }

    val regex = Regex("\"class_name\":\"(.*?)\"")
    val matches = regex.findAll(rawResponse).map { it.groupValues[1] }.toList()

    return if (matches.isEmpty()) {
        "Nenhum objeto detectado pelo backend."
    } else {
        val translated = matches
            .map { translateClassName(it) }
            .distinct()

        "Objetos detectados pelo backend: " + translated.joinToString(", ")
    }
}

fun formatBackendTtsResult(rawResponse: String): String {
    if (rawResponse.startsWith("Erro")) {
        return rawResponse
    }

    val objects = extractDetectedObjects(rawResponse)

    if (objects.isEmpty()) {
        return "Nenhum objeto detectado pelo backend."
    }

    return if (objects.size == 1) {
        "Detectado pelo backend: ${objects.first()}."
    } else {
        "Backend detectou: ${objects.joinToString(", ")}."
    }
}

fun formatLocalDetectionResult(result: LocalDetectionResult): String {
    if (result.detections.isEmpty()) {
        return "Modelo: ${result.modelDisplayName}. " +
                "Nenhum objeto reconhecido com segurança. " +
                "Tente aproximar o objeto, melhorar a iluminação ou tirar outra foto. " +
                "Tempo aproximado: ${result.inferenceMs} ms."
    }

    val highConfidenceDetections = result.detections.filter { it.confidence >= 0.70f }
    val mediumConfidenceDetections = result.detections.filter { it.confidence in 0.40f..<0.70f }

    fun formatConfidence(detection: LocalDetection): String {
        val translatedName = translateClassName(detection.className)
        val percentage = (detection.confidence * 100).toInt()
        return "$translatedName ${percentage}%"
    }

    return when {
        highConfidenceDetections.isNotEmpty() -> {
            val detectedNames = highConfidenceDetections
                .map { translateClassName(it.className) }
                .distinct()

            val mainConfidences = highConfidenceDetections
                .take(3)
                .joinToString(", ") { formatConfidence(it) }

            val possiblePart = if (mediumConfidenceDetections.isNotEmpty()) {
                val possibleNames = mediumConfidenceDetections
                    .map { translateClassName(it.className) }
                    .distinct()
                    .joinToString(", ")

                " Possíveis objetos com menor confiança: $possibleNames."
            } else {
                ""
            }

            "Modelo: ${result.modelDisplayName}. " +
                    "Detectei on-device: ${detectedNames.joinToString(", ")}. " +
                    "Confianças principais: $mainConfidences." +
                    possiblePart +
                    " Tempo aproximado: ${result.inferenceMs} ms."
        }

        mediumConfidenceDetections.isNotEmpty() -> {
            val possibleNames = mediumConfidenceDetections
                .map { translateClassName(it.className) }
                .distinct()

            val mainConfidences = mediumConfidenceDetections
                .take(3)
                .joinToString(", ") { formatConfidence(it) }

            "Modelo: ${result.modelDisplayName}. " +
                    "Possível detecção on-device: ${possibleNames.joinToString(", ")}. " +
                    "Confiança moderada: $mainConfidences. " +
                    "Tente confirmar com outra foto. " +
                    "Tempo aproximado: ${result.inferenceMs} ms."
        }

        else -> {
            "Modelo: ${result.modelDisplayName}. " +
                    "Nenhum objeto reconhecido com segurança. " +
                    "Tempo aproximado: ${result.inferenceMs} ms."
        }
    }
}

fun formatLocalTtsResult(result: LocalDetectionResult): String {
    if (result.detections.isEmpty()) {
        return "Não consegui reconhecer com segurança."
    }

    val highConfidenceDetections = result.detections.filter { it.confidence >= 0.70f }
    val mediumConfidenceDetections = result.detections.filter { it.confidence in 0.40f..<0.70f }

    return when {
        highConfidenceDetections.isNotEmpty() -> {
            val objectNames = highConfidenceDetections
                .map { translateClassName(it.className) }
                .distinct()

            if (objectNames.size == 1) {
                "Detectado: ${objectNames.first()}."
            } else {
                "Detectei: ${objectNames.joinToString(", ")}."
            }
        }

        mediumConfidenceDetections.isNotEmpty() -> {
            val objectNames = mediumConfidenceDetections
                .map { translateClassName(it.className) }
                .distinct()

            if (objectNames.size == 1) {
                "Possível: ${objectNames.first()}."
            } else {
                "Possíveis objetos: ${objectNames.joinToString(", ")}."
            }
        }

        else -> {
            "Não consegui reconhecer com segurança."
        }
    }
}

enum class DetectionFeedbackType {
    HIGH_CONFIDENCE,
    MEDIUM_CONFIDENCE,
    NO_SAFE_DETECTION
}

fun getLocalFeedbackType(result: LocalDetectionResult): DetectionFeedbackType {
    if (result.detections.isEmpty()) {
        return DetectionFeedbackType.NO_SAFE_DETECTION
    }

    val hasHighConfidence = result.detections.any { it.confidence >= 0.70f }

    if (hasHighConfidence) {
        return DetectionFeedbackType.HIGH_CONFIDENCE
    }

    val hasMediumConfidence = result.detections.any { it.confidence in 0.40f..<0.70f }

    if (hasMediumConfidence) {
        return DetectionFeedbackType.MEDIUM_CONFIDENCE
    }

    return DetectionFeedbackType.NO_SAFE_DETECTION
}

fun triggerHapticFeedback(
    context: Context,
    feedbackType: DetectionFeedbackType
) {
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager =
            context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    if (!vibrator.hasVibrator()) {
        return
    }

    val pattern = when (feedbackType) {
        DetectionFeedbackType.HIGH_CONFIDENCE -> longArrayOf(0, 90)
        DetectionFeedbackType.MEDIUM_CONFIDENCE -> longArrayOf(0, 70, 90, 70)
        DetectionFeedbackType.NO_SAFE_DETECTION -> longArrayOf(0, 180)
    }

    val amplitudes = when (feedbackType) {
        DetectionFeedbackType.HIGH_CONFIDENCE -> intArrayOf(0, 180)
        DetectionFeedbackType.MEDIUM_CONFIDENCE -> intArrayOf(0, 150, 0, 150)
        DetectionFeedbackType.NO_SAFE_DETECTION -> intArrayOf(0, 90)
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val effect = VibrationEffect.createWaveform(pattern, amplitudes, -1)
        vibrator.vibrate(effect)
    } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(pattern, -1)
    }
}