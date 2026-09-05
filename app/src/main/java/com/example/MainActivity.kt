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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.DentalViewModel
import com.example.ui.dashboard.DashboardScreen
import com.example.ui.media.MediaComparisonScreen
import com.example.ui.patients.PatientsScreen
import com.example.ui.report.PdfReportScreen
import com.example.ui.theme.DentalVaultTheme
import com.example.util.AppLanguage
import com.example.util.DentalStrings
import com.example.util.DeveloperCreditBanner
import com.example.util.LanguageSelectorBox
import com.example.util.LocalAppLanguage
import com.example.util.LocalDentalStrings

enum class DentalNavDestination(
    val titleEn: String,
    val titleAr: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    DASHBOARD("Odontogram", "مخطط الأسنان", Icons.Filled.Dashboard, Icons.Outlined.Dashboard, "nav_dashboard"),
    MEDIA("Before/After", "مقارنة قبل/بعد", Icons.Filled.Compare, Icons.Outlined.Compare, "nav_media"),
    PATIENTS("Patients", "سجل المرضى", Icons.Filled.People, Icons.Outlined.People, "nav_patients"),
    REPORT("PDF Report", "تقرير PDF", Icons.Filled.PictureAsPdf, Icons.Outlined.PictureAsPdf, "nav_report");

    fun getTitle(lang: AppLanguage): String = if (lang == AppLanguage.ARABIC) titleAr else titleEn
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
            val currentLanguage by viewModel.appLanguage.collectAsState()
            val strings = remember(currentLanguage) { DentalStrings(currentLanguage) }
            val layoutDirection = if (currentLanguage == AppLanguage.ARABIC) LayoutDirection.Rtl else LayoutDirection.Ltr

            CompositionLocalProvider(
                LocalAppLanguage provides currentLanguage,
                LocalDentalStrings provides strings,
                LocalLayoutDirection provides layoutDirection
            ) {
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
                                            contentDescription = strings.appName,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Column {
                                            Text(
                                                text = strings.appName,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            if (selectedPatient != null) {
                                                Text(
                                                    text = "${strings.casePrefix}${selectedPatient!!.fullName}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }
                                    }
                                },
                                actions = {
                                    // Language Selector Box (مربع تغيير اللغة)
                                    LanguageSelectorBox(
                                        currentLanguage = currentLanguage,
                                        onLanguageChanged = { newLang ->
                                            viewModel.setLanguage(newLang)
                                        }
                                    )

                                    Spacer(modifier = Modifier.width(4.dp))

                                    if (currentDestination != DentalNavDestination.REPORT) {
                                        IconButton(
                                            onClick = { currentDestination = DentalNavDestination.REPORT },
                                            modifier = Modifier.testTag("top_action_report")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PictureAsPdf,
                                                contentDescription = strings.quickPdfAction,
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
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surface)
                            ) {
                                // Prominent Developer Credit Banner as requested:
                                // "تصميم وبرمجة الدكتور مالك الرميمة، رقم الهاتف 771134103"
                                DeveloperCreditBanner()

                                NavigationBar(
                                    modifier = Modifier
                                        .windowInsetsPadding(WindowInsets.navigationBars)
                                        .testTag("bottom_nav_bar"),
                                    containerColor = MaterialTheme.colorScheme.surface
                                ) {
                                    DentalNavDestination.entries.forEach { dest ->
                                        val isSelected = currentDestination == dest
                                        val label = dest.getTitle(currentLanguage)
                                        NavigationBarItem(
                                            selected = isSelected,
                                            onClick = { currentDestination = dest },
                                            icon = {
                                                Icon(
                                                    imageVector = if (isSelected) dest.selectedIcon else dest.unselectedIcon,
                                                    contentDescription = label
                                                )
                                            },
                                            label = {
                                                Text(
                                                    text = label,
                                                    fontSize = 11.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                )
                                            },
                                            modifier = Modifier.testTag(dest.testTag)
                                        )
                                    }
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
}

