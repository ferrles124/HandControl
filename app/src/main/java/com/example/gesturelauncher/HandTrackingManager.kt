package com.example.gesturelauncher

import kotlin.math.hypot

class HandTrackingManager {

    private var lastClickTime = 0L
    private val CLICK_COOLDOWN_MS = 1000L // Yanlışlıkla peş peşe tıklamayı önlemek için 1 saniye bekleme

    // İşaret parmağı (Tip 8) ve Başparmak (Tip 4) koordinatlarını alıp işlem yapar
    fun processHandLandmarks(thumbX: Float, thumbY: Float, indexX: Float, indexY: Float, screenWidth: Int, screenHeight: Int) {
        // İki nokta arasındaki mesafeyi hesapla
        val distance = hypot((indexX - thumbX).toDouble(), (indexY - thumbY).toDouble())

        // Mesafe eşik değerinin altındaysa tıklama algıla
        if (distance < 0.05) { // 0.05 normalize edilmiş mesafe eşiği
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastClickTime > CLICK_COOLDOWN_MS) {
                lastClickTime = currentTime
                
                // Kamera görüntüsünü ekran koordinatlarına dönüştür
                val targetX = indexX * screenWidth
                val targetY = indexY * screenHeight

                // Servis aktifse dokunmayı gerçekleştir
                GestureAccessibilityService.instance?.performClickAt(targetX, targetY)
            }
        }
    }
}

