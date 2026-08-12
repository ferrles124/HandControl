package com.example.gesturelauncher

import android.util.Log

class HandTrackingManager {
    var overlayService: OverlayService? = null
    
    // Hareket takibi için değişkenler
    private var startX = 0f
    private var startY = 0f
    private val SWIPE_THRESHOLD = 150f // Ekranda ne kadar hareket ederse "hareket" sayılacak

    fun processHandLandmarks(thumbX: Float, thumbY: Float, indexX: Float, indexY: Float, screenW: Int, screenH: Int) {
        // İki parmağın orta noktasını hesapla (İmleç burada olacak)
        val midX = (thumbX + indexX) / 2 * screenW
        val midY = (thumbY + indexY) / 2 * screenH

        // İmleci güncelle
        overlayService?.updateCursor(midX, midY, false)

        // Subway Surfers Mantığı: Swipe Algılama
        detectSwipe(midX, midY)
    }

    private fun detectSwipe(x: Float, y: Float) {
        if (startX == 0f) { startX = x; startY = y; return }

        val diffX = x - startX
        val diffY = y - startY

        // Yatay hareket mi dikey hareket mi daha baskın?
        if (Math.abs(diffX) > Math.abs(diffY)) {
            // YATAY SWIPE
            if (Math.abs(diffX) > SWIPE_THRESHOLD) {
                if (diffX > 0) Log.d("Gesture", "SAĞA KAYDIRILDI")
                else Log.d("Gesture", "SOLA KAYDIRILDI")
                startX = x; startY = y // Sıfırla
            }
        } else {
            // DİKEY SWIPE
            if (Math.abs(diffY) > SWIPE_THRESHOLD) {
                if (diffY > 0) Log.d("Gesture", "AŞAĞI KAYDIRILDI (EĞİL)")
                else Log.d("Gesture", "YUKARI KAYDIRILDI (ZIPLA)")
                startX = x; startY = y // Sıfırla
            }
        }
    }
}
