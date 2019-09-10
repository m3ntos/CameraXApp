package com.example.cameraxapp.vendor_extensions

import android.Manifest
import android.media.MediaScannerConnection
import android.os.Bundle
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraX
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureConfig
import androidx.camera.core.PreviewConfig
import androidx.camera.extensions.BokehImageCaptureExtender
import androidx.camera.extensions.BokehPreviewExtender
import com.example.cameraxapp.R
import com.example.cameraxapp.preview.AutoFitPreviewBuilder
import com.github.florent37.runtimepermission.kotlin.askPermission
import kotlinx.android.synthetic.main.activity_image_capture.*
import kotlinx.android.synthetic.main.activity_preview.textureView
import java.io.File

class VendorExtensionsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vendor_extensions)

        askPermission(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE) {
            textureView.post { bindCameraUseCases() }
        }
    }

    private fun bindCameraUseCases() {
        val previewConfigBuilder = PreviewConfig.Builder().setLensFacing(CameraX.LensFacing.BACK)

        val imageCaptureConfigBuilder = ImageCaptureConfig.Builder()
            .setLensFacing(CameraX.LensFacing.BACK)
            .setCaptureMode(ImageCapture.CaptureMode.MAX_QUALITY)

        // Create a Extender object which can be used to apply extension configurations.
        val bokehPreview = BokehPreviewExtender.create(previewConfigBuilder)
        val bokehImageCapture = BokehImageCaptureExtender.create(imageCaptureConfigBuilder)

        // Enable the extension if available.
        if (bokehPreview.isExtensionAvailable) bokehPreview.enableExtension()
        if (bokehImageCapture.isExtensionAvailable) bokehImageCapture.enableExtension()

        val preview = AutoFitPreviewBuilder.build(previewConfigBuilder.build(), textureView)
        val imageCapture = ImageCapture(imageCaptureConfigBuilder.build())

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
            })
        }

        // Bind use cases to lifecycle. If Android Studio complains about "this" being not a LifecycleOwner
        // try rebuilding the project or updating the appcompat dependency to version 1.1.0 or higher.
        CameraX.bindToLifecycle(this, preview, imageCapture)
    }

    private fun makeImageVisibleInGalleryApp(photoFile: File) {
        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(photoFile.extension)
        MediaScannerConnection.scanFile(this, arrayOf(photoFile.absolutePath), arrayOf(mimeType), null)
    }
}
