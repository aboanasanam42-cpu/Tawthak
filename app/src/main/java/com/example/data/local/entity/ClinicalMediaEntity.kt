package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.domain.model.ClinicalMedia
import com.example.domain.model.MediaType

@Entity(
    tableName = "clinical_media",
    foreignKeys = [
        ForeignKey(
            entity = PatientEntity::class,
            parentColumns = ["id"],
            childColumns = ["patient_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = VisitEntity::class,
            parentColumns = ["id"],
            childColumns = ["visit_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["patient_id"]),
        Index(value = ["visit_id"])
    ]
)
data class ClinicalMediaEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "patient_id")
    val patientId: String,

    @ColumnInfo(name = "visit_id")
    val visitId: String?,

    @ColumnInfo(name = "media_type")
    val mediaType: String,

    @ColumnInfo(name = "local_path")
    val localPath: String,

    @ColumnInfo(name = "remote_url")
    val remoteUrl: String?,

    @ColumnInfo(name = "annotations_json")
    val annotationsJson: String,

    @ColumnInfo(name = "created_at")
    val createdAt: Long
) {
    fun toDomain(): ClinicalMedia = ClinicalMedia(
        id = id,
        patientId = patientId,
        visitId = visitId,
        mediaType = MediaType.fromString(mediaType),
        localPath = localPath,
        remoteUrl = remoteUrl,
        annotationsJson = annotationsJson,
        createdAt = createdAt
    )

    companion object {
        fun fromDomain(media: ClinicalMedia): ClinicalMediaEntity = ClinicalMediaEntity(
            id = media.id,
            patientId = media.patientId,
            visitId = media.visitId,
            mediaType = media.mediaType.name,
            localPath = media.localPath,
            remoteUrl = media.remoteUrl,
            annotationsJson = media.annotationsJson,
            createdAt = media.createdAt
        )
    }
}
