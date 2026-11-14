package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.*
import com.example.ui.components.*
import com.example.ui.theme.*

enum class SecondaryDestination {
    RECORDED_CLASSES,
    PDF_NOTES,
    MOCK_PAPERS,
    AI_TUTOR,
    NOTIFICATIONS,
    CONTACT_US,
    ABOUT_APP
}

@Composable
fun HomeScreen(
    user: UserEntity,
    settings: AppSettingsEntity,
    courses: List<CourseEntity>,
    enrollments: List<EnrollmentEntity>,
    liveClasses: List<LiveClassEntity>,
    recordedClasses: List<RecordedClassEntity>,
    announcements: List<AnnouncementEntity>,
    unreadNotifications: Int,
    onOpenCoursesTab: () -> Unit,
    onOpenLiveTab: () -> Unit,
    onOpenTestsTab: () -> Unit,
    onOpenCourseDetail: (CourseEntity) -> Unit,
    onBuyCourse: (CourseEntity) -> Unit,
    onJoinLiveClass: (LiveClassEntity) -> Unit,
    onPlayRecordedVideo: (RecordedClassEntity) -> Unit,
    onNavigateSecondary: (SecondaryDestination) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val enrolledIds = remember(enrollments) { enrollments.map { it.courseId }.toSet() }
    val filteredCourses = remember(courses, searchQuery) {
        if (searchQuery.isBlank()) courses
        else courses.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
                it.category.contains(searchQuery, ignoreCase = true) ||
                it.subtitle.contains(searchQuery, ignoreCase = true)
        }
    }

    val liveNowClass = liveClasses.firstOrNull { it.status == "LIVE" } ?: liveClasses.firstOrNull()
    val continueWatching = recordedClasses.filter { it.watchedSeconds > 0 && !it.isCompleted }
        .ifEmpty { recordedClasses.take(2) }

    Column(modifier = Modifier.fillMaxSize()) {
        ScrollingAnnouncementTicker(tickerText = settings.tickerHeadline)
        TargetSsbTopBar(
            unreadNotifications = unreadNotifications,
            onNotificationsClick = { onNavigateSecondary(SecondaryDestination.NOTIFICATIONS) },
            onAiTutorClick = { onNavigateSecondary(SecondaryDestination.AI_TUTOR) }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Search Courses Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search ANM & GNM, RRB NTPC, Notes...") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Filled.Clear, contentDescription = "Clear search")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_search_input")
                )
            }

            // Student Welcome & Bengali Headline Card
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp,
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Welcome back, ${user.fullName.substringBefore(" ")} 👋",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "আপনার সফলতার প্রস্তুতি শুরু হোক Target SSB-এর সাথে",
                                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = BengaliFontFamily),
                                color = BrandOrangeDark,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "ID: ${user.studentCode} • ${enrolledIds.size} Enrolled Batch(es)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Image(
                            painter = painterResource(id = R.drawable.img_student_avatar),
                            contentDescription = "Student Avatar",
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            // Promotional Hero Banner
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(178.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .clickable { onOpenCoursesTab() }
                        .testTag("home_hero_banner")
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_hero_banner),
                        contentDescription = "Target SSB Promotional Course Banner",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(TargetGradients.HeroOverlay)
                            .padding(18.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxHeight(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatusPill(
                                text = "ADMISSION OPEN 2026",
                                containerColor = BrandOrange,
                                contentColor = PureWhite
                            )
                            Column {
                                Text(
                                    text = "ANM & GNM • RRB NTPC",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = PureWhite,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "Live Classes, PDF Notes & Mock Tests starting at ₹599/-",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = PureWhite.copy(alpha = 0.88f)
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = PureWhite,
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        text = "Explore Batches →",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = BrandBlueDeep,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Learning Hub Quick Grid (All Key Sections)
            item {
                SectionHeader(title = "Learning Hub")
                Spacer(Modifier.height(10.dp))
                val tiles = listOf(
                    QuickModuleTile("Live Classes", Icons.Filled.LiveTv, BrandRose, BrandRoseSoft) { onOpenLiveTab() },
                    QuickModuleTile("Recorded", Icons.Filled.PlayCircle, BrandBlue, BrandBlueSoft) {
                        onNavigateSecondary(SecondaryDestination.RECORDED_CLASSES)
                    },
                    QuickModuleTile("PDF Notes", Icons.Filled.PictureAsPdf, BrandPurple, BrandPurpleSoft) {
                        onNavigateSecondary(SecondaryDestination.PDF_NOTES)
                    },
                    QuickModuleTile("Mock Tests", Icons.Filled.Quiz, BrandGreen, BrandGreenSoft) { onOpenTestsTab() },
                    QuickModuleTile("Test Papers", Icons.AutoMirrored.Filled.MenuBook, BrandAmber, BrandAmberSoft) {
                        onNavigateSecondary(SecondaryDestination.MOCK_PAPERS)
                    },
                    QuickModuleTile("AI Tutor", Icons.Filled.AutoAwesome, BrandPurple, BrandPurpleSoft) {
                        onNavigateSecondary(SecondaryDestination.AI_TUTOR)
                    },
                    QuickModuleTile("Support", Icons.Filled.SupportAgent, BrandBlue, BrandBlueSoft) {
                        onNavigateSecondary(SecondaryDestination.CONTACT_US)
                    },
                    QuickModuleTile("About Us", Icons.Filled.Info, BrandOrange, BrandOrangeSoft) {
                        onNavigateSecondary(SecondaryDestination.ABOUT_APP)
                    }
                )
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    tiles.chunked(4).forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            rowItems.forEach { item ->
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable(onClick = item.onClick)
                                        .testTag("quick_tile_${item.label.lowercase().replace(" ", "_")}"),
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    tonalElevation = 1.dp
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(item.bg),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(item.icon, contentDescription = item.label, tint = item.tint)
                                        }
                                        Spacer(Modifier.height(6.dp))
                                        Text(
                                            text = item.label,
                                            style = MaterialTheme.typography.labelSmall,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Available Courses
            item {
                SectionHeader(
                    title = "Available Courses",
                    actionLabel = "View All",
                    onActionClick = onOpenCoursesTab
                )
                Spacer(Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    items(filteredCourses, key = { it.id }) { course ->
                        val isEnrolled = course.id in enrolledIds
                        Surface(
                            modifier = Modifier
                                .width(285.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { onOpenCourseDetail(course) }
                                .testTag("home_course_card_${course.id}"),
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 2.dp,
                            shadowElevation = 2.dp
                        ) {
                            Column {
                                Box(modifier = Modifier.height(132.dp).fillMaxWidth()) {
                                    Image(
                                        painter = painterResource(id = courseBannerDrawable(course.bannerKey)),
                                        contentDescription = course.title,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    StatusPill(
                                        text = course.category,
                                        containerColor = PureWhite.copy(alpha = 0.92f),
                                        contentColor = BrandBlueDeep,
                                        modifier = Modifier
                                            .align(Alignment.TopStart)
                                            .padding(10.dp)
                                    )
                                    if (isEnrolled) {
                                        StatusPill(
                                            text = "ENROLLED",
                                            containerColor = BrandGreen,
                                            contentColor = PureWhite,
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(10.dp)
                                        )
                                    }
                                }
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = course.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = course.subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.Bottom) {
                                            Text(
                                                text = "₹${course.priceInr}/-",
                                                style = MaterialTheme.typography.titleLarge,
                                                color = BrandOrange,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                            Spacer(Modifier.width(6.dp))
                                            Text(
                                                text = "₹${course.originalPriceInr}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                textDecoration = TextDecoration.LineThrough
                                            )
                                        }
                                        Button(
                                            onClick = {
                                                if (isEnrolled) onOpenCourseDetail(course) else onBuyCourse(course)
                                            },
                                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isEnrolled) BrandGreen else BrandOrange
                                            ),
                                            modifier = Modifier.height(38.dp)
                                        ) {
                                            Text(if (isEnrolled) "Study Now" else "Buy Now")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Live Class Highlight
            if (liveNowClass != null) {
                item {
                    SectionHeader(
                        title = "Live Class Spotlight",
                        actionLabel = "All Live",
                        onActionClick = onOpenLiveTab
                    )
                    Spacer(Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxWidth(),
                        tonalElevation = 2.dp
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF1E1B4B), Color(0xFF312E81))
                                    )
                                )
                                .padding(16.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    StatusPill(
                                        text = if (liveNowClass.status == "LIVE") "● LIVE NOW" else liveNowClass.status,
                                        containerColor = if (liveNowClass.status == "LIVE") BrandRose else BrandBlue,
                                        contentColor = PureWhite
                                    )
                                    Text(
                                        text = liveNowClass.subject,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = PureWhite.copy(alpha = 0.8f)
                                    )
                                }
                                Spacer(Modifier.height(10.dp))
                                Text(
                                    text = liveNowClass.title,
                                    style = MaterialTheme.typography.titleMedium.copy(fontFamily = BengaliFontFamily),
                                    color = PureWhite,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "Faculty: ${liveNowClass.teacherName} • ${liveNowClass.durationMinutes} mins",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = PureWhite.copy(alpha = 0.82f)
                                )
                                Spacer(Modifier.height(12.dp))
                                Button(
                                    onClick = { onJoinLiveClass(liveNowClass) },
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandOrange),
                                    modifier = Modifier.testTag("home_join_live_button")
                                ) {
                                    Icon(Icons.Filled.PlayArrow, contentDescription = null)
                                    Spacer(Modifier.width(6.dp))
                                    Text("Join Live Class")
                                }
                            }
                        }
                    }
                }
            }

            // Continue Watching Recorded Classes
            if (continueWatching.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Continue Watching",
                        actionLabel = "Library",
                        onActionClick = { onNavigateSecondary(SecondaryDestination.RECORDED_CLASSES) }
                    )
                    Spacer(Modifier.height(10.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        continueWatching.take(2).forEach { video ->
                            val isEnrolled = video.courseId in enrolledIds
                            val progress = if (video.durationSeconds > 0)
                                (video.watchedSeconds.toFloat() / video.durationSeconds.toFloat()).coerceIn(0f, 1f)
                            else 0f
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surface,
                                tonalElevation = 1.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable {
                                        if (isEnrolled) onPlayRecordedVideo(video)
                                        else onNavigateSecondary(SecondaryDestination.RECORDED_CLASSES)
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(if (isEnrolled) BrandBlueSoft else SoftGreySurface),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (isEnrolled) Icons.Filled.PlayCircle else Icons.Filled.Lock,
                                            contentDescription = null,
                                            tint = if (isEnrolled) BrandBlue else SlateMuted
                                        )
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = video.title,
                                            style = MaterialTheme.typography.titleSmall.copy(fontFamily = BengaliFontFamily),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${video.chapterName} • ${formatDurationSeconds(video.watchedSeconds)} / ${formatDurationSeconds(video.durationSeconds)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(Modifier.height(6.dp))
                                        LinearProgressIndicator(
                                            progress = { progress },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(6.dp)
                                                .clip(RoundedCornerShape(4.dp)),
                                            color = BrandOrange
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Latest Announcements
            if (announcements.isNotEmpty()) {
                item {
                    SectionHeader(title = "Latest Announcements")
                    Spacer(Modifier.height(10.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        announcements.take(3).forEach { ann ->
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surface,
                                tonalElevation = 1.dp,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(BrandAmberSoft),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Filled.Campaign, contentDescription = null, tint = BrandAmber)
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column {
                                        Text(ann.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                        Spacer(Modifier.height(2.dp))
                                        Text(
                                            text = ann.message,
                                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = BengaliFontFamily),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Contact Support Footer Card
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = BrandBlueSoft,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .clickable { onNavigateSecondary(SecondaryDestination.CONTACT_US) }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Need Help with Admission or Syllabus?",
                                style = MaterialTheme.typography.titleSmall,
                                color = BrandBlueDeep,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Call / WhatsApp: ${settings.supportPhone}",
                                style = MaterialTheme.typography.bodySmall,
                                color = BrandBlue
                            )
                        }
                        FilledTonalButton(
                            onClick = { onNavigateSecondary(SecondaryDestination.CONTACT_US) }
                        ) {
                            Text("Contact")
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

private data class QuickModuleTile(
    val label: String,
    val icon: ImageVector,
    val tint: Color,
    val bg: Color,
    val onClick: () -> Unit
)
