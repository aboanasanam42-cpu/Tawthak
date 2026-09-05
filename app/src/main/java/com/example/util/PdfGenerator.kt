package com.example.util

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import com.example.domain.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfGenerator {

    suspend fun generateClinicalReport(
        context: Context,
        patient: Patient,
        visits: List<Visit>,
        odontogram: List<OdontogramRecord>,
        mediaList: List<ClinicalMedia>
    ): File = withContext(Dispatchers.IO) {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 at 72dpi
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        val dateShort = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

        var currentY = 40f
        val leftMargin = 36f
        val rightMargin = 559f
        val contentWidth = rightMargin - leftMargin

        // Header Background Banner
        val headerPaint = Paint().apply {
            color = Color.rgb(13, 148, 136) // Dental Teal
            isAntiAlias = true
        }
        canvas.drawRoundRect(RectF(leftMargin, currentY, rightMargin, currentY + 60f), 8f, 8f, headerPaint)

        // Header Title
        val titlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("DENTAL CASE VAULT - CLINICAL SUMMARY", leftMargin + 16f, currentY + 34f, titlePaint)

        val subtitlePaint = Paint().apply {
            color = Color.rgb(204, 251, 241)
            textSize = 10f
            isAntiAlias = true
        }
        canvas.drawText("Generated: ${dateFormat.format(Date())} | Ref ID: ${patient.id.take(8).uppercase()}", leftMargin + 16f, currentY + 48f, subtitlePaint)

        currentY += 75f

        // Patient Demographics Section
        val sectionTitlePaint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("1. PATIENT DEMOGRAPHICS", leftMargin, currentY, sectionTitlePaint)
        currentY += 12f

        val boxPaint = Paint().apply {
            color = Color.rgb(248, 250, 252)
            isAntiAlias = true
        }
        val borderPaint = Paint().apply {
            color = Color.rgb(226, 232, 240)
            style = Paint.Style.STROKE
            strokeWidth = 1f
            isAntiAlias = true
        }
        val patientBoxRect = RectF(leftMargin, currentY, rightMargin, currentY + 70f)
        canvas.drawRoundRect(patientBoxRect, 6f, 6f, boxPaint)
        canvas.drawRoundRect(patientBoxRect, 6f, 6f, borderPaint)

        val textPaint = Paint().apply {
            color = Color.rgb(51, 65, 85)
            textSize = 10f
            isAntiAlias = true
        }
        val boldTextPaint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        canvas.drawText("Full Name:", leftMargin + 14f, currentY + 22f, boldTextPaint)
        canvas.drawText(patient.fullName, leftMargin + 75f, currentY + 22f, textPaint)

        canvas.drawText("Age / Sex:", leftMargin + 280f, currentY + 22f, boldTextPaint)
        canvas.drawText("${patient.age} yrs / ${patient.gender}", leftMargin + 340f, currentY + 22f, textPaint)

        canvas.drawText("Phone:", leftMargin + 14f, currentY + 40f, boldTextPaint)
        canvas.drawText(patient.phone, leftMargin + 75f, currentY + 40f, textPaint)

        canvas.drawText("Med History:", leftMargin + 14f, currentY + 58f, boldTextPaint)
        val medHist = if (patient.medicalHistory.isBlank()) "None reported" else patient.medicalHistory
        canvas.drawText(medHist.take(65), leftMargin + 85f, currentY + 58f, textPaint)

        currentY += 85f

        // Odontogram Status Summary
        canvas.drawText("2. ODONTOGRAM STATUS SUMMARY (FDI SYSTEM)", leftMargin, currentY, sectionTitlePaint)
        currentY += 12f

        val odontoBoxRect = RectF(leftMargin, currentY, rightMargin, currentY + 80f)
        canvas.drawRoundRect(odontoBoxRect, 6f, 6f, boxPaint)
        canvas.drawRoundRect(odontoBoxRect, 6f, 6f, borderPaint)

        val cariesTeeth = odontogram.filter { it.status == ToothStatus.CARIES }
        val restorationTeeth = odontogram.filter { it.status == ToothStatus.RESTORATION }
        val endoTeeth = odontogram.filter { it.status == ToothStatus.ENDO }
        val crownTeeth = odontogram.filter { it.status == ToothStatus.CROWN }
        val missingTeeth = odontogram.filter { it.status == ToothStatus.MISSING }
        val implantTeeth = odontogram.filter { it.status == ToothStatus.IMPLANT }

        fun formatTeethList(list: List<OdontogramRecord>): String {
            if (list.isEmpty()) return "None"
            return list.joinToString(", ") {
                val surf = if (it.surfaces.isNotEmpty()) " (${it.surfaces.joinToString("")})" else ""
                "#${it.toothNumber}$surf"
            }
        }

        canvas.drawText("• Caries:", leftMargin + 14f, currentY + 20f, boldTextPaint)
        canvas.drawText(formatTeethList(cariesTeeth), leftMargin + 70f, currentY + 20f, textPaint)

        canvas.drawText("• Restorations:", leftMargin + 280f, currentY + 20f, boldTextPaint)
        canvas.drawText(formatTeethList(restorationTeeth), leftMargin + 360f, currentY + 20f, textPaint)

        canvas.drawText("• Endodontic:", leftMargin + 14f, currentY + 40f, boldTextPaint)
        canvas.drawText(formatTeethList(endoTeeth), leftMargin + 85f, currentY + 40f, textPaint)

        canvas.drawText("• Crowns / Onlays:", leftMargin + 280f, currentY + 40f, boldTextPaint)
        canvas.drawText(formatTeethList(crownTeeth), leftMargin + 380f, currentY + 40f, textPaint)

        canvas.drawText("• Implants:", leftMargin + 14f, currentY + 60f, boldTextPaint)
        canvas.drawText(formatTeethList(implantTeeth), leftMargin + 80f, currentY + 60f, textPaint)

        canvas.drawText("• Missing:", leftMargin + 280f, currentY + 60f, boldTextPaint)
        canvas.drawText(formatTeethList(missingTeeth), leftMargin + 340f, currentY + 60f, textPaint)

        currentY += 95f

        // Visits & Treatments History
        canvas.drawText("3. CLINICAL VISITS & PROCEDURES", leftMargin, currentY, sectionTitlePaint)
        currentY += 12f

        var totalCost = 0.0
        var totalPaid = 0.0

        if (visits.isEmpty()) {
            val emptyBox = RectF(leftMargin, currentY, rightMargin, currentY + 35f)
            canvas.drawRoundRect(emptyBox, 6f, 6f, boxPaint)
            canvas.drawRoundRect(emptyBox, 6f, 6f, borderPaint)
            canvas.drawText("No documented visits on record.", leftMargin + 14f, currentY + 22f, textPaint)
            currentY += 45f
        } else {
            visits.take(3).forEach { visit ->
                totalCost += visit.cost
                totalPaid += visit.paid

                val visitHeight = 62f
                val vRect = RectF(leftMargin, currentY, rightMargin, currentY + visitHeight)
                canvas.drawRoundRect(vRect, 6f, 6f, boxPaint)
                canvas.drawRoundRect(vRect, 6f, 6f, borderPaint)

                canvas.drawText("Date: ${dateShort.format(Date(visit.visitDate))}", leftMargin + 12f, currentY + 18f, boldTextPaint)
                canvas.drawText("Fee: $${"%.2f".format(visit.cost)} | Paid: $${"%.2f".format(visit.paid)} | Bal: $${"%.2f".format(visit.balance)}", leftMargin + 310f, currentY + 18f, boldTextPaint)

                canvas.drawText("Diagnosis: ${visit.diagnosis.take(75)}", leftMargin + 12f, currentY + 34f, textPaint)
                canvas.drawText("Treatment: ${visit.treatmentPlan.take(75)}", leftMargin + 12f, currentY + 50f, textPaint)

                currentY += visitHeight + 8f
            }
        }

        // Financial summary row
        val balance = (totalCost - totalPaid).coerceAtLeast(0.0)
        canvas.drawText("Total Billed: $${"%.2f".format(totalCost)}   |   Total Paid: $${"%.2f".format(totalPaid)}   |   Outstanding Balance: $${"%.2f".format(balance)}", leftMargin + 14f, currentY + 8f, boldTextPaint)
        currentY += 24f

        // Photographic Plates (Before / After Comparison)
        canvas.drawText("4. CLINICAL PHOTOGRAPHIC EVIDENCE", leftMargin, currentY, sectionTitlePaint)
        currentY += 12f

        val beforeMedia = mediaList.firstOrNull { it.mediaType == MediaType.INTRAORAL_BEFORE }
        val afterMedia = mediaList.firstOrNull { it.mediaType == MediaType.INTRAORAL_AFTER }

        val photoWidth = (contentWidth - 16f) / 2f
        val photoHeight = 130f

        // Draw Before Frame
        drawMediaFrame(canvas, beforeMedia, "PRE-OPERATIVE (BEFORE)", leftMargin, currentY, photoWidth, photoHeight)
        // Draw After Frame
        drawMediaFrame(canvas, afterMedia, "POST-OPERATIVE (AFTER)", leftMargin + photoWidth + 16f, currentY, photoWidth, photoHeight)

        currentY += photoHeight + 35f

        // Footer signature line
        val dividerPaint = Paint().apply {
            color = Color.rgb(203, 213, 225)
            strokeWidth = 1f
        }
        canvas.drawLine(leftMargin, 800f, rightMargin, 800f, dividerPaint)

        val footerPaint = Paint().apply {
            color = Color.rgb(100, 116, 139)
            textSize = 9f
            isAntiAlias = true
        }
        canvas.drawText("Clinician Signature: _______________________", leftMargin, 820f, footerPaint)
        canvas.drawText("Verified & Sealed by Dental Case Vault EMR", rightMargin - 200f, 820f, footerPaint)

        document.finishPage(page)

        val outputDir = File(context.cacheDir, "reports").apply { mkdirs() }
        val pdfFile = File(outputDir, "Clinical_Report_${patient.fullName.replace(" ", "_")}_${System.currentTimeMillis()}.pdf")

        FileOutputStream(pdfFile).use { out ->
            document.writeTo(out)
        }
        document.close()

        pdfFile
    }

    private fun drawMediaFrame(
        canvas: Canvas,
        media: ClinicalMedia?,
        label: String,
        x: Float,
        y: Float,
        width: Float,
        height: Float
    ) {
        val frameRect = RectF(x, y, x + width, y + height)
        val bgPaint = Paint().apply {
            color = Color.rgb(241, 245, 249)
            isAntiAlias = true
        }
        val borderPaint = Paint().apply {
            color = Color.rgb(203, 213, 225)
            style = Paint.Style.STROKE
            strokeWidth = 1f
            isAntiAlias = true
        }
        canvas.drawRoundRect(frameRect, 6f, 6f, bgPaint)

        if (media != null && File(media.localPath).exists()) {
            try {
                val bitmap = BitmapFactory.decodeFile(media.localPath)
                if (bitmap != null) {
                    val destRect = RectF(x + 2f, y + 2f, x + width - 2f, y + height - 20f)
                    canvas.drawBitmap(bitmap, null, destRect, null)
                    bitmap.recycle()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            val emptyPaint = Paint().apply {
                color = Color.rgb(148, 163, 184)
                textSize = 10f
                isAntiAlias = true
            }
            canvas.drawText("Photo Plate Not Available", x + width / 4f, y + height / 2f, emptyPaint)
        }

        canvas.drawRoundRect(frameRect, 6f, 6f, borderPaint)

        // Label footer
        val labelBg = Paint().apply {
            color = Color.rgb(15, 23, 42)
            isAntiAlias = true
        }
        val labelRect = RectF(x, y + height - 18f, x + width, y + height)
        canvas.drawRoundRect(labelRect, 4f, 4f, labelBg)

        val labelTextPaint = Paint().apply {
            color = Color.WHITE
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText(label, x + 8f, y + height - 5f, labelTextPaint)
    }
}
