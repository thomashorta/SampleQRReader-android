package me.thdev.sampleqrreader

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import me.thdev.mlkitreader.newMLKitAnalysisUseCase
import me.thdev.sampleqrreader.databinding.ActivityMainBinding

private const val CAMERA_PERMISSION_REQUEST_CODE = 1
private const val TAG = "SampleQRReader"

/**
 * CameraX preview setup based on:
 * https://beakutis.medium.com/using-googles-mlkit-and-camerax-for-lightweight-barcode-scanning-bb2038164cdc
 */
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (hasCameraPermission()) bindCameraUseCases() else requestPermission()
    }

    // checking to see whether user has already granted permission
    private fun hasCameraPermission() =
        ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

    private fun requestPermission() {
        // opening up dialog to ask for camera permission
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            // user granted permissions - we can set up our scanner
            bindCameraUseCases()
        } else {
            // user did not grant permissions - we can't use the camera
            Toast.makeText(
                this,
                "Camera permission required",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun bindCameraUseCases() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener(
            {
                val cameraProvider = cameraProviderFuture.get()

                // setting up the preview use case
                val previewUseCase = Preview.Builder()
                    .build()
                    .also {
                        it.setSurfaceProvider(binding.preview.surfaceProvider)
                    }

                // configure to use the back camera
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                try {
                    cameraProvider.bindToLifecycle(
                        this,
                        cameraSelector,
                        previewUseCase,
                        newMLKitAnalysisUseCase { codeValue ->
                            if (codeValue != null) {
                                binding.resultText.text =
                                    getString(R.string.qr_code_found, codeValue)
                            } else {
                                binding.resultText.text = getString(R.string.qr_code_not_found)
                            }
                        }
                    )
                } catch (illegalStateException: IllegalStateException) {
                    // If the use case has already been bound to another lifecycle or method is not
                    // called on main thread.
                    Log.e(TAG, illegalStateException.message.orEmpty())
                } catch (illegalArgumentException: IllegalArgumentException) {
                    // If the provided camera selector is unable to resolve a camera to be used for
                    // the given use cases.
                    Log.e(TAG, illegalArgumentException.message.orEmpty())
                }
            },
            ContextCompat.getMainExecutor(this)
        )
    }
}
