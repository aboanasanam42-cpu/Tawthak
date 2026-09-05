package com.example.util

import android.content.Context
import android.graphics.*
import com.example.data.repository.DentalRepository
import com.example.domain.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object SampleDataGenerator {

    suspend fun seedInitialDataIfEmpty(context: Context, repository: DentalRepository) = withContext(Dispatchers.IO) {
        val existing = repository.getPatientByIdDirect("p1")
        if (existing != null) return@withContext

        // Ensure sample media files directory
        val mediaDir = File(context.filesDir, "clinical_media").apply { mkdirs() }

        val beforePhotoFile = File(mediaDir, "sample_before_smile.jpg")
        val afterPhotoFile = File(mediaDir, "sample_after_smile.jpg")
        val opgFile = File(mediaDir, "sample_panoramic_opg.jpg")
        val paFile = File(mediaDir, "sample_periapical_xray.jpg")

        if (!beforePhotoFile.exists()) {
            createClinicalToothBitmap(beforePhotoFile, isAfter = false)
        }
        if (!afterPhotoFile.exists()) {
            createClinicalToothBitmap(afterPhotoFile, isAfter = true)
        }
        if (!opgFile.exists()) {
            createOpgXrayBitmap(opgFile)
        }
        if (!paFile.exists()) {
            createPeriapicalXrayBitmap(paFile)
        }

        // Patient 1: Eleanor Vance (Comprehensive Restorative)
        val p1 = Patient(
            id = "p1",
            fullName = "Eleanor Vance",
            phone = "+1 (555) 234-8901",
            age = 34,
            gender = "Female",
            medicalHistory = "Penicillin allergy. Nocturnal bruxism. No chronic cardiac conditions.",
            createdAt = System.currentTimeMillis() - 14L * 86400000L
        )
        repository.insertPatient(p1)

        // Odontogram for P1
        repository.updateToothStatus("p1", 16, ToothStatus.CROWN, listOf("O", "B", "L"))
        repository.updateToothStatus("p1", 26, ToothStatus.ENDO, listOf("O"))
        repository.updateToothStatus("p1", 14, ToothStatus.CARIES, listOf("M", "O"))
        repository.updateToothStatus("p1", 24, ToothStatus.RESTORATION, listOf("D", "O"))
        repository.updateToothStatus("p1", 36, ToothStatus.IMPLANT, listOf("O"))
        repository.updateToothStatus("p1", 46, ToothStatus.MISSING, emptyList())
        repository.updateToothStatus("p1", 47, ToothStatus.CARIES, listOf("O"))

        // Visits for P1
        val v1 = Visit(
            id = "v1",
            patientId = "p1",
            visitDate = System.currentTimeMillis() - 14L * 86400000L,
            chiefComplaint = "Severe throbbing pain in upper left back tooth (#26), cold sensitivity.",
            diagnosis = "Irreversible pulpitis #26, occlusal secondary decay #14, missing #46.",
            treatmentPlan = "Emergency pulpectomy & root canal #26, porcelain fused to zirconia crown #16.",
            cost = 1450.0,
            paid = 1450.0
        )
        val v2 = Visit(
            id = "v2",
            patientId = "p1",
            visitDate = System.currentTimeMillis() - 4L * 86400000L,
            chiefComplaint = "Post-endo obturation checkup and crown preparation #16.",
            diagnosis = "Completed endodontic obturation #26. Good apical seal.",
            treatmentPlan = "Digital optical scan and CAD/CAM crown placement for #16. Deep cleaning.",
            cost = 980.0,
            paid = 500.0
        )
        repository.insertVisit(v1)
        repository.insertVisit(v2)

        // Clinical Media for P1
        repository.insertMedia(
            ClinicalMedia(
                id = "m1",
                patientId = "p1",
                visitId = "v1",
                mediaType = MediaType.INTRAORAL_BEFORE,
                localPath = beforePhotoFile.absolutePath,
                remoteUrl = null,
                annotationsJson = "[{\"x\":0.35,\"y\":0.45,\"label\":\"Caries #14\"},{\"x\":0.65,\"y\":0.48,\"label\":\"Periapical tenderness #26\"}]",
                createdAt = System.currentTimeMillis() - 14L * 86400000L
            )
        )
        repository.insertMedia(
            ClinicalMedia(
                id = "m2",
                patientId = "p1",
                visitId = "v2",
                mediaType = MediaType.INTRAORAL_AFTER,
                localPath = afterPhotoFile.absolutePath,
                remoteUrl = null,
                annotationsJson = "[{\"x\":0.35,\"y\":0.45,\"label\":\"Restored Composite\"},{\"x\":0.65,\"y\":0.48,\"label\":\"Completed Crown\"}]",
                createdAt = System.currentTimeMillis() - 4L * 86400000L
            )
        )
        repository.insertMedia(
            ClinicalMedia(
                id = "m3",
                patientId = "p1",
                visitId = "v1",
                mediaType = MediaType.OPG,
                localPath = opgFile.absolutePath,
                remoteUrl = null,
                annotationsJson = "[{\"x\":0.72,\"y\":0.55,\"label\":\"Bone crest level #36 implant\"}]",
                createdAt = System.currentTimeMillis() - 14L * 86400000L
            )
        )
        repository.insertMedia(
            ClinicalMedia(
                id = "m4",
                patientId = "p1",
                visitId = "v2",
                mediaType = MediaType.PERIAPICAL,
                localPath = paFile.absolutePath,
                remoteUrl = null,
                annotationsJson = "[{\"x\":0.5,\"y\":0.6,\"label\":\"Apical seal intact\"}]",
                createdAt = System.currentTimeMillis() - 4L * 86400000L
            )
        )

        // Patient 2: Marcus Reed (Anterior Aesthetics)
        val p2 = Patient(
            id = "p2",
            fullName = "Marcus Aurelius Reed",
            phone = "+1 (555) 871-3320",
            age = 42,
            gender = "Male",
            medicalHistory = "Controlled hypertension (Lisinopril 10mg). Non-smoker.",
            createdAt = System.currentTimeMillis() - 7L * 86400000L
        )
        repository.insertPatient(p2)
        repository.updateToothStatus("p2", 11, ToothStatus.RESTORATION, listOf("M", "D"))
        repository.updateToothStatus("p2", 21, ToothStatus.RESTORATION, listOf("M"))
        repository.updateToothStatus("p2", 46, ToothStatus.ENDO, listOf("O"))
        repository.updateToothStatus("p2", 47, ToothStatus.CROWN, listOf("O", "B"))
        repository.updateToothStatus("p2", 38, ToothStatus.MISSING, emptyList())

        val v3 = Visit(
            id = "v3",
            patientId = "p2",
            visitDate = System.currentTimeMillis() - 7L * 86400000L,
            chiefComplaint = "Cosmetic midline gap and fractured distal incisal corner of #11.",
            diagnosis = "Diastema closure & Class IV composite restoration #11, #21.",
            treatmentPlan = "Direct composite stratification with nanofill resin, shade A2/B1.",
            cost = 850.0,
            paid = 850.0
        )
        repository.insertVisit(v3)

        // Patient 3: Liam Chen (Pediatric)
        val p3 = Patient(
            id = "p3",
            fullName = "Liam Chen",
            phone = "+1 (555) 492-1088",
            age = 7,
            gender = "Male",
            medicalHistory = "Asthma (Albuterol PRN). No known drug allergies.",
            createdAt = System.currentTimeMillis() - 2L * 86400000L
        )
        repository.insertPatient(p3)
        repository.updateToothStatus("p3", 54, ToothStatus.CARIES, listOf("O"))
        repository.updateToothStatus("p3", 64, ToothStatus.RESTORATION, listOf("M", "O"))
        repository.updateToothStatus("p3", 85, ToothStatus.CARIES, listOf("D"))

        val v4 = Visit(
            id = "v4",
            patientId = "p3",
            visitDate = System.currentTimeMillis() - 2L * 86400000L,
            chiefComplaint = "Food catching in upper right molar during snacks.",
            diagnosis = "Class I occlusal dentinal caries primary molar #54.",
            treatmentPlan = "Fluoride varnish application and glass ionomer cement restoration.",
            cost = 220.0,
            paid = 220.0
        )
        repository.insertVisit(v4)
    }

    private fun createClinicalToothBitmap(file: File, isAfter: Boolean) {
        val width = 800
        val height = 600
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Soft clinical surgical drape / dental mouth background
        val bgPaint = Paint().apply {
            color = if (isAfter) Color.rgb(240, 249, 255) else Color.rgb(250, 245, 240)
            isAntiAlias = true
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Gingival tissue (Gums)
        val gumPaint = Paint().apply {
            color = if (isAfter) Color.rgb(244, 114, 182) else Color.rgb(225, 95, 140)
            isAntiAlias = true
            style = Paint.Style.FILL
        }
        val gumPath = Path().apply {
            moveTo(0f, 220f)
            cubicTo(200f, 170f, 280f, 240f, 400f, 180f)
            cubicTo(520f, 240f, 600f, 170f, 800f, 220f)
            lineTo(800f, 0f)
            lineTo(0f, 0f)
            close()
        }
        canvas.drawPath(gumPath, gumPaint)

        // Teeth Central Incisors #11 and #21
        val toothPaint = Paint().apply {
            color = if (isAfter) Color.rgb(255, 255, 255) else Color.rgb(245, 235, 210)
            isAntiAlias = true
            style = Paint.Style.FILL
        }
        val toothBorder = Paint().apply {
            color = Color.rgb(200, 210, 220)
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }

        // Left central tooth #11
        val tooth11 = RectF(220f, 190f, 390f, 480f)
        canvas.drawRoundRect(tooth11, 40f, 40f, toothPaint)
        canvas.drawRoundRect(tooth11, 40f, 40f, toothBorder)

        // Right central tooth #21
        val tooth21 = RectF(410f, 190f, 580f, 480f)
        canvas.drawRoundRect(tooth21, 40f, 40f, toothPaint)
        canvas.drawRoundRect(tooth21, 40f, 40f, toothBorder)

        // If Before: Draw dark fracture / cavity spot and stains
        if (!isAfter) {
            val cavityPaint = Paint().apply {
                color = Color.rgb(120, 53, 15)
                isAntiAlias = true
            }
            canvas.drawOval(RectF(340f, 360f, 395f, 430f), cavityPaint)

            val crackPaint = Paint().apply {
                color = Color.rgb(90, 40, 10)
                strokeWidth = 4f
                style = Paint.Style.STROKE
            }
            canvas.drawLine(350f, 320f, 385f, 440f, crackPaint)

            // Staining banner
            drawLabelBadge(canvas, "BEFORE: Class IV Fracture & Decay #11", 40f, 540f, Color.rgb(185, 28, 28))
        } else {
            // If After: Brilliant lustre highlight & polished enamel
            val highlightPaint = Paint().apply {
                color = Color.argb(140, 255, 255, 255)
                isAntiAlias = true
            }
            canvas.drawRoundRect(RectF(250f, 220f, 275f, 440f), 15f, 15f, highlightPaint)
            canvas.drawRoundRect(RectF(440f, 220f, 465f, 440f), 15f, 15f, highlightPaint)

            // Sparkle
            val sparklePaint = Paint().apply {
                color = Color.rgb(56, 189, 248)
                isAntiAlias = true
            }
            canvas.drawCircle(370f, 250f, 8f, sparklePaint)
            canvas.drawCircle(430f, 250f, 8f, sparklePaint)

            drawLabelBadge(canvas, "AFTER: Nanofill Composite Stratification", 40f, 540f, Color.rgb(13, 148, 136))
        }

        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
        }
    }

    private fun createOpgXrayBitmap(file: File) {
        val width = 900
        val height = 500
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Dark medical radiograph background
        canvas.drawColor(Color.rgb(10, 15, 25))

        val bonePaint = Paint().apply {
            color = Color.rgb(160, 175, 195)
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = 14f
        }

        // Mandibular & Maxillary arch curvature lines
        val archPath = Path().apply {
            moveTo(60f, 340f)
            cubicTo(250f, 420f, 650f, 420f, 840f, 340f)
        }
        canvas.drawPath(archPath, bonePaint)

        val upperArchPath = Path().apply {
            moveTo(80f, 190f)
            cubicTo(260f, 130f, 640f, 130f, 820f, 190f)
        }
        canvas.drawPath(upperArchPath, bonePaint)

        // Radiopaque tooth silhouettes
        val toothXrayPaint = Paint().apply {
            color = Color.rgb(215, 230, 245)
            isAntiAlias = true
            style = Paint.Style.FILL
        }

        for (i in 0..15) {
            val x = 120f + i * 42f
            val yUpper = 195f + kotlin.math.sin(i * 0.2).toFloat() * 18f
            canvas.drawRoundRect(RectF(x - 14f, yUpper - 40f, x + 14f, yUpper + 30f), 8f, 8f, toothXrayPaint)

            val yLower = 310f - kotlin.math.sin(i * 0.2).toFloat() * 18f
            canvas.drawRoundRect(RectF(x - 14f, yLower - 30f, x + 14f, yLower + 40f), 8f, 8f, toothXrayPaint)
        }

        drawLabelBadge(canvas, "PANORAMIC RADIOGRAPH (OPG) - 1:1 Diagnostic Scale", 40f, 450f, Color.rgb(2, 132, 199))

        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
    }

    private fun createPeriapicalXrayBitmap(file: File) {
        val width = 600
        val height = 700
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Radiograph dark film base
        canvas.drawColor(Color.rgb(18, 22, 30))

        // Alveolar bone trabecular shading
        val boneShader = Paint().apply {
            color = Color.rgb(75, 85, 100)
            isAntiAlias = true
        }
        canvas.drawRect(0f, 250f, width.toFloat(), height.toFloat(), boneShader)

        // Tooth crown and root canal
        val toothEnamel = Paint().apply {
            color = Color.rgb(230, 235, 245)
            isAntiAlias = true
        }
        // Crown
        canvas.drawRoundRect(RectF(180f, 100f, 420f, 320f), 35f, 35f, toothEnamel)

        // Dual roots
        val rootPath = Path().apply {
            moveTo(200f, 300f)
            quadTo(180f, 500f, 220f, 620f)
            quadTo(250f, 500f, 280f, 320f)
            close()
        }
        val rootPath2 = Path().apply {
            moveTo(320f, 320f)
            quadTo(350f, 500f, 380f, 620f)
            quadTo(420f, 500f, 400f, 300f)
            close()
        }
        canvas.drawPath(rootPath, toothEnamel)
        canvas.drawPath(rootPath2, toothEnamel)

        // Radiopaque root canal filling (Gutta-percha)
        val endoSeal = Paint().apply {
            color = Color.WHITE
            strokeWidth = 10f
            isAntiAlias = true
            style = Paint.Style.STROKE
        }
        canvas.drawLine(210f, 330f, 220f, 600f, endoSeal)
        canvas.drawLine(390f, 330f, 380f, 600f, endoSeal)

        drawLabelBadge(canvas, "PERIAPICAL X-RAY: Obturation Quality #26", 30f, 650f, Color.rgb(147, 51, 234))

        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
        }
    }

    private fun drawLabelBadge(canvas: Canvas, text: String, x: Float, y: Float, bgColor: Int) {
        val paintBg = Paint().apply {
            color = bgColor
            isAntiAlias = true
        }
        val paintText = Paint().apply {
            color = Color.WHITE
            textSize = 24f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }
        val textWidth = paintText.measureText(text)
        val rect = RectF(x, y - 30f, x + textWidth + 30f, y + 14f)
        canvas.drawRoundRect(rect, 10f, 10f, paintBg)
        canvas.drawText(text, x + 15f, y, paintText)
    }
}
