package com.zara.ai.core.security.di

import android.content.Context
import com.zara.ai.core.common.logger.ZaraLogger
import com.zara.ai.core.security.SecurityManager
import com.zara.ai.core.security.biometric.BiometricAuthManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SecurityModule {

    @Provides
    @Singleton
    fun provideSecurityManager(
        @ApplicationContext context: Context,
        logger: ZaraLogger
    ): SecurityManager = SecurityManager(context, logger)

    @Provides
    @Singleton
    fun provideBiometricAuthManager(
        @ApplicationContext context: Context
    ): BiometricAuthManager = BiometricAuthManager(context)
}
