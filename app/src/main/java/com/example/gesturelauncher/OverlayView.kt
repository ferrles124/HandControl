package com.example.gesturelauncher

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult

class OverlayView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private var results: HandLandmarkerResult? = null
    private val pointPaint = Paint().apply {
        color = Color.GREEN
        strokeWidth = 12f
        style = Paint.Style.FILL
    }
    private val linePaint = Paint().apply {
        color = Color.RED
        strokeWidth = 6f
        style = Paint.Style.STROKE
    }

    fun setResults(handLandmarkerResult: HandLandmarkerResult) {
        results = handLandmarkerResult
        postInvalidate()
    }

    fun clear() {
        results = null
        postInvalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val currentResults = results ?: return

        for (landmarks in currentResults.landmarks()) {
            // El eklemlerinin noktalarını çiz (Aynalayarak)
            for (landmark in landmarks) {
                val x = (1.0f - landmark.x()) * width
                val y = landmark.y() * height
                canvas.drawCircle(x, y, 8f, pointPaint)
            }

            // Temel parmak bağlantı çizgilerini çiz
            val connections = listOf(
                Pair(0, 1), Pair(1, 2), Pair(2, 3), Pair(3, 4), // Başparmak
                Pair(0, 5), Pair(5, 6), Pair(6, 7), Pair(7, 8), // İşaret
                Pair(5, 9), Pair(9, 10), Pair(10, 11), Pair(11, 12), // Orta
                Pair(9, 13), Pair(13, 14), Pair(14, 15), Pair(15, 16), // Yüzük
                Pair(13, 17), Pair(17, 18), Pair(18, 19), Pair(19, 20), Pair(0, 17) // Serçe
            )

            for (conn in connections) {
                val start = landmarks[conn.first]
                val end = landmarks[conn.second]

                val startX = (1.0f - start.x()) * width
                val startY = start.y() * height
                val endX = (1.0f - end.x()) * width
                val endY = end.y() * height

                canvas.drawLine(startX, startY, endX, endY, linePaint)
            }
        }
    }
}
