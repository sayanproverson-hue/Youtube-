@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.MenuBook
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
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.local.*
import com.example.data.pdf.PdfGenerator
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatusPill
import com.example.ui.components.formatTimestamp
import com.example.ui.theme.*
import java.io.File

@Composable
fun ProfileScreen(
    user: UserEntity,
    settings: AppSettingsEntity,
    courses: List<CourseEntity>,
    enrollments: List<EnrollmentEntity>,
    payments: List<PaymentEntity>,
    attempts: List<MockAttemptEntity>,
    darkModeEnabled: Boolean,
    onToggleDarkMode: () -> Unit,
    onUpdateProfile: (fullName: String, mobile: String, avatarUri: String?) -> Unit,
    onOpenCourse: (CourseEntity) -> Unit,
    onPreviewReceiptPdf: (PaymentEntity, File) -> Unit,
    onNavigateSecondary: (SecondaryDestination) -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    var showEditDialog by remember { mutableStateOf(false) }
    var showCertificateDialog by remember { mutableStateOf<EnrollmentEntity?>(null) }
    val courseMap = remember(courses) { courses.associateBy { it.id } }

    // Zero-permission Android Photo Picker
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                onUpdateProfile(user.fullName, user.mobile, uri.toString())
            }
        }
    )

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Student Profile", fontWeight = FontWeight.Bold) },
            actions = {
                IconButton(onClick = onToggleDarkMode) {
                    Icon(
                        if (darkModeEnabled) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                        contentDescription = "Toggle Dark Mode"
                    )
                }
                IconButton(
                    onClick = { showEditDialog = true },
                    modifier = Modifier.testTag("edit_profile_button")
                ) {
                    Icon(Icons.Filled.Edit, contentDescription = "Edit Profile")
                }
            }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Header Card
            item {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(86.dp)
                                .clip(CircleShape)
                                .clickable {
                                    photoPicker.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                        ) {
                            if (user.avatarUri != null) {
                                AsyncImage(
                                    model = user.avatarUri,
                                    contentDescription = "Profile Photo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Image(
                                    painter = painterResource(id = R.drawable.img_student_avatar),
                                    contentDescription = "Profile Photo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(BrandOrange),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.CameraAlt, contentDescription = "Change Photo", tint = PureWhite, modifier = Modifier.size(14.dp))
                            }
                        }

                        Spacer(Modifier.height(10.dp))
                        Text(user.fullName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(2.dp))
                        StatusPill("Student ID: ${user.studentCode}", BrandBlueSoft, BrandBlueDeep)
                        Spacer(Modifier.height(8.dp))
                        Text("${user.mobile} • ${user.email}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // Purchased Courses + Expiry + Learning Progress + Certificates
            item {
                SectionHeader(title = "Purchased Courses (${enrollments.size})")
            }
            if (enrollments.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "No courses purchased yet. Explore ANM & GNM or RRB NTPC batches!",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            } else {
                items(enrollments, key = { it.id }) { en ->
                    val course = courseMap[en.courseId]
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 2.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { course?.let(onOpenCourse) }
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = course?.title ?: "Course #${en.courseId}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                StatusPill("${en.progressPercent}% Done", BrandGreenSoft, BrandGreen)
                            }
                            Spacer(Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { (en.progressPercent / 100f).coerceIn(0f, 1f) },
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(4.dp)),
                                color = BrandOrange
                            )
                            Spacer(Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Expires: ${formatTimestamp(en.expiryAt).substringBefore(",")}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (settings.certificatesEnabled) {
                                    TextButton(onClick = { showCertificateDialog = en }) {
                                        Icon(Icons.Filled.WorkspacePremium, contentDescription = null, modifier = Modifier.size(16.dp), tint = BrandAmber)
                                        Spacer(Modifier.width(4.dp))
                                        Text("Certificate", color = BrandAmber)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Payment History with Receipt PDF
            item {
                SectionHeader(title = "Payment History")
            }
            if (payments.isEmpty()) {
                item {
                    Text(
                        "No transactions recorded yet.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(payments, key = { it.id }) { pay ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
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
                                Text(pay.courseTitle, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                Text(
                                    "${pay.transactionId} • ₹${pay.amountInr}/- • ${pay.method} (${pay.status})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (pay.status == "SUCCESS") {
                                IconButton(
                                    onClick = {
                                        val file = PdfGenerator.createReceiptPdf(context, pay)
                                        onPreviewReceiptPdf(pay, file)
                                    }
                                ) {
                                    Icon(Icons.Filled.ReceiptLong, contentDescription = "Download Receipt", tint = BrandOrange)
                                }
                            }
                        }
                    }
                }
            }

            // Mock Test Summary
            if (attempts.isNotEmpty()) {
                item {
                    SectionHeader(title = "Recent Mock Test Results")
                    Spacer(Modifier.height(8.dp))
                    val latest = attempts.first()
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = BrandPurpleSoft,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(latest.testTitle, style = MaterialTheme.typography.titleSmall, color = BrandPurple, fontWeight = FontWeight.Bold)
                                Text("Score: ${latest.scoreMarks}/${latest.maxMarks} • Correct: ${latest.correctCount}", style = MaterialTheme.typography.bodySmall)
                            }
                            Text("${attempts.size} Tests", style = MaterialTheme.typography.labelLarge, color = BrandPurple)
                        }
                    }
                }
            }

            // Additional Menu Items (Section 16)
            item {
                SectionHeader(title = "Study Tools & Support")
                Spacer(Modifier.height(8.dp))
                val menuItems = listOf(
                    Triple("Recorded Classes", Icons.Filled.PlayCircle, SecondaryDestination.RECORDED_CLASSES),
                    Triple("PDF Notes Library", Icons.Filled.PictureAsPdf, SecondaryDestination.PDF_NOTES),
                    Triple("Mock Test Papers", Icons.AutoMirrored.Filled.MenuBook, SecondaryDestination.MOCK_PAPERS),
                    Triple("AI Study Tutor", Icons.Filled.AutoAwesome, SecondaryDestination.AI_TUTOR),
                    Triple("Notifications", Icons.Filled.Notifications, SecondaryDestination.NOTIFICATIONS),
                    Triple("Contact Us & Help Support", Icons.Filled.SupportAgent, SecondaryDestination.CONTACT_US),
                    Triple("About Target SSB", Icons.Filled.Info, SecondaryDestination.ABOUT_APP)
                )
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        menuItems.forEachIndexed { idx, (label, icon, dest) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onNavigateSecondary(dest) }
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(icon, contentDescription = null, tint = BrandOrange)
                                Spacer(Modifier.width(14.dp))
                                Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = SlateMuted)
                            }
                            if (idx < menuItems.lastIndex) HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    }
                }
            }

            // Logout
            item {
                OutlinedButton(
                    onClick = onLogout,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandRose),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("logout_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Logout")
                }
                Spacer(Modifier.height(12.dp))
            }
        }
    }

    if (showEditDialog) {
        var nameInput by remember { mutableStateOf(user.fullName) }
        var mobileInput by remember { mutableStateOf(user.mobile) }
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Profile") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = nameInput, onValueChange = { nameInput = it }, label = { Text("Full Name") }, singleLine = true)
                    OutlinedTextField(value = mobileInput, onValueChange = { mobileInput = it }, label = { Text("Mobile Number") }, singleLine = true)
                }
            },
            confirmButton = {
                Button(onClick = {
                    onUpdateProfile(nameInput, mobileInput, user.avatarUri)
                    showEditDialog = false
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showEditDialog = false }) { Text("Cancel") } }
        )
    }

    showCertificateDialog?.let { en ->
        val courseName = courseMap[en.courseId]?.title ?: "Target SSB Course"
        AlertDialog(
            onDismissRequest = { showCertificateDialog = null },
            title = { Text("Certificate of Achievement") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Filled.WorkspacePremium, contentDescription = null, tint = BrandAmber, modifier = Modifier.size(56.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("TARGET SSB LEARNING PLATFORM", style = MaterialTheme.typography.labelLarge, color = BrandBlueDeep)
                    Spacer(Modifier.height(6.dp))
                    Text("This certifies that ${user.fullName} (${user.studentCode}) is actively enrolled in $courseName with ${en.progressPercent}% learning completion.")
                }
            },
            confirmButton = { TextButton(onClick = { showCertificateDialog = null }) { Text("Close") } }
        )
    }
}
