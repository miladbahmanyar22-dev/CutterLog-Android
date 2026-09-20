package com.example.ui.components

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.entity.PaymentEntity
import com.example.data.entity.ProjectClipEntity
import com.example.data.entity.ProjectEntity
import com.example.data.entity.ProjectRevisionEntity
import com.example.data.entity.TimerSessionEntity
import com.example.data.model.ProjectReportData
import com.example.data.model.RevisionRoundSummary
import com.example.ui.theme.BorderDark
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber
import com.example.util.PersianUtils
import com.example.util.ProjectReportExporter

/**
 * Official Project Report Preview Dialog
 *
 * Professional, clean, responsive internal report preview crafted for
 * video editing project management. Strictly preserves all existing data,
 * business logic, calculations, and PDF/PNG export renderers.
 */
@Composable
fun ProjectReportDialog(
    project: ProjectEntity,
    clips: List<ProjectClipEntity>,
    payments: List<PaymentEntity>,
    sessions: List<TimerSessionEntity>,
    revisions: List<ProjectRevisionEntity>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val report = ProjectReportData(
        project = project,
        clips = clips,
        payments = payments,
        sessions = sessions,
        revisions = revisions
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true
        )
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.90f)),
                color = DarkBg
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // ================= TOP ACTION BAR =================
                    ReportTopBar(
                        report = report,
                        context = context,
                        onDismiss = onDismiss
                    )

                    HorizontalDivider(color = BorderDark, thickness = 1.dp)

                    // ================= DOCUMENT VIEWER AREA =================
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(Color(0xFF060910)),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxHeight()
                                .widthIn(max = 840.dp)
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 12.dp, vertical = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Official Document Paper Card
                            OfficialDocumentPaper(report = report)

                            Spacer(modifier = Modifier.height(28.dp))
                        }
                    }
                }
            }
        }
    }
}

// ================= TOP ACTION BAR =================
@Composable
private fun ReportTopBar(
    report: ProjectReportData,
    context: Context,
    onDismiss: () -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        val isNarrow = maxWidth < 640.dp

        if (isNarrow) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Row 1: Back/Close button + Document Info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("btn_close_report")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "بستن",
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Article,
                                contentDescription = null,
                                tint = PrimaryPurple,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "پیش‌نمایش رسمی گزارش پروژه",
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontSize = 13.5.sp
                            )
                        }
                        Text(
                            text = "${report.project.name} • ${report.reportNumber}",
                            color = TextMuted,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Row 2: Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { ProjectReportExporter.saveReportAsPng(context, report) },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                        modifier = Modifier
                            .weight(1.25f)
                            .height(40.dp)
                            .testTag("btn_export_image")
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(15.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("خروجی تصویر", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { ProjectReportExporter.shareReportAsPng(context, report) },
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, BorderDark),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                        modifier = Modifier
                            .weight(0.95f)
                            .height(40.dp)
                            .testTag("btn_share_image")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp), tint = TextSecondary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("اشتراک", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = { ProjectReportExporter.shareReportAsPdf(context, report) },
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFF6366F1).copy(alpha = 0.7f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFA5B4FC)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                        modifier = Modifier
                            .weight(1.15f)
                            .height(40.dp)
                            .testTag("btn_export_pdf")
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFFA5B4FC))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("خروجی PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            // Desktop / Tablet Horizontal Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("btn_close_report")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "بستن",
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Article, contentDescription = null, tint = PrimaryPurple, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "پیش‌نمایش سند رسمی گزارش پروژه",
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontSize = 14.sp
                            )
                        }
                        Text(
                            text = "${report.project.name} • ${report.studioDisplayName} • ${report.reportNumber}",
                            color = TextSecondary,
                            fontSize = 11.5.sp
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { ProjectReportExporter.saveReportAsPng(context, report) },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        modifier = Modifier
                            .height(40.dp)
                            .testTag("btn_export_image")
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("خروجی تصویر (PNG)", fontSize = 11.5.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { ProjectReportExporter.shareReportAsPng(context, report) },
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, BorderDark),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        modifier = Modifier
                            .height(40.dp)
                            .testTag("btn_share_image")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(15.dp), tint = TextSecondary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("اشتراک تصویر", fontSize = 11.5.sp)
                    }

                    OutlinedButton(
                        onClick = { ProjectReportExporter.shareReportAsPdf(context, report) },
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFF6366F1).copy(alpha = 0.7f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFA5B4FC)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        modifier = Modifier
                            .height(40.dp)
                            .testTag("btn_export_pdf")
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(15.dp), tint = Color(0xFFA5B4FC))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("خروجی رسمی PDF", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ================= OFFICIAL DOCUMENT PAPER =================
@Composable
private fun OfficialDocumentPaper(report: ProjectReportData) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("project_report_paper"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0B1120)),
        border = BorderStroke(1.2.dp, Color(0xFF1E293B))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Executive Top Accent Line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.5.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                PrimaryPurple,
                                Color(0xFF818CF8),
                                Color(0xFF22D3EE)
                            )
                        )
                    )
            )

            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val isCompact = maxWidth < 540.dp
                val contentPadding = if (isCompact) 14.dp else 22.dp

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(contentPadding)
                ) {
                    // 1. Official Header
                    ReportHeaderSection(report = report)

                    Spacer(modifier = Modifier.height(18.dp))
                    HorizontalDivider(color = BorderDark.copy(alpha = 0.6f), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(18.dp))

                    // 2. Project Information Grid
                    ReportProjectInfoSection(report = report)

                    Spacer(modifier = Modifier.height(20.dp))

                    // 3. Project Summary & Metrics
                    ReportProjectSummarySection(report = report)

                    Spacer(modifier = Modifier.height(22.dp))

                    // 4. Delivered Clips Table / List
                    ReportClipsDetailSection(report = report)

                    Spacer(modifier = Modifier.height(22.dp))

                    // 5. Revision & Review History
                    ReportRevisionHistorySection(report = report)

                    Spacer(modifier = Modifier.height(22.dp))

                    // 6. Financial Account & Settlement
                    ReportFinancialSection(report = report)

                    Spacer(modifier = Modifier.height(20.dp))

                    // 7. Delivery Status & Archival Record
                    ReportDeliveryStatusSection(report = report)

                    // 8. Remarks & Notes (if any)
                    ReportNotesSection(report = report)

                    Spacer(modifier = Modifier.height(20.dp))
                    HorizontalDivider(color = BorderDark.copy(alpha = 0.6f), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(14.dp))

                    // 9. Document Footer & Digital Seal
                    ReportDocumentFooter(report = report)
                }
            }
        }
    }
}

