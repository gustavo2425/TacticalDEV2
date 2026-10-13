package com.tacticaldev.tacticaldev2.ui.screens.form

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tacticaldev.tacticaldev2.data.local.AppDatabase
import com.tacticaldev.tacticaldev2.data.local.VisitEntity
import com.tacticaldev.tacticaldev2.data.repository.VisitRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class VisitFormViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: VisitRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = VisitRepository(database.visitDao())
    }

    // Campos del formulario
    private val _puntoDeVenta = MutableStateFlow("")
    val puntoDeVenta: StateFlow<String> = _puntoDeVenta.asStateFlow()

    private val _tipoVisita = MutableStateFlow("Rutina")
    val tipoVisita: StateFlow<String> = _tipoVisita.asStateFlow()

    private val _estadoEquipo = MutableStateFlow("Óptimo")
    val estadoEquipo: StateFlow<String> = _estadoEquipo.asStateFlow()

    private val _observaciones = MutableStateFlow("")
    val observaciones: StateFlow<String> = _observaciones.asStateFlow()

    private val _fotoUri = MutableStateFlow("")
    val fotoUri: StateFlow<String> = _fotoUri.asStateFlow()

    private val _ubicacionGps = MutableStateFlow("")
    val ubicacionGps: StateFlow<String> = _ubicacionGps.asStateFlow()

    // Mensajes de error (100% centralizados en el ViewModel)
    private val _puntoDeVentaError = MutableStateFlow<String?>(null)
    val puntoDeVentaError: StateFlow<String?> = _puntoDeVentaError.asStateFlow()

    private val _observacionesError = MutableStateFlow<String?>(null)
    val observacionesError: StateFlow<String?> = _observacionesError.asStateFlow()

    private val _fotoError = MutableStateFlow<String?>(null)
    val fotoError: StateFlow<String?> = _fotoError.asStateFlow()

    private val _gpsError = MutableStateFlow<String?>(null)
    val gpsError: StateFlow<String?> = _gpsError.asStateFlow()

    // Métodos setter con validación en tiempo real
    fun onPuntoDeVentaChanged(nuevoValor: String) {
        _puntoDeVenta.value = nuevoValor
        _puntoDeVentaError.value = if (nuevoValor.isBlank()) {
            "El punto de venta es obligatorio"
        } else {
            null
        }
    }

    fun onTipoVisitaChanged(nuevoValor: String) {
        _tipoVisita.value = nuevoValor
    }

    fun onEstadoEquipoChanged(nuevoValor: String) {
        _estadoEquipo.value = nuevoValor
    }

    fun onObservacionesChanged(nuevoValor: String) {
        _observaciones.value = nuevoValor
        _observacionesError.value = if (nuevoValor.length < 5) {
            "Las observaciones deben tener al menos 5 caracteres"
        } else {
            null
        }
    }

    fun onFotoUriChanged(nuevoValor: String) {
        _fotoUri.value = nuevoValor
        _fotoError.value = if (nuevoValor.isBlank()) {
            "La evidencia fotográfica es obligatoria"
        } else {
            null
        }
    }

    fun onUbicacionGpsChanged(nuevoValor: String) {
        _ubicacionGps.value = nuevoValor
        _gpsError.value = if (nuevoValor.isBlank()) {
            "La ubicación GPS es obligatoria para confirmar la visita"
        } else {
            null
        }
    }

    // Función para validar todos los campos y persistir en la base de datos local
    fun validarYGuardar(onSuccess: () -> Unit) {
        // Forzar validación en todos los campos actuales
        onPuntoDeVentaChanged(_puntoDeVenta.value)
        onObservacionesChanged(_observaciones.value)
        onFotoUriChanged(_fotoUri.value)
        onUbicacionGpsChanged(_ubicacionGps.value)

        val hayErrores = _puntoDeVentaError.value != null ||
                _observacionesError.value != null ||
                _fotoError.value != null ||
                _gpsError.value != null

        if (!hayErrores) {
            viewModelScope.launch {
                val fechaActual = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                val visita = VisitEntity(
                    puntoDeVenta = _puntoDeVenta.value.trim(),
                    tipoVisita = _tipoVisita.value,
                    estadoEquipo = _estadoEquipo.value,
                    observaciones = _observaciones.value.trim(),
                    fotoUri = _fotoUri.value,
                    ubicacionGps = _ubicacionGps.value,
                    fechaHora = fechaActual
                )
                repository.insertVisit(visita)
                onSuccess()
            }
        }
    }
}
