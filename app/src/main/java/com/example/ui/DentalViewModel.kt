package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.ai.ClinicalAnalysisResult
import com.example.data.ai.ClinicalAuditResult
import com.example.data.ai.GeminiClinicalService
import com.example.data.repository.DentalRepository
import com.example.domain.model.*
import com.example.util.PdfGenerator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

data class DentalDashboardStats(
    val totalPatients: Int = 0,
    val totalVisits: Int = 0,
    val totalBilled: Double = 0.0,
    val totalPaid: Double = 0.0
) {
    val totalOutstanding: Double get() = (totalBilled - totalPaid).coerceAtLeast(0.0)
}

@OptIn(ExperimentalCoroutinesApi::class)
class DentalViewModel(
    private val repository: DentalRepository,
    private val geminiService: GeminiClinicalService = GeminiClinicalService()
) : ViewModel() {

    private val _appLanguage = MutableStateFlow(com.example.util.AppLanguage.ARABIC)
    val appLanguage: StateFlow<com.example.util.AppLanguage> = _appLanguage.asStateFlow()

    fun setLanguage(language: com.example.util.AppLanguage) {
        _appLanguage.value = language
    }

    fun toggleLanguage() {
        _appLanguage.value = _appLanguage.value.other()
    }

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val patients: StateFlow<List<Patient>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) repository.allPatients else repository.searchPatients(query)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedPatientId = MutableStateFlow<String?>(null)
    val selectedPatientId: StateFlow<String?> = _selectedPatientId.asStateFlow()

    val selectedPatient: StateFlow<Patient?> = _selectedPatientId
        .flatMapLatest { id ->
            if (id == null) flowOf(null) else repository.getPatientById(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val currentOdontogram: StateFlow<List<OdontogramRecord>> = _selectedPatientId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else repository.getOdontogramForPatient(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentVisits: StateFlow<List<Visit>> = _selectedPatientId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else repository.getVisitsForPatient(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentMedia: StateFlow<List<ClinicalMedia>> = _selectedPatientId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else repository.getMediaForPatient(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dashboardStats: StateFlow<DentalDashboardStats> = combine(
        repository.patientCount,
        repository.visitCount,
        repository.totalBilled,
        repository.totalPaid
    ) { pCount, vCount, billed, paid ->
        DentalDashboardStats(
            totalPatients = pCount,
            totalVisits = vCount,
            totalBilled = billed,
            totalPaid = paid
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DentalDashboardStats())

    // Gemini AI & Clinical Safety
    val isAiConfigured: Boolean get() = geminiService.isApiKeyConfigured()

    private val _aiAnalysis = MutableStateFlow<ClinicalAnalysisResult?>(null)
    val aiAnalysis: StateFlow<ClinicalAnalysisResult?> = _aiAnalysis.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    private val _auditResult = MutableStateFlow<ClinicalAuditResult?>(null)
    val auditResult: StateFlow<ClinicalAuditResult?> = _auditResult.asStateFlow()

    fun runAiCaseAnalysis(language: String = "ar") = viewModelScope.launch {
        val patient = selectedPatient.value ?: return@launch
        _isAiLoading.value = true
        val result = geminiService.analyzeCase(
            patient = patient,
            odontogram = currentOdontogram.value,
            visits = currentVisits.value,
            language = language
        )
        _aiAnalysis.value = result.getOrNull()
        _isAiLoading.value = false
    }

    fun runSafetyAudit(procedure: String) = viewModelScope.launch {
        val patient = selectedPatient.value ?: return@launch
        val audit = geminiService.auditSafetyAndContraindications(
            patient = patient,
            odontogram = currentOdontogram.value,
            plannedProcedure = procedure
        )
        _auditResult.value = audit
    }

    fun clearAudit() {
        _auditResult.value = null
    }

    fun clearAiAnalysis() {
        _aiAnalysis.value = null
    }

    init {
        // Auto-select first patient if available
        viewModelScope.launch {
            patients.collect { list ->
                if (_selectedPatientId.value == null && list.isNotEmpty()) {
                    _selectedPatientId.value = list.first().id
                }
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectPatient(patientId: String) {
        _selectedPatientId.value = patientId
    }

    fun addPatient(patient: Patient) = viewModelScope.launch {
        repository.insertPatient(patient)
        _selectedPatientId.value = patient.id
    }

    fun updatePatient(patient: Patient) = viewModelScope.launch {
        repository.updatePatient(patient)
    }

    fun deletePatient(patientId: String) = viewModelScope.launch {
        repository.deletePatient(patientId)
        if (_selectedPatientId.value == patientId) {
            val remaining = patients.value.filter { it.id != patientId }
            _selectedPatientId.value = remaining.firstOrNull()?.id
        }
    }

    fun updateTooth(toothNumber: Int, status: ToothStatus, surfaces: List<String>) = viewModelScope.launch {
        val patientId = _selectedPatientId.value ?: return@launch
        repository.updateToothStatus(patientId, toothNumber, status, surfaces)
    }

    fun resetTooth(toothNumber: Int) = viewModelScope.launch {
        val patientId = _selectedPatientId.value ?: return@launch
        repository.resetTooth(patientId, toothNumber)
    }

    fun clearOdontogram() = viewModelScope.launch {
        val patientId = _selectedPatientId.value ?: return@launch
        repository.clearOdontogram(patientId)
    }

    fun addVisit(visit: Visit) = viewModelScope.launch {
        repository.insertVisit(visit)
    }

    fun deleteVisit(visitId: String) = viewModelScope.launch {
        repository.deleteVisit(visitId)
    }

    fun addMedia(mediaType: MediaType, file: File, visitId: String?) = viewModelScope.launch {
        val patientId = _selectedPatientId.value ?: return@launch
        val media = ClinicalMedia(
            id = UUID.randomUUID().toString(),
            patientId = patientId,
            visitId = visitId,
            mediaType = mediaType,
            localPath = file.absolutePath,
            remoteUrl = null,
            annotationsJson = "[]",
            createdAt = System.currentTimeMillis()
        )
        repository.insertMedia(media)
    }

    fun updateMediaAnnotations(media: ClinicalMedia, updatedJson: String) = viewModelScope.launch {
        repository.updateMedia(media.copy(annotationsJson = updatedJson))
    }

    fun deleteMedia(mediaId: String) = viewModelScope.launch {
        repository.deleteMedia(mediaId)
    }

    fun generateReport(context: Context, onReady: (File) -> Unit) = viewModelScope.launch {
        val patient = selectedPatient.value ?: return@launch
        val file = PdfGenerator.generateClinicalReport(
            context = context,
            patient = patient,
            visits = currentVisits.value,
            odontogram = currentOdontogram.value,
            mediaList = currentMedia.value
        )
        onReady(file)
    }

    class Factory(private val repository: DentalRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DentalViewModel(repository) as T
        }
    }
}
