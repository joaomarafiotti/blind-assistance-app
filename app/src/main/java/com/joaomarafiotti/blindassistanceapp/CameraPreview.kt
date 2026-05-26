package com.joaomarafiotti.blindassistanceapp

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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.roundToInt

private const val LIVE_ANALYSIS_INTERVAL_MS = 2500L
private const val LIVE_SPEECH_COOLDOWN_MS = 5000L
private const val HIGH_CONFIDENCE_THRESHOLD = 0.65f

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
                text = "Permissão de câmera necessária para iniciar a detecção contínua."
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
        mutableStateOf("Aguardando frames da câmera...")
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
                                        "Falha ao converter frame da câmera para Bitmap."
                                }
                            } else {
                                try {
                                    val result = detector.runOnBitmap(bitmap)
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
                                } catch (exception: Exception) {
                                    exception.printStackTrace()

                                    mainExecutor.execute {
                                        analysisStatusText =
                                            "Erro ao executar detecção no frame da câmera."
                                    }
                                }
                            }
                        }

                        imageProxy.close()
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
                .semantics {
                    contentDescription = analysisStatusText
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
        "Frame $frameCount | Nenhum objeto reconhecido | ${result.inferenceMs} ms"
    } else {
        val confidencePercent = (topDetection.confidence * 100).roundToInt()
        "Frame $frameCount | ${topDetection.className} $confidencePercent% | ${result.inferenceMs} ms"
    }
}

private fun buildLiveSpeechMessage(
    result: LocalDetectionResult,
    now: Long,
    lastSpokenObject: AtomicReference<String?>,
    lastSpeechTimestamp: AtomicLong
): String? {
    val topDetection = result.detections.firstOrNull() ?: return null

    val objectName = topDetection.className
    val confidence = topDetection.confidence
    val confidencePercent = (confidence * 100).roundToInt()

    val previousObject = lastSpokenObject.get()
    val previousSpeechTime = lastSpeechTimestamp.get()

    val isSameObject = previousObject == objectName
    val isInsideCooldown = now - previousSpeechTime < LIVE_SPEECH_COOLDOWN_MS

    if (isSameObject && isInsideCooldown) {
        return null
    }

    lastSpokenObject.set(objectName)
    lastSpeechTimestamp.set(now)

    return if (confidence >= HIGH_CONFIDENCE_THRESHOLD) {
        "Detectado: $objectName. Confiança $confidencePercent por cento."
    } else {
        "Possível objeto: $objectName. Confiança $confidencePercent por cento."
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

    val vibrationPattern = if (topDetection.confidence >= HIGH_CONFIDENCE_THRESHOLD) {
        longArrayOf(0, 90)
    } else {
        longArrayOf(0, 60, 80, 60)
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