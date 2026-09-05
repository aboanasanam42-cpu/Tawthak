package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.PatientEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PatientDao {
    @Query("SELECT * FROM patients ORDER BY created_at DESC")
    fun getAllPatients(): Flow<List<PatientEntity>>

    @Query("SELECT * FROM patients WHERE id = :id LIMIT 1")
    fun getPatientById(id: String): Flow<PatientEntity?>

    @Query("SELECT * FROM patients WHERE id = :id LIMIT 1")
    suspend fun getPatientByIdDirect(id: String): PatientEntity?

    @Query("SELECT * FROM patients WHERE full_name LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%' ORDER BY created_at DESC")
    fun searchPatients(query: String): Flow<List<PatientEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPatient(patient: PatientEntity)

    @Update
    suspend fun updatePatient(patient: PatientEntity)

    @Query("DELETE FROM patients WHERE id = :id")
    suspend fun deletePatientById(id: String)

    @Query("SELECT COUNT(*) FROM patients")
    fun getPatientCount(): Flow<Int>
}
