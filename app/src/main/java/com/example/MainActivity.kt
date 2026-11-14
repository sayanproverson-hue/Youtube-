package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.ai.PlaceholderAiRepository
import com.example.data.local.*
import com.example.data.repository.TargetSsbRepository
import com.example.ui.components.ExoVideoPlayerDialog
import com.example.ui.components.PdfPreviewDialog
import com.example.ui.screens.*
import com.example.ui.theme.TargetSsbTheme
import com.example.ui.viewmodel.TargetSsbViewModel
import kotlinx.coroutines.flow.collectLatest
import java.io.File

private enum class StudentTab(
    val label: String
) {
    HOME("Home"),
    COURSES("Courses"),
    LIVE("Live"),
    TESTS("Tests"),
    PROFILE("Profile")
}

private data class ActivePlayerState(
    val title: String,
    val subtitle: String,
    val url: String,
    val initialSeconds: Int = 0,
    val isLive: Boolean = false,
    val recordedEntity: RecordedClassEntity? = null
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = TargetSsbDatabase.getInstance(applicationContext)
        val prefs = SessionPreferences(applicationContext)
        val repository = TargetSsbRepository(db.dao(), prefs)
        val aiRepository = PlaceholderAiRepository()

        setContent {
            val viewModel: TargetSsbViewModel = viewModel(
                factory = TargetSsbViewModel.Factory(repository, aiRepository)
            )
            val darkMode by viewModel.darkModeEnabled.collectAsStateWithLifecycle()

            TargetSsbTheme(darkTheme = darkMode) {
                TargetSsbAppRoot(viewModel = viewModel, darkModeEnabled = darkMode)
            }
        }
    }
}

