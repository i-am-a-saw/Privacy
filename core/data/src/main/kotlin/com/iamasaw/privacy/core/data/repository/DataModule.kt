package com.iamasaw.privacy.core.data.repository

import dagger.Binds
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
}
