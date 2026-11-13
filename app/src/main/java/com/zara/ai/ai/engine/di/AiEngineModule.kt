package com.zara.ai.ai.engine.di

import com.zara.ai.ai.engine.tools.builtin.ZaraBuiltinTools
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AiEngineModule {

    @Provides
    @Singleton
    fun provideInitializeBuiltinTools(
        builtinTools: ZaraBuiltinTools
    ): Boolean {
        builtinTools.registerAll()
        return true
    }
}
