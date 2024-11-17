package net.microink.cl.camerax.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.UseCaseGroup
import androidx.camera.core.ViewPort
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import net.microink.cl.camerax.app.databinding.ActivityMainBinding
import net.microink.cl.camerax.converter.CLCameraXConverter
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
        // camera permission code
        private const val PERMISSION_CAMERA_CODE: Int = 10001
    }

    private lateinit var dataBinding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var cameraProviderFuture : ListenableFuture<ProcessCameraProvider>

    private lateinit var clCameraXConverter: CLCameraXConverter // CameraX图像转换工具

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        initVM()
        initView()
    }

    private fun initVM() {
        dataBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        dataBinding.lifecycleOwner = this
        dataBinding.viewModel = viewModel
    }

    private fun initView() {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // check permission
        checkPermission()
    }

    /**
     * check camera permission
     */
    private fun checkPermission() {
        val permissions = arrayOf(Manifest.permission.CAMERA)
        val cameraPermissionOpen =
            PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(
                this,
                permissions[0]
            )
        if (!cameraPermissionOpen) {
            ActivityCompat.requestPermissions(
                this,
                permissions,
                PERMISSION_CAMERA_CODE
            )
        } else {
            dataBinding.previewViewMain.post {
                initCameraX()
            }
        }
    }

    /**
     * init camerax
     */
    private fun initCameraX() {
        clCameraXConverter = CLCameraXConverter(this.lifecycle)
        viewModel.setCameraXConverter(clCameraXConverter)
        viewModel.data.observe(this) { data ->
            val newCameraXBitmap = data.newCLCameraXBitmap
            val oldCameraXBitmap = data.oldCLCameraXBitmap

            newCameraXBitmap?.run {
                val bitmap = getBitmap()
                bitmap?.run {
                    // use bitmap do some thing ... ... ...
                    // It means that the old data is no longer used
                }
            }

            viewModel.releaseCameraXBitmap(newCameraXBitmap, oldCameraXBitmap)
        }

        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            bindPreview(cameraProvider)
        }, ContextCompat.getMainExecutor(this))
    }

    /**
     * setup camerax preview and analysis
     */
    private fun bindPreview(cameraProvider : ProcessCameraProvider) {
        val preview : Preview = Preview.Builder()
            .build()

        val cameraSelector : CameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()
        val resolutionSelector = ResolutionSelector.Builder()
            .setResolutionStrategy(
                ResolutionStrategy(
                    Size(1920, 1080),
                    ResolutionStrategy.FALLBACK_RULE_CLOSEST_LOWER)
            )
            .build()
        val imageAnalysis = ImageAnalysis.Builder()
            .setResolutionSelector(resolutionSelector)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
        imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->

            viewModel.handleImageProxy(imageProxy, dataBinding.previewViewMain.rotation.toInt())
        }

        preview.surfaceProvider = dataBinding.previewViewMain.getSurfaceProvider()
        val viewPort: ViewPort? = dataBinding.previewViewMain.getViewPort()
        if (null == viewPort) {
            Log.e(TAG, "open camerax failed")
            return
        }

        val useCaseGroup = UseCaseGroup.Builder()
            .addUseCase(preview)
            .addUseCase(imageAnalysis)
            .setViewPort(viewPort)
            .build()

        val camera = cameraProvider.bindToLifecycle(this as LifecycleOwner, cameraSelector,
            useCaseGroup)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (PERMISSION_CAMERA_CODE == requestCode) {
            val cameraPermissionOpen =
                PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(
                    this,
                    permissions[0]
                )
            if (!cameraPermissionOpen) {
                ActivityCompat.requestPermissions(
                    this,
                    permissions,
                    PERMISSION_CAMERA_CODE
                )
            } else {
                dataBinding.previewViewMain.post {
                    initCameraX()
                }
            }
        }
    }
}