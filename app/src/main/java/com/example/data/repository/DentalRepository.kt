package com.example.data.repository

import com.example.data.local.DentalDatabase
import com.example.data.local.entity.ClinicalMediaEntity
import com.example.data.local.entity.OdontogramEntity
import com.example.data.local.entity.PatientEntity
import com.example.data.local.entity.VisitEntity
import com.example.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class DentalRepository(private val database: DentalDatabase) {
    private val patientDao = database.patientDao()
    private val visitDao = database.visitDao()
    private val odontogramDao = database.odontogramDao()
    private val clinicalMediaDao = database.clinicalMediaDao()

    // Patients
    val allPatients: Flow<List<Patient>> = patientDao.getAllPatients().map { list ->
        list.map { it.toDomain() }
    }

    val patientCount: Flow<Int> = patientDao.getPatientCount()

    fun getPatientById(id: String): Flow<Patient?> = patientDao.getPatientById(id).map {
        it?.toDomain()
    }

    suspend fun getPatientByIdDirect(id: String): Patient? =
        patientDao.getPatientByIdDirect(id)?.toDomain()

    fun searchPatients(query: String): Flow<List<Patient>> =
        patientDao.searchPatients(query).map { list -> list.map { it.toDomain() } }

    suspend fun insertPatient(patient: Patient) {
        patientDao.insertPatient(PatientEntity.fromDomain(patient))
    }

    suspend fun updatePatient(patient: Patient) {
        patientDao.updatePatient(PatientEntity.fromDomain(patient))
    }

    suspend fun deletePatient(patientId: String) {
        patientDao.deletePatientById(patientId)
    }

    // Visits
    fun getVisitsForPatient(patientId: String): Flow<List<Visit>> =
        visitDao.getVisitsForPatient(patientId).map { list -> list.map { it.toDomain() } }

    suspend fun getVisitsForPatientDirect(patientId: String): List<Visit> =
        visitDao.getVisitsForPatientDirect(patientId).map { it.toDomain() }

    val allVisits: Flow<List<Visit>> = visitDao.getAllVisits().map { list ->
        list.map { it.toDomain() }
    }

    val totalBilled: Flow<Double> = visitDao.getTotalBilled().map { it ?: 0.0 }
    val totalPaid: Flow<Double> = visitDao.getTotalPaid().map { it ?: 0.0 }
    val visitCount: Flow<Int> = visitDao.getVisitCount()

    suspend fun insertVisit(visit: Visit) {
        visitDao.insertVisit(VisitEntity.fromDomain(visit))
    }

    suspend fun updateVisit(visit: Visit) {
        visitDao.updateVisit(VisitEntity.fromDomain(visit))
    }

    suspend fun deleteVisit(visitId: String) {
        visitDao.deleteVisitById(visitId)
    }

    // Odontogram
    fun getOdontogramForPatient(patientId: String): Flow<List<OdontogramRecord>> =
        odontogramDao.getOdontogramForPatient(patientId).map { list -> list.map { it.toDomain() } }

    suspend fun getOdontogramForPatientDirect(patientId: String): List<OdontogramRecord> =
        odontogramDao.getOdontogramForPatientDirect(patientId).map { it.toDomain() }

    fun getToothRecord(patientId: String, toothNumber: Int): Flow<OdontogramRecord?> =
        odontogramDao.getToothRecord(patientId, toothNumber).map { it?.toDomain() }

    suspend fun updateToothStatus(
        patientId: String,
        toothNumber: Int,
        status: ToothStatus,
        surfaces: List<String>
    ) {
        val record = OdontogramRecord(
            id = UUID.randomUUID().toString(),
            patientId = patientId,
            toothNumber = toothNumber,
            status = status,
            surfaces = surfaces,
            updatedAt = System.currentTimeMillis()
        )
        odontogramDao.insertOrUpdateTooth(OdontogramEntity.fromDomain(record))
    }

    suspend fun resetTooth(patientId: String, toothNumber: Int) {
        odontogramDao.resetTooth(patientId, toothNumber)
    }

    suspend fun clearOdontogram(patientId: String) {
        odontogramDao.clearOdontogramForPatient(patientId)
    }

    // Clinical Media
    fun getMediaForPatient(patientId: String): Flow<List<ClinicalMedia>> =
        clinicalMediaDao.getMediaForPatient(patientId).map { list -> list.map { it.toDomain() } }

    suspend fun getMediaForPatientDirect(patientId: String): List<ClinicalMedia> =
        clinicalMediaDao.getMediaForPatientDirect(patientId).map { it.toDomain() }

    fun getMediaForVisit(visitId: String): Flow<List<ClinicalMedia>> =
        clinicalMediaDao.getMediaForVisit(visitId).map { list -> list.map { it.toDomain() } }

    suspend fun insertMedia(media: ClinicalMedia) {
        clinicalMediaDao.insertMedia(ClinicalMediaEntity.fromDomain(media))
    }

    suspend fun updateMedia(media: ClinicalMedia) {
        clinicalMediaDao.updateMedia(ClinicalMediaEntity.fromDomain(media))
    }

    suspend fun deleteMedia(mediaId: String) {
        clinicalMediaDao.deleteMediaById(mediaId)
    }
}
