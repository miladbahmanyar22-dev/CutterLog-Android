package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.entity.InvoiceData
import com.example.data.entity.InvoiceItem
import com.example.data.entity.PaymentEntity
import com.example.data.entity.ProjectEntity
import com.example.ui.theme.BorderDark
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.InvoiceExporter
import com.example.util.PersianUtils

/**
 * Step 1: Dialog to select the scope of the invoice for a studio:
 * - Option 1: تمام بدهی‌های استودیو (All studio projects with balance > 0)
 * - Option 2: پروژه‌های انتخاب‌شده (User manually selects projects)
 * - Option 3: فقط بدهی‌های پرداخت‌نشده (Unsettled projects with balance > 0)
 */
@Composable
fun InvoiceScopeDialog(
    studioName: String,
    studioProjects: List<ProjectEntity>,
    payments: List<PaymentEntity>,
    onDismiss: () -> Unit,
    onConfirm: (List<ProjectEntity>) -> Unit
) {
    // 0: All debts (projects with balance > 0)
    // 1: Selected projects (manual checkbox selection)
    // 2: Only unsettled debts (isSettled == 0 && balance > 0)
    var selectedScopeOption by remember { mutableIntStateOf(0) }
    val selectedProjectIds = remember { mutableStateListOf<Int>() }

    // Pre-select projects that have debt by default
    remember(studioProjects) {
        val debtIds = studioProjects.filter { proj ->
            val projPaid = payments.filter { it.projectId == proj.id }.sumOf { it.amount }
            (proj.price - projPaid) > 0
        }.map { it.id }
        selectedProjectIds.clear()
        selectedProjectIds.addAll(if (debtIds.isNotEmpty()) debtIds else studioProjects.map { it.id })
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        AlertDialog(
            onDismissRequest = onDismiss,
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier.fillMaxWidth(0.80f),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Receipt,
                        contentDescription = null,
                        tint = PrimaryPurple,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ایجاد صورتحساب: ${PersianUtils.formatStudioName(studioName)}",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "نوع پروژه‌های مندرج در صورتحساب را انتخاب کنید:",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Option 1
                    ScopeOptionItem(
                        title = "تمام بدهی‌های استودیو",
                        description = "تمامی پروژه‌هایی که دارای مانده حساب تسویه‌نشده هستند",
                        isSelected = selectedScopeOption == 0,
                        onClick = { selectedScopeOption = 0 }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Option 2
                    ScopeOptionItem(
                        title = "پروژه‌های انتخاب‌شده (دستی)",
                        description = "انتخاب پرونده‌های مشخص از میان لیست پروژه‌های آتلیه",
                        isSelected = selectedScopeOption == 1,
                        onClick = { selectedScopeOption = 1 }
                    )

                    // If Option 2 is selected: Show Checklist
                    if (selectedScopeOption == 1) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp, bottom = 4.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = DarkSurface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .height(180.dp)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "فهرست پروژه‌ها (${PersianUtils.faNum(selectedProjectIds.size)} از ${PersianUtils.faNum(studioProjects.size)}):",
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                    Row {
                                        TextButton(onClick = {
                                            selectedProjectIds.clear()
                                            selectedProjectIds.addAll(studioProjects.map { it.id })
                                        }) {
                                            Text("همه", fontSize = 11.sp, color = PrimaryPurple)
                                        }
                                        TextButton(onClick = { selectedProjectIds.clear() }) {
                                            Text("هیچکدام", fontSize = 11.sp, color = TextMuted)
                                        }
                                    }
                                }

                                HorizontalDivider(color = BorderDark, modifier = Modifier.padding(vertical = 4.dp))

                                studioProjects.forEach { proj ->
                                    val isChecked = selectedProjectIds.contains(proj.id)
                                    val projPaid = payments.filter { it.projectId == proj.id }.sumOf { it.amount }
                                    val projDebt = (proj.price - projPaid).coerceAtLeast(0.0)

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                if (isChecked) selectedProjectIds.remove(proj.id)
                                                else selectedProjectIds.add(proj.id)
                                            }
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = isChecked,
                                            onCheckedChange = { checked ->
                                                if (checked) selectedProjectIds.add(proj.id)
                                                else selectedProjectIds.remove(proj.id)
                                            },
                                            colors = CheckboxDefaults.colors(checkedColor = PrimaryPurple)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(proj.name, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                            Text(
                                                "ارزش: ${PersianUtils.formatCurrencyFa(proj.price)} • بدهی: ${PersianUtils.formatCurrencyFa(projDebt)}",
                                                color = if (projDebt > 0) ErrorRed else SuccessGreen,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Option 3
                    ScopeOptionItem(
                        title = "فقط بدهی‌های پرداخت‌نشده",
                        description = "صرفاً پرونده‌هایی با وضعیت تسویه‌نشده و مانده بیشتر از صفر",
                        isSelected = selectedScopeOption == 2,
                        onClick = { selectedScopeOption = 2 }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val filteredList: List<ProjectEntity> = when (selectedScopeOption) {
                            0 -> {
                                // All projects with debt
                                studioProjects.filter { proj ->
                                    val paid = payments.filter { it.projectId == proj.id }.sumOf { it.amount }
                                    (proj.price - paid) > 0
                                }.ifEmpty { studioProjects } // fallback if all paid
                            }
                            1 -> {
                                // Selected projects
                                studioProjects.filter { selectedProjectIds.contains(it.id) }
                            }
                            2 -> {
                                // Only unsettled debts
                                studioProjects.filter { proj ->
                                    val paid = payments.filter { it.projectId == proj.id }.sumOf { it.amount }
                                    proj.isSettled == 0 && (proj.price - paid) > 0
                                }.ifEmpty { studioProjects }
                            }
                            else -> studioProjects
                        }

                        if (filteredList.isNotEmpty()) {
                            onConfirm(filteredList)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("مشاهده پیش‌نمایش صورتحساب", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
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
}

@Composable
private fun ScopeOptionItem(
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) PrimaryPurple.copy(alpha = 0.15f) else DarkSurface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp, if (isSelected) PrimaryPurple else BorderDark
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = PrimaryPurple)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Text(description, color = TextSecondary, fontSize = 11.sp)
            }
        }
    }
}

/**
 * Step 2: Full Invoice Preview Dialog with Actions:
 * - 🖼 ذخیره تصویر (PNG)
 * - 📤 اشتراک‌گذاری (Share)
 * - 📄 خروجی PDF (PDF Export)
 * - بازگشت
 * - زوم و بررسی سند
 */
@Composable
fun InvoicePreviewDialog(
    invoice: InvoiceData,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var zoomLevel by remember { mutableFloatStateOf(1.0f) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color(0xFF0F172A) // Sleek slate backdrop for previewing the white document
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // --- TOP CONTROL BAR ---
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF1E293B),
                        shadowElevation = 6.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Right Side: Back button and Title
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f, fill = false)
                            ) {
                                IconButton(onClick = onDismiss) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "بازگشت",
                                        tint = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                Column {
                                    Text(
                                        text = "پیش‌نمایش سند صورتحساب",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${PersianUtils.formatStudioName(invoice.studioName)} • شماره: ${invoice.invoiceNumber}",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 11.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            // Center/Left: Zoom & Action Buttons
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Zoom Controls Pill
                                Surface(
                                    color = Color(0xFF334155),
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        IconButton(
                                            onClick = { zoomLevel = (zoomLevel - 0.15f).coerceAtLeast(0.7f) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.ZoomOut,
                                                contentDescription = "کوچک‌نمایی",
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }

                                        Text(
                                            text = "${PersianUtils.faNum((zoomLevel * 100).toInt())}٪",
                                            color = Color.White,
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier
                                                .padding(horizontal = 4.dp)
                                                .clickable { zoomLevel = 1.0f }
                                        )

                                        IconButton(
                                            onClick = { zoomLevel = (zoomLevel + 0.15f).coerceAtMost(1.6f) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.ZoomIn,
                                                contentDescription = "بزرگ‌نمایی",
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }

                                // 1. Primary Action: Save Image (High Visual Intensity)
                                Button(
                                    onClick = { InvoiceExporter.saveInvoiceAsPng(context, invoice) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Download,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "ذخیره تصویر",
                                        fontSize = 11.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                // 2. Secondary Action: Share Image (Medium Intensity)
                                FilledTonalButton(
                                    onClick = { InvoiceExporter.shareInvoiceAsPng(context, invoice) },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Share,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "اشتراک",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                // 3. Document Action: PDF (Outlined / Clean)
                                OutlinedButton(
                                    onClick = { InvoiceExporter.shareInvoiceAsPdf(context, invoice) },
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF818CF8)),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        Icons.Default.PictureAsPdf,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = Color(0xFFC7D2FE)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "PDF",
                                        fontSize = 11.sp,
                                        color = Color(0xFFC7D2FE),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // --- DOCUMENT PREVIEW CANVAS ---
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(scrollState)
                            .padding(horizontal = 16.dp, vertical = 20.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Box(
                            modifier = Modifier.graphicsLayer {
                                scaleX = zoomLevel
                                scaleY = zoomLevel
                                transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 0f)
                            }
                        ) {
                            InvoiceDocumentPaper(invoice = invoice)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Pure, high-contrast, professional Statement/Invoice Paper layout (white page, printable, crisp typography).
 * Features fully responsive layout:
 * - On compact mobile screens (< 520dp): Structured, scannable project cards preventing squished numbers
 * - On wide screens (>= 520dp): Formal multi-column financial table
 */
@Composable
fun InvoiceDocumentPaper(invoice: InvoiceData) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 740.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header Top Bar Accent (CutterLog Purple Brand Line)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF4F46E5))
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- HEADER CONTENT ---
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val isCompact = maxWidth < 480.dp

                if (isCompact) {
                    // Stacked for small mobile
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFEEF2FF),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.ReceiptLong,
                                            contentDescription = null,
                                            tint = Color(0xFF4F46E5),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = invoice.brandTitle.ifBlank { "کاترلاگ" },
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF4338CA)
                                    )
                                    Text(
                                        text = "صورتحساب خدمات تدوین و ادیت ویدئو",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF475569)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Studio Recipient Chip
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFF1F5F9),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Business,
                                    contentDescription = null,
                                    tint = Color(0xFF64748B),
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "طرف حساب: ${PersianUtils.formatStudioName(invoice.studioName)}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Metadata Details Box
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF8FAFC),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("شماره صورتحساب:", fontSize = 11.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                                    Text(invoice.invoiceNumber, fontSize = 11.sp, color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("تاریخ صدور:", fontSize = 11.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                                    Text(PersianUtils.faNum(invoice.issueDate), fontSize = 11.sp, color = Color(0xFF0F172A))
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("وضعیت سند:", fontSize = 11.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                                    val isSettled = invoice.totalRemaining <= 0
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (isSettled) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                                            contentDescription = null,
                                            tint = if (isSettled) Color(0xFF16A34A) else Color(0xFFDC2626),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = invoice.accountStatus,
                                            fontSize = 11.sp,
                                            color = if (isSettled) Color(0xFF16A34A) else Color(0xFFDC2626),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Standard Row Layout for wider screens
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        // Brand and Service info
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFEEF2FF),
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.ReceiptLong,
                                            contentDescription = null,
                                            tint = Color(0xFF4F46E5),
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = invoice.brandTitle.ifBlank { "کاترلاگ" },
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF4338CA)
                                    )
                                    Text(
                                        text = "صورتحساب خدمات تدوین و ادیت ویدئو",
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF475569)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFF1F5F9),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Business,
                                        contentDescription = null,
                                        tint = Color(0xFF64748B),
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "طرف حساب: ${PersianUtils.formatStudioName(invoice.studioName)}",
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A)
                                    )
                                }
                            }
                        }

                        // Metadata Box on Left
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF8FAFC),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier.widthIn(min = 220.dp)
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("شماره صورتحساب: ", fontSize = 11.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                                    Text(invoice.invoiceNumber, fontSize = 11.sp, color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("تاریخ صدور: ", fontSize = 11.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                                    Text(PersianUtils.faNum(invoice.issueDate), fontSize = 11.sp, color = Color(0xFF0F172A))
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("وضعیت حساب: ", fontSize = 11.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                                    val isSettled = invoice.totalRemaining <= 0
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (isSettled) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                                            contentDescription = null,
                                            tint = if (isSettled) Color(0xFF16A34A) else Color(0xFFDC2626),
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = invoice.accountStatus,
                                            fontSize = 11.sp,
                                            color = if (isSettled) Color(0xFF16A34A) else Color(0xFFDC2626),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            // --- PROJECTS SECTION (RESPONSIVE TABLE) ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "شرح پروژه‌ها و خدمات انجام‌شده",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF1F5F9)
                ) {
                    Text(
                        text = "${PersianUtils.faNum(invoice.items.size)} پروژه",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF475569),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val isMobileWidth = maxWidth < 540.dp

                if (isMobileWidth) {
                    // MOBILE ADAPTIVE: Structured Project Cards to prevent line-wrapping or column clipping
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        invoice.items.forEachIndexed { _, item ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFF8FAFC),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    // Row 1: Index Badge + Project Name + Date
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
                                                shape = CircleShape,
                                                color = Color(0xFFE2E8F0),
                                                modifier = Modifier.size(20.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(
                                                        text = PersianUtils.faNum(item.rowNumber),
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF334155)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = item.projectName,
                                                fontSize = 12.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF0F172A),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        Text(
                                            text = PersianUtils.faNum(item.date),
                                            fontSize = 10.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 0.5.dp)
                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Row 2: 3-column financial strip
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        // Total Price
                                        Column(horizontalAlignment = Alignment.Start) {
                                            Text("مبلغ کل:", fontSize = 9.5.sp, color = Color(0xFF64748B))
                                            Text(
                                                text = PersianUtils.formatCurrencyFa(item.totalPrice),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF0F172A)
                                            )
                                        }

                                        // Paid
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("پرداخت‌شده:", fontSize = 9.5.sp, color = Color(0xFF64748B))
                                            Text(
                                                text = PersianUtils.formatCurrencyFa(item.paidAmount),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF16A34A)
                                            )
                                        }

                                        // Remaining
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("مانده طلب:", fontSize = 9.5.sp, color = Color(0xFF64748B))
                                            val debtColor = if (item.remainingBalance > 0) Color(0xFFDC2626) else Color(0xFF16A34A)
                                            Text(
                                                text = PersianUtils.formatCurrencyFa(item.remainingBalance),
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = debtColor
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // WIDE SCREENS (TABLET / DESKTOP): Formal Multi-Column Table
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Headers
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFF1F5F9),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp, horizontal = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("ردیف", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B), modifier = Modifier.width(36.dp), textAlign = TextAlign.Center)
                                Text("شرح پروژه", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B), modifier = Modifier.weight(2.2f), textAlign = TextAlign.Start)
                                Text("تاریخ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B), modifier = Modifier.weight(1.1f), textAlign = TextAlign.Center)
                                Text("مبلغ کل", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B), modifier = Modifier.weight(1.3f), textAlign = TextAlign.Center)
                                Text("پرداخت‌شده", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B), modifier = Modifier.weight(1.3f), textAlign = TextAlign.Center)
                                Text("مانده", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B), modifier = Modifier.weight(1.3f), textAlign = TextAlign.Center)
                            }
                        }

                        // Data Rows
                        invoice.items.forEachIndexed { idx, item ->
                            val bg = if (idx % 2 == 1) Color(0xFFF8FAFC) else Color.White
                            Surface(
                                color = bg,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 9.dp, horizontal = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(PersianUtils.faNum(item.rowNumber), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569), modifier = Modifier.width(36.dp), textAlign = TextAlign.Center)
                                    Text(item.projectName, fontSize = 11.5.sp, fontWeight = FontWeight.Medium, color = Color(0xFF0F172A), modifier = Modifier.weight(2.2f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(PersianUtils.faNum(item.date), fontSize = 10.sp, color = Color(0xFF64748B), modifier = Modifier.weight(1.1f), textAlign = TextAlign.Center)
                                    Text(PersianUtils.formatCurrencyFa(item.totalPrice), fontSize = 11.sp, color = Color(0xFF0F172A), modifier = Modifier.weight(1.3f), textAlign = TextAlign.Center)
                                    Text(PersianUtils.formatCurrencyFa(item.paidAmount), fontSize = 11.sp, color = Color(0xFF16A34A), modifier = Modifier.weight(1.3f), textAlign = TextAlign.Center)
                                    val debtColor = if (item.remainingBalance > 0) Color(0xFFDC2626) else Color(0xFF16A34A)
                                    Text(
                                        PersianUtils.formatCurrencyFa(item.remainingBalance),
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = debtColor,
                                        modifier = Modifier.weight(1.3f),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                            HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 0.5.dp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- FINANCIAL SUMMARY & BANKING SECTION ---
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val isCompactSummary = maxWidth < 540.dp

                if (isCompactSummary) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Summary Card
                        InvoiceSummaryCalculationCard(invoice = invoice, modifier = Modifier.fillMaxWidth())

                        // Bank Info (if present)
                        if (!invoice.bankCard.isNullOrBlank()) {
                            InvoiceBankInfoCard(invoice = invoice, modifier = Modifier.fillMaxWidth())
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        // Bank Details on the Right (in RTL)
                        if (!invoice.bankCard.isNullOrBlank()) {
                            InvoiceBankInfoCard(
                                invoice = invoice,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 12.dp)
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }

                        // Summary Calculation Card on the Left (in RTL)
                        InvoiceSummaryCalculationCard(
                            invoice = invoice,
                            modifier = Modifier.width(300.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // --- ACCOUNT STATUS BANNER ---
            val isSettled = invoice.totalRemaining <= 0
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = if (isSettled) Color(0xFFECFDF5) else Color(0xFFFFF1F2),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isSettled) Color(0xFFA7F3D0) else Color(0xFFFECDD3)
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isSettled) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                        contentDescription = null,
                        tint = if (isSettled) Color(0xFF059669) else Color(0xFFE11D48),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isSettled) {
                            "وضعیت حساب: تسویه شده (تمامی تعهدات مالی این صورتحساب پرداخت شده است)."
                        } else {
                            "وضعیت حساب: تسویه نشده — دارای مانده بدهی به مبلغ ${PersianUtils.formatCurrencyFa(invoice.totalRemaining)}."
                        },
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSettled) Color(0xFF065F46) else Color(0xFF9F1239)
                    )
                }
            }

            // --- FOOTER NOTE & OFFICIAL STAMP ---
            if (!invoice.footerNote.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFF8FAFC),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "یادداشت: ${invoice.footerNote}",
                        fontSize = 11.sp,
                        color = Color(0xFF475569),
                        modifier = Modifier.padding(10.dp),
                        textAlign = TextAlign.Start
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp))

            // Printable / Official Brand Signature
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "سامانه مدیریت و پایش تدوین فیلم کاترلاگ (CutterLog)",
                    fontSize = 10.sp,
                    color = Color(0xFF94A3B8)
                )
                Text(
                    text = "تاریخ سند: ${PersianUtils.faNum(invoice.issueDate)}",
                    fontSize = 10.sp,
                    color = Color(0xFF94A3B8)
                )
            }
        }
    }
}

