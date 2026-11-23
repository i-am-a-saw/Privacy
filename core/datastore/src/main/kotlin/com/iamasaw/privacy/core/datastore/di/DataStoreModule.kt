package com.iamasaw.privacy.core.datastore.di

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.iamasaw.privacy.core.datastore.DevicePreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.devicePreferences by preferencesDataStore(
    name = "device_preferences",
)

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {
    @Provides
    @Singleton
    fun provideDevicePreferences(
        @ApplicationContext context: Context,
    ): DevicePreferences = DevicePreferences(context.devicePreferences)
}
