package com.zara.ai.core.voice.di

import com.zara.ai.core.voice.provider.RealtimeVoiceProvider
import com.zara.ai.core.voice.provider.gemini.GeminiRealtimeVoiceProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class VoiceModule {

    @Binds
    @Singleton
    abstract fun bindRealtimeVoiceProvider(
        geminiRealtimeVoiceProvider: GeminiRealtimeVoiceProvider
    ): RealtimeVoiceProvider
}
