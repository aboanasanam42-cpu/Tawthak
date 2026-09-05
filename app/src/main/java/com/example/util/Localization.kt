package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.ToothStatus

enum class AppLanguage(val code: String, val title: String, val shortTitle: String, val flag: String) {
    ARABIC("ar", "العربية", "عربي", "🇾🇪"),
    ENGLISH("en", "English", "EN", "🇺🇸");

    fun other(): AppLanguage = if (this == ARABIC) ENGLISH else ARABIC
}

val LocalAppLanguage = compositionLocalOf { AppLanguage.ARABIC }
val LocalDentalStrings = compositionLocalOf { DentalStrings(AppLanguage.ARABIC) }

class DentalStrings(val lang: AppLanguage) {
    val isAr: Boolean = lang == AppLanguage.ARABIC

    // Developer Attribution
    val developerCredit: String = if (isAr) {
        "تصميم وبرمجة الدكتور مالك الرميمة، رقم الهاتف 771134103"
    } else {
        "Designed & Programmed by Dr. Malek Al-Rumaimah, Tel: 771134103"
    }
    val developerPhone: String = "771134103"

    // App & Top Bar
    val appName: String = if (isAr) "توثيق - ملفات الأسنان" else "Tawthak - Dental Vault"
    val activePatientCase: String = if (isAr) "ملف المريض النشط" else "Active Patient Case"
    val casePrefix: String = if (isAr) "الحالة: " else "Case: "
    val quickPdfAction: String = if (isAr) "تقرير PDF" else "PDF Report"

    // Bottom Navigation
    val navOdontogram: String = if (isAr) "مخطط الأسنان" else "Odontogram"
    val navMedia: String = if (isAr) "مقارنة قبل/بعد" else "Before/After"
    val navPatients: String = if (isAr) "المرضى" else "Patients"
    val navReport: String = if (isAr) "تقرير PDF" else "PDF Report"

    // Dashboard Screen
    val switchPatient: String = if (isAr) "تغيير المريض" else "Switch"
    val noPatientSelected: String = if (isAr) "لم يتم تحديد مريض" else "No Patient Selected"
    val selectPatientPrompt: String = if (isAr) {
        "اختر مريضاً من السجل أو أضف مريضاً جديداً لبدء فحص وتوثيق الأسنان."
    } else {
        "Select a patient from records or create a new case to start examination."
    }
    val selectPatientBtn: String = if (isAr) "اختيار مريض" else "Select Patient"
    val newPatientBtn: String = if (isAr) "مريض جديد" else "New Patient"
    val phoneLabel: String = if (isAr) "الهاتف:" else "Phone:"
    val ageLabel: String = if (isAr) "العمر:" else "Age:"
    val yearsSuffix: String = if (isAr) "سنة" else "yrs"
    val medicalAlertLabel: String = if (isAr) "تنبيه طبي:" else "Medical Alert:"

    // Clinical Stats Overview
    val clinicalOverview: String = if (isAr) "ملخص الحالة السريرية" else "Clinical Overview"
    val documentedTeeth: String = if (isAr) "الأسنان المسجلة" else "Documented"
    val caries: String = if (isAr) "تسوس نشط" else "Caries"
    val restorations: String = if (isAr) "حشوة / عصب" else "Restorations"
    val missing: String = if (isAr) "مفقود / زراعة" else "Missing / Implant"
    val balanceDue: String = if (isAr) "المتبقي المالي" else "Balance Due"

    // FDI Odontogram
    val odontogramTitle: String = if (isAr) "مخطط الأسنان التفاعلي (FDI)" else "Interactive FDI Odontogram"
    val odontogramSubtitle: String = if (isAr) {
        "انقر على أي سن لتعديل التشخيص والأسطح المعالجة"
    } else {
        "Tap any tooth to update diagnosis & surfaces"
    }
    val permanentDentition: String = if (isAr) "الأسنان الدائمة (32)" else "Permanent (32)"
    val primaryDentition: String = if (isAr) "الأسنان اللبنية (20)" else "Primary (20)"
    val upperArch: String = if (isAr) "الفك العلوي (Maxillary)" else "Upper Arch (Maxillary)"
    val lowerArch: String = if (isAr) "الفك السفلي (Mandibular)" else "Lower Arch (Mandibular)"
    val rightSide: String = if (isAr) "اليمين (R)" else "Right (R)"
    val leftSide: String = if (isAr) "اليسار (L)" else "Left (L)"

