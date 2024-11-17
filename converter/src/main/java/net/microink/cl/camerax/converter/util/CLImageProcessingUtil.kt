package net.microink.cl.camerax.converter.util

import android.graphics.Bitmap
import android.graphics.ImageFormat
import androidx.camera.core.ImageProcessingUtil
import androidx.camera.core.ImageProxy
import java.nio.ByteBuffer

class CLImageProcessingUtil {

    companion object {

        @JvmStatic
        fun convertYUVToBitmap(imageProxy: ImageProxy, bitmap: Bitmap) {
            require(imageProxy.format == ImageFormat.YUV_420_888) {
                "Input image format must be YUV_420_888"
            }

            val imageWidth = imageProxy.width
            val imageHeight = imageProxy.height
            val srcStrideY = imageProxy.planes[0].rowStride
            val srcStrideU = imageProxy.planes[1].rowStride
            val srcStrideV = imageProxy.planes[2].rowStride
            val srcPixelStrideY = imageProxy.planes[0].pixelStride
            val srcPixelStrideUV = imageProxy.planes[1].pixelStride

            val bitmapStride = bitmap.rowBytes
            var result: Int = -1

            val targetClass: Class<*> = ImageProcessingUtil::class.java
            val privateMethod = targetClass.getDeclaredMethod(
                "nativeConvertAndroid420ToBitmap",
                ByteBuffer::class.java, Int::class.java,
                ByteBuffer::class.java, Int::class.java,
                ByteBuffer::class.java, Int::class.java,
                Int::class.java, Int::class.java,
                Bitmap::class.java,
                Int::class.java, Int::class.java, Int::class.java)

            privateMethod.isAccessible = true

            result = privateMethod.invoke(null, imageProxy.planes[0].buffer,
                srcStrideY,
                imageProxy.planes[1].buffer,
                srcStrideU,
                imageProxy.planes[2].buffer,
                srcStrideV,
                srcPixelStrideY,
                srcPixelStrideUV,
                bitmap,
                bitmapStride,
                imageWidth,
                imageHeight) as Int
            if (result != 0) {
                throw UnsupportedOperationException("YUV to RGB conversion failed")
            }
        }
    }
}