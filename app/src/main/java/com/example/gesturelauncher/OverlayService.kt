package com.example.gesturelauncher

import android.app.Service
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
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        setupCursor()
    }

    private fun setupCursor() {
        val circle = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(Color.RED)
            setStroke(4, Color.WHITE)
        }

        cursorView = View(this).apply {
            background = circle
        }

        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }

        layoutParams = WindowManager.LayoutParams(
            60, 60,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
        }

        try {
            windowManager?.addView(cursorView, layoutParams)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateCursor(x: Float, y: Float, isClicking: Boolean) {
        layoutParams?.let { params ->
            params.x = x.toInt() - 30
            params.y = y.toInt() - 30

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
