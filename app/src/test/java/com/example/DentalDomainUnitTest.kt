package com.example

import com.example.domain.model.ToothStatus
import com.example.domain.model.ToothSurface
import com.example.domain.model.FdiTeethHelper
import com.example.domain.model.Patient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DentalDomainUnitTest {

    @Test
    fun toothStatus_parsingWorksAccurately() {
        assertEquals(ToothStatus.SOUND, ToothStatus.fromString("SOUND"))
        assertEquals(ToothStatus.CARIES, ToothStatus.fromString("CARIES"))
        assertEquals(ToothStatus.RESTORATION, ToothStatus.fromString("restoration"))
        assertEquals(ToothStatus.ENDO, ToothStatus.fromString("ENDO"))
        assertEquals(ToothStatus.CROWN, ToothStatus.fromString("CROWN"))
        assertEquals(ToothStatus.MISSING, ToothStatus.fromString("MISSING"))
        assertEquals(ToothStatus.IMPLANT, ToothStatus.fromString("implant"))
        assertEquals(ToothStatus.SOUND, ToothStatus.fromString("UNKNOWN_VALUE"))
    }

    @Test
    fun toothSurface_fromCodeResolvesAllSurfaces() {
        assertEquals(ToothSurface.MESIAL, ToothSurface.fromCode("M"))
        assertEquals(ToothSurface.DISTAL, ToothSurface.fromCode("D"))
        assertEquals(ToothSurface.OCCLUSAL, ToothSurface.fromCode("O"))
        assertEquals(ToothSurface.BUCCAL, ToothSurface.fromCode("B"))
        assertEquals(ToothSurface.LINGUAL, ToothSurface.fromCode("L"))
    }

    @Test
    fun fdiTeethHelper_containsStandardPermanentAndPrimaryTeeth() {
        val permanentUpperRight = FdiTeethHelper.upperRightPermanent
        assertEquals(8, permanentUpperRight.size)
        assertTrue(permanentUpperRight.contains(11))
        assertTrue(permanentUpperRight.contains(18))

        val primaryUpperRight = FdiTeethHelper.upperRightPrimary
        assertEquals(5, primaryUpperRight.size)
        assertTrue(primaryUpperRight.contains(51))
        assertTrue(primaryUpperRight.contains(55))

        val tooth11Name = FdiTeethHelper.getToothName(11)
        assertTrue(tooth11Name.contains("Central Incisor"))
    }

    @Test
    fun patientModel_preservesClinicalData() {
        val patient = Patient(
            id = "patient_101",
            fullName = "Tariq Dental Case",
            phone = "+966500000000",
            age = 32,
            gender = "Male",
            medicalHistory = "Penicillin Allergy, Hypertensive",
            createdAt = System.currentTimeMillis()
        )
        assertNotNull(patient.id)
        assertEquals("Tariq Dental Case", patient.fullName)
        assertTrue(patient.medicalHistory.contains("Penicillin Allergy"))
    }
}
