package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.OdontogramEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OdontogramDao {
    @Query("SELECT * FROM odontogram_state WHERE patient_id = :patientId")
    fun getOdontogramForPatient(patientId: String): Flow<List<OdontogramEntity>>

    @Query("SELECT * FROM odontogram_state WHERE patient_id = :patientId")
    suspend fun getOdontogramForPatientDirect(patientId: String): List<OdontogramEntity>

    @Query("SELECT * FROM odontogram_state WHERE patient_id = :patientId AND tooth_number = :toothNumber LIMIT 1")
    fun getToothRecord(patientId: String, toothNumber: Int): Flow<OdontogramEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateTooth(record: OdontogramEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTeeth(records: List<OdontogramEntity>)

    @Query("DELETE FROM odontogram_state WHERE patient_id = :patientId AND tooth_number = :toothNumber")
    suspend fun resetTooth(patientId: String, toothNumber: Int)

    @Query("DELETE FROM odontogram_state WHERE patient_id = :patientId")
    suspend fun clearOdontogramForPatient(patientId: String)
}
