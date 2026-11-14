package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fullName: String,
    val mobile: String,
    val email: String,
    val password: String,
    val role: String = "STUDENT", // "STUDENT" or "ADMIN"
    val studentCode: String,
    val avatarUri: String? = null,
    val isBlocked: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val subtitle: String,
    val category: String, // "ANM & GNM", "RRB NTPC", "Foundation"
    val priceInr: Int,
    val originalPriceInr: Int,
    val description: String,
    val featuresCsv: String,
    val bannerKey: String, // "anm_gnm", "rrb_ntpc", "hero"
    val instructor: String,
    val validityDays: Int = 365,
    val isPublished: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "enrollments")
data class EnrollmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val courseId: Long,
    val purchasedAt: Long,
    val expiryAt: Long,
    val progressPercent: Int = 0,
    val transactionId: String,
    val amountPaid: Int
)

@Entity(tableName = "payments")
data class PaymentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val userName: String,
    val courseId: Long,
    val courseTitle: String,
    val amountInr: Int,
    val method: String, // "UPI", "Debit Card", "Credit Card", "Net Banking"
    val status: String, // "PENDING", "SUCCESS", "FAILED"
    val transactionId: String,
    val verifiedByAdmin: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "live_classes")
data class LiveClassEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val courseId: Long,
    val courseTitle: String,
    val title: String,
    val subject: String,
    val teacherName: String,
    val scheduledAtMillis: Long,
    val durationMinutes: Int,
    val status: String, // "LIVE", "UPCOMING", "COMPLETED"
    val streamUrl: String,
    val reminderEnabled: Boolean = false
)

@Entity(tableName = "recorded_classes")
data class RecordedClassEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val courseId: Long,
    val courseTitle: String,
    val chapterName: String,
    val title: String,
    val teacherName: String,
    val durationSeconds: Int,
    val watchedSeconds: Int = 0,
    val videoUrl: String,
    val isCompleted: Boolean = false
)

@Entity(tableName = "pdf_notes")
data class PdfNoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val courseId: Long,
    val courseTitle: String,
    val subject: String,
    val chapter: String,
    val title: String,
    val pageCount: Int,
    val fileSizeLabel: String,
    val contentBody: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "mock_tests")
data class MockTestEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val courseId: Long,
    val courseTitle: String,
    val title: String,
    val subject: String,
    val durationMinutes: Int,
    val totalMarks: Int,
    val negativeMarksPerWrong: Double = 0.25,
    val isPublished: Boolean = true
)

@Entity(tableName = "mock_questions")
data class MockQuestionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val testId: Long,
    val questionText: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctOptionIndex: Int, // 0..3
    val explanation: String
)

@Entity(tableName = "mock_attempts")
data class MockAttemptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val testId: Long,
    val testTitle: String,
    val totalQuestions: Int,
    val correctCount: Int,
    val wrongCount: Int,
    val unansweredCount: Int,
    val scoreMarks: Double,
    val maxMarks: Int,
    val timeTakenSeconds: Int,
    val attemptedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "mock_papers")
data class MockPaperEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val courseId: Long,
    val courseTitle: String,
    val title: String,
    val paperType: String, // "Previous Year", "Practice Set", "Model Paper"
    val yearLabel: String,
    val questionsSummary: String,
    val answerKeyText: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "announcements")
data class AnnouncementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val message: String,
    val isTicker: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long = 0L, // 0L = broadcast to all students
    val type: String, // "LIVE_CLASS", "REMINDER", "VIDEO", "PDF", "MOCK_TEST", "PAYMENT", "ANNOUNCEMENT"
    val title: String,
    val body: String,
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "contact_queries")
data class ContactQueryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val name: String,
    val mobile: String,
    val email: String,
    val subject: String,
    val message: String,
    val status: String = "OPEN", // "OPEN", "RESOLVED"
    val adminReply: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val supportPhone: String = "+91 98765 43210",
    val whatsappNumber: String = "+91 98765 43210",
    val supportEmail: String = "support@targetssb.in",
    val tickerHeadline: String = "🎯 TARGET SSB – ANM & GNM | RRB NTPC | Live Classes | Mock Tests | Online Learning",
    val certificatesEnabled: Boolean = true
)
