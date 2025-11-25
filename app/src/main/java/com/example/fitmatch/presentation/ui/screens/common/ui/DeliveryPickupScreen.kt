package com.example.fitmatch.presentation.ui.screens.common.ui

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fitmatch.presentation.ui.screens.common.state.*
import com.example.fitmatch.presentation.ui.screens.common.viewmodel.DeliveryEvent
import com.example.fitmatch.presentation.ui.screens.common.viewmodel.DeliveryPickupViewModel
import com.example.fitmatch.presentation.utils.LocationHandler
import com.example.fitmatch.presentation.utils.MapHelper
import com.example.fitmatch.presentation.utils.MapStyleHelper
import com.example.fitmatch.presentation.utils.RouteService
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Schedule

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun DeliveryPickupScreen(
    vm: DeliveryPickupViewModel = viewModel(),
    onDial: (String) -> Unit = {},
    onOpenChat: (String) -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme
    val isDarkTheme = isSystemInDarkTheme()
    val scope = rememberCoroutineScope()

    // Estados
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    val snackbarHost = remember { SnackbarHostState() }

    // Estados del mapa
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var currentLocationMarker by remember { mutableStateOf<Marker?>(null) }
    var pickupMarker by remember { mutableStateOf<Marker?>(null) }
    var deliveryMarker by remember { mutableStateOf<Marker?>(null) }
    var currentLocation by remember { mutableStateOf<GeoPoint?>(null) }
    var pathPolyline by remember { mutableStateOf<Polyline?>(null) }
    var routePolyline by remember { mutableStateOf<Polyline?>(null) }
    var pathPoints by remember { mutableStateOf<MutableList<GeoPoint>>(mutableListOf()) }

    // Location handler
    val locationHandler = remember { LocationHandler(context) }

    // Permisos de ubicación
    val locationPermissions = rememberMultiplePermissionsState(
        permissions = listOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    // Inicializar ubicación cuando se otorguen permisos
    LaunchedEffect(locationPermissions.allPermissionsGranted) {
        if (locationPermissions.allPermissionsGranted) {
            // Ubicación inicial del repartidor (inicio de la ruta)
            val initialPosition = GeoPoint(4.60667, -74.08591) // Ubicación cerca al centro comercial
            currentLocation = initialPosition

            mapView?.let { map ->
                if (currentLocationMarker == null) {
                    currentLocationMarker = MapHelper.addMarker(
                        map, initialPosition, "Repartidor", "" //
                    )
                }
                MapHelper.centerMapOnLocation(map, initialPosition, 15.0)
            }
        }
    }

    // Limpiar al salir
    DisposableEffect(Unit) {
        onDispose {
            locationHandler.stopLocationUpdates()
        }
    }

    // Mostrar mensajes
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { snackbarHost.showSnackbar(it) }
    }
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { snackbarHost.showSnackbar(it) }
    }

    // Actualizar marcadores cuando cambien los pasos
    LaunchedEffect(uiState.tripSteps, uiState.currentStepIndex) {
        mapView?.let { map ->
            val steps = uiState.tripSteps
            if (steps.isNotEmpty() && steps.size >= 2) {
                val pickupStep = steps[0]
                if (pickupStep.latitude != null && pickupStep.longitude != null) {
                    val pickupPoint = GeoPoint(pickupStep.latitude, pickupStep.longitude)
                    if (pickupMarker == null) {
                        pickupMarker = MapHelper.addMarker(
                            map, pickupPoint, "🏪 ${pickupStep.title}", pickupStep.address
                        )
                    }
                }

                val deliveryStep = steps[1]
                if (deliveryStep.latitude != null && deliveryStep.longitude != null) {
                    val deliveryPoint = GeoPoint(deliveryStep.latitude, deliveryStep.longitude)
                    if (deliveryMarker == null) {
                        deliveryMarker = MapHelper.addMarker(
                            map, deliveryPoint, "📦 ${deliveryStep.title}", deliveryStep.address
                        )
                    }
                }

                // OBTENER RUTA REAL POR LAS CALLES
                currentLocation?.let { current ->
                    val activeStep = steps[uiState.currentStepIndex]
                    if (activeStep.latitude != null && activeStep.longitude != null) {
                        val destination = GeoPoint(activeStep.latitude, activeStep.longitude)

                        scope.launch {
                            // Obtener ruta real usando OSRM
                            val realRoute = RouteService.getRoute(current, destination)

                            // Eliminar polyline anterior si existe
                            routePolyline?.let { MapHelper.removePolyline(map, it) }

                            // Agregar nueva ruta
                            routePolyline = MapHelper.addRoutePolyline(map, realRoute)

                            // Ajustar zoom para mostrar toda la ruta
                            MapHelper.fitBoundsToRoute(map, realRoute)

                            vm.onRouteReceived(realRoute)
                        }
                    }
                }
            }
        }
    }

    // Escuchar eventos para actualizar ruta y manejar el movimiento
    LaunchedEffect(Unit) {
        vm.events.collect { event ->
            when (event) {
                is DeliveryEvent.UpdateRoute -> {
                    mapView?.let { map ->
                        scope.launch {
                            val realRoute = RouteService.getRoute(event.from, event.to)
                            routePolyline?.let { MapHelper.removePolyline(map, it) }
                            routePolyline = MapHelper.addRoutePolyline(map, realRoute)

                            // Guardar la ruta en el ViewModel para simulación
                            vm.onRouteReceived(realRoute)
                        }
                    }
                }
                is DeliveryEvent.UpdateDriverPosition -> {
                    // Actualizar posición del marcador del repartidor
                    mapView?.let { map ->
                        currentLocation = event.position

                        if (currentLocationMarker == null) {
                            currentLocationMarker = MapHelper.addMarker(
                                map, event.position, "Repartidor", ""
                            )
                        } else {
                            MapHelper.updateMarker(currentLocationMarker!!, event.position, map)
                        }

                        // Agregar punto al path recorrido (polyline azul)
                        pathPoints.add(event.position)
                        if (pathPolyline == null) {
                            pathPolyline = MapHelper.addPolyline(map, pathPoints)
                        } else {
                            MapHelper.updatePolyline(pathPolyline!!, pathPoints, map)
                        }
                    }
                }
                is DeliveryEvent.StepCompleted -> {
                    // Mover el marcador del repartidor al punto de recogida/entrega completado
                    mapView?.let { map ->
                        val completedStep = uiState.tripSteps.getOrNull(event.stepIndex - 1)
                        if (completedStep != null &&
                            completedStep.latitude != null &&
                            completedStep.longitude != null) {
                            val completedPosition = GeoPoint(
                                completedStep.latitude,
                                completedStep.longitude
                            )

                            currentLocationMarker?.let { marker ->
                                MapHelper.updateMarker(marker, completedPosition, map)
                            }

                            currentLocation = completedPosition
                        }
                    }
                }
                else -> {}
            }
        }
    }

    Scaffold(
        containerColor = colors.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHost) },
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
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
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = colors.onSurface
                        )
                    }
                    Text(
                        text = "Entrega en progreso",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = colors.onSurface
                        ),
                        modifier = Modifier.align(Alignment.Center)
                    )
                    Row(
                        modifier = Modifier.align(Alignment.CenterEnd),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { /* overflow */ }) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = "Más opciones",
                                tint = colors.onSurface
                            )
                        }
                    }
                }
            }
        }
    ) { inner ->
        // LAYOUT CORREGIDO: Column con pesos balanceados
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
        ) {
            // Header de estado
            HeaderCard(
                eta = uiState.estimatedTime,
                orderNumber = uiState.order?.orderNumber ?: "—",
                title = if (uiState.isPickupStep) "En camino a recogida" else "En camino a entrega"
            )

            //MAPA: ocupa 50% de la pantalla
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.5f) // 50% del espacio disponible
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AndroidView(
                        factory = { ctx ->
                            createMapView(ctx, isDarkTheme).also { mapView = it }
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    // Overlay con estado de GPS
                    if (currentLocation != null) {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(12.dp),
                            color = colors.primaryContainer,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.GpsFixed,
                                    contentDescription = null,
                                    tint = colors.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    "GPS Activo",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            }

            //DETALLES: ocupa 50% de la pantalla con scroll
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.5f), // 50% del espacio disponible
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                border = BorderStroke(1.dp, colors.outline.copy(alpha = 0.12f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(4.dp)
                            .background(
                                colors.onSurfaceVariant.copy(alpha = 0.35f),
                                RoundedCornerShape(2.dp)
                            )
                            .align(Alignment.CenterHorizontally)
                    )
                    Spacer(Modifier.height(16.dp))

                    // Pasos del viaje (scrolleable)
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        // Título
                        item {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = 12.dp)
                            ) {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = colors.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Pasos del viaje",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.onSurface
                                )
                            }
                            Spacer(Modifier.height(12.dp))
                        }

                        // Cliente
                        item {
                            CustomerNoteCard(
                                initials = uiState.order?.customerInitials ?: "—",
                                nameWithAge = "${uiState.order?.customerName ?: "Cliente"} · ${uiState.order?.createdDaysAgo ?: 0}d",
                                note = uiState.order?.customerNote ?: ""
                            )
                            Spacer(Modifier.height(16.dp))
                        }

                        // Pasos del viaje
                        items(uiState.tripSteps.size) { index ->
                            val step = uiState.tripSteps[index]
                            StepContainer(
                                isCompleted = step.isCompleted,
                                isActive = step.isActive
                            ) {
                                TripStepRow(
                                    step = step,
                                    showConnector = index < uiState.tripSteps.lastIndex
                                )
                            }
                            if (index < uiState.tripSteps.lastIndex) {
                                Spacer(Modifier.height(12.dp))
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Acciones (fijas al fondo)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { vm.onNavigate() },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.onSurface),
                            border = BorderStroke(1.dp, colors.onSurface.copy(alpha = 0.12f)),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Text("Navegar")
                        }
                        OutlinedButton(
                            onClick = { vm.onCall() },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.onSurface),
                            border = BorderStroke(1.dp, colors.onSurface.copy(alpha = 0.12f)),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Text("Llamar")
                        }
                        OutlinedButton(
                            onClick = { vm.onChat() },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.onSurface),
                            border = BorderStroke(1.dp, colors.onSurface.copy(alpha = 0.12f)),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Text("Chatear")
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    val ctaLabel = when {
                        uiState.isPickupStep -> "Marcar como recogido"
                        uiState.isDeliveryStep -> "Marcar como entregado"
                        else -> "Continuar"
                    }

                    Button(
                        onClick = { vm.onMarkStepComplete() },
                        enabled = uiState.canMarkComplete && !uiState.isMarkingComplete,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        if (uiState.isMarkingComplete) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(ctaLabel, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }


        // Diálogo de permisos
        if (!locationPermissions.allPermissionsGranted) {
            AlertDialog(
                onDismissRequest = { },
                title = { Text("Permisos necesarios") },
                text = { Text("Se necesitan permisos de ubicación para mostrar el mapa y rastrear la entrega.") },
                confirmButton = {
                    Button(onClick = { locationPermissions.launchMultiplePermissionRequest() }) {
                        Text("Otorgar permisos")
                    }
                },
                dismissButton = {
                    Button(onClick = onBackClick) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

// Componentes auxiliares (sin cambios)
@Composable
fun TripStepRow(step: TripStep, showConnector: Boolean) {
    val colors = MaterialTheme.colorScheme
    Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(40.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            step.isCompleted -> colors.tertiary
                            step.isActive -> colors.primary
                            else -> colors.surface
                        }
                    )
                    .border(
                        width = 2.dp,
                        color = when {
                            step.isCompleted -> colors.tertiary
                            step.isActive -> colors.primary
                            else -> colors.outline
                        },
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (step.isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completado",
                        tint = colors.onTertiary,
                        modifier = Modifier.size(18.dp)
                    )
                } else {
                    Text(
                        step.stepNumber.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            step.isActive -> colors.onPrimary
                            else -> colors.onSurfaceVariant
                        }
                    )
                }
            }

            if (showConnector) {
                Spacer(Modifier.height(6.dp))
                Box(modifier = Modifier.width(2.dp).height(44.dp)) {
                    Box(
                        modifier = Modifier.fillMaxSize()
                            .background(
                                if (step.isCompleted) colors.tertiary
                                else colors.outline.copy(alpha = 0.8f)
                            )
                    )
                }
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.padding(top = 4.dp)) {
            Text(
                text = step.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = if (step.isActive) FontWeight.Bold else FontWeight.Medium,
                color = if (step.isActive) colors.primary else colors.onSurface
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = step.address,
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant
            )
            Spacer(Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = colors.onSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = step.timeWindow,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun HeaderCard(eta: String, orderNumber: String, title: String) {
    val colors = MaterialTheme.colorScheme
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        shape = RoundedCornerShape(0.dp),
        border = BorderStroke(1.dp, colors.outline.copy(alpha = 0.12f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.LocalShipping,
                    contentDescription = "Estado",
                    tint = colors.onSurface
                )
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(title, style = MaterialTheme.typography.titleMedium)
                    Text(
                        orderNumber,
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.onSurfaceVariant
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    eta,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "ETA",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.onSurfaceVariant
                )
            }
        }
    }
}

private fun createMapView(context: Context, isDarkTheme: Boolean): MapView {
    return MapView(context).apply {
        setMultiTouchControls(true)

        // Ubicación inicial (Bogotá, Colombia)
        val defaultLocation = GeoPoint(4.6097, -74.0817)
        controller.setCenter(defaultLocation)
        controller.setZoom(15.0)

        // Aplicar estilo según el tema
        if (isDarkTheme) {
            MapStyleHelper.applyDarkStyle(this)
        } else {
            MapStyleHelper.applyLightStyle(this)
        }
    }
}

@Composable
fun StepContainer(
    isCompleted: Boolean = false,
    isActive: Boolean = false,
    content: @Composable () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    val containerColor = when {
        isCompleted -> colors.tertiaryContainer // Verde suave para completado
        isActive -> colors.primaryContainer // Color primario para activo
        else -> colors.surface // Normal
    }

    val borderColor = when {
        isCompleted -> colors.tertiary
        isActive -> colors.primary
        else -> colors.outline
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(containerColor, RoundedCornerShape(12.dp))
            .border(2.dp, borderColor, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        content()
    }
}

@Composable
fun CustomerNoteCard(initials: String, nameWithAge: String, note: String) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, colors.outline, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(colors.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    initials,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = colors.onPrimary
                )
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    nameWithAge,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                if (note.isNotBlank()) {
                    Text("\"$note\"", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                }
            }
        }
    }
}