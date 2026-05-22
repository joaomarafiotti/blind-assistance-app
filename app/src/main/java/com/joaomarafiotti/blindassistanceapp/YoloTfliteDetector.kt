package com.joaomarafiotti.blindassistanceapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import kotlin.math.min

data class LocalDetectionSummary(
    val message: String,
    val inferenceMs: Long,
    val outputShape: String,
    val topValues: List<Float>
)

class YoloTfliteDetector(
    private val context: Context
) {
    private val interpreter: Interpreter by lazy {
        val modelBuffer = loadModelFile("classroom_yolo26n_e50_best_float32.tflite")
        Interpreter(modelBuffer)
    }

    fun runOnImageUri(imageUri: Uri): LocalDetectionSummary {
        val bitmap = context.contentResolver.openInputStream(imageUri).use { inputStream ->
            BitmapFactory.decodeStream(inputStream)
        } ?: return LocalDetectionSummary(
            message = "Erro ao abrir imagem para inferência local.",
            inferenceMs = 0,
            outputShape = "N/A",
            topValues = emptyList()
        )

        return runOnBitmap(bitmap)
    }

    private fun runOnBitmap(bitmap: Bitmap): LocalDetectionSummary {
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 640, 640, true)
        val inputBuffer = bitmapToFloat32ByteBuffer(resizedBitmap)

        val output = Array(1) { Array(300) { FloatArray(6) } }

        val start = System.nanoTime()
        interpreter.run(inputBuffer, output)
        val end = System.nanoTime()

        val inferenceMs = (end - start) / 1_000_000

        val flattened = output[0]
            .flatMap { it.asList() }
            .filter { it.isFinite() }

        val topValues = flattened
            .sortedDescending()
            .take(10)

        val message = buildString {
            append("Inferência local executada com sucesso. ")
            append("Tempo aproximado: ")
            append(inferenceMs)
            append(" ms. ")
            append("Formato da saída: 1 x 300 x 6.")
        }

        return LocalDetectionSummary(
            message = message,
            inferenceMs = inferenceMs,
            outputShape = "1 x 300 x 6",
            topValues = topValues
        )
    }

    private fun bitmapToFloat32ByteBuffer(bitmap: Bitmap): ByteBuffer {
        val inputBuffer = ByteBuffer.allocateDirect(1 * 640 * 640 * 3 * 4)
        inputBuffer.order(ByteOrder.nativeOrder())

        val pixels = IntArray(640 * 640)
        bitmap.getPixels(pixels, 0, 640, 0, 0, 640, 640)

        for (pixel in pixels) {
            val r = (pixel shr 16 and 0xFF) / 255.0f
            val g = (pixel shr 8 and 0xFF) / 255.0f
            val b = (pixel and 0xFF) / 255.0f

            inputBuffer.putFloat(r)
            inputBuffer.putFloat(g)
            inputBuffer.putFloat(b)
        }

        inputBuffer.rewind()
        return inputBuffer
    }

    private fun loadModelFile(assetName: String): ByteBuffer {
        val fileDescriptor = context.assets.openFd(assetName)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel

        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength

        return fileChannel.map(
            FileChannel.MapMode.READ_ONLY,
            startOffset,
            declaredLength
        )
    }
}