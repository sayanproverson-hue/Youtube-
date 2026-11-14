@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.*
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatusPill
import com.example.ui.components.formatTimestamp
import com.example.ui.theme.*

@Composable
fun AdminDashboardScreen(
    adminUser: UserEntity,
    settings: AppSettingsEntity,
    users: List<UserEntity>,
    courses: List<CourseEntity>,
    enrollments: List<EnrollmentEntity>,
    payments: List<PaymentEntity>,
    liveClasses: List<LiveClassEntity>,
    recordedClasses: List<RecordedClassEntity>,
    pdfNotes: List<PdfNoteEntity>,
    mockTests: List<MockTestEntity>,
    mockPapers: List<MockPaperEntity>,
    announcements: List<AnnouncementEntity>,
    contactQueries: List<ContactQueryEntity>,
    onSaveCourse: (CourseEntity) -> Unit,
    onDeleteCourse: (Long) -> Unit,
    onSaveLiveClass: (LiveClassEntity) -> Unit,
    onDeleteLiveClass: (Long) -> Unit,
    onAddRecordedClass: (RecordedClassEntity) -> Unit,
    onDeleteRecordedClass: (Long) -> Unit,
    onAddPdfNote: (PdfNoteEntity) -> Unit,
    onDeletePdfNote: (Long) -> Unit,
    onCreateMockTest: (MockTestEntity, MockQuestionEntity) -> Unit,
    onToggleMockTestPublish: (MockTestEntity) -> Unit,
    onDeleteMockTest: (Long) -> Unit,
    onAddMockPaper: (MockPaperEntity) -> Unit,
    onDeleteMockPaper: (Long) -> Unit,
    onPublishAnnouncement: (String, String, Boolean) -> Unit,
    onDeleteAnnouncement: (Long) -> Unit,
    onToggleUserBlocked: (UserEntity) -> Unit,
    onGrantCourseAccess: (UserEntity, CourseEntity) -> Unit,
    onRevokeAccess: (Long) -> Unit,
    onVerifyPayment: (PaymentEntity) -> Unit,
    onReplyContactQuery: (ContactQueryEntity, String) -> Unit,
    onSaveSettings: (AppSettingsEntity) -> Unit,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val students = remember(users) { users.filter { it.role == "STUDENT" } }
    val successfulPayments = remember(payments) { payments.filter { it.status == "SUCCESS" } }
    val totalRevenue = remember(successfulPayments) { successfulPayments.sumOf { it.amountInr } }
    val upcomingLivesCount = remember(liveClasses) { liveClasses.count { it.status == "UPCOMING" || it.status == "LIVE" } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("TARGET SSB • ADMIN", fontWeight = FontWeight.ExtraBold)
                        Text("Signed in as ${adminUser.fullName}", style = MaterialTheme.typography.labelSmall)
                    }
                },
                actions = {
                    IconButton(onClick = onLogout, modifier = Modifier.testTag("admin_logout_button")) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout Admin", tint = BrandRose)
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                val items = listOf(
                    "Overview" to Icons.Filled.Dashboard,
                    "Courses" to Icons.Filled.School,
                    "Tests" to Icons.Filled.Quiz,
                    "Manage" to Icons.Filled.ManageAccounts
                )
                items.forEachIndexed { idx, (label, icon) ->
                    NavigationBarItem(
                        selected = selectedTab == idx,
                        onClick = { selectedTab = idx },
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label, maxLines = 1) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (selectedTab) {
                0 -> AdminOverviewTab(
                    totalStudents = students.size,
                    totalCourses = courses.size,
                    totalPurchases = enrollments.size,
                    successfulPayments = successfulPayments.size,
                    totalRevenue = totalRevenue,
                    upcomingLiveClasses = upcomingLivesCount,
                    courses = courses,
                    payments = payments,
                    announcements = announcements,
                    onPublishAnnouncement = onPublishAnnouncement,
                    onDeleteAnnouncement = onDeleteAnnouncement
                )

                1 -> AdminCoursesAndMediaTab(
                    courses = courses,
                    liveClasses = liveClasses,
                    recordedClasses = recordedClasses,
                    pdfNotes = pdfNotes,
                    onSaveCourse = onSaveCourse,
                    onDeleteCourse = onDeleteCourse,
                    onSaveLiveClass = onSaveLiveClass,
                    onDeleteLiveClass = onDeleteLiveClass,
                    onAddRecordedClass = onAddRecordedClass,
                    onDeleteRecordedClass = onDeleteRecordedClass,
                    onAddPdfNote = onAddPdfNote,
                    onDeletePdfNote = onDeletePdfNote
                )

                2 -> AdminTestsAndPapersTab(
                    courses = courses,
                    mockTests = mockTests,
                    mockPapers = mockPapers,
                    onCreateMockTest = onCreateMockTest,
                    onToggleMockTestPublish = onToggleMockTestPublish,
                    onDeleteMockTest = onDeleteMockTest,
                    onAddMockPaper = onAddMockPaper,
                    onDeleteMockPaper = onDeleteMockPaper
                )

                3 -> AdminUsersPaymentsSettingsTab(
                    settings = settings,
                    students = students,
                    courses = courses,
                    enrollments = enrollments,
                    payments = payments,
                    contactQueries = contactQueries,
                    onToggleUserBlocked = onToggleUserBlocked,
                    onGrantCourseAccess = onGrantCourseAccess,
                    onRevokeAccess = onRevokeAccess,
                    onVerifyPayment = onVerifyPayment,
                    onReplyContactQuery = onReplyContactQuery,
                    onSaveSettings = onSaveSettings
                )
            }
        }
    }
}

