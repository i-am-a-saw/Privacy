package com.iamasaw.privacy.core.network

import com.iamasaw.privacy.core.network.dto.ProfileDto
import com.iamasaw.privacy.core.network.dto.ProfileInsertDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject

class SupabaseProfileRemoteDataSource
    @Inject
    constructor(
        private val client: SupabaseClient,
    ) : ProfileRemoteDataSource {
        override suspend fun createProfile(
            username: String,
            displayName: String?,
            avatarPath: String?,
        ): ProfileDto {
            val userId =
                checkNotNull(client.auth.currentUserOrNull()?.id) {
                    "No authenticated user"
                }

            return client.postgrest["profiles"]
                .insert(ProfileInsertDto(userId, username, displayName, avatarPath)) {
                    select()
                }.decodeSingle<ProfileDto>()
        }

        override suspend fun findByUsername(username: String): ProfileDto? =
            client.postgrest
                .rpc(
                    function = "find_profile_by_username",
                    parameters =
                        buildJsonObject {
                            put("lookup_username", username)
                        },
                ).decodeSingleOrNull<ProfileDto>()

        override suspend fun fetchOwnProfile(): ProfileDto? {
            val userId =
                checkNotNull(client.auth.currentUserOrNull()?.id) {
                    "No authenticated user"
                }

            return client.postgrest["profiles"]
                .select {
                    filter { eq("id", userId) }
                }.decodeSingleOrNull<ProfileDto>()
        }
    }
