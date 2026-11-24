package com.iamasaw.privacy.core.network.realtime

import com.iamasaw.privacy.core.network.dto.InboxMessageDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.broadcastFlow
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import okio.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RealtimeConnection
    @Inject
    constructor(
        private val client: SupabaseClient,
    ) {
        fun events(): Flow<InboxEvent> =
            flow {
                client.auth.awaitInitialization()

                val userId =
                    checkNotNull(client.auth.currentUserOrNull()?.id) {
                        "An authenticated session is required"
                    }

                var failures = 0L

                emitAll(
                    inboxSession(userId)
                        .onEach {
                            if (it == InboxEvent.Connected) failures = 0
                        }.retryWhen { cause, _ ->
                            if (cause !is IOException) return@retryWhen false

                            delay(reconnectDelayMillis(failures++))
                            true
                        },
                )
            }

        private fun inboxSession(userId: String): Flow<InboxEvent> =
            channelFlow {
                if (client.auth.currentUserOrNull()?.id != userId) {
                    throw CancellationException("The authenticated user changed")
                }

                val channel =
                    client.channel("user:$userId") {
                        isPrivate = true
                    }

                val messages =
                    launch(start = CoroutineStart.UNDISPATCHED) {
                        channel.broadcastFlow<InboxMessageDto>("message_created").collect { event ->
                            send(
                                InboxEvent.MessageCreated(
                                    messageId = event.id,
                                    conversationId = event.conversationId,
                                ),
                            )
                        }
                    }

                try {
                    val joined =
                        withTimeoutOrNull(JOIN_TIMEOUT_MILLIS) {
                            channel.subscribe(blockUntilSubscribed = true)
                            true
                        } ?: false

                    if (!joined) {
                        throw IOException("Inbox subscription timed out")
                    }

                    send(InboxEvent.Connected)

                    coroutineScope {
                        launch {
                            client.realtime.status.first {
                                it != Realtime.Status.CONNECTED
                            }
                            throw IOException("Realtime socket disconnected")
                        }

                        channel.status.first {
                            it != RealtimeChannel.Status.SUBSCRIBED
                        }
                        throw IOException("Inbox subscription disconnected")
                    }
                } finally {
                    messages.cancel()

                    withContext(NonCancellable) {
                        withTimeoutOrNull(CLEANUP_TIMEOUT_MILLIS) {
                            client.realtime.removeChannel(channel)
                        }
                    }
                }
            }

        private companion object {
            const val JOIN_TIMEOUT_MILLIS = 15_000L
            const val CLEANUP_TIMEOUT_MILLIS = 5_000L
        }
    }

@Suppress("MagicNumber")
internal fun reconnectDelayMillis(attempt: Long): Long {
    val exponent = attempt.coerceIn(0, 5).toInt()
    return (1_000L shl exponent).coerceAtMost(30_000L)
}
