package me.thdev.zxingreader

import android.graphics.ImageFormat.YUV_420_888
import android.graphics.ImageFormat.YUV_422_888
import android.graphics.ImageFormat.YUV_444_888
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.BinaryBitmap
import com.google.zxing.ChecksumException
import com.google.zxing.FormatException
import com.google.zxing.NotFoundException
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.multi.qrcode.QRCodeMultiReader
import java.util.concurrent.Executors

private const val TAG = "ZXingAnalyzer"

/**
 * Based on:
 * https://learntodroid.com/how-to-create-a-qr-code-scanner-app-in-android/
 */
internal class ZXingAnalyzer(
    val onAnalysisUpdate: (String?) -> Unit
) : ImageAnalysis.Analyzer {
    private val mainHandler = Handler(Looper.getMainLooper())

    override fun analyze(imageProxy: ImageProxy) {
        imageProxy.use { image ->
            if (
                image.format == YUV_420_888 ||
                image.format == YUV_422_888 ||
                image.format == YUV_444_888
            ) {
                val byteBuffer = image.planes[0].buffer
                val imageData = ByteArray(byteBuffer.capacity())
                byteBuffer[imageData]
                val source = PlanarYUVLuminanceSource(
                    imageData,
                    image.width, image.height,
                    0, 0,
                    image.width, image.height,
                    false
                )
                val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
                val qrValue = try {
                    QRCodeMultiReader().decode(binaryBitmap).text
                } catch (e: Exception) {
                    when (e) {
                        is FormatException,
                        is ChecksumException,
                        is NotFoundException -> {
                            // ZXing "failure" responses
                            Log.v(TAG, e.message.orEmpty())
                            null
                        }
                        else -> {
                            // Some other issue
                            throw e
                        }
                    }
                }

                mainHandler.post {
                    onAnalysisUpdate(qrValue)
                }
            }
        }
    }
}

fun newZXingAnalysisUseCase(
    onAnalysisUpdate: (String?) -> Unit
) = ImageAnalysis.Builder()
    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
    .build()
    .also {
        it.setAnalyzer(Executors.newSingleThreadExecutor(), ZXingAnalyzer(onAnalysisUpdate))
    }
