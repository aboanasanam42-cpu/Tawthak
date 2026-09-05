package com.example.data.ai

import android.util.Log
import com.example.BuildConfig
import com.example.domain.model.OdontogramRecord
import com.example.domain.model.Patient
import com.example.domain.model.ToothStatus
import com.example.domain.model.Visit
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GeminiContent(
    @Json(name = "parts") val parts: List<GeminiPart>,
    @Json(name = "role") val role: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiPart(
    @Json(name = "text") val text: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiGenerationConfig(
    @Json(name = "temperature") val temperature: Float? = null,
    @Json(name = "topP") val topP: Float? = null,
    @Json(name = "topK") val topK: Int? = null
)

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    @Json(name = "contents") val contents: List<GeminiContent>,
    @Json(name = "systemInstruction") val systemInstruction: GeminiContent? = null,
    @Json(name = "generationConfig") val generationConfig: GeminiGenerationConfig? = null
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    @Json(name = "candidates") val candidates: List<GeminiCandidate>? = null,
    @Json(name = "error") val error: GeminiError? = null
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    @Json(name = "content") val content: GeminiContent? = null,
    @Json(name = "finishReason") val finishReason: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiError(
    @Json(name = "code") val code: Int? = null,
    @Json(name = "message") val message: String? = null
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

data class ClinicalAuditResult(
    val hasAlerts: Boolean,
    val alertMessages: List<String>,
    val safetyRecommendations: List<String>,
    val fullAuditText: String
)

data class ClinicalAnalysisResult(
    val diagnosisSummary: String,
    val treatmentPhases: List<String>,
    val patientBriefing: String,
    val isFromLiveAi: Boolean
)

object GeminiClientProvider {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        .build()

    val apiService: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }
}

class GeminiClinicalService(
    private val apiService: GeminiApiService = GeminiClientProvider.apiService
) {

    fun isApiKeyConfigured(): Boolean {
        val key = BuildConfig.GEMINI_API_KEY
        return !key.isNullOrBlank() && key != "MY_GEMINI_API_KEY" && key.length > 10
    }

    suspend fun analyzeCase(
        patient: Patient,
        odontogram: List<OdontogramRecord>,
        visits: List<Visit>,
        language: String = "ar"
    ): Result<ClinicalAnalysisResult> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY

        val teethSummary = if (odontogram.isEmpty()) {
            "No dental pathology recorded in odontogram (healthy dentition)."
        } else {
            odontogram.joinToString(separator = "\n") { rec ->
                "- Tooth #${rec.toothNumber}: Status=${rec.status.label}, Surfaces=[${rec.surfaces.joinToString()}]"
            }
        }

        val visitsSummary = if (visits.isEmpty()) {
            "No prior visits."
        } else {
            visits.take(3).joinToString(separator = "\n") { v ->
                "- Visit: Complaint='${v.chiefComplaint}', Diagnosis='${v.diagnosis}', TreatmentPlan='${v.treatmentPlan}'"
            }
        }

        val prompt = """
            Patient Profile:
            - Name: ${patient.fullName} (Age: ${patient.age}, Gender: ${patient.gender})
            - Medical Alerts / Allergies: ${if (patient.medicalHistory.isBlank()) "None reported" else patient.medicalHistory}
            
            Charted Odontogram Teeth:
            $teethSummary
            
            Recent Clinical Visits:
            $visitsSummary
            
            Clinical Request:
            Please provide a rigorous clinical dental case analysis and treatment roadmap formatted clearly:
            1. Clinical Differential Diagnosis & Etiology based on active odontogram pathologies and complaints.
            2. Phased Evidence-Based Treatment Plan (Phase 1: Urgent/Pain Relief, Phase 2: Restorative/Endodontic, Phase 3: Prosthetics/Rehabilitation, Phase 4: Maintenance).
            3. Patient Consultation Briefing (${if (language == "ar") "in Arabic for clear patient communication" else "in English"}).
            4. Clinical Safety & Medical Alert Precautions based on the patient's medical alerts.
        """.trimIndent()

        if (!isApiKeyConfigured()) {
            return@withContext Result.success(
                generateLocalFallbackAnalysis(patient, odontogram, visits, language)
            )
        }

        try {
            val request = GeminiRequest(
                contents = listOf(
                    GeminiContent(
                        parts = listOf(GeminiPart(text = prompt))
                    )
                ),
                systemInstruction = GeminiContent(
                    parts = listOf(
                        GeminiPart(
                            text = "You are an expert senior clinical dental specialist and patient documentation (Tawtheeq/توثيق) consultant. Provide structured, medically sound dental recommendations adhering to FDI international standards."
                        )
                    )
                ),
                generationConfig = GeminiGenerationConfig(
                    temperature = 0.3f
                )
            )

            val response = apiService.generateContent(apiKey, request)
            val generatedText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text

            if (!generatedText.isNullOrBlank()) {
                val parsed = parseClinicalResponse(generatedText, isFromLiveAi = true)
                Result.success(parsed)
            } else {
                val errorMsg = response.error?.message ?: "Empty response from Gemini AI"
                Log.w("GeminiClinicalService", "AI generation returned empty: $errorMsg")
                Result.success(generateLocalFallbackAnalysis(patient, odontogram, visits, language))
            }
        } catch (e: Exception) {
            Log.e("GeminiClinicalService", "Failed to call Gemini API", e)
            Result.success(generateLocalFallbackAnalysis(patient, odontogram, visits, language))
        }
    }

    suspend fun auditSafetyAndContraindications(
        patient: Patient,
        odontogram: List<OdontogramRecord>,
        plannedProcedure: String
    ): ClinicalAuditResult = withContext(Dispatchers.IO) {
        val alerts = mutableListOf<String>()
        val recommendations = mutableListOf<String>()

        val medAlertsLower = patient.medicalHistory.lowercase()

        // 1. Allergy Checks
        if (medAlertsLower.contains("penicillin") || medAlertsLower.contains("amoxicillin")) {
            alerts.add("Severe Allergy Alert: Penicillin / Beta-lactams reported.")
            recommendations.add("Avoid Amoxicillin/Augmentin. Use Clindamycin (300mg) or Azithromycin (500mg) if antibiotic therapy is indicated.")
        }
        if (medAlertsLower.contains("aspirin") || medAlertsLower.contains("nsaid")) {
            alerts.add("NSAID Sensitivity: Patient allergic to Aspirin or NSAIDs.")
            recommendations.add("Prescribe Acetaminophen (Paracetamol) for postoperative analgesia. Avoid Ibuprofen and Ketorolac.")
        }
        if (medAlertsLower.contains("latex")) {
            alerts.add("Latex Allergy Alert: Use non-latex gloves and rubber dams.")
            recommendations.add("Ensure operatory is equipped with nitrile gloves and silicone/nitrile dental dams.")
        }

        // 2. Systemic Condition Checks
        if (medAlertsLower.contains("hypertens") || medAlertsLower.contains("blood pressure") || medAlertsLower.contains("ضغط")) {
            alerts.add("Cardiovascular / Hypertension Alert.")
            recommendations.add("Monitor pre-operative BP. Limit Epinephrine (max 0.04mg = 2 carpules of 1:100,000 Lido) or use Mepivacaine 3% plain.")
        }
        if (medAlertsLower.contains("diabet") || medAlertsLower.contains("سكر")) {
            alerts.add("Diabetic Patient: Risk of delayed wound healing and periodontal vulnerability.")
            recommendations.add("Schedule morning appointments after breakfast and insulin. Verify recent HbA1c level.")
        }
        if (medAlertsLower.contains("warfarin") || medAlertsLower.contains("blood thinner") || medAlertsLower.contains("anticoagulant") || medAlertsLower.contains("سيولة")) {
            alerts.add("Anticoagulant Therapy Alert: Elevated bleeding risk.")
            recommendations.add("Check recent INR (ideal <= 2.5 for minor oral surgery). Use local hemostatic agents (Surgicel, suturing). Do not stop anticoagulant without physician consent.")
        }
        if (medAlertsLower.contains("heart") || medAlertsLower.contains("valve") || medAlertsLower.contains("endocarditis")) {
            alerts.add("Infective Endocarditis Risk: Artificial cardiac valve or prior endocarditis.")
            recommendations.add("Administer standard antibiotic prophylaxis 30-60 minutes prior to invasive dental manipulations.")
        }

        // 3. Odontogram Pathologies Check
        val cariesCount = odontogram.count { it.status == ToothStatus.CARIES }
        val endoCount = odontogram.count { it.status == ToothStatus.ENDO }
        val missingCount = odontogram.count { it.status == ToothStatus.MISSING }

        if (cariesCount >= 3) {
            alerts.add("High Caries Activity: $cariesCount teeth identified with active decay.")
            recommendations.add("Implement high-fluoride therapy (5000ppm toothpaste), dietary counseling, and complete caries control before final indirect restorations.")
        }
        if (endoCount > 0) {
            recommendations.add("Evaluate endodontically treated teeth for definitive cuspal coverage (onlay/crown) to prevent catastrophic root fractures.")
        }
        if (missingCount > 0) {
            recommendations.add("Discuss prosthetic space rehabilitation (implants or fixed bridges) to prevent opposing tooth overeruption and drifting.")
        }

        val hasAlerts = alerts.isNotEmpty()
        val auditSummary = buildString {
            appendLine("=== Clinical Safety & Medical Validation Audit ===")
            appendLine("Patient: ${patient.fullName} | Medical Alerts: ${if (patient.medicalHistory.isBlank()) "None reported" else patient.medicalHistory}")
            appendLine("Planned Procedure: $plannedProcedure")
            appendLine()
            if (alerts.isNotEmpty()) {
                appendLine("WARNING FLAGS:")
                alerts.forEach { appendLine("• $it") }
                appendLine()
            } else {
                appendLine("✓ No critical systemic contraindications detected for routine procedures.")
                appendLine()
            }
            appendLine("CLINICAL RECOMMENDATIONS:")
            recommendations.forEach { appendLine("→ $it") }
        }

        ClinicalAuditResult(
            hasAlerts = hasAlerts,
            alertMessages = alerts,
            safetyRecommendations = recommendations,
            fullAuditText = auditSummary
        )
    }

    private fun parseClinicalResponse(text: String, isFromLiveAi: Boolean): ClinicalAnalysisResult {
        val lines = text.lines()
        val phases = mutableListOf<String>()
        val diagnosisBuilder = StringBuilder()
        val briefingBuilder = StringBuilder()

        var currentSection = 0 // 1 = diag, 2 = plan, 3 = brief

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.contains("Diagnosis", ignoreCase = true) || trimmed.contains("تشخيص", ignoreCase = true)) {
                currentSection = 1
                continue
            } else if (trimmed.contains("Treatment Plan", ignoreCase = true) || trimmed.contains("خطة العلاج", ignoreCase = true)) {
                currentSection = 2
                continue
            } else if (trimmed.contains("Briefing", ignoreCase = true) || trimmed.contains("Patient", ignoreCase = true) && trimmed.contains("Consultation", ignoreCase = true) || trimmed.contains("المريض", ignoreCase = true)) {
                currentSection = 3
                continue
            }

            when (currentSection) {
                1 -> if (trimmed.isNotBlank()) diagnosisBuilder.appendLine(trimmed)
                2 -> if (trimmed.isNotBlank() && (trimmed.startsWith("-") || trimmed.startsWith("•") || trimmed.startsWith("Phase") || trimmed.startsWith("المرحلة") || trimmed.matches(Regex("^\\d+\\..*")))) {
                    phases.add(trimmed.trimStart('-', '•', ' '))
                }
                3 -> if (trimmed.isNotBlank()) briefingBuilder.appendLine(trimmed)
            }
        }

        val diagnosis = if (diagnosisBuilder.isNotBlank()) diagnosisBuilder.toString().trim() else text.take(400)
        val briefing = if (briefingBuilder.isNotBlank()) briefingBuilder.toString().trim() else "الرجاء مراجعة خطة العلاج المعتمدة من الطبيب المعالج والالتزام بمواعيد الجلسات ونظافة الفم اليومية."
        val finalPhases = if (phases.isNotEmpty()) phases else listOf(
            "Phase 1 (Urgent): Caries control and immediate pain relief.",
            "Phase 2 (Restorative): Composite resin restorations and endodontic treatments.",
            "Phase 3 (Prosthodontics): Crown placements and missing tooth replacements.",
            "Phase 4 (Maintenance): 6-month recall prophylaxis and oral hygiene instruction."
        )

        return ClinicalAnalysisResult(
            diagnosisSummary = diagnosis,
            treatmentPhases = finalPhases,
            patientBriefing = briefing,
            isFromLiveAi = isFromLiveAi
        )
    }

    private fun generateLocalFallbackAnalysis(
        patient: Patient,
        odontogram: List<OdontogramRecord>,
        visits: List<Visit>,
        language: String
    ): ClinicalAnalysisResult {
        val cariesTeeth = odontogram.filter { it.status == ToothStatus.CARIES }.map { it.toothNumber }
        val endoTeeth = odontogram.filter { it.status == ToothStatus.ENDO }.map { it.toothNumber }
        val missingTeeth = odontogram.filter { it.status == ToothStatus.MISSING }.map { it.toothNumber }
        val crownTeeth = odontogram.filter { it.status == ToothStatus.CROWN }.map { it.toothNumber }

        val isAr = language == "ar"

        val diag = if (isAr) {
            buildString {
                appendLine("التقييم الإكلينيكي لحالة: ${patient.fullName}")
                if (cariesTeeth.isNotEmpty()) appendLine("• تسوس نشط يتطلب تدخلاً ترميمياً في الأسنان رقم: ${cariesTeeth.joinToString()}")
                if (endoTeeth.isNotEmpty()) appendLine("• علاج عصب مكتمل/قيد المتابعة بحاجة إلى تغطية تاجية في الأسنان: ${endoTeeth.joinToString()}")
                if (missingTeeth.isNotEmpty()) appendLine("• فقدان أسناني بحاجة لتعويض تركيبي في الأسنان: ${missingTeeth.joinToString()}")
                if (cariesTeeth.isEmpty() && endoTeeth.isEmpty() && missingTeeth.isEmpty()) {
                    appendLine("• حالة صحية عامة مستقرة للأسنان مع حاجة لإجراء الفحص الدوري وإزالة الترسبات.")
                }
                if (patient.medicalHistory.isNotBlank()) {
                    appendLine("• تنبيه طبي: ${patient.medicalHistory}")
                }
            }
        } else {
            buildString {
                appendLine("Clinical Assessment for ${patient.fullName}:")
                if (cariesTeeth.isNotEmpty()) appendLine("• Active carious lesions in teeth #${cariesTeeth.joinToString()}")
                if (endoTeeth.isNotEmpty()) appendLine("• Endodontically treated teeth requiring cuspal protection in #${endoTeeth.joinToString()}")
                if (missingTeeth.isNotEmpty()) appendLine("• Edentulous space requiring prosthetic restoration in #${missingTeeth.joinToString()}")
                if (patient.medicalHistory.isNotBlank()) appendLine("• Medical Precautions: ${patient.medicalHistory}")
            }
        }

        val phases = if (isAr) {
            listOf(
                "المرحلة الأولى: إزالة التسوسات النشطة وتسكين الألم ومعالجة الحالات الطارئة.",
                "المرحلة الثانية: استكمال الحشوات التجميلية للأسنان المصابة ومعالجة الجذور.",
                "المرحلة الثالثة: تركيب التيجان الخزفية أو زرع الأسنان لتعويض الأسنان المفقودة.",
                "المرحلة الرابعة: المتابعة الوقائية كل 6 أشهر وتطبيق الفلورايد الموضعي."
            )
        } else {
            listOf(
                "Phase 1 (Urgent): Caries excavation, temporary restorations, and acute symptoms management.",
                "Phase 2 (Restorative & Endo): Direct composite restorations and root canal therapy completion.",
                "Phase 3 (Rehabilitation): Full contour crowns/onlays and implant-supported restorations.",
                "Phase 4 (Maintenance): Routine 6-month supportive periodontal and prophylaxis care."
            )
        }

        val briefing = if (isAr) {
            "عزيزي المريض ${patient.fullName}، بناءً على الفحص الإكلينيكي ومخطط الأسنان، تم إعداد خطة علاجية متكاملة تهدف لاستعادة صحة وجمال فمك ووظيفة المضغ الطبيعية. يرجى الالتزام بمواعيد الجلسات وتطبيق إرشادات نظافة الفم اليومية."
        } else {
            "Dear ${patient.fullName}, based on your clinical examination and dental chart, a personalized phased treatment plan has been prepared to restore your oral health, chewing function, and smile aesthetics. Please follow the scheduled visit timeline and daily hygiene recommendations."
        }

        return ClinicalAnalysisResult(
            diagnosisSummary = diag.trim(),
            treatmentPhases = phases,
            patientBriefing = briefing,
            isFromLiveAi = false
        )
    }
}
