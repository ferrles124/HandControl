package com.example.gesturelauncher

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.camera.core.ImageProxy
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult

class MediaPipeHandDetector(
    private val context: Context,
    private val onResults: (thumbX: Float, thumbY: Float, indexX: Float, indexY: Float) -> Unit
) {

    private var handLandmarker: HandLandmarker? = null

    init {
        setupHandLandmarker()
    }

    private fun setupHandLandmarker() {
        val baseOptions = BaseOptions.builder()
            .setModelAssetPath("hand_landmarker.task")
            .build()

        val options = HandLandmarker.HandLandmarkerOptions.builder()
            .setBaseOptions(baseOptions)
            .setMinHandDetectionConfidence(0.5f)
            .setMinHandPresenceConfidence(0.5f)
            .setMinTrackingConfidence(0.5f)
            .setNumHands(1)
            .setRunningMode(RunningMode.IMAGE)
            .build()

        try {
            handLandmarker = HandLandmarker.createFromOptions(context, options)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun detectHand(imageProxy: ImageProxy): HandLandmarkerResult? {
        var landmarkerResult: HandLandmarkerResult? = null
        val bitmap = imageProxy.toBitmapOrNull()
        if (bitmap != null && handLandmarker != null) {
            val rotatedBitmap = rotateBitmap(bitmap, imageProxy.imageInfo.rotationDegrees.toFloat())
            val mpImage = BitmapImageBuilder(rotatedBitmap).build()

            landmarkerResult = handLandmarker?.detect(mpImage)

            landmarkerResult?.landmarks()?.firstOrNull()?.let { landmarks ->
                if (landmarks.size >= 9) {
                    val thumb = landmarks[4]
                    val index = landmarks[8]

                    onResults(thumb.x(), thumb.y(), index.x(), index.y())
                }
            }
        }
        imageProxy.close()
        return landmarkerResult
    }

    private fun ImageProxy.toBitmapOrNull(): Bitmap? {
        return try {
            toBitmap()
        } catch (e: Exception) {
            null
        }
    }

    private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        if (degrees == 0f) return bitmap
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    fun close() {
        handLandmarker?.close()
    }
}