@Composable
private fun TargetSsbAppRoot(
    viewModel: TargetSsbViewModel,
    darkModeEnabled: Boolean
) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    val settings by viewModel.appSettings.collectAsStateWithLifecycle()
    val courses by viewModel.courses.collectAsStateWithLifecycle()
    val liveClasses by viewModel.liveClasses.collectAsStateWithLifecycle()
    val recordedClasses by viewModel.recordedClasses.collectAsStateWithLifecycle()
    val pdfNotes by viewModel.pdfNotes.collectAsStateWithLifecycle()
    val mockTests by viewModel.mockTests.collectAsStateWithLifecycle()
    val mockPapers by viewModel.mockPapers.collectAsStateWithLifecycle()
    val announcements by viewModel.announcements.collectAsStateWithLifecycle()

    val enrollments by viewModel.userEnrollments.collectAsStateWithLifecycle()
    val payments by viewModel.userPayments.collectAsStateWithLifecycle()
    val attempts by viewModel.userAttempts.collectAsStateWithLifecycle()
    val notifications by viewModel.userNotifications.collectAsStateWithLifecycle()
    val userContactQueries by viewModel.userContactQueries.collectAsStateWithLifecycle()

    val allUsers by viewModel.allUsers.collectAsStateWithLifecycle()
    val allEnrollments by viewModel.allEnrollments.collectAsStateWithLifecycle()
    val allPayments by viewModel.allPayments.collectAsStateWithLifecycle()
    val allContactQueries by viewModel.allContactQueries.collectAsStateWithLifecycle()

    val checkoutState by viewModel.checkoutState.collectAsStateWithLifecycle()
    val aiState by viewModel.aiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        viewModel.snackbarMessage.collectLatest { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    val user = currentUser
    if (user == null) {
        AuthScreen(
            authState = authState,
            onLogin = viewModel::login,
            onDemoLogin = viewModel::quickDemoLogin,
            onStartRegister = viewModel::startStudentRegistration,
            onVerifyOtp = viewModel::verifyOtpAndRegister,
            onResetPassword = viewModel::resetPassword,
            onClearError = viewModel::clearAuthState
        )
        return
    }

    if (user.role == "ADMIN") {
        AdminDashboardScreen(
            adminUser = user,
            settings = settings,
            users = allUsers,
            courses = courses,
            enrollments = allEnrollments,
            payments = allPayments,
            liveClasses = liveClasses,
            recordedClasses = recordedClasses,
            pdfNotes = pdfNotes,
            mockTests = mockTests,
            mockPapers = mockPapers,
            announcements = announcements,
            contactQueries = allContactQueries,
            onSaveCourse = viewModel::adminSaveCourse,
            onDeleteCourse = viewModel::adminDeleteCourse,
            onSaveLiveClass = viewModel::adminSaveLiveClass,
            onDeleteLiveClass = viewModel::adminDeleteLiveClass,
            onAddRecordedClass = viewModel::adminAddRecordedClass,
            onDeleteRecordedClass = viewModel::adminDeleteRecordedClass,
            onAddPdfNote = viewModel::adminAddPdfNote,
            onDeletePdfNote = viewModel::adminDeletePdfNote,
            onCreateMockTest = viewModel::adminCreateMockTest,
            onToggleMockTestPublish = viewModel::adminToggleMockTestPublish,
            onDeleteMockTest = viewModel::adminDeleteMockTest,
            onAddMockPaper = viewModel::adminAddMockPaper,
            onDeleteMockPaper = viewModel::adminDeleteMockPaper,
            onPublishAnnouncement = viewModel::adminPublishAnnouncement,
            onDeleteAnnouncement = viewModel::adminDeleteAnnouncement,
            onToggleUserBlocked = viewModel::adminToggleUserBlocked,
            onGrantCourseAccess = viewModel::adminGrantCourseAccess,
            onRevokeAccess = viewModel::adminRevokeAccess,
            onVerifyPayment = viewModel::adminVerifyPayment,
            onReplyContactQuery = viewModel::adminReplyContactQuery,
            onSaveSettings = viewModel::adminSaveSettings,
            onLogout = viewModel::logout
        )
        return
    }

    // Student Workspace
    var activeTab by remember { mutableStateOf(StudentTab.HOME) }
    var secondaryDest by remember { mutableStateOf<SecondaryDestination?>(null) }
    var selectedCourseDetail by remember { mutableStateOf<CourseEntity?>(null) }
    var checkoutCourse by remember { mutableStateOf<CourseEntity?>(null) }
    var activePlayer by remember { mutableStateOf<ActivePlayerState?>(null) }
    var activePdfPreview by remember { mutableStateOf<Pair<String, File>?>(null) }
    var activeMockTest by remember { mutableStateOf<MockTestEntity?>(null) }

    val unreadCount = remember(notifications) { notifications.count { !it.isRead } }
    val enrollmentByCourse = remember(enrollments) { enrollments.associateBy { it.courseId } }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = maxWidth >= 700.dp

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            contentWindowInsets = WindowInsets.safeDrawing,
            bottomBar = {
                if (!useRail && secondaryDest == null) {
                    NavigationBar {
                        StudentTab.entries.forEach { tab ->
                            val selected = activeTab == tab
                            NavigationBarItem(
                                selected = selected,
                                onClick = { activeTab = tab },
                                icon = {
                                    Icon(
                                        imageVector = when (tab) {
                                            StudentTab.HOME -> if (selected) Icons.Filled.Home else Icons.Outlined.Home
                                            StudentTab.COURSES -> if (selected) Icons.Filled.School else Icons.Outlined.School
                                            StudentTab.LIVE -> if (selected) Icons.Filled.LiveTv else Icons.Outlined.LiveTv
                                            StudentTab.TESTS -> if (selected) Icons.Filled.Quiz else Icons.Outlined.Quiz
                                            StudentTab.PROFILE -> if (selected) Icons.Filled.Person else Icons.Outlined.Person
                                        },
                                        contentDescription = tab.label
                                    )
                                },
                                label = { Text(tab.label, maxLines = 1) },
                                modifier = Modifier.testTag("bottom_nav_${tab.name.lowercase()}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                if (useRail && secondaryDest == null) {
                    NavigationRail {
                        StudentTab.entries.forEach { tab ->
                            val selected = activeTab == tab
                            NavigationRailItem(
                                selected = selected,
                                onClick = { activeTab = tab },
                                icon = {
                                    Icon(
                                        imageVector = when (tab) {
                                            StudentTab.HOME -> if (selected) Icons.Filled.Home else Icons.Outlined.Home
                                            StudentTab.COURSES -> if (selected) Icons.Filled.School else Icons.Outlined.School
                                            StudentTab.LIVE -> if (selected) Icons.Filled.LiveTv else Icons.Outlined.LiveTv
                                            StudentTab.TESTS -> if (selected) Icons.Filled.Quiz else Icons.Outlined.Quiz
                                            StudentTab.PROFILE -> if (selected) Icons.Filled.Person else Icons.Outlined.Person
                                        },
                                        contentDescription = tab.label
                                    )
                                },
                                label = { Text(tab.label) }
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    val activeSec = secondaryDest
                    if (activeSec != null) {
                        SecondaryFeatureScreen(
                            destination = activeSec,
                            user = user,
                            settings = settings,
                            courses = courses,
                            enrollments = enrollments,
                            recordedClasses = recordedClasses,
                            pdfNotes = pdfNotes,
                            mockPapers = mockPapers,
                            notifications = notifications,
                            contactQueries = userContactQueries,
                            aiState = aiState,
                            onBack = { secondaryDest = null },
                            onPlayRecorded = { video ->
                                activePlayer = ActivePlayerState(
                                    title = video.title,
                                    subtitle = "${video.chapterName} • ${video.teacherName}",
                                    url = video.videoUrl,
                                    initialSeconds = video.watchedSeconds,
                                    isLive = false,
                                    recordedEntity = video
                                )
                            },
                            onUnlockCourse = { cId ->
                                courses.firstOrNull { it.id == cId }?.let {
                                    viewModel.resetCheckoutState()
                                    checkoutCourse = it
                                }
                            },
                            onPreviewPdf = { title, file -> activePdfPreview = title to file },
                            onAskAiDoubt = viewModel::askAiDoubt,
                            onGenerateAiPlan = viewModel::generateAiStudyPlan,
                            onMarkAllNotificationsRead = viewModel::markAllNotificationsRead,
                            onMarkNotificationRead = viewModel::markNotificationRead,
                            onSubmitContactQuery = viewModel::submitContactQuery
                        )
                    } else {
                        when (activeTab) {
                            StudentTab.HOME -> HomeScreen(
                                user = user,
                                settings = settings,
                                courses = courses,
                                enrollments = enrollments,
                                liveClasses = liveClasses,
                                recordedClasses = recordedClasses,
                                announcements = announcements,
                                unreadNotifications = unreadCount,
                                onOpenCoursesTab = { activeTab = StudentTab.COURSES },
                                onOpenLiveTab = { activeTab = StudentTab.LIVE },
                                onOpenTestsTab = { activeTab = StudentTab.TESTS },
                                onOpenCourseDetail = { selectedCourseDetail = it },
                                onBuyCourse = {
                                    viewModel.resetCheckoutState()
                                    checkoutCourse = it
                                },
                                onJoinLiveClass = { live ->
                                    activePlayer = ActivePlayerState(
                                        title = live.title,
                                        subtitle = "Faculty: ${live.teacherName} • ${live.subject}",
                                        url = live.streamUrl,
                                        isLive = live.status == "LIVE"
                                    )
                                },
                                onPlayRecordedVideo = { video ->
                                    activePlayer = ActivePlayerState(
                                        title = video.title,
                                        subtitle = "${video.chapterName} • ${video.teacherName}",
                                        url = video.videoUrl,
                                        initialSeconds = video.watchedSeconds,
                                        isLive = false,
                                        recordedEntity = video
                                    )
                                },
                                onNavigateSecondary = { secondaryDest = it }
                            )

                            StudentTab.COURSES -> CoursesScreen(
                                courses = courses,
                                enrollments = enrollments,
                                payments = payments,
                                onOpenCourseDetail = { selectedCourseDetail = it },
                                onBuyCourse = {
                                    viewModel.resetCheckoutState()
                                    checkoutCourse = it
                                }
                            )

                            StudentTab.LIVE -> LiveClassesScreen(
                                liveClasses = liveClasses,
                                enrollments = enrollments,
                                onJoinLiveClass = { live ->
                                    activePlayer = ActivePlayerState(
                                        title = live.title,
                                        subtitle = "Faculty: ${live.teacherName} • ${live.subject}",
                                        url = live.streamUrl,
                                        isLive = live.status == "LIVE"
                                    )
                                },
                                onToggleReminder = viewModel::toggleLiveReminder,
                                onEnrollRequired = { cId ->
                                    courses.firstOrNull { it.id == cId }?.let {
                                        viewModel.resetCheckoutState()
                                        checkoutCourse = it
                                    }
                                }
                            )

                            StudentTab.TESTS -> TestsScreen(
                                mockTests = mockTests,
                                mockPapers = mockPapers,
                                attempts = attempts,
                                onStartMockTest = { activeMockTest = it },
                                onPreviewPaperPdf = { paper, file -> activePdfPreview = paper.title to file }
                            )

                            StudentTab.PROFILE -> ProfileScreen(
                                user = user,
                                settings = settings,
                                courses = courses,
                                enrollments = enrollments,
                                payments = payments,
                                attempts = attempts,
                                darkModeEnabled = darkModeEnabled,
                                onToggleDarkMode = viewModel::toggleDarkMode,
                                onUpdateProfile = viewModel::updateStudentProfile,
                                onOpenCourse = { selectedCourseDetail = it },
                                onPreviewReceiptPdf = { pay, file ->
                                    activePdfPreview = "Receipt ${pay.transactionId}" to file
                                },
                                onNavigateSecondary = { secondaryDest = it },
                                onLogout = viewModel::logout
                            )
                        }
                    }
                }
            }
        }
    }

    // Course Details Fullscreen Dialog
    selectedCourseDetail?.let { course ->
        CourseDetailDialog(
            course = course,
            enrollment = enrollmentByCourse[course.id],
            liveClasses = liveClasses,
            recordedClasses = recordedClasses,
            pdfNotes = pdfNotes,
            mockTests = mockTests,
            onBuyNow = {
                selectedCourseDetail = null
                viewModel.resetCheckoutState()
                checkoutCourse = course
            },
            onPlayRecorded = { video ->
                activePlayer = ActivePlayerState(
                    title = video.title,
                    subtitle = "${video.chapterName} • ${video.teacherName}",
                    url = video.videoUrl,
                    initialSeconds = video.watchedSeconds,
                    isLive = false,
                    recordedEntity = video
                )
            },
            onJoinLive = { live ->
                activePlayer = ActivePlayerState(
                    title = live.title,
                    subtitle = "Faculty: ${live.teacherName}",
                    url = live.streamUrl,
                    isLive = live.status == "LIVE"
                )
            },
            onOpenPdf = { note ->
                val f = com.example.data.pdf.PdfGenerator.createNotePdf(context, note)
                activePdfPreview = note.title to f
            },
            onStartTest = { test ->
                selectedCourseDetail = null
                activeMockTest = test
            },
            onDismiss = { selectedCourseDetail = null }
        )
    }

    // Checkout Dialog
    checkoutCourse?.let { course ->
        CheckoutDialog(
            course = course,
            checkoutState = checkoutState,
            onPay = { method, simulateFailure ->
                viewModel.payForCourse(course, method, simulateFailure)
            },
            onViewReceipt = { file ->
                checkoutCourse = null
                viewModel.resetCheckoutState()
                activePdfPreview = "Payment Receipt" to file
            },
            onDismiss = {
                checkoutCourse = null
                viewModel.resetCheckoutState()
            }
        )
    }

    // Media3 Video / Live Player Dialog
    activePlayer?.let { player ->
        ExoVideoPlayerDialog(
            title = player.title,
            subtitle = player.subtitle,
            videoUrl = player.url,
            initialPositionSeconds = player.initialSeconds,
            isLiveStream = player.isLive,
            onProgressSave = { pos ->
                player.recordedEntity?.let { viewModel.updateVideoProgress(it, pos) }
            },
            onDismiss = { activePlayer = null }
        )
    }

    // Real PDF Renderer Dialog
    activePdfPreview?.let { (pdfTitle, pdfFile) ->
        PdfPreviewDialog(
            title = pdfTitle,
            pdfFile = pdfFile,
            onDismiss = { activePdfPreview = null }
        )
    }

    // Interactive Mock Test Exam Runner
    activeMockTest?.let { test ->
        MockTestRunnerDialog(
            test = test,
            loadQuestions = viewModel::loadQuestions,
            onSubmitTest = { questions, answers, elapsed ->
                viewModel.submitMockTest(test, questions, answers, elapsed)
            },
            onDismiss = { activeMockTest = null }
        )
    }
}
