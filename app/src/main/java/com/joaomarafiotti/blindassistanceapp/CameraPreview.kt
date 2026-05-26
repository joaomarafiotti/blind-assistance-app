package com.joaomarafiotti.blindassistanceapp

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
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

@Composable
fun CameraPreviewScreen(
    modifier: Modifier = Modifier
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
        CameraPreviewContent(modifier = modifier)
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
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var analysisStatusText by remember {
        mutableStateOf("Aguardando frames da câmera...")
    }

    val cameraExecutor = remember {
        Executors.newSingleThreadExecutor()
    }

    val mainExecutor = remember(context) {
        ContextCompat.getMainExecutor(context)
    }

    val convertedFrameCount = remember {
        AtomicInteger(0)
    }

    val lastAnalysisTimestamp = remember {
        AtomicLong(0L)
    }

    val analysisIntervalMs = 2500L

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
                            now - last >= analysisIntervalMs &&
                            lastAnalysisTimestamp.compareAndSet(last, now)
                        ) {
                            val bitmap = imageProxy.toBitmapFromRgba8888()
                            val count = convertedFrameCount.incrementAndGet()
                            val rotationDegrees = imageProxy.imageInfo.rotationDegrees

                            mainExecutor.execute {
                                analysisStatusText = if (bitmap != null) {
                                    "Frames convertidos: $count | Bitmap: ${bitmap.width}x${bitmap.height} | rotação: ${rotationDegrees}°"
                                } else {
                                    "Falha ao converter frame da câmera para Bitmap."
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

private fun hasCameraPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED
}