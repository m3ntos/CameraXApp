package com.example.cameraxapp.image_from_intent

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import androidx.exifinterface.media.ExifInterface.*


/**
 * Decodes sampled bitmap. Use it to avoid out of memory errors while opening big bitmaps.
 * Additionally rotates bitmap if needed according to its exif data.
 *
 * The bitmap will be the sampled by the biggest possible sample size that keeps both height and width
 * larger than the requested height and width.
 *
 * Note: The sample size is the number of pixels in either dimension that correspond to a single pixel
 * in the decoded bitmap. For example, inSampleSize == 4 returns an image that is 1/4 the width/height
 * of the original, and 1/16 the number of pixels. The sample size must be a power of 2.
 *
 * Summary: bitmap will be larger size than reqWidth and reqHeight, but still smaller than original.
 */
fun sampleAndRotateBitmap(appContext: Context, imageUri: Uri, reqWidth: Int, reqHeight: Int): Bitmap {
    val getImageInputStream = { appContext.contentResolver.openInputStream(imageUri) }

    // First get orientation
    val ei = getImageInputStream().use { ExifInterface(it!!) }
    val orientation = ei.getAttributeInt(TAG_ORIENTATION, ORIENTATION_NORMAL)
    val isSideways = (orientation == ORIENTATION_ROTATE_90 || orientation == ORIENTATION_ROTATE_270)

    // Then decode with inJustDecodeBounds=true to check dimensions
    val options = BitmapFactory.Options()
    options.inJustDecodeBounds = true
    getImageInputStream().use { BitmapFactory.decodeStream(it, null, options) }

    // Calculate inSampleSize
    if (isSideways) {
        options.inSampleSize = calculateInSampleSize(options, reqHeight, reqWidth)
    } else {
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
    }

    // Decode bitmap with inSampleSize set
    options.inJustDecodeBounds = false
    val sampledBitmap = getImageInputStream().use { BitmapFactory.decodeStream(it, null, options)!! }

    //Rotate bitmap
    return when (orientation) {
        ORIENTATION_ROTATE_90 -> rotateImage(sampledBitmap, 90)
        ORIENTATION_ROTATE_180 -> rotateImage(sampledBitmap, 180)
        ORIENTATION_ROTATE_270 -> rotateImage(sampledBitmap, 270)
        else -> sampledBitmap
    }
}

private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
    // Raw height and width of image
    val (height: Int, width: Int) = options.run { outHeight to outWidth }
    var inSampleSize = 1

    if (height > reqHeight || width > reqWidth) {
        val halfHeight: Int = height / 2
        val halfWidth: Int = width / 2

        // Calculate the largest inSampleSize value that is a power of 2 and keeps both
        // height and width larger than the requested height and width.
        while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
            inSampleSize *= 2
        }
    }
    return inSampleSize
}

private fun rotateImage(img: Bitmap, degree: Int): Bitmap {
    val matrix = Matrix()
    matrix.postRotate(degree.toFloat())
    val rotatedImg = Bitmap.createBitmap(img, 0, 0, img.width, img.height, matrix, true)
    img.recycle()
    return rotatedImg
}
