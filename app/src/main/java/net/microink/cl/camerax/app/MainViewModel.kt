package net.microink.cl.camerax.app

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import net.microink.cl.camerax.converter.CLCameraXConverter
import net.microink.cl.camerax.converter.callback.CLCameraXCallback
import net.microink.cl.camerax.converter.model.CLCameraXBitmap

/**
 * main ViewModel
 */
class MainViewModel: ViewModel() {

    private var clCameraXConverter: CLCameraXConverter? = null
    private var oldCameraXBitmap: CLCameraXBitmap? = null

    private val _data = MutableLiveData<Data>()

    val data: LiveData<Data> = _data

    /**
     * set CameraXConverter
     */
    fun setCameraXConverter(clCameraXConverter: CLCameraXConverter) {
        this.clCameraXConverter = clCameraXConverter
    }

    /**
     * release old data
     */
    fun releaseCameraXBitmap(cameraXBitmap: CLCameraXBitmap?, oldCLCameraXBitmap: CLCameraXBitmap?) {
        oldCLCameraXBitmap?.release()
        this.oldCameraXBitmap = cameraXBitmap
    }

    /**
     * main method
     */
    @OptIn(ExperimentalGetImage::class)
    fun handleImageProxy(imageProxy: ImageProxy, rotation: Int) {

        clCameraXConverter?.imageProxyToBitmapFormPool(imageProxy,
            rotation,
            object : CLCameraXCallback {
                override fun onCLCameraXBitmap(clCameraXBitmap: CLCameraXBitmap) {
                    _data.postValue(Data(clCameraXBitmap, oldCameraXBitmap))
                }

            })

        // use official getBitmap
//        var bitmap = imageProxy.toBitmap()
//        binding.previewViewMain.post {
//            binding.ivMain.setImageBitmap(bitmap)
//        }
//
//        imageProxy.close()
    }

    override fun onCleared() {
        super.onCleared()

        clCameraXConverter = null
    }
}