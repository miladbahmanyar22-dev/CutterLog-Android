package com.example.ui.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import com.example.util.PersianUtils

/**
 * Visual transformation that formats numbers into 3-digit grouped Persian digits (e.g. 1000000 -> ۱,۰۰۰,۰۰۰).
 * 
 * Preserves raw English digits in the state while providing seamless cursor mapping
 * during insertion, deletion, and cursor movement without jumping or cursor bugs.
 */
class PersianNumberVisualTransformation(
    private val separator: Char = '٬'
) : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val originalText = text.text
        if (originalText.isEmpty()) {
            return TransformedText(text, OffsetMapping.Identity)
        }

        // Clean out any non-digit characters to be robust
        val digitsOnly = originalText.filter { it.isDigit() }
        if (digitsOnly.isEmpty()) {
            return TransformedText(text, OffsetMapping.Identity)
        }

        // Build formatted string with Persian digits and 3-digit separators
        val sb = StringBuilder()
        val len = originalText.length
        
        // Map of original index to transformed index
        // transformedToOriginal and originalToTransformed arrays
        val origToTrans = IntArray(len + 1)
        
        for (i in 0 until len) {
            origToTrans[i] = sb.length
            val c = originalText[i]
            val persianChar = when (c) {
                '0' -> '۰'
                '1' -> '۱'
                '2' -> '۲'
                '3' -> '۳'
                '4' -> '۴'
                '5' -> '۵'
                '6' -> '۶'
                '7' -> '۷'
                '8' -> '۸'
                '9' -> '۹'
                else -> c
            }
            sb.append(persianChar)
            
            // Add separator if needed: distance from end is positive multiple of 3
            val remainingDigits = len - 1 - i
            if (remainingDigits > 0 && remainingDigits % 3 == 0) {
                sb.append(separator)
            }
        }
        origToTrans[len] = sb.length

        val transformedString = sb.toString()
        val transLen = transformedString.length
        
        // Build reverse mapping array
        val transToOrig = IntArray(transLen + 1)
        var origIndex = 0
        for (t in 0 until transLen) {
            transToOrig[t] = origIndex
            // If transformed char matches original position advancement
            if (transformedString[t] != separator) {
                origIndex++
            }
        }
        transToOrig[transLen] = len

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val clamped = offset.coerceIn(0, len)
                return origToTrans[clamped]
            }

            override fun transformedToOriginal(offset: Int): Int {
                val clamped = offset.coerceIn(0, transLen)
                return transToOrig[clamped]
            }
        }

        return TransformedText(AnnotatedString(transformedString), offsetMapping)
    }
}
