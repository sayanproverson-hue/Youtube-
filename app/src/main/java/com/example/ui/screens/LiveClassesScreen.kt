@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.EnrollmentEntity
import com.example.data.local.LiveClassEntity
import com.example.ui.components.EmptyStateBox
import com.example.ui.components.StatusPill
import com.example.ui.components.formatTimestamp
import com.example.ui.theme.*

@Composable
fun LiveClassesScreen(
    liveClasses: List<LiveClassEntity>,
    enrollments: List<EnrollmentEntity>,
    onJoinLiveClass: (LiveClassEntity) -> Unit,
    onToggleReminder: (LiveClassEntity) -> Unit,
    onEnrollRequired: (Long) -> Unit
) {
    var selectedFilter by remember { mutableStateOf("ALL") }
    val enrolledCourseIds = remember(enrollments) { enrollments.map { it.courseId }.toSet() }

    val filtered = remember(liveClasses, selectedFilter) {
        when (selectedFilter) {
            "LIVE" -> liveClasses.filter { it.status == "LIVE" }
            "UPCOMING" -> liveClasses.filter { it.status == "UPCOMING" }
            "COMPLETED" -> liveClasses.filter { it.status == "COMPLETED" }
            else -> liveClasses
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Column {
                    Text("Live Classes & Schedule", fontWeight = FontWeight.Bold)
                    Text(
                        "Real-time interactive streaming classrooms",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("ALL" to "All", "LIVE" to "Live Now", "UPCOMING" to "Upcoming", "COMPLETED" to "Archive")
                        .forEach { (key, label) ->
                            FilterChip(
                                selected = selectedFilter == key,
                                onClick = { selectedFilter = key },
                                label = { Text(label) }
                            )
                        }
                }
            }

            if (filtered.isEmpty()) {
                item {
                    EmptyStateBox(
                        icon = Icons.Filled.LiveTv,
                        title = "No live sessions in this view",
                        subtitle = "Check Upcoming or All tabs to view the complete Target SSB class timetable."
                    )
                }
            } else {
                items(filtered, key = { it.id }) { live ->
                    val isLive = live.status == "LIVE"
                    val isEnrolled = live.courseId in enrolledCourseIds

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 2.dp,
                        shadowElevation = 1.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("live_class_card_${live.id}")
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                StatusPill(
                                    text = when (live.status) {
                                        "LIVE" -> "● LIVE NOW"
                                        "UPCOMING" -> "UPCOMING"
                                        else -> "RECORDED REPLAY"
                                    },
                                    containerColor = when (live.status) {
                                        "LIVE" -> BrandRose
                                        "UPCOMING" -> BrandBlueSoft
                                        else -> BrandGreenSoft
                                    },
                                    contentColor = when (live.status) {
                                        "LIVE" -> PureWhite
                                        "UPCOMING" -> BrandBlue
                                        else -> BrandGreen
                                    }
                                )
                                Text(
                                    text = live.subject,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = BrandPurple,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Spacer(Modifier.height(10.dp))
                            Text(
                                text = live.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontFamily = BengaliFontFamily),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = live.courseTitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(Modifier.height(10.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Person, contentDescription = null, modifier = Modifier.size(16.dp), tint = BrandOrange)
                                Spacer(Modifier.width(4.dp))
                                Text(live.teacherName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                                Spacer(Modifier.width(12.dp))
                                Icon(Icons.Filled.Schedule, contentDescription = null, modifier = Modifier.size(16.dp), tint = BrandBlue)
                                Spacer(Modifier.width(4.dp))
                                Text("${live.durationMinutes} min", style = MaterialTheme.typography.bodySmall)
                            }
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.CalendarToday, contentDescription = null, modifier = Modifier.size(15.dp), tint = SlateMuted)
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    formatTimestamp(live.scheduledAtMillis),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(Modifier.height(14.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (live.status == "UPCOMING") {
                                    OutlinedButton(
                                        onClick = { onToggleReminder(live) },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            if (live.reminderEnabled) Icons.Filled.NotificationsActive else Icons.Filled.NotificationsNone,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text(if (live.reminderEnabled) "Reminder On" else "Notify Me")
                                    }
                                }

                                Button(
                                    onClick = {
                                        if (isEnrolled || isLive) onJoinLiveClass(live)
                                        else onEnrollRequired(live.courseId)
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("join_live_btn_${live.id}"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isLive) BrandRose else BrandOrange
                                    )
                                ) {
                                    Icon(
                                        if (isEnrolled || isLive) Icons.Filled.PlayArrow else Icons.Filled.Lock,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        when {
                                            isLive -> "Join Live Class"
                                            isEnrolled -> "Open Stream"
                                            else -> "Unlock Batch"
                                        }
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
