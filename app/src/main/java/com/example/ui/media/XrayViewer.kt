package com.example.ui.media

import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.ClinicalMedia
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

data class AnnotationPin(
    val x: Float, // Normalized 0..1
    val y: Float, // Normalized 0..1
    val label: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun XrayViewer(
    media: ClinicalMedia,
    onSaveAnnotation: ((updatedJson: String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var containerSize by remember { mutableStateOf(IntSize.Zero) }

    // Diagnostic filters
    var isInverted by remember { mutableStateOf(false) }
    var isHighContrast by remember { mutableStateOf(false) }

    // Parse annotations
    val annotations = remember(media.annotationsJson) {
        val list = mutableStateListOf<AnnotationPin>()
        try {
            if (media.annotationsJson.isNotBlank()) {
                val jsonArray = JSONArray(media.annotationsJson)
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    list.add(
                        AnnotationPin(
                            x = obj.optDouble("x", 0.5).toFloat(),
                            y = obj.optDouble("y", 0.5).toFloat(),
                            label = obj.optString("label", "Marker")
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        list
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var pendingTapPosition by remember { mutableStateOf(Offset.Zero) }
    var newPinLabel by remember { mutableStateOf("") }
    var selectedPin by remember { mutableStateOf<AnnotationPin?>(null) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("xray_viewer_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = media.mediaType.label,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Pinch to zoom & pan • Tap to place annotation pin",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Filter Invert button
                    IconButton(
                        onClick = { isInverted = !isInverted },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.InvertColors,
                            contentDescription = "Invert Radiograph Film",
                            tint = if (isInverted) MaterialTheme.colorScheme.primary else Color.White
                        )
                    }

                    // Reset zoom
                    if (scale > 1.05f) {
                        IconButton(
                            onClick = {
                                scale = 1f
                                offsetX = 0f
                                offsetY = 0f
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ZoomOutMap,
                                contentDescription = "Reset Zoom",
                                tint = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Interactive X-Ray Canvas Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.Black)
                    .onSizeChanged { containerSize = it }
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(1f, 6f)
                            if (scale > 1f) {
                                val maxOffsetX = (containerSize.width * (scale - 1)) / 2f
                                val maxOffsetY = (containerSize.height * (scale - 1)) / 2f
                                offsetX = (offsetX + pan.x * scale).coerceIn(-maxOffsetX, maxOffsetX)
                                offsetY = (offsetY + pan.y * scale).coerceIn(-maxOffsetY, maxOffsetY)
                            } else {
                                offsetX = 0f
                                offsetY = 0f
                            }
                        }
                    }
                    .pointerInput(Unit) {
                        detectTapGestures { tapOffset ->
                            if (containerSize.width > 0 && containerSize.height > 0) {
                                pendingTapPosition = tapOffset
                                newPinLabel = ""
                                showAddDialog = true
                            }
                        }
                    }
            ) {
                // Rendered Image
                val file = remember(media.localPath) { File(media.localPath) }
                val bitmap = remember(media.localPath) {
                    if (file.exists()) BitmapFactory.decodeFile(file.absolutePath) else null
                }

                val colorFilter = remember(isInverted, isHighContrast) {
                    if (isInverted) {
                        val matrix = ColorMatrix(
                            floatArrayOf(
                                -1f, 0f, 0f, 0f, 255f,
                                0f, -1f, 0f, 0f, 255f,
                                0f, 0f, -1f, 0f, 255f,
                                0f, 0f, 0f, 1f, 0f
                            )
                        )
                        ColorFilter.colorMatrix(matrix)
                    } else null
                }

                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Diagnostic X-ray Film",
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(
                                scaleX = scale,
                                scaleY = scale,
                                translationX = offsetX,
                                translationY = offsetY
                            ),
                        contentScale = ContentScale.Fit,
                        colorFilter = colorFilter
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Radiograph file not found", color = Color(0xFF64748B))
                    }
                }

                // Render Diagnostic Annotation Pins
                annotations.forEach { pin ->
                    val pinPxX = pin.x * containerSize.width
                    val pinPxY = pin.y * containerSize.height

                    Box(
                        modifier = Modifier
                            .offset(
                                x = with(androidx.compose.ui.platform.LocalDensity.current) { (pinPxX - 14.dp.toPx()).toDp() },
                                y = with(androidx.compose.ui.platform.LocalDensity.current) { (pinPxY - 28.dp.toPx()).toDp() }
                            )
                            .clickable { selectedPin = pin }
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color.Red,
                                contentColor = Color.White
                            ) {
                                Text(
                                    text = pin.label,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = pin.label,
                                tint = Color.Red,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Annotations chips list below
            if (annotations.isNotEmpty()) {
                Text(
                    text = "Clinical Markers (${annotations.size}):",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    annotations.forEach { pin ->
                        InputChip(
                            selected = selectedPin == pin,
                            onClick = { selectedPin = pin },
                            label = { Text(pin.label, fontSize = 11.sp) },
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        annotations.remove(pin)
                                        saveAnnotationsToJson(annotations, onSaveAnnotation)
                                    },
                                    modifier = Modifier.size(16.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Delete marker",
                                        tint = Color.White
                                    )
                                }
                            },
                            colors = InputChipDefaults.inputChipColors(
                                containerColor = Color(0xFF334155),
                                labelColor = Color.White
                            )
                        )
                    }
                }
            }
        }
    }

    // Add Annotation Dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Diagnostic Marker") },
            text = {
                Column {
                    Text(
                        text = "Enter pathology note or anatomical measurement:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newPinLabel,
                        onValueChange = { newPinLabel = it },
                        label = { Text("e.g., Periapical lesion, Caries #16") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPinLabel.isNotBlank() && containerSize.width > 0 && containerSize.height > 0) {
                            val normX = (pendingTapPosition.x / containerSize.width).coerceIn(0f, 1f)
                            val normY = (pendingTapPosition.y / containerSize.height).coerceIn(0f, 1f)
                            annotations.add(AnnotationPin(normX, normY, newPinLabel.trim()))
                            saveAnnotationsToJson(annotations, onSaveAnnotation)
                        }
                        showAddDialog = false
                    }
                ) {
                    Text("Pin Note")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun saveAnnotationsToJson(
    list: List<AnnotationPin>,
    onSaveAnnotation: ((String) -> Unit)?
) {
    if (onSaveAnnotation == null) return
    val jsonArray = JSONArray()
    list.forEach { pin ->
        val obj = JSONObject()
        obj.put("x", pin.x)
        obj.put("y", pin.y)
        obj.put("label", pin.label)
        jsonArray.put(obj)
    }
    onSaveAnnotation(jsonArray.toString())
}
