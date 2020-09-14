package com.example.ropelandia

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.hardware.camera2.*
import android.media.Image
import android.media.ImageReader
import android.util.Log
import android.view.Surface
import android.view.SurfaceView
import androidx.core.app.ActivityCompat
import java.lang.Exception

private const val TAG = "CAMERA"
const val REQUEST_CAMERA_PERMISSION_CODE = 1

class Camera(
    private var context: Context,
    private var surfaceView: SurfaceView
) {
    private var isCameraOpen = false
    private var flashLightOn = false

    var onEachFrameListener: OnEachFrameListener? = null

    private lateinit var cameraDevice: CameraDevice
    private lateinit var imageReader: ImageReader
    private lateinit var cameraSession: CameraCaptureSession
    private lateinit var captureRequest: CaptureRequest

    private val surfaces: MutableList<Surface> = mutableListOf()

    private val onImageAvailable = ImageReader.OnImageAvailableListener { readBitmap(it) }

    private val cameraManager: CameraManager =
        context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

    private fun readBitmap(reader: ImageReader) {
        val image = reader.acquireLatestImage()
        onEachFrameListener?.onNewFrame(image)
    }

    fun open() {
        if (isCameraOpen) return
        try {
            tryOpenCamera()
        } catch (e: CameraAccessException) {
            Log.d(TAG, e.message.toString())
        }
    }

    private fun tryOpenCamera() {
        val facingBackCameraId = getCameraId()
        facingBackCameraId?.let {
            val cameraAvailableListener = object : CameraManager.AvailabilityCallback() {
                override fun onCameraAvailable(cameraId: String) {
                    if (cameraId == facingBackCameraId) {
                        cameraManager.openCamera(facingBackCameraId)
                    }
                }

                override fun onCameraUnavailable(cameraId: String) {
                    Log.i("CameraAvailability", "unavailable")
                }
            }
            cameraManager.registerAvailabilityCallback(cameraAvailableListener, null)
        }
    }

    private fun CameraManager.openCamera(facingBackCameraId: String) {
        val openCameraCallback = object : CameraDevice.StateCallback() {

            override fun onOpened(camera: CameraDevice) {
                cameraDevice = camera
                startCameraSession(camera)
                isCameraOpen = true
            }

            override fun onDisconnected(camera: CameraDevice) {
                isCameraOpen = false
            }

            override fun onError(camera: CameraDevice, error: Int) {
                isCameraOpen = false
            }

            private fun startCameraSession(camera: CameraDevice) = try {
                prepareImageReader()
//                createTargetSurfaces()
                createCaptureRequest()
                createCaptureSession(camera)
            } catch (e: CameraAccessException) {
                e.printStackTrace()
            }
        }

        val activity = context as Activity

        val hasCameraPermission = ActivityCompat.checkSelfPermission(
            activity,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (hasCameraPermission) {
            try {
                openCamera(facingBackCameraId, openCameraCallback, null)
            } catch (e: Exception) {
                print(e)
            }
        }
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.CAMERA),
            REQUEST_CAMERA_PERMISSION_CODE
        )
    }

    private fun prepareImageReader() {
        val maxImages = 2
        imageReader = ImageReader.newInstance(
            surfaceView.width, surfaceView.height,
            ImageFormat.JPEG, maxImages
        )
        imageReader.setOnImageAvailableListener(onImageAvailable, null)
    }

    private fun createCaptureRequest() {
        val captureRequestBuilder =
            cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_ZERO_SHUTTER_LAG)

        if (flashLightOn) {
            captureRequestBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_TORCH)
        }

        surfaces.forEach { captureRequestBuilder.addTarget(it) }
        captureRequest = captureRequestBuilder.build()
    }

    private fun createCaptureSession(camera: CameraDevice) {

        val captureSessionCallback = object : CameraCaptureSession.StateCallback() {
            override fun onConfigured(session: CameraCaptureSession) {
                cameraSession = session
            }

            override fun onConfigureFailed(session: CameraCaptureSession) {
                Log.e(TAG, "onConfigureFailed")
            }
        }

        camera.createCaptureSession(surfaces, captureSessionCallback, null)
    }

    @Throws(CameraAccessException::class)
    private fun getCameraId(): String? {
        val back = CameraCharacteristics.LENS_FACING_BACK

        return cameraManager.cameraIdList.find {
            val lensFacing =
                cameraManager.getCameraCharacteristics(it).get(CameraCharacteristics.LENS_FACING)
            lensFacing == back
        }
    }

    interface OnEachFrameListener {
        fun onNewFrame(image: Image)
    }
}
