package com.tacticaldev.tacticaldev2.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "visitas")
data class VisitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val puntoDeVenta: String,
    val tipoVisita: String,
    val estadoEquipo: String,
    val observaciones: String,
    val fotoUri: String,
    val ubicacionGps: String,
    val fechaHora: String
)
