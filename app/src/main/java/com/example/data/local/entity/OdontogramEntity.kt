package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.domain.model.OdontogramRecord
import com.example.domain.model.ToothStatus

@Entity(
    tableName = "odontogram_state",
    foreignKeys = [
        ForeignKey(
            entity = PatientEntity::class,
            parentColumns = ["id"],
            childColumns = ["patient_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["patient_id"]),
        Index(value = ["patient_id", "tooth_number"], unique = true)
    ]
)
data class OdontogramEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "patient_id")
    val patientId: String,

    @ColumnInfo(name = "tooth_number")
    val toothNumber: Int,

    @ColumnInfo(name = "status")
    val status: String,

    @ColumnInfo(name = "surfaces")
    val surfaces: String, // Comma separated or JSON e.g. "M,O,D"

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long
) {
    fun toDomain(): OdontogramRecord = OdontogramRecord(
        id = id,
        patientId = patientId,
        toothNumber = toothNumber,
        status = ToothStatus.fromString(status),
        surfaces = if (surfaces.isBlank()) emptyList() else surfaces.split(",").map { it.trim() },
        updatedAt = updatedAt
    )

    companion object {
        fun fromDomain(record: OdontogramRecord): OdontogramEntity = OdontogramEntity(
            id = record.id,
            patientId = record.patientId,
            toothNumber = record.toothNumber,
            status = record.status.name,
            surfaces = record.surfaces.joinToString(","),
            updatedAt = record.updatedAt
        )
    }
}