// ================= 1. REPORT HEADER SECTION =================
@Composable
private fun ReportHeaderSection(report: ProjectReportData) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val isNarrow = maxWidth < 540.dp

        if (isNarrow) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Brand Badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = PrimaryPurple.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, PrimaryPurple.copy(alpha = 0.35f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Movie,
                                contentDescription = null,
                                tint = PrimaryPurple,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = report.brandTitle,
                                color = PrimaryPurple,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    // Archival Status Pill
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = SuccessGreen.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.35f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(5.dp).background(SuccessGreen, CircleShape))
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "مختومه در آرشیو",
                                color = SuccessGreen,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Text(
                    text = "گزارش رسمی پروژه تدوین",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    fontSize = 17.sp
                )
                Text(
                    text = "شناسنامه جامع عملیات، کارکرد و مشخصات تحویل نهایی",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 17.sp
                )

                // Meta Box (Document Number + Issue Date)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF0F172A),
                    border = BorderStroke(1.dp, BorderDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("شماره سند: ", color = TextMuted, fontSize = 10.5.sp)
                            Text(report.reportNumber, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("تاریخ صدور: ", color = TextMuted, fontSize = 10.5.sp)
                            Text(PersianUtils.faNum(report.reportDate), color = TextSecondary, fontSize = 11.sp)
                        }
                    }
                }
            }
        } else {
            // Wide Screen Header Layout
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = PrimaryPurple.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, PrimaryPurple.copy(alpha = 0.35f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Movie,
                                contentDescription = null,
                                tint = PrimaryPurple,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = report.brandTitle,
                                color = PrimaryPurple,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "گزارش رسمی پروژه تدوین",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "شناسنامه جامع عملیات، کارکرد و مشخصات تحویل نهایی",
                        color = TextSecondary,
                        fontSize = 11.5.sp
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF0F172A),
                    border = BorderStroke(1.dp, BorderDark)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("شماره سند:", color = TextMuted, fontSize = 10.5.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                report.reportNumber,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("تاریخ صدور:", color = TextMuted, fontSize = 10.5.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                PersianUtils.faNum(report.reportDate),
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("وضعیت پرونده:", color = TextMuted, fontSize = 10.5.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "مختومه در آرشیو",
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
}

// ================= 2. PROJECT IDENTITY SECTION =================
@Composable
private fun ReportProjectInfoSection(report: ProjectReportData) {
    Column(modifier = Modifier.fillMaxWidth()) {
        ReportSectionHeading(
            number = "۱",
            title = "شناسنامه و مشخصات پرونده پروژه"
        )
        Spacer(modifier = Modifier.height(10.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFF0F172A),
            border = BorderStroke(1.dp, BorderDark)
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val width = maxWidth

                if (width < 500.dp) {
                    // Mobile: Single column with dedicated Key-Value rows
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ReportFieldRow(label = "نام پروژه", value = report.project.name, isBold = true)
                        HorizontalDivider(color = BorderDark.copy(alpha = 0.35f), thickness = 0.6.dp)

                        ReportFieldRow(label = "استودیو طرف حساب", value = report.studioDisplayName, isAccent = true)
                        HorizontalDivider(color = BorderDark.copy(alpha = 0.35f), thickness = 0.6.dp)

                        val weddingTxt = if (!report.project.weddingDate.isNullOrBlank()) {
                            PersianUtils.faNum(report.project.weddingDate)
                        } else "ثبت نشده"
                        ReportFieldRow(label = "تاریخ روز پروژه / مراسم", value = weddingTxt)
                        HorizontalDivider(color = BorderDark.copy(alpha = 0.35f), thickness = 0.6.dp)

                        ReportFieldRow(label = "تاریخ ثبت در سامانه", value = PersianUtils.faNum(report.project.createdAt))
                        HorizontalDivider(color = BorderDark.copy(alpha = 0.35f), thickness = 0.6.dp)

                        val deadlineTxt = if (!report.project.deadlineDate.isNullOrBlank()) {
                            PersianUtils.faNum(report.project.deadlineDate)
                        } else "نامحدود"
                        ReportFieldRow(label = "موعد تحویل اولیه", value = deadlineTxt)
                        HorizontalDivider(color = BorderDark.copy(alpha = 0.35f), thickness = 0.6.dp)

                        ReportFieldRow(label = "شناسه سیستمی پرونده", value = "#${PersianUtils.faNum(report.project.id)}")
                    }
                } else if (width < 720.dp) {
                    // Tablet: 2 Columns
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            ReportFieldBlock(label = "نام پروژه", value = report.project.name, isBold = true, modifier = Modifier.weight(1f))
                            ReportFieldBlock(label = "استودیو طرف حساب", value = report.studioDisplayName, isAccent = true, modifier = Modifier.weight(1f))
                        }
                        HorizontalDivider(color = BorderDark.copy(alpha = 0.35f), thickness = 0.6.dp)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            val weddingTxt = if (!report.project.weddingDate.isNullOrBlank()) {
                                PersianUtils.faNum(report.project.weddingDate)
                            } else "ثبت نشده"
                            ReportFieldBlock(label = "تاریخ روز پروژه / مراسم", value = weddingTxt, modifier = Modifier.weight(1f))
                            ReportFieldBlock(label = "تاریخ ثبت در سامانه", value = PersianUtils.faNum(report.project.createdAt), modifier = Modifier.weight(1f))
                        }
                        HorizontalDivider(color = BorderDark.copy(alpha = 0.35f), thickness = 0.6.dp)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            val deadlineTxt = if (!report.project.deadlineDate.isNullOrBlank()) {
                                PersianUtils.faNum(report.project.deadlineDate)
                            } else "نامحدود"
                            ReportFieldBlock(label = "موعد تحویل اولیه", value = deadlineTxt, modifier = Modifier.weight(1f))
                            ReportFieldBlock(label = "شناسه سیستمی پرونده", value = "#${PersianUtils.faNum(report.project.id)}", modifier = Modifier.weight(1f))
                        }
                    }
                } else {
                    // Desktop: 3 Columns
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            ReportFieldBlock(label = "نام پروژه", value = report.project.name, isBold = true, modifier = Modifier.weight(1.3f))
                            ReportFieldBlock(label = "استودیو طرف حساب", value = report.studioDisplayName, isAccent = true, modifier = Modifier.weight(1f))
                            val weddingTxt = if (!report.project.weddingDate.isNullOrBlank()) {
                                PersianUtils.faNum(report.project.weddingDate)
                            } else "ثبت نشده"
                            ReportFieldBlock(label = "تاریخ روز مراسم", value = weddingTxt, modifier = Modifier.weight(1f))
                        }
                        HorizontalDivider(color = BorderDark.copy(alpha = 0.35f), thickness = 0.6.dp)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            ReportFieldBlock(label = "تاریخ ثبت در سامانه", value = PersianUtils.faNum(report.project.createdAt), modifier = Modifier.weight(1.3f))
                            val deadlineTxt = if (!report.project.deadlineDate.isNullOrBlank()) {
                                PersianUtils.faNum(report.project.deadlineDate)
                            } else "نامحدود"
                            ReportFieldBlock(label = "موعد تحویل اولیه", value = deadlineTxt, modifier = Modifier.weight(1f))
                            ReportFieldBlock(label = "شناسه سیستمی پرونده", value = "#${PersianUtils.faNum(report.project.id)}", modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportFieldRow(
    label: String,
    value: String,
    isBold: Boolean = false,
    isAccent: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label:",
            color = TextSecondary,
            fontSize = 11.5.sp,
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(
            text = value,
            color = if (isAccent) PrimaryPurple else TextPrimary,
            fontWeight = if (isBold || isAccent) FontWeight.Bold else FontWeight.Medium,
            fontSize = 12.sp,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f, fill = false)
        )
    }
}

