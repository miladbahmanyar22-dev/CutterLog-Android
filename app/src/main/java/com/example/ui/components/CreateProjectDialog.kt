package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
import com.example.ui.util.PersianNumberVisualTransformation
import com.example.util.PersianUtils

/**
 * Modern, Responsive, and Minimalist Project Registration Modal for CutterLog.
 * Strictly preserves all existing logic, validation, state, and callbacks.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateProjectDialog(
    studios: List<String>,
    defaultClips: List<String>,
    defaultDeadlineDays: Int,
    quickPricesJson: String,
    packagesJson: String,
    onDismiss: () -> Unit,
    onCreate: (String, String, Double, List<String>, Int, String?) -> Unit
) {
    // ----------------------------------------------------
    // Existing State Management (Preserved 100%)
    // ----------------------------------------------------
    var title by remember { mutableStateOf("") }
    var titleError by remember { mutableStateOf(false) }
    var weddingDate by remember { mutableStateOf<String?>(PersianUtils.getCurrentJalaliDate()) }
    var showWeddingDatePicker by remember { mutableStateOf(false) }
    var studio by remember { mutableStateOf(studios.firstOrNull() ?: "آتلیه عمومی") }
    var priceText by remember { mutableStateOf("2000000") }
    var deadlineDays by remember { mutableIntStateOf(defaultDeadlineDays) }
    val selectedClips = remember { mutableStateListOf<String>().apply { addAll(defaultClips) } }

    // Parse Packages
    val packages = remember(packagesJson) {
        try {
            val jsonArr = org.json.JSONArray(packagesJson)
            val list = mutableListOf<Pair<String, List<String>>>()
            for (i in 0 until jsonArr.length()) {
                val obj = jsonArr.getJSONObject(i)
                val name = obj.getString("name")
                val clipsArr = obj.getJSONArray("clips")
                val clips = mutableListOf<String>()
                for (j in 0 until clipsArr.length()) {
                    clips.add(clipsArr.getString(j))
                }
                list.add(Pair(name, clips))
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    var selectedPackageName by remember { mutableStateOf<String?>(packages.firstOrNull()?.first) }

    // Helper for current clip list according to package or default
    val currentClipList = remember(selectedPackageName, packages, defaultClips) {
        packages.find { it.first == selectedPackageName }?.second ?: defaultClips
    }

    // Full custom Dialog with platform width disabled for precise responsive bounds
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            // Screen Overlay with subtle dimming and centered responsive container taking 80% of screen
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding(),
                contentAlignment = Alignment.Center
            ) {
                val dialogWidth = maxWidth * 0.80f
                val dialogMaxHeight = maxHeight * 0.82f

                Surface(
                    modifier = Modifier
                        .width(dialogWidth)
                        .heightIn(max = dialogMaxHeight),
                    shape = RoundedCornerShape(20.dp),
                    color = DarkCard,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                    shadowElevation = 16.dp
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // ==============================================================
                        // 1. FIXED HEADER
                        // ==============================================================
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = PrimaryPurple.copy(alpha = 0.15f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryPurple.copy(alpha = 0.3f)),
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Movie,
                                            contentDescription = null,
                                            tint = PrimaryPurple,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "ثبت پروژه جدید",
                                        color = TextPrimary,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "مشخصات و شرایط تدوین پروژه را وارد کنید",
                                        color = TextSecondary,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            // Close Button (×)
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(DarkInputBg)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "بستن پنجره",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        HorizontalDivider(
                            color = BorderDark.copy(alpha = 0.6f),
                            thickness = 1.dp
                        )

                        // ==============================================================
                        // 2. SCROLLABLE CONTENT (Structured Sections)
                        // ==============================================================
                        Column(
                            modifier = Modifier
                                .weight(1f, fill = false)
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 20.dp, vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(18.dp)
                        ) {
                            // ----------------------------------------------------------
                            // SECTION A: اطلاعات اصلی پروژه
                            // ----------------------------------------------------------
                            SectionContainer(
                                icon = Icons.Default.VideoLibrary,
                                title = "اطلاعات اصلی پروژه"
                            ) {
                                // Project Title Field
                                Column {
                                    Text(
                                        text = "عنوان یا نام پروژه *",
                                        color = TextSecondary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    OutlinedTextField(
                                        value = title,
                                        onValueChange = {
                                            title = it
                                            if (titleError && it.isNotBlank()) {
                                                titleError = false
                                            }
                                        },
                                        placeholder = {
                                            Text(
                                                "مثلاً: عروسی علی و مریم / تیزر شرکتی",
                                                color = TextMuted,
                                                fontSize = 13.sp
                                            )
                                        },
                                        isError = titleError && title.isBlank(),
                                        supportingText = if (titleError && title.isBlank()) {
                                            {
                                                Text(
                                                    text = "لطفاً عنوان پروژه را وارد کنید",
                                                    color = ErrorRed,
                                                    fontSize = 11.sp
                                                )
                                            }
                                        } else null,
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = DarkInputBg,
                                            unfocusedContainerColor = DarkInputBg,
                                            errorContainerColor = DarkInputBg,
                                            focusedBorderColor = PrimaryPurple,
                                            unfocusedBorderColor = BorderDark,
                                            errorBorderColor = ErrorRed,
                                            focusedTextColor = TextPrimary,
                                            unfocusedTextColor = TextPrimary
                                        ),
                                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Wedding/Event Date Field
                                Column {
                                    Text(
                                        text = "تاریخ روز مراسم یا تصویربرداری",
                                        color = TextSecondary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    WeddingDateField(
                                        value = weddingDate,
                                        onClick = { showWeddingDatePicker = true },
                                        onClear = { weddingDate = null }
                                    )
                                }

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

                                Spacer(modifier = Modifier.height(10.dp))

                                // Studio Selection
                                Column {
                                    Text(
                                        text = "استودیو / کارفرما طرف حساب",
                                        color = TextSecondary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    ResponsiveCapsuleGrid(
                                        items = studios,
                                        maxPerRow = 3
                                    ) { s, modifier ->
                                        ChipSelectable(
                                            text = s,
                                            isSelected = studio == s,
                                            onSelect = { studio = s },
                                            modifier = modifier
                                        )
                                    }
                                }
                            }

                            // ----------------------------------------------------------
                            // SECTION B: اطلاعات مالی
                            // ----------------------------------------------------------
                            SectionContainer(
                                icon = Icons.Default.Payments,
                                title = "اطلاعات مالی و مبلغ قرارداد"
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "مبلغ قرارداد (تومان)",
                                            color = TextSecondary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        val numericPrice = priceText.toDoubleOrNull() ?: 0.0
                                        if (numericPrice > 0) {
                                            Text(
                                                text = PersianUtils.formatCurrencyFa(numericPrice),
                                                color = SuccessGreen,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    OutlinedTextField(
                                        value = priceText,
                                        onValueChange = { input ->
                                            val converted = PersianUtils.convertFaToEnNum(input)
                                            priceText = converted.filter { it.isDigit() }
                                        },
                                        placeholder = { Text("مثال: ۲٬۰۰۰٬۰۰۰", color = TextMuted) },
                                        singleLine = true,
                                        visualTransformation = PersianNumberVisualTransformation(),
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Number,
                                            imeAction = ImeAction.Done
                                        ),
                                        trailingIcon = {
                                            Surface(
                                                color = DarkSurface,
                                                shape = RoundedCornerShape(8.dp),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                                                modifier = Modifier.padding(end = 8.dp)
                                            ) {
                                                Text(
                                                    text = "تومان",
                                                    color = TextSecondary,
                                                    fontSize = 11.sp,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        },
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

                                    // Live Amount in Persian Words (مبلغ به حروف)
                                    val currentAmountLong = priceText.toLongOrNull() ?: 0L
                                    if (currentAmountLong > 0L) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Surface(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(8.dp),
                                            color = PrimaryPurple.copy(alpha = 0.08f),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryPurple.copy(alpha = 0.25f))
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "به حروف: ",
                                                    color = PrimaryPurple,
                                                    fontSize = 11.5.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = PersianUtils.numberToWordsFa(currentAmountLong, "تومان"),
                                                    color = TextPrimary,
                                                    fontSize = 11.5.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Quick Price Capsules
                                Column {
                                    Text(
                                        text = "قیمت‌های سریع پیشنهادی:",
                                        color = TextMuted,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    QuickPriceCapsules(
                                        quickPricesJson = quickPricesJson,
                                        selectedAmount = priceText.toDoubleOrNull(),
                                        onSelectPrice = { priceText = it.amount.toLong().toString() }
                                    )
                                }
                            }

                            // ----------------------------------------------------------
                            // SECTION C: پکیج و کلیپ‌های تدوین
                            // ----------------------------------------------------------
                            SectionContainer(
                                icon = Icons.Default.Movie,
                                title = "پکیج و کلیپ‌های تدوین"
                            ) {
                                // Packages (if defined)
                                if (packages.isNotEmpty()) {
                                    Column {
                                        Text(
                                            text = "انتخاب پکیج آماده:",
                                            color = TextSecondary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        ResponsiveCapsuleGrid(
                                            items = packages,
                                            maxPerRow = 2
                                        ) { pkg, modifier ->
                                            ChipSelectable(
                                                text = pkg.first,
                                                isSelected = selectedPackageName == pkg.first,
                                                onSelect = {
                                                    selectedPackageName = pkg.first
                                                    selectedClips.clear()
                                                    selectedClips.addAll(pkg.second)
                                                },
                                                accentColor = PrimaryPurple,
                                                modifier = modifier
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                }

                                // Clips Selection Grid
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "کلیپ‌های پروژه (${PersianUtils.faNum(selectedClips.size)} از ${PersianUtils.faNum(currentClipList.size)}):",
                                            color = TextSecondary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        TextButton(
                                            onClick = {
                                                selectedClips.clear()
                                                val pkgClips = packages.find { it.first == selectedPackageName }?.second
                                                if (!pkgClips.isNullOrEmpty()) {
                                                    selectedClips.addAll(pkgClips)
                                                } else {
                                                    selectedClips.addAll(defaultClips)
                                                }
                                            },
                                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = MediaAccentCyan,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "تدوین کامل (همه)",
                                                color = MediaAccentCyan,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    ResponsiveCapsuleGrid(
                                        items = currentClipList,
                                        maxPerRow = 2
                                    ) { clipName, modifier ->
                                        val isSel = selectedClips.contains(clipName)
                                        ChipSelectable(
                                            text = clipName,
                                            isSelected = isSel,
                                            onSelect = {
                                                if (isSel) selectedClips.remove(clipName) else selectedClips.add(clipName)
                                            },
                                            modifier = modifier
                                        )
                                    }
                                }
                            }

                            // ----------------------------------------------------------
                            // SECTION D: زمان‌بندی و مهلت تحویل
                            // ----------------------------------------------------------
                            SectionContainer(
                                icon = Icons.Default.Schedule,
                                title = "زمان‌بندی و مهلت تحویل"
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "مهلت تحویل پروژه:",
                                            color = TextSecondary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "${PersianUtils.faNum(deadlineDays)} روز کاری از زمان ثبت",
                                            color = TextPrimary,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    // Stepper Controls
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        IconButton(
                                            onClick = { if (deadlineDays > 1) deadlineDays-- },
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(DarkSurface)
                                                .border(1.dp, BorderDark, RoundedCornerShape(8.dp))
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Remove,
                                                contentDescription = "کاهش مهلت",
                                                tint = TextSecondary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }

                                        OutlinedTextField(
                                            value = deadlineDays.toString(),
                                            onValueChange = { deadlineDays = it.toIntOrNull() ?: 1 },
                                            singleLine = true,
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            textStyle = androidx.compose.ui.text.TextStyle(
                                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary,
                                                fontSize = 14.sp
                                            ),
                                            modifier = Modifier
                                                .width(64.dp)
                                                .height(44.dp),
                                            shape = RoundedCornerShape(8.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedContainerColor = DarkInputBg,
                                                unfocusedContainerColor = DarkInputBg,
                                                focusedBorderColor = PrimaryPurple,
                                                unfocusedBorderColor = BorderDark
                                            )
                                        )

                                        IconButton(
                                            onClick = { deadlineDays++ },
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(DarkSurface)
                                                .border(1.dp, BorderDark, RoundedCornerShape(8.dp))
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = "افزایش مهلت",
                                                tint = TextSecondary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Quick Days Presets
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf(3, 7, 10, 14, 21, 30).forEach { days ->
                                        val isSelected = deadlineDays == days
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isSelected) PrimaryPurple.copy(alpha = 0.2f) else DarkSurface,
                                            border = androidx.compose.foundation.BorderStroke(
                                                1.dp,
                                                if (isSelected) PrimaryPurple else BorderDark.copy(alpha = 0.6f)
                                            ),
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable { deadlineDays = days }
                                        ) {
                                            Box(
                                                modifier = Modifier.padding(vertical = 6.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "${PersianUtils.faNum(days)} روز",
                                                    color = if (isSelected) PrimaryPurple else TextSecondary,
                                                    fontSize = 11.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // ==============================================================
                        // 3. FIXED FOOTER (Always Visible Actions)
                        // ==============================================================
                        HorizontalDivider(
                            color = BorderDark.copy(alpha = 0.6f),
                            thickness = 1.dp
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Primary Submit Action
                            Button(
                                onClick = {
                                    val p = priceText.toDoubleOrNull() ?: 0.0
                                    if (title.isBlank()) {
                                        titleError = true
                                    } else {
                                        titleError = false
                                        onCreate(
                                            title.trim(),
                                            studio,
                                            p,
                                            selectedClips.toList(),
                                            deadlineDays,
                                            weddingDate
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .weight(1.3f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PrimaryPurple,
                                    contentColor = Color.White
                                ),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "ثبت پروژه",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Secondary Cancel Action
                            OutlinedButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .weight(0.9f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = DarkSurface,
                                    contentColor = TextSecondary
                                )
                            ) {
                                Text(
                                    text = "انصراف",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Clean Section Card Container with an accent icon and title
 */
@Composable
private fun SectionContainer(
    icon: ImageVector,
    title: String,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = DarkSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark.copy(alpha = 0.8f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = PrimaryPurple.copy(alpha = 0.12f),
                    modifier = Modifier.size(24.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = PrimaryPurple,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            content()
        }
    }
}
