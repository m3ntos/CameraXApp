package com.example.cameraxapp.preview

import android.Manifest
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraX
import androidx.camera.core.PreviewConfig
import com.example.cameraxapp.R
import com.github.florent37.runtimepermission.kotlin.askPermission
import kotlinx.android.synthetic.main.activity_preview.*

class PreviewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview)

        askPermission(Manifest.permission.CAMERA) {
            textureView.post { startCamera() }
        }
    }

    private fun startCamera() {
        // Create configuration object for the viewfinder use case
        val previewConfig = PreviewConfig.Builder()
            .setLensFacing(CameraX.LensFacing.BACK)
            // .setTargetAspectRatio(Rational(1, 1))
            // .setTargetResolution(Size(640, 640))
            // .setTargetRotation(Surface.ROTATION_180)
            .build()

        // Build the viewfinder use case
        val preview = AutoFitPreviewBuilder.build(previewConfig, textureView)

//        val preview = Preview(previewConfig)
//        preview.setOnPreviewOutputUpdateListener {
//            Log.d("PREVIEW_OUTPUT", "texture size: ${it.textureSize}")
//            Log.d("PREVIEW_OUTPUT", "rotation: ${it.rotationDegrees}")
//
//            // To update the SurfaceTexture, we have to remove it and re-add it
//            val parent = textureView.parent as ViewGroup
//            parent.removeView(textureView)
//            parent.addView(textureView, 0)
//
//            // Update internal texture
//            textureView.surfaceTexture = it.surfaceTexture
//        }

        // Bind use cases to lifecycle. If Android Studio complains about "this" being not a LifecycleOwner
        // try rebuilding the project or updating the appcompat dependency to version 1.1.0 or higher.
        CameraX.bindToLifecycle(this, preview)
    }
}
