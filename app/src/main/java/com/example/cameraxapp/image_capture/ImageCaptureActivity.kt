package com.example.cameraxapp.image_capture

import android.Manifest
import android.media.MediaScannerConnection
import android.os.Bundle
import android.util.Rational
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.core.CameraX.LensFacing.BACK
import androidx.camera.core.CameraX.LensFacing.FRONT
import com.example.cameraxapp.R
import com.example.cameraxapp.preview.AutoFitPreviewBuilder
import com.github.florent37.runtimepermission.kotlin.askPermission
import kotlinx.android.synthetic.main.activity_image_capture.*
import kotlinx.android.synthetic.main.activity_preview.textureView
import java.io.File

class ImageCaptureActivity : AppCompatActivity() {

    private lateinit var preview: Preview
    private var lensFacing = BACK

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_capture)

        askPermission(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE) {
            textureView.post { bindCameraUseCases() }
        }
        btnToggleTorch.setOnClickListener { preview.enableTorch(!preview.isTorchOn) }
        btnToggleCamera.setOnClickListener { toggleCamera() }
    }

    private fun bindCameraUseCases() {
        val previewConfig = PreviewConfig.Builder().setLensFacing(lensFacing).build()
        preview = AutoFitPreviewBuilder.build(previewConfig, textureView)

        // Create configuration object for the image capture use case
        val imageCaptureConfig = ImageCaptureConfig.Builder()
            .setTargetAspectRatio(Rational(1, 1))
            .setLensFacing(lensFacing)
            .setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)
            .setFlashMode(FlashMode.AUTO)
            .build()

        // Build the image capture use case and attach button click listener
        val imageCapture = ImageCapture(imageCaptureConfig)

        // Mirror image when using the front camera
        val metadata = ImageCapture.Metadata().apply {
            //isReversedVertical = lensFacing == CameraX.LensFacing.FRONT
        }

        btnTakePhoto.setOnClickListener {
            val file = File(externalMediaDirs.first(), "${System.currentTimeMillis()}.jpg")
            imageCapture.takePicture(file, object : ImageCapture.OnImageSavedListener {

                override fun onError(
                    imageCaptureError: ImageCapture.ImageCaptureError,
                    message: String,
                    cause: Throwable?
                ) {
                    Toast.makeText(baseContext, "Fail: $message", Toast.LENGTH_SHORT).show()
                    cause?.printStackTrace()
                }

                override fun onImageSaved(file: File) {
                    Toast.makeText(baseContext, "Success: ${file.absolutePath}", Toast.LENGTH_SHORT).show()
                    makeImageVisibleInGalleryApp(file)
                }
            }, metadata)
        }

        // Bind use cases to lifecycle. If Android Studio complains about "this" being not a LifecycleOwner
        // try rebuilding the project or updating the appcompat dependency to version 1.1.0 or higher.
        CameraX.bindToLifecycle(this, preview, imageCapture)
    }

    private fun makeImageVisibleInGalleryApp(photoFile: File) {
        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(photoFile.extension)
        MediaScannerConnection.scanFile(this, arrayOf(photoFile.absolutePath), arrayOf(mimeType), null)
    }

    private fun toggleCamera() {
        val newLensFacing = if (FRONT == lensFacing) BACK else FRONT
        if (CameraX.hasCameraWithLensFacing(newLensFacing)) {
            lensFacing = newLensFacing
            CameraX.unbindAll()
            bindCameraUseCases()
        }
    }
}
