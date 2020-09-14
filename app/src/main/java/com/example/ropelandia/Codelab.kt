package com.example.ropelandia

import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_codelab.*
import topcodes.TopCodesScanner
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class Codelab : AppCompatActivity() {

    private lateinit var cameraExecutor: ExecutorService
    private lateinit var topCodesAnalyser: ImageAnalysis.Analyzer

    private lateinit var imageCapture: ImageCapture

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_codelab)
        createTopCodesImageAnalyser()

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun createTopCodesImageAnalyser() {
        val cameraImageConverter = CameraImageConverter(context = baseContext)
        val topCodesScanner = TopCodesScanner()

        topCodesAnalyser =
            TopCodesAnalyser(cameraImageConverter, topCodesScanner) {
                print(it.size)
            }
    }

    fun takePhoto(view: View) {

        val imageCapture = imageCapture

        val photoFile = File(filesDir, "topCodes.jpg")

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val bitmap = BitmapFactory.decodeFile(photoFile.path)
                    TopCodesScanner().let {
                        it.setMaxCodeDiameter(100)
                        it.searchTopCodes(bitmap)
                    }.let { topCodes ->
                        print(topCodes)
                        textView.text = topCodes.size.toString()
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    val message = exception.message ?: "Image save callback error"
                    Toast.makeText(baseContext, message, Toast.LENGTH_SHORT)
                        .show()
                }
            }
        )


    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        val cameraProviderFutureListener = Runnable {
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewFinder.createSurfaceProvider())
                }

            imageCapture = ImageCapture.Builder().build()

            val imageAnalyser = ImageAnalysis.Builder()
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, topCodesAnalyser)
                }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageCapture
                )
            } catch (e: Exception) {
                Log.e(TAG, "Use case binding failed", e)
            }
        }

        cameraProviderFuture.addListener(
            cameraProviderFutureListener,
            ContextCompat.getMainExecutor(this)
        )
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(
                    this,
                    resources.getString(R.string.permission_not_granted),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "CameraXBasic"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }

}