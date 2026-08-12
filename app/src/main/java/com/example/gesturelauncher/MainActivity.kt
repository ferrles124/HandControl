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
    
    // UI Bileşenleri
    private lateinit var btnCamera: Button
    private lateinit var previewView: PreviewView
    private lateinit var overlayView: OverlayView
    private lateinit var tvStatus: TextView

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as OverlayService.LocalBinder
            overlayService = binder.getService()
            // Servis bağlandığı an handTrackingManager'a bildiriyoruz
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

        // UI Elemanlarını bağla
        btnCamera = findViewById(R.id.btnCameraPermission)
        previewView = findViewById(R.id.viewFinder)
        overlayView = findViewById(R.id.overlayView)
        tvStatus = findViewById(R.id.tvStatus)

        // Servis izinlerini ve başlatılmasını kontrol et
        checkAndRequestOverlayPermission()
        bindService(Intent(this, OverlayService::class.java), serviceConnection, BIND_AUTO_CREATE)

        btnCamera.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                initCamera()
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_REQ_CODE)
            }
        }
    }

    private fun checkAndRequestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            startActivityForResult(intent, OVERLAY_REQ_CODE)
        } else {
            startService(Intent(this, OverlayService::class.java))
        }
    }

    private fun initCamera() {
        val dm = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(dm)

        // Sadece baş ve işaret parmağı verisi alan CameraManager callback'i
        cameraManager = CameraManager(this, this, previewView, overlayView) { thumbX, thumbY, indexX, indexY ->
            runOnUiThread {
                tvStatus.text = "El Takip Ediliyor"
                // Koordinatları işlenmesi için Manager'a gönderiyoruz
                handTrackingManager.processHandLandmarks(
                    thumbX, thumbY, indexX, indexY, 
                    dm.widthPixels, dm.heightPixels
                )
            }
        }
        cameraManager?.startCamera()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_REQ_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initCamera()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraManager?.shutdown()
        // Servis bağlantısını kopar
        try {
            unbindService(serviceConnection)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
