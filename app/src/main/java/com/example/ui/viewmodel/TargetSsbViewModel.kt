package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.ai.AiRepository
import com.example.data.ai.AiTutorResponse
import com.example.data.local.*
import com.example.data.repository.TargetSsbRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface AuthUiState {
    data object Idle : AuthUiState
    data object Loading : AuthUiState
    data class OtpPending(
        val fullName: String,
        val mobile: String,
        val email: String,
        val password: String,
        val generatedOtp: String
    ) : AuthUiState
    data class Error(val message: String) : AuthUiState
    data class SuccessMessage(val message: String) : AuthUiState
}

sealed interface CheckoutUiState {
    data object Idle : CheckoutUiState
    data object Verifying : CheckoutUiState
    data class Success(val payment: PaymentEntity) : CheckoutUiState
    data class Failed(val message: String) : CheckoutUiState
}

sealed interface AiUiState {
    data object Idle : AiUiState
    data object Loading : AiUiState
    data class Success(val response: AiTutorResponse) : AiUiState
    data class Error(val message: String) : AiUiState
}

@OptIn(ExperimentalCoroutinesApi::class)
class TargetSsbViewModel(
    private val repository: TargetSsbRepository,
    private val aiRepository: AiRepository
) : ViewModel() {

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
        }
    }

    val darkModeEnabled: StateFlow<Boolean> = repository.darkModeEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), false)

    val loggedInUserId: StateFlow<Long?> = repository.loggedInUserId
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), null)

    val currentUser: StateFlow<UserEntity?> = loggedInUserId
        .flatMapLatest { uid ->
            if (uid == null) flowOf(null) else repository.observeUser(uid)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), null)

    val courses: StateFlow<List<CourseEntity>> = repository.courses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val liveClasses: StateFlow<List<LiveClassEntity>> = repository.liveClasses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val recordedClasses: StateFlow<List<RecordedClassEntity>> = repository.recordedClasses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val pdfNotes: StateFlow<List<PdfNoteEntity>> = repository.pdfNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val mockTests: StateFlow<List<MockTestEntity>> = repository.mockTests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val allQuestions: StateFlow<List<MockQuestionEntity>> = repository.allQuestions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val mockPapers: StateFlow<List<MockPaperEntity>> = repository.mockPapers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val announcements: StateFlow<List<AnnouncementEntity>> = repository.announcements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val appSettings: StateFlow<AppSettingsEntity> = repository.appSettings
        .map { it ?: AppSettingsEntity() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), AppSettingsEntity())

    val userEnrollments: StateFlow<List<EnrollmentEntity>> = loggedInUserId
        .flatMapLatest { uid ->
            if (uid == null) flowOf(emptyList()) else repository.observeUserEnrollments(uid)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val userPayments: StateFlow<List<PaymentEntity>> = loggedInUserId
        .flatMapLatest { uid ->
            if (uid == null) flowOf(emptyList()) else repository.observeUserPayments(uid)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val userAttempts: StateFlow<List<MockAttemptEntity>> = loggedInUserId
        .flatMapLatest { uid ->
            if (uid == null) flowOf(emptyList()) else repository.observeUserAttempts(uid)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val userNotifications: StateFlow<List<NotificationEntity>> = loggedInUserId
        .flatMapLatest { uid ->
            if (uid == null) flowOf(emptyList()) else repository.observeUserNotifications(uid)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val userContactQueries: StateFlow<List<ContactQueryEntity>> = loggedInUserId
        .flatMapLatest { uid ->
            if (uid == null) flowOf(emptyList()) else repository.observeUserContactQueries(uid)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    // Admin streams
    val allUsers: StateFlow<List<UserEntity>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())
    val allEnrollments: StateFlow<List<EnrollmentEntity>> = repository.allEnrollments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())
    val allPayments: StateFlow<List<PaymentEntity>> = repository.allPayments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())
    val allContactQueries: StateFlow<List<ContactQueryEntity>> = repository.allContactQueries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    private val _authState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val authState: StateFlow<AuthUiState> = _authState.asStateFlow()

    private val _checkoutState = MutableStateFlow<CheckoutUiState>(CheckoutUiState.Idle)
    val checkoutState: StateFlow<CheckoutUiState> = _checkoutState.asStateFlow()

    private val _aiState = MutableStateFlow<AiUiState>(AiUiState.Idle)
    val aiState: StateFlow<AiUiState> = _aiState.asStateFlow()

    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage: SharedFlow<String> = _snackbarMessage.asSharedFlow()

    private fun emitMessage(msg: String) {
        viewModelScope.launch { _snackbarMessage.emit(msg) }
    }

    fun toggleDarkMode() {
        viewModelScope.launch {
            repository.setDarkMode(!darkModeEnabled.value)
        }
    }

    // --- Auth ---

    fun clearAuthState() {
        _authState.value = AuthUiState.Idle
    }

    fun login(identifier: String, password: String, requireAdmin: Boolean) {
        if (identifier.isBlank() || password.isBlank()) {
            _authState.value = AuthUiState.Error("Please enter your email/mobile and password.")
            return
        }
        viewModelScope.launch {
            _authState.value = AuthUiState.Loading
            val res = repository.login(identifier, password, requireAdmin)
            _authState.value = res.fold(
                onSuccess = { AuthUiState.Idle },
                onFailure = { AuthUiState.Error(it.message ?: "Login failed") }
            )
        }
    }

    fun quickDemoLogin(role: String) {
        viewModelScope.launch {
            _authState.value = AuthUiState.Loading
            val res = repository.quickDemoLogin(role)
            _authState.value = res.fold(
                onSuccess = { AuthUiState.Idle },
                onFailure = { AuthUiState.Error(it.message ?: "Demo login failed") }
            )
        }
    }

    fun startStudentRegistration(
        fullName: String,
        mobile: String,
        email: String,
        password: String,
        confirmPassword: String
    ) {
        when {
            fullName.trim().length < 2 -> {
                _authState.value = AuthUiState.Error("Enter your full name.")
                return
            }
            mobile.trim().length < 10 -> {
                _authState.value = AuthUiState.Error("Enter a valid 10-digit mobile number.")
                return
            }
            !email.contains("@") -> {
                _authState.value = AuthUiState.Error("Enter a valid email address.")
                return
            }
            password.length < 6 -> {
                _authState.value = AuthUiState.Error("Password must be at least 6 characters.")
                return
            }
            password != confirmPassword -> {
                _authState.value = AuthUiState.Error("Passwords do not match.")
                return
            }
        }
        val otp = (100000..999999).random().toString()
        _authState.value = AuthUiState.OtpPending(
            fullName = fullName.trim(),
            mobile = mobile.trim(),
            email = email.trim(),
            password = password,
            generatedOtp = otp
        )
    }

    fun verifyOtpAndRegister(enteredOtp: String) {
        val pending = _authState.value as? AuthUiState.OtpPending ?: return
        if (enteredOtp.trim() != pending.generatedOtp) {
            emitMessage("Invalid OTP. Check the verification code shown above.")
            return
        }
        viewModelScope.launch {
            _authState.value = AuthUiState.Loading
            val res = repository.registerStudent(
                fullName = pending.fullName,
                mobile = pending.mobile,
                email = pending.email,
                password = pending.password
            )
            _authState.value = res.fold(
                onSuccess = { AuthUiState.Idle },
                onFailure = { AuthUiState.Error(it.message ?: "Registration failed") }
            )
        }
    }

    fun resetPassword(identifier: String, newPassword: String) {
        if (identifier.isBlank() || newPassword.length < 6) {
            _authState.value = AuthUiState.Error("Enter email/mobile and a new password (min 6 chars).")
            return
        }
        viewModelScope.launch {
            _authState.value = AuthUiState.Loading
            val res = repository.resetPasswordByEmailOrMobile(identifier, newPassword)
            _authState.value = res.fold(
                onSuccess = { AuthUiState.SuccessMessage("Password reset successfully! You can now log in.") },
                onFailure = { AuthUiState.Error(it.message ?: "Reset failed") }
            )
        }
    }

    fun updateStudentProfile(fullName: String, mobile: String, avatarUri: String?) {
        val uid = currentUser.value?.id ?: return
        viewModelScope.launch {
            val res = repository.updateProfile(uid, fullName, mobile, avatarUri)
            if (res.isSuccess) emitMessage("Profile updated successfully")
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _authState.value = AuthUiState.Idle
        }
    }

    // --- Checkout & Course Purchase ---

    fun resetCheckoutState() {
        _checkoutState.value = CheckoutUiState.Idle
    }

    fun payForCourse(course: CourseEntity, method: String, simulateFailure: Boolean = false) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            _checkoutState.value = CheckoutUiState.Verifying
            val res = repository.processCoursePayment(user, course, method, simulateFailure)
            _checkoutState.value = res.fold(
                onSuccess = { CheckoutUiState.Success(it) },
                onFailure = { CheckoutUiState.Failed(it.message ?: "Payment verification failed") }
            )
        }
    }

    // --- Learning Progress & Media ---

    fun updateVideoProgress(video: RecordedClassEntity, watchedSeconds: Int) {
        val uid = currentUser.value?.id ?: return
        viewModelScope.launch {
            repository.updateVideoProgress(uid, video, watchedSeconds)
        }
    }

    fun toggleLiveReminder(liveClass: LiveClassEntity) {
        viewModelScope.launch {
            repository.toggleLiveReminder(liveClass)
            emitMessage(
                if (!liveClass.reminderEnabled) "Live class reminder enabled"
                else "Reminder turned off"
            )
        }
    }

    // --- Mock Tests ---

    suspend fun loadQuestions(testId: Long): List<MockQuestionEntity> =
        repository.getQuestionsForTest(testId)

    suspend fun submitMockTest(
        test: MockTestEntity,
        questions: List<MockQuestionEntity>,
        answers: Map<Long, Int>,
        timeTakenSeconds: Int
    ): MockAttemptEntity? {
        val uid = currentUser.value?.id ?: return null
        return repository.submitMockTest(uid, test, questions, answers, timeTakenSeconds)
    }

    // --- Notifications & Contact ---

    fun markAllNotificationsRead() {
        val uid = currentUser.value?.id ?: return
        viewModelScope.launch { repository.markAllNotificationsRead(uid) }
    }

    fun markNotificationRead(id: Long) {
        viewModelScope.launch { repository.markNotificationRead(id) }
    }

    fun submitContactQuery(subject: String, message: String) {
        val user = currentUser.value ?: return
        if (subject.isBlank() || message.isBlank()) {
            emitMessage("Please fill in subject and message.")
            return
        }
        viewModelScope.launch {
            repository.submitContactQuery(
                userId = user.id,
                name = user.fullName,
                mobile = user.mobile,
                email = user.email,
                subject = subject,
                message = message
            )
            emitMessage("Your query has been sent to Target SSB Support!")
        }
    }

    // --- AI Study Assistant (Routed via AiRepository) ---

    fun askAiDoubt(subject: String, question: String) {
        if (question.isBlank()) {
            _aiState.value = AiUiState.Error("Please enter a topic or question to ask.")
            return
        }
        viewModelScope.launch {
            _aiState.value = AiUiState.Loading
            try {
                val res = aiRepository.askStudyDoubt(subject, question)
                _aiState.value = AiUiState.Success(res)
            } catch (e: Exception) {
                _aiState.value = AiUiState.Error(e.localizedMessage ?: "AI Tutor request failed.")
            }
        }
    }

    fun generateAiStudyPlan(examTrack: String, dailyHours: Int) {
        viewModelScope.launch {
            _aiState.value = AiUiState.Loading
            try {
                val res = aiRepository.generateStudyPlan(examTrack, dailyHours)
                _aiState.value = AiUiState.Success(res)
            } catch (e: Exception) {
                _aiState.value = AiUiState.Error(e.localizedMessage ?: "Unable to generate plan.")
            }
        }
    }

    fun clearAiState() {
        _aiState.value = AiUiState.Idle
    }

    // --- Admin Actions ---

    fun adminSaveCourse(course: CourseEntity) {
        viewModelScope.launch {
            repository.saveCourse(course)
            emitMessage("Course saved")
        }
    }

    fun adminDeleteCourse(courseId: Long) {
        viewModelScope.launch {
            repository.deleteCourse(courseId)
            emitMessage("Course deleted")
        }
    }

    fun adminSaveLiveClass(liveClass: LiveClassEntity) {
        viewModelScope.launch {
            repository.saveLiveClass(liveClass)
            emitMessage("Live class saved")
        }
    }

    fun adminDeleteLiveClass(id: Long) {
        viewModelScope.launch {
            repository.deleteLiveClass(id)
            emitMessage("Live class deleted")
        }
    }

    fun adminAddRecordedClass(video: RecordedClassEntity) {
        viewModelScope.launch {
            repository.addRecordedClass(video)
            emitMessage("Recorded class uploaded")
        }
    }

    fun adminDeleteRecordedClass(id: Long) {
        viewModelScope.launch {
            repository.deleteRecordedClass(id)
            emitMessage("Recorded class removed")
        }
    }

    fun adminAddPdfNote(note: PdfNoteEntity) {
        viewModelScope.launch {
            repository.addPdfNote(note)
            emitMessage("PDF Note published")
        }
    }

    fun adminDeletePdfNote(id: Long) {
        viewModelScope.launch {
            repository.deletePdfNote(id)
            emitMessage("PDF Note deleted")
        }
    }

    fun adminCreateMockTest(test: MockTestEntity, firstQuestion: MockQuestionEntity) {
        viewModelScope.launch {
            repository.createMockTestWithQuestion(test, firstQuestion)
            emitMessage("Mock test created")
        }
    }

    fun adminAddQuestion(question: MockQuestionEntity) {
        viewModelScope.launch {
            repository.addQuestionToTest(question)
            emitMessage("Question added to mock test")
        }
    }

    fun adminToggleMockTestPublish(test: MockTestEntity) {
        viewModelScope.launch {
            repository.toggleMockTestPublish(test)
            emitMessage(if (!test.isPublished) "Mock test published" else "Mock test unpublished")
        }
    }

    fun adminDeleteMockTest(id: Long) {
        viewModelScope.launch {
            repository.deleteMockTest(id)
            emitMessage("Mock test deleted")
        }
    }

    fun adminAddMockPaper(paper: MockPaperEntity) {
        viewModelScope.launch {
            repository.addMockPaper(paper)
            emitMessage("Mock test paper uploaded")
        }
    }

    fun adminDeleteMockPaper(id: Long) {
        viewModelScope.launch {
            repository.deleteMockPaper(id)
            emitMessage("Mock test paper deleted")
        }
    }

    fun adminPublishAnnouncement(title: String, message: String, isTicker: Boolean) {
        if (title.isBlank() || message.isBlank()) return
        viewModelScope.launch {
            repository.publishAnnouncement(title, message, isTicker)
            emitMessage("Announcement published & notification sent")
        }
    }

    fun adminDeleteAnnouncement(id: Long) {
        viewModelScope.launch { repository.deleteAnnouncement(id) }
    }

    fun adminToggleUserBlocked(user: UserEntity) {
        viewModelScope.launch {
            repository.toggleUserBlocked(user)
            emitMessage(if (!user.isBlocked) "${user.fullName} blocked" else "${user.fullName} unblocked")
        }
    }

    fun adminGrantCourseAccess(user: UserEntity, course: CourseEntity) {
        viewModelScope.launch {
            repository.grantCourseAccess(user, course)
            emitMessage("Granted ${course.title} to ${user.fullName}")
        }
    }

    fun adminRevokeAccess(enrollmentId: Long) {
        viewModelScope.launch {
            repository.revokeCourseAccess(enrollmentId)
            emitMessage("Course access revoked")
        }
    }

    fun adminVerifyPayment(payment: PaymentEntity) {
        viewModelScope.launch {
            repository.verifyPaymentByAdmin(payment)
            emitMessage("Payment ${payment.transactionId} verified & course unlocked")
        }
    }

    fun adminReplyContactQuery(query: ContactQueryEntity, reply: String) {
        if (reply.isBlank()) return
        viewModelScope.launch {
            repository.replyContactQuery(query, reply)
            emitMessage("Reply saved & query resolved")
        }
    }

    fun adminSaveSettings(settings: AppSettingsEntity) {
        viewModelScope.launch {
            repository.saveAppSettings(settings)
            emitMessage("Platform settings updated")
        }
    }

    class Factory(
        private val repository: TargetSsbRepository,
        private val aiRepository: AiRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TargetSsbViewModel(repository, aiRepository) as T
        }
    }
}
