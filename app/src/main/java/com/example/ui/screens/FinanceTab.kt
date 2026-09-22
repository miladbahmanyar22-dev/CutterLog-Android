package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.InvoiceData
import com.example.data.entity.InvoiceItem
import com.example.data.entity.PaymentEntity
import com.example.data.entity.ProjectClipEntity
import com.example.data.entity.ProjectEntity
import com.example.ui.components.InvoicePreviewDialog
import com.example.ui.components.InvoiceScopeDialog
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

private enum class FinanceFilterTab {
    ALL,
    DEBTORS_ONLY,
    SETTLED_ONLY,
    OVERDUE_ONLY
}

enum class FinanceTimePeriod(val titleFa: String, val subtitleFa: String) {
    ALL_TIME("کل دوران", "تمام تاریخچه"),
    THIS_MONTH("ماه جاری", "۳۰ روز اخیر / ماه فعلی"),
    LAST_3_MONTHS("۳ ماه اخیر", "فصل جاری"),
    LAST_6_MONTHS("۶ ماه اخیر", "نیم‌سال اخیر"),
    THIS_YEAR("سال جاری", "از ابتدای سال خورشیدی"),
    CUSTOM("بازه دلخواه", "تاریخ انتخابی شما")
}

@Composable
fun FinanceTab(viewModel: MainViewModel) {
    val projects by viewModel.allProjects.collectAsState()
    val allClips by viewModel.allClips.collectAsState()
    val payments by viewModel.allPayments.collectAsState()
    val studios by viewModel.studios.collectAsState()
    val config by viewModel.appConfig.collectAsState()

    // Navigation state: null = Overview list; string = Studio Details view
    var selectedStudioForDetail by remember { mutableStateOf<String?>(null) }

    // Search and filter state
    var searchQuery by remember { mutableStateOf("") }
    var activeFilter by remember { mutableStateOf(FinanceFilterTab.ALL) }

    // Time Period Filter State
    var selectedTimePeriod by remember { mutableStateOf(FinanceTimePeriod.ALL_TIME) }
    var showTimePeriodDialog by remember { mutableStateOf(false) }
    var customStartDate by remember { mutableStateOf("1403/01/01") }
    var customEndDate by remember { mutableStateOf("1403/12/29") }

    // Action Dialog States
    var showBulkPaymentDialog by remember { mutableStateOf(false) }
    var targetStudioForBulkPayment by remember { mutableStateOf<String?>(null) }
    var showInvoiceScopeDialog by remember { mutableStateOf(false) }
    var targetStudioForInvoice by remember { mutableStateOf<String?>(null) }
    var activeInvoiceData by remember { mutableStateOf<InvoiceData?>(null) }

    // Date Range Report Expansion State
    var showDateRangeReportSection by remember { mutableStateOf(false) }
    var reportStudioName by remember { mutableStateOf<String?>(studios.firstOrNull()?.name) }
    var reportStartDate by remember { mutableStateOf("1403/01/01") }
    var reportEndDate by remember { mutableStateOf("1403/12/29") }
    var showReportResults by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // Period-filtered Projects and Payments
    val currentJalali = remember { PersianUtils.getCurrentJalaliDate() }
    val currentYearMonth = remember { PersianUtils.parseJalaliYearMonth(currentJalali) }

    val periodFilteredProjects = remember(projects, selectedTimePeriod, customStartDate, customEndDate, currentJalali) {
        when (selectedTimePeriod) {
            FinanceTimePeriod.ALL_TIME -> projects
            FinanceTimePeriod.THIS_MONTH -> {
                projects.filter { proj ->
                    val projDate = proj.weddingDate?.ifBlank { null } ?: proj.createdAt
                    val ym = PersianUtils.parseJalaliYearMonth(projDate)
                    if (currentYearMonth != null && ym != null) {
                        ym.first == currentYearMonth.first && ym.second == currentYearMonth.second
                    } else {
                        PersianUtils.getDaysElapsed(projDate) <= 30
                    }
                }
            }
            FinanceTimePeriod.LAST_3_MONTHS -> {
                projects.filter { proj ->
                    val projDate = proj.weddingDate?.ifBlank { null } ?: proj.createdAt
                    PersianUtils.getDaysElapsed(projDate) <= 90
                }
            }
            FinanceTimePeriod.LAST_6_MONTHS -> {
                projects.filter { proj ->
                    val projDate = proj.weddingDate?.ifBlank { null } ?: proj.createdAt
                    PersianUtils.getDaysElapsed(projDate) <= 180
                }
            }
            FinanceTimePeriod.THIS_YEAR -> {
                val curYear = currentYearMonth?.first ?: 1404
                projects.filter { proj ->
                    val projDate = proj.weddingDate?.ifBlank { null } ?: proj.createdAt
                    val ym = PersianUtils.parseJalaliYearMonth(projDate)
                    ym?.first == curYear
                }
            }
            FinanceTimePeriod.CUSTOM -> {
                val start = customStartDate.trim()
                val end = customEndDate.trim()
                projects.filter { proj ->
                    val projDate = proj.weddingDate?.ifBlank { null } ?: proj.createdAt
                    projDate >= start && projDate <= end
                }
            }
        }
    }

    val periodFilteredProjectIds = remember(periodFilteredProjects) {
        periodFilteredProjects.map { it.id }.toSet()
    }

    val periodFilteredPayments = remember(payments, selectedTimePeriod, periodFilteredProjectIds, customStartDate, customEndDate) {
        when (selectedTimePeriod) {
            FinanceTimePeriod.ALL_TIME -> payments
            FinanceTimePeriod.THIS_MONTH,
            FinanceTimePeriod.LAST_3_MONTHS,
            FinanceTimePeriod.LAST_6_MONTHS,
            FinanceTimePeriod.THIS_YEAR,
            FinanceTimePeriod.CUSTOM -> {
                payments.filter { p ->
                    periodFilteredProjectIds.contains(p.projectId) ||
                    (p.date.isNotBlank() && when (selectedTimePeriod) {
                        FinanceTimePeriod.THIS_MONTH -> PersianUtils.getDaysElapsed(p.date) <= 30
                        FinanceTimePeriod.LAST_3_MONTHS -> PersianUtils.getDaysElapsed(p.date) <= 90
                        FinanceTimePeriod.LAST_6_MONTHS -> PersianUtils.getDaysElapsed(p.date) <= 180
                        FinanceTimePeriod.THIS_YEAR -> {
                            val curYear = currentYearMonth?.first ?: 1404
                            PersianUtils.parseJalaliYearMonth(p.date)?.first == curYear
                        }
                        FinanceTimePeriod.CUSTOM -> p.date >= customStartDate.trim() && p.date <= customEndDate.trim()
                        else -> true
                    })
                }
            }
        }
    }

    // Global Financial Calculations based on selected time period
    val totalContractAmount = periodFilteredProjects.sumOf { it.price }
    val totalCollected = periodFilteredPayments.sumOf { it.amount }
    val totalMarketDebt = (totalContractAmount - totalCollected).coerceAtLeast(0.0)

    // Realized Revenue vs Pipeline (Delivered vs In-Progress)
    val deliveredProjects = periodFilteredProjects.filter { it.isDelivered }
    val deliveredRevenue = deliveredProjects.sumOf { it.price }
    val inProgressProjects = periodFilteredProjects.filter { !it.isDelivered }
    val inProgressValue = inProgressProjects.sumOf { it.price }

    // Overdue calculations (>20 days)
    val overdueProjects = periodFilteredProjects.filter { proj ->
        val projPayments = payments.filter { it.projectId == proj.id }
        val projPaid = projPayments.sumOf { it.amount }
        val isUnsettled = proj.isSettled == 0 && projPaid < proj.price
        val daysElapsed = PersianUtils.getDaysElapsed(proj.createdAt)
        isUnsettled && daysElapsed > 20
    }
    val overdueClaimsAmount = overdueProjects.sumOf { proj ->
        val projPaid = payments.filter { it.projectId == proj.id }.sumOf { it.amount }
        (proj.price - projPaid).coerceAtLeast(0.0)
    }

    // Debtor studios count within the period
    val debtorStudiosCount = studios.count { studio ->
        val studioProjects = periodFilteredProjects.filter { it.studioName == studio.name }
        if (studioProjects.isEmpty()) false
        else {
            val studioPrice = studioProjects.sumOf { it.price }
            val studioPaid = payments.filter { p -> studioProjects.any { it.id == p.projectId } }.sumOf { it.amount }
            val studioDebt = (studioPrice - studioPaid).coerceAtLeast(0.0)
            studioDebt > 0
        }
    }

    if (selectedStudioForDetail != null) {
        // --- VIEW 2: STUDIO DETAIL & CARDEX SCREEN ---
        val studioName = selectedStudioForDetail!!
        StudioFinancialDetailView(
            studioName = studioName,
            projects = projects.filter { it.studioName == studioName },
            allClips = allClips,
            payments = payments,
            onBack = { selectedStudioForDetail = null },
            onCreateInvoice = {
                targetStudioForInvoice = studioName
                showInvoiceScopeDialog = true
            },
            onBulkPayment = {
                targetStudioForBulkPayment = studioName
                showBulkPaymentDialog = true
            },
            onSettleProject = { projectId ->
                viewModel.settleProjectFully(projectId)
            }
        )
    } else {
        // --- VIEW 1: OVERVIEW SCREEN (FINANCIAL SUMMARY & STUDIOS LIST) ---
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Screen Title & Header Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "حسابرسی",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary,
                        fontSize = 24.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "وضعیت مالی پروژه‌ها و استودیوها",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }

                // Time Period Filter Capsule Button
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { showTimePeriodDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    color = if (selectedTimePeriod != FinanceTimePeriod.ALL_TIME) PrimaryPurple.copy(alpha = 0.2f) else DarkSurface,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (selectedTimePeriod != FinanceTimePeriod.ALL_TIME) PrimaryPurple else BorderDark
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "فیلتر بازه زمانی درآمد",
                            tint = if (selectedTimePeriod != FinanceTimePeriod.ALL_TIME) PrimaryPurple else TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = selectedTimePeriod.titleFa,
                            color = if (selectedTimePeriod != FinanceTimePeriod.ALL_TIME) PrimaryPurple else TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 1. FINANCIAL SUMMARY SECTION (خلاصه مالی)
            FinancialGlobalSummarySection(
                totalContractAmount = totalContractAmount,
                deliveredRevenue = deliveredRevenue,
                deliveredCount = deliveredProjects.size,
                inProgressValue = inProgressValue,
                inProgressCount = inProgressProjects.size,
                totalCollected = totalCollected,
                totalMarketDebt = totalMarketDebt,
                debtorStudiosCount = debtorStudiosCount,
                totalStudiosCount = studios.size,
                overdueCount = overdueProjects.size,
                overdueAmount = overdueClaimsAmount,
                selectedPeriod = selectedTimePeriod,
                onPeriodClick = { showTimePeriodDialog = true }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 2. SEARCH & FILTERS
            SearchAndFilterBar(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                activeFilter = activeFilter,
                onFilterSelect = { activeFilter = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 3. STUDIOS LIST SECTION
            val filteredStudios = studios.filter { studio ->
                val studioProjects = projects.filter { it.studioName == studio.name }
                val studioPrice = studioProjects.sumOf { it.price }
                val studioPaid = payments.filter { p -> studioProjects.any { it.id == p.projectId } }.sumOf { it.amount }
                val studioDebt = (studioPrice - studioPaid).coerceAtLeast(0.0)

                val oldestUnsettled = studioProjects
                    .filter { it.isSettled == 0 && (it.price - payments.filter { p -> p.projectId == it.id }.sumOf { p -> p.amount }) > 0 }
                    .minByOrNull { it.id }
                val daysElapsed = oldestUnsettled?.let { PersianUtils.getDaysElapsed(it.createdAt) } ?: 0
                val isOverdue = daysElapsed > 20 && studioDebt > 0

                val matchesSearch = searchQuery.isBlank() || studio.name.contains(searchQuery.trim(), ignoreCase = true)

                val matchesFilter = when (activeFilter) {
                    FinanceFilterTab.ALL -> true
                    FinanceFilterTab.DEBTORS_ONLY -> studioDebt > 0
                    FinanceFilterTab.SETTLED_ONLY -> studioDebt <= 0 && studioProjects.isNotEmpty()
                    FinanceFilterTab.OVERDUE_ONLY -> isOverdue
                }

                matchesSearch && matchesFilter
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "استودیوها",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    fontSize = 16.sp
                )
                Text(
                    text = "${PersianUtils.faNum(filteredStudios.size)} استودیو",
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (filteredStudios.isEmpty()) {
                // Empty State
                FinanceEmptyState(
                    title = "اطلاعات حسابرسی وجود ندارد",
                    description = if (searchQuery.isNotBlank() || activeFilter != FinanceFilterTab.ALL) {
                        "هیچ استودیویی با فیلترها یا عبارت جستجو شده تطابق ندارد."
                    } else {
                        "هنوز استودیو یا پروژه‌ای در سیستم ثبت نشده است."
                    }
                )
            } else {
                filteredStudios.forEach { studio ->
                    val studioProjects = projects.filter { it.studioName == studio.name }
                    val studioPrice = studioProjects.sumOf { it.price }
                    val studioPaid = payments.filter { p -> studioProjects.any { it.id == p.projectId } }.sumOf { it.amount }
                    val studioDebt = (studioPrice - studioPaid).coerceAtLeast(0.0)

                    val completedUnsettledCount = studioProjects.count { proj ->
                        val isDelivered = proj.isDelivered
                        val projPaid = payments.filter { it.projectId == proj.id }.sumOf { it.amount }
                        val projDebt = (proj.price - projPaid).coerceAtLeast(0.0)
                        isDelivered && (proj.isSettled == 0 || projDebt > 0)
                    }

                    val oldestUnsettled = studioProjects
                        .filter { it.isSettled == 0 && (it.price - payments.filter { p -> p.projectId == it.id }.sumOf { p -> p.amount }) > 0 }
                        .minByOrNull { it.id }
                    val daysElapsed = oldestUnsettled?.let { PersianUtils.getDaysElapsed(it.createdAt) } ?: 0
                    val isOverdue = daysElapsed > 20 && studioDebt > 0

                    StudioSummaryCard(
                        studioName = studio.name,
                        projectCount = studioProjects.size,
                        totalPrice = studioPrice,
                        paidAmount = studioPaid,
                        remainingDebt = studioDebt,
                        completedUnsettledCount = completedUnsettledCount,
                        isOverdue = isOverdue,
                        daysElapsed = daysElapsed,
                        onClick = { selectedStudioForDetail = studio.name }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 4. COLLAPSIBLE DATE RANGE REPORT SECTION
            DateRangeReportSection(
                studios = studios.map { it.name },
                projects = projects,
                payments = payments,
                isExpanded = showDateRangeReportSection,
                onToggleExpand = { showDateRangeReportSection = !showDateRangeReportSection },
                reportStudioName = reportStudioName,
                onSelectStudio = { reportStudioName = it },
                reportStartDate = reportStartDate,
                onStartDateChange = { reportStartDate = it },
                reportEndDate = reportEndDate,
                onEndDateChange = { reportEndDate = it },
                showResults = showReportResults,
                onCalculate = { showReportResults = true },
                onShare = { rText ->
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, rText)
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "ارسال گزارش مالی"))
                }
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // --- BULK PAYMENT DIALOG ---
    if (showBulkPaymentDialog && targetStudioForBulkPayment != null) {
        val studioName = targetStudioForBulkPayment!!
        var bulkAmountText by remember { mutableStateOf("") }
        var bulkNote by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = {
                showBulkPaymentDialog = false
                targetStudioForBulkPayment = null
            },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier.fillMaxWidth(0.80f),
            title = {
                Text(
                    text = "واریزی کلی و توزیع خودکار: ${PersianUtils.formatStudioName(studioName)}",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "مبلغ واریز شده مستقیماً از بدهی قدیمی‌ترین پروژه‌های این استودیو کسر و تسویه خواهد شد.",
                        color = TextMuted,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedTextField(
                        value = bulkAmountText,
                        onValueChange = { input ->
                            val converted = PersianUtils.convertFaToEnNum(input)
                            bulkAmountText = converted.filter { it.isDigit() }
                        },
                        label = { Text("مبلغ واریزی (تومان)", fontSize = 11.sp) },
                        placeholder = { Text("مثلاً: ۵٬۰۰۰٬۰۰۰", color = TextMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = PersianNumberVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SuccessGreen,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            unfocusedBorderColor = BorderDark
                        )
                    )
                    val bulkAmountLong = bulkAmountText.toLongOrNull() ?: 0L
                    if (bulkAmountLong > 0L) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "به حروف: ${PersianUtils.numberToWordsFa(bulkAmountLong, "تومان")}",
                            color = SuccessGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = bulkNote,
                        onValueChange = { bulkNote = it },
                        label = { Text("توضیح یا شناسه واریز (اختیاری)", fontSize = 11.sp) },
                        placeholder = { Text("مثلاً: واریزی بیعانه ماه جاری", color = TextMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SuccessGreen,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            unfocusedBorderColor = BorderDark
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = bulkAmountText.toDoubleOrNull() ?: 0.0
                        if (amt > 0) {
                            viewModel.submitBulkPayment(studioName, amt, bulkNote.ifBlank { "واریزی کلی استودیو" })
                            showBulkPaymentDialog = false
                            targetStudioForBulkPayment = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("ثبت و توزیع واریزی", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showBulkPaymentDialog = false
                    targetStudioForBulkPayment = null
                }) {
                    Text("انصراف", color = TextMuted)
                }
            },
            containerColor = DarkCard
        )
    }

    // --- INVOICE SCOPE SELECTION DIALOG ---
    if (showInvoiceScopeDialog && targetStudioForInvoice != null) {
        val studioName = targetStudioForInvoice!!
        val studioProjects = projects.filter { it.studioName == studioName }

        InvoiceScopeDialog(
            studioName = studioName,
            studioProjects = studioProjects,
            payments = payments,
            onDismiss = {
                showInvoiceScopeDialog = false
                targetStudioForInvoice = null
            },
            onConfirm = { selectedProjs ->
                val invoiceItems = selectedProjs.mapIndexed { idx, proj ->
                    val projPaid = payments.filter { it.projectId == proj.id }.sumOf { it.amount }
                    val projDebt = (proj.price - projPaid).coerceAtLeast(0.0)
                    val projDate = proj.weddingDate?.ifBlank { null } ?: proj.createdAt
                    InvoiceItem(
                        rowNumber = idx + 1,
                        projectId = proj.id,
                        projectName = proj.name,
                        date = projDate,
                        totalPrice = proj.price,
                        paidAmount = projPaid,
                        remainingBalance = projDebt
                    )
                }

                val totalAmount = invoiceItems.sumOf { it.totalPrice }
                val totalPaid = invoiceItems.sumOf { it.paidAmount }
                val totalRemaining = invoiceItems.sumOf { it.remainingBalance }
                val accountStatus = if (totalRemaining <= 0) "تسویه شده" else "تسویه نشده"

                val dateCompact = PersianUtils.getCurrentJalaliDate().replace("/", "")
                val studioCode = (Math.abs(studioName.hashCode()) % 900) + 100
                val invoiceNumber = "CL-$dateCompact-$studioCode"

                activeInvoiceData = InvoiceData(
                    invoiceNumber = invoiceNumber,
                    issueDate = PersianUtils.getCurrentJalaliDate(),
                    studioName = studioName,
                    brandTitle = config?.invoiceBrandTitle ?: "استودیو فیلم و تدوین",
                    items = invoiceItems,
                    totalAmount = totalAmount,
                    totalPaid = totalPaid,
                    totalRemaining = totalRemaining,
                    accountStatus = accountStatus,
                    bankCard = config?.invoiceBankCard,
                    bankOwner = config?.invoiceBankOwner,
                    footerNote = config?.invoiceFooterNote
                )
                showInvoiceScopeDialog = false
                targetStudioForInvoice = null
            }
        )
    }

    // --- TIME PERIOD SELECTOR DIALOG ---
    if (showTimePeriodDialog) {
        FinanceTimePeriodDialog(
            currentPeriod = selectedTimePeriod,
            customStart = customStartDate,
            customEnd = customEndDate,
            onSelectPeriod = { period ->
                selectedTimePeriod = period
                if (period != FinanceTimePeriod.CUSTOM) {
                    showTimePeriodDialog = false
                }
            },
            onSaveCustomRange = { start, end ->
                customStartDate = start
                customEndDate = end
                selectedTimePeriod = FinanceTimePeriod.CUSTOM
                showTimePeriodDialog = false
            },
            onDismiss = { showTimePeriodDialog = false }
        )
    }

    // --- INVOICE PREVIEW DIALOG ---
    if (activeInvoiceData != null) {
        InvoicePreviewDialog(
            invoice = activeInvoiceData!!,
            onDismiss = { activeInvoiceData = null }
        )
    }
}

// -------------------------------------------------------------------------------------------------
// COMPONENT: TIME PERIOD SELECTOR DIALOG
// -------------------------------------------------------------------------------------------------
@Composable
private fun FinanceTimePeriodDialog(
    currentPeriod: FinanceTimePeriod,
    customStart: String,
    customEnd: String,
    onSelectPeriod: (FinanceTimePeriod) -> Unit,
    onSaveCustomRange: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var isCustomMode by remember { mutableStateOf(currentPeriod == FinanceTimePeriod.CUSTOM) }
    var startDateInput by remember { mutableStateOf(customStart) }
    var endDateInput by remember { mutableStateOf(customEnd) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = PrimaryPurple,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "انتخاب بازه زمانی گزارش مالی",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "درآمد، مبالغ قراردادها و مطالبات بر اساس بازه انتخابی شما محاسبه خواهند شد:",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                FinanceTimePeriod.values().forEach { period ->
                    val isSelected = (!isCustomMode && currentPeriod == period) || (isCustomMode && period == FinanceTimePeriod.CUSTOM)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                if (period == FinanceTimePeriod.CUSTOM) {
                                    isCustomMode = true
                                } else {
                                    isCustomMode = false
                                    onSelectPeriod(period)
                                }
                            },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) PrimaryPurple.copy(alpha = 0.15f) else DarkSurface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) PrimaryPurple else BorderDark
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = period.titleFa,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) PrimaryPurple else TextPrimary
                                )
                                Text(
                                    text = period.subtitleFa,
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = PrimaryPurple,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                if (isCustomMode) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = DarkSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "تعیین بازه شمسی (مثال: ۱۴۰۳/۰۶/۰۱)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = startDateInput,
                                    onValueChange = { startDateInput = it },
                                    label = { Text("از تاریخ", fontSize = 11.sp) },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = DarkInputBg,
                                        unfocusedContainerColor = DarkInputBg,
                                        focusedBorderColor = PrimaryPurple,
                                        unfocusedBorderColor = BorderDark
                                    )
                                )
                                OutlinedTextField(
                                    value = endDateInput,
                                    onValueChange = { endDateInput = it },
                                    label = { Text("تا تاریخ", fontSize = 11.sp) },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = DarkInputBg,
                                        unfocusedContainerColor = DarkInputBg,
                                        focusedBorderColor = PrimaryPurple,
                                        unfocusedBorderColor = BorderDark
                                    )
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (isCustomMode) {
                Button(
                    onClick = {
                        onSaveCustomRange(startDateInput, endDateInput)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
                ) {
                    Text("اعمال بازه دلخواه", fontWeight = FontWeight.Bold)
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text("بستن", color = TextSecondary)
                }
            }
        },
        dismissButton = {
            if (isCustomMode) {
                TextButton(onClick = { isCustomMode = false }) {
                    Text("انصراف", color = TextMuted)
                }
            }
        },
        containerColor = DarkCard,
        shape = RoundedCornerShape(16.dp)
    )
}

// -------------------------------------------------------------------------------------------------
// COMPONENT 1: GLOBAL FINANCIAL SUMMARY (خلاصه مالی کل)
// -------------------------------------------------------------------------------------------------
@Composable
private fun FinancialGlobalSummarySection(
    totalContractAmount: Double,
    deliveredRevenue: Double,
    deliveredCount: Int,
    inProgressValue: Double,
    inProgressCount: Int,
    totalCollected: Double,
    totalMarketDebt: Double,
    debtorStudiosCount: Int,
    totalStudiosCount: Int,
    overdueCount: Int,
    overdueAmount: Double,
    selectedPeriod: FinanceTimePeriod = FinanceTimePeriod.ALL_TIME,
    onPeriodClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Active Filter Banner (if not ALL_TIME)
        if (selectedPeriod != FinanceTimePeriod.ALL_TIME) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onPeriodClick() },
                shape = RoundedCornerShape(10.dp),
                color = PrimaryPurple.copy(alpha = 0.12f),
                border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryPurple.copy(alpha = 0.35f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = PrimaryPurple,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "گزارش مالی فیلتر شده: ${selectedPeriod.titleFa} (${selectedPeriod.subtitleFa})",
                            color = PrimaryPurple,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "تغییر بازه",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Row 1: مجموع قراردادها & درآمد محقق‌شده (تحویل قطعی)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SummaryMetricCard(
                title = "کل ارزش پروژه‌ها",
                value = PersianUtils.formatCurrencyFa(totalContractAmount),
                subtitle = "${PersianUtils.faNum(deliveredCount + inProgressCount)} پروژه کل",
                icon = Icons.Default.Business,
                accentColor = PrimaryPurple,
                modifier = Modifier.weight(1f)
            )

            SummaryMetricCard(
                title = "درآمد تحویل قطعی",
                value = PersianUtils.formatCurrencyFa(deliveredRevenue),
                subtitle = "${PersianUtils.faNum(deliveredCount)} پروژه تحویل شده",
                icon = Icons.Default.CheckCircle,
                accentColor = MediaAccentCyan,
                modifier = Modifier.weight(1f)
            )
        }

        // Row 2: دریافتی‌ها & مانده مطالبات
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SummaryMetricCard(
                title = "مجموع دریافتی",
                value = PersianUtils.formatCurrencyFa(totalCollected),
                subtitle = if (selectedPeriod == FinanceTimePeriod.ALL_TIME) "کل واریزی‌های دریافتی" else "واریزی‌های این بازه",
                icon = Icons.Default.AccountBalanceWallet,
                accentColor = SuccessGreen,
                modifier = Modifier.weight(1f)
            )

            SummaryMetricCard(
                title = "مانده بدهی",
                value = PersianUtils.formatCurrencyFa(totalMarketDebt),
                subtitle = "${PersianUtils.faNum(debtorStudiosCount)} استودیو بدهکار",
                icon = Icons.Default.AttachMoney,
                accentColor = if (totalMarketDebt > 0) WarningAmber else SuccessGreen,
                modifier = Modifier.weight(1f)
            )
        }

        // Optional Overdue Banner
        if (overdueCount > 0 && overdueAmount > 0) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = DarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, ErrorRed.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = ErrorRed,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "مطالبات معوقه (>۲۰ روز): ${PersianUtils.formatCurrencyFa(overdueAmount)} (${PersianUtils.faNum(overdueCount)} پروژه)",
                        color = ErrorRed,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryMetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = TextMuted,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Normal
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(accentColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = value,
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                color = accentColor,
                fontSize = 10.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// -------------------------------------------------------------------------------------------------
// COMPONENT 2: SEARCH & FILTER BAR
// -------------------------------------------------------------------------------------------------
@Composable
private fun SearchAndFilterBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    activeFilter: FinanceFilterTab,
    onFilterSelect: (FinanceFilterTab) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Search Input Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("جستجوی نام استودیو...", color = TextMuted, fontSize = 13.sp) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "جستجو",
                    tint = TextMuted,
                    modifier = Modifier.size(18.dp)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "پاک کردن",
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = DarkSurface,
                unfocusedContainerColor = DarkSurface,
                focusedBorderColor = PrimaryPurple,
                unfocusedBorderColor = BorderDark,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            )
        )

        // Filter Chips Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterTabChip(
                label = "همه",
                isSelected = activeFilter == FinanceFilterTab.ALL,
                onClick = { onFilterSelect(FinanceFilterTab.ALL) },
                modifier = Modifier.weight(1f)
            )
            FilterTabChip(
                label = "دارای بدهی",
                isSelected = activeFilter == FinanceFilterTab.DEBTORS_ONLY,
                onClick = { onFilterSelect(FinanceFilterTab.DEBTORS_ONLY) },
                modifier = Modifier.weight(1f)
            )
            FilterTabChip(
                label = "تسویه شده",
                isSelected = activeFilter == FinanceFilterTab.SETTLED_ONLY,
                onClick = { onFilterSelect(FinanceFilterTab.SETTLED_ONLY) },
                modifier = Modifier.weight(1f)
            )
            FilterTabChip(
                label = "معوقه (>۲۰ روز)",
                isSelected = activeFilter == FinanceFilterTab.OVERDUE_ONLY,
                onClick = { onFilterSelect(FinanceFilterTab.OVERDUE_ONLY) },
                modifier = Modifier.weight(1.2f)
            )
        }
    }
}

