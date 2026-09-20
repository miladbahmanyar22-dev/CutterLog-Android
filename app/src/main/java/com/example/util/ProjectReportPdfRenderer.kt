package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextDirectionHeuristics
import android.text.TextPaint
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.example.R
import com.example.data.model.ProjectReportData
import java.io.File
import java.io.FileOutputStream

/**
 * Official, executive-grade PDF Document Renderer for CutterLog Project Reports.
 *
 * Designed to output a formal, clean, multi-page document adhering strictly to
 * professional A4 standards, intelligent page breaking, Persian typography (Vazirmatn),
 * true RTL alignment, and comprehensive project history without truncating data.
 */
object ProjectReportPdfRenderer {

    // A4 Standard Dimensions at 72 dpi (Points)
    private const val PAGE_WIDTH = 595f
    private const val PAGE_HEIGHT = 842f
    private const val MARGIN = 36f

    // Content bounds for pages
    private const val CONTENT_LEFT = MARGIN
    private const val CONTENT_RIGHT = PAGE_WIDTH - MARGIN
    private const val CONTENT_WIDTH = CONTENT_RIGHT - CONTENT_LEFT // 523 points

    // Printable vertical area on content pages (Page 2+)
    private const val HEADER_BOTTOM = 68f
    private const val CONTENT_START_Y = 82f
    private const val CONTENT_MAX_Y = 794f
    private const val FOOTER_DIVIDER_Y = 806f

    // Official Corporate Palette (White Paper / Deep Indigo & Slate)
    private const val COLOR_PAGE_BG = "#FFFFFF"
    private const val COLOR_OUTER_BORDER = "#E2E8F0"
    private const val COLOR_CARD_BORDER = "#CBD5E1"
    private const val COLOR_DIVIDER = "#E2E8F0"
    private const val COLOR_SECTION_HEADER_BG = "#EEF2F6"
    private const val COLOR_CARD_BG = "#F8FAFC"
    private const val COLOR_ALT_ROW_BG = "#F8FAFC"

    private const val COLOR_PRIMARY_BRAND = "#3730A3" // Deep Indigo
    private const val COLOR_ACCENT_LINE = "#4338CA"
    private const val COLOR_ACCENT_CYAN = "#0EA5E9"

    private const val COLOR_TEXT_PRIMARY = "#0F172A" // Slate 900
    private const val COLOR_TEXT_SECONDARY = "#334155" // Slate 700
    private const val COLOR_TEXT_MUTED = "#64748B" // Slate 500
    private const val COLOR_TEXT_LIGHT = "#94A3B8" // Slate 400

    private const val COLOR_SUCCESS = "#15803D"
    private const val COLOR_SUCCESS_BG = "#DCFCE7"
    private const val COLOR_SUCCESS_BORDER = "#86EFAC"

    private const val COLOR_WARNING = "#B45309"
    private const val COLOR_WARNING_BG = "#FEF3C7"
    private const val COLOR_WARNING_BORDER = "#FCD34D"

    private const val COLOR_ERROR = "#B91C1C"
    private const val COLOR_ERROR_BG = "#FEE2E2"
    private const val COLOR_ERROR_BORDER = "#FCA5A5"

    private const val COLOR_INFO = "#0369A1"
    private const val COLOR_INFO_BG = "#E0F2FE"
    private const val COLOR_INFO_BORDER = "#BAE6FD"

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

