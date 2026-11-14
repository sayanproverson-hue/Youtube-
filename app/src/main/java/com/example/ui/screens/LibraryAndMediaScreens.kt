@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.*
import com.example.data.pdf.PdfGenerator
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.AiUiState
import java.io.File

@Composable
fun SecondaryFeatureScreen(
    destination: SecondaryDestination,
    user: UserEntity,
    settings: AppSettingsEntity,
    courses: List<CourseEntity>,
    enrollments: List<EnrollmentEntity>,
    recordedClasses: List<RecordedClassEntity>,
    pdfNotes: List<PdfNoteEntity>,
    mockPapers: List<MockPaperEntity>,
    notifications: List<NotificationEntity>,
    contactQueries: List<ContactQueryEntity>,
    aiState: AiUiState,
    onBack: () -> Unit,
    onPlayRecorded: (RecordedClassEntity) -> Unit,
    onUnlockCourse: (Long) -> Unit,
    onPreviewPdf: (String, File) -> Unit,
    onAskAiDoubt: (String, String) -> Unit,
    onGenerateAiPlan: (String, Int) -> Unit,
    onMarkAllNotificationsRead: () -> Unit,
    onMarkNotificationRead: (Long) -> Unit,
    onSubmitContactQuery: (String, String) -> Unit
) {
    BackHandler { onBack() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        val title = when (destination) {
            SecondaryDestination.RECORDED_CLASSES -> "Recorded Classes"
            SecondaryDestination.PDF_NOTES -> "PDF Notes Library"
            SecondaryDestination.MOCK_PAPERS -> "Mock Test Papers"
            SecondaryDestination.AI_TUTOR -> "AI Study Tutor"
            SecondaryDestination.NOTIFICATIONS -> "Notifications"
            SecondaryDestination.CONTACT_US -> "Contact & Help Support"
            SecondaryDestination.ABOUT_APP -> "About Target SSB"
        }

        TopAppBar(
            title = { Text(title, fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                if (destination == SecondaryDestination.NOTIFICATIONS) {
                    TextButton(onClick = onMarkAllNotificationsRead) {
                        Text("Mark all read")
                    }
                }
            }
        )

        when (destination) {
            SecondaryDestination.RECORDED_CLASSES -> RecordedLibraryView(
                courses = courses,
                enrollments = enrollments,
                recordedClasses = recordedClasses,
                onPlayRecorded = onPlayRecorded,
                onUnlockCourse = onUnlockCourse
            )

            SecondaryDestination.PDF_NOTES -> PdfLibraryView(
                courses = courses,
                enrollments = enrollments,
                pdfNotes = pdfNotes,
                onPreviewPdf = onPreviewPdf,
                onUnlockCourse = onUnlockCourse
            )

            SecondaryDestination.MOCK_PAPERS -> {
                val context = LocalContext.current
                var keyPaper by remember { mutableStateOf<MockPaperEntity?>(null) }
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(mockPapers, key = { it.id }) { paper ->
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 2.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                StatusPill("${paper.paperType} • ${paper.yearLabel}", BrandAmberSoft, BrandAmber)
                                Spacer(Modifier.height(8.dp))
                                Text(paper.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(paper.questionsSummary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(Modifier.height(10.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(onClick = { keyPaper = paper }, modifier = Modifier.weight(1f)) {
                                        Text("Answer Key")
                                    }
                                    Button(
                                        onClick = {
                                            val f = PdfGenerator.createMockPaperPdf(context, paper)
                                            onPreviewPdf(paper.title, f)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = BrandOrange),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Open PDF")
                                    }
                                }
                            }
                        }
                    }
                }
                keyPaper?.let { p ->
                    AlertDialog(
                        onDismissRequest = { keyPaper = null },
                        title = { Text("Answer Key") },
                        text = { Text(p.answerKeyText) },
                        confirmButton = { TextButton(onClick = { keyPaper = null }) { Text("Close") } }
                    )
                }
            }

            SecondaryDestination.AI_TUTOR -> AiTutorView(
                aiState = aiState,
                onAskDoubt = onAskAiDoubt,
                onGeneratePlan = onGenerateAiPlan
            )

            SecondaryDestination.NOTIFICATIONS -> NotificationsView(
                notifications = notifications,
                onMarkRead = onMarkNotificationRead
            )

            SecondaryDestination.CONTACT_US -> ContactSupportView(
                settings = settings,
                queries = contactQueries,
                onSubmitQuery = onSubmitContactQuery
            )

            SecondaryDestination.ABOUT_APP -> AboutTargetSsbView(settings = settings)
        }
    }
}

@Composable
private fun RecordedLibraryView(
    courses: List<CourseEntity>,
    enrollments: List<EnrollmentEntity>,
    recordedClasses: List<RecordedClassEntity>,
    onPlayRecorded: (RecordedClassEntity) -> Unit,
    onUnlockCourse: (Long) -> Unit
) {
    val enrolledIds = remember(enrollments) { enrollments.map { it.courseId }.toSet() }
    var selectedCourseId by remember { mutableStateOf<Long?>(null) }
    val filtered = remember(recordedClasses, selectedCourseId) {
        if (selectedCourseId == null) recordedClasses
        else recordedClasses.filter { it.courseId == selectedCourseId }
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = selectedCourseId == null,
                        onClick = { selectedCourseId = null },
                        label = { Text("All Courses") }
                    )
                }
                items(courses, key = { it.id }) { c ->
                    FilterChip(
                        selected = selectedCourseId == c.id,
                        onClick = { selectedCourseId = c.id },
                        label = { Text(c.category) }
                    )
                }
            }
        }

        items(filtered, key = { it.id }) { video ->
            val unlocked = video.courseId in enrolledIds
            val pct = if (video.durationSeconds > 0)
                (video.watchedSeconds.toFloat() / video.durationSeconds).coerceIn(0f, 1f)
            else 0f
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (unlocked) onPlayRecorded(video) else onUnlockCourse(video.courseId)
                    }
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (unlocked) BrandBlueSoft else SoftGreySurface),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                if (unlocked) Icons.Filled.PlayArrow else Icons.Filled.Lock,
                                contentDescription = null,
                                tint = if (unlocked) BrandBlue else SlateMuted
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(video.chapterName, style = MaterialTheme.typography.labelSmall, color = BrandPurple)
                            Text(video.title, style = MaterialTheme.typography.titleSmall.copy(fontFamily = BengaliFontFamily), fontWeight = FontWeight.Bold)
                            Text("${video.teacherName} • ${formatDurationSeconds(video.durationSeconds)}", style = MaterialTheme.typography.bodySmall)
                        }
                        if (!unlocked) StatusPill("Locked", BrandOrangeSoft, BrandOrangeDark)
                        else if (video.isCompleted) StatusPill("Done", BrandGreenSoft, BrandGreen)
                    }
                    if (unlocked) {
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { pct },
                            modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(4.dp)),
                            color = BrandOrange
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PdfLibraryView(
    courses: List<CourseEntity>,
    enrollments: List<EnrollmentEntity>,
    pdfNotes: List<PdfNoteEntity>,
    onPreviewPdf: (String, File) -> Unit,
    onUnlockCourse: (Long) -> Unit
) {
    val context = LocalContext.current
    val enrolledIds = remember(enrollments) { enrollments.map { it.courseId }.toSet() }
    var search by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All") + courses.map { it.category }.distinct()

    val filtered = remember(pdfNotes, search, selectedCategory, courses) {
        pdfNotes.filter { note ->
            val courseCat = courses.firstOrNull { it.id == note.courseId }?.category ?: ""
            val catOk = selectedCategory == "All" || courseCat == selectedCategory
            val qOk = search.isBlank() || note.title.contains(search, true) || note.subject.contains(search, true)
            catOk && qOk
        }
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                placeholder = { Text("Search PDF notes by topic or subject...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat) }
                    )
                }
            }
        }

        items(filtered, key = { it.id }) { note ->
            val unlocked = note.courseId in enrolledIds
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatusPill(note.subject, BrandPurpleSoft, BrandPurple)
                        Text("${note.pageCount} pages • ${note.fileSizeLabel}", style = MaterialTheme.typography.labelSmall)
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(note.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(note.chapter, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(10.dp))
                    if (unlocked) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    val file = PdfGenerator.createNotePdf(context, note)
                                    onPreviewPdf(note.title, file)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = BrandPurple),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Filled.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Open PDF")
                            }
                            OutlinedButton(
                                onClick = {
                                    val file = PdfGenerator.createNotePdf(context, note)
                                    PdfGenerator.shareOrOpenPdf(context, file, "Download PDF Note")
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Download")
                            }
                        }
                    } else {
                        Button(
                            onClick = { onUnlockCourse(note.courseId) },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandOrange),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Enroll to Unlock PDF")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AiTutorView(
    aiState: AiUiState,
    onAskDoubt: (String, String) -> Unit,
    onGeneratePlan: (String, Int) -> Unit
) {
    var subject by remember { mutableStateOf("ANM & GNM Life Science") }
    var question by remember { mutableStateOf("") }
    var dailyHours by remember { mutableIntStateOf(3) }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Surface(
                color = BrandAmberSoft,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                    Icon(Icons.Filled.Info, contentDescription = null, tint = BrandAmber)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.ai_firebase_notice),
                        style = MaterialTheme.typography.bodySmall,
                        color = SlateInk
                    )
                }
            }
        }

        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Ask AI Doubt Solver", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(listOf("ANM & GNM Life Science", "RRB NTPC Maths", "General Awareness", "Physical Science")) { sub ->
                            FilterChip(
                                selected = subject == sub,
                                onClick = { subject = sub },
                                label = { Text(sub) }
                            )
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = question,
                        onValueChange = { question = it },
                        label = { Text("Ask any exam doubt (Bengali or English)") },
                        placeholder = { Text("e.g. Explain human heart blood circulation or train speed formula") },
                        minLines = 3,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ai_question_input")
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = { onAskDoubt(subject, question) },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandPurple),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("ai_ask_button")
                        ) {
                            Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Ask AI")
                        }
                        OutlinedButton(
                            onClick = { onGeneratePlan(subject, dailyHours) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("7-Day Plan (${dailyHours}h)")
                        }
                    }
                }
            }
        }

        item {
            when (aiState) {
                is AiUiState.Idle -> EmptyStateBox(
                    icon = Icons.Filled.AutoAwesome,
                    title = "AI Study Companion Ready",
                    subtitle = "Ask a concept question or generate a 7-day study schedule tailored for ANM & GNM or RRB NTPC."
                )

                is AiUiState.Loading -> Box(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = BrandPurple)
                }

                is AiUiState.Error -> Surface(
                    color = BrandRoseSoft,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(aiState.message, color = BrandRose, modifier = Modifier.padding(14.dp))
                }

                is AiUiState.Success -> Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        StatusPill("SAMPLE AI PREVIEW", BrandPurpleSoft, BrandPurple)
                        Spacer(Modifier.height(8.dp))
                        Text(aiState.response.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            aiState.response.answerText,
                            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = BengaliFontFamily)
                        )
                        Spacer(Modifier.height(10.dp))
                        aiState.response.keyPoints.forEach { pt ->
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = BrandGreen, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(pt, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationsView(
    notifications: List<NotificationEntity>,
    onMarkRead: (Long) -> Unit
) {
    if (notifications.isEmpty()) {
        Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
            EmptyStateBox(Icons.Filled.NotificationsNone, "All caught up!", "Live class, PDF, mock test & payment alerts appear here.")
        }
        return
    }
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(notifications, key = { it.id }) { n ->
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (n.isRead) MaterialTheme.colorScheme.surface else BrandOrangeSoft.copy(alpha = 0.45f),
                tonalElevation = 1.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onMarkRead(n.id) }
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(BrandPurpleSoft),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (n.type) {
                                "LIVE_CLASS" -> Icons.Filled.LiveTv
                                "PAYMENT" -> Icons.Filled.Verified
                                "PDF" -> Icons.Filled.PictureAsPdf
                                "MOCK_TEST" -> Icons.Filled.Quiz
                                else -> Icons.Filled.Notifications
                            },
                            contentDescription = null,
                            tint = BrandPurple
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(n.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text(n.body, style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(4.dp))
                        Text(formatTimestamp(n.createdAt), style = MaterialTheme.typography.labelSmall, color = SlateMuted)
                    }
                }
            }
        }
    }
}

