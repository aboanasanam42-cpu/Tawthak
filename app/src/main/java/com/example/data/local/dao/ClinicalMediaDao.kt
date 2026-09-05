package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.ClinicalMediaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClinicalMediaDao {
    @Query("SELECT * FROM clinical_media WHERE patient_id = :patientId ORDER BY created_at DESC")
    fun getMediaForPatient(patientId: String): Flow<List<ClinicalMediaEntity>>

    @Query("SELECT * FROM clinical_media WHERE patient_id = :patientId ORDER BY created_at DESC")
    suspend fun getMediaForPatientDirect(patientId: String): List<ClinicalMediaEntity>

    @Query("SELECT * FROM clinical_media WHERE visit_id = :visitId ORDER BY created_at DESC")
    fun getMediaForVisit(visitId: String): Flow<List<ClinicalMediaEntity>>

    @Query("SELECT * FROM clinical_media WHERE id = :id LIMIT 1")
    fun getMediaById(id: String): Flow<ClinicalMediaEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedia(media: ClinicalMediaEntity)

    @Update
    suspend fun updateMedia(media: ClinicalMediaEntity)

    @Query("DELETE FROM clinical_media WHERE id = :id")
    suspend fun deleteMediaById(id: String)
}
