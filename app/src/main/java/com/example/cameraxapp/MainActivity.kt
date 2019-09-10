package com.example.cameraxapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.cameraxapp.camera_view.CameraViewActivity
import com.example.cameraxapp.image_analysis.ImageAnalysisActivity
import com.example.cameraxapp.image_capture.ImageCaptureActivity
import com.example.cameraxapp.image_from_intent.ImageFromIntentActivity
import com.example.cameraxapp.preview.PreviewActivity
import com.example.cameraxapp.vendor_extensions.VendorExtensionsActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnImageFromIntent.setOnClickListener { startActivity(Intent(this, ImageFromIntentActivity::class.java)) }
        btnPreview.setOnClickListener { startActivity(Intent(this, PreviewActivity::class.java)) }
        btnImageCapture.setOnClickListener { startActivity(Intent(this, ImageCaptureActivity::class.java)) }
        btnImageAnalysis.setOnClickListener { startActivity(Intent(this, ImageAnalysisActivity::class.java)) }
        btnVendorExtensions.setOnClickListener { startActivity(Intent(this, VendorExtensionsActivity::class.java)) }
        btnCameraView.setOnClickListener { startActivity(Intent(this, CameraViewActivity::class.java)) }
    }
}
