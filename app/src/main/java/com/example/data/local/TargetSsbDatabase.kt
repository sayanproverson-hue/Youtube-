package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserEntity::class,
        CourseEntity::class,
        EnrollmentEntity::class,
        PaymentEntity::class,
        LiveClassEntity::class,
        RecordedClassEntity::class,
        PdfNoteEntity::class,
        MockTestEntity::class,
        MockQuestionEntity::class,
        MockAttemptEntity::class,
        MockPaperEntity::class,
        AnnouncementEntity::class,
        NotificationEntity::class,
        ContactQueryEntity::class,
        AppSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class TargetSsbDatabase : RoomDatabase() {
    abstract fun dao(): TargetSsbDao

    companion object {
        @Volatile
        private var INSTANCE: TargetSsbDatabase? = null

        fun getInstance(context: Context): TargetSsbDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TargetSsbDatabase::class.java,
                    "target_ssb_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
