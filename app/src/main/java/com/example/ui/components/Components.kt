package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BorderDark
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkInputBg
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.MediaAccentCyan
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.PersianUtils
import java.util.Locale

@Composable
fun KpiCard(
    title: String,
    value: String,
    subtitle: String? = null,
    icon: ImageVector,
    accentColor: Color = PrimaryPurple,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark.copy(alpha = 0.7f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    fontSize = 19.sp
                )
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = accentColor,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun StatusBadge(
    text: String,
    backgroundColor: Color,
    textColor: Color = Color.White
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor.copy(alpha = 0.14f),
        border = androidx.compose.foundation.BorderStroke(1.dp, backgroundColor.copy(alpha = 0.38f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(backgroundColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                color = textColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun AccordionPanel(
    title: String,
    subtitle: String? = null,
    initiallyExpanded: Boolean = false,
    content: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(initiallyExpanded) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark.copy(alpha = 0.7f))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        fontSize = 13.5.sp
                    )
                    if (subtitle != null) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = TextSecondary
                )
            }
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkSurface)
                        .padding(14.dp)
                ) {
                    content()
                }
            }
        }
    }
}

@Composable
fun ChipSelectable(
    text: String,
    isSelected: Boolean,
    onSelect: () -> Unit,
    accentColor: Color = PrimaryPurple,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) accentColor else DarkInputBg)
            .border(
                width = 1.dp,
                color = if (isSelected) accentColor else BorderDark.copy(alpha = 0.7f),
                shape = RoundedCornerShape(10.dp)
            )
            .clickable { onSelect() }
            .padding(horizontal = 10.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = PersianUtils.faNum(text),
                color = if (isSelected) Color.White else TextSecondary,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun <T> ResponsiveCapsuleGrid(
    items: List<T>,
    modifier: Modifier = Modifier,
    maxPerRow: Int = 2,
    itemContent: @Composable (item: T, modifier: Modifier) -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.chunked(maxPerRow).forEach { chunk ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                chunk.forEach { item ->
                    itemContent(item, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun WeddingDateField(
    value: String?,
    onClick: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        color = DarkInputBg,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (!value.isNullOrBlank()) PrimaryPurple else BorderDark)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Event,
                    contentDescription = null,
                    tint = if (!value.isNullOrBlank()) PrimaryPurple else TextMuted,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "تاریخ روز عروسی (شمسی)",
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (!value.isNullOrBlank()) PersianUtils.faNum(value) else "انتخاب تاریخ روز عروسی (کلیک کنید)",
                        color = if (!value.isNullOrBlank()) TextPrimary else TextMuted,
                        fontWeight = if (!value.isNullOrBlank()) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 13.sp
                    )
                }
            }
            if (!value.isNullOrBlank()) {
                IconButton(
                    onClick = onClear,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "حذف تاریخ",
                        tint = ErrorRed,
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun PersianDatePickerDialog(
    initialDate: String? = null,
    onDismiss: () -> Unit,
    onDateConfirm: (String?) -> Unit
) {
    val currentDateStr = PersianUtils.getCurrentJalaliDate()
    val parts = (if (initialDate.isNullOrBlank()) currentDateStr else initialDate!!).split("/").mapNotNull { PersianUtils.convertFaToEnNum(it).toIntOrNull() }
    
    var selectedYear by remember { mutableIntStateOf(if (parts.size == 3) parts[0] else 1405) }
    var selectedMonth by remember { mutableIntStateOf(if (parts.size == 3) parts[1] else 1) }
    var selectedDay by remember { mutableIntStateOf(if (parts.size == 3) parts[2] else 1) }

    val monthNames = listOf(
        "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
    )

    val maxDays = when (selectedMonth) {
        in 1..6 -> 31
        in 7..11 -> 30
        else -> 29
    }

    if (selectedDay > maxDays) {
        selectedDay = maxDays
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.fillMaxWidth(0.80f),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Event, contentDescription = null, tint = PrimaryPurple)
                Spacer(modifier = Modifier.width(8.dp))
                Text("تقویم انتخاب تاریخ روز عروسی", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = DarkSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryPurple.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "تاریخ انتخابی:",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${PersianUtils.faNum(selectedDay)} ${monthNames.getOrElse(selectedMonth - 1) { "" }} ${PersianUtils.faNum(selectedYear)}",
                            color = PrimaryPurple,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = PersianUtils.faNum(String.format(java.util.Locale.US, "%04d/%02d/%02d", selectedYear, selectedMonth, selectedDay)),
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 1. Year selector
                Text("۱. انتخاب سال:", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                val years = (1400..1410).toList()
                ResponsiveCapsuleGrid(
                    items = years.map { it.toString() },
                    maxPerRow = 4
                ) { yStr, modifier ->
                    val yInt = yStr.toInt()
                    ChipSelectable(
                        text = PersianUtils.faNum(yStr),
                        isSelected = selectedYear == yInt,
                        onSelect = { selectedYear = yInt },
                        modifier = modifier
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 2. Month selector
                Text("۲. انتخاب ماه:", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                ResponsiveCapsuleGrid(
                    items = monthNames,
                    maxPerRow = 3
                ) { mName, modifier ->
                    val mIdx = monthNames.indexOf(mName) + 1
                    ChipSelectable(
                        text = mName,
                        isSelected = selectedMonth == mIdx,
                        onSelect = { selectedMonth = mIdx },
                        accentColor = PrimaryPurple,
                        modifier = modifier
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 3. Day selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("۳. انتخاب روز:", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(
                            onClick = { if (selectedDay > 1) selectedDay-- },
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            Text("- قبلی", color = PrimaryPurple, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            text = "روز ${PersianUtils.faNum(selectedDay)}",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                        TextButton(
                            onClick = { if (selectedDay < maxDays) selectedDay++ },
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            Text("+ بعدی", color = PrimaryPurple, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))

                val daysList = (1..maxDays).map { it.toString() }
                ResponsiveCapsuleGrid(
                    items = daysList,
                    maxPerRow = 7
                ) { dStr, modifier ->
                    val dInt = dStr.toInt()
                    ChipSelectable(
                        text = PersianUtils.faNum(dStr),
                        isSelected = selectedDay == dInt,
                        onSelect = { selectedDay = dInt },
                        modifier = modifier
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val formatted = String.format(java.util.Locale.US, "%04d/%02d/%02d", selectedYear, selectedMonth, selectedDay)
                    onDateConfirm(formatted)
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("تأیید تاریخ", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = { onDateConfirm(null) }) {
                    Text("حذف تاریخ", color = ErrorRed)
                }
                TextButton(onClick = onDismiss) {
                    Text("انصراف", color = TextMuted)
                }
            }
        },
        containerColor = DarkCard
    )
}

data class QuickPriceItem(
    val name: String,
    val amount: Double
) {
    fun getFormattedLabel(): String = "$name (${PersianUtils.formatPriceShort(amount)})"
}

fun parseQuickPrices(jsonStr: String?): List<QuickPriceItem> {
    if (jsonStr.isNullOrBlank()) {
        return listOf(
            QuickPriceItem("بیعانه اول", 1000000.0),
            QuickPriceItem("پیش‌پرداخت", 2000000.0),
            QuickPriceItem("پکیج استاندارد", 5000000.0),
            QuickPriceItem("تسویه کامل", 10000000.0)
        )
    }
    return try {
        val arr = org.json.JSONArray(jsonStr)
        val list = mutableListOf<QuickPriceItem>()
        for (i in 0 until arr.length()) {
            val item = arr.get(i)
            if (item is org.json.JSONObject) {
                val name = item.optString("name", "قیمت ${i + 1}")
                val amount = item.optDouble("amount", 0.0)
                if (amount > 0) list.add(QuickPriceItem(name, amount))
            } else if (item is Number) {
                val amount = item.toDouble()
                if (amount > 0) list.add(QuickPriceItem(PersianUtils.formatPriceShort(amount), amount))
            }
        }
        if (list.isNotEmpty()) list else listOf(
            QuickPriceItem("بیعانه اول", 1000000.0),
            QuickPriceItem("پیش‌پرداخت", 2000000.0),
            QuickPriceItem("پکیج استاندارد", 5000000.0),
            QuickPriceItem("تسویه کامل", 10000000.0)
        )
    } catch (e: Exception) {
        listOf(
            QuickPriceItem("بیعانه اول", 1000000.0),
            QuickPriceItem("پیش‌پرداخت", 2000000.0),
            QuickPriceItem("پکیج استاندارد", 5000000.0),
            QuickPriceItem("تسویه کامل", 10000000.0)
        )
    }
}

fun serializeQuickPrices(items: List<QuickPriceItem>): String {
    val arr = org.json.JSONArray()
    items.forEach { item ->
        val obj = org.json.JSONObject()
        obj.put("name", item.name)
        obj.put("amount", item.amount)
        arr.put(obj)
    }
    return arr.toString()
}

@Composable
fun QuickPriceCapsules(
    quickPricesJson: String?,
    selectedAmount: Double?,
    onSelectPrice: (QuickPriceItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = remember(quickPricesJson) { parseQuickPrices(quickPricesJson) }
    ResponsiveCapsuleGrid(
        items = items,
        maxPerRow = 2,
        modifier = modifier
    ) { item, itemModifier ->
        val isSelected = selectedAmount != null && Math.abs(selectedAmount - item.amount) < 1.0
        ChipSelectable(
            text = item.getFormattedLabel(),
            isSelected = isSelected,
            onSelect = { onSelectPrice(item) },
            accentColor = MediaAccentCyan,
            modifier = itemModifier
        )
    }
}
