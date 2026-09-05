package com.example.ui.media

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import java.io.File
import kotlin.math.roundToInt

@Composable
fun BeforeAfterSlider(
    beforePath: String?,
    afterPath: String?,
    modifier: Modifier = Modifier,
    beforeLabel: String = "PRE-OP",
    afterLabel: String = "POST-OP",
    enableZoomPan: Boolean = true
) {
    var sliderRatio by remember { mutableFloatStateOf(0.5f) }
    var containerSize by remember { mutableStateOf(IntSize.Zero) }

    // Zoom & Pan state
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF0F172A))
            .onSizeChanged { containerSize = it }
            .then(
                if (enableZoomPan) {
                    Modifier.pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(1f, 4f)
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
                } else Modifier
            )
            .testTag("before_after_slider_container")
    ) {
        val dividerX = containerSize.width * sliderRatio

        // Zoom/Pan Layer
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offsetX,
                    translationY = offsetY
                )
        ) {
            // Layer 1: After Image (Full width background)
            ImageRenderer(
                path = afterPath,
                modifier = Modifier.fillMaxSize(),
                contentDescription = "After Clinical Photo"
            )

            // Layer 2: Before Image clipped to slider width
            if (containerSize.width > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(with(androidx.compose.ui.platform.LocalDensity.current) { dividerX.toDp() })
                        .clipToBounds()
                ) {
                    ImageRenderer(
                        path = beforePath,
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(with(androidx.compose.ui.platform.LocalDensity.current) { containerSize.width.toDp() }),
                        contentDescription = "Before Clinical Photo"
                    )
                }
            }
        }

        // Draggable Vertical Divider & Handle
        if (containerSize.width > 0) {
            // Divider Line
            Box(
                modifier = Modifier
                    .offset { IntOffset(dividerX.roundToInt() - 1, 0) }
                    .width(2.dp)
                    .fillMaxHeight()
                    .background(Color.White)
            )

            // Floating Circular Slider Thumb
            Box(
                modifier = Modifier
                    .offset { IntOffset((dividerX - 22.dp.toPx()).roundToInt(), (containerSize.height / 2f - 22.dp.toPx()).roundToInt()) }
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            if (containerSize.width > 0) {
                                val newRatio = (sliderRatio + dragAmount.x / containerSize.width).coerceIn(0.05f, 0.95f)
                                sliderRatio = newRatio
                            }
                        }
                    }
                    .testTag("slider_handle"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SwapHoriz,
                    contentDescription = "Slide comparison",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Labels (PRE-OP & POST-OP Badges)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color.Black.copy(alpha = 0.65f),
                contentColor = Color.White
            ) {
                Text(
                    text = beforeLabel,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color.Black.copy(alpha = 0.65f),
                contentColor = Color.White
            ) {
                Text(
                    text = afterLabel,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        // Zoom Reset button if scaled
        if (scale > 1.05f) {
            FloatingActionButton(
                onClick = {
                    scale = 1f
                    offsetX = 0f
                    offsetY = 0f
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp)
                    .size(36.dp),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Text("1x", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ImageRenderer(
    path: String?,
    modifier: Modifier = Modifier,
    contentDescription: String
) {
    if (path.isNullOrBlank()) {
        Box(
            modifier = modifier.background(Color(0xFF1E293B)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Image not available",
                color = Color(0xFF94A3B8),
                fontSize = 12.sp
            )
        }
    } else {
        val file = remember(path) { File(path) }
        if (file.exists()) {
            val bitmap = remember(path) {
                BitmapFactory.decodeFile(file.absolutePath)
            }
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = contentDescription,
                    modifier = modifier,
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = modifier.background(Color(0xFF1E293B)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Invalid image file", color = Color(0xFF94A3B8), fontSize = 12.sp)
                }
            }
        } else {
            AsyncImage(
                model = path,
                contentDescription = contentDescription,
                modifier = modifier,
                contentScale = ContentScale.Crop
            )
        }
    }
}
