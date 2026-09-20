package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.AppConfigEntity
import com.example.data.entity.ProjectClipEntity
import com.example.data.entity.ProjectEntity
import com.example.data.entity.ProjectRevisionEntity
import com.example.data.entity.TimerSessionEntity
import com.example.ui.theme.BorderDark
import com.example.ui.theme.DarkBg
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
import java.util.Calendar
import java.util.Locale

private data class DayWorkRecord(
    val dateStr: String,
    val dayName: String,
    val totalSeconds: Long,
    val isToday: Boolean
)

private sealed class SmartActionTarget {
    data class Timer(val projectId: Int?, val clipName: String?) : SmartActionTarget()
    data class WorkspaceProject(val projectId: Int) : SmartActionTarget()
    object Finance : SmartActionTarget()
    object Workspace : SmartActionTarget()
}

private data class SmartRecommendation(
    val title: String,
    val description: String,
    val actionText: String,
    val badgeText: String,
    val badgeColor: Color,
    val target: SmartActionTarget
)

private data class UrgentAlertItem(
    val id: String,
    val title: String,
    val description: String,
    val actionText: String,
    val target: SmartActionTarget,
    val isDanger: Boolean
)

private data class ProjectAttentionItem(
    val project: ProjectEntity,
    val description: String,
    val actionText: String,
    val badgeText: String,
    val badgeColor: Color,
    val target: SmartActionTarget
)

