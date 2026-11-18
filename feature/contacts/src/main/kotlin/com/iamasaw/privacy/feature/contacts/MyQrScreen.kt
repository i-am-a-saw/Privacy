package com.iamasaw.privacy.feature.contacts

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iamasaw.privacy.core.designsystem.component.MessengerButton

@Composable
fun MyQrRoute(viewModel: MyQrViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    MyQrScreen(state = state, onRetry = viewModel::retry)
}

@Composable
fun MyQrScreen(
    state: MyQrUiState,
    onRetry: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            stringResource(R.string.contacts_my_qr),
            style = MaterialTheme.typography.headlineMedium,
        )

        if (state.loading) {
            Text(stringResource(R.string.contacts_loading))
        }

        state.error?.let {
            Text(
                contactErrorText(it),
                color = MaterialTheme.colorScheme.error,
            )
            MessengerButton(
                text = stringResource(R.string.contacts_retry),
                onClick = onRetry,
            )
        }

        state.bitmap?.let { bitmap ->
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription =
                    stringResource(
                        R.string.contacts_qr_description,
                        state.username,
                    ),
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
            )

            Text("@${state.username}")
            Text(stringResource(R.string.contacts_qr_explanation))
        }
    }
}
