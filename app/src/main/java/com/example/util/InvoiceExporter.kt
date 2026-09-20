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
import com.example.data.entity.InvoiceData
import com.example.data.entity.InvoiceItem
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

object InvoiceExporter {

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
     * Renders a high-resolution PNG image (1200px width) of the invoice,
     * faithfully mirroring the InvoiceDocumentPaper design.
     */
    fun renderInvoiceBitmap(context: Context, invoice: InvoiceData): Bitmap {
        val (tfBold, tfRegular) = getVazirFonts(context)

        val width = 1200
        val margin = 50f
        val contentWidth = width - (margin * 2)

        // Calculate dynamic height based on item count and optional boxes
        val headerHeight = 220f
        val tableHeaderHeight = 60f
        val rowHeight = 52f
        val tableHeight = tableHeaderHeight + (invoice.items.size * rowHeight)
        val summaryHeight = 200f
        val statusBannerHeight = 70f
        val footerHeight = 100f

        val totalHeight = (headerHeight + tableHeight + summaryHeight + statusBannerHeight + footerHeight + (margin * 2)).toInt()

        val bitmap = Bitmap.createBitmap(width, totalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 1. Clean White Background
        val bgPaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, width.toFloat(), totalHeight.toFloat(), bgPaint)

        // 2. Subtle Outer Border
        val borderPaint = Paint().apply {
            color = Color.parseColor("#E2E8F0")
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        canvas.drawRect(20f, 20f, (width - 20).toFloat(), (totalHeight - 20).toFloat(), borderPaint)

        // Header Top Accent Line (CutterLog Brand Line)
        val topAccentPaint = Paint().apply {
            color = Color.parseColor("#4F46E5")
            style = Paint.Style.FILL
        }
        canvas.drawRect(20f, 20f, (width - 20).toFloat(), 28f, topAccentPaint)

        var currentY = margin + 35f

        // Paints for Typography
        val titlePaint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#4338CA")
            textSize = 38f
            typeface = tfBold
            textAlign = Paint.Align.RIGHT
        }

        val subtitlePaint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#475569")
            textSize = 21f
            typeface = tfBold
            textAlign = Paint.Align.RIGHT
        }

