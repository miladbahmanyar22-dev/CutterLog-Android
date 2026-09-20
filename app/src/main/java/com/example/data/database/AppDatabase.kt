package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
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
    version = 2,
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

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cutterlog_pro.db"
                )
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
