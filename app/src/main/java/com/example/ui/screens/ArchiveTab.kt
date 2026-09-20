package com.example.ui.screens

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.PaymentEntity
import com.example.data.entity.ProjectClipEntity
import com.example.data.entity.ProjectEntity
import com.example.data.entity.ProjectRevisionEntity
import com.example.data.entity.TimerSessionEntity
import com.example.ui.components.ChipSelectable
import com.example.ui.components.ProjectReportDialog
import com.example.ui.components.ResponsiveCapsuleGrid
import com.example.ui.components.StatusBadge
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
import com.example.ui.viewmodel.MainViewModel
import com.example.util.PersianUtils

enum class ArchiveSortOption(val title: String) {
    NEWEST("جدیدترین"),
    OLDEST("قدیمی‌ترین"),
    HIGHEST_PRICE("بیشترین مبلغ"),
    LOWEST_PRICE("کمترین مبلغ"),
    ALPHABETICAL("بر اساس نام پروژه")
}

enum class ArchiveFinancialFilter(val title: String) {
    ALL("همه وضعیت‌ها"),
    SETTLED("تسویه‌شده"),
    UNSETTLED("دارای مانده بدهی")
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ArchiveTab(viewModel: MainViewModel) {
    val projects by viewModel.allProjects.collectAsState()
    val allClips by viewModel.allClips.collectAsState()
    val allPayments by viewModel.allPayments.collectAsState()
    val allSessions by viewModel.allSessions.collectAsState()
    val allRevisions by viewModel.allRevisions.collectAsState()
    val studios by viewModel.studios.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedStudio by remember { mutableStateOf<String?>(null) }
    var selectedFinancialFilter by remember { mutableStateOf(ArchiveFinancialFilter.ALL) }
    var selectedSortOption by remember { mutableStateOf(ArchiveSortOption.NEWEST) }

    var selectedProjectForCard by remember { mutableStateOf<ProjectEntity?>(null) }
    var selectedProjectForReport by remember { mutableStateOf<ProjectEntity?>(null) }
    var selectedArchivedProjectId by remember { mutableStateOf<Int?>(null) }
    var showFilterBottomSheet by remember { mutableStateOf(false) }

    // Revision dialog state
    var showRevisionDialogInArchive by remember { mutableStateOf(false) }
    var revisionDescInArchive by remember { mutableStateOf("") }
    var projectForRevision by remember { mutableStateOf<ProjectEntity?>(null) }

    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // 1. Base archive filter: strictly completed projects (completed clips & revisions finalized)
    val baseArchivedProjects = projects.filter { proj ->
        proj.status == "COMPLETED"
    }

    // 2. Search & Filter
    val filteredProjects = baseArchivedProjects.filter { proj ->
        val matchesSearch = if (searchQuery.isBlank()) true else {
            proj.name.contains(searchQuery, ignoreCase = true) ||
                    proj.studioName.contains(searchQuery, ignoreCase = true) ||
                    (proj.weddingDate?.contains(searchQuery, ignoreCase = true) == true)
        }
        val matchesStudio = if (selectedStudio == null) true else proj.studioName == selectedStudio

        val payments = allPayments.filter { it.projectId == proj.id }
        val totalPaid = payments.sumOf { it.amount }
        val isSettled = proj.isSettled == 1 || totalPaid >= proj.price

        val matchesFinancial = when (selectedFinancialFilter) {
            ArchiveFinancialFilter.ALL -> true
            ArchiveFinancialFilter.SETTLED -> isSettled
            ArchiveFinancialFilter.UNSETTLED -> !isSettled
        }

        matchesSearch && matchesStudio && matchesFinancial
    }.let { list ->
        when (selectedSortOption) {
            ArchiveSortOption.NEWEST -> list.sortedByDescending { it.id }
            ArchiveSortOption.OLDEST -> list.sortedBy { it.id }
            ArchiveSortOption.HIGHEST_PRICE -> list.sortedByDescending { it.price }
            ArchiveSortOption.LOWEST_PRICE -> list.sortedBy { it.price }
            ArchiveSortOption.ALPHABETICAL -> list.sortedBy { it.name }
        }
    }

    val selectedArchivedProject = projects.find { it.id == selectedArchivedProjectId }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkSurface),
        contentAlignment = Alignment.TopCenter
    ) {
        val screenWidth = maxWidth
        val isTabletOrDesktop = screenWidth >= 640.dp
        val isVeryWide = screenWidth >= 980.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 1100.dp)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            if (selectedArchivedProject != null) {
                BackHandler(enabled = true) {
                    selectedArchivedProjectId = null
                }

                ArchivedProjectDetailView(
                    project = selectedArchivedProject,
                    allClips = allClips,
                    allPayments = allPayments,
                    allSessions = allSessions,
                    allRevisions = allRevisions,
                    onBack = { selectedArchivedProjectId = null },
                    onReopen = { proj ->
                        viewModel.reopenProjectFromArchive(proj.id)
                        selectedArchivedProjectId = null
                        viewModel.selectProject(proj.id)
                        viewModel.setSelectedTab(0)
                    },
                    onAddRevision = { proj ->
                        projectForRevision = proj
                        showRevisionDialogInArchive = true
                    },
                    onShowCard = { proj -> selectedProjectForCard = proj },
                    onShowReport = { proj -> selectedProjectForReport = proj }
                )
            } else {
                // ================= 1. ARCHIVE HEADER =================
                ArchiveHeader(
                    totalCount = baseArchivedProjects.size,
                    filteredCount = filteredProjects.size,
                    isTabletOrDesktop = isTabletOrDesktop,
                    onExportCsv = { exportArchiveToCsv(context, filteredProjects.ifEmpty { baseArchivedProjects }, allClips, allPayments) }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // ================= 2. SEARCH & FILTER CONTROLS BAR =================
                ArchiveControlsBar(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    selectedStudio = selectedStudio,
                    selectedFinancialFilter = selectedFinancialFilter,
                    selectedSortOption = selectedSortOption,
                    onSelectSort = { selectedSortOption = it },
                    onOpenFilterSheet = { showFilterBottomSheet = true },
                    onClearAllFilters = {
                        searchQuery = ""
                        selectedStudio = null
                        selectedFinancialFilter = ArchiveFinancialFilter.ALL
                        selectedSortOption = ArchiveSortOption.NEWEST
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // ================= 3. STUDIO CAPSULES (HORIZONTALLY ADAPTIVE) =================
                val allStudioNames = listOf("همه آتلیه‌ها") + studios.map { it.name }
                ResponsiveCapsuleGrid(
                    items = allStudioNames,
                    maxPerRow = if (isTabletOrDesktop) 4 else 3,
                    modifier = Modifier.padding(bottom = 14.dp)
                ) { studioName, modifier ->
                    val isSelected = if (studioName == "همه آتلیه‌ها") selectedStudio == null else selectedStudio == studioName
                    ChipSelectable(
                        text = studioName,
                        isSelected = isSelected,
                        onSelect = {
                            selectedStudio = if (studioName == "همه آتلیه‌ها") null else studioName
                        },
                        accentColor = PrimaryPurple,
                        modifier = modifier
                    )
                }

                // ================= 4. MAIN CONTENT (LIST OF MINIMAL CARDS) =================
                if (filteredProjects.isEmpty()) {
                    ArchiveEmptyState(
                        hasQuery = searchQuery.isNotBlank() || selectedStudio != null || selectedFinancialFilter != ArchiveFinancialFilter.ALL,
                        onClearFilters = {
                            searchQuery = ""
                            selectedStudio = null
                            selectedFinancialFilter = ArchiveFinancialFilter.ALL
                        }
                    )
                } else {
                    ProjectsGrid(
                        projects = filteredProjects,
                        allPayments = allPayments,
                        allClips = allClips,
                        columns = if (isVeryWide) 3 else if (isTabletOrDesktop) 2 else 1,
                        onSelectProject = { selectedArchivedProjectId = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // ================= FILTER BOTTOM SHEET =================
    if (showFilterBottomSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showFilterBottomSheet = false },
            sheetState = sheetState,
            containerColor = DarkCard,
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .size(width = 36.dp, height = 4.dp)
                        .background(BorderDark, RoundedCornerShape(2.dp))
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.FilterList, contentDescription = null, tint = PrimaryPurple, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("فیلترهای پیشرفته آرشیو", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
                    }
                    TextButton(onClick = {
                        selectedFinancialFilter = ArchiveFinancialFilter.ALL
                        selectedStudio = null
                        showFilterBottomSheet = false
                    }) {
                        Text("حذف فیلترها", color = ErrorRed, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text("وضعیت تسویه مالی:", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ArchiveFinancialFilter.values().forEach { filter ->
                        val isSelected = selectedFinancialFilter == filter
                        Surface(
                            onClick = { selectedFinancialFilter = filter },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) PrimaryPurple else DarkSurface,
                            border = BorderStroke(1.dp, if (isSelected) PrimaryPurple else BorderDark),
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = filter.title,
                                    color = if (isSelected) Color.White else TextSecondary,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = { showFilterBottomSheet = false },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("اعمال فیلتر", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // ================= REVISION DIALOG =================
    if (showRevisionDialogInArchive && projectForRevision != null) {
        val proj = projectForRevision!!
        val projRevisions = allRevisions.filter { it.projectId == proj.id }
        val maxPhase = projRevisions.maxOfOrNull { it.phaseNum } ?: 0
        val targetPhase = maxPhase + 1
        var revisionPhaseInArchive by remember(proj.id) { mutableIntStateOf(targetPhase) }

        AlertDialog(
            onDismissRequest = {
                showRevisionDialogInArchive = false
                projectForRevision = null
            },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier.fillMaxWidth(0.80f),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ثبت اصلاحیه برای ${proj.name}", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = DarkSurface,
                        border = BorderStroke(1.dp, BorderDark),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("دوره فاز اصلاحیه:", color = TextSecondary, fontSize = 12.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "دور ${PersianUtils.faNum(revisionPhaseInArchive)}",
                                    color = WarningAmber,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                if (maxPhase > 0) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "(${PersianUtils.faNum(maxPhase)} دور قبلی ثبت شده)",
                                        color = TextMuted,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }

                    Text(
                        "شرح اصلاحیه یا تغییرات موردنیاز مشتری را یادداشت کنید تا پرونده مجدداً فعال گردد:",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = revisionDescInArchive,
                        onValueChange = { revisionDescInArchive = it },
                        placeholder = { Text("مثلاً: تغییر موزیک کلیپ باغ یا کات‌های رقص تانگو...", color = TextMuted, fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        shape = RoundedCornerShape(10.dp),
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
                        if (revisionDescInArchive.isNotBlank()) {
                            viewModel.addRevision(proj.id, revisionDescInArchive, revisionPhaseInArchive)
                            showRevisionDialogInArchive = false
                            projectForRevision = null
                            revisionDescInArchive = ""
                            selectedArchivedProjectId = null
                            viewModel.selectProject(proj.id)
                            viewModel.setSelectedTab(0)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("ثبت و بازگردانی به کارتابل فعال", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showRevisionDialogInArchive = false
                    projectForRevision = null
                    revisionDescInArchive = ""
                }) {
                    Text("انصراف", color = TextMuted)
                }
            },
            containerColor = DarkCard,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // ================= PROJECT ID CARD MODAL DIALOG =================
    if (selectedProjectForCard != null) {
        val proj = selectedProjectForCard!!
        val clips = allClips.filter { it.projectId == proj.id }
        val payments = allPayments.filter { it.projectId == proj.id }
        val totalPaid = payments.sumOf { it.amount }
        val isSettled = proj.isSettled == 1 || totalPaid >= proj.price

        AlertDialog(
            onDismissRequest = { selectedProjectForCard = null },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier.fillMaxWidth(0.80f),
            title = null,
            text = {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    border = BorderStroke(1.dp, BorderDark)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = PrimaryPurple.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, PrimaryPurple.copy(alpha = 0.3f))
                        ) {
                            Text(
                                "شناسنامه رسمی کار کاترلاگ",
                                color = PrimaryPurple,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                                fontSize = 11.5.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = proj.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 17.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "کارفرما: ${PersianUtils.formatStudioName(proj.studioName)} • تاریخ ثبت: ${PersianUtils.faNum(proj.createdAt)}" +
                                    if (!proj.weddingDate.isNullOrBlank()) " • 💍 عروسی: ${PersianUtils.faNum(proj.weddingDate)}" else "",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = BorderDark, thickness = 1.dp)
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("تعداد کلیپ‌ها", color = TextMuted, fontSize = 10.5.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(PersianUtils.faNum(clips.size), color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("مبلغ قرارداد", color = TextMuted, fontSize = 10.5.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(PersianUtils.formatCurrencyFa(proj.price), color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("وضعیت تسویه", color = TextMuted, fontSize = 10.5.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (isSettled) "تسویه کامل" else "دارای بدهی",
                                    color = if (isSettled) SuccessGreen else WarningAmber,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = {
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        "پرونده کار: ${proj.name}\nطرف حساب: ${PersianUtils.formatStudioName(proj.studioName)}" +
                                                (if (!proj.weddingDate.isNullOrBlank()) "\nتاریخ روز عروسی: ${proj.weddingDate}" else "") +
                                                "\nارزش قرارداد: ${PersianUtils.formatCurrencyFa(proj.price)}" +
                                                "\nوضعیت: ${if (isSettled) "تسویه‌شده در کاترلاگ" else "دارای مانده بدهی"}"
                                    )
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "اشتراک‌گذاری شناسنامه کار"))
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("اشتراک‌گذاری شناسنامه پروژه", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { selectedProjectForCard = null }) {
                    Text("بستن", color = TextMuted)
                }
            },
            containerColor = Color.Transparent
        )
    }

    // ================= PROJECT REPORT MODAL DIALOG =================
    if (selectedProjectForReport != null) {
        val proj = selectedProjectForReport!!
        val projClips = allClips.filter { it.projectId == proj.id }
        val projPayments = allPayments.filter { it.projectId == proj.id }
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

// ================= HEADER COMPONENT =================
@Composable
private fun ArchiveHeader(
    totalCount: Int,
    filteredCount: Int,
    isTabletOrDesktop: Boolean,
    onExportCsv: () -> Unit
) {
    if (isTabletOrDesktop) {
        // Desktop / Tablet Horizontal Header Layout
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    modifier = Modifier.size(42.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = PrimaryPurple.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, PrimaryPurple.copy(alpha = 0.35f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Archive,
                            contentDescription = null,
                            tint = PrimaryPurple,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "آرشیو پروژه‌ها",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SuccessGreen.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.35f))
                        ) {
                            Text(
                                text = "${PersianUtils.faNum(filteredCount)} پرونده",
                                color = SuccessGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "مدیریت و بازیابی پرونده‌های تحویل‌شده و بایگانی‌شده",
                        color = TextSecondary,
                        fontSize = 11.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Surface(
                onClick = onExportCsv,
                shape = RoundedCornerShape(10.dp),
                color = DarkCard,
                border = BorderStroke(1.dp, BorderDark)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Download,
                        contentDescription = "خروجی اکسل",
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "خروجی اکسل / CSV",
                        color = TextPrimary,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.5.sp
                    )
                }
            }
        }
    } else {
        // Mobile Layout: Title row + Full-width premium Action Bar below
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(38.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = PrimaryPurple.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, PrimaryPurple.copy(alpha = 0.35f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Archive,
                                contentDescription = null,
                                tint = PrimaryPurple,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "آرشیو پروژه‌ها",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontSize = 17.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = SuccessGreen.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.35f))
                            ) {
                                Text(
                                    text = "${PersianUtils.faNum(filteredCount)} پرونده",
                                    color = SuccessGreen,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "بایگانی پروژه‌های تسویه‌شده و نهایی",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Full-width elegant Export button for Mobile
            Surface(
                onClick = onExportCsv,
                shape = RoundedCornerShape(10.dp),
                color = DarkCard,
                border = BorderStroke(1.dp, BorderDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 9.dp, horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Download,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "دریافت خروجی اکسل و CSV از پرونده‌ها",
                        color = TextPrimary,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.5.sp
                    )
                }
            }
        }
    }
}

// ================= CONTROLS & SEARCH COMPONENT =================
@Composable
private fun ArchiveControlsBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedStudio: String?,
    selectedFinancialFilter: ArchiveFinancialFilter,
    selectedSortOption: ArchiveSortOption,
    onSelectSort: (ArchiveSortOption) -> Unit,
    onOpenFilterSheet: () -> Unit,
    onClearAllFilters: () -> Unit
) {
    var sortMenuExpanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("جستجوی نام پروژه، تاریخ یا آتلیه...", color = TextMuted, fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp)) },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { onSearchQueryChange("") }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "پاک کردن جستجو", tint = TextMuted, modifier = Modifier.size(16.dp))
                        }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
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

            // Filter Button (with indicator if active)
            val isFilterActive = selectedStudio != null || selectedFinancialFilter != ArchiveFinancialFilter.ALL
            Surface(
                onClick = onOpenFilterSheet,
                shape = RoundedCornerShape(12.dp),
                color = if (isFilterActive) PrimaryPurple.copy(alpha = 0.2f) else DarkCard,
                border = BorderStroke(1.dp, if (isFilterActive) PrimaryPurple else BorderDark),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.FilterList,
                        contentDescription = "فیلتر",
                        tint = if (isFilterActive) PrimaryPurple else TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Sort Dropdown Button
            Box {
                Surface(
                    onClick = { sortMenuExpanded = true },
                    shape = RoundedCornerShape(12.dp),
                    color = DarkCard,
                    border = BorderStroke(1.dp, BorderDark),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Sort, contentDescription = "مرتب‌سازی", tint = TextSecondary, modifier = Modifier.size(20.dp))
                    }
                }

                DropdownMenu(
                    expanded = sortMenuExpanded,
                    onDismissRequest = { sortMenuExpanded = false },
                    modifier = Modifier.background(DarkCard)
                ) {
                    ArchiveSortOption.values().forEach { option ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = option.title,
                                    color = if (selectedSortOption == option) PrimaryPurple else TextPrimary,
                                    fontWeight = if (selectedSortOption == option) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 12.sp
                                )
                            },
                            onClick = {
                                onSelectSort(option)
                                sortMenuExpanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

// ================= PROJECTS GRID COMPONENT (MINIMAL CLEAN CARDS) =================
@Composable
private fun ProjectsGrid(
    projects: List<ProjectEntity>,
    allPayments: List<PaymentEntity>,
    allClips: List<ProjectClipEntity> = emptyList(),
    columns: Int,
    onSelectProject: (Int) -> Unit
) {
    if (columns == 1) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            projects.forEach { project ->
                val payments = allPayments.filter { it.projectId == project.id }
                val totalPaid = payments.sumOf { it.amount }
                val isSettled = project.isSettled == 1 || totalPaid >= project.price
                val clips = allClips.filter { it.projectId == project.id }

                MinimalArchiveProjectCard(
                    project = project,
                    totalPaid = totalPaid,
                    isSettled = isSettled,
                    clips = clips,
                    onCardClick = { onSelectProject(project.id) }
                )
            }
        }
    } else {
        val chunked = projects.chunked(columns)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            chunked.forEach { rowProjects ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowProjects.forEach { project ->
                        val payments = allPayments.filter { it.projectId == project.id }
                        val totalPaid = payments.sumOf { it.amount }
                        val isSettled = project.isSettled == 1 || totalPaid >= project.price
                        val clips = allClips.filter { it.projectId == project.id }

                        Box(modifier = Modifier.weight(1f)) {
                            MinimalArchiveProjectCard(
                                project = project,
                                totalPaid = totalPaid,
                                isSettled = isSettled,
                                clips = clips,
                                onCardClick = { onSelectProject(project.id) }
                            )
                        }
                    }
                    if (rowProjects.size < columns) {
                        for (i in 0 until (columns - rowProjects.size)) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

// ================= MINIMAL ARCHIVE PROJECT CARD =================
@Composable
private fun MinimalArchiveProjectCard(
    project: ProjectEntity,
    totalPaid: Double,
    isSettled: Boolean,
    clips: List<ProjectClipEntity> = emptyList(),
    onCardClick: () -> Unit
) {
    val remainingDebt = (project.price - totalPaid).coerceAtLeast(0.0)
    val totalClips = clips.size
    val completedClips = clips.count { it.isDone == 1 }
    val progressPct = if (totalClips > 0) (completedClips.toFloat() / totalClips).coerceIn(0f, 1f) else 1f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("archive_project_card_${project.id}")
            .clickable { onCardClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        border = BorderStroke(1.dp, BorderDark.copy(alpha = 0.8f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // ================= 1. HEADER: PROJECT NAME, ID & STATUS =================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = project.name,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFF0F172A),
                        border = BorderStroke(0.8.dp, BorderDark)
                    ) {
                        Text(
                            text = "#${PersianUtils.faNum(project.id)}",
                            color = TextMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Delicate Status Badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = SuccessGreen.copy(alpha = 0.10f),
                    border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.25f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .background(SuccessGreen, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "تکمیل شده",
                            color = SuccessGreen,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ================= 2. CORE DETAILS: STUDIO & DATE =================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Studio
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Icon(
                        imageVector = Icons.Default.Store,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "آتلیه: ",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                    Text(
                        text = PersianUtils.formatStudioName(project.studioName),
                        color = TextSecondary,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Date (Wedding or Created)
                val hasWedding = !project.weddingDate.isNullOrBlank()
                val dateLabel = if (hasWedding) "مراسم:" else "ثبت:"
                val dateVal = if (hasWedding) PersianUtils.faNum(project.weddingDate!!) else PersianUtils.faNum(project.createdAt)

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = dateLabel,
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = dateVal,
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            // ================= 3. PROGRESS & CLIPS (IF PRESENT) =================
            if (totalClips > 0) {
                Spacer(modifier = Modifier.height(10.dp))
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "کلیپ‌ها: ${PersianUtils.faNum(completedClips)} از ${PersianUtils.faNum(totalClips)} تحویل‌شده",
                            color = TextMuted,
                            fontSize = 10.5.sp
                        )
                        Text(
                            text = "${PersianUtils.faNum((progressPct * 100).toInt())}٪",
                            color = SuccessGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.5.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    // Sleek Subtle Progress Line
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.5.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color(0xFF0F172A))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progressPct)
                                .height(3.5.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(SuccessGreen)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(11.dp))
            HorizontalDivider(color = BorderDark.copy(alpha = 0.5f), thickness = 0.7.dp)
            Spacer(modifier = Modifier.height(10.dp))

            // ================= 4. FINANCIAL SUMMARY =================
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val isNarrow = maxWidth < 290.dp

                if (isNarrow) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0C1322))
                            .border(BorderStroke(0.6.dp, BorderDark.copy(alpha = 0.6f)), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("مبلغ قرارداد:", color = TextMuted, fontSize = 10.5.sp)
                            Text(PersianUtils.formatCurrencyFa(project.price), color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("دریافتی کل:", color = TextMuted, fontSize = 10.5.sp)
                            Text(PersianUtils.formatCurrencyFa(totalPaid), color = SuccessGreen, fontWeight = FontWeight.SemiBold, fontSize = 11.5.sp)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("وضعیت حساب:", color = TextMuted, fontSize = 10.5.sp)
                            Text(
                                text = if (isSettled) "تسویه کامل" else "مانده: ${PersianUtils.formatCurrencyFa(remainingDebt)}",
                                color = if (isSettled) SuccessGreen else ErrorRed,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0C1322))
                            .border(BorderStroke(0.6.dp, BorderDark.copy(alpha = 0.6f)), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 7.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Contract Price
                        Column {
                            Text("ارزش قرارداد", color = TextMuted, fontSize = 9.5.sp)
                            Spacer(modifier = Modifier.height(1.dp))
                            Text(
                                text = PersianUtils.formatCurrencyFa(project.price),
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp
                            )
                        }

                        // Paid Amount
                        Column {
                            Text("دریافتی کل", color = TextMuted, fontSize = 9.5.sp)
                            Spacer(modifier = Modifier.height(1.dp))
                            Text(
                                text = PersianUtils.formatCurrencyFa(totalPaid),
                                color = SuccessGreen,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.5.sp
                            )
                        }

                        // Settlement / Remaining
                        Column(horizontalAlignment = Alignment.End) {
                            Text(if (isSettled) "وضعیت حساب" else "مانده بدهی", color = TextMuted, fontSize = 9.5.sp)
                            Spacer(modifier = Modifier.height(1.dp))
                            Text(
                                text = if (isSettled) "تسویه شده" else PersianUtils.formatCurrencyFa(remainingDebt),
                                color = if (isSettled) SuccessGreen else ErrorRed,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ================= 5. ACTION AFFORDANCE =================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "مشاهده سوابق و جزئیات پرونده",
                    color = TextMuted,
                    fontSize = 10.5.sp
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = "ورود به پرونده",
                        color = PrimaryPurple,
                        fontWeight = FontWeight.Medium,
                        fontSize = 10.5.sp
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        tint = PrimaryPurple,
                        modifier = Modifier.size(11.dp)
                    )
                }
            }
        }
    }
}

// ================= DEDICATED ARCHIVED PROJECT DETAIL VIEW =================
@Composable
private fun ArchivedProjectDetailView(
    project: ProjectEntity,
    allClips: List<ProjectClipEntity>,
    allPayments: List<PaymentEntity>,
    allSessions: List<TimerSessionEntity>,
    allRevisions: List<ProjectRevisionEntity>,
    onBack: () -> Unit,
    onReopen: (ProjectEntity) -> Unit,
    onAddRevision: (ProjectEntity) -> Unit,
    onShowCard: (ProjectEntity) -> Unit,
    onShowReport: (ProjectEntity) -> Unit
) {
    val clips = remember(project.id, allClips) { allClips.filter { it.projectId == project.id } }
    val payments = remember(project.id, allPayments) { allPayments.filter { it.projectId == project.id } }
    val sessions = remember(project.id, allSessions) { allSessions.filter { it.projectId == project.id } }
    val revisions = remember(project.id, allRevisions) { allRevisions.filter { it.projectId == project.id } }

    val totalPaid = remember(payments) { payments.sumOf { it.amount } }
    val totalSeconds = remember(sessions) { sessions.sumOf { it.durationSeconds } }
    val remaining = remember(project.price, totalPaid) { (project.price - totalPaid).coerceAtLeast(0.0) }
    val isSettled = remember(project.isSettled, totalPaid, project.price) {
        project.isSettled == 1 || totalPaid >= project.price
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Navigation Top Bar: Back Button & Archival Status Badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onBack,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, BorderDark),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "بازگشت",
                    modifier = Modifier.size(16.dp),
                    tint = PrimaryPurple
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("بازگشت به لیست آرشیو", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = SuccessGreen.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.35f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(6.dp).background(SuccessGreen, CircleShape))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "پرونده مختومه در آرشیو",
                        color = SuccessGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 2. Project Header Identity Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            border = BorderStroke(1.dp, BorderDark)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = project.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Store, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = PersianUtils.formatStudioName(project.studioName),
                            color = TextSecondary,
                            fontSize = 12.5.sp
                        )
                    }

                    if (!project.weddingDate.isNullOrBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "مراسم: ${PersianUtils.faNum(project.weddingDate)}",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Text(
                        text = "کد پرونده: #${PersianUtils.faNum(project.id)}",
                        color = TextMuted,
                        fontSize = 11.5.sp
                    )
                }
            }
        }

        // 3. Primary Action Buttons (Clear, organized, and responsive)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            border = BorderStroke(1.dp, BorderDark)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "عملیات و دسترسی‌های پرونده",
                    color = TextPrimary,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold
                )

                // Row 1: Report Preview & Project ID Card
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onShowReport(project) },
                        modifier = Modifier
                            .weight(1.3f)
                            .height(42.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
                    ) {
                        Icon(Icons.Default.Article, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("مشاهده و صدور گزارش رسمی", color = Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { onShowCard(project) },
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, BorderDark),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                    ) {
                        Icon(Icons.Default.Badge, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("شناسنامه پروژه", fontSize = 11.5.sp)
                    }
                }

                // Row 2: Restore & Add Revision
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { onReopen(project) },
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, WarningAmber.copy(alpha = 0.7f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = WarningAmber)
                    ) {
                        Icon(Icons.Default.Archive, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("بازگردانی به کارتابل فعال", color = WarningAmber, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { onAddRevision(project) },
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.7f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SuccessGreen)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ثبت اصلاحیه جدید", color = SuccessGreen, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 4. Financial & Payment Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            border = BorderStroke(1.dp, BorderDark)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "وضعیت مالی و تسویه حساب",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSettled) SuccessGreen.copy(alpha = 0.12f) else ErrorRed.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, if (isSettled) SuccessGreen.copy(alpha = 0.4f) else ErrorRed.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = if (isSettled) "تسویه کامل" else "دارای مانده بدهی",
                            color = if (isSettled) SuccessGreen else ErrorRed,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("ارزش کل قرارداد:", color = TextSecondary, fontSize = 12.sp)
                    Text(PersianUtils.formatCurrencyFa(project.price), color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("کل دریافتی:", color = TextSecondary, fontSize = 12.sp)
                    Text(PersianUtils.formatCurrencyFa(totalPaid), color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                }
                if (!isSettled) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("مانده بدهی:", color = TextSecondary, fontSize = 12.sp)
                        Text(PersianUtils.formatCurrencyFa(remaining), color = ErrorRed, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                    }
                }

                if (payments.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = BorderDark.copy(alpha = 0.5f), thickness = 0.8.dp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("ریز واریزی‌های ثبت‌شده:", color = TextSecondary, fontSize = 11.5.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(6.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        payments.forEachIndexed { pIdx, p ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(DarkSurface, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "واریز " + PersianUtils.faNum(pIdx + 1) + " • " + PersianUtils.faNum(p.date) +
                                            (if (!p.note.isNullOrBlank()) " (" + p.note + ")" else ""),
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = PersianUtils.formatCurrencyFa(p.amount),
                                    color = SuccessGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. Clips List Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            border = BorderStroke(1.dp, BorderDark)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "کلیپ‌های تحویل‌شده پروژه (" + PersianUtils.faNum(clips.size) + ")",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))

                if (clips.isEmpty()) {
                    Text("کلیپی برای این پروژه ثبت نشده است.", color = TextMuted, fontSize = 11.5.sp)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        clips.forEach { clip ->
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                color = DarkSurface,
                                border = BorderStroke(1.dp, BorderDark.copy(alpha = 0.6f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(clip.clipName, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    }

                                    if (!clip.endDate.isNullOrBlank()) {
                                        Text("تکمیل: " + PersianUtils.faNum(clip.endDate), color = TextSecondary, fontSize = 10.5.sp)
                                    } else {
                                        Text("تحویل شده", color = SuccessGreen, fontSize = 10.5.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 6. Revisions History Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            border = BorderStroke(1.dp, BorderDark)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "سوابق اصلاحات پروژه (" + PersianUtils.faNum(revisions.size) + ")",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))

                if (revisions.isEmpty()) {
                    Text("هیچ اصلاحیه‌ای برای این پروژه ثبت نشده است (تایید نسخه اولیه).", color = TextMuted, fontSize = 11.5.sp)
                } else {
                    val groupedRevisions = revisions.groupBy { it.phaseNum }.toSortedMap()
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        groupedRevisions.forEach { (phase, revList) ->
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                color = DarkSurface,
                                border = BorderStroke(1.dp, BorderDark.copy(alpha = 0.6f))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "فاز / دور " + PersianUtils.faNum(phase) + " اصلاحیه:",
                                        color = WarningAmber,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    revList.forEach { rev ->
                                        val isApplied = rev.isApplied == 1
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 2.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                                Icon(
                                                    if (isApplied) Icons.Default.CheckCircle else Icons.Default.HourglassEmpty,
                                                    contentDescription = null,
                                                    tint = if (isApplied) SuccessGreen else TextMuted,
                                                    modifier = Modifier.size(13.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    rev.description,
                                                    color = if (isApplied) TextPrimary else TextSecondary,
                                                    fontSize = 11.sp
                                                )
                                            }
                                            Text(
                                                text = if (isApplied) "اعمال شد" else "معلق",
                                                color = if (isApplied) SuccessGreen else TextMuted,
                                                fontSize = 10.sp
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

        // 7. Work Sessions / Net Duration Summary
        if (totalSeconds > 0) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                border = BorderStroke(1.dp, BorderDark)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccessTime, contentDescription = null, tint = PrimaryPurple, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("کل زمان کارکرد خالص تدوین:", color = TextSecondary, fontSize = 12.sp)
                    }
                    Text(
                        text = PersianUtils.faNum(PersianUtils.formatSecondsToHMS(totalSeconds)),
                        color = PrimaryPurple,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

// ================= EMPTY STATE =================
@Composable
private fun ArchiveEmptyState(
    hasQuery: Boolean,
    onClearFilters: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        border = BorderStroke(1.dp, BorderDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = DarkSurface,
                border = BorderStroke(1.dp, BorderDark),
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Archive,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = if (hasQuery) "هیچ پرونده‌ای منطبق با جستجو یا فیلتر یافت نشد" else "هنوز پروژه‌ای در آرشیو ثبت نشده است",
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (hasQuery) "می‌توانید فیلترها را حذف کنید یا عبارت دیگری را جستجو نمایید." else "پروژه‌ها پس از اتمام ۱۰۰٪ کلیپ‌ها یا تسویه مالی در اینجا بایگانی می‌شوند.",
                color = TextSecondary,
                fontSize = 11.5.sp,
                textAlign = TextAlign.Center
            )

            if (hasQuery) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onClearFilters,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("حذف فیلترها و مشاهده همه", color = Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private fun exportArchiveToCsv(
    context: Context,
    projects: List<ProjectEntity>,
    allClips: List<ProjectClipEntity>,
    allPayments: List<PaymentEntity>
) {
    if (projects.isEmpty()) {
        Toast.makeText(context, "هیچ پروژه‌ای در آرشیو برای خروجی وجود ندارد", Toast.LENGTH_SHORT).show()
        return
    }

    try {
        val jalaliDate = PersianUtils.getCurrentJalaliDate().replace("/", "-")
        val fileName = "CutterLog_Archive_$jalaliDate.csv"

        val sb = StringBuilder()
        // UTF-8 BOM for Microsoft Excel compatibility with Persian / RTL characters
        sb.append("\uFEFF")

        // CSV Header (Standard Persian Columns)
        sb.append("ردیف,شناسه پروژه,نام پروژه,استودیو / آتلیه,تاریخ ثبت,تاریخ مراسم,مبلغ قرارداد (تومان),کل دریافتی (تومان),مانده بدهی (تومان),وضعیت تسویه,تعداد کل کلیپ‌ها,کلیپ‌های تحویل‌شده,وضعیت پرونده\n")

        // CSV Rows
        projects.forEachIndexed { index, p ->
            val pClips = allClips.filter { it.projectId == p.id }
            val pPayments = allPayments.filter { it.projectId == p.id }
            val totalPaid = pPayments.sumOf { it.amount }
            val remaining = (p.price - totalPaid).coerceAtLeast(0.0)
            val isSettled = p.isSettled == 1 || totalPaid >= p.price
            val completedClipsCount = pClips.count { it.isDone == 1 }

            val row = listOf(
                (index + 1).toString(),
                p.id.toString(),
                escapeCsv(p.name),
                escapeCsv(p.studioName),
                escapeCsv(p.createdAt),
                escapeCsv(p.weddingDate ?: "ثبت نشده"),
                p.price.toLong().toString(),
                totalPaid.toLong().toString(),
                remaining.toLong().toString(),
                if (isSettled) "تسویه کامل" else "دارای مانده بدهی",
                pClips.size.toString(),
                completedClipsCount.toString(),
                "بایگانی‌شده / تحویل کامل"
            )
            sb.append(row.joinToString(",")).append("\n")
        }

        val csvBytes = sb.toString().toByteArray(Charsets.UTF_8)

        // 1. Write file to cacheDir/exports for immediate sharing via FileProvider
        val exportDir = File(context.cacheDir, "exports")
        if (!exportDir.exists()) exportDir.mkdirs()
        val file = File(exportDir, fileName)
        FileOutputStream(file).use { fos ->
            fos.write(csvBytes)
            fos.flush()
        }

        // 2. Also save to MediaStore.Downloads on Android Q+ so user has a permanent downloaded copy
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/CutterLog")
                }
                val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                if (uri != null) {
                    context.contentResolver.openOutputStream(uri)?.use { os ->
                        os.write(csvBytes)
                        os.flush()
                    }
                }
            } catch (ignored: Exception) {
                // Ignore download saving error and proceed with FileProvider share
            }
        }

        // 3. Share real CSV file via FileProvider
        val contentUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            putExtra(Intent.EXTRA_SUBJECT, "خروجی پرونده‌های آرشیو کاترلاگ - $jalaliDate")
            putExtra(
                Intent.EXTRA_TEXT,
                "خروجی رسمی پرونده‌های آرشیو نرم‌افزار کاترلاگ\n" +
                        "تعداد پرونده‌ها: ${PersianUtils.faNum(projects.size)} پروژه\n" +
                        "تاریخ تهیه: ${PersianUtils.faNum(PersianUtils.getCurrentJalaliDate())}"
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(sendIntent, "ارسال و باز کردن فایل اکسل/CSV آرشیو"))
        Toast.makeText(context, "فایل اکسل/CSV آرشیو با موفقیت ایجاد شد", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "خطا در تولید فایل اکسل/CSV: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

private fun escapeCsv(text: String): String {
    val escaped = text.replace("\"", "\"\"")
    return if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n") || escaped.contains("\r")) {
        "\"$escaped\""
    } else {
        escaped
    }
}
