package com.example.ui.screens

import android.content.Intent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.R
import com.example.data.entity.AppConfigEntity
import com.example.data.entity.DefaultClipEntity
import com.example.data.entity.StudioEntity
import com.example.ui.components.AboutAppDialog
import com.example.ui.components.AboutAppTopButton
import com.example.ui.components.ChipSelectable
import com.example.ui.components.DeveloperSupportCard
import com.example.ui.components.DeveloperSupportDialog
import com.example.ui.components.DeveloperSupportTopBannerButton
import com.example.ui.components.QuickPriceItem
import com.example.ui.components.ResponsiveCapsuleGrid
import com.example.ui.components.parseQuickPrices
import com.example.ui.components.serializeQuickPrices
import com.example.ui.screens.settings.ResetCenterSection
import com.example.ui.screens.settings.SafetyAndBackupSection
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
import org.json.JSONArray
import org.json.JSONObject

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsTab(viewModel: MainViewModel) {
    val config by viewModel.appConfig.collectAsState()
    val studios by viewModel.studios.collectAsState()
    val defaultClips by viewModel.defaultClips.collectAsState()

    var activeSubSection by remember { mutableIntStateOf(0) }

    val scrollState = rememberScrollState()
    val currentConfig = config ?: AppConfigEntity()

    var dailyQuotaHours by remember(config) { mutableFloatStateOf(currentConfig.dailyQuotaHours.toFloat()) }
    var soundAlertsEnabled by remember(config) { mutableStateOf(currentConfig.soundAlertsEnabled) }

    var brandTitle by remember(config) { mutableStateOf(currentConfig.invoiceBrandTitle) }
    var bankCard by remember(config) { mutableStateOf(currentConfig.invoiceBankCard) }
    var bankOwner by remember(config) { mutableStateOf(currentConfig.invoiceBankOwner) }
    var footerNote by remember(config) { mutableStateOf(currentConfig.invoiceFooterNote) }

    // Quick prices management
    var newQuickPriceText by remember { mutableStateOf("") }
    val quickPricesList = remember(config?.quickPricesJson) {
        val list = mutableStateListOf<Double>()
        try {
            val arr = JSONArray(currentConfig.quickPricesJson)
            for (i in 0 until arr.length()) list.add(arr.getDouble(i))
        } catch (e: Exception) {
            list.addAll(listOf(500000.0, 1000000.0, 2000000.0, 3000000.0, 5000000.0, 10000000.0))
        }
        list
    }

    // Packages management
    var newPackageName by remember { mutableStateOf("") }
    var newPackageClips by remember { mutableStateOf("") }
    val packagesList = remember(config?.packagesJson) {
        val list = mutableStateListOf<Pair<String, List<String>>>()
        try {
            val arr = JSONArray(currentConfig.packagesJson)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val pName = obj.getString("name")
                val cArr = obj.getJSONArray("clips")
                val cList = mutableListOf<String>()
                for (j in 0 until cArr.length()) cList.add(cArr.getString(j))
                list.add(Pair(pName, cList))
            }
        } catch (e: Exception) {
            list.addAll(
                listOf(
                    Pair("پکیج کامل عروسی", listOf("سینمایی", "فرمالیته", "میکسر/اینستا", "سرمجلس", "گیفت")),
                    Pair("پکیج نامزدی/تولد", listOf("کلیپ اصلی", "هایلایت", "استوری"))
                )
            )
        }
        list
    }

    var studioToEdit by remember { mutableStateOf<StudioEntity?>(null) }
    var studioToDelete by remember { mutableStateOf<StudioEntity?>(null) }
    var clipToEdit by remember { mutableStateOf<DefaultClipEntity?>(null) }
    var clipToDelete by remember { mutableStateOf<DefaultClipEntity?>(null) }

    var showAboutAppDialog by remember { mutableStateOf(false) }
    var showDeveloperSupportDialog by remember { mutableStateOf(false) }

    val navItems = listOf(
        Pair("عمومی و هدف", Icons.Default.Settings),
        Pair("پکیج‌ها و کلیپ‌ها", Icons.Default.Movie),
        Pair("مالی و برندینگ", Icons.Default.Receipt),
        Pair("ایمنی و بک‌آپ", Icons.Default.Security),
        Pair("مرکز بازنشانی", Icons.Default.Warning),
        Pair("درباره کاترلاگ", Icons.Default.Info)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkSurface),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 760.dp)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 18.dp)
        ) {
            // Header Section
            SettingsHeader()

            Spacer(modifier = Modifier.height(14.dp))

            // Coffee Support Button with warm coffee gradient
            Surface(
                onClick = { showDeveloperSupportDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("support_coffee_button"),
                shape = RoundedCornerShape(14.dp),
                color = Color.Transparent,
                border = BorderStroke(1.5.dp, Color(0xFFFFB74D).copy(alpha = 0.6f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF4E342E), // Rich Coffee Brown
                                    Color(0xFF5D4037), // Warm Medium Coffee
                                    Color(0xFF3E2723)  // Dark Espresso
                                )
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("☕", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "حمایت از توسعه‌دهنده (یک قهوه مهمان کن)",
                            color = Color(0xFFFFECB3),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Section Selector Pills
            ResponsiveCapsuleGrid(
                items = navItems.map { it.first },
                maxPerRow = 2,
                modifier = Modifier.padding(bottom = 16.dp)
            ) { tabName, modifier ->
                val index = navItems.indexOfFirst { it.first == tabName }
                val isSelected = activeSubSection == index
                val itemIcon = navItems.getOrNull(index)?.second ?: Icons.Default.Settings

                Surface(
                    onClick = { activeSubSection = index },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) PrimaryPurple else DarkCard,
                    border = BorderStroke(1.dp, if (isSelected) PrimaryPurple else BorderDark),
                    modifier = modifier.height(44.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Box(
                            modifier = Modifier.width(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = itemIcon,
                                contentDescription = null,
                                tint = if (isSelected) Color.White else TextSecondary,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = tabName,
                            color = if (isSelected) Color.White else TextPrimary,
                            fontSize = 12.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Main Settings Section Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 18.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                border = BorderStroke(1.dp, BorderDark)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    when (activeSubSection) {
                        // ================= 0: GENERAL & GOALS =================
                        0 -> {
                            SectionTitleHeader(
                                title = "تنظیمات عمومی و هدف کاری",
                                subtitle = "تنظیم ساعات هدف تمرکز کاری روزانه و هشدارهای صوتی نرم‌افزار",
                                icon = Icons.Default.Settings
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // Setting 1: Daily Focus Goal
                            SettingGroupCard(
                                title = "هدف تمرکز روزانه",
                                description = "هدف ساعات مفید تدوین روزانه برای پایش در داشبورد و کاترپایلوت",
                                icon = Icons.Default.Timer
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "میزان تعیین‌شده:",
                                        color = TextSecondary,
                                        fontSize = 12.5.sp
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        color = PrimaryPurple.copy(alpha = 0.15f),
                                        border = BorderStroke(1.dp, PrimaryPurple.copy(alpha = 0.4f))
                                    ) {
                                        Text(
                                            text = "${PersianUtils.faNum(dailyQuotaHours.toInt())} ساعت در روز",
                                            color = PrimaryPurple,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.5.sp,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Slider(
                                    value = dailyQuotaHours,
                                    onValueChange = { dailyQuotaHours = it },
                                    valueRange = 2f..14f,
                                    steps = 11,
                                    colors = SliderDefaults.colors(
                                        thumbColor = PrimaryPurple,
                                        activeTrackColor = PrimaryPurple,
                                        inactiveTrackColor = BorderDark
                                    )
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "۲ ساعت (حداقل)", color = TextMuted, fontSize = 10.5.sp)
                                    Text(text = "۱۴ ساعت (حداکثر)", color = TextMuted, fontSize = 10.5.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Setting 2: Sound Alerts Switch
                            SettingGroupCard(
                                title = "پخش صدای هشدار سیستم",
                                description = "پخش آلارم صوتی هنگام پایان زمان‌های تعیین‌شده و رویدادهای مهم",
                                icon = Icons.Default.NotificationsActive
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (soundAlertsEnabled) "هشدارهای صوتی فعال است" else "هشدارهای صوتی خاموش است",
                                            color = if (soundAlertsEnabled) SuccessGreen else TextMuted,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "Sound Alerts",
                                            color = TextMuted,
                                            fontSize = 10.5.sp
                                        )
                                    }
                                    Switch(
                                        checked = soundAlertsEnabled,
                                        onCheckedChange = { soundAlertsEnabled = it },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = SuccessGreen,
                                            uncheckedThumbColor = TextMuted,
                                            uncheckedTrackColor = DarkSurface
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Button(
                                onClick = {
                                    viewModel.saveConfig(
                                        currentConfig.copy(
                                            dailyQuotaHours = dailyQuotaHours.toDouble(),
                                            soundAlertsEnabled = soundAlertsEnabled
                                        )
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("ذخیره تغییرات عمومی", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                            }
                        }

                        // ================= 1: PACKAGES & CLIPS =================
                        1 -> {
                            SectionTitleHeader(
                                title = "مدیریت پکیج‌ها، کلیپ‌ها و قیمت‌های سریع",
                                subtitle = "تعریف بسته‌های آماده تدوین، افزودن و مدیریت استودیوها و عناوین کلیپ‌ها",
                                icon = Icons.Default.Movie
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // 1. Package Management
                            SettingGroupCard(
                                title = "۱. پکیج‌های آماده تدوین",
                                description = "تعریف عناوین پکیج به همراه انتخاب کپسولی کلیپ‌های زیرمجموعه",
                                icon = Icons.Default.Movie
                            ) {
                                val selectedClipsForNewPkg = remember { mutableStateListOf<String>() }

                                OutlinedTextField(
                                    value = newPackageName,
                                    onValueChange = { newPackageName = it },
                                    placeholder = { Text("عنوان پکیج (مثلاً: پکیج فرمالیته VIP)", color = TextMuted, fontSize = 12.sp) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    singleLine = true,
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

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "انتخاب کپسولی کلیپ‌های پکیج:",
                                    color = TextSecondary,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(6.dp))

                                val availableRefClips = defaultClips.map { it.name }.ifEmpty {
                                    listOf("سینمایی", "فرمالیته", "میکسر/اینستا", "سرمجلس", "گیفت", "هایلایت", "استوری")
                                }

                                ResponsiveCapsuleGrid(items = availableRefClips, maxPerRow = 3) { clipName, modifier ->
                                    val isSel = selectedClipsForNewPkg.contains(clipName)
                                    ChipSelectable(
                                        text = clipName,
                                        isSelected = isSel,
                                        onSelect = {
                                            if (isSel) selectedClipsForNewPkg.remove(clipName)
                                            else selectedClipsForNewPkg.add(clipName)
                                        },
                                        accentColor = PrimaryPurple,
                                        modifier = modifier
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Button(
                                    onClick = {
                                        if (newPackageName.isNotBlank() && selectedClipsForNewPkg.isNotEmpty()) {
                                            packagesList.add(Pair(newPackageName, selectedClipsForNewPkg.toList()))
                                            newPackageName = ""
                                            selectedClipsForNewPkg.clear()
                                            // Save
                                            val jsonArr = JSONArray()
                                            packagesList.forEach { pkg ->
                                                val obj = JSONObject()
                                                obj.put("name", pkg.first)
                                                val cArr = JSONArray()
                                                pkg.second.forEach { cArr.put(it) }
                                                obj.put("clips", cArr)
                                                jsonArr.put(obj)
                                            }
                                            viewModel.saveConfig(currentConfig.copy(packagesJson = jsonArr.toString()))
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(42.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(17.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("افزودن پکیج جدید", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                if (packagesList.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    HorizontalDivider(color = BorderDark, thickness = 1.dp)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text("پکیج‌های فعال:", color = TextSecondary, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                                    Spacer(modifier = Modifier.height(6.dp))

                                    packagesList.forEach { pkg ->
                                        Surface(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            shape = RoundedCornerShape(10.dp),
                                            color = DarkSurface,
                                            border = BorderStroke(1.dp, BorderDark)
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(pkg.first, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 12.5.sp)
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(pkg.second.joinToString(" • "), color = TextSecondary, fontSize = 11.sp)
                                                }
                                                IconButton(
                                                    onClick = {
                                                        packagesList.remove(pkg)
                                                        val jsonArr = JSONArray()
                                                        packagesList.forEach { p ->
                                                            val obj = JSONObject()
                                                            obj.put("name", p.first)
                                                            val cArr = JSONArray()
                                                            p.second.forEach { cArr.put(it) }
                                                            obj.put("clips", cArr)
                                                            jsonArr.put(obj)
                                                        }
                                                        viewModel.saveConfig(currentConfig.copy(packagesJson = jsonArr.toString()))
                                                    },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(Icons.Default.Delete, contentDescription = "حذف پکیج", tint = ErrorRed, modifier = Modifier.size(17.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // 2. Studio Management
                            SettingGroupCard(
                                title = "۲. مدیریت آتلیه‌ها و استودیوها",
                                description = "افزودن نام استودیوهای همکار جهت تفکیک پروژه‌ها و حسابرسی مالی",
                                icon = Icons.Default.Store
                            ) {
                                var newStudioText by remember { mutableStateOf("") }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = newStudioText,
                                        onValueChange = { newStudioText = it },
                                        placeholder = { Text("نام آتلیه جدید...", color = TextMuted, fontSize = 12.sp) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp),
                                        singleLine = true,
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
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = {
                                            if (newStudioText.isNotBlank()) {
                                                viewModel.addStudio(PersianUtils.formatStudioName(newStudioText))
                                                newStudioText = ""
                                            }
                                        },
                                        modifier = Modifier.height(48.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "افزودن آتلیه", modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("افزودن", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                if (studios.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        studios.forEach { st ->
                                            Surface(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 3.dp),
                                                shape = RoundedCornerShape(10.dp),
                                                color = DarkSurface,
                                                border = BorderStroke(1.dp, BorderDark)
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(
                                                            Icons.Default.Store,
                                                            contentDescription = null,
                                                            tint = PrimaryPurple,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text(PersianUtils.formatStudioName(st.name), color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 12.5.sp)
                                                    }
                                                    Row {
                                                        IconButton(onClick = { studioToEdit = st }, modifier = Modifier.size(32.dp)) {
                                                            Icon(Icons.Default.Edit, contentDescription = "ویرایش", tint = PrimaryPurple, modifier = Modifier.size(16.dp))
                                                        }
                                                        IconButton(onClick = { studioToDelete = st }, modifier = Modifier.size(32.dp)) {
                                                            Icon(Icons.Default.Delete, contentDescription = "حذف", tint = ErrorRed, modifier = Modifier.size(16.dp))
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // 3. Clip Management
                            SettingGroupCard(
                                title = "۳. کلیپ‌های مرجع پایه",
                                description = "تعریف آیتم‌های پیش‌فرض کلیپ جهت استفاده سریع هنگام ثبت پروژه جدید",
                                icon = Icons.Default.Movie
                            ) {
                                var newClipText by remember { mutableStateOf("") }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = newClipText,
                                        onValueChange = { newClipText = it },
                                        placeholder = { Text("عنوان کلیپ جدید...", color = TextMuted, fontSize = 12.sp) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp),
                                        singleLine = true,
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
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = {
                                            if (newClipText.isNotBlank()) {
                                                viewModel.addDefaultClip(newClipText)
                                                newClipText = ""
                                            }
                                        },
                                        modifier = Modifier.height(48.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "افزودن کلیپ", modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("افزودن", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                if (defaultClips.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        defaultClips.forEach { clip ->
                                            Surface(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 3.dp),
                                                shape = RoundedCornerShape(10.dp),
                                                color = DarkSurface,
                                                border = BorderStroke(1.dp, BorderDark)
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(
                                                            Icons.Default.Movie,
                                                            contentDescription = null,
                                                            tint = MediaAccentCyan,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text(clip.name, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 12.5.sp)
                                                    }
                                                    Row {
                                                        IconButton(onClick = { clipToEdit = clip }, modifier = Modifier.size(32.dp)) {
                                                            Icon(Icons.Default.Edit, contentDescription = "ویرایش", tint = PrimaryPurple, modifier = Modifier.size(16.dp))
                                                        }
                                                        IconButton(onClick = { clipToDelete = clip }, modifier = Modifier.size(32.dp)) {
                                                            Icon(Icons.Default.Delete, contentDescription = "حذف", tint = ErrorRed, modifier = Modifier.size(16.dp))
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // ================= 2: FINANCE & BRANDING =================
                        2 -> {
                            SectionTitleHeader(
                                title = "تنظیمات مالی، قیمت سریع و فاکتور",
                                subtitle = "مدیریت دکمه‌های درج سریع مبالغ، هویت برند در فاکتور رسمی و اطلاعات بانکی",
                                icon = Icons.Default.Receipt
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // Quick Prices CRUD
                            SettingGroupCard(
                                title = "دکمه‌های قیمت سریع (پیش‌تنظیم مبالغ)",
                                description = "برای درج با یک لمس در فرم‌های مالی، مبالغ پرتکرار را نام‌گذاری و ذخیره کنید",
                                icon = Icons.Default.CreditCard
                            ) {
                                var quickPriceTitleInput by remember { mutableStateOf("") }
                                var quickPriceAmountInput by remember { mutableStateOf("") }
                                var quickPriceToEditIndex by remember { mutableStateOf<Int?>(null) }

                                val parsedQuickPrices = remember(config?.quickPricesJson) {
                                    parseQuickPrices(currentConfig.quickPricesJson).toMutableList()
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = quickPriceTitleInput,
                                        onValueChange = { quickPriceTitleInput = it },
                                        placeholder = { Text("عنوان (مثلاً: بیعانه اول)", color = TextMuted, fontSize = 11.5.sp) },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
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
                                    OutlinedTextField(
                                        value = quickPriceAmountInput,
                                        onValueChange = { input ->
                                            val converted = PersianUtils.convertFaToEnNum(input)
                                            quickPriceAmountInput = converted.filter { it.isDigit() }
                                        },
                                        placeholder = { Text("مبلغ به تومان", color = TextMuted, fontSize = 11.5.sp) },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        visualTransformation = PersianNumberVisualTransformation(),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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

                                val quickAmtLong = quickPriceAmountInput.toLongOrNull() ?: 0L
                                if (quickAmtLong > 0L) {
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        color = MediaAccentCyan.copy(alpha = 0.08f),
                                        border = BorderStroke(1.dp, MediaAccentCyan.copy(alpha = 0.25f))
                                    ) {
                                        Text(
                                            text = "به حروف: ${PersianUtils.numberToWordsFa(quickAmtLong, "تومان")}",
                                            color = MediaAccentCyan,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Button(
                                    onClick = {
                                        val amt = quickPriceAmountInput.toDoubleOrNull()
                                        val name = if (quickPriceTitleInput.isNotBlank()) quickPriceTitleInput else "قیمت جدید"
                                        if (amt != null && amt > 0) {
                                            val list = parsedQuickPrices.toMutableList()
                                            if (quickPriceToEditIndex != null && quickPriceToEditIndex!! in list.indices) {
                                                list[quickPriceToEditIndex!!] = QuickPriceItem(name, amt)
                                                quickPriceToEditIndex = null
                                            } else {
                                                list.add(QuickPriceItem(name, amt))
                                            }
                                            viewModel.saveConfig(currentConfig.copy(quickPricesJson = serializeQuickPrices(list)))
                                            quickPriceTitleInput = ""
                                            quickPriceAmountInput = ""
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(17.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        if (quickPriceToEditIndex != null) "بروزرسانی قیمت سریع" else "افزودن به لیست قیمت‌های سریع",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                if (parsedQuickPrices.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text("برای حذف روی هر کپسول ضربه بزنید:", color = TextMuted, fontSize = 11.sp)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    ResponsiveCapsuleGrid(items = parsedQuickPrices, maxPerRow = 2) { qp, modifier ->
                                        ChipSelectable(
                                            text = "❌ ${qp.getFormattedLabel()}",
                                            isSelected = true,
                                            onSelect = {
                                                val list = parsedQuickPrices.toMutableList()
                                                list.remove(qp)
                                                viewModel.saveConfig(currentConfig.copy(quickPricesJson = serializeQuickPrices(list)))
                                            },
                                            accentColor = MediaAccentCyan,
                                            modifier = modifier
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Invoice & Brand Settings
                            SettingGroupCard(
                                title = "هویت برند و اطلاعات فاکتور رسمی",
                                description = "این اطلاعات در سربرگ، پانوشت و بخش واریز فاکتورهای چاپی و تصویری قرار می‌گیرند",
                                icon = Icons.Default.Receipt
                            ) {
                                OutlinedTextField(
                                    value = brandTitle,
                                    onValueChange = { brandTitle = it },
                                    label = { Text("عنوان استودیو / نام برند شخصی شما", fontSize = 12.sp) },
                                    placeholder = { Text("مثال: استودیو تدوین کات برتر", color = TextMuted) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
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

                                OutlinedTextField(
                                    value = bankCard,
                                    onValueChange = { bankCard = PersianUtils.convertFaToEnNum(it) },
                                    label = { Text("شماره کارت بانکی جهت واریز", fontSize = 12.sp) },
                                    placeholder = { Text("مثال: ۶۰۳۷۹۹...", color = TextMuted) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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

                                OutlinedTextField(
                                    value = bankOwner,
                                    onValueChange = { bankOwner = it },
                                    label = { Text("نام صاحب حساب بانکی", fontSize = 12.sp) },
                                    placeholder = { Text("مثال: میلاد بهمنیار", color = TextMuted) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
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

                                OutlinedTextField(
                                    value = footerNote,
                                    onValueChange = { footerNote = it },
                                    label = { Text("متن ذیل فاکتور رسمی (پانوشت)", fontSize = 12.sp) },
                                    placeholder = { Text("مثال: با تشکر از حسن انتخاب شما، لطفاً پس از واریز فیش را ارسال فرمایید.", color = TextMuted) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    maxLines = 3,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = DarkInputBg,
                                        unfocusedContainerColor = DarkInputBg,
                                        focusedBorderColor = PrimaryPurple,
                                        unfocusedBorderColor = BorderDark,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    )
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                Button(
                                    onClick = {
                                        viewModel.saveConfig(
                                            currentConfig.copy(
                                                invoiceBrandTitle = brandTitle,
                                                invoiceBankCard = bankCard,
                                                invoiceBankOwner = bankOwner,
                                                invoiceFooterNote = footerNote
                                            )
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("ذخیره تنظیمات فاکتور و برند", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }

                        // ================= 3: SAFETY & BACKUP =================
                        3 -> {
                            SafetyAndBackupSection(
                                viewModel = viewModel,
                                config = config
                            )
                        }

                        // ================= 4: RESET CENTER =================
                        4 -> {
                            ResetCenterSection(
                                viewModel = viewModel
                            )
                        }

                        // ================= 5: ABOUT CATALOG =================
                        5 -> {
                            SectionTitleHeader(
                                title = "درباره کاترلاگ",
                                subtitle = "شناسنامه نرم‌افزار، مشخصات نسخه و معرفی امکانات",
                                icon = Icons.Default.Info
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_cutterlog_logo_card),
                                    contentDescription = "آیکون کاترلاگ",
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(RoundedCornerShape(18.dp))
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "کاترلاگ (Cutterlog Pro)",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    fontSize = 18.sp
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = DarkSurface,
                                    border = BorderStroke(1.dp, BorderDark)
                                ) {
                                    Text(
                                        text = "نسخه ${PersianUtils.faNum(BuildConfig.VERSION_NAME)}",
                                        color = PrimaryPurple,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.5.sp,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = "کاترلاگ یک ابزار حرفه‌ای برای مدیریت پروژه‌ها، استودیوها، زمان، حسابرسی و امور مالی مرتبط با فعالیت‌های تدوین و ادیت ویدئو است که به صورت کاملاً آفلاین و محلی فعالیت می‌کند.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 22.sp,
                                    fontSize = 12.5.sp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp)
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    AboutFeatureRow(icon = Icons.Default.Code, title = "توسعه‌دهنده", value = "میلاد بهمنیار")
                                    AboutFeatureRow(icon = Icons.Default.Security, title = "امنیت و حریم خصوصی", value = "ذخیره‌سازی ۱۰۰٪ محلی (SQLite)")
                                    AboutFeatureRow(icon = Icons.Default.Speed, title = "عملکرد", value = "بهینه‌سازی شده برای تدوینگران ویدئو")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // ================= DIALOGS =================

    // 1. STUDIO EDIT DIALOG
    if (studioToEdit != null) {
        val st = studioToEdit!!
        var stNameInput by remember { mutableStateOf(st.name) }
        AlertDialog(
            onDismissRequest = { studioToEdit = null },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier.fillMaxWidth(0.80f),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = PrimaryPurple, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ویرایش نام آتلیه", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            },
            text = {
                OutlinedTextField(
                    value = stNameInput,
                    onValueChange = { stNameInput = it },
                    label = { Text("نام آتلیه", fontSize = 12.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkInputBg,
                        unfocusedContainerColor = DarkInputBg,
                        focusedBorderColor = PrimaryPurple,
                        unfocusedBorderColor = BorderDark,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (stNameInput.isNotBlank()) {
                            viewModel.updateStudio(st.copy(name = PersianUtils.formatStudioName(stNameInput)))
                            studioToEdit = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("ذخیره تغییرات", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { studioToEdit = null }) {
                    Text("انصراف", color = TextMuted)
                }
            },
            containerColor = DarkCard,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // 2. STUDIO DELETE DIALOG
    if (studioToDelete != null) {
        val st = studioToDelete!!
        AlertDialog(
            onDismissRequest = { studioToDelete = null },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier.fillMaxWidth(0.80f),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("حذف آتلیه", color = ErrorRed, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            },
            text = {
                Text("آیا از حذف «${PersianUtils.formatStudioName(st.name)}» اطمینان دارید؟", color = TextSecondary, fontSize = 13.sp)
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteStudio(st)
                        studioToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("حذف قطعی", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { studioToDelete = null }) {
                    Text("انصراف", color = TextMuted)
                }
            },
            containerColor = DarkCard,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // 3. DEFAULT CLIP EDIT DIALOG
    if (clipToEdit != null) {
        val clip = clipToEdit!!
        var clipNameInput by remember { mutableStateOf(clip.name) }
        AlertDialog(
            onDismissRequest = { clipToEdit = null },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier.fillMaxWidth(0.80f),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = PrimaryPurple, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ویرایش عنوان کلیپ مرجع", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            },
            text = {
                OutlinedTextField(
                    value = clipNameInput,
                    onValueChange = { clipNameInput = it },
                    label = { Text("عنوان کلیپ", fontSize = 12.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkInputBg,
                        unfocusedContainerColor = DarkInputBg,
                        focusedBorderColor = PrimaryPurple,
                        unfocusedBorderColor = BorderDark,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (clipNameInput.isNotBlank()) {
                            viewModel.updateDefaultClip(clip.copy(name = clipNameInput))
                            clipToEdit = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("ذخیره تغییرات", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { clipToEdit = null }) {
                    Text("انصراف", color = TextMuted)
                }
            },
            containerColor = DarkCard,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // 4. DEFAULT CLIP DELETE DIALOG
    if (clipToDelete != null) {
        val clip = clipToDelete!!
        AlertDialog(
            onDismissRequest = { clipToDelete = null },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier.fillMaxWidth(0.80f),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("حذف کلیپ مرجع", color = ErrorRed, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            },
            text = {
                Text("آیا از حذف کلیپ مرجع «${clip.name}» اطمینان دارید؟", color = TextSecondary, fontSize = 13.sp)
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteDefaultClip(clip)
                        clipToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("حذف قطعی", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { clipToDelete = null }) {
                    Text("انصراف", color = TextMuted)
                }
            },
            containerColor = DarkCard,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // 5. ABOUT APP COMPREHENSIVE DIALOG
    if (showAboutAppDialog) {
        AboutAppDialog(
            onDismissRequest = { showAboutAppDialog = false }
        )
    }

    // 7. DEVELOPER SUPPORT DIALOG
    if (showDeveloperSupportDialog) {
        DeveloperSupportDialog(
            onDismissRequest = { showDeveloperSupportDialog = false }
        )
    }
}

// ================= HELPER UI COMPONENTS =================

@Composable
private fun SettingsHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "تنظیمات کاترلاگ",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                fontSize = 20.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "پیکربندی هویت برند، ابزارها، اهداف و پایگاه داده",
                color = TextSecondary,
                fontSize = 11.5.sp
            )
        }

        Surface(
            shape = CircleShape,
            color = PrimaryPurple.copy(alpha = 0.12f),
            border = BorderStroke(1.dp, PrimaryPurple.copy(alpha = 0.3f)),
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = PrimaryPurple,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun SectionTitleHeader(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color = PrimaryPurple
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
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                fontSize = 15.sp
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
private fun SettingGroupCard(
    title: String,
    description: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = DarkSurface,
        border = BorderStroke(1.dp, BorderDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = PrimaryPurple,
                    modifier = Modifier.size(17.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = title,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = description,
                        color = TextMuted,
                        fontSize = 10.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = BorderDark.copy(alpha = 0.5f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            content()
        }
    }
}

@Composable
private fun ResetActionButton(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = DarkSurface,
        border = BorderStroke(1.dp, ErrorRed.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = ErrorRed,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.5.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = TextSecondary,
                    fontSize = 10.5.sp
                )
            }
            Icon(
                Icons.Default.ArrowBack,
                contentDescription = null,
                tint = ErrorRed,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun AboutFeatureRow(
    icon: ImageVector,
    title: String,
    value: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = DarkSurface,
        border = BorderStroke(1.dp, BorderDark)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = PrimaryPurple, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(text = title, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
            Text(text = value, color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