/**
 * Clean Calculation Summary Card for Invoice Totals
 */
@Composable
private fun InvoiceSummaryCalculationCard(
    invoice: InvoiceData,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFF8FAFC),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "خلاصه مالی صورتحساب",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF334155)
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Total Amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("مبلغ کل پروژه‌ها:", fontSize = 11.sp, color = Color(0xFF475569), fontWeight = FontWeight.Bold)
                Text(PersianUtils.formatCurrencyFa(invoice.totalAmount), fontSize = 11.5.sp, color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(6.dp))

            // Total Paid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("مجموع پرداختی‌ها:", fontSize = 11.sp, color = Color(0xFF475569), fontWeight = FontWeight.Bold)
                Text(PersianUtils.formatCurrencyFa(invoice.totalPaid), fontSize = 11.5.sp, color = Color(0xFF16A34A), fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(6.dp))

            HorizontalDivider(color = Color(0xFFCBD5E1), thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(6.dp))

            // Remaining Total - High Visual Prominence
            val isRemaining = invoice.totalRemaining > 0
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (isRemaining) Color(0xFFFFF1F2) else Color(0xFFECFDF5),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "مانده کل قابل پرداخت:",
                        fontSize = 11.5.sp,
                        color = if (isRemaining) Color(0xFF9F1239) else Color(0xFF065F46),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = PersianUtils.formatCurrencyFa(invoice.totalRemaining),
                        fontSize = 13.5.sp,
                        color = if (isRemaining) Color(0xFFDC2626) else Color(0xFF16A34A),
                        fontWeight = FontWeight.Black
                    )
                }
            }

            if (invoice.totalRemaining > 0) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "به حروف: ${PersianUtils.numberToWordsFa(invoice.totalRemaining, "تومان")}",
                    fontSize = 10.5.sp,
                    color = Color(0xFF9F1239),
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }
    }
}

/**
 * Bank Details Card for Settling Invoice
 */
@Composable
private fun InvoiceBankInfoCard(
    invoice: InvoiceData,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFEEF2FF),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC7D2FE))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.CreditCard,
                    contentDescription = null,
                    tint = Color(0xFF4F46E5),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    "اطلاعات واریز به حساب:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF3730A3)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "شماره کارت: ${PersianUtils.faNum(invoice.bankCard ?: "")}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E1B4B)
            )
            if (!invoice.bankOwner.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    "صاحب حساب: ${invoice.bankOwner}",
                    fontSize = 11.sp,
                    color = Color(0xFF4338CA)
                )
            }
        }
    }
}