    private fun getLogoBitmap(context: Context, sizePx: Int): Bitmap? {
        return try {
            val drawable = ContextCompat.getDrawable(context, R.drawable.ic_cutterlog_logo) ?: return null
            val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, sizePx, sizePx)
            drawable.draw(canvas)
            bitmap
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Generates a multi-page official PDF file for the project report.
     * Returns the generated file in cacheDir.
     */
    fun generateReportPdf(context: Context, report: ProjectReportData): File? {
        return try {
            val (tfBold, tfRegular) = getVazirFonts(context)
            val logoBitmap = getLogoBitmap(context, 112)

            // Pass 1: Dry run to measure layout and determine exact total pages
            val totalPages = runLayoutPass(
                context = context,
                report = report,
                tfBold = tfBold,
                tfRegular = tfRegular,
                logoBitmap = logoBitmap,
                pdfDoc = null,
                totalPagesExpected = 1
            )

            // Pass 2: Real rendering with exact page numbering
            val pdfDoc = PdfDocument()
            runLayoutPass(
                context = context,
                report = report,
                tfBold = tfBold,
                tfRegular = tfRegular,
                logoBitmap = logoBitmap,
                pdfDoc = pdfDoc,
                totalPagesExpected = totalPages
            )

            val file = File(context.cacheDir, "Report_${report.reportNumber}.pdf")
            val outputStream = FileOutputStream(file)
            pdfDoc.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            pdfDoc.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Core layout execution method.
     * When pdfDoc is null, performs a dry run to calculate total pages.
     * When pdfDoc is provided, writes pages with complete graphics and text.
     */
    private fun runLayoutPass(
        context: Context,
        report: ProjectReportData,
        tfBold: Typeface,
        tfRegular: Typeface,
        logoBitmap: Bitmap?,
        pdfDoc: PdfDocument?,
        totalPagesExpected: Int
    ): Int {
        val isDryRun = (pdfDoc == null)

        var pageIndex = 1
        var currentPage: PdfDocument.Page? = null
        var currentCanvas: Canvas? = null

        // Helper to start a page
        fun openPage(index: Int) {
            if (!isDryRun && pdfDoc != null) {
                val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), index).create()
                val p = pdfDoc.startPage(pageInfo)
                currentPage = p
                currentCanvas = p.canvas

                // Draw standard white page background and outer border
                val bgPaint = Paint().apply { color = Color.parseColor(COLOR_PAGE_BG); style = Paint.Style.FILL }
                currentCanvas?.drawRect(0f, 0f, PAGE_WIDTH, PAGE_HEIGHT, bgPaint)

                if (index > 1) {
                    drawContentPageHeader(currentCanvas!!, report, tfBold, tfRegular)
                    drawContentPageFooter(currentCanvas!!, report, index, totalPagesExpected, tfBold, tfRegular)
                }
            }
        }

        // Helper to close a page
        fun closePage() {
            if (!isDryRun && pdfDoc != null && currentPage != null) {
                pdfDoc.finishPage(currentPage)
                currentPage = null
                currentCanvas = null
            }
        }

        // ---------------- PAGE 1: COVER PAGE ----------------
        openPage(1)
        if (!isDryRun && currentCanvas != null) {
            drawCoverPage(currentCanvas!!, report, tfBold, tfRegular, logoBitmap, totalPagesExpected)
        }
        closePage()

        // ---------------- PAGE 2+: CONTENT PAGES ----------------
        pageIndex = 2
        openPage(pageIndex)
        var currentY = CONTENT_START_Y

        // Table header continuation callbacks
        var activeTableHeaderDrawer: ((Canvas, Float) -> Float)? = null

        // Smart page break helper
        fun ensureSpace(neededHeight: Float) {
            if (currentY + neededHeight > CONTENT_MAX_Y) {
                closePage()
                pageIndex++
                openPage(pageIndex)
                currentY = CONTENT_START_Y

                // If inside a continuing table, repeat header
                val headerDrawer = activeTableHeaderDrawer
                if (headerDrawer != null) {
                    if (!isDryRun && currentCanvas != null) {
                        currentY = headerDrawer(currentCanvas!!, currentY)
                    } else {
                        currentY += 24f
                    }
                }
            }
        }

        // Section 1: Project Identity Box
        val identityHeight = 115f
        ensureSpace(identityHeight + 24f)
        if (!isDryRun && currentCanvas != null) {
            currentY = drawSection1ProjectIdentity(currentCanvas!!, report, currentY, tfBold, tfRegular)
        } else {
            currentY += identityHeight + 24f
        }

        // Section 2: Operational Metrics & Progress Bar
        val metricsHeight = 118f
        ensureSpace(metricsHeight + 20f)
        if (!isDryRun && currentCanvas != null) {
            currentY = drawSection2OperationalMetrics(currentCanvas!!, report, currentY, tfBold, tfRegular)
        } else {
            currentY += metricsHeight + 20f
        }

        // Section 3: Delivery Status
        val deliveryHeight = 68f
        ensureSpace(deliveryHeight + 20f)
        if (!isDryRun && currentCanvas != null) {
            currentY = drawSection3DeliveryStatus(currentCanvas!!, report, currentY, tfBold, tfRegular)
        } else {
            currentY += deliveryHeight + 20f
        }

        // Section 4: Notes (if any exist)
        val projectNotes = collectProjectNotes(report)
        if (projectNotes.isNotBlank()) {
            val notePaint = TextPaint().apply {
                isAntiAlias = true
                color = Color.parseColor(COLOR_TEXT_SECONDARY)
                textSize = 9f
                typeface = tfRegular
            }
            val textH = measureRtlTextHeight(projectNotes, CONTENT_WIDTH - 24f, notePaint)
            val notesBoxHeight = textH + 42f
            ensureSpace(notesBoxHeight + 20f)
            if (!isDryRun && currentCanvas != null) {
                currentY = drawSection4ProjectNotes(currentCanvas!!, projectNotes, currentY, tfBold, tfRegular, notePaint)
            } else {
                currentY += notesBoxHeight + 20f
            }
        }

        // Section 5: Clips Table
        val clipSectionHeaderH = 28f
        val clipTableHeaderH = 24f
        ensureSpace(clipSectionHeaderH + clipTableHeaderH + 30f)

        if (!isDryRun && currentCanvas != null) {
            currentY = drawSectionHeader(currentCanvas!!, "فهرست و وضعیت تفصیلی کلیپ‌های پروژه", currentY, tfBold)
        } else {
            currentY += clipSectionHeaderH
        }

        activeTableHeaderDrawer = { c, y ->
            drawClipsTableHeader(c, y, tfBold)
        }

        if (!isDryRun && currentCanvas != null) {
            currentY = drawClipsTableHeader(currentCanvas!!, currentY, tfBold)
        } else {
            currentY += clipTableHeaderH
        }

        if (report.clips.isEmpty()) {
            val emptyH = 34f
            ensureSpace(emptyH)
            if (!isDryRun && currentCanvas != null) {
                currentY = drawEmptyBox(currentCanvas!!, "هیچ کلیپی برای این پروژه در سامانه ثبت نشده است.", currentY, tfRegular)
            } else {
                currentY += emptyH
            }
        } else {
            val namePaint = TextPaint().apply {
                isAntiAlias = true
                color = Color.parseColor(COLOR_TEXT_PRIMARY)
                textSize = 8.5f
                typeface = tfRegular
            }
            for (i in report.clips.indices) {
                val clip = report.clips[i]
                val nameH = measureRtlTextHeight(clip.clipName, 220f, namePaint)
                val rowHeight = maxOf(25f, nameH + 11f)

                ensureSpace(rowHeight)

                if (!isDryRun && currentCanvas != null) {
                    currentY = drawClipRow(currentCanvas!!, i + 1, clip, currentY, rowHeight, tfBold, tfRegular, namePaint)
                } else {
                    currentY += rowHeight
                }
            }
        }
        activeTableHeaderDrawer = null
        currentY += 16f

        // Section 6: Comprehensive Revision History
        val revSectionHeaderH = 28f
        ensureSpace(revSectionHeaderH + 40f)

        if (!isDryRun && currentCanvas != null) {
            currentY = drawSectionHeader(currentCanvas!!, "سوابق جامع اصلاحات و بازبینی‌ها", currentY, tfBold)
        } else {
            currentY += revSectionHeaderH
        }

        if (report.revisionRounds.isEmpty()) {
            val emptyRevH = 40f
            ensureSpace(emptyRevH)
            if (!isDryRun && currentCanvas != null) {
                currentY = drawEmptyRevisionBox(currentCanvas!!, currentY, tfBold)
            } else {
                currentY += emptyRevH
            }
        } else {
            val descPaint = TextPaint().apply {
                isAntiAlias = true
                color = Color.parseColor(COLOR_TEXT_SECONDARY)
                textSize = 8.5f
                typeface = tfRegular
            }

            for (round in report.revisionRounds) {
                val roundBannerH = 26f
                val revTableH = 22f
                ensureSpace(roundBannerH + revTableH + 32f)

                activeTableHeaderDrawer = { c, y ->
                    val y1 = drawRevisionRoundBanner(c, round.phaseNum, round.totalCount, round.appliedCount, round.isRoundComplete, y, tfBold)
                    drawRevisionTableHeader(c, y1, tfBold)
                }

                if (!isDryRun && currentCanvas != null) {
                    currentY = drawRevisionRoundBanner(currentCanvas!!, round.phaseNum, round.totalCount, round.appliedCount, round.isRoundComplete, currentY, tfBold)
                    currentY = drawRevisionTableHeader(currentCanvas!!, currentY, tfBold)
                } else {
                    currentY += roundBannerH + revTableH
                }

                for (idx in round.items.indices) {
                    val item = round.items[idx]
                    val descH = measureRtlTextHeight(item.description, 370f, descPaint)
                    val rowH = maxOf(25f, descH + 11f)

                    ensureSpace(rowH)

                    if (!isDryRun && currentCanvas != null) {
                        currentY = drawRevisionItemRow(currentCanvas!!, idx + 1, item.description, item.isApplied == 1, currentY, rowH, tfBold, tfRegular, descPaint)
                    } else {
                        currentY += rowH
                    }
                }
                activeTableHeaderDrawer = null
                currentY += 10f
            }
        }
        currentY += 12f

        // Section 7: Financial Status & Payments
        val finHeaderH = 28f
        val finBoxH = 88f
        ensureSpace(finHeaderH + finBoxH + 20f)

        if (!isDryRun && currentCanvas != null) {
            currentY = drawSectionHeader(currentCanvas!!, "صورت‌وضعیت مالی و تسویه حساب پروژه", currentY, tfBold)
            currentY = drawFinancialSummaryBox(currentCanvas!!, report, currentY, tfBold, tfRegular)
        } else {
            currentY += finHeaderH + finBoxH
        }

        if (report.payments.isNotEmpty()) {
            val payHeaderH = 24f
            ensureSpace(payHeaderH + 30f)

            activeTableHeaderDrawer = { c, y ->
                drawPaymentsTableHeader(c, y, tfBold)
            }

            if (!isDryRun && currentCanvas != null) {
                currentY = drawPaymentsTableHeader(currentCanvas!!, currentY, tfBold)
            } else {
                currentY += payHeaderH
            }

            for (pIdx in report.payments.indices) {
                val payment = report.payments[pIdx]
                val payRowH = 24f
                ensureSpace(payRowH)

                if (!isDryRun && currentCanvas != null) {
                    currentY = drawPaymentRow(currentCanvas!!, pIdx + 1, payment.date, payment.amount, payment.note ?: "واریز نقدی / ثبت حسابداری", currentY, payRowH, tfBold, tfRegular)
                } else {
                    currentY += payRowH
                }
            }
            activeTableHeaderDrawer = null
            currentY += 14f
        }

        // Section 8: Formal Sign-off & Verification Seal
        val signOffH = 92f
        ensureSpace(signOffH + 10f)

        if (!isDryRun && currentCanvas != null) {
            currentY = drawOfficialSignOff(currentCanvas!!, report, currentY, tfBold, tfRegular)
        } else {
            currentY += signOffH
        }

        closePage()

        return pageIndex
    }

    // =========================================================================
    // COVER PAGE RENDERING (PAGE 1)
    // =========================================================================

    private fun drawCoverPage(
        canvas: Canvas,
        report: ProjectReportData,
        tfBold: Typeface,
        tfRegular: Typeface,
        logoBitmap: Bitmap?,
        totalPages: Int
    ) {
        // 1. Elegant outer document border
        val outerBorder = Paint().apply {
            color = Color.parseColor(COLOR_OUTER_BORDER)
            style = Paint.Style.STROKE
            strokeWidth = 1f
            isAntiAlias = true
        }
        val outerRect = RectF(24f, 24f, PAGE_WIDTH - 24f, PAGE_HEIGHT - 24f)
        canvas.drawRoundRect(outerRect, 8f, 8f, outerBorder)

        // 2. Top Header Accent Band (Deep Indigo with Cyan Line)
        val topAccent = Paint().apply { color = Color.parseColor(COLOR_PRIMARY_BRAND); style = Paint.Style.FILL }
        canvas.drawRect(24f, 24f, PAGE_WIDTH - 24f, 32f, topAccent)
        val topCyan = Paint().apply { color = Color.parseColor(COLOR_ACCENT_CYAN); style = Paint.Style.FILL }
        canvas.drawRect(24f, 32f, PAGE_WIDTH - 24f, 34f, topCyan)

        // 3. Logo Mark
        val logoSize = 52f
        val logoLeft = (PAGE_WIDTH - logoSize) / 2f
        val logoTop = 75f
        if (logoBitmap != null) {
            canvas.drawBitmap(logoBitmap, null, RectF(logoLeft, logoTop, logoLeft + logoSize, logoTop + logoSize), null)
        }

        // 4. Software Title & Subtitle
        val brandPaint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor(COLOR_PRIMARY_BRAND)
            textSize = 21f
            typeface = tfBold
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("کاترلاگ (CutterLog)", PAGE_WIDTH / 2f, 150f, brandPaint)

        val subBrandPaint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor(COLOR_TEXT_MUTED)
            textSize = 9.5f
            typeface = tfRegular
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("سامانه مدیریت سفارشات و پایش زمان پروژه‌های تدوین ویدئو", PAGE_WIDTH / 2f, 168f, subBrandPaint)

        // Subtle Divider
        val divPaint = Paint().apply { color = Color.parseColor(COLOR_DIVIDER); strokeWidth = 0.8f }
        canvas.drawLine(140f, 192f, PAGE_WIDTH - 140f, 192f, divPaint)

        // 5. Document Main Title
        val docTitlePaint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor(COLOR_TEXT_PRIMARY)
            textSize = 23f
            typeface = tfBold
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("گزارش رسمی پروژه تدوین", PAGE_WIDTH / 2f, 234f, docTitlePaint)

        val docSubtitlePaint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor(COLOR_TEXT_SECONDARY)
            textSize = 10.5f
            typeface = tfRegular
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("شناسنامه، سوابق اصلاحات، صورت‌وضعیت مالی و تحویل", PAGE_WIDTH / 2f, 254f, docSubtitlePaint)

        val docEnPaint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor(COLOR_ACCENT_LINE)
            textSize = 8.5f
            typeface = tfBold
            textAlign = Paint.Align.CENTER
            letterSpacing = 0.12f
        }
        canvas.drawText("OFFICIAL PROJECT EDITING & DELIVERY DOSSIER", PAGE_WIDTH / 2f, 272f, docEnPaint)

        // 6. Central Formal Project Overview Card
        val cardW = 472f
        val cardH = 280f
        val cardLeft = (PAGE_WIDTH - cardW) / 2f
        val cardTop = 305f
        val cardRect = RectF(cardLeft, cardTop, cardLeft + cardW, cardTop + cardH)

        val cardBgPaint = Paint().apply { color = Color.parseColor(COLOR_CARD_BG); style = Paint.Style.FILL }
        val cardBorderPaint = Paint().apply { color = Color.parseColor(COLOR_CARD_BORDER); style = Paint.Style.STROKE; strokeWidth = 1f }
        canvas.drawRoundRect(cardRect, 8f, 8f, cardBgPaint)
        canvas.drawRoundRect(cardRect, 8f, 8f, cardBorderPaint)

        // Card Header Banner
        val cardHeaderRect = RectF(cardLeft, cardTop, cardLeft + cardW, cardTop + 34f)
        val cardHeaderBg = Paint().apply { color = Color.parseColor(COLOR_SECTION_HEADER_BG); style = Paint.Style.FILL }
        canvas.drawRoundRect(cardHeaderRect, 8f, 8f, cardHeaderBg)
        canvas.drawRect(cardLeft, cardTop + 24f, cardLeft + cardW, cardTop + 34f, cardHeaderBg) // square bottom corners
        canvas.drawLine(cardLeft, cardTop + 34f, cardLeft + cardW, cardTop + 34f, cardBorderPaint)

        val chRightText = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor(COLOR_PRIMARY_BRAND)
            textSize = 11f
            typeface = tfBold
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText("مشخصات و شناسنامه سند", cardLeft + cardW - 14f, cardTop + 22f, chRightText)

        val chLeftText = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor(COLOR_TEXT_PRIMARY)
            textSize = 10f
            typeface = tfBold
            textAlign = Paint.Align.LEFT
        }
        canvas.drawText("شماره سند: ${report.reportNumber}", cardLeft + 14f, cardTop + 22f, chLeftText)

