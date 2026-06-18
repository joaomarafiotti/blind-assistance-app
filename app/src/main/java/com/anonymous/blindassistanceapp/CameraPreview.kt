package com.anonymous.blindassistanceapp

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.roundToInt

private const val LIVE_ANALYSIS_INTERVAL_MS = 3500L
private const val LIVE_MIN_TIME_BETWEEN_SPEECH_MS = 4500L
private const val LIVE_SAME_OBJECT_COOLDOWN_MS = 10000L

private const val HIGH_CONFIDENCE_THRESHOLD = 0.80f
private const val MEDIUM_CONFIDENCE_THRESHOLD = 0.50f
private const val LOW_CONFIDENCE_THRESHOLD = 0.30f

@Composable
fun CameraPreviewScreen(
    modifier: Modifier = Modifier,
    onLiveDetectionMessage: (String) -> Unit = {}
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(hasCameraPermission(context))
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    if (hasCameraPermission) {
        CameraPreviewContent(
            modifier = modifier,
            onLiveDetectionMessage = onLiveDetectionMessage
        )
    } else {
        Box(modifier = modifier.fillMaxSize()) {
            Text(
                text = "Permissão de câmera necessária para iniciar a detecção contínua.",
                modifier = Modifier.semantics {
                    contentDescription =
                        "Permissão de câmera necessária para iniciar a detecção contínua."
                }
            )
        }
    }
}

@Composable
private fun CameraPreviewContent(
    modifier: Modifier = Modifier,
    onLiveDetectionMessage: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnLiveDetectionMessage by rememberUpdatedState(onLiveDetectionMessage)

    val detector = remember(context) {
        YoloTfliteDetector(context.applicationContext)
    }

    var analysisStatusText by remember {
        mutableStateOf("Câmera ativa. Aponte para um objeto.")
    }

    val cameraExecutor = remember {
        Executors.newSingleThreadExecutor()
    }

    val mainExecutor = remember(context) {
        ContextCompat.getMainExecutor(context)
    }

    val analyzedFrameCount = remember {
        AtomicInteger(0)
    }

    val lastAnalysisTimestamp = remember {
        AtomicLong(0L)
    }

    val lastSpeechTimestamp = remember {
        AtomicLong(0L)
    }

    val lastSpokenObject = remember {
        AtomicReference<String?>(null)
    }

    val previewView = remember {
        PreviewView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    DisposableEffect(lifecycleOwner) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        val listener = Runnable {
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also { cameraPreview ->
                    cameraPreview.setSurfaceProvider(previewView.surfaceProvider)
                }

            val imageAnalysis = ImageAnalysis.Builder()
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also { analysis ->
                    analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                        try {
                            val now = System.currentTimeMillis()
                            val last = lastAnalysisTimestamp.get()

                            if (
                                now - last >= LIVE_ANALYSIS_INTERVAL_MS &&
                                lastAnalysisTimestamp.compareAndSet(last, now)
                            ) {
                                val count = analyzedFrameCount.incrementAndGet()
                                val rotationDegrees = imageProxy.imageInfo.rotationDegrees

                                val bitmap = imageProxy
                                    .toBitmapFromRgba8888()
                                    ?.rotate(rotationDegrees)

                                if (bitmap == null) {
                                    mainExecutor.execute {
                                        analysisStatusText =
                                            "Não foi possível processar a imagem da câmera."
                                    }
                                } else {
                                    val result = detector.runOnBitmap(
                                        bitmap = bitmap,
                                        confidenceThreshold = LOW_CONFIDENCE_THRESHOLD
                                    )

                                    val liveText = formatLiveDetectionResult(
                                        result = result,
                                        frameCount = count
                                    )

                                    val speechText = buildLiveSpeechMessage(
                                        result = result,
                                        now = now,
                                        lastSpokenObject = lastSpokenObject,
                                        lastSpeechTimestamp = lastSpeechTimestamp
                                    )

                                    mainExecutor.execute {
                                        analysisStatusText = liveText

                                        if (speechText != null) {
                                            currentOnLiveDetectionMessage(speechText)
                                            vibrateForLiveDetection(context, result)
                                        }
                                    }
                                }
                            }
                        } catch (exception: Exception) {
                            exception.printStackTrace()

                            mainExecutor.execute {
                                analysisStatusText =
                                    "Erro ao analisar a câmera. Tente reiniciar a detecção contínua."
                            }
                        } finally {
                            imageProxy.close()
                        }
                    }
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalysis
                )
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
        }

        cameraProviderFuture.addListener(
            listener,
            mainExecutor
        )

        onDispose {
            if (cameraProviderFuture.isDone) {
                cameraProviderFuture.get().unbindAll()
            }
            cameraExecutor.shutdown()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { previewView }
        )

        Text(
            text = analysisStatusText,
            style = MaterialTheme.typography.labelMedium,
            color = Color.White,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)
                .background(
                    color = Color.Black.copy(alpha = 0.65f),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .semantics {
                    contentDescription = analysisStatusText
                    liveRegion = LiveRegionMode.Polite
                }
        )
    }
}

