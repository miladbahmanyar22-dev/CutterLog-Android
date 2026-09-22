package com.example.data.repository

import com.example.data.database.AppDatabase
import com.example.data.entity.AppConfigEntity
import com.example.data.entity.DefaultClipEntity
import com.example.data.entity.PaymentEntity
import com.example.data.entity.ProjectClipEntity
import com.example.data.entity.ProjectEntity
import com.example.data.entity.ProjectRevisionEntity
import com.example.data.entity.StudioEntity
import com.example.data.entity.TimerSessionEntity
import com.example.util.PersianUtils
import kotlinx.coroutines.flow.Flow
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

    // Backup and Restore
    suspend fun backupDataToJson(): String {
        val json = org.json.JSONObject()
        json.put("backup_version", 2)
        json.put("created_at", PersianUtils.getCurrentJalaliDate())

        // Config
        val cfg = getConfigSync()
        val cfgObj = org.json.JSONObject().apply {
            put("dailyQuotaHours", cfg.dailyQuotaHours)
            put("soundAlertsEnabled", cfg.soundAlertsEnabled)
            put("invoiceBrandTitle", cfg.invoiceBrandTitle)
            put("invoiceBankCard", cfg.invoiceBankCard)
            put("invoiceBankOwner", cfg.invoiceBankOwner)
            put("invoiceFooterNote", cfg.invoiceFooterNote)
            put("quickPricesJson", cfg.quickPricesJson)
            put("packagesJson", cfg.packagesJson)
        }
        json.put("app_config", cfgObj)

        // Studios
        val studiosArr = org.json.JSONArray()
        val studioList = db.studioDao().getAllStudiosSync()
        studioList.forEach { st ->
            studiosArr.put(org.json.JSONObject().apply {
                put("id", st.id)
                put("name", st.name)
            })
        }
        json.put("studios", studiosArr)

        // Default Clips
        val clipsArr = org.json.JSONArray()
        val clipList = db.defaultClipDao().getAllDefaultClipsSync()
        clipList.forEach { cl ->
            clipsArr.put(org.json.JSONObject().apply {
                put("id", cl.id)
                put("name", cl.name)
            })
        }
        json.put("default_clips", clipsArr)

        // Projects
        val projectsArr = org.json.JSONArray()
        val projectList = db.projectDao().getAllProjectsSync()
        projectList.forEach { pr ->
            projectsArr.put(org.json.JSONObject().apply {
                put("id", pr.id)
                put("projectCode", pr.projectCode)
                put("name", pr.name)
                put("studioName", pr.studioName)
                put("price", pr.price)
                put("status", pr.status)
                put("isSettled", pr.isSettled)
                put("createdAt", pr.createdAt)
                put("deadlineDate", pr.deadlineDate)
                put("weddingDate", pr.weddingDate ?: "")
            })
        }
        json.put("projects", projectsArr)

        // Project Clips
        val pClipsArr = org.json.JSONArray()
        val pClipList = db.projectClipDao().getAllProjectClipsSync()
        pClipList.forEach { pc ->
            pClipsArr.put(org.json.JSONObject().apply {
                put("id", pc.id)
                put("projectId", pc.projectId)
                put("clipName", pc.clipName)
                put("isDone", pc.isDone)
                put("estimateMins", pc.estimateMins)
                put("endDate", pc.endDate ?: "")
            })
        }
        json.put("project_clips", pClipsArr)

        // Payments
        val paymentsArr = org.json.JSONArray()
        val paymentList = db.paymentDao().getAllPaymentsSync()
        paymentList.forEach { py ->
            paymentsArr.put(org.json.JSONObject().apply {
                put("id", py.id)
                put("projectId", py.projectId)
                put("amount", py.amount)
                put("date", py.date)
                put("note", py.note ?: "")
            })
        }
        json.put("payments", paymentsArr)

        // Revisions
        val revsArr = org.json.JSONArray()
        val revList = db.projectRevisionDao().getAllRevisionsSync()
        revList.forEach { rv ->
            revsArr.put(org.json.JSONObject().apply {
                put("id", rv.id)
                put("projectId", rv.projectId)
                put("description", rv.description)
                put("isApplied", rv.isApplied)
                put("phaseNum", rv.phaseNum)
            })
        }
        json.put("project_revisions", revsArr)

        return json.toString(2)
    }

    suspend fun restoreDataFromJson(jsonStr: String): Boolean {
        return try {
            val json = org.json.JSONObject(jsonStr)

            // Step 1: Save App Config
            if (json.has("app_config")) {
                val cfgObj = json.getJSONObject("app_config")
                val cfg = AppConfigEntity(
                    id = 1,
                    dailyQuotaHours = cfgObj.optDouble("dailyQuotaHours", 8.0),
                    soundAlertsEnabled = cfgObj.optBoolean("soundAlertsEnabled", true),
                    invoiceBrandTitle = cfgObj.optString("invoiceBrandTitle", "آتلیه و استودیو تخصصی فیلم و عکس"),
                    invoiceBankCard = cfgObj.optString("invoiceBankCard", "6037-9918-0000-0000"),
                    invoiceBankOwner = cfgObj.optString("invoiceBankOwner", "تدوین‌گر کاترلاگ"),
                    invoiceFooterNote = cfgObj.optString("invoiceFooterNote", "باتشکر از حسن اعتماد و همکاری شما با استودیو."),
                    quickPricesJson = cfgObj.optString("quickPricesJson", "[{\"name\":\"بیعانه اول\",\"amount\":1000000},{\"name\":\"پیش‌پرداخت\",\"amount\":2000000},{\"name\":\"پکیج استاندارد\",\"amount\":5000000},{\"name\":\"تسویه کامل\",\"amount\":10000000}]"),
                    packagesJson = cfgObj.optString("packagesJson", "[]")
                )
                saveConfig(cfg)
            }

            // Step 2: Restore Studios
            if (json.has("studios")) {
                val arr = json.getJSONArray("studios")
                for (i in 0 until arr.length()) {
                    val stObj = arr.getJSONObject(i)
                    val sName = stObj.optString("name")
                    if (sName.isNotBlank()) addStudio(sName)
                }
            }

            // Step 3: Restore Default Clips
            if (json.has("default_clips")) {
                val arr = json.getJSONArray("default_clips")
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val cName = obj.optString("name")
                    if (cName.isNotBlank()) addDefaultClip(cName)
                }
            }

            // Step 4: Restore Projects
            if (json.has("projects")) {
                val arr = json.getJSONArray("projects")
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val proj = ProjectEntity(
                        id = obj.optInt("id", 0),
                        projectCode = obj.optString("projectCode", ""),
                        name = obj.optString("name", "پروژه"),
                        studioName = obj.optString("studioName", "آتلیه"),
                        price = obj.optDouble("price", 0.0),
                        status = obj.optString("status", "EDITING"),
                        isSettled = obj.optInt("isSettled", 0),
                        createdAt = obj.optString("createdAt", PersianUtils.getCurrentJalaliDate()),
                        deadlineDate = obj.optString("deadlineDate", PersianUtils.getCurrentJalaliDate()),
                        weddingDate = obj.optString("weddingDate", "").ifBlank { null }
                    )
                    db.projectDao().insertProject(proj)
                }
            }

            // Step 5: Restore Project Clips
            if (json.has("project_clips")) {
                val arr = json.getJSONArray("project_clips")
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

            // Step 6: Restore Payments
            if (json.has("payments")) {
                val arr = json.getJSONArray("payments")
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

            // Step 7: Restore Revisions
            if (json.has("project_revisions")) {
                val arr = json.getJSONArray("project_revisions")
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

            ensureProjectCodesMigrated()

            true
        } catch (e: Exception) {
            false
        }
    }

    // Specific Reset Operations
    suspend fun resetProjectsOnly() {
        val projects = db.projectDao().getAllProjects()
        // Delete all projects, clips, revisions, sessions
        db.openHelper.writableDatabase.execSQL("DELETE FROM projects;")
        db.openHelper.writableDatabase.execSQL("DELETE FROM project_clips;")
        db.openHelper.writableDatabase.execSQL("DELETE FROM project_revisions;")
        db.openHelper.writableDatabase.execSQL("DELETE FROM timer_sessions;")
    }

    suspend fun resetFinanceOnly() {
        db.openHelper.writableDatabase.execSQL("DELETE FROM payments;")
        // Reset project is_settled to 0 if price > 0
        db.openHelper.writableDatabase.execSQL("UPDATE projects SET is_settled = 0 WHERE price > 0;")
    }

    suspend fun resetSettingsOnly() {
        saveConfig(AppConfigEntity())
    }

    suspend fun resetBaseDataOnly() {
        db.openHelper.writableDatabase.execSQL("DELETE FROM studios;")
        db.openHelper.writableDatabase.execSQL("DELETE FROM default_clips;")
    }

    suspend fun resetAllData() {
        db.clearAllTables()
        saveConfig(AppConfigEntity())
    }
}
