package com.example.cameraxapp.image_from_intent

import android.content.Intent
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.example.cameraxapp.R
import com.github.florent37.runtimepermission.kotlin.askPermission
import kotlinx.android.synthetic.main.activity_image_from_intent.*
import java.io.File

class ImageFromIntentActivity : AppCompatActivity() {

    companion object {
        const val AVATAR_SIZE = 512
        const val REQUEST_GALLERY = 1
        const val REQUEST_CAMERA = 2
    }

    private var imageUri: Uri? = null

    private val uriToSaveCameraResultInto by lazy {
        val photoFile = File(cacheDir, "cameraImageResultFile")
        FileProvider.getUriForFile(this, "$packageName.fileprovider", photoFile)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_from_intent)
        btnTakePicture.setOnClickListener { startCameraForPicture() }
        btnChooseFromGallery.setOnClickListener { startGalleryForPicture() }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable("imageUri", imageUri)
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        imageUri = savedInstanceState.getParcelable("imageUri")
        imageUri?.let { onAvatarImageChosen(it) }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_GALLERY -> data?.data?.let { uri -> onAvatarImageChosen(uri) }
                REQUEST_CAMERA -> onAvatarImageChosen(uriToSaveCameraResultInto)
            }
        }
    }

    private fun onAvatarImageChosen(imageUri: Uri) {
        this.imageUri = imageUri
        val imageBitmap = sampleAndRotateBitmap(applicationContext, imageUri, AVATAR_SIZE, AVATAR_SIZE)
        val thumbnail = ThumbnailUtils.extractThumbnail(imageBitmap, AVATAR_SIZE, AVATAR_SIZE)

        imageView.setImageBitmap(imageBitmap)
    }

    private fun startCameraForPicture() {
        askPermission(android.Manifest.permission.CAMERA) {
            Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { intent ->
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uriToSaveCameraResultInto)
                intent.resolveActivity(packageManager)?.also {
                    startActivityForResult(intent, REQUEST_CAMERA)
                } ?: Toast.makeText(this, "no camera app found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startGalleryForPicture() {
        askPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) {
            Intent(Intent.ACTION_GET_CONTENT).also { intent ->
                intent.type = "image/*"
                intent.resolveActivity(packageManager)?.also {
                    startActivityForResult(intent, REQUEST_GALLERY)
                } ?: Toast.makeText(this, "no gallery app found", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