@Composable
private fun AdminOverviewTab(
    totalStudents: Int,
    totalCourses: Int,
    totalPurchases: Int,
    successfulPayments: Int,
    totalRevenue: Int,
    upcomingLiveClasses: Int,
    courses: List<CourseEntity>,
    payments: List<PaymentEntity>,
    announcements: List<AnnouncementEntity>,
    onPublishAnnouncement: (String, String, Boolean) -> Unit,
    onDeleteAnnouncement: (Long) -> Unit
) {
    var annTitle by remember { mutableStateOf("") }
    var annMessage by remember { mutableStateOf("") }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionHeader(title = "Platform Statistics")
            Spacer(Modifier.height(8.dp))
            val stats = listOf(
                Triple("Total Students", "$totalStudents", BrandBlue to BrandBlueSoft),
                Triple("Total Courses", "$totalCourses", BrandPurple to BrandPurpleSoft),
                Triple("Total Purchases", "$totalPurchases", BrandOrange to BrandOrangeSoft),
                Triple("Success Payments", "$successfulPayments", BrandGreen to BrandGreenSoft),
                Triple("Total Revenue", "₹$totalRevenue/-", BrandGreen to BrandGreenSoft),
                Triple("Live & Upcoming", "$upcomingLiveClasses", BrandRose to BrandRoseSoft)
            )
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                stats.chunked(2).forEach { pair ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        pair.forEach { (label, value, colors) ->
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = colors.second,
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(label, style = MaterialTheme.typography.labelMedium, color = colors.first)
                                    Spacer(Modifier.height(4.dp))
                                    Text(value, style = MaterialTheme.typography.headlineSmall, color = colors.first, fontWeight = FontWeight.ExtraBold)
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            SectionHeader(title = "Course Sales Report")
            Spacer(Modifier.height(6.dp))
            courses.forEach { c ->
                val sales = payments.filter { it.courseId == c.id && it.status == "SUCCESS" }
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(c.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text("${sales.size} purchases", style = MaterialTheme.typography.bodySmall)
                        }
                        Text("₹${sales.sumOf { it.amountInr }}/-", style = MaterialTheme.typography.titleMedium, color = BrandGreen, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            SectionHeader(title = "Publish Announcement")
            Spacer(Modifier.height(6.dp))
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = annTitle, onValueChange = { annTitle = it }, label = { Text("Announcement Title") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = annMessage, onValueChange = { annMessage = it }, label = { Text("Message (Bengali/English)") }, minLines = 2, modifier = Modifier.fillMaxWidth())
                    Button(
                        onClick = {
                            onPublishAnnouncement(annTitle, annMessage, false)
                            annTitle = ""
                            annMessage = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandOrange),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Publish & Notify Students")
                    }
                }
            }
        }

        items(announcements, key = { it.id }) { ann ->
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(ann.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text(ann.message, style = MaterialTheme.typography.bodySmall)
                    }
                    IconButton(onClick = { onDeleteAnnouncement(ann.id) }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete Announcement", tint = BrandRose)
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminCoursesAndMediaTab(
    courses: List<CourseEntity>,
    liveClasses: List<LiveClassEntity>,
    recordedClasses: List<RecordedClassEntity>,
    pdfNotes: List<PdfNoteEntity>,
    onSaveCourse: (CourseEntity) -> Unit,
    onDeleteCourse: (Long) -> Unit,
    onSaveLiveClass: (LiveClassEntity) -> Unit,
    onDeleteLiveClass: (Long) -> Unit,
    onAddRecordedClass: (RecordedClassEntity) -> Unit,
    onDeleteRecordedClass: (Long) -> Unit,
    onAddPdfNote: (PdfNoteEntity) -> Unit,
    onDeletePdfNote: (Long) -> Unit
) {
    var showCourseDialog by remember { mutableStateOf<CourseEntity?>(null) }
    var showLiveDialog by remember { mutableStateOf(false) }
    var showVideoDialog by remember { mutableStateOf(false) }
    var showPdfDialog by remember { mutableStateOf(false) }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        showCourseDialog = CourseEntity(
                            title = "",
                            subtitle = "",
                            category = "ANM & GNM",
                            priceInr = 799,
                            originalPriceInr = 1999,
                            description = "",
                            featuresCsv = "Live Classes,Recorded Classes,PDF Notes,Mock Tests",
                            bannerKey = "anm_gnm",
                            instructor = "Target SSB Faculty"
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandOrange),
                    modifier = Modifier.weight(1f)
                ) { Text("+ Course") }

                Button(
                    onClick = { showLiveDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandRose),
                    modifier = Modifier.weight(1f)
                ) { Text("+ Live Class") }
            }
            Spacer(Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { showVideoDialog = true }, modifier = Modifier.weight(1f)) { Text("+ Recorded") }
                OutlinedButton(onClick = { showPdfDialog = true }, modifier = Modifier.weight(1f)) { Text("+ PDF Note") }
            }
        }

        item { SectionHeader(title = "Courses (${courses.size})") }
        items(courses, key = { "c_${it.id}" }) { c ->
            Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(c.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text("${c.category} • ₹${c.priceInr}/-", style = MaterialTheme.typography.bodySmall, color = BrandOrange)
                    }
                    IconButton(onClick = { showCourseDialog = c }) { Icon(Icons.Filled.Edit, contentDescription = "Edit Course") }
                    IconButton(onClick = { onDeleteCourse(c.id) }) { Icon(Icons.Filled.Delete, contentDescription = "Delete Course", tint = BrandRose) }
                }
            }
        }

        item { SectionHeader(title = "Live Classes (${liveClasses.size})") }
        items(liveClasses, key = { "l_${it.id}" }) { live ->
            Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(live.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text("${live.teacherName} • Status: ${live.status}", style = MaterialTheme.typography.bodySmall)
                    }
                    TextButton(onClick = {
                        val nextStatus = when (live.status) {
                            "UPCOMING" -> "LIVE"
                            "LIVE" -> "COMPLETED"
                            else -> "UPCOMING"
                        }
                        onSaveLiveClass(live.copy(status = nextStatus))
                    }) { Text("Cycle") }
                    IconButton(onClick = { onDeleteLiveClass(live.id) }) { Icon(Icons.Filled.Delete, contentDescription = "Delete Live", tint = BrandRose) }
                }
            }
        }

        item { SectionHeader(title = "Recorded Classes (${recordedClasses.size})") }
        items(recordedClasses, key = { "r_${it.id}" }) { vid ->
            Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(vid.title, style = MaterialTheme.typography.titleSmall)
                        Text(vid.chapterName, style = MaterialTheme.typography.bodySmall)
                    }
                    IconButton(onClick = { onDeleteRecordedClass(vid.id) }) { Icon(Icons.Filled.Delete, contentDescription = "Delete Video", tint = BrandRose) }
                }
            }
        }

        item { SectionHeader(title = "PDF Notes (${pdfNotes.size})") }
        items(pdfNotes, key = { "p_${it.id}" }) { pdf ->
            Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(pdf.title, style = MaterialTheme.typography.titleSmall)
                        Text("${pdf.subject} • ${pdf.chapter}", style = MaterialTheme.typography.bodySmall)
                    }
                    IconButton(onClick = { onDeletePdfNote(pdf.id) }) { Icon(Icons.Filled.Delete, contentDescription = "Delete PDF", tint = BrandRose) }
                }
            }
        }
    }

    showCourseDialog?.let { editing ->
        var title by remember(editing) { mutableStateOf(editing.title) }
        var subtitle by remember(editing) { mutableStateOf(editing.subtitle) }
        var category by remember(editing) { mutableStateOf(editing.category) }
        var price by remember(editing) { mutableStateOf(editing.priceInr.toString()) }
        var desc by remember(editing) { mutableStateOf(editing.description) }
        AlertDialog(
            onDismissRequest = { showCourseDialog = null },
            title = { Text(if (editing.id == 0L) "Add Course" else "Edit Course") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Course Title") })
                    OutlinedTextField(value = subtitle, onValueChange = { subtitle = it }, label = { Text("Subtitle") })
                    OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category (ANM & GNM / RRB NTPC)") })
                    OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Price (₹)") })
                    OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") }, minLines = 2)
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (title.isNotBlank()) {
                        onSaveCourse(
                            editing.copy(
                                title = title.trim(),
                                subtitle = subtitle.trim().ifBlank { category },
                                category = category.trim().ifBlank { "ANM & GNM" },
                                priceInr = price.toIntOrNull() ?: 999,
                                description = desc.trim().ifBlank { title.trim() },
                                bannerKey = if (category.contains("RRB", true)) "rrb_ntpc" else "anm_gnm"
                            )
                        )
                        showCourseDialog = null
                    }
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showCourseDialog = null }) { Text("Cancel") } }
        )
    }

    if (showLiveDialog && courses.isNotEmpty()) {
        val defaultCourse = courses.first()
        var title by remember { mutableStateOf("") }
        var subject by remember { mutableStateOf("Life Science") }
        var teacher by remember { mutableStateOf("Dr. Ananya Sen") }
        var streamUrl by remember { mutableStateOf("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4") }
        AlertDialog(
            onDismissRequest = { showLiveDialog = false },
            title = { Text("Schedule Live Class") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Class Topic") })
                    OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text("Subject") })
                    OutlinedTextField(value = teacher, onValueChange = { teacher = it }, label = { Text("Teacher Name") })
                    OutlinedTextField(value = streamUrl, onValueChange = { streamUrl = it }, label = { Text("Stream URL (HLS / MP4)") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (title.isNotBlank()) {
                        onSaveLiveClass(
                            LiveClassEntity(
                                courseId = defaultCourse.id,
                                courseTitle = defaultCourse.title,
                                title = title.trim(),
                                subject = subject.trim(),
                                teacherName = teacher.trim(),
                                scheduledAtMillis = System.currentTimeMillis() + 3_600_000L,
                                durationMinutes = 60,
                                status = "UPCOMING",
                                streamUrl = streamUrl.trim()
                            )
                        )
                        showLiveDialog = false
                    }
                }) { Text("Schedule") }
            },
            dismissButton = { TextButton(onClick = { showLiveDialog = false }) { Text("Cancel") } }
        )
    }

    if (showVideoDialog && courses.isNotEmpty()) {
        val defaultCourse = courses.first()
        var title by remember { mutableStateOf("") }
        var chapter by remember { mutableStateOf("Chapter 4: Revision") }
        var url by remember { mutableStateOf("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4") }
        AlertDialog(
            onDismissRequest = { showVideoDialog = false },
            title = { Text("Upload Recorded Class") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Lesson Title") })
                    OutlinedTextField(value = chapter, onValueChange = { chapter = it }, label = { Text("Chapter Name") })
                    OutlinedTextField(value = url, onValueChange = { url = it }, label = { Text("Video Stream URL") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (title.isNotBlank()) {
                        onAddRecordedClass(
                            RecordedClassEntity(
                                courseId = defaultCourse.id,
                                courseTitle = defaultCourse.title,
                                chapterName = chapter.trim(),
                                title = title.trim(),
                                teacherName = "Target SSB Faculty",
                                durationSeconds = 180,
                                videoUrl = url.trim()
                            )
                        )
                        showVideoDialog = false
                    }
                }) { Text("Upload") }
            },
            dismissButton = { TextButton(onClick = { showVideoDialog = false }) { Text("Cancel") } }
        )
    }

    if (showPdfDialog && courses.isNotEmpty()) {
        val defaultCourse = courses.first()
        var title by remember { mutableStateOf("") }
        var subject by remember { mutableStateOf("Life Science") }
        var chapter by remember { mutableStateOf("Chapter 3") }
        var body by remember { mutableStateOf("Important exam key points and formulas for quick revision.") }
        AlertDialog(
            onDismissRequest = { showPdfDialog = false },
            title = { Text("Upload PDF Study Note") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("PDF Title") })
                    OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text("Subject") })
                    OutlinedTextField(value = chapter, onValueChange = { chapter = it }, label = { Text("Chapter") })
                    OutlinedTextField(value = body, onValueChange = { body = it }, label = { Text("PDF Content") }, minLines = 3)
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (title.isNotBlank()) {
                        onAddPdfNote(
                            PdfNoteEntity(
                                courseId = defaultCourse.id,
                                courseTitle = defaultCourse.title,
                                subject = subject.trim(),
                                chapter = chapter.trim(),
                                title = title.trim(),
                                pageCount = 2,
                                fileSizeLabel = "640 KB",
                                contentBody = body.trim()
                            )
                        )
                        showPdfDialog = false
                    }
                }) { Text("Publish PDF") }
            },
            dismissButton = { TextButton(onClick = { showPdfDialog = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun AdminTestsAndPapersTab(
    courses: List<CourseEntity>,
    mockTests: List<MockTestEntity>,
    mockPapers: List<MockPaperEntity>,
    onCreateMockTest: (MockTestEntity, MockQuestionEntity) -> Unit,
    onToggleMockTestPublish: (MockTestEntity) -> Unit,
    onDeleteMockTest: (Long) -> Unit,
    onAddMockPaper: (MockPaperEntity) -> Unit,
    onDeleteMockPaper: (Long) -> Unit
) {
    var showCreateTestDialog by remember { mutableStateOf(false) }
    var showAddPaperDialog by remember { mutableStateOf(false) }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { showCreateTestDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                    modifier = Modifier.weight(1f)
                ) { Text("+ Create Mock Test") }
                OutlinedButton(
                    onClick = { showAddPaperDialog = true },
                    modifier = Modifier.weight(1f)
                ) { Text("+ Upload Paper") }
            }
        }

        item { SectionHeader(title = "Mock Tests (${mockTests.size})") }
        items(mockTests, key = { "mt_${it.id}" }) { test ->
            Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(test.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text("${test.durationMinutes} min • ${test.totalMarks} marks", style = MaterialTheme.typography.bodySmall)
                    }
                    TextButton(onClick = { onToggleMockTestPublish(test) }) {
                        Text(if (test.isPublished) "Published" else "Draft")
                    }
                    IconButton(onClick = { onDeleteMockTest(test.id) }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete Test", tint = BrandRose)
                    }
                }
            }
        }

        item { SectionHeader(title = "Mock Test Papers (${mockPapers.size})") }
        items(mockPapers, key = { "mp_${it.id}" }) { paper ->
            Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(paper.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text("${paper.paperType} • ${paper.yearLabel}", style = MaterialTheme.typography.bodySmall)
                    }
                    IconButton(onClick = { onDeleteMockPaper(paper.id) }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete Paper", tint = BrandRose)
                    }
                }
            }
        }
    }

    if (showCreateTestDialog && courses.isNotEmpty()) {
        val defaultCourse = courses.first()
        var title by remember { mutableStateOf("") }
        var qText by remember { mutableStateOf("") }
        var optA by remember { mutableStateOf("") }
        var optB by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCreateTestDialog = false },
            title = { Text("Create Mock Test") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Test Title") })
                    OutlinedTextField(value = qText, onValueChange = { qText = it }, label = { Text("Sample MCQ Question") })
                    OutlinedTextField(value = optA, onValueChange = { optA = it }, label = { Text("Option A (Correct)") })
                    OutlinedTextField(value = optB, onValueChange = { optB = it }, label = { Text("Option B") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (title.isNotBlank() && qText.isNotBlank()) {
                        onCreateMockTest(
                            MockTestEntity(
                                courseId = defaultCourse.id,
                                courseTitle = defaultCourse.title,
                                title = title.trim(),
                                subject = defaultCourse.category,
                                durationMinutes = 10,
                                totalMarks = 5,
                                negativeMarksPerWrong = 0.25,
                                isPublished = true
                            ),
                            MockQuestionEntity(
                                testId = 0L,
                                questionText = qText.trim(),
                                optionA = optA.ifBlank { "Option A" },
                                optionB = optB.ifBlank { "Option B" },
                                optionC = "Option C",
                                optionD = "Option D",
                                correctOptionIndex = 0,
                                explanation = "Verified by Target SSB Faculty."
                            )
                        )
                        showCreateTestDialog = false
                    }
                }) { Text("Publish") }
            },
            dismissButton = { TextButton(onClick = { showCreateTestDialog = false }) { Text("Cancel") } }
        )
    }

    if (showAddPaperDialog && courses.isNotEmpty()) {
        val defaultCourse = courses.first()
        var title by remember { mutableStateOf("") }
        var type by remember { mutableStateOf("Model Paper") }
        var year by remember { mutableStateOf("2026") }
        AlertDialog(
            onDismissRequest = { showAddPaperDialog = false },
            title = { Text("Upload Mock Test Paper") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Paper Title") })
                    OutlinedTextField(value = type, onValueChange = { type = it }, label = { Text("Type (Previous Year / Practice Set)") })
                    OutlinedTextField(value = year, onValueChange = { year = it }, label = { Text("Year") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (title.isNotBlank()) {
                        onAddMockPaper(
                            MockPaperEntity(
                                courseId = defaultCourse.id,
                                courseTitle = defaultCourse.title,
                                title = title.trim(),
                                paperType = type.trim(),
                                yearLabel = year.trim(),
                                questionsSummary = "Full practice booklet uploaded by Admin.",
                                answerKeyText = "1-A, 2-B, 3-C, 4-D, 5-A"
                            )
                        )
                        showAddPaperDialog = false
                    }
                }) { Text("Upload") }
            },
            dismissButton = { TextButton(onClick = { showAddPaperDialog = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun AdminUsersPaymentsSettingsTab(
    settings: AppSettingsEntity,
    students: List<UserEntity>,
    courses: List<CourseEntity>,
    enrollments: List<EnrollmentEntity>,
    payments: List<PaymentEntity>,
    contactQueries: List<ContactQueryEntity>,
    onToggleUserBlocked: (UserEntity) -> Unit,
    onGrantCourseAccess: (UserEntity, CourseEntity) -> Unit,
    onRevokeAccess: (Long) -> Unit,
    onVerifyPayment: (PaymentEntity) -> Unit,
    onReplyContactQuery: (ContactQueryEntity, String) -> Unit,
    onSaveSettings: (AppSettingsEntity) -> Unit
) {
    var supportPhone by remember(settings) { mutableStateOf(settings.supportPhone) }
    var whatsapp by remember(settings) { mutableStateOf(settings.whatsappNumber) }
    var ticker by remember(settings) { mutableStateOf(settings.tickerHeadline) }
    var certsEnabled by remember(settings) { mutableStateOf(settings.certificatesEnabled) }
    var grantTargetStudent by remember { mutableStateOf<UserEntity?>(null) }
    var replyTargetQuery by remember { mutableStateOf<ContactQueryEntity?>(null) }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { SectionHeader(title = "Students & Access (${students.size})") }
        items(students, key = { "st_${it.id}" }) { st ->
            val stEnrollments = enrollments.filter { it.userId == st.id }
            Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(st.fullName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text("${st.studentCode} • ${st.mobile} • ${stEnrollments.size} Batch(es)", style = MaterialTheme.typography.bodySmall)
                        }
                        StatusPill(
                            if (st.isBlocked) "Blocked" else "Active",
                            if (st.isBlocked) BrandRoseSoft else BrandGreenSoft,
                            if (st.isBlocked) BrandRose else BrandGreen
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { grantTargetStudent = st }, modifier = Modifier.weight(1f)) {
                            Text("Grant Course")
                        }
                        OutlinedButton(
                            onClick = { onToggleUserBlocked(st) },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = if (st.isBlocked) BrandGreen else BrandRose),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (st.isBlocked) "Unblock" else "Block")
                        }
                    }
                    stEnrollments.forEach { en ->
                        val cName = courses.firstOrNull { it.id == en.courseId }?.title ?: "Course #${en.courseId}"
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("• $cName", style = MaterialTheme.typography.labelSmall)
                            TextButton(onClick = { onRevokeAccess(en.id) }) {
                                Text("Revoke", color = BrandRose, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }

        item { SectionHeader(title = "Payments Verification (${payments.size})") }
        items(payments, key = { "pay_${it.id}" }) { pay ->
            Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("${pay.userName} • ₹${pay.amountInr}/-", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text("${pay.transactionId} • ${pay.method} • ${pay.status}", style = MaterialTheme.typography.bodySmall)
                    }
                    if (!pay.verifiedByAdmin || pay.status != "SUCCESS") {
                        Button(onClick = { onVerifyPayment(pay) }, colors = ButtonDefaults.buttonColors(containerColor = BrandGreen)) {
                            Text("Verify")
                        }
                    } else {
                        StatusPill("Verified", BrandGreenSoft, BrandGreen)
                    }
                }
            }
        }

        if (contactQueries.isNotEmpty()) {
            item { SectionHeader(title = "Contact Messages (${contactQueries.size})") }
            items(contactQueries, key = { "cq_${it.id}" }) { q ->
                Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${q.name}: ${q.subject}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            StatusPill(q.status, if (q.status == "RESOLVED") BrandGreenSoft else BrandAmberSoft, if (q.status == "RESOLVED") BrandGreen else BrandAmber)
                        }
                        Text(q.message, style = MaterialTheme.typography.bodySmall)
                        if (q.status != "RESOLVED") {
                            TextButton(onClick = { replyTargetQuery = q }) { Text("Reply & Resolve") }
                        } else if (q.adminReply.isNotBlank()) {
                            Text("Reply: ${q.adminReply}", style = MaterialTheme.typography.labelSmall, color = BrandBlue)
                        }
                    }
                }
            }
        }

        item {
            SectionHeader(title = "App Settings")
            Spacer(Modifier.height(6.dp))
            Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = supportPhone, onValueChange = { supportPhone = it }, label = { Text("Official Contact Mobile") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = whatsapp, onValueChange = { whatsapp = it }, label = { Text("WhatsApp Support Number") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = ticker, onValueChange = { ticker = it }, label = { Text("Scrolling Ticker Headline") }, modifier = Modifier.fillMaxWidth())
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Enable Student Certificates", style = MaterialTheme.typography.bodyMedium)
                        Switch(checked = certsEnabled, onCheckedChange = { certsEnabled = it })
                    }
                    Button(
                        onClick = {
                            onSaveSettings(
                                settings.copy(
                                    supportPhone = supportPhone.trim(),
                                    whatsappNumber = whatsapp.trim(),
                                    tickerHeadline = ticker.trim(),
                                    certificatesEnabled = certsEnabled
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandOrange),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save Platform Settings")
                    }
                }
            }
        }
    }

    grantTargetStudent?.let { st ->
        AlertDialog(
            onDismissRequest = { grantTargetStudent = null },
            title = { Text("Grant Course to ${st.fullName}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    courses.forEach { c ->
                        Button(
                            onClick = {
                                onGrantCourseAccess(st, c)
                                grantTargetStudent = null
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("${c.title} (₹${c.priceInr})")
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { grantTargetStudent = null }) { Text("Cancel") } }
        )
    }

    replyTargetQuery?.let { q ->
        var reply by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { replyTargetQuery = null },
            title = { Text("Reply to ${q.name}") },
            text = {
                OutlinedTextField(value = reply, onValueChange = { reply = it }, label = { Text("Admin Response") }, minLines = 2)
            },
            confirmButton = {
                Button(onClick = {
                    onReplyContactQuery(q, reply)
                    replyTargetQuery = null
                }) { Text("Send Reply") }
            },
            dismissButton = { TextButton(onClick = { replyTargetQuery = null }) { Text("Cancel") } }
        )
    }
}
