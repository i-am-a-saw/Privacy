package com.iamasaw.privacy.core.network

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
}
