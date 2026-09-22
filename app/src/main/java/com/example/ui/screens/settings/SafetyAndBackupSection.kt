package com.example.ui.screens.settings

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoMode
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.data.entity.AppConfigEntity
import com.example.data.model.BackupMetadata
import com.example.data.model.BackupValidationResult
import com.example.data.model.LocalBackupSnapshot
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
import java.io.File

@Composable
fun SafetyAndBackupSection(
    viewModel: MainViewModel,
    config: AppConfigEntity?
) {
    val context = LocalContext.current
    val localSnapshots by viewModel.localSnapshots.collectAsState()
    val allProjects by viewModel.allProjects.collectAsState()
    val allPayments by viewModel.allPayments.collectAsState()

    var isProcessing by remember { mutableStateOf(false) }
    var operationFeedback by remember { mutableStateOf<String?>(null) }

    // Dialog States
    var showDirectJsonDialog by remember { mutableStateOf(false) }
    var directJsonInputText by remember { mutableStateOf("") }

    var pendingRestoreJson by remember { mutableStateOf<String?>(null) }
    var pendingRestoreValidation by remember { mutableStateOf<BackupValidationResult?>(null) }
    var showRestorePreviewDialog by remember { mutableStateOf(false) }

    var snapshotToDelete by remember { mutableStateOf<LocalBackupSnapshot?>(null) }

    // SAF Export Launcher (Create Document)
    val exportBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            isProcessing = true
            viewModel.backupDataToJson { jsonStr ->
                try {
                    context.contentResolver.openOutputStream(uri)?.use { os ->
                        os.write(jsonStr.toByteArray(Charsets.UTF_8))
                        os.flush()
                    }
                    isProcessing = false
                    operationFeedback = "فایل پشتیبان با موفقیت در حافظه دستگاه ذخیره شد."
                    Toast.makeText(context, "فایل پشتیبان با موفقیت ذخیره شد.", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    isProcessing = false
                    operationFeedback = "خطا در ذخیره فایل پشتیبان: ${e.localizedMessage}"
                    Toast.makeText(context, "خطا در ذخیره فایل", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // SAF Import Launcher (Open Document)
    val importBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val jsonStr = inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                    val validation = viewModel.validateBackupJson(jsonStr)
                    if (validation.isValid && validation.metadata != null) {
                        pendingRestoreJson = jsonStr
                        pendingRestoreValidation = validation
                        showRestorePreviewDialog = true
                    } else {
                        Toast.makeText(
                            context,
                            validation.errorMessage ?: "فرمت فایل پشتیبان نامعتبر است.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "خطا در خواندن فایل انتخابی: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header & Quick Status
        SectionHeaderCard(
            title = "ایمنی، پشتیبان‌گیری و بازیابی داده‌ها",
            subtitle = "مدیریت جامع فایل‌های پشتیبان آفلاین، اسنپ‌شات‌های ایمنی و انتقال امن اطلاعات",
            icon = Icons.Default.Security,
            accentColor = PrimaryPurple
        )

        // 2. Operation Feedback Banner (if any)
        if (operationFeedback != null) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = SuccessGreen.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(operationFeedback!!, color = SuccessGreen, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                    IconButton(
                        onClick = { operationFeedback = null },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        // 3. Database Diagnostics & Auto-Backup Configuration
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = DarkCard,
            border = BorderStroke(1.dp, BorderDark),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Database Header Info (Clean, single-line & responsive)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(SuccessGreen.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Storage, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "پایگاه داده محلی SQLite",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp,
                                maxLines = 1,
                                softWrap = false
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = SuccessGreen.copy(alpha = 0.15f),
                                border = BorderStroke(0.5.dp, SuccessGreen.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = "نسخه ۴",
                                    color = SuccessGreen,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(modifier = Modifier.size(6.dp).background(SuccessGreen, CircleShape))
                            Text(
                                text = "آفلاین و امن • فایل cutterlog_pro.db",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = BorderDark.copy(alpha = 0.5f), thickness = 1.dp)
                Spacer(modifier = Modifier.height(14.dp))

                // Stats Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DbStatChip(label = "کل پروژه‌ها", value = "${allProjects.size}", modifier = Modifier.weight(1f))
                    DbStatChip(label = "اسناد مالی", value = "${allPayments.size}", modifier = Modifier.weight(1f))
                    DbStatChip(
                        label = "آخرین بک‌آپ",
                        value = config?.lastBackupDate ?: "ثبت نشده",
                        modifier = Modifier.weight(1.3f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Auto Backup Switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkSurface)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.AutoMode, contentDescription = null, tint = PrimaryPurple, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("پشتیبان‌گیری خودکار و اسنپ‌شات ایمنی", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            Text("ذخیره خودکار پیش از بازیابی یا تغییرات اساسی", color = TextMuted, fontSize = 10.5.sp)
                        }
                    }

                    Switch(
                        checked = config?.autoBackupOnExit ?: true,
                        onCheckedChange = { isChecked ->
                            val current = config ?: AppConfigEntity()
                            viewModel.saveConfig(current.copy(autoBackupOnExit = isChecked))
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = PrimaryPurple,
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = DarkInputBg
                        ),
                        modifier = Modifier.testTag("switch_auto_backup")
                    )
                }
            }
        }

        // 4. Primary Actions (Export / Import / Snapshot / Share)
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = DarkCard,
            border = BorderStroke(1.dp, BorderDark),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "عملیات فایل‌های پشتیبان (JSON)",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp
                )
                Text(
                    text = "فایل‌های خروجی کاترلاگ با ساختار کامل و استاندارد بدون وابستگی به اینترنت تولید می‌شوند",
                    color = TextMuted,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 2 Primary Action Buttons (Responsive, single-line)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Export Button (SAF)
                    Button(
                        onClick = {
                            val defaultName = "CutterLog_Backup_${PersianUtils.getCurrentJalaliDate().replace('/', '_')}.json"
                            exportBackupLauncher.launch(defaultName)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("btn_export_backup"),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "خروجی بک‌آپ",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Import Button (SAF)
                    Button(
                        onClick = {
                            importBackupLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("btn_import_backup"),
                        colors = ButtonDefaults.buttonColors(containerColor = MediaAccentCyan),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null, tint = DarkSurface, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "بازیابی از فایل",
                            color = DarkSurface,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Secondary Action Row (3 Responsive Quick-Action Tiles)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Quick Internal Snapshot
                    QuickBackupActionTile(
                        icon = Icons.Default.History,
                        iconTint = MediaAccentCyan,
                        title = "اسنپ‌شات سریع",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            isProcessing = true
                            viewModel.createLocalSnapshot(isSafety = false) { snap ->
                                isProcessing = false
                                if (snap != null) {
                                    operationFeedback = "اسنپ‌شات سریع با موفقیت در حافظه برنامه ذخیره شد."
                                    Toast.makeText(context, "اسنپ‌شات ایجاد شد.", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "خطا در ایجاد اسنپ‌شات", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    )

                    // Share Intent
                    QuickBackupActionTile(
                        icon = Icons.Default.Share,
                        iconTint = WarningAmber,
                        title = "اشتراک‌گذاری",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.backupDataToJson { jsonStr ->
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, jsonStr)
                                    type = "application/json"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "اشتراک‌گذاری فایل پشتیبان کاترلاگ"))
                            }
                        }
                    )

                    // Direct JSON Text Paste
                    QuickBackupActionTile(
                        icon = Icons.Default.Layers,
                        iconTint = PrimaryPurple,
                        title = "کد متنی JSON",
                        modifier = Modifier.weight(1f),
                        onClick = { showDirectJsonDialog = true }
                    )
                }
            }
        }

        // 5. Local Backup Snapshots Manager
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = DarkCard,
            border = BorderStroke(1.dp, BorderDark),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.FolderOpen, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("مدیریت نسخه‌های پشتیبان محلی", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                            Text("${localSnapshots.size} نسخه پشتیبان در حافظه نرم‌افزار موجود است", color = TextMuted, fontSize = 10.5.sp)
                        }
                    }

                    IconButton(
                        onClick = { viewModel.loadLocalSnapshots() },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "بروزرسانی", tint = TextMuted, modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (localSnapshots.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(DarkSurface)
                            .padding(vertical = 20.dp, horizontal = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.CloudDone, contentDescription = null, tint = TextMuted, modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("هنوز هیچ اسنپ‌شات محلی ثبت نشده است.", color = TextMuted, fontSize = 11.5.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("با زدن «اسنپ‌شات سریع» یک نسخه آماده نگهداری کنید", color = TextSecondary, fontSize = 10.5.sp)
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        localSnapshots.forEach { snapshot ->
                            LocalSnapshotItemRow(
                                snapshot = snapshot,
                                onRestoreClick = {
                                    try {
                                        val file = File(snapshot.filePath)
                                        if (file.exists()) {
                                            val json = file.readText(Charsets.UTF_8)
                                            val valRes = viewModel.validateBackupJson(json)
                                            if (valRes.isValid && valRes.metadata != null) {
                                                pendingRestoreJson = json
                                                pendingRestoreValidation = valRes
                                                showRestorePreviewDialog = true
                                            } else {
                                                Toast.makeText(context, "فایل پشتیبان معتبر نیست", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "خطا در خواندن فایل", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                onDeleteClick = {
                                    snapshotToDelete = snapshot
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // =============================================================================================
    // RESTORE PREVIEW & CONFIRMATION DIALOG
    // =============================================================================================
    if (showRestorePreviewDialog && pendingRestoreValidation != null && pendingRestoreJson != null) {
        val meta = pendingRestoreValidation!!.metadata!!
        var createSafetyBackupBeforeRestore by remember { mutableStateOf(true) }
        var isRestoring by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { if (!isRestoring) showRestorePreviewDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier.fillMaxWidth(0.85f),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(MediaAccentCyan.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.CloudDownload, contentDescription = null, tint = MediaAccentCyan, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("پیش‌نمایش و تأیید بازیابی فایل پشتیبان", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.5.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = DarkSurface,
                        border = BorderStroke(1.dp, BorderDark),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            MetadataRow(label = "برنامه صادرکننده", value = meta.appName)
                            MetadataRow(label = "نسخه پشتیبان", value = "نسخه دیتابیس ${meta.schemaVersion} (نرم‌افزار ${meta.appVersion})")
                            MetadataRow(label = "تاریخ و زمان ثبت", value = meta.jalaliDate)
                            HorizontalDivider(color = BorderDark.copy(alpha = 0.5f), thickness = 1.dp)
                            MetadataRow(label = "تعداد کل پروژه‌ها", value = "${meta.projectCount} پروژه")
                            MetadataRow(label = "تعداد اسناد واریزی", value = "${meta.paymentCount} سند")
                            MetadataRow(label = "جلسات کاری تایمر", value = "${meta.sessionCount} جلسه")
                            MetadataRow(label = "تعداد استودیوها", value = "${meta.studioCount} استودیو")
                            if (meta.totalContractAmount > 0) {
                                MetadataRow(
                                    label = "مجموع مبالغ قراردادها",
                                    value = PersianUtils.formatCurrencyFa(meta.totalContractAmount)
                                )
                            }
                        }
                    }

                    // Safety Backup Checkbox
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkInputBg)
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = createSafetyBackupBeforeRestore,
                            onCheckedChange = { createSafetyBackupBeforeRestore = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = MediaAccentCyan,
                                checkmarkColor = DarkSurface
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "تهیه خودکار اسنپ‌شات ایمنی از داده‌های فعلی پیش از بازیابی",
                            color = TextPrimary,
                            fontSize = 11.sp
                        )
                    }

                    // Warning Alert
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = WarningAmber.copy(alpha = 0.10f),
                        border = BorderStroke(1.dp, WarningAmber.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "هشدار: با تأیید این عملیات، تمامی اطلاعات فعلی پایگاه داده با محتوای این فایل جایگزین خواهند شد.",
                                color = WarningAmber,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    if (isRestoring) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(color = MediaAccentCyan, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("در حال بازنویسی و بازیابی اطلاعات دیتابیس...", color = MediaAccentCyan, fontSize = 11.5.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isRestoring = true
                        viewModel.restoreDataFromJson(
                            jsonStr = pendingRestoreJson!!,
                            createSafetyBackup = createSafetyBackupBeforeRestore
                        ) { result ->
                            isRestoring = false
                            showRestorePreviewDialog = false
                            pendingRestoreJson = null
                            pendingRestoreValidation = null
                            if (result.success) {
                                operationFeedback = "بازیابی اطلاعات با موفقیت انجام شد: ${result.restoredProjectsCount} پروژه و ${result.restoredPaymentsCount} رکورد مالی بازیابی گردید."
                                Toast.makeText(context, "بازیابی اطلاعات با موفقیت انجام شد.", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "خطا در بازیابی: ${result.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    enabled = !isRestoring,
                    colors = ButtonDefaults.buttonColors(containerColor = MediaAccentCyan),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("تأیید و اجرای قطعی بازیابی", color = DarkSurface, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showRestorePreviewDialog = false
                        pendingRestoreJson = null
                        pendingRestoreValidation = null
                    },
                    enabled = !isRestoring
                ) {
                    Text("انصراف", color = TextMuted)
                }
            },
            containerColor = DarkCard,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // =============================================================================================
    // DIRECT JSON TEXT DIALOG
    // =============================================================================================
    if (showDirectJsonDialog) {
        AlertDialog(
            onDismissRequest = { showDirectJsonDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier.fillMaxWidth(0.85f),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Layers, contentDescription = null, tint = PrimaryPurple, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("وارد کردن مستقیم متن JSON", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            },
            text = {
                Column {
                    Text("محتوای متنی فایل بک‌آپ را در کادر زیر قرار دهید:", color = TextSecondary, fontSize = 11.5.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = directJsonInputText,
                        onValueChange = { directJsonInputText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        shape = RoundedCornerShape(10.dp),
                        placeholder = { Text("{\"schemaVersion\": 2, ...}", color = TextMuted, fontSize = 11.sp) },
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
                        if (directJsonInputText.isNotBlank()) {
                            val validation = viewModel.validateBackupJson(directJsonInputText)
                            if (validation.isValid && validation.metadata != null) {
                                pendingRestoreJson = directJsonInputText
                                pendingRestoreValidation = validation
                                showDirectJsonDialog = false
                                directJsonInputText = ""
                                showRestorePreviewDialog = true
                            } else {
                                Toast.makeText(context, validation.errorMessage ?: "فرمت نامعتبر است", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("بررسی و اعتبارسنجی", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDirectJsonDialog = false }) {
                    Text("انصراف", color = TextMuted)
                }
            },
            containerColor = DarkCard,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // =============================================================================================
    // DELETE SNAPSHOT CONFIRMATION DIALOG
    // =============================================================================================
    if (snapshotToDelete != null) {
        val snap = snapshotToDelete!!
        AlertDialog(
            onDismissRequest = { snapshotToDelete = null },
            properties = DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier.fillMaxWidth(0.80f),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("حذف اسنپ‌شات محلی", color = ErrorRed, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            },
            text = {
                Text(
                    text = "آیا از حذف نسخه پشتیبان «${snap.fileName}» از حافظه برنامه اطمینان دارید؟",
                    color = TextPrimary,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteLocalSnapshot(snap.filePath) {
                            Toast.makeText(context, "اسنپ‌شات حذف شد", Toast.LENGTH_SHORT).show()
                        }
                        snapshotToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("حذف قطعی", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { snapshotToDelete = null }) {
                    Text("انصراف", color = TextMuted)
                }
            },
            containerColor = DarkCard,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

// =============================================================================================
// SUB-COMPONENTS
// =============================================================================================

@Composable
private fun SectionHeaderCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = accentColor.copy(alpha = 0.12f),
            border = BorderStroke(1.dp, accentColor.copy(alpha = 0.3f)),
            modifier = Modifier.size(38.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                fontSize = 14.5.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = TextSecondary,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun DbStatChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = DarkSurface,
        border = BorderStroke(1.dp, BorderDark),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, color = TextMuted, fontSize = 10.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
        }
    }
}

@Composable
private fun LocalSnapshotItemRow(
    snapshot: LocalBackupSnapshot,
    onRestoreClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = DarkSurface,
        border = BorderStroke(1.dp, if (snapshot.isSafetySnapshot) MediaAccentCyan.copy(alpha = 0.3f) else BorderDark),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (snapshot.isSafetySnapshot) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MediaAccentCyan.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "اسنپ‌شات ایمنی",
                                color = MediaAccentCyan,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = snapshot.jalaliDate,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "(${snapshot.formattedSize})",
                        color = TextMuted,
                        fontSize = 10.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                val pCount = snapshot.metadata?.projectCount ?: 0
                val payCount = snapshot.metadata?.paymentCount ?: 0
                val sCount = snapshot.metadata?.sessionCount ?: 0
                Text(
                    text = "$pCount پروژه • $payCount پرداخت • $sCount جلسه کار",
                    color = TextSecondary,
                    fontSize = 10.5.sp
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = onRestoreClick,
                    modifier = Modifier.height(32.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MediaAccentCyan.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("بازیابی", color = MediaAccentCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "حذف", tint = ErrorRed.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun MetadataRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = TextSecondary, fontSize = 11.sp)
        Text(value, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 11.5.sp)
    }
}

@Composable
private fun QuickBackupActionTile(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(10.dp),
        color = DarkSurface,
        border = BorderStroke(1.dp, BorderDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                softWrap = false,
                textAlign = TextAlign.Center
            )
        }
    }
}

