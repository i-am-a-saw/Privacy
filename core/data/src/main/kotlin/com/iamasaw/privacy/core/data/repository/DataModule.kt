package com.iamasaw.privacy.core.data.repository

import com.iamasaw.privacy.core.data.repository.defaults.DefaultAuthRepository
import com.iamasaw.privacy.core.data.repository.defaults.DefaultContactRepository
import com.iamasaw.privacy.core.data.repository.defaults.DefaultConversationRepository
import com.iamasaw.privacy.core.data.repository.defaults.DefaultLocalDataRepository
import com.iamasaw.privacy.core.data.repository.defaults.DefaultMessageRepository
import com.iamasaw.privacy.core.data.repository.defaults.DefaultProfileRepository
import dagger.Binds
import dagger.BindsOptionalOf
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    @Binds
    abstract fun bindAuthRepository(impl: DefaultAuthRepository): AuthRepository

    @Binds
    abstract fun bindProfileRepository(impl: DefaultProfileRepository): ProfileRepository

    @Binds
    abstract fun bindContactRepository(impl: DefaultContactRepository): ContactRepository

    @Binds
    abstract fun bindLocalDataRepository(impl: DefaultLocalDataRepository): LocalDataRepository

    @Binds
    abstract fun bindMessageRepository(impl: DefaultMessageRepository): MessageRepository

    @BindsOptionalOf
    abstract fun optionalOutboxScheduler(): OutboxScheduler

    @Binds
    abstract fun bindConversationRepository(impl: DefaultConversationRepository): ConversationRepository
}