    // AI Clinical Consult
    val aiConsultTitle: String = if (isAr) "الاستشارة الذكية والتدقيق الطبي (Gemini)" else "AI Clinical Consult & Safety Audit"
    val aiConsultSubtitle: String = if (isAr) {
        "تحليل متقدم للأسنان المسجلة ومراجعة موانع الاستخدام واقتراح خطة علاجية"
    } else {
        "Clinical analysis, contraindication checks, and treatment plans"
    }
    val runAiAnalysis: String = if (isAr) "بدء الفحص والتحليل الذكي" else "Run AI Clinical Analysis"

    // Visits Section
    val clinicalVisits: String = if (isAr) "سجل الزيارات والإجراءات" else "Clinical Visits & Treatments"
    val addVisit: String = if (isAr) "تسجيل زيارة" else "Add Visit"
    val noVisits: String = if (isAr) "لا توجد زيارات مسجلة لهذا المريض بعد." else "No visits documented yet."
    val complaint: String = if (isAr) "الشكوى:" else "Complaint:"
    val diagnosis: String = if (isAr) "التشخيص:" else "Diagnosis:"
    val treatment: String = if (isAr) "العلاج المنفذ:" else "Treatment:"
    val cost: String = if (isAr) "التكلفة:" else "Cost:"
    val paid: String = if (isAr) "المدفوع:" else "Paid:"
    val balance: String = if (isAr) "المتبقي:" else "Balance:"

    // Tooth Status Labels
    fun getToothStatusLabel(status: ToothStatus): String = when (status) {
        ToothStatus.SOUND -> if (isAr) "سليم" else "Sound"
        ToothStatus.CARIES -> if (isAr) "تسوس نشط" else "Caries"
        ToothStatus.RESTORATION -> if (isAr) "حشوة ترميمية" else "Restoration"
        ToothStatus.ENDO -> if (isAr) "علاج عصب" else "Endodontic"
        ToothStatus.CROWN -> if (isAr) "تاج / جسر" else "Crown"
        ToothStatus.MISSING -> if (isAr) "مفقود" else "Missing"
        ToothStatus.IMPLANT -> if (isAr) "زراعة سنية" else "Implant"
    }

    // Tooth Surfaces
    val surfacesTitle: String = if (isAr) "الأسطح المتأثرة (Surfaces):" else "Affected Surfaces:"
    val surfaceMesial: String = if (isAr) "M - إنسي" else "M - Mesial"
    val surfaceDistal: String = if (isAr) "D - وحشي" else "D - Distal"
    val surfaceOcclusal: String = if (isAr) "O - إطباقي" else "O - Occlusal"
    val surfaceBuccal: String = if (isAr) "B - دهليزي" else "B - Buccal"
    val surfaceLingual: String = if (isAr) "L - لساني" else "L - Lingual"

    // Common Buttons
    val save: String = if (isAr) "حفظ" else "Save"
    val cancel: String = if (isAr) "إلغاء" else "Cancel"
    val delete: String = if (isAr) "حذف" else "Delete"
    val close: String = if (isAr) "إغلاق" else "Close"
    val resetToSound: String = if (isAr) "إعادة تعيين (سليم)" else "Reset to Sound"
    val callAction: String = if (isAr) "اتصال" else "Call"
    val langBoxToggle: String = if (isAr) "EN" else "عربي"
}

/**
 * A sleek, compact small box (خيار مربع صغير) for toggling between Arabic and English.
 */
@Composable
fun LanguageSelectorBox(
    currentLanguage: AppLanguage,
    onLanguageChanged: (AppLanguage) -> Unit,
    modifier: Modifier = Modifier
) {
    val nextLanguage = currentLanguage.other()

    Surface(
        modifier = modifier
            .testTag("language_selector_box")
            .clip(RoundedCornerShape(8.dp))
            .clickable { onLanguageChanged(nextLanguage) }
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp)
            ),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Language,
                contentDescription = "Change Language",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )

            // Current Language Flag + Label
            Text(
                text = "${currentLanguage.flag} ${currentLanguage.shortTitle}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontSize = 12.sp
            )

            Text(
                text = "⇄",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 11.sp
            )

            // Next Language to switch to
            Text(
                text = nextLanguage.shortTitle,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 11.sp
            )
        }
    }
}

/**
 * Prominent Developer attribution banner as explicitly requested by the user:
 * "تصميم وبرمجة الدكتور مالك الرميمة، رقم الهاتف 771134103"
 */
@Composable
fun DeveloperCreditBanner(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val strings = LocalDentalStrings.current

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("developer_credit_banner")
            .clickable {
                try {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${strings.developerPhone}"))
                    context.startActivity(intent)
                } catch (e: Exception) {
                    // Fallback
                }
            },
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
        shape = RoundedCornerShape(0.dp),
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = strings.developerCredit,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = Icons.Default.Call,
                contentDescription = "Call",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(13.dp)
            )
        }
    }
}
