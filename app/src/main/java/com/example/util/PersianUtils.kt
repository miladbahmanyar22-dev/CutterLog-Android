package com.example.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Calendar
import java.util.Locale

object PersianUtils {

    fun faNum(input: Any?): String {
        if (input == null) return ""
        val str = input.toString()
        val englishDigits = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
        val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
        var result = str
        for (i in 0..9) {
            result = result.replace(englishDigits[i], persianDigits[i])
        }
        return result
    }

    fun convertFaToEnNum(input: String): String {
        val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
        val englishDigits = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
        var result = input
        for (i in 0..9) {
            result = result.replace(persianDigits[i], englishDigits[i])
        }
        return result
    }

    fun formatCurrencyFa(amount: Double): String {
        val symbols = DecimalFormatSymbols(Locale.US).apply {
            groupingSeparator = '٬'
        }
        val df = DecimalFormat("#,###", symbols)
        val formatted = df.format(amount.toLong())
        return "${faNum(formatted)} تومان"
    }

    /**
     * Format a number string or Long with 3-digit Persian grouping (e.g. 1000000 -> ۱٬۰۰۰٬۰۰۰)
     */
    fun formatGroupedFa(value: Any?): String {
        if (value == null) return ""
        val num = when (value) {
            is Number -> value.toLong()
            else -> convertFaToEnNum(value.toString()).filter { it.isDigit() }.toLongOrNull() ?: return faNum(value)
        }
        val symbols = DecimalFormatSymbols(Locale.US).apply {
            groupingSeparator = '٬'
        }
        val df = DecimalFormat("#,###", symbols)
        return faNum(df.format(num))
    }

    /**
     * Convert an integer / currency amount (in Toman) to Persian words.
     * e.g. 1500000 -> یک میلیون و پانصد هزار تومان
     */
    fun numberToWordsFa(amount: Long, unit: String = "تومان"): String {
        if (amount == 0L) return "صفر $unit".trim()
        if (amount < 0L) return "منفی " + numberToWordsFa(-amount, unit)

        val units = arrayOf("", "یک", "دو", "سه", "چهار", "پنج", "شش", "هفت", "هشت", "نه")
        val teens = arrayOf("ده", "یازده", "دوازده", "سیزده", "چهارده", "پانزده", "شانزده", "هفده", "هجده", "نوزده")
        val tens = arrayOf("", "", "بیست", "سی", "چهل", "پنجاه", "شصت", "هفتاد", "هشتاد", "نود")
        val hundreds = arrayOf("", "صد", "دویست", "سیصد", "چهارصد", "پانصد", "ششصد", "هفتصد", "هشتصد", "نهصد")
        val thousandsScales = arrayOf("", "هزار", "میلیون", "میلیارد", "تریلیون")

        fun convertThreeDigits(n: Int): String {
            val parts = mutableListOf<String>()
            val c = n / 100
            val r = n % 100
            val t = r / 10
            val u = r % 10

            if (c > 0) {
                parts.add(hundreds[c])
            }

            if (r in 10..19) {
                parts.add(teens[r - 10])
            } else {
                if (t > 0) parts.add(tens[t])
                if (u > 0) parts.add(units[u])
            }

            return parts.joinToString(" و ")
        }

        var temp = amount
        val groups = mutableListOf<Int>()
        while (temp > 0L) {
            groups.add((temp % 1000L).toInt())
            temp /= 1000L
        }

        val wordParts = mutableListOf<String>()
        for (i in groups.indices.reversed()) {
            val gVal = groups[i]
            if (gVal != 0) {
                val groupWord = convertThreeDigits(gVal)
                val scale = thousandsScales[i]
                if (scale.isNotEmpty()) {
                    wordParts.add("$groupWord $scale")
                } else {
                    wordParts.add(groupWord)
                }
            }
        }

        val resultWords = wordParts.joinToString(" و ")
        return if (unit.isNotBlank()) "$resultWords $unit" else resultWords
    }

    fun numberToWordsFa(amount: Double, unit: String = "تومان"): String {
        return numberToWordsFa(amount.toLong(), unit)
    }

    fun formatPriceShort(amount: Double): String {
        return if (amount >= 1_000_000) {
            val millions = amount / 1_000_000
            val df = DecimalFormat("#.#")
            "${faNum(df.format(millions))} م"
        } else if (amount >= 1_000) {
            val thousands = amount / 1_000
            val df = DecimalFormat("#")
            "${faNum(df.format(thousands))} هزار"
        } else {
            "${faNum(amount.toInt())}"
        }
    }

    // Modern Jalali Date Helper
    fun getCurrentJalaliDate(): String {
        val cal = Calendar.getInstance()
        val gYear = cal.get(Calendar.YEAR)
        val gMonth = cal.get(Calendar.MONTH) + 1
        val gDay = cal.get(Calendar.DAY_OF_MONTH)
        val (jYear, jMonth, jDay) = gregorianToJalali(gYear, gMonth, gDay)
        return String.format(Locale.US, "%04d/%02d/%02d", jYear, jMonth, jDay)
    }

