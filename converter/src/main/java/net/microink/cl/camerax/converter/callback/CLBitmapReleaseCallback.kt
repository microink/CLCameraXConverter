package net.microink.cl.camerax.converter.callback

import android.graphics.Bitmap

/**
 * CLCameraXBitmap instance, active release callback
 */
interface CLBitmapReleaseCallback {

    fun releaseBitmap(bitmap: Bitmap?)
}