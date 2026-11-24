package com.iamasaw.privacy.core.network.message

import com.iamasaw.privacy.core.network.dto.MessageCursorDto
import com.iamasaw.privacy.core.network.dto.MessageDto
import com.iamasaw.privacy.core.network.dto.MessageInsertDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject

class SupabaseMessageRemoteDataSource
    @Inject
    constructor(
        private val client: SupabaseClient,
    ) : MessageRemoteDataSource {
        override suspend fun insert(message: MessageInsertDto): MessageDto {
            val inserted =
                client.postgrest["messages"]
                    .upsert(message) {
                        onConflict = "id"
                        ignoreDuplicates = true
                        select()
                    }.decodeSingleOrNull<MessageDto>()

            return inserted ?: client.postgrest["messages"]
                .select {
                    filter {
                        eq("id", message.id)
                    }
                }.decodeSingle<MessageDto>()
        }

        override suspend fun fetchSince(
            cursor: MessageCursorDto?,
            pageSize: Int,
        ): List<MessageDto> {
            require(pageSize in 1..MAX_PAGE_SIZE) {
                "Page size must be in 1 and $MAX_PAGE_SIZE"
            }

            return client.postgrest
                .rpc(
                    function = "fetch_messages_since",
                    parameters =
                        buildJsonObject {
                            put("after_created_at", cursor?.createdAt)
                            put("after_id", cursor?.id)
                            put("page_size", pageSize)
                        },
                ).decodeList<MessageDto>()
        }

        private companion object {
            const val MAX_PAGE_SIZE = 200
        }
    }
