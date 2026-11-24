package com.iamasaw.privacy.core.network

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.iamasaw.privacy.core.network.conversation.SupabaseConversationRemoteDataSource
import com.iamasaw.privacy.core.network.dto.MessageCursorDto
import com.iamasaw.privacy.core.network.dto.MessageInsertDto
import com.iamasaw.privacy.core.network.message.SupabaseMessageRemoteDataSource
import com.iamasaw.privacy.core.network.realtime.InboxEvent
import com.iamasaw.privacy.core.network.realtime.RealtimeConnection
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.minimalConfig
import io.github.jan.supabase.auth.providers.builtin.OTP
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class ChatRemoteIntegrationTest {
    @Test
    fun messagesAreIdempotentAndDeliveredToThePrivateInbox() =
        runBlocking {
            assumeTrue(BuildConfig.FLAVOR == "local")

            val alice = newClient()
            val bob = newClient()

            try {
                login(alice, "+16505550101", "123456")
                login(bob, "+16505550102", "123457")

                val aliceId = checkNotNull(alice.auth.currentUserOrNull()?.id)
                val bobId = checkNotNull(bob.auth.currentUserOrNull()?.id)

                assertNotNull(
                    SupabaseProfileRemoteDataSource(alice).fetchOwnProfile(),
                )
                assertNotNull(
                    SupabaseProfileRemoteDataSource(bob).fetchOwnProfile(),
                )

                verifyExchange(alice, bob, aliceId, bobId)
            } finally {
                alice.close()
                bob.close()
            }
        }

    private suspend fun verifyExchange(
        alice: SupabaseClient,
        bob: SupabaseClient,
        aliceId: String,
        bobId: String,
    ) = coroutineScope {
        val conversations = SupabaseConversationRemoteDataSource(alice)
        val conversationId = conversations.startDirectChat(bobId)

        assertEquals(
            conversationId,
            conversations.startDirectChat(bobId),
        )

        val conversation =
            conversations
                .fetchMyConversations()
                .single { it.id == conversationId }

        assertTrue(
            conversation.members.any {
                it.userId == bobId && it.profile != null
            },
        )

        val events = Channel<InboxEvent>(Channel.UNLIMITED)
        val listener =
            launch {
                RealtimeConnection(bob).events().collect {
                    events.send(it)
                }
            }

        try {
            assertEquals(InboxEvent.Connected, nextEvent(events))

            val sender = SupabaseMessageRemoteDataSource(alice)
            val receiver = SupabaseMessageRemoteDataSource(bob)

            val request =
                MessageInsertDto(
                    id = UUID.randomUUID().toString(),
                    conversationId = conversationId,
                    senderId = aliceId,
                    payload = byteArrayOf(0, 15, -128, -1),
                    cipherSuite = "none-v0",
                )

            val inserted = sender.insert(request)

            assertEquals(
                InboxEvent.MessageCreated(request.id, conversationId),
                nextEvent(events),
            )

            val repeated =
                sender.insert(
                    request.copy(payload = byteArrayOf(42)),
                )

            assertEquals(inserted.id, repeated.id)
            assertEquals(inserted.createdAt, repeated.createdAt)
            assertArrayEquals(request.payload, repeated.payload)

            val cursor =
                MessageCursorDto(
                    createdAt = inserted.createdAt,
                    id = inserted.id,
                )

            val second =
                sender.insert(
                    request.copy(id = UUID.randomUUID().toString()),
                )

            assertEquals(
                InboxEvent.MessageCreated(second.id, conversationId),
                nextEvent(events),
            )

            val page = receiver.fetchSince(cursor)

            assertTrue(page.any { it.id == second.id })
            assertTrue(page.none { it.id == inserted.id })
            assertArrayEquals(
                request.payload,
                page.single { it.id == second.id }.payload,
            )

            bob.realtime.disconnect()

            assertEquals(InboxEvent.Connected, nextEvent(events))

            val third =
                sender.insert(
                    request.copy(id = UUID.randomUUID().toString()),
                )

            assertEquals(
                InboxEvent.MessageCreated(third.id, conversationId),
                nextEvent(events),
            )
        } finally {
            listener.cancelAndJoin()
            events.close()
        }
    }

    private suspend fun nextEvent(events: Channel<InboxEvent>): InboxEvent =
        withTimeout(45_000L) {
            events.receive()
        }

    private suspend fun login(
        client: SupabaseClient,
        phone: String,
        code: String,
    ) {
        client.auth.awaitInitialization()

        client.auth.signInWith(OTP) {
            this.phone = phone
            createUser = false
        }

        client.auth.verifyPhoneOtp(
            type = OtpType.Phone.SMS,
            phone = phone,
            token = code,
        )
    }

    private fun newClient(): SupabaseClient =
        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_PUBLISHABLE_KEY,
        ) {
            install(Auth) {
                minimalConfig()
            }
            install(Postgrest)
            install(Realtime)
        }
}
