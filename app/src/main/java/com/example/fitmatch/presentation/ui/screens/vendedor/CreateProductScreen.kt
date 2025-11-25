package com.example.fitmatch.presentation.ui.screens.vendedor

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import android.provider.MediaStore
import android.widget.Toast
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import kotlin.collections.minus
import kotlin.collections.plus
import com.example.compose.FitMatchTheme
import com.example.fitmatch.presentation.viewmodel.vendedor.CreateProductViewModel
import java.io.File
import com.google.accompanist.permissions.PermissionStatus





enum class MediaType { IMAGE, VIDEO }

@Parcelize
data class SelectedMedia(
    val uri: String,
    val type: MediaType
) : Parcelable
private fun createTempImageUri(context: Context): Uri {
    val dir = File(context.cacheDir, "camera").apply { mkdirs() }
    val file = File.createTempFile("photo_", ".jpg", dir)
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}



data class SizeOption(
    val id: String,
    val name: String
)

data class ColorOption(
    val id: String,
    val name: String,
    val color: Color
)

/* ===================== PANTALLA PRINCIPAL  ===================== */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateProductScreen(
    onCloseClick: () -> Unit = {},
    onSaveDraftClick: () -> Unit = {},
    onPublishClick: () -> Unit = {}
) {
    val viewModel: CreateProductViewModel = viewModel()
    val colors = MaterialTheme.colorScheme
    val shapes = MaterialTheme.shapes
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()


    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var price by rememberSaveable { mutableStateOf("") }
    var sizeGuide by rememberSaveable { mutableStateOf("") }
    var labels by rememberSaveable { mutableStateOf("") }

    //estado de medios y navegación a la segunda pantalla
    var media by rememberSaveable { mutableStateOf(listOf<SelectedMedia>()) }
    var showMediaScreen by remember { mutableStateOf(false) }        // “otra ventana”
    var mediaStartAsVideo by remember { mutableStateOf(false) }      // abrir como video/foto

    // sets de selección
    var selectedSizes by remember { mutableStateOf(setOf("s")) }     // S marcado por defecto
    var selectedColors by remember { mutableStateOf(setOf("negro")) } // Negro marcado por defecto

    // catálogos
    val sizeOptions = listOf(
        SizeOption("xs", "XS"), SizeOption("s", "S"),
        SizeOption("m", "M"), SizeOption("l", "L"), SizeOption("xl", "XL")
    )
    val colorOptions = listOf(
        ColorOption("negro", "Negro", Color(0xFF424242)),
        ColorOption("blanco", "Blanco", Color(0xFFF5F5F5)),
        ColorOption("rojo", "Rojo", Color(0xFFF44336)),
        ColorOption("beige", "Beige", Color(0xFFF5F5DC))
    )
    val context = LocalContext.current
    // validación simple para publicar
    val canPublish = title.isNotBlank() && price.isNotBlank() && media.size in 3..10 && !uiState.isPublishing

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.onMessageConsumed()
        }
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            Toast.makeText(context, "Producto publicado", Toast.LENGTH_SHORT).show()
            onPublishClick()
            viewModel.onMessageConsumed()
        }
    }


    if (showMediaScreen) {
        MediaView(
            isVideoStart = mediaStartAsVideo,
            onBack = { showMediaScreen = false },
            onAccept = { item ->
                val currentVideos = media.count { it.type == MediaType.VIDEO }
                val newVideos = currentVideos + if (item.type == MediaType.VIDEO) 1 else 0
                when {
                    media.size >= 10 -> {
                        Toast.makeText(context, "Máximo 10 elementos.", Toast.LENGTH_SHORT).show()
                    }
                    newVideos > 3 -> {
                        Toast.makeText(context, "Máximo 3 videos.", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        media = (media + item).distinctBy { it.uri }
                        showMediaScreen = false
                    }
                }
            }
        )
        return
    }



    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        //header
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = colors.surface,
                tonalElevation = 1.dp,
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Crear publicación",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = colors.onSurface
                        )
                    )
                    IconButton(onClick = onCloseClick) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = colors.onSurface
                        )
                    }
                }
            }
        }

        //selector fotos/videos
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(media, key = { it.uri }) { item ->
                        MediaThumb(
                            item = item,
                            onRemove = {
                                media = media.filterNot { m -> m.uri == item.uri }
                            }
                        )
                    }


                    item {
                        Surface(
                            onClick = {
                                mediaStartAsVideo = false
                                showMediaScreen = true
                            },
                            modifier = Modifier.size(80.dp),
                            shape = CircleShape,
                            color = colors.primary
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Agregar más",
                                    tint = colors.onPrimary
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Agrega entre 3 y 10 elementos. Máximo 3 videos.",
                    style = MaterialTheme.typography.bodySmall.copy(color = colors.onSurfaceVariant),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        //campos para subir la prenda
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Título
                LabeledField(label = "Título") {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.outline,
                            cursorColor = colors.primary,
                            focusedLabelColor = colors.primary,
                            focusedContainerColor = colors.surface,
                            unfocusedContainerColor = colors.surface,
                            focusedTextColor = colors.onSurface,
                            unfocusedTextColor = colors.onSurface
                        ),
                        shape = shapes.medium
                    )
                }

                // Descripción
                LabeledField(label = "Descripción") {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.outline,
                            cursorColor = colors.primary,
                            focusedLabelColor = colors.primary,
                            focusedContainerColor = colors.surface,
                            unfocusedContainerColor = colors.surface
                        ),
                        shape = shapes.medium
                    )
                }

                // Precio
                LabeledField(label = "Precio") {
                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.outline,
                            cursorColor = colors.primary,
                            focusedLabelColor = colors.primary,
                            focusedContainerColor = colors.surface,
                            unfocusedContainerColor = colors.surface
                        ),
                        shape = shapes.medium
                    )
                }

                // Tallas
                Column {
                    Text(
                        text = "Tallas",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Medium,
                            color = colors.onSurface
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(sizeOptions) { size ->
                            val selected = size.id in selectedSizes
                            SizeChip(
                                size = size.name,
                                selected = selected,
                                onToggle = {
                                    selectedSizes = if (selected) selectedSizes - size.id else selectedSizes + size.id
                                }
                            )
                        }
                    }
                }

                // Colores
                Column {
                    Text(
                        text = "Colores",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Medium,
                            color = colors.onSurface
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(colorOptions) { colorOpt ->
                            val selected = colorOpt.id in selectedColors
                            ColorChip(
                                name = colorOpt.name,
                                swatch = colorOpt.color,
                                selected = selected,
                                onToggle = {
                                    selectedColors = if (selected) selectedColors - colorOpt.id else selectedColors + colorOpt.id
                                }
                            )
                        }
                    }
                }

                // Guía de tallas y etiquetas
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LabeledField(label = "Guía de Tallas", modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = sizeGuide,
                            onValueChange = { sizeGuide = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colors.primary,
                                unfocusedBorderColor = colors.outline,
                                cursorColor = colors.primary,
                                focusedLabelColor = colors.primary,
                                focusedContainerColor = colors.surface,
                                unfocusedContainerColor = colors.surface
                            ),
                            shape = shapes.medium
                        )
                    }
                    LabeledField(label = "Etiquetas", modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = labels,
                            onValueChange = { labels = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colors.primary,
                                unfocusedBorderColor = colors.outline,
                                cursorColor = colors.primary,
                                focusedLabelColor = colors.primary,
                                focusedContainerColor = colors.surface,
                                unfocusedContainerColor = colors.surface
                            ),
                            shape = shapes.medium
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                Spacer(Modifier.height(16.dp))


                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onSaveDraftClick,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = colors.primary
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = SolidColor(colors.outline)
                        ),
                        shape = shapes.large
                    ) {
                        Text(
                            text = "Guardar borrador",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }

                    Button(
                        onClick = {
                            val videosCount = media.count { it.type == MediaType.VIDEO } // ⬅️ opcional, por limpieza
                            val priceValue = price.toIntOrNull()
                            val imageUris = media.filter { it.type == MediaType.IMAGE }.map { Uri.parse(it.uri) }
                            val tags = labels.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                            when {
                                media.size < 3 -> Toast.makeText(context, "Mínimo 3 elementos.", Toast.LENGTH_SHORT).show()
                                media.size > 10 -> Toast.makeText(context, "Máximo 10 elementos.", Toast.LENGTH_SHORT).show()
                                videosCount > 3 -> Toast.makeText(context, "Máximo 3 videos.", Toast.LENGTH_SHORT).show()
                                media.any { it.type == MediaType.VIDEO } -> Toast.makeText(context, "Por ahora solo se suben imágenes.", Toast.LENGTH_SHORT).show()
                                priceValue == null -> Toast.makeText(context, "Ingresa un precio válido", Toast.LENGTH_SHORT).show()
                                imageUris.isEmpty() -> Toast.makeText(context, "Agrega al menos una imagen.", Toast.LENGTH_SHORT).show()
                                else -> viewModel.publishProduct(
                                    title = title,
                                    description = description,
                                    price = priceValue,
                                    sizes = selectedSizes.toList(),
                                    colors = selectedColors.toList(),
                                    tags = tags,
                                    mediaUris = imageUris
                                )
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = canPublish,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primary,
                            contentColor = colors.onPrimary,
                            disabledContainerColor = colors.surfaceVariant,
                            disabledContentColor = colors.onSurfaceVariant
                        ),
                        shape = shapes.large
                    ) {
                        if (uiState.isPublishing) {
                            CircularProgressIndicator(
                                color = colors.onPrimary,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Text(
                                text = "Publicar",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium)
                            )
                        }
                        Text(
                            text = "Publicar",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium)
                        )
                    }
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

/* ===================== MEDIA VIEW: permisos con Accompanist ===================== */

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun MediaView(
    isVideoStart: Boolean,
    onBack: () -> Unit,
    onAccept: (SelectedMedia) -> Unit
) {
    val ctx = LocalContext.current

    var recordingMode by remember { mutableStateOf(isVideoStart) } // false=foto, true=video
    var currentUri by remember { mutableStateOf<Uri?>(null) }
    var lastPickedType by remember { mutableStateOf(MediaType.IMAGE) }

    /* ------------ PERMISOS: una variable por permiso (flujo clásico) ------------ */
    val cameraPerm = rememberPermissionState(Manifest.permission.CAMERA)
    val audioPerm = rememberPermissionState(Manifest.permission.RECORD_AUDIO)

    // Permiso de lectura según versión
    val needsReadMedia = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    val readImagesPerm = if (needsReadMedia) rememberPermissionState(Manifest.permission.READ_MEDIA_IMAGES) else null
    val readVideoPerm  = if (needsReadMedia) rememberPermissionState(Manifest.permission.READ_MEDIA_VIDEO) else null
    val readExternalPerm = if (!needsReadMedia) rememberPermissionState(Manifest.permission.READ_EXTERNAL_STORAGE) else null

    fun ensureCameraForPhoto(onOk: () -> Unit) {
        when (val s = cameraPerm.status) {
            is PermissionStatus.Granted -> onOk()
            is PermissionStatus.Denied -> {
                // 1) Estado: no concedido
                // 2) Si NO fue concedido de forma permanente -> rationale (Accompanist: shouldShowRationale = true)
                if (s.shouldShowRationale) {
                    Toast.makeText(ctx, "Necesitamos la Cámara para tomar la foto.", Toast.LENGTH_SHORT).show()
                } else {
                    // 3) Solicitar (primer uso o denegado con “no volver a preguntar”)
                    cameraPerm.launchPermissionRequest()
                }
            }
        }
    }


    fun ensureCameraForVideo(onOk: () -> Unit) {
        val camGranted = cameraPerm.status is PermissionStatus.Granted
        val micGranted = audioPerm.status is PermissionStatus.Granted

        when {
            camGranted && micGranted -> onOk()

            !camGranted -> {
                when (val s = cameraPerm.status) {
                    is PermissionStatus.Granted -> {  }
                    is PermissionStatus.Denied -> {
                        if (s.shouldShowRationale) {
                            Toast.makeText(ctx, "Activa la Cámara para grabar video.", Toast.LENGTH_SHORT).show()
                        } else {
                            cameraPerm.launchPermissionRequest()
                        }
                    }
                }
            }

            camGranted && !micGranted -> {
                when (val s = audioPerm.status) {
                    is PermissionStatus.Granted -> {  }
                    is PermissionStatus.Denied -> {
                        if (s.shouldShowRationale) {
                            Toast.makeText(ctx, "El micrófono es recomendado para el audio del video.", Toast.LENGTH_SHORT).show()
                        } else {
                            audioPerm.launchPermissionRequest()
                        }
                    }
                }
            }
        }
    }

    fun ensureReadForGallery(isVideo: Boolean, onOk: () -> Unit) {
        if (needsReadMedia) {
            // Android 13+
            val target = if (isVideo) readVideoPerm!! else readImagesPerm!!
            when (val s = target.status) {
                is PermissionStatus.Granted -> onOk()
                is PermissionStatus.Denied -> {
                    if (s.shouldShowRationale) {
                        Toast.makeText(ctx, "Se requiere permiso para leer la galería.", Toast.LENGTH_SHORT).show()
                    } else {
                        target.launchPermissionRequest()
                    }
                }
            }
        } else {
            // Android 12-
            val target = readExternalPerm!!
            when (val s = target.status) {
                is PermissionStatus.Granted -> onOk()
                is PermissionStatus.Denied -> {
                    if (s.shouldShowRationale) {
                        Toast.makeText(ctx, "Se requiere permiso para leer el almacenamiento.", Toast.LENGTH_SHORT).show()
                    } else {
                        target.launchPermissionRequest()
                    }
                }
            }
        }
    }



    var pendingPhotoUri by remember { mutableStateOf<Uri?>(null) }

    val takePictureLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        if (ok) {
            currentUri = pendingPhotoUri
            lastPickedType = MediaType.IMAGE
        }
    }

    val captureVideoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { res ->
        if (res.resultCode == Activity.RESULT_OK) {
            val uri = res.data?.data
            if (uri != null) {
                currentUri = uri
                lastPickedType = MediaType.VIDEO
            }
        }
    }

    val pickFromGallery = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            currentUri = uri
            lastPickedType = if (recordingMode) MediaType.VIDEO else MediaType.IMAGE
        }
    }

    fun launchCameraPhoto() = ensureCameraForPhoto {
        val out = createTempImageUri(ctx)
        pendingPhotoUri = out
        takePictureLauncher.launch(out)
    }

    fun launchCameraVideo() = ensureCameraForVideo {
        val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_DURATION_LIMIT, 30) // opcional
            putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1)
        }
        captureVideoLauncher.launch(intent)
    }

    fun launchGalleryPick() = ensureReadForGallery(recordingMode) {
        pickFromGallery.launch(if (recordingMode) "video/*" else "image/*")
    }

    /* ----------------------------------- UI ----------------------------------- */

    Scaffold(topBar = {
        CenterAlignedTopAppBar(
            title = { Text("Media (Foto/Video)") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                }
            }
        )
    }) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Foto", fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(8.dp))
                Switch(checked = recordingMode, onCheckedChange = { recordingMode = it })
                Spacer(Modifier.width(8.dp))
                Text("Video", fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .size(300.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (currentUri != null) {
                    if (recordingMode) {
                        var vvRef: android.widget.VideoView? = null
                        AndroidView(factory = { ctxV ->
                            android.widget.VideoView(ctxV).apply {
                                setVideoURI(currentUri)
                                setOnPreparedListener { mp ->
                                    mp.isLooping = true
                                    start()
                                }
                            }.also { vvRef = it }
                        }, update = { view ->
                            if (!view.isPlaying) {
                                view.setVideoURI(currentUri)
                                view.start()
                            }
                        })
                        DisposableEffect(currentUri) {
                            onDispose { vvRef?.stopPlayback() }
                        }
                    } else {
                        AsyncImage(
                            model = currentUri,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                } else {
                    Text("Sin contenido", style = MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = { if (recordingMode) launchCameraVideo() else launchCameraPhoto() }) {
                    Text(if (recordingMode) "Grabar Video (Cámara)" else "Tomar Foto (Cámara)")
                }
                Button(onClick = { launchGalleryPick() }) {
                    Text(if (recordingMode) "Seleccionar Video (Galería)" else "Seleccionar Foto (Galería)")
                }
            }

            Spacer(Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onBack) { Text("Cancelar") }
                Button(
                    enabled = currentUri != null,
                    onClick = {
                        val uri = currentUri ?: return@Button
                        onAccept(SelectedMedia(uri.toString(), if (recordingMode) MediaType.VIDEO else MediaType.IMAGE))
                    }
                ) { Text("Aceptar") }
            }
        }
    }
}



