package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.AppConfigEntity
import com.example.data.entity.DefaultClipEntity
import com.example.data.entity.PaymentEntity
import com.example.data.entity.ProjectClipEntity
import com.example.data.entity.ProjectEntity
import com.example.data.entity.ProjectRevisionEntity
import com.example.data.entity.StudioEntity
import com.example.data.entity.TimerSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StudioDao {
    @Query("SELECT * FROM studios ORDER BY name ASC")
    fun getAllStudios(): Flow<List<StudioEntity>>

    @Query("SELECT * FROM studios ORDER BY name ASC")
    suspend fun getAllStudiosSync(): List<StudioEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudio(studio: StudioEntity): Long

    @Update
    suspend fun updateStudio(studio: StudioEntity)

    @Delete
    suspend fun deleteStudio(studio: StudioEntity)
}

@Dao
interface DefaultClipDao {
    @Query("SELECT * FROM default_clips ORDER BY id ASC")
    fun getAllDefaultClips(): Flow<List<DefaultClipEntity>>

    @Query("SELECT * FROM default_clips ORDER BY id ASC")
    suspend fun getAllDefaultClipsSync(): List<DefaultClipEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDefaultClip(clip: DefaultClipEntity): Long

    @Update
    suspend fun updateDefaultClip(clip: DefaultClipEntity)

    @Delete
    suspend fun deleteDefaultClip(clip: DefaultClipEntity)
}

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY id DESC")
    fun getAllProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects ORDER BY id DESC")
    suspend fun getAllProjectsSync(): List<ProjectEntity>

    @Query("SELECT * FROM projects WHERE id = :id LIMIT 1")
    suspend fun getProjectById(id: Int): ProjectEntity?

    @Query("SELECT * FROM projects WHERE studio_name = :studioName ORDER BY id ASC")
    suspend fun getProjectsByStudioSync(studioName: String): List<ProjectEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity): Long

    @Update
    suspend fun updateProject(project: ProjectEntity)

    @Delete
    suspend fun deleteProject(project: ProjectEntity)

    @Query("DELETE FROM projects WHERE id = :id")
    suspend fun deleteProjectById(id: Int)
}

@Dao
interface ProjectClipDao {
    @Query("SELECT * FROM project_clips WHERE project_id = :projectId ORDER BY id ASC")
    fun getClipsForProject(projectId: Int): Flow<List<ProjectClipEntity>>

    @Query("SELECT * FROM project_clips WHERE project_id = :projectId ORDER BY id ASC")
    suspend fun getClipsForProjectSync(projectId: Int): List<ProjectClipEntity>

    @Query("SELECT * FROM project_clips ORDER BY id ASC")
    fun getAllProjectClips(): Flow<List<ProjectClipEntity>>

    @Query("SELECT * FROM project_clips ORDER BY id ASC")
    suspend fun getAllProjectClipsSync(): List<ProjectClipEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClip(clip: ProjectClipEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClips(clips: List<ProjectClipEntity>)

    @Update
    suspend fun updateClip(clip: ProjectClipEntity)

    @Delete
    suspend fun deleteClip(clip: ProjectClipEntity)
}

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payments WHERE project_id = :projectId ORDER BY id DESC")
    fun getPaymentsForProject(projectId: Int): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE project_id = :projectId")
    suspend fun getPaymentsForProjectSync(projectId: Int): List<PaymentEntity>

    @Query("SELECT * FROM payments ORDER BY id DESC")
    fun getAllPayments(): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments ORDER BY id DESC")
    suspend fun getAllPaymentsSync(): List<PaymentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentEntity): Long

    @Update
    suspend fun updatePayment(payment: PaymentEntity)

    @Delete
    suspend fun deletePayment(payment: PaymentEntity)
}

@Dao
interface ProjectRevisionDao {
    @Query("SELECT * FROM project_revisions WHERE project_id = :projectId ORDER BY phase_num ASC, id ASC")
    fun getRevisionsForProject(projectId: Int): Flow<List<ProjectRevisionEntity>>

    @Query("SELECT * FROM project_revisions WHERE project_id = :projectId ORDER BY phase_num ASC, id ASC")
    suspend fun getRevisionsForProjectSync(projectId: Int): List<ProjectRevisionEntity>

    @Query("SELECT * FROM project_revisions ORDER BY id ASC")
    suspend fun getAllRevisionsSync(): List<ProjectRevisionEntity>

    @Query("SELECT * FROM project_revisions ORDER BY phase_num ASC, id ASC")
    fun getAllRevisions(): Flow<List<ProjectRevisionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRevision(revision: ProjectRevisionEntity): Long

    @Update
    suspend fun updateRevision(revision: ProjectRevisionEntity)

    @Delete
    suspend fun deleteRevision(revision: ProjectRevisionEntity)
}

@Dao
interface TimerSessionDao {
    @Query("SELECT * FROM timer_sessions ORDER BY id DESC")
    fun getAllSessions(): Flow<List<TimerSessionEntity>>

    @Query("SELECT * FROM timer_sessions WHERE date = :dateStr ORDER BY id DESC")
    fun getSessionsForDate(dateStr: String): Flow<List<TimerSessionEntity>>

    @Query("SELECT * FROM timer_sessions WHERE project_id = :projectId ORDER BY id DESC")
    fun getSessionsForProject(projectId: Int): Flow<List<TimerSessionEntity>>

    @Query("SELECT * FROM timer_sessions WHERE project_id = :projectId")
    suspend fun getSessionsForProjectSync(projectId: Int): List<TimerSessionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: TimerSessionEntity): Long

    @Update
    suspend fun updateSession(session: TimerSessionEntity)

    @Delete
    suspend fun deleteSession(session: TimerSessionEntity)
}

@Dao
interface AppConfigDao {
    @Query("SELECT * FROM app_config WHERE id = 1 LIMIT 1")
    fun getConfigFlow(): Flow<AppConfigEntity?>

    @Query("SELECT * FROM app_config WHERE id = 1 LIMIT 1")
    suspend fun getConfigSync(): AppConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveConfig(config: AppConfigEntity)
}
