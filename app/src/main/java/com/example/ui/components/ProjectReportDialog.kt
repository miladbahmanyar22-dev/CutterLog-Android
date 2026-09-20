package com.example.ui.components

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
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
 * Redesigned for official document aesthetics, clean responsive layout,
 * strict RTL, Persian typography, and zero clipping across all screen sizes.
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
                    .background(Color.Black.copy(alpha = 0.88f)),
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
                            .background(Color(0xFF070A11)),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxHeight()
                                .widthIn(max = 800.dp)
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 14.dp, vertical = 18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Official Document Paper Card
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("project_report_paper"),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF0C1220)),
                                border = BorderStroke(1.5.dp, Color(0xFF1E293B))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 18.dp, vertical = 22.dp)
                                ) {
                                    // 1. Official Document Header
                                    ReportHeaderSection(report)

                                    Spacer(modifier = Modifier.height(18.dp))
                                    HorizontalDivider(color = BorderDark.copy(alpha = 0.6f), thickness = 1.dp)
                                    Spacer(modifier = Modifier.height(18.dp))

                                    // 2. Project Identification & Contract Parties
                                    ReportProjectInfoSection(report)

                                    Spacer(modifier = Modifier.height(20.dp))

                                    // 3. Technical & Operations Metric Summary
                                    ReportOperationsSummarySection(report)

                                    Spacer(modifier = Modifier.height(22.dp))

                                    // 4. Delivered Clips Breakdown
                                    ReportClipsTableSection(report)

                                    Spacer(modifier = Modifier.height(22.dp))

                                    // 5. Revision & Feedback History
                                    ReportRevisionHistorySection(report)

                                    Spacer(modifier = Modifier.height(22.dp))

                                    // 6. Financial Account & Settlement
                                    ReportFinancialSection(report)

                                    Spacer(modifier = Modifier.height(20.dp))

                                    // 7. Final Archival Certification
                                    ReportDeliveryStatusSection(report)

                                    // 8. Editor Notes & Remarks
                                    ReportNotesSection(report)

                                    Spacer(modifier = Modifier.height(22.dp))
                                    HorizontalDivider(color = BorderDark.copy(alpha = 0.6f), thickness = 1.dp)
                                    Spacer(modifier = Modifier.height(14.dp))

                                    // 9. Document Footer & Digital Seal
                                    ReportDocumentFooter(report)
                                }
                            }
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
                // Top Row: Close Button + Title
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
                                .size(40.dp)
                                .testTag("btn_close_report")
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "بستن",
                                tint = TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "پیش‌نمایش سند رسمی گزارش پروژه",
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontSize = 13.5.sp
                            )
                            Text(
                                text = "${report.project.name} • #${PersianUtils.faNum(report.project.id)}",
                                color = TextMuted,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Action Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { ProjectReportExporter.saveReportAsPng(context, report) },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                        modifier = Modifier
                            .weight(1.3f)
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
                            .weight(1f)
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
                        border = BorderStroke(1.dp, Color(0xFF6366F1).copy(alpha = 0.6f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFA5B4FC)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                        modifier = Modifier
                            .weight(1.1f)
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
            // Desktop / Tablet Row
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
                            .size(40.dp)
                            .testTag("btn_close_report")
                    ) {
                        Icon(
                            Icons.Default.Close,
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
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontSize = 14.sp
                            )
                        }
                        Text(
                            text = "${report.project.name} • ${report.studioDisplayName} • ${report.reportNumber}",
                            color = TextSecondary,
                            fontSize = 11.sp
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

// ================= 1. REPORT HEADER SECTION =================
@Composable
private fun ReportHeaderSection(report: ProjectReportData) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val isNarrow = maxWidth < 520.dp

        if (isNarrow) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = DarkCard,
                    border = BorderStroke(1.dp, BorderDark)
                ) {
                    Text(
                        text = report.brandTitle,
                        color = PrimaryPurple,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 11.sp
                    )
                }

                Text(
                    text = "گزارش جامع و سوابق نهایی تدوین",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    fontSize = 16.sp
                )
                Text(
                    text = "سند رسمی اتمام فرآیند و تحویل کلیپ‌های ویدئویی",
                    color = TextSecondary,
                    fontSize = 11.sp
                )

                // Meta Box
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = DarkCard,
                        border = BorderStroke(1.dp, BorderDark)
                    ) {
                        Text(
                            text = report.brandTitle,
                            color = PrimaryPurple,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "گزارش جامع و سوابق نهایی تدوین",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 17.sp
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "سند رسمی اتمام فرآیند و تحویل کلیپ‌های ویدئویی",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF0F172A),
                    border = BorderStroke(1.dp, BorderDark)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
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
                                fontSize = 11.sp
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
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = CircleShape, color = PrimaryPurple.copy(alpha = 0.15f), modifier = Modifier.size(18.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Text("۱", color = PrimaryPurple, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "شناسنامه و مشخصات پرونده پروژه",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFF0F172A),
            border = BorderStroke(1.dp, BorderDark)
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val isNarrow = maxWidth < 520.dp

                if (isNarrow) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        InfoRowFull("نام پروژه", report.project.name, isBold = true)
                        HorizontalDivider(color = BorderDark.copy(alpha = 0.3f), thickness = 0.5.dp)
                        InfoRowFull("استودیو طرف حساب", report.studioDisplayName, isAccent = true)
                        HorizontalDivider(color = BorderDark.copy(alpha = 0.3f), thickness = 0.5.dp)

                        val weddingTxt = if (!report.project.weddingDate.isNullOrBlank()) {
                            PersianUtils.faNum(report.project.weddingDate)
                        } else "ثبت نشده"
                        InfoRowFull("تاریخ روز پروژه / مراسم", weddingTxt)
                        HorizontalDivider(color = BorderDark.copy(alpha = 0.3f), thickness = 0.5.dp)

                        InfoRowFull("تاریخ ثبت در سامانه", PersianUtils.faNum(report.project.createdAt))
                        HorizontalDivider(color = BorderDark.copy(alpha = 0.3f), thickness = 0.5.dp)

                        val deadlineTxt = if (!report.project.deadlineDate.isNullOrBlank()) {
                            PersianUtils.faNum(report.project.deadlineDate)
                        } else "نامحدود"
                        InfoRowFull("موعد تحویل اولیه", deadlineTxt)
                        HorizontalDivider(color = BorderDark.copy(alpha = 0.3f), thickness = 0.5.dp)

                        InfoRowFull("شناسه پرونده در دیتابیس", "#${PersianUtils.faNum(report.project.id)}")
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            InfoKeyValue("نام پروژه", report.project.name, isBold = true, modifier = Modifier.weight(1f))
                            InfoKeyValue("استودیو طرف حساب", report.studioDisplayName, isAccent = true, modifier = Modifier.weight(1f))
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = BorderDark.copy(alpha = 0.4f), thickness = 0.5.dp)
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val weddingTxt = if (!report.project.weddingDate.isNullOrBlank()) {
                                PersianUtils.faNum(report.project.weddingDate)
                            } else "ثبت نشده"
                            InfoKeyValue("تاریخ روز پروژه / مراسم", weddingTxt, modifier = Modifier.weight(1f))
                            InfoKeyValue("تاریخ ثبت در سامانه", PersianUtils.faNum(report.project.createdAt), modifier = Modifier.weight(1f))
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = BorderDark.copy(alpha = 0.4f), thickness = 0.5.dp)
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val deadlineTxt = if (!report.project.deadlineDate.isNullOrBlank()) {
                                PersianUtils.faNum(report.project.deadlineDate)
                            } else "نامحدود"
                            InfoKeyValue("موعد تحویل اولیه", deadlineTxt, modifier = Modifier.weight(1f))
                            InfoKeyValue("شناسه سیستمی", "#${PersianUtils.faNum(report.project.id)}", modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRowFull(
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
        Text(text = "$label:", color = TextSecondary, fontSize = 11.sp)
        Text(
            text = value,
            color = if (isAccent) PrimaryPurple else TextPrimary,
            fontWeight = if (isBold || isAccent) FontWeight.Bold else FontWeight.Medium,
            fontSize = 11.5.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun InfoKeyValue(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    isBold: Boolean = false,
    isAccent: Boolean = false
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(text = "$label:", color = TextSecondary, fontSize = 11.5.sp)
        Text(
            text = value,
            color = if (isAccent) PrimaryPurple else TextPrimary,
            fontWeight = if (isBold || isAccent) FontWeight.Bold else FontWeight.Medium,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ================= 3. OPERATIONS SUMMARY SECTION =================
@Composable
private fun ReportOperationsSummarySection(report: ProjectReportData) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = CircleShape, color = PrimaryPurple.copy(alpha = 0.15f), modifier = Modifier.size(18.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Text("۲", color = PrimaryPurple, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "خلاصه عملیات تدوین و زمان‌بندی",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val isNarrow = maxWidth < 540.dp

            if (isNarrow) {
                // 2x2 Grid for Mobile (No text clipping!)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricCard(
                            title = "تعداد کلیپ‌ها",
                            value = "${PersianUtils.faNum(report.totalClipsCount)} کلیپ",
                            accentColor = TextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            title = "تحویل‌شده",
                            value = "${PersianUtils.faNum(report.completedClipsCount)} کلیپ",
                            accentColor = SuccessGreen,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricCard(
                            title = "درصد پیشرفت",
                            value = "${PersianUtils.faNum(report.progressPercent)}٪",
                            accentColor = if (report.progressPercent == 100) SuccessGreen else WarningAmber,
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            title = "زمان کارکرد خالص",
                            value = PersianUtils.faNum(report.formattedDurationHMS),
                            accentColor = PrimaryPurple,
                            subtitle = report.formattedDurationPersianWords,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            } else {
                // 1x4 Row for Wide Displays
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = "تعداد کلیپ‌ها",
                        value = "${PersianUtils.faNum(report.totalClipsCount)} کلیپ",
                        accentColor = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "تحویل‌شده",
                        value = "${PersianUtils.faNum(report.completedClipsCount)} کلیپ",
                        accentColor = SuccessGreen,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "درصد پیشرفت",
                        value = "${PersianUtils.faNum(report.progressPercent)}٪",
                        accentColor = if (report.progressPercent == 100) SuccessGreen else WarningAmber,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "زمان کارکرد خالص",
                        value = PersianUtils.faNum(report.formattedDurationHMS),
                        accentColor = PrimaryPurple,
                        subtitle = report.formattedDurationPersianWords,
                        modifier = Modifier.weight(1.2f)
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
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
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, color = TextMuted, fontSize = 10.sp, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                value,
                color = accentColor,
                fontWeight = FontWeight.Bold,
                fontSize = 13.5.sp,
                textAlign = TextAlign.Center
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    subtitle,
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

// ================= 4. CLIPS TABLE SECTION =================
@Composable
private fun ReportClipsTableSection(report: ProjectReportData) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = PrimaryPurple.copy(alpha = 0.15f), modifier = Modifier.size(18.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("۳", color = PrimaryPurple, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "جزئیات کلیپ‌های تدوین‌شده و وضعیت تحویل",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
            Text(
                text = "${PersianUtils.faNum(report.clips.size)} کلیپ ثبت‌شده",
                color = TextMuted,
                fontSize = 11.sp
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF0F172A),
            border = BorderStroke(1.dp, BorderDark)
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val isNarrow = maxWidth < 540.dp

                if (isNarrow) {
                    // Mobile Adaptive Card Rows (clean, spacious, zero horizontal clipping)
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
                                color = if (index % 2 == 0) Color(0xFF131D31) else Color(0xFF0B111D),
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
                                                "${PersianUtils.faNum(index + 1)}.",
                                                color = TextMuted,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                clip.clipName,
                                                color = TextPrimary,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

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
                                        if (clip.estimateMins > 0) {
                                            Text(
                                                "برآورد: ${PersianUtils.faNum(clip.estimateMins)} دقیقه",
                                                color = TextSecondary,
                                                fontSize = 10.5.sp
                                            )
                                        } else {
                                            Text("برآورد: —", color = TextMuted, fontSize = 10.5.sp)
                                        }

                                        if (!clip.endDate.isNullOrBlank()) {
                                            Text(
                                                "تکمیل: ${PersianUtils.faNum(clip.endDate)}",
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
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("ردیف", color = TextSecondary, fontWeight = FontWeight.Bold, fontSize = 10.5.sp, modifier = Modifier.width(36.dp))
                            Text("عنوان کلیپ", color = TextSecondary, fontWeight = FontWeight.Bold, fontSize = 10.5.sp, modifier = Modifier.weight(1.5f))
                            Text("برآورد", color = TextSecondary, fontWeight = FontWeight.Bold, fontSize = 10.5.sp, modifier = Modifier.weight(0.8f))
                            Text("تاریخ تکمیل", color = TextSecondary, fontWeight = FontWeight.Bold, fontSize = 10.5.sp, modifier = Modifier.weight(1f))
                            Text("وضعیت تحویل", color = TextSecondary, fontWeight = FontWeight.Bold, fontSize = 10.5.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                        }

                        HorizontalDivider(color = BorderDark, thickness = 0.8.dp)

                        report.clips.forEachIndexed { index, clip ->
                            val isDone = clip.isDone == 1
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(if (index % 2 == 0) Color(0xFF0F172A) else Color(0xFF0B111D))
                                    .padding(horizontal = 12.dp, vertical = 9.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    PersianUtils.faNum(index + 1),
                                    color = TextMuted,
                                    fontSize = 11.sp,
                                    modifier = Modifier.width(36.dp)
                                )
                                Text(
                                    clip.clipName,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 11.5.sp,
                                    modifier = Modifier.weight(1.5f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    if (clip.estimateMins > 0) "${PersianUtils.faNum(clip.estimateMins)} دقیقه" else "—",
                                    color = TextSecondary,
                                    fontSize = 10.5.sp,
                                    modifier = Modifier.weight(0.8f)
                                )
                                Text(
                                    if (!clip.endDate.isNullOrBlank()) PersianUtils.faNum(clip.endDate) else "—",
                                    color = TextSecondary,
                                    fontSize = 10.5.sp,
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
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = PrimaryPurple.copy(alpha = 0.15f), modifier = Modifier.size(18.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("۴", color = PrimaryPurple, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "سوابق جامع اصلاحات و بازبینی‌ها (Revision History)",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
            if (report.revisionRounds.isNotEmpty()) {
                Text(
                    text = "${PersianUtils.faNum(report.revisionRounds.size)} دور اصلاحیه",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        if (report.revisionRounds.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF0F172A),
                border = BorderStroke(1.dp, BorderDark)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = SuccessGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "هیچ اصلاحیه‌ای برای این پروژه ثبت نشده است (پروژه در نسخه اولیه مورد تایید قرار گرفت)",
                        color = SuccessGreen,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.5.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                report.revisionRounds.forEach { round ->
                    RevisionRoundItem(round)
                }
            }
        }
    }
}

@Composable
private fun RevisionRoundItem(round: RevisionRoundSummary) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF0F172A),
        border = BorderStroke(
            1.dp,
            if (round.isRoundComplete) BorderDark else WarningAmber.copy(alpha = 0.4f)
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (round.isRoundComplete) Color(0xFF162032) else Color(0xFF261D13)
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
                        text = if (round.isRoundComplete) "✓ اعمال و بسته شد" else "در انتظار اعمال",
                        color = if (round.isRoundComplete) SuccessGreen else WarningAmber,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                round.items.forEachIndexed { idx, item ->
                    val isApplied = item.isApplied == 1
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (isApplied) Icons.Default.CheckCircle else Icons.Default.HourglassEmpty,
                                contentDescription = null,
                                tint = if (isApplied) SuccessGreen else TextMuted,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "مورد ${PersianUtils.faNum(idx + 1)}: ${item.description}",
                                color = if (isApplied) TextPrimary else TextSecondary,
                                fontSize = 11.5.sp
                            )
                        }

                        Text(
                            text = if (isApplied) "اعمال شد" else "معلق",
                            color = if (isApplied) SuccessGreen else TextMuted,
                            fontWeight = FontWeight.Medium,
                            fontSize = 10.5.sp
                        )
                    }

                    if (idx < round.items.size - 1) {
                        HorizontalDivider(color = BorderDark.copy(alpha = 0.3f), thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}

// ================= 6. FINANCIAL SECTION =================
@Composable
private fun ReportFinancialSection(report: ProjectReportData) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = CircleShape, color = PrimaryPurple.copy(alpha = 0.15f), modifier = Modifier.size(18.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Text("۵", color = PrimaryPurple, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "وضعیت مالی، مبالغ قرارداد و تسویه حساب",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

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
                // Contract Value + in words
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("ارزش کل قرارداد:", color = TextSecondary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            PersianUtils.formatCurrencyFa(report.project.price),
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp
                        )
                    }

                    Text(
                        "(${report.priceInWords})",
                        color = Color(0xFF818CF8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = BorderDark.copy(alpha = 0.5f), thickness = 0.8.dp)
                Spacer(modifier = Modifier.height(10.dp))

                // Paid & Remaining
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("کل دریافتی:", color = TextSecondary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            PersianUtils.formatCurrencyFa(report.totalPaid),
                            color = SuccessGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("مانده بدهی:", color = TextSecondary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            PersianUtils.formatCurrencyFa(report.remainingBalance),
                            color = if (report.isSettled) SuccessGreen else ErrorRed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Settlement Badge Banner
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
                            text = if (report.isSettled) "✓ حساب پروژه کاملاً تسویه شده است" else "⚠ دارای مانده بدهی پرداخت‌نشده",
                            color = if (report.isSettled) SuccessGreen else ErrorRed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp
                        )
                    }
                }

                // Payment Records (if any)
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
                                    "واریز ${PersianUtils.faNum(pIdx + 1)} • ${PersianUtils.faNum(payment.date)}" +
                                            (if (!payment.note.isNullOrBlank()) " (${payment.note})" else ""),
                                    color = TextSecondary,
                                    fontSize = 10.5.sp
                                )
                                Text(
                                    PersianUtils.formatCurrencyFa(payment.amount),
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

// ================= 7. DELIVERY STATUS SECTION =================
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
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF60A5FA), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "تاییدیه نهایی و وضعیت آرشیو کاترلاگ:",
                    color = Color(0xFF60A5FA),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            val summaryText = if (report.isSettled && report.progressPercent == 100) {
                "کلیه مراحل تدوین، بازبینی اصلاحات و تسویه حساب با موفقیت به اتمام رسیده و پروژه رسماً در آرشیو نهایی کاترلاگ بایگانی شد."
            } else {
                "اطلاعات پرونده و وضعیت تدوین در تاریخ استخراج گزارش ثبت و بروزرسانی شده است."
            }
            Text(
                text = summaryText,
                color = TextSecondary,
                fontSize = 11.sp
            )
        }
    }
}

// ================= 8. NOTES SECTION =================
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
                Text("یادداشت‌ها و ملاحظات تدوینگر:", color = WarningAmber, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                Spacer(modifier = Modifier.height(6.dp))
                distinctNotes.forEach { note ->
                    Text("• $note", color = TextSecondary, fontSize = 11.sp)
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
                    text = "شناسه سند: ${report.reportNumber}  •  سند معتبر دیجیتال",
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