        // Card Rows
        val col1Right = cardLeft + cardW - 16f
        val col2Right = cardLeft + (cardW / 2f) - 6f
        val rowDivPaint = Paint().apply { color = Color.parseColor("#E8EDF4"); strokeWidth = 0.6f }

        val lblPaint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor(COLOR_TEXT_MUTED)
            textSize = 9.5f
            typeface = tfRegular
            textAlign = Paint.Align.RIGHT
        }
        val valPaint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor(COLOR_TEXT_PRIMARY)
            textSize = 10.5f
            typeface = tfBold
            textAlign = Paint.Align.RIGHT
        }

        var rY = cardTop + 62f

        // Row 1: Project Name & Studio
        canvas.drawText("نام پروژه:", col1Right, rY, lblPaint)
        val pName = if (report.project.name.length > 28) report.project.name.take(26) + "..." else report.project.name
        canvas.drawText(pName, col1Right - 60f, rY, valPaint)

        canvas.drawText("استودیو طرف حساب:", col2Right, rY, lblPaint)
        val studioPaint = Paint(valPaint).apply { color = Color.parseColor(COLOR_PRIMARY_BRAND) }
        canvas.drawText(report.studioDisplayName, col2Right - 84f, rY, studioPaint)

        rY += 26f
        canvas.drawLine(cardLeft + 12f, rY - 14f, cardLeft + cardW - 12f, rY - 14f, rowDivPaint)

        // Row 2: Wedding Date & Created At
        canvas.drawText("تاریخ پروژه / مراسم:", col1Right, rY, lblPaint)
        val wStr = report.project.weddingDate?.let { PersianUtils.faNum(it) } ?: "ثبت نشده"
        val regValPaint = Paint(valPaint).apply { typeface = tfRegular }
        canvas.drawText(wStr, col1Right - 88f, rY, regValPaint)

        canvas.drawText("تاریخ ثبت در سامانه:", col2Right, rY, lblPaint)
        canvas.drawText(PersianUtils.faNum(report.project.createdAt), col2Right - 86f, rY, regValPaint)

        rY += 26f
        canvas.drawLine(cardLeft + 12f, rY - 14f, cardLeft + cardW - 12f, rY - 14f, rowDivPaint)

        // Row 3: Price & Settlement Status
        canvas.drawText("ارزش کل قرارداد:", col1Right, rY, lblPaint)
        canvas.drawText(PersianUtils.formatCurrencyFa(report.project.price), col1Right - 78f, rY, valPaint)

        canvas.drawText("وضعیت تسویه مالی:", col2Right, rY, lblPaint)
        val settlePaint = Paint(valPaint).apply {
            color = if (report.isSettled) Color.parseColor(COLOR_SUCCESS) else Color.parseColor(COLOR_ERROR)
        }
        val settleText = if (report.isSettled) "تسویه کامل و نهایی ✓" else "دارای مانده بدهی (${PersianUtils.formatCurrencyFa(report.remainingBalance)})"
        canvas.drawText(settleText, col2Right - 84f, rY, settlePaint)

        rY += 26f
        canvas.drawLine(cardLeft + 12f, rY - 14f, cardLeft + cardW - 12f, rY - 14f, rowDivPaint)

        // Row 4: Clips count & Total Work Time
        canvas.drawText("کلیپ‌های پروژه:", col1Right, rY, lblPaint)
        val clipsSummary = "${PersianUtils.faNum(report.completedClipsCount)} از ${PersianUtils.faNum(report.totalClipsCount)} تکمیل‌شده (${PersianUtils.faNum(report.progressPercent)}٪)"
        canvas.drawText(clipsSummary, col1Right - 72f, rY, valPaint)

        canvas.drawText("کل زمان کارکرد خالص:", col2Right, rY, lblPaint)
        canvas.drawText(report.formattedDurationPersianWords, col2Right - 94f, rY, valPaint)

        rY += 28f
        canvas.drawLine(cardLeft + 12f, rY - 14f, cardLeft + cardW - 12f, rY - 14f, rowDivPaint)

        // Row 5: Final Status Pill Badge
        canvas.drawText("وضعیت نهایی پروژه:", col1Right, rY + 8f, lblPaint)

        val (statusText, stBg, stBorder, stColor) = when (report.project.status) {
            "COMPLETED" -> Quadruple("تکمیل شده و تحویل نهایی کارفرما ✓", COLOR_SUCCESS_BG, COLOR_SUCCESS_BORDER, COLOR_SUCCESS)
            "REVISION" -> Quadruple("در مرحله اعمال اصلاحات و بازبینی", COLOR_WARNING_BG, COLOR_WARNING_BORDER, COLOR_WARNING)
            else -> Quadruple("در حال تدوین اولیه روی تایم‌لاین", COLOR_INFO_BG, COLOR_INFO_BORDER, COLOR_INFO)
        }

        val badgePaint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor(stColor)
            textSize = 9.5f
            typeface = tfBold
            textAlign = Paint.Align.CENTER
        }
        val badgeW = 210f
        val badgeH = 22f
        val badgeLeft = col1Right - 92f - badgeW
        val badgeTop = rY - 6f
        val badgeRect = RectF(badgeLeft, badgeTop, badgeLeft + badgeW, badgeTop + badgeH)

        val pillBg = Paint().apply { color = Color.parseColor(stBg); style = Paint.Style.FILL }
        val pillStroke = Paint().apply { color = Color.parseColor(stBorder); style = Paint.Style.STROKE; strokeWidth = 0.8f }
        canvas.drawRoundRect(badgeRect, 11f, 11f, pillBg)
        canvas.drawRoundRect(badgeRect, 11f, 11f, pillStroke)
        canvas.drawText(statusText, badgeRect.centerX(), badgeRect.centerY() + 3.5f, badgePaint)

        // 7. Bottom Verification Note Box
        val verBoxW = 472f
        val verBoxH = 62f
        val verBoxLeft = (PAGE_WIDTH - verBoxW) / 2f
        val verBoxTop = 645f
        val verRect = RectF(verBoxLeft, verBoxTop, verBoxLeft + verBoxW, verBoxTop + verBoxH)

        val verBg = Paint().apply { color = Color.parseColor(COLOR_PAGE_BG); style = Paint.Style.FILL }
        val verStroke = Paint().apply { color = Color.parseColor(COLOR_DIVIDER); style = Paint.Style.STROKE; strokeWidth = 0.8f }
        canvas.drawRoundRect(verRect, 6f, 6f, verBg)
        canvas.drawRoundRect(verRect, 6f, 6f, verStroke)

        val verText1 = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor(COLOR_TEXT_MUTED)
            textSize = 8.5f
            typeface = tfRegular
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("این سند رسمی دیجیتال استخراج‌شده از نرم‌افزار کاترلاگ (CutterLog) می‌باشد و اطلاعات آن معتبر و مستند است.", PAGE_WIDTH / 2f, verBoxTop + 24f, verText1)

        val verText2 = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor(COLOR_TEXT_PRIMARY)
            textSize = 9f
            typeface = tfBold
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("تاریخ صدور سند: ${PersianUtils.faNum(report.reportDate)}   •   شناسه پیگیری: ${report.reportNumber}", PAGE_WIDTH / 2f, verBoxTop + 44f, verText2)

        // Cover Footer
        val coverFootPaint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor(COLOR_TEXT_LIGHT)
            textSize = 7.5f
            typeface = tfRegular
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("CutterLog Video Editing Project Dossier  •  Confidential & Official Document", PAGE_WIDTH / 2f, 792f, coverFootPaint)

        val pageNumPaint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor(COLOR_TEXT_MUTED)
            textSize = 8.5f
            typeface = tfBold
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("صفحه ۱ از ${PersianUtils.faNum(totalPages)}", PAGE_WIDTH / 2f, 814f, pageNumPaint)
    }

    // =========================================================================
    // CONTENT PAGES HEADER & FOOTER (PAGE 2+)
    // =========================================================================

    private fun drawContentPageHeader(
        canvas: Canvas,
        report: ProjectReportData,
        tfBold: Typeface,
        tfRegular: Typeface
    ) {
        val hRight = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor(COLOR_PRIMARY_BRAND)
            textSize = 10f
            typeface = tfBold
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText("کاترلاگ (CutterLog)", CONTENT_RIGHT, 54f, hRight)

        val hCenter = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor(COLOR_TEXT_SECONDARY)
            textSize = 9f
            typeface = tfRegular
            textAlign = Paint.Align.CENTER
        }
        val projTitle = if (report.project.name.length > 35) report.project.name.take(33) + "..." else report.project.name
        canvas.drawText("گزارش پروژه: $projTitle", PAGE_WIDTH / 2f, 54f, hCenter)

        val hLeft = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor(COLOR_TEXT_MUTED)
            textSize = 8.5f
            typeface = tfBold
            textAlign = Paint.Align.LEFT
        }
        canvas.drawText("شناسه: ${report.reportNumber}", CONTENT_LEFT, 54f, hLeft)

        val divPaint = Paint().apply { color = Color.parseColor(COLOR_CARD_BORDER); strokeWidth = 0.75f }
        canvas.drawLine(CONTENT_LEFT, HEADER_BOTTOM, CONTENT_RIGHT, HEADER_BOTTOM, divPaint)
    }

    private fun drawContentPageFooter(
        canvas: Canvas,
        report: ProjectReportData,
        pageIndex: Int,
        totalPages: Int,
        tfBold: Typeface,
        tfRegular: Typeface
    ) {
        val divPaint = Paint().apply { color = Color.parseColor(COLOR_DIVIDER); strokeWidth = 0.75f }
        canvas.drawLine(CONTENT_LEFT, FOOTER_DIVIDER_Y, CONTENT_RIGHT, FOOTER_DIVIDER_Y, divPaint)

        val fRight = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor(COLOR_TEXT_LIGHT)
            textSize = 8f
            typeface = tfRegular
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText("کاترلاگ • سامانه پایش زمان و مدیریت تدوین", CONTENT_RIGHT, 822f, fRight)

        val fCenter = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor(COLOR_TEXT_PRIMARY)
            textSize = 8.5f
            typeface = tfBold
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("صفحه ${PersianUtils.faNum(pageIndex)} از ${PersianUtils.faNum(totalPages)}", PAGE_WIDTH / 2f, 822f, fCenter)

        val fLeft = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor(COLOR_TEXT_LIGHT)
            textSize = 8f
            typeface = tfRegular
            textAlign = Paint.Align.LEFT
        }
        canvas.drawText("تاریخ صدور: ${PersianUtils.faNum(report.reportDate)}", CONTENT_LEFT, 822f, fLeft)
    }

    // =========================================================================
    // SECTION BUILDERS
    // =========================================================================

    private fun drawSectionHeader(canvas: Canvas, title: String, startY: Float, tfBold: Typeface): Float {
        val barPaint = Paint().apply { color = Color.parseColor(COLOR_PRIMARY_BRAND); style = Paint.Style.FILL }
        canvas.drawRoundRect(RectF(CONTENT_RIGHT - 4f, startY + 2f, CONTENT_RIGHT, startY + 16f), 2f, 2f, barPaint)

        val titlePaint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor(COLOR_TEXT_PRIMARY)
            textSize = 11.5f
            typeface = tfBold
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText(title, CONTENT_RIGHT - 10f, startY + 13f, titlePaint)

        val linePaint = Paint().apply { color = Color.parseColor(COLOR_DIVIDER); strokeWidth = 0.6f }
        val titleW = titlePaint.measureText(title)
        canvas.drawLine(CONTENT_LEFT, startY + 10f, CONTENT_RIGHT - 16f - titleW, startY + 10f, linePaint)

        return startY + 24f
    }

    // SECTION 1: PROJECT IDENTITY
    private fun drawSection1ProjectIdentity(
        canvas: Canvas,
        report: ProjectReportData,
        startY: Float,
        tfBold: Typeface,
        tfRegular: Typeface
    ): Float {
        var curY = drawSectionHeader(canvas, "۱. شناسنامه و مشخصات پایه پروژه", startY, tfBold)

        val boxH = 82f
        val boxRect = RectF(CONTENT_LEFT, curY, CONTENT_RIGHT, curY + boxH)
        val bgPaint = Paint().apply { color = Color.parseColor(COLOR_CARD_BG); style = Paint.Style.FILL }
        val borderPaint = Paint().apply { color = Color.parseColor(COLOR_CARD_BORDER); style = Paint.Style.STROKE; strokeWidth = 0.8f }
        canvas.drawRoundRect(boxRect, 6f, 6f, bgPaint)
        canvas.drawRoundRect(boxRect, 6f, 6f, borderPaint)

        val col1R = CONTENT_RIGHT - 14f
        val col2R = CONTENT_LEFT + (CONTENT_WIDTH / 2f) - 6f
        val divPaint = Paint().apply { color = Color.parseColor("#EBF0F5"); strokeWidth = 0.5f }

        val lblP = Paint().apply { isAntiAlias = true; color = Color.parseColor(COLOR_TEXT_MUTED); textSize = 8.5f; typeface = tfRegular; textAlign = Paint.Align.RIGHT }
        val valP = Paint().apply { isAntiAlias = true; color = Color.parseColor(COLOR_TEXT_PRIMARY); textSize = 9.5f; typeface = tfBold; textAlign = Paint.Align.RIGHT }

        var rY = curY + 20f

        // Row 1
        canvas.drawText("نام پروژه:", col1R, rY, lblP)
        val pName = if (report.project.name.length > 30) report.project.name.take(28) + "..." else report.project.name
        canvas.drawText(pName, col1R - 52f, rY, valP)

        canvas.drawText("طرف حساب:", col2R, rY, lblP)
        val stPaint = Paint(valP).apply { color = Color.parseColor(COLOR_PRIMARY_BRAND) }
        canvas.drawText(report.studioDisplayName, col2R - 58f, rY, stPaint)

        rY += 23f
        canvas.drawLine(CONTENT_LEFT + 8f, rY - 12f, CONTENT_RIGHT - 8f, rY - 12f, divPaint)

        // Row 2
        canvas.drawText("تاریخ مراسم:", col1R, rY, lblP)
        val wStr = report.project.weddingDate?.let { PersianUtils.faNum(it) } ?: "ثبت نشده"
        canvas.drawText(wStr, col1R - 60f, rY, valP)

        canvas.drawText("تاریخ ثبت:", col2R, rY, lblP)
        canvas.drawText(PersianUtils.faNum(report.project.createdAt), col2R - 50f, rY, valP)

        rY += 23f
        canvas.drawLine(CONTENT_LEFT + 8f, rY - 12f, CONTENT_RIGHT - 8f, rY - 12f, divPaint)

        // Row 3
        canvas.drawText("موعد تحویل:", col1R, rY, lblP)
        val dStr = report.project.deadlineDate?.let { PersianUtils.faNum(it) } ?: "نامحدود / تعیین‌نشده"
        canvas.drawText(dStr, col1R - 60f, rY, valP)

        canvas.drawText("وضعیت تسویه:", col2R, rY, lblP)
        val setP = Paint(valP).apply {
            color = if (report.isSettled) Color.parseColor(COLOR_SUCCESS) else Color.parseColor(COLOR_ERROR)
        }
        val setStr = if (report.isSettled) "تسویه کامل نهایی ✓" else "دارای مانده بدهی (${PersianUtils.formatCurrencyFa(report.remainingBalance)})"
        canvas.drawText(setStr, col2R - 64f, rY, setP)

        return curY + boxH + 16f
    }

    // SECTION 2: OPERATIONAL METRICS & PROGRESS
    private fun drawSection2OperationalMetrics(
        canvas: Canvas,
        report: ProjectReportData,
        startY: Float,
        tfBold: Typeface,
        tfRegular: Typeface
    ): Float {
        var curY = drawSectionHeader(canvas, "۲. خلاصه آماری و شاخص‌های تدوین", startY, tfBold)

        val cardSpacing = 7f
        val cardW = (CONTENT_WIDTH - (3f * cardSpacing)) / 4f
        val cardH = 46f

        val metrics = listOf(
            Pair(PersianUtils.faNum(report.totalClipsCount), "کلیپ‌های ثبت‌شده"),
            Pair(PersianUtils.faNum(report.completedClipsCount), "تکمیل‌شده"),
            Pair(PersianUtils.faNum(report.remainingClipsCount), "باقیمانده"),
            Pair("${PersianUtils.faNum(report.progressPercent)}٪", "پیشرفت فیزیکی")
        )

        val numPaint = Paint().apply { isAntiAlias = true; color = Color.parseColor(COLOR_TEXT_PRIMARY); textSize = 14f; typeface = tfBold; textAlign = Paint.Align.CENTER }
        val lblPaint = Paint().apply { isAntiAlias = true; color = Color.parseColor(COLOR_TEXT_SECONDARY); textSize = 8f; typeface = tfRegular; textAlign = Paint.Align.CENTER }
        val cardBg = Paint().apply { color = Color.parseColor(COLOR_CARD_BG); style = Paint.Style.FILL }
        val cardBorder = Paint().apply { color = Color.parseColor(COLOR_CARD_BORDER); style = Paint.Style.STROKE; strokeWidth = 0.8f }

        for (i in 0..3) {
            val cLeft = CONTENT_RIGHT - ((i + 1) * cardW) - (i * cardSpacing)
            val cRect = RectF(cLeft, curY, cLeft + cardW, curY + cardH)
            canvas.drawRoundRect(cRect, 5f, 5f, cardBg)
            canvas.drawRoundRect(cRect, 5f, 5f, cardBorder)

            val (num, lbl) = metrics[i]
            canvas.drawText(num, cRect.centerX(), curY + 20f, numPaint)
            canvas.drawText(lbl, cRect.centerX(), curY + 36f, lblPaint)
        }

        curY += cardH + 12f

        // Progress Bar
        val pBarBg = Paint().apply { color = Color.parseColor("#E2E8F0"); style = Paint.Style.FILL }
        val pBarFill = Paint().apply { color = Color.parseColor(COLOR_PRIMARY_BRAND); style = Paint.Style.FILL }
        val barRect = RectF(CONTENT_LEFT, curY, CONTENT_RIGHT, curY + 7f)
        canvas.drawRoundRect(barRect, 3.5f, 3.5f, pBarBg)

        val fillW = CONTENT_WIDTH * (report.progressPercent.coerceIn(0, 100) / 100f)
        if (fillW > 0f) {
            val fillRect = RectF(CONTENT_RIGHT - fillW, curY, CONTENT_RIGHT, curY + 7f)
            canvas.drawRoundRect(fillRect, 3.5f, 3.5f, pBarFill)
        }

        curY += 19f

        // Pure Duration Line
        val durLabelPaint = Paint().apply { isAntiAlias = true; color = Color.parseColor(COLOR_TEXT_SECONDARY); textSize = 9f; typeface = tfRegular; textAlign = Paint.Align.RIGHT }
        val durValPaint = Paint().apply { isAntiAlias = true; color = Color.parseColor(COLOR_PRIMARY_BRAND); textSize = 9f; typeface = tfBold; textAlign = Paint.Align.RIGHT }

        canvas.drawText("مجموع کارکرد خالص روی تایم‌لاین:", CONTENT_RIGHT, curY, durLabelPaint)
        val lblW = durLabelPaint.measureText("مجموع کارکرد خالص روی تایم‌لاین:")
        canvas.drawText("  ${report.formattedDurationPersianWords} (${report.formattedDurationHMS})", CONTENT_RIGHT - lblW, curY, durValPaint)

        return curY + 16f
    }

    // SECTION 3: DELIVERY STATUS
    private fun drawSection3DeliveryStatus(
        canvas: Canvas,
        report: ProjectReportData,
        startY: Float,
        tfBold: Typeface,
        tfRegular: Typeface
    ): Float {
        var curY = drawSectionHeader(canvas, "۳. وضعیت تحویل و خروجی‌های پروژه", startY, tfBold)

        val boxH = 46f
        val boxRect = RectF(CONTENT_LEFT, curY, CONTENT_RIGHT, curY + boxH)
        val bgPaint = Paint().apply { color = Color.parseColor(COLOR_CARD_BG); style = Paint.Style.FILL }
        val borderPaint = Paint().apply { color = Color.parseColor(COLOR_CARD_BORDER); style = Paint.Style.STROKE; strokeWidth = 0.8f }
        canvas.drawRoundRect(boxRect, 6f, 6f, bgPaint)
        canvas.drawRoundRect(boxRect, 6f, 6f, borderPaint)

        val isAllDone = (report.completedClipsCount == report.totalClipsCount && report.totalClipsCount > 0)
        val statusText = if (isAllDone) {
            "وضعیت تحویل: کلیه کلیپ‌های پروژه با موفقیت تدوین و آماده تحویل نهایی گردیده است."
        } else {
            "وضعیت تحویل: تعداد ${PersianUtils.faNum(report.completedClipsCount)} کلیپ تکمیل شده و ${PersianUtils.faNum(report.remainingClipsCount)} کلیپ در فرآیند تدوین قرار دارد."
        }

        val sPaint = Paint().apply {
            isAntiAlias = true
            color = if (isAllDone) Color.parseColor(COLOR_SUCCESS) else Color.parseColor(COLOR_TEXT_PRIMARY)
            textSize = 9f
            typeface = tfBold
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText(statusText, CONTENT_RIGHT - 12f, curY + 18f, sPaint)

        val lastDelDate = report.lastDeliveryDate?.let { "آخرین تاریخ تحویل کلیپ: ${PersianUtils.faNum(it)}" }
            ?: "هنوز تاریخ تحویل قطعی برای این پروژه در سامانه ثبت نشده است."
        val datePaint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor(COLOR_TEXT_MUTED)
            textSize = 8.5f
            typeface = tfRegular
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText(lastDelDate, CONTENT_RIGHT - 12f, curY + 34f, datePaint)

        return curY + boxH + 16f
    }

    // SECTION 4: PROJECT NOTES
    private fun collectProjectNotes(report: ProjectReportData): String {
        val notes = mutableListOf<String>()
        report.sessions.forEach { s ->
            if (!s.note.isNullOrBlank()) notes.add(s.note.trim())
        }
        report.payments.forEach { p ->
            if (!p.note.isNullOrBlank()) notes.add(p.note.trim())
        }
        return notes.distinct().joinToString("\n• ", prefix = "• ")
    }

    private fun drawSection4ProjectNotes(
        canvas: Canvas,
        notes: String,
        startY: Float,
        tfBold: Typeface,
        tfRegular: Typeface,
        notePaint: TextPaint
    ): Float {
        var curY = drawSectionHeader(canvas, "۴. یادداشت‌ها و توضیحات تکمیلی پروژه", startY, tfBold)

        val textH = measureRtlTextHeight(notes, CONTENT_WIDTH - 24f, notePaint)
        val boxH = textH + 24f

        val boxRect = RectF(CONTENT_LEFT, curY, CONTENT_RIGHT, curY + boxH)
        val bgPaint = Paint().apply { color = Color.parseColor(COLOR_CARD_BG); style = Paint.Style.FILL }
        val borderPaint = Paint().apply { color = Color.parseColor(COLOR_CARD_BORDER); style = Paint.Style.STROKE; strokeWidth = 0.8f }
        canvas.drawRoundRect(boxRect, 6f, 6f, bgPaint)
        canvas.drawRoundRect(boxRect, 6f, 6f, borderPaint)

        drawRtlText(canvas, notes, CONTENT_LEFT + 12f, curY + 12f, CONTENT_WIDTH - 24f, notePaint)

        return curY + boxH + 16f
    }

    // =========================================================================
    // SECTION 5: CLIPS TABLE
    // =========================================================================

    private fun drawClipsTableHeader(canvas: Canvas, curY: Float, tfBold: Typeface): Float {
        val thH = 24f
        val rect = RectF(CONTENT_LEFT, curY, CONTENT_RIGHT, curY + thH)
        val bgPaint = Paint().apply { color = Color.parseColor(COLOR_SECTION_HEADER_BG); style = Paint.Style.FILL }
        val borderPaint = Paint().apply { color = Color.parseColor(COLOR_CARD_BORDER); style = Paint.Style.STROKE; strokeWidth = 0.8f }
        canvas.drawRoundRect(rect, 4f, 4f, bgPaint)
        canvas.drawRoundRect(rect, 4f, 4f, borderPaint)

        val thText = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor(COLOR_TEXT_PRIMARY)
            textSize = 8.5f
            typeface = tfBold
            textAlign = Paint.Align.CENTER
        }

        // Columns:
        // Col 1 (Row): width 35f (Right edge: CONTENT_RIGHT)
        // Col 2 (Name): width 235f
        // Col 3 (Time/Date): width 110f
        // Col 4 (Status): width 143f
        val col1R = CONTENT_RIGHT
        val col1L = col1R - 35f
        val col2R = col1L
        val col2L = col2R - 235f
        val col3R = col2L
        val col3L = col3R - 110f
        val col4R = col3L
        val col4L = CONTENT_LEFT

        val base = curY + 16f
        canvas.drawText("ردیف", (col1R + col1L) / 2f, base, thText)

        thText.textAlign = Paint.Align.RIGHT
        canvas.drawText("عنوان کلیپ", col2R - 10f, base, thText)

        thText.textAlign = Paint.Align.CENTER
        canvas.drawText("زمان برآورد / تحویل", (col3R + col3L) / 2f, base, thText)
        canvas.drawText("وضعیت تدوین", (col4R + col4L) / 2f, base, thText)

        return curY + thH
    }

    private fun drawClipRow(
        canvas: Canvas,
        rowNum: Int,
        clip: com.example.data.entity.ProjectClipEntity,
        curY: Float,
        rowH: Float,
        tfBold: Typeface,
        tfRegular: Typeface,
        namePaint: TextPaint
    ): Float {
        val rect = RectF(CONTENT_LEFT, curY, CONTENT_RIGHT, curY + rowH)
        if (rowNum % 2 == 1) {
            val altPaint = Paint().apply { color = Color.parseColor(COLOR_ALT_ROW_BG); style = Paint.Style.FILL }
            canvas.drawRect(rect, altPaint)
        }
        val linePaint = Paint().apply { color = Color.parseColor(COLOR_DIVIDER); strokeWidth = 0.5f }
        canvas.drawLine(CONTENT_LEFT, curY + rowH, CONTENT_RIGHT, curY + rowH, linePaint)

        val col1R = CONTENT_RIGHT
        val col1L = col1R - 35f
        val col2R = col1L
        val col2L = col2R - 235f
        val col3R = col2L
        val col3L = col3R - 110f
        val col4R = col3L
        val col4L = CONTENT_LEFT

        val numPaint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor(COLOR_TEXT_MUTED)
            textSize = 8.5f
            typeface = tfBold
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(PersianUtils.faNum(rowNum), (col1R + col1L) / 2f, curY + 16f, numPaint)

        // Title (wrapped if needed)
        drawRtlText(canvas, clip.clipName, col2L + 10f, curY + 6f, 220f, namePaint)

        // Estimated Time / Delivery Date
        val metaPaint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor(COLOR_TEXT_SECONDARY)
            textSize = 8f
            typeface = tfRegular
            textAlign = Paint.Align.CENTER
        }
        val timeOrDate = if (!clip.endDate.isNullOrBlank()) {
            "تحویل: ${PersianUtils.faNum(clip.endDate)}"
        } else if (clip.estimateMins > 0) {
            "برآورد: ${PersianUtils.faNum(clip.estimateMins)} دقیقه"
        } else {
            "—"
        }
        canvas.drawText(timeOrDate, (col3R + col3L) / 2f, curY + 16f, metaPaint)

        // Status Badge
        val isDone = clip.isDone == 1
        val badgeW = 92f
        val badgeH = 18f
        val badgeLeft = (col4R + col4L - badgeW) / 2f
        val badgeTop = curY + (rowH - badgeH) / 2f
        val bRect = RectF(badgeLeft, badgeTop, badgeLeft + badgeW, badgeTop + badgeH)

        val bBg = Paint().apply {
            color = Color.parseColor(if (isDone) COLOR_SUCCESS_BG else "#F1F5F9")
            style = Paint.Style.FILL
        }
        val bStroke = Paint().apply {
            color = Color.parseColor(if (isDone) COLOR_SUCCESS_BORDER else "#CBD5E1")
            style = Paint.Style.STROKE
            strokeWidth = 0.6f
        }
        canvas.drawRoundRect(bRect, 9f, 9f, bBg)
        canvas.drawRoundRect(bRect, 9f, 9f, bStroke)

        val bText = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor(if (isDone) COLOR_SUCCESS else COLOR_TEXT_MUTED)
            textSize = 7.5f
            typeface = tfBold
            textAlign = Paint.Align.CENTER
        }
        val bTitle = if (isDone) "تحویل کامل ✓" else "در حال تدوین"
        canvas.drawText(bTitle, bRect.centerX(), bRect.centerY() + 3f, bText)

        return curY + rowH
    }

    // =========================================================================
    // SECTION 6: REVISION HISTORY
    // =========================================================================

    private fun drawEmptyRevisionBox(canvas: Canvas, curY: Float, tfBold: Typeface): Float {
        val h = 38f
        val rect = RectF(CONTENT_LEFT, curY, CONTENT_RIGHT, curY + h)
        val bgPaint = Paint().apply { color = Color.parseColor(COLOR_SUCCESS_BG); style = Paint.Style.FILL }
        val borderPaint = Paint().apply { color = Color.parseColor(COLOR_SUCCESS_BORDER); style = Paint.Style.STROKE; strokeWidth = 0.8f }
        canvas.drawRoundRect(rect, 6f, 6f, bgPaint)
        canvas.drawRoundRect(rect, 6f, 6f, borderPaint)

        val textPaint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor(COLOR_SUCCESS)
            textSize = 9f
            typeface = tfBold
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("✓ هیچ‌گونه اصلاحیه‌ای برای این پروژه ثبت نشده است. خروجی در نسخه اولیه مورد تایید نهایی قرار گرفت.", PAGE_WIDTH / 2f, curY + 23f, textPaint)

        return curY + h
    }

    private fun drawRevisionRoundBanner(
        canvas: Canvas,
        phaseNum: Int,
        totalCount: Int,
        appliedCount: Int,
        isRoundComplete: Boolean,
        curY: Float,
        tfBold: Typeface
    ): Float {
        val bannerH = 26f
        val rect = RectF(CONTENT_LEFT, curY, CONTENT_RIGHT, curY + bannerH)
        val bgPaint = Paint().apply { color = Color.parseColor(COLOR_SECTION_HEADER_BG); style = Paint.Style.FILL }
        val borderPaint = Paint().apply { color = Color.parseColor(COLOR_CARD_BORDER); style = Paint.Style.STROKE; strokeWidth = 0.8f }
        canvas.drawRoundRect(rect, 4f, 4f, bgPaint)
        canvas.drawRoundRect(rect, 4f, 4f, borderPaint)

        val titlePaint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor(COLOR_PRIMARY_BRAND)
            textSize = 9.5f
            typeface = tfBold
            textAlign = Paint.Align.RIGHT
        }
        val roundTitle = "دور اصلاحات ${PersianUtils.faNum(phaseNum)}  •  تعداد کل: ${PersianUtils.faNum(totalCount)} مورد  •  اعمال‌شده: ${PersianUtils.faNum(appliedCount)}"
        canvas.drawText(roundTitle, CONTENT_RIGHT - 12f, curY + 17f, titlePaint)

        // Status Badge on Left
        val stText = if (isRoundComplete) "تکمیل و بسته شد ✓" else "در انتظار اعمال"
        val stColor = if (isRoundComplete) COLOR_SUCCESS else COLOR_WARNING
        val stBg = if (isRoundComplete) COLOR_SUCCESS_BG else COLOR_WARNING_BG
        val stBorder = if (isRoundComplete) COLOR_SUCCESS_BORDER else COLOR_WARNING_BORDER

        val badgeW = 94f
        val badgeH = 18f
        val bLeft = CONTENT_LEFT + 8f
        val bTop = curY + 4f
        val bRect = RectF(bLeft, bTop, bLeft + badgeW, bTop + badgeH)

        val pillBg = Paint().apply { color = Color.parseColor(stBg); style = Paint.Style.FILL }
        val pillStroke = Paint().apply { color = Color.parseColor(stBorder); style = Paint.Style.STROKE; strokeWidth = 0.6f }
        canvas.drawRoundRect(bRect, 9f, 9f, pillBg)
        canvas.drawRoundRect(bRect, 9f, 9f, pillStroke)

        val bText = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor(stColor)
            textSize = 7.5f
            typeface = tfBold
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(stText, bRect.centerX(), bRect.centerY() + 3f, bText)

        return curY + bannerH + 4f
    }

    private fun drawRevisionTableHeader(canvas: Canvas, curY: Float, tfBold: Typeface): Float {
        val thH = 22f
        val rect = RectF(CONTENT_LEFT, curY, CONTENT_RIGHT, curY + thH)
        val bgPaint = Paint().apply { color = Color.parseColor("#F1F5F9"); style = Paint.Style.FILL }
        val borderPaint = Paint().apply { color = Color.parseColor(COLOR_CARD_BORDER); style = Paint.Style.STROKE; strokeWidth = 0.6f }
        canvas.drawRect(rect, bgPaint)
        canvas.drawRect(rect, borderPaint)

        val thText = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor(COLOR_TEXT_PRIMARY)
            textSize = 8.5f
            typeface = tfBold
            textAlign = Paint.Align.CENTER
        }

        val col1R = CONTENT_RIGHT
        val col1L = col1R - 35f
        val col2R = col1L
        val col2L = col2R - 385f
        val col3R = col2L
        val col3L = CONTENT_LEFT

        val base = curY + 15f
        canvas.drawText("ردیف", (col1R + col1L) / 2f, base, thText)

        thText.textAlign = Paint.Align.RIGHT
        canvas.drawText("شرح اصلاحیه و بازبینی", col2R - 10f, base, thText)

        thText.textAlign = Paint.Align.CENTER
        canvas.drawText("وضعیت", (col3R + col3L) / 2f, base, thText)

        return curY + thH
    }

    private fun drawRevisionItemRow(
        canvas: Canvas,
        rowNum: Int,
        description: String,
        isApplied: Boolean,
        curY: Float,
        rowH: Float,
        tfBold: Typeface,
        tfRegular: Typeface,
        descPaint: TextPaint
    ): Float {
        val rect = RectF(CONTENT_LEFT, curY, CONTENT_RIGHT, curY + rowH)
        if (rowNum % 2 == 1) {
            val altPaint = Paint().apply { color = Color.parseColor(COLOR_ALT_ROW_BG); style = Paint.Style.FILL }
            canvas.drawRect(rect, altPaint)
        }
        val linePaint = Paint().apply { color = Color.parseColor(COLOR_DIVIDER); strokeWidth = 0.5f }
        canvas.drawLine(CONTENT_LEFT, curY + rowH, CONTENT_RIGHT, curY + rowH, linePaint)

        val col1R = CONTENT_RIGHT
        val col1L = col1R - 35f
        val col2R = col1L
        val col2L = col2R - 385f
        val col3R = col2L
        val col3L = CONTENT_LEFT

        val numPaint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor(COLOR_TEXT_MUTED)
            textSize = 8.5f
            typeface = tfBold
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(PersianUtils.faNum(rowNum), (col1R + col1L) / 2f, curY + 16f, numPaint)

        // Description with proper wrapping
        drawRtlText(canvas, description, col2L + 10f, curY + 6f, 370f, descPaint)

        // Badge
        val badgeW = 68f
        val badgeH = 18f
        val badgeLeft = (col3R + col3L - badgeW) / 2f
        val badgeTop = curY + (rowH - badgeH) / 2f
        val bRect = RectF(badgeLeft, badgeTop, badgeLeft + badgeW, badgeTop + badgeH)

        val bBg = Paint().apply {
            color = Color.parseColor(if (isApplied) COLOR_SUCCESS_BG else COLOR_WARNING_BG)
            style = Paint.Style.FILL
        }
        val bStroke = Paint().apply {
            color = Color.parseColor(if (isApplied) COLOR_SUCCESS_BORDER else COLOR_WARNING_BORDER)
            style = Paint.Style.STROKE
            strokeWidth = 0.6f
        }
        canvas.drawRoundRect(bRect, 9f, 9f, bBg)
        canvas.drawRoundRect(bRect, 9f, 9f, bStroke)

        val bText = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor(if (isApplied) COLOR_SUCCESS else COLOR_WARNING)
            textSize = 7.5f
            typeface = tfBold
            textAlign = Paint.Align.CENTER
        }
        val bTitle = if (isApplied) "اعمال شد ✓" else "معلق"
        canvas.drawText(bTitle, bRect.centerX(), bRect.centerY() + 3f, bText)

        return curY + rowH
    }

    // =========================================================================
    // SECTION 7: FINANCIAL SECTION
    // =========================================================================

    private fun drawFinancialSummaryBox(
        canvas: Canvas,
        report: ProjectReportData,
        curY: Float,
        tfBold: Typeface,
        tfRegular: Typeface
    ): Float {
        val boxH = 68f
        val boxRect = RectF(CONTENT_LEFT, curY, CONTENT_RIGHT, curY + boxH)
        val bgPaint = Paint().apply { color = Color.parseColor(COLOR_CARD_BG); style = Paint.Style.FILL }
        val borderPaint = Paint().apply { color = Color.parseColor(COLOR_CARD_BORDER); style = Paint.Style.STROKE; strokeWidth = 0.8f }
        canvas.drawRoundRect(boxRect, 6f, 6f, bgPaint)
        canvas.drawRoundRect(boxRect, 6f, 6f, borderPaint)

        val col1R = CONTENT_RIGHT - 14f
        val col2R = CONTENT_LEFT + (CONTENT_WIDTH / 2f) - 6f
        val divPaint = Paint().apply { color = Color.parseColor("#EBF0F5"); strokeWidth = 0.5f }

        val lblP = Paint().apply { isAntiAlias = true; color = Color.parseColor(COLOR_TEXT_MUTED); textSize = 8.5f; typeface = tfRegular; textAlign = Paint.Align.RIGHT }
        val valP = Paint().apply { isAntiAlias = true; color = Color.parseColor(COLOR_TEXT_PRIMARY); textSize = 10f; typeface = tfBold; textAlign = Paint.Align.RIGHT }

        var rY = curY + 20f

        // Row 1
        canvas.drawText("مبلغ کل قرارداد:", col1R, rY, lblP)
        val prStr = "${PersianUtils.formatCurrencyFa(report.project.price)} (${report.priceInWords})"
        canvas.drawText(prStr, col1R - 74f, rY, valP)

        canvas.drawText("وضعیت تسویه:", col2R, rY, lblP)
        val setP = Paint(valP).apply {
            color = if (report.isSettled) Color.parseColor(COLOR_SUCCESS) else Color.parseColor(COLOR_ERROR)
        }
        val setStr = if (report.isSettled) "تسویه کامل و نهایی ✓" else "دارای مانده بدهی قابل پیگیری"
        canvas.drawText(setStr, col2R - 64f, rY, setP)

        rY += 26f
        canvas.drawLine(CONTENT_LEFT + 8f, rY - 14f, CONTENT_RIGHT - 8f, rY - 14f, divPaint)

        // Row 2
        canvas.drawText("مجموع واریزشده:", col1R, rY, lblP)
        val paidP = Paint(valP).apply { color = Color.parseColor(COLOR_SUCCESS) }
        canvas.drawText(PersianUtils.formatCurrencyFa(report.totalPaid), col1R - 74f, rY, paidP)

        canvas.drawText("مانده بدهی / مطالبات:", col2R, rY, lblP)
        val remP = Paint(valP).apply {
            color = if (report.remainingBalance > 0) Color.parseColor(COLOR_ERROR) else Color.parseColor(COLOR_SUCCESS)
        }
        canvas.drawText(PersianUtils.formatCurrencyFa(report.remainingBalance), col2R - 92f, rY, remP)

        return curY + boxH + 12f
    }

    private fun drawPaymentsTableHeader(canvas: Canvas, curY: Float, tfBold: Typeface): Float {
        val subLbl = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor(COLOR_TEXT_SECONDARY)
            textSize = 9f
            typeface = tfBold
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText("ریز پرداخت‌ها و واریزی‌های ثبت‌شده:", CONTENT_RIGHT, curY + 10f, subLbl)

        val thH = 22f
        val startThY = curY + 16f
        val rect = RectF(CONTENT_LEFT, startThY, CONTENT_RIGHT, startThY + thH)
        val bgPaint = Paint().apply { color = Color.parseColor(COLOR_SECTION_HEADER_BG); style = Paint.Style.FILL }
        val borderPaint = Paint().apply { color = Color.parseColor(COLOR_CARD_BORDER); style = Paint.Style.STROKE; strokeWidth = 0.6f }
        canvas.drawRoundRect(rect, 4f, 4f, bgPaint)
        canvas.drawRoundRect(rect, 4f, 4f, borderPaint)

        val thText = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor(COLOR_TEXT_PRIMARY)
            textSize = 8.5f
            typeface = tfBold
            textAlign = Paint.Align.CENTER
        }

        val col1R = CONTENT_RIGHT
        val col1L = col1R - 35f
        val col2R = col1L
        val col2L = col2R - 95f
        val col3R = col2L
        val col3L = col3R - 120f
        val col4R = col3L
        val col4L = CONTENT_LEFT

        val base = startThY + 15f
        canvas.drawText("ردیف", (col1R + col1L) / 2f, base, thText)
        canvas.drawText("تاریخ واریز", (col2R + col2L) / 2f, base, thText)
        canvas.drawText("مبلغ (تومان)", (col3R + col3L) / 2f, base, thText)

        thText.textAlign = Paint.Align.RIGHT
        canvas.drawText("شرح و بابت", col4R - 10f, base, thText)

        return startThY + thH
    }

    private fun drawPaymentRow(
        canvas: Canvas,
        rowNum: Int,
        date: String,
        amount: Double,
        note: String,
        curY: Float,
        rowH: Float,
        tfBold: Typeface,
        tfRegular: Typeface
    ): Float {
        val rect = RectF(CONTENT_LEFT, curY, CONTENT_RIGHT, curY + rowH)
        if (rowNum % 2 == 1) {
            val altPaint = Paint().apply { color = Color.parseColor(COLOR_ALT_ROW_BG); style = Paint.Style.FILL }
            canvas.drawRect(rect, altPaint)
        }
        val linePaint = Paint().apply { color = Color.parseColor(COLOR_DIVIDER); strokeWidth = 0.5f }
        canvas.drawLine(CONTENT_LEFT, curY + rowH, CONTENT_RIGHT, curY + rowH, linePaint)

        val col1R = CONTENT_RIGHT
        val col1L = col1R - 35f
        val col2R = col1L
        val col2L = col2R - 95f
        val col3R = col2L
        val col3L = col3R - 120f
        val col4R = col3L
        val col4L = CONTENT_LEFT

        val numPaint = Paint().apply { isAntiAlias = true; color = Color.parseColor(COLOR_TEXT_MUTED); textSize = 8.5f; typeface = tfBold; textAlign = Paint.Align.CENTER }
        canvas.drawText(PersianUtils.faNum(rowNum), (col1R + col1L) / 2f, curY + 16f, numPaint)

        val datePaint = Paint().apply { isAntiAlias = true; color = Color.parseColor(COLOR_TEXT_SECONDARY); textSize = 8.5f; typeface = tfRegular; textAlign = Paint.Align.CENTER }
        canvas.drawText(PersianUtils.faNum(date), (col2R + col2L) / 2f, curY + 16f, datePaint)

        val amtPaint = Paint().apply { isAntiAlias = true; color = Color.parseColor(COLOR_SUCCESS); textSize = 9f; typeface = tfBold; textAlign = Paint.Align.CENTER }
        canvas.drawText(PersianUtils.formatCurrencyFa(amount), (col3R + col3L) / 2f, curY + 16f, amtPaint)

        val notePaint = Paint().apply { isAntiAlias = true; color = Color.parseColor(COLOR_TEXT_SECONDARY); textSize = 8.5f; typeface = tfRegular; textAlign = Paint.Align.RIGHT }
        val shortNote = if (note.length > 36) note.take(34) + "..." else note
        canvas.drawText(shortNote, col4R - 10f, curY + 16f, notePaint)

        return curY + rowH
    }

    // =========================================================================
    // SECTION 8: OFFICIAL SIGN-OFF & SEAL
    // =========================================================================

    private fun drawOfficialSignOff(
        canvas: Canvas,
        report: ProjectReportData,
        curY: Float,
        tfBold: Typeface,
        tfRegular: Typeface
    ): Float {
        var y = drawSectionHeader(canvas, "تاییدیه و اعتبار رسمی سند", curY, tfBold)

        val boxW = CONTENT_WIDTH
        val boxH = 68f
        val boxRect = RectF(CONTENT_LEFT, y, CONTENT_RIGHT, y + boxH)
        val bgPaint = Paint().apply { color = Color.parseColor(COLOR_CARD_BG); style = Paint.Style.FILL }
        val borderPaint = Paint().apply { color = Color.parseColor(COLOR_CARD_BORDER); style = Paint.Style.STROKE; strokeWidth = 0.8f }
        canvas.drawRoundRect(boxRect, 6f, 6f, bgPaint)
        canvas.drawRoundRect(boxRect, 6f, 6f, borderPaint)

        val notePaint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor(COLOR_TEXT_MUTED)
            textSize = 8f
            typeface = tfRegular
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("صحت کلیه اطلاعات مندرج در این گزارش اعم از تایم‌لاین، سوابق اصلاحات و صورت‌وضعیت مالی مورد تایید طرفین می‌باشد.", PAGE_WIDTH / 2f, y + 16f, notePaint)

        // Two signature boxes
        val sigW = (boxW - 32f) / 2f
        val sigH = 36f
        val sigTop = y + 24f

        val rightSigLeft = CONTENT_RIGHT - 12f - sigW
        val leftSigLeft = CONTENT_LEFT + 12f

        val sigBorder = Paint().apply {
            color = Color.parseColor(COLOR_DIVIDER)
            style = Paint.Style.STROKE
            strokeWidth = 0.6f
            pathEffect = DashPathEffect(floatArrayOf(3f, 3f), 0f)
        }
        canvas.drawRoundRect(RectF(rightSigLeft, sigTop, rightSigLeft + sigW, sigTop + sigH), 4f, 4f, sigBorder)
        canvas.drawRoundRect(RectF(leftSigLeft, sigTop, leftSigLeft + sigW, sigTop + sigH), 4f, 4f, sigBorder)

        val sigLabel = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor(COLOR_TEXT_SECONDARY)
            textSize = 8f
            typeface = tfBold
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("مهر و امضای تدوینگر پروژه", rightSigLeft + (sigW / 2f), sigTop + 14f, sigLabel)
        canvas.drawText("مهر و امضای مدیریت استودیو / کارفرما", leftSigLeft + (sigW / 2f), sigTop + 14f, sigLabel)

        return y + boxH + 10f
    }

    private fun drawEmptyBox(canvas: Canvas, text: String, curY: Float, tfRegular: Typeface): Float {
        val h = 28f
        val rect = RectF(CONTENT_LEFT, curY, CONTENT_RIGHT, curY + h)
        val bgPaint = Paint().apply { color = Color.parseColor(COLOR_CARD_BG); style = Paint.Style.FILL }
        val borderPaint = Paint().apply { color = Color.parseColor(COLOR_CARD_BORDER); style = Paint.Style.STROKE; strokeWidth = 0.6f }
        canvas.drawRoundRect(rect, 4f, 4f, bgPaint)
        canvas.drawRoundRect(rect, 4f, 4f, borderPaint)

        val tPaint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor(COLOR_TEXT_MUTED)
            textSize = 8.5f
            typeface = tfRegular
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(text, PAGE_WIDTH / 2f, curY + 18f, tPaint)
        return curY + h
    }

    // =========================================================================
    // TEXT RENDERING HELPERS WITH RTL COMPLEX TEXT LAYOUT
    // =========================================================================

    private fun drawRtlText(
        canvas: Canvas,
        text: String,
        xLeft: Float,
        yTop: Float,
        width: Float,
        paint: TextPaint,
        alignment: Layout.Alignment = Layout.Alignment.ALIGN_NORMAL
    ): Float {
        if (text.isEmpty()) return 0f
        val layout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StaticLayout.Builder
                .obtain(text, 0, text.length, paint, maxOf(1, width.toInt()))
                .setAlignment(alignment)
                .setTextDirection(TextDirectionHeuristics.RTL)
                .setLineSpacing(0f, 1.25f)
                .setIncludePad(false)
                .build()
        } else {
            @Suppress("DEPRECATION")
            StaticLayout(text, paint, maxOf(1, width.toInt()), alignment, 1.25f, 0f, false)
        }
        canvas.save()
        canvas.translate(xLeft, yTop)
        layout.draw(canvas)
        canvas.restore()
        return layout.height.toFloat()
    }

    private fun measureRtlTextHeight(
        text: String,
        width: Float,
        paint: TextPaint
    ): Float {
        if (text.isEmpty()) return 0f
        val layout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StaticLayout.Builder
                .obtain(text, 0, text.length, paint, maxOf(1, width.toInt()))
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setTextDirection(TextDirectionHeuristics.RTL)
                .setLineSpacing(0f, 1.25f)
                .setIncludePad(false)
                .build()
        } else {
            @Suppress("DEPRECATION")
            StaticLayout(text, paint, maxOf(1, width.toInt()), Layout.Alignment.ALIGN_NORMAL, 1.25f, 0f, false)
        }
        return layout.height.toFloat()
    }

    private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
}
