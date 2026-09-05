package com.example.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.FdiTeethHelper
import com.example.domain.model.OdontogramRecord
import com.example.domain.model.ToothStatus
import com.example.domain.model.ToothSurface
import com.example.util.LocalDentalStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToothActionSheet(
    toothNumber: Int,
    currentRecord: OdontogramRecord?,
    onDismiss: () -> Unit,
    onSaveStatus: (toothNumber: Int, status: ToothStatus, surfaces: List<String>) -> Unit,
    onResetTooth: (toothNumber: Int) -> Unit
) {
    val strings = LocalDentalStrings.current

    var selectedStatus by remember(currentRecord) {
        mutableStateOf(currentRecord?.status ?: ToothStatus.SOUND)
    }

    val initialSurfaces = remember(currentRecord) {
        currentRecord?.surfaces?.toSet() ?: emptySet()
    }
    var selectedSurfaces by remember(currentRecord) {
        mutableStateOf(initialSurfaces)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp)
                .testTag("tooth_action_sheet")
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (strings.isAr) "السن رقم #$toothNumber" else "Tooth #$toothNumber",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = FdiTeethHelper.getToothName(toothNumber),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (currentRecord != null) {
                    IconButton(
                        onClick = {
                            onResetTooth(toothNumber)
                            onDismiss()
                        },
                        modifier = Modifier.testTag("reset_tooth_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = strings.resetToSound,
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Clinical Status Selector
            Text(
                text = if (strings.isAr) "التشخيص وحالة السن السريرية:" else "Clinical Diagnosis / Condition",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(10.dp))

            val statuses = ToothStatus.entries.toList()
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                statuses.chunked(2).forEach { rowStatuses ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowStatuses.forEach { status ->
                            val isSelected = selectedStatus == status
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) status.color else MaterialTheme.colorScheme.outlineVariant,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { selectedStatus = status }
                                    .testTag("status_chip_${status.name}"),
                                color = if (isSelected) status.color.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clip(CircleShape)
                                            .background(status.color)
                                    )
                                    Text(
                                        text = strings.getToothStatusLabel(status),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                        if (rowStatuses.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            // Surface Selector (relevant for Caries and Restoration)
            if (selectedStatus in listOf(ToothStatus.CARIES, ToothStatus.RESTORATION, ToothStatus.CROWN, ToothStatus.ENDO)) {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = strings.surfacesTitle,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ToothSurface.entries.forEach { surface ->
                        val isChecked = selectedSurfaces.contains(surface.code)
                        FilterChip(
                            selected = isChecked,
                            onClick = {
                                selectedSurfaces = if (isChecked) {
                                    selectedSurfaces - surface.code
                                } else {
                                    selectedSurfaces + surface.code
                                }
                            },
                            label = {
                                Text(
                                    text = surface.code,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            leadingIcon = if (isChecked) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.testTag("surface_chip_${surface.code}")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(strings.cancel)
                }

                Button(
                    onClick = {
                        onSaveStatus(
                            toothNumber,
                            selectedStatus,
                            selectedSurfaces.toList()
                        )
                        onDismiss()
                    },
                    modifier = Modifier
                        .weight(1.5f)
                        .testTag("save_tooth_status_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(strings.save)
                }
            }
        }
    }
}
