package com.example.ui.viewmodel

import android.app.Application
import android.media.AudioManager
import android.media.ToneGenerator
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.entity.AppConfigEntity
import com.example.data.entity.DefaultClipEntity
import com.example.data.entity.PaymentEntity
import com.example.data.entity.ProjectClipEntity
import com.example.data.entity.ProjectEntity
import com.example.data.entity.ProjectRevisionEntity
import com.example.data.entity.StudioEntity
import com.example.data.entity.TimerSessionEntity
import com.example.data.repository.CutterLogRepository
import com.example.util.PersianUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CutterLogRepository
    init {
        val db = AppDatabase.getDatabase(application)
        repository = CutterLogRepository(db)
        viewModelScope.launch {
            repository.ensureProjectCodesMigrated()
        }
    }

    // Active Navigation Tab Index (0: Workspace, 1: Archive, 2: Finance, 3: Timer, 4: Pilot AI, 5: Settings)
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    fun setSelectedTab(index: Int) {
        _selectedTab.value = index
    }

    // Database Flows
    val studios: StateFlow<List<StudioEntity>> = repository.studiosFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val defaultClips: StateFlow<List<DefaultClipEntity>> = repository.defaultClipsFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val allProjects: StateFlow<List<ProjectEntity>> = repository.allProjectsFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val allClips: StateFlow<List<ProjectClipEntity>> = repository.allClipsFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val allPayments: StateFlow<List<PaymentEntity>> = repository.allPaymentsFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val allSessions: StateFlow<List<TimerSessionEntity>> = repository.allSessionsFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val allRevisions: StateFlow<List<ProjectRevisionEntity>> = repository.allRevisionsFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val appConfig: StateFlow<AppConfigEntity?> = repository.appConfigFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), null
    )

    // Workspace State
    val workspaceSearch = MutableStateFlow("")
    val workspaceStudioFilter = MutableStateFlow<String?>(null)
    val workspaceStatusFilter = MutableStateFlow("ALL") // "ALL", "EDITING", "NEAR_DONE"
    val selectedProjectId = MutableStateFlow<Int?>(null)

    // Selected Project Clips
    val selectedProjectClips: StateFlow<List<ProjectClipEntity>> = combine(
        selectedProjectId, allClips
    ) { pId, clips ->
        if (pId == null) emptyList() else clips.filter { it.projectId == pId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected Project Payments
    val selectedProjectPayments: StateFlow<List<PaymentEntity>> = combine(
        selectedProjectId, allPayments
    ) { pId, payments ->
        if (pId == null) emptyList() else payments.filter { it.projectId == pId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected Project Revisions
    val selectedProjectRevisions: StateFlow<List<ProjectRevisionEntity>> = combine(
        selectedProjectId, allRevisions
    ) { pId, revs ->
        if (pId == null) emptyList() else revs.filter { it.projectId == pId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectProject(projectId: Int?) {
        selectedProjectId.value = projectId
    }

    // New Project Form
    fun createProject(
        name: String,
        studioName: String,
        price: Double,
        selectedClips: List<String>,
        deadlineDays: Int,
        weddingDate: String? = null
    ) {
        viewModelScope.launch {
            val newId = repository.createProjectWithClips(
                name = name,
                studioName = studioName,
                price = price,
                clipNames = selectedClips,
                deadlineDays = deadlineDays,
                weddingDate = weddingDate
            )
            selectProject(newId.toInt())
        }
    }

    fun updateProject(project: ProjectEntity) {
        viewModelScope.launch {
            repository.updateProject(project)
        }
    }

    fun toggleClipDone(clip: ProjectClipEntity) {
        viewModelScope.launch {
            repository.toggleClipDone(clip)
        }
    }

    fun addPayment(projectId: Int, amount: Double, note: String?) {
        viewModelScope.launch {
            repository.addPayment(projectId, amount, note)
        }
    }

    fun settleProjectFully(projectId: Int) {
        viewModelScope.launch {
            repository.settleProjectFully(projectId)
        }
    }

    fun addRevision(projectId: Int, description: String, phaseNum: Int) {
        viewModelScope.launch {
            repository.addRevision(projectId, description, phaseNum)
        }
    }

    fun toggleRevisionApplied(revision: ProjectRevisionEntity) {
        viewModelScope.launch {
            repository.toggleRevisionApplied(revision)
        }
    }

    fun deleteRevision(revision: ProjectRevisionEntity) {
        viewModelScope.launch {
            repository.deleteRevision(revision)
        }
    }

    fun markRoundComplete(projectId: Int, phaseNum: Int) {
        viewModelScope.launch {
            val revisions = repository.getRevisionsForProjectSync(projectId)
            revisions.filter { it.phaseNum == phaseNum && it.isApplied == 0 }.forEach { rev ->
                repository.toggleRevisionApplied(rev)
            }
        }
    }

    fun deleteProject(project: ProjectEntity) {
        viewModelScope.launch {
            repository.deleteProject(project)
            if (selectedProjectId.value == project.id) {
                selectProject(null)
            }
        }
    }

    fun reopenProjectFromArchive(projectId: Int) {
        viewModelScope.launch {
            repository.reopenProjectFromArchive(projectId)
        }
    }

    // Finance Bulk Payment
    fun submitBulkPayment(studioName: String, amount: Double, note: String?) {
        viewModelScope.launch {
            repository.submitBulkPaymentForStudio(studioName, amount, note)
        }
    }

    // TIMER STATE & LOGIC
    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

    // Pure elapsed seconds of the current active work session
    private val _activeSessionSeconds = MutableStateFlow(0L)
    val activeSessionSeconds: StateFlow<Long> = _activeSessionSeconds.asStateFlow()
    val timerSeconds: StateFlow<Long> = _activeSessionSeconds.asStateFlow()

    private val _timerTargetSeconds = MutableStateFlow<Long?>(null) // null = free, 2700 = 45m, 5400 = 90m
    val timerTargetSeconds: StateFlow<Long?> = _timerTargetSeconds.asStateFlow()

    private val _isNleActive = MutableStateFlow(true)
    val isNleActive: StateFlow<Boolean> = _isNleActive.asStateFlow()

    private val _isGracePeriodActive = MutableStateFlow(false)
    val isGracePeriodActive: StateFlow<Boolean> = _isGracePeriodActive.asStateFlow()

    private val _gracePeriodRemaining = MutableStateFlow(15)
    val gracePeriodRemaining: StateFlow<Int> = _gracePeriodRemaining.asStateFlow()

    val timerSelectedProjectId = MutableStateFlow<Int?>(null)
    val timerSelectedClip = MutableStateFlow("کلیپ اصلی")
    val timerCategory = MutableStateFlow("تدوین / رافکات")
    val timerNote = MutableStateFlow("")

    // Map to preserve uncommitted active session time when switching clips/projects
    // Key: "projectId_clipName", Value: Triple(accumulatedSeconds, startTimeStr, targetSeconds)
    private val pendingClipSessions = mutableMapOf<String, Triple<Long, String, Long?>>()

    private var timerJob: Job? = null
    private var startTimeStr: String = ""

    private fun getTargetKey(projId: Int?, clipName: String): String = "${projId ?: -1}_$clipName"

    fun setTimerTarget(projectId: Int?, clipName: String) {
        val currentKey = getTargetKey(timerSelectedProjectId.value, timerSelectedClip.value)
        val newKey = getTargetKey(projectId, clipName)

        if (currentKey == newKey) {
            timerSelectedProjectId.value = projectId
            timerSelectedClip.value = clipName
            return
        }

        // 1. If currently timer is running or has uncommitted elapsed seconds, pause and store for current target
        if (_isTimerRunning.value) {
            pauseTimer()
        }
        if (_activeSessionSeconds.value > 0L) {
            pendingClipSessions[currentKey] = Triple(_activeSessionSeconds.value, startTimeStr, _timerTargetSeconds.value)
        } else {
            pendingClipSessions.remove(currentKey)
        }

        // 2. Switch to new target
        timerSelectedProjectId.value = projectId
        timerSelectedClip.value = clipName

        // 3. Restore any previously pending uncommitted session for the new target
        val pending = pendingClipSessions.remove(newKey)
        if (pending != null) {
            _activeSessionSeconds.value = pending.first
            startTimeStr = pending.second
            _timerTargetSeconds.value = pending.third
        } else {
            _activeSessionSeconds.value = 0L
            startTimeStr = ""
            _timerTargetSeconds.value = null
        }
    }

    fun startTimer(targetSecs: Long? = null) {
        if (_isTimerRunning.value) return
        _timerTargetSeconds.value = targetSecs
        _isTimerRunning.value = true
        _isGracePeriodActive.value = false
        if (startTimeStr.isEmpty()) {
            startTimeStr = PersianUtils.formatSecondsToHMS(System.currentTimeMillis() / 1000 % 86400)
        }

        timerJob = viewModelScope.launch {
            while (_isTimerRunning.value) {
                delay(1000)
                _activeSessionSeconds.value += 1
                if (_timerTargetSeconds.value != null && _activeSessionSeconds.value >= _timerTargetSeconds.value!!) {
                    pauseTimerWithAlert()
                    break
                }
            }
        }
    }

    fun pauseTimer() {
        _isTimerRunning.value = false
        timerJob?.cancel()
    }

    fun discardActiveSession() {
        _activeSessionSeconds.value = 0L
        _isTimerRunning.value = false
        startTimeStr = ""
        timerJob?.cancel()
        pendingClipSessions.remove(getTargetKey(timerSelectedProjectId.value, timerSelectedClip.value))
    }

    private fun pauseTimerWithAlert() {
        pauseTimer()
        playBeepAlert()
    }

    fun stopAndSaveTimerSession(projectId: Int?, clipName: String? = null, category: String? = null) {
        val sessionDuration = _activeSessionSeconds.value
        val actualClipName = clipName ?: timerSelectedClip.value
        if (sessionDuration > 0) {
            val endTimeStr = PersianUtils.formatSecondsToHMS(System.currentTimeMillis() / 1000 % 86400)
            viewModelScope.launch {
                repository.saveTimerSession(
                    projectId = projectId,
                    clipName = actualClipName,
                    category = category ?: timerCategory.value,
                    startTime = startTimeStr.ifEmpty { "00:00:00" },
                    endTime = endTimeStr,
                    durationSeconds = sessionDuration,
                    note = timerNote.value.ifBlank { null }
                )
                _activeSessionSeconds.value = 0L
                _isTimerRunning.value = false
                startTimeStr = ""
                timerJob?.cancel()
                pendingClipSessions.remove(getTargetKey(projectId, actualClipName))
            }
        }
    }

    fun stopAndSaveTimerSessionWithClipComplete(
        projectId: Int?,
        clipName: String,
        category: String,
        note: String?,
        isClipCompleted: Boolean
    ) {
        val sessionDuration = _activeSessionSeconds.value
        val endTimeStr = PersianUtils.formatSecondsToHMS(System.currentTimeMillis() / 1000 % 86400)
        viewModelScope.launch {
            if (sessionDuration > 0) {
                repository.saveTimerSession(
                    projectId = projectId,
                    clipName = clipName,
                    category = category,
                    startTime = startTimeStr.ifEmpty { "00:00:00" },
                    endTime = endTimeStr,
                    durationSeconds = sessionDuration,
                    note = note?.ifBlank { null }
                )
            }
            if (projectId != null && isClipCompleted) {
                repository.markClipCompletedInProject(projectId, clipName)
            }
            _activeSessionSeconds.value = 0L
            _isTimerRunning.value = false
            startTimeStr = ""
            timerJob?.cancel()
            pendingClipSessions.remove(getTargetKey(projectId, clipName))
        }
    }

    fun updateTimerSession(session: TimerSessionEntity) {
        viewModelScope.launch { repository.updateTimerSession(session) }
    }

    fun deleteTimerSession(session: TimerSessionEntity) {
        viewModelScope.launch { repository.deleteTimerSession(session) }
    }

    fun toggleNleStatus(active: Boolean) {
        _isNleActive.value = active
        if (!active && _isTimerRunning.value) {
            triggerGracePeriod()
        } else if (active) {
            _isGracePeriodActive.value = false
        }
    }

    private fun triggerGracePeriod() {
        val graceSecs = appConfig.value?.gracePeriodSeconds ?: 15
        _gracePeriodRemaining.value = graceSecs
        _isGracePeriodActive.value = true

        viewModelScope.launch {
            var left = graceSecs
            while (left > 0 && !_isNleActive.value && _isGracePeriodActive.value) {
                delay(1000)
                left--
                _gracePeriodRemaining.value = left
            }
            if (left <= 0 && !_isNleActive.value && _isGracePeriodActive.value) {
                pauseTimerWithAlert()
            }
        }
    }

    fun playBeepAlert() {
        if (appConfig.value?.soundAlertsEnabled != false) {
            try {
                val toneG = ToneGenerator(AudioManager.STREAM_ALARM, 100)
                toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 500)
            } catch (e: Exception) {
                // Ignore audio failure
            }
        }
    }

    // Settings actions
    fun addStudio(name: String) {
        viewModelScope.launch { repository.addStudio(name) }
    }

    fun updateStudio(studio: StudioEntity) {
        viewModelScope.launch { repository.updateStudio(studio) }
    }

    fun deleteStudio(studio: StudioEntity) {
        viewModelScope.launch { repository.deleteStudio(studio) }
    }

    fun addDefaultClip(name: String) {
        viewModelScope.launch { repository.addDefaultClip(name) }
    }

    fun updateDefaultClip(clip: DefaultClipEntity) {
        viewModelScope.launch { repository.updateDefaultClip(clip) }
    }

    fun deleteDefaultClip(clip: DefaultClipEntity) {
        viewModelScope.launch { repository.deleteDefaultClip(clip) }
    }

    fun saveConfig(config: AppConfigEntity) {
        viewModelScope.launch { repository.saveConfig(config) }
    }

    fun backupDataToJson(onResult: (String) -> Unit) {
        viewModelScope.launch {
            val json = repository.backupDataToJson()
            onResult(json)
        }
    }

    fun restoreDataFromJson(jsonStr: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.restoreDataFromJson(jsonStr)
            onResult(success)
        }
    }

    fun resetProjectsOnly() {
        viewModelScope.launch { repository.resetProjectsOnly() }
    }

    fun resetFinanceOnly() {
        viewModelScope.launch { repository.resetFinanceOnly() }
    }

    fun resetSettingsOnly() {
        viewModelScope.launch { repository.resetSettingsOnly() }
    }

    fun resetBaseDataOnly() {
        viewModelScope.launch { repository.resetBaseDataOnly() }
    }

    fun resetData() {
        viewModelScope.launch { repository.resetAllData() }
    }
}
