package com.zara.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.zara.ai.core.common.error.ErrorHandler
import com.zara.ai.core.designsystem.theme.ThemeConfig
import com.zara.ai.core.designsystem.theme.ThemeManager
import com.zara.ai.core.designsystem.theme.ZaraTheme
import com.zara.ai.features.navigation.ZaraNavHost
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var themeManager: ThemeManager

    @Inject
    lateinit var errorHandler: ErrorHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            val themeConfig by themeManager.themeConfig.collectAsState(initial = ThemeConfig())

            ZaraTheme(themeConfig = themeConfig) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ZaraNavHost(errorHandler = errorHandler)
                }
            }
        }
    }
}
