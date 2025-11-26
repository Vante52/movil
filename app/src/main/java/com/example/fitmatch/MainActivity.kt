package com.example.fitmatch

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.compose.FitMatchTheme
import com.example.fitmatch.navigation.MainNavigation
import com.example.fitmatch.config.CloudinaryConfig
import com.example.fitmatch.config.FirebaseDatabaseConfig

class MainActivity : ComponentActivity() {

    private var pendingChatId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseDatabaseConfig.initialize()
        CloudinaryConfig.initialize(this)
        enableEdgeToEdge()

        // Leer chatId si la app se abrió desde una notificación
        pendingChatId = intent?.getStringExtra("chatId")

        setContent {
            FitMatchTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Pasamos el chatId a la navegación
                    MainNavigation(
                        startChatId = pendingChatId
                    )
                }
            }
        }
    }

    // Si la app YA estaba abierta y llega una notificación
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        setIntent(intent)

        val chatId = intent.getStringExtra("chatId")
        if (chatId != null) {
            pendingChatId = chatId
        }
    }


}