@Composable
private fun FilterTabChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) PrimaryPurple else DarkSurface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) PrimaryPurple else BorderDark
        )
    ) {
        Box(
            modifier = Modifier.padding(vertical = 7.dp, horizontal = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = if (isSelected) Color.White else TextSecondary,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                maxLines = 1
            )
        }
    }
}

// -------------------------------------------------------------------------------------------------
// COMPONENT 3: STUDIO SUMMARY CARD (ROW IN LIST)
// -------------------------------------------------------------------------------------------------
@Composable
private fun StudioSummaryCard(
    studioName: String,
    projectCount: Int,
    totalPrice: Double,
    paidAmount: Double,
    remainingDebt: Double,
    completedUnsettledCount: Int,
    isOverdue: Boolean,
    daysElapsed: Int,
    onClick: () -> Unit
) {
    val isSettled = remainingDebt <= 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSettled) BorderDark else if (isOverdue) ErrorRed.copy(alpha = 0.5f) else WarningAmber.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Top Row: Studio Name + Project Count & Account Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSettled) SuccessGreen.copy(alpha = 0.12f) else PrimaryPurple.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Business,
                            contentDescription = null,
                            tint = if (isSettled) SuccessGreen else PrimaryPurple,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = PersianUtils.formatStudioName(studioName),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "${PersianUtils.faNum(projectCount)} پروژه",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                // Account Status Badge
                StatusPill(
                    text = when {
                        isSettled -> "تسویه شده"
                        completedUnsettledCount > 0 -> "آماده تسویه"
                        isOverdue -> "معوقه"
                        else -> "دارای بدهی"
                    },
                    statusColor = when {
                        isSettled -> SuccessGreen
                        completedUnsettledCount > 0 -> WarningAmber
                        isOverdue -> ErrorRed
                        else -> PrimaryPurple
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = BorderDark.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(10.dp))

            // Bottom Financial Breakdown Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Paid Amount
                Column {
                    Text(
                        text = "پرداخت‌شده",
                        color = TextMuted,
                        fontSize = 10.5.sp
                    )
                    Spacer(modifier = Modifier.height(1.dp))
                    Text(
                        text = PersianUtils.formatCurrencyFa(paidAmount),
                        color = SuccessGreen,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Remaining Debt (Primary Visual Focus)
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "مانده بدهی",
                        color = TextMuted,
                        fontSize = 10.5.sp
                    )
                    Spacer(modifier = Modifier.height(1.dp))
                    Text(
                        text = PersianUtils.formatCurrencyFa(remainingDebt),
                        color = if (isSettled) SuccessGreen else if (isOverdue) ErrorRed else WarningAmber,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Detail indicator icon
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = "مشاهده جزئیات",
                    tint = TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// COMPONENT 4: STUDIO FINANCIAL DETAIL & CARDEX SCREEN (نمای جزئیات اختصاصی استودیو)
// -------------------------------------------------------------------------------------------------
@Composable
private fun StudioFinancialDetailView(
    studioName: String,
    projects: List<ProjectEntity>,
    allClips: List<ProjectClipEntity>,
    payments: List<PaymentEntity>,
    onBack: () -> Unit,
    onCreateInvoice: () -> Unit,
    onBulkPayment: () -> Unit,
    onSettleProject: (Int) -> Unit
) {
    val scrollState = rememberScrollState()

    val totalProjectsCount = projects.size
    val totalStudioPrice = projects.sumOf { it.price }
    val studioPayments = payments.filter { p -> projects.any { it.id == p.projectId } }
    val totalStudioPaid = studioPayments.sumOf { it.amount }
    val totalStudioDebt = (totalStudioPrice - totalStudioPaid).coerceAtLeast(0.0)

    val completedProjectsCount = projects.count { it.status == "COMPLETED" }
    val activeProjectsCount = totalProjectsCount - completedProjectsCount

    // Projects 100% finished BUT not yet settled
    val completedUnsettledProjects = projects.filter { proj ->
        val isCompleted = proj.status == "COMPLETED"
        val projPaid = payments.filter { it.projectId == proj.id }.sumOf { it.amount }
        val projDebt = (proj.price - projPaid).coerceAtLeast(0.0)
        isCompleted && (proj.isSettled == 0 || projDebt > 0)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Top Navigation Header with Back button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(DarkSurface)
                    .border(1.dp, BorderDark, RoundedCornerShape(10.dp))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "بازگشت به لیست",
                    tint = TextPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = PersianUtils.formatStudioName(studioName),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary,
                    fontSize = 20.sp
                )
                Text(
                    text = "کاردکس حسابرسی و صورتحساب استودیو",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // STUDIO ACCOUNT SUMMARY CARD (خلاصه حساب استودیو)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "خلاصه حساب مالی",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 14.sp
                    )

                    StatusPill(
                        text = if (totalStudioDebt <= 0) "تسویه کامل" else "دارای بدهی",
                        statusColor = if (totalStudioDebt <= 0) SuccessGreen else WarningAmber
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 4 Explicit Financial Breakdown Items
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DetailMetricTile(
                        label = "مجموع پروژه‌ها",
                        value = "${PersianUtils.faNum(totalProjectsCount)} پروژه",
                        subValue = "${PersianUtils.faNum(activeProjectsCount)} فعال | ${PersianUtils.faNum(completedProjectsCount)} تکمیل",
                        color = MediaAccentCyan,
                        modifier = Modifier.weight(1f)
                    )

                    DetailMetricTile(
                        label = "مبلغ کل پروژه‌ها",
                        value = PersianUtils.formatCurrencyFa(totalStudioPrice),
                        subValue = "ارزش قراردادها",
                        color = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DetailMetricTile(
                        label = "مبلغ پرداخت‌شده",
                        value = PersianUtils.formatCurrencyFa(totalStudioPaid),
                        subValue = "کل واریزی‌ها",
                        color = SuccessGreen,
                        modifier = Modifier.weight(1f)
                    )

                    DetailMetricTile(
                        label = "مبلغ باقی‌مانده (بدهی)",
                        value = PersianUtils.formatCurrencyFa(totalStudioDebt),
                        subValue = if (totalStudioDebt <= 0) "تسویه شده" else "مانده پرداخت‌نشده",
                        color = if (totalStudioDebt <= 0) SuccessGreen else WarningAmber,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // PRIMARY AND SECONDARY ACTIONS (Section 10 & 18)
        // Primary Action: ایجاد صورتحساب
        Button(
            onClick = onCreateInvoice,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Receipt,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "ایجاد صورتحساب استودیو",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Secondary Action: واریزی کلی (توزیع)
        OutlinedButton(
            onClick = onBulkPayment,
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = SuccessGreen),
            border = androidx.compose.foundation.BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.6f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Payment,
                contentDescription = null,
                tint = SuccessGreen,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "ثبت واریزی کلی (توزیع خودکار روی پروژه‌ها)",
                color = SuccessGreen,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // COMPLETED UNSETTLED PROJECTS HIGHLIGHT
        if (completedUnsettledProjects.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = DarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, WarningAmber.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = WarningAmber,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "پروژه‌های ۱۰۰٪ تکمیل‌شده در انتظار تسویه:",
                            fontWeight = FontWeight.Bold,
                            color = WarningAmber,
                            fontSize = 12.5.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    completedUnsettledProjects.forEach { proj ->
                        val projPayments = payments.filter { it.projectId == proj.id }
                        val projPaid = projPayments.sumOf { it.amount }
                        val projDebt = (proj.price - projPaid).coerceAtLeast(0.0)

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkCard),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = proj.name,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary,
                                        fontSize = 13.sp
                                    )
                                    StatusPill(text = "آماده تسویه", statusColor = WarningAmber)
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "مبلغ کل پروژه: ${PersianUtils.formatCurrencyFa(proj.price)}",
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        text = "مبلغ باقی‌مانده: ${PersianUtils.formatCurrencyFa(projDebt)}",
                                        color = WarningAmber,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Button(
                                    onClick = { onSettleProject(proj.id) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "تسویه کامل این پروژه (${PersianUtils.formatCurrencyFa(projDebt)})",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.5.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // PROJECTS LIST OF THIS STUDIO
        Text(
            text = "پروژه‌های استودیو (${PersianUtils.faNum(projects.size)})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            fontSize = 15.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (projects.isEmpty()) {
            FinanceEmptyState(
                title = "پروژه‌ای برای این استودیو ثبت نشده است",
                description = "پروژه‌های ایجاد شده در سربرگ پروژه‌ها در اینجا نمایش داده می‌شوند."
            )
        } else {
            projects.forEach { proj ->
                val projPayments = payments.filter { it.projectId == proj.id }
                val projPaid = projPayments.sumOf { it.amount }
                val projDebt = (proj.price - projPaid).coerceAtLeast(0.0)

                ProjectFinancialCard(
                    project = proj,
                    paidAmount = projPaid,
                    remainingDebt = projDebt,
                    payments = projPayments,
                    onSettle = { onSettleProject(proj.id) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun DetailMetricTile(
    label: String,
    value: String,
    subValue: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = DarkSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark.copy(alpha = 0.7f))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = label,
                color = TextMuted,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = value,
                color = color,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subValue,
                color = TextSecondary,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// -------------------------------------------------------------------------------------------------
// COMPONENT 5: PROJECT FINANCIAL CARD (DISAMBIGUATED FINANCIAL LABELS)
// -------------------------------------------------------------------------------------------------
@Composable
private fun ProjectFinancialCard(
    project: ProjectEntity,
    paidAmount: Double,
    remainingDebt: Double,
    payments: List<PaymentEntity>,
    onSettle: () -> Unit
) {
    var isHistoryExpanded by remember { mutableStateOf(false) }
    val isSettled = remainingDebt <= 0 || project.isSettled == 1

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Project Name & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = project.name,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 13.5.sp
                    )
                    Row(
                        modifier = Modifier.padding(top = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!project.projectCode.isNullOrBlank()) {
                            Text(
                                text = "کد: ${PersianUtils.faNum(project.projectCode)}",
                                color = MediaAccentCyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        if (!project.weddingDate.isNullOrBlank()) {
                            Text(
                                text = "مراسم: ${PersianUtils.faNum(project.weddingDate)}",
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        }
                        if (!project.deliveredAt.isNullOrBlank()) {
                            Text(
                                text = "تحویل: ${PersianUtils.faNum(project.deliveredAt)}",
                                color = SuccessGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    val isDelivered = project.status == "COMPLETED" || !project.deliveredAt.isNullOrBlank()
                    val isReady = project.status == "READY_FOR_DELIVERY" && !isDelivered
                    if (isDelivered) {
                        StatusPill(text = "تحویل قطعی", statusColor = SuccessGreen)
                    } else if (isReady) {
                        StatusPill(text = "آماده تحویل", statusColor = MediaAccentCyan)
                    }

                    StatusPill(
                        text = if (isSettled) "تسویه شده" else "دارای بدهی",
                        statusColor = if (isSettled) SuccessGreen else WarningAmber
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Disambiguated 3 Financial Labels (Section 9)
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = DarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("مبلغ کل پروژه:", color = TextSecondary, fontSize = 11.5.sp)
                        Text(PersianUtils.formatCurrencyFa(project.price), color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 11.5.sp)
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("مبلغ پرداخت‌شده:", color = TextSecondary, fontSize = 11.5.sp)
                        Text(PersianUtils.formatCurrencyFa(paidAmount), color = SuccessGreen, fontWeight = FontWeight.SemiBold, fontSize = 11.5.sp)
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("مبلغ باقی‌مانده:", color = TextSecondary, fontSize = 11.5.sp)
                        Text(
                            PersianUtils.formatCurrencyFa(remainingDebt),
                            color = if (isSettled) SuccessGreen else WarningAmber,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Quick Settle Button if not settled
            if (!isSettled) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onSettle,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "تسویه کامل این پروژه (${PersianUtils.formatCurrencyFa(remainingDebt)})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Expandable Payment History Log
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { isHistoryExpanded = !isHistoryExpanded }
                    .padding(vertical = 4.dp, horizontal = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ریز واریزی‌های ثبت‌شده (${PersianUtils.faNum(payments.size)})",
                    color = MediaAccentCyan,
                    fontSize = 11.sp
                )
                Icon(
                    imageVector = if (isHistoryExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MediaAccentCyan,
                    modifier = Modifier.size(18.dp)
                )
            }

            AnimatedVisibility(visible = isHistoryExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                ) {
                    if (payments.isEmpty()) {
                        Text(
                            text = "هنوز هیچ واریزی مجزایی برای این پروژه ثبت نشده است.",
                            color = TextMuted,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    } else {
                        payments.forEach { pay ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                shape = RoundedCornerShape(6.dp),
                                color = DarkSurface
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = pay.note ?: "واریز نقدی",
                                            color = TextPrimary,
                                            fontSize = 11.sp
                                        )
                                        Text(
                                            text = PersianUtils.faNum(pay.date),
                                            color = TextMuted,
                                            fontSize = 9.5.sp
                                        )
                                    }
                                    Text(
                                        text = PersianUtils.formatCurrencyFa(pay.amount),
                                        color = SuccessGreen,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.5.sp
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

// -------------------------------------------------------------------------------------------------
// COMPONENT 6: DATE RANGE REPORT SECTION (COLLAPSIBLE & STREAMLINED)
// -------------------------------------------------------------------------------------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DateRangeReportSection(
    studios: List<String>,
    projects: List<ProjectEntity>,
    payments: List<PaymentEntity>,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    reportStudioName: String?,
    onSelectStudio: (String) -> Unit,
    reportStartDate: String,
    onStartDateChange: (String) -> Unit,
    reportEndDate: String,
    onEndDateChange: (String) -> Unit,
    showResults: Boolean,
    onCalculate: () -> Unit,
    onShare: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onToggleExpand() }
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = PrimaryPurple,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "گزارش مالی در بازه زمانی مشخص",
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 13.5.sp
                        )
                        Text(
                            text = "محاسبه و اشتراک‌گذاری صورتحساب بازه زمانی خاص",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    HorizontalDivider(color = BorderDark.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(10.dp))

                    Text("انتخاب استودیو:", color = TextSecondary, fontSize = 11.5.sp)
                    Spacer(modifier = Modifier.height(4.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        studios.forEach { stName ->
                            val isSelected = reportStudioName == stName
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onSelectStudio(stName) },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) PrimaryPurple else DarkSurface,
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) PrimaryPurple else BorderDark)
                            ) {
                                Text(
                                    text = stName,
                                    color = if (isSelected) Color.White else TextPrimary,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Date Presets
                    Text("میانبرهای بازه زمانی:", color = TextMuted, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val todayStr = PersianUtils.getCurrentJalaliDate()
                        FilterTabChip(
                            label = "امروز",
                            isSelected = reportStartDate == todayStr && reportEndDate == todayStr,
                            onClick = {
                                onStartDateChange(todayStr)
                                onEndDateChange(todayStr)
                            },
                            modifier = Modifier.weight(1f)
                        )
                        FilterTabChip(
                            label = "این ماه",
                            isSelected = reportStartDate.endsWith("/01"),
                            onClick = {
                                val parts = todayStr.split("/")
                                if (parts.size == 3) {
                                    onStartDateChange("${parts[0]}/${parts[1]}/01")
                                    onEndDateChange(todayStr)
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                        FilterTabChip(
                            label = "امسال",
                            isSelected = reportStartDate.endsWith("/01/01"),
                            onClick = {
                                val parts = todayStr.split("/")
                                if (parts.size == 3) {
                                    onStartDateChange("${parts[0]}/01/01")
                                    onEndDateChange("${parts[0]}/12/29")
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Date Inputs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = reportStartDate,
                            onValueChange = onStartDateChange,
                            label = { Text("از تاریخ", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryPurple,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                unfocusedBorderColor = BorderDark
                            )
                        )

                        OutlinedTextField(
                            value = reportEndDate,
                            onValueChange = onEndDateChange,
                            label = { Text("تا تاریخ", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryPurple,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                unfocusedBorderColor = BorderDark
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = onCalculate,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("محاسبه گزارش مالی بازه", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    if (showResults && reportStudioName != null) {
                        val stName = reportStudioName
                        val filteredProjects = projects.filter {
                            it.studioName == stName && it.createdAt >= reportStartDate && it.createdAt <= reportEndDate
                        }
                        val totalPPrice = filteredProjects.sumOf { it.price }
                        val filteredPayments = payments.filter { p -> filteredProjects.any { it.id == p.projectId } }
                        val totalPPaid = filteredPayments.sumOf { it.amount }
                        val remainingPDebt = (totalPPrice - totalPPaid).coerceAtLeast(0.0)

                        Spacer(modifier = Modifier.height(12.dp))

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            color = DarkSurface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryPurple.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "نتیجه: ${PersianUtils.formatStudioName(stName)} ($reportStartDate تا $reportEndDate)",
                                    fontWeight = FontWeight.Bold,
                                    color = MediaAccentCyan,
                                    fontSize = 12.5.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("تعداد پروژه‌ها: ${PersianUtils.faNum(filteredProjects.size)} پروژه", color = TextPrimary, fontSize = 11.5.sp)
                                Text("مجموع ارزش قراردادها: ${PersianUtils.formatCurrencyFa(totalPPrice)}", color = TextPrimary, fontSize = 11.5.sp)
                                Text("مجموع پرداخت‌شده: ${PersianUtils.formatCurrencyFa(totalPPaid)}", color = SuccessGreen, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                                Text(
                                    "مانده بدهی این بازه: ${PersianUtils.formatCurrencyFa(remainingPDebt)}",
                                    color = if (remainingPDebt > 0) WarningAmber else SuccessGreen,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Button(
                                    onClick = {
                                        val rText = "گزارش مالی $stName\nبازه: $reportStartDate تا $reportEndDate\nتعداد پروژه: ${filteredProjects.size}\nارزش کل: ${PersianUtils.formatCurrencyFa(totalPPrice)}\nدریافتی: ${PersianUtils.formatCurrencyFa(totalPPaid)}\nمانده بدهی: ${PersianUtils.formatCurrencyFa(remainingPDebt)}"
                                        onShare(rText)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = MediaAccentCyan)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null, tint = DarkSurface, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("اشتراک‌گذاری گزارش این بازه", color = DarkSurface, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// COMPONENT 7: STATUS PILL & EMPTY STATE
// -------------------------------------------------------------------------------------------------
@Composable
private fun StatusPill(
    text: String,
    statusColor: Color
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = statusColor.copy(alpha = 0.12f),
        border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(statusColor)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = text,
                color = statusColor,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun FinanceEmptyState(
    title: String,
    description: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        shape = RoundedCornerShape(12.dp),
        color = DarkCard,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                color = TextMuted,
                fontSize = 11.5.sp,
                lineHeight = 16.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
