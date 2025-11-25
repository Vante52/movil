package com.example.fitmatch.presentation.ui.screens.auth.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.compose.FitMatchTheme
import com.example.fitmatch.R
import com.example.fitmatch.presentation.ui.screens.auth.state.RegisterUiState
import com.example.fitmatch.presentation.viewmodel.login.RegisterViewModel

//pantalla de registro en donde el viewmodel maneja to do el estado
//recibe el rol para navegar correctamente

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onBackClick: () -> Unit = {},
    onRegisterSuccess: (role: String) -> Unit = {},
    viewModel: RegisterViewModel = viewModel()
) {
    val colors = MaterialTheme.colorScheme
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // si esto va a ViewModel, levantar a estado de VM
    var email by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("") }

    val scroll = rememberScrollState()

    Scaffold(
        containerColor = colors.background,
        topBar = {
            // Header unificado
            Surface(
                modifier = Modifier
                    .fillMaxWidth(),
                color = colors.surface,
                tonalElevation = 1.dp,
                shadowElevation = 1.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.align(Alignment.CenterStart)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = colors.onSurface
                        )
                    }

                    Text(
                        text = "Registrarse",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 22.sp,
                            color = colors.onSurface
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.Center)
                    )

                    // Espaciador “fantasma” para balancear el back (sin acciones a la derecha)
                    Spacer(
                        modifier = Modifier
                            .width(48.dp)
                            .align(Alignment.CenterEnd)
                    )
                }
            }
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .background(colors.background)
                .padding(24.dp)
                .verticalScroll(scroll)
                .imePadding()
                .navigationBarsPadding()
        ) {
            Text(
                text = "Únete a nuestra comunidad de moda",
                fontSize = 16.sp,
                color = colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Email
            OutlinedTextField(
                value = uiState.email,
                onValueChange = { viewModel.onEmailChanged(it) },
                label = { Text("Email") },
                placeholder = { Text("ejemplo@email.com") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.primary,
                    unfocusedBorderColor = colors.outline,
                    cursorColor = colors.primary,
                    focusedContainerColor = colors.surface,
                    unfocusedContainerColor = colors.surface,
                    focusedLabelColor = colors.primary,
                    focusedTextColor = colors.onSurface,
                    unfocusedTextColor = colors.onSurface
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                enabled = !uiState.isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Contraseña
            OutlinedTextField(
                value = uiState.password,
                onValueChange = { viewModel.onPasswordChanged(it) },
                label = { Text("Contraseña") },
                placeholder = { Text("Mínimo 8 caracteres") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.primary,
                    unfocusedBorderColor = colors.outline,
                    cursorColor = colors.primary,
                    focusedContainerColor = colors.surface,
                    unfocusedContainerColor = colors.surface,
                    focusedLabelColor = colors.primary,
                    focusedTextColor = colors.onSurface,
                    unfocusedTextColor = colors.onSurface
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = if (uiState.showPassword)
                    VisualTransformation.None
                else
                    PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { viewModel.onTogglePasswordVisibility() }) {
                        Icon(
                            imageVector = if (uiState.showPassword)
                                Icons.Filled.VisibilityOff
                            else
                                Icons.Filled.Visibility,
                            contentDescription = if (uiState.showPassword)
                                "Ocultar contraseña"
                            else
                                "Mostrar contraseña",
                            tint = colors.primary
                        )
                    }
                },
                singleLine = true,
                enabled = !uiState.isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Nombre Completo
            OutlinedTextField(
                value = uiState.fullName,
                onValueChange = { viewModel.onFullNameChanged(it) },
                label = { Text("Nombre Completo") },
                placeholder = { Text("Tu nombre completo") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.primary,
                    unfocusedBorderColor = colors.outline,
                    cursorColor = colors.primary,
                    focusedContainerColor = colors.surface,
                    unfocusedContainerColor = colors.surface,
                    focusedLabelColor = colors.primary,
                    focusedTextColor = colors.onSurface,
                    unfocusedTextColor = colors.onSurface
                ),
                singleLine = true,
                enabled = !uiState.isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Numero de telefono
            OutlinedTextField(
                value = uiState.phone,
                onValueChange = { viewModel.onPhoneChanged(it) },
                label = { Text("Numero de teléfono") },
                placeholder = { Text("Tu numero de telefono") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.primary,
                    unfocusedBorderColor = colors.outline,
                    cursorColor = colors.primary,
                    focusedContainerColor = colors.surface,
                    unfocusedContainerColor = colors.surface,
                    focusedLabelColor = colors.primary,
                    focusedTextColor = colors.onSurface,
                    unfocusedTextColor = colors.onSurface
                ),
                singleLine = true,
                enabled = !uiState.isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Fecha de Nacimiento
            OutlinedTextField(
                value = uiState.birthDate,
                onValueChange = { viewModel.onBirthDateChanged(it) },
                label = { Text("Fecha de Nacimiento") },
                placeholder = { Text("dd / mm / aaaa") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.primary,
                    unfocusedBorderColor = colors.outline,
                    cursorColor = colors.primary,
                    focusedContainerColor = colors.surface,
                    unfocusedContainerColor = colors.surface,
                    focusedLabelColor = colors.primary,
                    focusedTextColor = colors.onSurface,
                    unfocusedTextColor = colors.onSurface
                ),
                singleLine = true,
                enabled = !uiState.isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Ciudad
            OutlinedTextField(
                value = uiState.city,
                onValueChange = { viewModel.onCityChanged(it) },
                label = { Text("Ciudad") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.primary,
                    unfocusedBorderColor = colors.outline,
                    cursorColor = colors.primary,
                    focusedContainerColor = colors.surface,
                    unfocusedContainerColor = colors.surface,
                    focusedLabelColor = colors.primary,
                    focusedTextColor = colors.onSurface,
                    unfocusedTextColor = colors.onSurface
                ),
                singleLine = true,
                enabled = !uiState.isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Género (ExposedDropdown)
            ExposedDropdownMenuBox(
                expanded = uiState.isGenderDropdownExpanded,
                onExpandedChange = {
                    if (!uiState.isLoading) {
                        viewModel.onGenderDropdownToggle()
                    }
                }
            ) {
                OutlinedTextField(
                    value = uiState.selectedGender,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Género") },
                    placeholder = { Text("Seleccionar") },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = colors.onSurfaceVariant
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.primary,
                        unfocusedBorderColor = colors.outline,
                        cursorColor = colors.primary,
                        focusedContainerColor = colors.surface,
                        unfocusedContainerColor = colors.surface,
                        focusedLabelColor = colors.primary,
                        focusedTextColor = colors.onSurface,
                        unfocusedTextColor = colors.onSurface
                    ),
                    singleLine = true,
                    enabled = !uiState.isLoading
                )
                ExposedDropdownMenu(
                    expanded = uiState.isGenderDropdownExpanded,
                    onDismissRequest = { viewModel.onGenderDropdownToggle() }
                ) {
                    RegisterUiState.GENDER_OPTIONS.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = { viewModel.onGenderSelected(option) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ¿Cómo quieres usar la app?
            Text(
                text = "¿Cómo quieres usar la app?",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = colors.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RegisterUiState.ROLE_OPTIONS.forEach { role ->
                    FilterChip(
                        selected = uiState.selectedRole == role,
                        onClick = {
                            if (!uiState.isLoading) {
                                viewModel.onRoleSelected(role)
                            }
                        },
                        label = { Text(role) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = colors.primary,
                            selectedLabelColor = colors.onPrimary,
                            containerColor = colors.surface,
                            labelColor = colors.onSurface,
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedRole == role,
                            borderColor = colors.outline,
                            selectedBorderColor = colors.primary
                        ),
                        shape = RoundedCornerShape(20.dp),
                        enabled = !uiState.isLoading
                    )
                }
            }
            if (uiState.errorMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = uiState.errorMessage!!,
                    color = colors.error,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Personaje Tito
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(Modifier.weight(1f))

                // Personaje "Tito" (imagen desde drawable)
                Image(
                    painter = painterResource(id = R.drawable.guru),
                    contentDescription = "Tito",
                    modifier = Modifier.size(370.dp),
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.width(16.dp))


            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botón Registrarse
            Button(
                onClick = {
                    viewModel.onRegisterClick(onSuccess = onRegisterSuccess)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primary,
                    disabledContainerColor = colors.surfaceVariant,
                    disabledContentColor = colors.onSurfaceVariant
                ),
                shape = RoundedCornerShape(25.dp),
                enabled = uiState.isRegisterEnabled && !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = colors.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Registrarse",
                        color = colors.onPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// Previews con el tema FitMatchTheme
@Preview(showBackground = true, name = "Register – Light (Brand)")
@Composable
private fun RegisterPreviewLight() {
    FitMatchTheme(darkTheme = false, dynamicColor = false) {
        RegisterScreen()
    }
}

@Preview(showBackground = true, name = "Register – Dark (Brand)")
@Composable
private fun RegisterPreviewDark() {
    FitMatchTheme(darkTheme = true, dynamicColor = false) {
        RegisterScreen()
    }
}
