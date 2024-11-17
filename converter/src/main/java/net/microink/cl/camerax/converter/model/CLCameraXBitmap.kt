package net.microink.cl.camerax.converter.model

import android.graphics.Bitmap
import net.microink.cl.camerax.converter.callback.CLBitmapReleaseCallback

/**
 * CameraX Camera Bitmap Encapsulation Bean
 */
class CLCameraXBitmap(private var bitmap: Bitmap?,
                      private var releaseCallback: CLBitmapReleaseCallback
) {

    /**
     * get Bitmap
     */
    fun getBitmap(): Bitmap? {
        return bitmap
    }

    /**
     * release bitmap
     */
    fun release() {
        releaseCallback.releaseBitmap(bitmap)
        bitmap = null
    }
}
