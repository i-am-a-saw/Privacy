package com.iamasaw.privacy.core.crypto

import android.annotation.SuppressLint
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class MessageBody(
    val type: MessageBodyType,
    val text: String,
    @SerialName("client_time")
    @Serializable(with = InstantSerializer::class)
    val clientTime: Instant,
)
