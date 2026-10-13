package com.tacticaldev.tacticaldev2.ui.screens.form

import android.Manifest
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.tacticaldev.tacticaldev2.utils.CameraHelper
import com.tacticaldev.tacticaldev2.utils.LocationHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisitFormScreen(
    viewModel: VisitFormViewModel = viewModel(),
    onVisitSaved: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // ViewModel States
    val puntoDeVenta by viewModel.puntoDeVenta.collectAsState()
    val tipoVisita by viewModel.tipoVisita.collectAsState()
    val estadoEquipo by viewModel.estadoEquipo.collectAsState()
    val observaciones by viewModel.observaciones.collectAsState()
    val fotoUri by viewModel.fotoUri.collectAsState()
    val ubicacionGps by viewModel.ubicacionGps.collectAsState()

    // Error States
    val puntoDeVentaError by viewModel.puntoDeVentaError.collectAsState()
    val observacionesError by viewModel.observacionesError.collectAsState()
    val fotoError by viewModel.fotoError.collectAsState()
    val gpsError by viewModel.gpsError.collectAsState()

    // Temporary URI for camera capture
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraUri != null) {
            viewModel.onFotoUriChanged(tempCameraUri.toString())
        }
    }

    // Camera permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val uri = CameraHelper.createImageFileUri(context)
            tempCameraUri = uri
            cameraLauncher.launch(uri)
        } else {
            Toast.makeText(context, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
        }
    }

    // Location helper and launcher
    val locationHelper = remember { LocationHelper(context) }
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineGranted || coarseGranted) {
            locationHelper.getCurrentLocation(
                onLocationReceived = { coords ->
                    viewModel.onUbicacionGpsChanged(coords)
                },
                onError = { error ->
                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                }
            )
        } else {
            Toast.makeText(context, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registro de Visita a Vitrina") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Punto de Venta
            OutlinedTextField(
                value = puntoDeVenta,
                onValueChange = viewModel::onPuntoDeVentaChanged,
                label = { Text("Punto de Venta *") },
                leadingIcon = { Icon(Icons.Default.Store, contentDescription = null) },
                isError = puntoDeVentaError != null,
                trailingIcon = {
                    if (puntoDeVentaError != null) {
                        Icon(Icons.Default.Error, contentDescription = "Error", tint = MaterialTheme.colorScheme.error)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            AnimatedVisibility(
                visible = puntoDeVentaError != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                if (puntoDeVentaError != null) {
                    Text(
                        text = puntoDeVentaError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // 2. Tipo de Visita
            OutlinedTextField(
                value = tipoVisita,
                onValueChange = viewModel::onTipoVisitaChanged,
                label = { Text("Tipo de Visita") },
                leadingIcon = { Icon(Icons.Default.Assignment, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // 3. Estado del Equipo
            OutlinedTextField(
                value = estadoEquipo,
                onValueChange = viewModel::onEstadoEquipoChanged,
                label = { Text("Estado del Equipo") },
                leadingIcon = { Icon(Icons.Default.Build, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // 4. Observaciones
            OutlinedTextField(
                value = observaciones,
                onValueChange = viewModel::onObservacionesChanged,
                label = { Text("Observaciones (mín. 5 caracteres) *") },
                leadingIcon = { Icon(Icons.Default.Comment, contentDescription = null) },
                isError = observacionesError != null,
                trailingIcon = {
                    if (observacionesError != null) {
                        Icon(Icons.Default.Error, contentDescription = "Error", tint = MaterialTheme.colorScheme.error)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            AnimatedVisibility(
                visible = observacionesError != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                if (observacionesError != null) {
                    Text(
                        text = observacionesError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // 5. Sección GPS
            OutlinedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Ubicación GPS *",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Button(
                            onClick = {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("Obtener GPS")
                        }
                    }
                    Text(
                        text = if (ubicacionGps.isBlank()) "Sin ubicación GPS registrada" else ubicacionGps,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (ubicacionGps.isBlank()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                    AnimatedVisibility(
                        visible = gpsError != null,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        if (gpsError != null) {
                            Text(
                                text = gpsError!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            // 6. Sección de Foto de Evidencia
            OutlinedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Evidencia Fotográfica *",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Button(
                            onClick = {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("Tomar Foto")
                        }
                    }

                    AnimatedVisibility(
                        visible = fotoUri.isNotBlank(),
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        if (fotoUri.isNotBlank()) {
                            AsyncImage(
                                model = Uri.parse(fotoUri),
                                contentDescription = "Vista previa foto",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .padding(top = 8.dp)
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = fotoError != null,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        if (fotoError != null) {
                            Text(
                                text = fotoError!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // 7. Botón Registrar Visita
            Button(
                onClick = {
                    viewModel.validarYGuardar(
                        onSuccess = {
                            Toast.makeText(context, "Visita registrada exitosamente", Toast.LENGTH_LONG).show()
                            onVisitSaved()
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Registrar Visita", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
