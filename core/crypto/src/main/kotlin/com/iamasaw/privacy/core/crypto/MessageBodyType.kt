package com.iamasaw.privacy.core.crypto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class MessageBodyType {
    @SerialName("text")
    TEXT,
}
