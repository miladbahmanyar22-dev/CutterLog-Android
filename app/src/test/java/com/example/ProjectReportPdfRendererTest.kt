package com.example

import com.example.data.entity.PaymentEntity
import com.example.data.entity.ProjectClipEntity
import com.example.data.entity.ProjectEntity
import com.example.data.entity.ProjectRevisionEntity
import com.example.data.entity.TimerSessionEntity
import com.example.data.model.ProjectReportData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProjectReportPdfRendererTest {

    @Test
    fun testProjectReportData_minimalCalculations() {
        val project = ProjectEntity(
            id = 1,
            name = "پروژه عروسی مینیمال",
            studioName = "استودیو نور و نگاه",
            weddingDate = "1403/06/15",
            price = 15_000_000.0,
            status = "COMPLETED",
            createdAt = "1403/06/10"
        )
        val report = ProjectReportData(
            project = project,
            clips = emptyList(),
            payments = emptyList(),
            sessions = listOf(
                TimerSessionEntity(
                    id = 1,
                    projectId = 1,
                    clipName = "کلیپ اصلی",
                    category = "تدوین",
                    startTime = "10:00:00",
                    endTime = "12:00:00",
                    durationSeconds = 7200L,
                    date = "1403/06/12",
                    note = null
                )
            ),
            revisions = emptyList()
        )

        assertEquals("REP-0001", report.reportNumber)
        assertEquals(0, report.totalClipsCount)
        assertEquals(0, report.completedClipsCount)
        assertEquals(100, report.progressPercent)
        assertEquals(7200L, report.totalDurationSeconds)
        assertEquals("02:00:00", report.formattedDurationHMS)
        assertEquals(0.0, report.totalPaid, 0.01)
        assertEquals(15_000_000.0, report.remainingBalance, 0.01)
        assertFalse(report.isSettled)
        assertTrue(report.revisionRounds.isEmpty())
    }

    @Test
    fun testProjectReportData_richCalculations() {
        val project = ProjectEntity(
            id = 42,
            name = "پروژه فرمالیته و عروسی مجلل هتل اسپیناس پالاس",
            studioName = "استودیو رویای سفید",
            weddingDate = "1403/05/20",
            price = 45_000_000.0,
            status = "REVISION",
            deadlineDate = "1403/06/30",
            createdAt = "1403/05/01"
        )

        val clips = (1..15).map { i ->
            ProjectClipEntity(
                id = i,
                projectId = 42,
                clipName = "کلیپ $i",
                estimateMins = 180,
                isDone = if (i <= 10) 1 else 0,
                endDate = if (i <= 10) "1403/05/2$i" else null
            )
        }

        val revisions = listOf(
            ProjectRevisionEntity(id = 1, projectId = 42, phaseNum = 1, description = "اصلاح رنگ سکانس‌های ورود", isApplied = 1),
            ProjectRevisionEntity(id = 2, projectId = 42, phaseNum = 1, description = "تغییر آهنگ بخش رقص", isApplied = 1),
            ProjectRevisionEntity(id = 3, projectId = 42, phaseNum = 2, description = "تنظیم ولوم صدای سخنرانی", isApplied = 0)
        )

        val payments = listOf(
            PaymentEntity(id = 1, projectId = 42, amount = 25_000_000.0, date = "1403/05/01", note = "بیعانه اولیه"),
            PaymentEntity(id = 2, projectId = 42, amount = 20_000_000.0, date = "1403/05/22", note = "تسویه نهایی")
        )

        val report = ProjectReportData(
            project = project,
            clips = clips,
            payments = payments,
            sessions = emptyList(),
            revisions = revisions
        )

        assertEquals("REP-0042", report.reportNumber)
        assertEquals(15, report.totalClipsCount)
        assertEquals(10, report.completedClipsCount)
        assertEquals(5, report.remainingClipsCount)
        assertEquals(66, report.progressPercent)
        assertEquals(45_000_000.0, report.totalPaid, 0.01)
        assertEquals(0.0, report.remainingBalance, 0.01)
        assertTrue(report.isSettled)
        assertEquals(2, report.revisionRounds.size)
        assertEquals(2, report.revisionRounds[0].totalCount)
        assertEquals(2, report.revisionRounds[0].appliedCount)
        assertTrue(report.revisionRounds[0].isRoundComplete)
        assertEquals(1, report.revisionRounds[1].totalCount)
        assertEquals(0, report.revisionRounds[1].appliedCount)
        assertFalse(report.revisionRounds[1].isRoundComplete)
    }
}


