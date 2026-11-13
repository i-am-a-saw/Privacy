package com.iamasaw.privacy.core.network

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SupabaseClientProvider {
    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient {
        val url = BuildConfig.SUPABASE_URL
        val key = BuildConfig.SUPABASE_PUBLISHABLE_KEY

        require(url.isNotBlank()) { "Supabase URL is not configured" }
        require(key.isNotBlank()) { "Supabase publishable key is not configured" }

        return createSupabaseClient(
            supabaseUrl = url,
            supabaseKey = key,
        ) {
            install(Auth)
            install(Postgrest)
            install(Realtime)
            install(Storage)
            install(Functions)
        }
    }
}
