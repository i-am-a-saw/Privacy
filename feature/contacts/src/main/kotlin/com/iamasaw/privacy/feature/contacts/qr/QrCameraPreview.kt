package com.iamasaw.privacy.feature.contacts.qr

import androidx.camera.core.ImageAnalysis
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi

@OptIn(ExperimentalAtomicApi::class)
@Composable
internal fun QrCameraPreview(
    onPayload: (String) -> Unit,
    onFailure: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val controller = remember(context) { LifecycleCameraController(context) }
    val latestPayload = rememberUpdatedState(onPayload)
    val latestFailure = rememberUpdatedState(onFailure)

    DisposableEffect(controller, lifecycleOwner) {
        val executor = Executors.newSingleThreadExecutor()
        val mainExecutor = ContextCompat.getMainExecutor(context)
        val handled = AtomicBoolean(false)
        var disposed = false

        val scanner =
            BarcodeScanning.getClient(
                BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE).build(),
            )

        val fail = {
            if (!disposed && handled.compareAndSet(false, true)) {
                latestFailure.value()
            }
        }

        val analyzer =
            MlKitAnalyzer(
                listOf(scanner),
                ImageAnalysis.COORDINATE_SYSTEM_SENSOR,
                mainExecutor,
            ) { result ->
                if (!disposed) {
                    if (result.getThrowable(scanner) != null) {
                        fail()
                    } else {
                        val payload =
                            result.getValue(scanner).orEmpty().firstNotNullOfOrNull { it.rawValue }
                        if (payload != null && handled.compareAndSet(false, true)) {
                            latestPayload.value(payload)
                        }
                    }
                }
            }

        controller.setEnabledUseCases(CameraController.IMAGE_ANALYSIS)
        controller.setImageAnalysisBackpressureStrategy(
            ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST,
        )
        controller.setImageAnalysisAnalyzer(executor, analyzer)

        controller.initializationFuture.addListener(
            {
                if (!disposed) {
                    bindCamera(controller, lifecycleOwner, fail)
                }
            },
            mainExecutor,
        )

        onDispose {
            disposed = true
            controller.clearImageAnalysisAnalyzer()
            controller.unbind()
            scanner.close()
            executor.shutdown()
        }
    }

    AndroidView(modifier = modifier, factory = { viewContext ->
        PreviewView(viewContext).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            this.controller = controller
        }
    })
}

private fun bindCamera(
    controller: LifecycleCameraController,
    lifecycleOwner: LifecycleOwner,
    onFailure: () -> Unit,
) {
    try {
        controller.initializationFuture.get()
        controller.bindToLifecycle(lifecycleOwner)
    } catch (_: ExecutionException) {
        onFailure()
    } catch (_: InterruptedException) {
        Thread.currentThread().interrupt()
        onFailure()
    } catch (_: SecurityException) {
        onFailure()
    } catch (_: IllegalArgumentException) {
        onFailure()
    } catch (_: IllegalStateException) {
        onFailure()
    }
}
