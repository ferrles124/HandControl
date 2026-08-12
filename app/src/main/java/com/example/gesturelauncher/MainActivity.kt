package com.example.gesturelauncher

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.util.DisplayMetrics
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private val CAMERA_REQ_CODE = 101
    private var cameraManager: CameraManager? = null
    private val handTrackingManager = HandTrackingManager()

    private val clickCounts = IntArray(4) { 0 }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnCamera = findViewById<Button>(R.id.btnCameraPermission)
        val btnAccessibility = findViewById<Button>(R.id.btnAccessibilityPermission)
        val previewView = findViewById<PreviewView>(R.id.viewFinder)
        val overlayView = findViewById<OverlayView>(R.id.overlayView)
        val tvStatus = findViewById<TextView>(R.id.tvStatus)

        val btnTest1 = findViewById<Button>(R.id.btnTest1)
        val btnTest2 = findViewById<Button>(R.id.btnTest2)
        val btnTest3 = findViewById<Button>(R.id.btnTest3)
        val btnTest4 = findViewById<Button>(R.id.btnTest4)

        // Test Butonlarının Tıklama Dinleyicileri
        btnTest1.setOnClickListener { clickCounts[0]++; btnTest1.text = "Test 1\nTık: ${clickCounts[0]}" }
        btnTest2.setOnClickListener { clickCounts[1]++; btnTest2.text = "Test 2\nTık: ${clickCounts[1]}" }
        btnTest3.setOnClickListener { clickCounts[2]++; btnTest3.text = "Test 3\nTık: ${clickCounts[2]}" }
        btnTest4.setOnClickListener { clickCounts[3]++; btnTest4.text = "Test 4\nTık: ${clickCounts[4]}" }

        btnCamera.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_REQ_CODE)
            } else {
                initCamera(previewView, overlayView, tvStatus)
            }
        }

        btnAccessibility.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            initCamera(previewView, overlayView, tvStatus)
        }
    }

    private fun initCamera(previewView: PreviewView, overlayView: OverlayView, tvStatus: TextView) {
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels

        cameraManager = CameraManager(this, this, previewView, overlayView) { thumbX, thumbY, indexX, indexY ->
            runOnUiThread {
                tvStatus.text = "El Takip Ediliyor"
            }
            handTrackingManager.processHandLandmarks(thumbX, thumbY, indexX, indexY, screenWidth, screenHeight)
        }
        cameraManager?.startCamera()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraManager?.shutdown()
    }
}