private fun formatLiveDetectionResult(
    result: LocalDetectionResult,
    frameCount: Int
): String {
    val topDetection = result.detections.firstOrNull()

    return if (topDetection == null) {
        "Câmera ativa | Nenhum objeto reconhecido | ${result.inferenceMs} ms | análise $frameCount"
    } else {
        val translatedName = translateClassName(topDetection.className)
        val confidencePercent = (topDetection.confidence * 100).roundToInt()
        val confidenceLevel = confidenceLevelLabel(topDetection.confidence)
        val detectionLabel = if (topDetection.confidence >= HIGH_CONFIDENCE_THRESHOLD) {
            "Detectado"
        } else {
            "Possível"
        }

        "$detectionLabel: $translatedName $confidencePercent% | $confidenceLevel | ${result.inferenceMs} ms | análise $frameCount"
    }
}

private fun buildLiveSpeechMessage(
    result: LocalDetectionResult,
    now: Long,
    lastSpokenObject: AtomicReference<String?>,
    lastSpeechTimestamp: AtomicLong
): String? {
    val topDetection = result.detections.firstOrNull() ?: return null

    val objectName = translateClassName(topDetection.className)
    val confidence = topDetection.confidence
    val previousObject = lastSpokenObject.get()
    val previousSpeechTime = lastSpeechTimestamp.get()

    val isSameObject = previousObject == objectName
    val isInsideGlobalCooldown = now - previousSpeechTime < LIVE_MIN_TIME_BETWEEN_SPEECH_MS
    val isInsideSameObjectCooldown = now - previousSpeechTime < LIVE_SAME_OBJECT_COOLDOWN_MS

    if (isInsideGlobalCooldown) {
        return null
    }

    if (isSameObject && isInsideSameObjectCooldown) {
        return null
    }

    lastSpokenObject.set(objectName)
    lastSpeechTimestamp.set(now)

    return when {
        confidence >= HIGH_CONFIDENCE_THRESHOLD -> {
            "$objectName detectado. Confiança alta."
        }

        confidence >= MEDIUM_CONFIDENCE_THRESHOLD -> {
            "Possível objeto: $objectName. Confiança média."
        }

        confidence >= LOW_CONFIDENCE_THRESHOLD -> {
            "Possível objeto: $objectName. Confiança baixa."
        }

        else -> {
            null
        }
    }
}

private fun confidenceLevelLabel(confidence: Float): String {
    return when {
        confidence >= HIGH_CONFIDENCE_THRESHOLD -> "confiança alta"
        confidence >= MEDIUM_CONFIDENCE_THRESHOLD -> "confiança média"
        confidence >= LOW_CONFIDENCE_THRESHOLD -> "confiança baixa"
        else -> "sem confiança suficiente"
    }
}

private fun ImageProxy.toBitmapFromRgba8888(): Bitmap? {
    val plane = planes.firstOrNull() ?: return null
    val buffer = plane.buffer

    val pixelStride = plane.pixelStride
    val rowStride = plane.rowStride
    val rowPadding = rowStride - pixelStride * width

    val bitmapWidth = width + rowPadding / pixelStride

    val bitmapWithPadding = Bitmap.createBitmap(
        bitmapWidth,
        height,
        Bitmap.Config.ARGB_8888
    )

    buffer.rewind()
    bitmapWithPadding.copyPixelsFromBuffer(buffer)

    return if (bitmapWidth == width) {
        bitmapWithPadding
    } else {
        Bitmap.createBitmap(
            bitmapWithPadding,
            0,
            0,
            width,
            height
        )
    }
}

private fun Bitmap.rotate(rotationDegrees: Int): Bitmap {
    if (rotationDegrees == 0) {
        return this
    }

    val matrix = Matrix().apply {
        postRotate(rotationDegrees.toFloat())
    }

    return Bitmap.createBitmap(
        this,
        0,
        0,
        width,
        height,
        matrix,
        true
    )
}

private fun vibrateForLiveDetection(
    context: Context,
    result: LocalDetectionResult
) {
    val topDetection = result.detections.firstOrNull() ?: return
    val vibrator = getVibrator(context) ?: return

    if (!vibrator.hasVibrator()) {
        return
    }

    val vibrationPattern = when {
        topDetection.confidence >= HIGH_CONFIDENCE_THRESHOLD -> {
            longArrayOf(0, 70)
        }

        topDetection.confidence >= MEDIUM_CONFIDENCE_THRESHOLD -> {
            longArrayOf(0, 50, 90, 50)
        }

        else -> {
            longArrayOf(0, 40)
        }
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(
            VibrationEffect.createWaveform(
                vibrationPattern,
                -1
            )
        )
    } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(vibrationPattern, -1)
    }
}

private fun getVibrator(context: Context): Vibrator? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(VibratorManager::class.java)
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }
}

private fun hasCameraPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED
}