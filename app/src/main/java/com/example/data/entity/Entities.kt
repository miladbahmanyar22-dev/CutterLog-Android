package com.example.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "studios",
    indices = [Index(value = ["name"], unique = true)]
)
data class StudioEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String
)

@Entity(
    tableName = "default_clips",
    indices = [Index(value = ["name"], unique = true)]
)
data class DefaultClipEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String
)

@Entity(
    tableName = "projects",
    indices = [Index(value = ["project_code"], unique = true)]
)
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "project_code") val projectCode: String = "",
    val name: String,
    @ColumnInfo(name = "studio_name") val studioName: String,
    val price: Double = 0.0,
    @ColumnInfo(name = "is_settled") val isSettled: Int = 0, // 1 = settled, 0 = active debt
    @ColumnInfo(name = "created_at") val createdAt: String, // YYYY/MM/DD
    @ColumnInfo(name = "deadline_date") val deadlineDate: String? = null,
    @ColumnInfo(name = "status") val status: String = STATUS_EDITING, // EDITING, REVISION, READY_FOR_DELIVERY, COMPLETED
    @ColumnInfo(name = "wedding_date") val weddingDate: String? = null,
    @ColumnInfo(name = "delivered_at") val deliveredAt: String? = null // YYYY/MM/DD - Null means not yet delivered to studio
) {
    companion object {
        const val STATUS_EDITING = "EDITING"
        const val STATUS_REVISION = "REVISION"
        const val STATUS_READY_FOR_DELIVERY = "READY_FOR_DELIVERY"
        const val STATUS_COMPLETED = "COMPLETED" // Equivalent to Delivered
        const val STATUS_DELIVERED = "DELIVERED"
    }

    val isDelivered: Boolean
        get() = status == STATUS_COMPLETED || status == STATUS_DELIVERED || deliveredAt != null

    val isReadyForDelivery: Boolean
        get() = (status == STATUS_READY_FOR_DELIVERY || (!isDelivered && status != STATUS_REVISION))
}

@Entity(
    tableName = "project_clips",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["project_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["project_id"])]
)
data class ProjectClipEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "project_id") val projectId: Int,
    @ColumnInfo(name = "clip_name") val clipName: String,
    @ColumnInfo(name = "is_done") val isDone: Int = 0, // 1 = done
    @ColumnInfo(name = "end_date") val endDate: String? = null,
    @ColumnInfo(name = "estimate_mins") val estimateMins: Int = 0
)

@Entity(
    tableName = "payments",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["project_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["project_id"])]
)
data class PaymentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "project_id") val projectId: Int,
    val amount: Double,
    val date: String, // YYYY/MM/DD
    val note: String? = null
)

@Entity(
    tableName = "project_revisions",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["project_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["project_id"])]
)
data class ProjectRevisionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "project_id") val projectId: Int,
    val description: String,
    @ColumnInfo(name = "is_applied") val isApplied: Int = 0, // 1 = applied
    @ColumnInfo(name = "phase_num") val phaseNum: Int = 1
)

@Entity(
    tableName = "timer_sessions",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["project_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index(value = ["project_id"])]
)
data class TimerSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "project_id") val projectId: Int? = null,
    @ColumnInfo(name = "clip_name") val clipName: String,
    val category: String,
    @ColumnInfo(name = "start_time") val startTime: String, // HH:MM:SS
    @ColumnInfo(name = "end_time") val endTime: String,     // HH:MM:SS
    @ColumnInfo(name = "duration_seconds") val durationSeconds: Long,
    val note: String? = null,
    val date: String // YYYY/MM/DD
)

@Entity(tableName = "app_config")
data class AppConfigEntity(
    @PrimaryKey val id: Int = 1,
    @ColumnInfo(name = "daily_quota_hours") val dailyQuotaHours: Double = 8.0,
    @ColumnInfo(name = "grace_period_seconds") val gracePeriodSeconds: Int = 15,
    @ColumnInfo(name = "default_deadline_days") val defaultDeadlineDays: Int = 7,
    @ColumnInfo(name = "sound_alerts_enabled") val soundAlertsEnabled: Boolean = true,
    @ColumnInfo(name = "auto_start") val autoStart: Boolean = false,
    @ColumnInfo(name = "auto_backup_on_exit") val autoBackupOnExit: Boolean = true,
    @ColumnInfo(name = "last_backup_date") val lastBackupDate: String = "",
    @ColumnInfo(name = "invoice_brand_title") val invoiceBrandTitle: String = "استودیو فیلم و تدوین",
    @ColumnInfo(name = "invoice_bank_card") val invoiceBankCard: String = "۶۰۳۷-۹۹۷۹-۰۰۰۰-۰۰۰۰",
    @ColumnInfo(name = "invoice_bank_owner") val invoiceBankOwner: String = "تدوینگر گرامی",
    @ColumnInfo(name = "invoice_logo_path") val invoiceLogoPath: String = "",
    @ColumnInfo(name = "invoice_signature_path") val invoiceSignaturePath: String = "",
    @ColumnInfo(name = "invoice_footer_note") val invoiceFooterNote: String = "با تشکر از همکاری شما. تسویه حساب طبق قرارداد الزامی است.",
    @ColumnInfo(name = "quick_prices") val quickPricesJson: String = "[500000, 1000000, 2000000, 3000000, 5000000, 10000000]",
    @ColumnInfo(name = "packages_json") val packagesJson: String = "[{\"name\":\"پکیج کامل\",\"clips\":[\"کلیپ اصلی\",\"کلیپ فرمالیته\",\"تیزر اینستاگرام\"]},{\"name\":\"پکیج اقتصادی\",\"clips\":[\"کلیپ اصلی\",\"تیزر اینستاگرام\"]}]",
    @ColumnInfo(name = "last_project_code_sequences") val lastProjectCodeSequencesJson: String = "{}"
)
