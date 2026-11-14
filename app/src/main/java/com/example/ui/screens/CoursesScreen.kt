@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.local.*
import com.example.data.pdf.PdfGenerator
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.CheckoutUiState
import java.io.File

@Composable
fun CoursesScreen(
    courses: List<CourseEntity>,
    enrollments: List<EnrollmentEntity>,
    payments: List<PaymentEntity>,
    onOpenCourseDetail: (CourseEntity) -> Unit,
    onBuyCourse: (CourseEntity) -> Unit
) {
    var selectedCategory by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }

    val enrollmentByCourse = remember(enrollments) { enrollments.associateBy { it.courseId } }
    val categories = remember(courses) {
        listOf("All", "My Courses") + courses.map { it.category }.distinct()
    }

    val filteredCourses = remember(courses, selectedCategory, searchQuery, enrollmentByCourse) {
        courses.filter { course ->
            val catMatch = when (selectedCategory) {
                "All" -> true
                "My Courses" -> course.id in enrollmentByCourse
                else -> course.category == selectedCategory
            }
            val queryMatch = searchQuery.isBlank() ||
                course.title.contains(searchQuery, ignoreCase = true) ||
                course.subtitle.contains(searchQuery, ignoreCase = true)
            catMatch && queryMatch
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Column {
                    Text("Courses & Batches", fontWeight = FontWeight.Bold)
                    Text(
                        "ANM & GNM • RRB NTPC Preparation",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search courses or subjects...") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("courses_search_input")
                )
            }

            item {
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

            if (filteredCourses.isEmpty()) {
                item {
                    EmptyStateBox(
                        icon = Icons.Filled.School,
                        title = "No matching courses",
                        subtitle = "Try clearing the filter or search term to explore all Target SSB batches."
                    )
                }
            } else {
                items(filteredCourses, key = { it.id }) { course ->
                    val enrollment = enrollmentByCourse[course.id]
                    val isEnrolled = enrollment != null
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 2.dp,
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { onOpenCourseDetail(course) }
                            .testTag("course_card_${course.id}")
                    ) {
                        Column {
                            Box(modifier = Modifier.height(160.dp).fillMaxWidth()) {
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
                                        .padding(12.dp)
                                )
                                if (isEnrolled) {
                                    StatusPill(
                                        text = "PURCHASED • ${enrollment!!.progressPercent}%",
                                        containerColor = BrandGreen,
                                        contentColor = PureWhite,
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(12.dp)
                                    )
                                }
                            }

                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = course.title,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = course.subtitle,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(10.dp))

                                val features = course.featuresCsv.split(",").map { it.trim() }.filter { it.isNotBlank() }
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    features.forEach { feat ->
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Filled.CheckCircle,
                                                contentDescription = null,
                                                tint = BrandGreen,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(Modifier.width(6.dp))
                                            Text(feat, style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                }

                                if (isEnrolled && enrollment != null) {
                                    Spacer(Modifier.height(10.dp))
                                    LinearProgressIndicator(
                                        progress = { (enrollment.progressPercent / 100f).coerceIn(0f, 1f) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(7.dp)
                                            .clip(RoundedCornerShape(4.dp)),
                                        color = BrandGreen
                                    )
                                }

                                Spacer(Modifier.height(14.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.Bottom) {
                                        Text(
                                            text = "₹${course.priceInr}/-",
                                            style = MaterialTheme.typography.headlineSmall,
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
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedButton(onClick = { onOpenCourseDetail(course) }) {
                                            Text("Details")
                                        }
                                        Button(
                                            onClick = {
                                                if (isEnrolled) onOpenCourseDetail(course) else onBuyCourse(course)
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isEnrolled) BrandGreen else BrandOrange
                                            ),
                                            modifier = Modifier.testTag("buy_course_button_${course.id}")
                                        ) {
                                            Text(if (isEnrolled) "Open" else "Buy Now")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (payments.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(6.dp))
                    SectionHeader(title = "Recent Purchase History")
                    Spacer(Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        payments.take(3).forEach { pay ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surface,
                                tonalElevation = 1.dp,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(pay.courseTitle, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                        Text(
                                            "${pay.transactionId} • ${pay.method} • ${formatTimestamp(pay.createdAt)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    StatusPill(
                                        text = "₹${pay.amountInr} ${pay.status}",
                                        containerColor = if (pay.status == "SUCCESS") BrandGreenSoft else BrandRoseSoft,
                                        contentColor = if (pay.status == "SUCCESS") BrandGreen else BrandRose
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CourseDetailDialog(
    course: CourseEntity,
    enrollment: EnrollmentEntity?,
    liveClasses: List<LiveClassEntity>,
    recordedClasses: List<RecordedClassEntity>,
    pdfNotes: List<PdfNoteEntity>,
    mockTests: List<MockTestEntity>,
    onBuyNow: () -> Unit,
    onPlayRecorded: (RecordedClassEntity) -> Unit,
    onJoinLive: (LiveClassEntity) -> Unit,
    onOpenPdf: (PdfNoteEntity) -> Unit,
    onStartTest: (MockTestEntity) -> Unit,
    onDismiss: () -> Unit
) {
    val isEnrolled = enrollment != null
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Overview", "Videos", "PDF Notes", "Live & Tests")

    val courseVideos = remember(recordedClasses, course.id) { recordedClasses.filter { it.courseId == course.id } }
    val coursePdfs = remember(pdfNotes, course.id) { pdfNotes.filter { it.courseId == course.id } }
    val courseLives = remember(liveClasses, course.id) { liveClasses.filter { it.courseId == course.id } }
    val courseTests = remember(mockTests, course.id) { mockTests.filter { it.courseId == course.id && it.isPublished } }

    Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
            ) {
                TopAppBar(
                    title = { Text(course.title, maxLines = 1) },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )

                TabRow(selectedTabIndex = selectedTab) {
                    tabs.forEachIndexed { idx, label ->
                        Tab(
                            selected = selectedTab == idx,
                            onClick = { selectedTab = idx },
                            text = { Text(label, maxLines = 1) }
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    when (selectedTab) {
                        0 -> {
                            item {
                                Image(
                                    painter = painterResource(id = courseBannerDrawable(course.bannerKey)),
                                    contentDescription = course.title,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(185.dp)
                                        .clip(RoundedCornerShape(18.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(Modifier.height(12.dp))
                                Text(course.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                                Text(
                                    "Faculty: ${course.instructor} • Validity: ${course.validityDays} days",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(10.dp))
                                Text(course.description, style = MaterialTheme.typography.bodyLarge)
                                Spacer(Modifier.height(14.dp))
                                if (enrollment != null) {
                                    Surface(
                                        color = BrandGreenSoft,
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Text(
                                                "Enrolled Batch • Progress ${enrollment.progressPercent}%",
                                                style = MaterialTheme.typography.titleSmall,
                                                color = BrandGreen
                                            )
                                            Text(
                                                "Valid until: ${formatTimestamp(enrollment.expiryAt)}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = BrandGreen
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        1 -> {
                            if (courseVideos.isEmpty()) {
                                item {
                                    EmptyStateBox(Icons.Filled.VideoLibrary, "No videos yet", "Recorded classes will appear here.")
                                }
                            } else {
                                items(courseVideos, key = { it.id }) { vid ->
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        tonalElevation = 1.dp,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                if (isEnrolled) onPlayRecorded(vid) else onBuyNow()
                                            }
                                    ) {
                                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                if (isEnrolled) Icons.Filled.PlayCircle else Icons.Filled.Lock,
                                                contentDescription = null,
                                                tint = if (isEnrolled) BrandBlue else SlateMuted
                                            )
                                            Spacer(Modifier.width(12.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(vid.title, style = MaterialTheme.typography.titleSmall)
                                                Text(
                                                    "${vid.chapterName} • ${formatDurationSeconds(vid.durationSeconds)}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            if (!isEnrolled) {
                                                StatusPill("Locked", BrandOrangeSoft, BrandOrangeDark)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        2 -> {
                            if (coursePdfs.isEmpty()) {
                                item {
                                    EmptyStateBox(Icons.Filled.PictureAsPdf, "No PDF Notes yet", "Study notes will appear here.")
                                }
                            } else {
                                items(coursePdfs, key = { it.id }) { pdf ->
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        tonalElevation = 1.dp,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { if (isEnrolled) onOpenPdf(pdf) else onBuyNow() }
                                    ) {
                                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                if (isEnrolled) Icons.Filled.PictureAsPdf else Icons.Filled.Lock,
                                                contentDescription = null,
                                                tint = if (isEnrolled) BrandPurple else SlateMuted
                                            )
                                            Spacer(Modifier.width(12.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(pdf.title, style = MaterialTheme.typography.titleSmall)
                                                Text(
                                                    "${pdf.subject} • ${pdf.pageCount} pages • ${pdf.fileSizeLabel}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            if (!isEnrolled) StatusPill("Locked", BrandOrangeSoft, BrandOrangeDark)
                                        }
                                    }
                                }
                            }
                        }

                        3 -> {
                            items(courseLives, key = { "live_${it.id}" }) { live ->
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    tonalElevation = 1.dp,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            StatusPill(
                                                live.status,
                                                if (live.status == "LIVE") BrandRoseSoft else BrandBlueSoft,
                                                if (live.status == "LIVE") BrandRose else BrandBlue
                                            )
                                            Spacer(Modifier.height(4.dp))
                                            Text(live.title, style = MaterialTheme.typography.titleSmall)
                                            Text("By ${live.teacherName}", style = MaterialTheme.typography.bodySmall)
                                        }
                                        Button(onClick = { if (isEnrolled) onJoinLive(live) else onBuyNow() }) {
                                            Text(if (isEnrolled) "Join" else "Unlock")
                                        }
                                    }
                                }
                            }
                            items(courseTests, key = { "test_${it.id}" }) { test ->
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    tonalElevation = 1.dp,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(test.title, style = MaterialTheme.typography.titleSmall)
                                            Text(
                                                "${test.durationMinutes} mins • ${test.totalMarks} marks",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                        Button(
                                            onClick = { onStartTest(test) },
                                            colors = ButtonDefaults.buttonColors(containerColor = BrandGreen)
                                        ) {
                                            Text("Start Test")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (!isEnrolled) {
                    Surface(tonalElevation = 4.dp, modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Course Fee", style = MaterialTheme.typography.labelSmall)
                                Text(
                                    "₹${course.priceInr}/-",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = BrandOrange,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            Button(
                                onClick = onBuyNow,
                                colors = ButtonDefaults.buttonColors(containerColor = BrandOrange),
                                modifier = Modifier.height(48.dp)
                            ) {
                                Text("Enroll & Unlock Now")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CheckoutDialog(
    course: CourseEntity,
    checkoutState: CheckoutUiState,
    onPay: (method: String, simulateFailure: Boolean) -> Unit,
    onViewReceipt: (File) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedMethod by remember { mutableStateOf("UPI") }
    var upiId by remember { mutableStateOf("student@okaxis") }
    var cardLast4 by remember { mutableStateOf("4242 8810") }
    var selectedBank by remember { mutableStateOf("State Bank of India (SBI)") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Secure Checkout", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("Razorpay Verified Gateway • INR (₹)", style = MaterialTheme.typography.labelSmall, color = BrandBlue)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Close checkout")
                    }
                }
                Spacer(Modifier.height(10.dp))
                Surface(
                    color = SoftGreySurface,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(course.title, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                        Text(
                            "₹${course.priceInr}/-",
                            style = MaterialTheme.typography.titleLarge,
                            color = BrandOrange,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))

                when (checkoutState) {
                    is CheckoutUiState.Idle -> {
                        Text("Select Payment Method", style = MaterialTheme.typography.labelLarge)
                        Spacer(Modifier.height(8.dp))
                        val methods = listOf("UPI", "Debit Card", "Credit Card", "Net Banking")
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            methods.chunked(2).forEach { pair ->
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                    pair.forEach { method ->
                                        FilterChip(
                                            selected = selectedMethod == method,
                                            onClick = { selectedMethod = method },
                                            label = { Text(method) },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        when (selectedMethod) {
                            "UPI" -> OutlinedTextField(
                                value = upiId,
                                onValueChange = { upiId = it },
                                label = { Text("UPI ID (GPay / PhonePe / Paytm)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            "Debit Card", "Credit Card" -> OutlinedTextField(
                                value = cardLast4,
                                onValueChange = { cardLast4 = it },
                                label = { Text("Card Number") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            else -> OutlinedTextField(
                                value = selectedBank,
                                onValueChange = { selectedBank = it },
                                label = { Text("Bank Name") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Spacer(Modifier.height(14.dp))
                        Button(
                            onClick = { onPay(selectedMethod, false) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("confirm_pay_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandGreen)
                        ) {
                            Icon(Icons.Filled.VerifiedUser, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Pay ₹${course.priceInr}/- Securely")
                        }
                        Spacer(Modifier.height(6.dp))
                        TextButton(
                            onClick = { onPay(selectedMethod, true) },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text("Simulate Failed Payment Test", style = MaterialTheme.typography.labelSmall, color = BrandRose)
                        }
                    }

                    is CheckoutUiState.Verifying -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = BrandOrange)
                            Spacer(Modifier.height(12.dp))
                            Text("Verifying payment on server...", style = MaterialTheme.typography.titleSmall)
                            Text("Do not close this window", style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    is CheckoutUiState.Success -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = BrandGreen, modifier = Modifier.size(54.dp))
                            Spacer(Modifier.height(8.dp))
                            Text("Payment Verified!", style = MaterialTheme.typography.titleLarge, color = BrandGreen, fontWeight = FontWeight.Bold)
                            Text("Transaction ID: ${checkoutState.payment.transactionId}", style = MaterialTheme.typography.bodySmall)
                            Spacer(Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = {
                                        val file = PdfGenerator.createReceiptPdf(context, checkoutState.payment)
                                        onViewReceipt(file)
                                    }
                                ) {
                                    Icon(Icons.Filled.Receipt, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Receipt PDF")
                                }
                                Button(
                                    onClick = onDismiss,
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandOrange)
                                ) {
                                    Text("Start Learning")
                                }
                            }
                        }
                    }

                    is CheckoutUiState.Failed -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Filled.Cancel, contentDescription = null, tint = BrandRose, modifier = Modifier.size(54.dp))
                            Spacer(Modifier.height(8.dp))
                            Text("Payment Failed", style = MaterialTheme.typography.titleLarge, color = BrandRose, fontWeight = FontWeight.Bold)
                            Text(checkoutState.message, style = MaterialTheme.typography.bodySmall)
                            Spacer(Modifier.height(12.dp))
                            Button(onClick = onDismiss) {
                                Text("Close & Retry")
                            }
                        }
                    }
                }
            }
        }
    }
}
