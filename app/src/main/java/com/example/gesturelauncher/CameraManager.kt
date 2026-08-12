package com.example.gesturelauncher

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.Executors

class CameraManager(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val previewView: PreviewView,
    private val overlayView: OverlayView,
    private val onHandDetected: (thumbX: Float, thumbY: Float, indexX: Float, indexY: Float) -> Unit
) {

    private val cameraExecutor = Executors.newSingleThreadExecutor()
    private var detector: MediaPipeHandDetector? = null

    fun startCamera() {
        detector = MediaPipeHandDetector(context) { thumbX, thumbY, indexX, indexY ->
            onHandDetected(thumbX, thumbY, indexX, indexY)
        }

        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build()

            imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                val result = detector?.detectHand(imageProxy)
                result?.let { handLandmarkerResult ->
                    overlayView.setResults(handLandmarkerResult)
                } ?: run {
                    overlayView.clear()
                }
            }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalysis
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(context))
    }

    fun shutdown() {
        detector?.close()
        cameraExecutor.shutdown()
    }
}
