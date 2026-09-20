package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import java.util.Locale
import com.example.data.entity.ProjectEntity
import com.example.data.entity.TimerSessionEntity
import com.example.ui.components.ChipSelectable
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
import com.example.ui.util.PersianNumberVisualTransformation
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber
import com.example.ui.viewmodel.MainViewModel
import com.example.util.PersianUtils

@Composable
fun TimerTab(viewModel: MainViewModel) {
    val isTimerRunning by viewModel.isTimerRunning.collectAsState()
    val activeSessionSeconds by viewModel.activeSessionSeconds.collectAsState()
    val timerTargetSeconds by viewModel.timerTargetSeconds.collectAsState()
    val isGracePeriodActive by viewModel.isGracePeriodActive.collectAsState()
    val gracePeriodRemaining by viewModel.gracePeriodRemaining.collectAsState()
    val appConfig by viewModel.appConfig.collectAsState()

    val projects by viewModel.allProjects.collectAsState()
    val allClips by viewModel.allClips.collectAsState()
    val studios by viewModel.studios.collectAsState()
    val sessions by viewModel.allSessions.collectAsState()
    val workspaceSelectedProjectId by viewModel.selectedProjectId.collectAsState()

    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // 1. Active Projects for Deadlines & Timer Assignment
    val activeProjects = remember(projects) {
        projects.filter { proj -> proj.status != "COMPLETED" }
    }

    // 2. Selection States for Timer Target Attribution (Studio -> Project -> Clip)
    val selectedProjectIdForTimer by viewModel.timerSelectedProjectId.collectAsState()
    val selectedClipName by viewModel.timerSelectedClip.collectAsState()

    // Auto-initialize timer target if not set yet
    LaunchedEffect(projects, activeProjects, workspaceSelectedProjectId) {
        if (selectedProjectIdForTimer == null && projects.isNotEmpty()) {
            val initialProjId = workspaceSelectedProjectId ?: activeProjects.firstOrNull()?.id ?: projects.firstOrNull()?.id
            val clips = if (initialProjId != null) allClips.filter { it.projectId == initialProjId } else emptyList()
            val initialClip = clips.firstOrNull { it.isDone == 0 }?.clipName ?: clips.firstOrNull()?.clipName ?: "کلیپ اصلی"
            viewModel.setTimerTarget(initialProjId, initialClip)
        }
    }

    // Auto-sync with workspace selection if user switched project in Workspace tab and timer is not running
    LaunchedEffect(workspaceSelectedProjectId) {
        if (workspaceSelectedProjectId != null && workspaceSelectedProjectId != selectedProjectIdForTimer) {
            if (!isTimerRunning && activeSessionSeconds == 0L) {
                val clips = allClips.filter { it.projectId == workspaceSelectedProjectId }
                val initialClip = clips.firstOrNull { it.isDone == 0 }?.clipName ?: clips.firstOrNull()?.clipName ?: "کلیپ اصلی"
                viewModel.setTimerTarget(workspaceSelectedProjectId, initialClip)
            }
        }
    }

    val selectedProject = remember(projects, selectedProjectIdForTimer) {
        projects.firstOrNull { it.id == selectedProjectIdForTimer }
    }
    val selectedProjectClips = remember(allClips, selectedProjectIdForTimer) {
        if (selectedProjectIdForTimer == null) emptyList() else allClips.filter { it.projectId == selectedProjectIdForTimer }
    }

    // Accumulated permanent time from Room database for this specific clip
    val savedClipTotalSeconds = remember(sessions, selectedProjectIdForTimer, selectedClipName) {
        sessions.filter {
            it.projectId == selectedProjectIdForTimer && it.clipName == selectedClipName
        }.sumOf { it.durationSeconds }
    }

    // Total display time = permanent saved clip time + active unsaved sitting session
    val displayTotalSeconds = savedClipTotalSeconds + activeSessionSeconds

    // 3. Calculations: Today's Work Time & Daily Quota
    val todayDateStr = PersianUtils.getCurrentJalaliDate()
    val todaySessions = sessions.filter { it.date == todayDateStr }
    val todayTotalSecs = todaySessions.sumOf { it.durationSeconds } + activeSessionSeconds

    val dailyQuotaHours = appConfig?.dailyQuotaHours ?: 8.0
    val dailyQuotaSecs = (dailyQuotaHours * 3600).toLong()
    val quotaPct = (todayTotalSecs.toFloat() / dailyQuotaSecs).coerceIn(0f, 1f)

    // Modal Dialog State for switching project/clip target without cluttering the screen
    var showTargetSelectorDialog by remember { mutableStateOf(false) }

    // Session Management Dialogs
    var sessionToEdit by remember { mutableStateOf<TimerSessionEntity?>(null) }
    var sessionToDelete by remember { mutableStateOf<TimerSessionEntity?>(null) }
    var showSaveTimerDialog by remember { mutableStateOf(false) }
    var showDiscardConfirmDialog by remember { mutableStateOf(false) }
    var markClipAsDoneOnSave by remember { mutableStateOf(false) }
    var timerSaveNote by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // ==========================================
        // 0. HEADER & WORKSTATION STATUS
        // ==========================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "پایش و زمان",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    fontSize = 22.sp
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "مدیریت زمان کارکرد خالص، تمرکز و موعدهای تحویل",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }

            // Live Timer Indicator Chip (Text + Visual Indicator)
            StatusBadge(
                text = if (isTimerRunning) "تایمر در حال ضبط" else "تایمر متوقف",
                backgroundColor = if (isTimerRunning) SuccessGreen else DarkSurface
            )
        }

        // ==========================================
        // LEVEL 1: NOW — ACTIVE TIMER HERO CONSOLE
        // ==========================================
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = if (isTimerRunning) SuccessGreen.copy(alpha = 0.5f) else PrimaryPurple.copy(alpha = 0.35f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1.1 Active Target Context Banner (Project & Clip Attribution)
                Surface(
                    onClick = {
                        if (isTimerRunning) {
                            Toast.makeText(context, "برای تغییر کلیپ هدف، ابتدا تایمر را متوقف کنید", Toast.LENGTH_SHORT).show()
                        } else if (activeSessionSeconds > 0L) {
                            Toast.makeText(context, "ابتدا جلسه کاری ثبت‌نشده را ثبت یا لغو کنید", Toast.LENGTH_SHORT).show()
                        } else {
                            showTargetSelectorDialog = true
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = DarkInputBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (isTimerRunning) SuccessGreen else PrimaryPurple)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = selectedProject?.let { "پروژه: ${it.name}" } ?: "جلسه آزاد (بدون پروژه)",
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${PersianUtils.formatStudioName(selectedProject?.studioName)} • کلیپ: ${selectedClipName ?: "کلیپ اصلی"}",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Switch Target Action Button
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkSurface)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = null,
                                tint = MediaAccentCyan,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "تغییر هدف",
                                color = TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 1.2 Grace Period Warning Alert (if active)
                AnimatedVisibility(
                    visible = isGracePeriodActive,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = WarningAmber.copy(alpha = 0.15f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, WarningAmber)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "مهلت بازگشت به نرم‌افزار تدوین: ${PersianUtils.faNum(gracePeriodRemaining)} ثانیه",
                                color = WarningAmber,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // 1.3 High-Legibility Digital Clock Frame (Seven-Segment Monospaced Display)
                val digitalFontFamily = remember {
                    try {
                        FontFamily(Font(R.font.dseg7_modern_bold, FontWeight.Bold))
                    } catch (e: Exception) {
                        FontFamily.Monospace
                    }
                }

                val timerHours = displayTotalSeconds / 3600
                val timerMinutes = (displayTotalSeconds % 3600) / 60
                val timerSecs = displayTotalSeconds % 60
                val hh = String.format(Locale.US, "%02d", timerHours)
                val mm = String.format(Locale.US, "%02d", timerMinutes)
                val ss = String.format(Locale.US, "%02d", timerSecs)

                val annotatedTimerText = remember(hh, mm, ss) {
                    buildAnnotatedString {
                        // Hours in primary digital display color
                        withStyle(SpanStyle(color = TextPrimary)) {
                            append(hh)
                        }
                        // Colon separator (balanced digital opacity)
                        withStyle(SpanStyle(color = TextPrimary.copy(alpha = 0.65f))) {
                            append(":")
                        }
                        // Minutes in primary digital display color
                        withStyle(SpanStyle(color = TextPrimary)) {
                            append(mm)
                        }
                        // Colon separator (balanced digital opacity)
                        withStyle(SpanStyle(color = TextPrimary.copy(alpha = 0.65f))) {
                            append(":")
                        }
                        // Seconds in dedicated accent color (MediaAccentCyan)
                        withStyle(SpanStyle(color = MediaAccentCyan)) {
                            append(ss)
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(DarkSurface)
                        .border(
                            width = 1.dp,
                            color = if (isTimerRunning) SuccessGreen.copy(alpha = 0.45f) else BorderDark,
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Operational Status Indicator (pinned to TopStart to prevent any center layout shift)
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(start = 14.dp, top = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isTimerRunning) SuccessGreen else TextMuted.copy(alpha = 0.35f))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isTimerRunning) "REC" else "PAUSED",
                            color = if (isTimerRunning) SuccessGreen else TextMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                    }

                    // Format Indicator (pinned to TopEnd)
                    Text(
                        text = "HH:MM:SS",
                        color = TextMuted.copy(alpha = 0.45f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(end = 14.dp, top = 10.dp)
                    )

                    // Main Digital Timer Digits (Total Accumulated Clip Time)
                    Text(
                        text = annotatedTimerText,
                        fontFamily = digitalFontFamily,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Breakdown of Session Time vs Total Clip Time
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = if (activeSessionSeconds > 0) MediaAccentCyan else TextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "جلسه جاری: ${PersianUtils.faNum(PersianUtils.formatSecondsToHMS(activeSessionSeconds))}",
                            color = if (activeSessionSeconds > 0) MediaAccentCyan else TextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.HourglassTop,
                            contentDescription = null,
                            tint = if (savedClipTotalSeconds > 0) SuccessGreen else TextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "کل کلیپ: ${PersianUtils.faNum(PersianUtils.formatSecondsToHMS(displayTotalSeconds))}",
                            color = if (savedClipTotalSeconds > 0) SuccessGreen else TextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // ==========================================
                // LEVEL 2: MAIN OPERATIONAL ACTIONS
                // ==========================================
                // Primary Control Buttons (Start/Pause, Save Session & Discard)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!isTimerRunning) {
                        Button(
                            onClick = { viewModel.startTimer() },
                            modifier = Modifier
                                .weight(1.2f)
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("شروع تایمر", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    } else {
                        Button(
                            onClick = { viewModel.pauseTimer() },
                            modifier = Modifier
                                .weight(1.2f)
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = WarningAmber),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Pause, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("توقف موقت", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }

                    Button(
                        onClick = {
                            if (isTimerRunning) {
                                viewModel.pauseTimer()
                            }
                            showSaveTimerDialog = true
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ثبت جلسه", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    if (activeSessionSeconds > 0 && !isTimerRunning) {
                        IconButton(
                            onClick = { showDiscardConfirmDialog = true },
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkSurface)
                                .border(1.dp, BorderDark, RoundedCornerShape(12.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "لغو جلسه جاری",
                                tint = ErrorRed,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Quick Focus Presets
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "جلسه تمرکز:",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )

                    val presets = listOf("آزاد" to null, "۴۵ دقیقه" to 2700L, "۹۰ دقیقه" to 5400L)
                    presets.forEach { (label, targetSecs) ->
                        val isSelected = timerTargetSeconds == targetSecs
                        Surface(
                            onClick = {
                                if (targetSecs != null) viewModel.startTimer(targetSecs) else viewModel.startTimer()
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) PrimaryPurple.copy(alpha = 0.25f) else DarkInputBg,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) PrimaryPurple else BorderDark
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) PrimaryPurple else TextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // LEVEL 3: IMPORTANT INFORMATION (TODAY'S WORK & DEADLINES)
        // ==========================================
        // 3.1 Daily Work Summary Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MediaAccentCyan.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Schedule, contentDescription = null, tint = MediaAccentCyan, modifier = Modifier.size(17.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "کارکرد کاری امروز",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "هدف روزانه: ${PersianUtils.faNum(dailyQuotaHours.toInt())} ساعت خالص",
                                color = TextSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = PersianUtils.faNum(PersianUtils.formatSecondsToHMS(todayTotalSecs)),
                            color = MediaAccentCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "${PersianUtils.faNum((quotaPct * 100).toInt())}٪ تحقق یافته",
                            color = if (quotaPct >= 1f) SuccessGreen else TextMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LinearProgressIndicator(
                    progress = { quotaPct },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (quotaPct >= 1f) SuccessGreen else PrimaryPurple,
                    trackColor = DarkInputBg
                )
            }
        }

        // 3.2 Deadlines by Urgency (موعدهای تحویل بر اساس فوریت)
        Text(
            text = "موعدهای تحویل پیش‌رو (بر اساس فوریت)",
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            fontSize = 13.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        val projectsWithDeadlines = remember(activeProjects) {
            activeProjects.mapNotNull { proj ->
                val daysLeft = calculateDaysRemaining(proj.deadlineDate)
                if (daysLeft != null) Pair(proj, daysLeft) else null
            }.sortedBy { it.second } // Sort by urgency (overdue / nearest first)
        }

        if (projectsWithDeadlines.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "هیچ موعد تحویل فوری برای پروژه‌های فعال ثبت نشده است",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                projectsWithDeadlines.take(3).forEach { (proj, daysLeft) ->
                    DeadlineItemRow(
                        project = proj,
                        daysLeft = daysLeft,
                        onSelectForTimer = {
                            if (isTimerRunning) {
                                Toast.makeText(context, "برای تغییر هدف، ابتدا تایمر را متوقف کنید", Toast.LENGTH_SHORT).show()
                            } else if (activeSessionSeconds > 0L) {
                                Toast.makeText(context, "لطفاً ابتدا جلسه کاری جاری را ثبت یا لغو کنید", Toast.LENGTH_SHORT).show()
                            } else {
                                val clips = allClips.filter { it.projectId == proj.id }
                                val targetClip = clips.firstOrNull { it.isDone == 0 }?.clipName ?: clips.firstOrNull()?.clipName ?: "کلیپ اصلی"
                                viewModel.setTimerTarget(proj.id, targetClip)
                            }
                        }
                    )
                }
            }
        }

        // ==========================================
        // LEVEL 4: DETAILS & TODAY'S SESSION HISTORY
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
                    text = "سوابق جلسات امروز",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                StatusBadge(
                    text = "${PersianUtils.faNum(todaySessions.size)} جلسه",
                    backgroundColor = DarkSurface
                )
            }

            // Export to CSV Shortcut Button
            IconButton(
                onClick = { exportSessionsToCsv(context, todaySessions) },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.Download,
                    contentDescription = "خروجی سوابق امروز",
                    tint = MediaAccentCyan,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                if (todaySessions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "امروز هنوز هیچ جلسه کاری ثبت نشده است",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        todaySessions.forEach { session ->
                            SessionItemRow(
                                session = session,
                                onEdit = { sessionToEdit = session },
                                onDelete = { sessionToDelete = session }
                            )
                        }
                    }
                }
            }
        }
    }

    // ==========================================
    // DIALOG 1: TARGET SELECTOR (Studio -> Project -> Clip)
    // ==========================================
    if (showTargetSelectorDialog) {
        var tempStudioName by remember {
            mutableStateOf(selectedProject?.studioName ?: studios.firstOrNull()?.name)
        }
        val tempFilteredProjects = remember(projects, tempStudioName) {
            if (tempStudioName == null) projects else projects.filter { it.studioName == tempStudioName }
        }
        var tempProjectId by remember(tempFilteredProjects) {
            mutableStateOf(tempFilteredProjects.firstOrNull()?.id)
        }
        val tempClips = remember(allClips, tempProjectId) {
            if (tempProjectId == null) emptyList() else allClips.filter { it.projectId == tempProjectId }
        }
        var tempClipName by remember(tempClips) {
            mutableStateOf(tempClips.firstOrNull()?.clipName ?: "کلیپ اصلی")
        }

        AlertDialog(
            onDismissRequest = { showTargetSelectorDialog = false },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier.fillMaxWidth(0.80f),
            title = {
                Text(
                    text = "انتخاب هدف تایمر (آتلیه ⬅️ پروژه ⬅️ کلیپ)",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Step 1: Studio
                    Text("۱. انتخاب آتلیه:", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    val studioNames = studios.map { it.name }
                    ResponsiveCapsuleGrid(items = studioNames, maxPerRow = 3) { sName, modifier ->
                        ChipSelectable(
                            text = sName,
                            isSelected = tempStudioName == sName,
                            onSelect = { tempStudioName = sName },
                            modifier = modifier
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Step 2: Project
                    Text("۲. انتخاب پروژه:", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    if (tempFilteredProjects.isEmpty()) {
                        Text("هیچ پروژه‌ای برای این آتلیه یافت نشد", color = TextMuted, fontSize = 11.sp)
                    } else {
                        ResponsiveCapsuleGrid(items = tempFilteredProjects, maxPerRow = 2) { proj, modifier ->
                            ChipSelectable(
                                text = proj.name,
                                isSelected = tempProjectId == proj.id,
                                onSelect = { tempProjectId = proj.id },
                                accentColor = PrimaryPurple,
                                modifier = modifier
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Step 3: Clip
                    Text("۳. انتخاب کلیپ مدنظر:", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    if (tempClips.isEmpty()) {
                        Text("کلیپی برای این پروژه ثبت نشده است", color = TextMuted, fontSize = 11.sp)
                    } else {
                        ResponsiveCapsuleGrid(items = tempClips.map { it.clipName }, maxPerRow = 2) { cName, modifier ->
                            ChipSelectable(
                                text = cName,
                                isSelected = tempClipName == cName,
                                onSelect = { tempClipName = cName },
                                accentColor = MediaAccentCyan,
                                modifier = modifier
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.setTimerTarget(tempProjectId, tempClipName)
                        showTargetSelectorDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("تأیید هدف", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTargetSelectorDialog = false }) {
                    Text("انصراف", color = TextMuted)
                }
            },
            containerColor = DarkCard
        )
    }

    // ==========================================
    // DIALOG 2: SAVE TIMER SESSION
    // ==========================================
    if (showSaveTimerDialog) {
        val clipName = selectedClipName ?: "کلیپ اصلی"
        AlertDialog(
            onDismissRequest = { showSaveTimerDialog = false },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier.fillMaxWidth(0.85f),
            title = { Text("ثبت جلسه کاری", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column {
                    Text("پروژه: ${selectedProject?.name ?: "جلسه آزاد (بدون پروژه)"}", color = TextSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("کلیپ: $clipName", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = DarkSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("مدت این جلسه کاری:", color = TextSecondary, fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = PersianUtils.faNum(PersianUtils.formatSecondsToHMS(activeSessionSeconds)),
                                    color = MediaAccentCyan,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("زمان کل تجمعی پس از ثبت:", color = TextSecondary, fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = PersianUtils.faNum(PersianUtils.formatSecondsToHMS(displayTotalSeconds)),
                                    color = SuccessGreen,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    if (activeSessionSeconds == 0L) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "نکته: تایمر کارکرد جدیدی ثبت نکرده است. در صورت تمایل می‌توانید تیک تحویل کلیپ را ثبت کنید.",
                            color = WarningAmber,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurface)
                            .padding(8.dp)
                    ) {
                        Checkbox(
                            checked = markClipAsDoneOnSave,
                            onCheckedChange = { markClipAsDoneOnSave = it }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "کلیپ تحویل داده شده (۱۰۰٪ تکمیل)",
                            color = SuccessGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = timerSaveNote,
                        onValueChange = { timerSaveNote = it },
                        label = { Text("توضیحات جلسه کارکرد (اختیاری)", fontSize = 12.sp) },
                        placeholder = { Text("مثال: تا دقیقه ۱:۲۰ تدوین شد، اصلاح رنگ باقی مانده.", fontSize = 11.sp, color = TextMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryPurple,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.stopAndSaveTimerSessionWithClipComplete(
                            projectId = selectedProjectIdForTimer,
                            clipName = clipName,
                            category = "تدوین",
                            note = timerSaveNote,
                            isClipCompleted = markClipAsDoneOnSave
                        )
                        showSaveTimerDialog = false
                        markClipAsDoneOnSave = false
                        timerSaveNote = ""
                    },
                    enabled = activeSessionSeconds > 0L || markClipAsDoneOnSave,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = if (activeSessionSeconds > 0L) {
                            "تأیید و ذخیره جلسه (${PersianUtils.faNum(PersianUtils.formatSecondsToHMS(activeSessionSeconds))})"
                        } else {
                            "تأیید و ذخیره تغییرات"
                        },
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveTimerDialog = false }) {
                    Text("انصراف", color = TextMuted)
                }
            },
            containerColor = DarkCard
        )
    }

    // ==========================================
    // DIALOG 2.5: DISCARD ACTIVE SESSION
    // ==========================================
    if (showDiscardConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardConfirmDialog = false },
            title = { Text("لغو کارکرد جلسه جاری", color = ErrorRed, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Text(
                    text = "آیا از لغو زمان ثبت‌نشده این جلسه (${PersianUtils.faNum(PersianUtils.formatSecondsToHMS(activeSessionSeconds))}) اطمینان دارید؟\nزمان‌های جلسات قبلی این کلیپ دست‌نخورده باقی خواهند ماند.",
                    color = TextPrimary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.discardActiveSession()
                        showDiscardConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("بله، لغو شود", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardConfirmDialog = false }) {
                    Text("انصراف", color = TextMuted)
                }
            },
            containerColor = DarkCard
        )
    }

    // ==========================================
    // DIALOG 3: EDIT SESSION
    // ==========================================
    if (sessionToEdit != null) {
        val sess = sessionToEdit!!
        var editClipName by remember { mutableStateOf(sess.clipName) }
        var editCategory by remember { mutableStateOf(sess.category) }
        var editDurationMins by remember { mutableStateOf((sess.durationSeconds / 60).toString()) }
        var editNote by remember { mutableStateOf(sess.note ?: "") }

        AlertDialog(
            onDismissRequest = { sessionToEdit = null },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier.fillMaxWidth(0.80f),
            title = { Text("ویرایش جلسه کارکرد ثبت شده", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column {
                    OutlinedTextField(
                        value = editClipName,
                        onValueChange = { editClipName = it },
                        label = { Text("نام کلیپ / بخش") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editCategory,
                        onValueChange = { editCategory = it },
                        label = { Text("دسته‌بندی (تدوین، رندر، اصلاح نور...)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editDurationMins,
                        onValueChange = { input ->
                            val converted = PersianUtils.convertFaToEnNum(input)
                            editDurationMins = converted.filter { it.isDigit() }
                        },
                        label = { Text("مدت زمان (دقیقه)") },
                        singleLine = true,
                        visualTransformation = PersianNumberVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editNote,
                        onValueChange = { editNote = it },
                        label = { Text("شرح / توضیحات") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val mins = editDurationMins.toLongOrNull() ?: (sess.durationSeconds / 60)
                        val updated = sess.copy(
                            clipName = editClipName,
                            category = editCategory,
                            durationSeconds = mins * 60,
                            note = editNote.ifBlank { null }
                        )
                        viewModel.updateTimerSession(updated)
                        sessionToEdit = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("ذخیره تغییرات", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { sessionToEdit = null }) {
                    Text("انصراف", color = TextMuted)
                }
            },
            containerColor = DarkCard
        )
    }

    // ==========================================
    // DIALOG 4: DELETE SESSION CONFIRMATION
    // ==========================================
    if (sessionToDelete != null) {
        val sess = sessionToDelete!!
        AlertDialog(
            onDismissRequest = { sessionToDelete = null },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier.fillMaxWidth(0.80f),
            title = { Text("حذف جلسه کارکرد", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = { Text("آیا از حذف این جلسه ثبت شده (${sess.clipName}) اطمینان دارید؟", color = TextSecondary, fontSize = 13.sp) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteTimerSession(sess)
                        sessionToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("حذف قطعی", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { sessionToDelete = null }) { Text("انصراف", color = TextMuted) }
            },
            containerColor = DarkCard
        )
    }
}

// ==========================================
// SUB-COMPONENTS FOR TIMER TAB
// ==========================================

@Composable
fun DeadlineItemRow(
    project: ProjectEntity,
    daysLeft: Int,
    onSelectForTimer: () -> Unit
) {
    val (statusLabel, statusColor) = when {
        daysLeft < 0 -> Pair("${PersianUtils.faNum(-daysLeft)} روز گذشته", ErrorRed)
        daysLeft == 0 -> Pair("امروز موعد تحویل است", WarningAmber)
        daysLeft <= 2 -> Pair("${PersianUtils.faNum(daysLeft)} روز مانده (فوری)", WarningAmber)
        else -> Pair("${PersianUtils.faNum(daysLeft)} روز مانده", MediaAccentCyan)
    }

    Surface(
        onClick = onSelectForTimer,
        shape = RoundedCornerShape(12.dp),
        color = DarkCard,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = project.name,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${PersianUtils.formatStudioName(project.studioName)} • تحویل: ${PersianUtils.faNum(project.deadlineDate)}",
                    color = TextSecondary,
                    fontSize = 10.sp
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                StatusBadge(text = statusLabel, backgroundColor = statusColor)
            }
        }
    }
}

@Composable
fun SessionItemRow(
    session: TimerSessionEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = session.clipName,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(DarkInputBg)
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = session.category,
                            color = TextSecondary,
                            fontSize = 9.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "بازه: ${PersianUtils.faNum(session.startTime)} تا ${PersianUtils.faNum(session.endTime)}",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
                if (!session.note.isNullOrBlank()) {
                    Text(
                        text = "شرح: ${session.note}",
                        color = TextMuted,
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = PersianUtils.faNum(PersianUtils.formatSecondsToHMS(session.durationSeconds)),
                    color = MediaAccentCyan,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "ویرایش",
                        tint = PrimaryPurple,
                        modifier = Modifier.size(16.dp)
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "حذف",
                        tint = ErrorRed,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

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

private fun exportSessionsToCsv(context: Context, sessions: List<TimerSessionEntity>) {
    try {
        val sb = StringBuilder()
        sb.append("ID,Clip Name,Category,Start Time,End Time,Duration Secs,Date\n")
        sessions.forEach { s ->
            sb.append("${s.id},\"${s.clipName}\",\"${s.category}\",${s.startTime},${s.endTime},${s.durationSeconds},${s.date}\n")
        }
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, sb.toString())
            type = "text/csv"
        }
        context.startActivity(Intent.createChooser(sendIntent, "خروجی تایمر کاترلاگ"))
    } catch (e: Exception) {
        // Fallback
    }
}
