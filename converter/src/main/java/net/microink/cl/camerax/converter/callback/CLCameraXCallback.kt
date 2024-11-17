package net.microink.cl.camerax.converter.callback

import net.microink.cl.camerax.converter.model.CLCameraXBitmap

/**
 * CameraX data acquisition callback
 */
interface CLCameraXCallback {

    /**
     * Camera Bitmap acquisition callback
     */
    fun onCLCameraXBitmap(clCameraXBitmap: CLCameraXBitmap)
}