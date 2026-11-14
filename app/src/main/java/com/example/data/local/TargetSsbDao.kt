package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TargetSsbDao {

    // Users
    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    fun observeAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun observeUserById(id: Long): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Long): UserEntity?

    @Query("SELECT * FROM users WHERE (email = :identifier OR mobile = :identifier) LIMIT 1")
    suspend fun findUserByIdentifier(identifier: String): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun findUserByEmail(email: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    // Courses
    @Query("SELECT * FROM courses ORDER BY id ASC")
    fun observeCourses(): Flow<List<CourseEntity>>

    @Query("SELECT COUNT(*) FROM courses")
    suspend fun countCourses(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: CourseEntity): Long

    @Update
    suspend fun updateCourse(course: CourseEntity)

    @Query("DELETE FROM courses WHERE id = :courseId")
    suspend fun deleteCourse(courseId: Long)

    // Enrollments
    @Query("SELECT * FROM enrollments WHERE userId = :userId ORDER BY purchasedAt DESC")
    fun observeEnrollmentsForUser(userId: Long): Flow<List<EnrollmentEntity>>

    @Query("SELECT * FROM enrollments ORDER BY purchasedAt DESC")
    fun observeAllEnrollments(): Flow<List<EnrollmentEntity>>

    @Query("SELECT * FROM enrollments WHERE userId = :userId AND courseId = :courseId LIMIT 1")
    suspend fun getEnrollment(userId: Long, courseId: Long): EnrollmentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEnrollment(enrollment: EnrollmentEntity): Long

    @Update
    suspend fun updateEnrollment(enrollment: EnrollmentEntity)

    @Query("DELETE FROM enrollments WHERE id = :enrollmentId")
    suspend fun deleteEnrollment(enrollmentId: Long)

    // Payments
    @Query("SELECT * FROM payments WHERE userId = :userId ORDER BY createdAt DESC")
    fun observePaymentsForUser(userId: Long): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments ORDER BY createdAt DESC")
    fun observeAllPayments(): Flow<List<PaymentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentEntity): Long

    @Update
    suspend fun updatePayment(payment: PaymentEntity)

    // Live Classes
    @Query("SELECT * FROM live_classes ORDER BY scheduledAtMillis ASC")
    fun observeLiveClasses(): Flow<List<LiveClassEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLiveClass(liveClass: LiveClassEntity): Long

    @Update
    suspend fun updateLiveClass(liveClass: LiveClassEntity)

    @Query("DELETE FROM live_classes WHERE id = :id")
    suspend fun deleteLiveClass(id: Long)

    // Recorded Classes
    @Query("SELECT * FROM recorded_classes ORDER BY id ASC")
    fun observeRecordedClasses(): Flow<List<RecordedClassEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecordedClass(recordedClass: RecordedClassEntity): Long

    @Update
    suspend fun updateRecordedClass(recordedClass: RecordedClassEntity)

    @Query("DELETE FROM recorded_classes WHERE id = :id")
    suspend fun deleteRecordedClass(id: Long)

    // PDF Notes
    @Query("SELECT * FROM pdf_notes ORDER BY createdAt DESC")
    fun observePdfNotes(): Flow<List<PdfNoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPdfNote(pdfNote: PdfNoteEntity): Long

    @Update
    suspend fun updatePdfNote(pdfNote: PdfNoteEntity)

    @Query("DELETE FROM pdf_notes WHERE id = :id")
    suspend fun deletePdfNote(id: Long)

    // Mock Tests & Questions & Attempts
    @Query("SELECT * FROM mock_tests ORDER BY id ASC")
    fun observeMockTests(): Flow<List<MockTestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMockTest(mockTest: MockTestEntity): Long

    @Update
    suspend fun updateMockTest(mockTest: MockTestEntity)

    @Query("DELETE FROM mock_tests WHERE id = :id")
    suspend fun deleteMockTest(id: Long)

    @Query("SELECT * FROM mock_questions WHERE testId = :testId ORDER BY id ASC")
    suspend fun getQuestionsForTest(testId: Long): List<MockQuestionEntity>

    @Query("SELECT * FROM mock_questions")
    fun observeAllQuestions(): Flow<List<MockQuestionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMockQuestion(question: MockQuestionEntity): Long

    @Query("SELECT * FROM mock_attempts WHERE userId = :userId ORDER BY attemptedAt DESC")
    fun observeAttemptsForUser(userId: Long): Flow<List<MockAttemptEntity>>

    @Query("SELECT * FROM mock_attempts ORDER BY attemptedAt DESC")
    fun observeAllAttempts(): Flow<List<MockAttemptEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMockAttempt(attempt: MockAttemptEntity): Long

    // Mock Test Papers
    @Query("SELECT * FROM mock_papers ORDER BY createdAt DESC")
    fun observeMockPapers(): Flow<List<MockPaperEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMockPaper(paper: MockPaperEntity): Long

    @Query("DELETE FROM mock_papers WHERE id = :id")
    suspend fun deleteMockPaper(id: Long)

    // Announcements
    @Query("SELECT * FROM announcements ORDER BY createdAt DESC")
    fun observeAnnouncements(): Flow<List<AnnouncementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnouncement(announcement: AnnouncementEntity): Long

    @Query("DELETE FROM announcements WHERE id = :id")
    suspend fun deleteAnnouncement(id: Long)

    // Notifications
    @Query("SELECT * FROM notifications WHERE userId = 0 OR userId = :userId ORDER BY createdAt DESC")
    fun observeNotificationsForUser(userId: Long): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity): Long

    @Query("UPDATE notifications SET isRead = 1 WHERE (userId = 0 OR userId = :userId)")
    suspend fun markAllNotificationsRead(userId: Long)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markNotificationRead(id: Long)

    // Contact Queries
    @Query("SELECT * FROM contact_queries ORDER BY createdAt DESC")
    fun observeContactQueries(): Flow<List<ContactQueryEntity>>

    @Query("SELECT * FROM contact_queries WHERE userId = :userId ORDER BY createdAt DESC")
    fun observeContactQueriesForUser(userId: Long): Flow<List<ContactQueryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContactQuery(query: ContactQueryEntity): Long

    @Update
    suspend fun updateContactQuery(query: ContactQueryEntity)

    // App Settings
    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    fun observeAppSettings(): Flow<AppSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAppSettings(settings: AppSettingsEntity)
}
