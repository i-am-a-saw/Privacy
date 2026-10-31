package com.iamasaw.privacy.core.model

sealed interface MessageContent {
    data class Text(
        val text: String,
    ) : MessageContent
}
