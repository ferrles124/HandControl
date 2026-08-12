package com.example.gesturelauncher

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.Executors

class CameraManager(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val onHandDetected: (thumbX: Float, thumbY: Float, indexX: Float, indexY: Float) -> Unit
) {

    private val cameraExecutor = Executors.newSingleThreadExecutor()

    fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            // Ön kamerayı kullanıyoruz
            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            // Canlı görüntü analizi için ImageAnalysis yapılandırması
            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                processImageProxy(imageProxy)
            }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    imageAnalysis
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(context))
    }

    private fun processImageProxy(imageProxy: ImageProxy) {
        // İlerleyen adımda MediaPipe modelinin çıktısı buraya bağlanacak
        // Şimdilik çerçevenin işlendiğini belirtip kapatıyoruz
        imageProxy.close()
    }

    fun shutdown() {
        cameraExecutor.shutdown()
    }
}

