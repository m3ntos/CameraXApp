package com.example.cameraxapp.camera_view

import android.Manifest.permission.*
import android.annotation.SuppressLint
import android.media.MediaScannerConnection
import android.os.Bundle
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.ImageCapture
import androidx.camera.core.VideoCapture
import com.example.cameraxapp.R
import com.github.florent37.runtimepermission.kotlin.askPermission
import kotlinx.android.synthetic.main.activity_camera_view.*
import java.io.File

class CameraViewActivity : AppCompatActivity() {

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera_view)

        askPermission(CAMERA, WRITE_EXTERNAL_STORAGE, RECORD_AUDIO) {
            cameraView.bindToLifecycle(this)
        }
        btnToggleCamera.setOnClickListener { cameraView.toggleCamera() }
        btnToggleTorch.setOnClickListener { cameraView.enableTorch(!cameraView.isTorchOn) }
        btnTakePhoto.setOnClickListener { takePhoto() }
        btnStartRecording.setOnClickListener { startRecordingVideo() }
        btnStopRecording.setOnClickListener { cameraView.stopRecording() }
    }

    private fun takePhoto() {
        val file = File(externalMediaDirs.first(), "${System.currentTimeMillis()}.jpg")
        cameraView.takePicture(file, object : ImageCapture.OnImageSavedListener {

            override fun onError(
                imageCaptureError: ImageCapture.ImageCaptureError,
                message: String,
                cause: Throwable?
            ) {
                Toast.makeText(baseContext, "photo fail: $message", Toast.LENGTH_SHORT).show()
                cause?.printStackTrace()
            }

            override fun onImageSaved(file: File) {
                Toast.makeText(baseContext, "photo success: ${file.absolutePath}", Toast.LENGTH_SHORT).show()
                makeFileVisibleInGalleryApp(file)
            }
        })
    }

    private fun startRecordingVideo() {
        val file = File(externalMediaDirs.first(), "${System.currentTimeMillis()}.mp4")
        cameraView.startRecording(file, object : VideoCapture.OnVideoSavedListener {

            override fun onError(
                videoCaptureError: VideoCapture.VideoCaptureError,
                message: String,
                cause: Throwable?
            ) {
                Toast.makeText(baseContext, "video fail: $message", Toast.LENGTH_SHORT).show()
                cause?.printStackTrace()
            }

            override fun onVideoSaved(file: File) {
                Toast.makeText(baseContext, "video saved: ${file.absolutePath}", Toast.LENGTH_SHORT).show()
                makeFileVisibleInGalleryApp(file)
            }
        })
    }

    private fun makeFileVisibleInGalleryApp(photoFile: File) {
        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(photoFile.extension)
        MediaScannerConnection.scanFile(this, arrayOf(photoFile.absolutePath), arrayOf(mimeType), null)
    }
}
