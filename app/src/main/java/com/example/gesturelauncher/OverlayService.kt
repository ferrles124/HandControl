package com.example.gesturelauncher

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.View
import android.view.WindowManager

class OverlayService : Service() {

    private var windowManager: WindowManager? = null
    private var cursorView: View? = null
    private var layoutParams: WindowManager.LayoutParams? = null
    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): OverlayService = this@OverlayService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        cursorView = View(this).apply {
            val drawable = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.RED)
            }
            background = drawable
        }

        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }

        // Noktanın görünür kalması ve ekranın üzerinde kalması için flags ayarı
        layoutParams = WindowManager.LayoutParams(
            60, 60, // Nokta boyutunu biraz büyüterek (60x60 px) fark edilmesini kolaylaştırdık
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or 
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or 
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 300 // Başlangıçta ekranın ortalarına yakın durması için
            y = 500
        }

        try {
            windowManager?.addView(cursorView, layoutParams)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateCursor(x: Float, y: Float, isClicking: Boolean) {
        layoutParams?.let { params ->
            params.x = x.toInt()
            params.y = y.toInt()
            
            cursorView?.post {
                val drawable = cursorView?.background as? GradientDrawable
                drawable?.setColor(if (isClicking) Color.GREEN else Color.RED)
                try {
                    windowManager?.updateViewLayout(cursorView, params)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cursorView?.let { 
            try {
                windowManager?.removeView(it)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
