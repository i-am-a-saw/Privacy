package com.iamasaw.privacy.core.network

import com.iamasaw.privacy.core.network.conversation.ConversationRemoteDataSource
import com.iamasaw.privacy.core.network.conversation.SupabaseConversationRemoteDataSource
import com.iamasaw.privacy.core.network.message.MessageRemoteDataSource
import com.iamasaw.privacy.core.network.message.SupabaseMessageRemoteDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkModule {
    @Binds
    abstract fun bindAuthRemoteDataSource(impl: SupabaseAuthRemoteDataSource): AuthRemoteDataSource

    @Binds
    abstract fun bindProfileRemoteDataSource(impl: SupabaseProfileRemoteDataSource): ProfileRemoteDataSource

    @Binds
    abstract fun bindConversations(
        impl: SupabaseConversationRemoteDataSource
    ): ConversationRemoteDataSource

    @Binds
    abstract fun bindMessages(
        impl: SupabaseMessageRemoteDataSource
    ): MessageRemoteDataSource
}
