package com.example.ui.dashboard

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.FdiTeethHelper
import com.example.domain.model.OdontogramRecord
import com.example.domain.model.ToothStatus
import com.example.domain.model.ToothSurface

@Composable
fun OdontogramView(
    records: List<OdontogramRecord>,
    onToothClick: (toothNumber: Int, record: OdontogramRecord?) -> Unit,
    modifier: Modifier = Modifier
) {
    var isPediatricMode by remember { mutableStateOf(false) }

    val recordMap = remember(records) {
        records.associateBy { it.toothNumber }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("odontogram_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with Dentition Switcher
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "FDI Teeth Chart",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isPediatricMode) "Primary / Deciduous (51-85)" else "Adult Permanent (11-48)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    FilterChip(
                        selected = !isPediatricMode,
                        onClick = { isPediatricMode = false },
                        label = { Text("Adult", fontSize = 12.sp) },
                        modifier = Modifier.height(32.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                    FilterChip(
                        selected = isPediatricMode,
                        onClick = { isPediatricMode = true },
                        label = { Text("Pediatric", fontSize = 12.sp) },
                        modifier = Modifier.height(32.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Odontogram Legend
            OdontogramLegend()

            Spacer(modifier = Modifier.height(16.dp))

            // Horizontally Scrollable Dental Arch Matrix
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .padding(vertical = 12.dp, horizontal = 6.dp)
            ) {
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Quadrant Header
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PATIENT's RIGHT (Maxillary)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(40.dp))
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(16.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant)
                        )
                        Spacer(modifier = Modifier.width(40.dp))
                        Text(
                            text = "PATIENT's LEFT (Maxillary)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Upper Arch (Maxilla)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Upper Right Quadrant
                        val upperRight = if (isPediatricMode) FdiTeethHelper.upperRightPrimary else FdiTeethHelper.upperRightPermanent
                        upperRight.forEach { toothNum ->
                            ToothCell(
                                toothNumber = toothNum,
                                isUpper = true,
                                record = recordMap[toothNum],
                                onClick = { onToothClick(toothNum, recordMap[toothNum]) }
                            )
                        }

                        // Midline Divider
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 6.dp)
                                .width(2.dp)
                                .height(80.dp)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                        )

                        // Upper Left Quadrant
                        val upperLeft = if (isPediatricMode) FdiTeethHelper.upperLeftPrimary else FdiTeethHelper.upperLeftPermanent
                        upperLeft.forEach { toothNum ->
                            ToothCell(
                                toothNumber = toothNum,
                                isUpper = true,
                                record = recordMap[toothNum],
                                onClick = { onToothClick(toothNum, recordMap[toothNum]) }
                            )
                        }
                    }

                    // Arch Midline Horizontal Separator
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.width(620.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f),
                            thickness = 1.dp
                        )
                    }

                    // Lower Arch (Mandible)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Lower Right Quadrant
                        val lowerRight = if (isPediatricMode) FdiTeethHelper.lowerRightPrimary else FdiTeethHelper.lowerRightPermanent
                        lowerRight.forEach { toothNum ->
                            ToothCell(
                                toothNumber = toothNum,
                                isUpper = false,
                                record = recordMap[toothNum],
                                onClick = { onToothClick(toothNum, recordMap[toothNum]) }
                            )
                        }

                        // Midline Divider
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 6.dp)
                                .width(2.dp)
                                .height(80.dp)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                        )

                        // Lower Left Quadrant
                        val lowerLeft = if (isPediatricMode) FdiTeethHelper.lowerLeftPrimary else FdiTeethHelper.lowerLeftPermanent
                        lowerLeft.forEach { toothNum ->
                            ToothCell(
                                toothNumber = toothNum,
                                isUpper = false,
                                record = recordMap[toothNum],
                                onClick = { onToothClick(toothNum, recordMap[toothNum]) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Lower Arch Label
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PATIENT's RIGHT (Mandibular)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.width(40.dp))
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(16.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant)
                        )
                        Spacer(modifier = Modifier.width(40.dp))
                        Text(
                            text = "PATIENT's LEFT (Mandibular)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ToothCell(
    toothNumber: Int,
    isUpper: Boolean,
    record: OdontogramRecord?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val status = record?.status ?: ToothStatus.SOUND
    val surfaces = record?.surfaces ?: emptyList()

    val statusColor by animateColorAsState(
        targetValue = status.color,
        label = "tooth_status_color"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .padding(horizontal = 3.dp)
            .width(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(vertical = 4.dp)
            .testTag("tooth_$toothNumber")
    ) {
        // Tooth number indicator for upper arch
        if (isUpper) {
            Text(
                text = "$toothNumber",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (status != ToothStatus.SOUND) statusColor else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        // Custom Anatomical Canvas drawing of the tooth & 5 surfaces
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color.White)
                .border(
                    width = if (status != ToothStatus.SOUND) 2.dp else 1.dp,
                    color = if (status != ToothStatus.SOUND) statusColor else Color(0xFFCBD5E1),
                    shape = RoundedCornerShape(6.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawToothSurfaces(
                    status = status,
                    surfaces = surfaces,
                    isUpper = isUpper,
                    toothNumber = toothNumber
                )
            }

            // If missing, draw prominent X mark
            if (status == ToothStatus.MISSING) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Missing",
                    tint = ToothStatus.MISSING.color,
                    modifier = Modifier.size(24.dp)
                )
            } else if (status == ToothStatus.IMPLANT) {
                // Implant symbol
                Icon(
                    imageVector = Icons.Default.Build,
                    contentDescription = "Implant",
                    tint = ToothStatus.IMPLANT.color,
                    modifier = Modifier.size(18.dp)
                )
            } else if (status == ToothStatus.ENDO) {
                // Endo symbol
                Icon(
                    imageVector = Icons.Default.FlashOn,
                    contentDescription = "Endo",
                    tint = ToothStatus.ENDO.color,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Tooth number indicator for lower arch
        if (!isUpper) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$toothNumber",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (status != ToothStatus.SOUND) statusColor else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun DrawScope.drawToothSurfaces(
    status: ToothStatus,
    surfaces: List<String>,
    isUpper: Boolean,
    toothNumber: Int
) {
    val w = size.width
    val h = size.height
    val strokeColor = Color(0xFF94A3B8)
    val defaultFill = Color(0xFFF8FAFC)

    // Center Occlusal / Incisal square
    val centerLeft = w * 0.32f
    val centerTop = h * 0.32f
    val centerSize = w * 0.36f

    // Check surface presence
    val hasOcclusal = surfaces.any { it.equals("O", ignoreCase = true) || it.equals("OCCLUSAL", ignoreCase = true) }
    val hasBuccal = surfaces.any { it.equals("B", ignoreCase = true) || it.equals("BUCCAL", ignoreCase = true) }
    val hasLingual = surfaces.any { it.equals("L", ignoreCase = true) || it.equals("LINGUAL", ignoreCase = true) }
    val hasMesial = surfaces.any { it.equals("M", ignoreCase = true) || it.equals("MESIAL", ignoreCase = true) }
    val hasDistal = surfaces.any { it.equals("D", ignoreCase = true) || it.equals("DISTAL", ignoreCase = true) }

    fun getSurfaceColor(active: Boolean): Color {
        return when {
            status == ToothStatus.CROWN -> ToothStatus.CROWN.color.copy(alpha = 0.4f)
            status == ToothStatus.ENDO -> ToothStatus.ENDO.color.copy(alpha = 0.25f)
            status == ToothStatus.CARIES && (active || surfaces.isEmpty()) -> ToothStatus.CARIES.color
            status == ToothStatus.RESTORATION && (active || surfaces.isEmpty()) -> ToothStatus.RESTORATION.color
            else -> defaultFill
        }
    }

    // Top trapezoid
    val topPath = Path().apply {
        moveTo(0f, 0f)
        lineTo(w, 0f)
        lineTo(centerLeft + centerSize, centerTop)
        lineTo(centerLeft, centerTop)
        close()
    }
    drawPath(topPath, color = getSurfaceColor(if (isUpper) hasBuccal else hasLingual))
    drawPath(topPath, color = strokeColor, style = Stroke(width = 1f))

    // Bottom trapezoid
    val bottomPath = Path().apply {
        moveTo(centerLeft, centerTop + centerSize)
        lineTo(centerLeft + centerSize, centerTop + centerSize)
        lineTo(w, h)
        lineTo(0f, h)
        close()
    }
    drawPath(bottomPath, color = getSurfaceColor(if (isUpper) hasLingual else hasBuccal))
    drawPath(bottomPath, color = strokeColor, style = Stroke(width = 1f))

    // Left trapezoid
    val leftPath = Path().apply {
        moveTo(0f, 0f)
        lineTo(centerLeft, centerTop)
        lineTo(centerLeft, centerTop + centerSize)
        lineTo(0f, h)
        close()
    }
    val q = toothNumber / 10
    val isRightSideOfArch = q in listOf(1, 4, 5, 8)
    val isLeftSurfaceMesial = !isRightSideOfArch
    drawPath(leftPath, color = getSurfaceColor(if (isLeftSurfaceMesial) hasMesial else hasDistal))
    drawPath(leftPath, color = strokeColor, style = Stroke(width = 1f))

    // Right trapezoid
    val rightPath = Path().apply {
        moveTo(centerLeft + centerSize, centerTop)
        lineTo(w, 0f)
        lineTo(w, h)
        lineTo(centerLeft + centerSize, centerTop + centerSize)
        close()
    }
    drawPath(rightPath, color = getSurfaceColor(if (isLeftSurfaceMesial) hasDistal else hasMesial))
    drawPath(rightPath, color = strokeColor, style = Stroke(width = 1f))

    // Center Occlusal square
    val centerRect = Rect(centerLeft, centerTop, centerLeft + centerSize, centerTop + centerSize)
    drawRect(
        color = getSurfaceColor(hasOcclusal),
        topLeft = Offset(centerLeft, centerTop),
        size = Size(centerSize, centerSize)
    )
    drawRect(
        color = strokeColor,
        topLeft = Offset(centerLeft, centerTop),
        size = Size(centerSize, centerSize),
        style = Stroke(width = 1f)
    )
}

@Composable
fun OdontogramLegend() {
    val items = listOf(
        ToothStatus.SOUND,
        ToothStatus.CARIES,
        ToothStatus.RESTORATION,
        ToothStatus.ENDO,
        ToothStatus.CROWN,
        ToothStatus.MISSING,
        ToothStatus.IMPLANT
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { status ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(status.color)
                )
                Text(
                    text = status.label,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
