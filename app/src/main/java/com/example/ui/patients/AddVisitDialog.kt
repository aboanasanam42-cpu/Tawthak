package com.example.ui.patients

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.domain.model.Visit
import java.util.UUID

@Composable
fun AddVisitDialog(
    patientId: String,
    onDismiss: () -> Unit,
    onConfirm: (Visit) -> Unit,
    visitToEdit: Visit? = null
) {
    var chiefComplaint by remember { mutableStateOf(visitToEdit?.chiefComplaint ?: "") }
    var diagnosis by remember { mutableStateOf(visitToEdit?.diagnosis ?: "") }
    var treatmentPlan by remember { mutableStateOf(visitToEdit?.treatmentPlan ?: "") }
    var costText by remember { mutableStateOf(visitToEdit?.cost?.toString() ?: "0.0") }
    var paidText by remember { mutableStateOf(visitToEdit?.paid?.toString() ?: "0.0") }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (visitToEdit == null) "Log Clinical Visit" else "Edit Visit Record",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = chiefComplaint,
                    onValueChange = { chiefComplaint = it },
                    label = { Text("Chief Complaint *") },
                    placeholder = { Text("e.g. Sensitivity in upper left molar") },
                    minLines = 2,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("visit_complaint_input")
                )

                OutlinedTextField(
                    value = diagnosis,
                    onValueChange = { diagnosis = it },
                    label = { Text("Clinical Diagnosis *") },
                    placeholder = { Text("e.g. Irreversible pulpitis #26") },
                    minLines = 2,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("visit_diagnosis_input")
                )

                OutlinedTextField(
                    value = treatmentPlan,
                    onValueChange = { treatmentPlan = it },
                    label = { Text("Treatment Performed / Plan *") },
                    placeholder = { Text("e.g. Pulpectomy, canal instrumentation, temporary restoration") },
                    minLines = 2,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("visit_treatment_input")
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = costText,
                        onValueChange = { costText = it },
                        label = { Text("Total Cost ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("visit_cost_input")
                    )

                    OutlinedTextField(
                        value = paidText,
                        onValueChange = { paidText = it },
                        label = { Text("Paid Amount ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("visit_paid_input")
                    )
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (chiefComplaint.isBlank() || diagnosis.isBlank() || treatmentPlan.isBlank()) {
                        errorMessage = "Please fill in complaint, diagnosis, and treatment."
                        return@Button
                    }
                    val cost = costText.toDoubleOrNull() ?: 0.0
                    val paid = paidText.toDoubleOrNull() ?: 0.0

                    val visit = Visit(
                        id = visitToEdit?.id ?: UUID.randomUUID().toString(),
                        patientId = patientId,
                        visitDate = visitToEdit?.visitDate ?: System.currentTimeMillis(),
                        chiefComplaint = chiefComplaint.trim(),
                        diagnosis = diagnosis.trim(),
                        treatmentPlan = treatmentPlan.trim(),
                        cost = cost,
                        paid = paid
                    )
                    onConfirm(visit)
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("save_visit_button")
            ) {
                Text("Save Visit Record")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
