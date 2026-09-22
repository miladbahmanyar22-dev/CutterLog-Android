package com.example.data.repository

import android.content.Context
import androidx.room.withTransaction
import com.example.BuildConfig
import com.example.data.database.AppDatabase
import com.example.data.entity.AppConfigEntity
import com.example.data.entity.DefaultClipEntity
import com.example.data.entity.PaymentEntity
import com.example.data.entity.ProjectClipEntity
import com.example.data.entity.ProjectEntity
import com.example.data.entity.ProjectRevisionEntity
import com.example.data.entity.StudioEntity
import com.example.data.entity.TimerSessionEntity
import com.example.data.model.BackupMetadata
import com.example.data.model.BackupValidationResult
import com.example.data.model.LocalBackupSnapshot
import com.example.data.model.ResetExecutionResult
import com.example.data.model.RestoreExecutionResult
import com.example.util.PersianUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.math.min

class CutterLogRepository(private val db: AppDatabase) {

    // Studios
    val studiosFlow: Flow<List<StudioEntity>> = db.studioDao().getAllStudios()
    suspend fun addStudio(name: String) = db.studioDao().insertStudio(StudioEntity(name = name.trim()))
    suspend fun updateStudio(studio: StudioEntity) = db.studioDao().updateStudio(studio)
    suspend fun deleteStudio(studio: StudioEntity) = db.studioDao().deleteStudio(studio)

    // Default Clips
    val defaultClipsFlow: Flow<List<DefaultClipEntity>> = db.defaultClipDao().getAllDefaultClips()
    suspend fun addDefaultClip(name: String) = db.defaultClipDao().insertDefaultClip(DefaultClipEntity(name = name.trim()))
    suspend fun updateDefaultClip(clip: DefaultClipEntity) = db.defaultClipDao().updateDefaultClip(clip)
    suspend fun deleteDefaultClip(clip: DefaultClipEntity) = db.defaultClipDao().deleteDefaultClip(clip)

    // Projects
    val allProjectsFlow: Flow<List<ProjectEntity>> = db.projectDao().getAllProjects()
    val allClipsFlow: Flow<List<ProjectClipEntity>> = db.projectClipDao().getAllProjectClips()
    val allPaymentsFlow: Flow<List<PaymentEntity>> = db.paymentDao().getAllPayments()
    val allSessionsFlow: Flow<List<TimerSessionEntity>> = db.timerSessionDao().getAllSessions()
    val allRevisionsFlow: Flow<List<ProjectRevisionEntity>> = db.projectRevisionDao().getAllRevisions()

