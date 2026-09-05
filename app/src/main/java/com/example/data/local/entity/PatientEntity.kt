package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.Patient

@Entity(tableName = "patients")
data class PatientEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "full_name")
    val fullName: String,

    @ColumnInfo(name = "phone")
    val phone: String,

    @ColumnInfo(name = "age")
    val age: Int,

    @ColumnInfo(name = "gender")
    val gender: String,

    @ColumnInfo(name = "medical_history")
    val medicalHistory: String,

    @ColumnInfo(name = "created_at")
    val createdAt: Long
) {
    fun toDomain(): Patient = Patient(
        id = id,
        fullName = fullName,
        phone = phone,
        age = age,
        gender = gender,
        medicalHistory = medicalHistory,
        createdAt = createdAt
    )

    companion object {
        fun fromDomain(patient: Patient): PatientEntity = PatientEntity(
            id = patient.id,
            fullName = patient.fullName,
            phone = patient.phone,
            age = patient.age,
            gender = patient.gender,
            medicalHistory = patient.medicalHistory,
            createdAt = patient.createdAt
        )
    }
}
