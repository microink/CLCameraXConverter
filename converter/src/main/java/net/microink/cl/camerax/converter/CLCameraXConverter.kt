package net.microink.cl.camerax.converter

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.RectF
import android.media.Image
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import androidx.collection.ArrayMap
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import net.microink.cl.camerax.converter.callback.CLBitmapReleaseCallback
import net.microink.cl.camerax.converter.callback.CLCameraXCallback
import net.microink.cl.camerax.converter.model.CLCameraXBitmap
import net.microink.cl.camerax.converter.util.CLImageProcessingUtil
import java.lang.reflect.InvocationTargetException
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.abs


/**
 * Converter for transforming ImageProxy to Bitmap in CameraX,
 * utilizing Bitmap caching for reuse to reduce memory recycling and jitter.
 */
class CLCameraXConverter(private var lifecycle: Lifecycle?): DefaultLifecycleObserver {

    companion object {
        private const val TAG = "CLCameraXConverter"

        // HandlerThread thread name
        private const val HANDLER_THREAD_NAME = "CLCameraXThread";
    }

    private val bitmapPool: ArrayMap<String, CopyOnWriteArrayList<Bitmap>> = ArrayMap() // Bitmap缓存池
    private var handlerThread: HandlerThread? = null
    private var backgroundHandler: Handler? = null

    init {
        lifecycle?.addObserver(this)
        handlerThread = HandlerThread(HANDLER_THREAD_NAME);
        handlerThread?.start();

        handlerThread?.run {
            backgroundHandler = object : Handler(looper) {
                override fun handleMessage(msg: Message) {
                }
            }
        }
    }


    /**
     * main method
     */
    @OptIn(ExperimentalGetImage::class)
    @Throws(IllegalAccessException::class, IllegalArgumentException::class,
        InvocationTargetException::class, UnsupportedOperationException::class)
    fun imageProxyToBitmapFormPool(imageProxy: ImageProxy, rotateAngle: Int,
                                   callback: CLCameraXCallback) {
        if (0 >= imageProxy.width || 0 >= imageProxy.height) {
            imageProxy.close()
            return
        }
        val rect: Rect = imageProxy.cropRect
        if (0 >= rect.width() || 0 >= rect.height()) {
            imageProxy.close()
            return
        }
        val realImage: Image? = imageProxy.image
        if (null == realImage) {
            imageProxy.close()
            return
        }

        backgroundHandler?.post {
            val fullBitmap = getBitmap(imageProxy.width, imageProxy.height)
            convertFullBitmap(imageProxy, fullBitmap)

            // cut bitmap
            val cutBitmap = getBitmap(rect.width(), rect.height())
            cutBitmap(fullBitmap, cutBitmap, rect)
            putBitmapToPool(fullBitmap)

            // rotate bitmap
            val rotateBitmap = rotateBitmap(cutBitmap, rotateAngle)
            putBitmapToPool(cutBitmap)

            val clCameraXBitmap = CLCameraXBitmap(rotateBitmap, object : CLBitmapReleaseCallback {
                override fun releaseBitmap(bitmap: Bitmap?) {
                    if (null == bitmap) {
                        return
                    }
                    backgroundHandler?.post {
                        val key = getKey(bitmap.width, bitmap.height)
                        var bitmapListInPool = bitmapPool[key]
                        if (null == bitmapListInPool) {
                            bitmapListInPool = CopyOnWriteArrayList()
                        }
                        bitmapListInPool.add(bitmap)
                        bitmapPool[key] = bitmapListInPool
                    }
                }

            })
            callback.onCLCameraXBitmap(clCameraXBitmap)
            imageProxy.close()
        }
    }

    /**
     * convert full bitmap
     */
    private fun convertFullBitmap(imageProxy: ImageProxy, bitmap: Bitmap) {
        CLImageProcessingUtil.convertYUVToBitmap(imageProxy, bitmap)
    }

    /**
     * cut Bitmap
     */
    private fun cutBitmap(fullBitmap: Bitmap, bitmap: Bitmap, rect: Rect) {
        val canvas = Canvas(bitmap)
        canvas.drawBitmap(fullBitmap, rect, Rect(0, 0, rect.width(), rect.height()),
            null)
        canvas.setBitmap(null)
    }

    /**
     * rotate Bitmap
     */
    private fun rotateBitmap(bitmap: Bitmap, rotateAngle: Int): Bitmap {
        val resultBitmapWidth: Int
        val resultBitmapHeight: Int
        if (90 == abs(rotateAngle % 180)) {
            resultBitmapWidth = bitmap.height
            resultBitmapHeight = bitmap.width
        } else {
            resultBitmapWidth = bitmap.width
            resultBitmapHeight = bitmap.height
        }

        val rotateBitmap = getBitmap(resultBitmapWidth, resultBitmapHeight)

        val matrix = Matrix()
        matrix.postRotate(rotateAngle.toFloat())
        val srcR = Rect(0, 0, bitmap.width, bitmap.height)
        val dstR = RectF(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat())
        val deviceR = RectF()
        matrix.mapRect(deviceR, dstR)
        val canvas = Canvas(rotateBitmap)
        canvas.translate(-deviceR.left, -deviceR.top)
        canvas.concat(matrix)
        canvas.drawBitmap(bitmap, srcR, dstR, null)
        canvas.setBitmap(null)

        return rotateBitmap
    }

    /**
     * put bitmap to pool
     */
    private fun putBitmapToPool(bitmap: Bitmap) {
        val key = getKey(bitmap.width, bitmap.height)
        var bitmapListInPool = bitmapPool[key]
        if (null == bitmapListInPool) {
            bitmapListInPool = CopyOnWriteArrayList()
        }
        bitmapListInPool.add(bitmap)
        bitmapPool[key] = bitmapListInPool
    }

    /**
     * getBitmap from pool or create it
     */
    private fun getBitmap(width: Int, height: Int): Bitmap {
        val key = getKey(width, height)
        val fullBitmap: Bitmap
        val bitmapList = bitmapPool[key]
        if (bitmapList.isNullOrEmpty()) {
            fullBitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888)
        } else {
            fullBitmap = bitmapList.removeAt(0)
        }
        return fullBitmap
    }

    /**
     * key
     */
    private fun getKey(width: Int, height: Int) = "$width|$height"

    /**
     * clear
     */
    override fun onDestroy(owner: LifecycleOwner) {
        stopBackgroundThread()
        lifecycle = null
    }

    /**
     * stop handler thread
     */
    private fun stopBackgroundThread() {
        backgroundHandler?.post {
            backgroundHandler?.removeCallbacksAndMessages(null)
            bitmapPool.clear()
            handlerThread = null
            backgroundHandler = null
        }
    }
}