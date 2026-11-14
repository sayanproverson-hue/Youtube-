package com.example.data.repository

import com.example.data.local.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlin.math.roundToInt
import kotlin.random.Random

class TargetSsbRepository(
    private val dao: TargetSsbDao,
    private val prefs: SessionPreferences
) {
    val loggedInUserId: Flow<Long?> = prefs.loggedInUserId
    val darkModeEnabled: Flow<Boolean> = prefs.darkModeEnabled

    val courses: Flow<List<CourseEntity>> = dao.observeCourses()
    val allUsers: Flow<List<UserEntity>> = dao.observeAllUsers()
    val allEnrollments: Flow<List<EnrollmentEntity>> = dao.observeAllEnrollments()
    val allPayments: Flow<List<PaymentEntity>> = dao.observeAllPayments()
    val liveClasses: Flow<List<LiveClassEntity>> = dao.observeLiveClasses()
    val recordedClasses: Flow<List<RecordedClassEntity>> = dao.observeRecordedClasses()
    val pdfNotes: Flow<List<PdfNoteEntity>> = dao.observePdfNotes()
    val mockTests: Flow<List<MockTestEntity>> = dao.observeMockTests()
    val allQuestions: Flow<List<MockQuestionEntity>> = dao.observeAllQuestions()
    val allAttempts: Flow<List<MockAttemptEntity>> = dao.observeAllAttempts()
    val mockPapers: Flow<List<MockPaperEntity>> = dao.observeMockPapers()
    val announcements: Flow<List<AnnouncementEntity>> = dao.observeAnnouncements()
    val allContactQueries: Flow<List<ContactQueryEntity>> = dao.observeContactQueries()
    val appSettings: Flow<AppSettingsEntity?> = dao.observeAppSettings()

    fun observeUser(userId: Long): Flow<UserEntity?> = dao.observeUserById(userId)
    fun observeUserEnrollments(userId: Long): Flow<List<EnrollmentEntity>> = dao.observeEnrollmentsForUser(userId)
    fun observeUserPayments(userId: Long): Flow<List<PaymentEntity>> = dao.observePaymentsForUser(userId)
    fun observeUserAttempts(userId: Long): Flow<List<MockAttemptEntity>> = dao.observeAttemptsForUser(userId)
    fun observeUserNotifications(userId: Long): Flow<List<NotificationEntity>> = dao.observeNotificationsForUser(userId)
    fun observeUserContactQueries(userId: Long): Flow<List<ContactQueryEntity>> = dao.observeContactQueriesForUser(userId)

    suspend fun ensureSeeded() {
        SeedData.ensureSeeded(dao)
    }

    suspend fun setDarkMode(enabled: Boolean) {
        prefs.setDarkMode(enabled)
    }

    // --- Authentication ---

    suspend fun login(identifier: String, password: String, requireAdmin: Boolean = false): Result<UserEntity> {
        val user = dao.findUserByIdentifier(identifier.trim())
            ?: return Result.failure(IllegalArgumentException("No account found with that email or mobile number."))
        if (user.isBlocked) {
            return Result.failure(IllegalStateException("This account has been suspended by Admin. Contact support."))
        }
        if (user.password != password) {
            return Result.failure(IllegalArgumentException("Incorrect password. Please try again."))
        }
        if (requireAdmin && user.role != "ADMIN") {
            return Result.failure(IllegalStateException("Admin privileges required for this portal."))
        }
        prefs.setLoggedInUserId(user.id)
        return Result.success(user)
    }

    suspend fun quickDemoLogin(role: String): Result<UserEntity> {
        val identifier = if (role == "ADMIN") "admin@targetssb.in" else "student@targetssb.in"
        val user = dao.findUserByIdentifier(identifier)
            ?: return Result.failure(IllegalStateException("Demo account unavailable."))
        prefs.setLoggedInUserId(user.id)
        return Result.success(user)
    }

    suspend fun registerStudent(
        fullName: String,
        mobile: String,
        email: String,
        password: String
    ): Result<UserEntity> {
        val existing = dao.findUserByIdentifier(email.trim()) ?: dao.findUserByIdentifier(mobile.trim())
        if (existing != null) {
            return Result.failure(IllegalArgumentException("An account with this email or mobile already exists."))
        }
        val studentCode = "TSSB-2026-${Random.nextInt(1100, 9999)}"
        val newUser = UserEntity(
            fullName = fullName.trim(),
            mobile = mobile.trim(),
            email = email.trim(),
            password = password,
            role = "STUDENT",
            studentCode = studentCode
        )
        val newId = dao.insertUser(newUser)
        val created = dao.getUserById(newId) ?: newUser.copy(id = newId)
        dao.insertNotification(
            NotificationEntity(
                userId = created.id,
                type = "ANNOUNCEMENT",
                title = "Welcome to Target SSB!",
                body = "আপনার সফলতার প্রস্তুতি শুরু হোক Target SSB-এর সাথে। Your Student ID is $studentCode."
            )
        )
        prefs.setLoggedInUserId(created.id)
        return Result.success(created)
    }

    suspend fun resetPasswordByEmailOrMobile(identifier: String, newPassword: String): Result<Unit> {
        val user = dao.findUserByIdentifier(identifier.trim())
            ?: return Result.failure(IllegalArgumentException("No matching account found."))
        dao.updateUser(user.copy(password = newPassword))
        return Result.success(Unit)
    }

    suspend fun updateProfile(userId: Long, fullName: String, mobile: String, avatarUri: String?): Result<Unit> {
        val user = dao.getUserById(userId)
            ?: return Result.failure(IllegalStateException("User not found"))
        dao.updateUser(
            user.copy(
                fullName = fullName.trim(),
                mobile = mobile.trim(),
                avatarUri = avatarUri ?: user.avatarUri
            )
        )
        return Result.success(Unit)
    }

    suspend fun logout() {
        prefs.setLoggedInUserId(null)
    }

    // --- Payment & Course Purchase (Server-Verified Pattern) ---

    suspend fun processCoursePayment(
        user: UserEntity,
        course: CourseEntity,
        paymentMethod: String,
        simulateFailure: Boolean = false
    ): Result<PaymentEntity> {
        val alreadyEnrolled = dao.getEnrollment(user.id, course.id)
        if (alreadyEnrolled != null) {
            return Result.failure(IllegalStateException("You are already enrolled in this course."))
        }

        val txnId = "TXN_TSSB_${Random.nextInt(100000, 999999)}"
        // Step 1: Create PENDING payment record before verification
        val pendingId = dao.insertPayment(
            PaymentEntity(
                userId = user.id,
                userName = user.fullName,
                courseId = course.id,
                courseTitle = course.title,
                amountInr = course.priceInr,
                method = paymentMethod,
                status = "PENDING",
                transactionId = txnId,
                verifiedByAdmin = false
            )
        )

        // Step 2: Simulate payment gateway & server signature verification latency
        delay(950)

        if (simulateFailure) {
            val failedPayment = PaymentEntity(
                id = pendingId,
                userId = user.id,
                userName = user.fullName,
                courseId = course.id,
                courseTitle = course.title,
                amountInr = course.priceInr,
                method = paymentMethod,
                status = "FAILED",
                transactionId = txnId,
                verifiedByAdmin = false
            )
            dao.updatePayment(failedPayment)
            return Result.failure(IllegalStateException("Payment verification failed ($txnId). Course remains locked."))
        }

        // Step 3: Verification succeeded -> Mark SUCCESS and unlock enrollment
        val verifiedPayment = PaymentEntity(
            id = pendingId,
            userId = user.id,
            userName = user.fullName,
            courseId = course.id,
            courseTitle = course.title,
            amountInr = course.priceInr,
            method = paymentMethod,
            status = "SUCCESS",
            transactionId = txnId,
            verifiedByAdmin = true
        )
        dao.updatePayment(verifiedPayment)

        val now = System.currentTimeMillis()
        val expiry = now + course.validityDays.toLong() * 86_400_000L
        dao.insertEnrollment(
            EnrollmentEntity(
                userId = user.id,
                courseId = course.id,
                purchasedAt = now,
                expiryAt = expiry,
                progressPercent = 5,
                transactionId = txnId,
                amountPaid = course.priceInr
            )
        )

        dao.insertNotification(
            NotificationEntity(
                userId = user.id,
                type = "PAYMENT",
                title = "Payment Verified: ₹${course.priceInr}/-",
                body = "${course.title} unlocked! Transaction ID: $txnId"
            )
        )

        return Result.success(verifiedPayment)
    }

    // --- Video Progress & Course Progress ---

    suspend fun updateVideoProgress(userId: Long, video: RecordedClassEntity, watchedSeconds: Int) {
        val clamped = watchedSeconds.coerceIn(0, video.durationSeconds.coerceAtLeast(1))
        val completed = clamped >= (video.durationSeconds * 0.85).toInt()
        dao.updateRecordedClass(
            video.copy(
                watchedSeconds = clamped,
                isCompleted = completed || video.isCompleted
            )
        )
        val enrollment = dao.getEnrollment(userId, video.courseId)
        if (enrollment != null) {
            val nextProgress = (enrollment.progressPercent + if (completed) 15 else 5).coerceAtMost(100)
            dao.updateEnrollment(enrollment.copy(progressPercent = nextProgress))
        }
    }

    suspend fun toggleLiveReminder(liveClass: LiveClassEntity) {
        val next = !liveClass.reminderEnabled
        dao.updateLiveClass(liveClass.copy(reminderEnabled = next))
        if (next) {
            dao.insertNotification(
                NotificationEntity(
                    userId = 0L,
                    type = "REMINDER",
                    title = "Class Reminder Set",
                    body = "We'll remind you before ${liveClass.title} by ${liveClass.teacherName}."
                )
            )
        }
    }

    // --- Mock Tests ---

    suspend fun getQuestionsForTest(testId: Long): List<MockQuestionEntity> = dao.getQuestionsForTest(testId)

    suspend fun submitMockTest(
        userId: Long,
        test: MockTestEntity,
        questions: List<MockQuestionEntity>,
        answers: Map<Long, Int>,
        timeTakenSeconds: Int
    ): MockAttemptEntity {
        var correct = 0
        var wrong = 0
        var unanswered = 0
        questions.forEach { q ->
            val selected = answers[q.id]
            when {
                selected == null -> unanswered++
                selected == q.correctOptionIndex -> correct++
                else -> wrong++
            }
        }
        val marksPerQ = if (questions.isNotEmpty()) test.totalMarks.toDouble() / questions.size else 1.0
        val rawScore = (correct * marksPerQ) - (wrong * test.negativeMarksPerWrong)
        val roundedScore = ((rawScore.coerceAtLeast(0.0)) * 100.0).roundToInt() / 100.0

        val attempt = MockAttemptEntity(
            userId = userId,
            testId = test.id,
            testTitle = test.title,
            totalQuestions = questions.size,
            correctCount = correct,
            wrongCount = wrong,
            unansweredCount = unanswered,
            scoreMarks = roundedScore,
            maxMarks = test.totalMarks,
            timeTakenSeconds = timeTakenSeconds
        )
        val id = dao.insertMockAttempt(attempt)
        return attempt.copy(id = id)
    }

    // --- Notifications & Contact ---

    suspend fun markAllNotificationsRead(userId: Long) {
        dao.markAllNotificationsRead(userId)
    }

    suspend fun markNotificationRead(id: Long) {
        dao.markNotificationRead(id)
    }

    suspend fun submitContactQuery(
        userId: Long,
        name: String,
        mobile: String,
        email: String,
        subject: String,
        message: String
    ) {
        dao.insertContactQuery(
            ContactQueryEntity(
                userId = userId,
                name = name.trim(),
                mobile = mobile.trim(),
                email = email.trim(),
                subject = subject.trim(),
                message = message.trim()
            )
        )
    }

    // --- Admin Management Actions ---

    suspend fun saveCourse(course: CourseEntity) {
        if (course.id == 0L) {
            dao.insertCourse(course)
        } else {
            dao.updateCourse(course)
        }
    }

    suspend fun deleteCourse(courseId: Long) {
        dao.deleteCourse(courseId)
    }

    suspend fun saveLiveClass(liveClass: LiveClassEntity) {
        if (liveClass.id == 0L) {
            dao.insertLiveClass(liveClass)
            dao.insertNotification(
                NotificationEntity(
                    userId = 0L,
                    type = "LIVE_CLASS",
                    title = "New Live Class Scheduled",
                    body = "${liveClass.title} by ${liveClass.teacherName} (${liveClass.courseTitle})"
                )
            )
        } else {
            dao.updateLiveClass(liveClass)
        }
    }

    suspend fun deleteLiveClass(id: Long) {
        dao.deleteLiveClass(id)
    }

    suspend fun addRecordedClass(video: RecordedClassEntity) {
        dao.insertRecordedClass(video)
        dao.insertNotification(
            NotificationEntity(
                userId = 0L,
                type = "VIDEO",
                title = "New Recorded Class Added",
                body = "${video.title} is now available in ${video.courseTitle}"
            )
        )
    }

    suspend fun deleteRecordedClass(id: Long) {
        dao.deleteRecordedClass(id)
    }

    suspend fun addPdfNote(note: PdfNoteEntity) {
        dao.insertPdfNote(note)
        dao.insertNotification(
            NotificationEntity(
                userId = 0L,
                type = "PDF",
                title = "New PDF Study Note",
                body = "${note.title} (${note.subject}) uploaded."
            )
        )
    }

    suspend fun deletePdfNote(id: Long) {
        dao.deletePdfNote(id)
    }

    suspend fun createMockTestWithQuestion(
        test: MockTestEntity,
        firstQuestion: MockQuestionEntity
    ) {
        val testId = dao.insertMockTest(test)
        dao.insertMockQuestion(firstQuestion.copy(testId = testId))
        if (test.isPublished) {
            dao.insertNotification(
                NotificationEntity(
                    userId = 0L,
                    type = "MOCK_TEST",
                    title = "New Mock Test Published",
                    body = "${test.title} (${test.totalMarks} Marks) is now live!"
                )
            )
        }
    }

    suspend fun addQuestionToTest(question: MockQuestionEntity) {
        dao.insertMockQuestion(question)
    }

    suspend fun toggleMockTestPublish(test: MockTestEntity) {
        dao.updateMockTest(test.copy(isPublished = !test.isPublished))
    }

    suspend fun deleteMockTest(id: Long) {
        dao.deleteMockTest(id)
    }

    suspend fun addMockPaper(paper: MockPaperEntity) {
        dao.insertMockPaper(paper)
    }

    suspend fun deleteMockPaper(id: Long) {
        dao.deleteMockPaper(id)
    }

    suspend fun publishAnnouncement(title: String, message: String, isTicker: Boolean) {
        dao.insertAnnouncement(AnnouncementEntity(title = title.trim(), message = message.trim(), isTicker = isTicker))
        dao.insertNotification(
            NotificationEntity(
                userId = 0L,
                type = "ANNOUNCEMENT",
                title = title.trim(),
                body = message.trim()
            )
        )
    }

    suspend fun deleteAnnouncement(id: Long) {
        dao.deleteAnnouncement(id)
    }

    suspend fun toggleUserBlocked(user: UserEntity) {
        dao.updateUser(user.copy(isBlocked = !user.isBlocked))
    }

    suspend fun grantCourseAccess(user: UserEntity, course: CourseEntity) {
        val existing = dao.getEnrollment(user.id, course.id)
        if (existing != null) return
        val now = System.currentTimeMillis()
        dao.insertEnrollment(
            EnrollmentEntity(
                userId = user.id,
                courseId = course.id,
                purchasedAt = now,
                expiryAt = now + course.validityDays.toLong() * 86_400_000L,
                progressPercent = 0,
                transactionId = "ADMIN_GRANT_${Random.nextInt(10000, 99999)}",
                amountPaid = 0
            )
        )
    }

    suspend fun revokeCourseAccess(enrollmentId: Long) {
        dao.deleteEnrollment(enrollmentId)
    }

    suspend fun verifyPaymentByAdmin(payment: PaymentEntity) {
        dao.updatePayment(payment.copy(status = "SUCCESS", verifiedByAdmin = true))
        val existing = dao.getEnrollment(payment.userId, payment.courseId)
        if (existing == null) {
            val now = System.currentTimeMillis()
            dao.insertEnrollment(
                EnrollmentEntity(
                    userId = payment.userId,
                    courseId = payment.courseId,
                    purchasedAt = now,
                    expiryAt = now + 365L * 86_400_000L,
                    progressPercent = 0,
                    transactionId = payment.transactionId,
                    amountPaid = payment.amountInr
                )
            )
        }
    }

    suspend fun replyContactQuery(query: ContactQueryEntity, reply: String) {
        dao.updateContactQuery(query.copy(status = "RESOLVED", adminReply = reply.trim()))
    }

    suspend fun saveAppSettings(settings: AppSettingsEntity) {
        dao.upsertAppSettings(settings)
    }
}
