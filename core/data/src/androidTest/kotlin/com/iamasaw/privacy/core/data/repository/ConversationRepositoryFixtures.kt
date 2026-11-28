package com.iamasaw.privacy.core.data.repository

import com.iamasaw.privacy.core.common.result.AppResult
import com.iamasaw.privacy.core.data.usecase.SignOutUseCase
import com.iamasaw.privacy.core.model.SessionState
import com.iamasaw.privacy.core.model.UserId
import com.iamasaw.privacy.core.network.conversation.ConversationRemoteDataSource
import com.iamasaw.privacy.core.network.dto.ConversationDto
import com.iamasaw.privacy.core.network.dto.ConversationMemberDto
import com.iamasaw.privacy.core.network.dto.ProfileDto
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.IOException

internal class FakeConversationRemote : ConversationRemoteDataSource {
    var startCalls = 0
    var failFetch = false
    var peerName = "Bob"

    override suspend fun startDirectChat(otherUser: String): String {
        check(otherUser == "bob")
        startCalls++
        return "conversation-1"
    }

    override suspend fun fetchMyConversations(): List<ConversationDto> {
        if (failFetch) throw IOException("Offline")

        return listOf(
            ConversationDto(
                id = "conversation-1",
                type = "direct",
                createdAt = "2026-11-25T12:00:00.123456Z",
                createdy = "alice",
                members =
                    listOf(
                        ConversationMemberDto(
                            "alice",
                            ProfileDto("alice", "alice", "Alice"),
                        ),
                        ConversationMemberDto(
                            "bob",
                            ProfileDto("bob", "bob", peerName),
                        ),
                    ),
            ),
        )
    }
}

internal suspend fun readyChatUser(): CurrentChatUser {
    val auth =
        object : AuthRepository {
            override val sessionState =
                MutableStateFlow<SessionState>(
                    SessionState.SignedIn(UserId("alice")),
                )

            override suspend fun requestCode(phone: String): AppResult<Unit> = AppResult.Success(Unit)

            override suspend fun verifyCode(
                phone: String,
                code: String,
            ): AppResult<Unit> = AppResult.Success(Unit)

            override suspend fun signOut(): AppResult<Unit> = AppResult.Success(Unit)
        }

    val local =
        object : LocalDataRepository {
            override suspend fun isSignOutPending(): AppResult<Boolean> = AppResult.Success(false)

            override suspend fun beginSignOut(): AppResult<Unit> = AppResult.Success(Unit)

            override suspend fun clearUserData(): AppResult<Unit> = AppResult.Success(Unit)

            override suspend fun completeSignOut(): AppResult<Unit> = AppResult.Success(Unit)
        }

    val signOut = SignOutUseCase(auth, local)
    check(signOut.restoreIfNeeded() is AppResult.Success)

    return CurrentChatUser(auth, signOut)
}
