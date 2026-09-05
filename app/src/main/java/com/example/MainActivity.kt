package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.DentalViewModel
import com.example.ui.dashboard.DashboardScreen
import com.example.ui.media.MediaComparisonScreen
import com.example.ui.patients.PatientsScreen
import com.example.ui.report.PdfReportScreen
import com.example.ui.theme.DentalVaultTheme

enum class DentalNavDestination(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    DASHBOARD("Odontogram", Icons.Filled.Dashboard, Icons.Outlined.Dashboard, "nav_dashboard"),
    MEDIA("Before/After", Icons.Filled.Compare, Icons.Outlined.Compare, "nav_media"),
    PATIENTS("Patients", Icons.Filled.People, Icons.Outlined.People, "nav_patients"),
    REPORT("PDF Report", Icons.Filled.PictureAsPdf, Icons.Outlined.PictureAsPdf, "nav_report")
}

class MainActivity : ComponentActivity() {

    private val viewModel: DentalViewModel by viewModels {
        val app = application as DentalApp
        DentalViewModel.Factory(app.repository)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DentalVaultTheme {
                var currentDestination by remember { mutableStateOf(DentalNavDestination.DASHBOARD) }
                val selectedPatient by viewModel.selectedPatient.collectAsState()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MedicalInformation,
                                        contentDescription = "Dental Vault",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Column {
                                        Text(
                                            text = "Dental Vault",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (selectedPatient != null) {
                                            Text(
                                                text = "Case: ${selectedPatient!!.fullName}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                            },
                            actions = {
                                if (currentDestination != DentalNavDestination.REPORT) {
                                    IconButton(
                                        onClick = { currentDestination = DentalNavDestination.REPORT },
                                        modifier = Modifier.testTag("top_action_report")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PictureAsPdf,
                                            contentDescription = "PDF Report",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            modifier = Modifier
                                .windowInsetsPadding(WindowInsets.navigationBars)
                                .testTag("bottom_nav_bar"),
                            containerColor = MaterialTheme.colorScheme.surface
                        ) {
                            DentalNavDestination.entries.forEach { dest ->
                                val isSelected = currentDestination == dest
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = { currentDestination = dest },
                                    icon = {
                                        Icon(
                                            imageVector = if (isSelected) dest.selectedIcon else dest.unselectedIcon,
                                            contentDescription = dest.title
                                        )
                                    },
                                    label = { Text(dest.title, fontSize = 11.sp) },
                                    modifier = Modifier.testTag(dest.testTag)
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentDestination) {
                            DentalNavDestination.DASHBOARD -> DashboardScreen(
                                viewModel = viewModel,
                                onNavigateToMedia = { currentDestination = DentalNavDestination.MEDIA },
                                onNavigateToReport = { currentDestination = DentalNavDestination.REPORT }
                            )
                            DentalNavDestination.MEDIA -> MediaComparisonScreen(
                                viewModel = viewModel
                            )
                            DentalNavDestination.PATIENTS -> PatientsScreen(
                                viewModel = viewModel,
                                onPatientSelected = { currentDestination = DentalNavDestination.DASHBOARD }
                            )
                            DentalNavDestination.REPORT -> PdfReportScreen(
                                viewModel = viewModel
                            )
                        }
                    }
                }
            }
        }
    }
}
