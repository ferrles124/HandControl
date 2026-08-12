package com.example.gesturelauncher

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.util.DisplayMetrics
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private val CAMERA_REQ_CODE = 101
    private val OVERLAY_REQ_CODE = 102
    private var cameraManager: CameraManager? = null
    private var overlayService: OverlayService? = null
    private val handTrackingManager = HandTrackingManager()
    private val clickCounts = IntArray(4) { 0 }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            overlayService = (service as OverlayService.LocalBinder).getService()
            handTrackingManager.overlayService = overlayService
        }
        override fun onServiceDisconnected(name: ComponentName?) {
            overlayService = null
            handTrackingManager.overlayService = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkAndRequestOverlayPermission()
        bindService(Intent(this, OverlayService::class.java), serviceConnection, BIND_AUTO_CREATE)

        val btnCamera = findViewById<Button>(R.id.btnCameraPermission)
        val previewView = findViewById<PreviewView>(R.id.viewFinder)
        val overlayView = findViewById<OverlayView>(R.id.overlayView)
        val tvStatus = findViewById<TextView>(R.id.tvStatus)

        val btns = listOf(R.id.btnTest1, R.id.btnTest2, R.id.btnTest3, R.id.btnTest4).map { findViewById<Button>(it) }
        btns.forEachIndexed { i, btn ->
            btn.setOnClickListener { clickCounts[i]++; btn.text = "Test ${i+1}\nTık: ${clickCounts[i]}" }
        }

        btnCamera.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                initCamera(previewView, overlayView, tvStatus)
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_REQ_CODE)
            }
        }
    }

    private fun checkAndRequestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            startActivityForResult(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")), OVERLAY_REQ_CODE)
        } else {
            startService(Intent(this, OverlayService::class.java))
        }
    }

    private fun initCamera(previewView: PreviewView, overlayView: OverlayView, tvStatus: TextView) {
        val dm = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(dm)

        cameraManager = CameraManager(this, this, previewView, overlayView) { thumbX, thumbY, indexX, indexY ->
            runOnUiThread {
                tvStatus.text = "El Takip Ediliyor"
                handTrackingManager.processHandLandmarks(thumbX, thumbY, indexX, indexY, dm.widthPixels, dm.heightPixels)
            }
        }
        cameraManager?.startCamera()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraManager?.shutdown()
        unbindService(serviceConnection)
    }
}
