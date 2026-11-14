@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.*
import com.example.data.pdf.PdfGenerator
import com.example.ui.components.*
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun TestsScreen(
    mockTests: List<MockTestEntity>,
    mockPapers: List<MockPaperEntity>,
    attempts: List<MockAttemptEntity>,
    onStartMockTest: (MockTestEntity) -> Unit,
    onPreviewPaperPdf: (MockPaperEntity, File) -> Unit
) {
    val context = LocalContext.current
    var selectedSection by remember { mutableIntStateOf(0) }
    var answerKeyPaper by remember { mutableStateOf<MockPaperEntity?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Column {
                    Text("Mock Tests & Papers", fontWeight = FontWeight.Bold)
                    Text(
                        "Timed MCQ Exams, Question Papers & Analytics",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        )

        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            listOf("Online Tests", "Test Papers", "My Results").forEachIndexed { index, label ->
                SegmentedButton(
                    selected = selectedSection == index,
                    onClick = { selectedSection = index },
                    shape = SegmentedButtonDefaults.itemShape(index, 3)
                ) {
                    Text(label, maxLines = 1)
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (selectedSection) {
                0 -> {
                    val published = mockTests.filter { it.isPublished }
                    if (published.isEmpty()) {
                        item {
                            EmptyStateBox(Icons.Filled.Quiz, "No published mock tests", "New online tests will appear here.")
                        }
                    } else {
                        items(published, key = { it.id }) { test ->
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.surface,
                                tonalElevation = 2.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("mock_test_card_${test.id}")
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        StatusPill(test.subject, BrandPurpleSoft, BrandPurple)
                                        Text(
                                            "-${test.negativeMarksPerWrong} negative",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = BrandRose
                                        )
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    Text(test.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text(
                                        test.courseTitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            "⏱ ${test.durationMinutes} mins • 🏆 ${test.totalMarks} Marks",
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                        Button(
                                            onClick = { onStartMockTest(test) },
                                            colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                                            modifier = Modifier.testTag("start_mock_test_${test.id}")
                                        ) {
                                            Text("Start Test")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    if (mockPapers.isEmpty()) {
                        item {
                            EmptyStateBox(Icons.Filled.Description, "No papers uploaded", "Previous year & model papers appear here.")
                        }
                    } else {
                        items(mockPapers, key = { it.id }) { paper ->
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = MaterialTheme.colorScheme.surface,
                                tonalElevation = 2.dp,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        StatusPill("${paper.paperType} • ${paper.yearLabel}", BrandAmberSoft, BrandAmber)
                                        Text(paper.courseTitle.substringBefore(" "), style = MaterialTheme.typography.labelSmall)
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    Text(paper.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        paper.questionsSummary,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(Modifier.height(12.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedButton(
                                            onClick = { answerKeyPaper = paper },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Filled.Key, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("Answer Key")
                                        }
                                        Button(
                                            onClick = {
                                                val file = PdfGenerator.createMockPaperPdf(context, paper)
                                                onPreviewPaperPdf(paper, file)
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = BrandOrange),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Filled.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("PDF Preview")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    if (attempts.isEmpty()) {
                        item {
                            EmptyStateBox(
                                icon = Icons.Filled.Assessment,
                                title = "No mock tests attempted yet",
                                subtitle = "Attempt any online mock test to view your accuracy, score & detailed breakdown."
                            )
                        }
                    } else {
                        items(attempts, key = { it.id }) { attempt ->
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = MaterialTheme.colorScheme.surface,
                                tonalElevation = 2.dp,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(attempt.testTitle, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                        StatusPill("${attempt.scoreMarks} / ${attempt.maxMarks}", BrandGreenSoft, BrandGreen)
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("✓ Correct: ${attempt.correctCount}", color = BrandGreen, style = MaterialTheme.typography.labelMedium)
                                        Text("✕ Wrong: ${attempt.wrongCount}", color = BrandRose, style = MaterialTheme.typography.labelMedium)
                                        Text("○ Skipped: ${attempt.unansweredCount}", color = SlateMuted, style = MaterialTheme.typography.labelMedium)
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        "Time taken: ${formatDurationSeconds(attempt.timeTakenSeconds)} • ${formatTimestamp(attempt.attemptedAt)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    answerKeyPaper?.let { paper ->
        AlertDialog(
            onDismissRequest = { answerKeyPaper = null },
            title = { Text("Answer Key • ${paper.yearLabel}") },
            text = { Text(paper.answerKeyText, style = MaterialTheme.typography.bodySmall) },
            confirmButton = {
                TextButton(onClick = { answerKeyPaper = null }) { Text("Close") }
            }
        )
    }
}

@Composable
fun MockTestRunnerDialog(
    test: MockTestEntity,
    loadQuestions: suspend (Long) -> List<MockQuestionEntity>,
    onSubmitTest: suspend (List<MockQuestionEntity>, Map<Long, Int>, Int) -> MockAttemptEntity?,
    onDismiss: () -> Unit
) {
    var questions by remember { mutableStateOf<List<MockQuestionEntity>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var currentIndex by remember { mutableIntStateOf(0) }
    val selectedAnswers = remember { mutableStateMapOf<Long, Int>() }
    val markedForReview = remember { mutableStateListOf<Long>() }
    var remainingSeconds by remember(test.id) { mutableIntStateOf(test.durationMinutes * 60) }
    var submittedAttempt by remember { mutableStateOf<MockAttemptEntity?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(test.id) {
        questions = loadQuestions(test.id)
        isLoading = false
    }

    fun triggerSubmit() {
        if (isSubmitting || submittedAttempt != null || questions.isEmpty()) return
        isSubmitting = true
        val elapsed = (test.durationMinutes * 60 - remainingSeconds).coerceAtLeast(1)
        scope.launch {
            submittedAttempt = onSubmitTest(questions, selectedAnswers.toMap(), elapsed)
            isSubmitting = false
        }
    }

    LaunchedEffect(isLoading, submittedAttempt) {
        if (!isLoading && submittedAttempt == null) {
            while (remainingSeconds > 0 && submittedAttempt == null) {
                delay(1000)
                remainingSeconds--
            }
            if (remainingSeconds <= 0 && submittedAttempt == null) {
                triggerSubmit()
            }
        }
    }

    BackHandler(enabled = true) { onDismiss() }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
            ) {
                TopAppBar(
                    title = {
                        Column {
                            Text(test.title, maxLines = 1, style = MaterialTheme.typography.titleMedium)
                            if (submittedAttempt == null) {
                                Text(
                                    "Time Left: ${formatDurationSeconds(remainingSeconds)}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (remainingSeconds < 60) BrandRose else BrandOrange
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Exit Test")
                        }
                    },
                    actions = {
                        if (submittedAttempt == null && questions.isNotEmpty()) {
                            Button(
                                onClick = { triggerSubmit() },
                                colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .testTag("submit_mock_test_button")
                            ) {
                                Text("Submit")
                            }
                        }
                    }
                )

                when {
                    isLoading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = BrandOrange)
                        }
                    }

                    questions.isEmpty() -> {
                        Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                            EmptyStateBox(Icons.Filled.Quiz, "No questions added yet", "Ask Admin to add questions to this test.")
                        }
                    }

                    submittedAttempt != null -> {
                        val result = submittedAttempt!!
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
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
                                            .padding(20.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("Performance Report", style = MaterialTheme.typography.titleMedium, color = PureWhite.copy(alpha = 0.85f))
                                        Spacer(Modifier.height(6.dp))
                                        Text(
                                            "${result.scoreMarks} / ${result.maxMarks}",
                                            style = MaterialTheme.typography.displaySmall,
                                            color = PureWhite,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                        Spacer(Modifier.height(10.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceEvenly
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text("${result.correctCount}", color = BrandGreenSoft, fontWeight = FontWeight.Bold)
                                                Text("Correct", style = MaterialTheme.typography.labelSmall, color = PureWhite)
                                            }
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text("${result.wrongCount}", color = BrandRoseSoft, fontWeight = FontWeight.Bold)
                                                Text("Wrong", style = MaterialTheme.typography.labelSmall, color = PureWhite)
                                            }
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text("${result.unansweredCount}", color = BrandAmberSoft, fontWeight = FontWeight.Bold)
                                                Text("Skipped", style = MaterialTheme.typography.labelSmall, color = PureWhite)
                                            }
                                        }
                                    }
                                }
                            }

                            item {
                                SectionHeader(title = "Question-by-Question Solutions")
                            }

                            items(questions.indices.toList()) { idx ->
                                val q = questions[idx]
                                val chosen = selectedAnswers[q.id]
                                val options = listOf(q.optionA, q.optionB, q.optionC, q.optionD)
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    tonalElevation = 1.dp,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text("Q${idx + 1}. ${q.questionText}", style = MaterialTheme.typography.titleSmall.copy(fontFamily = BengaliFontFamily))
                                        Spacer(Modifier.height(6.dp))
                                        Text(
                                            "Correct Answer: ${options[q.correctOptionIndex]}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = BrandGreen,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            "Your Answer: ${chosen?.let { options[it] } ?: "Not Answered"}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (chosen == q.correctOptionIndex) BrandGreen else BrandRose
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            "Explanation: ${q.explanation}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            item {
                                Button(
                                    onClick = onDismiss,
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandOrange)
                                ) {
                                    Text("Done")
                                }
                            }
                        }
                    }

                    else -> {
                        val q = questions[currentIndex]
                        val options = listOf(q.optionA, q.optionB, q.optionC, q.optionD)
                        val isMarked = q.id in markedForReview

                        // Question Navigation Palette
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(questions.size) { idx ->
                                val itemQ = questions[idx]
                                val answered = selectedAnswers[itemQ.id] != null
                                val marked = itemQ.id in markedForReview
                                val bg = when {
                                    idx == currentIndex -> BrandOrange
                                    marked -> BrandPurple
                                    answered -> BrandGreen
                                    else -> SoftGreySurface
                                }
                                val fg = if (idx == currentIndex || marked || answered) PureWhite else SlateInk
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(bg)
                                        .clickable { currentIndex = idx },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("${idx + 1}", color = fg, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Question ${currentIndex + 1} of ${questions.size}", style = MaterialTheme.typography.labelLarge, color = BrandBlue)
                                TextButton(onClick = {
                                    if (isMarked) markedForReview.remove(q.id) else markedForReview.add(q.id)
                                }) {
                                    Icon(
                                        if (isMarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                                        contentDescription = null,
                                        tint = BrandPurple
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(if (isMarked) "Marked" else "Mark for Review", color = BrandPurple)
                                }
                            }

                            Spacer(Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = MaterialTheme.colorScheme.surface,
                                tonalElevation = 2.dp,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = q.questionText,
                                    style = MaterialTheme.typography.titleMedium.copy(fontFamily = BengaliFontFamily),
                                    modifier = Modifier.padding(16.dp)
                                )
                            }

                            Spacer(Modifier.height(14.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                options.forEachIndexed { optIdx, text ->
                                    val selected = selectedAnswers[q.id] == optIdx
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = if (selected) BrandOrangeSoft else MaterialTheme.colorScheme.surface,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(
                                                width = if (selected) 2.dp else 1.dp,
                                                color = if (selected) BrandOrange else MaterialTheme.colorScheme.outline,
                                                shape = RoundedCornerShape(14.dp)
                                            )
                                            .clip(RoundedCornerShape(14.dp))
                                            .clickable { selectedAnswers[q.id] = optIdx }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(14.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(
                                                selected = selected,
                                                onClick = { selectedAnswers[q.id] = optIdx }
                                            )
                                            Spacer(Modifier.width(6.dp))
                                            Text("${('A' + optIdx)}. $text", style = MaterialTheme.typography.bodyLarge)
                                        }
                                    }
                                }
                            }
                        }

                        Surface(tonalElevation = 4.dp, modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                OutlinedButton(
                                    onClick = { if (currentIndex > 0) currentIndex-- },
                                    enabled = currentIndex > 0
                                ) {
                                    Text("Previous")
                                }
                                OutlinedButton(onClick = { selectedAnswers.remove(q.id) }) {
                                    Text("Clear")
                                }
                                if (currentIndex < questions.lastIndex) {
                                    Button(
                                        onClick = { currentIndex++ },
                                        colors = ButtonDefaults.buttonColors(containerColor = BrandOrange)
                                    ) {
                                        Text("Next")
                                    }
                                } else {
                                    Button(
                                        onClick = { triggerSubmit() },
                                        colors = ButtonDefaults.buttonColors(containerColor = BrandGreen)
                                    ) {
                                        Text("Submit Test")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
