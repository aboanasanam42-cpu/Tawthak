package com.example.ui.media

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.ClinicalMedia
import com.example.domain.model.MediaType
import com.example.ui.DentalViewModel
import com.example.util.ImageCompressor
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaComparisonScreen(
    viewModel: DentalViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val selectedPatient by viewModel.selectedPatient.collectAsState()
    val mediaList by viewModel.currentMedia.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Before/After Slider, 1: Diagnostic Radiographs, 2: All Gallery
    var selectedMediaTypeToAdd by remember { mutableStateOf(MediaType.INTRAORAL_BEFORE) }
    var showTypePickerModal by remember { mutableStateOf(false) }

    // Photo picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                val compressedFile = ImageCompressor.compressAndSaveImage(context, uri)
                if (compressedFile != null) {
                    viewModel.addMedia(
                        mediaType = selectedMediaTypeToAdd,
                        file = compressedFile,
                        visitId = null
                    )
                }
            }
        }
    }

    // Identify before and after photos
    val beforeMedia = remember(mediaList) {
        mediaList.firstOrNull { it.mediaType == MediaType.INTRAORAL_BEFORE }
    }
    val afterMedia = remember(mediaList) {
        mediaList.firstOrNull { it.mediaType == MediaType.INTRAORAL_AFTER }
    }
    val radiographs = remember(mediaList) {
        mediaList.filter { it.mediaType in listOf(MediaType.OPG, MediaType.PERIAPICAL, MediaType.CEPHALOMETRIC) }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
    ) {
        // Patient Header Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Clinical Photography & X-Rays",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = selectedPatient?.let { "Patient: ${it.fullName}" } ?: "No active patient",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                FilledTonalButton(
                    onClick = { showTypePickerModal = true },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("add_photo_button")
                ) {
                    Icon(imageVector = Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Media", fontSize = 12.sp)
                }
            }
        }

        // Tab Row
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.clip(RoundedCornerShape(14.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Before / After") },
                    icon = { Icon(Icons.Default.Compare, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Radiographs (${radiographs.size})") },
                    icon = { Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("All Media (${mediaList.size})") },
                    icon = { Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }
        }

        // Tab 0: Interactive Split-Screen Before/After Slider
        if (selectedTab == 0) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Interactive Split Comparison",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Drag slider left/right",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        BeforeAfterSlider(
                            beforePath = beforeMedia?.localPath,
                            afterPath = afterMedia?.localPath,
                            beforeLabel = "PRE-OP",
                            afterLabel = "POST-OP"
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Before: ${beforeMedia?.let { "Loaded" } ?: "No photo yet"}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "After: ${afterMedia?.let { "Loaded" } ?: "No photo yet"}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Tab 1: Diagnostic Radiographs with Zoom/Pan & Marker Pin Drop
        if (selectedTab == 1) {
            if (radiographs.isEmpty()) {
                item {
                    EmptyMediaCard("No X-Rays or Radiographs attached. Tap 'Add Media' to import an OPG or Periapical radiograph.")
                }
            } else {
                items(radiographs) { media ->
                    XrayViewer(
                        media = media,
                        onSaveAnnotation = { updatedJson ->
                            viewModel.updateMediaAnnotations(media, updatedJson)
                        }
                    )
                }
            }
        }

        // Tab 2: Full Clinical Gallery Grid
        if (selectedTab == 2) {
            if (mediaList.isEmpty()) {
                item {
                    EmptyMediaCard("No media records saved for this case.")
                }
            } else {
                items(mediaList) { media ->
                    MediaItemCard(
                        media = media,
                        onDelete = { viewModel.deleteMedia(media.id) }
                    )
                }
            }
        }
    }

    // Modal to pick which media type to upload
    if (showTypePickerModal) {
        AlertDialog(
            onDismissRequest = { showTypePickerModal = false },
            title = { Text("Select Media Type") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        MediaType.INTRAORAL_BEFORE,
                        MediaType.INTRAORAL_AFTER,
                        MediaType.OPG,
                        MediaType.PERIAPICAL,
                        MediaType.CEPHALOMETRIC
                    ).forEach { type ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    selectedMediaTypeToAdd = type
                                    showTypePickerModal = false
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary)
                                )
                                Text(
                                    text = type.label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showTypePickerModal = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun EmptyMediaCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.ImageNotSupported,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun MediaItemCard(
    media: ClinicalMedia,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Text(
                        text = media.mediaType.badge,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete photo",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0F172A))
            ) {
                ImageRenderer(
                    path = media.localPath,
                    modifier = Modifier.fillMaxSize(),
                    contentDescription = media.mediaType.label
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = media.mediaType.label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