@Composable
private fun ReportFieldBlock(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    isBold: Boolean = false,
    isAccent: Boolean = false
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            color = TextMuted,
            fontSize = 10.5.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            color = if (isAccent) PrimaryPurple else TextPrimary,
            fontWeight = if (isBold || isAccent) FontWeight.Bold else FontWeight.SemiBold,
            fontSize = 12.5.sp
        )
    }
}

// ================= 3. OPERATIONS SUMMARY & METRICS SECTION =================
@Composable
private fun ReportProjectSummarySection(report: ProjectReportData) {
    Column(modifier = Modifier.fillMaxWidth()) {
        ReportSectionHeading(
            number = "۲",
            title = "خلاصه عملیات و شاخص‌های تدوین"
        )
        Spacer(modifier = Modifier.height(10.dp))

        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val width = maxWidth

            if (width < 500.dp) {
                // Mobile: 3 rows of 2 columns each (Structured, clean, never clips words!)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SummaryMetricCard(
                            label = "کلیپ‌های ثبت‌شده",
                            value = "${PersianUtils.faNum(report.totalClipsCount)} کلیپ",
                            accentColor = TextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        SummaryMetricCard(
                            label = "کلیپ‌های تکمیل‌شده",
                            value = "${PersianUtils.faNum(report.completedClipsCount)} کلیپ",
                            accentColor = SuccessGreen,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SummaryMetricCard(
                            label = "کلیپ‌های باقیمانده",
                            value = if (report.remainingClipsCount == 0) "فاقد مانده" else "${PersianUtils.faNum(report.remainingClipsCount)} کلیپ",
                            accentColor = if (report.remainingClipsCount == 0) SuccessGreen else WarningAmber,
                            modifier = Modifier.weight(1f)
                        )
                        SummaryMetricCard(
                            label = "درصد پیشرفت",
                            value = "${PersianUtils.faNum(report.progressPercent)}٪",
                            accentColor = if (report.progressPercent == 100) SuccessGreen else WarningAmber,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SummaryMetricCard(
                            label = "زمان کارکرد خالص",
                            value = PersianUtils.faNum(report.formattedDurationHMS),
                            subtitle = report.formattedDurationPersianWords,
                            accentColor = PrimaryPurple,
                            modifier = Modifier.weight(1f)
                        )
                        SummaryMetricCard(
                            label = "وضعیت تحویل نهایی",
                            value = if (report.progressPercent == 100) "تحویل کامل" else "در حال تحویل",
                            accentColor = if (report.progressPercent == 100) SuccessGreen else WarningAmber,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            } else if (width < 740.dp) {
                // Tablet: 2 rows of 3 columns
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SummaryMetricCard(
                            label = "کلیپ‌های ثبت‌شده",
                            value = "${PersianUtils.faNum(report.totalClipsCount)} کلیپ",
                            accentColor = TextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        SummaryMetricCard(
                            label = "کلیپ‌های تکمیل‌شده",
                            value = "${PersianUtils.faNum(report.completedClipsCount)} کلیپ",
                            accentColor = SuccessGreen,
                            modifier = Modifier.weight(1f)
                        )
                        SummaryMetricCard(
                            label = "کلیپ‌های باقیمانده",
                            value = if (report.remainingClipsCount == 0) "صفر (تکمیل)" else "${PersianUtils.faNum(report.remainingClipsCount)} کلیپ",
                            accentColor = if (report.remainingClipsCount == 0) SuccessGreen else WarningAmber,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SummaryMetricCard(
                            label = "درصد پیشرفت",
                            value = "${PersianUtils.faNum(report.progressPercent)}٪",
                            accentColor = if (report.progressPercent == 100) SuccessGreen else WarningAmber,
                            modifier = Modifier.weight(1f)
                        )
                        SummaryMetricCard(
                            label = "زمان کارکرد خالص",
                            value = PersianUtils.faNum(report.formattedDurationHMS),
                            subtitle = report.formattedDurationPersianWords,
                            accentColor = PrimaryPurple,
                            modifier = Modifier.weight(1.3f)
                        )
                        SummaryMetricCard(
                            label = "وضعیت تحویل نهایی",
                            value = if (report.progressPercent == 100) "تحویل کامل" else "در حال تحویل",
                            accentColor = if (report.progressPercent == 100) SuccessGreen else WarningAmber,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            } else {
                // Desktop: 6 Columns Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SummaryMetricCard(
                        label = "کلیپ‌های ثبت‌شده",
                        value = "${PersianUtils.faNum(report.totalClipsCount)} کلیپ",
                        accentColor = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    SummaryMetricCard(
                        label = "کلیپ‌های تکمیل‌شده",
                        value = "${PersianUtils.faNum(report.completedClipsCount)} کلیپ",
                        accentColor = SuccessGreen,
                        modifier = Modifier.weight(1f)
                    )
                    SummaryMetricCard(
                        label = "کلیپ‌های باقیمانده",
                        value = if (report.remainingClipsCount == 0) "فاقد مانده" else "${PersianUtils.faNum(report.remainingClipsCount)} کلیپ",
                        accentColor = if (report.remainingClipsCount == 0) SuccessGreen else WarningAmber,
                        modifier = Modifier.weight(1f)
                    )
                    SummaryMetricCard(
                        label = "درصد پیشرفت",
                        value = "${PersianUtils.faNum(report.progressPercent)}٪",
                        accentColor = if (report.progressPercent == 100) SuccessGreen else WarningAmber,
                        modifier = Modifier.weight(1f)
                    )
                    SummaryMetricCard(
                        label = "زمان کارکرد خالص",
                        value = PersianUtils.faNum(report.formattedDurationHMS),
                        subtitle = report.formattedDurationPersianWords,
                        accentColor = PrimaryPurple,
                        modifier = Modifier.weight(1.2f)
                    )
                    SummaryMetricCard(
                        label = "وضعیت تحویل",
                        value = if (report.progressPercent == 100) "تحویل کامل" else "در حال تکمیل",
                        accentColor = if (report.progressPercent == 100) SuccessGreen else WarningAmber,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryMetricCard(
    label: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF0F172A),
        border = BorderStroke(1.dp, BorderDark)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 9.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                color = TextMuted,
                fontSize = 10.5.sp,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = value,
                color = accentColor,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = TextSecondary,
                    fontSize = 9.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// ================= 4. DELIVERED CLIPS BREAKDOWN SECTION =================
@Composable
private fun ReportClipsDetailSection(report: ProjectReportData) {
    Column(modifier = Modifier.fillMaxWidth()) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val isNarrow = maxWidth < 480.dp

            if (isNarrow) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    ReportSectionHeading(
                        number = "۳",
                        title = "فهرست کلیپ‌های تدوین‌شده و تحویل"
                    )
                    Text(
                        text = "${PersianUtils.faNum(report.clips.size)} کلیپ ثبت‌شده در پرونده",
                        color = TextMuted,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(start = 28.dp)
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ReportSectionHeading(
                        number = "۳",
                        title = "فهرست تفکیکی کلیپ‌ها و وضعیت تحویل"
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF1E293B).copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = "${PersianUtils.faNum(report.clips.size)} کلیپ ثبت‌شده",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF0F172A),
            border = BorderStroke(1.dp, BorderDark)
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val isNarrow = maxWidth < 560.dp

                if (isNarrow) {
                    // Mobile Adaptive Item Rows
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        report.clips.forEachIndexed { index, clip ->
                            val isDone = clip.isDone == 1
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(6.dp),
                                color = if (index % 2 == 0) Color(0xFF131D31) else Color(0xFF0C1322),
                                border = BorderStroke(0.8.dp, BorderDark.copy(alpha = 0.5f))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = "${PersianUtils.faNum(index + 1)}.",
                                                color = TextMuted,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = clip.clipName,
                                                color = TextPrimary,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                lineHeight = 17.sp
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(6.dp))

                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = if (isDone) SuccessGreen.copy(alpha = 0.15f) else WarningAmber.copy(alpha = 0.15f),
                                            border = BorderStroke(1.dp, if (isDone) SuccessGreen.copy(alpha = 0.4f) else WarningAmber.copy(alpha = 0.4f))
                                        ) {
                                            Text(
                                                text = if (isDone) "تحویل کامل" else "در حال ادیت",
                                                color = if (isDone) SuccessGreen else WarningAmber,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (clip.estimateMins > 0) "برآورد: ${PersianUtils.faNum(clip.estimateMins)} دقیقه" else "برآورد: —",
                                            color = TextSecondary,
                                            fontSize = 10.5.sp
                                        )

                                        if (!clip.endDate.isNullOrBlank()) {
                                            Text(
                                                text = "تکمیل: ${PersianUtils.faNum(clip.endDate)}",
                                                color = TextSecondary,
                                                fontSize = 10.5.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Standard Tabular Format for Wide Screens
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF162032))
                                .padding(horizontal = 14.dp, vertical = 9.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("ردیف", color = TextSecondary, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.width(40.dp))
                            Text("عنوان کلیپ", color = TextSecondary, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1.6f))
                            Text("زمان برآورد", color = TextSecondary, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(0.9f))
                            Text("تاریخ تحویل", color = TextSecondary, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1f))
                            Text("وضعیت نهایی", color = TextSecondary, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                        }

                        HorizontalDivider(color = BorderDark, thickness = 0.8.dp)

                        report.clips.forEachIndexed { index, clip ->
                            val isDone = clip.isDone == 1
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(if (index % 2 == 0) Color(0xFF0F172A) else Color(0xFF0B111D))
                                    .padding(horizontal = 14.dp, vertical = 9.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = PersianUtils.faNum(index + 1),
                                    color = TextMuted,
                                    fontSize = 11.sp,
                                    modifier = Modifier.width(40.dp)
                                )
                                Text(
                                    text = clip.clipName,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 11.5.sp,
                                    modifier = Modifier.weight(1.6f)
                                )
                                Text(
                                    text = if (clip.estimateMins > 0) "${PersianUtils.faNum(clip.estimateMins)} دقیقه" else "—",
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    modifier = Modifier.weight(0.9f)
                                )
                                Text(
                                    text = if (!clip.endDate.isNullOrBlank()) PersianUtils.faNum(clip.endDate) else "—",
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    modifier = Modifier.weight(1f)
                                )

                                Row(
                                    modifier = Modifier.weight(1f),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = if (isDone) SuccessGreen.copy(alpha = 0.12f) else WarningAmber.copy(alpha = 0.12f),
                                        border = BorderStroke(1.dp, if (isDone) SuccessGreen.copy(alpha = 0.35f) else WarningAmber.copy(alpha = 0.35f))
                                    ) {
                                        Text(
                                            text = if (isDone) "تحویل کامل" else "در حال ادیت",
                                            color = if (isDone) SuccessGreen else WarningAmber,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            if (index < report.clips.size - 1) {
                                HorizontalDivider(color = BorderDark.copy(alpha = 0.3f), thickness = 0.5.dp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ================= 5. REVISION HISTORY SECTION =================
@Composable
private fun ReportRevisionHistorySection(report: ProjectReportData) {
    Column(modifier = Modifier.fillMaxWidth()) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val isNarrow = maxWidth < 480.dp

            if (isNarrow) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    ReportSectionHeading(
                        number = "۴",
                        title = "سوابق جامع اصلاحات و بازبینی"
                    )
                    if (report.revisionRounds.isNotEmpty()) {
                        Text(
                            text = "${PersianUtils.faNum(report.revisionRounds.size)} دور اصلاحیه ثبت‌شده",
                            color = TextMuted,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(start = 28.dp)
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ReportSectionHeading(
                        number = "۴",
                        title = "سوابق جامع اصلاحات و بازبینی"
                    )
                    if (report.revisionRounds.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF1E293B).copy(alpha = 0.6f)
                        ) {
                            Text(
                                text = "${PersianUtils.faNum(report.revisionRounds.size)} دور اصلاحیه ثبت‌شده",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (report.revisionRounds.isEmpty()) {
            // Elegant First-Cut Approval Box
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF0F172A),
                border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.35f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = null,
                        tint = SuccessGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "تایید خروجی در نسخه اولیه (فاقد دور اصلاحیه)",
                            color = SuccessGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "هیچ اصلاحیه‌ای برای این پروژه ثبت نشده است. کلیپ‌های تدوین‌شده در نسخه اول مورد تایید نهایی قرار گرفته‌اند.",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 17.sp
                        )
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                report.revisionRounds.forEach { round ->
                    RevisionRoundCard(round = round)
                }
            }
        }
    }
}

@Composable
private fun RevisionRoundCard(round: RevisionRoundSummary) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF0F172A),
        border = BorderStroke(
            1.dp,
            if (round.isRoundComplete) BorderDark else WarningAmber.copy(alpha = 0.45f)
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Round Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (round.isRoundComplete) Color(0xFF162032) else Color(0xFF241A12)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = if (round.isRoundComplete) PrimaryPurple.copy(alpha = 0.2f) else WarningAmber.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = PersianUtils.faNum(round.phaseNum),
                            color = if (round.isRoundComplete) PrimaryPurple else WarningAmber,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "دور ${PersianUtils.faNum(round.phaseNum)} اصلاحیه  •  ${PersianUtils.faNum(round.totalCount)} مورد",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (round.isRoundComplete) SuccessGreen.copy(alpha = 0.15f) else WarningAmber.copy(alpha = 0.15f),
                    border = BorderStroke(
                        1.dp,
                        if (round.isRoundComplete) SuccessGreen.copy(alpha = 0.4f) else WarningAmber.copy(alpha = 0.4f)
                    )
                ) {
                    Text(
                        text = if (round.isRoundComplete) "✓ اعمال و نهایی شد" else "در انتظار اعمال",
                        color = if (round.isRoundComplete) SuccessGreen else WarningAmber,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            // Round Items List
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                round.items.forEachIndexed { idx, item ->
                    val isApplied = item.isApplied == 1
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = if (isApplied) Icons.Default.CheckCircle else Icons.Default.HourglassEmpty,
                                contentDescription = null,
                                tint = if (isApplied) SuccessGreen else TextMuted,
                                modifier = Modifier
                                    .padding(top = 2.dp)
                                    .size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(7.dp))
                            Text(
                                text = "مورد ${PersianUtils.faNum(idx + 1)}: ${item.description}",
                                color = if (isApplied) TextPrimary else TextSecondary,
                                fontSize = 11.5.sp,
                                lineHeight = 18.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = if (isApplied) "اعمال شد" else "معلق",
                            color = if (isApplied) SuccessGreen else TextMuted,
                            fontWeight = FontWeight.Medium,
                            fontSize = 10.5.sp,
                            modifier = Modifier.padding(top = 1.dp)
                        )
                    }

                    if (idx < round.items.size - 1) {
                        HorizontalDivider(color = BorderDark.copy(alpha = 0.35f), thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}

// ================= 6. FINANCIAL ACCOUNT & SETTLEMENT SECTION =================
@Composable
private fun ReportFinancialSection(report: ProjectReportData) {
    Column(modifier = Modifier.fillMaxWidth()) {
        ReportSectionHeading(
            number = "۵",
            title = "صورت‌حساب مالی، مبالغ و تسویه قرارداد"
        )
        Spacer(modifier = Modifier.height(10.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF0F172A),
            border = BorderStroke(1.dp, BorderDark)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // 3 Financial Highlights (Contract Price, Total Paid, Remaining Balance)
                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val width = maxWidth

                    if (width < 500.dp) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FinancialStatRow(label = "مبلغ کل قرارداد:", value = PersianUtils.formatCurrencyFa(report.project.price), color = TextPrimary)
                            HorizontalDivider(color = BorderDark.copy(alpha = 0.35f), thickness = 0.6.dp)
                            FinancialStatRow(label = "مبلغ پرداخت‌شده (دریافتی):", value = PersianUtils.formatCurrencyFa(report.totalPaid), color = SuccessGreen)
                            HorizontalDivider(color = BorderDark.copy(alpha = 0.35f), thickness = 0.6.dp)
                            FinancialStatRow(
                                label = "مبلغ باقیمانده (مانده حساب):",
                                value = if (report.isSettled) "تسویه کامل" else PersianUtils.formatCurrencyFa(report.remainingBalance),
                                color = if (report.isSettled) SuccessGreen else ErrorRed
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("مبلغ قرارداد", color = TextMuted, fontSize = 10.5.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(PersianUtils.formatCurrencyFa(report.project.price), color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                            }
                            Column {
                                Text("مبلغ پرداخت‌شده", color = TextMuted, fontSize = 10.5.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(PersianUtils.formatCurrencyFa(report.totalPaid), color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("مبلغ باقیمانده", color = TextMuted, fontSize = 10.5.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (report.isSettled) "تسویه شده" else PersianUtils.formatCurrencyFa(report.remainingBalance),
                                    color = if (report.isSettled) SuccessGreen else ErrorRed,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = BorderDark.copy(alpha = 0.45f), thickness = 0.8.dp)
                Spacer(modifier = Modifier.height(10.dp))

                // Price in Persian Words Banner
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("مبلغ قرارداد به حروف: ", color = TextSecondary, fontSize = 11.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${report.priceInWords} تومان",
                        color = Color(0xFFA5B4FC),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Settlement Verification Banner
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = if (report.isSettled) SuccessGreen.copy(alpha = 0.12f) else ErrorRed.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, if (report.isSettled) SuccessGreen.copy(alpha = 0.4f) else ErrorRed.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp, horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .background(if (report.isSettled) SuccessGreen else ErrorRed, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (report.isSettled) "✓ پرونده مالی با استودیو کاملاً تسویه شده است (فاقد بدهی معوق)" else "⚠ پرونده دارای مانده حساب پرداخت‌نشده است",
                            color = if (report.isSettled) SuccessGreen else ErrorRed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp
                        )
                    }
                }

                // Itemized Payments List (if any exist)
                if (report.payments.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text("ریز واریزی‌های ثبت‌شده:", color = TextSecondary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(6.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        report.payments.forEachIndexed { pIdx, payment ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF162032), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "واریز ${PersianUtils.faNum(pIdx + 1)} • ${PersianUtils.faNum(payment.date)}" +
                                            (if (!payment.note.isNullOrBlank()) " (${payment.note})" else ""),
                                    color = TextSecondary,
                                    fontSize = 10.5.sp
                                )
                                Text(
                                    text = PersianUtils.formatCurrencyFa(payment.amount),
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
    }
}

@Composable
private fun FinancialStatRow(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = TextSecondary, fontSize = 11.sp)
        Text(text = value, color = color, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}

// ================= 7. DELIVERY STATUS & ARCHIVAL RECORD SECTION =================
@Composable
private fun ReportDeliveryStatusSection(report: ProjectReportData) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF0F172A),
        border = BorderStroke(1.dp, BorderDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF60A5FA),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "وضعیت تحویل نهایی و ثبت در بایگانی کاترلاگ:",
                    color = Color(0xFF60A5FA),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.height(6.dp))

            val summaryText = if (report.isSettled && report.progressPercent == 100) {
                "کلیه کلیپ‌ها و مراحل تدوین به اتمام رسیده و پروژه پس از بازبینی و تسویه کامل، رسماً در سامانه بایگانی کاترلاگ مختومه گردید."
            } else {
                "سوابق پروژه و مراحل کار در تاریخ استخراج گزارش ثبت و در سیستم بایگانی کاترلاگ ذخیره شده است."
            }
            Text(
                text = summaryText,
                color = TextSecondary,
                fontSize = 11.sp,
                lineHeight = 17.sp
            )

            if (!report.lastDeliveryDate.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "تاریخ آخرین تحویل ثبت‌شده: ${PersianUtils.faNum(report.lastDeliveryDate)}",
                    color = TextMuted,
                    fontSize = 10.5.sp
                )
            }
        }
    }
}

// ================= 8. NOTES & REMARKS SECTION =================
@Composable
private fun ReportNotesSection(report: ProjectReportData) {
    val allNotes = report.sessions.mapNotNull { it.note }.filter { it.isNotBlank() } +
            report.payments.mapNotNull { it.note }.filter { it.isNotBlank() }
    val distinctNotes = allNotes.distinct()

    if (distinctNotes.isNotEmpty()) {
        Spacer(modifier = Modifier.height(14.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF0F172A),
            border = BorderStroke(1.dp, BorderDark)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Text(
                    text = "توضیحات تکمیلی و ملاحظات تدوینگر:",
                    color = WarningAmber,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                distinctNotes.forEach { note ->
                    Text(
                        text = "• $note",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }
    }
}

// ================= 9. DOCUMENT FOOTER =================
@Composable
private fun ReportDocumentFooter(report: ProjectReportData) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val isNarrow = maxWidth < 520.dp

        if (isNarrow) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "تولید شده توسط نرم‌افزار مدیریت تدوین کاترلاگ (CutterLog Workspace)",
                    color = TextMuted,
                    fontSize = 10.sp
                )
                Text(
                    text = "شناسه سند: ${report.reportNumber}  •  سند رسمی معتبر دیجیتال",
                    color = TextMuted,
                    fontSize = 9.5.sp
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "تولید شده توسط نرم‌افزار مدیریت تدوین کاترلاگ (CutterLog Workspace)",
                    color = TextMuted,
                    fontSize = 10.sp
                )
                Text(
                    text = "شناسه سند: ${report.reportNumber}  •  سند معتبر دیجیتال",
                    color = TextMuted,
                    fontSize = 9.5.sp
                )
            }
        }
    }
}

// ================= REUSABLE SECTION HEADING =================
@Composable
private fun ReportSectionHeading(
    number: String,
    title: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            shape = CircleShape,
            color = PrimaryPurple.copy(alpha = 0.18f),
            modifier = Modifier.size(20.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = number,
                    color = PrimaryPurple,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
    }
}
