package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.VisitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VisitDao {
    @Query("SELECT * FROM visits WHERE patient_id = :patientId ORDER BY visit_date DESC")
    fun getVisitsForPatient(patientId: String): Flow<List<VisitEntity>>

    @Query("SELECT * FROM visits WHERE patient_id = :patientId ORDER BY visit_date DESC")
    suspend fun getVisitsForPatientDirect(patientId: String): List<VisitEntity>

    @Query("SELECT * FROM visits ORDER BY visit_date DESC")
    fun getAllVisits(): Flow<List<VisitEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVisit(visit: VisitEntity)

    @Update
    suspend fun updateVisit(visit: VisitEntity)

    @Query("DELETE FROM visits WHERE id = :id")
    suspend fun deleteVisitById(id: String)

    @Query("SELECT SUM(cost) FROM visits")
    fun getTotalBilled(): Flow<Double?>

    @Query("SELECT SUM(paid) FROM visits")
    fun getTotalPaid(): Flow<Double?>

    @Query("SELECT COUNT(*) FROM visits")
    fun getVisitCount(): Flow<Int>
}
