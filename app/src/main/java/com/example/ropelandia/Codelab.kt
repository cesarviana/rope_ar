package com.example.ropelandia

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_codelab.*
import topcodes.TopCodesScanner
import java.io.File
import java.util.*
import java.util.concurrent.ExecutorService
import kotlin.concurrent.scheduleAtFixedRate

class Codelab : AppCompatActivity() {

    private lateinit var cameraExecutor: ExecutorService
    private lateinit var imageCapture: ImageCapture

    private lateinit var photoFile: File
    private lateinit var photoFileOutputOptions: ImageCapture.OutputFileOptions

    private val imageSavedCallback = object : ImageCapture.OnImageSavedCallback {
        private val topCodesScanner = TopCodesScanner()

        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
            getBitmap().also {
                searchTopCodes(it)
            }
        }

        private fun getBitmap() = BitmapFactory.decodeFile(photoFile.path)

        private fun searchTopCodes(bitmap: Bitmap) {

            val scaledBitmap = scale(bitmap)

//            val scaleProportion = mat.height.toFloat() / scaledBitmap.height
//            val blocksConverter = ScreenSizeBlocksConverter(scaleProportion)
            val targetScreenHeight = mat.height
            val targetScreenWidth = mat.width
            val blocksConverter = ProjectorBlocksConverter(targetScreenHeight, targetScreenWidth)

            topCodesScanner.searchTopCodes(scaledBitmap).let { topCodes ->
                mat.blocks = blocksConverter.convert(topCodes)
                mat.invalidate()
            }
        }

        private fun scale(bitmap: Bitmap): Bitmap {
            val scale = .5
            val width = (bitmap.width * scale).toInt()
            val height = (bitmap.height * scale).toInt()
            return Bitmap.createScaledBitmap(bitmap, width, height, true)
        }

        override fun onError(exception: ImageCaptureException) {
            val message = exception.message ?: "Image save callback error"
            Toast.makeText(baseContext, message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_codelab)
        photoFile = File(filesDir, "topCodes.jpg")
        photoFileOutputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissions()
        }

        Timer().apply {
            scheduleAtFixedRate(1000, 2000) {
                takePhoto(mat)
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
        )
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
                    baseContext,
                    resources.getString(R.string.permission_not_granted),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
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

    fun takePhoto(view: View) {
        imageCapture.takePicture(
            photoFileOutputOptions,
            ContextCompat.getMainExecutor(this),
            imageSavedCallback
        )
    }

    fun togglePreview(view: View){
        if(viewFinder.visibility == View.VISIBLE){
            viewFinder.visibility = View.INVISIBLE
        } else {
            viewFinder.visibility = View.VISIBLE
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
            android.Manifest.permission.CAMERA
        )
    }

}