    fun addDaysToJalali(jalaliDateStr: String, daysToAdd: Int): String {
        val parts = jalaliDateStr.split("/").mapNotNull { convertFaToEnNum(it).toIntOrNull() }
        if (parts.size != 3) return getCurrentJalaliDate()
        var (jY, jM, jD) = parts
        
        // Approximate day addition
        jD += daysToAdd
        while (jD > 30) {
            jD -= 30
            jM += 1
            if (jM > 12) {
                jM = 1
                jY += 1
            }
        }
        return String.format(Locale.US, "%04d/%02d/%02d", jY, jM, jD)
    }

    fun getDaysElapsed(jalaliDateStr: String): Int {
        val currentStr = getCurrentJalaliDate()
        val parts1 = jalaliDateStr.split("/").mapNotNull { convertFaToEnNum(it).toIntOrNull() }
        val parts2 = currentStr.split("/").mapNotNull { convertFaToEnNum(it).toIntOrNull() }
        if (parts1.size != 3 || parts2.size != 3) return 0

        val days1 = parts1[0] * 365 + parts1[1] * 30 + parts1[2]
        val days2 = parts2[0] * 365 + parts2[1] * 30 + parts2[2]
        val diff = days2 - days1
        return if (diff < 0) 0 else diff
    }

    private fun gregorianToJalali(gy: Int, gm: Int, gd: Int): Triple<Int, Int, Int> {
        val gDaysInMonth = intArrayOf(0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        var gy2 = if (gm > 2) gy + 1 else gy
        var days = 355666 + (365 * gy) + ((gy2 + 3) / 4) - ((gy2 + 99) / 100) + ((gy2 + 399) / 400) + gd
        for (i in 0 until gm) {
            days += gDaysInMonth[i]
        }
        if (gm > 2 && (gy % 4 == 0 && gy % 100 != 0 || gy % 400 == 0)) {
            days++
        }
        var jy = -1595 + (33 * (days / 12053))
        days %= 12053
        jy += 4 * (days / 1461)
        days %= 1461
        if (days > 365) {
            jy += ((days - 1) / 365)
            days = (days - 1) % 365
        }
        val jm: Int
        val jd: Int
        if (days < 186) {
            jm = 1 + (days / 31)
            jd = 1 + (days % 31)
        } else {
            jm = 7 + ((days - 186) / 30)
            jd = 1 + ((days - 186) % 30)
        }
        return Triple(jy, jm, jd)
    }

    fun formatSecondsToHMS(totalSecs: Long): String {
        val h = totalSecs / 3600
        val m = (totalSecs % 3600) / 60
        val s = totalSecs % 60
        return String.format(Locale.US, "%02d:%02d:%02d", h, m, s)
    }

    /**
     * Cleans and strips repetitive 'استودیو' or 'آتلیه' prefixes from a studio name.
     * e.g.:
     *  "وزیر" -> "وزیر"
     *  "استودیو وزیر" -> "وزیر"
     *  "استودیو  استودیو وزیر" -> "وزیر"
     *  "آتلیه تصویرسازان" -> "تصویرسازان"
     */
    fun cleanStudioBaseName(rawName: String?): String {
        if (rawName == null) return ""
        // Normalize whitespace and remove non-breaking spaces / zero-width spaces
        var cleaned = rawName.replace('\u00A0', ' ')
            .replace('\u200C', ' ')
            .trim()
        
        // Repeatedly strip leading 'استودیو' or 'آتلیه'
        var changed = true
        while (changed && cleaned.isNotBlank()) {
            changed = false
            val prefixes = listOf("استودیو", "آتلیه")
            for (p in prefixes) {
                if (cleaned.startsWith(p, ignoreCase = true)) {
                    val after = cleaned.substring(p.length).trim()
                    // Only strip if there is actual remaining text or if it was standalone
                    if (after.isNotBlank()) {
                        cleaned = after
                        changed = true
                        break
                    }
                }
            }
        }
        return cleaned.ifBlank { rawName.trim() }
    }

    /**
     * Formats a studio name cleanly with a single 'استودیو' prefix:
     * - "وزیر" -> "استودیو وزیر"
     * - "استودیو وزیر" -> "استودیو وزیر"
     * - "استودیو  وزیر" -> "استودیو وزیر"
     * - "استودیو استودیو وزیر" -> "استودیو وزیر"
     * - "آتلیه تصویرسازان" -> "استودیو تصویرسازان"
     */
    fun formatStudioName(rawName: String?): String {
        if (rawName.isNullOrBlank()) return "استودیو عمومی"
        val base = cleanStudioBaseName(rawName)
        return "استودیو $base"
    }
}
