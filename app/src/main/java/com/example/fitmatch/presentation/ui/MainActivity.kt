package com.example.fitmatch.presentation.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.compose.FitMatchTheme
import com.example.fitmatch.presentation.navigation.MainNavigation
import com.auth0.android.Auth0
import com.auth0.android.provider.WebAuthProvider
class MainActivity : ComponentActivity() {
    private lateinit var account: Auth0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        account = Auth0(
            "pmbBAHoCgmoL86440Hhds4Na7bAhr5nH",
            "dev-bt057t8fkm1ddqi1.us.auth0.com"
        )
        enableEdgeToEdge()
        setContent { // ¡Este lambda SÍ es el contexto composable!
            FitMatchTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.background
                ) {
                    MainNavigation()
                }
            }
        }
    }
}