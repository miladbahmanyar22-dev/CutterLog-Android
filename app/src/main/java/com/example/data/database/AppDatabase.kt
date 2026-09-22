package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.migration.Migration
import com.example.data.dao.AppConfigDao
import com.example.data.dao.DefaultClipDao
import com.example.data.dao.PaymentDao
import com.example.data.dao.ProjectClipDao
import com.example.data.dao.ProjectDao
import com.example.data.dao.ProjectRevisionDao
import com.example.data.dao.StudioDao
import com.example.data.dao.TimerSessionDao
import com.example.data.entity.AppConfigEntity
import com.example.data.entity.DefaultClipEntity
import com.example.data.entity.PaymentEntity
import com.example.data.entity.ProjectClipEntity
import com.example.data.entity.ProjectEntity
import com.example.data.entity.ProjectRevisionEntity
import com.example.data.entity.StudioEntity
import com.example.data.entity.TimerSessionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        StudioEntity::class,
        DefaultClipEntity::class,
        ProjectEntity::class,
        ProjectClipEntity::class,
        PaymentEntity::class,
        ProjectRevisionEntity::class,
        TimerSessionEntity::class,
        AppConfigEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun studioDao(): StudioDao
    abstract fun defaultClipDao(): DefaultClipDao
    abstract fun projectDao(): ProjectDao
    abstract fun projectClipDao(): ProjectClipDao
    abstract fun paymentDao(): PaymentDao
    abstract fun projectRevisionDao(): ProjectRevisionDao
    abstract fun timerSessionDao(): TimerSessionDao
    abstract fun appConfigDao(): AppConfigDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Version 1 to 2 migrations if any
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE projects ADD COLUMN project_code TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE app_config ADD COLUMN last_project_code_sequences TEXT NOT NULL DEFAULT '{}'")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_projects_project_code ON projects(project_code)")
            }
        }

        val MIGRATION_1_3 = object : Migration(1, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE projects ADD COLUMN project_code TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE app_config ADD COLUMN last_project_code_sequences TEXT NOT NULL DEFAULT '{}'")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_projects_project_code ON projects(project_code)")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE projects ADD COLUMN delivered_at TEXT")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cutterlog_pro.db"
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_1_3, MIGRATION_3_4)
                .addCallback(DatabaseCallback())
                .fallbackToDestructiveMigration(true)
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateDatabase(database)
                    }
                }
            }

            suspend fun populateDatabase(db: AppDatabase) {
                // Seed Initial App Config
                db.appConfigDao().saveConfig(AppConfigEntity())

                // Seed Default Studios
                val initialStudios = listOf("استودیو هورام", "آتلیه تصویرسازان", "آتلیه لنز برتر", "استودیو سپید")
                initialStudios.forEach { name ->
                    db.studioDao().insertStudio(StudioEntity(name = name))
                }

                // Seed Default Clips
                val initialClips = listOf(
                    "کلیپ اصلی (همگامسازی)",
                    "تیزر اینستاگرام",
                    "سانشاین",
                    "آماده شدن",
                    "میکاپ و گریم",
                    "مراسم عقد",
                    "ورودی و رقص",
                    "فیلم کامل (میکس)"
                )
                initialClips.forEach { name ->
                    db.defaultClipDao().insertDefaultClip(DefaultClipEntity(name = name))
                }
            }
        }
    }
}
