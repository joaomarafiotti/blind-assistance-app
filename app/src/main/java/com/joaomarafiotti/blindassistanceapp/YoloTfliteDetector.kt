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
import kotlin.math.roundToInt

data class LocalDetection(
    val classId: Int,
    val className: String,
    val confidence: Float,
    val box: List<Float>
)

data class LocalDetectionResult(
    val detections: List<LocalDetection>,
    val inferenceMs: Long,
    val outputShape: String,
    val rawCandidateCount: Int
)

class YoloTfliteDetector(
    private val context: Context
) {
    private val labels: List<String> by lazy {
        loadLabels("labels.txt")
    }

    private val interpreter: Interpreter by lazy {
        val modelBuffer = loadModelFile("classroom_yolo26n_e50_best_float32.tflite")
        Interpreter(modelBuffer)
    }

    fun runOnImageUri(
        imageUri: Uri,
        confidenceThreshold: Float = 0.25f
    ): LocalDetectionResult {
        val bitmap = context.contentResolver.openInputStream(imageUri).use { inputStream ->
            BitmapFactory.decodeStream(inputStream)
        } ?: return LocalDetectionResult(
            detections = emptyList(),
            inferenceMs = 0,
            outputShape = "N/A",
            rawCandidateCount = 0
        )

        return runOnBitmap(
            bitmap = bitmap,
            confidenceThreshold = confidenceThreshold
        )
    }

    private fun runOnBitmap(
        bitmap: Bitmap,
        confidenceThreshold: Float
    ): LocalDetectionResult {
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 640, 640, true)
        val inputBuffer = bitmapToFloat32ByteBuffer(resizedBitmap)

        // Exported YOLO26n TFLite output:
        // shape: 1 x 300 x 6
        //
        // Expected row layout:
        // [x1, y1, x2, y2, confidence, class_id]
        val output = Array(1) { Array(300) { FloatArray(6) } }

        val start = System.nanoTime()
        interpreter.run(inputBuffer, output)
        val end = System.nanoTime()

        val inferenceMs = (end - start) / 1_000_000

        val detections = parseOutput(
            outputRows = output[0],
            confidenceThreshold = confidenceThreshold
        )

        return LocalDetectionResult(
            detections = detections,
            inferenceMs = inferenceMs,
            outputShape = "1 x 300 x 6",
            rawCandidateCount = output[0].size
        )
    }

    private fun parseOutput(
        outputRows: Array<FloatArray>,
        confidenceThreshold: Float
    ): List<LocalDetection> {
        val parsedDetections = mutableListOf<LocalDetection>()

        for (row in outputRows) {
            if (row.size < 6) continue

            val parsedClassAndConfidence = parseClassAndConfidence(row) ?: continue
            val classId = parsedClassAndConfidence.first
            val confidence = parsedClassAndConfidence.second

            if (confidence < confidenceThreshold) continue
            if (classId !in labels.indices) continue

            val className = labels[classId]

            parsedDetections.add(
                LocalDetection(
                    classId = classId,
                    className = className,
                    confidence = confidence,
                    box = listOf(row[0], row[1], row[2], row[3])
                )
            )
        }

        // Keep the highest-confidence detection for each class.
        return parsedDetections
            .groupBy { it.classId }
            .mapNotNull { (_, detectionsForClass) ->
                detectionsForClass.maxByOrNull { it.confidence }
            }
            .sortedByDescending { it.confidence }
    }

    private fun parseClassAndConfidence(row: FloatArray): Pair<Int, Float>? {
        // Most likely layout:
        // [x1, y1, x2, y2, confidence, class_id]
        val confidenceA = row[4]
        val classIdA = row[5].roundToInt()

        if (confidenceA in 0.0f..1.0f && classIdA in labels.indices) {
            return classIdA to confidenceA
        }

        // Fallback layout, in case model output comes as:
        // [x1, y1, x2, y2, class_id, confidence]
        val classIdB = row[4].roundToInt()
        val confidenceB = row[5]

        if (confidenceB in 0.0f..1.0f && classIdB in labels.indices) {
            return classIdB to confidenceB
        }

        return null
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

    private fun loadLabels(assetName: String): List<String> {
        return context.assets.open(assetName).bufferedReader().useLines { lines ->
            lines
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .toList()
        }
    }
}