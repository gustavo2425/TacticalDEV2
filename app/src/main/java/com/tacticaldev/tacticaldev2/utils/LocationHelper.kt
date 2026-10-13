package com.tacticaldev.tacticaldev2.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

class LocationHelper(private val context: Context) {
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    fun getCurrentLocation(
        onLocationReceived: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val hasFineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasCoarseLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasFineLocation && !hasCoarseLocation) {
            onError("Permisos de ubicación no concedidos")
            return
        }

        try {
            val cancellationTokenSource = CancellationTokenSource()
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            ).addOnSuccessListener { location ->
                if (location != null) {
                    val latLonStr = "Lat: ${location.latitude}, Long: ${location.longitude}"
                    onLocationReceived(latLonStr)
                } else {
                    fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc ->
                        if (lastLoc != null) {
                            val latLonStr = "Lat: ${lastLoc.latitude}, Long: ${lastLoc.longitude}"
                            onLocationReceived(latLonStr)
                        } else {
                            onError("No se pudo obtener la ubicación actual")
                        }
                    }.addOnFailureListener { e ->
                        onError("Error al obtener última ubicación: ${e.localizedMessage}")
                    }
                }
            }.addOnFailureListener { e ->
                onError("Error al obtener ubicación: ${e.localizedMessage}")
            }
        } catch (e: SecurityException) {
            onError("Excepción de seguridad en ubicación: ${e.localizedMessage}")
        }
    }
}
