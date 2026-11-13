package com.zara.ai.core.designsystem.theme

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "zara_theme_preferences")

enum class ThemeMode {
    SYSTEM, LIGHT, DARK
}

data class ThemeConfig(
    val themeMode: ThemeMode = ThemeMode.DARK,
    val useDynamicColor: Boolean = false,
    val useAmoledBlack: Boolean = true
)

@Singleton
class ThemeManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val AMOLED_BLACK = booleanPreferencesKey("amoled_black")
    }

    val themeConfig: Flow<ThemeConfig> = context.dataStore.data.map { preferences ->
        val modeString = preferences[PreferencesKeys.THEME_MODE] ?: ThemeMode.DARK.name
        val themeMode = try {
            ThemeMode.valueOf(modeString)
        } catch (_: Exception) {
            ThemeMode.DARK
        }
        val dynamicColor = preferences[PreferencesKeys.DYNAMIC_COLOR] ?: false
        val amoledBlack = preferences[PreferencesKeys.AMOLED_BLACK] ?: true

        ThemeConfig(
            themeMode = themeMode,
            useDynamicColor = dynamicColor,
            useAmoledBlack = amoledBlack
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode.name
        }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DYNAMIC_COLOR] = enabled
        }
    }

    suspend fun setAmoledBlack(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AMOLED_BLACK] = enabled
        }
    }
}