@Composable
private fun ContactSupportView(
    settings: AppSettingsEntity,
    queries: List<ContactQueryEntity>,
    onSubmitQuery: (String, String) -> Unit
) {
    val context = LocalContext.current
    var subject by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Direct Support Channels", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${settings.supportPhone}"))
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Call")
                        }
                        Button(
                            onClick = {
                                val cleanNum = settings.whatsappNumber.filter { it.isDigit() }
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$cleanNum"))
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("WhatsApp")
                        }
                        OutlinedButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${settings.supportEmail}"))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.Email, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Email")
                        }
                    }
                    Text(
                        "Phone: ${settings.supportPhone} • Email: ${settings.supportEmail}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Send a Message to Target SSB", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = subject,
                        onValueChange = { subject = it },
                        label = { Text("Subject") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = message,
                        onValueChange = { message = it },
                        label = { Text("Your Question / Query") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = {
                            onSubmitQuery(subject, message)
                            subject = ""
                            message = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandOrange),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Submit Query")
                    }
                }
            }
        }

        if (queries.isNotEmpty()) {
            item { SectionHeader(title = "My Support Tickets") }
            items(queries, key = { it.id }) { q ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(q.subject, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            StatusPill(
                                q.status,
                                if (q.status == "RESOLVED") BrandGreenSoft else BrandAmberSoft,
                                if (q.status == "RESOLVED") BrandGreen else BrandAmber
                            )
                        }
                        Text(q.message, style = MaterialTheme.typography.bodySmall)
                        if (q.adminReply.isNotBlank()) {
                            Spacer(Modifier.height(6.dp))
                            Text("Admin Reply: ${q.adminReply}", style = MaterialTheme.typography.bodySmall, color = BrandBlue, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AboutTargetSsbView(settings: AppSettingsEntity) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = BrandBlueDeep,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .background(TargetGradients.BluePurple)
                        .padding(20.dp)
                ) {
                    Text("TARGET SSB", style = MaterialTheme.typography.headlineMedium, color = PureWhite, fontWeight = FontWeight.ExtraBold)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "আপনার সফলতার প্রস্তুতি শুরু হোক Target SSB-এর সাথে",
                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = BengaliFontFamily),
                        color = PureWhite
                    )
                    Text("Learn Today, Achieve Tomorrow.", style = MaterialTheme.typography.bodySmall, color = BrandOrangeSoft)
                }
            }
        }
        item {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Our Mission", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "Target SSB is dedicated to empowering students across West Bengal and India for competitive exams including ANM & GNM Nursing Entrance and RRB NTPC with bilingual Bengali + English live classes, recorded lectures, downloadable PDF notes, and full-length mock tests.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    HorizontalDivider()
                    Text("Helpline: ${settings.supportPhone}", style = MaterialTheme.typography.bodySmall)
                    Text("WhatsApp: ${settings.whatsappNumber}", style = MaterialTheme.typography.bodySmall)
                    Text("Email: ${settings.supportEmail}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
