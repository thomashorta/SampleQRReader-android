package me.thdev.mlkitreader

import android.annotation.SuppressLint
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors.newSingleThreadExecutor

private const val TAG = "MLKitAnalyzer"

/**
 * Based on:
 * https://beakutis.medium.com/using-googles-mlkit-and-camerax-for-lightweight-barcode-scanning-bb2038164cdc
 */
internal class MLKitAnalyzer(
    val onAnalysisUpdate: (String?) -> Unit
) : ImageAnalysis.Analyzer {
    private val options = BarcodeScannerOptions.Builder().setBarcodeFormats(
        Barcode.FORMAT_QR_CODE,
        Barcode.FORMAT_AZTEC,
    ).build()

    private val scanner = BarcodeScanning.getClient(options)

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        imageProxy.image?.let { image ->
            val inputImage =
                InputImage.fromMediaImage(
                    image,
                    imageProxy.imageInfo.rotationDegrees
                )

            scanner.process(inputImage)
                .addOnSuccessListener { barcodeList ->
                    val qrCode = barcodeList.getOrNull(0)
                    onAnalysisUpdate(qrCode?.rawValue)
                }
                .addOnFailureListener {
                    Log.e(TAG, it.message.orEmpty())
                }.addOnCompleteListener {
                    imageProxy.image?.close()
                    imageProxy.close()
                }
        }
    }
}

fun newMLKitAnalysisUseCase(
    onAnalysisUpdate: (String?) -> Unit
) = ImageAnalysis.Builder()
    .setBackpressureStrategy(STRATEGY_KEEP_ONLY_LATEST)
    .build()
    .also {
        it.setAnalyzer(newSingleThreadExecutor(), MLKitAnalyzer(onAnalysisUpdate))
    }
