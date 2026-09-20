package com.example

import com.example.ui.components.DeveloperSupportConstants
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DeveloperSupportTest {

    @Test
    fun testDeveloperSupportConstants() {
        // 1. Verify Developer Name
        assertEquals("میلاد بهمنیار", DeveloperSupportConstants.DEVELOPER_NAME)

        // 2. Verify Bank Name
        assertEquals("بلوبانک", DeveloperSupportConstants.BANK_NAME)

        // 3. Verify Card Number Raw format
        assertEquals("6219861980223514", DeveloperSupportConstants.CARD_NUMBER_RAW)
        assertEquals(16, DeveloperSupportConstants.CARD_NUMBER_RAW.length)
        assertTrue(DeveloperSupportConstants.CARD_NUMBER_RAW.all { it.isDigit() })

        // 4. Verify Formatted Card Number contains exact raw digits without alterations
        val digitsOnly = DeveloperSupportConstants.CARD_NUMBER_FORMATTED.filter { it.isDigit() }
        assertEquals(DeveloperSupportConstants.CARD_NUMBER_RAW, digitsOnly)
        assertFalse(DeveloperSupportConstants.CARD_NUMBER_RAW.contains(" "))
        assertFalse(DeveloperSupportConstants.CARD_NUMBER_RAW.contains("-"))

        // 5. Verify SMS Contact and Pre-filled Message
        assertEquals("09368330224", DeveloperSupportConstants.CONTACT_PHONE)
        assertTrue(DeveloperSupportConstants.SMS_DEFAULT_MESSAGE.contains("سلام میلاد، من از کاترلاگ حمایت کردم"))
        assertTrue(DeveloperSupportConstants.SMS_DEFAULT_MESSAGE.contains("خواستم بابت زمانی که برای توسعه و بهبود برنامه می‌گذاری تشکر کنم"))
    }
}
