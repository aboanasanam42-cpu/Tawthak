package com.example.domain.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*

enum class ToothStatus(val label: String, val description: String, val color: Color) {
    SOUND("Sound", "Healthy tooth structure", ToothSoundColor),
    CARIES("Caries", "Active decay / Cavity", ToothCariesColor),
    RESTORATION("Restoration", "Filling / Composite / Amalgam", ToothRestorationColor),
    ENDO("Endodontic", "Root canal treated / Pulpectomy", ToothEndoColor),
    CROWN("Crown", "Full crown / Onlay prosthesis", ToothCrownColor),
    MISSING("Missing", "Extracted / Missing tooth", ToothMissingColor),
    IMPLANT("Implant", "Implant fixture + Abutment", ToothImplantColor);

    companion object {
        fun fromString(value: String): ToothStatus {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: SOUND
        }
    }
}

enum class ToothSurface(val code: String, val label: String) {
    MESIAL("M", "Mesial (Midline)"),
    DISTAL("D", "Distal"),
    OCCLUSAL("O", "Occlusal / Incisal"),
    BUCCAL("B", "Buccal / Facial"),
    LINGUAL("L", "Lingual / Palatal");

    companion object {
        fun fromCode(code: String): ToothSurface? =
            entries.firstOrNull { it.code.equals(code, ignoreCase = true) || it.name.equals(code, ignoreCase = true) }
    }
}

enum class MediaType(val label: String, val badge: String) {
    INTRAORAL_BEFORE("Intraoral (Before)", "BEFORE"),
    INTRAORAL_AFTER("Intraoral (After)", "AFTER"),
    OPG("Panoramic X-Ray (OPG)", "OPG"),
    PERIAPICAL("Periapical X-Ray", "PA"),
    CEPHALOMETRIC("Cephalometric X-Ray", "CEPH"),
    REPORT_PDF("Clinical PDF Report", "PDF");

    companion object {
        fun fromString(value: String): MediaType {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: INTRAORAL_BEFORE
        }
    }
}

data class Patient(
    val id: String,
    val fullName: String,
    val phone: String,
    val age: Int,
    val gender: String,
    val medicalHistory: String,
    val createdAt: Long
)

data class Visit(
    val id: String,
    val patientId: String,
    val visitDate: Long,
    val chiefComplaint: String,
    val diagnosis: String,
    val treatmentPlan: String,
    val cost: Double,
    val paid: Double
) {
    val balance: Double get() = (cost - paid).coerceAtLeast(0.0)
}

data class OdontogramRecord(
    val id: String,
    val patientId: String,
    val toothNumber: Int,
    val status: ToothStatus,
    val surfaces: List<String>,
    val updatedAt: Long
)

data class ClinicalMedia(
    val id: String,
    val patientId: String,
    val visitId: String?,
    val mediaType: MediaType,
    val localPath: String,
    val remoteUrl: String?,
    val annotationsJson: String,
    val createdAt: Long
)

object FdiTeethHelper {
    // FDI Permanent Quadrants:
    // Q1 Upper Right: 18..11
    // Q2 Upper Left: 21..28
    // Q3 Lower Left: 31..38
    // Q4 Lower Right: 41..48
    val upperRightPermanent = listOf(18, 17, 16, 15, 14, 13, 12, 11)
    val upperLeftPermanent = listOf(21, 22, 23, 24, 25, 26, 27, 28)
    val lowerRightPermanent = listOf(48, 47, 46, 45, 44, 43, 42, 41)
    val lowerLeftPermanent = listOf(31, 32, 33, 34, 35, 36, 37, 38)

    // Primary Quadrants:
    val upperRightPrimary = listOf(55, 54, 53, 52, 51)
    val upperLeftPrimary = listOf(61, 62, 63, 64, 65)
    val lowerRightPrimary = listOf(85, 84, 83, 82, 81)
    val lowerLeftPrimary = listOf(71, 72, 73, 74, 75)

    fun getToothName(toothNumber: Int): String {
        val q = toothNumber / 10
        val pos = toothNumber % 10

        val arch = when (q) {
            1 -> "Upper Right Permanent"
            2 -> "Upper Left Permanent"
            3 -> "Lower Left Permanent"
            4 -> "Lower Right Permanent"
            5 -> "Upper Right Primary"
            6 -> "Upper Left Primary"
            7 -> "Lower Left Primary"
            8 -> "Lower Right Primary"
            else -> "Tooth"
        }

        val type = if (q in 1..4) {
            when (pos) {
                1 -> "Central Incisor"
                2 -> "Lateral Incisor"
                3 -> "Canine (Cuspid)"
                4 -> "First Premolar (Bicuspid)"
                5 -> "Second Premolar"
                6 -> "First Molar (6-yr)"
                7 -> "Second Molar (12-yr)"
                8 -> "Third Molar (Wisdom)"
                else -> "Tooth"
            }
        } else {
            when (pos) {
                1 -> "Primary Central Incisor"
                2 -> "Primary Lateral Incisor"
                3 -> "Primary Canine"
                4 -> "Primary First Molar"
                5 -> "Primary Second Molar"
                else -> "Primary Tooth"
            }
        }
        return "#$toothNumber: $arch $type"
    }

    fun isAnterior(toothNumber: Int): Boolean {
        val pos = toothNumber % 10
        return pos in 1..3
    }
}
