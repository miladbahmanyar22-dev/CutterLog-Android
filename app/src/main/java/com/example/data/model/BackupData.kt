package com.example.data.model

data class BackupMetadata(
    val appName: String = "CutterLog Pro",
    val appVersion: String = "1.0.0",
    val schemaVersion: Int = 4,
    val backupId: String = "",
    val timestampMillis: Long = System.currentTimeMillis(),
    val jalaliDate: String = "",
    val projectCount: Int = 0,
    val clipCount: Int = 0,
    val paymentCount: Int = 0,
    val revisionCount: Int = 0,
    val sessionCount: Int = 0,
    val studioCount: Int = 0,
    val defaultClipCount: Int = 0,
    val totalContractAmount: Double = 0.0,
    val totalCollectedAmount: Double = 0.0
)

data class BackupValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null,
    val metadata: BackupMetadata? = null
)

data class LocalBackupSnapshot(
    val fileName: String,
    val filePath: String,
    val fileSizeBytes: Long,
    val formattedSize: String,
    val timestampMillis: Long,
    val jalaliDate: String,
    val metadata: BackupMetadata? = null,
    val isSafetySnapshot: Boolean = false
)

data class RestoreExecutionResult(
    val success: Boolean,
    val message: String,
    val metadata: BackupMetadata? = null,
    val restoredProjectsCount: Int = 0,
    val restoredPaymentsCount: Int = 0,
    val restoredSessionsCount: Int = 0,
    val safetyBackupSaved: Boolean = false
)

data class ResetExecutionResult(
    val success: Boolean,
    val type: String,
    val title: String,
    val description: String,
    val timestamp: String,
    val itemsAffectedCount: Int
)
