package com.iamasaw.privacy.core.common.di

import com.iamasaw.privacy.core.common.time.Clock
import com.iamasaw.privacy.core.common.time.DefaultClock
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ClockModule {
    @Binds
    abstract fun bindsClock(clock: DefaultClock): Clock
}
