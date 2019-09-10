package com.example.cameraxapp.image_analysis

import android.Manifest
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraX
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageAnalysisConfig
import com.example.cameraxapp.R
import com.github.florent37.runtimepermission.kotlin.askPermission
import kotlinx.android.synthetic.main.activity_image_analysis.*


class ImageAnalysisActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_analysis)

        askPermission(Manifest.permission.CAMERA) { startImageAnalysis() }
    }

    private fun startImageAnalysis() {
        // Setup image analysis pipeline that computes average pixel luminance
        val analyzerConfig = ImageAnalysisConfig.Builder().apply {
            // Use a worker thread for image analysis to prevent glitches
            val analyzerThread = HandlerThread("ImageyAnalysis").apply { start() }
            setCallbackHandler(Handler(analyzerThread.looper))
            // In our analysis, we care more about the latest image than analyzing *every* image
            setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
        }.build()

        // Build the image analysis use case and instantiate our analyzer
        val analyzerUseCase = ImageAnalysis(analyzerConfig).apply {
            setAnalyzer { imageProxy, rotationDegrees ->
                // CameraX produces images in YUV_422_888 format, convert them to bitmap
                val bitmap = imageProxy.image!!.convertToBitmap(this@ImageAnalysisActivity)
                    .rotate(rotationDegrees)
                    .blur(this@ImageAnalysisActivity)

                runOnUiThread { imageView.setImageBitmap(bitmap) }
            }
        }

        // Bind use cases to lifecycle. If Android Studio complains about "this" being not a LifecycleOwner
        // try rebuilding the project or updating the appcompat dependency to version 1.1.0 or higher.
        CameraX.bindToLifecycle(this, analyzerUseCase)
    }
}


