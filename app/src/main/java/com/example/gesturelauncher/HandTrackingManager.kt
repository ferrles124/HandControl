package com.example.gesturelauncher

import kotlin.math.hypot

class HandTrackingManager {

    private var lastClickTime = 0L
    private val CLICK_COOLDOWN_MS = 1000L
    var overlayService: OverlayService? = null

    fun processHandLandmarks(thumbX: Float, thumbY: Float, indexX: Float, indexY: Float, screenWidth: Int, screenHeight: Int) {
        // Aynalama (mirroring) düzeltmesi için X koordinatını ters çeviriyoruz
        val correctedIndexX = 1.0f - indexX
        val correctedThumbX = 1.0f - thumbX

        val targetX = correctedIndexX * screenWidth
        val targetY = indexY * screenHeight

        val distance = hypot((correctedIndexX - correctedThumbX).toDouble(), (indexY - thumbY).toDouble())
        val isClicking = distance < 0.08 // Tıklama hassasiyet eşiği

        // Ekrandaki imlecin konumunu güncelle
        overlayService?.updateCursor(targetX, targetY, isClicking)

        if (isClicking) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastClickTime > CLICK_COOLDOWN_MS) {
                lastClickTime = currentTime
                GestureAccessibilityService.instance?.performClickAt(targetX, targetY)
            }
        }
    }
}
