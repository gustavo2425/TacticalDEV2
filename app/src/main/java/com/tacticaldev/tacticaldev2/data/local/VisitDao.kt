package com.tacticaldev.tacticaldev2.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface VisitDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVisit(visita: VisitEntity)

    @Query("SELECT * FROM visitas ORDER BY id DESC")
    fun getAllVisits(): Flow<List<VisitEntity>>
}
