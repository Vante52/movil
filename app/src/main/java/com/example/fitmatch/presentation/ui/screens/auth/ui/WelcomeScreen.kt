package com.example.fitmatch.presentation.ui.screens.auth.ui

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.compose.FitMatchTheme
import com.example.fitmatch.R
import com.example.fitmatch.presentation.viewmodel.login.WelcomeViewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch
import android.util.Log


@Composable
fun WelcomeScreen(
    onCreateAccount: () -> Unit = {},
    onContinueWithEmail: () -> Unit = {},
    onContinueWithGoogle: () -> Unit = {},
    onContinueWithFacebook: () -> Unit = {},
    onNavigateToCompleteProfile: (userId: String) -> Unit = {},
    onNavigateToHome: (userId: String) -> Unit = {},
    viewModel: WelcomeViewModel = viewModel()
) {

    val colors = MaterialTheme.colorScheme
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    // ========== GOOGLE SIGN-IN ==========
    val credentialManager = remember { CredentialManager.create(context) }

    val handleGoogleSignIn: () -> Unit = {
        scope.launch {
            try {
                val idToken = signInWithGoogle(context, credentialManager)

                idToken?.let {
                    // Lambda con LoginResult
                    viewModel.onGoogleSignIn(it) { result ->
                        if (result.isNewUser) {
                            onNavigateToCompleteProfile(result.userId)
                        } else {
                            onNavigateToHome(result.userId)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("GoogleSignIn", "Error: ${e.message}")
            }
        }
    }

    // ========== FACEBOOK SIGN-IN - DESHABILITADO TEMPORALMENTE ==========
    // TODO: Configurar Facebook SDK correctamente antes de habilitar
    /*
    val callbackManager = remember { CallbackManager.Factory.create() }

    DisposableEffect(Unit) {
        LoginManager.getInstance().registerCallback(
            callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult) {
                    val accessToken = result.accessToken.token
                    viewModel.onFacebookSignIn(accessToken) { result ->
                        if (result.isNewUser) {
                            onNavigateToCompleteProfile(result.userId)
                        } else {
                            onNavigateToHome(result.userId)
                        }
                    }
                }

                override fun onCancel() {
                    Log.d("FacebookSignIn", "Cancelado")
                }

                override fun onError(error: FacebookException) {
                    Log.e("FacebookSignIn", "Error: ${error.message}")
                }
            }
        )

        onDispose {
            LoginManager.getInstance().unregisterCallback(callbackManager)
        }
    }
    */

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background) // Fondo del tema
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Título de bienvenida
            Text(
                text = "¡Bienvenido/a a fitmatch!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = colors.onBackground, // del tema
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Personaje "Tito" (imagen desde drawable)
            Image(
                painter = painterResource(id = R.drawable.welcome),
                contentDescription = "Tito",
                modifier = Modifier.size(370.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.weight(1f))


            // ========== MOSTRAR ERROR ==========
            if (uiState.errorMessage != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = colors.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = colors.onErrorContainer
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            uiState.errorMessage!!,
                            color = colors.onErrorContainer,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Botones de acción
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Botón Crear Cuenta
                Button(
                    onClick = onCreateAccount,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = !uiState.isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primary,
                        contentColor = colors.onPrimary
                    ),
                    shape = RoundedCornerShape(25.dp)
                ) {
                    Text(
                        text = "Crear Cuenta",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Botón Continuar con Email
                Button(
                    onClick = onContinueWithEmail,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = !uiState.isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primary,
                        contentColor = colors.onPrimary
                    ),
                    shape = RoundedCornerShape(25.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Continuar con Email",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Botón Continuar con Google
                Button(
                    onClick = handleGoogleSignIn, // Llama a la función local
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = !uiState.isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primary,
                        contentColor = colors.onPrimary
                    ),
                    shape = RoundedCornerShape(25.dp)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = colors.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text("G", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.width(8.dp))
                            Text("Continuar con Google", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                // ========== BOTÓN FACEBOOK DESHABILITADO ==========
                // TODO: Habilitar después de configurar Facebook SDK
                /*
                Button(
                    onClick = {
                        LoginManager.getInstance().logInWithReadPermissions(
                            context as androidx.activity.ComponentActivity,
                            callbackManager,
                            listOf("email", "public_profile")
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = !uiState.isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primary,
                        contentColor = colors.onPrimary
                    ),
                    shape = RoundedCornerShape(25.dp)
                ) {
                    Text("Continuar con Facebook", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
                */
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Términos y condiciones
            Text(
                text = "Al hacer clic en continuar, aceptas nuestros Términos de Servicio y nuestra Política de Privacidad",
                fontSize = 12.sp,
                color = colors.onSurfaceVariant, // gris del tema
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

}

// ========== FUNCIÓN HELPER PARA GOOGLE SIGN-IN ==========
private suspend fun signInWithGoogle(
    context: Context,
    credentialManager: CredentialManager
): String? {
    return try {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(context.getString(R.string.default_web_client_id))
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val result: GetCredentialResponse = credentialManager.getCredential(
            request = request,
            context = context
        )

        when (val credential = result.credential) {
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = GoogleIdTokenCredential
                        .createFrom(credential.data)
                    googleIdTokenCredential.idToken
                } else {
                    null
                }
            }
            else -> null
        }
    } catch (e: Exception) {
        Log.e("GoogleSignIn", "Error: ${e.message}")
        null
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "welcome – Light")
@Composable
private fun CreateProductPreviewLight() {
    FitMatchTheme(darkTheme = false, dynamicColor = false) {
        WelcomeScreen()
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "welcome – Dark")
@Composable
private fun CreateProductPreviewDark() {
    FitMatchTheme(darkTheme = true, dynamicColor = false) {
        WelcomeScreen()
    }
}