@Composable
fun CutterPilotTab(viewModel: MainViewModel) {
    val projects by viewModel.allProjects.collectAsState()
    val clips by viewModel.allClips.collectAsState()
    val payments by viewModel.allPayments.collectAsState()
    val sessions by viewModel.allSessions.collectAsState()
    val revisions by viewModel.allRevisions.collectAsState()
    val config by viewModel.appConfig.collectAsState()

    val isTimerRunning by viewModel.isTimerRunning.collectAsState()
    val activeSessionSeconds by viewModel.activeSessionSeconds.collectAsState()
    val timerSelectedProjectId by viewModel.timerSelectedProjectId.collectAsState()
    val timerSelectedClip by viewModel.timerSelectedClip.collectAsState()

    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current

    // Dialog States
    var showEditQuotaDialog by remember { mutableStateOf(false) }
    var showQuickReportDialog by remember { mutableStateOf(false) }
    var showAlertsDialog by remember { mutableStateOf(false) }

    // Natural Query State
    var userQueryText by remember { mutableStateOf("") }
    var activeQueryAnswer by remember { mutableStateOf<String?>(null) }

    // ==========================================
    // 1. DATA COMPUTATIONS (100% REAL ROOM DATA)
    // ==========================================
    val todayDateStr = PersianUtils.getCurrentJalaliDate()
    val todaySessions = remember(sessions, todayDateStr) {
        sessions.filter { it.date == todayDateStr }
    }
    val todayTotalSecs = remember(todaySessions, activeSessionSeconds) {
        todaySessions.sumOf { it.durationSeconds } + activeSessionSeconds
    }
    val dailyQuotaHours = config?.dailyQuotaHours ?: 8.0
    val dailyQuotaSecs = (dailyQuotaHours * 3600).toLong()
    val quotaPct: Float = if (dailyQuotaSecs > 0) {
        (todayTotalSecs.toFloat() / dailyQuotaSecs.toFloat()).coerceIn(0f, 1f)
    } else 1f

    val activeProjects = remember(projects) {
        projects.filter { it.status != "COMPLETED" }
    }

    // Overdue Claims (>20 days since creation without full settlement)
    val overdueProjects = remember(projects, payments) {
        projects.filter { proj ->
            val projPaid = payments.filter { it.projectId == proj.id }.sumOf { it.amount }
            val isUnsettled = proj.isSettled == 0 && projPaid < proj.price
            val daysElapsed = PersianUtils.getDaysElapsed(proj.createdAt)
            isUnsettled && daysElapsed > 20
        }
    }
    val sumOverdueClaims = remember(overdueProjects, payments) {
        overdueProjects.sumOf { proj ->
            val paid = payments.filter { it.projectId == proj.id }.sumOf { it.amount }
            (proj.price - paid).coerceAtLeast(0.0)
        }
    }

    // Projects with overdue or imminent deadlines
    val overdueDeadlineProjects = remember(activeProjects) {
        activeProjects.filter { proj ->
            val remaining = calculateDaysRemaining(proj.deadlineDate)
            remaining != null && remaining < 0
        }
    }
    val imminentDeadlineProjects = remember(activeProjects) {
        activeProjects.filter { proj ->
            val remaining = calculateDaysRemaining(proj.deadlineDate)
            remaining != null && remaining in 0..2
        }
    }

    // Projects with unapplied revisions
    val projectsWithUnappliedRevisions = remember(activeProjects, revisions) {
        activeProjects.mapNotNull { proj ->
            val unapplied = revisions.filter { it.projectId == proj.id && it.isApplied == 0 }
            if (unapplied.isNotEmpty()) Pair(proj, unapplied.size) else null
        }
    }
    val totalUnappliedRevisions = remember(projectsWithUnappliedRevisions) {
        projectsWithUnappliedRevisions.sumOf { it.second }
    }

    // 7-Day Performance Timeline (exact calculation)
    val last7DaysRecords = remember(sessions, todayTotalSecs) {
        calculateLast7Days(sessions, todayTotalSecs)
    }
    val weekTotalSecs = remember(last7DaysRecords) {
        last7DaysRecords.sumOf { it.totalSeconds }
    }
    val daysPassedThisWeek = remember {
        val dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        val daysSinceSaturday = when (dayOfWeek) {
            Calendar.SATURDAY -> 0
            Calendar.SUNDAY -> 1
            Calendar.MONDAY -> 2
            Calendar.TUESDAY -> 3
            Calendar.WEDNESDAY -> 4
            Calendar.THURSDAY -> 5
            Calendar.FRIDAY -> 6
            else -> 0
        }
        (daysSinceSaturday + 1).coerceIn(1, 7)
    }
    val avgDailySecs = remember(weekTotalSecs, daysPassedThisWeek) {
        if (daysPassedThisWeek > 0) weekTotalSecs / daysPassedThisWeek else 0L
    }

    // Efficiency Coach Metrics
    val avgSessionMinutes = remember(sessions) {
        if (sessions.isNotEmpty()) {
            (sessions.map { it.durationSeconds }.average() / 60).toInt()
        } else 0
    }
    val focusScore = remember(avgSessionMinutes, quotaPct) {
        if (avgSessionMinutes == 0) 0
        else {
            val base = ((avgSessionMinutes.coerceIn(20, 80) / 60.0) * 85).toInt()
            val bonus = (quotaPct * 15).toInt()
            (base + bonus).coerceIn(40, 99)
        }
    }
    val peakTimeShift = remember(sessions) {
        if (sessions.isEmpty()) "ثبت نشده"
        else {
            var morningCount = 0
            var afternoonCount = 0
            var nightCount = 0
            sessions.forEach { s ->
                val hour = s.startTime.split(":").firstOrNull()?.toIntOrNull() ?: 12
                when {
                    hour in 5..11 -> morningCount++
                    hour in 12..17 -> afternoonCount++
                    else -> nightCount++
                }
            }
            when {
                morningCount >= afternoonCount && morningCount >= nightCount -> "شیفت صبح (۰۸:۰۰ الی ۱۲:۰۰)"
                afternoonCount >= morningCount && afternoonCount >= nightCount -> "شیفت عصر (۱۲:۰۰ الی ۱۸:۰۰)"
                else -> "شیفت شب (۱۸:۰۰ به بعد)"
            }
        }
    }

    // Top Project by Editing Time
    val topProjectData = remember(projects, sessions) {
        val grouped = sessions.groupBy { it.projectId }
        val topEntry = grouped.maxByOrNull { entry -> entry.value.sumOf { it.durationSeconds } }
        if (topEntry != null && topEntry.key != null) {
            val proj = projects.firstOrNull { it.id == topEntry.key }
            if (proj != null) Pair(proj, topEntry.value.sumOf { it.durationSeconds }) else null
        } else null
    }

    // ==========================================
    // 2. SMART RECOMMENDATION LOGIC (RULE-BASED)
    // ==========================================
    val recommendation = remember(
        activeProjects, clips, revisions, overdueProjects, sumOverdueClaims,
        todayTotalSecs, dailyQuotaSecs
    ) {
        determineSmartRecommendation(
            activeProjects = activeProjects,
            clips = clips,
            revisions = revisions,
            overdueProjects = overdueProjects,
            sumOverdue = sumOverdueClaims,
            todayTotalSecs = todayTotalSecs,
            dailyQuotaSecs = dailyQuotaSecs
        )
    }

    // ==========================================
    // 3. URGENT ALERTS LIST
    // ==========================================
    val urgentAlerts = remember(
        overdueProjects, overdueDeadlineProjects, imminentDeadlineProjects,
        projectsWithUnappliedRevisions, clips
    ) {
        val list = mutableListOf<UrgentAlertItem>()

        // Overdue Deadlines
        overdueDeadlineProjects.forEach { proj ->
            val unDoneCount = clips.count { it.projectId == proj.id && it.isDone == 0 }
            if (unDoneCount > 0) {
                list.add(
                    UrgentAlertItem(
                        id = "deadline_${proj.id}",
                        title = "موعد تحویل گذشته: ${proj.name}",
                        description = "${PersianUtils.faNum(unDoneCount)} کلیپ ناتمام از تاریخ موعد عبور کرده است.",
                        actionText = "ادامه تدوین",
                        target = SmartActionTarget.Timer(
                            proj.id,
                            clips.firstOrNull { it.projectId == proj.id && it.isDone == 0 }?.clipName ?: "کلیپ اصلی"
                        ),
                        isDanger = true
                    )
                )
            }
        }

        // Imminent Deadlines
        imminentDeadlineProjects.forEach { proj ->
            val unDoneCount = clips.count { it.projectId == proj.id && it.isDone == 0 }
            val daysLeft = calculateDaysRemaining(proj.deadlineDate) ?: 0
            if (unDoneCount > 0) {
                list.add(
                    UrgentAlertItem(
                        id = "imminent_${proj.id}",
                        title = "نزدیک به موعد: ${proj.name}",
                        description = "تنها ${PersianUtils.faNum(daysLeft)} روز با ${PersianUtils.faNum(unDoneCount)} کلیپ باقی‌مانده فرصت دارید.",
                        actionText = "ادامه تدوین",
                        target = SmartActionTarget.Timer(
                            proj.id,
                            clips.firstOrNull { it.projectId == proj.id && it.isDone == 0 }?.clipName ?: "کلیپ اصلی"
                        ),
                        isDanger = false
                    )
                )
            }
        }

        // Unapplied Revisions
        projectsWithUnappliedRevisions.forEach { (proj, count) ->
            list.add(
                UrgentAlertItem(
                    id = "rev_${proj.id}",
                    title = "اصلاحیه ثبت‌شده: ${proj.name}",
                    description = "${PersianUtils.faNum(count)} اصلاحیه باز نیازمند اعمال در تایم‌لاین است.",
                    actionText = "مشاهده اصلاحات",
                    target = SmartActionTarget.WorkspaceProject(proj.id),
                    isDanger = false
                )
            )
        }

        // Overdue Claims
        if (overdueProjects.isNotEmpty()) {
            list.add(
                UrgentAlertItem(
                    id = "overdue_claims",
                    title = "مطالبات معوقه تسویه‌نشده",
                    description = "${PersianUtils.faNum(overdueProjects.size)} پروژه بیش از ۲۰ روز در انتظار تسویه باقی مانده‌اند.",
                    actionText = "مشاهده مطالبات",
                    target = SmartActionTarget.Finance,
                    isDanger = true
                )
            )
        }

        list
    }

    // ==========================================
    // 4. PROJECTS NEEDING ATTENTION
    // ==========================================
    val attentionProjects = remember(activeProjects, clips, revisions) {
        val list = mutableListOf<ProjectAttentionItem>()

        activeProjects.forEach { proj ->
            val projClips = clips.filter { it.projectId == proj.id }
            val unDoneCount = projClips.count { it.isDone == 0 }
            val unappliedRevCount = revisions.count { it.projectId == proj.id && it.isApplied == 0 }

            if (unappliedRevCount > 0) {
                list.add(
                    ProjectAttentionItem(
                        project = proj,
                        description = "${PersianUtils.faNum(unappliedRevCount)} اصلاحیه باقی‌مانده",
                        actionText = "اصلاحات",
                        badgeText = "اصلاحیه",
                        badgeColor = WarningAmber,
                        target = SmartActionTarget.WorkspaceProject(proj.id)
                    )
                )
            } else if (projClips.isNotEmpty() && unDoneCount == 0) {
                list.add(
                    ProjectAttentionItem(
                        project = proj,
                        description = "همه کلیپ‌ها تکمیل شده • آماده تحویل",
                        actionText = "تحویل",
                        badgeText = "آماده تحویل",
                        badgeColor = SuccessGreen,
                        target = SmartActionTarget.WorkspaceProject(proj.id)
                    )
                )
            } else if (unDoneCount > 0) {
                val remainingDays = calculateDaysRemaining(proj.deadlineDate)
                val desc = if (remainingDays != null && remainingDays < 0) {
                    "${PersianUtils.faNum(unDoneCount)} کلیپ • موعد گذشته"
                } else if (remainingDays != null) {
                    "${PersianUtils.faNum(unDoneCount)} کلیپ باقی‌مانده (${PersianUtils.faNum(remainingDays)} روز تا تحویل)"
                } else {
                    "${PersianUtils.faNum(unDoneCount)} کلیپ باقی‌مانده"
                }
                list.add(
                    ProjectAttentionItem(
                        project = proj,
                        description = desc,
                        actionText = "ادامه",
                        badgeText = if (remainingDays != null && remainingDays <= 2) "فوری" else "در حال تدوین",
                        badgeColor = if (remainingDays != null && remainingDays <= 2) ErrorRed else MediaAccentCyan,
                        target = SmartActionTarget.Timer(
                            proj.id,
                            projClips.firstOrNull { it.isDone == 0 }?.clipName ?: "کلیپ اصلی"
                        )
                    )
                )
            }
        }
        list.take(4)
    }

    // Jalali Date & Day Name for Header
    val (headerDateText, headerDayOfWeek) = remember {
        formatCurrentJalaliDateAndDay()
    }

    // Helper to execute SmartActionTarget
    fun handleActionTarget(target: SmartActionTarget) {
        when (target) {
            is SmartActionTarget.Timer -> {
                if (target.projectId != null) {
                    viewModel.setTimerTarget(target.projectId, target.clipName ?: "کلیپ اصلی")
                    viewModel.selectProject(target.projectId)
                }
                viewModel.setSelectedTab(3)
            }
            is SmartActionTarget.WorkspaceProject -> {
                viewModel.selectProject(target.projectId)
                viewModel.setSelectedTab(0)
            }
            SmartActionTarget.Finance -> {
                viewModel.setSelectedTab(2)
            }
            SmartActionTarget.Workspace -> {
                viewModel.setSelectedTab(0)
            }
        }
    }

    // ==========================================
    // UI LAYOUT
    // ==========================================
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 760.dp)
            ) {
                // ==========================================
                // 1. SMART HEADER
                // ==========================================
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(PrimaryPurple.copy(alpha = 0.15f))
                                .border(1.dp, PrimaryPurple.copy(alpha = 0.35f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "دستیار هوشمند تدوین",
                                tint = PrimaryPurple,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "دستیار هوشمند",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    fontSize = 19.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = PrimaryPurple.copy(alpha = 0.18f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryPurple.copy(alpha = 0.3f))
                                ) {
                                    Text(
                                        text = "دستیار تدوین",
                                        color = PrimaryPurple,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "امروز • $headerDayOfWeek $headerDateText",
                                color = TextMuted,
                                fontSize = 11.5.sp
                            )
                        }
                    }
                }

                // ==========================================
                // 1.5. REDESIGNED WORK TIME STATUS CARD
                // ==========================================
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = DarkCard,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (todayTotalSecs >= dailyQuotaSecs && dailyQuotaSecs > 0) SuccessGreen.copy(alpha = 0.45f)
                        else PrimaryPurple.copy(alpha = 0.35f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (todayTotalSecs >= dailyQuotaSecs && dailyQuotaSecs > 0) SuccessGreen.copy(alpha = 0.15f)
                                        else PrimaryPurple.copy(alpha = 0.15f)
                                    )
                                    .border(
                                        1.dp,
                                        if (todayTotalSecs >= dailyQuotaSecs && dailyQuotaSecs > 0) SuccessGreen.copy(alpha = 0.35f)
                                        else PrimaryPurple.copy(alpha = 0.35f),
                                        RoundedCornerShape(10.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (todayTotalSecs >= dailyQuotaSecs && dailyQuotaSecs > 0) Icons.Default.CheckCircle else Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = if (todayTotalSecs >= dailyQuotaSecs && dailyQuotaSecs > 0) SuccessGreen else PrimaryPurple,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "مجموع زمان تدوین امروز:",
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                val hrs = todayTotalSecs / 3600
                                val mins = (todayTotalSecs % 3600) / 60
                                val fullTimeText = when {
                                    hrs > 0 && mins > 0 -> "${PersianUtils.faNum(hrs)} ساعت و ${PersianUtils.faNum(mins)} دقیقه"
                                    hrs > 0 -> "${PersianUtils.faNum(hrs)} ساعت کامل"
                                    mins > 0 -> "${PersianUtils.faNum(mins)} دقیقه"
                                    todayTotalSecs > 0 -> "${PersianUtils.faNum(todayTotalSecs)} ثانیه"
                                    else -> "۰ ساعت و ۰ دقیقه"
                                }
                                Text(
                                    text = fullTimeText,
                                    color = if (todayTotalSecs >= dailyQuotaSecs && dailyQuotaSecs > 0) SuccessGreen else TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (todayTotalSecs >= dailyQuotaSecs && dailyQuotaSecs > 0) SuccessGreen.copy(alpha = 0.15f) else DarkSurface,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (todayTotalSecs >= dailyQuotaSecs && dailyQuotaSecs > 0) SuccessGreen.copy(alpha = 0.4f) else BorderDark
                            )
                        ) {
                            Text(
                                text = if (todayTotalSecs >= dailyQuotaSecs && dailyQuotaSecs > 0) {
                                    "هدف روزانه تکمیل شد ✓"
                                } else if (todayTotalSecs > 0) {
                                    "در حال ثبت روزانه"
                                } else {
                                    "بدون کارکرد امروز"
                                },
                                color = if (todayTotalSecs >= dailyQuotaSecs && dailyQuotaSecs > 0) SuccessGreen else MediaAccentCyan,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // ==========================================
                // 2. CUTTERPILOT SMART RECOMMENDATION (HERO)
                // ==========================================
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryPurple.copy(alpha = 0.45f))
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = PrimaryPurple,
                                    modifier = Modifier.size(17.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = recommendation.title,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryPurple,
                                    fontSize = 13.5.sp
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = recommendation.badgeColor.copy(alpha = 0.15f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, recommendation.badgeColor.copy(alpha = 0.35f))
                            ) {
                                Text(
                                    text = recommendation.badgeText,
                                    color = recommendation.badgeColor,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = recommendation.description,
                            color = TextPrimary,
                            fontSize = 13.sp,
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = { handleActionTarget(recommendation.target) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = recommendation.actionText,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                // ==========================================
                // 3. TODAY'S FOCUS GOAL
                // ==========================================
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.HourglassTop,
                                    contentDescription = null,
                                    tint = MediaAccentCyan,
                                    modifier = Modifier.size(17.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "تحقق هدف تمرکز امروز",
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    fontSize = 13.5.sp
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${PersianUtils.faNum(PersianUtils.formatSecondsToHMS(todayTotalSecs))} / ${PersianUtils.faNum(PersianUtils.formatSecondsToHMS(dailyQuotaSecs))}",
                                    color = TextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                IconButton(
                                    onClick = { showEditQuotaDialog = true },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Tune,
                                        contentDescription = "تغییر هدف",
                                        tint = TextMuted,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        LinearProgressIndicator(
                            progress = { quotaPct },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(7.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (quotaPct >= 1f) SuccessGreen else PrimaryPurple,
                            trackColor = DarkSurface
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "پیشرفت: ${PersianUtils.faNum((quotaPct * 100).toInt())}٪",
                                color = if (quotaPct >= 1f) SuccessGreen else PrimaryPurple,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )

                            Text(
                                text = if (todayTotalSecs >= dailyQuotaSecs && dailyQuotaSecs > 0) {
                                    "هدف امروز تکمیل شد ✓"
                                } else {
                                    "${PersianUtils.faNum(PersianUtils.formatSecondsToHMS(dailyQuotaSecs - todayTotalSecs))} تا هدف"
                                },
                                color = TextMuted,
                                fontSize = 11.5.sp
                            )
                        }

                        // Live Active Timer Indicator if timer is currently running or has unsaved session
                        if (isTimerRunning || activeSessionSeconds > 0) {
                            Spacer(modifier = Modifier.height(10.dp))
                            val runningProj = projects.firstOrNull { it.id == timerSelectedProjectId }
                            val clipTitle = timerSelectedClip ?: "کلیپ اصلی"
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = DarkSurface,
                                border = androidx.compose.foundation.BorderStroke(1.dp, MediaAccentCyan.copy(alpha = 0.35f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.setSelectedTab(3) }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(if (isTimerRunning) SuccessGreen else WarningAmber)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (isTimerRunning) "⏱ در حال تدوین:" else "⏸ تایمر متوقف:",
                                            color = MediaAccentCyan,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "${runningProj?.name ?: "پروژه آزاد"} • $clipTitle",
                                            color = TextPrimary,
                                            fontSize = 11.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Text(
                                        text = PersianUtils.faNum(PersianUtils.formatSecondsToHMS(activeSessionSeconds)),
                                        color = MediaAccentCyan,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // ==========================================
                // 4. TODAY'S PERFORMANCE SUMMARY (COMPACT GRID)
                // ==========================================
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Assessment,
                                contentDescription = null,
                                tint = MediaAccentCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "خلاصه عملکرد امروز",
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontSize = 13.5.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            SummaryMetricCell(
                                title = "زمان تدوین",
                                value = PersianUtils.faNum(PersianUtils.formatSecondsToHMS(todayTotalSecs)),
                                valueColor = MediaAccentCyan,
                                modifier = Modifier.weight(1f)
                            )
                            Divider(
                                color = BorderDark,
                                modifier = Modifier
                                    .height(36.dp)
                                    .width(1.dp)
                                    .align(Alignment.CenterVertically)
                            )
                            SummaryMetricCell(
                                title = "پروژه‌های فعال",
                                value = PersianUtils.faNum(activeProjects.size),
                                valueColor = TextPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            Divider(
                                color = BorderDark,
                                modifier = Modifier
                                    .height(36.dp)
                                    .width(1.dp)
                                    .align(Alignment.CenterVertically)
                            )
                            SummaryMetricCell(
                                title = "کلیپ‌های آماده",
                                value = PersianUtils.faNum(clips.count { it.isDone == 1 }),
                                valueColor = SuccessGreen,
                                modifier = Modifier.weight(1f)
                            )
                            Divider(
                                color = BorderDark,
                                modifier = Modifier
                                    .height(36.dp)
                                    .width(1.dp)
                                    .align(Alignment.CenterVertically)
                            )
                            SummaryMetricCell(
                                title = "جلسات تدوین",
                                value = PersianUtils.faNum(todaySessions.size),
                                valueColor = PrimaryPurple,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // ==========================================
                // 5. URGENT ALERTS (CONTEXT-AWARE)
                // ==========================================
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (urgentAlerts.isNotEmpty()) "هشدارهای مهم (${PersianUtils.faNum(urgentAlerts.size)} مورد):" else "وضعیت هشدارها:",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 13.5.sp
                    )
                    if (urgentAlerts.size > 2) {
                        TextButton(
                            onClick = { showAlertsDialog = true },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("مشاهده همه", color = PrimaryPurple, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                if (urgentAlerts.isEmpty()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 14.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = DarkCard,
                        border = androidx.compose.foundation.BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.35f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = SuccessGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("🟢 بدون هشدار فوری", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 12.5.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("همه موعدهای تحویل، اصلاحیه‌ها و تسویه‌ها در وضعیت مطلوب هستند.", color = TextSecondary, fontSize = 11.sp)
                            }
                        }
                    }
                } else {
                    urgentAlerts.take(2).forEach { alert ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = DarkCard,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (alert.isDanger) ErrorRed.copy(alpha = 0.45f) else WarningAmber.copy(alpha = 0.45f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = if (alert.isDanger) ErrorRed else WarningAmber,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = alert.title,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary,
                                            fontSize = 12.5.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = alert.description,
                                            color = TextSecondary,
                                            fontSize = 11.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = { handleActionTarget(alert.target) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (alert.isDanger) ErrorRed.copy(alpha = 0.2f) else WarningAmber.copy(alpha = 0.2f),
                                        contentColor = if (alert.isDanger) ErrorRed else WarningAmber
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text(alert.actionText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                // ==========================================
                // 6. EDITOR EFFICIENCY COACH
                // ==========================================
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f, fill = false)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Speed,
                                    contentDescription = null,
                                    tint = MediaAccentCyan,
                                    modifier = Modifier.size(17.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "تحلیل عملکرد تدوینگر",
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    fontSize = 13.5.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MediaAccentCyan.copy(alpha = 0.12f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MediaAccentCyan.copy(alpha = 0.35f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "شاخص استمرار: ",
                                        color = MediaAccentCyan,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "${PersianUtils.faNum(focusScore)}٪",
                                        color = MediaAccentCyan,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (sessions.isEmpty()) {
                            Text(
                                text = "داده کافی برای تحلیل عملکرد وجود ندارد. با ثبت جلسات تدوین در بخش پایش زمان، شاخص‌های ارگونومی و راندمان شما آنالیز خواهند شد.",
                                color = TextMuted,
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            )
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "• میانگین مدت هر جلسه کاری:",
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "${PersianUtils.faNum(avgSessionMinutes)} دقیقه",
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "• ساعات اوج تمرکز تدوین:",
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = peakTimeShift,
                                    color = MediaAccentCyan,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "• پیشنهاد دستیار هوشمند: بعد از هر ۴۵ الی ۵۰ دقیقه کار پیوسته روی تایم‌لاین، استراحت ۵ دقیقه‌ای به حفظ تمرکز کات‌ها و رفع خستگی چشم کمک می‌کند.",
                                color = TextMuted,
                                fontSize = 11.5.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }

                // ==========================================
                // 7. 7-DAY PERFORMANCE TIMELINE (BAR CHART)
                // ==========================================
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.TrendingUp,
                                    contentDescription = null,
                                    tint = PrimaryPurple,
                                    modifier = Modifier.size(17.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "عملکرد ۷ روز اخیر (شنبه تا جمعه)",
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    fontSize = 13.5.sp
                                )
                            }
                            Text(
                                text = "میانگین روزانه: ${PersianUtils.faNum(PersianUtils.formatSecondsToHMS(avgDailySecs))}",
                                color = TextMuted,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        val maxSecsInWeek = remember(last7DaysRecords) {
                            (last7DaysRecords.maxOfOrNull { it.totalSeconds } ?: 1L).coerceAtLeast(3600L)
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            last7DaysRecords.forEach { record ->
                                val heightRatio = (record.totalSeconds.toFloat() / maxSecsInWeek.toFloat()).coerceIn(0.06f, 1f)
                                val durationText = if (record.totalSeconds > 0) {
                                    val hrs = record.totalSeconds / 3600
                                    val mins = (record.totalSeconds % 3600) / 60
                                    "${PersianUtils.faNum(hrs)}:${PersianUtils.faNum(String.format(Locale.US, "%02d", mins))}"
                                } else "۰"

                                Column(
                                    modifier = Modifier.weight(1f),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Bottom
                                ) {
                                    Text(
                                        text = durationText,
                                        color = if (record.isToday) MediaAccentCyan else TextMuted,
                                        fontSize = 9.5.sp,
                                        fontWeight = if (record.isToday) FontWeight.Bold else FontWeight.Normal
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .width(16.dp)
                                            .height((70 * heightRatio).dp)
                                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                            .background(
                                                if (record.isToday) PrimaryPurple
                                                else if (record.totalSeconds > 0) BorderDark.copy(alpha = 0.9f)
                                                else DarkSurface
                                            )
                                            .border(
                                                1.dp,
                                                if (record.isToday) PrimaryPurple.copy(alpha = 0.8f) else BorderDark,
                                                RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                            )
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = record.dayName,
                                        color = if (record.isToday) TextPrimary else TextSecondary,
                                        fontSize = 10.5.sp,
                                        fontWeight = if (record.isToday) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Divider(color = BorderDark, thickness = 1.dp)
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "مجموع کارکرد هفته (شنبه تا جمعه):",
                                color = TextMuted,
                                fontSize = 11.5.sp
                            )
                            Text(
                                text = PersianUtils.faNum(PersianUtils.formatSecondsToHMS(weekTotalSecs)),
                                color = MediaAccentCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // ==========================================
                // 8. PROJECTS NEEDING ATTENTION
                // ==========================================
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "پروژه‌های نیازمند توجه:",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 13.5.sp
                    )
                    TextButton(
                        onClick = { viewModel.setSelectedTab(0) },
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("مشاهده پیشخوان", color = PrimaryPurple, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                if (attentionProjects.isEmpty()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 14.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = DarkCard,
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
                    ) {
                        Text(
                            text = "همه پروژه‌های فعال در وضعیت مطلوب هستند و پروژه‌ای با تاخیر یا اصلاحیه باز وجود ندارد.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                } else {
                    attentionProjects.forEach { item ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = DarkCard,
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = item.project.name,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary,
                                            fontSize = 13.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = item.badgeColor.copy(alpha = 0.15f),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, item.badgeColor.copy(alpha = 0.3f))
                                        ) {
                                            Text(
                                                text = item.badgeText,
                                                color = item.badgeColor,
                                                fontSize = 9.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${PersianUtils.formatStudioName(item.project.studioName)} • ${item.description}",
                                        color = TextSecondary,
                                        fontSize = 11.5.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Button(
                                    onClick = { handleActionTarget(item.target) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = PrimaryPurple.copy(alpha = 0.18f),
                                        contentColor = PrimaryPurple
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text(item.actionText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                // ==========================================
                // 9. OVERDUE CLAIMS (CONTEXT-AWARE)
                // ==========================================
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (overdueProjects.isNotEmpty()) ErrorRed.copy(alpha = 0.4f) else BorderDark
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = Icons.Default.AttachMoney,
                                contentDescription = null,
                                tint = if (overdueProjects.isNotEmpty()) ErrorRed else SuccessGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (overdueProjects.isNotEmpty()) {
                                        "🔴 ${PersianUtils.faNum(overdueProjects.size)} پروژه دارای مطالبه معوق (> ۲۰ روز)"
                                    } else {
                                        "🟢 بدون مطالبه معوقه"
                                    },
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    fontSize = 12.5.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (overdueProjects.isNotEmpty()) {
                                        "ارزش کل: ${PersianUtils.formatCurrencyFa(sumOverdueClaims)}"
                                    } else {
                                        "همه حساب‌ها در بازه زمانی مجاز تسویه شده‌اند."
                                    },
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        if (overdueProjects.isNotEmpty()) {
                            Button(
                                onClick = { viewModel.setSelectedTab(2) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ErrorRed.copy(alpha = 0.2f),
                                    contentColor = ErrorRed
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("مشاهده مطالبات", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // ==========================================
                // 10. ASK CUTTERPILOT (NATURAL QUERY ASSISTANT)
                // ==========================================
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryPurple.copy(alpha = 0.35f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null,
                                tint = PrimaryPurple,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "پرسش از دستیار هوشمند",
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontSize = 13.5.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = userQueryText,
                            onValueChange = { userQueryText = it },
                            placeholder = { Text("مثلاً: امروز چقدر کار کرده‌ام؟", fontSize = 12.sp, color = TextMuted) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(
                                onSearch = {
                                    if (userQueryText.isNotBlank()) {
                                        activeQueryAnswer = answerUserNaturalQuery(
                                            query = userQueryText,
                                            todayTotalSecs = todayTotalSecs,
                                            todaySessionsCount = todaySessions.size,
                                            dailyQuotaHours = dailyQuotaHours,
                                            weekTotalSecs = weekTotalSecs,
                                            avgDailySecs = avgDailySecs,
                                            topProject = topProjectData?.first,
                                            topProjectTime = topProjectData?.second ?: 0L,
                                            overdueDeadlines = overdueDeadlineProjects,
                                            overdueSettlements = overdueProjects,
                                            unappliedRevisionsCount = totalUnappliedRevisions,
                                            recommendationText = recommendation.description
                                        )
                                        focusManager.clearFocus()
                                    }
                                }
                            ),
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        if (userQueryText.isNotBlank()) {
                                            activeQueryAnswer = answerUserNaturalQuery(
                                                query = userQueryText,
                                                todayTotalSecs = todayTotalSecs,
                                                todaySessionsCount = todaySessions.size,
                                                dailyQuotaHours = dailyQuotaHours,
                                                weekTotalSecs = weekTotalSecs,
                                                avgDailySecs = avgDailySecs,
                                                topProject = topProjectData?.first,
                                                topProjectTime = topProjectData?.second ?: 0L,
                                                overdueDeadlines = overdueDeadlineProjects,
                                                overdueSettlements = overdueProjects,
                                                unappliedRevisionsCount = totalUnappliedRevisions,
                                                recommendationText = recommendation.description
                                            )
                                            focusManager.clearFocus()
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Send,
                                        contentDescription = "ارسال پرسش",
                                        tint = if (userQueryText.isNotBlank()) PrimaryPurple else TextMuted,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = DarkInputBg,
                                unfocusedContainerColor = DarkInputBg,
                                focusedBorderColor = PrimaryPurple,
                                unfocusedBorderColor = BorderDark,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Quick Question Chips
                        val quickQuestions = listOf(
                            "امروز چند ساعت تدوین کرده‌ام؟",
                            "این هفته چقدر کار کرده‌ام؟",
                            "کدام پروژه بیشترین زمان را گرفته؟",
                            "کدام پروژه عقب افتاده؟",
                            "چند اصلاحیه باقی مانده؟",
                            "امروز روی چه کاری تمرکز کنم؟"
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            quickQuestions.forEach { q ->
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = DarkSurface,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                                    modifier = Modifier.clickable {
                                        userQueryText = q
                                        activeQueryAnswer = answerUserNaturalQuery(
                                            query = q,
                                            todayTotalSecs = todayTotalSecs,
                                            todaySessionsCount = todaySessions.size,
                                            dailyQuotaHours = dailyQuotaHours,
                                            weekTotalSecs = weekTotalSecs,
                                            avgDailySecs = avgDailySecs,
                                            topProject = topProjectData?.first,
                                            topProjectTime = topProjectData?.second ?: 0L,
                                            overdueDeadlines = overdueDeadlineProjects,
                                            overdueSettlements = overdueProjects,
                                            unappliedRevisionsCount = totalUnappliedRevisions,
                                            recommendationText = recommendation.description
                                        )
                                    }
                                ) {
                                    Text(
                                        text = q,
                                        color = TextSecondary,
                                        fontSize = 10.5.sp,
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }

                        // Answer Box
                        AnimatedVisibility(
                            visible = activeQueryAnswer != null,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                color = DarkSurface,
                                border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryPurple.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.AutoAwesome,
                                                contentDescription = null,
                                                tint = PrimaryPurple,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("پاسخ دستیار هوشمند", color = PrimaryPurple, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                        }
                                        IconButton(
                                            onClick = { activeQueryAnswer = null },
                                            modifier = Modifier.size(20.dp)
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = "بستن", tint = TextMuted, modifier = Modifier.size(14.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = activeQueryAnswer ?: "",
                                        color = TextPrimary,
                                        fontSize = 12.sp,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // ==========================================
                // 11. QUICK ACTIONS
                // ==========================================
                Text(
                    text = "دسترسی‌های سریع:",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    fontSize = 13.5.sp,
                    modifier = Modifier.padding(horizontal = 2.dp, vertical = 4.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickActionButton(
                        icon = Icons.Default.Timer,
                        label = "شروع تدوین",
                        accentColor = MediaAccentCyan,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.setSelectedTab(3) }
                    )
                    QuickActionButton(
                        icon = Icons.Default.Assessment,
                        label = "گزارش جامع",
                        accentColor = PrimaryPurple,
                        modifier = Modifier.weight(1f),
                        onClick = { showQuickReportDialog = true }
                    )
                    QuickActionButton(
                        icon = Icons.Default.AttachMoney,
                        label = "مطالبات",
                        accentColor = SuccessGreen,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.setSelectedTab(2) }
                    )
                    QuickActionButton(
                        icon = Icons.Default.PlayArrow,
                        label = "پیشخوان کار",
                        accentColor = WarningAmber,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.setSelectedTab(0) }
                    )
                }
            }
        }
    }

    // ==========================================
    // DIALOG 1: EDIT DAILY QUOTA
    // ==========================================
    if (showEditQuotaDialog) {
        var tempHours by remember { mutableDoubleStateOf(dailyQuotaHours) }
        AlertDialog(
            onDismissRequest = { showEditQuotaDialog = false },
            title = {
                Text("تنظیم هدف تمرکز روزانه", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            },
            text = {
                Column {
                    Text(
                        "میزان ساعت کارکرد مورد انتظار شما در یک روز کاری:",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listOf(4.0, 6.0, 8.0, 10.0).forEach { h ->
                            Button(
                                onClick = { tempHours = h },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (tempHours == h) PrimaryPurple else DarkSurface,
                                    contentColor = if (tempHours == h) Color.White else TextSecondary
                                ),
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (tempHours == h) PrimaryPurple else BorderDark),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Text("${PersianUtils.faNum(h.toInt())} ساعت", fontSize = 11.5.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val currentConfig = config ?: AppConfigEntity()
                        viewModel.saveConfig(currentConfig.copy(dailyQuotaHours = tempHours))
                        showEditQuotaDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("ذخیره", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditQuotaDialog = false }) {
                    Text("انصراف", color = TextMuted)
                }
            },
            containerColor = DarkCard
        )
    }

    // ==========================================
    // DIALOG 2: COMPREHENSIVE DAILY REPORT
    // ==========================================
    if (showQuickReportDialog) {
        AlertDialog(
            onDismissRequest = { showQuickReportDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Assessment, contentDescription = null, tint = PrimaryPurple)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("گزارش جامع وضعیت تدوینگر", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    ReportRowItem("تاریخ امروز:", "$headerDayOfWeek $headerDateText")
                    ReportRowItem("کارکرد امروز:", PersianUtils.faNum(PersianUtils.formatSecondsToHMS(todayTotalSecs)))
                    ReportRowItem("هدف روزانه:", "${PersianUtils.faNum(dailyQuotaHours.toInt())} ساعت (${PersianUtils.faNum((quotaPct * 100).toInt())}٪ محقق شده)")
                    ReportRowItem("تعداد جلسات امروز:", "${PersianUtils.faNum(todaySessions.size)} جلسه")
                    Divider(color = BorderDark, modifier = Modifier.padding(vertical = 8.dp))
                    ReportRowItem("پروژه‌های فعال:", "${PersianUtils.faNum(activeProjects.size)} پروژه")
                    ReportRowItem("کل کلیپ‌های تکمیل‌شده:", "${PersianUtils.faNum(clips.count { it.isDone == 1 })} از ${PersianUtils.faNum(clips.size)}")
                    ReportRowItem("اصلاحات اعمال‌نشده:", "${PersianUtils.faNum(totalUnappliedRevisions)} مورد")
                    Divider(color = BorderDark, modifier = Modifier.padding(vertical = 8.dp))
                    ReportRowItem("مجموع کارکرد ۷ روز گذشته:", PersianUtils.faNum(PersianUtils.formatSecondsToHMS(weekTotalSecs)))
                    ReportRowItem("میانگین روزانه هفته:", PersianUtils.faNum(PersianUtils.formatSecondsToHMS(avgDailySecs)))
                    ReportRowItem("مطالبات معوق (> ۲۰ روز):", "${PersianUtils.faNum(overdueProjects.size)} پروژه (${PersianUtils.formatCurrencyFa(sumOverdueClaims)})")
                }
            },
            confirmButton = {
                Button(
                    onClick = { showQuickReportDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("متوجه شدم", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = DarkCard
        )
    }

    // ==========================================
    // DIALOG 3: ALL ALERTS LIST
    // ==========================================
    if (showAlertsDialog) {
        AlertDialog(
            onDismissRequest = { showAlertsDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = ErrorRed)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("تمامی هشدارهای نیازمند توجه", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    if (urgentAlerts.isEmpty()) {
                        Text("هیچ هشداری برای نمایش وجود ندارد.", color = TextSecondary, fontSize = 12.sp)
                    } else {
                        urgentAlerts.forEach { alert ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = DarkSurface,
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (alert.isDanger) ErrorRed.copy(alpha = 0.4f) else WarningAmber.copy(alpha = 0.4f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(alert.title, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 12.5.sp)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(alert.description, color = TextSecondary, fontSize = 11.sp)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Button(
                                        onClick = {
                                            showAlertsDialog = false
                                            handleActionTarget(alert.target)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(32.dp)
                                    ) {
                                        Text(alert.actionText, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAlertsDialog = false }) {
                    Text("بستن", color = TextMuted)
                }
            },
            containerColor = DarkCard
        )
    }
}

// ==========================================
// COMPONENT HELPERS
// ==========================================
@Composable
private fun SummaryMetricCell(
    title: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontWeight = FontWeight.Bold,
            color = valueColor,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = title,
            color = TextMuted,
            fontSize = 10.5.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun QuickActionButton(
    icon: ImageVector,
    label: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = DarkCard,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
        modifier = modifier.height(64.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = accentColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                color = TextPrimary,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ReportRowItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = TextSecondary, fontSize = 11.5.sp)
        Text(value, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}

// ==========================================
// NATURAL QUERY & RECOMMENDATION ENGINE
// ==========================================
private fun determineSmartRecommendation(
    activeProjects: List<ProjectEntity>,
    clips: List<ProjectClipEntity>,
    revisions: List<ProjectRevisionEntity>,
    overdueProjects: List<ProjectEntity>,
    sumOverdue: Double,
    todayTotalSecs: Long,
    dailyQuotaSecs: Long
): SmartRecommendation {
    // 1. Check projects with past deadline and unfinished clips
    val overdueDeadlineProj = activeProjects.firstOrNull { proj ->
        val remainingDays = calculateDaysRemaining(proj.deadlineDate)
        val unDone = clips.count { it.projectId == proj.id && it.isDone == 0 }
        remainingDays != null && remainingDays < 0 && unDone > 0
    }
    if (overdueDeadlineProj != null) {
        val unDoneCount = clips.count { it.projectId == overdueDeadlineProj.id && it.isDone == 0 }
        val targetClip = clips.firstOrNull { it.projectId == overdueDeadlineProj.id && it.isDone == 0 }?.clipName ?: "کلیپ اصلی"
        return SmartRecommendation(
            title = "✦ پیشنهاد دستیار هوشمند",
            description = "موعد تحویل پروژه «${overdueDeadlineProj.name}» سپری شده و ${PersianUtils.faNum(unDoneCount)} کلیپ ناتمام باقی مانده است. پیشنهاد می‌شود اولویت فوری امروز را به این پروژه اختصاص دهید.",
            actionText = "ادامه تدوین",
            badgeText = "ددلاین گذشته",
            badgeColor = ErrorRed,
            target = SmartActionTarget.Timer(overdueDeadlineProj.id, targetClip)
        )
    }

    // 2. Check projects with imminent deadline (<= 2 days)
    val imminentProj = activeProjects.firstOrNull { proj ->
        val remainingDays = calculateDaysRemaining(proj.deadlineDate)
        val unDone = clips.count { it.projectId == proj.id && it.isDone == 0 }
        remainingDays != null && remainingDays in 0..2 && unDone > 0
    }
    if (imminentProj != null) {
        val unDoneCount = clips.count { it.projectId == imminentProj.id && it.isDone == 0 }
        val remainingDays = calculateDaysRemaining(imminentProj.deadlineDate) ?: 1
        val targetClip = clips.firstOrNull { it.projectId == imminentProj.id && it.isDone == 0 }?.clipName ?: "کلیپ اصلی"
        return SmartRecommendation(
            title = "✦ پیشنهاد دستیار هوشمند",
            description = "${PersianUtils.faNum(unDoneCount)} کلیپ از پروژه «${imminentProj.name}» باقی مانده و فقط ${PersianUtils.faNum(remainingDays)} روز تا موعد تحویل فرصت دارید.",
            actionText = "ادامه تدوین",
            badgeText = "موعد نزدیک",
            badgeColor = WarningAmber,
            target = SmartActionTarget.Timer(imminentProj.id, targetClip)
        )
    }

    // 3. Check projects with unapplied revisions
    val projWithRev = activeProjects.firstOrNull { proj ->
        revisions.any { it.projectId == proj.id && it.isApplied == 0 }
    }
    if (projWithRev != null) {
        val revCount = revisions.count { it.projectId == projWithRev.id && it.isApplied == 0 }
        return SmartRecommendation(
            title = "✦ پیشنهاد دستیار هوشمند",
            description = "${PersianUtils.faNum(revCount)} اصلاحیه اعمال‌نشده برای پروژه «${projWithRev.name}» ثبت شده است که نیازمند بازبینی و تحویل است.",
            actionText = "مشاهده اصلاحات",
            badgeText = "اصلاحات باز",
            badgeColor = WarningAmber,
            target = SmartActionTarget.WorkspaceProject(projWithRev.id)
        )
    }

    // 4. Check project ready for final delivery (all clips done, status != COMPLETED)
    val readyProj = activeProjects.firstOrNull { proj ->
        val projClips = clips.filter { it.projectId == proj.id }
        projClips.isNotEmpty() && projClips.all { it.isDone == 1 }
    }
    if (readyProj != null) {
        return SmartRecommendation(
            title = "✦ پیشنهاد دستیار هوشمند",
            description = "تمام کلیپ‌های پروژه «${readyProj.name}» تکمیل شده است. این پروژه آماده خروجی نهایی، تحویل به کارفرما و تسویه حساب است.",
            actionText = "مدیریت تحویل",
            badgeText = "آماده تحویل",
            badgeColor = SuccessGreen,
            target = SmartActionTarget.WorkspaceProject(readyProj.id)
        )
    }

    // 5. Overdue claim check
    if (overdueProjects.isNotEmpty()) {
        return SmartRecommendation(
            title = "✦ پیشنهاد دستیار هوشمند",
            description = "${PersianUtils.faNum(overdueProjects.size)} پروژه با بیش از ۲۰ روز تاخیر در تسویه به ارزش ${PersianUtils.formatCurrencyFa(sumOverdue)} شناسایی شد. پیگیری وصول مطالبات را مد نظر قرار دهید.",
            actionText = "مشاهده مطالبات",
            badgeText = "مطالبه معوق",
            badgeColor = ErrorRed,
            target = SmartActionTarget.Finance
        )
    }

    // 6. Focus quota check
    if (todayTotalSecs < dailyQuotaSecs && activeProjects.isNotEmpty()) {
        val diffSecs = dailyQuotaSecs - todayTotalSecs
        val firstProj = activeProjects.first()
        val targetClip = clips.firstOrNull { it.projectId == firstProj.id && it.isDone == 0 }?.clipName ?: "کلیپ اصلی"
        return SmartRecommendation(
            title = "✦ پیشنهاد دستیار هوشمند",
            description = "${PersianUtils.faNum(PersianUtils.formatSecondsToHMS(diffSecs))} تا تحقق کامل سهمیه تمرکز امروز باقی است. برای حفظ ریتم کاری، یک جلسه تدوین روی «${firstProj.name}» پیشنهاد می‌شود.",
            actionText = "شروع تمرکز",
            badgeText = "هدف تمرکز",
            badgeColor = PrimaryPurple,
            target = SmartActionTarget.Timer(firstProj.id, targetClip)
        )
    }

    // 7. Default All Clear
    return SmartRecommendation(
        title = "✦ پیشنهاد دستیار هوشمند",
        description = "عالی! تمام پروژه‌ها، ددلاین‌ها و امور مالی روی برنامه هستند. می‌توانید پروژه جدیدی ثبت کنید یا کارکرد آزاد روی تایم‌لاین داشته باشید.",
        actionText = "مشاهده پیشخوان",
        badgeText = "وضعیت عالی",
        badgeColor = SuccessGreen,
        target = SmartActionTarget.Workspace
    )
}

private fun answerUserNaturalQuery(
    query: String,
    todayTotalSecs: Long,
    todaySessionsCount: Int,
    dailyQuotaHours: Double,
    weekTotalSecs: Long,
    avgDailySecs: Long,
    topProject: ProjectEntity?,
    topProjectTime: Long,
    overdueDeadlines: List<ProjectEntity>,
    overdueSettlements: List<ProjectEntity>,
    unappliedRevisionsCount: Int,
    recommendationText: String
): String {
    val q = query.lowercase().trim()

    return when {
        q.contains("امروز") || q.contains("چند ساعت") || q.contains("ساعت کار") -> {
            val quotaPct = ((todayTotalSecs.toFloat() / (dailyQuotaHours * 3600f)) * 100).toInt().coerceIn(0, 100)
            "شما امروز مجموعاً ${PersianUtils.faNum(PersianUtils.formatSecondsToHMS(todayTotalSecs))} در قالب ${PersianUtils.faNum(todaySessionsCount)} جلسه کاری تدوین کرده‌اید که معادل ${PersianUtils.faNum(quotaPct)}٪ از هدف روزانه شما است."
        }
        q.contains("هفته") || q.contains("۷ روز") || q.contains("کل کارکرد") -> {
            "در ۷ روز اخیر، مجموعاً ${PersianUtils.faNum(PersianUtils.formatSecondsToHMS(weekTotalSecs))} تدوین ثبت شده که معادل میانگین روزانه ${PersianUtils.faNum(PersianUtils.formatSecondsToHMS(avgDailySecs))} است."
        }
        q.contains("بیشترین") || q.contains("طولانی") || q.contains("پروژه") && q.contains("زمان") -> {
            if (topProject != null) {
                "پروژه «${topProject.name}» (${PersianUtils.formatStudioName(topProject.studioName)}) با ثبت مجموع ${PersianUtils.faNum(PersianUtils.formatSecondsToHMS(topProjectTime))} بیشترین زمان تدوین را به خود اختصاص داده است."
            } else {
                "هنوز جلسات تدوین ثبت‌شده‌ای برای پروژه‌ها در دیتابیس وجود ندارد."
            }
        }
        q.contains("عقب") || q.contains("ددلاین") || q.contains("تاخیر") -> {
            if (overdueDeadlines.isNotEmpty()) {
                val names = overdueDeadlines.joinToString("، ") { "«${it.name}»" }
                "تعداد ${PersianUtils.faNum(overdueDeadlines.size)} پروژه از موعد تحویل گذشته‌اند: $names. پیشنهاد می‌شود در اولویت قرار گیرند."
            } else {
                "خوشبختانه هیچ پروژه‌ای دچار عقب‌افتادگی موعد تحویل نیست و همه کارها روی برنامه هستند."
            }
        }
        q.contains("اصلاح") || q.contains("بازبینی") -> {
            if (unappliedRevisionsCount > 0) {
                "تعداد ${PersianUtils.faNum(unappliedRevisionsCount)} اصلاحیه اعمال‌نشده برای پروژه‌های فعال ثبت شده است که نیاز به اعمال در تایم‌لاین دارند."
            } else {
                "عالی! هیچ اصلاحیه باز یا انجام‌نشده‌ای برای پروژه‌های فعال وجود ندارد."
            }
        }
        q.contains("طلب") || q.contains("معوق") || q.contains("مالی") || q.contains("حساب") -> {
            if (overdueSettlements.isNotEmpty()) {
                "تعداد ${PersianUtils.faNum(overdueSettlements.size)} پروژه با تاخیر تسویه بیش از ۲۰ روز در تب حسابرسی ثبت شده است."
            } else {
                "تمام مطالبات مالی در بازه زمانی مجاز قرار دارند و طلب معوقه‌ای بالاتر از ۲۰ روز وجود ندارد."
            }
        }
        q.contains("تمرکز") || q.contains("چه کاری") || q.contains("کدام") || q.contains("پیشنهاد") -> {
            "پیشنهاد دستیار هوشمند: $recommendationText"
        }
        else -> {
            "سؤال شما بررسی شد. شما می‌توانید درباره ساعات تدوین امروز و هفته، پروژه‌های دارای بیشترین زمان، اصلاحات باقی‌مانده، مطالبات معوق و اولویت تمرکز سوال بفرمایید."
        }
    }
}

// ==========================================
// DATE & CALENDAR COMPUTATION HELPERS
// ==========================================
private fun calculateDaysRemaining(deadlineDateStr: String?): Int? {
    if (deadlineDateStr.isNullOrBlank()) return null
    val currentStr = PersianUtils.getCurrentJalaliDate()
    val partsCurrent = currentStr.split("/").mapNotNull { PersianUtils.convertFaToEnNum(it).toIntOrNull() }
    val partsTarget = deadlineDateStr.split("/").mapNotNull { PersianUtils.convertFaToEnNum(it).toIntOrNull() }
    if (partsCurrent.size != 3 || partsTarget.size != 3) return null
    val currentDays = partsCurrent[0] * 365 + partsCurrent[1] * 30 + partsCurrent[2]
    val targetDays = partsTarget[0] * 365 + partsTarget[1] * 30 + partsTarget[2]
    return targetDays - currentDays
}

private fun calculateLast7Days(sessions: List<TimerSessionEntity>, todayTotalSecs: Long): List<DayWorkRecord> {
    val list = mutableListOf<DayWorkRecord>()
    val todayJalali = PersianUtils.getCurrentJalaliDate()

    val now = Calendar.getInstance()
    val dayOfWeek = now.get(Calendar.DAY_OF_WEEK)
    val daysSinceSaturday = when (dayOfWeek) {
        Calendar.SATURDAY -> 0
        Calendar.SUNDAY -> 1
        Calendar.MONDAY -> 2
        Calendar.TUESDAY -> 3
        Calendar.WEDNESDAY -> 4
        Calendar.THURSDAY -> 5
        Calendar.FRIDAY -> 6
        else -> 0
    }

    val satCal = Calendar.getInstance()
    satCal.add(Calendar.DAY_OF_YEAR, -daysSinceSaturday)

    for (dayIndex in 0..6) {
        val cal = satCal.clone() as Calendar
        cal.add(Calendar.DAY_OF_YEAR, dayIndex)

        val gy = cal.get(Calendar.YEAR)
        val gm = cal.get(Calendar.MONTH) + 1
        val gd = cal.get(Calendar.DAY_OF_MONTH)
        val (jy, jm, jd) = gregorianToJalali(gy, gm, gd)
        val dateStr = String.format(Locale.US, "%04d/%02d/%02d", jy, jm, jd)

        val isToday = (dateStr == todayJalali)

        val dayName = when (dayIndex) {
            0 -> "شنبه"
            1 -> "۱شنبه"
            2 -> "۲شنبه"
            3 -> "۳شنبه"
            4 -> "۴شنبه"
            5 -> "۵شنبه"
            6 -> "جمعه"
            else -> ""
        }

        val totalSecs = if (isToday) {
            todayTotalSecs
        } else if (dayIndex <= daysSinceSaturday) {
            sessions.filter { it.date == dateStr }.sumOf { it.durationSeconds }
        } else {
            0L
        }

        list.add(
            DayWorkRecord(
                dateStr = dateStr,
                dayName = dayName,
                totalSeconds = totalSecs,
                isToday = isToday
            )
        )
    }
    return list
}

private fun formatCurrentJalaliDateAndDay(): Pair<String, String> {
    val cal = Calendar.getInstance()
    val dayOfWeek = when (cal.get(Calendar.DAY_OF_WEEK)) {
        Calendar.SATURDAY -> "شنبه"
        Calendar.SUNDAY -> "یکشنبه"
        Calendar.MONDAY -> "دوشنبه"
        Calendar.TUESDAY -> "سه‌شنبه"
        Calendar.WEDNESDAY -> "چهارشنبه"
        Calendar.THURSDAY -> "پنج‌شنبه"
        Calendar.FRIDAY -> "جمعه"
        else -> "امروز"
    }

    val jalaliStr = PersianUtils.getCurrentJalaliDate()
    val parts = jalaliStr.split("/").mapNotNull { it.toIntOrNull() }
    val formattedDate = if (parts.size == 3) {
        val monthNames = arrayOf(
            "", "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
            "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
        )
        val mName = monthNames.getOrElse(parts[1]) { "" }
        "${PersianUtils.faNum(parts[2])} $mName ${PersianUtils.faNum(parts[0])}"
    } else {
        PersianUtils.faNum(jalaliStr)
    }

    return Pair(formattedDate, dayOfWeek)
}

private fun gregorianToJalali(gy: Int, gm: Int, gd: Int): Triple<Int, Int, Int> {
    val gDaysInMonth = intArrayOf(0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
    var gy2 = if (gm > 2) gy + 1 else gy
    var days = 355666 + (365 * gy) + ((gy2 + 3) / 4) - ((gy2 + 99) / 100) + ((gy2 + 399) / 400) + gd
    for (i in 0 until gm) {
        days += gDaysInMonth[i]
    }
    if (gm > 2 && (gy % 4 == 0 && gy % 100 != 0 || gy % 400 == 0)) {
        days++
    }
    var jy = -1595 + (33 * (days / 12053))
    days %= 12053
    jy += 4 * (days / 1461)
    days %= 1461
    if (days > 365) {
        jy += ((days - 1) / 365)
        days = (days - 1) % 365
    }
    val jm: Int
    val jd: Int
    if (days < 186) {
        jm = 1 + (days / 31)
        jd = 1 + (days % 31)
    } else {
        jm = 7 + ((days - 186) / 30)
        jd = 1 + ((days - 186) % 30)
    }
    return Triple(jy, jm, jd)
}
