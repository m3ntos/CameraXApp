package com.example.cameraxapp.image_analysis

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.Image
import android.renderscript.*


/**
 * Converts image in YUV_422_888 format to a bitmap.
 * Taken from [this stack overflow answer](https://stackoverflow.com/a/55544614)
 */
fun Image.convertToBitmap(context: Context): Bitmap {
    // Get the YUV data
    val yuvBytes = toYuvN21Bytes(this)

    // Convert YUV to RGB
    val rs = RenderScript.create(context)

    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val allocationRgb = Allocation.createFromBitmap(rs, bitmap)

    val allocationYuv = Allocation.createSized(rs, Element.U8(rs), yuvBytes.size)
    allocationYuv.copyFrom(yuvBytes)

    val scriptYuvToRgb = ScriptIntrinsicYuvToRGB.create(rs, Element.U8_4(rs))
    scriptYuvToRgb.setInput(allocationYuv)
    scriptYuvToRgb.forEach(allocationRgb)

    allocationRgb.copyTo(bitmap)

    // Release
    allocationYuv.destroy()
    allocationRgb.destroy()
    rs.destroy()

    return bitmap
}

private fun toYuvN21Bytes(image: Image): ByteArray {
    val yBuffer = image.planes[0].buffer
    val uBuffer = image.planes[1].buffer
    val vBuffer = image.planes[2].buffer

    val ySize = yBuffer.remaining()
    val uSize = uBuffer.remaining()
    val vSize = vBuffer.remaining()

    val nv21Bytes = ByteArray(ySize + uSize + vSize)

    //U and V are swapped
    yBuffer.get(nv21Bytes, 0, ySize)
    vBuffer.get(nv21Bytes, ySize, vSize)
    uBuffer.get(nv21Bytes, ySize + vSize, uSize)

    return nv21Bytes
}

fun Bitmap.blur(context: Context): Bitmap {
    val rs = RenderScript.create(context)
    val theIntrinsic = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
    val tmpIn = Allocation.createFromBitmap(rs, this)
    val tmpOut = Allocation.createFromBitmap(rs, this)
    theIntrinsic.setRadius(25f)
    theIntrinsic.setInput(tmpIn)
    theIntrinsic.forEach(tmpOut)
    tmpOut.copyTo(this)
    return this
}

fun Bitmap.rotate(angle: Int): Bitmap {
    val matrix = Matrix()
    matrix.postRotate(angle.toFloat())
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}
