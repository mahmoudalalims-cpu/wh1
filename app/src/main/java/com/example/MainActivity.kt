package com.example

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.WarehouseApp
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.WarehouseViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: WarehouseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val sharedImageUri = extractSharedImageUri(intent)

        setContent {
            val themeMode by viewModel.themePreferencesManager.themeMode.collectAsStateWithLifecycle()
            val colorPalette by viewModel.themePreferencesManager.colorPalette.collectAsStateWithLifecycle()

            MyApplicationTheme(
                themeMode = themeMode,
                colorPalette = colorPalette
            ) {
                WarehouseApp(
                    viewModel = viewModel,
                    initialSharedImageUri = sharedImageUri
                )
            }
        }
    }

    private fun extractSharedImageUri(intent: Intent?): Uri? {
        if (intent == null) return null
        val action = intent.action
        val type = intent.type
        if (Intent.ACTION_SEND == action && type?.startsWith("image/") == true) {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(Intent.EXTRA_STREAM)
            }
        }
        return null
    }
}
