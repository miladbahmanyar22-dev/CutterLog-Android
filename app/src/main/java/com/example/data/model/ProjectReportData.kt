package com.example.data.model

import com.example.data.entity.PaymentEntity
import com.example.data.entity.ProjectClipEntity
import com.example.data.entity.ProjectEntity
import com.example.data.entity.ProjectRevisionEntity
import com.example.data.entity.TimerSessionEntity
import com.example.util.PersianUtils

data class RevisionRoundSummary(
    val phaseNum: Int,
    val items: List<ProjectRevisionEntity>,
    val totalCount: Int,
    val appliedCount: Int,
    val isRoundComplete: Boolean
)

data class ProjectReportData(
    val project: ProjectEntity,
    val clips: List<ProjectClipEntity>,
    val payments: List<PaymentEntity>,
    val sessions: List<TimerSessionEntity>,
    val revisions: List<ProjectRevisionEntity>,
    val reportDate: String = PersianUtils.getCurrentJalaliDate(),
    val brandTitle: String = "کاترلاگ"
) {
    val reportNumber: String = "REP-${project.id.toString().padStart(4, '0')}"

    val totalDurationSeconds: Long = sessions.sumOf { it.durationSeconds }

    val formattedDurationHMS: String = PersianUtils.formatSecondsToHMS(totalDurationSeconds)

    val formattedDurationPersianWords: String get() {
        val h = totalDurationSeconds / 3600
        val m = (totalDurationSeconds % 3600) / 60
        return when {
            h > 0 && m > 0 -> "${PersianUtils.faNum(h)} ساعت و ${PersianUtils.faNum(m)} دقیقه"
            h > 0 -> "${PersianUtils.faNum(h)} ساعت"
            m > 0 -> "${PersianUtils.faNum(m)} دقیقه"
            totalDurationSeconds > 0 -> "${PersianUtils.faNum(totalDurationSeconds)} ثانیه"
            else -> "ثبت نشده"
        }
    }

    val totalClipsCount: Int = clips.size
    val completedClipsCount: Int = clips.count { it.isDone == 1 }
    val remainingClipsCount: Int = totalClipsCount - completedClipsCount
    val progressPercent: Int = if (totalClipsCount == 0) 100 else ((completedClipsCount * 100) / totalClipsCount)

    val totalPaid: Double = payments.sumOf { it.amount }
    val remainingBalance: Double = (project.price - totalPaid).coerceAtLeast(0.0)
    val isSettled: Boolean = project.isSettled == 1 || totalPaid >= project.price

    val studioDisplayName: String = PersianUtils.formatStudioName(project.studioName)

    val priceInWords: String = PersianUtils.numberToWordsFa(project.price)

    // Last clip delivery date if any
    val lastDeliveryDate: String? = clips.mapNotNull { it.endDate }.filter { it.isNotBlank() }.maxOrNull()

    // Revision rounds
    val revisionRounds: List<RevisionRoundSummary> = revisions
        .groupBy { it.phaseNum }
        .toSortedMap()
        .map { (phase, revList) ->
            val total = revList.size
            val applied = revList.count { it.isApplied == 1 }
            RevisionRoundSummary(
                phaseNum = phase,
                items = revList,
                totalCount = total,
                appliedCount = applied,
                isRoundComplete = total > 0 && total == applied
            )
        }

    val totalRevisionsCount: Int = revisions.size
    val totalAppliedRevisionsCount: Int = revisions.count { it.isApplied == 1 }
}
