package com.iamasaw.privacy.core.database.di

import android.content.Context
import com.iamasaw.privacy.core.common.dispatcher.Dispatcher
import com.iamasaw.privacy.core.common.dispatcher.MessengerDispatchers
import com.iamasaw.privacy.core.database.MessengerDatabase
import com.iamasaw.privacy.core.database.MessengerDatabaseFactory
import com.iamasaw.privacy.core.database.dao.ContactDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    fun provideMessengerDatabase(
        @ApplicationContext context: Context,
        @Dispatcher(MessengerDispatchers.IO)
        queryDispatcher: CoroutineDispatcher,
    ) =
        MessengerDatabaseFactory.create(
            context = context,
            queryDispatcher = queryDispatcher,
        )

    @Provides
    fun provideContactDao(database: MessengerDatabase): ContactDao =
        database.contactDao()
}
