package com.tacticaldev.tacticaldev2.data.repository

import com.tacticaldev.tacticaldev2.data.local.VisitDao
import com.tacticaldev.tacticaldev2.data.local.VisitEntity
import kotlinx.coroutines.flow.Flow

class VisitRepository(private val visitDao: VisitDao) {

    val allVisits: Flow<List<VisitEntity>> = visitDao.getAllVisits()

    suspend fun insertVisit(visita: VisitEntity) {
        visitDao.insertVisit(visita)
    }
}
