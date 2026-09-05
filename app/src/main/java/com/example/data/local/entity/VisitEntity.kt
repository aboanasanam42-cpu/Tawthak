package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.domain.model.Visit

@Entity(
    tableName = "visits",
    foreignKeys = [
        ForeignKey(
            entity = PatientEntity::class,
            parentColumns = ["id"],
            childColumns = ["patient_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["patient_id"])]
)
data class VisitEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "patient_id")
    val patientId: String,

    @ColumnInfo(name = "visit_date")
    val visitDate: Long,

    @ColumnInfo(name = "chief_complaint")
    val chiefComplaint: String,

    @ColumnInfo(name = "diagnosis")
    val diagnosis: String,

    @ColumnInfo(name = "treatment_plan")
    val treatmentPlan: String,

    @ColumnInfo(name = "cost")
    val cost: Double,

    @ColumnInfo(name = "paid")
    val paid: Double
) {
    fun toDomain(): Visit = Visit(
        id = id,
        patientId = patientId,
        visitDate = visitDate,
        chiefComplaint = chiefComplaint,
        diagnosis = diagnosis,
        treatmentPlan = treatmentPlan,
        cost = cost,
        paid = paid
    )

    companion object {
        fun fromDomain(visit: Visit): VisitEntity = VisitEntity(
            id = visit.id,
            patientId = visit.patientId,
            visitDate = visit.visitDate,
            chiefComplaint = visit.chiefComplaint,
            diagnosis = visit.diagnosis,
            treatmentPlan = visit.treatmentPlan,
            cost = visit.cost,
            paid = visit.paid
        )
    }
}
