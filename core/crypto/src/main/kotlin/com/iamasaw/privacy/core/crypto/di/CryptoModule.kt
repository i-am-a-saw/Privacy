package com.iamasaw.privacy.core.crypto.di

import com.iamasaw.privacy.core.crypto.CipherRegistry
import com.iamasaw.privacy.core.crypto.MessageBodyCodec
import com.iamasaw.privacy.core.crypto.NoOpCipher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CryptoModule {
    @Provides
    @Singleton
    fun bindMessageBodyCodec(): MessageBodyCodec = MessageBodyCodec()

    @Provides
    @Singleton
    fun bindCipherRegistry(): CipherRegistry =
        CipherRegistry(
            ciphers = listOf(NoOpCipher()),
        )
}
