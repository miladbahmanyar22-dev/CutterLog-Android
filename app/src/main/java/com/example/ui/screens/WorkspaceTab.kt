package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.PaymentEntity
import com.example.data.entity.ProjectClipEntity
import com.example.data.entity.ProjectEntity
import com.example.data.entity.ProjectRevisionEntity
import com.example.data.entity.TimerSessionEntity
import com.example.ui.components.AccordionPanel
import com.example.ui.components.ChipSelectable
import com.example.ui.components.CreateProjectDialog
import com.example.ui.components.PersianDatePickerDialog
import com.example.ui.components.ProjectReportDialog
import com.example.ui.components.QuickPriceCapsules
import com.example.ui.components.ResponsiveCapsuleGrid
import com.example.ui.components.StatusBadge
import com.example.ui.components.WeddingDateField
import com.example.ui.theme.BorderDark
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkInputBg
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.MediaAccentCyan
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber
import com.example.ui.util.PersianNumberVisualTransformation
import com.example.ui.viewmodel.MainViewModel
import com.example.util.PersianUtils

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WorkspaceTab(viewModel: MainViewModel) {
    val projects by viewModel.allProjects.collectAsState()
    val allClips by viewModel.allClips.collectAsState()
    val studios by viewModel.studios.collectAsState()
    val defaultClips by viewModel.defaultClips.collectAsState()
    val appConfig by viewModel.appConfig.collectAsState()
    val payments by viewModel.allPayments.collectAsState()
    val allSessions by viewModel.allSessions.collectAsState()
    val allRevisions by viewModel.allRevisions.collectAsState()

    val selectedProjectId by viewModel.selectedProjectId.collectAsState()
    val selectedProjectClips by viewModel.selectedProjectClips.collectAsState()
    val selectedProjectRevisions by viewModel.selectedProjectRevisions.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var showEditProjectDialog by remember { mutableStateOf(false) }
    var showPaymentDialog by remember { mutableStateOf(false) }
    var showAddRevisionDialog by remember { mutableStateOf(false) }
    var showAddStudioDialog by remember { mutableStateOf(false) }
    var statusMenuExpanded by remember { mutableStateOf(false) }
    var studioMenuExpanded by remember { mutableStateOf(false) }
    var projectToDelete by remember { mutableStateOf<ProjectEntity?>(null) }
    var selectedProjectForReport by remember { mutableStateOf<ProjectEntity?>(null) }

    val searchQuery by viewModel.workspaceSearch.collectAsState()
    val studioFilter by viewModel.workspaceStudioFilter.collectAsState()
    val statusFilter by viewModel.workspaceStatusFilter.collectAsState()

    // 1. Calculate Active Projects (Including projects in active REVISION and reopened projects)
    val allActiveProjects = projects.filter { proj ->
        proj.status != "COMPLETED"
    }

    // 2. Filter Active Projects by Search, Studio, and Progress Status
    val filteredProjects = allActiveProjects.filter { proj ->
        if (searchQuery.isBlank()) true else {
            val q = searchQuery.trim()
            val qEn = PersianUtils.convertFaToEnNum(q)
            proj.name.contains(q, ignoreCase = true) ||
                    proj.studioName.contains(q, ignoreCase = true) ||
                    (proj.weddingDate?.contains(q, ignoreCase = true) == true) ||
                    (proj.weddingDate?.let { PersianUtils.convertFaToEnNum(it).contains(qEn) } == true) ||
                    proj.projectCode.contains(qEn, ignoreCase = true) ||
                    PersianUtils.faNum(proj.projectCode).contains(q, ignoreCase = true) ||
                    (proj.projectCode.split("-").lastOrNull()?.contains(qEn) == true)
        }
    }.filter { proj ->
        if (studioFilter == null) true else proj.studioName == studioFilter
    }.filter { proj ->
        val clips = allClips.filter { it.projectId == proj.id }
        val doneCount = clips.count { it.isDone == 1 }
        val totalCount = clips.size
        val pct = if (totalCount > 0) doneCount.toFloat() / totalCount else 0f
        when (statusFilter) {
            "EDITING" -> proj.status != "REVISION" && pct < 0.8f
            "REVISION" -> proj.status == "REVISION"
            "NEAR_DONE" -> proj.status != "REVISION" && pct >= 0.8f
            else -> true
        }
    }

    // Clear selected project if it is deleted or no longer available in projects
    androidx.compose.runtime.LaunchedEffect(projects) {
        if (selectedProjectId != null && projects.none { it.id == selectedProjectId }) {
            viewModel.selectProject(null)
        }
    }

    val selectedProject = projects.find { it.id == selectedProjectId }
    val scrollState = rememberScrollState()

    // Aggregate KPI Stats for Dashboard Summary
    val completedProjectsCount = projects.count { proj ->
        proj.status == "COMPLETED"
    }

    val allActiveClips = allClips.filter { clip -> allActiveProjects.any { it.id == clip.projectId } }
    val doneActiveClipsCount = allActiveClips.count { it.isDone == 1 }
    val totalActiveClipsCount = allActiveClips.size
    val overallProgressPct = if (totalActiveClipsCount > 0) {
        doneActiveClipsCount.toFloat() / totalActiveClipsCount
    } else 0f

    val activeDebt = allActiveProjects.sumOf { p ->
        val pPaid = payments.filter { it.projectId == p.id }.sumOf { it.amount }
        (p.price - pPaid).coerceAtLeast(0.0)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        if (selectedProject == null) {
            // ==========================================
            // 1. DASHBOARD HEADER
            // ==========================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "پیشخوان",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 22.sp
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    val liveStatusText = if (allActiveProjects.isNotEmpty()) {
                        "${PersianUtils.faNum(allActiveProjects.size)} پروژه در حال انجام • ${PersianUtils.faNum(doneActiveClipsCount)} از ${PersianUtils.faNum(totalActiveClipsCount)} کلیپ نهایی شده"
                    } else {
                        "همه پروژه‌ها تحویل یا بایگانی شده‌اند"
                    }
                    Text(
                        text = liveStatusText,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Primary Action: New Project
                Button(
                    onClick = { showCreateDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ثبت پروژه جدید",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }
            }

            // ==========================================
            // 2. DASHBOARD KPI SUMMARY (خلاصه وضعیت)
            // ==========================================
        // Hero Featured Card: Active Work Status
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryPurple.copy(alpha = 0.35f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "پروژه‌های در حال تدوین",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = PersianUtils.faNum(allActiveProjects.size),
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 28.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "پروژه فعال",
                                color = TextSecondary,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(bottom = 3.dp)
                            )
                        }
                    }

                    StatusBadge(
                        text = if (allActiveProjects.isNotEmpty()) "در جریان تولید" else "آماده سفارش جدید",
                        backgroundColor = if (allActiveProjects.isNotEmpty()) PrimaryPurple else SuccessGreen
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Overall Clips Progress Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "پیشرفت کل کلیپ‌های فعال: ${PersianUtils.faNum(doneActiveClipsCount)} از ${PersianUtils.faNum(totalActiveClipsCount)}",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "${PersianUtils.faNum((overallProgressPct * 100).toInt())}٪",
                        color = MediaAccentCyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { overallProgressPct },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = MediaAccentCyan,
                    trackColor = DarkInputBg
                )
            }
        }

        // Secondary KPI Metric Grid (3 clean balanced cards)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Metric 1: Active Debt / Pending Receivables
            DashboardMiniMetricCard(
                modifier = Modifier.weight(1f),
                title = "مانده مطالبات",
                value = PersianUtils.formatCurrencyFa(activeDebt),
                subtitle = "از قراردادهای جاری",
                icon = Icons.Default.AccountBalanceWallet,
                accentColor = WarningAmber
            )

            // Metric 2: Completed Projects
            DashboardMiniMetricCard(
                modifier = Modifier.weight(1f),
                title = "پروژه‌های تحویل‌شده",
                value = "${PersianUtils.faNum(completedProjectsCount)} کار",
                subtitle = "در آرشیو و تسویه",
                icon = Icons.Default.Inventory2,
                accentColor = SuccessGreen
            )

            // Metric 3: Partner Studios
            DashboardMiniMetricCard(
                modifier = Modifier.weight(1f),
                title = "آتلیه‌های همکار",
                value = "${PersianUtils.faNum(studios.size)} استودیو",
                subtitle = "طرف‌های فعال",
                icon = Icons.Default.Business,
                accentColor = PrimaryPurple
            )
        }

        // ==========================================
        // ACTIVE PROJECTS SECTION & FILTERS
        // ==========================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "پروژه‌های در حال تدوین",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                StatusBadge(
                    text = "${PersianUtils.faNum(filteredProjects.size)} پروژه",
                    backgroundColor = DarkSurface
                )
            }

            // Add studio shortcut
            Button(
                onClick = { showAddStudioDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("افزودن آتلیه", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.workspaceSearch.value = it },
            placeholder = { Text("جستجوی عنوان پروژه، آتلیه یا تاریخ عروسی...", color = TextMuted, fontSize = 12.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp)) },
            trailingIcon = {
                if (searchQuery.isNotBlank()) {
                    IconButton(onClick = { viewModel.workspaceSearch.value = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "پاک کردن", tint = TextMuted, modifier = Modifier.size(16.dp))
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = DarkInputBg,
                unfocusedContainerColor = DarkInputBg,
                focusedBorderColor = PrimaryPurple,
                unfocusedBorderColor = BorderDark,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            singleLine = true
        )

        // ==========================================
        // 3. ELEGANT SIDE-BY-SIDE FILTER CAPSULES WITH POPUPS
        // ==========================================
        val isStatusFilterActive = statusFilter != "ALL"
        val isStudioFilterActive = studioFilter != null
        val isAnyFilterActive = isStatusFilterActive || isStudioFilterActive || searchQuery.isNotBlank()

        val currentStatusLabel = when (statusFilter) {
            "EDITING" -> "در حال تدوین"
            "REVISION" -> "اصلاحات"
            "NEAR_DONE" -> "آماده تحویل"
            else -> "همه وضعیت‌ها"
        }
        val currentStudioLabel = if (studioFilter != null) PersianUtils.formatStudioName(studioFilter!!) else "همه آتلیه‌ها"

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = if (isAnyFilterActive) 6.dp else 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Capsule 1: Status Filter Popup
            Box(modifier = Modifier.weight(1f)) {
                Surface(
                    onClick = { statusMenuExpanded = true },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isStatusFilterActive) PrimaryPurple.copy(alpha = 0.15f) else DarkInputBg,
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isStatusFilterActive) PrimaryPurple.copy(alpha = 0.6f) else BorderDark
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = null,
                                tint = if (isStatusFilterActive) PrimaryPurple else TextSecondary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = currentStatusLabel,
                                color = if (isStatusFilterActive) PrimaryPurple else TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = if (isStatusFilterActive) FontWeight.Bold else FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Icon(
                            imageVector = if (statusMenuExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = if (isStatusFilterActive) PrimaryPurple else TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                DropdownMenu(
                    expanded = statusMenuExpanded,
                    onDismissRequest = { statusMenuExpanded = false },
                    modifier = Modifier.background(DarkCard)
                ) {
                    val statusOptions = listOf(
                        "ALL" to "همه وضعیت‌ها",
                        "EDITING" to "در حال تدوین",
                        "REVISION" to "اصلاحات",
                        "NEAR_DONE" to "آماده تحویل"
                    )
                    statusOptions.forEach { (key, label) ->
                        val isSelected = statusFilter == key
                        DropdownMenuItem(
                            text = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = label,
                                        color = if (isSelected) PrimaryPurple else TextPrimary,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 12.5.sp
                                    )
                                    if (isSelected) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = PrimaryPurple,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            },
                            onClick = {
                                viewModel.workspaceStatusFilter.value = key
                                statusMenuExpanded = false
                            }
                        )
                    }
                }
            }

            // Capsule 2: Studio Filter Popup
            Box(modifier = Modifier.weight(1f)) {
                Surface(
                    onClick = { studioMenuExpanded = true },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isStudioFilterActive) PrimaryPurple.copy(alpha = 0.15f) else DarkInputBg,
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isStudioFilterActive) PrimaryPurple.copy(alpha = 0.6f) else BorderDark
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Store,
                                contentDescription = null,
                                tint = if (isStudioFilterActive) PrimaryPurple else TextSecondary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = currentStudioLabel,
                                color = if (isStudioFilterActive) PrimaryPurple else TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = if (isStudioFilterActive) FontWeight.Bold else FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Icon(
                            imageVector = if (studioMenuExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = if (isStudioFilterActive) PrimaryPurple else TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                DropdownMenu(
                    expanded = studioMenuExpanded,
                    onDismissRequest = { studioMenuExpanded = false },
                    modifier = Modifier.background(DarkCard)
                ) {
                    DropdownMenuItem(
                        text = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "همه آتلیه‌ها",
                                    color = if (studioFilter == null) PrimaryPurple else TextPrimary,
                                    fontWeight = if (studioFilter == null) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 12.5.sp
                                )
                                if (studioFilter == null) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = PrimaryPurple,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        },
                        onClick = {
                            viewModel.workspaceStudioFilter.value = null
                            studioMenuExpanded = false
                        }
                    )
                    studios.forEach { studio ->
                        val isSelected = studioFilter == studio.name
                        DropdownMenuItem(
                            text = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = PersianUtils.formatStudioName(studio.name),
                                        color = if (isSelected) PrimaryPurple else TextPrimary,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 12.5.sp
                                    )
                                    if (isSelected) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = PrimaryPurple,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            },
                            onClick = {
                                viewModel.workspaceStudioFilter.value = studio.name
                                studioMenuExpanded = false
                            }
                        )
                    }
                }
            }
        }

        // Quick Reset Row (Shown only when any filter is active)
        if (isAnyFilterActive) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Surface(
                    onClick = {
                        viewModel.workspaceSearch.value = ""
                        viewModel.workspaceStatusFilter.value = "ALL"
                        viewModel.workspaceStudioFilter.value = null
                    },
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Transparent
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "پاک کردن فیلترها",
                            tint = WarningAmber,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "حذف فیلترها",
                            color = WarningAmber,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Project Cards List
        if (filteredProjects.isEmpty()) {
            // Clean Empty State
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(DarkSurface),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "هنوز پروژه‌ای برای نمایش وجود ندارد",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (searchQuery.isNotBlank() || studioFilter != null) {
                                "با تغییر فیلتر یا عبارت جستجو مجدداً بررسی کنید"
                            } else {
                                "جهت شروع کار تدوین، اولین پروژه را ثبت کنید"
                            },
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                        if (searchQuery.isBlank() && studioFilter == null) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = { showCreateDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("ثبت اولین پروژه", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                filteredProjects.forEach { project ->
                    val clips = allClips.filter { it.projectId == project.id }
                    val doneClips = clips.count { it.isDone == 1 }
                    val totalClips = clips.size
                    val progressPct = if (totalClips > 0) (doneClips.toFloat() / totalClips) else 0f
                    val pPaid = payments.filter { it.projectId == project.id }.sumOf { it.amount }

                    ProjectRowCard(
                        project = project,
                        doneClips = doneClips,
                        totalClips = totalClips,
                        progressPct = progressPct,
                        paidAmount = pPaid,
                        isSelected = false,
                        onClick = { viewModel.selectProject(project.id) },
                        onDelete = { projectToDelete = project }
                    )
                }
            }
        }
        } // End of Dashboard content
        else {
            // ==========================================
            // PROJECT DETAIL & CLIPS MANAGEMENT VIEW (لایه ۲ و ۳)
            // ==========================================
            ProjectDetailView(
                project = selectedProject,
                clips = selectedProjectClips,
                revisions = selectedProjectRevisions,
                payments = payments.filter { it.projectId == selectedProject.id },
                sessions = allSessions.filter { it.projectId == selectedProject.id },
                paidAmount = payments.filter { it.projectId == selectedProject.id }.sumOf { it.amount },
                onBack = { viewModel.selectProject(null) },
                onEditProject = { showEditProjectDialog = true },
                onDeleteProject = { projectToDelete = selectedProject },
                onRecordPayment = { showPaymentDialog = true },
                onAddRevision = { showAddRevisionDialog = true },
                onToggleClip = { clip -> viewModel.toggleClipDone(clip) },
                onStartTimerForClip = { clip ->
                    viewModel.setTimerTarget(selectedProject.id, clip.clipName)
                },
                onToggleRevision = { rev -> viewModel.toggleRevisionApplied(rev) },
                onDeleteRevision = { rev -> viewModel.deleteRevision(rev) },
                onMarkRoundComplete = { phase -> viewModel.markRoundComplete(selectedProject.id, phase) },
                onViewReport = { selectedProjectForReport = selectedProject }
            )
        }
    }

    // ==========================================
    // 6. DIALOGS (حفظ کامل تمام دیالوگ‌ها و منطق)
    // ==========================================

    // CREATE PROJECT DIALOG
    if (showCreateDialog) {
        CreateProjectDialog(
            studios = studios.map { it.name },
            defaultClips = defaultClips.map { it.name },
            defaultDeadlineDays = appConfig?.defaultDeadlineDays ?: 7,
            quickPricesJson = appConfig?.quickPricesJson ?: "[500000, 1000000, 2000000, 3000000, 5000000, 10000000]",
            packagesJson = appConfig?.packagesJson ?: "[]",
            onDismiss = { showCreateDialog = false },
            onCreate = { name, studio, price, selectedClips, deadlineDays, weddingDate ->
                viewModel.createProject(name, studio, price, selectedClips, deadlineDays, weddingDate)
                showCreateDialog = false
            }
        )
    }

    // EDIT PROJECT DIALOG
    if (showEditProjectDialog && selectedProject != null) {
        EditProjectDialog(
            project = selectedProject,
            studios = studios.map { it.name },
            onDismiss = { showEditProjectDialog = false },
            onSave = { updatedProject ->
                viewModel.updateProject(updatedProject)
                showEditProjectDialog = false
            }
        )
    }

    // ADD STUDIO DIALOG
    if (showAddStudioDialog) {
        AddStudioDialog(
            onDismiss = { showAddStudioDialog = false },
            onAdd = { studioName ->
                viewModel.addStudio(studioName)
                showAddStudioDialog = false
            }
        )
    }

    // RECORD PAYMENT DIALOG
    if (showPaymentDialog && selectedProject != null) {
        RecordPaymentDialog(
            projectName = selectedProject.name,
            onDismiss = { showPaymentDialog = false },
            onSubmit = { amount, note ->
                viewModel.addPayment(selectedProject.id, amount, note)
                showPaymentDialog = false
            }
        )
    }

    // ADD REVISION DIALOG
    if (showAddRevisionDialog && selectedProject != null) {
        AddRevisionDialog(
            onDismiss = { showAddRevisionDialog = false },
            onSubmit = { desc, phase ->
                viewModel.addRevision(selectedProject.id, desc, phase)
                showAddRevisionDialog = false
            }
        )
    }

    // DELETE PROJECT CONFIRMATION DIALOG (UX Safety)
    if (projectToDelete != null) {
        AlertDialog(
            onDismissRequest = { projectToDelete = null },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier.fillMaxWidth(0.80f),
            title = { Text("حذف پروژه", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Text(
                    text = "آیا از حذف پروژه «${projectToDelete?.name}» اطمینان دارید؟ کلیپ‌ها و اصلاحات مرتبط با آن نیز حذف خواهند شد.",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        projectToDelete?.let { viewModel.deleteProject(it) }
                        projectToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("حذف نهایی", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { projectToDelete = null }) {
                    Text("انصراف", color = TextMuted)
                }
            },
            containerColor = DarkCard
        )
    }

    // PROJECT REPORT MODAL DIALOG
    if (selectedProjectForReport != null) {
        val proj = selectedProjectForReport!!
        val projClips = allClips.filter { it.projectId == proj.id }
        val projPayments = payments.filter { it.projectId == proj.id }
        val projSessions = allSessions.filter { it.projectId == proj.id }
        val projRevisions = allRevisions.filter { it.projectId == proj.id }

        ProjectReportDialog(
            project = proj,
            clips = projClips,
            payments = projPayments,
            sessions = projSessions,
            revisions = projRevisions,
            onDismiss = { selectedProjectForReport = null }
        )
    }
}

// ==========================================
// SUB-COMPONENTS FOR DASHBOARD
// ==========================================

@Composable
fun DashboardMiniMetricCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = TextSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = value,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                color = TextMuted,
                fontSize = 9.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun QuickNavigationItem(
    modifier: Modifier = Modifier,
    title: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(12.dp),
        color = DarkCard,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProjectRowCard(
    project: ProjectEntity,
    doneClips: Int,
    totalClips: Int,
    progressPct: Float,
    paidAmount: Double,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val remainingDebt = (project.price - paidAmount).coerceAtLeast(0.0)
    val isSettled = project.isSettled == 1 || remainingDebt <= 0.0

    val (statusLabel, statusColor) = when {
        project.status == "REVISION" -> Pair("اصلاحات درخواستی", WarningAmber)
        project.status == "COMPLETED" -> Pair("تحویل نهایی", SuccessGreen)
        totalClips == 0 -> Pair("بدون کلیپ", TextMuted)
        progressPct >= 0.8f -> Pair("در آستانه تحویل", SuccessGreen)
        progressPct >= 0.3f -> Pair("اصلاح رنگ و رافکات", MediaAccentCyan)
        else -> Pair("تدوین اولیه (شروع)", WarningAmber)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        border = BorderStroke(1.dp, BorderDark.copy(alpha = 0.9f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ==========================================
            // ۱. نوار شناسه و وضعیت (تفکیک متقارن بالا)
            // ==========================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // شناسه یکتای پروژه با نشان اختصاصی
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(PrimaryPurple.copy(alpha = 0.12f))
                            .border(0.8.dp, PrimaryPurple.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = null,
                            tint = PrimaryPurple,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    if (project.projectCode.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = PrimaryPurple.copy(alpha = 0.12f),
                            border = BorderStroke(0.8.dp, PrimaryPurple.copy(alpha = 0.35f))
                        ) {
                            Text(
                                text = PersianUtils.faNum(project.projectCode),
                                color = PrimaryPurple,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.5.dp)
                            )
                        }
                    } else {
                        Text(
                            text = "پروژه #${PersianUtils.faNum(project.id)}",
                            color = TextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // برچسب وضعیت پروژه
                StatusBadge(text = statusLabel, backgroundColor = statusColor)
            }

            // ==========================================
            // ۲. نام پروژه (عروس و داماد) - با فضای کامل و بدون شکستگی یا برش
            // ==========================================
            Text(
                text = project.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                fontSize = 16.sp,
                lineHeight = 23.sp
            )

            // ==========================================
            // ۳. مشخصات آتلیه و تاریخ مراسم (منعطف و کامل)
            // ==========================================
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // نام کامل آتلیه / کارفرما
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 1.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Store,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = PersianUtils.formatStudioName(project.studioName),
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // تاریخ مراسم (در صورت ثبت)
                if (!project.weddingDate.isNullOrBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 1.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = PrimaryPurple,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "مراسم: ${PersianUtils.faNum(project.weddingDate)}",
                            color = PrimaryPurple,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // ==========================================
            // ۴. نوار موعد تحویل (ددلاین) در صورت وجود
            // ==========================================
            if (!project.deadlineDate.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = DarkInputBg,
                    border = BorderStroke(0.7.dp, BorderDark.copy(alpha = 0.7f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Event,
                                contentDescription = null,
                                tint = PrimaryPurple,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "موعد تحویل پروژه (ددلاین):",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                        Text(
                            text = PersianUtils.faNum(project.deadlineDate),
                            color = TextPrimary,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // ==========================================
            // ۵. شاخص‌های مالی استاندارد (۳ ستون منظم)
            // ==========================================
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = DarkInputBg,
                border = BorderStroke(0.8.dp, BorderDark.copy(alpha = 0.8f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // ستون ۱: مبلغ کل قرارداد
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "مبلغ قرارداد",
                            color = TextMuted,
                            fontSize = 9.5.sp,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = PersianUtils.formatCurrencyFa(project.price),
                            color = TextPrimary,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // خط جداکننده ستون ۱ و ۲
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(24.dp)
                            .background(BorderDark.copy(alpha = 0.7f))
                    )

                    // ستون ۲: دریافتی کل
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "دریافتی کل",
                            color = TextMuted,
                            fontSize = 9.5.sp,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = PersianUtils.formatCurrencyFa(paidAmount),
                            color = SuccessGreen,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // خط جداکننده ستون ۲ و ۳
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(24.dp)
                            .background(BorderDark.copy(alpha = 0.7f))
                    )

                    // ستون ۳: وضعیت مانده یا تسویه
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (isSettled) "وضعیت حساب" else "مانده بدهی",
                            color = TextMuted,
                            fontSize = 9.5.sp,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isSettled) "تسویه شده" else PersianUtils.formatCurrencyFa(remainingDebt),
                            color = if (isSettled) SuccessGreen else WarningAmber,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // ==========================================
            // ۴. نوار وضعیت پیشرفت تدوین کلیپ‌ها
            // ==========================================
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Movie,
                            contentDescription = null,
                            tint = MediaAccentCyan,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "پیشرفت تدوین کلیپ‌ها",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1
                        )
                    }

                    Text(
                        text = "${PersianUtils.faNum(doneClips)} از ${PersianUtils.faNum(totalClips)} کلیپ • ${PersianUtils.faNum((progressPct * 100).toInt())}٪",
                        color = if (progressPct >= 0.8f) SuccessGreen else PrimaryPurple,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }

                LinearProgressIndicator(
                    progress = { progressPct },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (progressPct >= 0.8f) SuccessGreen else PrimaryPurple,
                    trackColor = DarkSurface
                )
            }

            // ==========================================
            // ۵. نوار عملیات پایین کارت (دکمه ورود و دکمه حذف)
            // ==========================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onClick,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, PrimaryPurple.copy(alpha = 0.4f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = PrimaryPurple.copy(alpha = 0.10f),
                        contentColor = PrimaryPurple
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "ورود به پرونده و کلیپ‌ها",
                        color = PrimaryPurple,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = null,
                        tint = PrimaryPurple,
                        modifier = Modifier.size(14.dp)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkSurface)
                        .border(0.8.dp, BorderDark, RoundedCornerShape(8.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "حذف پروژه",
                        tint = ErrorRed.copy(alpha = 0.8f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ProjectDetailView(
    project: ProjectEntity,
    clips: List<ProjectClipEntity>,
    revisions: List<ProjectRevisionEntity>,
    payments: List<PaymentEntity> = emptyList(),
    sessions: List<TimerSessionEntity> = emptyList(),
    paidAmount: Double,
    onBack: () -> Unit,
    onEditProject: () -> Unit,
    onDeleteProject: () -> Unit,
    onRecordPayment: () -> Unit,
    onAddRevision: () -> Unit,
    onToggleClip: (ProjectClipEntity) -> Unit,
    onStartTimerForClip: (ProjectClipEntity) -> Unit = {},
    onToggleRevision: (ProjectRevisionEntity) -> Unit,
    onDeleteRevision: (ProjectRevisionEntity) -> Unit = {},
    onMarkRoundComplete: (Int) -> Unit = {},
    onViewReport: () -> Unit = {}
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val remainingDebt = (project.price - paidAmount).coerceAtLeast(0.0)
    val isSettled = project.isSettled == 1 || remainingDebt <= 0.0
    val doneCount = clips.count { it.isDone == 1 }
    val totalCount = clips.size
    val remainingClips = (totalCount - doneCount).coerceAtLeast(0)
    val progressPct = if (totalCount > 0) (doneCount.toFloat() / totalCount).coerceIn(0f, 1f) else 0f

    val unappliedRevs = revisions.count { it.isApplied == 0 }
    val latestPhase = revisions.maxOfOrNull { it.phaseNum } ?: 1

    val totalLoggedSeconds = sessions.sumOf { it.durationSeconds }
    val totalLoggedHours = (totalLoggedSeconds / 3600).toInt()
    val totalLoggedMins = ((totalLoggedSeconds % 3600) / 60).toInt()

    var clipFilter by remember { mutableStateOf("ALL") } // "ALL", "PENDING", "DONE"
    val filteredClips = clips.filter { clip ->
        when (clipFilter) {
            "PENDING" -> clip.isDone == 0
            "DONE" -> clip.isDone == 1
            else -> true
        }
    }

    var showPaymentsHistory by remember { mutableStateOf(false) }
    var secondaryInfoExpanded by remember { mutableStateOf(false) }

    val (statusText, statusColor) = when {
        project.status == "COMPLETED" -> Pair("تکمیل شده", SuccessGreen)
        unappliedRevs > 0 || project.status == "REVISION" -> Pair("در انتظار اصلاحات", WarningAmber)
        progressPct >= 1f && totalCount > 0 -> Pair("آماده تحویل", SuccessGreen)
        progressPct >= 0.7f -> Pair("در آستانه تحویل", MediaAccentCyan)
        progressPct > 0f -> Pair("در حال تدوین", PrimaryPurple)
        else -> Pair("تدوین اولیه", WarningAmber)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ==========================================
        // ۱. HEADER ساده و تمیز پروژه
        // ==========================================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(DarkCard)
                    .clickable { onBack() }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "بازگشت به پیشخوان",
                    tint = PrimaryPurple,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "بازگشت به پیشخوان پروژه‌ها",
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedButton(
                    onClick = onEditProject,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, PrimaryPurple.copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = DarkCard, contentColor = PrimaryPurple),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "ویرایش پروژه",
                        tint = PrimaryPurple,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("ویرایش پروژه", color = PrimaryPurple, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                }

                IconButton(
                    onClick = onDeleteProject,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkCard)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "حذف پروژه",
                        tint = ErrorRed.copy(alpha = 0.85f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Project Identity Banner (Header ساده، خلوت و خوانا با شماره دائمی پروژه)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            border = BorderStroke(1.dp, BorderDark)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = project.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 17.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = PersianUtils.formatStudioName(project.studioName),
                                color = TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(text = "•", color = TextMuted, fontSize = 11.sp)
                            if (!project.weddingDate.isNullOrBlank()) {
                                Text(
                                    text = "مراسم: ${PersianUtils.faNum(project.weddingDate)}",
                                    color = PrimaryPurple,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            } else {
                                Text(
                                    text = "ثبت: ${PersianUtils.faNum(project.createdAt)}",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    // Single clean Status Badge
                    StatusBadge(text = statusText, backgroundColor = statusColor)
                }

                // Project ID Permanent Chip with Copy Action
                if (project.projectCode.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = PrimaryPurple.copy(alpha = 0.08f),
                        border = BorderStroke(0.9.dp, PrimaryPurple.copy(alpha = 0.25f)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                clipboardManager.setText(AnnotatedString(project.projectCode))
                                Toast.makeText(context, "شماره پروژه (${project.projectCode}) کپی شد", Toast.LENGTH_SHORT).show()
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "شماره یکتای پروژه:",
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = PersianUtils.faNum(project.projectCode),
                                    color = PrimaryPurple,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "کپی شماره پروژه",
                                    tint = PrimaryPurple,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "کپی",
                                    color = PrimaryPurple,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // ۲. بخش «وضعیت پروژه» (یکپارچه و کامپکت)
        // ==========================================
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            border = BorderStroke(1.dp, BorderDark)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Progress Bar & Percentage
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "پیشرفت کلی تدوین",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${PersianUtils.faNum((progressPct * 100).toInt())}٪",
                            color = if (progressPct >= 1f) SuccessGreen else PrimaryPurple,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    LinearProgressIndicator(
                        progress = { progressPct },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (progressPct >= 1f) SuccessGreen else PrimaryPurple,
                        trackColor = DarkInputBg
                    )
                }

                // 4 Compact KPI Indicators
                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val isSmall = maxWidth < 480.dp
                    if (isSmall) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CompactStatusPill(
                                    modifier = Modifier.weight(1f),
                                    label = "کلیپ‌ها",
                                    value = "${PersianUtils.faNum(doneCount)} / ${PersianUtils.faNum(totalCount)}",
                                    subValue = if (remainingClips == 0) "تکمیل ۱۰۰٪" else "${PersianUtils.faNum(remainingClips)} مانده",
                                    valueColor = if (remainingClips == 0) SuccessGreen else TextPrimary
                                )
                                CompactStatusPill(
                                    modifier = Modifier.weight(1f),
                                    label = "اصلاحات",
                                    value = if (revisions.isEmpty()) "بدون اصلاحیه" else "${PersianUtils.faNum(unappliedRevs)} باز",
                                    subValue = if (revisions.isEmpty()) "ثبت نشده" else "دور ${PersianUtils.faNum(latestPhase)}",
                                    valueColor = if (unappliedRevs > 0) WarningAmber else SuccessGreen
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CompactStatusPill(
                                    modifier = Modifier.weight(1f),
                                    label = "وضعیت مالی",
                                    value = if (isSettled) "تسویه کامل" else PersianUtils.formatCurrencyFa(remainingDebt),
                                    subValue = "کل: ${PersianUtils.formatCurrencyFa(project.price)}",
                                    valueColor = if (isSettled) SuccessGreen else WarningAmber
                                )
                                CompactStatusPill(
                                    modifier = Modifier.weight(1f),
                                    label = "وضعیت تحویل",
                                    value = when {
                                        project.status == "COMPLETED" -> "تکمیل شده"
                                        progressPct >= 1f && unappliedRevs == 0 -> "آماده تحویل"
                                        unappliedRevs > 0 -> "مرحله اصلاحات"
                                        else -> "در جریان تدوین"
                                    },
                                    subValue = "#${PersianUtils.faNum(project.id)}",
                                    valueColor = if (project.status == "COMPLETED" || (progressPct >= 1f && unappliedRevs == 0)) SuccessGreen else MediaAccentCyan
                                )
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CompactStatusPill(
                                modifier = Modifier.weight(1f),
                                label = "کلیپ‌ها",
                                value = "${PersianUtils.faNum(doneCount)} / ${PersianUtils.faNum(totalCount)}",
                                subValue = if (remainingClips == 0) "تکمیل ۱۰۰٪" else "${PersianUtils.faNum(remainingClips)} مانده",
                                valueColor = if (remainingClips == 0) SuccessGreen else TextPrimary
                            )
                            CompactStatusPill(
                                modifier = Modifier.weight(1f),
                                label = "اصلاحات",
                                value = if (revisions.isEmpty()) "بدون اصلاحیه" else "${PersianUtils.faNum(unappliedRevs)} باز",
                                subValue = if (revisions.isEmpty()) "ثبت نشده" else "دور ${PersianUtils.faNum(latestPhase)}",
                                valueColor = if (unappliedRevs > 0) WarningAmber else SuccessGreen
                            )
                            CompactStatusPill(
                                modifier = Modifier.weight(1f),
                                label = "وضعیت مالی",
                                value = if (isSettled) "تسویه کامل" else PersianUtils.formatCurrencyFa(remainingDebt),
                                subValue = "کل: ${PersianUtils.formatCurrencyFa(project.price)}",
                                valueColor = if (isSettled) SuccessGreen else WarningAmber
                            )
                            CompactStatusPill(
                                modifier = Modifier.weight(1f),
                                label = "وضعیت تحویل",
                                value = when {
                                    project.status == "COMPLETED" -> "تکمیل شده"
                                    progressPct >= 1f && unappliedRevs == 0 -> "آماده تحویل"
                                    unappliedRevs > 0 -> "مرحله اصلاحات"
                                    else -> "در جریان تدوین"
                                },
                                subValue = "#${PersianUtils.faNum(project.id)}",
                                valueColor = if (project.status == "COMPLETED" || (progressPct >= 1f && unappliedRevs == 0)) SuccessGreen else MediaAccentCyan
                            )
                        }
                    }
                }
            }
        }

        // ==========================================
        // ۳. بخش بسیار مهم «قدم بعدی»
        // ==========================================
        ProjectNextActionSection(
            remainingClips = remainingClips,
            unappliedRevs = unappliedRevs,
            latestPhase = latestPhase,
            isSettled = isSettled,
            remainingDebt = remainingDebt,
            isCompleted = project.status == "COMPLETED",
            onActionClips = { clipFilter = if (remainingClips > 0) "PENDING" else "ALL" },
            onActionRevisions = onAddRevision,
            onActionPayment = onRecordPayment,
            onActionReport = onViewReport
        )

        // ==========================================
        // ۴. گروه‌بندی قابلیت‌ها (۴ گروه اصلی)
        // ==========================================

        // گروه ۱: تدوین و کلیپ‌ها
        ProjectEditingGroupCard(
            clips = clips,
            filteredClips = filteredClips,
            clipFilter = clipFilter,
            onFilterChange = { clipFilter = it },
            doneCount = doneCount,
            totalCount = totalCount,
            totalLoggedHours = totalLoggedHours,
            totalLoggedMins = totalLoggedMins,
            onToggleClip = onToggleClip,
            onStartTimerForClip = onStartTimerForClip
        )

        // گروه ۲: اصلاحات (Revisions)
        ProjectRevisionsGroupCard(
            revisions = revisions,
            unappliedRevs = unappliedRevs,
            latestPhase = latestPhase,
            onAddRevision = onAddRevision,
            onToggleRevision = onToggleRevision,
            onDeleteRevision = onDeleteRevision,
            onMarkRoundComplete = onMarkRoundComplete
        )

        // گروه ۳: مالی و پرداخت‌ها
        ProjectFinanceGroupCard(
            price = project.price,
            paidAmount = paidAmount,
            remainingDebt = remainingDebt,
            isSettled = isSettled,
            payments = payments,
            showPaymentsHistory = showPaymentsHistory,
            onTogglePaymentsHistory = { showPaymentsHistory = !showPaymentsHistory },
            onRecordPayment = onRecordPayment
        )

        // گروه ۴: تحویل و پروژه
        ProjectDeliveryGroupCard(
            projectId = project.id,
            isCompleted = project.status == "COMPLETED",
            isReady = progressPct >= 1f && unappliedRevs == 0,
            remainingClips = remainingClips,
            onViewReport = onViewReport
        )

        // ==========================================
        // ۵. اطلاعات ثانویه (بخش Collapsible)
        // ==========================================
        ProjectSecondaryInfoCard(
            project = project,
            isExpanded = secondaryInfoExpanded,
            onToggleExpand = { secondaryInfoExpanded = !secondaryInfoExpanded },
            onEditProject = onEditProject,
            onDeleteProject = onDeleteProject
        )
    }
}

// ==========================================
// SUB-COMPONENTS FOR REDESIGNED PROJECT DETAIL
// ==========================================

private data class NextActionData(
    val title: String,
    val description: String,
    val buttonText: String,
    val buttonColor: Color,
    val buttonIcon: ImageVector,
    val onAction: () -> Unit
)

@Composable
private fun ProjectNextActionSection(
    remainingClips: Int,
    unappliedRevs: Int,
    latestPhase: Int,
    isSettled: Boolean,
    remainingDebt: Double,
    isCompleted: Boolean,
    onActionClips: () -> Unit,
    onActionRevisions: () -> Unit,
    onActionPayment: () -> Unit,
    onActionReport: () -> Unit
) {
    val actionData = when {
        remainingClips > 0 -> {
            NextActionData(
                title = "${PersianUtils.faNum(remainingClips)} کلیپ باقی مانده برای تدوین",
                description = "برای پیشبرد کار، کلیپ‌های در دست اقدام را تدوین و تکمیل نمایید.",
                buttonText = "مشاهده و مدیریت کلیپ‌ها",
                buttonColor = PrimaryPurple,
                buttonIcon = Icons.Default.Movie,
                onAction = onActionClips
            )
        }
        unappliedRevs > 0 -> {
            NextActionData(
                title = "${PersianUtils.faNum(unappliedRevs)} اصلاحیه در انتظار اعمال",
                description = "اصلاحات درخواست‌شده در دور ${PersianUtils.faNum(latestPhase)} را روی پروژه اعمال کنید.",
                buttonText = "بررسی و اعمال اصلاحات",
                buttonColor = WarningAmber,
                buttonIcon = Icons.Default.Edit,
                onAction = onActionRevisions
            )
        }
        !isSettled && remainingDebt > 0.0 -> {
            NextActionData(
                title = "کلیپ‌ها تکمیل شدند؛ بررسی مانده مالی",
                description = "مبلغ باقیمانده ${PersianUtils.formatCurrencyFa(remainingDebt)} در انتظار تسویه نهایی است.",
                buttonText = "ثبت دریافتی و تسویه",
                buttonColor = SuccessGreen,
                buttonIcon = Icons.Default.Payment,
                onAction = onActionPayment
            )
        }
        !isCompleted -> {
            NextActionData(
                title = "پروژه آماده تحویل است",
                description = "کلیپ‌ها نهایی و حساب تسویه شده است؛ پرونده آماده صدور گزارش رسمی و تحویل می‌باشد.",
                buttonText = "مشاهده گزارش و تحویل",
                buttonColor = SuccessGreen,
                buttonIcon = Icons.Default.CheckCircle,
                onAction = onActionReport
            )
        }
        else -> {
            NextActionData(
                title = "پروژه تکمیل شده است",
                description = "کلیه مراحل تدوین، اصلاحات و تسویه مالی با موفقیت به پایان رسیده است.",
                buttonText = "مشاهده گزارش رسمی پرونده",
                buttonColor = PrimaryPurple,
                buttonIcon = Icons.Default.Article,
                onAction = onActionReport
            )
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        border = BorderStroke(1.dp, actionData.buttonColor.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(actionData.buttonColor)
                    )
                    Text(
                        text = "قدم بعدی",
                        color = actionData.buttonColor,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = actionData.title,
                    color = TextPrimary,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = actionData.description,
                    color = TextSecondary,
                    fontSize = 11.5.sp,
                    lineHeight = 18.sp
                )
            }

            Button(
                onClick = actionData.onAction,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                colors = ButtonDefaults.buttonColors(containerColor = actionData.buttonColor),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = actionData.buttonIcon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(17.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = actionData.buttonText,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.5.sp
                )
            }
        }
    }
}

@Composable
private fun CompactStatusPill(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    subValue: String,
    valueColor: Color = TextPrimary
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = DarkInputBg,
        border = BorderStroke(1.dp, BorderDark.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = label,
                color = TextMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                color = valueColor,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subValue,
                color = TextSecondary,
                fontSize = 9.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ProjectEditingGroupCard(
    clips: List<ProjectClipEntity>,
    filteredClips: List<ProjectClipEntity>,
    clipFilter: String,
    onFilterChange: (String) -> Unit,
    doneCount: Int,
    totalCount: Int,
    totalLoggedHours: Int,
    totalLoggedMins: Int,
    onToggleClip: (ProjectClipEntity) -> Unit,
    onStartTimerForClip: (ProjectClipEntity) -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        border = BorderStroke(1.dp, BorderDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Group Header (Icon + Title + Short Status)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(PrimaryPurple.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Movie,
                            contentDescription = null,
                            tint = PrimaryPurple,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "تدوین و کلیپ‌ها",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 14.5.sp
                        )
                        val timeStr = if (totalLoggedHours > 0 || totalLoggedMins > 0) {
                            " • زمان کار: ${PersianUtils.faNum(totalLoggedHours)}:${if (totalLoggedMins < 10) "۰" else ""}${PersianUtils.faNum(totalLoggedMins)}"
                        } else ""
                        Text(
                            text = "${PersianUtils.faNum(doneCount)} از ${PersianUtils.faNum(totalCount)} کلیپ نهایی شده$timeStr",
                            color = TextSecondary,
                            fontSize = 11.5.sp
                        )
                    }
                }
            }

            // Filter Tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ChipSelectable(
                    text = "همه (${PersianUtils.faNum(clips.size)})",
                    isSelected = clipFilter == "ALL",
                    onSelect = { onFilterChange("ALL") }
                )
                ChipSelectable(
                    text = "در جریان (${PersianUtils.faNum(clips.count { it.isDone == 0 })})",
                    isSelected = clipFilter == "PENDING",
                    onSelect = { onFilterChange("PENDING") }
                )
                ChipSelectable(
                    text = "تکمیل (${PersianUtils.faNum(clips.count { it.isDone == 1 })})",
                    isSelected = clipFilter == "DONE",
                    onSelect = { onFilterChange("DONE") }
                )
            }

            // Clips List
            if (filteredClips.isEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = DarkInputBg
                ) {
                    Text(
                        text = if (clips.isEmpty()) "هیچ کلیپی برای این پروژه تعریف نشده است." else "کلیپی در این فیلتر یافت نشد.",
                        color = TextMuted,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    filteredClips.forEach { clip ->
                        ClipDetailCard(
                            clip = clip,
                            onToggle = { onToggleClip(clip) },
                            onStartTimer = { onStartTimerForClip(clip) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProjectRevisionsGroupCard(
    revisions: List<ProjectRevisionEntity>,
    unappliedRevs: Int,
    latestPhase: Int,
    onAddRevision: () -> Unit,
    onToggleRevision: (ProjectRevisionEntity) -> Unit,
    onDeleteRevision: (ProjectRevisionEntity) -> Unit,
    onMarkRoundComplete: (Int) -> Unit
) {
    val revisionsByPhase = revisions.groupBy { it.phaseNum }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        border = BorderStroke(1.dp, BorderDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Group Header (Icon + Title + Short Status + Action)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(WarningAmber.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = WarningAmber,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "اصلاحات و تغییرات (Revisions)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 14.5.sp
                        )
                        Text(
                            text = if (revisions.isEmpty()) "بدون اصلاحیه باز" else "دور ${PersianUtils.faNum(latestPhase)} • ${PersianUtils.faNum(unappliedRevs)} مورد باز",
                            color = TextSecondary,
                            fontSize = 11.5.sp
                        )
                    }
                }

                Button(
                    onClick = onAddRevision,
                    colors = ButtonDefaults.buttonColors(containerColor = WarningAmber.copy(alpha = 0.18f)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = WarningAmber,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "ثبت اصلاحیه",
                        color = WarningAmber,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Revisions Content
            if (revisions.isEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = DarkInputBg
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "هیچ اصلاحیه‌ای برای این پروژه ثبت نشده است.",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    revisionsByPhase.keys.sortedDescending().forEach { phaseNum ->
                        val phaseRevs = revisionsByPhase[phaseNum] ?: emptyList()
                        val appliedCount = phaseRevs.count { it.isApplied == 1 }
                        val isAllApplied = appliedCount == phaseRevs.size

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkInputBg),
                            border = BorderStroke(1.dp, BorderDark.copy(alpha = 0.5f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "دور ${PersianUtils.faNum(phaseNum)}",
                                            fontWeight = FontWeight.Bold,
                                            color = WarningAmber,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = "(${PersianUtils.faNum(appliedCount)} از ${PersianUtils.faNum(phaseRevs.size)} اعمال شد)",
                                            color = TextSecondary,
                                            fontSize = 11.sp
                                        )
                                    }

                                    if (!isAllApplied) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = SuccessGreen.copy(alpha = 0.15f),
                                            modifier = Modifier.clickable { onMarkRoundComplete(phaseNum) }
                                        ) {
                                            Text(
                                                text = "تکمیل کل دور",
                                                color = SuccessGreen,
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }

                                phaseRevs.forEach { rev ->
                                    RevisionItemRow(
                                        revision = rev,
                                        onToggle = { onToggleRevision(rev) },
                                        onDelete = { onDeleteRevision(rev) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProjectFinanceGroupCard(
    price: Double,
    paidAmount: Double,
    remainingDebt: Double,
    isSettled: Boolean,
    payments: List<PaymentEntity>,
    showPaymentsHistory: Boolean,
    onTogglePaymentsHistory: () -> Unit,
    onRecordPayment: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        border = BorderStroke(1.dp, BorderDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Group Header (Icon + Title + Short Status + Action)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(SuccessGreen.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = SuccessGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "امور مالی و پرداخت‌ها",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 14.5.sp
                        )
                        Text(
                            text = if (isSettled) "تسویه کامل (${PersianUtils.formatCurrencyFa(price)})" else "مانده بدهی: ${PersianUtils.formatCurrencyFa(remainingDebt)}",
                            color = if (isSettled) SuccessGreen else WarningAmber,
                            fontSize = 11.5.sp
                        )
                    }
                }

                Button(
                    onClick = onRecordPayment,
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen.copy(alpha = 0.18f)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Payment,
                        contentDescription = null,
                        tint = SuccessGreen,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "ثبت دریافتی",
                        color = SuccessGreen,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Financial Summary Snapshot
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    color = DarkInputBg
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(text = "کل قرارداد", color = TextMuted, fontSize = 10.sp)
                        Text(
                            text = PersianUtils.formatCurrencyFa(price),
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    color = DarkInputBg
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(text = "دریافتی", color = TextMuted, fontSize = 10.sp)
                        Text(
                            text = PersianUtils.formatCurrencyFa(paidAmount),
                            color = SuccessGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    color = DarkInputBg
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(text = "مانده بدهی", color = TextMuted, fontSize = 10.sp)
                        Text(
                            text = if (isSettled) "تسویه کامل" else PersianUtils.formatCurrencyFa(remainingDebt),
                            color = if (isSettled) SuccessGreen else WarningAmber,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Transactions History Toggle
            if (payments.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onTogglePaymentsHistory() }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "مشاهده ریز سوابق پرداخت (${PersianUtils.faNum(payments.size)} فقره)",
                        color = PrimaryPurple,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Icon(
                        imageVector = if (showPaymentsHistory) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = PrimaryPurple,
                        modifier = Modifier.size(20.dp)
                    )
                }

                AnimatedVisibility(visible = showPaymentsHistory) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        payments.forEach { payment ->
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                color = DarkInputBg,
                                border = BorderStroke(1.dp, BorderDark.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = PersianUtils.faNum(payment.date),
                                            color = TextSecondary,
                                            fontSize = 11.sp
                                        )
                                        if (!payment.note.isNullOrBlank()) {
                                            Text(
                                                text = payment.note,
                                                color = TextMuted,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                    Text(
                                        text = PersianUtils.formatCurrencyFa(payment.amount),
                                        color = SuccessGreen,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.5.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProjectDeliveryGroupCard(
    projectId: Int,
    isCompleted: Boolean,
    isReady: Boolean,
    remainingClips: Int,
    onViewReport: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        border = BorderStroke(1.dp, BorderDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Group Header (Icon + Title + Short Status + Action)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(PrimaryPurple.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inventory2,
                            contentDescription = null,
                            tint = PrimaryPurple,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "تحویل و خروجی پروژه",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 14.5.sp
                        )
                        Text(
                            text = "شناسه: #${PersianUtils.faNum(projectId)} • ${if (isCompleted) "تکمیل شده" else if (isReady) "آماده تحویل" else "در جریان تدوین"}",
                            color = TextSecondary,
                            fontSize = 11.5.sp
                        )
                    }
                }

                Button(
                    onClick = onViewReport,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Article,
                        contentDescription = null,
                        tint = PrimaryPurple,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "گزارش رسمی",
                        color = PrimaryPurple,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Description
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = DarkInputBg
            ) {
                Text(
                    text = when {
                        isCompleted -> "این پروژه به طور کامل تدوین، اصلاح و تحویل داده شده و در آرشیو نیز در دسترس است."
                        isReady -> "تمامی کلیپ‌ها و اصلاحات انجام شده‌اند؛ پروژه آماده تحویل رسمی به کارفرما است."
                        else -> "${PersianUtils.faNum(remainingClips)} کلیپ تا اتمام تدوین و آماده‌سازی نهایی برای تحویل باقی مانده است."
                    },
                    color = TextSecondary,
                    fontSize = 11.5.sp,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}

@Composable
private fun ProjectSecondaryInfoCard(
    project: ProjectEntity,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onEditProject: () -> Unit,
    onDeleteProject: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        border = BorderStroke(1.dp, BorderDark.copy(alpha = 0.7f))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "اطلاعات تکمیلی و مشخصات پرونده",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        fontSize = 13.5.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "تاریخچه، مشخصات، ویرایش و مدیریت پروژه",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = TextSecondary
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkSurface)
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Metadata Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            color = DarkInputBg
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(text = "شناسه پرونده", color = TextMuted, fontSize = 10.sp)
                                Text(
                                    text = "#${PersianUtils.faNum(project.id)}",
                                    color = TextPrimary,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            color = DarkInputBg
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(text = "آتلیه طرف حساب", color = TextMuted, fontSize = 10.sp)
                                Text(
                                    text = PersianUtils.formatStudioName(project.studioName),
                                    color = TextPrimary,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            color = DarkInputBg
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(text = "تاریخ ثبت پروژه", color = TextMuted, fontSize = 10.sp)
                                Text(
                                    text = PersianUtils.faNum(project.createdAt),
                                    color = TextPrimary,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            color = DarkInputBg
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(text = "تاریخ مراسم عروسی", color = TextMuted, fontSize = 10.sp)
                                Text(
                                    text = project.weddingDate?.let { PersianUtils.faNum(it) } ?: "ثبت نشده",
                                    color = TextPrimary,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            color = DarkInputBg
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(text = "مهلت تحویل (ددلاین)", color = TextMuted, fontSize = 10.sp)
                                Text(
                                    text = project.deadlineDate?.let { PersianUtils.faNum(it) } ?: "تعیین نشده",
                                    color = TextPrimary,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            color = DarkInputBg
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(text = "وضعیت در دیتابیس", color = TextMuted, fontSize = 10.sp)
                                Text(
                                    text = project.status,
                                    color = TextPrimary,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Action buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onEditProject,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple.copy(alpha = 0.2f)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = PrimaryPurple,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ویرایش مشخصات",
                                color = PrimaryPurple,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = onDeleteProject,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = ErrorRed.copy(alpha = 0.2f)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                tint = ErrorRed,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "حذف پروژه",
                                color = ErrorRed,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RevisionItemRow(
    revision: ProjectRevisionEntity,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val isApplied = revision.isApplied == 1

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isApplied) DarkCard.copy(alpha = 0.6f) else DarkCard
        ),
        border = BorderStroke(
            1.dp,
            if (isApplied) SuccessGreen.copy(alpha = 0.35f) else BorderDark
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable { onToggle() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isApplied) Icons.Default.CheckCircle else Icons.Default.CheckBoxOutlineBlank,
                    contentDescription = if (isApplied) "اعمال شد" else "در انتظار اعمال",
                    tint = if (isApplied) SuccessGreen else TextMuted,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = revision.description,
                color = if (isApplied) TextMuted else TextPrimary,
                fontSize = 12.5.sp,
                textDecoration = if (isApplied) androidx.compose.ui.text.style.TextDecoration.LineThrough else null,
                modifier = Modifier.weight(1f)
            )

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "حذف اصلاحیه",
                    tint = TextMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun ClipDetailCard(
    clip: ProjectClipEntity,
    onToggle: () -> Unit,
    onStartTimer: (() -> Unit)? = null
) {
    val isDone = clip.isDone == 1

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDone) DarkCard.copy(alpha = 0.6f) else DarkCard
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isDone) SuccessGreen.copy(alpha = 0.3f) else BorderDark
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Accessible Checkbox with at least 48x48dp touch target
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .clickable { onToggle() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isDone) Icons.Default.CheckCircle else Icons.Default.CheckBoxOutlineBlank,
                    contentDescription = if (isDone) "تکمیل شده" else "در حال انجام",
                    tint = if (isDone) SuccessGreen else TextMuted,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = clip.clipName,
                    color = if (isDone) TextMuted else TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = if (isDone) FontWeight.Normal else FontWeight.SemiBold,
                    textDecoration = if (isDone) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isDone && clip.endDate != null) {
                        Text(
                            text = "تکمیل در: ${PersianUtils.faNum(clip.endDate)}",
                            color = SuccessGreen,
                            fontSize = 11.sp
                        )
                    } else if (isDone) {
                        Text(
                            text = "نهایی و تحویل‌شده",
                            color = SuccessGreen,
                            fontSize = 11.sp
                        )
                    } else {
                        Text(
                            text = "در دست اقدام تدوینگر",
                            color = WarningAmber,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Quick Timer Button for Clip
            if (onStartTimer != null) {
                IconButton(
                    onClick = onStartTimer,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = "تنظیم تایمر برای این کلیپ",
                        tint = MediaAccentCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
            }

            // Status Indicator Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        if (isDone) SuccessGreen.copy(alpha = 0.15f) else WarningAmber.copy(alpha = 0.15f)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (isDone) "تکمیل" else "در جریان",
                    color = if (isDone) SuccessGreen else WarningAmber,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditProjectDialog(
    project: ProjectEntity,
    studios: List<String>,
    onDismiss: () -> Unit,
    onSave: (ProjectEntity) -> Unit
) {
    var title by remember { mutableStateOf(project.name) }
    var studio by remember { mutableStateOf(project.studioName) }
    var priceText by remember { mutableStateOf(project.price.toLong().toString()) }
    var weddingDate by remember { mutableStateOf<String?>(project.weddingDate) }
    var deadlineDate by remember { mutableStateOf(project.deadlineDate) }
    var showWeddingDatePicker by remember { mutableStateOf(false) }
    var showDeadlineDatePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.fillMaxWidth(0.85f),
        title = {
            Text("ویرایش اطلاعات پروژه", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Read-Only Project Code Banner
                if (project.projectCode.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = PrimaryPurple.copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, PrimaryPurple.copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = PrimaryPurple,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "شماره یکتای پروژه (دائمی)",
                                        color = TextSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = PersianUtils.faNum(project.projectCode),
                                        color = PrimaryPurple,
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Text(
                                text = "ثابت 🔒",
                                color = TextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // 1. Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("عنوان پروژه") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryPurple,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                // 2. Wedding Date
                WeddingDateField(
                    value = weddingDate,
                    onClick = { showWeddingDatePicker = true },
                    onClear = { weddingDate = null }
                )

                if (showWeddingDatePicker) {
                    PersianDatePickerDialog(
                        initialDate = weddingDate,
                        onDismiss = { showWeddingDatePicker = false },
                        onDateConfirm = { selected ->
                            weddingDate = selected
                            showWeddingDatePicker = false
                        }
                    )
                }

                // 3. Deadline Date Field
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = DarkInputBg,
                    border = BorderStroke(1.dp, BorderDark),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { showDeadlineDatePicker = true }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Event,
                                contentDescription = null,
                                tint = PrimaryPurple,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "موعد تحویل (ددلاین):",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = PersianUtils.faNum(deadlineDate),
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Text(
                            text = "تغییر تاریخ",
                            color = PrimaryPurple,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                if (showDeadlineDatePicker) {
                    PersianDatePickerDialog(
                        initialDate = deadlineDate,
                        onDismiss = { showDeadlineDatePicker = false },
                        onDateConfirm = { selected ->
                            deadlineDate = selected
                            showDeadlineDatePicker = false
                        }
                    )
                }

                // 4. Studio
                Column {
                    Text("آتلیه / کارفرما:", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    ResponsiveCapsuleGrid(
                        items = studios,
                        maxPerRow = 2
                    ) { s, itemModifier ->
                        val isSelected = studio == s
                        Surface(
                            onClick = { studio = s },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) PrimaryPurple else DarkSurface,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) PrimaryPurple else BorderDark
                            ),
                            modifier = itemModifier.clip(RoundedCornerShape(10.dp))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                                Text(
                                    text = PersianUtils.formatStudioName(s),
                                    color = if (isSelected) Color.White else TextPrimary,
                                    fontSize = 12.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                // 5. Price
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { input ->
                        val converted = PersianUtils.convertFaToEnNum(input)
                        priceText = converted.filter { it.isDigit() }
                    },
                    label = { Text("مبلغ قرارداد (تومان)") },
                    singleLine = true,
                    visualTransformation = PersianNumberVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryPurple,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                val editAmountLong = priceText.toLongOrNull() ?: 0L
                if (editAmountLong > 0L) {
                    Text(
                        text = "به حروف: ${PersianUtils.numberToWordsFa(editAmountLong, "تومان")}",
                        color = PrimaryPurple,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val p = priceText.toDoubleOrNull() ?: 0.0
                    if (title.isNotBlank()) {
                        onSave(project.copy(
                            name = title,
                            studioName = studio,
                            price = p,
                            weddingDate = weddingDate,
                            deadlineDate = deadlineDate
                        ))
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("ذخیره تغییرات", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("انصراف", color = TextMuted)
            }
        },
        containerColor = DarkCard
    )
}

@Composable
fun AddStudioDialog(
    onDismiss: () -> Unit,
    onAdd: (String) -> Unit
) {
    var studioName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.fillMaxWidth(0.80f),
        title = { Text("افزودن آتلیه جدید", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("نام آتلیه یا کارفرمای جدید را وارد کنید:", color = TextSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = studioName,
                    onValueChange = { studioName = it },
                    placeholder = { Text("مثلاً: استودیو لنز برتر", color = TextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryPurple,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (studioName.isNotBlank()) {
                        onAdd(PersianUtils.formatStudioName(studioName))
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("افزودن", color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("انصراف", color = TextMuted)
            }
        },
        containerColor = DarkCard
    )
}

@Composable
fun RecordPaymentDialog(
    projectName: String,
    onDismiss: () -> Unit,
    onSubmit: (Double, String?) -> Unit
) {
    var amountText by remember { mutableStateOf("500000") }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.fillMaxWidth(0.80f),
        title = { Text("ثبت واریزی / بیعانه برای $projectName", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            Column {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { input ->
                        val converted = PersianUtils.convertFaToEnNum(input)
                        amountText = converted.filter { it.isDigit() }
                    },
                    label = { Text("مبلغ واریزی (تومان)", fontSize = 12.sp) },
                    singleLine = true,
                    visualTransformation = PersianNumberVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkInputBg,
                        unfocusedContainerColor = DarkInputBg,
                        focusedBorderColor = PrimaryPurple,
                        unfocusedBorderColor = BorderDark,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
                val paymentAmountLong = amountText.toLongOrNull() ?: 0L
                if (paymentAmountLong > 0L) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "به حروف: ${PersianUtils.numberToWordsFa(paymentAmountLong, "تومان")}",
                        color = SuccessGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("توضیحات / شماره پیگیری (اختیاری)", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkInputBg,
                        unfocusedContainerColor = DarkInputBg,
                        focusedBorderColor = PrimaryPurple,
                        unfocusedBorderColor = BorderDark,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    if (amt > 0) onSubmit(amt, note.ifBlank { null })
                },
                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("ثبت واریزی", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("انصراف", color = TextMuted) }
        },
        containerColor = DarkCard
    )
}

@Composable
fun AddRevisionDialog(
    onDismiss: () -> Unit,
    onSubmit: (String, Int) -> Unit
) {
    var description by remember { mutableStateOf("") }
    var phaseNum by remember { mutableIntStateOf(1) }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.fillMaxWidth(0.80f),
        title = { Text("ثبت اصلاحیه جدید (Revision)", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            Column {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("شرح اصلاحیه یا تایم‌کد (مثلا: تغییر موزیک تیزر در 01:24)", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkInputBg,
                        unfocusedContainerColor = DarkInputBg,
                        focusedBorderColor = PrimaryPurple,
                        unfocusedBorderColor = BorderDark,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("شماره دور/فاز اصلاحیه:", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = phaseNum.toString(),
                        onValueChange = { phaseNum = it.toIntOrNull() ?: 1 },
                        modifier = Modifier.width(80.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = DarkInputBg,
                            unfocusedContainerColor = DarkInputBg,
                            focusedBorderColor = PrimaryPurple,
                            unfocusedBorderColor = BorderDark,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (description.isNotBlank()) onSubmit(description, phaseNum)
                },
                colors = ButtonDefaults.buttonColors(containerColor = WarningAmber),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("ثبت اصلاحیه", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("انصراف", color = TextMuted) }
        },
        containerColor = DarkCard
    )
}
