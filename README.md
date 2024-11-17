<!--
 * @Author: liuyan yan.liu@neox-inc.com
 * @Date: 2024-11-16 23:26:09
 * @LastEditors: liuyan yan.liu@neox-inc.com
 * @LastEditTime: 2024-11-17 16:50:28
 * @FilePath: \undefinedd:\Project\Android\Github\CLCameraXConverter\README.md
 * @Description: 这是默认设置,请设置`customMade`, 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
-->
# CLCameraXConverter
<h4 align="left"><strong>English</strong> | 
<a href="https://github.com/microink/CLCameraXConverter/blob/main/README_zh.md">简体中文</a></h4>
<div>

[![License](https://img.shields.io/github/license/microink/CLCameraXConverter)](https://github.com/microink/CLCameraXConverter/blob/main/LICENSE)
[![GitHub release](https://img.shields.io/github/release/microink/CLCameraXConverter)](https://github.com/microink/CLCameraXConverter/releases)

</div>

&nbsp; A quick tool for image analysis using CameraX's ImageAnalysis, featuring the abilities to avoid Bitmap memory churn, reduce frequent garbage collection (GC), handle rotation, and crop the field of view image, with a single method to obtain the Bitmap within the view.


### Feature

- Reuse the Bitmap output by ImageAnalysis to avoid frequent garbage collection (GC) and memory jitter.
- Automatically rotate the image based on the input angle, which is necessary for subsequent recognition steps such as OCR and one-dimensional barcode processing.
- Crop the image based on the preview effect for a what-you-see-is-what-you-get experience, no longer using the original large image output by the camera, which can shorten the subsequent image processing time.

### Usage

1. Compatibility (Important!): Version 1.0.0 is compatible with CameraX version 1.4.0. Due to the use of reflection, please be sure to test and ensure normal usage before updating the CameraX version!
```gradle
// build.gradle
dependencies {
    ...
    implementation "androidx.camera:camera-core:1.4.0"
}
```
&nbsp; &nbsp; or
```kotlin
// build.gradle.kts
dependencies {
    ...
    implementation("androidx.camera:camera-core:1.4.0")
}
```
2. Import the AAR file from the release into the "libs" folder.
3. Import the AAR file into the project's Gradle configuration.
```gradle
dependencies {
	implementation files('libs/CLCameraXConverter-v1.0.0.aar')
}
```

4. use it.

```kotlin
        clCameraXConverter.imageProxyToBitmapFormPool(imageProxy,
            rotationAngle,
            object : CLCameraXCallback{
            override fun onCLCameraXBitmap(clCameraXBitmap: CLCameraXBitmap) {
                // 1. get bitmap form CLCameraXBitmap
                val bitmap = clCameraXBitmap.getBitmap()
                bitmap?.run {
                    binding.ivMain.post {
                        // 2. use bitmap or do something...
                        binding.ivMain.setImageBitmap(this)
                        // 3. release old CLCameraXBitmap
                        oldCameraXBitmap?.run {
                            release()
                        }
                        oldCameraXBitmap = clCameraXBitmap
                    }
                }
            }

        })
```  
&nbsp; &nbsp; For a demo of the MVVM architecture, please refer to the sample code in the app module.
### Use Effect
1. The memory performance of using the official ImageProxy.toBitmap method, along with Bitmap.createBitmap for cropping and rotating.<br>
<picture>
 <img alt="screenshot" src="https://github.com/microink/CLCameraXConverter/blob/main/img/official_mem.png">
</picture>

2. The memory performance of using CLCameraXConverter.<br>
<picture>
 <img alt="screenshot" src="https://github.com/microink/CLCameraXConverter/blob/main/img/CLCameraXConverter_mem.png">
</picture>
