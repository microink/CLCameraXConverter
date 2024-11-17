# CLCameraXConverter
<h4 align="left"><a href="https://github.com/microink/CLCameraXConverter/blob/main/README.md">English </a> | 
<strong>简体中文</strong></h4>
<div>

[![License](https://img.shields.io/github/license/microink/CLCameraXConverter)](https://github.com/microink/CLCameraXConverter/blob/main/LICENSE)
[![GitHub release](https://img.shields.io/github/release/microink/CLCameraXConverter)](https://github.com/microink/CLCameraXConverter/releases)

</div>

&nbsp; 使用CameraX的ImageAnalysis进行图像分析的快速工具，一行代码搞定Bitmap复用避免内存抖动与频繁GC、处理CameraX输出图像角度与所见不同、自动裁剪成视野中结果。


### 支持特性

- ImageAnalysis输出的图像Bitmap复用，避免频繁GC与内存抖动。
- 根据传入角度自动旋转图像，这在后续识别步骤比如OCR、一维条码处理等步骤时是必要的。
- 根据预览效果裁剪图像，所见即所得，不再是相机输出的原始大图，可以缩短后续图像处理的时间。

### 接入

1. 兼容性（重要！）：
1.0.0版本兼容CameraX的1.4.0版本，由于使用了反射，更新CameraX版本前请务必测试确保使用正常！
```gradle
// build.gradle
dependencies {
    ...
    implementation "androidx.camera:camera-core:1.4.0"
}
```
&nbsp; &nbsp; 或者
```kotlin
// build.gradle.kts
dependencies {
    ...
    implementation("androidx.camera:camera-core:1.4.0")
}
```
2. 导入release中的aar到libs文件夹下。
3. 导入aar到工程gradle。
```gradle
dependencies {
	implementation files('libs/CLCameraXConverter-v1.0.0.aar')
}
```

4. 使用。

```kotlin
        clCameraXConverter.imageProxyToBitmapFormPool(imageProxy,
            binding.previewViewMain.rotation.toInt(),
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
&nbsp; &nbsp; 针对于MVVM架构的Demo可以参考app模块示例代码。
### 使用效果
1. 使用官方的ImageProxy.toBitmap的内存表现，使用Bitmap.createBitmap的方式进行裁剪和旋转。<br>
<picture>
 <img alt="screenshot" src="https://github.com/microink/CLCameraXConverter/blob/main/img/official_mem.png">
</picture>

2. 使用CLCameraXConverter的内存表现。<br>
<picture>
 <img alt="screenshot" src="https://github.com/microink/CLCameraXConverter/blob/main/img/CLCameraXConverter_mem.png">
</picture>