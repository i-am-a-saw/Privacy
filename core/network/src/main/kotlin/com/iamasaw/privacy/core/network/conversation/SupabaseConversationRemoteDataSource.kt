package com.iamasaw.privacy.core.network.conversation

import com.iamasaw.privacy.core.network.dto.ConversationDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject

class SupabaseConversationRemoteDataSource @Inject constructor(
    private val client: SupabaseClient
) : ConversationRemoteDataSource {
    override suspend fun startDirectChat(otherUser: String): String =
        client.postgrest.rpc(
            function = "get_or_create_direct_conversation",
            parameters = buildJsonObject {
                put("other_user", otherUser)
            }
        ).decodeAs<String>()


    override suspend fun fetchMyConversations(): List<ConversationDto> =
        client.postgrest["conversations"].select(
            columns = Columns.raw(
                "id,type,created_by,created_at," +
                "members:conversation_members(" +
                "user_id,profile:profiles(id,username,display_name,avatar_path))",
            )
        ).decodeList<ConversationDto>()
}
