package com.example.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import com.example.R
import com.example.data.model.ProjectReportData
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

object ProjectReportExporter {

    private fun getVazirFonts(context: Context): Pair<Typeface, Typeface> {
        val tfBold = try {
            ResourcesCompat.getFont(context, R.font.vazirmatn_bold) ?: Typeface.DEFAULT_BOLD
        } catch (e: Exception) {
            Typeface.DEFAULT_BOLD
        }
        val tfRegular = try {
            ResourcesCompat.getFont(context, R.font.vazirmatn_regular) ?: Typeface.DEFAULT
        } catch (e: Exception) {
            Typeface.DEFAULT
        }
        return Pair(tfBold, tfRegular)
    }

    /**
     * Renders a high-resolution 1200px Bitmap of the project report
     * in the Dark Premium / Cinematic design language.
     */
    fun renderReportBitmap(context: Context, report: ProjectReportData): Bitmap {
        val (tfBold, tfRegular) = getVazirFonts(context)

        val width = 1200
        val margin = 50f
        val contentWidth = width - (margin * 2)

        // Calculate dynamic height based on content
        var calculatedHeight = margin * 2 + 20f // Top & bottom margins + padding
        calculatedHeight += 180f // Header
        calculatedHeight += 210f // Project Info Card
        calculatedHeight += 140f // Operations Summary (4 metric cards)

        // Clips section
        calculatedHeight += 50f // Section title
        calculatedHeight += 50f // Clips table header
        calculatedHeight += report.clips.size * 46f + 20f

        // Revisions section
        calculatedHeight += 50f // Section title
        if (report.revisionRounds.isEmpty()) {
            calculatedHeight += 80f
        } else {
            for (round in report.revisionRounds) {
                calculatedHeight += 52f // Round header banner
                calculatedHeight += round.items.size * 42f + 16f
            }
        }

        // Financial section
        calculatedHeight += 50f // Section title
        calculatedHeight += 190f // Financial main box
        if (report.payments.isNotEmpty()) {
            calculatedHeight += 44f + (report.payments.size * 40f) // Payments sub-table
        }

        // Final status & completion banner
        calculatedHeight += 100f

        // Notes section (if any)
        val hasNotes = report.sessions.any { !it.note.isNullOrBlank() } || report.payments.any { !it.note.isNullOrBlank() }
        if (hasNotes) {
            calculatedHeight += 120f
        }

        // Document footer
        calculatedHeight += 110f

        val totalHeight = calculatedHeight.toInt()

        val bitmap = Bitmap.createBitmap(width, totalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 1. Dark Premium Background (#0A0F1D)
        val bgPaint = Paint().apply {
            color = Color.parseColor("#0A0F1D")
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, width.toFloat(), totalHeight.toFloat(), bgPaint)

        // 2. Document Outer Border (#1E293B)
        val outerBorderPaint = Paint().apply {
            color = Color.parseColor("#1E293B")
            style = Paint.Style.STROKE
            strokeWidth = 2.5f
        }
        canvas.drawRect(20f, 20f, (width - 20).toFloat(), (totalHeight - 20).toFloat(), outerBorderPaint)

        // 3. Top Cinematic Accent Line (Gradient Indigo to Cyan)
        val topAccentPaint = Paint().apply {
            color = Color.parseColor("#6366F1")
            style = Paint.Style.FILL
        }
        canvas.drawRect(20f, 20f, (width - 20).toFloat(), 28f, topAccentPaint)

        // Shared Paints
        val cardBgPaint = Paint().apply {
            color = Color.parseColor("#111827")
            style = Paint.Style.FILL
        }
        val cardBorderPaint = Paint().apply {
            color = Color.parseColor("#1F2937")
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
        }
        val subCardBgPaint = Paint().apply {
            color = Color.parseColor("#161F32")
            style = Paint.Style.FILL
        }

        var currentY = margin + 35f

        // ================= HEADER =================
        val brandPaint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#818CF8")
            textSize = 34f
            typeface = tfBold
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText(report.brandTitle, width - margin, currentY, brandPaint)

        val reportTitlePaint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#F8FAFC")
            textSize = 22f
            typeface = tfBold
            textAlign = Paint.Align.RIGHT
        }
        currentY += 34f
        canvas.drawText("گزارش جامع و سوابق نهایی تدوین", width - margin, currentY, reportTitlePaint)

        val reportSubPaint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#94A3B8")
            textSize = 15f
            typeface = tfRegular
            textAlign = Paint.Align.RIGHT
        }
        currentY += 24f
        canvas.drawText("سامانه حرفه‌ای مدیریت سفارشات و تحویل پروژه‌های ویدئویی", width - margin, currentY, reportSubPaint)

        // Header Left Side (Document Meta Box)
        val metaBoxW = 260f
        val metaBoxH = 80f
        val metaBoxRect = RectF(margin, margin + 5f, margin + metaBoxW, margin + 5f + metaBoxH)
        canvas.drawRoundRect(metaBoxRect, 10f, 10f, subCardBgPaint)
        canvas.drawRoundRect(metaBoxRect, 10f, 10f, cardBorderPaint)

        val metaLabelPaint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#64748B")
            textSize = 13.5f
            typeface = tfBold
            textAlign = Paint.Align.RIGHT
        }
        val metaValPaint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#E2E8F0")
            textSize = 14f
            typeface = tfBold
            textAlign = Paint.Align.LEFT
        }

        canvas.drawText("شماره سند:", margin + metaBoxW - 14f, margin + 32f, metaLabelPaint)
        canvas.drawText(report.reportNumber, margin + 14f, margin + 32f, metaValPaint)

        canvas.drawText("تاریخ استخراج:", margin + metaBoxW - 14f, margin + 62f, metaLabelPaint)
        metaValPaint.typeface = tfRegular
        canvas.drawText(PersianUtils.faNum(report.reportDate), margin + 14f, margin + 62f, metaValPaint)

        currentY = margin + 115f

        // Divider
        val divPaint = Paint().apply {
            color = Color.parseColor("#1F2937")
            strokeWidth = 1f
        }
        canvas.drawLine(margin, currentY, width - margin, currentY, divPaint)
        currentY += 24f

        // ================= SECTION 1: PROJECT IDENTITY =================
        val sectionTitlePaint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#38BDF8")
            textSize = 17f
            typeface = tfBold
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText("شناسنامه و مشخصات پروژه", width - margin, currentY, sectionTitlePaint)
        currentY += 12f

        val projInfoRect = RectF(margin, currentY, width - margin, currentY + 160f)
        canvas.drawRoundRect(projInfoRect, 12f, 12f, cardBgPaint)
        canvas.drawRoundRect(projInfoRect, 12f, 12f, cardBorderPaint)

        // Inside Project Info Card (Two Columns)
        val col1Right = width - margin - 24f
        val col2Right = width / 2f + 30f
        var infoY = currentY + 34f

        val itemLabelPaint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#94A3B8")
            textSize = 15f
            typeface = tfRegular
            textAlign = Paint.Align.RIGHT
        }
        val itemValPaint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#F8FAFC")
            textSize = 15.5f
            typeface = tfBold
            textAlign = Paint.Align.RIGHT
        }

        // Row 1
        canvas.drawText("نام پروژه: ", col1Right, infoY, itemLabelPaint)
        val label1W = itemLabelPaint.measureText("نام پروژه: ")
        canvas.drawText(report.project.name, col1Right - label1W, infoY, itemValPaint)

        canvas.drawText("استودیو طرف حساب: ", col2Right, infoY, itemLabelPaint)
        val label2W = itemLabelPaint.measureText("استودیو طرف حساب: ")
        val studioPaint = Paint(itemValPaint).apply { color = Color.parseColor("#818CF8") }
        canvas.drawText(report.studioDisplayName, col2Right - label2W, infoY, studioPaint)

        // Row 2
        infoY += 40f
        val weddingTxt = if (!report.project.weddingDate.isNullOrBlank()) PersianUtils.faNum(report.project.weddingDate) else "ثبت نشده"
        canvas.drawText("تاریخ مراسم / روز پروژه: ", col1Right, infoY, itemLabelPaint)
        val label3W = itemLabelPaint.measureText("تاریخ مراسم / روز پروژه: ")
        canvas.drawText(weddingTxt, col1Right - label3W, infoY, itemValPaint)

        val createdTxt = PersianUtils.faNum(report.project.createdAt)
        canvas.drawText("تاریخ ثبت در سامانه: ", col2Right, infoY, itemLabelPaint)
        val label4W = itemLabelPaint.measureText("تاریخ ثبت در سامانه: ")
        canvas.drawText(createdTxt, col2Right - label4W, infoY, itemValPaint)

        // Row 3
        infoY += 40f
        val deadlineTxt = if (!report.project.deadlineDate.isNullOrBlank()) PersianUtils.faNum(report.project.deadlineDate) else "نامحدود"
        canvas.drawText("موعد تحویل اولیه: ", col1Right, infoY, itemLabelPaint)
        val label5W = itemLabelPaint.measureText("موعد تحویل اولیه: ")
        canvas.drawText(deadlineTxt, col1Right - label5W, infoY, itemValPaint)

        canvas.drawText("شناسه سیستمی: ", col2Right, infoY, itemLabelPaint)
        val label6W = itemLabelPaint.measureText("شناسه سیستمی: ")
        canvas.drawText("#${PersianUtils.faNum(report.project.id)}", col2Right - label6W, infoY, itemValPaint)

        currentY += 184f

        // ================= SECTION 2: EDITING OPERATIONS SUMMARY =================
        canvas.drawText("خلاصه عملیات تدوین و زمان‌بندی", width - margin, currentY, sectionTitlePaint)
        currentY += 12f

        // 4 Key Metric Tiles
        val tileGap = 16f
        val tileWidth = (contentWidth - (tileGap * 3)) / 4f
        val tileHeight = 100f

        val metrics = listOf(
            Triple("تعداد کلیپ‌ها", "${PersianUtils.faNum(report.totalClipsCount)} کلیپ", "#38BDF8"),
            Triple("کلیپ‌های تحویلی", "${PersianUtils.faNum(report.completedClipsCount)} تحویل‌شده", "#10B981"),
            Triple("درصد پیشرفت", "${PersianUtils.faNum(report.progressPercent)}٪", if (report.progressPercent == 100) "#10B981" else "#F59E0B"),
            Triple("کل مدت کارکرد", report.formattedDurationHMS, "#A855F7")
        )

        for (i in metrics.indices) {
            val (title, value, colorHex) = metrics[i]
            val left = margin + (i * (tileWidth + tileGap))
            val rect = RectF(left, currentY, left + tileWidth, currentY + tileHeight)
            canvas.drawRoundRect(rect, 10f, 10f, cardBgPaint)
            canvas.drawRoundRect(rect, 10f, 10f, cardBorderPaint)

            // Accent bar on top of card
            val accentBar = RectF(left, currentY, left + tileWidth, currentY + 5f)
            val barPaint = Paint().apply { color = Color.parseColor(colorHex); style = Paint.Style.FILL }
            canvas.drawRoundRect(accentBar, 3f, 3f, barPaint)

            val mTitlePaint = Paint().apply {
                isAntiAlias = true
                color = Color.parseColor("#94A3B8")
                textSize = 13f
                typeface = tfRegular
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(title, left + (tileWidth / 2f), currentY + 38f, mTitlePaint)

            val mValPaint = Paint().apply {
                isAntiAlias = true
                color = Color.parseColor(colorHex)
                textSize = 18f
                typeface = tfBold
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(value, left + (tileWidth / 2f), currentY + 74f, mValPaint)
        }

        currentY += tileHeight + 28f

        // ================= SECTION 3: CLIPS DETAILS =================
        canvas.drawText("جزئیات کلیپ‌های تدوین‌شده و وضعیت تحویل", width - margin, currentY, sectionTitlePaint)
        currentY += 14f

        // Clips Table Header
        val clipHeaderRect = RectF(margin, currentY, width - margin, currentY + 44f)
        val tableHeaderBg = Paint().apply { color = Color.parseColor("#1E293B"); style = Paint.Style.FILL }
        canvas.drawRoundRect(clipHeaderRect, 8f, 8f, tableHeaderBg)

        val thPaint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#CBD5E1")
            textSize = 14f
            typeface = tfBold
            textAlign = Paint.Align.RIGHT
        }

        val colClipNumX = width - margin - 20f
        val colClipNameX = width - margin - 90f
        val colClipEstX = width - margin - 520f
        val colClipDateX = width - margin - 740f
        val colClipStatusX = margin + 30f

        canvas.drawText("ردیف", colClipNumX, currentY + 28f, thPaint)
        canvas.drawText("عنوان کلیپ", colClipNameX, currentY + 28f, thPaint)
        canvas.drawText("زمان برآورد", colClipEstX, currentY + 28f, thPaint)
        canvas.drawText("تاریخ تحویل", colClipDateX, currentY + 28f, thPaint)

        val thStatusPaint = Paint(thPaint).apply { textAlign = Paint.Align.LEFT }
        canvas.drawText("وضعیت تحویل", colClipStatusX, currentY + 28f, thStatusPaint)

        currentY += 48f

        // Clips Rows
        val tdPaint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#E2E8F0")
            textSize = 14.5f
            typeface = tfRegular
            textAlign = Paint.Align.RIGHT
        }

        val rowBgAlt = Paint().apply { color = Color.parseColor("#0F172A"); style = Paint.Style.FILL }
        val rowBgNorm = Paint().apply { color = Color.parseColor("#141E33"); style = Paint.Style.FILL }

        report.clips.forEachIndexed { index, clip ->
            val rowRect = RectF(margin, currentY, width - margin, currentY + 42f)
            canvas.drawRect(rowRect, if (index % 2 == 0) rowBgNorm else rowBgAlt)

            val rowY = currentY + 27f
            canvas.drawText(PersianUtils.faNum(index + 1), colClipNumX, rowY, tdPaint)
            canvas.drawText(clip.clipName, colClipNameX, rowY, tdPaint)

            val estTxt = if (clip.estimateMins > 0) "${PersianUtils.faNum(clip.estimateMins)} دقیقه" else "—"
            canvas.drawText(estTxt, colClipEstX, rowY, tdPaint)

            val dateTxt = if (!clip.endDate.isNullOrBlank()) PersianUtils.faNum(clip.endDate) else "—"
            canvas.drawText(dateTxt, colClipDateX, rowY, tdPaint)

            // Status Badge
            val isDone = clip.isDone == 1
            val badgeText = if (isDone) "تحویل کامل" else "در حال ادیت"
            val badgeColor = if (isDone) Color.parseColor("#10B981") else Color.parseColor("#F59E0B")
            val statusPaint = Paint().apply {
                isAntiAlias = true
                color = badgeColor
                textSize = 13.5f
                typeface = tfBold
                textAlign = Paint.Align.LEFT
            }
            canvas.drawText(badgeText, colClipStatusX, rowY, statusPaint)

            currentY += 44f
        }

        currentY += 24f

        // ================= SECTION 4: REVISION HISTORY =================
        canvas.drawText("سوابق جامع اصلاحات و بازبینی‌ها (Revision History)", width - margin, currentY, sectionTitlePaint)
        currentY += 14f

        if (report.revisionRounds.isEmpty()) {
            val emptyRevRect = RectF(margin, currentY, width - margin, currentY + 70f)
            canvas.drawRoundRect(emptyRevRect, 10f, 10f, cardBgPaint)
            canvas.drawRoundRect(emptyRevRect, 10f, 10f, cardBorderPaint)

            val emptyTextPaint = Paint().apply {
                isAntiAlias = true
                color = Color.parseColor("#10B981")
                textSize = 15f
                typeface = tfBold
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(
                "✓ هیچ اصلاحیه‌ای برای این پروژه ثبت نشده است (پروژه در اولین نسخه تایید و تحویل شده است)",
                width / 2f,
                currentY + 42f,
                emptyTextPaint
            )
            currentY += 90f
        } else {
            for (round in report.revisionRounds) {
                // Round Header Box
                val rHeaderRect = RectF(margin, currentY, width - margin, currentY + 44f)
                val roundHeaderBg = Paint().apply {
                    color = if (round.isRoundComplete) Color.parseColor("#1E2D4A") else Color.parseColor("#2D2418")
                    style = Paint.Style.FILL
                }
                canvas.drawRoundRect(rHeaderRect, 8f, 8f, roundHeaderBg)

                val roundBorderPaint = Paint().apply {
                    color = if (round.isRoundComplete) Color.parseColor("#38BDF8") else Color.parseColor("#F59E0B")
                    style = Paint.Style.STROKE
                    strokeWidth = 1.2f
                }
                canvas.drawRoundRect(rHeaderRect, 8f, 8f, roundBorderPaint)

                val roundTitlePaint = Paint().apply {
                    isAntiAlias = true
                    color = Color.parseColor("#F8FAFC")
                    textSize = 15f
                    typeface = tfBold
                    textAlign = Paint.Align.RIGHT
                }
                canvas.drawText("دور ${PersianUtils.faNum(round.phaseNum)} اصلاحیه  •  تعداد کل: ${PersianUtils.faNum(round.totalCount)} مورد", width - margin - 18f, currentY + 28f, roundTitlePaint)

                val roundStatusPaint = Paint().apply {
                    isAntiAlias = true
                    color = if (round.isRoundComplete) Color.parseColor("#10B981") else Color.parseColor("#F59E0B")
                    textSize = 13.5f
                    typeface = tfBold
                    textAlign = Paint.Align.LEFT
                }
                val rStatusTxt = if (round.isRoundComplete) "✓ تمام اصلاحات اعمال و بسته شد" else "در حال پیگیری و اصلاح"
                canvas.drawText(rStatusTxt, margin + 18f, currentY + 28f, roundStatusPaint)

                currentY += 50f

                // Round items list
                round.items.forEachIndexed { itemIdx, item ->
                    val itemRect = RectF(margin, currentY, width - margin, currentY + 38f)
                    val itemBg = Paint().apply {
                        color = if (itemIdx % 2 == 0) Color.parseColor("#111827") else Color.parseColor("#0D1322")
                        style = Paint.Style.FILL
                    }
                    canvas.drawRect(itemRect, itemBg)

                    val itemY = currentY + 24f
                    val isApp = item.isApplied == 1

                    // Dot or Check icon text
                    val symbolPaint = Paint().apply {
                        isAntiAlias = true
                        color = if (isApp) Color.parseColor("#10B981") else Color.parseColor("#F59E0B")
                        textSize = 14f
                        typeface = tfBold
                        textAlign = Paint.Align.RIGHT
                    }
                    val symbol = if (isApp) "✓" else "•"
                    canvas.drawText("$symbol مورد ${PersianUtils.faNum(itemIdx + 1)}:", width - margin - 20f, itemY, symbolPaint)

                    val descPaint = Paint().apply {
                        isAntiAlias = true
                        color = Color.parseColor("#E2E8F0")
                        textSize = 14f
                        typeface = tfRegular
                        textAlign = Paint.Align.RIGHT
                    }
                    val descStartX = width - margin - 130f
                    // Prevent overflow by cutting if too long
                    val displayDesc = if (item.description.length > 75) item.description.take(72) + "..." else item.description
                    canvas.drawText(displayDesc, descStartX, itemY, descPaint)

                    val itemBadgePaint = Paint().apply {
                        isAntiAlias = true
                        color = if (isApp) Color.parseColor("#10B981") else Color.parseColor("#94A3B8")
                        textSize = 12.5f
                        typeface = tfBold
                        textAlign = Paint.Align.LEFT
                    }
                    canvas.drawText(if (isApp) "اعمال شد" else "معلق", margin + 20f, itemY, itemBadgePaint)

                    currentY += 40f
                }
                currentY += 12f
            }
        }

        currentY += 16f

        // ================= SECTION 5: FINANCIAL STATUS =================
        canvas.drawText("وضعیت مالی، مبالغ قرارداد و تسویه حساب", width - margin, currentY, sectionTitlePaint)
        currentY += 14f

        val finBoxH = 175f
        val finRect = RectF(margin, currentY, width - margin, currentY + finBoxH)
        canvas.drawRoundRect(finRect, 12f, 12f, cardBgPaint)
        canvas.drawRoundRect(finRect, 12f, 12f, cardBorderPaint)

        var finY = currentY + 34f
        val fLabelPaint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#94A3B8")
            textSize = 15f
            typeface = tfRegular
            textAlign = Paint.Align.RIGHT
        }
        val fValPaint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#F8FAFC")
            textSize = 16f
            typeface = tfBold
            textAlign = Paint.Align.RIGHT
        }

        // Line 1: Contract Price + Price in words
        canvas.drawText("مبلغ کل قرارداد:", col1Right, finY, fLabelPaint)
        val pLabelW = fLabelPaint.measureText("مبلغ کل قرارداد: ")
        canvas.drawText(PersianUtils.formatCurrencyFa(report.project.price), col1Right - pLabelW, finY, fValPaint)

        val wordsPaint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#818CF8")
            textSize = 14f
            typeface = tfRegular
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText("(${report.priceInWords})", col2Right, finY, wordsPaint)

        // Line 2: Total Paid & Remaining Balance
        finY += 40f
        canvas.drawText("کل دریافتی:", col1Right, finY, fLabelPaint)
        val paidLabelW = fLabelPaint.measureText("کل دریافتی: ")
        val paidValPaint = Paint(fValPaint).apply { color = Color.parseColor("#10B981") }
        canvas.drawText(PersianUtils.formatCurrencyFa(report.totalPaid), col1Right - paidLabelW, finY, paidValPaint)

        canvas.drawText("مانده حساب:", col2Right, finY, fLabelPaint)
        val remLabelW = fLabelPaint.measureText("مانده حساب: ")
        val remColor = if (report.isSettled) Color.parseColor("#10B981") else Color.parseColor("#EF4444")
        val remValPaint = Paint(fValPaint).apply { color = remColor }
        canvas.drawText(PersianUtils.formatCurrencyFa(report.remainingBalance), col2Right - remLabelW, finY, remValPaint)

        // Line 3: Settlement Status Banner
        finY += 48f
        val statusBgColor = if (report.isSettled) Color.parseColor("#064E3B") else Color.parseColor("#7F1D1D")
        val statusBorderColor = if (report.isSettled) Color.parseColor("#059669") else Color.parseColor("#DC2626")
        val statusRect = RectF(col2Right - 200f, finY - 24f, col1Right, finY + 16f)
        val sBgPaint = Paint().apply { color = statusBgColor; style = Paint.Style.FILL }
        val sBrdPaint = Paint().apply { color = statusBorderColor; style = Paint.Style.STROKE; strokeWidth = 1.2f }
        canvas.drawRoundRect(statusRect, 8f, 8f, sBgPaint)
        canvas.drawRoundRect(statusRect, 8f, 8f, sBrdPaint)

        val sTxtPaint = Paint().apply {
            isAntiAlias = true
            color = Color.WHITE
            textSize = 14f
            typeface = tfBold
            textAlign = Paint.Align.CENTER
        }
        val settleTxt = if (report.isSettled) "✓ حساب پروژه کاملاً تسویه شده است" else "⚠ دارای مانده بدهی پرداخت‌نشده"
        canvas.drawText(settleTxt, statusRect.centerX(), finY, sTxtPaint)

        currentY += finBoxH + 16f

        // Payment History rows if exist
        if (report.payments.isNotEmpty()) {
            val payHeaderRect = RectF(margin, currentY, width - margin, currentY + 36f)
            canvas.drawRoundRect(payHeaderRect, 6f, 6f, subCardBgPaint)
            val pHeadPaint = Paint().apply {
                isAntiAlias = true
                color = Color.parseColor("#94A3B8")
                textSize = 13.5f
                typeface = tfBold
                textAlign = Paint.Align.RIGHT
            }
            canvas.drawText("سوابق واریزی‌های ثبت‌شده:", width - margin - 16f, currentY + 24f, pHeadPaint)
            currentY += 40f

            report.payments.forEachIndexed { pIdx, payment ->
                val pRowRect = RectF(margin, currentY, width - margin, currentY + 36f)
                canvas.drawRect(pRowRect, if (pIdx % 2 == 0) rowBgNorm else rowBgAlt)

                val pRowY = currentY + 24f
                val pTxtPaint = Paint().apply {
                    isAntiAlias = true
                    color = Color.parseColor("#E2E8F0")
                    textSize = 13.5f
                    typeface = tfRegular
                    textAlign = Paint.Align.RIGHT
                }
                canvas.drawText("واریز ${PersianUtils.faNum(pIdx + 1)}  •  تاریخ: ${PersianUtils.faNum(payment.date)}", width - margin - 20f, pRowY, pTxtPaint)

                val pNoteTxt = if (!payment.note.isNullOrBlank()) "بابت: ${payment.note}" else "واریز حساب"
                canvas.drawText(pNoteTxt, width / 2f + 40f, pRowY, pTxtPaint)

                val pAmtPaint = Paint().apply {
                    isAntiAlias = true
                    color = Color.parseColor("#10B981")
                    textSize = 14f
                    typeface = tfBold
                    textAlign = Paint.Align.LEFT
                }
                canvas.drawText(PersianUtils.formatCurrencyFa(payment.amount), margin + 20f, pRowY, pAmtPaint)

                currentY += 38f
            }
            currentY += 16f
        }

        // ================= SECTION 6: FINAL SYSTEM STATUS =================
        val finalBoxRect = RectF(margin, currentY, width - margin, currentY + 76f)
        val finalBg = Paint().apply { color = Color.parseColor("#0F1E36"); style = Paint.Style.FILL }
        val finalBorder = Paint().apply { color = Color.parseColor("#2563EB"); style = Paint.Style.STROKE; strokeWidth = 1.5f }
        canvas.drawRoundRect(finalBoxRect, 10f, 10f, finalBg)
        canvas.drawRoundRect(finalBoxRect, 10f, 10f, finalBorder)

        val finalTitlePaint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#60A5FA")
            textSize = 16f
            typeface = tfBold
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText("وضعیت تحویل نهایی و آرشیو کاترلاگ:", width - margin - 20f, currentY + 32f, finalTitlePaint)

        val finalDescPaint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#CBD5E1")
            textSize = 14f
            typeface = tfRegular
            textAlign = Paint.Align.RIGHT
        }
        val statusExpl = if (report.isSettled && report.progressPercent == 100) {
            "کلیه مراحل تدوین، بازبینی اصلاحات و تسویه حساب با موفقیت به اتمام رسیده و پروژه رسماً در آرشیو نهایی کاترلاگ بایگانی شد."
        } else {
            "اطلاعات پرونده در تاریخ استخراج گزارش ثبت و بروزرسانی شده است."
        }
        canvas.drawText(statusExpl, width - margin - 20f, currentY + 58f, finalDescPaint)

        currentY += 96f

        // Notes section if any
        if (hasNotes) {
            val notesRect = RectF(margin, currentY, width - margin, currentY + 90f)
            canvas.drawRoundRect(notesRect, 10f, 10f, cardBgPaint)
            canvas.drawRoundRect(notesRect, 10f, 10f, cardBorderPaint)

            val notesTitlePaint = Paint().apply {
                isAntiAlias = true
                color = Color.parseColor("#F59E0B")
                textSize = 14.5f
                typeface = tfBold
                textAlign = Paint.Align.RIGHT
            }
            canvas.drawText("یادداشت‌ها و ملاحظات تدوینگر:", width - margin - 20f, currentY + 30f, notesTitlePaint)

            val allNotes = report.sessions.mapNotNull { it.note }.filter { it.isNotBlank() } +
                    report.payments.mapNotNull { it.note }.filter { it.isNotBlank() }
            val joinedNotes = allNotes.distinct().take(2).joinToString("  |  ")
            val nValPaint = Paint().apply {
                isAntiAlias = true
                color = Color.parseColor("#CBD5E1")
                textSize = 13.5f
                typeface = tfRegular
                textAlign = Paint.Align.RIGHT
            }
            canvas.drawText(joinedNotes.ifBlank { "توضیحات تکمیلی ثبت نشده است." }, width - margin - 20f, currentY + 60f, nValPaint)

            currentY += 106f
        }

        // ================= DOCUMENT FOOTER =================
        val footDivPaint = Paint().apply { color = Color.parseColor("#1E293B"); strokeWidth = 1.2f }
        canvas.drawLine(margin, currentY, width - margin, currentY, footDivPaint)
        currentY += 28f

        val fBrandPaint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#64748B")
            textSize = 13f
            typeface = tfBold
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText("تولید شده توسط نرم‌افزار مدیریت تدوین کاترلاگ (CutterLog Workspace)", width - margin, currentY, fBrandPaint)

        val fStampPaint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#64748B")
            textSize = 12.5f
            typeface = tfRegular
            textAlign = Paint.Align.LEFT
        }
        canvas.drawText("شناسه سند: ${report.reportNumber}  •  سند معتبر دیجیتال", margin, currentY, fStampPaint)

        return bitmap
    }

    /**
     * Saves the report Bitmap to Gallery / External MediaStore as PNG.
     */
    fun saveReportAsPng(context: Context, report: ProjectReportData): Uri? {
        val bitmap = renderReportBitmap(context, report)
        val filename = "Report_${report.project.name.replace(" ", "_")}_${report.reportNumber}.png"

        return try {
            var uri: Uri? = null
            var outputStream: OutputStream? = null

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/CutterLog/Reports")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }

                val resolver = context.contentResolver
                uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                if (uri != null) {
                    outputStream = resolver.openOutputStream(uri)
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream!!)
                    values.clear()
                    values.put(MediaStore.Images.Media.IS_PENDING, 0)
                    resolver.update(uri, values, null, null)
                }
            } else {
                val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val appDir = File(picturesDir, "CutterLog/Reports")
                if (!appDir.exists()) appDir.mkdirs()
                val file = File(appDir, filename)
                outputStream = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                uri = Uri.fromFile(file)
            }

            outputStream?.flush()
            outputStream?.close()

            Toast.makeText(context, "سند گزارش پروژه با موفقیت در گالری ذخیره شد", Toast.LENGTH_LONG).show()
            uri
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to cacheDir
            try {
                val file = File(context.cacheDir, filename)
                val out = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                out.flush()
                out.close()
                val fallbackUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                Toast.makeText(context, "سند گزارش ذخیره شد", Toast.LENGTH_SHORT).show()
                fallbackUri
            } catch (ex: Exception) {
                ex.printStackTrace()
                Toast.makeText(context, "خطا در ذخیره تصویر گزارش: ${e.message}", Toast.LENGTH_SHORT).show()
                null
            }
        }
    }

    /**
     * Shares report image via Android standard share sheet.
     */
    fun shareReportAsPng(context: Context, report: ProjectReportData) {
        try {
            val bitmap = renderReportBitmap(context, report)
            val file = File(context.cacheDir, "Report_${report.reportNumber}.png")
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.flush()
            stream.close()

            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_SUBJECT, "گزارش نهایی پروژه ${report.project.name} - کاترلاگ")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "گزارش نهایی و سوابق تدوین پروژه «${report.project.name}»\n" +
                            "استودیو: ${report.studioDisplayName}\n" +
                            "ارزش قرارداد: ${PersianUtils.formatCurrencyFa(report.project.price)}\n" +
                            "تعداد کلیپ‌ها: ${PersianUtils.faNum(report.totalClipsCount)}\n" +
                            "سوابق اصلاحیه: ${PersianUtils.faNum(report.revisionRounds.size)} دور\n" +
                            "وضعیت تسویه: ${if (report.isSettled) "تسویه کامل" else "دارای مانده بدهی"}\n" +
                            "ثبت شده در سامانه کاترلاگ (CutterLog)"
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "اشتراک‌گذاری گزارش پروژه"))
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "خطا در اشتراک‌گذاری: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Generates an official, multi-page PDF document for the project report.
     * Returns the file stored in context.cacheDir, or null on error.
     */
    fun generateReportPdf(context: Context, report: ProjectReportData): File? {
        return ProjectReportPdfRenderer.generateReportPdf(context, report)
    }

    /**
     * Generates and shares an official PDF document for the project report.
     */
    fun shareReportAsPdf(context: Context, report: ProjectReportData) {
        try {
            val file = generateReportPdf(context, report)
            if (file == null || !file.exists()) {
                Toast.makeText(context, "خطا در ایجاد فایل PDF گزارش", Toast.LENGTH_SHORT).show()
                return
            }

            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_SUBJECT, "گزارش رسمی تدوین پروژه «${report.project.name}»")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "گزارش رسمی و سوابق جامع تدوین پروژه «${report.project.name}»\n" +
                            "استودیو: ${report.studioDisplayName}\n" +
                            "شناسه سند: ${report.reportNumber}\n" +
                            "استخراج‌شده از سامانه کاترلاگ (CutterLog)"
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "اشتراک‌گذاری نسخه PDF گزارش"))
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "خطا در اشتراک‌گذاری PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}