@Composable
private fun MediaThumb(
    item: SelectedMedia,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier
            .size(80.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        when (item.type) {
            MediaType.IMAGE -> {
                AsyncImage(
                    model = item.uri,
                    contentDescription = "Imagen del producto",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            MediaType.VIDEO -> {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Video del producto",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        Icon(
            Icons.Default.Close, contentDescription = "Quitar",
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .clickable { onRemove() }
                .padding(2.dp)
        )
    }
}

@Composable
private fun LabeledField(
    label: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier.padding(bottom = 4.dp)
        )
        content()
    }
}

@Composable
private fun SizeChip(
    size: String,
    selected: Boolean,
    onToggle: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        onClick = onToggle,
        modifier = Modifier.size(40.dp),
        shape = CircleShape,
        color = if (selected) colors.primary else colors.surface,
        border = BorderStroke(
            1.dp,
            if (selected) colors.primary else colors.outline.copy(alpha = 0.6f)
        )
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(
                text = size,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Medium,
                    color = if (selected) colors.onPrimary else colors.onSurface
                )
            )
        }
    }
}

@Composable
private fun ColorChip(
    name: String,
    swatch: Color,
    selected: Boolean,
    onToggle: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        onClick = onToggle,
        modifier = Modifier.height(32.dp),
        shape = RoundedCornerShape(16.dp),
        color = colors.surface,
        border = BorderStroke(
            if (selected) 2.dp else 1.dp,
            if (selected) colors.primary else colors.outline.copy(alpha = 0.6f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(swatch)
                    .border(1.dp, colors.outline.copy(alpha = 0.4f), CircleShape)
            )
            Text(
                text = name,
                style = MaterialTheme.typography.labelMedium.copy(
                    color = colors.onSurface
                )
            )
        }
    }
}

/* ===================== PREVIEWS ===================== */

@Preview(showBackground = true, showSystemUi = true, name = "Create Product – Light")
@Composable
private fun CreateProductPreviewLight() {
    FitMatchTheme(darkTheme = false, dynamicColor = false) {
        CreateProductScreen()
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Create Product – Dark")
@Composable
private fun CreateProductPreviewDark() {
    FitMatchTheme(darkTheme = true, dynamicColor = false) {
        CreateProductScreen()
    }
}
