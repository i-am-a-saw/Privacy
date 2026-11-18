package com.iamasaw.privacy.feature.contacts.qr

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import com.iamasaw.privacy.core.common.dispatcher.Dispatcher
import com.iamasaw.privacy.core.common.dispatcher.MessengerDispatchers
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val QR_SIZE = 512

class QrRenderer
    @Inject
    constructor(
        @Dispatcher(MessengerDispatchers.DEFAULT) private val dispatcher: CoroutineDispatcher,
    ) {
        suspend fun render(username: String): Bitmap? =
            withContext(dispatcher) {
                try {
                    val matrix =
                        QRCodeWriter().encode(
                            QrPayloadCodec.encode(username),
                            BarcodeFormat.QR_CODE,
                            QR_SIZE,
                            QR_SIZE,
                        )

                    val pixels =
                        IntArray(QR_SIZE * QR_SIZE) { index ->
                            val x = index % QR_SIZE
                            val y = index / QR_SIZE
                            if (matrix[x, y]) Color.BLACK else Color.WHITE
                        }
                    Bitmap.createBitmap(
                        pixels,
                        QR_SIZE,
                        QR_SIZE,
                        Bitmap.Config.ARGB_8888,
                    )
                } catch (_: WriterException) {
                    null
                } catch (_: IllegalArgumentException) {
                    null
                }
            }
    }
