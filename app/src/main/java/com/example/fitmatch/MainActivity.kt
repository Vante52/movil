package com.example.fitmatch

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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseDatabaseConfig.initialize()
        CloudinaryConfig.initialize(this)
        enableEdgeToEdge()
        setContent {
            FitMatchTheme {
                Surface(
                    modifier = Modifier.Companion.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainNavigation()
                }
            }
        }
    }
}