    private fun parseSequenceMap(jsonStr: String?): MutableMap<Int, Int> {
        val map = mutableMapOf<Int, Int>()
        if (jsonStr.isNullOrBlank()) return map
        try {
            val obj = org.json.JSONObject(jsonStr)
            val keys = obj.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                val year = k.toIntOrNull()
                if (year != null) {
                    map[year] = obj.optInt(k, 0)
                }
            }
        } catch (e: Exception) {
            // fallback
        }
        return map
    }

    private fun serializeSequenceMap(map: Map<Int, Int>): String {
        val obj = org.json.JSONObject()
        map.forEach { (year, seq) ->
            obj.put(year.toString(), seq)
        }
        return obj.toString()
    }

    suspend fun allocateProjectCodeForYear(year: Int): String {
        val cfg = getConfigSync()
        val seqMap = parseSequenceMap(cfg.lastProjectCodeSequencesJson)
        val storedSeq = seqMap[year] ?: 0

        // Scan DB for any existing project codes for this year to guarantee monotonic growth without collision
        val existingProjects = db.projectDao().getAllProjectsSync()
        val dbMaxSeq = existingProjects.mapNotNull { p ->
            val code = p.projectCode
            val clean = PersianUtils.convertFaToEnNum(code.trim().uppercase())
            val parts = clean.split("-")
            if (parts.size == 3 && parts[0] == "CL" && parts[1].toIntOrNull() == year) {
                parts[2].toIntOrNull()
            } else null
        }.maxOrNull() ?: 0

        val maxKnown = maxOf(storedSeq, dbMaxSeq)
        val nextSeq = maxKnown + 1
        seqMap[year] = nextSeq

        // Save updated sequence map back to app config
        val updatedCfg = cfg.copy(lastProjectCodeSequencesJson = serializeSequenceMap(seqMap))
        db.appConfigDao().saveConfig(updatedCfg)

        return PersianUtils.formatProjectCode(year, nextSeq)
    }

    suspend fun generateNextProjectCode(creationDate: String? = null): String {
        val year = PersianUtils.extractJalaliYear(creationDate ?: PersianUtils.getCurrentJalaliDate())
        return allocateProjectCodeForYear(year)
    }

    suspend fun ensureProjectCodesMigrated() {
        val projects = db.projectDao().getAllProjectsSync()
        val unassigned = projects.filter { it.projectCode.isBlank() }
        if (unassigned.isEmpty()) return

        // Sort by ID ascending so earlier created projects get earlier sequence numbers
        val sorted = unassigned.sortedBy { it.id }
        for (proj in sorted) {
            val y = PersianUtils.extractJalaliYear(proj.createdAt.ifBlank { proj.weddingDate })
            val code = allocateProjectCodeForYear(y)
            db.projectDao().updateProject(proj.copy(projectCode = code))
        }
    }

    suspend fun createProjectWithClips(
        name: String,
        studioName: String,
        price: Double,
        clipNames: List<String>,
        deadlineDays: Int,
        weddingDate: String? = null
    ): Long {
        val createdAt = PersianUtils.getCurrentJalaliDate()
        val deadlineDate = PersianUtils.addDaysToJalali(createdAt, deadlineDays)
        val projectCode = generateNextProjectCode(createdAt)
        val project = ProjectEntity(
            projectCode = projectCode,
            name = name,
            studioName = studioName,
            price = price,
            isSettled = if (price <= 0) 1 else 0,
            createdAt = createdAt,
            deadlineDate = deadlineDate,
            weddingDate = weddingDate
        )
        val projectId = db.projectDao().insertProject(project).toInt()

        val clipEntities = clipNames.map { clipName ->
            ProjectClipEntity(
                projectId = projectId,
                clipName = clipName,
                isDone = 0,
                estimateMins = 60
            )
        }
        db.projectClipDao().insertClips(clipEntities)
        return projectId.toLong()
    }

    suspend fun updateProject(project: ProjectEntity) = db.projectDao().updateProject(project)
    suspend fun deleteProject(project: ProjectEntity) = db.projectDao().deleteProject(project)
    suspend fun deleteProjectById(id: Int) = db.projectDao().deleteProjectById(id)

    // Project Clips
    fun getClipsForProject(projectId: Int): Flow<List<ProjectClipEntity>> = db.projectClipDao().getClipsForProject(projectId)
    suspend fun toggleClipDone(clip: ProjectClipEntity) {
        val newDone = if (clip.isDone == 1) 0 else 1
        val endDate = if (newDone == 1) PersianUtils.getCurrentJalaliDate() else null
        db.projectClipDao().updateClip(clip.copy(isDone = newDone, endDate = endDate))
        checkAndUpdateProjectStatus(clip.projectId)
    }
    suspend fun addClipToProject(projectId: Int, clipName: String) {
        db.projectClipDao().insertClip(ProjectClipEntity(projectId = projectId, clipName = clipName))
        checkAndUpdateProjectStatus(projectId)
    }
    suspend fun deleteClip(clip: ProjectClipEntity) {
        db.projectClipDao().deleteClip(clip)
        checkAndUpdateProjectStatus(clip.projectId)
    }

    suspend fun markClipCompletedInProject(projectId: Int, clipName: String) {
        val clips = db.projectClipDao().getClipsForProjectSync(projectId)
        val targetClip = clips.firstOrNull { it.clipName.equals(clipName, ignoreCase = true) }
        if (targetClip != null && targetClip.isDone == 0) {
            db.projectClipDao().updateClip(targetClip.copy(isDone = 1, endDate = PersianUtils.getCurrentJalaliDate()))
            checkAndUpdateProjectStatus(projectId)
        }
    }

    suspend fun checkAndUpdateProjectStatus(projectId: Int) {
        val project = db.projectDao().getProjectById(projectId) ?: return
        val clips = db.projectClipDao().getClipsForProjectSync(projectId)
        val revisions = db.projectRevisionDao().getRevisionsForProjectSync(projectId)

        val allClipsDone = clips.isNotEmpty() && clips.all { it.isDone == 1 }
        val hasPendingRevisions = revisions.any { it.isApplied == 0 }

        // If the project is already delivered, do not change its status unless revisions are actively added
        val newStatus = when {
            hasPendingRevisions -> ProjectEntity.STATUS_REVISION
            project.deliveredAt != null || project.status == ProjectEntity.STATUS_COMPLETED || project.status == ProjectEntity.STATUS_DELIVERED -> {
                ProjectEntity.STATUS_COMPLETED
            }
            allClipsDone -> ProjectEntity.STATUS_READY_FOR_DELIVERY
            else -> ProjectEntity.STATUS_EDITING
        }

        if (project.status != newStatus) {
            db.projectDao().updateProject(project.copy(status = newStatus))
        }
    }

    /**
     * Explicitly deliver project to client/studio.
     * Sets status = COMPLETED and sets deliveredAt timestamp.
     * Strictly Idempotent: If already delivered with identical status and timestamp, does nothing and avoids redundant operations.
     */
    suspend fun deliverProject(projectId: Int, deliveryDate: String? = null) {
        val project = db.projectDao().getProjectById(projectId) ?: return
        val actualDeliveryDate = deliveryDate?.ifBlank { null } ?: project.deliveredAt ?: PersianUtils.getCurrentJalaliDate()
        
        // Idempotency: Avoid re-writing or triggering duplicate cascades if already delivered
        if (project.isDelivered && project.deliveredAt == actualDeliveryDate && project.status == ProjectEntity.STATUS_COMPLETED) {
            return
        }

        db.projectDao().updateProject(
            project.copy(
                status = ProjectEntity.STATUS_COMPLETED,
                deliveredAt = actualDeliveryDate
            )
        )
    }

    /**
     * Revert delivery status (e.g. if user needs to re-edit or undo delivery)
     */
    suspend fun undeliverProject(projectId: Int) {
        val project = db.projectDao().getProjectById(projectId) ?: return
        val clips = db.projectClipDao().getClipsForProjectSync(projectId)
        val revisions = db.projectRevisionDao().getRevisionsForProjectSync(projectId)

        val allClipsDone = clips.isNotEmpty() && clips.all { it.isDone == 1 }
        val hasPendingRevisions = revisions.any { it.isApplied == 0 }

        val restoredStatus = when {
            hasPendingRevisions -> ProjectEntity.STATUS_REVISION
            allClipsDone -> ProjectEntity.STATUS_READY_FOR_DELIVERY
            else -> ProjectEntity.STATUS_EDITING
        }

        db.projectDao().updateProject(
            project.copy(
                status = restoredStatus,
                deliveredAt = null
            )
        )
    }

    suspend fun reopenProjectFromArchive(projectId: Int) {
        val project = db.projectDao().getProjectById(projectId) ?: return
        db.projectDao().updateProject(
            project.copy(
                status = ProjectEntity.STATUS_EDITING,
                deliveredAt = null
            )
        )
    }

    // Payments
    fun getPaymentsForProject(projectId: Int): Flow<List<PaymentEntity>> = db.paymentDao().getPaymentsForProject(projectId)
    suspend fun addPayment(projectId: Int, amount: Double, note: String?) {
        val payment = PaymentEntity(
            projectId = projectId,
            amount = amount,
            date = PersianUtils.getCurrentJalaliDate(),
            note = note
        )
        db.paymentDao().insertPayment(payment)
        
        // Auto-check if project is fully settled
        val project = db.projectDao().getProjectById(projectId)
        if (project != null) {
            val allPayments = db.paymentDao().getPaymentsForProjectSync(projectId)
            val totalPaid = allPayments.sumOf { it.amount }
            if (totalPaid >= project.price) {
                db.projectDao().updateProject(project.copy(isSettled = 1))
            }
        }
    }

    suspend fun settleProjectFully(projectId: Int) {
        val project = db.projectDao().getProjectById(projectId) ?: return
        val existingPayments = db.paymentDao().getPaymentsForProjectSync(projectId)
        val totalPaid = existingPayments.sumOf { it.amount }
        val remainingDebt = project.price - totalPaid

        if (remainingDebt > 0) {
            val currentDate = PersianUtils.getCurrentJalaliDate()
            db.paymentDao().insertPayment(
                PaymentEntity(
                    projectId = projectId,
                    amount = remainingDebt,
                    date = currentDate,
                    note = "تسویه کامل پروژه"
                )
            )
        }
        db.projectDao().updateProject(project.copy(isSettled = 1))
    }

    suspend fun deletePayment(payment: PaymentEntity) = db.paymentDao().deletePayment(payment)

    // Smart Bulk Payment Distribution Algorithm (_submit_bulk_payment_action)
    suspend fun submitBulkPaymentForStudio(studioName: String, amount: Double, note: String?): Int {
        if (amount <= 0) return 0
        var remainingPayment = amount
        val studioProjects = db.projectDao().getProjectsByStudioSync(studioName)
            .filter { it.isSettled == 0 }
            .sortedBy { it.id } // Oldest projects first

        var projectsAffected = 0
        val currentDate = PersianUtils.getCurrentJalaliDate()

        for (project in studioProjects) {
            if (remainingPayment <= 0) break
            val existingPayments = db.paymentDao().getPaymentsForProjectSync(project.id)
            val totalPaid = existingPayments.sumOf { it.amount }
            val remDebt = project.price - totalPaid

            if (remDebt <= 0) {
                db.projectDao().updateProject(project.copy(isSettled = 1))
                continue
            }

            val payForThis = min(remainingPayment, remDebt)
            if (payForThis > 0) {
                val paymentNote = if (!note.isNull_or_blank()) "$note (واریزی کلی)" else "واریزی کلی آتلیه - $currentDate"
                db.paymentDao().insertPayment(
                    PaymentEntity(
                        projectId = project.id,
                        amount = payForThis,
                        date = currentDate,
                        note = paymentNote
                    )
                )
                remainingPayment -= payForThis
                projectsAffected++

                if ((totalPaid + payForThis) >= project.price) {
                    db.projectDao().updateProject(project.copy(isSettled = 1))
                }
            }
        }
        return projectsAffected
    }

    private fun String?.isNull_or_blank(): Boolean = this == null || this.trim().isEmpty()

    // Revisions
    fun getRevisionsForProject(projectId: Int): Flow<List<ProjectRevisionEntity>> = db.projectRevisionDao().getRevisionsForProject(projectId)
    suspend fun getRevisionsForProjectSync(projectId: Int): List<ProjectRevisionEntity> = db.projectRevisionDao().getRevisionsForProjectSync(projectId)
    suspend fun addRevision(projectId: Int, description: String, phaseNum: Int) {
        db.projectRevisionDao().insertRevision(
            ProjectRevisionEntity(
                projectId = projectId,
                description = description,
                isApplied = 0,
                phaseNum = phaseNum
            )
        )
        checkAndUpdateProjectStatus(projectId)
    }
    suspend fun toggleRevisionApplied(revision: ProjectRevisionEntity) {
        val newApplied = if (revision.isApplied == 1) 0 else 1
        db.projectRevisionDao().updateRevision(revision.copy(isApplied = newApplied))
        checkAndUpdateProjectStatus(revision.projectId)
    }
    suspend fun deleteRevision(revision: ProjectRevisionEntity) {
        db.projectRevisionDao().deleteRevision(revision)
        checkAndUpdateProjectStatus(revision.projectId)
    }

    // Timer Sessions
    fun getSessionsForDate(dateStr: String): Flow<List<TimerSessionEntity>> = db.timerSessionDao().getSessionsForDate(dateStr)
    suspend fun saveTimerSession(
        projectId: Int?,
        clipName: String,
        category: String,
        startTime: String,
        endTime: String,
        durationSeconds: Long,
        note: String?
    ) {
        val session = TimerSessionEntity(
            projectId = projectId,
            clipName = clipName,
            category = category,
            startTime = startTime,
            endTime = endTime,
            durationSeconds = durationSeconds,
            note = note,
            date = PersianUtils.getCurrentJalaliDate()
        )
        db.timerSessionDao().insertSession(session)
    }
    suspend fun updateTimerSession(session: TimerSessionEntity) = db.timerSessionDao().updateSession(session)
    suspend fun deleteTimerSession(session: TimerSessionEntity) = db.timerSessionDao().deleteSession(session)

    // App Config
    val appConfigFlow: Flow<AppConfigEntity?> = db.appConfigDao().getConfigFlow()
    suspend fun getConfigSync(): AppConfigEntity {
        return db.appConfigDao().getConfigSync() ?: AppConfigEntity().also { db.appConfigDao().saveConfig(it) }
    }
    suspend fun saveConfig(config: AppConfigEntity) = db.appConfigDao().saveConfig(config)

    // =============================================================================================
    // BACKUP, VALIDATION, SNAPSHOTS & ATOMIC RESTORE SYSTEM
    // =============================================================================================

    /**
     * Generates a complete, structured JSON backup string containing full metadata and all entity tables.
     */
    suspend fun generateBackupJson(context: Context? = null): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        val nowMillis = System.currentTimeMillis()
        val jalaliNow = PersianUtils.getCurrentJalaliDate()
        val backupId = "BK-${SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date(nowMillis))}"

        val cfg = getConfigSync()
        val studioList = db.studioDao().getAllStudiosSync()
        val clipList = db.defaultClipDao().getAllDefaultClipsSync()
        val projectList = db.projectDao().getAllProjectsSync()
        val pClipList = db.projectClipDao().getAllProjectClipsSync()
        val paymentList = db.paymentDao().getAllPaymentsSync()
        val revList = db.projectRevisionDao().getAllRevisionsSync()
        val sessionList = db.timerSessionDao().getAllSessionsSync()

        val totalContract = projectList.sumOf { it.price }
        val totalPaid = paymentList.sumOf { it.amount }

        // 1. Metadata Block
        val metaObj = JSONObject().apply {
            put("appName", "CutterLog Pro")
            put("appVersion", BuildConfig.VERSION_NAME)
            put("schemaVersion", 4)
            put("backupId", backupId)
            put("timestampMillis", nowMillis)
            put("jalaliDate", jalaliNow)
            put("projectCount", projectList.size)
            put("clipCount", pClipList.size)
            put("paymentCount", paymentList.size)
            put("revisionCount", revList.size)
            put("sessionCount", sessionList.size)
            put("studioCount", studioList.size)
            put("defaultClipCount", clipList.size)
            put("totalContractAmount", totalContract)
            put("totalCollectedAmount", totalPaid)
        }
        root.put("metadata", metaObj)

        // 2. App Config Block
        val cfgObj = JSONObject().apply {
            put("dailyQuotaHours", cfg.dailyQuotaHours)
            put("gracePeriodSeconds", cfg.gracePeriodSeconds)
            put("defaultDeadlineDays", cfg.defaultDeadlineDays)
            put("soundAlertsEnabled", cfg.soundAlertsEnabled)
            put("autoStart", cfg.autoStart)
            put("autoBackupOnExit", cfg.autoBackupOnExit)
            put("lastBackupDate", jalaliNow)
            put("invoiceBrandTitle", cfg.invoiceBrandTitle)
            put("invoiceBankCard", cfg.invoiceBankCard)
            put("invoiceBankOwner", cfg.invoiceBankOwner)
            put("invoiceLogoPath", cfg.invoiceLogoPath)
            put("invoiceSignaturePath", cfg.invoiceSignaturePath)
            put("invoiceFooterNote", cfg.invoiceFooterNote)
            put("quickPricesJson", cfg.quickPricesJson)
            put("packagesJson", cfg.packagesJson)
            put("lastProjectCodeSequencesJson", cfg.lastProjectCodeSequencesJson)
        }
        root.put("app_config", cfgObj)

        // 3. Studios
        val studiosArr = JSONArray()
        studioList.forEach { st ->
            studiosArr.put(JSONObject().apply {
                put("id", st.id)
                put("name", st.name)
            })
        }
        root.put("studios", studiosArr)

        // 4. Default Clips
        val defaultClipsArr = JSONArray()
        clipList.forEach { cl ->
            defaultClipsArr.put(JSONObject().apply {
                put("id", cl.id)
                put("name", cl.name)
            })
        }
        root.put("default_clips", defaultClipsArr)

        // 5. Projects
        val projectsArr = JSONArray()
        projectList.forEach { pr ->
            projectsArr.put(JSONObject().apply {
                put("id", pr.id)
                put("projectCode", pr.projectCode)
                put("name", pr.name)
                put("studioName", pr.studioName)
                put("price", pr.price)
                put("status", pr.status)
                put("isSettled", pr.isSettled)
                put("createdAt", pr.createdAt)
                put("deadlineDate", pr.deadlineDate ?: "")
                put("weddingDate", pr.weddingDate ?: "")
                put("deliveredAt", pr.deliveredAt ?: "")
            })
        }
        root.put("projects", projectsArr)

        // 6. Project Clips
        val projectClipsArr = JSONArray()
        pClipList.forEach { pc ->
            projectClipsArr.put(JSONObject().apply {
                put("id", pc.id)
                put("projectId", pc.projectId)
                put("clipName", pc.clipName)
                put("isDone", pc.isDone)
                put("estimateMins", pc.estimateMins)
                put("endDate", pc.endDate ?: "")
            })
        }
        root.put("project_clips", projectClipsArr)

        // 7. Payments
        val paymentsArr = JSONArray()
        paymentList.forEach { py ->
            paymentsArr.put(JSONObject().apply {
                put("id", py.id)
                put("projectId", py.projectId)
                put("amount", py.amount)
                put("date", py.date)
                put("note", py.note ?: "")
            })
        }
        root.put("payments", paymentsArr)

        // 8. Project Revisions
        val revisionsArr = JSONArray()
        revList.forEach { rv ->
            revisionsArr.put(JSONObject().apply {
                put("id", rv.id)
                put("projectId", rv.projectId)
                put("description", rv.description)
                put("isApplied", rv.isApplied)
                put("phaseNum", rv.phaseNum)
            })
        }
        root.put("project_revisions", revisionsArr)

        // 9. Timer Sessions
        val sessionsArr = JSONArray()
        sessionList.forEach { ss ->
            sessionsArr.put(JSONObject().apply {
                put("id", ss.id)
                put("projectId", ss.projectId ?: JSONObject.NULL)
                put("clipName", ss.clipName)
                put("category", ss.category)
                put("startTime", ss.startTime)
                put("endTime", ss.endTime)
                put("durationSeconds", ss.durationSeconds)
                put("note", ss.note ?: "")
                put("date", ss.date)
            })
        }
        root.put("timer_sessions", sessionsArr)

        // Update last backup date in config
        saveConfig(cfg.copy(lastBackupDate = jalaliNow))

        root.toString(2)
    }

    /**
     * Inspects and validates a JSON string without modifying the database.
     */
    fun validateBackupJson(jsonStr: String): BackupValidationResult {
        if (jsonStr.isBlank()) {
            return BackupValidationResult(isValid = false, errorMessage = "محتوای فایل پشتیبان خالی است.")
        }
        return try {
            val root = JSONObject(jsonStr)

            // Check if it's a valid CutterLog backup (either modern metadata or legacy backup_version)
            val hasMetadata = root.has("metadata")
            val hasLegacyVersion = root.has("backup_version")
            val hasProjects = root.has("projects")
            val hasConfig = root.has("app_config")

            if (!hasMetadata && !hasLegacyVersion && !hasProjects && !hasConfig) {
                return BackupValidationResult(
                    isValid = false,
                    errorMessage = "فرمت فایل نامعتبر است؛ ساختار شناسه کاترلاگ در فایل یافت نشد."
                )
            }

            val meta: BackupMetadata = if (hasMetadata) {
                val m = root.getJSONObject("metadata")
                BackupMetadata(
                    appName = m.optString("appName", "CutterLog Pro"),
                    appVersion = m.optString("appVersion", "1.0.0"),
                    schemaVersion = m.optInt("schemaVersion", 4),
                    backupId = m.optString("backupId", "BK-UNKNOWN"),
                    timestampMillis = m.optLong("timestampMillis", System.currentTimeMillis()),
                    jalaliDate = m.optString("jalaliDate", PersianUtils.getCurrentJalaliDate()),
                    projectCount = m.optInt("projectCount", root.optJSONArray("projects")?.length() ?: 0),
                    clipCount = m.optInt("clipCount", root.optJSONArray("project_clips")?.length() ?: 0),
                    paymentCount = m.optInt("paymentCount", root.optJSONArray("payments")?.length() ?: 0),
                    revisionCount = m.optInt("revisionCount", root.optJSONArray("project_revisions")?.length() ?: 0),
                    sessionCount = m.optInt("sessionCount", root.optJSONArray("timer_sessions")?.length() ?: 0),
                    studioCount = m.optInt("studioCount", root.optJSONArray("studios")?.length() ?: 0),
                    defaultClipCount = m.optInt("defaultClipCount", root.optJSONArray("default_clips")?.length() ?: 0),
                    totalContractAmount = m.optDouble("totalContractAmount", 0.0),
                    totalCollectedAmount = m.optDouble("totalCollectedAmount", 0.0)
                )
            } else {
                // Legacy reconstruction
                val projects = root.optJSONArray("projects")
                val payments = root.optJSONArray("payments")
                var totalP = 0.0
                if (projects != null) {
                    for (i in 0 until projects.length()) {
                        totalP += projects.optJSONObject(i)?.optDouble("price", 0.0) ?: 0.0
                    }
                }
                var totalPaid = 0.0
                if (payments != null) {
                    for (i in 0 until payments.length()) {
                        totalPaid += payments.optJSONObject(i)?.optDouble("amount", 0.0) ?: 0.0
                    }
                }
                BackupMetadata(
                    appName = "CutterLog Pro (نسخه قدیمی)",
                    appVersion = "1.0.0",
                    schemaVersion = root.optInt("backup_version", 2),
                    backupId = "BK-LEGACY",
                    timestampMillis = System.currentTimeMillis(),
                    jalaliDate = root.optString("created_at", PersianUtils.getCurrentJalaliDate()),
                    projectCount = projects?.length() ?: 0,
                    clipCount = root.optJSONArray("project_clips")?.length() ?: 0,
                    paymentCount = payments?.length() ?: 0,
                    revisionCount = root.optJSONArray("project_revisions")?.length() ?: 0,
                    sessionCount = root.optJSONArray("timer_sessions")?.length() ?: 0,
                    studioCount = root.optJSONArray("studios")?.length() ?: 0,
                    defaultClipCount = root.optJSONArray("default_clips")?.length() ?: 0,
                    totalContractAmount = totalP,
                    totalCollectedAmount = totalPaid
                )
            }

            BackupValidationResult(isValid = true, metadata = meta)
        } catch (e: Exception) {
            BackupValidationResult(
                isValid = false,
                errorMessage = "خطا در خواندن فایل JSON: ${e.localizedMessage ?: "فرمت نامعتبر"}"
            )
        }
    }

    /**
     * Creates an internal snapshot backup stored in app internal storage.
     */
    suspend fun createLocalBackupSnapshot(
        context: Context,
        isSafetySnapshot: Boolean = false
    ): LocalBackupSnapshot? = withContext(Dispatchers.IO) {
        try {
            val backupsDir = File(context.filesDir, "backups")
            if (!backupsDir.exists()) {
                backupsDir.mkdirs()
            }

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val prefix = if (isSafetySnapshot) "safety_snapshot_pre_restore_" else "cutterlog_backup_"
            val fileName = "$prefix$timestamp.json"
            val file = File(backupsDir, fileName)

            val jsonContent = generateBackupJson(context)
            file.writeText(jsonContent, Charsets.UTF_8)

            if (!file.exists() || file.length() == 0L) {
                return@withContext null
            }

            val valResult = validateBackupJson(jsonContent)
            val sizeKb = (file.length() / 1024.0)
            val formattedSize = if (sizeKb >= 1024) String.format(Locale.US, "%.1f MB", sizeKb / 1024) else String.format(Locale.US, "%.1f KB", sizeKb)

            LocalBackupSnapshot(
                fileName = fileName,
                filePath = file.absolutePath,
                fileSizeBytes = file.length(),
                formattedSize = formattedSize,
                timestampMillis = file.lastModified(),
                jalaliDate = PersianUtils.getCurrentJalaliDate(),
                metadata = valResult.metadata,
                isSafetySnapshot = isSafetySnapshot
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Retrieves all locally saved snapshot backups.
     */
    suspend fun getLocalBackupSnapshots(context: Context): List<LocalBackupSnapshot> = withContext(Dispatchers.IO) {
        val backupsDir = File(context.filesDir, "backups")
        if (!backupsDir.exists() || !backupsDir.isDirectory) {
            return@withContext emptyList()
        }

        val files = backupsDir.listFiles { f -> f.isFile && f.name.endsWith(".json") } ?: return@withContext emptyList()
        files.sortedByDescending { it.lastModified() }.mapNotNull { file ->
            try {
                val json = file.readText(Charsets.UTF_8)
                val validation = validateBackupJson(json)
                val sizeKb = (file.length() / 1024.0)
                val formattedSize = if (sizeKb >= 1024) String.format(Locale.US, "%.1f MB", sizeKb / 1024) else String.format(Locale.US, "%.1f KB", sizeKb)
                val isSafety = file.name.startsWith("safety_snapshot_")

                LocalBackupSnapshot(
                    fileName = file.name,
                    filePath = file.absolutePath,
                    fileSizeBytes = file.length(),
                    formattedSize = formattedSize,
                    timestampMillis = file.lastModified(),
                    jalaliDate = validation.metadata?.jalaliDate ?: PersianUtils.getCurrentJalaliDate(),
                    metadata = validation.metadata,
                    isSafetySnapshot = isSafety
                )
            } catch (e: Exception) {
                null
            }
        }
    }

    /**
     * Safely deletes a local snapshot.
     */
    suspend fun deleteLocalBackupSnapshot(filePath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = File(filePath)
            if (file.exists()) file.delete() else false
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Fully atomic, validated restore execution from JSON string.
     * Takes safety snapshot before touching database, and executes in a single Room transaction.
     */
    suspend fun restoreDataFromJson(
        jsonStr: String,
        context: Context? = null,
        createSafetyBackup: Boolean = true
    ): RestoreExecutionResult = withContext(Dispatchers.IO) {
        // Step 1: Pre-validation
        val validation = validateBackupJson(jsonStr)
        if (!validation.isValid || validation.metadata == null) {
            return@withContext RestoreExecutionResult(
                success = false,
                message = validation.errorMessage ?: "فایل پشتیبان انتخاب‌شده نامعتبر است."
            )
        }

        // Step 2: Safety snapshot creation
        var safetySaved = false
        if (createSafetyBackup && context != null) {
            val snap = createLocalBackupSnapshot(context, isSafetySnapshot = true)
            safetySaved = (snap != null)
        }

        // Step 3: Atomic Transactional Database Restore
        try {
            val root = JSONObject(jsonStr)

            db.withTransaction {
                // Clear existing tables in correct order respecting Foreign Keys
                db.openHelper.writableDatabase.execSQL("DELETE FROM timer_sessions;")
                db.openHelper.writableDatabase.execSQL("DELETE FROM payments;")
                db.openHelper.writableDatabase.execSQL("DELETE FROM project_revisions;")
                db.openHelper.writableDatabase.execSQL("DELETE FROM project_clips;")
                db.openHelper.writableDatabase.execSQL("DELETE FROM projects;")
                db.openHelper.writableDatabase.execSQL("DELETE FROM default_clips;")
                db.openHelper.writableDatabase.execSQL("DELETE FROM studios;")

                // 1. Restore App Config
                if (root.has("app_config")) {
                    val cfgObj = root.getJSONObject("app_config")
                    val cfg = AppConfigEntity(
                        id = 1,
                        dailyQuotaHours = cfgObj.optDouble("dailyQuotaHours", 8.0),
                        gracePeriodSeconds = cfgObj.optInt("gracePeriodSeconds", 15),
                        defaultDeadlineDays = cfgObj.optInt("defaultDeadlineDays", 7),
                        soundAlertsEnabled = cfgObj.optBoolean("soundAlertsEnabled", true),
                        autoStart = cfgObj.optBoolean("autoStart", false),
                        autoBackupOnExit = cfgObj.optBoolean("autoBackupOnExit", true),
                        lastBackupDate = cfgObj.optString("lastBackupDate", PersianUtils.getCurrentJalaliDate()),
                        invoiceBrandTitle = cfgObj.optString("invoiceBrandTitle", "استودیو فیلم و تدوین"),
                        invoiceBankCard = cfgObj.optString("invoiceBankCard", "۶۰۳۷-۹۹۷۹-۰۰۰۰-۰۰۰۰"),
                        invoiceBankOwner = cfgObj.optString("invoiceBankOwner", "تدوینگر گرامی"),
                        invoiceLogoPath = cfgObj.optString("invoiceLogoPath", ""),
                        invoiceSignaturePath = cfgObj.optString("invoiceSignaturePath", ""),
                        invoiceFooterNote = cfgObj.optString("invoiceFooterNote", "با تشکر از همکاری شما."),
                        quickPricesJson = cfgObj.optString("quickPricesJson", "[500000, 1000000, 2000000, 3000000, 5000000, 10000000]"),
                        packagesJson = cfgObj.optString("packagesJson", "[]"),
                        lastProjectCodeSequencesJson = cfgObj.optString("lastProjectCodeSequencesJson", "{}")
                    )
                    db.appConfigDao().saveConfig(cfg)
                }

                // 2. Restore Studios
                if (root.has("studios")) {
                    val arr = root.getJSONArray("studios")
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        val sName = obj.optString("name").trim()
                        val sId = obj.optInt("id", 0)
                        if (sName.isNotBlank()) {
                            db.studioDao().insertStudio(StudioEntity(id = if (sId > 0) sId else 0, name = sName))
                        }
                    }
                }

                // 3. Restore Default Clips
                if (root.has("default_clips")) {
                    val arr = root.getJSONArray("default_clips")
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        val cName = obj.optString("name").trim()
                        val cId = obj.optInt("id", 0)
                        if (cName.isNotBlank()) {
                            db.defaultClipDao().insertDefaultClip(DefaultClipEntity(id = if (cId > 0) cId else 0, name = cName))
                        }
                    }
                }

                // 4. Restore Projects
                if (root.has("projects")) {
                    val arr = root.getJSONArray("projects")
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        val proj = ProjectEntity(
                            id = obj.optInt("id", 0),
                            projectCode = obj.optString("projectCode", ""),
                            name = obj.optString("name", "پروژه"),
                            studioName = obj.optString("studioName", "آتلیه"),
                            price = obj.optDouble("price", 0.0),
                            status = obj.optString("status", ProjectEntity.STATUS_EDITING),
                            isSettled = obj.optInt("isSettled", 0),
                            createdAt = obj.optString("createdAt", PersianUtils.getCurrentJalaliDate()),
                            deadlineDate = obj.optString("deadlineDate", "").ifBlank { null },
                            weddingDate = obj.optString("weddingDate", "").ifBlank { null },
                            deliveredAt = obj.optString("deliveredAt", "").ifBlank { null }
                        )
                        db.projectDao().insertProject(proj)
                    }
                }

                // 5. Restore Project Clips
                if (root.has("project_clips")) {
                    val arr = root.getJSONArray("project_clips")
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        val pc = ProjectClipEntity(
                            id = obj.optInt("id", 0),
                            projectId = obj.optInt("projectId", 0),
                            clipName = obj.optString("clipName", "کلیپ"),
                            isDone = obj.optInt("isDone", 0),
                            estimateMins = obj.optInt("estimateMins", 60),
                            endDate = obj.optString("endDate", "").ifBlank { null }
                        )
                        db.projectClipDao().insertClip(pc)
                    }
                }

                // 6. Restore Payments
                if (root.has("payments")) {
                    val arr = root.getJSONArray("payments")
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        val py = PaymentEntity(
                            id = obj.optInt("id", 0),
                            projectId = obj.optInt("projectId", 0),
                            amount = obj.optDouble("amount", 0.0),
                            date = obj.optString("date", PersianUtils.getCurrentJalaliDate()),
                            note = obj.optString("note", "").ifBlank { null }
                        )
                        db.paymentDao().insertPayment(py)
                    }
                }

                // 7. Restore Revisions
                if (root.has("project_revisions")) {
                    val arr = root.getJSONArray("project_revisions")
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        val rv = ProjectRevisionEntity(
                            id = obj.optInt("id", 0),
                            projectId = obj.optInt("projectId", 0),
                            description = obj.optString("description", ""),
                            isApplied = obj.optInt("isApplied", 0),
                            phaseNum = obj.optInt("phaseNum", 1)
                        )
                        db.projectRevisionDao().insertRevision(rv)
                    }
                }

                // 8. Restore Timer Sessions
                if (root.has("timer_sessions")) {
                    val arr = root.getJSONArray("timer_sessions")
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        val pId = if (obj.isNull("projectId")) null else obj.optInt("projectId").takeIf { it > 0 }
                        val ss = TimerSessionEntity(
                            id = obj.optInt("id", 0),
                            projectId = pId,
                            clipName = obj.optString("clipName", "کار متفرقه"),
                            category = obj.optString("category", "General"),
                            startTime = obj.optString("startTime", "00:00:00"),
                            endTime = obj.optString("endTime", "00:00:00"),
                            durationSeconds = obj.optLong("durationSeconds", 0L),
                            note = obj.optString("note", "").ifBlank { null },
                            date = obj.optString("date", PersianUtils.getCurrentJalaliDate())
                        )
                        db.timerSessionDao().insertSession(ss)
                    }
                }
            }

            // Post-migration check
            ensureProjectCodesMigrated()

            RestoreExecutionResult(
                success = true,
                message = "اطلاعات با موفقیت و به صورت کامل بازیابی شد.",
                metadata = validation.metadata,
                restoredProjectsCount = validation.metadata.projectCount,
                restoredPaymentsCount = validation.metadata.paymentCount,
                restoredSessionsCount = validation.metadata.sessionCount,
                safetyBackupSaved = safetySaved
            )
        } catch (e: Exception) {
            RestoreExecutionResult(
                success = false,
                message = "خطای غیرمنتظره در حین بازیابی پایگاه داده: ${e.localizedMessage ?: "تراکنش لغو شد"}",
                safetyBackupSaved = safetySaved
            )
        }
    }

    // =============================================================================================
    // GRANULAR & HARD RESET OPERATIONS (مرکز بازنشانی)
    // =============================================================================================

    /**
     * 1. Reset Projects & Work History:
     * Removes all projects, project clips, revisions, and timer sessions.
     * Retains: Studios, default clips, financial accounts branding, and config.
     */
    suspend fun resetProjectsOnly(): ResetExecutionResult = withContext(Dispatchers.IO) {
        val projCount = db.projectDao().getAllProjectsSync().size
        db.withTransaction {
            db.openHelper.writableDatabase.execSQL("DELETE FROM timer_sessions;")
            db.openHelper.writableDatabase.execSQL("DELETE FROM payments;")
            db.openHelper.writableDatabase.execSQL("DELETE FROM project_revisions;")
            db.openHelper.writableDatabase.execSQL("DELETE FROM project_clips;")
            db.openHelper.writableDatabase.execSQL("DELETE FROM projects;")
        }
        ResetExecutionResult(
            success = true,
            type = "PROJECTS",
            title = "پاک‌سازی پروژه‌ها و کارکرد",
            description = "تمام پروژه‌ها، کلیپ‌ها، اصلاحیه‌ها و سوابق زمانی حذف شدند.",
            timestamp = PersianUtils.getCurrentJalaliDate(),
            itemsAffectedCount = projCount
        )
    }

    /**
     * 2. Reset Financial History:
     * Removes all payment records and resets is_settled = 0 on projects.
     * Retains: Projects, clips, sessions, studios, config.
     */
    suspend fun resetFinanceOnly(): ResetExecutionResult = withContext(Dispatchers.IO) {
        val payCount = db.paymentDao().getAllPaymentsSync().size
        db.withTransaction {
            db.openHelper.writableDatabase.execSQL("DELETE FROM payments;")
            db.openHelper.writableDatabase.execSQL("UPDATE projects SET is_settled = 0 WHERE price > 0;")
        }
        ResetExecutionResult(
            success = true,
            type = "FINANCE",
            title = "پاک‌سازی سوابق مالی و تراکنش‌ها",
            description = "تمام پرداختی‌ها و فیش‌های ثبت‌شده تصفیه شدند و وضعیت پروژه‌ها به بدهکار تغییر یافت.",
            timestamp = PersianUtils.getCurrentJalaliDate(),
            itemsAffectedCount = payCount
        )
    }

    /**
     * 3. Reset Settings:
     * Resets AppConfig to standard defaults.
     * Retains: All projects, financial records, sessions, studios.
     */
    suspend fun resetSettingsOnly(): ResetExecutionResult = withContext(Dispatchers.IO) {
        saveConfig(AppConfigEntity())
        ResetExecutionResult(
            success = true,
            type = "SETTINGS",
            title = "بازنشانی تنظیمات به پیش‌فرض",
            description = "تنظیمات برندینگ فاکتور، هدف روزانه، قیمت‌های سریع و پکیج‌ها به حالت کارخانه‌ای برگشتند.",
            timestamp = PersianUtils.getCurrentJalaliDate(),
            itemsAffectedCount = 1
        )
    }

    /**
     * 4. Reset Base Data:
     * Removes custom studios and default clips, re-seeding standard defaults.
     * Retains: Active projects and finances.
     */
    suspend fun resetBaseDataOnly(): ResetExecutionResult = withContext(Dispatchers.IO) {
        val stCount = db.studioDao().getAllStudiosSync().size
        val clCount = db.defaultClipDao().getAllDefaultClipsSync().size
        db.withTransaction {
            db.openHelper.writableDatabase.execSQL("DELETE FROM default_clips;")
            db.openHelper.writableDatabase.execSQL("DELETE FROM studios;")

            // Seed clean defaults
            listOf("استودیو نمونه", "آتلیه عروس").forEach {
                db.studioDao().insertStudio(StudioEntity(name = it))
            }
            listOf("کلیپ اصلی", "کلیپ فرمالیته", "تیزر اینستاگرام", "سرمجلس").forEach {
                db.defaultClipDao().insertDefaultClip(DefaultClipEntity(name = it))
            }
        }
        ResetExecutionResult(
            success = true,
            type = "BASE",
            title = "بازنشانی آتلیه‌ها و کلیپ‌های مرجع",
            description = "فهرست استودیوها و کلیپ‌های پیش‌فرض به عناوین استاندارد اولیه برگشتند.",
            timestamp = PersianUtils.getCurrentJalaliDate(),
            itemsAffectedCount = stCount + clCount
        )
    }

    /**
     * 5. Factory Hard Reset (خام‌سازی کامل نرم‌افزار):
     * Wipes all database tables, re-initializes clean default config and baseline entries.
     */
    suspend fun resetAllData(): ResetExecutionResult = withContext(Dispatchers.IO) {
        db.withTransaction {
            db.clearAllTables()
            saveConfig(AppConfigEntity())
            listOf("استودیو نور", "آتلیه مایا", "استودیو رویال").forEach {
                db.studioDao().insertStudio(StudioEntity(name = it))
            }
            listOf("کلیپ سینمایی اصلی", "کلیپ فرمالیته / شمال", "تیزر اینستاگرام", "سرمجلس و گیفت").forEach {
                db.defaultClipDao().insertDefaultClip(DefaultClipEntity(name = it))
            }
        }
        ResetExecutionResult(
            success = true,
            type = "ALL",
            title = "خام‌سازی و بازنشانی کامل نرم‌افزار",
            description = "تمام پایگاه داده، پروژه‌ها، حسابداری و تنظیمات پاک شدند و نرم‌افزار به حالت اولیه برگشت.",
            timestamp = PersianUtils.getCurrentJalaliDate(),
            itemsAffectedCount = 100
        )
    }
}
