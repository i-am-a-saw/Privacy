package com.iamasaw.privacy.feature.contacts

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.iamasaw.privacy.core.designsystem.component.MessengerButton
import com.iamasaw.privacy.feature.contacts.qr.QrCameraPreview
import com.iamasaw.privacy.feature.contacts.qr.QrDecodeResult
import com.iamasaw.privacy.feature.contacts.qr.QrPayloadCodec

@Composable
fun ScanQrScreen(onUsername: (String) -> Unit) {
    val context = LocalContext.current
    val activity = LocalActivity.current

    fun hasPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA,
        ) == PackageManager.PERMISSION_GRANTED

    var granted by remember { mutableStateOf(hasPermission()) }
    var requested by rememberSaveable { mutableStateOf(false) }
    var scanError by rememberSaveable { mutableStateOf<Int?>(null) }

    val launcher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { allowed ->
            requested = true
            granted = allowed
        }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        granted = hasPermission()
    }

    val showRationale =
        activity?.let {
            ActivityCompat.shouldShowRequestPermissionRationale(
                it,
                Manifest.permission.CAMERA,
            )
        } == true

    val openSettings = requested && !showRationale && !granted

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            stringResource(R.string.contacts_scan_qr),
            style = MaterialTheme.typography.headlineMedium,
        )

        when {
            !granted -> {
                CameraPermissionContent(showRationale = showRationale, openSettings = openSettings, onContinue = {
                    if (openSettings) {
                        context.startActivity(
                            Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                "package:${context.packageName}".toUri(),
                            ),
                        )
                    } else {
                        launcher.launch(Manifest.permission.CAMERA)
                    }
                })
            }

            scanError != null -> {
                Text(
                    stringResource(requireNotNull(scanError)),
                    color = MaterialTheme.colorScheme.error,
                )
                MessengerButton(
                    text = stringResource(R.string.contacts_retry),
                    onClick = { scanError = null },
                )
            }

            else -> {
                Text(stringResource(R.string.contacts_scan_explanation))

                QrCameraPreview(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .weight(1f),
                    onFailure = {
                        scanError = R.string.contacts_camera_error
                    },
                    onPayload = { payload ->
                        when (val result = QrPayloadCodec.decode(payload)) {
                            is QrDecodeResult.Success -> {
                                onUsername(result.username)
                            }

                            QrDecodeResult.Invalid -> {
                                scanError = R.string.contacts_qr_invalid
                            }

                            QrDecodeResult.UnsupportedVersion -> {
                                scanError = R.string.contacts_qr_future_version
                            }
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun CameraPermissionContent(
    showRationale: Boolean,
    openSettings: Boolean,
    onContinue: () -> Unit,
) {
    Text(
        stringResource(
            when {
                openSettings -> R.string.contacts_camera_settings_explanation
                showRationale -> R.string.contacts_camera_rationale
                else -> R.string.contacts_camera_explanation
            },
        ),
    )

    MessengerButton(
        text =
            stringResource(
                if (openSettings) {
                    R.string.contacts_camera_settings
                } else {
                    R.string.contacts_camera_allow
                },
            ),
        onClick = onContinue,
    )
}