        val textDarkPaint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#0F172A")
            textSize = 19f
            typeface = tfBold
            textAlign = Paint.Align.RIGHT
        }

        // --- HEADER SECTION ---
        // Right side: Brand and Subtitle
        val brandName = invoice.brandTitle.ifBlank { "کاترلاگ" }
        canvas.drawText(brandName, width - margin, currentY, titlePaint)
        currentY += 34f
        canvas.drawText("صورتحساب خدمات تدوین و ادیت ویدئو", width - margin, currentY, subtitlePaint)
        currentY += 28f

        // Studio Recipient Chip
        val chipW = 380f
        val chipH = 46f
        val chipRect = RectF(width - margin - chipW, currentY, width - margin, currentY + chipH)
        val chipBg = Paint().apply {
            color = Color.parseColor("#F1F5F9")
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(chipRect, 10f, 10f, chipBg)
        canvas.drawRoundRect(chipRect, 10f, 10f, borderPaint)
        canvas.drawText("طرف حساب: ${PersianUtils.formatStudioName(invoice.studioName)}", width - margin - 20f, currentY + 31f, textDarkPaint)

        // Left side: Invoice Details Box
        val metaBoxWidth = 380f
        val metaBoxHeight = 125f
        val metaBoxRect = RectF(margin, margin + 10f, margin + metaBoxWidth, margin + 10f + metaBoxHeight)
        val metaBoxBg = Paint().apply {
            color = Color.parseColor("#F8FAFC")
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(metaBoxRect, 12f, 12f, metaBoxBg)
        canvas.drawRoundRect(metaBoxRect, 12f, 12f, borderPaint)

        val metaTextPaint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#64748B")
            textSize = 17f
            typeface = tfBold
            textAlign = Paint.Align.RIGHT
        }
        val metaValPaint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#0F172A")
            textSize = 17f
            typeface = tfBold
            textAlign = Paint.Align.LEFT
        }

        val metaRightX = margin + metaBoxWidth - 18f
        val metaLeftX = margin + 18f
        var metaY = margin + 44f

        canvas.drawText("شماره صورتحساب:", metaRightX, metaY, metaTextPaint)
        canvas.drawText(invoice.invoiceNumber, metaLeftX, metaY, metaValPaint)

        metaY += 34f
        canvas.drawText("تاریخ صدور:", metaRightX, metaY, metaTextPaint)
        metaValPaint.typeface = tfRegular
        canvas.drawText(PersianUtils.faNum(invoice.issueDate), metaLeftX, metaY, metaValPaint)

        metaY += 34f
        canvas.drawText("وضعیت حساب:", metaRightX, metaY, metaTextPaint)
        val isSettled = invoice.totalRemaining <= 0
        metaValPaint.color = if (isSettled) Color.parseColor("#16A34A") else Color.parseColor("#DC2626")
        metaValPaint.typeface = tfBold
        canvas.drawText(invoice.accountStatus, metaLeftX, metaY, metaValPaint)

        currentY = margin + 160f

        // Separator line
        val linePaint = Paint().apply {
            color = Color.parseColor("#E2E8F0")
            strokeWidth = 1.5f
        }
        canvas.drawLine(margin, currentY, width - margin, currentY, linePaint)
        currentY += 30f

        // --- TABLE SECTION ---
        // Column widths and X offsets (RTL: right to left)
        // Order: [مانده | پرداخت‌شده | مبلغ کل | تاریخ | شرح پروژه | ردیف]
        val colRemainingW = 160f
        val colPaidW = 160f
        val colPriceW = 160f
        val colDateW = 140f
        val colDescW = 410f
        val colIdxW = 70f

        val xRemaining = margin
        val xPaid = xRemaining + colRemainingW
        val xPrice = xPaid + colPaidW
        val xDate = xPrice + colPriceW
        val xDesc = xDate + colDateW
        val xIdx = xDesc + colDescW
        val xRightEdge = width - margin

        // Table Header Background
        val thRect = RectF(margin, currentY, width - margin, currentY + tableHeaderHeight)
        val thBg = Paint().apply {
            color = Color.parseColor("#F1F5F9")
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(thRect, 8f, 8f, thBg)
        canvas.drawRoundRect(thRect, 8f, 8f, borderPaint)

        val thTextPaint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#1E293B")
            textSize = 17f
            typeface = tfBold
            textAlign = Paint.Align.CENTER
        }

        val thY = currentY + 38f
        canvas.drawText("ردیف", xIdx + (colIdxW / 2), thY, thTextPaint)
        canvas.drawText("شرح پروژه", xDesc + (colDescW / 2), thY, thTextPaint)
        canvas.drawText("تاریخ", xDate + (colDateW / 2), thY, thTextPaint)
        canvas.drawText("مبلغ کل", xPrice + (colPriceW / 2), thY, thTextPaint)
        canvas.drawText("پرداخت‌شده", xPaid + (colPaidW / 2), thY, thTextPaint)
        canvas.drawText("مانده", xRemaining + (colRemainingW / 2), thY, thTextPaint)

        currentY += tableHeaderHeight

        // Data Rows
        val rowBgAlt = Paint().apply {
            color = Color.parseColor("#F8FAFC")
            style = Paint.Style.FILL
        }
        val cellTextPaint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#1E293B")
            textSize = 16f
            typeface = tfRegular
        }

        val rowBorderPaint = Paint().apply {
            color = Color.parseColor("#E2E8F0")
            strokeWidth = 1f
        }

        invoice.items.forEachIndexed { i, item ->
            val rowY = currentY + (i * rowHeight)
            val textBaseY = rowY + 33f

            if (i % 2 == 1) {
                canvas.drawRect(margin, rowY, width - margin, rowY + rowHeight, rowBgAlt)
            }
            canvas.drawLine(margin, rowY + rowHeight, width - margin, rowY + rowHeight, rowBorderPaint)

            // 1. Index
            cellTextPaint.textAlign = Paint.Align.CENTER
            cellTextPaint.typeface = tfBold
            cellTextPaint.color = Color.parseColor("#475569")
            canvas.drawText(PersianUtils.faNum(item.rowNumber), xIdx + (colIdxW / 2), textBaseY, cellTextPaint)

            // 2. Project Name
            cellTextPaint.textAlign = Paint.Align.RIGHT
            cellTextPaint.typeface = tfRegular
            cellTextPaint.color = Color.parseColor("#0F172A")
            var pName = item.projectName
            if (pName.length > 32) pName = pName.substring(0, 30) + "..."
            canvas.drawText(pName, xRightEdge - colIdxW - 16f, textBaseY, cellTextPaint)

            // 3. Date
            cellTextPaint.textAlign = Paint.Align.CENTER
            cellTextPaint.color = Color.parseColor("#64748B")
            canvas.drawText(PersianUtils.faNum(item.date), xDate + (colDateW / 2), textBaseY, cellTextPaint)

            // 4. Total Price
            cellTextPaint.color = Color.parseColor("#0F172A")
            canvas.drawText(PersianUtils.formatCurrencyFa(item.totalPrice), xPrice + (colPriceW / 2), textBaseY, cellTextPaint)

            // 5. Paid
            cellTextPaint.color = Color.parseColor("#16A34A")
            canvas.drawText(PersianUtils.formatCurrencyFa(item.paidAmount), xPaid + (colPaidW / 2), textBaseY, cellTextPaint)

            // 6. Remaining
            val remColor = if (item.remainingBalance > 0) Color.parseColor("#DC2626") else Color.parseColor("#16A34A")
            cellTextPaint.color = remColor
            cellTextPaint.typeface = if (item.remainingBalance > 0) tfBold else tfRegular
            canvas.drawText(PersianUtils.formatCurrencyFa(item.remainingBalance), xRemaining + (colRemainingW / 2), textBaseY, cellTextPaint)
            cellTextPaint.color = Color.parseColor("#1E293B")
        }

        currentY += (invoice.items.size * rowHeight) + 25f

        // --- SUMMARY & BANK INFO SECTION (SIDE BY SIDE ON WIDE IMAGE) ---
        val sumBoxWidth = 510f
        val sumBoxHeight = 175f

        // 1. Summary Box on the Left (x = margin)
        val sumBoxRect = RectF(margin, currentY, margin + sumBoxWidth, currentY + sumBoxHeight)
        val sumBg = Paint().apply {
            color = Color.parseColor("#F8FAFC")
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(sumBoxRect, 12f, 12f, sumBg)
        canvas.drawRoundRect(sumBoxRect, 12f, 12f, borderPaint)

        var sumY = currentY + 36f
        val sumRightX = margin + sumBoxWidth - 20f
        val sumLeftX = margin + 20f

        val sumLabelPaint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#475569")
            textSize = 17f
            typeface = tfBold
            textAlign = Paint.Align.RIGHT
        }
        val sumValuePaint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#0F172A")
            textSize = 18f
            typeface = tfBold
            textAlign = Paint.Align.LEFT
        }

        // Row 1: Total
        canvas.drawText("مبلغ کل پروژه‌ها:", sumRightX, sumY, sumLabelPaint)
        canvas.drawText(PersianUtils.formatCurrencyFa(invoice.totalAmount), sumLeftX, sumY, sumValuePaint)

        // Row 2: Paid
        sumY += 34f
        canvas.drawText("مجموع پرداختی‌ها:", sumRightX, sumY, sumLabelPaint)
        sumValuePaint.color = Color.parseColor("#16A34A")
        canvas.drawText(PersianUtils.formatCurrencyFa(invoice.totalPaid), sumLeftX, sumY, sumValuePaint)

        // Divider
        sumY += 16f
        canvas.drawLine(margin + 16f, sumY, margin + sumBoxWidth - 16f, sumY, linePaint)
        sumY += 30f

        // Row 3: Remaining Balance Highlighted Card
        val remBoxRect = RectF(margin + 12f, sumY - 22f, margin + sumBoxWidth - 12f, sumY + 26f)
        val remBoxBg = Paint().apply {
            color = if (invoice.totalRemaining > 0) Color.parseColor("#FFF1F2") else Color.parseColor("#ECFDF5")
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(remBoxRect, 8f, 8f, remBoxBg)

        sumLabelPaint.color = if (invoice.totalRemaining > 0) Color.parseColor("#9F1239") else Color.parseColor("#065F46")
        sumLabelPaint.textSize = 17.5f
        canvas.drawText("مانده کل قابل پرداخت:", sumRightX - 10f, sumY + 8f, sumLabelPaint)

        sumValuePaint.color = if (invoice.totalRemaining > 0) Color.parseColor("#DC2626") else Color.parseColor("#16A34A")
        sumValuePaint.textSize = 21f
        canvas.drawText(PersianUtils.formatCurrencyFa(invoice.totalRemaining), sumLeftX + 10f, sumY + 8f, sumValuePaint)

        // 2. Bank Account Info on the Right (if present)
        if (!invoice.bankCard.isNullOrBlank()) {
            val bankBoxWidth = 530f
            val bankBoxRect = RectF(width - margin - bankBoxWidth, currentY, width - margin, currentY + sumBoxHeight)
            val bankBg = Paint().apply {
                color = Color.parseColor("#EEF2FF")
                style = Paint.Style.FILL
            }
            canvas.drawRoundRect(bankBoxRect, 12f, 12f, bankBg)

            val bankBorder = Paint().apply {
                color = Color.parseColor("#C7D2FE")
                style = Paint.Style.STROKE
                strokeWidth = 1.5f
            }
            canvas.drawRoundRect(bankBoxRect, 12f, 12f, bankBorder)

            val bankLabelPaint = Paint().apply {
                isAntiAlias = true
                color = Color.parseColor("#3730A3")
                textSize = 17.5f
                typeface = tfBold
                textAlign = Paint.Align.RIGHT
            }
            val bankValPaint = Paint().apply {
                isAntiAlias = true
                color = Color.parseColor("#1E1B4B")
                textSize = 20f
                typeface = tfBold
                textAlign = Paint.Align.RIGHT
            }

            val bRightX = width - margin - 22f
            canvas.drawText("اطلاعات واریز به حساب:", bRightX, currentY + 40f, bankLabelPaint)
            val cardInfo = "شماره کارت: ${PersianUtils.faNum(invoice.bankCard)}"
            canvas.drawText(cardInfo, bRightX, currentY + 82f, bankValPaint)

            if (!invoice.bankOwner.isNullOrBlank()) {
                val ownerPaint = Paint().apply {
                    isAntiAlias = true
                    color = Color.parseColor("#4338CA")
                    textSize = 17f
                    typeface = tfRegular
                    textAlign = Paint.Align.RIGHT
                }
                canvas.drawText("صاحب حساب: ${invoice.bankOwner}", bRightX, currentY + 120f, ownerPaint)
            }
        }

        currentY += sumBoxHeight + 20f

        // --- ACCOUNT STATUS BANNER ---
        val statusBannerRect = RectF(margin, currentY, width - margin, currentY + 54f)
        val statusBg = Paint().apply {
            color = if (isSettled) Color.parseColor("#ECFDF5") else Color.parseColor("#FFF1F2")
            style = Paint.Style.FILL
        }
        val statusBorder = Paint().apply {
            color = if (isSettled) Color.parseColor("#A7F3D0") else Color.parseColor("#FECDD3")
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
        }
        canvas.drawRoundRect(statusBannerRect, 10f, 10f, statusBg)
        canvas.drawRoundRect(statusBannerRect, 10f, 10f, statusBorder)

        val statusTextPaint = Paint().apply {
            isAntiAlias = true
            color = if (isSettled) Color.parseColor("#065F46") else Color.parseColor("#9F1239")
            textSize = 16.5f
            typeface = tfBold
            textAlign = Paint.Align.RIGHT
        }
        val statusMsg = if (isSettled) {
            "وضعیت حساب: تسویه شده (تمامی تعهدات مالی این صورتحساب پرداخت شده است)."
        } else {
            "وضعیت حساب: تسویه نشده — دارای مانده بدهی به مبلغ ${PersianUtils.formatCurrencyFa(invoice.totalRemaining)}."
        }
        canvas.drawText(statusMsg, width - margin - 20f, currentY + 34f, statusTextPaint)

        currentY += 74f

        // --- FOOTER NOTE & OFFICIAL SYSTEM SIGNATURE ---
        if (!invoice.footerNote.isNullOrBlank()) {
            val footerPaint = Paint().apply {
                isAntiAlias = true
                color = Color.parseColor("#475569")
                textSize = 16f
                typeface = tfRegular
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("یادداشت: ${invoice.footerNote}", width / 2f, currentY, footerPaint)
            currentY += 30f
        }

        canvas.drawLine(margin, currentY, width - margin, currentY, linePaint)
        currentY += 24f

        val sysTextPaint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#94A3B8")
            textSize = 14.5f
            typeface = tfRegular
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText("سامانه مدیریت و پایش تدوین فیلم کاترلاگ (CutterLog)", width - margin, currentY, sysTextPaint)

        sysTextPaint.textAlign = Paint.Align.LEFT
        canvas.drawText("تاریخ سند: ${PersianUtils.faNum(invoice.issueDate)}", margin, currentY, sysTextPaint)

        return bitmap
    }

    /**
     * Saves invoice image to device storage and returns Uri.
     */
    fun saveInvoiceAsPng(context: Context, invoice: InvoiceData): Uri? {
        val bitmap = renderInvoiceBitmap(context, invoice)
        val filename = "Invoice_${invoice.studioName}_${invoice.invoiceNumber}.png"

        return try {
            var uri: Uri? = null
            var outputStream: OutputStream? = null

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/CutterLog")
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
                val appDir = File(picturesDir, "CutterLog")
                if (!appDir.exists()) appDir.mkdirs()
                val file = File(appDir, filename)
                outputStream = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                uri = Uri.fromFile(file)
            }

            outputStream?.flush()
            outputStream?.close()

            Toast.makeText(context, "تصویر صورتحساب با موفقیت در گالری ذخیره شد", Toast.LENGTH_LONG).show()
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
                Toast.makeText(context, "تصویر صورتحساب ذخیره شد", Toast.LENGTH_SHORT).show()
                fallbackUri
            } catch (ex: Exception) {
                ex.printStackTrace()
                Toast.makeText(context, "خطا در ذخیره تصویر: ${e.message}", Toast.LENGTH_SHORT).show()
                null
            }
        }
    }

    /**
     * Shares invoice image via Android standard share sheet.
     */
    fun shareInvoiceAsPng(context: Context, invoice: InvoiceData) {
        try {
            val bitmap = renderInvoiceBitmap(context, invoice)
            val file = File(context.cacheDir, "Invoice_${invoice.invoiceNumber}.png")
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
                putExtra(Intent.EXTRA_SUBJECT, "صورتحساب استودیو ${invoice.studioName}")
                putExtra(Intent.EXTRA_TEXT, "صورتحساب خدمات تدوین استودیو ${invoice.studioName} (شماره: ${invoice.invoiceNumber})")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "ارسال صورتحساب استودیو"))
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "خطا در اشتراک‌گذاری: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Generates a multi-page PDF document for the invoice (A4 standard: 595 x 842 points)
     * Handles pagination cleanly so no row or summary is ever cut off.
     */
    fun generateInvoicePdf(context: Context, invoice: InvoiceData): File? {
        val (tfBold, tfRegular) = getVazirFonts(context)

        return try {
            val pdfDoc = PdfDocument()
            val pageWidth = 595
            val pageHeight = 842
            val margin = 30f

            val itemsPerPage = 14
            val totalItems = invoice.items.size
            val totalPages = if (totalItems == 0) 1 else ((totalItems + itemsPerPage - 1) / itemsPerPage)

            val bgPaint = Paint().apply { color = Color.WHITE; style = Paint.Style.FILL }
            val borderPaint = Paint().apply { color = Color.parseColor("#E2E8F0"); style = Paint.Style.STROKE; strokeWidth = 1f }
            val topAccentPaint = Paint().apply { color = Color.parseColor("#6366F1"); style = Paint.Style.FILL }

            val titlePaint = Paint().apply { isAntiAlias = true; color = Color.parseColor("#4F46E5"); textSize = 20f; typeface = tfBold; textAlign = Paint.Align.RIGHT }
            val subPaint = Paint().apply { isAntiAlias = true; color = Color.parseColor("#334155"); textSize = 12f; typeface = tfBold; textAlign = Paint.Align.RIGHT }
            val textDark = Paint().apply { isAntiAlias = true; color = Color.parseColor("#0F172A"); textSize = 10f; typeface = tfRegular; textAlign = Paint.Align.RIGHT }

            for (pageIndex in 0 until totalPages) {
                val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageIndex + 1).create()
                val page = pdfDoc.startPage(pageInfo)
                val canvas = page.canvas

                // Background
                canvas.drawRect(0f, 0f, pageWidth.toFloat(), pageHeight.toFloat(), bgPaint)
                canvas.drawRect(15f, 15f, (pageWidth - 15).toFloat(), (pageHeight - 15).toFloat(), borderPaint)
                canvas.drawRect(15f, 15f, (pageWidth - 15).toFloat(), 20f, topAccentPaint)

                var curY = margin + 15f

                // Page 1 Header
                if (pageIndex == 0) {
                    val brandName = invoice.brandTitle.ifBlank { "کاترلاگ" }
                    canvas.drawText(brandName, pageWidth - margin, curY, titlePaint)
                    curY += 17f
                    canvas.drawText("صورتحساب خدمات تدوین و ادیت ویدئو", pageWidth - margin, curY, subPaint)
                    curY += 15f

                    // Studio Chip
                    val chipW = 200f
                    val chipH = 22f
                    val chipRect = RectF(pageWidth - margin - chipW, curY, pageWidth - margin, curY + chipH)
                    val chipBg = Paint().apply { color = Color.parseColor("#F1F5F9"); style = Paint.Style.FILL }
                    canvas.drawRoundRect(chipRect, 4f, 4f, chipBg)
                    canvas.drawRoundRect(chipRect, 4f, 4f, borderPaint)
                    val chipText = Paint().apply { isAntiAlias = true; color = Color.parseColor("#0F172A"); textSize = 9f; typeface = tfBold; textAlign = Paint.Align.RIGHT }
                    canvas.drawText("طرف حساب: ${PersianUtils.formatStudioName(invoice.studioName)}", pageWidth - margin - 8f, curY + 15f, chipText)

                    // Meta box
                    val metaW = 180f
                    val metaH = 58f
                    val metaRect = RectF(margin, margin + 5f, margin + metaW, margin + 5f + metaH)
                    val metaBg = Paint().apply { color = Color.parseColor("#F8FAFC"); style = Paint.Style.FILL }
                    canvas.drawRoundRect(metaRect, 6f, 6f, metaBg)
                    canvas.drawRoundRect(metaRect, 6f, 6f, borderPaint)

                    val mLabel = Paint().apply { isAntiAlias = true; color = Color.parseColor("#64748B"); textSize = 8.5f; typeface = tfBold; textAlign = Paint.Align.RIGHT }
                    val mVal = Paint().apply { isAntiAlias = true; color = Color.parseColor("#0F172A"); textSize = 8.5f; typeface = tfBold; textAlign = Paint.Align.LEFT }

                    canvas.drawText("شماره صورتحساب:", margin + metaW - 8f, margin + 19f, mLabel)
                    canvas.drawText(invoice.invoiceNumber, margin + 8f, margin + 19f, mVal)

                    canvas.drawText("تاریخ صدور:", margin + metaW - 8f, margin + 34f, mLabel)
                    mVal.typeface = tfRegular
                    canvas.drawText(PersianUtils.faNum(invoice.issueDate), margin + 8f, margin + 34f, mVal)

                    canvas.drawText("وضعیت حساب:", margin + metaW - 8f, margin + 49f, mLabel)
                    mVal.color = if (invoice.totalRemaining <= 0) Color.parseColor("#16A34A") else Color.parseColor("#DC2626")
                    mVal.typeface = tfBold
                    canvas.drawText(invoice.accountStatus, margin + 8f, margin + 49f, mVal)

                    curY = margin + 78f
                } else {
                    // Subsequent page header
                    canvas.drawText("کاترلاگ - صورتحساب ${PersianUtils.formatStudioName(invoice.studioName)} (ادامه)", pageWidth - margin, curY, subPaint)
                    curY += 25f
                }

                // Table Header
                val thHeight = 28f
                val thRect = RectF(margin, curY, pageWidth - margin, curY + thHeight)
                val thBg = Paint().apply { color = Color.parseColor("#F1F5F9"); style = Paint.Style.FILL }
                canvas.drawRoundRect(thRect, 4f, 4f, thBg)
                canvas.drawRoundRect(thRect, 4f, 4f, borderPaint)

                val colRemW = 75f
                val colPdW = 75f
                val colPrW = 75f
                val colDtW = 65f
                val colDsW = 205f
                val colIdW = 40f

                val xRem = margin
                val xPd = xRem + colRemW
                val xPr = xPd + colPdW
                val xDt = xPr + colPrW
                val xDs = xDt + colDtW
                val xId = xDs + colDsW

                val thText = Paint().apply { isAntiAlias = true; color = Color.parseColor("#1E293B"); textSize = 9f; typeface = tfBold; textAlign = Paint.Align.CENTER }
                val thY = curY + 18f
                canvas.drawText("ردیف", xId + (colIdW / 2), thY, thText)
                canvas.drawText("شرح پروژه", xDs + (colDsW / 2), thY, thText)
                canvas.drawText("تاریخ", xDt + (colDtW / 2), thY, thText)
                canvas.drawText("مبلغ کل", xPr + (colPrW / 2), thY, thText)
                canvas.drawText("پرداخت‌شده", xPd + (colPdW / 2), thY, thText)
                canvas.drawText("مانده", xRem + (colRemW / 2), thY, thText)

                curY += thHeight

                // Page Rows
                val startIdx = pageIndex * itemsPerPage
                val endIdx = minOf(startIdx + itemsPerPage, totalItems)
                val rowH = 24f
                val rowText = Paint().apply { isAntiAlias = true; color = Color.parseColor("#1E293B"); textSize = 8.5f; typeface = tfRegular }
                val altBg = Paint().apply { color = Color.parseColor("#F8FAFC"); style = Paint.Style.FILL }

                for (i in startIdx until endIdx) {
                    val item = invoice.items[i]
                    val rY = curY
                    val tBase = rY + 16f

                    if ((i - startIdx) % 2 == 1) {
                        canvas.drawRect(margin, rY, pageWidth - margin, rY + rowH, altBg)
                    }
                    canvas.drawLine(margin, rY + rowH, pageWidth - margin, rY + rowH, borderPaint)

                    rowText.textAlign = Paint.Align.CENTER
                    rowText.typeface = tfBold
                    canvas.drawText(PersianUtils.faNum(item.rowNumber), xId + (colIdW / 2), tBase, rowText)

                    rowText.textAlign = Paint.Align.RIGHT
                    rowText.typeface = tfRegular
                    var name = item.projectName
                    if (name.length > 30) name = name.substring(0, 28) + "..."
                    canvas.drawText(name, pageWidth - margin - colIdW - 8f, tBase, rowText)

                    rowText.textAlign = Paint.Align.CENTER
                    canvas.drawText(PersianUtils.faNum(item.date), xDt + (colDtW / 2), tBase, rowText)
                    canvas.drawText(PersianUtils.formatCurrencyFa(item.totalPrice), xPr + (colPrW / 2), tBase, rowText)
                    canvas.drawText(PersianUtils.formatCurrencyFa(item.paidAmount), xPd + (colPdW / 2), tBase, rowText)

                    rowText.color = if (item.remainingBalance > 0) Color.parseColor("#DC2626") else Color.parseColor("#16A34A")
                    canvas.drawText(PersianUtils.formatCurrencyFa(item.remainingBalance), xRem + (colRemW / 2), tBase, rowText)
                    rowText.color = Color.parseColor("#1E293B")

                    curY += rowH
                }

                // If on Last Page: Draw Summary Box & Bank Info
                if (pageIndex == totalPages - 1) {
                    curY += 15f

                    val sW = 230f
                    val sH = 85f
                    val sRect = RectF(margin, curY, margin + sW, curY + sH)
                    val sBg = Paint().apply { color = Color.parseColor("#F8FAFC"); style = Paint.Style.FILL }
                    canvas.drawRoundRect(sRect, 6f, 6f, sBg)
                    canvas.drawRoundRect(sRect, 6f, 6f, borderPaint)

                    val sLabel = Paint().apply { isAntiAlias = true; color = Color.parseColor("#475569"); textSize = 9f; typeface = tfBold; textAlign = Paint.Align.RIGHT }
                    val sVal = Paint().apply { isAntiAlias = true; color = Color.parseColor("#0F172A"); textSize = 9.5f; typeface = tfBold; textAlign = Paint.Align.LEFT }

                    val sRight = margin + sW - 10f
                    val sLeft = margin + 10f
                    var sy = curY + 20f

                    canvas.drawText("مبلغ کل پروژه‌ها:", sRight, sy, sLabel)
                    canvas.drawText(PersianUtils.formatCurrencyFa(invoice.totalAmount), sLeft, sy, sVal)

                    sy += 18f
                    canvas.drawText("مجموع پرداختی‌ها:", sRight, sy, sLabel)
                    sVal.color = Color.parseColor("#16A34A")
                    canvas.drawText(PersianUtils.formatCurrencyFa(invoice.totalPaid), sLeft, sy, sVal)

                    sy += 12f
                    canvas.drawLine(margin + 8f, sy, margin + sW - 8f, sy, borderPaint)
                    sy += 16f

                    sLabel.color = Color.parseColor("#0F172A")
                    canvas.drawText("مانده کل قابل پرداخت:", sRight, sy, sLabel)
                    sVal.color = if (invoice.totalRemaining > 0) Color.parseColor("#DC2626") else Color.parseColor("#16A34A")
                    sVal.textSize = 10.5f
                    canvas.drawText(PersianUtils.formatCurrencyFa(invoice.totalRemaining), sLeft, sy, sVal)

                    curY += sH + 10f

                    // Status Banner in PDF
                    val statH = 26f
                    val statRect = RectF(margin, curY, pageWidth - margin, curY + statH)
                    val isSettled = invoice.totalRemaining <= 0
                    val statBg = Paint().apply {
                        color = if (isSettled) Color.parseColor("#ECFDF5") else Color.parseColor("#FFF1F2")
                        style = Paint.Style.FILL
                    }
                    val statBorder = Paint().apply {
                        color = if (isSettled) Color.parseColor("#A7F3D0") else Color.parseColor("#FECDD3")
                        style = Paint.Style.STROKE
                        strokeWidth = 1f
                    }
                    canvas.drawRoundRect(statRect, 4f, 4f, statBg)
                    canvas.drawRoundRect(statRect, 4f, 4f, statBorder)

                    val statText = Paint().apply {
                        isAntiAlias = true
                        color = if (isSettled) Color.parseColor("#065F46") else Color.parseColor("#9F1239")
                        textSize = 8.5f
                        typeface = tfBold
                        textAlign = Paint.Align.RIGHT
                    }
                    val msg = if (isSettled) {
                        "وضعیت حساب: تسویه شده (تمامی تعهدات مالی این صورتحساب پرداخت شده است)."
                    } else {
                        "وضعیت حساب: تسویه نشده — دارای مانده بدهی به مبلغ ${PersianUtils.formatCurrencyFa(invoice.totalRemaining)}."
                    }
                    canvas.drawText(msg, pageWidth - margin - 8f, curY + 16f, statText)
                    curY += statH + 10f

                    // Bank Info Box
                    if (!invoice.bankCard.isNullOrBlank()) {
                        val bH = 42f
                        val bRect = RectF(margin, curY, pageWidth - margin, curY + bH)
                        val bBg = Paint().apply { color = Color.parseColor("#EEF2FF"); style = Paint.Style.FILL }
                        canvas.drawRoundRect(bRect, 5f, 5f, bBg)
                        val bBorder = Paint().apply { color = Color.parseColor("#C7D2FE"); style = Paint.Style.STROKE; strokeWidth = 1f }
                        canvas.drawRoundRect(bRect, 5f, 5f, bBorder)

                        val bText = Paint().apply { isAntiAlias = true; color = Color.parseColor("#3730A3"); textSize = 8.5f; typeface = tfBold; textAlign = Paint.Align.RIGHT }
                        canvas.drawText("اطلاعات واریز به حساب:", pageWidth - margin - 10f, curY + 15f, bText)
                        val bInfo = "شماره کارت: ${PersianUtils.faNum(invoice.bankCard)}" + if (!invoice.bankOwner.isNullOrBlank()) " • بنام: ${invoice.bankOwner}" else ""
                        bText.color = Color.parseColor("#1E1B4B")
                        bText.textSize = 9.5f
                        canvas.drawText(bInfo, pageWidth - margin - 10f, curY + 30f, bText)

                        curY += bH + 8f
                    }

                    if (!invoice.footerNote.isNullOrBlank()) {
                        val footText = Paint().apply { isAntiAlias = true; color = Color.parseColor("#64748B"); textSize = 8f; typeface = tfRegular; textAlign = Paint.Align.CENTER }
                        canvas.drawText("یادداشت: ${invoice.footerNote}", pageWidth / 2f, curY + 10f, footText)
                        curY += 16f
                    }

                    // Official Brand Signature line
                    val sysLine = Paint().apply { color = Color.parseColor("#E2E8F0"); strokeWidth = 0.8f }
                    canvas.drawLine(margin, curY + 4f, pageWidth - margin, curY + 4f, sysLine)
                    val sysText = Paint().apply { isAntiAlias = true; color = Color.parseColor("#94A3B8"); textSize = 7.5f; typeface = tfRegular; textAlign = Paint.Align.RIGHT }
                    canvas.drawText("سامانه مدیریت و پایش تدوین فیلم کاترلاگ (CutterLog)", pageWidth - margin, curY + 16f, sysText)
                    sysText.textAlign = Paint.Align.LEFT
                    canvas.drawText("تاریخ سند: ${PersianUtils.faNum(invoice.issueDate)}", margin, curY + 16f, sysText)
                }

                // Page Number footer
                val pageNumPaint = Paint().apply { isAntiAlias = true; color = Color.parseColor("#94A3B8"); textSize = 8f; typeface = tfRegular; textAlign = Paint.Align.CENTER }
                canvas.drawText("صفحه ${PersianUtils.faNum(pageIndex + 1)} از ${PersianUtils.faNum(totalPages)}", pageWidth / 2f, pageHeight - 20f, pageNumPaint)

                pdfDoc.finishPage(page)
            }

            val file = File(context.cacheDir, "Invoice_${invoice.studioName}_${invoice.invoiceNumber}.pdf")
            val outputStream = FileOutputStream(file)
            pdfDoc.writeTo(outputStream)
            pdfDoc.close()
            outputStream.flush()
            outputStream.close()

            file
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "خطا در تولید PDF: ${e.message}", Toast.LENGTH_SHORT).show()
            null
        }
    }

    /**
     * Shares invoice PDF via Android standard share sheet.
     */
    fun shareInvoiceAsPdf(context: Context, invoice: InvoiceData) {
        val file = generateInvoicePdf(context, invoice) ?: return

        try {
            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_SUBJECT, "فایل PDF صورتحساب استودیو ${invoice.studioName}")
                putExtra(Intent.EXTRA_TEXT, "فایل PDF صورتحساب خدمات تدوین استودیو ${invoice.studioName} (شماره: ${invoice.invoiceNumber})")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "ارسال فایل PDF صورتحساب"))
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "خطا در اشتراک‌گذاری PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
